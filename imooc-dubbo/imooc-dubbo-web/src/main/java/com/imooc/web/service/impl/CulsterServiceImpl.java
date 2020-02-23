package com.imooc.web.service.impl;

import com.imooc.curator.utils.DistributedLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.imooc.item.service.ItemsService;
import com.imooc.order.service.OrdersService;
import com.imooc.web.service.CulsterService;

@Service("buyService")
public class CulsterServiceImpl implements CulsterService {
	
	final static Logger log = LoggerFactory.getLogger(CulsterServiceImpl.class);
	
	@Autowired
	private ItemsService itemService;

	@Autowired
	private OrdersService ordersService;
	
	@Autowired
	private DistributedLock distributedLock;
	
	@Override
	public void doBuyItem(String itemId) {
		// 减少库存
		itemService.displayReduceCounts(itemId, 1);
		
		// 创建订单
		ordersService.createOrder(itemId);
	}
	
	@Override
	public boolean displayBuy(String itemId) {
		
		try {
			distributedLock.getLock();
			int buyCounts = 5;

			// 1. 判断库存
			int stockCounts = itemService.getItemCounts(itemId);
			if (stockCounts < buyCounts) {
				log.info("still have {}，required {}件，stock insufficient，fail to create order...",
						stockCounts, buyCounts);
				return false;
			}
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// 2. 创建订单
			boolean isOrderCreated = ordersService.createOrder(itemId);

			// 3. 创建订单成功后，扣除库存
			if (isOrderCreated) {
				log.info("Success to create order...");
				itemService.displayReduceCounts(itemId, buyCounts);
			} else {
				log.info("Fail to create order...");
				return false;
			}

			return true;
		}finally {
			distributedLock.releaseLock();
		}
	}
	
}

