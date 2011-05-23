//package de.julielab.semedico.search;
//
//
//import static org.junit.Assert.*;
//
//import org.junit.Test;
//
//import com.google.common.collect.Lists;
//
//import de.julielab.semedico.core.Label;
//import de.julielab.semedico.search.ILabelCacheService;
//import de.julielab.semedico.search.LabelCacheService;
//
//public class LabelCacheServiceTest {
//
//	@Test
//	public void testGetCachedLabel(){
//		ILabelCacheService labelCacheService = new LabelCacheService();
//		Label label1 = labelCacheService.getCachedLabel();
//		labelCacheService.releaseLabels(Lists.newArrayList(label1));
//		assertEquals(label1, labelCacheService.getCachedLabel());
//	}
//}
