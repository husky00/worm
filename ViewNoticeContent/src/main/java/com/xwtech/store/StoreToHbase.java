package com.xwtech.store;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.Admin;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.Get;
import org.apache.hadoop.hbase.client.Put;
import org.apache.hadoop.hbase.client.Table;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.Logger;

import com.xwtech.pojo.ExtendCandidate;
import com.xwtech.pojo.Record;
import com.xwtech.request.GetRequest;

public class StoreToHbase {
	private String firstFamily;
	private String secondFamily;
	private String nameSpaceName;
	private String tableName;
	private int noticeType;

	private Connection conn;
	private Admin admin;
	private Table table;

	private Logger logger;

	private GetRequest getRequest;

	public StoreToHbase(int noticeType) {
		logger = Logger.getLogger(StoreToHbase.class);
		this.noticeType = noticeType;
		/*
		 * 初始化HBase table connection相关信息
		 */

		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(
					"/home/xwtech/configuration/configuration.properties"));
			this.nameSpaceName = properties.getProperty("hbaseNamespaceName", "ns1");
			this.tableName = properties.getProperty("hbaseTableBaseName", "test") + noticeType;
			this.firstFamily = properties.getProperty("hbaseTableFirstFamilyName","baseInfo");
			this.secondFamily = properties.getProperty("hbaseTableSecondFamilyName","extendInfo");
		} catch (Exception e1) {
			logger.error("读取properties文件失败！\n" + e1.getMessage()+"\n");
		}

		/*
		 * 开始初始化connection
		 */
		try {
			init();
		} catch (Exception e) {
			logger.error("HBASE连接初始化失败！\n" + e.getMessage() + "\n");
		}

		getRequest = new GetRequest();
	}

	/*
	 * HBASE connection初始化
	 */
	public void init() throws Exception {
		conn = HbaseConnectionWarehouse.getWarehouse().getConnection();
		admin = conn.getAdmin();
		boolean tableExists = admin.tableExists(TableName.valueOf(nameSpaceName + ":" + tableName));
		if (!tableExists) {
			createTable();
		}
	}

	public void createTable() throws Exception {

		HColumnDescriptor family1 = new HColumnDescriptor(firstFamily);
		HColumnDescriptor family2 = new HColumnDescriptor(secondFamily);
		family1.setMaxVersions(3);
		family2.setMaxVersions(3);

		HTableDescriptor descriptor = new HTableDescriptor(TableName.valueOf(nameSpaceName + ":" + tableName));
		descriptor.addFamily(family1);
		descriptor.addFamily(family2);
		descriptor.setRegionReplication(3); // replication
		admin.createTable(descriptor);
		// admin.split(TableName.valueOf("StudentInfo:student1"),
		// Bytes.toBytes("10"));
		// admin.split(TableName.valueOf("StudentInfo:student1"),
		// Bytes.toBytes("20"));
		// admin.split(TableName.valueOf("StudentInfo:student1"),
		// Bytes.toBytes("30"));
		// admin.split(TableName.valueOf("StudentInfo:student1"),
		// Bytes.toBytes("40"));
		// admin.split(TableName.valueOf("StudentInfo:student1"),
		// Bytes.toBytes("50"));
		// admin.split(TableName.valueOf("StudentInfo:student1"),
		// Bytes.toBytes("60"));
	}

	/*
	 * 存储record实现______一条
	 */
	public void store(Record record) throws Exception {
		table = conn.getTable(TableName.valueOf(nameSpaceName + ":" + tableName));
		Get get = new Get(Bytes.toBytes(record.getId().hashCode() + "" + record.getDate().hashCode()));
		if (table.get(get) == null) {

			Put put = new Put(Bytes.toBytes(record.getId().hashCode() + "" + record.getDate().hashCode())); // row
																											// key
			/*
			 * 7 结果公示类型——读取二级页面
			 */
			if (noticeType == 7) {
				ExtendCandidate candidate = getRequest.getData(record.getId());
				if (candidate != null) {
					put.addColumn(Bytes.toBytes(secondFamily), Bytes.toBytes("title"),
							Bytes.toBytes(candidate.getTitle()));
					put.addColumn(Bytes.toBytes(secondFamily), Bytes.toBytes("content"),
							Bytes.toBytes(candidate.getContent()));
				}
			}

			put.addColumn(Bytes.toBytes(firstFamily), Bytes.toBytes("name"), Bytes.toBytes(record.getName()));
			put.addColumn(Bytes.toBytes(firstFamily), Bytes.toBytes("type"), Bytes.toBytes(record.getTypeName()));
			put.addColumn(Bytes.toBytes(firstFamily), Bytes.toBytes("content"), Bytes.toBytes(record.getContent()));
			put.addColumn(Bytes.toBytes(firstFamily), Bytes.toBytes("date"), Bytes.toBytes(record.getDate()));
			put.addColumn(Bytes.toBytes(firstFamily), Bytes.toBytes("id"), Bytes.toBytes(record.getId()));

			table.put(put);
		}
	}

	public void close() {
		logger.info(Thread.currentThread().getName() + " HBASE 连接关闭！");
		if (table != null) {
			try {
				table.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (admin != null) {
			try {
				admin.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// 放回HBase连接池
		HbaseConnectionWarehouse.getWarehouse().closeConnection(conn);
	}
}
