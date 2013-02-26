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

package org.olat.search.ui;

import org.olat.core.gui.control.Event;
import org.olat.search.model.ResultDocument;

/**
 * Description:<br>
 * 
 * <P>
 * Initial Date:  3 dec. 2009 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class SearchEvent extends Event {

	private static final long serialVersionUID = -1398284268579829998L;
	
	public static final String GOTO_DOCUMENT_EVENT = "cmd.goto";
	public static final String RELOAD_EVENT = "cmd.reload";
	public static final String NEXT_EVENT = "next";
	public static final String PREVIOUS_EVENT = "previous";
	public static final String NEW_SEARCH_EVENT = "new_search";
	public static final Event RELOAD = new SearchEvent(RELOAD_EVENT); 
	public static final Event NEXT = new SearchEvent(NEXT_EVENT); 
	//public static final Event PREVIOUS = new SearchEvent(PREVIOUS_EVENT); 
	
	private int firstResult;
	private int maxReturns;
	private ResultDocument document;
	
	public SearchEvent(String cmd) {
		super(cmd);
	}
	
	public SearchEvent(ResultDocument document) {
		super(GOTO_DOCUMENT_EVENT);
		this.document = document;
	}
	
	public SearchEvent(int firstResult, int maxReturns) {
		super(NEW_SEARCH_EVENT);
		this.firstResult = firstResult;
		this.maxReturns = maxReturns;
	}

	public ResultDocument getDocument() {
		return document;
	}

	public int getFirstResult() {
		return firstResult;
	}

	public int getMaxReturns() {
		return maxReturns;
	}
}
