package com.xwtech.pojo;

/**
 * 
 * @author husky
 *
 */
public class Record {
	private String name;
	private String id;
	private String date;
	private String content;
	private String typeName;
	
	public Record(String name, String id, String date, String content, String typeName) {
		this.name = name;
		this.id = id;
		this.date = date;
		this.content = content;
		this.typeName = typeName;
	}
	public Record() {
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
}
