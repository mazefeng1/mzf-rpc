## mzf-rpc
RPC框架的实现
- rpc—common : 
    （1）enums：存放枚举类（压缩类型、RPC配置信息路径、错误类型、响应信息、序列化类型）
    （2）exception：自定义异常处理信息
    （3）extension：拓展类加载器加载SPI提供服务的类的接口的注解
    （4）utils：线程池配置与创建工具，配置文件读取工具
- rpc-simple : 核心实现类
    （1）annotation：服务提供者、消费者注解
    （2）config：配置服务名、组号、版本号等服务信息
    （3）loadbalance：负载均衡算法
    （4）provider：服务提供者：发布服务并添加到服务列表，获取其他服务
    （5）proxy：动态代理发送rpc请求（可分为socket、netty异步）
    （6）registry：向zk注册服务、拉取服务，zk工具类
    （7）remoting：消息协议（魔数，序列化编号，消息体长度，解压缩编号），请求响应消息体，RPC请求处理器
    （8）netty：   客户端（存储channel，客户端启动，连接服务器，读事件，超时检测，异常捕获，消息处理）；服务端（处理连接，io处理，写，消息处理）
    （9）serialize： kyro（非线程安全），protostuff           
    （10）spring特性：自定义包扫描，自定义注解扫描，服务者注册服务，消费者动态代理
# 注册中心 - Zookeeper
 

# 网络传输 - Netty

- 优点
1.高性能、异步事件驱动的网络应用框架，提供了一个基于NIO的抽象层，使得开发者可以轻松构建高可扩展性的网络应用。它采用Java的反应器模式来实现，在事件处理方面有着很好的性能表现
2.异步非阻塞：Netty框架基于NIO实现，使用了异步非阻塞的I/O技术，使得在高并发场景下有着更好的性能表现。
3.可扩展性强：Netty框架提供了各种编解码器和处理器，使得它的扩展能力非常强。
4.高度封装和抽象：Netty框架对Java NIO API进行了高度封装和抽象，使得开发者无需关注底层网络I/O实现细节，减少了开发难度和代码量。
5.多种应用场景：Netty框架适用于各种网络协议的通信，如HTTP、WebSocket、TCP等

- 三层架构
网络通信层BootStrap、ServerBootStrap、Channel；
BootStrap：负责客户端启动、连接远程Netty server；
ServerBootStrap：负责服务端的端口的监听；
Channel：负责网络通信的一个载体；

事件调度器EventLoopGroup、EventLoop
EventLoopGroup：线程池，负责接受IO请求并分配线程去处理请求；
EventLoop：相当于线程池里面具体线程；

服务编排层ChannelPipeLine、ChannelHandler、ChannelHandlerContext
ChannelPipeLine：负责处理多个ChannelHandler，将它们构成一条链路去形成Pipeline；
ChannelHandler：IO数据的一个处理器，指定Handler进行处理；
ChannelHandlerContext：保存ChannelHandler上下文信息。

#