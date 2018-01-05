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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.qpool.ui.datasource.EmptyItemsSource;
import org.olat.modules.qpool.ui.datasource.FinalItemsSource;
import org.olat.modules.qpool.ui.events.QItemViewEvent;
import org.olat.modules.qpool.ui.tree.QPoolTaxonomyTreeBuilder;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 5 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ItemListMyCompetencesController extends AbstractItemListController {

    private SingleSelection myCompetenceLevelsEl;
    
    @Autowired
    private QPoolTaxonomyTreeBuilder qpoolTaxonomyTreeBuilder;


	public ItemListMyCompetencesController(UserRequest ureq, WindowControl wControl, String restrictToFormat) {
		super(ureq, wControl, new EmptyItemsSource(), restrictToFormat, "select");
	}
	
	@Override
	protected void initButtons(UserRequest ureq, FormItemContainer formLayout) {
		getItemsTable().setMultiSelect(true);

		qpoolTaxonomyTreeBuilder.loadTaxonomyLevelsFinal(getIdentity());
		String[] levelKeys = qpoolTaxonomyTreeBuilder.getSelectableKeys();
		String[] levelValues = qpoolTaxonomyTreeBuilder.getSelectableValues();

		myCompetenceLevelsEl = uifactory.addDropdownSingleselect("source.selector", "my.competences", formLayout,
				levelKeys, levelValues, null);
		myCompetenceLevelsEl.setDomReplacementWrapperRequired(false);
		myCompetenceLevelsEl.getLabelC().setDomReplaceable(false);
		myCompetenceLevelsEl.addActionListener(FormEvent.ONCHANGE);
		if(levelKeys.length > 0) {
			myCompetenceLevelsEl.select(levelKeys[0], true);
			doSelectLevel(ureq, myCompetenceLevelsEl.getSelectedKey());
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(myCompetenceLevelsEl == source) {
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
			FinalItemsSource source = new FinalItemsSource(getIdentity(), ureq.getUserSession().getRoles(), level);
			source.getDefaultParams().setFormat(restrictToFormat);
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
