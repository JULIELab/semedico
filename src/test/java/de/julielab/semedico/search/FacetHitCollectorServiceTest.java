package de.julielab.semedico.search;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import java.util.Collection;
import java.util.List;

import org.apache.lucene.util.OpenBitSet;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import de.julielab.semedico.core.Facet;
import de.julielab.semedico.core.FacetConfiguration;
import de.julielab.semedico.core.FacetHit;
import de.julielab.semedico.core.FacetTerm;
import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.services.IFacetService;
import de.julielab.semedico.core.services.ITermService;
import de.julielab.semedico.search.FacetHitCollectorService;
import de.julielab.semedico.search.ILabelCacheService;

public class FacetHitCollectorServiceTest {

	private Facet facet1;
	private Facet facet2;
	private List<Facet> facets;
	private FacetConfiguration facetConfiguration1;
	private FacetConfiguration facetConfiguration2;
	private Collection<FacetConfiguration> facetConfigurations;
	private ITermService termService;
	private IFacetService facetService;
	private ILabelCacheService labelCacheService;

	private FacetTerm term1;
	private FacetTerm term2;
	private FacetTerm term3;
	private FacetTerm term4;

	@Before
	public void setUp() throws Exception {
		facet1 = new Facet("facet1");
		facet2 = new Facet("facet2");

		facets = Lists.newArrayList(facet1, facet2);

		term1 = new FacetTerm("term1");
		term1.setFacet(facet1);

		term2 = new FacetTerm("term2");
		term2.setFacet(facet1);

		term3 = new FacetTerm("term3");
		term3.setFacet(facet2);

		term4 = new FacetTerm("term4");
		term4.setFacet(facet2);

		facetConfiguration1 = new FacetConfiguration(facet1);
		facetConfiguration2 = new FacetConfiguration(facet2);
		facetConfigurations = Lists.newArrayList(facetConfiguration1, facetConfiguration2);
		
		termService = createMock(ITermService.class);
		facetService = createMock(IFacetService.class);
		labelCacheService = createMock(ILabelCacheService.class);
	}


	@Test
	public void testCollectFacetHits() throws Exception {
		term2.setParent(term1);

		FacetField facetField1 = createMock(FacetField.class);
		FacetField facetField2 = createMock(FacetField.class);
		List<Count> countList1 = Lists.newArrayList(new Count(facetField1,
				"term1", 3), new Count(facetField1, "term2", 5));
		List<Count> countList2 = Lists.newArrayList(new Count(facetField1,
				"term3", 7), new Count(facetField1, "term4", 10));
		List<FacetField> facetFields = Lists.newArrayList(facetField1,
				facetField2);

		// Call to get the size of facets.
		expect(facetService.getFacets()).andReturn(facets);

		// Use blocks to structure the calls and avoid using of local variables
		// in the wrong place.
		{
			// Calls to get the current facet: Get the field name and strip the
			// leading "facet_". Thus, look for the facet named "facet1".
			expect(facetField1.getName()).andReturn("facet_field1");
			expect(facetService.getFacetWithName("field1")).andReturn(facet1);
			
			// Iterate over counts for the purpose of sub term hit marking.
			expect(facetField1.getValues()).andReturn(countList1);
			expect(termService.getTermWithInternalIdentifier("term1", facet1))
					.andReturn(term1);
			expect(termService.getTermWithInternalIdentifier("term2", facet1))
					.andReturn(term2);
			
			// Iterate once again over the same list of counts; this time to
			// create the actual labels.
			expect(facetField1.getValues()).andReturn(countList1);
			expect(termService.getTermWithInternalIdentifier("term1", facet1))
					.andReturn(term1);
			expect(labelCacheService.getCachedLabel()).andReturn(new Label());
			expect(termService.getTermWithInternalIdentifier("term2", facet1))
					.andReturn(term2);
			expect(labelCacheService.getCachedLabel()).andReturn(new Label());
		}

		// Do everything again for the 2nd facet.
		{
			expect(facetField2.getName()).andReturn("facet_field2");
			expect(facetService.getFacetWithName("field2")).andReturn(facet2);
			expect(facetField2.getValues()).andReturn(countList2);
			expect(termService.getTermWithInternalIdentifier("term3", facet2))
					.andReturn(term3);
			expect(termService.getTermWithInternalIdentifier("term4", facet2))
					.andReturn(term4);
			expect(facetField2.getValues()).andReturn(countList2);
			expect(termService.getTermWithInternalIdentifier("term3", facet2))
					.andReturn(term3);
			expect(labelCacheService.getCachedLabel()).andReturn(new Label());
			expect(termService.getTermWithInternalIdentifier("term4", facet2))
					.andReturn(term4);
			expect(labelCacheService.getCachedLabel()).andReturn(new Label());
		}
		// Store the expectations.
		replay(facetField1);
		replay(facetField2);
		replay(facetService);
		replay(termService);
		replay(labelCacheService);

		// Do the actual counting.
		FacetHitCollectorService facetHitCollector = new FacetHitCollectorService();
		facetHitCollector.setTermService(termService);
		facetHitCollector.setFacetService(facetService);
		facetHitCollector.setLabelCacheService(labelCacheService);
		List<FacetHit> facetHits = facetHitCollector
				.collectFacetHits(facetConfigurations, facetFields);

		// And verify whether all calls came as expected.
		verify(facetField1);
		verify(facetField2);
		verify(facetService);
		verify(termService);
		verify(labelCacheService);

		// Check whether the correct labels have been produced.
		{
			FacetHit facetHit1 = facetHits.get(0);
			assertEquals(facet1, facetHit1.getFacet());
			Label term1Label = facetHit1.getLabels().get(0);
			assertEquals(term1, term1Label.getTerm());
			assertEquals(new Integer(3), term1Label.getHits());

			Label term2Label = facetHit1.getLabels().get(1);
			assertEquals(term2, term2Label.getTerm());
			assertEquals(new Integer(5), term2Label.getHits());
		}

		{
			FacetHit facetHit2 = facetHits.get(1);
			assertEquals(facet2, facetHit2.getFacet());
			Label term3Label = facetHit2.getLabels().get(0);
			assertEquals(term3, term3Label.getTerm());
			assertEquals(new Integer(7), term3Label.getHits());

			Label term4Label = facetHit2.getLabels().get(1);
			assertEquals(term4, term4Label.getTerm());
			assertEquals(new Integer(10), term4Label.getHits());
		}

	}
}
