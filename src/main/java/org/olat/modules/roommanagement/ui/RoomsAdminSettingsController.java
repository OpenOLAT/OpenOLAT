/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.roommanagement.ui;

import org.olat.admin.site.ui.SitesConfigurationController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.navigation.SiteConfiguration;
import org.olat.core.gui.control.navigation.SiteDefinitions;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.site.CurriculumAdminSiteDef;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.roommanagement.RoomManagementModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 28 May 2026<br>
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class RoomsAdminSettingsController extends FormBasicController {

	private FormToggle enabledEl;

	@Autowired
	private RoomManagementModule roomManagementModule;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private CurriculumAdminSiteDef curriculumAdminSiteDef;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private SiteDefinitions siteDefinitions;

	public RoomsAdminSettingsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer moduleCont = FormLayoutContainer.createDefaultFormLayout("module", getTranslator());
		moduleCont.setFormTitle(translate("admin.module.title"));
		moduleCont.setFormInfo(translate("admin.module.info"));
		formLayout.add(moduleCont);

		enabledEl = uifactory.addToggleButton("admin.enabled", "admin.module.enabled",
				translate("on"), translate("off"), moduleCont);
		enabledEl.toggle(roomManagementModule.isEnabled());
		enabledEl.addActionListener(FormEvent.ONCHANGE);

		initDependenciesSection(formLayout);
	}

	private void initDependenciesSection(FormItemContainer formLayout) {
		FormLayoutContainer dependenciesCont = FormLayoutContainer.createCustomFormLayout(
				"dependencies", getTranslator(), velocity_root + "/module_dependencies.html");
		formLayout.add(dependenciesCont);

		boolean curriculumEnabled = curriculumModule.isEnabled();
		dependenciesCont.contextPut("curriculumEnabled", curriculumEnabled);
		if (curriculumEnabled) {
			SiteConfiguration siteConfig = siteDefinitions.getConfigurationSite(curriculumAdminSiteDef);
			String secCallbackBeanId = siteConfig == null ? null : siteConfig.getSecurityCallbackBeanId();
			if (StringHelper.containsNonWhitespace(secCallbackBeanId)) {
				Translator siteTranslator = Util.createPackageTranslator(SitesConfigurationController.class, getLocale());
				dependenciesCont.contextPut("curriculumAccess", siteTranslator.translate(secCallbackBeanId));
			}
		}

		dependenciesCont.contextPut("lectureEnabled", lectureModule.isEnabled());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == enabledEl) {
			roomManagementModule.setEnabled(enabledEl.isOn());
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
