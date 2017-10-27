package de.julielab.semedico.core.search.query;

import java.util.List;

public class EventBoolElement extends PrimitiveBoolElement {
	private List<String> eventType;
	private BoolElement arg1;
	private BoolElement arg2;

	public EventBoolElement(List<String> eventTypeIds, BoolElement arg1, BoolElement arg2) {
		super(false);
		this.eventType = eventTypeIds;
		this.arg1 = arg1;
		this.arg2 = arg2;
	}

	public List<String> getEventType() {
		return eventType;
	}

	@Override
	public String toString() {
		String eventTypeString = eventType.size() == 1 ? eventType.get(0) : eventType.toString();
		String arg1String = "";
		if (null != arg1)
			arg1String = arg1.getClass().equals(ComplexBoolElement.class) ? "(" + arg1.toString() + ")" : arg1
					.toString();

		String arg2String = "";
		if (null != arg2)
			arg2String = arg2.getClass().equals(ComplexBoolElement.class) ? "(" + arg2.toString() + ")" : arg2
					.toString();
		String ret = arg1String + " " + eventTypeString + " " + arg2String;
		ret = ret.trim();
		if (!negated)
			return ret;
		return "NOT " + ret;
	}

	public BoolElement getArg1() {
		return arg1;
	}

	public BoolElement getArg2() {
		return arg2;
	}

}
