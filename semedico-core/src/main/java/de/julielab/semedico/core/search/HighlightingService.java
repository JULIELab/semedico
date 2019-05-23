/** 
 * KwicService.java
 * 
 * Copyright (c) 2008, JULIE Lab. 
 * All rights reserved. This program and the accompanying materials 
 * are protected. Please contact JULIE Lab for further information.  
 *
 * Author: landefeld
 * 
 * Current version: //TODO insert current version number 	
 * Since version:   //TODO insert version number of first appearance of this class
 *
 * Creation date: 04.03.2008 
 * 
 * //TODO insert short description
 **/

package de.julielab.semedico.core.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import de.julielab.scicopia.core.elasticsearch.legacy.ISearchServerDocument;
import de.julielab.semedico.core.concepts.IConcept;
import de.julielab.semedico.core.search.components.data.Highlight;
import de.julielab.semedico.core.search.components.data.HighlightedSemedicoDocument.AuthorHighlight;
import de.julielab.semedico.core.search.interfaces.IHighlightingService;
import de.julielab.semedico.core.services.interfaces.IIndexInformationService;
import de.julielab.semedico.core.services.interfaces.ITermService;

public class HighlightingService implements IHighlightingService {

	private Logger log;
	private Matcher htmlTagMatcher;
	private ITermService termService;

	public HighlightingService(Logger log, ITermService termService) {
		this.log = log;
		this.termService = termService;
		this.htmlTagMatcher = Pattern.compile("<[^>]+>").matcher("");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.search.IKwicService#getHighlightedAbstract(java.
	 * util.Map, int)
	 */
	@Override
	public Highlight getHighlightedAbstract(ISearchServerDocument serverDoc) {
		Highlight ret = null;
		List<String> abstractHl = serverDoc.getHighlights()
				.get(IIndexInformationService.GeneralIndexStructure.abstracttext);

		if (abstractHl != null) {
			ret = new Highlight(abstractHl.get(0), IIndexInformationService.GeneralIndexStructure.abstracttext,
					serverDoc.getScore());
		} else {
			String abstractText = serverDoc.get(IIndexInformationService.GeneralIndexStructure.abstracttext);
			ret = new Highlight(abstractText, IIndexInformationService.GeneralIndexStructure.abstracttext, 0);
		}
		return ret;
	}

	@Override
	public Highlight getTitleHighlight(ISearchServerDocument serverDoc) {
		Highlight titleHighlight = null;
		Map<String, List<String>> highlights = serverDoc.getHighlights();
		if (highlights != null) {
			List<String> titleHlStrings = highlights.get(IIndexInformationService.GeneralIndexStructure.title);
			if (null != titleHlStrings && !titleHlStrings.isEmpty()) {
				titleHighlight = new Highlight(titleHlStrings.get(0), IIndexInformationService.GeneralIndexStructure.title,
						serverDoc.getScore());
			}
		}
		if (null == titleHighlight) {
			// try to get the title directly from the title field
			String title = serverDoc.get(IIndexInformationService.GeneralIndexStructure.title);

			if (!StringUtils.isBlank(title))
				titleHighlight = new Highlight(title, IIndexInformationService.GeneralIndexStructure.title, serverDoc.getScore());
		}
		if (null == titleHighlight) {
			log.warn("Neither a title highlighting nor the title field value could be retrieved for document {}.",
					serverDoc.getId());
			titleHighlight = new Highlight("<title could not be retrieved; please report this error>",
					IIndexInformationService.GeneralIndexStructure.title, 0);
		}
		return titleHighlight;
	}

	@Override
	public List<Highlight> getFieldHighlights(ISearchServerDocument serverDoc, String field, boolean multivalued) {
		return getFieldHighlights(serverDoc, field, multivalued, false, false);
	}

	@Override
	public List<Highlight> getFieldHighlights(ISearchServerDocument serverDoc, String field, boolean multivalued,
			boolean replaceMissingWithFieldValue, boolean merge) {
		return getFieldHighlights(serverDoc, field, multivalued, replaceMissingWithFieldValue, merge, false);
	}

