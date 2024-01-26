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
package org.olat.modules.ceditor.ui;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.tabbedpane.TabbedPaneItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.model.BlockLayoutSettings;
import org.olat.modules.ceditor.model.MathElement;
import org.olat.modules.ceditor.model.MathSettings;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.cemedia.ui.MediaUIHelper;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2024-01-22<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class MathLiveInspectorController extends FormBasicController implements PageElementInspectorController {

	private TabbedPaneItem tabbedPane;
	private MediaUIHelper.LayoutTabComponents layoutTabComponents;

	private MathElement math;

	private final PageElementStore<MathElement> store;

	@Autowired
	private DB dbInstance;

	public MathLiveInspectorController(UserRequest ureq, WindowControl wControl, MathElement math, PageElementStore<MathElement> store) {
		super(ureq, wControl, "tabs_inspector");
		this.math = math;
		this.store = store;
		initForm(ureq);
	}

	@Override
	public String getTitle() {
		return translate("inspector.math");
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		tabbedPane = uifactory.addTabbedPane("tabPane", getLocale(), formLayout);
		tabbedPane.setTabIndentation(TabbedPaneItem.TabIndentation.none);
		formLayout.add("tabs", tabbedPane);

		addLayoutTab(formLayout);
	}

	private void addLayoutTab(FormItemContainer formLayout) {
		BlockLayoutSettings layoutSettings = getLayoutSettings(getMathSettings());
		layoutTabComponents = MediaUIHelper.addLayoutTab(formLayout, tabbedPane, getTranslator(), uifactory, layoutSettings, velocity_root);
	}

	private BlockLayoutSettings getLayoutSettings(MathSettings mathSettings) {
		if (mathSettings.getLayoutSettings() != null) {
			return mathSettings.getLayoutSettings();
		}
		return BlockLayoutSettings.getPredefined();
	}

	private MathSettings getMathSettings() {
		if (math.getMathSettings() != null) {
			return math.getMathSettings();
		}
		return new MathSettings();
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (layoutTabComponents.matches(source)) {
			doChangeLayout(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doSave(ureq);
	}

	private void doSave(UserRequest ureq) {
		math = store.savePageElement(math);
		dbInstance.commit();
		fireEvent(ureq, new ChangePartEvent(math));
	}

	private void doChangeLayout(UserRequest ureq) {
		MathSettings mathSettings = getMathSettings();

		BlockLayoutSettings layoutSettings = getLayoutSettings(mathSettings);
		layoutTabComponents.sync(layoutSettings);
		mathSettings.setLayoutSettings(layoutSettings);

		math.setMathSettings(mathSettings);
		doSave(ureq);

		getInitialComponent().setDirty(true);
	}
}
