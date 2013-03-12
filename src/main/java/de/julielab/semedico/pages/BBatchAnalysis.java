/**
 * BBatchAnalysis.java
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
 * Creation date: 04.02.2013
 **/

/**
 * 
 */
package de.julielab.semedico.pages;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.apache.tapestry5.ComponentResources;
import org.apache.tapestry5.FieldTranslator;
import org.apache.tapestry5.Link;
import org.apache.tapestry5.MarkupWriter;
import org.apache.tapestry5.ValidationException;
import org.apache.tapestry5.annotations.Environmental;
import org.apache.tapestry5.annotations.Import;
import org.apache.tapestry5.annotations.InjectComponent;
import org.apache.tapestry5.annotations.InjectPage;
import org.apache.tapestry5.annotations.Persist;
import org.apache.tapestry5.annotations.Property;
import org.apache.tapestry5.corelib.components.Form;
import org.apache.tapestry5.corelib.components.Zone;
import org.apache.tapestry5.ioc.annotations.Inject;
import org.apache.tapestry5.services.Request;
import org.apache.tapestry5.services.javascript.JavaScriptSupport;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultiset;

import de.julielab.semedico.bterms.interfaces.IBTermService;
import de.julielab.semedico.core.Label;
import de.julielab.semedico.core.TermLabel;
import de.julielab.semedico.core.exceptions.EmptySearchComplementException;
import de.julielab.semedico.core.exceptions.TooFewSearchNodesException;
import de.julielab.semedico.core.services.interfaces.ITermService;
import de.julielab.semedico.core.taxonomy.interfaces.IFacetTerm;
import de.julielab.semedico.domain.BBatchTermPair;
import de.julielab.semedico.entities.BBatchEntity;
import de.julielab.semedico.query.IQueryDisambiguationService;
import de.julielab.semedico.query.TermAndPositionWrapper;
import de.julielab.util.DisplayGroup;
import de.julielab.util.LabelFilter;

/**
 * @author faessler
 * 
 */

@Import(library = { "bbatchanalysis.js", "context:js/jquery-1.7.1.min.js" }, stylesheet = { "context:css/bbatchanalysis.css" })
public class BBatchAnalysis {

	@InjectPage
	private Index index;
	
	@Inject
	Session hibernateSession;

	@SuppressWarnings("unused")
	@Property
	private BBatchTermPair loopItemPair;

	@Property
	private int termPairIndex;

	@Property
	@Persist
	private BBatchEntity bBatchEntity;

	@Property
	@Persist
	private DisplayGroup<Label> topBTermLabels;

	@InjectComponent
	private Form termform;

	@InjectComponent
	private Zone termPairsZone;

	@InjectComponent
	private Zone textAreaZone;

	@InjectComponent
	private Form saveform;

	@InjectComponent
	private Form newAnalysisForm;

	@InjectComponent
	private Form loadform;

	@Property
	private Long loadResultId;

	@Environmental
	private JavaScriptSupport javaScriptSupport;

	@Inject
	private ComponentResources resources;

	@Inject
	private IQueryDisambiguationService queryDisambiguationService;

	@Inject
	private IBTermService btermService;

	@Inject
	private ITermService termService;

	@Inject
	private Request request;
	
	@Persist
	private NumberFormat f;
	
	@Inject
	Logger log;

	private FieldTranslator<DisplayGroup> labelTranslatorForTextArea = new FieldTranslator<DisplayGroup>() {

		@Override
		public Class<DisplayGroup> getType() {
			return DisplayGroup.class;
		}

		@Override
		public DisplayGroup<Label> parse(String input)
				throws ValidationException {
			// output only
			return null;
		}

		@Override
		public String toClient(DisplayGroup value) {
			StringBuilder sb = new StringBuilder();
			for (Object o : value.getDisplayedObjects()) {
				Label l = (Label) o;
				sb.append("(");
				sb.append(f.format(l.getRankScore()));
				sb.append(") ");
				sb.append(l.getName());
				sb.append("\n");
			}
			return sb.toString();
		}

		@Override
		public void render(MarkupWriter writer) {
			// TODO Auto-generated method stub

		}

	};
	
	public Object onActivate() {
		if(null == request.getSession(false))
			return index;
		return null;
	}
	
