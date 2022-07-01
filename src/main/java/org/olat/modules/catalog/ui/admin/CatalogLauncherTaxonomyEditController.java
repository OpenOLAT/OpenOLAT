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

import static org.olat.core.gui.components.util.SelectionValues.VALUE_ASC;
import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.catalog.CatalogLauncher;
import org.olat.modules.catalog.CatalogLauncherHandler;
import org.olat.modules.catalog.launcher.TaxonomyLevelLauncherHandler;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.repository.RepositoryModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogLauncherTaxonomyEditController extends AbstractLauncherEditController {
	
	private SingleSelection taxonomyLevelEl;
	
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private RepositoryModule repositoryModule;

	public CatalogLauncherTaxonomyEditController(UserRequest ureq, WindowControl wControl,
			CatalogLauncherHandler handler, CatalogLauncher catalogLauncher) {
		super(ureq, wControl, handler, catalogLauncher);
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout) {
		String taxonomyTreeKey = repositoryModule.getTaxonomyTreeKey();
		if (StringHelper.isLong(taxonomyTreeKey)) {
			Taxonomy taxonomy = taxonomyService.getTaxonomy(() -> Long.valueOf(taxonomyTreeKey));
			List<TaxonomyLevel> allTaxonomyLevels = taxonomyService.getTaxonomyLevels(taxonomy);
			SelectionValues keyValues = createTaxonomyLevelKV(taxonomy, allTaxonomyLevels);
			keyValues.add(entry(TaxonomyLevelLauncherHandler.TAXONOMY_PREFIX + taxonomy.getKey(), taxonomy.getDisplayName()));
			keyValues.sort(VALUE_ASC);
			taxonomyLevelEl = uifactory.addDropdownSingleselect("taxonomyLevels", "admin.taxonomy.levels", formLayout,
					keyValues.keys(), keyValues.values());
			taxonomyLevelEl.setMandatory(true);
			String key = getCatalogLauncher() != null? getCatalogLauncher().getConfig(): null;
			if (StringHelper.containsNonWhitespace(key) && taxonomyLevelEl.containsKey(key)) {
				taxonomyLevelEl.select(key, true);
			}
		}
	}
	
	private SelectionValues createTaxonomyLevelKV(Taxonomy taxonomy, List<TaxonomyLevel> allTaxonomyLevels) {
		SelectionValues keyValues = new SelectionValues();
		for (TaxonomyLevel level:allTaxonomyLevels) {
			String key = Long.toString(level.getKey());
			ArrayList<String> names = new ArrayList<>();
			addParentNames(names, level);
			Collections.reverse(names);
			String value = taxonomy.getDisplayName() + ": " + String.join(" / ", names);
			keyValues.add(entry(key, value));
		}
		return keyValues;
	}
	
	private void addParentNames(List<String> names, TaxonomyLevel level) {
		names.add(TaxonomyUIFactory.translateDisplayName(getTranslator(), level));
		TaxonomyLevel parent = level.getParent();
		if (parent != null) {
			addParentNames(names, parent);
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if (taxonomyLevelEl != null && !taxonomyLevelEl.isOneSelected()) {
			taxonomyLevelEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected String getConfig() {
		return taxonomyLevelEl.getSelectedKey();
	}

}
