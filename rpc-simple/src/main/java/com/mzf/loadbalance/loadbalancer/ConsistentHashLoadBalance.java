package com.mzf.loadbalance.loadbalancer;

import com.mzf.loadbalance.AbstractLoadBalance;
import com.mzf.remoting.dto.RpcRequest;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 一致性hash负载均衡(参考 d u b b o)
 *
 * @author 好大雨
 * @create 2021/9/19 23:47
 */
@Slf4j
public class ConsistentHashLoadBalance extends AbstractLoadBalance {
    private final ConcurrentHashMap<String, ConsistentHashSelector> selectors = new ConcurrentHashMap<>();

    @Override
    protected String doSelect(List<String> serviceAddresses, RpcRequest rpcRequest) {
        int identityHashCode = System.identityHashCode(serviceAddresses);
        // build rpc service name by rpcRequest
        String rpcServiceName = rpcRequest.getRpcServiceName();
        ConsistentHashSelector selector = selectors.get(rpcServiceName);
        //当selector为空，说明这个key对应的服务方法还没有调用过
        //如果selector.identityHashCode != identityHashCode说明serviceAddresses发生了动态变化
        //都需要重构ConsistentHashSelector
        if (selector == null || selector.identityHashCode != identityHashCode) {
            selectors.put(rpcServiceName, new ConsistentHashSelector(serviceAddresses, 160, identityHashCode));
            selector = selectors.get(rpcServiceName);
        }
        return selector.select(rpcServiceName + Arrays.stream(rpcRequest.getParameters()));
    }

    /**
     * 初始化服务选择管理器类
     */
    static class ConsistentHashSelector {
        private final TreeMap<Long, String> virtualInvokers;

        private final int identityHashCode;

        ConsistentHashSelector(List<String> invokers, int replicaNumber, int identityHashCode) {
            this.virtualInvokers = new TreeMap<>();
            this.identityHashCode = identityHashCode;
            //构建虚拟节点管理器：将每个invoker的节点细分为n/4份，每份 散列到TreeMap中
            for (String invoker : invokers) {
                for (int i = 0; i < replicaNumber / 4; i++) {
                    //md5：每4个节点生成一份消息摘要，16字节128位
                    byte[] digest = md5(invoker + i);
                    //128分为四分，作为long的低32位，二次哈希，得到的m作为key
                    for (int h = 0; h < 4; h++) {
                        long m = hash(digest, h);
                        //存到虚拟节点中
                        virtualInvokers.put(m, invoker);
                    }
                }
            }
    }

        static byte[] md5(String key) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                md.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }

            return md.digest();
        }

        public String select(String rpcServiceKey) {
            byte[] digest = md5(rpcServiceKey);
            return selectForKey(hash(digest, 0));
        }

        /**
         * 根据hash值在红黑树上面找到小于hash值的第一个serviceAddresses
         * @param hash
         * @return
         */
        private String selectForKey(long hash) {
            Map.Entry<Long, String> entry = virtualInvokers.tailMap(hash, true).firstEntry();

            if (entry == null) {
                entry = virtualInvokers.firstEntry();
            }

            return entry.getValue();
        }
    }
        static long hash(byte[] digest, int idx){
            return ((long) (digest[3 + idx * 4] & 255) << 24
                    | (long) (digest[2 + idx * 4] & 255) << 16
                    | (long) (digest[1 + idx * 4] & 255) << 8
                    | (long) (digest[idx * 4] & 255))
                    & 4294967295L;
        }
    }
