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

import java.util.Collection;
import java.util.List;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.ui.CalendarColorChooserController;
import org.olat.core.commons.services.tag.TagRef;
import org.olat.core.commons.services.tag.ui.component.TagSelection;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextAreaElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.project.ProjMilestone;
import org.olat.modules.project.ProjMilestoneStatus;
import org.olat.modules.project.ProjTagInfo;
import org.olat.modules.project.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjMilestoneContentEditController extends FormBasicController {
	
	private static final String CMD_COLOR = "color";

	private TextElement subjectEl;
	private TagSelection tagsEl;
	private DateChooser dueEl;
	private DropdownItem statusEl;
	private FormLink statusLink;
	private DropdownItem colorEl;
	private TextAreaElement descriptionEl;
	
	private final ProjMilestone milestone;
	private final List<ProjTagInfo> projectTags;
	private final Collection<? extends TagRef> artefactTags;
	private ProjMilestoneStatus status;
	
	@Autowired
	private ProjectService projectService;


	public ProjMilestoneContentEditController(UserRequest ureq, WindowControl wControl, Form mainForm,
			ProjMilestone milestone) {
		super(ureq, wControl, LAYOUT_CUSTOM, "milestone_edit", mainForm);
		setTranslator(Util.createPackageTranslator(CalendarManager.class, getLocale(), getTranslator()));
		this.milestone = milestone;
		this.status = milestone.getStatus();
		this.projectTags = projectService.getTagInfos(milestone.getArtefact().getProject(), milestone.getArtefact());
		this.artefactTags = projectTags.stream().filter(ProjTagInfo::isSelected).toList();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		subjectEl = uifactory.addTextElement("subject", "milestone.edit.subject", 256, milestone.getSubject(), formLayout);
		subjectEl.setMandatory(true);
		
		tagsEl = uifactory.addTagSelection("tags", "tags", formLayout, getWindowControl(), projectTags, artefactTags);
		
		dueEl = uifactory.addDateChooser("due", "milestone.edit.due", milestone.getDueDate(), formLayout);
		dueEl.setMandatory(true);
		
		statusEl = uifactory.addDropdownMenu("status", "", "milestone.edit.status", formLayout, getTranslator());
		statusEl.addActionListener(FormEvent.ONCHANGE);
		
		statusLink = uifactory.addFormLink("stauts.link", "status", null, null, formLayout, Link.LINK + Link.NONTRANSLATED);
		statusLink.setDomReplacementWrapperRequired(false);
		statusEl.addElement(statusLink);
		updateStatusUI();
		
		colorEl = uifactory.addDropdownMenu("color", "", "cal.form.event.color", formLayout, getTranslator());
		colorEl.setElementCssClass("o_proj_color");
		colorEl.setOrientation(DropdownOrientation.normal);
		colorEl.addActionListener(FormEvent.ONCHANGE);
		for (String color : CalendarColorChooserController.colors) {
			FormLink colorLink = uifactory.addFormLink(color, CMD_COLOR, "", null, formLayout, Link.LINK + Link.NONTRANSLATED);
			if (color.equals(milestone.getColor())){
				colorLink.setIconLeftCSS("o_cal_color_element o_cal_colorchooser_selected " + color);
			} else {
				colorLink.setIconLeftCSS("o_cal_color_element " + color);
			}
			colorLink.setUserObject(color);
			colorEl.addElement(colorLink);
		}
		updateColorUI("o_cal_" + milestone.getColor());
		
		descriptionEl = uifactory.addTextAreaElement("description", "milestone.edit.description", -1, 3, 40, true,
				false, milestone.getDescription(), formLayout);
	}
	
	private void updateStatusUI() {
		if (ProjMilestoneStatus.open == status) {
			statusEl.setDropdownLabel("milestone.status.open");
			statusEl.setIconCSS("o_icon o_icon-lg " + ProjectUIFactory.getMilestoneStatusIconCss(ProjMilestoneStatus.open));
			statusLink.setI18nKey(ProjectUIFactory.getDisplayName(getTranslator(), ProjMilestoneStatus.achieved));
			statusLink.setIconLeftCSS("o_icon o_icon_fw " + ProjectUIFactory.getMilestoneStatusIconCss(ProjMilestoneStatus.achieved));
		} else {
			statusEl.setDropdownLabel("milestone.status.achieved");
			statusEl.setIconCSS("o_icon o_icon-lg " + ProjectUIFactory.getMilestoneStatusIconCss(ProjMilestoneStatus.achieved));
			statusLink.setI18nKey(ProjectUIFactory.getDisplayName(getTranslator(), ProjMilestoneStatus.open));
			statusLink.setIconLeftCSS("o_icon o_icon_fw " + ProjectUIFactory.getMilestoneStatusIconCss(ProjMilestoneStatus.open));
		}
	}
	
	private void updateColorUI(String colorCss) {
		if (CalendarColorChooserController.colorExists(colorCss)) {
			colorEl.setIconCSS("o_cal_color_element " + colorCss);
			colorEl.setUserObject(colorCss);
		} else {
			colorEl.setIconCSS("o_cal_color_element " + ProjectUIFactory.COLOR_MILESTONE);
			colorEl.setUserObject(null);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == statusLink) {
			doToggleStatus();
		} else if (source instanceof FormLink link) {
			if (CMD_COLOR.equals(link.getCmd())) {
				String colorCss = (String)link.getUserObject();
				updateColorUI(colorCss);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		subjectEl.clearError();
		if (!StringHelper.containsNonWhitespace(subjectEl.getValue())) {
			subjectEl.setErrorKey("form.mandatory.hover");
			allOk &= false;
		}
		
		dueEl.clearError();
		if (dueEl.getDate() == null) {
			dueEl.setErrorKey("form.mandatory.hover");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		projectService.updateMilestone(getIdentity(), milestone, status, dueEl.getDate(), subjectEl.getValue(),
				descriptionEl.getValue(), getColor());
		projectService.updateTags(getIdentity(), milestone.getArtefact(), tagsEl.getDisplayNames());
	}
	
	private String getColor() {
		if (colorEl.getUserObject() instanceof String color) {
			return color.substring(6, color.length());
		}
		return null;
	}
	
	private void doToggleStatus() {
		status = ProjMilestoneStatus.achieved == status
			? ProjMilestoneStatus.open
			: ProjMilestoneStatus.achieved;
		updateStatusUI();
	}
	
}
