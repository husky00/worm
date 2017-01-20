package com.xwtech.store;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.apache.log4j.Logger;


/**
 * 简易HBASE连接池-singleton
 * 使用双重同步锁
 * @author husky
 */
public class HbaseConnectionWarehouse {
	/*
	 * 根据noticeType维护一个connection的MAP 初始判断是否有，如果有直接返回，没有就创建一个返回
	 */
	private static HbaseConnectionWarehouse warehouse;
	
	private Logger logger;
	
	private List<Connection> conns = null;
	private Configuration conf;
	private int maxSize ;					// hbase.connection.maxsize
	private int currentSize;
	private int usableSize;						// 集合中没有使用的连接数量
	private HbaseConnectionWarehouse() {
		conns = new LinkedList<>();
		logger = Logger.getLogger(HbaseConnectionWarehouse.class);  // logger
		conf = HBaseConfiguration.create();							// configuration
		
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream("/home/xwtech/configuration/configuration.properties"));
		} catch (Exception e) {
			logger.error("读取properties文件出错！\n"+e.getMessage());
		} 
		maxSize = Integer.parseInt(properties.getProperty("hbaseConnectionMaxsize", "5"));
		String quorum = properties.getProperty("hbaseZookeeperQuorum", "n1.cluster,n2.cluster,n3.cluster");
		
		conf.set("hbase.zookeeper.quorum", quorum);
		
		currentSize = 0;
		usableSize = 0;
	}

	public static HbaseConnectionWarehouse getWarehouse() { // 对获取实例的方法进行同步
		if (warehouse == null) {
			synchronized (HbaseConnectionWarehouse.class) {
				if (warehouse == null)
					warehouse = new HbaseConnectionWarehouse();
			}
		}
		return warehouse;
	}
	
	/*
	 * 判断连接池里面是否有连接，有则返回第一个连接，没有则创建一个新的连接
	 * 
	 */
	public Connection getConnection() throws IOException {
		Connection resultConn;
		if (usableSize != 0) {
			resultConn = conns.remove(0);
			usableSize --;
		} else if (currentSize < maxSize) {
			resultConn = ConnectionFactory.createConnection(conf);
			currentSize ++;
		} else {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.error("获取HBASE连接出错了！\n"+e.getMessage());
			}
			return this.getConnection();
		}
		return resultConn;
	}
	
	/*
	 * 关闭连接，就是将连接放到连接池里面
	 * 简易HBASE连接池-没有为连接提供代理
	 * 开发使用时需要注意！
	 */
	public void closeConnection(Connection connection) {
		conns.add(connection);
		usableSize ++;
	}
}
