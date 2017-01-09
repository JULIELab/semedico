package de.julielab.semedico.util;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import de.julielab.semedico.core.util.LazyDisplayGroup;

public class LazyDisplayGroupTest {
	
	
	private LazyDisplayGroup<Integer> lazyDisplayGroup;
	
	@Before
	public void setUp(){
		lazyDisplayGroup = new LazyDisplayGroup<Integer>(13, 3, 3, Lists.newArrayList(1));	
	}
	
	@Test
	public void testBatchIndexes(){
		Collection<Integer> indexes = lazyDisplayGroup.batchIndexes();
		assert new Integer(3).equals(indexes.size());
		
		Iterator<Integer> iterator = indexes.iterator();
		assert new Integer(1).equals(iterator.next()); 
		assert new Integer(2).equals(iterator.next());
		assert new Integer(3).equals(iterator.next());
		
		lazyDisplayGroup.setCurrentBatchIndex(4);
		
		indexes = lazyDisplayGroup.batchIndexes();
		assert new Integer(3).equals(indexes.size());
		iterator = indexes.iterator();
		assert new Integer(3).equals(iterator.next()); 
		assert new Integer(4).equals(iterator.next());
		assert new Integer(5).equals(iterator.next());

		lazyDisplayGroup.setCurrentBatchIndex(5);
		
		indexes = lazyDisplayGroup.batchIndexes();
		assert new Integer(3).equals(indexes.size());
		iterator = indexes.iterator();
		assert new Integer(3).equals(iterator.next()); 
		assert new Integer(4).equals(iterator.next());
		assert new Integer(5).equals(iterator.next());
	}
	
	@Test
	public void testDisplayNextBatch(){
		lazyDisplayGroup.displayNextBatch();
		
		assert new Integer(2).equals(lazyDisplayGroup.getCurrentBatchIndex());
		
		lazyDisplayGroup.setCurrentBatchIndex(5);
		
		lazyDisplayGroup.displayNextBatch();
		
		assert new Integer(1).equals(lazyDisplayGroup.getCurrentBatchIndex());
	}
	
	@Test
	public void testDisplayPreviousBatch(){
		lazyDisplayGroup.displayPreviousBatch();
		
		assert new Integer(5).equals(lazyDisplayGroup.getCurrentBatchIndex());
		
		lazyDisplayGroup.setCurrentBatchIndex(2);
		
		lazyDisplayGroup.displayPreviousBatch();
		
		assert new Integer(1).equals(lazyDisplayGroup.getCurrentBatchIndex());
	}
}
