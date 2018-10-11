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
package org.olat.modules.curriculum.ui.event;

import java.util.Date;

import org.olat.core.gui.control.Event;

/**
 * 
 * Initial date: 11 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumSearchEvent extends Event {

	private static final long serialVersionUID = -5680420577878064843L;

	public static final String SEARCH_CURRICULUM = "search-curriculum";
	
	private String elementId;
	private String elementText;
	private Date elementBegin;
	private Date elementEnd;
	private String entryId;
	private String entryText;
	
	public CurriculumSearchEvent() {
		super(SEARCH_CURRICULUM);
	}

	public String getElementId() {
		return elementId;
	}

	public void setElementId(String elementId) {
		this.elementId = elementId;
	}

	public String getElementText() {
		return elementText;
	}

	public void setElementText(String elementText) {
		this.elementText = elementText;
	}

	public String getEntryId() {
		return entryId;
	}

	public void setEntryId(String entryId) {
		this.entryId = entryId;
	}

	public String getEntryText() {
		return entryText;
	}

	public void setEntryText(String entryText) {
		this.entryText = entryText;
	}

	public Date getElementBegin() {
		return elementBegin;
	}

	public void setElementBegin(Date elementBegin) {
		this.elementBegin = elementBegin;
	}

	public Date getElementEnd() {
		return elementEnd;
	}

	public void setElementEnd(Date elementEnd) {
		this.elementEnd = elementEnd;
	}
}
