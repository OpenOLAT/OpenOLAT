/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.filter.impl.NekoHTMLScanner;
import org.olat.core.util.filter.impl.OWASPAntiSamyXSSFilter;

import com.thoughtworks.xstream.core.util.Base64Encoder;

/**
 * enclosing_type Description: <br>
 * helper class for formating Strings (not locale specific)
 * 
 * @author Felix Jost
 */
public class StringHelper {
	
	private static final OLog log = Tracing.createLoggerFor(StringHelper.class);

	private static final NumberFormat numFormatter;
	private static final String WHITESPACE_REGEXP = "^\\s*$";
	private static final Pattern WHITESPACE_PATTERN = Pattern.compile(WHITESPACE_REGEXP);
	
	private static final int LONG_MAX_LENGTH = Long.toString(Long.MAX_VALUE).length();
	
	/**
	 * regex for not allowing
	 * <code>;,:</code> <code>ALL_WITHOUT_COMMA_2POINT_STRPNT</code>
	 */
	public static final String ALL_WITHOUT_COMMA_2POINT_STRPNT = "^[^,;:]*$";
	private static final Pattern ALL_WITHOUT_COMMA_2POINT_STRPNT_PATTERN = Pattern.compile(ALL_WITHOUT_COMMA_2POINT_STRPNT);
	private static final String X_MAC_ENC = "x-mac-";
	private static final String MAC_ENC = "mac";

	static {
		DecimalFormatSymbols dfs = new DecimalFormatSymbols();
		dfs.setDecimalSeparator('.');
		numFormatter = new DecimalFormat("#.#", dfs);
	}
	
	/**
	 * unused
	 * 
	 * @param in
	 * @param delim
	 * @return List
	 */
	public static List<String> getParts(String in, String delim) {
		List<String> li = new ArrayList<String>();
		String part;
		int delimlen = delim.length();
		int oldpos = 0;
		int k;
		while ((k = in.indexOf(delim, oldpos)) != -1) {
			part = in.substring(oldpos, k);
			li.add(part);
			oldpos = k + delimlen;
		}
		if (oldpos != 0) { // min. ein Trennzeichen -> nimm rest
			part = in.substring(oldpos);
			li.add(part);
		}
		return li;
	}

	/**
	 * @param date
	 * @param locale
	 * @return formatted date
	 */
	public static String formatLocaleDate(long date, Locale locale) {
		if (date == -1) return "-";
		return DateFormat.getDateInstance(DateFormat.SHORT, locale).format(new Date(date));
	}
	
	/**
	 * @param date
	 * @param locale
	 * @return formatted date
	 */
	public static String formatLocaleDateFull(long date, Locale locale) {
		if (date == -1) return "-";
		return DateFormat.getDateInstance(DateFormat.FULL, locale).format(new Date(date));
	}
	
	/**
	 * 
	 * @param date
	 * @param locale
	 * @return Formatted date
	 */
	public static String formatLocaleDateFull(Date date, Locale locale) {
		if (date == null) return "-";
		return DateFormat.getDateInstance(DateFormat.FULL, locale).format(date);
	}

