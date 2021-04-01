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
package org.olat.modules.taxonomy.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeNodeComparator;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.CompetenceBrowserTableModel.CompetenceBrowserCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 26.03.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CompetenceBrowserController extends FormBasicController {

	private CompetenceBrowserTableModel tableModel;
	private CompetenceBrowserTableRow rootCrumb;
	private FlexiTableElement tableEl;
	
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private PortfolioV2Module portfolioModule;
	
	public CompetenceBrowserController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// Create columns model
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CompetenceBrowserCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CompetenceBrowserCols.competences, new TreeNodeFlexiCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CompetenceBrowserCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CompetenceBrowserCols.description, new TextFlexiCellRenderer(EscapeMode.antisamy)));
		
		// Create table model
		tableModel = new CompetenceBrowserTableModel(columnsModel);
		
		// Add table model to formlayout
		tableEl = uifactory.addTableElement(getWindowControl(), "browser_table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setSearchEnabled(true);
		tableEl.setPageSize(20);
		
		// Create a fake root crumb
		rootCrumb = new CompetenceBrowserTableRow(getTranslator());
	}
	
	private void loadModel() {
		List<CompetenceBrowserTableRow> rows = new ArrayList<>();
		
		portfolioModule.getLinkedTaxonomies().stream()
			.forEach(taxonomy -> {
				CompetenceBrowserTableRow taxonomyRow = new CompetenceBrowserTableRow(null, taxonomy, null);
				rows.add(taxonomyRow);
				
				taxonomyService.getTaxonomyLevels(taxonomy).stream().forEach(level -> {
					CompetenceBrowserTableRow levelRow = new CompetenceBrowserTableRow(null, taxonomy, level);
					rows.add(levelRow);
				});
		});	
		
		// Set parents
		// Root levels to taxonomy
		rows.stream().filter(row -> row.getTaxonomyLevel() != null && row.getTaxonomyLevel().getParent() == null)
					 .forEach(level -> level.setParent(rows.stream().filter(parent -> parent.getTaxonomy() != null && parent.getTaxonomy().getKey().equals(level.getTaxonomy().getKey()))
							 										.findFirst().orElse(null)));
		
		// Levels to level
		rows.stream().filter(row -> row.getTaxonomyLevel() != null && row.getTaxonomyLevel().getParent() != null)
				 	 .forEach(level -> level.setParent(rows.stream().filter(parent -> parent.getTaxonomyLevel() != null && parent.getTaxonomyLevel().getKey().equals(level.getTaxonomyLevel().getParent().getKey()))
					 												.findFirst().orElse(null)));	
		// Sort rows
		rows.sort(new FlexiTreeNodeComparator());
		
		tableModel.setObjects(rows);
		tableEl.setRootCrumb(rootCrumb);
		tableEl.reset(true, true, true);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl) {
			if (event instanceof FlexiTableSearchEvent) {
				if (event.getCommand().equals(FlexiTableSearchEvent.RESET.getCommand())) {
					tableModel.filter(null, null);
				} else {
					tableModel.filter(tableEl.getQuickSearchString(), null);
				}
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// Nothing to save here
	}

	@Override
	protected void doDispose() {
		// Nothing to dispose here
	}

}
