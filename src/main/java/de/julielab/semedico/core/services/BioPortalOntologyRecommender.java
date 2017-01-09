package de.julielab.semedico.core.services;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.http.HttpEntity;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.tapestry5.json.JSONArray;
import org.apache.tapestry5.json.JSONObject;
import org.slf4j.Logger;

import de.julielab.semedico.core.facets.BioPortalFacet;
import de.julielab.semedico.core.facets.Facet;
import de.julielab.semedico.core.facets.FacetProperties.BioPortal;
import de.julielab.semedico.core.facets.UIFacet;
import de.julielab.semedico.core.services.interfaces.IBioPortalOntologyRecommender;
import de.julielab.semedico.core.services.interfaces.IHttpClientService;
import de.julielab.semedico.core.services.interfaces.IHttpClientService.GeneralHttpClient;

/**
 * 
 * @author faessler
 * @deprecated unused
 */
@Deprecated
public class BioPortalOntologyRecommender implements IBioPortalOntologyRecommender {

	public static final String BIOONTOLOGY_HOST = "data.bioontology.org";
	public static final String RECOMMENDER_PATH = "/recommender";
	/**
	 * Used by the unit test
	 */
	public static final String RECOMMENDER_URL = "http://data.bioontology.org/recommender";

	private Logger log;
	private IHttpClientService httpClientService;

	public BioPortalOntologyRecommender(Logger log, @GeneralHttpClient IHttpClientService httpClientService) {
		this.log = log;
		this.httpClientService = httpClientService;

	}

	@Override
	public List<UIFacet> recommendOntologies(String keywords, List<UIFacet> facetsToSelectFrom) {

		try {
			URI uri = new URIBuilder().setScheme("http").setHost(BIOONTOLOGY_HOST).setPath(RECOMMENDER_PATH)
					.setParameter("apikey", "f112b8c6-5103-4a77-8094-7f76bf0e1fd2").setParameter("text", keywords)
					.build();
			HttpGet request = new HttpGet(uri);

			log.debug("Calling Web service for keywords \"{}\".", keywords);
			StopWatch w = new StopWatch();
			w.start();
			HttpEntity responseEntity = httpClientService.sendRequest(request);
			if (null == responseEntity)
				return null;
			String response = EntityUtils.toString(responseEntity);
			log.debug("BioPortal ontology recommender returned the following recommendations: {}", response);
			w.stop();
			log.debug("BioPortal ontology recommendation took {}ms({}s).", w.getTime(), w.getTime() / 1000);

			Map<String, UIFacet> facetsByAcronym = new HashMap<>();
			for (UIFacet facet : facetsToSelectFrom) {
				Facet originalFacet = facet.getOriginalFacet();
				if (originalFacet.getClass().equals(BioPortalFacet.class)) {
					BioPortalFacet bioPortalFacet = (BioPortalFacet) originalFacet;
					facetsByAcronym.put(bioPortalFacet.getAcronym(), facet);
				} else {
					log.warn(
							"Facet \"{}\" was passed to BioPortal Ontology Recommender Service; however, this facet is no BioPortal facet and thus not applicable to the BioPortal recommender service.",
							originalFacet);
				}
			}

			List<UIFacet> recommendedFacets = new ArrayList<>();
			JSONArray scoredOntologiesArray = new JSONArray(response);
			for (int i = 0; i < scoredOntologiesArray.length(); i++) {
				JSONObject scoredOntology = scoredOntologiesArray.getJSONObject(i);
				JSONObject ontology = scoredOntology.getJSONObject("ontology");
				String acronym = ontology.getString(BioPortal.acronym);
				UIFacet facet = facetsByAcronym.get(acronym);
				if (null != facet)
					recommendedFacets.add(facet);
			}

			log.debug("BioPortal facets have been recommended: {}", recommendedFacets);
			return recommendedFacets;
		} catch (ParseException | IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return null;

	}
}
