package com.imooc.zk.demo;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Title: ZKConnectDemo.java
 * @Package com.imooc.zk.demo
 * @Description: zookeeper 连接demo演示
 */
public class ZKConnect implements Watcher {
		
	final static Logger log = LoggerFactory.getLogger(ZKConnect.class);

//	public static final String zkServerPath = "127.0.0.1:2181";
	public static final String zkServerPath = "127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183";
	public static final Integer timeout = 5000;
	
	public static void main(String[] args) throws Exception {
		/**
		 * 客户端和zk服务端链接是一个异步的过程
		 * 当连接成功后后，客户端会收的一个watch通知
		 * 
		 * 参数：
		 * connectString：连接服务器的ip字符串，
		 * 		比如: "192.168.1.1:2181,192.168.1.2:2181,192.168.1.3:2181"
		 * 		可以是一个ip，也可以是多个ip，一个ip代表单机，多个ip代表集群
		 * 		也可以在ip后加路径
		 * sessionTimeout：超时时间，心跳收不到了，那就超时
		 * watcher：通知事件，如果有对应的事件触发，则会收到一个通知；如果不需要，那就设置为null
		 * canBeReadOnly：可读，当这个物理机节点断开后，还是可以读到数据的，只是不能写，
		 * 					       此时数据被读取到的可能是旧数据，此处建议设置为false，不推荐使用
		 * sessionId：会话的id
		 * sessionPasswd：会话密码	当会话丢失后，可以依据 sessionId 和 sessionPasswd 重新获取会话
		 */
		ZooKeeper zk = new ZooKeeper(zkServerPath, timeout, new ZKConnect());
		
		log.warn("客户端开始连接zookeeper服务器...");
		log.warn("连接状态：{}", zk.getState());
		
		new Thread().sleep(2000);
		
		log.warn("连接状态：{}", zk.getState());
		
		/**	new Thread().sleep(2000);
		 * 2020-01-27 19:57:01,943 [main] [com.imooc.zk.demo.ZKConnect.main(ZKConnect.java:43)] - [WARN] 客户端开始连接zookeeper服务器...
		 * 2020-01-27 19:57:01,960 [main] [com.imooc.zk.demo.ZKConnect.main(ZKConnect.java:44)] - [WARN] 连接状态：CONNECTING
		 * 2020-01-27 19:57:01,997 [main-EventThread] [com.imooc.zk.demo.ZKConnect.process(ZKConnect.java:53)] - [WARN] 接受到watch通知：WatchedEvent state:SyncConnected type:None path:null
		 * 2020-01-27 19:57:03,962 [main] [com.imooc.zk.demo.ZKConnect.main(ZKConnect.java:48)] - [WARN] 连接状态：CONNECTED
		 */
		
		/**
		 * 2020-01-27 19:58:15,238 [main] [com.imooc.zk.demo.ZKConnect.main(ZKConnect.java:43)] - [WARN] 客户端开始连接zookeeper服务器...
		 * Disconnected from the target VM, address: '127.0.0.1:63153', transport: 'socket'
		 * 2020-01-27 19:58:15,250 [main] [com.imooc.zk.demo.ZKConnect.main(ZKConnect.java:44)] - [WARN] 连接状态：CONNECTING
		 * 2020-01-27 19:58:15,250 [main] [com.imooc.zk.demo.ZKConnect.main(ZKConnect.java:48)] - [WARN] 连接状态：CONNECTING
		 */
		
	}

	@Override
	public void process(WatchedEvent event) {
		log.warn("接受到watch通知：{}", event);
	}
}

