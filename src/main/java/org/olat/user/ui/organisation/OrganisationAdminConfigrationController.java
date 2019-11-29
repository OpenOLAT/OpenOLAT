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
package org.olat.user.ui.organisation;

import org.olat.basesecurity.OrganisationModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationAdminConfigrationController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	
	private MultipleSelectionElement enableEl;
	private FormLink migrationLink;
	
	private CloseableModalController cmc;
	private InstitutionMigratorController migratorCtrl;
	
	@Autowired
	private OrganisationModule organisationModule;
	
	public OrganisationAdminConfigrationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
		updateVisibility();
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("admin.description");
		setFormContextHelp("Modules: Organisations");
		
		String[] onValues = new String[] { translate("on") };
		enableEl = uifactory.addCheckboxesHorizontal("organisation.admin.enabled", formLayout, onKeys, onValues);
		enableEl.addActionListener(FormEvent.ONCHANGE);
		if(organisationModule.isEnabled()) {
			enableEl.select(onKeys[0], true);
		}
		
		migrationLink = uifactory.addFormLink("institution.to.org", formLayout);
		migrationLink.setLabel("institution.to.org.label", null);
	}
	
	private void updateVisibility() {
		migrationLink.setVisible(enableEl.isAtLeastSelected(1));
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(migratorCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(migratorCtrl);
		removeAsListenerAndDispose(cmc);
		migratorCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableEl == source) {
			organisationModule.setEnabled(enableEl.isAtLeastSelected(1));
			updateVisibility();
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if(migrationLink == source) {
			doConfirmMigration(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doConfirmMigration(UserRequest ureq) {
		if(guardModalController(migratorCtrl)) return;
		
		migratorCtrl = new InstitutionMigratorController(ureq, getWindowControl());
		listenTo(migratorCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", migratorCtrl.getInitialComponent(),
				true, translate("add.root.type"));
		listenTo(cmc);
		cmc.activate();
	}
}
