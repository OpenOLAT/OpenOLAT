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
package org.olat.modules.curriculum.ui.wizard;

import java.util.ArrayList;
import java.util.List;

import org.olat.admin.user.UserSearchFlexiController;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.member.wizard.ImportMemberByUsernamesController;
import org.olat.modules.curriculum.ui.CurriculumManagerController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 déc. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AddMembersController extends StepFormBasicController {
	
	private static final String SEARCH = "search";
	private static final String LIST = "list";

	private SingleSelection importTypeEl;
	
	private final MembersContext membersContext;
	
	private UserSearchFlexiController searchController;
	private ImportMemberByUsernamesController importController;
	
	@Autowired
	private BaseSecurityModule securityModule;
	
	public AddMembersController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, MembersContext membersContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "import_search");
		setTranslator(Util.createPackageTranslator(CurriculumManagerController.class, getLocale(), getTranslator()));
		this.membersContext = membersContext;
		
		initForm (ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer chooseCont = FormLayoutContainer.createDefaultFormLayout("choose", getTranslator());
		formLayout.add(chooseCont);
		
		SelectionValues values = new SelectionValues();
		values.add(SelectionValues.entry(SEARCH, translate("import.search"), null, "o_icon o_icon-fw o_icon_search", null, true));
		values.add(SelectionValues.entry(LIST, translate("import.list"), null, "o_icon o_icon-fw o_icon_import", null, true));
		importTypeEl = uifactory.addCardSingleSelectHorizontal("import.type", null, chooseCont, values);
		importTypeEl.setElementCssClass("o_sel_import_type o_radio_cards_vcenter");
		importTypeEl.addActionListener(FormEvent.ONCHANGE);
		importTypeEl.select(SEARCH, true);
		importTypeEl.setVisible(securityModule.isUserAllowedBulk(ureq.getUserSession().getRoles()));
		
		doOpenSearch(ureq);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event instanceof SingleIdentityChosenEvent e) {
			membersContext.setSearchedIdentities(List.of(e.getChosenIdentity()));
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		} else if(event instanceof MultiIdentityChosenEvent e) {
			membersContext.setSearchedIdentities(new ArrayList<>(e.getChosenIdentities()));
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		} else if(source == importController) {
			if(event == StepsEvent.ACTIVATE_NEXT) {
				fireEvent(ureq, event);
			}
		} else {
			super.event(ureq, source, event);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(importTypeEl == source) {
			if(importTypeEl.isOneSelected()) {
				String selectedKey = importTypeEl.getSelectedKey();
				if(SEARCH.equals(selectedKey)) {
					doOpenSearch(ureq);
				} else if(LIST.equals(selectedKey)) {
					doOpenImport(ureq);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void formNext(UserRequest ureq) {
		// The import do it alone
		if(importTypeEl.isOneSelected() && SEARCH.equals(importTypeEl.getSelectedKey())) {
			List<Identity> identities = searchController.getSelectedIdentities();
			if(identities.isEmpty()) {
				searchController.doSearch(ureq);
			} else {
				membersContext.setSearchedIdentities(new ArrayList<>(identities));
				fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenSearch(UserRequest ureq) {
		removeAsListenerAndDispose(importController);
		importController = null;
		
		searchController = new UserSearchFlexiController(ureq, getWindowControl(), mainForm, null,
				new OrganisationRoles[] { OrganisationRoles.invitee, OrganisationRoles.guest }, true, false, false);
		listenTo(searchController);

		flc.add("search", searchController.getInitialFormItem());
	}
	
	private void doOpenImport(UserRequest ureq) {
		removeAsListenerAndDispose(searchController);
		searchController = null;
		
		importController = new ImportMemberByUsernamesController(ureq, getWindowControl(), mainForm,
				getRunContext(), ImportMemberByUsernamesController.RUN_CONTEXT_KEY, null);
		listenTo(importController);
		flc.add("search", importController.getInitialFormItem());
	}
}
