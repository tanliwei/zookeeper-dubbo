package com.imooc.zk.demo;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

/**
 * @Description: zookeeper 判断阶段是否存在demo
 */
public class ZKNodeExist implements Watcher {

	private ZooKeeper zookeeper = null;
	
	public static final String zkServerPath = "127.0.0.1:2181";
	public static final Integer timeout = 5000;
	
	public ZKNodeExist() {}
	
	public ZKNodeExist(String connectString) {
		try {
			zookeeper = new ZooKeeper(connectString, timeout, new ZKNodeExist());
		} catch (IOException e) {
			e.printStackTrace();
			if (zookeeper != null) {
				try {
					zookeeper.close();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}
	
	private static CountDownLatch countDown = new CountDownLatch(1);
	
	public static void main(String[] args) throws Exception {
	
		ZKNodeExist zkServer = new ZKNodeExist(zkServerPath);
		
		/**
		 * 参数：
		 * path：节点路径
		 * watch：watch
		 */
		Stat stat = zkServer.getZookeeper().exists("/imooc-fake", true);
		if (stat != null) {
			System.out.println("查询的节点版本为dataVersion：" + stat.getVersion());
		} else {
			System.out.println("该节点不存在...");
		}
		
		countDown.await();
	}
	
	@Override
	public void process(WatchedEvent event) {
		if (event.getType() == EventType.NodeCreated) {
			System.out.println("节点创建");
			countDown.countDown();
		} else if (event.getType() == EventType.NodeDataChanged) {
			System.out.println("节点数据改变");
			Stat stat = new Stat();
			byte[] resByte = new byte[0];
			try {
				ZKGetNodeData zkServer = new ZKGetNodeData(zkServerPath);
				 resByte = zkServer.getZookeeper().getData("/imooc-fake", true, stat);
			} catch (KeeperException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			String result = new String(resByte);
			System.out.println("更改后的值:" + result);
			System.out.println("版本号变化dversion：" + stat.getVersion());
			countDown.countDown();
		} else if (event.getType() == EventType.NodeDeleted) {
			System.out.println("节点删除");
			countDown.countDown();
		}
	}
	
	public ZooKeeper getZookeeper() {
		return zookeeper;
	}
	public void setZookeeper(ZooKeeper zookeeper) {
		this.zookeeper = zookeeper;
	}
}

