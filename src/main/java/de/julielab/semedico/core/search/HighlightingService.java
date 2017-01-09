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
import java.util.Arrays;
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

import de.julielab.elastic.query.components.data.ISearchServerDocument;
import de.julielab.semedico.core.HighlightedSemedicoDocument;
import de.julielab.semedico.core.HighlightedSemedicoDocument.AuthorHighlight;
import de.julielab.semedico.core.HighlightedSemedicoDocument.Highlight;
import de.julielab.semedico.core.concepts.IConcept;
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
	 * de.julielab.semedico.search.IKwicService#getHighlightedTitle(java.util
	 * .Map)
	 */
	@Deprecated
	@Override
	public String getHighlightedTitle(Map<String, List<String>> docHighlights) {
		String highlightedTitle = null;

		if (docHighlights != null) {
			List<String> titleHighlights = docHighlights.get(IIndexInformationService.TITLE);

			if (titleHighlights != null && titleHighlights.size() > 0)
				highlightedTitle = titleHighlights.get(0);
		}
		return highlightedTitle;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.julielab.semedico.search.IKwicService#getAbstractHighlights(java.util
	 * .Map)
	 */
	@Override
	public String[] getAbstractHighlights(Map<String, List<String>> docHighlights) {
		List<String> abstractHighlights = docHighlights.get(IIndexInformationService.TEXT);

		if (abstractHighlights != null && abstractHighlights.size() > 0) {
			for (int i = 0; i < abstractHighlights.size(); i++) {
				String kwic = abstractHighlights.get(i).trim();
				// To determine whether to prefix the fragment with "..." or
				// not,
				// check if the first char is upper case (mostly sentence
				// beginning). If the char is '<', the first word is
				// highlighted, e.g. '<em>Interleukin-2<em> has proven to
				// [...]'. So the first char is the char after the closing brace
				// '>'.
				char firstChar = kwic.charAt(0);
				if (firstChar == '<')
					firstChar = kwic.charAt(kwic.indexOf('>') + 1);

				if (!Character.isUpperCase(firstChar))
					kwic = "..." + kwic;
				if (kwic.charAt(kwic.length() - 1) != '.')
					kwic = kwic + "...";
				abstractHighlights.set(i, kwic);
			}

			return abstractHighlights.toArray(new String[abstractHighlights.size()]);
		}
		return null;
	}

	@Override
	public Highlight getTitleHighlight(ISearchServerDocument serverDoc) {
		Highlight titleHighlight = null;
		Map<String, List<String>> highlights = serverDoc.getHighlights();
		if (highlights != null) {
			List<String> titleHlStrings = highlights.get(IIndexInformationService.TITLE);
			if (null != titleHlStrings && !titleHlStrings.isEmpty()) {
				titleHighlight = new Highlight(titleHlStrings.get(0), IIndexInformationService.TITLE,
						serverDoc.getScore());
			}
		}
		if (null == titleHighlight) {
			// try to get the title directly from the title field
			String title = serverDoc.get(IIndexInformationService.TITLE);

			if (!StringUtils.isBlank(title))
				titleHighlight = new Highlight(title, IIndexInformationService.TITLE, serverDoc.getScore());
		}
		if (null == titleHighlight) {
			log.warn("Neither a title highlighting nor the title field value could be retrieved for document {}.",
					serverDoc.getId());
			titleHighlight = new Highlight("<title could not be retrieved; please report this error>",
					IIndexInformationService.TITLE, 0);
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
			fieldValues = new ArrayList<>();
			if (multivalued)
				fieldValues = serverDoc.getFieldValues(field);
			else
				fieldValues = Collections.singletonList(serverDoc.get(field));

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
			// fieldHighlights
			// .add(new Highlight("<" + field + " could not be retrieved; please
			// report this error>", field, 0));
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
	public List<Highlight> getEventHighlights(ISearchServerDocument serverDoc) {
		String innerHitField = IIndexInformationService.events;
		String highlightField = IIndexInformationService.GeneralIndexStructure.EventFields.sentence;
		// hopefully, the first - and best - highlight tells much. For
		// events, multiple highlights won't help much because then, the
		// event elements are distributed across multiple snippets and thus
		// torn out out context
		return getInnerHitsHighlights(serverDoc, highlightField, innerHitField, 1);
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
			log.debug("Document with ID " + serverDoc.getId() + " has no inner hits for field " + innerHitField);
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
						new Object[] { innerHitField, serverDoc.getId(), highlightField });
				continue;
			}
			for (int j = 0; j < maxHlsPerInnerHit && j < innerFieldHls.size(); ++j) {
				highlights.add(new Highlight(addFragmentDots(innerFieldHls.get(j)), highlightField, innerHit.getScore()));
			}
		}
		return highlights.isEmpty() ? Collections.<Highlight> emptyList() : highlights;
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
		List<Highlight> eventHighlights = getEventHighlights(serverDoc);

		if (sentenceHighlights.isEmpty() && eventHighlights.isEmpty()) {
			String abstracttext = serverDoc.get(IIndexInformationService.ABSTRACT);
			if (null == abstracttext)
				return null;
			return Arrays.asList(new Highlight(abbreviate(abstracttext, 200), IIndexInformationService.ABSTRACT, 0f));
		}

		List<Highlight> sortedHighlights = new ArrayList<>(sentenceHighlights.size() + eventHighlights.size());

		Set<String> seenHighlightedStrings = new HashSet<>(sortedHighlights.size());
		for (Highlight eventHl : eventHighlights) {
			String pureString = stripTags(eventHl.highlight);
			if (seenHighlightedStrings.add(pureString))
				sortedHighlights.add(eventHl);
		}
		for (Highlight sentenceHl : sentenceHighlights) {
			String pureString = stripTags(sentenceHl.highlight);
			if (seenHighlightedStrings.add(pureString))
				sortedHighlights.add(sentenceHl);
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
				HighlightedSemedicoDocument.Highlight highlight = (HighlightedSemedicoDocument.Highlight) hlIt.next();
				if (allExcludedText.contains(stripTags(highlight.highlight)))
					hlIt.remove();
			}
		}

		if (num < 0)
			return sortedHighlights;
		return sortedHighlights.subList(0, Math.min(num + 1, sortedHighlights.size()));
	}

	private String abbreviate(String text, int length) {
		if (text.length() < length)
			return text;
		String abbreviatedText = StringUtils.abbreviate(text, length);
		// abbreviatedText += "...";

		return abbreviatedText;
	}

	@Override
	public List<AuthorHighlight> getAuthorHighlights(ISearchServerDocument serverDoc) {
		if (serverDoc.get(IIndexInformationService.AUTHORS) == null)
			return null;
		List<String> authorHls = serverDoc.getHighlights().get(IIndexInformationService.AUTHORS);
		if (null == authorHls || authorHls.isEmpty())
			return null;
		List<AuthorHighlight> ret = new ArrayList<>();

		List<Highlight> mergedAuthors = getFieldHighlights(serverDoc, IIndexInformationService.AUTHORS, true, true,
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
			} else
				authorHl.lastname = mergedAuthorHighlight.highlight;

			if (mergedAffilliations != null && i < mergedAffilliations.size()) {
				Highlight mergedAffiliationHighlight = mergedAffilliations.get(i);
				authorHl.affiliation = mergedAffiliationHighlight.highlight;
			}
			ret.add(authorHl);
		}
		return ret;
	}

}