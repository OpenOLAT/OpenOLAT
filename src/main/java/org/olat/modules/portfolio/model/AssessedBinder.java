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

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.Group;
import org.olat.core.id.Identity;
import org.olat.modules.assessment.AssessmentEntryLight;

/**
 * 
 * Initial date: 23.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessedBinder implements AssessmentEntryLight {
	
	private final Long binderKey;
	private final String binderTitle;
	private final Long entryKey;
	private final String entryDisplayname;
	
	private final Boolean passed;
	private final BigDecimal score;
	private final Identity assessedIdentity;
	
	private Date lastModified;
	private List<AssessedBinderSection> sections;
	private int numOfOpenSections;
	
	private int numOfDraftPages = 0;
	private int numOfInRevisionPages = 0;
	private int numOfClosedPages = 0;
	private int numOfNewlyPublishedPages = 0;
	
	private Date recentLaunch;
	
	private final Group baseGroup;
	
	public AssessedBinder(Long binderKey, String binderTitle, Long entryKey, String entryDisplayname,
			Boolean passed, BigDecimal score, Identity assessedIdentity, Group baseGroup) {
		this.binderKey = binderKey;
		this.binderTitle = binderTitle;
		this.entryKey = entryKey;
		this.entryDisplayname = entryDisplayname;
		this.passed = passed;
		this.score = score;
		this.assessedIdentity = assessedIdentity;
		this.baseGroup = baseGroup;
	}
	
	public Long getBinderKey() {
		return binderKey;
	}
	
	public String getBinderTitle() {
		return binderTitle;
	}
	
	public Group getBaseGroup() {
		return baseGroup;
	}

	public Date getLastModified() {
		return lastModified;
	}
	
	public void setLastModified(Date lastModified) {
		this.lastModified = lastModified;
	}
	
	public Long getEntryKey() {
		return entryKey;
	}
	
	public String getEntryDisplayname() {
		return entryDisplayname;
	}

	@Override
	public Boolean getPassed() {
		return passed;
	}

	@Override
	public BigDecimal getScore() {
		return score;
	}

	public Identity getAssessedIdentity() {
		return assessedIdentity;
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

	public void incrementNumOfDraftPages() {
		numOfDraftPages++;
	}

	public int getNumOfInRevisionPages() {
		return numOfInRevisionPages;
	}

	public void incrementNumOfInRevisionPages() {
		numOfInRevisionPages++;
	}

	public int getNumOfClosedPages() {
		return numOfClosedPages;
	}

	public void incrementNumOfClosedPages() {
		numOfClosedPages++;
	}
	
	public int getNumOfNewlyPublishedPages() {
		return numOfNewlyPublishedPages;
	}
	
	public void incrementNumOfNewlyPublishedPages() {
		numOfNewlyPublishedPages++;
	}
}
