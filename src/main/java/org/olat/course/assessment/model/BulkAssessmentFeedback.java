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
package org.olat.course.assessment.model;

import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 22.11.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BulkAssessmentFeedback {
	
	private String errorKey;
	private String assessedId;
	private Identity assessedIdentity;
	
	public BulkAssessmentFeedback(String errorKey, String assessedId) {
		this.errorKey = errorKey;
		this.assessedId = assessedId;
	}
	
	public BulkAssessmentFeedback(String errorKey, Identity assessedIdentity) {
		this.errorKey = errorKey;
		this.assessedIdentity = assessedIdentity;
	}
	
	public String getErrorKey() {
		return errorKey;
	}
	
	public void setErrorKey(String errorKey) {
		this.errorKey = errorKey;
	}
	
	public String getAssessedId() {
		return assessedId;
	}
	
	public void setAssessedId(String assessedId) {
		this.assessedId = assessedId;
	}
	
	public Identity getAssessedIdentity() {
		return assessedIdentity;
	}
	
	public void setAssessedIdentity(Identity assessedIdentity) {
		this.assessedIdentity = assessedIdentity;
	}
}
