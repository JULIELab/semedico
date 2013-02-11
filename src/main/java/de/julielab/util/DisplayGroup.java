package de.julielab.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultiset;

public class DisplayGroup<T> {

	/**
	 * All objects available to this <tt>DisplayGroup</tt>, regardless of any
	 * filtering.
	 */
	private Collection<T> allObjects;
	/**
	 * Objects having passed the filtering, i.e. objects determined for display.
	 */
	private List<T> filteredObjects;
	/**
	 * Just a pointer to either {@link #allObjects} or {@link #filteredObjects},
	 * dependent on whether {@link #filter} is active or not.
	 */
	private List<T> visibleObjects;
	/**
	 * A filter to restrict the list of visible objects. It is used for search
	 * functionality where the user types in a prefix and only those objects are
	 * displayed whose name begins with this prefix, for example.
	 */
	private Filter<T> filter;
	private int batchSize;
	private int firstObjectIndex;
	private int batchNumber;
	private List<Integer> batchIndizes;
	private int firstBatchIndex;
	private static int BATCH_BLOCK_SIZE = 5;
	private boolean[] selectedIndizes;
	private final int defaultBatchSize;
	private Comparator<T> comparator;

	public interface Filter<T> {
		/**
		 * Indicates whether <tt>object</tt> passes the filter and should be
		 * displayed.
		 * 
		 * @param object
		 *            the object being filtered
		 * @return <tt>true</tt> if <tt>object</tt> passes the filter for
		 *         display, <tt>false</tt> otherwise
		 */
		public boolean displayObject(T object);

		public void setFilterToken(String filterToken);

		public String getFilterToken();

		public void reset();

		/**
		 * @return
		 */
		public boolean isFiltering();
	}

	public DisplayGroup(Filter<T> filter, int defaultBatchSize) {
		this(filter, defaultBatchSize, new ArrayList<T>());
	}

	public DisplayGroup(Filter<T> filter, int defaultBatchSize,
			Collection<T> allObjects) {
		this.filter = filter;
		this.defaultBatchSize = defaultBatchSize;
		this.batchSize = this.defaultBatchSize;
		this.allObjects = allObjects;
		this.visibleObjects = new ArrayList<T>();
		this.reset();
	}

	public DisplayGroup(Filter<T> filter, int defaultBatchSize,
			Comparator<T> comparator, boolean keepSorted) {
		this(filter, defaultBatchSize, keepSorted ? TreeMultiset.create(comparator) : new ArrayList<T>());
	}

