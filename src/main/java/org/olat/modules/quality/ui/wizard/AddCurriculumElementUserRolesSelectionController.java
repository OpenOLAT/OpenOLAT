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
package org.olat.modules.quality.ui.wizard;

import static org.olat.core.gui.translator.TranslatorHelper.translateAll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.quality.ui.ParticipationListController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 02.07.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AddCurriculumElementUserRolesSelectionController  extends StepFormBasicController {
	
	private static final String OWNER_KEY = CurriculumRoles.owner.name();
	private static final String COACH_KEY= CurriculumRoles.coach.name();
	private static final String PARTICIPANT_KEY = CurriculumRoles.participant.name();
	private static final String[] ROLES_KEYS = new String[] {
			OWNER_KEY,
			COACH_KEY,
			PARTICIPANT_KEY
	};
	private static final String OWNER_I18N = "participation.user.curele.add.role.owner";
	private static final String COACH_I18N = "participation.user.curele.add.role.coach";
	private static final String PARTICIPANT_I18N = "participation.user.curele.add.role.participant";
	private static final String[] ROLES_I18N = new String[] {
			OWNER_I18N,
			COACH_I18N,
			PARTICIPANT_I18N
	};
	
	private MultipleSelectionElement rolesEl;
	
	private CurriculumElementContext context;

	@Autowired
	private CurriculumService curriculumService;

	public AddCurriculumElementUserRolesSelectionController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
		setTranslator(Util.createPackageTranslator(ParticipationListController.class, getLocale(), getTranslator()));
		context = (CurriculumElementContext) getFromRunContext("context");
		initForm(ureq);
	}

	@Override
	protected void formNext(UserRequest ureq) {
		Collection<CurriculumRoles> roles = context.getRoles();
		Set<Identity> identities = new HashSet<>();
		if (!roles.isEmpty()) {
			CurriculumElement curriculumElement = context.getCurriculumElement();
			for (CurriculumRoles role: roles) {
				List<Identity> members = curriculumService.getMembersIdentity(curriculumElement, role);
				identities.addAll(members);
			}
		}
		context.setIdentities(new ArrayList<>(identities));
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		rolesEl = uifactory.addCheckboxesVertical("participation.user.curele.add.roles", formLayout, ROLES_KEYS,
				translateAll(getTranslator(), ROLES_I18N), 1);
		Collection<String> roleNames = context.getRoles().stream().map(CurriculumRoles::name).collect(Collectors.toSet());
		rolesEl.select(OWNER_KEY, roleNames.contains(OWNER_KEY));
		rolesEl.select(COACH_KEY, roleNames.contains(COACH_KEY));
		rolesEl.select(PARTICIPANT_KEY, roleNames.contains(PARTICIPANT_KEY));
		rolesEl.addActionListener(FormEvent.ONCHANGE);
	}
	

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == rolesEl) {
			doSetRoles();
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doSetRoles() {
		Collection<String> selectedRoleNames = rolesEl.getSelectedKeys();
		List<CurriculumRoles> roles = new ArrayList<>();
		for (String key: selectedRoleNames) {
			roles.add(CurriculumRoles.valueOf(key));
		}
		context.setRoles(roles);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
