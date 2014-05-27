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
package org.olat.admin.restapi;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.group.BusinessGroupModule;
import org.olat.repository.RepositoryModule;
import org.olat.restapi.RestModule;
import org.olat.restapi.security.RestSecurityHelper;

/**
 * 
 * Description:<br>
 * This is a controller to configure the SimpleVersionConfig, the configuration
 * of the versioning system for briefcase.
 * 
 * <P>
 * Initial Date:  21 sept. 2009 <br>
 *
 * @author srosse
 */
public class RestapiAdminController extends FormBasicController {
	
	private MultipleSelectionElement enabled, managedGroupsEl, managedRepoEl;
	private FormLayoutContainer docLinkFlc;
	
	private static final String[] keys = {"on"};
	
	private final RestModule restModule;
	private final BusinessGroupModule groupModule;
	private final RepositoryModule repositoryModule;

	public RestapiAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "rest");

		restModule = CoreSpringFactory.getImpl(RestModule.class);
		groupModule = CoreSpringFactory.getImpl(BusinessGroupModule.class);
		repositoryModule = CoreSpringFactory.getImpl(RepositoryModule.class);
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("rest.title");
		setFormContextHelp(RestapiAdminController.class.getPackage().getName(), "rest.html", "help.hover.rest");
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutContainer = (FormLayoutContainer)formLayout;
			
			boolean restEnabled = restModule.isEnabled();
			docLinkFlc = FormLayoutContainer.createCustomFormLayout("doc_link", getTranslator(), velocity_root + "/docLink.html");
			layoutContainer.add(docLinkFlc);
			docLinkFlc.setVisible(restEnabled);
			
			String link = Settings.getServerContextPathURI() + RestSecurityHelper.SUB_CONTEXT + "/api/doc";
			docLinkFlc.contextPut("docLink", link);
			
			FormLayoutContainer accessDataFlc = FormLayoutContainer.createDefaultFormLayout("flc_access_data", getTranslator());
			layoutContainer.add(accessDataFlc);

			String[] values = new String[] { getTranslator().translate("rest.on") };
			enabled = uifactory.addCheckboxesHorizontal("rest.enabled", accessDataFlc, keys, values);
			enabled.select(keys[0], restEnabled);
			enabled.addActionListener(FormEvent.ONCHANGE);
			
			accessDataFlc.setVisible(true);
			formLayout.add(accessDataFlc);
			
			FormLayoutContainer managedFlc = FormLayoutContainer.createDefaultFormLayout("flc_managed", getTranslator());
			layoutContainer.add(managedFlc);
			
			String[] valueGrps = new String[] { getTranslator().translate("rest.on") };
			managedGroupsEl = uifactory.addCheckboxesHorizontal("managed.group", managedFlc, keys, valueGrps);
			managedGroupsEl.addActionListener(FormEvent.ONCHANGE);
			managedGroupsEl.select(keys[0], groupModule.isManagedBusinessGroups());
			
			String[] valueRes = new String[] { getTranslator().translate("rest.on") };
			managedRepoEl = uifactory.addCheckboxesHorizontal("managed.repo", managedFlc, keys, valueRes);
			managedRepoEl.addActionListener(FormEvent.ONCHANGE);
			managedRepoEl.select(keys[0], repositoryModule.isManagedRepositoryEntries());
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == enabled) {
			boolean on = enabled.isAtLeastSelected(1);
			restModule.setEnabled(on);
			docLinkFlc.setVisible(on);
			getWindowControl().setInfo("saved");
		} else if(source == managedGroupsEl) {
			boolean enabled = managedGroupsEl.isAtLeastSelected(1);
			groupModule.setManagedBusinessGroups(enabled);
		} else if (source == managedRepoEl) {
			boolean enable = managedRepoEl.isAtLeastSelected(1);
			repositoryModule.setManagedRepositoryEntries(enable);
		}
		super.formInnerEvent(ureq, source, event);
	}
}
