package com.xwtech.parser;

import org.apache.log4j.Logger;
import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;

import com.xwtech.pojo.Record;
import com.xwtech.store.StoreToHbase;
import com.xwtech.util.Program;

/**
 * Post请求响应内容解析类
 */
public class PostRequestHtmlParser extends Thread {
	private Parser parser;
	private StoreToHbase store;
	private Program program;
	private Logger logger;
	private String content;

	public PostRequestHtmlParser(int noticeType, Program program) {
		this.program = program;
		logger = Logger.getLogger(PostRequestHtmlParser.class);
		store = new StoreToHbase(noticeType);
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Override
	public void run() {
		try {
			parser = new Parser(content);
			logger.info(currentThread().getName() + "开始解析Post请求响应的HTML!,并存储到HBASE中!");
			NodeIterator rootList = parser.elements();
			rootList.nextNode();
			NodeList nodeList = rootList.nextNode().getChildren();
			// System.out.println("===================="+nodeList.size());
			/*
			 * 判断该HTML响应是否有具体的内容，在出错或者到所有数据读取完毕时起效
			 * 如果起效，修改endFlag标志位，停止开启新的线程，结束当前任务！
			 */
			if (nodeList.size() <= 4) {
				program.endFlag = true;
			}
			/*
			 * 找到对应的tag记录，然后解析
			 */
			nodeList.remove(0);
			nodeList.remove(0);
			SimpleNodeIterator childList = nodeList.elements();
			while (childList.hasMoreNodes()) {
				Node node = childList.nextNode();
				if (node.getChildren() != null) {
					toObject(node);
				}
			}
		} catch (Exception e) {
			logger.error(currentThread().getName() + "解析HTML文件出现异常！\n"+e.getMessage()+"\n");
		} finally {
			logger.info(currentThread().getName() + "HTML文件解析结束！");
			store.close();
		}
	}

	private void toObject(Node node) {
		Record record = new Record();
		record.setName(node.getChildren().elementAt(1).toPlainTextString().trim());
		record.setTypeName(node.getChildren().elementAt(3).toPlainTextString().trim());
		record.setContent(node.getChildren().elementAt(5).toPlainTextString().trim());
		record.setDate(node.getChildren().elementAt(7).toPlainTextString().trim());
		String click = node.getText().split("\\s")[4];
		record.setId(click.substring(23, click.length() - 3));
		try {
//			 System.out.println(record.getName()+ "|"
//			 + record.getTypeName()+ "|"
//			 + record.getContent()+ "|"
//			 + record.getDate()+ "|"
//			 + record.getId());
			store.store(record);
		} catch (Exception e) {
			logger.error(currentThread().getName() + "存储到hbase出现错误！\n"+e.getMessage()+"\n");
		}
	}
}
