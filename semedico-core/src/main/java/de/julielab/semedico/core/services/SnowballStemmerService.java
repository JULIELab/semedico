package de.julielab.semedico.core.services;

import de.julielab.semedico.core.services.interfaces.IStemmerService;
import org.apache.tapestry5.ioc.ScopeConstants;
import org.tartarus.snowball.SnowballProgram;

/**
 * A stemmer service using a snowball stemmer. Caution: the snowball programs are not thread safe. Instantiations of
 * this service should be restricted to the {@link ScopeConstants#PERTHREAD} scope.
 * 
 * @author faessler
 * @deprecated use elasticsearch query analysis
 */
@Deprecated
public class SnowballStemmerService implements IStemmerService {
	private SnowballProgram stemmer;

	public SnowballStemmerService(SnowballProgram stemmer) {
		this.stemmer = stemmer;
	}

	@Override
	public String stem(String token) {
		stemmer.setCurrent(token);
		stemmer.stem();
		return stemmer.getCurrent();
	}

}
