package com.xwtech.util;

import com.xwtech.request.PostRequest;

public class Program {

	public boolean endFlag = false;

	public static void main(String[] args) throws InterruptedException {

//		Program p1 = new Program();
//		p1.start(Type.supplier);
		
//		采购公告
		Program p2 = new Program();
		p2.start(Type.procurement);
		
		//结果公示
		Program p7 = new Program();
		p7.start(Type.result_publicity);
		
//		Program p3 = new Program();
//		p3.start(Type.single_source_procurement);
	}

	public void start(int type) {
		int i = 0;
		while (!this.endFlag) {
			// int noticeType,int perPageSize,int currentPage
			PostRequest t = new PostRequest(type, 20, i++, this);
			t.start();
		}
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println("OK!!!");
	}
}
