package de.julielab.semedico.core.services;

import de.julielab.semedico.core.services.interfaces.IServiceReconfigurationHub;
import de.julielab.semedico.core.services.interfaces.ReconfigurableService;
import org.apache.tapestry5.ioc.services.SymbolProvider;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ServiceReconfigurationHub implements IServiceReconfigurationHub {

	private Set<ReconfigurableService> services;
	
	public ServiceReconfigurationHub(Collection<ReconfigurableService> services) {
		this.services = new HashSet<>();
		services.forEach(this::registerService);
	}
	
	@Override
	public void registerService(ReconfigurableService service) {
		synchronized (services) {
			services.add(service);
		}
	}

	@Override
	public void reconfigureServices(SymbolProvider symbolProvider) {
		synchronized (services) {
			services.forEach(s -> s.configure(symbolProvider));
		}		
	}


}