	public void onPassivate() {
		request.getSession(true);
	}

	public void setupRender() {
		if (null == bBatchEntity)
			bBatchEntity = new BBatchEntity();
		if (null == topBTermLabels)
			topBTermLabels = new DisplayGroup<Label>(new LabelFilter(),
					bBatchEntity.numPairs * bBatchEntity.numTopTerms,
					TreeMultiset.<Label> create());
		if (null == f) {
			f = NumberFormat.getNumberInstance(Locale.US);
			f.setMaximumFractionDigits(2);
			f.setMinimumFractionDigits(2);
		}
	}

	// ------------------------------ Main input form ------------------------

	public void onValidateFromTermform() {
		for (BBatchTermPair b : bBatchEntity.termPairs) {
			if (null == b.term1 || null == b.term2) {
				termform.recordError("Please specify both terms for each term pair.");
				return;
			}
		}
		List<List<Multimap<String, IFacetTerm>>> parsedTerms = new ArrayList<List<Multimap<String, IFacetTerm>>>();
		for (int i = 0; i < bBatchEntity.termPairs.size(); ++i) {
			BBatchTermPair b = bBatchEntity.termPairs.get(i);
			parsedTerms.add(null);
			// Do we already have the terms for the this search?
			if (bBatchEntity.isTermPairSearched(b)) {
				log.debug("Term pair \"{}\" has already been searched for, skipping.", b);
				continue;
			}
			Multimap<String, TermAndPositionWrapper> parsed1 = queryDisambiguationService
					.disambiguateQuery(b.term1, null);
			Multimap<String, IFacetTerm> parsed1Old = getOldQueryStructure(parsed1);
			log.debug("Term {},1: {}", i, getQueryString(parsed1Old));
			Multimap<String, TermAndPositionWrapper> parsed2 = queryDisambiguationService
					.disambiguateQuery(b.term2, null);
			Multimap<String, IFacetTerm> parsed2Old = getOldQueryStructure(parsed2);
			log.debug("Term {},2: {}", i, getQueryString(parsed2Old));
			parsedTerms.set(i, Lists.newArrayList(parsed1Old, parsed2Old));
		}

		// bBatchEntity.bTermFindingNumbers.clear();
		topBTermLabels = new DisplayGroup<Label>(new LabelFilter(),
				bBatchEntity.numPairs * bBatchEntity.numTopTerms,
				TreeMultiset.<Label> create());
		for (int i = 0; i < parsedTerms.size(); ++i) {
			List<Multimap<String, IFacetTerm>> pair = parsedTerms.get(i);
			// null means: we already have the results from a former analysis
			if (null == pair)
				continue;
			try {
				List<Label> bTermLabelList = btermService
						.determineBTermLabelList(pair);
				bBatchEntity.bTermLists.set(i, bTermLabelList);
				bBatchEntity.bTermFindingNumbers.set(i, bTermLabelList.size());
				bBatchEntity.setTermPairAsSearched(i);
				log.debug("Retrieved {} b-terms", bTermLabelList.size());
			} catch (TooFewSearchNodesException e) {
				e.printStackTrace();
			} catch (EmptySearchComplementException e) {
				e.printStackTrace();
				termform.recordError("Term pair "
						+ i
						+ " has an empty result set complement, i.e. one found document set subsumes the other.");
			}
		}
	}

	public void onSuccessFromTermform() {
		refreshTopTerms();
	}

	// Called by the select's zone parameter
	Object onValueChanged(int value) {
		bBatchEntity.numPairs = value;
		bBatchEntity.adjustTermPairs();
		return termPairsZone.getBody();
	}

	// Called from custom JavaScript Ajax call since the CheckBox component
	// doesn't have a zone parameter.
	Object onCheckboxClicked(boolean state) {
		bBatchEntity.filterNonTerms = state;
		refreshTopTerms();
		return textAreaZone.getBody();
	}

	private void refreshTopTerms() {
		topBTermLabels.clear();
		for (List<Label> bTermLabelList : bBatchEntity.bTermLists)
			getTopTermsFromList(bTermLabelList);
	}

