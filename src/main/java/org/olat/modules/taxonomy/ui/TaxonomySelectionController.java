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
package org.olat.modules.taxonomy.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyInfos;
import org.olat.modules.taxonomy.ui.TaxonomyListDataModel.TaxonomyCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: May 21, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TaxonomySelectionController extends FormBasicController {

	private TaxonomyListDataModel dataModel;
	private FlexiTableElement tableEl;

	private Set<Long> taxonomyKeys;
	
	@Autowired
	private TaxonomyService taxonomyService;

	public TaxonomySelectionController(UserRequest ureq, WindowControl wControl, List<TaxonomyRef> taxonomies) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.taxonomyKeys = taxonomies.stream().map(TaxonomyRef::getKey).collect(Collectors.toSet());
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, TaxonomyCols.key, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TaxonomyCols.displayName, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TaxonomyCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TaxonomyCols.externalId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TaxonomyCols.creationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TaxonomyCols.numOfLevels));

		dataModel = new TaxonomyListDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setSearchEnabled(false);
		tableEl.setEmptyTableMessageKey("table.taxonomy.empty");
	}

	private void loadModel() {
		List<TaxonomyInfos> taxonomies = taxonomyService.getTaxonomyInfosList();
		
		List<TaxonomyRow> rows = new ArrayList<>(taxonomies.size());
		for (TaxonomyInfos taxonomy : taxonomies) {
			if (taxonomyKeys.contains(taxonomy.getKey())) {
				rows.add(new TaxonomyRow(taxonomy));
			}
		}
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (tableEl == source) {
			if (event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if ("select".equals(cmd)) {
					TaxonomyRow row = dataModel.getObject(se.getIndex());
					doSelectTaxonomyL(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doSelectTaxonomyL(UserRequest ureq, TaxonomyRow row) {
		Taxonomy taxonomy = taxonomyService.getTaxonomy(() -> row.getKey());
		if (taxonomy != null) {
			fireEvent(ureq, new TaxonomySelectionEvent(taxonomy));
		}
	}
	
	public static final class TaxonomySelectionEvent extends Event {
		
		private static final long serialVersionUID = -546542741844230201L;
		
		private final Taxonomy taxonomy;
		
		public TaxonomySelectionEvent(Taxonomy taxonomy) {
			super("taxonomy.selection");
			this.taxonomy = taxonomy;
		}
		
		public Taxonomy getTaxonomy() {
			return taxonomy;
		}
		
	}

}
