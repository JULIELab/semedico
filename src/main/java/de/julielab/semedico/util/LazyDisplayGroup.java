package de.julielab.semedico.util;

import java.util.Collection;
import java.util.List;

import com.google.common.collect.Lists;

public class LazyDisplayGroup<T> {

	private int totalSize;
	private int batchSize;
	private int currentBatchIndex;
	private int batchBlockSize;
	private Collection<T> displayedObjects;
	private int batchCount;

	public LazyDisplayGroup(int totalSize, int batchSize, int batchBlockSize, Collection<T> displayedObjects) {
		super();
		this.currentBatchIndex = 1;
		this.totalSize = totalSize;
		this.batchSize = batchSize;
		this.batchBlockSize = batchBlockSize;
		this.displayedObjects = displayedObjects;
		this.batchCount = (int) Math.ceil((double)totalSize / (double)batchSize);
	}
	
	public Collection<Integer> batchIndexes(){
		List<Integer> batchIndexes = Lists.newArrayList();
		for( int i = 1; i <= batchCount; i++ )
			batchIndexes.add(i);

		if( batchCount <= batchBlockSize )
			return batchIndexes;
		else{
			int firstElementIndex = 0;
			if( currentBatchIndex > (batchBlockSize - 1)/2 )
				if( currentBatchIndex < batchCount - (batchBlockSize - 1)/2 - 1)
					firstElementIndex = (int) (currentBatchIndex - (((double)batchBlockSize - 1) / 2) -1);
				else
					firstElementIndex = batchCount - batchBlockSize;
			
			int lastElementIndex = firstElementIndex + batchBlockSize;
			if( lastElementIndex > batchCount -1 )
				lastElementIndex = batchCount;
			
			return batchIndexes.subList(firstElementIndex, lastElementIndex); 
		}
	}
	
	public void displayNextBatch(){
		currentBatchIndex++;
		if( currentBatchIndex > batchCount )
			currentBatchIndex = 1;
	}
	
	public void displayPreviousBatch(){
		currentBatchIndex--;
		if( currentBatchIndex < 1 )
			currentBatchIndex = batchCount;
	}
	
	public int getIndexOfFirstDisplayedObject(){
		return batchSize * (currentBatchIndex -1);
	}

	public int getIndexOfLastDisplayedObject(){
		int lastIndex = batchSize * (currentBatchIndex);
		
		return lastIndex > totalSize ? totalSize : lastIndex;
	}

	public Collection<T> getDisplayedObjects() {
		return displayedObjects;
	}

	public void setDisplayedObjects(Collection<T> displayedObjects) {
		this.displayedObjects = displayedObjects;
	}

	public int getCurrentBatchIndex() {
		return currentBatchIndex;
	}

	public int getBatchCount() {
		return batchCount;
	}

	public void setCurrentBatchIndex(int currentBatchIndex) {
		this.currentBatchIndex = currentBatchIndex;
	}
	
}