	/**
	 * Fills {@link #topBTermLabels} with all labels which are in the top-
	 * {@link #numTopTerms} after filtering is applied.
	 * 
	 * @param bTermLabelList
	 */
	private void getTopTermsFromList(List<Label> bTermLabelList) {
		int currentNumTopTerms = 0;
		for (int j = 0; currentNumTopTerms < bBatchEntity.numTopTerms
				&& j < bTermLabelList.size(); j++) {
			Label label = bTermLabelList.get(j);
			if (bBatchEntity.filterNonTerms) {
				if (!(label instanceof TermLabel)) {
					continue;
				}
			}
			topBTermLabels.add(label);
			currentNumTopTerms++;
		}
	}

	// only required because the current representation of disambiguated queries
	// is stuck between versions. Should all be replaced by the ParseTree
	private Multimap<String, IFacetTerm> getOldQueryStructure(
			Multimap<String, TermAndPositionWrapper> result) {
		// TODO this is for legacy reasons until the new query structure can be
		// used in the whole of Semedico.
		Multimap<String, IFacetTerm> disambiguatedQuery = HashMultimap.create();
		for (String key : result.keySet()) {
			Collection<TermAndPositionWrapper> collection = result.get(key);
			for (TermAndPositionWrapper wrapper : collection) {
				IFacetTerm term = wrapper.getTerm();
				disambiguatedQuery.put(key, term);
				// searchState.getQueryTermFacetMap().put(term,
				// term.getFirstFacet());
			}
		}
		return disambiguatedQuery;
	}

	// should be a method of the query object; this will have to wait until we
	// HAVE a decent query structure, namely the ParseTree
	private String getQueryString(Multimap<String, IFacetTerm> parsedQuery) {
		String ret = "\n";
		for (String key : parsedQuery.keySet()) {
			Collection<IFacetTerm> values = parsedQuery.get(key);
			ret += key + " --> ";
			for (IFacetTerm t : values)
				ret += t.getName() + " ";
			ret += "\n";
		}
		return ret;
	}

	@SuppressWarnings("rawtypes")
	public FieldTranslator<DisplayGroup> getFieldTranslatorForTextArea() {
		return labelTranslatorForTextArea;
	}

	// ---------------------- Property Methods -------------------------------
	public int getBTermNumber() {
		if (termPairIndex < bBatchEntity.bTermFindingNumbers.size())
			return bBatchEntity.bTermFindingNumbers.get(termPairIndex);
		return 0;
	}

	public String getTextAreaContent() {
		if (null != topBTermLabels) {
			String ret = labelTranslatorForTextArea.toClient(topBTermLabels);
			return ret;
		}
		return null;
	}

	// ---------------------- Property Methods End ---------------------------

	// ------------------------------ Save Result form -----------------------

	void onValidateFromSaveform() {
		if (0 == topBTermLabels.size()) {
			saveform.recordError("There is currently no result to store.");
		}
	}

	void onSuccessFromSaveform() {
		Transaction t = hibernateSession.beginTransaction();
		if (null == bBatchEntity.id) {
			hibernateSession.persist(bBatchEntity);
		} else {
			hibernateSession.update(bBatchEntity);
		}
		t.commit();
	}

	public String getSaveformLabel() {
		return bBatchEntity.id == null ? "Save this result"
				: "Update this result";
	}

	// ------------------------------ New Analysis form ----------------------

	void onSuccessFromNewAnalysisForm() {
		resetAllForms();
	}

	// ------------------------------ Load Result form -----------------------

	void onValidateFromLoadform() {
		if (null == loadResultId) {
			loadform.recordError("You must specify the ID of a stored result.");
		}
	}

	void onSuccessFromLoadform() {
		Transaction t = hibernateSession.beginTransaction();
		BBatchEntity loaded = (BBatchEntity) hibernateSession.get(
				BBatchEntity.class, loadResultId);
		t.commit();
		if (null == loaded)
			loadform.recordError("The result with ID " + loadResultId
					+ " could not be found.");
		else {
			bBatchEntity = loaded;
			bBatchEntity.recoverFromDeserialization(termService);
			refreshTopTerms();
		}
	}

	private void resetAllForms() {
		bBatchEntity = null;
		topBTermLabels = null;
	}

	void afterRender() {
		Link link = resources.createEventLink("checkboxClicked");
		javaScriptSupport.addScript("filterNonTermCheckbox('%s')", link);
	}

}
