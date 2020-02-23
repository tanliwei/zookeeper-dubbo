package com.imooc.curator.utils;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;


/**
 * 分布式锁工具类
 */
public class DistributedLock {
    private CuratorFramework client = null;
    final static Logger log = LoggerFactory.getLogger(DistributedLock.class);
    //用于block请求，等待分布式锁释放
    private static CountDownLatch zkLockLatch = new CountDownLatch(1);
    //分布式锁根节点名
    private static final String ZK_LOCK_PROJECT = "imooc-locks";
    //分布式锁业务节点名
    private static final String DISTRIBUTED_LOCK = "distributed_lock";
    
    public DistributedLock(CuratorFramework client){
        this.client = client;
    }
    
    public void init(){
        client = client.usingNamespace("ZKLocks-Namespace");
        /**
         * 创建zk锁的总节点，相当于eclipse的工作空间下的项目
         * 		ZKLocks-Namespace
         * 			|
         * 			 —— imooc-locks
         * 					|
         * 					 —— distributed_lock
         */
        try{
            if (client.checkExists().forPath("/" + ZK_LOCK_PROJECT ) == null){
                client.create()
                        .creatingParentContainersIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                        .forPath("/" + ZK_LOCK_PROJECT);
            }
            //为节点创建watcher事件监听
            addWatchToLock("/" + ZK_LOCK_PROJECT);
        }catch (Exception e){
            
        }
    }

    public void getLock(){
        // 使用死循环，当且仅当上一个锁释放并且当前请求获得锁成功后才会跳出
        while(true){
            try{
                client.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL)
                        .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                        .forPath("/" + ZK_LOCK_PROJECT + "/" + DISTRIBUTED_LOCK);
                log.info("Success to get the distributed lock...");
                return;
            }catch (Exception e){
                log.info("fail to get the distributed lock...");
                try{
                    //获取锁失败 重置同步资源
                    if (zkLockLatch.getCount() <= 0){
                        zkLockLatch = new CountDownLatch(1);
                    }
                    //阻塞线程
                    zkLockLatch.await();
                }catch (InterruptedException ie){
                    ie.printStackTrace();
                }
            }
        }
    }
    
    public boolean releaseLock(){
        try{
            if (client.checkExists().forPath("/"+ZK_LOCK_PROJECT+"/"+DISTRIBUTED_LOCK) != null){
                client.delete().forPath("/" + ZK_LOCK_PROJECT + "/" + DISTRIBUTED_LOCK);
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        log.info("released the lock");
        return true;
    }
    
    private void addWatchToLock(String path) throws Exception {
        final PathChildrenCache cache = new PathChildrenCache(client, path, true);
        cache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        cache.getListenable().addListener(new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
                if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)){
                    String path1 = event.getData().getPath();
                    log.info("The lock has been released or the session has been closed, path:" + path1);
                    if (path1.contains(DISTRIBUTED_LOCK)) {
                        log.info("Released the countDownLatch to try to get the lock");
                        zkLockLatch.countDown();
                    }
                }
            }
        });
    }
}
