package de.julielab.semedico.core.search.results;

import de.julielab.elastic.query.components.data.SearchCarrier;
import de.julielab.semedico.core.search.components.data.ISemedicoSearchCarrier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public abstract class SearchResultCollector<C extends ISemedicoSearchCarrier<?, ?>, R extends SemedicoSearchResult> {
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

	public abstract R collectResult(C carrier, int responseIndex);

	private BiFunction<Object, String, String> notNull = (o, m) -> o == null ? m + " is null." : null;
	private BiFunction<Collection<?>, String, String> notEmpty = (o, m) -> o.isEmpty() ? m + " is empty." : null;

	/**
	 * Error messages for state checks.
	 */
	private List<String> errorMessages = new ArrayList<>();

	/**
	 * <p>
	 * Checks for null objects given by {@link Supplier} instances in the even
	 * indices and, if found, generates an error message. For this purpose, the
	 * odd-indexed elements must be strings giving a name to the previous object.
	 * </p>
	 * <p>
	 * Example in a subclass:
	 * 
	 * <pre>
	 * SemedicoESSearchCarrier semCarrier = castCarrier(carrier);
	 * Supplier&lt;ISearchServerResponse&gt; s1 = semCarrier::getSingleSearchServerResponse;
	 * Supplier&lt;Map&lt;String, AggregationRequest&gt;&gt; s2 = () -> semCarrier.serverRequests.get(0).aggregationRequests;
	 * checkNotNull(s1, "Search Server Response", s2, "Aggregation Requests");
	 * stopIfError();
	 * 
	 * ISearchServerResponse response =  s1.get();
	 * Map&lt;String, AggregationRequest&gt; aggregationRequests = s2.get();
	 * ...
	 * </pre>
	 * 
	 * The above example shows a case where two elements from the search carrier are
	 * required, namely the single search server response and an aggregation
	 * request. The two are accessed via suppliers for the reason to push all
	 * exceptions into this method. When not using a supplier, null pointer
	 * exceptions could happen preventing a proper error reporting. Even more so
	 * because all given suppliers are checked for null values and the call to
	 * {@link #stopIfError()} does not only stop in case of an error but also
	 * reports all missing elements at once. This circumvents the situation where
	 * one null error is fixed at a time, just to run the application again and
	 * discover that another value was also null but could not be reported in the
	 * previous application run due to the error thrown by the first null-value.
	 * </p>
	 * 
	 * @param objects
	 *            A list of pairs where the even-indexed elements are
	 *            {@link Supplier} that provide the object for the null check and
	 *            odd-indexed elements are their names.
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
				// TODO here, we could a NPE, right? If the supplier uses objects that are null
				// in its calling chain. Should be caught and given as an error of its own, even
				// though we don't know exactly where the error was.
				String returnMessage = notNull.apply(((Supplier<?>) objects[i - 1]).get(), (String) object);
				if (returnMessage != null)
					errorMessages.add(returnMessage);
			}
		}
	}

	/**
	 * Checks for empty collections at the even indices of the input parameters and,
	 * if found, generates an error message. For this purpose, the odd-indexed
	 * elements must be strings giving a name to the previous object.
	 * 
	 * @param objects
	 *            A list of pair where the even-indexed elements are collections for
	 *            the empty check and odd-indexed elements are their names.
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
	 * {@link #checkNotNull(Object...)} and {@link #checkNotEmpty(Collection...)}
	 * which need to be called before this method. If there is at least one error
	 * message, the message is logged on the ERROR level and an exception is thrown.
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

	/**
	 * Convenience method to cast the carrier into the required type. This is useful
	 * when using a subclass of {@link SearchCarrier}.
	 * 
	 * @param carrier
	 *            The search carrier given.
	 * @return The same search carrier but typecast into the requested subtype.
	 */
	@SuppressWarnings("unchecked")
	protected <T extends SearchCarrier> T castCarrier(SearchCarrier carrier) {
		return (T) carrier;
	}
}
