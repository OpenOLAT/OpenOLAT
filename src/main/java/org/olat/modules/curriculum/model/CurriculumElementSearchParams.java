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
package org.olat.modules.curriculum.model;

import java.util.Date;

import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 10 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementSearchParams {
	
	private String searchString;
	private String elementId;
	private String elementText;
	private Date elementBeginDate;
	private Date elementEndDate;
	private String entryId;
	private String entryText;

	private Identity managerIdentity;
	
	public CurriculumElementSearchParams(Identity managerIdentity) {
		this.managerIdentity = managerIdentity;
	}
	
	public Identity getManagerIdentity() {
		return managerIdentity;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
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

	public Date getElementBeginDate() {
		return elementBeginDate;
	}

	public void setElementBeginDate(Date elementBeginDate) {
		this.elementBeginDate = elementBeginDate;
	}

	public Date getElementEndDate() {
		return elementEndDate;
	}

	public void setElementEndDate(Date elementEndDate) {
		this.elementEndDate = elementEndDate;
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
}
