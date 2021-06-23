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
package org.olat.modules.portfolio.model.export;

import org.olat.core.util.StringHelper;
import org.olat.modules.portfolio.AssignmentStatus;
import org.olat.modules.portfolio.AssignmentType;

/**
 * 
 * Initial date: 23 juin 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssignmentXML {

	private String status;
	private String type;
	private int version;
	private boolean template;
	
	private String title;
	private String summary;
	private String content;

	private boolean onlyAutoEvaluation;
	private boolean reviewerSeeAutoEvaluation;
	private boolean anonymousExternalEvaluation;

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public AssignmentStatus getAssignmentStatus() {
		return StringHelper.containsNonWhitespace(status) ? AssignmentStatus.valueOf(status) : null;
	}

	public void setAssignmentStatus(AssignmentStatus status) {
		if(status == null) {
			this.status = null;
		} else {
			this.status = status.name();
		}
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public AssignmentType getAssignmentType() {
		return StringHelper.containsNonWhitespace(type) ? AssignmentType.valueOf(type) : null;
	}

	public boolean isTemplate() {
		return template;
	}

	public void setTemplate(boolean template) {
		this.template = template;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isOnlyAutoEvaluation() {
		return onlyAutoEvaluation;
	}

	public void setOnlyAutoEvaluation(boolean onlyAutoEvaluation) {
		this.onlyAutoEvaluation = onlyAutoEvaluation;
	}

	public boolean isReviewerSeeAutoEvaluation() {
		return reviewerSeeAutoEvaluation;
	}

	public void setReviewerSeeAutoEvaluation(boolean reviewerSeeAutoEvaluation) {
		this.reviewerSeeAutoEvaluation = reviewerSeeAutoEvaluation;
	}

	public boolean isAnonymousExternalEvaluation() {
		return anonymousExternalEvaluation;
	}

	public void setAnonymousExternalEvaluation(boolean anonymousExternalEvaluation) {
		this.anonymousExternalEvaluation = anonymousExternalEvaluation;
	}
}
