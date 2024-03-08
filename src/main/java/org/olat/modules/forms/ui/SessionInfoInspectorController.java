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
package org.olat.modules.forms.ui;

import org.olat.core.commons.services.color.ColorService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.tabbedpane.TabbedPaneItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.model.AlertBoxSettings;
import org.olat.modules.ceditor.model.BlockLayoutSettings;
import org.olat.modules.ceditor.ui.PageElementTarget;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.ceditor.ui.event.ClosePartEvent;
import org.olat.modules.cemedia.ui.MediaUIHelper;
import org.olat.modules.forms.model.xml.SessionInformations;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2024-01-25<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class SessionInfoInspectorController extends FormBasicController implements PageElementInspectorController {
	private TabbedPaneItem tabbedPane;
	private MediaUIHelper.LayoutTabComponents layoutTabComponents;
	private MediaUIHelper.AlertBoxComponents alertBoxComponents;

	private final SessionInformations sessionInfo;

	@Autowired
	private ColorService colorService;

	public SessionInfoInspectorController(UserRequest ureq, WindowControl wControl, SessionInformations sessionInfo) {
		super(ureq, wControl, "session_info_inspector");
		this.sessionInfo = sessionInfo;
		initForm(ureq);
	}

	@Override
	public String getTitle() {
		return translate("add.formsessioninformations");
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		tabbedPane = uifactory.addTabbedPane("tabPane", getLocale(), formLayout);
		tabbedPane.setTabIndentation(TabbedPaneItem.TabIndentation.none);
		formLayout.add("tabs", tabbedPane);

		addStyleTab(formLayout);
		addLayoutTab(formLayout);
	}

	private void addStyleTab(FormItemContainer formLayout) {
		alertBoxComponents = MediaUIHelper.addAlertBoxStyleTab(formLayout, tabbedPane, uifactory,
				getAlertBoxSettings(), colorService, getLocale());
	}

	private void addLayoutTab(FormItemContainer formLayout) {
		Translator translator = Util.createPackageTranslator(PageElementTarget.class, getLocale());
		layoutTabComponents = MediaUIHelper.addLayoutTab(formLayout, tabbedPane, translator, uifactory, getLayoutSettings(), velocity_root);
	}

	private BlockLayoutSettings getLayoutSettings() {
		if (sessionInfo.getLayoutSettings() != null) {
			return sessionInfo.getLayoutSettings();
		}
		return BlockLayoutSettings.getPredefined();
	}

	private AlertBoxSettings getAlertBoxSettings() {
		if (sessionInfo.getAlertBoxSettings() != null) {
			return sessionInfo.getAlertBoxSettings();
		}
		return AlertBoxSettings.getPredefined();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (layoutTabComponents.matches(source)) {
			doChangeLayout(ureq);
		} else if (alertBoxComponents.matches(source)) {
			doChangeAlertBoxSettings(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSave(ureq);
		fireEvent(ureq, new ClosePartEvent(sessionInfo));
	}

	private void doSave(UserRequest ureq) {
		fireEvent(ureq, new ChangePartEvent(sessionInfo));
	}

	private void doChangeLayout(UserRequest ureq) {
		BlockLayoutSettings layoutSettings = getLayoutSettings();
		layoutTabComponents.sync(layoutSettings);
		sessionInfo.setLayoutSettings(layoutSettings);
		fireEvent(ureq, new ChangePartEvent(sessionInfo));

		getInitialComponent().setDirty(true);
	}

	private void doChangeAlertBoxSettings(UserRequest ureq) {
		AlertBoxSettings alertBoxSettings = getAlertBoxSettings();
		alertBoxComponents.sync(alertBoxSettings);
		sessionInfo.setAlertBoxSettings(alertBoxSettings);
		fireEvent(ureq, new ChangePartEvent(sessionInfo));

		getInitialComponent().setDirty(true);
	}
}