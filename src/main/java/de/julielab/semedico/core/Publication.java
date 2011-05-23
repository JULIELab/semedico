package de.julielab.semedico.core;

import java.util.Date;

public class Publication {

	private String title;
	private String volume;
	private String issue;
	private String pages;
	private Date date;
	
	public Publication(String title, String volume, String issue, String pages, Date date) {
		this.title = title;
		this.volume= volume;
		this.issue = issue;
		this.pages = pages;
		this.date = date;
	}
	
	public Publication() {}
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getVolume() {
		return volume;
	}
	public void setVolume(String volume) {
		this.volume = volume;
	}
	public String getIssue() {
		return issue;
	}
	public void setIssue(String issue) {
		this.issue = issue;
	}
	public String getPages() {
		return pages;
	}
	public void setPages(String pages) {
		this.pages = pages;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
}
