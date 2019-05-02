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
package org.olat.core.commons.services.doceditor.office365.ui;

import static org.olat.core.commons.services.doceditor.office365.Office365Service.REFRESH_EVENT_ORES;
import static org.olat.core.commons.services.doceditor.office365.ui.Office365UIFactory.validateIsMandatory;
import static org.olat.core.gui.translator.TranslatorHelper.translateAll;

import org.olat.core.commons.services.doceditor.office365.Office365Module;
import org.olat.core.commons.services.doceditor.office365.Office365RefreshDiscoveryEvent;
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
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 02.05.2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class Office365AdminController extends FormBasicController {

	private static final String[] ENABLED_KEYS = new String[]{"on"};
	
	private MultipleSelectionElement enabledEl;
	private TextElement baseUrlEl;
	private FormLink refreshDiscoveryLink;

	@Autowired
	private Office365Module office365Module;

	public Office365AdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.title");
		setFormDescription("admin.desc");
		
		enabledEl = uifactory.addCheckboxesHorizontal("admin.enabled", formLayout, ENABLED_KEYS, translateAll(getTranslator(), ENABLED_KEYS));
		enabledEl.select(ENABLED_KEYS[0], office365Module.isEnabled());
		
		String url = office365Module.getBaseUrl();
		baseUrlEl = uifactory.addTextElement("admin.url", 128, url, formLayout);
		baseUrlEl.setMandatory(true);
		
		refreshDiscoveryLink = uifactory.addFormLink("admin.refresh.discovery", "admin.refresh.discovery", "admin.refresh.discovery.label", formLayout, Link.BUTTON);
		refreshDiscoveryLink.setHelpTextKey("admin.refresh.discovery.help", null);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add("buttons", buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == refreshDiscoveryLink) {
			doRefreshDiscovery();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		if (enabledEl.isAtLeastSelected(1)) {
			allOk &= validateIsMandatory(baseUrlEl);
		}
		
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = enabledEl.isAtLeastSelected(1);
		office365Module.setEnabled(enabled);
		
		String url = baseUrlEl.getValue();
		url = url.endsWith("/")? url: url + "/";
		boolean urlChanged = !url.equals(office365Module.getBaseUrl());
		office365Module.setBaseUrl(url);
		if (urlChanged) {
			doRefreshDiscovery();
		}
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private void doRefreshDiscovery() {
		// Notify all cluster nodes to refresh the discovery
		Office365RefreshDiscoveryEvent event = new Office365RefreshDiscoveryEvent();
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(event, REFRESH_EVENT_ORES);
	}

}