	@Override
	public List<Highlight> getFieldHighlights(ISearchServerDocument serverDoc, String field, boolean multivalued,
			boolean replaceMissingWithFieldValue) {
		return getFieldHighlights(serverDoc, field, multivalued, replaceMissingWithFieldValue, false, false);
	}

	@Override
	public List<Highlight> getFieldHighlights(ISearchServerDocument serverDoc, String field, boolean multivalued,
			boolean replaceMissingWithFieldValue, boolean merge, boolean replaceConceptIds) {
		List<Highlight> fieldHighlights = new ArrayList<>();
		List<Object> fieldValues;
		Map<String, List<String>> highlights = serverDoc.getHighlights();
		if (highlights != null) {
			List<String> fieldHlStrings = highlights.get(field);
			if (null != fieldHlStrings) {
				for (String hlString : fieldHlStrings)
					fieldHighlights.add(new Highlight(hlString, field, serverDoc.getScore()));
			}
		}
		if ((fieldHighlights.isEmpty() && replaceMissingWithFieldValue) || (merge && multivalued)) {
			if (multivalued) {
				fieldValues = serverDoc.getFieldValues(field);
			} else {
				fieldValues = Collections.singletonList(serverDoc.get(field));
			}
			if (fieldHighlights.isEmpty()) {
				if (null != fieldValues) {
					for (Object fieldValue : fieldValues)
						fieldHighlights.add(new Highlight((String) fieldValue, field, 0f));
				}
			} else {
				// merging; the basis are the field values since those are
				// complete in any way. we look for those elements that equal
				// the highlighted items after stripping the HTML tags
				// first create a map to connect the HTML-tag-stripped
				// highlighted items with the highlighted version
				Map<String, Highlight> hlMap = new HashMap<>();
				for (Highlight hl : fieldHighlights) {
					String hlTerm = stripTags(hl.highlight);
					hlMap.put(hlTerm, hl);
				}

				List<Highlight> mergedHighlights = new ArrayList<>();
				for (int i = 0; i < fieldValues.size(); ++i) {
					String fieldValueString = String.valueOf(fieldValues.get(i));
					Highlight highlight = hlMap.get(fieldValueString);
					if (null != highlight)
						mergedHighlights.add(highlight);
					else
						mergedHighlights.add(new Highlight(fieldValueString, field, 0f));
				}
				fieldHighlights = mergedHighlights;
			}
		}
		if (fieldHighlights.isEmpty()) {
			// this message only makes sense when we wanted to merge and not
			// even got a stored value
			if (merge || replaceMissingWithFieldValue)
				log.warn(
						"Neither a field highlighting nor the field value could be retrieved for document {}, field {}.",
						serverDoc.getId(), field);
		} else if (replaceConceptIds) {
			for (Highlight hl : fieldHighlights) {
				String hlTerm = stripTags(hl.highlight);
				IConcept concept = termService.getTerm(hlTerm);
				if (null != concept) {
					List<String> tags = getTags(hl.highlight);
					hl.highlight = tags.get(0) + concept.getPreferredName() + tags.get(1);
				}
			}
		}
		// remove duplicates
		Set<String> hlset = new HashSet<>();
		for (Iterator<Highlight> hlIt = fieldHighlights.iterator(); hlIt.hasNext();) {
			if (!hlset.add(hlIt.next().highlight))
				hlIt.remove();
		}
		return fieldHighlights;
	}

	private List<String> getTags(String highlight) {
		List<String> tags = new ArrayList<>();
		htmlTagMatcher.reset(highlight);
		while (htmlTagMatcher.find())
			tags.add(htmlTagMatcher.group());
		return tags;
	}

	private synchronized String stripTags(String highlight) {
		htmlTagMatcher.reset(highlight);
		return htmlTagMatcher.replaceAll("");
	}

