package de.julielab.stemnet.core;

public class Author {

	private String firstname;
	private String lastname;
	private String affiliation;
	
	public Author(String firstname, String lastname, String affiliation) {
		this.firstname = firstname;
		this.lastname = lastname;
		this.affiliation = affiliation;
	}
	
	public Author() {}
	
	public String getForename() {
		return firstname;
	}
	public void setForename(String forename) {
		this.firstname = forename;
	}
	public String getLastname() {
		return lastname;
	}
	public void setLastname(String lastname) {
		this.lastname = lastname;
	}
	public String getAffiliation() {
		return affiliation;
	}
	public void setAffiliation(String affiliation) {
		this.affiliation = affiliation;
	}
	
}
