package de.julielab.semedico.core.services;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.tapestry5.ioc.Registry;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import de.julielab.semedico.core.TestUtils;
import de.julielab.semedico.core.facets.BioPortalFacet;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.services.interfaces.IBioPortalOntologyRecommender;

@Ignore
@Deprecated
public class BioPortalOntologyRecommenderServiceTest {
	private static Registry registry;
	private static IBioPortalOntologyRecommender ontologyRecommender;
private static final Logger log = LoggerFactory.getLogger(BioPortalOntologyRecommenderServiceTest.class);
	@BeforeClass
	public static void setup() {
		registry = TestUtils.createTestRegistry();
		ontologyRecommender = registry.getService(IBioPortalOntologyRecommender.class);

		org.junit.Assume.assumeTrue(TestUtils.isAddressReachable(BioPortalOntologyRecommender.RECOMMENDER_URL));
	}

	@Test
	public void testOntologyRecommender() {
		BioPortalFacet bpFacet1 = new BioPortalFacet("id1", "GRO");
		BioPortalFacet bpFacet2 = new BioPortalFacet("id2", "MESH");
		BioPortalFacet bpFacet3 = new BioPortalFacet("id3", "Die total andere Facette");
		UIFacet facet1 = new UIFacet(log, bpFacet1);
		UIFacet facet2 = new UIFacet(log, bpFacet2);
		UIFacet facet3 = new UIFacet(log, bpFacet3);
		List<UIFacet> recommendedFacets = ontologyRecommender.recommendOntologies(
				"il-2 in phosphorylation of bcat enzymes", Lists.newArrayList(facet1, facet2, facet3));
		assertTrue(recommendedFacets.contains(facet1));
		assertTrue(recommendedFacets.contains(facet2));
		assertFalse(recommendedFacets.contains(facet3));
	}

	@AfterClass
	public static void shutdown() {
		registry.shutdown();
	}
}