	@Override
	public List<Highlight> getSentenceHighlights(ISearchServerDocument serverDoc) {
		String innerHitField = IIndexInformationService.sentences;
		String highlightField = IIndexInformationService.GeneralIndexStructure.Nested.sentencestext;
		// hopefully, the first - and best - highlight tells much. For
		// events, multiple highlights won't help much because then, the
		// event elements are distributed across multiple snippets and thus
		// torn out out context
		return getInnerHitsHighlights(serverDoc, highlightField, innerHitField, 1);
	}

	private List<Highlight> getInnerHitsHighlights(ISearchServerDocument serverDoc, String highlightField,
			String innerHitField, int maxHlsPerInnerHit) {
		Map<String, List<ISearchServerDocument>> innerHits = serverDoc.getInnerHits();
		if (null == innerHits || !innerHits.containsKey(innerHitField)) {
			log.debug("Document with ID {} has no inner hits for field {}", serverDoc.getId(), innerHitField);
			return Collections.emptyList();
		}
		List<ISearchServerDocument> events = innerHits.get(innerHitField);
		List<Highlight> highlights = new ArrayList<>(events.size());
		for (int i = 0; i < events.size(); ++i) {
			ISearchServerDocument innerHit = events.get(i);
			Map<String, List<String>> innerHitHls = innerHit.getHighlights();
			if (null == innerHitHls) {
				log.warn("Document with ID {} has no highlights for inner hits of type {}.", serverDoc.getId(),
						innerHitField);
				continue;
			}
			List<String> innerFieldHls = innerHitHls.get(highlightField);
			if (null == innerFieldHls || innerFieldHls.isEmpty()) {
				log.warn("Inner hit of type {} document with ID {} has no highlights for field {}",
						innerHitField, serverDoc.getId(), highlightField);
				continue;
			}
			for (int j = 0; j < maxHlsPerInnerHit && j < innerFieldHls.size(); ++j) {
				highlights
						.add(new Highlight(addFragmentDots(innerFieldHls.get(j)), highlightField, innerHit.getScore()));
			}
		}
		return highlights.isEmpty() ? Collections.<Highlight>emptyList() : highlights;
	}

	private String addFragmentDots(String fragment) {
		// To determine whether to prefix the fragment with "..." or
		// not,
		// check if the first char is upper case (mostly sentence
		// beginning). If the char is '<', the first word is
		// highlighted, e.g. '<em>Interleukin-2<em> has proven to
		// [...]'. So the first char is the char after the closing brace
		// '>'.
		char firstChar = fragment.charAt(0);
		if (firstChar == '<')
			firstChar = fragment.charAt(fragment.indexOf('>') + 1);

		if (!Character.isUpperCase(firstChar))
			fragment = "..." + fragment;
		if (fragment.charAt(fragment.length() - 1) != '.')
			fragment = fragment + "...";
		return fragment;
	}

