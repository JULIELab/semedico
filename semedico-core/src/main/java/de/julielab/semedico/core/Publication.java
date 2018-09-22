package de.julielab.semedico.core;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;

public class Publication
{
	private String title;
	private String volume;
	private String issue;
	private String pages;
	private Date date;
	private boolean dateComplete = true;

	public boolean isComplete()
	{
		return !StringUtils.isBlank(title)
				&& !StringUtils.isBlank(volume)
				&& !StringUtils.isBlank(issue)
				&& !StringUtils.isBlank(pages);
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getVolume()
	{
		return volume;
	}

	public void setVolume(String volume)
	{
		this.volume = volume;
	}

	public String getIssue()
	{
		return issue;
	}

	public void setIssue(String issue)
	{
		this.issue = issue;
	}

	public String getPages()
	{
		return pages;
	}

	public void setPages(String pages)
	{
		this.pages = pages;
	}

	public Date getDate()
	{
		return date;
	}

	public void setDate(Date date)
	{
		this.date = date;
	}

	public boolean isDateComplete()
	{
		return dateComplete;
	}

	public void setDateComplete(boolean dateComplete)
	{
		this.dateComplete = dateComplete;
	}
}