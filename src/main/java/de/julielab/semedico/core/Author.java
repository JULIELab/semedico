package de.julielab.semedico.core;

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
	
	public String getFirstname() {
		return firstname;
	}
	public void setForename(String firstname) {
		this.firstname = firstname;
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
