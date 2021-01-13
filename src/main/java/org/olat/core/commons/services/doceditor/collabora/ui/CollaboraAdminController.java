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
package org.olat.core.commons.services.doceditor.collabora.ui;

import static org.olat.core.commons.services.doceditor.collabora.CollaboraService.REFRESH_EVENT_ORES;
import static org.olat.core.commons.services.doceditor.collabora.ui.CollaboraUIFactory.validateIsMandatory;
import static org.olat.core.gui.components.util.KeyValues.entry;
import static org.olat.core.gui.translator.TranslatorHelper.translateAll;

import java.util.Collection;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.doceditor.collabora.CollaboraModule;
import org.olat.core.commons.services.doceditor.collabora.CollaboraRefreshDiscoveryEvent;
import org.olat.core.commons.services.doceditor.collabora.CollaboraService;
import org.olat.core.commons.services.doceditor.discovery.Discovery;
import org.olat.core.commons.services.doceditor.ui.DocEditorController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 05.03.2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CollaboraAdminController extends FormBasicController {

	private static final Logger log = Tracing.createLoggerFor(CollaboraAdminController.class);

	private static final String[] ENABLED_KEYS = new String[]{"on"};
	private static final String USAGE_AUTHOR = "author";
	private static final String USAGE_COACH = "coach";
	private static final String USAGE_MANAGERS = "managers";
	
	private MultipleSelectionElement enabledEl;
	private TextElement baseUrlEl;
	private FormLink refreshDiscoveryLink;
	private FormLink testLink;
	private MultipleSelectionElement dataTransferConfirmationEnabledEl;
	private MultipleSelectionElement usageRolesEl;

	@Autowired
	private CollaboraModule collaboraModule;
	@Autowired
	private CollaboraService collaboraService;

	public CollaboraAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(DocEditorController.class, getLocale(), getTranslator()));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.title");
		setFormDescription("admin.desc");
		
		enabledEl = uifactory.addCheckboxesHorizontal("admin.enabled", formLayout, ENABLED_KEYS, translateAll(getTranslator(), ENABLED_KEYS));
		enabledEl.select(ENABLED_KEYS[0], collaboraModule.isEnabled());
		
		String url = collaboraModule.getBaseUrl();
		baseUrlEl = uifactory.addTextElement("admin.url", 128, url, formLayout);
		baseUrlEl.setMandatory(true);
		
		refreshDiscoveryLink = uifactory.addFormLink("admin.refresh.discovery", "admin.refresh.discovery", "admin.refresh.discovery.label", formLayout, Link.BUTTON);
		refreshDiscoveryLink.setHelpTextKey("admin.refresh.discovery.help", null);
		
		dataTransferConfirmationEnabledEl = uifactory.addCheckboxesHorizontal(
				"admin.data.transfer.confirmation.enabled", formLayout, ENABLED_KEYS,
				translateAll(getTranslator(), ENABLED_KEYS));
		dataTransferConfirmationEnabledEl.select(ENABLED_KEYS[0], collaboraModule.isDataTransferConfirmationEnabled());
		
		KeyValues usageRolesKV = new KeyValues();
		usageRolesKV.add(entry(USAGE_AUTHOR, translate("admin.usage.roles.author")));
		usageRolesKV.add(entry(USAGE_COACH, translate("admin.usage.roles.coach")));
		usageRolesKV.add(entry(USAGE_MANAGERS, translate("admin.usage.roles.managers")));
		usageRolesEl = uifactory.addCheckboxesVertical("admin.usage.roles", formLayout, usageRolesKV.keys(), usageRolesKV.values(), 1);
		usageRolesEl.setHelpTextKey("admin.usage.roles.help", null);
		usageRolesEl.select(USAGE_AUTHOR, collaboraModule.isUsageRestrictedToAuthors());
		usageRolesEl.select(USAGE_COACH, collaboraModule.isUsageRestrictedToCoaches());
		usageRolesEl.select(USAGE_MANAGERS, collaboraModule.isUsageRestrictedToManagers());
		
		if (Settings.isDebuging()) {
			testLink = uifactory.addFormLink("admin.test", formLayout, Link.BUTTON);
		}
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == refreshDiscoveryLink) {
			doRefreshDiscovery();
		} else if (source == testLink) {
			doTest();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if (enabledEl.isAtLeastSelected(1)) {
			allOk &= validateIsMandatory(baseUrlEl);
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = enabledEl.isAtLeastSelected(1);
		collaboraModule.setEnabled(enabled);
		
		String url = baseUrlEl.getValue();
		url = url.endsWith("/")? url: url + "/";
		boolean urlChanged = !url.equals(collaboraModule.getBaseUrl());
		collaboraModule.setBaseUrl(url);
		if (urlChanged) {
			doRefreshDiscovery();
		}
		
		boolean dataTransferConfirmationEnabled = dataTransferConfirmationEnabledEl.isAtLeastSelected(1);
		collaboraModule.setDataTransferConfirmationEnabled(dataTransferConfirmationEnabled);
		
		Collection<String> restrictionKeys = usageRolesEl.getSelectedKeys();
		collaboraModule.setUsageRestrictedToAuthors(restrictionKeys.contains(USAGE_AUTHOR));
		collaboraModule.setUsageRestrictedToCoaches(restrictionKeys.contains(USAGE_COACH));
		collaboraModule.setUsageRestrictedToManagers(restrictionKeys.contains(USAGE_MANAGERS));
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private void doRefreshDiscovery() {
		// Notify all cluster nodes to refresh the discovery
		CollaboraRefreshDiscoveryEvent event = new CollaboraRefreshDiscoveryEvent();
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(event, REFRESH_EVENT_ORES);
	}

	private void doTest() {
		Discovery discoveryImpl = collaboraService.getDiscovery();
		log.info("Discovery net zone: " + discoveryImpl.getNetZones().get(0).getName());
	 }

}
