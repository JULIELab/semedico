package de.julielab.semedico.core.search.components;

import java.util.ArrayList;
import java.util.List;

public class EventQuery {

	public List<String> firstArguments;
	public List<String> secondArguments;
	public List<String> eventTypes;
	public List<String> likelihoods;
	public boolean isBinaryEvent;

	public EventQuery() {}
	
	public EventQuery(List<String> firstArguments,
			List<String> secondArguments, List<String> eventTypes,
			List<String> likelihoods, boolean isBinaryEvent) {
		this.firstArguments = firstArguments;
		this.secondArguments = secondArguments;
		this.eventTypes = eventTypes;
		this.likelihoods = likelihoods;
		this.isBinaryEvent = isBinaryEvent;
	}
	
	public List<String> getFirstArguments() {
		return firstArguments;
	}

	public void setFirstArguments(List<String> firstArguments) {
		this.firstArguments = firstArguments;
	}

	public List<String> getSecondArguments() {
		return secondArguments;
	}

	public void setSecondArguments(List<String> secondArguments) {
		this.secondArguments = secondArguments;
	}

	public List<String> getEventTypes() {
		return eventTypes;
	}

	public void setEventTypes(List<String> eventTypes) {
		this.eventTypes = eventTypes;
	}

	public List<String> getLikelihoods() {
		return likelihoods;
	}

	public void setLikelihoods(ArrayList<String> likelihoods) {
		this.likelihoods = likelihoods;
	}

	public boolean isBinaryEvent() {
		return isBinaryEvent;
	}

	public void setBinaryEvent(boolean isBinaryEvent) {
		this.isBinaryEvent = isBinaryEvent;
	}

	@Override
	public String toString() {
		return "EventQuery [firstArguments=" + firstArguments
				+ ", secondArguments=" + secondArguments + ", eventTypes="
				+ eventTypes + ", likelihoods=" + likelihoods
				+ ", isBinaryEvent=" + isBinaryEvent + "]";
	}
	
	
}