	public Integer getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
		selectedIndizes = new boolean[batchSize];
	}

	public void scrollBatchUp() {
		if (batchNumber > 1) {
			firstObjectIndex = 0;
			batchNumber = 1;
		} else {
			if (firstObjectIndex > 0)
				firstObjectIndex--;
		}
	}

	public void scrollBatchDown() {
		if (batchNumber > 1) {
			firstObjectIndex = 0;
			batchNumber = 1;
		} else {
			if (firstObjectIndex + batchSize < visibleObjects.size())
				firstObjectIndex++;
		}
	}

	public boolean hasNextBatch() {
		return firstObjectIndex + batchSize < visibleObjects.size();
	}

	public boolean hasPreviousBatch() {
		return firstObjectIndex > 0;
	}

	public void displayNextBatch() {
		if (firstObjectIndex + batchSize < visibleObjects.size()) {
			firstObjectIndex += batchSize;
			batchNumber++;

			if (!getBatchIndizes().contains(batchNumber))
				displayNextBatchBlock();
		} else {
			firstObjectIndex = 0;
			batchNumber = 1;
		}
	}

	public void displayPreviousBatch() {
		if (firstObjectIndex > 0) {
			firstObjectIndex -= batchSize;
			batchNumber--;

			if (!getBatchIndizes().contains(batchNumber))
				displayPreviousBatchBlock();
		} else
			displayBatch(getBatchCount());
	}

	public void displayLastBatch() {
		displayBatch(getBatchCount());
	}

	public void displayBatch(int newBatchNumber) {
		if (newBatchNumber > getBatchCount() || newBatchNumber <= 0
				|| visibleObjects.size() == 0)
			return;
		while (newBatchNumber > batchNumber)
			displayNextBatch();
		while (newBatchNumber < batchNumber)
			displayPreviousBatch();
	}

	public List<T> getDisplayedObjects() {
		if (batchSize == 0 || batchSize > visibleObjects.size())
			return visibleObjects;

		int _lastObjectIndex = firstObjectIndex + batchSize;
		if (_lastObjectIndex > visibleObjects.size())
			_lastObjectIndex = visibleObjects.size();

		return visibleObjects.subList(firstObjectIndex, _lastObjectIndex);
	}

	public int getNumberOfDisplayedObjects() {
		return getDisplayedObjects().size();
	}

	/**
	 * Returns the number of all objects available to this <tt>DisplayGroup</tt>
	 * , filtered or not.
	 * 
	 * @return total size of this <tt>DisplayGroup</tt>
	 */
	public int size() {
		return allObjects.size();
	}

	public List<T> getDisplayedObjectsAtIndexes(List<Integer> indexes) {
		ArrayList<T> _objects = new ArrayList<T>();
		List<T> _displayedObjects = getDisplayedObjects();
		for (Integer _index : indexes)
			_objects.add(_displayedObjects.get(_index));

		return _objects;
	}

	public int getBatchCount() {
		if (visibleObjects.size() == 0)
			return 0;
		if (batchSize == 0)
			return 1;

		int _batchCount = visibleObjects.size() / batchSize;
		if (visibleObjects.size() % batchSize != 0)
			_batchCount++;

		return _batchCount;
	}

	public boolean hasMultipleBatches() {
		return getBatchCount() > 1;
	}

	/**
	 * Indicates whether there are more objects contains in this
	 * <code>DisplayGroup</code> than the size of the default batch size.
	 * 
	 * @return True if there are more objects in this <code>DisplayGroup</code>
	 *         than can be shown using the default batch size. False otherwise.
	 */
	public boolean hasManyObjects() {
		return visibleObjects.size() > defaultBatchSize;
	}

	public int getCurrentBatchNumber() {
		if (visibleObjects.size() == 0)
			return -1;
		return batchNumber;
	}

	public boolean canScrollBatchUp() {
		if (visibleObjects.size() == 0)
			return false;
		return firstObjectIndex > 0;
	}

	public boolean canScrollBatchDown() {
		if (visibleObjects.size() == 0)
			return false;

		return firstObjectIndex + batchSize != visibleObjects.size()
				&& visibleObjects.size() > batchSize;
	}

	public List<T> getAllObjects() {
		return visibleObjects;
	}

	public void setAllObjects(List<T> newAllObjects) {
		if (newAllObjects == null)
			newAllObjects = Collections.emptyList();
		batchNumber = 1;
		firstObjectIndex = 0;
		batchIndizes = Collections.emptyList();
		if (filter != null) {
			allObjects = newAllObjects;
			filteredObjects = new ArrayList<T>();
			for (T _object : allObjects)
				if (filter.displayObject(_object))
					filteredObjects.add(_object);
			this.visibleObjects = filteredObjects;
		} else
			this.visibleObjects = newAllObjects;
	}

	public void add(T object) {
		allObjects.add(object);
		if (isFiltered() && filter.displayObject(object))
			filteredObjects.add(object);
	}

	public boolean hasLowerBlock() {
		return firstBatchIndex - BATCH_BLOCK_SIZE >= 0;
	}

	public boolean hasUpperBlock() {
		if (batchIndizes == null)
			getBatchIndizes();
		return firstBatchIndex + BATCH_BLOCK_SIZE < batchIndizes.size();
	}

	public void displayNextBatchBlock() {
		if (hasUpperBlock()) {
			firstBatchIndex += BATCH_BLOCK_SIZE;
			displayBatch(batchIndizes.get(firstBatchIndex));
		}
	}

	public void displayPreviousBatchBlock() {
		if (hasLowerBlock()) {
			firstBatchIndex -= BATCH_BLOCK_SIZE;
			displayBatch(batchIndizes.get(firstBatchIndex + BATCH_BLOCK_SIZE
					- 1));
		}
	}

	public List<Integer> getBatchIndizes() {
		int _batchCount = getBatchCount();
		if (_batchCount == 0)
			return batchIndizes;

		if (batchIndizes.isEmpty()) {
			batchIndizes = new ArrayList<Integer>();
			for (int i = 1; i <= _batchCount; i++)
				batchIndizes.add(i);
		}
		int _end = firstBatchIndex + BATCH_BLOCK_SIZE;
		_end = _end > batchIndizes.size() ? batchIndizes.size() : _end;

		return batchIndizes.subList(firstBatchIndex, _end);
	}

	/**
	 * <p>
	 * Empties the <code>DisplayGroup</code> and resets its filter. However,
	 * this method does not reset the batch size.
	 * </p>
	 * <p>
	 * This behavior is useful when only the contents of the
	 * <code>DisplayGroup</code> change but not its state (e.g. whether a facet
	 * is expanded or not).
	 * </p>
	 */
	public void clear() {
		visibleObjects = Collections.emptyList();
		firstBatchIndex = 0;
		batchNumber = 1;
		batchIndizes = Collections.emptyList();
		selectedIndizes = new boolean[BATCH_BLOCK_SIZE];
		filter.reset();
	}

	/**
	 * Resets this <code>DisplayGroup</code> to initialization state.
	 */
	public void reset() {
		batchSize = defaultBatchSize;
		clear();
	}

	public boolean isEmpty() {
		return visibleObjects.size() == 0;
	}

	public void selectObject(int i) {
		if (i >= 0 && i < getDisplayedObjects().size()) {
			selectedIndizes[i] = true;
		}
	}

	public T getSelectedObject() {
		for (int i = 0; i < selectedIndizes.length; i++)
			if (selectedIndizes[i] && i < getDisplayedObjects().size())
				return getDisplayedObjects().get(i);
		return null;
	}

	public boolean isObjectSelected(T object) {
		int i = getDisplayedObjects().indexOf(object);
		if (i >= 0)
			return selectedIndizes[i];

		return false;
	}

	public void selectObject(T object) {
		if (getDisplayedObjects().contains(object))
			selectedIndizes[getDisplayedObjects().indexOf(object)] = true;
	}

	public void unselectObject(T object) {
		if (getDisplayedObjects().contains(object))
			selectedIndizes[getDisplayedObjects().indexOf(object)] = false;
	}

	public void setSelectedObject(T object) {
		for (int i = 0; i < selectedIndizes.length; i++)
			selectedIndizes[i] = false;

		selectObject(object);
	}

	public void setSelectedObject(int selected) {
		for (int i = 0; i < selectedIndizes.length; i++)
			selectedIndizes[i] = false;

		if (selected >= 0 && selected < getDisplayedObjects().size()) {
			selectedIndizes[selected] = true;
		}
	}

	public void unselectAll() {
		for (int i = 0; i < selectedIndizes.length; i++)
			selectedIndizes[i] = false;
	}

	public void setSelectedObjects(List<T> objects) {
		unselectAll();
		for (T _object : objects)
			selectObject(_object);
	}

	public List<T> getSelectedObjects() {
		List<T> _selectedObjects = new ArrayList<T>();
		List<T> _displayedObjects = getDisplayedObjects();
		int _length = _displayedObjects.size();

		for (int i = 0; i < _length; i++)
			if (selectedIndizes[i])
				_selectedObjects.add(_displayedObjects.get(i));

		return _selectedObjects;
	}

	public void deleteSelection() {
		visibleObjects.removeAll(getSelectedObjects());
		if (getDisplayedObjects().size() == 0)
			displayPreviousBatch();
		unselectAll();
	}

	/**
	 * Indicates whether this <tt>DisplayGroup</tt> is currently being filtered
	 * or offering all available objects for display.
	 * 
	 * @return <tt>true</tt> if the objects in this <tt>DisplayGroup</tt> are
	 *         currently filtered, <tt>false</tt> otherwise
	 */
	public boolean isFiltered() {
		return filter.isFiltering();
	}

	protected void doFiltering(Filter<T> filter) {
		// if (this.filter == null && newFilter != null)
		// unfilteredObjects = allObjects;
		// if (newFilter == null && this.filter != null)
		// allObjects = unfilteredObjects;

		// if (newFilter != null) {
		filteredObjects.clear();
		for (T _object : allObjects)
			if (filter.displayObject(_object))
				filteredObjects.add(_object);
		visibleObjects = filteredObjects;
		// }
		// this.filter = newFilter;
	}

	/**
	 * <p>
	 * Determines whether there are any objects in this
	 * <code>DisplayGroup</code>, filtered or not filtered.
	 * </p>
	 * 
	 * @return True if this <code>DisplayGroup</code> contains any elements,
	 *         false otherwise.
	 */
	public boolean hasObjects() {
		return visibleObjects.size() > 0 || filteredObjects.size() > 0
				|| allObjects.size() > 0;
	}

	/**
	 * 
	 */
	public void resetFilter() {
		filter.reset();
		doFiltering(filter);
	}

	/**
	 * @param filterToken
	 */
	public void setFilterToken(String filterToken) {
		filter.setFilterToken(filterToken);
		doFiltering(filter);
	}

	/**
	 * @return
	 */
	public String getFilterToken() {
		return filter.getFilterToken();
	}

}
