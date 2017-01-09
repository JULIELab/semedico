/**
 * JavaScriptUtils.java
 *
 * Copyright (c) 2012, JULIE Lab.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 *
 * Author: faessler
 *
 * Current version: 1.0
 * Since version:   1.0
 *
 * Creation date: 29.05.2012
 **/

/**
 * 
 */
package de.julielab.semedico.core.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Same small methods to help with JavaScript related stuff.
 * 
 * @author faessler
 *
 */
public class JavaScriptUtils {

	// -------------- Taken from Google Caja (http://code.google.com/p/google-caja/) ----
	// From class 'ParserBase':
	
	 /**
	   * String form of a regular expression that matches the javascript
	   * IdentifierOrKeyword production, with extensions for quasiliteral
	   * syntax.
	   * <p>From section 7.6 of EcmaScript 262 Edition 3 (ES3), currently found at
	   * http://www.ecma-international.org/publications/files/ECMA-ST/Ecma-262.pdf
	   * and based on http://www.erights.org/elang/grammar/quasi-overview.html
	   * <pre>
	   * <b>QuasiIdentifierOrKeyword</b> ->
	   *       IdentifierOrKeyword
	   *    |  QuasiliteralBegin IdentifierOrKeyword OptQuasiliteralQuantifier
	   * <b>IdentifierOrKeyword</b> ->
	   *       IdentifierName (but not Keyword)
	   * <b>IdentifierName</b> ->
	   *       IdentifierStart
	   *    |  IdentifierName IdentifierPart
	   * <b>IdentifierStart</b> ->
	   *       UnicodeLetter  |  $  |  _  |  \ UnicodeEscapeSequence
	   * <b>IdentifierPart</b> ->
	   *       IdentifierStart  |  UnicodeCombiningMark  |  UnicodeDigit
	   *    |  UnicodeConnectorPunctuation  |  \ UnicodeEscapeSequence
	   * <b>UnicodeLetter</b> ->
	   *       any character in the Unicode categories "Uppercase letter
	   *       (Lu)", "Lowercase letter (Ll)", "Titlecase letter (Lt)",
	   *       "Modifier letter (Lm)", "Other letter (Lo)", or "Letter
	   *       number (Nl)".
	   * <b>UnicodeCombiningMark</b> ->
	   *       any character in the Unicode categories "Non-spacing mark (Mn)"
	   *       or "Combining spacing mark (Mc)"
	   * <b>UnicodeDigit</b> ->
	   *       any character in the Unicode category "Decimal number (Nd)"
	   * <b>UnicodeConnectorPunctuation</b> ->
	   *       any character in the Unicode category "Connector punctuation (Pc)"
	   * <b>UnicodeEscapeSequence</b> ->
	   *       u HexDigit HexDigit HexDigit HexDigiti
	   * <b>HexDigit</b> ->
	   *       0  |  1  |  2  |  3  |  4  |  5  |  6  |  7  |  8  |  9  |  a
	   *    |  b  |  c  |  d  |  e  |  f  |  A  |  B  |  C  |  D  |  E  |  F
	   * <b>QuasiliteralBegin</b> ->
	   *       '@'
	   * <b>OptQuasiliteralQuantifier</b> ->
	   *       &epsilon;
	   *    |  '*'
	   *    |  '+'
	   *    |  '?'
	   * </pre>
	   * A <i>UnicodeEscapeSequence</i> cannot be used to put a character
	   * into an identifier that would otherwise be illegal.
	   */
	  private static final Pattern IDENTIFIER_OR_KEYWORD_RE;
	  private static final Pattern UNICODE_ESCAPE;  // hexDigits captured in group 1
	  static {
	    String hexDigit = "[0-9a-fA-F]";
	    String letter = "\\p{javaLetter}";
	    String letterOrDigit = "\\p{javaLetterOrDigit}";
	    String combinerOrConnector = "\\p{Mn}\\p{Mc}\\p{Pc}";
	    String identifierStart = "[" + letter + "$_]";
	    String identifierPart = "[" + letterOrDigit + combinerOrConnector + "$_]";
	    String identifierOrKeyword = identifierStart + identifierPart + "*";
	    IDENTIFIER_OR_KEYWORD_RE = Pattern.compile("^" + identifierOrKeyword + "$");

	    UNICODE_ESCAPE = Pattern.compile("\\\\u(" + hexDigit + "{4})");
	  }

