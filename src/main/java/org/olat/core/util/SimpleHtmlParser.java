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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * Simple html parser that grabs css, javascript etc. stuff out of the header. Not very tolerant but at least very fast
 * Initial Date:  11.07.2004
 *
 * @author Mike Stock
 * @author guido
 */
public class SimpleHtmlParser {

	private static final Pattern COMMENT_OR_LINK = Pattern.compile("<(!--.*--|link[^>]*)>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE
			| Pattern.MULTILINE);

	private static final Pattern LINK_REL = Pattern.compile("rel\\s*=\\s*\"?(home|next|prev(ious)?|start|up)\"?",
      Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
	
	// use extra characters that the StringBuilder doesn't grow
	// the optimistic length is 5 (navigation links) * 7 (comment tags) per header
	private static final int EXTRA_HEADER_LENGTH = 64;

	private static final String CHARSET = "charset=";
	private static final String ISO_8859_1 = "ISO-8859-1";
	private static final String END_BODY_TAG = "</body>";
	private static final String END_HTML_TAG = "</html>";

	private static final Logger log = Tracing.createLoggerFor(SimpleHtmlParser.class);
	
	private String htmlDocType;
	private String xhtmlNamespaces;
	private String htmlHead;
	private String jsOnLoad;
	private String bodyTag;
	private String htmlContent;
	private String outerBodyContent;
	private String charsetName;
	private boolean validHtml;
	//does the parsed content has it's own css classes inline or a css file defined
	private boolean ownCss;

	
	/**
	 * @param htmlString
	 * @param lookForCharset
	 */
	public SimpleHtmlParser(String htmlString) {
		parseContent(htmlString);
	}
	
	/**
	 * 
	 * @param file
	 */
	public static String extractHTMLCharset(VFSLeaf sourceFile) {
		
		ByteArrayOutputStream target = new ByteArrayOutputStream(8192);
		InputStream in = sourceFile.getInputStream();
		FileUtils.copy(in, target);
		FileUtils.closeSafely(in);

		byte[] bytes = target.toByteArray();
		if (bytes.length == 0) return ISO_8859_1; //empty file

		// we assume that, in order to indicate an encoding, the html meta element
		// is used.
		// if none is specified, we assume iso-8859-1
		// 1. parse the body assuming iso-8859-1 (so us-ascii is ok and we do not
		// needs a reparse if not iso-8859-1

		String parse = new String(bytes, StandardCharsets.ISO_8859_1);

		// Parse HTML header
		// look for body start tag - some people forget to use proper header tags, also support those
		int bspos = lookForTag(parse, 0, "<body");
		
		String charset = null;
		if (bspos > 0)  charset = checkForCharset(parse.substring(0, bspos));
		//if nothing found return default
		return StringHelper.containsNonWhitespace(charset) ? charset :ISO_8859_1;
	}
	
	/**
	 * Parses the given string and looks for head, onload functions and the page body
	 * the parsed data is stored in the class variables
	 * <ul>
	 * <li />htmlHead
	 * <li />jsOnLoad
	 * <li />htmlContent
	 * </ul>
	 * @param cont
	 * @param lookForCharset
	 */
	private void parseContent(String cont) {
		// check for doctype
		int docTypePos = cont.indexOf("<!DOCTYPE");
		if (docTypePos == -1) docTypePos = cont.indexOf("<doctype");
		if (docTypePos == -1) docTypePos = cont.toLowerCase().indexOf("<doctype");
		if (docTypePos != -1 ) {
			int endOfhtmlDocTypePos = cont.indexOf(">", docTypePos);
			htmlDocType = cont.substring(docTypePos, endOfhtmlDocTypePos+1);
		}
		
		// Check for <html>: lowercase, uppercase and mixed
		int spos = cont.indexOf("<html");
		if (spos == -1) spos = cont.indexOf("<HTML");
		if (spos == -1) spos = cont.toLowerCase().indexOf("<html");
		if (spos == -1) {
			// This is not valid HTML - assume whole file is the content
			htmlContent = cont;
			if (log.isDebugEnabled()) {
				log.debug("Could not detect proper HTML content, no HTML tag found. {}", cont);
			}
			return; 
		}
		
		// find positions of body part
		int bodypos = cont.indexOf("<body");
		if (bodypos == -1) bodypos = cont.indexOf("<BODY");
		if (bodypos == -1) bodypos = cont.toLowerCase().indexOf("<body");
		if (bodypos == -1) {
			// This is not valid HTML - assume whole file is the content
			htmlContent = cont;
			if (log.isDebugEnabled()) {
				log.debug("Could not detect proper HTML content, no BODY tag found. {}", cont);
			}
			return; 
		}
		
		// Parse HTML header
		int hspos = cont.indexOf("<head>");
		if (hspos == -1) hspos = cont.indexOf("<HEAD"); // be tolerant, try also capitals
		if (hspos == -1) hspos = cont.toLowerCase().indexOf("<HEAD"); // be tolerant, try also mixed
		if (hspos != -1) { // look for end tag if start tag found
			int hepos = lookForTag(cont, hspos, "</head>");
			if ( hepos > hspos )htmlHead = cont.substring(hspos + 6, hepos);
			// extract xhtml namespaces
			if (htmlDocType != null && htmlDocType.indexOf("XHTML") > 0) {
				xhtmlNamespaces = cont.substring(spos, hspos);
			}
		} else {
			// no head tag found - use everything between HTML and BODY tag to support those crippled pages as well
			htmlHead = cont.substring((cont.indexOf(">", spos))+1, bodypos).toLowerCase();
		}
		if (htmlHead != null) {
			// Filter out base tag
			int bsPos = htmlHead.indexOf("<base ");
			if (bsPos != -1) {
				int bePos = htmlHead.indexOf('>', bsPos + 6);
				if (bePos > -1) {
					// remove base tag from head
					String beforeBase = htmlHead.substring(0, bsPos);
					htmlHead = beforeBase + htmlHead.substring(bePos);
				}
			}
			// Filter the navigation links
			// olat and firefox problem 
			htmlHead = filterHeader(htmlHead);
			// Filter out CSS definitions from HEAD
			if (htmlHead.indexOf("text/css") > 0) ownCss = true;	// required for HTML 4.01
			else if (htmlHead.indexOf("stylesheet") > 0) ownCss = true;	// "purely advisory" for HTML 5
			// Filter out character set
			charsetName = checkForCharset(htmlHead);
		}

		
		// Parse body onLoad java script function
		int jspos = cont.indexOf("onload=", bodypos);
		if (jspos == -1) jspos = cont.indexOf("onLoad=", bodypos);
		if (jspos == -1) jspos = cont.toLowerCase().indexOf("onload=", bodypos);
		int jepos = cont.indexOf(">",jspos);
		if (jspos == -1 || jepos == -1) {
			jsOnLoad = null; // no match
		} else {
			jsOnLoad = cont.substring(jspos+7, jepos);
			// trimm whitespace
			jsOnLoad = jsOnLoad.trim();
			// trimm ' or "
			if (jsOnLoad.indexOf("'") == 0 || jsOnLoad.indexOf("\"") == 0) {
				jsOnLoad = jsOnLoad.substring(1, jsOnLoad.length()-1);
			}
			// else assume no ' or " are used, should be fine for us
			// check if commands end with ;. This is necessary when doing inline integration (OLAT-3340)
			if (jsOnLoad.length() > 0 && !jsOnLoad.endsWith(";")) {
				jsOnLoad = jsOnLoad + ";";
			}
		}

		//look for class attribute of body tag
		int endOfBodyTagPos = cont.indexOf(">", bodypos);

		// get entire body tag
		bodyTag = cont.substring(bodypos, endOfBodyTagPos+1);
		
		int bepos = -1;
		if (bodypos != -1) { // look for end tag is start tag found
			bepos = lookForTag(cont, endOfBodyTagPos, END_BODY_TAG);
			if (bepos == -1) {
				// use end of html as fallback
				bepos = lookForTag(cont, endOfBodyTagPos, END_HTML_TAG);
				if (bepos == -1) {
					// use end of content as fallback
					bepos = cont.length()-1;
				}
			} else {

				int outbepos = lookForTag(cont, bepos, END_HTML_TAG);
				outerBodyContent = cont.substring(bepos + END_BODY_TAG.length(), outbepos);

			}
		}
		
		if (bodypos == -1 || bepos == -1) {
			htmlContent = cont; // no match, return the string unmodified
		} else {
			String res = cont.substring(endOfBodyTagPos+1, bepos);
			htmlContent = res;
		}
		validHtml = true;
		
		if (log.isDebugEnabled()) {			
			log.debug("original content::{}", cont);
			log.debug("xhtmlNamespaces::{}", xhtmlNamespaces);
			log.debug("charsetName::{}", charsetName);
			log.debug("htmlHead::{}", htmlHead);
			log.debug("jsOnLoad::{}", jsOnLoad);
			log.debug("bodyTag::{}", bodyTag);
			log.debug("htmlContent::{}", htmlContent);
			log.debug("outerBodyContent::{}", outerBodyContent);
		}
	}

	private static String checkForCharset(String hdr) {
		// we assume that, in order to indicate an encoding, the html meta element
		// is used
		//<html><head>
		//<meta http-equiv="Content-Type" content="text/html; charset=utf-8">
		//<title>OLAT - Online Learning And Training (Version 3)</title>
		//..
								
		/*
		 * from http://www.w3.org/TR/html401/charset.html#doc-char-set 
		 * <META http-equiv="Content-Type" content="text/html; charset=EUC-JP"> 
		 * The META
		 * declaration must only be used when the character encoding is organized
		 * such that ASCII-valued bytes stand for ASCII characters (at least until
		 * the META element is parsed). META declarations should appear as early
		 * as possible in the HEAD element.
		 */
		int charsetPos = hdr.indexOf(CHARSET);
		if (charsetPos == -1) { 
			// try it lowercase
			charsetPos = hdr.toLowerCase().indexOf(CHARSET);
		}
		if (charsetPos != -1) { // found!
			int endofmetastop = hdr.indexOf('>', charsetPos + CHARSET.length());
			if (endofmetastop != -1) {
				// found meta tag stop character, continue searching for closing quotes
				int endofcs = hdr.indexOf('"', charsetPos + CHARSET.length());
				if (endofcs == -1 || endofcs > endofmetastop)
					endofcs = hdr.indexOf('\'', charsetPos + CHARSET.length());
				if (endofcs != -1 && endofcs < endofmetastop)
					return hdr.substring(charsetPos + CHARSET.length(), endofcs).trim(); 
			}
		}
		return null;
	}

	/**
	 * Looks for a tag (both lower and upper case). Returns -1 if not present,
	 * tag position within content, otherwise.
	 * 
	 * @param tag
	 * @return -1 if not present, tag position within content, otherwise.
	 */
	private static int lookForTag(String content, int offset, String tag) {
		int tagpos = offset;
		while (true) {
			tagpos = content.indexOf(tag, tagpos);
			if (tagpos == -1) {
				tagpos = content.indexOf(tag.toUpperCase(), tagpos); // be tolerant, try also capitals
			}
			if (tagpos == -1) {
				tagpos = content.toLowerCase().indexOf(tag, tagpos); // be tolerant, try also mixed
			}
			if (tagpos == -1) break;
			if(!isWithinComment(content, offset, tagpos)) break;
			else {
				tagpos = tagpos + tag.length(); // offset new search position by found </body> tag (7 chrs)
				if (content.indexOf(tag, tagpos) == -1) {
					// only found within the comment which is equal to not found. break to prevent endless loop
					tagpos = -1;
					break;
				}
			}
		}
		return tagpos;
	}
	
	/**
	 * Check if source beginning at startPos and ending at endPos has a comment
	 * start tag which is not (yet) closed
	 * @param startPos
	 * @param endPos
	 * @return true if content has unclosed comment start tag
	 */
	private static boolean isWithinComment(String content, int startPos, int endPos) {
		if (startPos == -1 || endPos == -1) return false;
		int commentTagS = -1;
		while ((commentTagS = content.indexOf("<!--", startPos)) != -1) {
			if (commentTagS > endPos) return false; // this is not within range of interest
			int commentTagE = content.indexOf("-->", commentTagS); // look for end tag
			if (commentTagE == -1) return false; // no end tag -> break
			if (commentTagE < endPos) {
				startPos = commentTagE;
				continue; // comment is closed within start/end -> not of interest
			}
			else return true; // this is within a comment.
		}
		return false;
	}

	
	
	private String filterHeader(String in) {
		if (in == null) return null;
		StringBuffer out = new StringBuffer(in.length() + EXTRA_HEADER_LENGTH);
		Matcher m = COMMENT_OR_LINK.matcher(in);
		String match;
		while (m.find()) {
			match = m.group();
			if (isComment(match)) {
				// ignore comment
				continue;
			} else if (isNavigationLink(match)) {
				// comment the navigation link out
				m.appendReplacement(out, "<!--");
				out.append(match).append("-->");
			}
		}
		m.appendTail(out);
		return out.toString();
	}

	private boolean isComment(String tag) {
		// is it "<!--"
		return tag.charAt(1) == '!';
	}
	
	private boolean isNavigationLink(String tag) {
		// looking for rel="next" ...
		// start searching after "<link " 
	  return LINK_REL.matcher(tag).find(6);
	}
	
	public String removeLineTerminators(String in) {
		return in.replace("\\n", "");
	}
	
	/**
	 * @return Returns the htmlContent.
	 */
	public String getHtmlContent() {
		return htmlContent;
	}
	/**
	 * @return Returns the content between the end body tag ad the end html tag.
	 */
	public String getOuterBodyContent() {
		return outerBodyContent;
	}
	/**
	 * @return Returns the htmlHead.
	 */
	public String getHtmlHead() {
		return htmlHead;
	}
	/**
	 * @return Returns the jsOnLoad.
	 */
	public String getJsOnLoad() {
		return jsOnLoad;
	}
	/**
	 * @return Returns the body tag including all onload, onclick, class, style etc.
	 */
	public String getBodyTag() {
		return bodyTag;
	}
	/**
	 * @return Returns the validHtml.
	 */
	public boolean isValidHtml() {
		return validHtml;
	}
	/**
	 * @return Returns the charsetName.
	 */
	public String getCharsetName() {
		return charsetName;
	}

	public boolean hasOwnCss() {
		return ownCss;
	}

	public String getHtmlDocType() {
		return htmlDocType;
	}

	public String getXhtmlNamespaces() {
		return xhtmlNamespaces;
	}
}
