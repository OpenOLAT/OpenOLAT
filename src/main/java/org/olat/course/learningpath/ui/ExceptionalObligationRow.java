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
package org.olat.course.learningpath.ui;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.course.learningpath.obligation.ExceptionalObligation;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 1 Sep 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ExceptionalObligationRow {
	
	private final ExceptionalObligation exceptionalObligation;
	private String name;
	private String type;
	private SingleSelection mandatoryEl;
	private SingleSelection optionalEl;
	private SingleSelection excludedEl;
	private FormLink deleteLink;
	
	// Field to hold changed state
	private AssessmentObligation obligation;
	private boolean deleted;
	
	public ExceptionalObligationRow(ExceptionalObligation exceptionalObligation) {
		this.exceptionalObligation = exceptionalObligation;
	}
	
	public boolean isDefaultObligation() {
		return exceptionalObligation == null;
	}
	
	public boolean isExceptionalObligation() {
		return exceptionalObligation != null;
	}
	
	public ExceptionalObligation getExceptionalObligation() {
		return exceptionalObligation;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public SingleSelection getMandatoryEl() {
		return mandatoryEl;
	}
	
	public void setMandatoryEl(SingleSelection mandatoryEl) {
		this.mandatoryEl = mandatoryEl;
	}
	
	public SingleSelection getOptionalEl() {
		return optionalEl;
	}
	
	public void setOptionalEl(SingleSelection optionalEl) {
		this.optionalEl = optionalEl;
	}
	
	public SingleSelection getExcludedEl() {
		return excludedEl;
	}
	
	public void setExcludedEl(SingleSelection excludedEl) {
		this.excludedEl = excludedEl;
	}

	public FormLink getDeleteLink() {
		return deleteLink;
	}

	public void setDeleteLink(FormLink deleteLink) {
		this.deleteLink = deleteLink;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}

	public AssessmentObligation getObligation() {
		return obligation;
	}

	public void setObligation(AssessmentObligation obligation) {
		this.obligation = obligation;
	}
	
}
