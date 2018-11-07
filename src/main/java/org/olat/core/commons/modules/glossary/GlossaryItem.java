/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.commons.modules.glossary;

import java.net.URI;
import java.text.Collator;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Description:<br>
 * Represents a single entry in glossary. 
 * 
 * <P>
 * Initial Date: 11.12.2008 <br>
 * 
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
public class GlossaryItem implements Comparable<Object> {

	private String glossTerm;
	private String glossDef;
	private List<GlossaryItem> glossSeeAlso;
	private List<String> glossFlexions;
	private List<String> glossSynonyms;
	private List<URI> glossLinks;
	private List<Revision> revHistory;

	public GlossaryItem(String glossTerm, String glossDef) {
		super();
		this.glossTerm = glossTerm;
		this.glossDef = glossDef;
	}

	/**
	 * returns first character from the Term as a String should return an
	 * alphanumerical in uppercase. make sure its uppercased only, if not
	 * numerical
	 */
	public String getIndex() {
		if (getGlossTerm().length()!=0){
			String firstChar = getGlossTerm();
			firstChar = Normalizer.normalize(firstChar, Normalizer.Form.NFD).substring(0,1);
			return firstChar.toUpperCase();
		} else {
			return "";
		}
	}

	/**
	 * Comparison of two GlossaryItem objects is based on the Term
	 * 
	 * @param arg0
	 * @return
	 */
	@Override
	public int compareTo(Object arg0) {
		// only compare against other GlossaryItem objects
		if (arg0 instanceof GlossaryItem) {
			GlossaryItem arg0Marker = (GlossaryItem) arg0;
			return Collator.getInstance(Locale.ENGLISH).compare(this.getGlossTerm(), arg0Marker.getGlossTerm());
		}
		return 0;
	}

	/**
	 * Check only term and ignore case
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof GlossaryItem) {
			GlossaryItem glossItem = (GlossaryItem) obj;
			if (getGlossTerm().equalsIgnoreCase(glossItem.getGlossTerm())) { return true; }
		}
		return false;
	}

	/**
	 * Delivers a List with all terms, which afterwards need to be highlighted in
	 * Text. contains glossTermn, glossSynonyms and glossFlexions.
	 * 
	 * @return allStrings
	 */
	public List<String> getAllStringsToMarkup() {
		List<String> allStrings = new ArrayList<>();
		allStrings.add(getGlossTerm());
		allStrings.addAll(getGlossSynonyms());
		allStrings.addAll(getGlossFlexions());
		return allStrings;
	}

	@Override
	public String toString() {
		return getGlossTerm();
	}
	
	/**
	 * @return Return the list of revisions
	 */
	public List<Revision> getRevHistory() {
		if(revHistory == null) {
			revHistory = new ArrayList<>();
		}
		return revHistory;
	}

	/**
	 * @param revHistory The list of revisions
	 */
	public void setRevHistory(List<Revision> revHistory) {
		this.revHistory = revHistory;
	}

	/**
	 * @return Returns the glossFlexions.
	 */
	public List<String> getGlossFlexions() {
		if (glossFlexions == null) return new ArrayList<>();
		return glossFlexions;
	}

	/**
	 * @param glossFlexions The glossFlexions to set.
	 */
	public void setGlossFlexions(List<String> glossFlexions) {
		this.glossFlexions = glossFlexions;
	}

	/**
	 * @return Returns the glossSynonyms.
	 */
	public List<String> getGlossSynonyms() {
		if (glossSynonyms == null) return new ArrayList<>();
		return glossSynonyms;
	}

	/**
	 * @param glossSynonyms The glossSynonyms to set.
	 */
	public void setGlossSynonyms(List<String> glossSynonyms) {
		this.glossSynonyms = glossSynonyms;
	}

	/**
	 * @return Returns the glossDef.
	 */
	public String getGlossDef() {
		return glossDef;
	}

	/**
	 * @param glossDef The glossDef to set.
	 */
	public void setGlossDef(String glossDef) {
		this.glossDef = glossDef;
	}

	/**
	 * @return Returns the glossLinks.
	 */
	public List<URI> getGlossLinks() {
		return glossLinks;
	}

	/**
	 * @param glossLinks The glossLinks to set.
	 */
	public void setGlossLinks(List<URI> glossLinks) {
		this.glossLinks = glossLinks;
	}

	/**
	 * @return Returns the glossSeeAlso.
	 */
	public List<GlossaryItem> getGlossSeeAlso() {
		return glossSeeAlso;
	}

	/**
	 * @param glossSeeAlso The glossSeeAlso to set.
	 */
	public void setGlossSeeAlso(List<GlossaryItem> glossSeeAlso) {
		this.glossSeeAlso = glossSeeAlso;
	}

	/**
	 * @return Returns the glossTerm.
	 */
	public String getGlossTerm() {
		return glossTerm;
	}

	/**
	 * @param glossTerm The glossTerm to set.
	 */
	public void setGlossTerm(String glossTerm) {
		this.glossTerm = glossTerm;
	}

}
