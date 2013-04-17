/**
 * BBatchEntity.java
 *
 * Copyright (c) 2013, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 14.02.2013
 **/

/**
 * 
 */
package de.julielab.semedico.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.tapestry5.beaneditor.NonVisual;

import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.TermLabel;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.domain.BBatchTermPair;

/**
 * @author faessler
 * 
 */
@Entity
public class BBatchEntity {

	/**
	 * 
	 */
	public BBatchEntity() {
		bTermFindingNumbers = new Stack<Integer>();
		bTermLists = new Stack<List<Label>>();
		termPairs = new Stack<BBatchTermPair>();
		termPairsSearched = new HashMap<Integer, BBatchTermPair>();
		numPairs = 2;
		numTopTerms = 20;
		adjustTermPairs();
	}

	@Id
	// I also tried GenerationType.AUTO; works perhaps, I don't know because
	// then I had problems caused by a writing error in the configuration
	// properties (in frontend AppModule). I changed it to IDENTITY and left it
	// there, now that it is working.
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@NonVisual
	public Long id;

	public Stack<BBatchTermPair> termPairs;

	/**
	 * The term pairs which have been searched for. This is kind of a memory so
	 * we know whether we have to search again or not when the user hit's the
	 * submit button again (e.g. for another number of top terms).
	 */
	public HashMap<Integer, BBatchTermPair> termPairsSearched;

	public int numPairs;

	public int numTopTerms;

	public boolean filterNonTerms;

	public Stack<Integer> bTermFindingNumbers;

	public Stack<List<Label>> bTermLists;

	/**
	 * Adjusts all relevant objects to the number of displayed term pairs. Is
	 * called when the number of displayed input fields for term pairs is
	 * changed ({@link #onValueChanged(int)}).
	 */
	public void adjustTermPairs() {
		if (numPairs == termPairs.size())
			return;
		while (numPairs > termPairs.size()) {
			termPairs.add(new BBatchTermPair());
			termPairsSearched.put(termPairs.size() - 1, new BBatchTermPair());
			bTermLists.add(null);
			bTermFindingNumbers.add(0);
		}
		while (numPairs < termPairs.size()) {
			termPairs.pop();
			termPairsSearched.remove(termPairs.size());
			bTermLists.pop();
			bTermFindingNumbers.pop();
		}
	}

	/**
	 * <p>
	 * Sets back the term instances to the termlabels possibly serialized with
	 * this entity.
	 * </p>
	 * <p>
	 * This is necessary since the term objects are not serialized with the
	 * labels.
	 * </p>
	 * 
	 * @param termService
	 */
	public void recoverFromDeserialization(ITermService termService) {
		for (List<Label> labelList : bTermLists) {
			for (Label l : labelList) {
				if (l instanceof TermLabel) {
					TermLabel termLabel = (TermLabel) l;
					termLabel.recoverFromSerialization(termService);
				}
			}
		}
	}

	/**
	 * @param i
	 */
	public void setTermPairAsSearched(int i) {
		termPairsSearched.get(i).setPair(termPairs.get(i));
	}

	/**
	 * @param b
	 * @return
	 */
	public boolean isTermPairSearched(BBatchTermPair b) {
		return termPairsSearched.values().contains(b);
	}
}
