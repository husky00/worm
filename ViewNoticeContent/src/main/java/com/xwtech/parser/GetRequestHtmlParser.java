package com.xwtech.parser;

import org.htmlparser.Node;
import org.htmlparser.Parser;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.ParserException;
//import org.htmlparser.util.SimpleNodeIterator;

import com.xwtech.pojo.ExtendCandidate;

/**
 * 二级页面解析器
 * @author husky
 *
 */
public class GetRequestHtmlParser{
	private Parser parser;
	public GetRequestHtmlParser() {

	}
	
	public ExtendCandidate parser(String contentInfo) throws ParserException {
		parser = new Parser(contentInfo);
		
		ExtendCandidate candidate = new ExtendCandidate();;
		try {
			NodeIterator rootList = parser.elements();
			rootList.nextNode();
			Node n = rootList.nextNode();
			// get title
			String title = n.getChildren()
					.elementAt(1).getChildren()
					.elementAt(7).getChildren()
					.elementAt(7).toPlainTextString().trim();
			
			String content = n.getChildren()
				.elementAt(1).getChildren()
				.elementAt(7).getChildren()
				.elementAt(9).getChildren()
				.elementAt(1)
//				.getChildren().elementAt(1)
				.toPlainTextString().replaceAll("&nbsp;", "").trim();
				;
			candidate.setTitle(title);
			candidate.setContent(content);
		} catch (ParserException e) {
			e.printStackTrace();
		} 
//		System.out.println("===============\n"+candidate.getTitle()+"\n"+candidate.getContent()+"\n=============");
		return candidate;
		
	}
	
}
