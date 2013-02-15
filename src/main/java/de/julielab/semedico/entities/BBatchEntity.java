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
		numPairs = 2;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@NonVisual
	public Long id;

	public Stack<BBatchTermPair> termPairs;

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
			bTermFindingNumbers.add(0);
		}
		while (numPairs < termPairs.size()) {
			termPairs.pop();
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
}