	/**
	 * @param date
	 * @param locale
	 * @return formatted date/time
	 */
	public static String formatLocaleDateTime(long date, Locale locale) {
		if (date == -1) return "-";
		return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale).format(new Date(date));
	}

	/**
	 * @param time
	 * @param locale
	 * @return formatted time
	 */
	public static String formatLocaleTime(long time, Locale locale) {
		if (time == -1) return "-";
		return DateFormat.getTimeInstance(DateFormat.SHORT, locale).format(new Date(time));
	}
	
	/**
	 * 
	 * @param time
	 * @param locale
	 * @return
	 */
	public static String formatLocaleTime(Date time, Locale locale) {
		if (time == null) return "-";
		return DateFormat.getTimeInstance(DateFormat.SHORT, locale).format(time);
	}

	/**
	 * @param f
	 * @param fractionDigits
	 * @return formatted float
	 */
	public static String formatFloat(float f, int fractionDigits) {
		numFormatter.setMaximumFractionDigits(fractionDigits);
		return numFormatter.format(f);
	}
	
	/**
	 * @param url
	 * @return encoded string
	 */
	public static String urlEncodeUTF8(String url) {
		String encodedURL;
		try {
			encodedURL = URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			/*
			 * from java.nio.Charset Standard charsets Every implementation of the
			 * Java platform is required to support the following standard charsets...
			 * ... UTF-8 Eight-bit UCS Transformation Format ...
			 */
			throw new AssertException("utf-8 encoding is needed for proper encoding, but not offered on this java platform????");
		}
		return encodedURL;
	}
	
	/**
	 * Encode the string into a Base64 string.
	 * 
	 * @param unencoded The string to encode
	 * @return The encoded string
	 */
	public static String encodeBase64(String unencoded) {
		return new Base64Encoder().encode(unencoded.getBytes());
	}
	
	/**
	 * Encode the string into a Base64 string.
	 * 
	 * @param unencoded The bytes to encode
	 * @return The encoded string
	 */
	public static String encodeBase64(byte[] unencoded) {
		return new Base64Encoder().encode(unencoded);
	}
	
	/**
	 * Decode a Base 64 string.
	 * 
	 * @param encoded The string to decode
	 * @return The decoded string
	 */
	public static String decodeBase64(String encoded) {
		byte[] decoded = new Base64Encoder().decode(encoded);
		return new String(decoded);
	}

	/**
	 * Converts all keys of a hash map to a string array.
	 * 
	 * @param m The (hash) map with the key and values
	 * @return The string array containing all keys for this map
	 */
	public static String[] getMapKeysAsStringArray(Map<String,?> m) {
		return m.keySet().toArray(new String[m.size()]);
	}

	/**
	 * Converts all values of a hash map to a string array.
	 * 
	 * @param m The (hash) map with the key and values
	 * @return The string array containing all values for this map
	 */
	public static String[] getMapValuesAsStringArray(Map<?,String> m) {
		return m.values().toArray(new String[m.size()]);
	}

	/**
	 * matches any but ^[^,;:]*$
	 * 
	 * @param s
	 * @return true if does not match regexp
	 */
	public static boolean containsNoneOfCoDouSemi(String s) {
		if (s == null) return false;
		Matcher m = ALL_WITHOUT_COMMA_2POINT_STRPNT_PATTERN.matcher(s);
		return m.find();
	}

	/**
	 * Checks if a string has anything in it to display. Will return true if the
	 * string is not null and does contain at least one none-whitespace character.
	 * 
	 * @param s The String to be evaluated
	 * @return true if the string contains any non-whitespace character, false
	 *         otherwhise
	 */
	public static boolean containsNonWhitespace(String s) {
		if (s == null || s.length() == 0) return false;
		
		char firstChar = s.charAt(0);
		if(firstChar > 32 && firstChar < 127) {
			return true;
		}

		Matcher matcher = WHITESPACE_PATTERN.matcher(s);
		// if string matches whitespace pattern then string does not
		// contain non-whitespace
		return !matcher.find();
	}
	
	/**
	 * Check if the string contains some HTML tags
	 * @param s
	 * @return
	 */
	public static boolean isHtml(String s) {
		if (s == null) return false;
		
		boolean containsHtml = new NekoHTMLScanner().scan(s);
		return containsHtml;
	}

	/**
	 * takes an array of Identies and converts them to a String containing the
	 * Identity-Emails separated by a <b>, </b>. The returned String can be fed
	 * directly to the e-mailer helper as the e-mail to field. <br>
	 * <ul>
	 * <li>Entries in the parameter emailRecipientIdentites are expected to be
	 * not null.</li>
	 * </ul>
	 * 
	 * @param emailRecipientIdentities
	 * @return "email1, email2, email3," or null if emailRecipientIdentites was
	 *         null
	 */
	public static String formatIdentitesAsEmailToString(final Identity[] emailRecipientIdentities) {
		int elCnt = emailRecipientIdentities.length;
		//2..n recipients
		StringBuilder tmpDET = new StringBuilder();
		for (int i = 0; i < elCnt; i++) {
			tmpDET.append(emailRecipientIdentities[i].getUser().getProperty(UserConstants.EMAIL, null));
			if (i < elCnt - 1) {
				tmpDET.append(", ");
			}
		}
		return tmpDET.toString();
	}

	/**
	 * takes a List containing email Strings and converts them to a String
	 * containing the Email Strings separated by a <b>, </b>. The returned String
	 * can be fed directly to the e-mailer helper as the e-mail to field. <br>
	 * <ul>
	 * <li>Entries in the parameter emailRecipients are expected to be not null
	 * and of Type String.</li>
	 * </ul>
	 * 
	 * @param emailRecipients
	 * @param delimiter
	 * @return "email1, email2, email3," or null if emailRecipientIdentites was
	 *         null
	 */
	public static String formatIdentitesAsEmailToString(final List<String> emailRecipients, String delimiter) {
		int elCnt = emailRecipients.size();
		//2..n recipients
		StringBuilder tmpDET = new StringBuilder();
		for (int i = 0; i < elCnt; i++) {
			tmpDET.append(emailRecipients.get(i));
			if (i < elCnt - 1) {
				tmpDET.append(delimiter);
			}
		}
		return tmpDET.toString();
	}
	
	public static final String escapeHtml(String str) {
		return StringEscapeUtils.escapeHtml(str);
	}
	
	public static final void escapeHtml(Writer writer, String str) {
		try {
			StringEscapeUtils.escapeHtml(writer, str);
		} catch (IOException e) {
			log.error("Error escaping HTML", e);
		}
	}
	
	public static final String xssScan(String str) {
		return new OWASPAntiSamyXSSFilter().filter(str);
	}
	
	public static final String xssScan(StringBuilder str) {
		if(str == null) return null;
		if(str.length() == 0) return "";
		return new OWASPAntiSamyXSSFilter().filter(str.toString());
	}
	
	public static final boolean xssScanForErrors(String str) {
		OWASPAntiSamyXSSFilter filter = new OWASPAntiSamyXSSFilter();
		filter.filter(str);
		return filter.getNumOfErrors() > 0;
	}
	
	public static final String escapeJavaScript(String str) {
		return StringEscapeUtils.escapeJavaScript(str);
	}
	
	public static final void escapeJavaScript(Writer writer, String str) {
		try {
			StringEscapeUtils.escapeJavaScript(writer, str);
		} catch (IOException e) {
			log.error("Error escaping JavaScript", e);
		}
	}

	/**
	 * @param cellValue
	 * @return stripped string
	 */
	public static String stripLineBreaks(String cellValue) {
		cellValue = cellValue.replace('\n', ' ');
		cellValue = cellValue.replace('\r', ' ');
		return cellValue;
	}
	
	/**
	 * transforms a displayname to a name that causes no problems on the filesystem
	 * (e.g. Webclass Energie 2004/2005 -> Webclass_Energie_2004_2005)
	 * 
	 * @param s
	 * @return transformed string
	 */
	public static String transformDisplayNameToFileSystemName(String s){
		//replace some separator with an underscore
		s = s.replace('?', '_').replace('\\', '_').replace('/', '_').replace(' ', '_');
		//remove all non-ascii characters
		return FileUtils.normalizeFilename(s);
	}
	
	/**
	 * The method do only a precheck if the string can be a number. It's goal
	 * is to prevent to generate hunderds of exceptions in a loop by using
	 * the Long.parseLong() method (exceptions is time and CPU intensive).
	 * 
	 * return True if the string can be a digit (there is not boundaries check)
	 */
	public static boolean isLong(String string) {
		if(string == null || string.length() == 0) {
			return false;
		}
		int stop = string.startsWith("-") ? 1 : 0;
		if(string.length() > LONG_MAX_LENGTH + stop) {
			return false;
		}
		char[] charArr = string.toCharArray();
		for(int i=charArr.length; i-->stop; ) {
			char ch = charArr[i];
			if(ch < 48 || ch > 57) {
				return false;
			}
		}
		return true;
	}
	
	public static String cleanUTF8ForXml(String string) {
		if(string == null) return null;
		if(string.length() == 0) return string;
		
		StringBuilder sb = new StringBuilder();
		char[] charArr = string.toCharArray();
		int numOfCharacters = charArr.length;
		for(int i=0; i<numOfCharacters; i++) {
			char ch = charArr[i];
			if(ch < 32) {
				switch(ch) {
					case '\n': sb.append(ch); break;//0x000A
					case '\t': sb.append(ch); break;//0x0009
					case '\r': sb.append(ch); break;//0x000D
				}
			} else if(ch >= 0x0020 && ch <= 0xD7FF) {
				sb.append(ch);
			} else if(ch >= 0xE000 && ch <= 0xFFFD) {
				sb.append(ch);
			} else if(ch >= 0x10000 && ch <= 0x10FFFF) {
				sb.append(ch);
			}
		}
		return sb.toString();
	}
	
	public static String replaceAllCaseInsensitive(String expression, String name, String replacement) {
		if(!StringHelper.containsNonWhitespace(expression)) {
			return expression;
		}
		
		String lcExpresion = expression.toLowerCase();
		String lcName = name.toLowerCase();

		int index = 0;
		while((index = lcExpresion.indexOf(lcName, index)) > 0) {
			int startIndex = index;
			int stopIndex = index + lcName.length();
			
			String newExpression = expression.substring(0, startIndex);
			newExpression += replacement;
			newExpression += expression.substring(stopIndex);
			
			expression = newExpression;
			lcExpresion = expression.toLowerCase();
			index = startIndex + replacement.length();	
		}
		return expression;
	}

	/**
	 * @param extractedCharset
	 * @return
	 */
	public static String check4xMacRoman(String extractedCharset) {
		//OLAT-1844
		//TODO:pb: why do http encoding names not match java encoding names?
		//the encoding name 'x-mac-roman' must be translated to javas 'x-MacRoman'
		//but it must be x-mac-roman for browser and htmleditor.. weird naming problem.
		if(extractedCharset == null) return null;
		if(extractedCharset.toLowerCase().startsWith(X_MAC_ENC)) {
			String tmp = extractedCharset.substring(6);
			String first = tmp.substring(0, 1);
		  tmp = tmp.substring(1);
		  //e.g. convert 'x-mac-roman' to 'x-MacRoman'
			extractedCharset = "x-Mac"+first.toUpperCase()+tmp;
			return extractedCharset;
		} else if (extractedCharset.toLowerCase().startsWith(MAC_ENC)) {
			//word for macintosh creates charset=macintosh which java does not know, load with iso-8859-1
			return "iso-8859-1";
		}
		return extractedCharset;
	}
	
	/**
	 * set of strings to one string comma separated.<br>
	 * e.g. ["a","b","c","s"] -> "a,b,c,s" 
	 * @param selection
	 * @return
	 */
	public static String formatAsCSVString(Set<String> entries) {
		boolean isFirst = true;
		String csvStr = null;
		for (Iterator<String> iter = entries.iterator(); iter.hasNext();) {
			String group = iter.next();
			if (isFirst) {
				csvStr = group;
				isFirst = false;
			} else {
				csvStr += ", " + group;
			}
		}
		return csvStr;
	}
	
	/**
	 * list of strings to one string comma separated.<br>
	 * e.g. ["a","b","c","s"] -> "a,b,c,s" 
	 * @param selection
	 * @return
	 */
	public static String formatAsCSVString(List<String> entries) {
		boolean isFirst = true;
		String csvStr = null;
		for (Iterator<String> iter = entries.iterator(); iter.hasNext();) {
			String group = iter.next();
			if (isFirst) {
				csvStr = group;
				isFirst = false;
			} else {
				csvStr += ", " + group;
			}
		}
		return csvStr;
	}
	
	/**
	 * list of strings to one string comma separated.<br>
	 * e.g. ["z","a","b","c","s","a"] -> "a, b, c, s, z"
	 * No duplicates, alphabetically sorted
	 * @param selection
	 * @return
	 */
	public static String formatAsSortUniqCSVString(List<String> s) {
		
		Map <String,String>u = new HashMap<String,String>();
		for (Iterator <String> si = s.iterator(); si.hasNext();) {
			u.put(si.next().trim(), null);
		}
		
		List <String>rv = new ArrayList<String>();
		rv.addAll(u.keySet());
		rv.remove("");
		Collections.sort(rv);
		
		return formatAsCSVString (rv);
	}
	
	/**
	 * list of strings to one string comma separated.<br>
	 * e.g. ["z","a","b","c","s","a"] -> "a, b, c, s, z"
	 * No duplicates, alphabetically sorted
	 * @param selection
	 * @return
	 */
	public static String formatAsSortUniqCSVString(Set<String> s) {
		
		Map <String,String>u = new HashMap<String,String>();
		for (Iterator <String> si = s.iterator(); si.hasNext();) {
			u.put(si.next().trim(), null);
		}
		
		List <String>rv = new ArrayList<String>();
		rv.addAll(u.keySet());
		rv.remove("");
		Collections.sort(rv);
		
		return formatAsCSVString (rv);
	}
}