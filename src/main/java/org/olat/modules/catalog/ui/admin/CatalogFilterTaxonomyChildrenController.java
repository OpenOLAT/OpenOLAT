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

import static org.olat.modules.catalog.filter.TaxonomyLevelChildrenHandler.KEY_HIDE;
import static org.olat.modules.catalog.filter.TaxonomyLevelChildrenHandler.KEY_SHOW;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.catalog.CatalogFilter;
import org.olat.modules.catalog.CatalogFilterHandler;

/**
 * 
 * Initial date: 14 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogFilterTaxonomyChildrenController extends AbstractFilterEditController {
	
	private MultipleSelectionElement defaultEl;

	public CatalogFilterTaxonomyChildrenController(UserRequest ureq, WindowControl wControl, CatalogFilterHandler handler, CatalogFilter catalogFilter) {
		super(ureq, wControl, handler, catalogFilter);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout) {
		SelectionValues childrenSV = new SelectionValues();
		childrenSV.add(SelectionValues.entry(KEY_SHOW, translate("filter.taxonomy.children.default.show")));
		defaultEl = uifactory.addCheckboxesHorizontal("filter.taxonomy.children.default", formLayout, childrenSV.keys(), childrenSV.values());
		defaultEl.select(defaultEl.getKey(0), getCatalogFilter() == null || KEY_SHOW.equals(getCatalogFilter().getConfig()));
	}

	@Override
	protected String getConfig() {
		return defaultEl.isAtLeastSelected(1)? KEY_SHOW: KEY_HIDE;
	}

}
