package de.julielab.semedico.core.services;

import org.apache.tapestry5.ioc.services.SymbolSource;

public class ServiceFeature {
	private String name;

	private Object value;

	public ServiceFeature(String name, Object value) {
		this.name = name;
		this.value = value;
	}
	
	public ServiceFeature(String name, SymbolSource symbolSource) {
		this(name, symbolSource.valueForSymbol(name));
	}

	public String getName() {
		return name;
	}

	public String getStringValue() {
		return (String) value;
	}

	public int getIntValue() {
		if (value.getClass().isPrimitive()) {
			return (int) value;
		} else if (value instanceof Integer) {
			return (Integer) value;
		} else if (value instanceof String) {
			return Integer.parseInt((String) value);
		}
		throw new IllegalArgumentException("The service feature value " + value + " is not an integer.");
	}
	
	public boolean getBooleanValue() {
		if (value.getClass().isPrimitive()) {
			return (boolean) value;
		} else if (value instanceof Boolean) {
			return (Boolean) value;
		} else if (value instanceof String) {
			return Boolean.parseBoolean((String) value);
		}
		throw new IllegalArgumentException("The service feature value " + value + " is not a boolean.");
	}
}
