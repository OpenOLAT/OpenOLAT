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

package org.olat.core.gui.control.generic.textmarker;

import java.text.Collator;
import java.util.Locale;

import org.dom4j.Element;

/**
 * 
 * Description: The TextMarker object represents the data
 *         structure for a single text marking The CSS class for formatting and
 *         the hoover text are optional
 *         
 * @author gnaegi <www.goodsolutions.ch>
 * Initial Date: Jul 14, 2006
 *  
 */
public class TextMarker implements Comparable<TextMarker> {
	// XML names for the elements
	public static final String XML_CSS_CLASS_ELEMENT = "cssClass";
	public static final String XML_HOOVER_TEXT_ELEMENT = "hooverText";
	public static final String XML_MARKED_TEXT_ELEMENT = "markedText";
	public static final String XML_TEXT_MARKER_ELEMENT = "textMarker";
	// Default CSS classes 
	public static final String CSS_MARK_GLOSSAR = "o_tm_glossary";
	public static final String CSS_MARK_RED = "o_tm_red";
	public static final String CSS_MARK_YELLOW = "o_tm_yellow";
	public static final String CSS_MARK_GREEN = "o_tm_green";
	public static final String CSS_MARK_BLUE = "o_tm_blue";

	/** This text will be marked. Can be a ';' separated list of keywords to have alias. */
	private String markedText;
	private String hooverText;
	private String cssClass;

	/**
	 * Constructor
	 * 
	 * @param markedText Text to be marked
	 * @param cssClass CSS class, can be null
	 * @param hooverText The hoover text, can be null
	 */
	public TextMarker(String markedText, String cssClass, String hooverText) {
		this.markedText = markedText;
		this.cssClass = cssClass;
		this.hooverText = hooverText;
	}

	/**
	 * Constructor, used to create an object from an XML element
	 * 
	 * @param textMarkerElement
	 */
	public TextMarker(Element textMarkerElement) {
		Element markedTexEl = textMarkerElement.element(XML_MARKED_TEXT_ELEMENT);
		this.markedText = (markedTexEl == null ? null : markedTexEl.getStringValue());
		Element hooverEl = textMarkerElement.element(XML_HOOVER_TEXT_ELEMENT);
		this.hooverText = (hooverEl == null ? null : hooverEl.getStringValue());
		Element cssEl = textMarkerElement.element(XML_CSS_CLASS_ELEMENT);
		this.cssClass = (cssEl == null ? null : cssEl.getStringValue());
	}

	/**
	 * Adds this text marker object to the give root element as XML object
	 * 
	 * @param root
	 */
	public void addToElement(Element root) {
		Element textMarker = root.addElement(XML_TEXT_MARKER_ELEMENT);
		textMarker.addElement(XML_MARKED_TEXT_ELEMENT).addCDATA(markedText);
		if (hooverText != null) textMarker.addElement(XML_HOOVER_TEXT_ELEMENT).addCDATA(hooverText);
		if (cssClass != null) textMarker.addElement(XML_CSS_CLASS_ELEMENT).addCDATA(cssClass);
	}

	// general getters and setters
	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	public String getHooverText() {
		return hooverText;
	}

	public void setHooverText(String hooverText) {
		this.hooverText = hooverText;
	}

	/**
	 * Return the hole marked text value.
	 * @return
	 */
	public String getMarkedText() {
		return markedText;
	}
	
	/**
	 * Return only the first marked text in case of a ';'-separated keyword list.
	 * When the marked text is only a single keyword, this keyword will be return 
	 * ( equals getMarkedText() ).
	 * @return
	 */
	public String getMarkedMainText() {
		if (markedText.indexOf(";") == -1) {
			return markedText; // no ';'delimited marked-text list
		}
		return markedText.substring(0,markedText.indexOf(";"));
	}

	/**
	 * Return only all alias keyword as ';'-separated list.
	 * When the marked text is only a single keyword, "" will be return.
	 * @return Return "" when it is a single keyword
	 */
	public String getMarkedAliasText() {
		if (markedText.indexOf(";") == -1) {
			return ""; // no ';'delimited marked-text list
		}
		return markedText.substring(markedText.indexOf(";")+1, markedText.length());
	}

	public void setMarkedText(String markedText) {
		this.markedText = markedText;
	}

	/**
	 * Comparison of two TextMarker objects is based on the markedText
	 * @param arg0
	 * @return
	 */
	@Override
	public int compareTo(TextMarker arg0) {
		// only compare against other text marker objects
		return Collator.getInstance(Locale.ENGLISH).compare(this.getMarkedText(), arg0.getMarkedText());
	}
    
    /**
     * Check only marked text and ignore case
     */
	@Override
    public boolean equals(Object obj) {
        if (obj instanceof TextMarker) {
            TextMarker tm = (TextMarker)obj;
            if ( getMarkedText().equalsIgnoreCase(tm.getMarkedText()) ) {
                return true;
            }
        }
        return false;
    }
}