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

import java.io.Serializable;
import java.util.List;

/**
 * 
 * Initial date: 20.11.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BulkAssessmentRow implements Serializable {

	private static final long serialVersionUID = 7584233156812946093L;
	
	private String assessedId;
	private Long identityKey;
	private Boolean passed;
	private Float score;
	private String comment;
	private List<String> returnFiles;
	
	public String getAssessedId() {
		return assessedId;
	}

	public void setAssessedId(String assessedId) {
		this.assessedId = assessedId;
	}

	public Long getIdentityKey() {
		return identityKey;
	}
	
	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}
	
	public Boolean getPassed() {
		return passed;
	}
	
	public void setPassed(Boolean passed) {
		this.passed = passed;
	}
	
	public Float getScore() {
		return score;
	}
	
	public void setScore(Float score) {
		this.score = score;
	}
	
	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}

	public List<String> getReturnFiles() {
		return returnFiles;
	}

	public void setReturnFiles(List<String> returnFiles) {
		this.returnFiles = returnFiles;
	}
}
