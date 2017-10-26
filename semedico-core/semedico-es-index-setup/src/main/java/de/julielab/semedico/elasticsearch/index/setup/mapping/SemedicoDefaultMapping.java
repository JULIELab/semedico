package de.julielab.semedico.elasticsearch.index.setup.mapping;

/**
 * This is not a complete default mapping for Semedico (such a thing doesn't
 * exist) but it is the commonly used variant of the _default_ mapping field in
 * Semedico. It is specified as follows:
 * 
 * <pre>
 * "_default_": {
 *	"_all": {
 *		"enabled": false
 *	},
 *	"_source": {
 *		"enabled": false
 *	}
 * }
 * </pre>
 * 
 * @author faessler
 *
 */
public class SemedicoDefaultMapping extends DefaultMapping {

	/**
	 * @see {@link SemedicoDefaultMapping}
	 */
	public SemedicoDefaultMapping() {
		super(new DefaultMapping.All(false), new DefaultMapping.Source(false));
	}

}
