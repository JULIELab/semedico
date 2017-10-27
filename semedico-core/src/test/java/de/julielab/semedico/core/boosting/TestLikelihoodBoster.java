package de.julielab.semedico.core.boosting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.julielab.semedico.core.SortCriteriumEvents;
import de.julielab.semedico.core.boosting.LikelihoodBooster;

public class TestLikelihoodBoster {
	

	@SuppressWarnings("unused")
	@Test
	public void testConstructorExceptionLegal() {
		boolean fail = false;
		try{
		LikelihoodBooster booster = new LikelihoodBooster(SortCriteriumEvents.CERTAINTY_CONTROVERSIAL);
		}catch (IllegalStateException e){
			fail = true;
		}
		assertFalse(fail);
	}
	
//	@SuppressWarnings("unused")
//	@Test
//	public void testConstructorExceptionIllegal() {
//		boolean fail = false;
//		try{
//		LikelihoodBooster booster = new LikelihoodBooster(SortCriterium.DATE);
//		}catch (IllegalStateException e){
//			fail = true;
//		}
//		assertTrue(fail);
//	}

	@Test
	public void testNeedsBoost() {
		LikelihoodBooster booster = new LikelihoodBooster(SortCriteriumEvents.CERTAINTY_CONTROVERSIAL);
		for(String s : "negation low investigation moderate high".split(" "))
			assertTrue(booster.needsBoost(s));
	}
	
	@Test
	public void testBoost() {
		LikelihoodBooster booster = new LikelihoodBooster(SortCriteriumEvents.CERTAINTY_CONTROVERSIAL);
		assertEquals("negation-foo^10.00", booster.boost("negation", "negation-foo"));
	}
}