	  public static boolean isJavascriptIdentifier(String s) {
	    return IDENTIFIER_OR_KEYWORD_RE.matcher(decodeIdentifier(s)).matches()
	        && isNormalized(s);
	  }


	  /**
	   * Decodes escapes in an identifier to their literal codepoints so that
	   * identifiers can be compared for equality via string equality of their
	   * values.
	   */
	  public static String decodeIdentifier(String identifier) {
	    // TODO(mikesamuel): is this true?
	    // Javascript identifiers use a different escaping scheme from strings.
	    // Specifically, \Uxxxxxxxx escapes handle extended unicode.  There are
	    // 8 hex digits allowed even though extended unicode can't use more than
	    // 6 of those.
	    if (identifier.indexOf('\\') < 0) { return identifier; }
	    StringBuffer sb = new StringBuffer();
	    Matcher m = UNICODE_ESCAPE.matcher(identifier);
	    while (m.find()) {
	      m.appendReplacement(sb, "");
	      sb.append((char) Integer.parseInt(m.group(1), 16));
	    }
	    m.appendTail(sb);
	    return sb.toString();
	  }
	
	  // From class 'Normalizer':
	  
	  private static final Method IS_NORMALIZED;
	  private static final Object NORMAL_FORM_C;

	  static {
	    Method isNormalized = null;
	    Object normalFormC = null;
	    try {
	      Class<?> normalizer = Class.forName("java.text.Normalizer");
	      Class<?> normalizerForm = Class.forName("java.text.Normalizer$Form");

	      isNormalized = normalizer.getMethod(
	          "isNormalized", CharSequence.class, normalizerForm);
	      normalFormC = normalizerForm.getField("NFC").get(null);
	    } catch (ClassNotFoundException ex) {
	      // JVM versions < 1.5 don't provide Normalizer.
	      // Use heuristic below.
	    } catch (IllegalAccessException ex) {
	      throw new RuntimeException(
	          "Normalizer exists but is unexpectedly inaccessible", ex);
	    } catch (NoSuchFieldException ex) {
	    	// AppEngine doesn't provide Normalizer.Form.
	    	// Use heuristic below.
	      throw new RuntimeException(
	          "Normalizer.Form unexpectedly missing", ex);
	    } catch (NoSuchMethodException ex) {
	        // Don't use the normalizer.
	        // Use heuristic below.
	      throw new RuntimeException(
	          "Normalizer unexpectedly missing methods", ex);
	    }

	    IS_NORMALIZED = isNormalized;
	    NORMAL_FORM_C = normalFormC;
	  }
	  
	  /**
	   * A conservative heuristic as to whether s is normalized according to Unicode
	   * Normal Form C.  It is heuristic, because Caja needs to run with versions
	   * of the Java standard libraries that do not include normalization.
	   * @return false if s is not normalized.
	   */
	  public static boolean isNormalized(CharSequence s) {
	    if (IS_NORMALIZED != null) {
	      try {
	        return ((Boolean) IS_NORMALIZED.invoke(null, s, NORMAL_FORM_C))
	            .booleanValue();
	      } catch (IllegalAccessException ex) {
	        throw new RuntimeException(
	            "Normalizer unexpectedly uninvokable", ex);
	      } catch (InvocationTargetException ex) {
	        Throwable th = ex.getTargetException();
	          throw new RuntimeException(
	              "Normalizer unexpectedly uninvokable", th);
	      }
	    }

	    // From http://unicode.org/reports/tr15/#D6
	    // Legacy character sets are classified into three categories
	    // based on their normalization behavior with accepted
	    // transcoders.
	    // 1. Prenormalized. Any string in the character set is already in
	    //    Normalization Form X.
	    //    For example, ISO 8859-1 is prenormalized in NFC.
	    // ...
	    for (int i = s.length(); --i >= 0;) {
	      char ch = s.charAt(i);
	      // Codepoints in [32, 126] U [160, 255] are identical in both Unicode and
	      // ISO 8859-1.
	      // Codepoints in [0, 31] and [127, 159] are not part of ISO 8859-1.  They
	      // are control characters in Unicode, and disallowed in identifiers so
	      // will never reach here.
	      if (ch >= 256) { return false; }
	    }
	    return true;
	  }
	  
	  // -------------- End Google Caja -------------------------------------------
}

