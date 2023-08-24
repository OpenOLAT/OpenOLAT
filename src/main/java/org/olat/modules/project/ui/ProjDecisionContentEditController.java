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
package org.olat.modules.project.ui;



import java.util.Date;
import java.util.List;

import org.olat.commons.calendar.CalendarManager;
import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.commons.services.tag.ui.component.TagSelection;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.project.ProjDecision;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 Jun 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjDecisionContentEditController extends FormBasicController {
	
	private TextElement titleEl;
	private TagSelection tagsEl;
	private DateChooser decisionDateEl;
	private TextAreaElement detailsEl;
	
	private final ProjProject project;
	private final boolean template;
	private ProjDecision decision;
	private final List<TagInfo> projectTags;
	private final boolean readOnly;
	
	@Autowired
	private ProjectService projectService;


	public ProjDecisionContentEditController(UserRequest ureq, WindowControl wControl, Form mainForm,
			ProjProject project, ProjDecision decision, boolean readOnly) {
		super(ureq, wControl, LAYOUT_CUSTOM, "decision_edit", mainForm);
		setTranslator(Util.createPackageTranslator(CalendarManager.class, getLocale(), getTranslator()));
		this.project = project;
		this.template = project.isTemplatePrivate() || project.isTemplatePublic();
		this.decision = decision;
		this.projectTags = projectService.getTagInfos(project, decision != null? decision.getArtefact(): null);
		this.readOnly = readOnly;
		
		initForm(ureq);
	}
	
	public ProjDecision getDecision() {
		return decision;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String title = decision != null? decision.getTitle(): null;
		titleEl = uifactory.addTextElement("title", "decision.edit.title", 1000, title, formLayout);
		titleEl.setMandatory(true);
		titleEl.setEnabled(!readOnly);
		
		tagsEl = uifactory.addTagSelection("tags", "tags", formLayout, getWindowControl(), projectTags);
		tagsEl.setEnabled(!readOnly);
		
		String details = decision != null? decision.getDetails(): null;
		detailsEl = uifactory.addTextAreaElement("details", "decision.details", -1, 3, 40, true, false, details,
				formLayout);
		detailsEl.setEnabled(!readOnly);

		Date decisionDate = decision != null? decision.getDecisionDate(): !template? new Date(): null;
		decisionDateEl = uifactory.addDateChooser("decision.date", "decision.edit.decision.date", decisionDate, formLayout);
		decisionDateEl.setDateChooserTimeEnabled(true);
		decisionDateEl.setEnabled(!readOnly);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		titleEl.clearError();
		if (!StringHelper.containsNonWhitespace(titleEl.getValue())) {
			titleEl.setErrorKey("form.mandatory.hover");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if (decision == null) {
			decision = projectService.createDecision(getIdentity(), project);
		}
		
		projectService.updateDecision(getIdentity(), decision, titleEl.getValue(), detailsEl.getValue(),
				decisionDateEl.getDate());
		projectService.updateTags(getIdentity(), decision.getArtefact(), tagsEl.getDisplayNames());
		
		fireEvent(ureq, FormEvent.DONE_EVENT);
	}
	
}
