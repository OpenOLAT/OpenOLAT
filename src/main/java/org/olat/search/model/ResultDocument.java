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
*/

package org.olat.search.model;

import org.apache.lucene.document.Document;
import org.olat.core.util.Formatter;
import org.olat.core.util.filter.FilterFactory;

/**
 * Lucene document mapper.
 * @author Christian Guretzki
 */
public class ResultDocument extends AbstractOlatDocument {

	private static final long serialVersionUID = 1031183603445282909L;
	/* Max text length of description field. */
	private static final int MAX_DESCRIPTION_LENGTH = 120;
	private String highlightResult = "";
	private String highlightTitle = "";
	private boolean isHighlightingDescription;
	private int lucenePosition;
	
	public ResultDocument(Document document, int lucenePosition) {
		super(document);
		this.lucenePosition = lucenePosition;
	}
	
	public String getHighlightResult() {
		return highlightResult;
	}

	public void setHighlightResult(String highlightResult) {
		this.highlightResult = highlightResult;
	}

	public String getHighlightTitle() {
		return highlightTitle;
	}

	public void setHighlightTitle(String highlightTitle) {
		this.highlightTitle = highlightTitle;
	}

	/**
	 * @return Returns the isHighlightingDescription.
	 */
	public boolean isHighlightingDescription() {
		return isHighlightingDescription;
	}

	/**
	 * @param isHighlightingDescription The isHighlightingDescription to set.
	 */
	public void setHighlightingDescription(boolean isHighlightingDescription) {
		this.isHighlightingDescription = isHighlightingDescription;
	}

	/**
	 * @return Returns the description.
	 */
	@Override
	public String getDescription() {
		if (description == null) {
			return ""; // Do not return null
		}
		if (description.length() > MAX_DESCRIPTION_LENGTH) {
			String truncatedDesc = FilterFactory.getHtmlTagAndDescapingFilter().filter(description);
			truncatedDesc = Formatter.truncate(truncatedDesc, MAX_DESCRIPTION_LENGTH);
			return truncatedDesc + "...";
		} else {
			return description;
		}
	}

	public int getLucenePosition() {
		return lucenePosition;
	}
}
