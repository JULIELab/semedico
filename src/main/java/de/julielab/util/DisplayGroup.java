package de.julielab.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;

public class DisplayGroup<t> {

	private List<t> allObjects;
	private List<t> filteredObjects;
	private List<t> unfilteredObjects;
	private int batchSize;
	private int firstObjectIndex;
	private int batchNumber;
	private List<Integer> batchIndizes;
	private int firstBatchIndex;
	private static int BATCH_BLOCK_SIZE = 5;
	private boolean[] selectedIndizes;
	private Filter<t> filter;
	private final int defaultBatchSize;

	public interface Filter<t> {
		public boolean displayObject(t object);

		public void setFilterToken(String filterToken);

		public String getFilterToken();

		public void reset();

		/**
		 * @return
		 */
		public boolean isFiltering();
	}

	public DisplayGroup(Filter<t> filter, int defaultBatchSize) {
		this.filter = filter;
		this.defaultBatchSize = defaultBatchSize;
		this.batchSize = this.defaultBatchSize;
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
			if (firstObjectIndex + batchSize < allObjects.size())
				firstObjectIndex++;
		}
	}

	public boolean hasNextBatch() {
		return firstObjectIndex + batchSize < allObjects.size();
	}

	public boolean hasPreviousBatch() {
		return firstObjectIndex > 0;
	}

	public void displayNextBatch() {
		if (firstObjectIndex + batchSize < allObjects.size()) {
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
				|| allObjects.size() == 0)
			return;
		while (newBatchNumber > batchNumber)
			displayNextBatch();
		while (newBatchNumber < batchNumber)
			displayPreviousBatch();
	}

	public List<t> getDisplayedObjects() {
		if (batchSize == 0 || batchSize > allObjects.size())
			return allObjects;

		int _lastObjectIndex = firstObjectIndex + batchSize;
		if (_lastObjectIndex > allObjects.size())
			_lastObjectIndex = allObjects.size();

		return allObjects.subList(firstObjectIndex, _lastObjectIndex);
	}

	public int getNumberOfDisplayedObjects() {
		return getDisplayedObjects().size();
	}

	public List<t> getDisplayedObjectsAtIndexes(List<Integer> indexes) {
		ArrayList<t> _objects = new ArrayList<t>();
		List<t> _displayedObjects = getDisplayedObjects();
		for (Integer _index : indexes)
			_objects.add(_displayedObjects.get(_index));

		return _objects;
	}

	public int getBatchCount() {
		if (allObjects.size() == 0)
			return 0;
		if (batchSize == 0)
			return 1;

		int _batchCount = allObjects.size() / batchSize;
		if (allObjects.size() % batchSize != 0)
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
		return allObjects.size() > defaultBatchSize;
	}

	public int getCurrentBatchNumber() {
		if (allObjects.size() == 0)
			return -1;
		return batchNumber;
	}

	public boolean canScrollBatchUp() {
		if (allObjects.size() == 0)
			return false;
		return firstObjectIndex > 0;
	}

	public boolean canScrollBatchDown() {
		if (allObjects.size() == 0)
			return false;

		return firstObjectIndex + batchSize != allObjects.size()
				&& allObjects.size() > batchSize;
	}

	public List<t> getAllObjects() {
		return allObjects;
	}

	public void setAllObjects(List<t> allObjects) {
		if (allObjects == null)
			allObjects = Collections.emptyList();
		batchNumber = 1;
		firstObjectIndex = 0;
		batchIndizes = Collections.emptyList();
		if (filter != null) {
			unfilteredObjects = allObjects;
			filteredObjects = new ArrayList<t>();
			for (t _object : unfilteredObjects)
				if (filter.displayObject(_object))
					filteredObjects.add(_object);
			this.allObjects = filteredObjects;
		} else
			this.allObjects = allObjects;
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
		allObjects = Collections.emptyList();
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
		return allObjects.size() == 0;
	}

	public void selectObject(int i) {
		if (i >= 0 && i < getDisplayedObjects().size()) {
			selectedIndizes[i] = true;
		}
	}

	public t getSelectedObject() {
		for (int i = 0; i < selectedIndizes.length; i++)
			if (selectedIndizes[i] && i < getDisplayedObjects().size())
				return getDisplayedObjects().get(i);
		return null;
	}

	public boolean isObjectSelected(t object) {
		int i = getDisplayedObjects().indexOf(object);
		if (i >= 0)
			return selectedIndizes[i];

		return false;
	}

	public void selectObject(t object) {
		if (getDisplayedObjects().contains(object))
			selectedIndizes[getDisplayedObjects().indexOf(object)] = true;
	}

	public void unselectObject(t object) {
		if (getDisplayedObjects().contains(object))
			selectedIndizes[getDisplayedObjects().indexOf(object)] = false;
	}

	public void setSelectedObject(t object) {
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

	public void setSelectedObjects(List<t> objects) {
		unselectAll();
		for (t _object : objects)
			selectObject(_object);
	}

	public List<t> getSelectedObjects() {
		List<t> _selectedObjects = new ArrayList<t>();
		List<t> _displayedObjects = getDisplayedObjects();
		int _length = _displayedObjects.size();

		for (int i = 0; i < _length; i++)
			if (selectedIndizes[i])
				_selectedObjects.add(_displayedObjects.get(i));

		return _selectedObjects;
	}

	public void deleteSelection() {
		allObjects.removeAll(getSelectedObjects());
		if (getDisplayedObjects().size() == 0)
			displayPreviousBatch();
		unselectAll();
	}

	public boolean isFiltered() {
		return filter.isFiltering();
	}

	protected void doFiltering(Filter<t> newFilter) {
		// if (this.filter == null && newFilter != null)
		// unfilteredObjects = allObjects;
		// if (newFilter == null && this.filter != null)
		// allObjects = unfilteredObjects;

		if (newFilter != null) {
			filteredObjects = new ArrayList<t>();
			for (t _object : unfilteredObjects)
				if (newFilter.displayObject(_object))
					filteredObjects.add(_object);
			allObjects = filteredObjects;
		}
		this.filter = newFilter;
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
		return allObjects.size() > 0 || filteredObjects.size() > 0
				|| unfilteredObjects.size() > 0;
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
	public void setFilter(String filterToken) {
		filter.setFilterToken(filterToken);
		doFiltering(filter);
	}

	/**
	 * @return
	 */
	public String getFilter() {
		return filter.getFilterToken();
	}

}
