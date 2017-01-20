package com.xwtech.pojo;

/**
 * 二级页面
 * @author husky
 *
 */
public class ExtendCandidate implements Candidate {
	private String title;
	private String content;
	
	public ExtendCandidate() {
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
}
