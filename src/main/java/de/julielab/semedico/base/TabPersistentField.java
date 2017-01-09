package de.julielab.semedico.base;

import static org.apache.tapestry5.ioc.internal.util.CollectionFactory.newList;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.tapestry5.internal.services.PersistentFieldChangeImpl;
import org.apache.tapestry5.ioc.internal.util.InternalUtils;
import org.apache.tapestry5.services.ApplicationStateManager;
import org.apache.tapestry5.services.PersistentFieldChange;
import org.apache.tapestry5.services.PersistentFieldStrategy;

import de.julielab.semedico.state.SemedicoSessionState;

public class TabPersistentField implements PersistentFieldStrategy {
	public static final String TAB = "tab";
	private ApplicationStateManager asm;

	public TabPersistentField(ApplicationStateManager asm) {
		this.asm = asm;

	}

	@Override
	public Collection<PersistentFieldChange> gatherFieldChanges(String pageName) {
		SemedicoSessionState sessionState = asm.getIfExists(SemedicoSessionState.class);

		if (sessionState == null)
			return Collections.emptyList();

		List<PersistentFieldChange> result = newList();

		int activeTab = sessionState.getActiveTabIndex();

		String fullPrefix = TAB + activeTab + ":" + pageName + ":";

		for (String name : sessionState.getAttributeNames(fullPrefix)) {
			Object persistedValue = sessionState.getAttribute(name);

			PersistentFieldChange change = buildChange(name, persistedValue);

			result.add(change);
		}

		return result;
	}

	@Override
	public void discardChanges(String pageName) {
		SemedicoSessionState sessionState = asm.getIfExists(SemedicoSessionState.class);

		if (sessionState == null)
			return;

		int activeTab = sessionState.getActiveTabIndex();

		String fullPrefix = TAB + activeTab + ":" + pageName + ":";

		for (String name : sessionState.getAttributeNames(fullPrefix)) {
			sessionState.removeAttribute(name);
		}
	}

	@Override
	public void postChange(String pageName, String componentId, String fieldName, Object newValue) {
		SemedicoSessionState sessionState = asm.getIfExists(SemedicoSessionState.class);

		assert InternalUtils.isNonBlank(pageName);
		assert InternalUtils.isNonBlank(fieldName);
		Object persistedValue = newValue;

		StringBuilder builder = new StringBuilder(TAB);
		if (sessionState != null)
			builder.append(sessionState.getActiveTabIndex());
		builder.append(":");
		builder.append(pageName);

		builder.append(':');

		if (componentId != null)
			builder.append(componentId);

		builder.append(':');
		builder.append(fieldName);

		if (sessionState != null) {
			sessionState.setAttribute(builder.toString(), persistedValue);
		}
	}

	private PersistentFieldChange buildChange(String name, Object newValue) {
		String[] chunks = name.split(":");

		// Will be empty string for the root component
		String componentId = chunks[2];
		String fieldName = chunks[3];

		return new PersistentFieldChangeImpl(componentId, fieldName, newValue);
	}

}
