package de.julielab.semedico.core.boosting;

import java.util.HashMap;
import java.util.Map;

import de.julielab.semedico.core.SortCriteriumEvents;

public class LikelihoodBooster implements IBooster {

	private final Map<String, Double> likelihoodBoostMap = new HashMap<String, Double>();
	private static final double boostFactor = 10.0; // should be > 1!

	public LikelihoodBooster(SortCriteriumEvents sortCriterium) {
		switch (sortCriterium) {
		case CERTAINTY_CONTROVERSIAL:
			likelihoodBoostMap.put("negation", boostFactor);
			likelihoodBoostMap.put("low", boostFactor / 2);
			likelihoodBoostMap.put("investigation", 1d);
			likelihoodBoostMap.put("moderate", boostFactor / 2);
			likelihoodBoostMap.put("high", boostFactor);
			break;
		case CERTAINTY_LOW:
			likelihoodBoostMap.put("negation", boostFactor);
			likelihoodBoostMap.put("low", boostFactor / 2);
			likelihoodBoostMap.put("investigation", 1d);
			likelihoodBoostMap.put("moderate", 2 / boostFactor);
			likelihoodBoostMap.put("high", 1 / boostFactor);
			break;
		case CERTAINTY_HIGH:
			likelihoodBoostMap.put("negation", 1 / boostFactor);
			likelihoodBoostMap.put("low", 2 / boostFactor);
			likelihoodBoostMap.put("investigation", 1d);
			likelihoodBoostMap.put("moderate", boostFactor / 2);
			likelihoodBoostMap.put("high", boostFactor);
			break;
		case CERTAINTY_MID:
			likelihoodBoostMap.put("negation", 1d);
			likelihoodBoostMap.put("low", boostFactor / 2);
			likelihoodBoostMap.put("investigation", boostFactor);
			likelihoodBoostMap.put("moderate", boostFactor / 2);
			likelihoodBoostMap.put("high", 1d);
			break;
		case NONE:
			likelihoodBoostMap.put("negation", 1d);
			likelihoodBoostMap.put("low", 1d);
			likelihoodBoostMap.put("investigation", 1d);
			likelihoodBoostMap.put("moderate", 1d);
			likelihoodBoostMap.put("high", 1d);
		default:
			throw new IllegalStateException(sortCriterium + " is not a valid way to sort by likelihood!");
		}
	}

	@Override
	public boolean needsBoost(Object boostCriterium) {
		if (boostCriterium instanceof String && likelihoodBoostMap.containsKey(boostCriterium)) {
			return true;
		}
		return false;
	}

	@Override
	public String boost(Object boostCriterium, String elasticSearchString) {
		return String.format("%s^%.2f", elasticSearchString, likelihoodBoostMap.get(boostCriterium));
	}

}
