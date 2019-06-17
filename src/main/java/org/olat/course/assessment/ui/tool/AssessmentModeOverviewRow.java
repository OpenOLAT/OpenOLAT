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
package org.olat.course.assessment.ui.tool;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.course.assessment.AssessmentMode;

/**
 * 
 * Initial date: 13 juin 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeOverviewRow {
	
	private FormLink actionButton;
	private final List<FormLink> elementLinks = new ArrayList<>();
	
	private final boolean today;
	private final AssessmentMode assessmentMode;
	
	public AssessmentModeOverviewRow(AssessmentMode assessmentMode, boolean today) {
		this.today = today;
		this.assessmentMode = assessmentMode;
	}
	
	public String getName() {
		return assessmentMode.getName();
	}
	
	public boolean isToday() {
		return today;
	}
	
	public AssessmentMode getAssessmentMode() {
		return assessmentMode;
	}

	public FormLink getActionButton() {
		return actionButton;
	}

	public void setActionButton(FormLink actionButton) {
		this.actionButton = actionButton;
	}
	
	public String getActionButtonName() {
		return actionButton == null ? null : actionButton.getComponent().getComponentName();
	}
	
	public List<String> getElementLinkNames() {
		List<String> names = new ArrayList<>(elementLinks.size());
		for(FormLink elementLink:elementLinks) {
			names.add(elementLink.getComponent().getComponentName());
		}
		return names;
	}

	public List<FormLink> getElementLinks() {
		return elementLinks;
	}
	
	public void addElementLink(FormLink elementLink) {
		elementLinks.add(elementLink);
	}
}
