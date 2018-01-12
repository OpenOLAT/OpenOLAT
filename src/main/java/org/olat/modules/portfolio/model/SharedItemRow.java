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
package org.olat.modules.portfolio.model;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;
import org.olat.modules.assessment.AssessmentEntryLight;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 15.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SharedItemRow extends UserPropertiesRow {
	
	private Long binderKey;
	private Date lastModified;
	private String binderTitle;
	private Date recentLaunch;
	private int numOfOpenSections;
	private String entryDisplayName;
	private AssessmentEntryLight assessmentEntry;
	
	private int numOfDraftPages = 0;
	private int numOfInRevisionPages = 0;
	private int numOfClosedPages = 0;
	private int numOfNewlyPublishedPages = 0;
	
	private boolean expandSections;
	private List<AssessedBinderSection> sections;
	
	public SharedItemRow(Identity identity, List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		super(identity, userPropertyHandlers, locale);
	}
	
	public Long getBinderKey() {
		return binderKey;
	}
	
	public void setBinderKey(Long binderKey) {
		this.binderKey = binderKey;
	}

	public String getBinderTitle() {
		return binderTitle;
	}

	public void setBinderTitle(String binder) {
		this.binderTitle = binder;
	}

	public String getEntryDisplayName() {
		return entryDisplayName;
	}

	public void setEntryDisplayName(String course) {
		this.entryDisplayName = course;
	}

	public Date getLastModified() {
		return lastModified;
	}

	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}

	public AssessmentEntryLight getAssessmentEntry() {
		return assessmentEntry;
	}

	public void setAssessmentEntry(AssessmentEntryLight assessmentEntry) {
		this.assessmentEntry = assessmentEntry;
	}

	public boolean isExpandSections() {
		return expandSections;
	}

	public void setExpandSections(boolean expandSections) {
		this.expandSections = expandSections;
	}

	public List<AssessedBinderSection> getSections() {
		return sections;
	}

	public void setSections(List<AssessedBinderSection> sections) {
		this.sections = sections;
	}

	public int getNumOfOpenSections() {
		return numOfOpenSections;
	}

	public void setNumOfOpenSections(int numOfOpenSections) {
		this.numOfOpenSections = numOfOpenSections;
	}

	public Date getRecentLaunch() {
		return recentLaunch;
	}

	public void setRecentLaunch(Date recentLaunch) {
		this.recentLaunch = recentLaunch;
	}

	public int getNumOfDraftPages() {
		return numOfDraftPages;
	}

	public void setNumOfDraftPages(int numOfDraftPages) {
		this.numOfDraftPages = numOfDraftPages;
	}

	public int getNumOfInRevisionPages() {
		return numOfInRevisionPages;
	}

	public void setNumOfInRevisionPages(int numOfInRevisionPages) {
		this.numOfInRevisionPages = numOfInRevisionPages;
	}

	public int getNumOfClosedPages() {
		return numOfClosedPages;
	}

	public void setNumOfClosedPages(int numOfClosedPages) {
		this.numOfClosedPages = numOfClosedPages;
	}

	public int getNumOfNewlyPublishedPages() {
		return numOfNewlyPublishedPages;
	}

	public void setNumOfNewlyPublishedPages(int numOfNewlyPublishedPages) {
		this.numOfNewlyPublishedPages = numOfNewlyPublishedPages;
	}
}
