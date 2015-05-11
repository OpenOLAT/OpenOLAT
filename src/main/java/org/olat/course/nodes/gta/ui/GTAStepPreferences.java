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
package org.olat.course.nodes.gta.ui;

/**
 * 
 * Initial date: 07.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAStepPreferences {
	
	private Boolean assignement;
	private Boolean submit;
	private Boolean reviewAndCorrection;
	private Boolean revision;
	private Boolean solution;
	private Boolean grading;
	
	public Boolean getAssignement() {
		return assignement;
	}
	
	public void setAssignement(Boolean assignement) {
		this.assignement = assignement;
	}
	
	public Boolean getSubmit() {
		return submit;
	}
	
	public void setSubmit(Boolean submit) {
		this.submit = submit;
	}
	
	public Boolean getReviewAndCorrection() {
		return reviewAndCorrection;
	}
	
	public void setReviewAndCorrection(Boolean reviewAndCorrection) {
		this.reviewAndCorrection = reviewAndCorrection;
	}
	
	public Boolean getRevision() {
		return revision;
	}
	
	public void setRevision(Boolean revision) {
		this.revision = revision;
	}
	
	public Boolean getSolution() {
		return solution;
	}
	
	public void setSolution(Boolean solution) {
		this.solution = solution;
	}
	
	public Boolean getGrading() {
		return grading;
	}
	
	public void setGrading(Boolean grading) {
		this.grading = grading;
	}
}
