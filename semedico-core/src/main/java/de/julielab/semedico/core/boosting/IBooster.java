package de.julielab.semedico.core.boosting;

/**
 * There was originally no particular reason to create an interface for this. Actually, one might argue that it's not
 * needed at all. But it works, so what. Perhaps some day we will have different boosting implementations...?
 * 
 * @author faessler
 * 
 */
public interface IBooster {

	boolean needsBoost(Object boostCriterium);

	String boost(Object boostCriterium, String elasticSearchString);

}
