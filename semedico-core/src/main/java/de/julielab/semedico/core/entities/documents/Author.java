package de.julielab.semedico.core.entities.documents;

public class Author
{
	private String firstname;
	private String firstnameForPrint;
	private String lastname;
	private String affiliation;
	
	public Author(String firstname, String lastname, String affiliation)
	{
		this.firstname = firstname;
		this.lastname = lastname;
		this.affiliation = affiliation;
		setFirstnameForPrint();
	}
	
	/**
	 * 
	 */
	private void setFirstnameForPrint()
	{
		if (firstname == null)
		{
			return;
		}
		
		String[] split = firstname.split("\\s+");
		String withoutWS = "";
		boolean initialsOnly = true;
		
		for (String name : split)
		{
			if (name.length() > 1)
			{
				initialsOnly = false;
			}
			withoutWS += name;
		}
		
		if (initialsOnly)
		{
			firstnameForPrint = withoutWS;
		}
		else
		{
			firstnameForPrint = firstname;
		}
	}

	public Author() {}
	
	public String getFirstname()
	{
		return firstname;
	}
	
	public void setForename(String firstname)
	{
		this.firstname = firstname;
	}
	
	public String getLastname()
	{
		return lastname;
	}
	
	public void setLastname(String lastname)
	{
		this.lastname = lastname;
	}
	
	public String getAffiliation() 
	{
		return affiliation;
	}
	
	public void setAffiliation(String affiliation)
	{
		this.affiliation = affiliation;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		if (firstnameForPrint == null)
		{
			setFirstnameForPrint();
		}
	
		return lastname + " " + firstnameForPrint;
	}
	
}
