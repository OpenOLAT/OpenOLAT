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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.catalog.CatalogFilter;
import org.olat.modules.catalog.CatalogFilterHandler;
import org.olat.modules.catalog.CatalogLauncher;
import org.olat.modules.catalog.CatalogLauncherHandler;
import org.olat.modules.catalog.CatalogLauncherSearchParams;
import org.olat.modules.catalog.CatalogV2Service;
import org.olat.modules.catalog.launcher.TaxonomyLevelLauncherHandler;
import org.olat.modules.catalog.launcher.TaxonomyLevelLauncherHandler.Config;
import org.olat.modules.catalog.ui.CatalogV2UIFactory;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryModule;
import org.olat.repository.ui.RepositoyUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogFilterTaxonomyLevelController extends AbstractFilterEditController {

	private SingleSelection taxonomyLevelEl;
	
	@Autowired
	private CatalogV2Service catalogService;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private RepositoryModule repositoryModule;

	public CatalogFilterTaxonomyLevelController(UserRequest ureq, WindowControl wControl, CatalogFilterHandler handler, CatalogFilter catalogFilter) {
		super(ureq, wControl, handler, catalogFilter);
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		
		initForm(ureq);
		validateTaxonomyLevelInLauncher();
	}

	@Override
	protected void initForm(FormItemContainer formLayout) {
		List<TaxonomyRef> taxonomyRefs = repositoryModule.getTaxonomyRefs();
		if (!taxonomyRefs.isEmpty()) {
			List<TaxonomyLevel> allTaxonomyLevels = taxonomyService.getTaxonomyLevels(taxonomyRefs);
			SelectionValues keyValues = RepositoyUIFactory.createTaxonomyLevelKV(getTranslator(), allTaxonomyLevels);
			taxonomyLevelEl = uifactory.addDropdownSingleselect("taxonomyLevels", "admin.taxonomy.levels", formLayout,
					keyValues.keys(), keyValues.values());
			taxonomyLevelEl.setMandatory(true);
			taxonomyLevelEl.addActionListener(FormEvent.ONCHANGE);
			String key = getCatalogFilter() != null? getCatalogFilter().getConfig(): null;
			if (StringHelper.containsNonWhitespace(key) && taxonomyLevelEl.containsKey(key)) {
				taxonomyLevelEl.select(key, true);
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == taxonomyLevelEl) {
			validateTaxonomyLevelInLauncher();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		taxonomyLevelEl.clearError();
		if (!taxonomyLevelEl.isOneSelected()) {
			taxonomyLevelEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		} else {
			validateTaxonomyLevelInLauncher();
		}
		
		return allOk;
	}

	@Override
	protected String getConfig() {
		return taxonomyLevelEl.getSelectedKey();
	}
	
	private void validateTaxonomyLevelInLauncher() {
		taxonomyLevelEl.clearError();
		if (!taxonomyLevelEl.isOneSelected()) {
			return;
		}
		
		Long selectedKey = Long.valueOf(taxonomyLevelEl.getSelectedKey());
		List<String> launcherNames = new ArrayList<>(1);
		
		List<CatalogLauncher> catalogLaunchers = catalogService.getCatalogLaunchers(new CatalogLauncherSearchParams());
		Collections.sort(catalogLaunchers);
		for (CatalogLauncher catalogLauncher : catalogLaunchers) {
			CatalogLauncherHandler handler = catalogService.getCatalogLauncherHandler(catalogLauncher.getType());
			if (handler instanceof TaxonomyLevelLauncherHandler) {
				TaxonomyLevelLauncherHandler taxonomyLevelLauncherHandler = (TaxonomyLevelLauncherHandler)handler;
				Config launcherConfig = taxonomyLevelLauncherHandler.fromXML(catalogLauncher.getConfig());
				if (launcherConfig != null && launcherConfig.getTaxonomyLevelKey() != null) {
					if (selectedKey.equals(launcherConfig.getTaxonomyLevelKey())) {
						String launcherName = CatalogV2UIFactory.translateLauncherName(getTranslator(), handler, catalogLauncher);
						launcherNames.add(launcherName);
					}
				}
			}
		}
		
		if (!launcherNames.isEmpty()) {
			String names = launcherNames.stream().collect(Collectors.joining(", "));
			taxonomyLevelEl.setWarningKey("error.taxonomy.level.filter.hidden", names);
		}
	}

}
