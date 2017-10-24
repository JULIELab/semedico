package de.julielab.semedico.core.search.results;

public abstract class SemedicoSearchResult {
	private long elapsedTime;
	private String errorMessage;
	

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public boolean hasError() {
		return errorMessage != null;
	}

	public void setElapsedTime(long time) {
		elapsedTime = time;
	}

	public long getElapsedTime() {
		return elapsedTime;
	}
}
