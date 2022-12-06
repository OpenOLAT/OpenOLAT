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
package org.olat.modules.qpool.ui;

import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.qpool.QPoolSecurityCallback;
import org.olat.modules.qpool.QuestionItemView;
import org.olat.modules.qpool.model.QItemType;
import org.olat.modules.qpool.ui.datasource.EmptyItemsSource;
import org.olat.modules.qpool.ui.datasource.FinalItemsSource;
import org.olat.modules.qpool.ui.events.QItemViewEvent;
import org.olat.modules.qpool.ui.tree.QPoolTaxonomyTreeBuilder;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ItemListMyCompetencesController extends AbstractItemListController {

	private FormLink selectLink;
    private SingleSelection myCompetenceLevelsEl;
    
    @Autowired
    private QPoolTaxonomyTreeBuilder qpoolTaxonomyTreeBuilder;


	public ItemListMyCompetencesController(UserRequest ureq, WindowControl wControl, QPoolSecurityCallback secCallback,
			String restrictToFormat, List<QItemType> excludeTypes) {
		super(ureq, wControl, secCallback, new EmptyItemsSource(), restrictToFormat, excludeTypes, "qti-select");
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
	}
	
	public boolean hasCompetences() {
		String[] keys = qpoolTaxonomyTreeBuilder.getSelectableKeys();
		return keys != null && keys.length > 0;
	}
	
	@Override
	protected void initButtons(UserRequest ureq, FormItemContainer formLayout) {
		getItemsTable().setMultiSelect(true);
		selectLink = uifactory.addFormLink("select-to-import", "select", null, formLayout, Link.BUTTON);

		qpoolTaxonomyTreeBuilder.loadTaxonomyLevelsFinal(getTranslator(), getIdentity());
		String[] levelKeys = qpoolTaxonomyTreeBuilder.getSelectableKeys();
		String[] levelValues = qpoolTaxonomyTreeBuilder.getSelectableValues();

		myCompetenceLevelsEl = uifactory.addDropdownSingleselect("source.selector", "my.competences", formLayout,
				levelKeys, levelValues, null);
		myCompetenceLevelsEl.setDomReplacementWrapperRequired(false);
		myCompetenceLevelsEl.addActionListener(FormEvent.ONCHANGE);
		if(levelKeys.length > 0) {
			myCompetenceLevelsEl.select(levelKeys[0], true);
			doSelectLevel(ureq, myCompetenceLevelsEl.getSelectedKey());
		}
	}
	
	@Override
	protected void initActionColumns(FlexiTableColumnModel columnsModel) {
		DefaultFlexiColumnModel selectCol = new DefaultFlexiColumnModel("select", translate("select"), "select-item");
		selectCol.setExportable(false);
		columnsModel.addFlexiColumnModel(selectCol);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(selectLink == source) {
			Set<Integer> selections = getItemsTable().getMultiSelectedIndex();
			if(!selections.isEmpty()) {
				List<QuestionItemView> items = getItemViews(selections);
				fireEvent(ureq, new QItemViewEvent("select-item", items));
			}
		} else if(myCompetenceLevelsEl == source) {
            String selectedKey = myCompetenceLevelsEl.getSelectedKey();
			if(StringHelper.isLong(selectedKey)) {
				doSelectLevel(ureq, selectedKey);
			}
        }
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doSelectLevel(UserRequest ureq, String levelKey) {
        TaxonomyLevel level = qpoolTaxonomyTreeBuilder.getTaxonomyLevel(levelKey);
		if(level == null) {
			updateSource(new EmptyItemsSource());
		} else {
			FinalItemsSource source = new FinalItemsSource(getIdentity(), ureq.getUserSession().getRoles(), getLocale(),
					level, TaxonomyUIFactory.translateDisplayName(getTranslator(), level));
			source.getDefaultParams().setFormat(restrictToFormat);
			source.getDefaultParams().setExcludedItemTypes(excludeTypes);
			updateSource(source);
		}
	}
	
	@Override
	protected void doSelect(UserRequest ureq, ItemRow row) {
		if(row == null) {
			showWarning("error.select.one");
		} else {
			fireEvent(ureq, new QItemViewEvent("select-item", row));
		}
	}
}
