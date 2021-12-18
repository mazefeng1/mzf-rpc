package com.mzf.registry.utils;

import com.mzf.enums.RpcConfigEnum;
import com.mzf.utils.PropertiesFileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Curator工具类(zookeeper客户端框架)
 *
 * @author 好大雨
 * @create 2021/9/19 17:35
 */
@Slf4j
public final class CuratorUtils {


    public static final String ZK_REGISTER_ROOT_PATH = "/my-rpc";
    private static final Set<String> REGISTERED_PATH_SET = ConcurrentHashMap.newKeySet();
    private static final Map<String, List<String>> SERVICE_ADDRESS_MAP = new ConcurrentHashMap<>();
    private static final int BASE_SLEEP_TIME = 1000;
    private static final int MAX_RETRIES = 3;
    private static CuratorFramework zkClient;
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "192.168.188.100:2181";

    public CuratorUtils() {
    }

    /**
     * 创建一个永久性节点
     * @param zkClient
     * @param path
     */
    public static void createPersistentNode(CuratorFramework zkClient, String path) {
        try {
            if(REGISTERED_PATH_SET.contains(path) ||zkClient.checkExists().forPath(path)!=null){
                log.info("节点[{}]已存在。",path);
            }else{
                //eg: /my-rpc/com.mzf.HelloService/127.0.0.1:9999
                zkClient.create()
                        .creatingParentContainersIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .forPath(path);
                log.info("节点[{}]创建成功。",path);
            }
            REGISTERED_PATH_SET.add(path);
        } catch (Exception e) {
            log.error("节点[{}]创建失败！",path);
        }
    }

    /**
     *  获取子节点
     * @param zkClient
     * @param rpcServiceName
     * @return List<String>
     */
    public static List<String> getChildrenNodes(CuratorFramework zkClient, String rpcServiceName) {
        //map中key存在
        if (SERVICE_ADDRESS_MAP.containsKey(rpcServiceName)) {
            return SERVICE_ADDRESS_MAP.get(rpcServiceName);
        }
        List<String> result = null;
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        try {
            result = zkClient.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, result);
            registerWatcher(rpcServiceName, zkClient);
        } catch (Exception e) {
            log.error("获取子节点[{}]失败！", servicePath);
        }
        return result;
    }

    /**
     *  寄存器来监听指定节点的变化
     * @param rpcServiceName
     * @param zkClient
     */
    private static void registerWatcher(String rpcServiceName, CuratorFramework zkClient) throws Exception {
        String servicePath = ZK_REGISTER_ROOT_PATH + "/" + rpcServiceName;
        PathChildrenCache pathChildrenCache = new PathChildrenCache(zkClient, servicePath, true);
        PathChildrenCacheListener pathChildrenCacheListener = (curatorFramework, pathChildrenCacheEvent) -> {
            List<String> serviceAddresses = curatorFramework.getChildren().forPath(servicePath);
            SERVICE_ADDRESS_MAP.put(rpcServiceName, serviceAddresses);
        };
        pathChildrenCache.getListenable().addListener(pathChildrenCacheListener);
        pathChildrenCache.start();
    }

    /**
     * 清空 数据注册表
     * @param zkClient
     * @param inetSocketAddress
     */
    public static void clearRegistry(CuratorFramework zkClient, InetSocketAddress inetSocketAddress) {
        REGISTERED_PATH_SET.stream().parallel().forEach(p -> {
            try {
                if (p.endsWith(inetSocketAddress.toString())) {
                    zkClient.delete().forPath(p);
                }
            } catch (Exception e) {
                log.error("清空数据[{}] 失败！", p);
            }
        });
        log.info("所有注册的服务已清空:[{}]", REGISTERED_PATH_SET.toString());
    }

    public static CuratorFramework getZkClient() {
        Properties properties = PropertiesFileUtil.readPropertiesFile(RpcConfigEnum.RPC_CONFIG_PATH.getPropertyValue());
        String zookeeperAddress = properties != null && properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue()) != null ?
                properties.getProperty(RpcConfigEnum.ZK_ADDRESS.getPropertyValue()) : 
                DEFAULT_ZOOKEEPER_ADDRESS;
        // zkClient是否启动
        if (zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED) {
            return zkClient;
        }
        // 重试策略。重试3次，将增加两次重试之间的睡眠时间
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(BASE_SLEEP_TIME, MAX_RETRIES);
        zkClient = CuratorFrameworkFactory.builder()
                .connectString(zookeeperAddress)
                .retryPolicy(retryPolicy)
                .build();
        zkClient.start();
        try {
            if (!zkClient.blockUntilConnected(30, TimeUnit.SECONDS)) {
                throw new RuntimeException("Time out waiting to connect to ZK!");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return zkClient;
    }

}
