package de.julielab.semedico.core.query;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import de.julielab.semedico.core.search.components.EventQuery;

public class InputEventQuery {
	private String firstArgument;
	private String secondArgument;
	private String eventType;
	private String likelihood;
	private boolean isBinaryEvent = true;
	private String secondArgumentText;
	private String firstArgumentText;


	@Override
	public String toString() {
		return "InputEventQuery [firstArgument=" + firstArgument
				+ ", secondArgument="
				+ secondArgument
				+ ", eventType="
				+ eventType
				+ ", likelihood="
				+ likelihood
				+ ", isBinaryEvent="
				+ isBinaryEvent
				+ ", secondArgumentText="
				+ secondArgumentText
				+ ", firstArgumentText="
				+ firstArgumentText
				+ "]";
	}

	public EventQuery asEventQuery() {
		if (StringUtils.isBlank(firstArgument) && StringUtils.isBlank(secondArgument) && StringUtils.isBlank(eventType))
			return null;
		
		List<String> firstArguments = new ArrayList<>();
		List<String> secondArguments = new ArrayList<>();
		List<String> eventTypes = new ArrayList<>();
		List<String> likelihoods = new ArrayList<>();
		if (!StringUtils.isBlank(firstArgument)) {
			firstArguments.add(firstArgument);
		}
		if (!StringUtils.isBlank(secondArgument)) {
			secondArguments.add(secondArgument);
		}
		if (!StringUtils.isBlank(eventType)) {
			eventTypes.add(eventType);
		}
		if (!StringUtils.isBlank(likelihood)) {
			likelihoods.add(likelihood);
		}
		EventQuery eventQuery = new EventQuery(firstArguments, secondArguments, eventTypes, likelihoods, true);
		return eventQuery;
	}

	public String getFirstArgument() {
		return firstArgument;
	}

	public void setFirstArgument(String firstArgument) {
		this.firstArgument = firstArgument;
	}

	public String getSecondArgument() {
		return secondArgument;
	}

	public void setSecondArgument(String secondArgument) {
		this.secondArgument = secondArgument;
	}

	public String getEventType() {
		return eventType;
	}

	public void setEventType(String eventType) {
		this.eventType = eventType;
	}

	public String getLikelihood() {
		return likelihood;
	}

	public void setLikelihood(String likelihood) {
		this.likelihood = likelihood;
	}

	public boolean isBinaryEvent() {
		return isBinaryEvent;
	}

	public void setBinaryEvent(boolean isBinaryEvent) {
		this.isBinaryEvent = isBinaryEvent;
	}
	
	public void setSecondArgumentText(String text) {
		this.secondArgumentText = text;
	}
	
	public String getSecondArgumentText() {
		return secondArgumentText;
	}

	public String getFirstArgumentText() {
		return firstArgumentText;
	}

	public void setFirstArgumentText(String text) {
		this.firstArgumentText = text;
	}
	
}
