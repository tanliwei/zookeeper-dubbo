package com.imooc.curator.utils;

import org.apache.curator.framework.CuratorFramework;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZKCurator {
    private CuratorFramework client = null;
    final static Logger log = LoggerFactory.getLogger(ZKCurator.class);
    
    public ZKCurator(CuratorFramework client){
        this.client = client;
    }
    
    public void init(){
        //使用命名空间
        client = client.usingNamespace("zk-curator-connector");
    }
    
    public boolean isZKAlive(){
        return client.isStarted();
    }
}
