/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui.copy;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.model.CurriculumCopySettings.CopyMemberships;
import org.olat.modules.curriculum.model.CurriculumCopySettings.CopyResources;
import org.olat.modules.curriculum.ui.CurriculumComposerController;
import org.olat.modules.curriculum.ui.CurriculumHelper;

/**
 * 
 * Initial date: 12 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CopyElementSettingsController extends StepFormBasicController {
	
	private TextElement displayNameEl;
	private TextElement identifierEl;
	private SingleSelection courseEventsEl;
	private SingleSelection standaloneEventsEl;
	private SingleSelection membershipCoachEl;
	private SingleSelection membershipOwnerAndMasterCoachEl;
	
	private final CopyElementContext context;
	
	public CopyElementSettingsController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext,
			CopyElementContext context) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		setTranslator(Util.createPackageTranslator(CurriculumComposerController.class, getLocale(), getTranslator()));
		this.context = context;

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer metadataCont = uifactory.addDefaultFormLayout("metadata", null, formLayout);
		metadataCont.setFormTitle(translate("wizard.metadata"));
		initMetadataForm(metadataCont);

		FormLayoutContainer optionsCont = uifactory.addDefaultFormLayout("options", null, formLayout);
		optionsCont.setFormTitle(translate("wizard.options"));
		initOptionsForm(optionsCont);
		
		FormLayoutContainer membersCont = uifactory.addDefaultFormLayout("members", null, formLayout);
		membersCont.setFormTitle(translate("wizard.members"));
		initMembersForm(membersCont);
	}
	
	private void initMetadataForm(FormItemContainer formLayout) {
		CurriculumElement elementToCopy = context.getCurriculumElement();
		
		String displayName = translate("to.copy", elementToCopy.getDisplayName());
		displayNameEl = uifactory.addTextElement("displayName", "curriculum.element.display.name", 110, displayName, formLayout);
		displayNameEl.setMandatory(true);
		
		String identifier = translate("to.copy", elementToCopy.getIdentifier());
		identifierEl = uifactory.addTextElement("identifier", "curriculum.element.identifier", 64, identifier, formLayout);
		identifierEl.setMandatory(true);
		
		String type = elementToCopy.getType() == null ? null : elementToCopy.getType().getDisplayName(); 
		if(StringHelper.containsNonWhitespace(type)) {
			uifactory.addStaticTextElement("curriculum.element.type", type, formLayout);
		}
	}
	
	private void initOptionsForm(FormItemContainer formLayout) {
		SelectionValues eventsPK = new SelectionValues();
		eventsPK.add(SelectionValues.entry(CopyResources.resource.name(),
				translate("copy.resources.resource"), translate("copy.resources.resource.desc"), "o_icon o_icon_copy", null, true));
		eventsPK.add(SelectionValues.entry(CopyResources.relation.name(),
				translate("copy.resources.relation"), translate("copy.resources.relation.desc"), "o_icon o_icon_recycle", null, true));
		eventsPK.add(SelectionValues.entry(CopyResources.dont.name(),
				translate("copy.resources.none"), translate("copy.resources.none.desc"), "o_icon o_icon_ban", null, true));
		courseEventsEl = uifactory.addCardSingleSelectHorizontal("copy.courses.with.events", formLayout,
				eventsPK.keys(), eventsPK.values(), eventsPK.descriptions(), eventsPK.icons());
		courseEventsEl.setElementCssClass("o_curriculum_copy_options");
		courseEventsEl.select(CopyResources.resource.name(), true);
		
		SelectionValues standalonePK = new SelectionValues();
		standalonePK.add(SelectionValues.entry(CopyResources.resource.name(),
				translate("copy.events.copy"), translate("copy.events.copy.desc"), "o_icon o_icon_copy", null, true));
		standalonePK.add(SelectionValues.entry(CopyResources.dont.name(),
				translate("copy.events.none"), translate("copy.events.none.desc"), "o_icon o_icon_ban", null, true));
		standaloneEventsEl = uifactory.addCardSingleSelectHorizontal("copy.standalone.events", "copy.standalone.events", formLayout,
				standalonePK.keys(), standalonePK.values(), standalonePK.descriptions(), standalonePK.icons());
		standaloneEventsEl.select(CopyResources.resource.name(), true);
	}

	private void initMembersForm(FormItemContainer formLayout) {	
		SelectionValues coachesPK = new SelectionValues();
		coachesPK.add(SelectionValues.entry(CopyMemberships.membershipsAddTeachers.name(),
				translate("copy.membership.coaches.teachers.copy"), translate("copy.membership.coaches.teachers.copy.desc"), "o_icon o_icon_copy", null, true));
		coachesPK.add(SelectionValues.entry(CopyMemberships.memberships.name(),
				translate("copy.membership.coaches.copy"), translate("copy.membership.coaches.copy.desc"), "o_icon o_icon_user", null, true));
		coachesPK.add(SelectionValues.entry(CopyMemberships.dont.name(),
				translate("copy.membership.coaches.none"), translate("copy.membership.coaches.none.desc"), "o_icon o_icon_ban", null, true));
		membershipCoachEl = uifactory.addCardSingleSelectHorizontal("copy.membership.coaches", "copy.membership.coaches", formLayout,
				coachesPK.keys(), coachesPK.values(), coachesPK.descriptions(), coachesPK.icons());
		membershipCoachEl.setElementCssClass("o_curriculum_copy_options");
		membershipCoachEl.select(CopyMemberships.membershipsAddTeachers.name(), true);
		
		SelectionValues ownersPK = new SelectionValues();
		ownersPK.add(SelectionValues.entry(CopyMemberships.memberships.name(),
				translate("copy.membership.owners.copy"), translate("copy.membership.owners.copy.desc"), "o_icon o_icon_copy", null, true));
		ownersPK.add(SelectionValues.entry(CopyMemberships.dont.name(),
				translate("copy.membership.owners.none"), translate("copy.membership.owners.none.desc"), "o_icon o_icon_ban", null, true));
		membershipOwnerAndMasterCoachEl = uifactory.addCardSingleSelectHorizontal("copy.membership.owners", "copy.membership.owners", formLayout,
				ownersPK.keys(), ownersPK.values(), ownersPK.descriptions(), ownersPK.icons());
		membershipOwnerAndMasterCoachEl.select(CopyMemberships.memberships.name(), true);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= CurriculumHelper.validateTextElement(displayNameEl, true, 110);
		allOk &= CurriculumHelper.validateTextElement(identifierEl, true, 64);
		
		allOk &= validateFormLogic(courseEventsEl);
		allOk &= validateFormLogic(standaloneEventsEl);
		allOk &= validateFormLogic(membershipCoachEl);
		allOk &= validateFormLogic(membershipOwnerAndMasterCoachEl);
		
		return allOk;
	}

	private boolean validateFormLogic(SingleSelection el) {
		boolean allOk = true;

		el.clearError();
		if(!el.isOneSelected()) {
			el.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		return allOk;
	}

	@Override
	protected void formNext(UserRequest ureq) {
		context.setDisplayName(displayNameEl.getValue());
		context.setIdentifier(identifierEl.getValue());
		context.setCoursesEventsCopySetting(CopyResources.valueOf(courseEventsEl.getSelectedKey()));
		context.setStandaloneEventsCopySetting(CopyResources.resource.name().equals(standaloneEventsEl.getSelectedKey()));
		boolean copyCoachesMemberships = CopyMemberships.memberships.name().equals(membershipCoachEl.getSelectedKey())
				|| CopyMemberships.membershipsAddTeachers.name().equals(membershipCoachEl.getSelectedKey());
		context.setCopyCoachesMemberships(copyCoachesMemberships);
		context.setAddCoachesAsTeacher(CopyMemberships.membershipsAddTeachers.name().equals(membershipCoachEl.getSelectedKey()));
		
		boolean copyOwnersAndMasterCoachesMemberships = CopyMemberships.memberships.name().equals(this.membershipOwnerAndMasterCoachEl.getSelectedKey());
		context.setCopyOwnersMemberships(copyOwnersAndMasterCoachesMemberships);
		context.setCopyMasterCoachesMemberships(copyOwnersAndMasterCoachesMemberships);
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
