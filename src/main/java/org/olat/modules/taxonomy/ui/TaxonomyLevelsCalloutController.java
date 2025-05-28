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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyLevelSearchParameters;
import org.olat.modules.taxonomy.ui.TaxonomyTreeTableModel.TaxonomyLevelCols;
import org.olat.modules.taxonomy.ui.events.OpenTaxonomyLevelEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: May 28, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TaxonomyLevelsCalloutController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private TaxonomyTreeTableModel tableModel;

	private TaxonomyLevel taxonomyLevel;
	
	@Autowired
	private TaxonomyService taxonomyService;

	protected TaxonomyLevelsCalloutController(UserRequest ureq, WindowControl wControl, TaxonomyLevel taxonomyLevel) {
		super(ureq, wControl, "structure");
		this.taxonomyLevel = taxonomyLevel;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		TreeNodeFlexiCellRenderer treeNodeRenderer = new TreeNodeFlexiCellRenderer("select");
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TaxonomyLevelCols.displayName, treeNodeRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TaxonomyLevelCols.identifier,
				new TaxonomyLevelSmallCellRenderer()));

		tableModel = new TaxonomyTreeTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 5000, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_taxonomy_level_structure o_table_reduced");
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setExportEnabled(false);
	}
	
	public class TaxonomyLevelSmallCellRenderer implements FlexiCellRenderer {
		
		@Override
		public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
				URLBuilder ubu, Translator trans) {
			if (cellValue instanceof String string) {
				target.append("<small class='mute'> \u00B7 ").appendHtmlEscaped(string).append("</small>");
			}
		}
	}
	
	private void loadModel() {
		TaxonomyLevelSearchParameters searchParams = new TaxonomyLevelSearchParameters();
		searchParams.setParentLevel(taxonomyLevel);
		
		List<TaxonomyLevel> taxonomyLevels = taxonomyService.getTaxonomyLevels(taxonomyLevel.getTaxonomy(), searchParams);
		List<TaxonomyLevelRow> rows = new ArrayList<>(taxonomyLevels.size());
		Map<Long,TaxonomyLevelRow> levelToRows = new HashMap<>();
		for(TaxonomyLevel taxonomyLevel:taxonomyLevels) {
			TaxonomyLevelRow row = forgeRow(taxonomyLevel);
			rows.add(row);
			levelToRows.put(taxonomyLevel.getKey(), row);
		}
		
		for(TaxonomyLevelRow row:rows) {
			Long parentLevelKey = row.getParentLevelKey();
			TaxonomyLevelRow parentRow = levelToRows.get(parentLevelKey);
			row.setParent(parentRow);
		}
		
		for(TaxonomyLevelRow row:rows) {
			for(FlexiTreeTableNode parent=row.getParent(); parent != null; parent=parent.getParent()) {
				((TaxonomyLevelRow)parent).incrementNumberOfChildren();
			}
		}
		
		try {
			rows.sort(new TaxonomyTreeTableController.TaxonomyTreeNodeComparator());
		} catch (Exception e) {
			logError("Cannot sort taxonomy tree", e);
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private TaxonomyLevelRow forgeRow(TaxonomyLevel taxonomyLevel) {
		String displayName = TaxonomyUIFactory.translateDisplayName(getTranslator(), taxonomyLevel);
		TaxonomyLevelRow row = new TaxonomyLevelRow(taxonomyLevel, getLocale().toString(), displayName, null, null, null);
		return row;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				TaxonomyLevelRow row = tableModel.getObject(se.getIndex());
				fireEvent(ureq, new OpenTaxonomyLevelEvent(row.getTaxonomyLevel()));
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}
