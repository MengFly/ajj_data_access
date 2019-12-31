package com.akxy.util;

import java.util.List;
import java.util.concurrent.CountDownLatch;

public class GroupInsertThread implements Runnable {
	private List<String> list;
	private CountDownLatch begin;
	private CountDownLatch end; // 创建个构造函数初始化 list,和其他用到的参数

	public GroupInsertThread(List<String> list, CountDownLatch begin, CountDownLatch end) {
		this.list = list;
		this.begin = begin;
		this.end = end;
	}

	@Override
	public void run() {
		try {
			for (int i = 0; i < list.size(); i++) { 
				// 这里还要说一下，，由于在实质项目中，当处理的数据存在等待超时和出错会使线程一直处于等待状态
				// 这里只是处理简单的，
				// 分批 批量插入
				System.out.println(list.get(i));
			} 
			// 执行完让线程直接进入等待
			begin.await();
		} catch (InterruptedException e) { // TODO Auto-generated catch block
			e.printStackTrace();
		} finally { // 这里要主要了，当一个线程执行完 了计数要减一不然这个线程会被一直挂起
			// ，end.countDown()，这个方法就是直接把计数器减一的
			end.countDown();
		}
	}
}
