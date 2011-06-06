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

package de.julielab.semedico.search;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.TokenGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.julielab.lucene.IIndexReaderWrapper;
import de.julielab.semedico.IndexFieldNames;
import de.julielab.semedico.core.DocumentHit;
import de.julielab.semedico.core.SemedicoDocument;

public class KwicService implements IKwicService {

	private IIndexReaderWrapper indexReaderWrapper;
	private static final Logger logger = LoggerFactory.getLogger(KwicService.class);
	private SpanCSSFormatter spanCSSFormatter;

	private class SpanCSSFormatter implements Formatter {

		String cssClass;

		public String highlightTerm(String orig, TokenGroup grp) {
			if (grp.getTotalScore() > 0)
				return "<span class=\"" + cssClass + "\">" + orig + "</span>";

			return orig;
		}

		public String getCssClass() {
			return cssClass;
		}

		public void setCssClass(String cssClass) {
			this.cssClass = cssClass;
		}
	}

	public KwicService() throws CorruptIndexException, IOException {

		spanCSSFormatter = new SpanCSSFormatter();
	}

//	public String[] getAbstractKwicsForQuery(String query, String origText,
//			Integer luceneDocumentId, int numberOfFragments)
//			throws IOException, org.apache.lucene.queryParser.ParseException,
//			InvalidTokenOffsetsException {
//		if (origText == null || origText.length() == 0)
//			return null;
//		IndexReader reader = indexReaderWrapper.getIndexReader();
//		long time = System.currentTimeMillis();
//		QueryParser queryParser = new QueryParser("text",
//				new WhitespaceAnalyzer());
//		TokenStream tokenStream = TokenSources.getTokenStream(reader,
//				luceneDocumentId, "text");
//		BufferedTokenStream bufferedTokenStream = new BufferedTokenStream(
//				tokenStream, 10);
//
//		spanCSSFormatter.setCssClass("highlight");
//		Highlighter highlighter = new Highlighter(spanCSSFormatter,
//				new CoOccurrenceQueryScorer(bufferedTokenStream,
//						queryParser.parse(query)));
//
//		// Get 3 best fragments and separate with a "..."
//		logger.info("orig text length " + origText.length());
//		numberOfFragments = numberOfFragments > 3 ? numberOfFragments : 3;
//
//		TextFragment[] fragments = highlighter.getBestTextFragments(
//				bufferedTokenStream, origText, false, numberOfFragments);
//
//		Collection<String> result = new ArrayList<String>();
//
//		for (int i = 0; i < fragments.length; i++) {
//			if (fragments[i].getScore() > 0.0)
//				result.add(fragments[i].toString());
//		}
//
//		time = System.currentTimeMillis() - time;
//		logger.info("kwic generation needs " + time + " ms");
//		return result.toArray(new String[result.size()]);
//	}
//
	// TODO Let Solr do this.
	public String getTitleKwicForQuery(String query, String origText, String cssClass) throws IOException,
			org.apache.lucene.queryParser.ParseException,
			InvalidTokenOffsetsException {
		
		return origText;
//		long time = System.currentTimeMillis();
//
//		QueryParser queryParser = new QueryParser("title",
//				new WhitespaceAnalyzer());
//		TokenStream tokenStream = TokenSources.getTokenStream("title", origText, new WhitespaceAnalyzer());
//		BufferedTokenStream bufferedTokenStream = new BufferedTokenStream(
//				tokenStream, 10);
//		spanCSSFormatter.setCssClass(cssClass);
//		Highlighter highlighter = new Highlighter(spanCSSFormatter,
//				new CoOccurrenceQueryScorer(bufferedTokenStream,
//						queryParser.parse(query)));
//
//		highlighter.setTextFragmenter(new NullFragmenter());
//
//		logger.info("orig text length " + origText.length());
//		String result = highlighter.getBestFragment(bufferedTokenStream,
//				origText);
//		time = System.currentTimeMillis() - time;
//		logger.info("kwic generation needs " + time + " ms");
//
//		if (result == null || result.equals(""))
//			return origText;
//
//		return result;
	}
//
//	public void createHitKwic(String query, DocumentHit hit,
//			int numberOfFragments) throws IOException {
//		SemedicoDocument document = hit.getDocument();
//		String abstractText = document.getAbstractText();
//		String titleText = document.getTitle();
//
//		try {
//			String kwics[] = null;
//			String summary = null;
//			String highlightedTitle = null;
//			if (query != null && !query.equals("")) {
//				if (abstractText != null)
//					kwics = getAbstractKwicsForQuery(query, abstractText,
//							document.getLuceneId(), numberOfFragments);
//				if (titleText != null)
//					highlightedTitle = getTitleKwicForQuery(query, titleText,
//							document.getLuceneId(), "titleHighlight");
//			}
//
//			if (kwics == null || kwics.length == 0) {
//				if (abstractText != null && abstractText.length() > 250)
//					summary = abstractText.substring(0, 250) + "...";
//				else
//					summary = abstractText;
//
//				hit.setKwicAbstractText(summary);
//			} else {
//				for (int i = 0; i < kwics.length; i++) {
//					String kwic = kwics[i].trim();
//					char firstChar = kwic.charAt(0);
//					if (firstChar == '<')
//						firstChar = kwic.charAt(kwic.indexOf('>') + 1);
//
//					if (!Character.isUpperCase(firstChar))
//						kwic = "..." + kwic;
//					if (kwic.charAt(kwic.length() - 1) != '.')
//						kwic = kwic + "...";
//					kwics[i] = kwic;
//				}
//
//				hit.setKwics(kwics);
//			}
//
//			if (highlightedTitle == null)
//				highlightedTitle = titleText;
//
//			hit.setKwicTitle(highlightedTitle);
//
//		} catch (ParseException e) {
//			throw new IOException(e.getMessage());
//		} catch (InvalidTokenOffsetsException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//	}
//
//	public void createHitKwics(String query, List<DocumentHit> hits,
//			int numberOfFragments) throws IOException {
//
//		for (DocumentHit hit : hits) {
//			createHitKwic(query, hit, numberOfFragments);
//		}
//	}
	// 	TODO Let Solr do this.
	public String createHighlightedAbstract(String query,
			SemedicoDocument document) throws IOException {

		if (document.getAbstractText() == null
				|| document.getAbstractText().length() == 0)
			return null;
		return document.getAbstractText();
		
//		long time = System.currentTimeMillis();
//		QueryParser queryParser = new QueryParser("text",
//				new WhitespaceAnalyzer());
//		TokenStream tokenStream = TokenSources.getTokenStream("text", document.getAbstractText(), new WhitespaceAnalyzer());
//		BufferedTokenStream bufferedTokenStream = new BufferedTokenStream(
//				tokenStream, 10);
//		Highlighter highlighter;
//
//		spanCSSFormatter.setCssClass("highlightFull");
//
//		try {
//			highlighter = new Highlighter(spanCSSFormatter,
//					new CoOccurrenceQueryScorer(bufferedTokenStream,
//							queryParser.parse(query)));
//			highlighter.setTextFragmenter(new NullFragmenter());
//		} catch (ParseException e) {
//			throw new IOException(e);
//		}
//
//		logger.info("orig text length " + document.getAbstractText().length());
//
//		String result = null;
//		try {
//			result = highlighter.getBestFragment(bufferedTokenStream,
//					document.getAbstractText());
//		} catch (InvalidTokenOffsetsException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//
//		time = System.currentTimeMillis() - time;
//		logger.info("kwic generation needs " + time + " ms");
//		return result;
	}

