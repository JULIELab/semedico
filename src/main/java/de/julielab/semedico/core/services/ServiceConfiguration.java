package de.julielab.semedico.core.services;

import java.util.HashMap;
import java.util.Map;

import org.apache.tapestry5.ioc.services.SymbolSource;

/**
 * Service configurations are used for services that take part in experimental
 * setups and thus need to be flexible regarding their configuration. Default
 * Tapestry configuration is loaded on the instantiation of the service class.
 * Thus, to test different configurations, the service must be re-instanced for
 * each configuration. To circumvent that, such services should implement
 * {@link ReconfigurablesService} and use a subclass of this class for
 * {@link ReconfigurablesService#configure(ServiceConfiguration)}.
 * 
 * @author faessler
 *
 */
public abstract class ServiceConfiguration {
	protected Map<String, ServiceFeature> featureMap;
	protected SymbolSource symbolSource;


	public ServiceConfiguration(SymbolSource symbolSource) {
		this.symbolSource = symbolSource;
		this.featureMap = new HashMap<>();
	};
	
	public ServiceFeature get(String name) {
		return featureMap.get(name);
	}
	
	public ServiceFeature getFeature(String name) {
		return featureMap.get(name);
	}

	public Map<String, ServiceFeature> getFeatureMap() {
		return featureMap;
	}
	public ServiceFeature put(ServiceFeature feature) {
		return featureMap.put(feature.getName(), feature);
	}

	public ServiceFeature put(String name) {
		return put(new ServiceFeature(SemedicoSymbolConstants.QUERY_CONCEPTS, symbolSource));
	}
}
