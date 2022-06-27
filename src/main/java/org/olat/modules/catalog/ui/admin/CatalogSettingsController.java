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
package org.olat.modules.catalog.ui.admin;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.catalog.ui.CatalogV2UIFactory;
import org.olat.repository.RepositoryModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 May 2022<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogSettingsController extends FormBasicController {

	private static final String KEY_NONE = "none";
	private static final String KEY_V1 = "v1";
	private static final String KEY_V2 = "v2";

	private SingleSelection enabledEl;

	@Autowired
	private CatalogV2Module catalogV2Module;
	@Autowired
	private RepositoryModule repositoryModule;

	public CatalogSettingsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(CatalogV2UIFactory.class, ureq.getLocale()));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.settings");
		
		SelectionValues enabledSV = new SelectionValues();
		enabledSV.add(entry(KEY_NONE, translate("admin.enabled.none")));
		enabledSV.add(entry(KEY_V1, translate("admin.enabled.v1")));
		enabledSV.add(entry(KEY_V2, translate("admin.enabled.v2")));
		enabledEl = uifactory.addRadiosVertical("admin.enabled", formLayout, enabledSV.keys(), enabledSV.values());
		enabledEl.addActionListener(FormEvent.ONCHANGE);
		if (catalogV2Module.isEnabled()) {
			enabledEl.select(KEY_V2, true);
		} else if (repositoryModule.isCatalogEnabled()) {
			enabledEl.select(KEY_V1, true);
		} else {
			enabledEl.select(KEY_NONE, true);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == enabledEl) {
			doSetEnabled();
			fireEvent(ureq, FormEvent.CHANGED_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doSetEnabled() {
		if (KEY_V2.equals(enabledEl.getSelectedKey())) {
			catalogV2Module.setEnabled(true);
			repositoryModule.setCatalogEnabled(false);
		} else if (KEY_V1.equals(enabledEl.getSelectedKey())) {
			catalogV2Module.setEnabled(false);
			repositoryModule.setCatalogEnabled(true);
		} else if (KEY_NONE.equals(enabledEl.getSelectedKey())) {
			catalogV2Module.setEnabled(false);
			repositoryModule.setCatalogEnabled(false);
		}
	}

}
