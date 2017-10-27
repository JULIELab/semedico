package de.julielab.semedico.core.search.results;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import org.slf4j.Logger;

import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.elastic.query.services.ISearchServerResponse;

public abstract class SearchResultCollector<R extends SemedicoSearchResult> {
	private Object name;
	private R result;

	public SearchResultCollector(Object name) {
		this.name = name;

	}

	public Object getName() {
		return name;
	}

	public void setResult(R result) {
		this.result = result;
	}

	public R getResult() {
		return result;
	}

	public abstract R collectResult(SearchCarrier carrier, ISearchServerResponse searchServerResponse);
	
	private BiFunction<Object, String, String> notNull = (o, m) -> o == null ? m + " is null." : null;
	private BiFunction<Collection<?>, String, String> notEmpty = (o, m) -> o.isEmpty() ? m + " is empty." : null;

	/**
	 * Error messages for state checks.
	 */
	private List<String> errorMessages = new ArrayList<>();

	/**
	 * Checks for null objects and, if found, generates an error message. For
	 * this purpose, the odd-indexed elements must be strings giving a name to
	 * the previous object.
	 * 
	 * @param objects
	 *            A list of pairs where the even-indexed elements are
	 *            {@link Supplier} that provide the object for the null check
	 *            and odd-indexed elements are their names.
	 */
	protected void checkNotNull(Object... objects) {
		if (objects.length % 2 == 1)
			throw new IllegalArgumentException(
					"An even number of arguments is required. The even elements are the objects to test for null, the odd arguments are their names.");
		for (int i = 0; i < objects.length; i++) {
			Object object = objects[i];
			if (i % 2 == 1) {
				if (!(object instanceof CharSequence))
					throw new IllegalArgumentException(
							"All odd arguments must be names describing the previous object but was of class "
									+ object.getClass().getCanonicalName() + ".");
				String returnMessage = notNull.apply(((Supplier<?>) objects[i - 1]).get(), (String) object);
				if (returnMessage != null)
					errorMessages.add(returnMessage);
			}
		}
	}

	/**
	 * Checks for empty collections and, if found, generates an error message.
	 * For this purpose, the odd-indexed elements must be strings giving a name
	 * to the previous object.
	 * 
	 * @param objects
	 *            A list of pair where the even-indexed elements are collections
	 *            for the empty check and odd-indexed elements are their names.
	 */
	protected void checkNotEmpty(Collection<?>... objects) {
		if (objects.length % 2 == 1)
			throw new IllegalArgumentException(
					"An even number of arguments is required. The even elements are the objects to test for null, the odd arguments are their names.");
		for (int i = 0; i < objects.length; i++) {
			Object object = objects[i];
			if (i % 2 == 1) {
				if (!(object instanceof CharSequence))
					throw new IllegalArgumentException(
							"All odd arguments must be names describing the previous object but was of class "
									+ object.getClass().getCanonicalName() + ".");
				String returnMessage = notEmpty.apply(objects[i - 1], (String) object);
				if (returnMessage != null)
					errorMessages.add(returnMessage);
			}
		}
	}

	/**
	 * Checks if there are error messages created by
	 * {@link #checkNotNull(Object...)} and
	 * {@link #checkNotEmpty(Collection...)} which need to be called before this
	 * method. If there is at least one error message, the message is logged on
	 * the ERROR level and an exception is thrown.
	 * 
	 * @throws IllegalArgumentException
	 *             If there was at least one error.
	 */
	protected void stopIfError() {
		if (errorMessages.isEmpty())
			return;
		errorMessages.forEach(System.out::println);
		throw new IllegalArgumentException("There was at least one failed precondition check for the component "
				+ getClass().getSimpleName() + ". Check the logs above.");
	}

	@SuppressWarnings("unchecked")
	protected <T extends SearchCarrier> T castCarrier(SearchCarrier carrier) {
		return (T) carrier;
	}
}