	public String createHighlightedTitle(String query, SemedicoDocument document)
			throws IOException {
		String highlightedTitle = null;

		try {
			highlightedTitle = getTitleKwicForQuery(query, document.getTitle(), "highlightFull");
		} catch (ParseException e) {
			throw new IOException(e);
		} catch (InvalidTokenOffsetsException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return highlightedTitle;
	}

	@Override
	public DocumentHit createDocumentHit(SemedicoDocument document,
			Map<String, Map<String, List<String>>> highlighting) {

		DocumentHit hit = new DocumentHit(document);
		Map<String, List<String>> docHighlights = highlighting.get(String
				.valueOf(document.getPubmedId()));

		
		
		setAbstractHighlights(document, hit, docHighlights);

		setTitleHighlights(document, hit, docHighlights);

		return hit;
	}

	private void setTitleHighlights(SemedicoDocument document, DocumentHit hit,
			Map<String, List<String>> docHighlights) {
		String highlightedTitle = null;
		List<String> titleHighlights = docHighlights.get(IndexFieldNames.TITLE);
		if (titleHighlights != null && titleHighlights.size() > 0)
			highlightedTitle = titleHighlights.get(0);

		
		if (highlightedTitle == null)
			highlightedTitle = document.getTitle();

		hit.setKwicTitle(highlightedTitle);
	}

	private void setAbstractHighlights(SemedicoDocument document,
			DocumentHit hit, Map<String, List<String>> docHighlights) {
		String abstractText = document.getAbstractText();
		
		List<String> abstractHighlights = docHighlights.get(IndexFieldNames.TEXT);
		
		if (abstractHighlights == null || abstractHighlights.size() == 0) {
			String summary = null;
			if (abstractText != null && abstractText.length() > 250)
				summary = abstractText.substring(0, 250) + "...";
			else
				summary = abstractText;

			hit.setKwicAbstractText(summary);
		} else {
			for (int i = 0; i < abstractHighlights.size(); i++) {
				String kwic = abstractHighlights.get(i).trim();
				// To judge whether to prefix the fragment with "..." or not,
				// check whether the first char is upper case (mostly sentence
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

			hit.setKwics(abstractHighlights.toArray(new String[abstractHighlights.size()]));
		}
	}

	
//	public IIndexReaderWrapper getIndexReaderWrapper() {
//		return indexReaderWrapper;
//	}
//
//	public void setIndexReaderWrapper(IIndexReaderWrapper indexReaderWrapper) {
//		this.indexReaderWrapper = indexReaderWrapper;
//	}

}