	/**
	 * Merges highlights in event and sentence fields
	 */
	@Override
	public List<Highlight> getBestTextContentHighlights(ISearchServerDocument serverDoc, int num,
			String... excludedTextFields) {
		// until we have something better: sort by score. Problem with that
		// approach: Scores across different searches are not directly
		// comparable.

		List<Highlight> sentenceHighlights = getSentenceHighlights(serverDoc);

		List<Highlight> sortedHighlights = new ArrayList<>(sentenceHighlights.size());

		// this is to avoid duplicates
		Set<String> seenHighlightedStrings = new HashSet<>(sortedHighlights.size());
		for (Highlight sentenceHl : sentenceHighlights) {
			String pureString = stripTags(sentenceHl.highlight);
			if (seenHighlightedStrings.add(pureString))
				sortedHighlights.add(sentenceHl);
		}

		// perhaps there we no matches for events or sentences
		if (sortedHighlights.isEmpty()) {
			List<Highlight> allTextHighlights = getFieldHighlights(serverDoc,
					IIndexInformationService.GeneralIndexStructure.alltext, false);
			sortedHighlights.addAll(allTextHighlights);
		}

		Collections.sort(sortedHighlights, new Comparator<Highlight>() {

			@Override
			public int compare(Highlight o1, Highlight o2) {
				return Float.compare(o2.docscore, o1.docscore);
			}

		});

		// this is to, for example, remove those sentences that also occur in
		// the abstract and title so we don't duplicate information
		if (excludedTextFields.length > 0) {
			StringBuilder excludedTextBuilder = new StringBuilder();
			for (int i = 0; i < excludedTextFields.length; i++) {
				String field = excludedTextFields[i];
				String text = serverDoc.getFieldValue(field);
				excludedTextBuilder.append(text);
			}
			String allExcludedText = stripTags(excludedTextBuilder.toString());
			Iterator<Highlight> hlIt = sortedHighlights.iterator();
			while (hlIt.hasNext()) {
				Highlight highlight = hlIt.next();
				if (allExcludedText.contains(stripTags(highlight.highlight)))
					hlIt.remove();
			}
		}

		// TODO solve by storing alltext field and then remove this
		if (sortedHighlights.isEmpty()) {
			List<Highlight> sectionHl = getInnerHitsHighlights(serverDoc,
					IIndexInformationService.PmcIndexStructure.Nested.SECTIONSTEXT,
					IIndexInformationService.PmcIndexStructure.SECTIONS, 4);
			// TODO the section text is not stored (this should obviously be changed...) and here we exclude highlights that reveal the preanalyzed fromat
			for (Highlight hl : sectionHl) {
				String hlString = hl.highlight;
				if (!hlString.contains("{"))
					sortedHighlights.add(hl);
			}
		}

		// if we still don't have highlights, there were no hits in the
		// document's textual body at all. We just show the abstract text
		if (sortedHighlights.isEmpty()) {
			List<Highlight> abstractHighlights = getFieldHighlights(serverDoc, IIndexInformationService.GeneralIndexStructure.abstracttext,
					false);

			if (!abstractHighlights.isEmpty()) {
				for (Highlight hl : abstractHighlights) {
					if (!hl.highlight.matches(".*\\p{Punct}$"))
						hl.highlight = hl.highlight + "...";
				}
			}
			sortedHighlights.addAll(abstractHighlights);
		}

		if (num < 0)
			return sortedHighlights;
		return sortedHighlights.subList(0, Math.min(num + 1, sortedHighlights.size()));
	}

	@Override
	public List<AuthorHighlight> getAuthorHighlights(ISearchServerDocument serverDoc) {
		if (serverDoc.get(IIndexInformationService.GeneralIndexStructure.authors) == null)
			return null;
		List<String> authorHls = serverDoc.getHighlights().get(IIndexInformationService.GeneralIndexStructure.authors);
		if (null == authorHls || authorHls.isEmpty())
			return null;
		List<AuthorHighlight> ret = new ArrayList<>();

		List<Highlight> mergedAuthors = getFieldHighlights(serverDoc, IIndexInformationService.GeneralIndexStructure.authors, true, true,
				true);
		List<Highlight> mergedAffilliations = getFieldHighlights(serverDoc,
				IIndexInformationService.GeneralIndexStructure.affiliation, true, true, true);
		for (int i = 0; i < mergedAuthors.size(); ++i) {
			Highlight mergedAuthorHighlight = mergedAuthors.get(i);
			AuthorHighlight authorHl = new AuthorHighlight();
			String[] names = mergedAuthorHighlight.highlight.split(",");
			if (names.length == 2) {
				authorHl.firstname = names[1];
				authorHl.lastname = names[0];
			} else {
				authorHl.lastname = mergedAuthorHighlight.highlight;
			}
			
			if (mergedAffilliations != null && i < mergedAffilliations.size()) {
				Highlight mergedAffiliationHighlight = mergedAffilliations.get(i);
				authorHl.affiliation = mergedAffiliationHighlight.highlight;
			}
			ret.add(authorHl);
		}
		return ret;
	}

}