package com.xwtech.request;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;
import org.htmlparser.util.ParserException;

import com.xwtech.parser.GetRequestHtmlParser;
import com.xwtech.pojo.ExtendCandidate;
/*
 * GET请求类
 */
public class GetRequest {
	private String url = "https://b2b.10086.cn/b2b/main/viewNoticeContent.html?noticeBean.id=";
	private Logger logger;
	private GetRequestHtmlParser parser;
	public GetRequest() {
		logger = Logger.getLogger(GetRequest.class);
		parser = new GetRequestHtmlParser();
	}

	public ExtendCandidate getData(String id) {
		this.url = url + id;
		BufferedReader in = null;
		HttpURLConnection conn = null;
		String result = "";
		try {
			conn = (HttpURLConnection)new URL(url).openConnection();
			// 发送GET请求必须设置如下两行
			conn.setDoInput(true);
			conn.setRequestMethod("GET");
			// flush输出流的缓冲
			in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				result += line;
			}
		} catch (Exception e) {
			logger.error("发送 GET 请求出现异常！\t请求ID:"+id+"\n"+e.getMessage()+"\n");
		} finally {// 使用finally块来关闭输出流、输入流
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				logger.error("关闭数据流出错了！\n"+ex.getMessage()+"\n");
			}
		}
		ExtendCandidate candidate = null ;
		try {
			candidate = parser.parser(result);
		} catch (ParserException e) {
			logger.error("解析二级页面出错了！\n"+e.getMessage()+"\n");
		}
		return candidate;
	}
}
