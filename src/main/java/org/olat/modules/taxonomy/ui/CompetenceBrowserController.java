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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeNodeComparator;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.util.StringHelper;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.ui.CompetenceBrowserTableModel.CompetenceBrowserCols;

/**
 * Initial date: 26.03.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CompetenceBrowserController extends FormBasicController {

	private static final String OPEN_INFO = "open_info";
	
	private FormLink selectButton;
	private CompetenceBrowserTableModel tableModel;
	private CompetenceBrowserTableRow rootCrumb;
	private FlexiTableElement tableEl;
	
	private CloseableCalloutWindowController ccmc;
	
	private final List<Taxonomy> taxonomies;
	private final Map<Taxonomy, List<TaxonomyLevel>> taxonomyToLevels;
	private final boolean withSelection;
	private final String displayNameHeader;


	public CompetenceBrowserController(UserRequest ureq, WindowControl wControl, List<Taxonomy> taxonomies,
			Collection<TaxonomyLevel> taxonomyLevels, boolean withSelection, String displayNameHeader) {
		super(ureq, wControl, "competence_browse");
		this.taxonomies = taxonomies;
		this.taxonomyToLevels = taxonomyLevels.stream().collect(Collectors.groupingBy(TaxonomyLevel::getTaxonomy));
		this.withSelection = withSelection;
		this.displayNameHeader = displayNameHeader;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// Create columns model
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CompetenceBrowserCols.key));
		DefaultFlexiColumnModel displayNameModel = new DefaultFlexiColumnModel(CompetenceBrowserCols.competences, new TreeNodeFlexiCellRenderer());
		if (StringHelper.containsNonWhitespace(displayNameHeader)) {
			displayNameModel.setHeaderLabel(displayNameHeader);
		}
		columnsModel.addFlexiColumnModel(displayNameModel);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CompetenceBrowserCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CompetenceBrowserCols.externalId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CompetenceBrowserCols.details));
		
		// Create table model
		tableModel = new CompetenceBrowserTableModel(columnsModel);
		
		// Add table model to formlayout
		tableEl = uifactory.addTableElement(getWindowControl(), "browser_table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setSearchEnabled(true);
		tableEl.setPageSize(20);
		if (withSelection) {
			tableEl.setMultiSelect(true);
			selectButton = uifactory.addFormLink("select", formLayout, Link.BUTTON);
			tableEl.addBatchButton(selectButton);
		}
		
		// Create a fake root crumb
		rootCrumb = new CompetenceBrowserTableRow(displayNameHeader);
	}
	
	private void loadModel() {
		List<CompetenceBrowserTableRow> rows = new ArrayList<>();
		
		int linkCounter = 0;
		
		if (taxonomies != null) {
			for (Taxonomy taxonomy : taxonomies) {
				CompetenceBrowserTableRow taxonomyRow = new CompetenceBrowserTableRow(taxonomy);
				if (StringHelper.containsNonWhitespace(taxonomyRow.getDescription())) {
					FormLink taxonomyDetailsLink = uifactory.addFormLink(linkCounter++ + "_" + taxonomyRow.getKey().toString(), OPEN_INFO, "competences.details.link", tableEl, Link.LINK);
					taxonomyDetailsLink.setIconLeftCSS("o_icon o_icon_fw o_icon_description");
					taxonomyDetailsLink.setUserObject(taxonomyRow);
					taxonomyRow.setDetailsLink(taxonomyDetailsLink);
				}
				rows.add(taxonomyRow);
				
				List<TaxonomyLevel> taxonomyLevels = taxonomyToLevels.getOrDefault(taxonomy, Collections.emptyList());
				for (TaxonomyLevel level : taxonomyLevels) {
					String displayName = TaxonomyUIFactory.translateDisplayName(getTranslator(), level);
					String description = TaxonomyUIFactory.translateDescription(getTranslator(), level);
					CompetenceBrowserTableRow levelRow = new CompetenceBrowserTableRow(taxonomy, level, displayName, description);
					if (StringHelper.containsNonWhitespace(levelRow.getDescription())) {
						FormLink levelDetailsLink = uifactory.addFormLink(linkCounter++ + "_" + levelRow.getKey().toString(), OPEN_INFO, "competences.details.link", tableEl, Link.LINK);
						levelDetailsLink.setIconLeftCSS("o_icon o_icon_fw o_icon_description");
						levelDetailsLink.setUserObject(levelRow);
						levelRow.setDetailsLink(levelDetailsLink);
					}
					rows.add(levelRow);
				}
			}
		}
				
		if (!rows.isEmpty()) {
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
		}
		
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
					tableEl.reset();
				} else {
					tableModel.filter(tableEl.getQuickSearchString(), null);
				}
			}
		} else if (selectButton == source) {
			List<TaxonomyLevel> taxonomyLevels = getSelectedTaxonomyLevels();
			fireEvent(ureq, new TaxonomyLevelSelectionEvent(taxonomyLevels));
		} else if (source instanceof FormLink) {
			FormLink sFl = (FormLink) source;
			if (sFl.getCmd().equals(OPEN_INFO)) {
				CompetenceBrowserTableRow row = (CompetenceBrowserTableRow) sFl.getUserObject();
				
				String title = row.getDisplayName();
				String description = row.getDescription();
				
				VelocityContainer taxonomyDetails = createVelocityContainer("taxonomy_details");
				
				if (StringHelper.containsNonWhitespace(title)) {
					taxonomyDetails.contextPut("title", title);
				}
				if (StringHelper.containsNonWhitespace(description)) {
					taxonomyDetails.contextPut("description", description);
				}
				
				ccmc = new CloseableCalloutWindowController(ureq, getWindowControl(), taxonomyDetails, sFl, null, true, null);
				ccmc.activate();
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// Nothing to save here
	}
	
	private List<TaxonomyLevel> getSelectedTaxonomyLevels() {
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		return selectedIndex.stream()
				.map(index -> tableModel.getObject(index.intValue()).getTaxonomyLevel())
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
	}
	
	public static final class TaxonomyLevelSelectionEvent extends Event {
		
		private static final long serialVersionUID = 8214549556291099273L;
		
		private final List<TaxonomyLevel> taxonomyLevels;
		
		public TaxonomyLevelSelectionEvent(List<TaxonomyLevel> taxonomyLevels) {
			super("tax-level-.selection");
			this.taxonomyLevels = taxonomyLevels;
		}
		
		public List<TaxonomyLevel> getTaxonomyLevels() {
			return taxonomyLevels;
		}
		
	}
	
}
