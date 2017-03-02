package de.julielab.semedico.core.search.components.data;

public abstract class SemedicoSearchResult {
	private long elapsedTime;

	public void setElapsedTime(long time) {
		elapsedTime = time;
	}

	public long getElapsedTime() {
		return elapsedTime;
	}
}
