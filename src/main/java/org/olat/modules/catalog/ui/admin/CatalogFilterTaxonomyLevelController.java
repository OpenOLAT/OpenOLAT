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

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.catalog.CatalogFilter;
import org.olat.modules.catalog.CatalogFilterHandler;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyRefImpl;
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
	private TaxonomyService taxonomyService;
	@Autowired
	private RepositoryModule repositoryModule;

	public CatalogFilterTaxonomyLevelController(UserRequest ureq, WindowControl wControl, CatalogFilterHandler handler, CatalogFilter catalogFilter) {
		super(ureq, wControl, handler, catalogFilter);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout) {
		String taxonomyTreeKey = repositoryModule.getTaxonomyTreeKey();
		if (StringHelper.isLong(taxonomyTreeKey)) {
			TaxonomyRef taxonomyRef = new TaxonomyRefImpl(Long.valueOf(taxonomyTreeKey));
			List<TaxonomyLevel> allTaxonomyLevels = taxonomyService.getTaxonomyLevels(taxonomyRef);
			SelectionValues keyValues = RepositoyUIFactory.createTaxonomyLevelKV(allTaxonomyLevels);
			taxonomyLevelEl = uifactory.addDropdownSingleselect("taxonomyLevels", "admin.taxonomy.levels", formLayout,
					keyValues.keys(), keyValues.values());
			taxonomyLevelEl.setMandatory(true);
			String key = getCatalogFilter() != null? getCatalogFilter().getConfig(): null;
			if (StringHelper.containsNonWhitespace(key) && taxonomyLevelEl.containsKey(key)) {
				taxonomyLevelEl.select(key, true);
			}
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
