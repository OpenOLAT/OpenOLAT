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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.chiefcontrollers.BaseChiefController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.ObjectSelectionBrowserEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeNodeComparator;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.ui.CompetenceBrowserTableModel.CompetenceBrowserCols;
import org.olat.modules.taxonomy.ui.component.TaxonomyRenderer;

/**
 * Initial date: 26.03.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CompetenceBrowserController extends FormBasicController {

	private static final String OPEN_INFO = "open_info";
	private static final String TAB_ID_ALL = "All";
	
	private CompetenceBrowserTableModel tableModel;
	private CompetenceBrowserTableRow rootCrumb;
	private FlexiTableElement tableEl;
	
	private DetailsController detailsCtrl;
	private CloseableCalloutWindowController ccmc;
	
	private final List<Taxonomy> taxonomies;
	private final Map<Taxonomy, List<TaxonomyLevel>> taxonomyToLevels;
	private final boolean withSelection;
	private final boolean multiSelection;
	private final String displayNameHeader;
	private final Set<Long> preselectedLevelKeys;

	public CompetenceBrowserController(UserRequest ureq, WindowControl wControl, List<Taxonomy> taxonomies,
			Collection<TaxonomyLevel> taxonomyLevels, boolean withSelection, boolean multiSelection, String displayNameHeader,
			Collection<Long> preselectedLevelKeys) {
		super(ureq, wControl, "competence_browse",
				Util.createPackageTranslator(BaseChiefController.class, ureq.getLocale()));
		this.taxonomies = taxonomies;
		this.taxonomyToLevels = taxonomyLevels.stream().collect(Collectors.groupingBy(TaxonomyLevel::getTaxonomy));
		this.withSelection = withSelection;
		this.multiSelection = multiSelection;
		this.displayNameHeader = displayNameHeader;
		this.preselectedLevelKeys = preselectedLevelKeys != null ? Set.copyOf(preselectedLevelKeys) : Set.of();
		
		initForm(ureq);
		loadModel();
		openFirstLevels();
		updateMultiSelectedIndex();
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
		if (taxonomies != null && taxonomies.size() > 1) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CompetenceBrowserCols.taxonomy, new TaxonomyRenderer()));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CompetenceBrowserCols.details));
		
		// Create table model
		tableModel = new CompetenceBrowserTableModel(columnsModel, multiSelection);

		// Add table model to formlayout
		tableEl = uifactory.addTableElement(getWindowControl(), "browser_table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setSearchEnabled(true);
		tableEl.setPageSize(20);
		if (taxonomies != null && taxonomies.size() > 1) {
			initFilterTabs(ureq);
		}
		if (withSelection) {
			tableEl.setSelection(true, multiSelection, false);
			FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
			formLayout.add(buttonsCont);
			uifactory.addFormSubmitButton("add", buttonsCont);
			uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		}
		
		// Create a fake root crumb
		rootCrumb = new CompetenceBrowserTableRow(displayNameHeader);
	}
	
	private void loadModel() {
		List<CompetenceBrowserTableRow> rows = new ArrayList<>();
		
		if (taxonomies != null) {
			for (Taxonomy taxonomy : taxonomies) {
				List<TaxonomyLevel> taxonomyLevels = taxonomyToLevels.getOrDefault(taxonomy, Collections.emptyList());
				for (TaxonomyLevel level : taxonomyLevels) {
					String displayName = TaxonomyUIFactory.translateDisplayName(getTranslator(), level);
					String description = TaxonomyUIFactory.translateDescription(getTranslator(), level);
					CompetenceBrowserTableRow levelRow = new CompetenceBrowserTableRow(taxonomy, level, displayName, description);
					boolean isPreselected = preselectedLevelKeys.contains(level.getKey());
					levelRow.setPreselected(isPreselected);
					levelRow.setSelected(isPreselected);
					if (StringHelper.containsNonWhitespace(levelRow.getDescription())) {
						FormLink levelDetailsLink = uifactory.addFormLink("det_" + levelRow.getKey(), OPEN_INFO, "competences.details.link", tableEl, Link.LINK);
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
			rows.stream()
				.filter(row -> row.getTaxonomyLevel() != null && row.getTaxonomyLevel().getParent() != null)
				.forEach(level -> level.setParent(
						rows.stream().filter(parent ->
							parent.getTaxonomyLevel() != null && parent.getTaxonomyLevel().getKey().equals(level.getTaxonomyLevel().getParent().getKey()))
						.findFirst().orElse(null)
				));	
			// Sort rows
			rows.sort(new TaxonomyTreeNodeComparator());
		}
		
		tableModel.setObjects(rows);
		tableEl.setRootCrumb(rootCrumb);
		tableEl.reset(true, true, true);
	}

	private void openFirstLevels() {
		tableModel.closeAll();
		for (int rowIndex = 0; rowIndex < tableModel.getRowCount(); rowIndex ++) {
			CompetenceBrowserTableRow row = tableModel.getObject(rowIndex);
			boolean open = row.getParent() == null;
			if (open) {
				tableModel.open(rowIndex);
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(ccmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(detailsCtrl);
		removeAsListenerAndDispose(ccmc);
		detailsCtrl = null;
		ccmc = null;
	}

	private void initFilterTabs(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		FlexiFiltersTab allTab = FlexiFiltersTabFactory.tabWithImplicitFilters(
				TAB_ID_ALL, translate("all"),
				TabSelectionBehavior.nothing, List.of());
		tabs.add(allTab);

		Collator collator = Collator.getInstance(getLocale());
		taxonomies.stream()
				.sorted(Comparator.comparing(Taxonomy::getDisplayName, Comparator.nullsLast(collator)))
				.forEach(tax -> tabs.add(FlexiFiltersTabFactory.tabWithImplicitFilters(
						tax.getKey().toString(), tax.getDisplayName(),
						TabSelectionBehavior.nothing, List.of())));

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, allTab);
	}

	private void filterModel() {
		FlexiFiltersTab selectedTab = tableEl.getSelectedFilterTab();
		String taxonomyKey = (selectedTab == null || TAB_ID_ALL.equals(selectedTab.getId()))
				? null : selectedTab.getId();
		tableModel.setTaxonomyFilter(taxonomyKey);
		tableModel.filter(tableEl.getQuickSearchString(), null);
		tableEl.reset(true, true, true);
		updateMultiSelectedIndex();
	}

	private void updateMultiSelectedIndex() {
		Set<Integer> indexes = new HashSet<>();
		for (int i = 0; i < tableModel.getRowCount(); i++) {
			CompetenceBrowserTableRow row = tableModel.getObject(i);
			if (row != null && row.isSelected()) {
				indexes.add(Integer.valueOf(i));
			}
		}
		tableEl.setMultiSelectedIndex(indexes);
	}

	private void doSelectionUpdate(int index, boolean checked) {
		CompetenceBrowserTableRow row = tableModel.getObject(index);
		if (row == null) {
			return;
		}
		if (multiSelection) {
			if (!row.isPreselected()) {
				row.setSelected(checked);
				updateMultiSelectedIndex();
			}
		} else {
			if (checked) {
				tableModel.clearAllSelections();
			}
			row.setSelected(checked);
			updateMultiSelectedIndex();
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl) {
			if (event instanceof SelectionEvent se) {
				if (FlexiTableElement.ROW_CHECKED_EVENT.equals(se.getCommand())) {
					doSelectionUpdate(se.getIndex(), true);
				} else if (FlexiTableElement.ROW_UNCHECKED_EVENT.equals(se.getCommand())) {
					doSelectionUpdate(se.getIndex(), false);
				}
			} else if (event instanceof FlexiTableSearchEvent) {
				filterModel();
			} else if (event instanceof FlexiTableFilterTabEvent) {
				filterModel();
			}
		} else if (source instanceof FormLink sFl) {
			if (sFl.getCmd().equals(OPEN_INFO)) {
				doOpen(ureq, sFl, (CompetenceBrowserTableRow) sFl.getUserObject());	
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Collection<String> keys = getSelectedTaxonomyLevelKeys();
		fireEvent(ureq, new ObjectSelectionBrowserEvent(keys));
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doOpen(UserRequest ureq, FormLink sFl, CompetenceBrowserTableRow row) {
		detailsCtrl = new DetailsController(ureq, getWindowControl(), row);
		listenTo(detailsCtrl);
		
		ccmc = new CloseableCalloutWindowController(ureq, getWindowControl(), detailsCtrl.getInitialComponent(), sFl, null, true, null);
		listenTo(ccmc);
		ccmc.activate();
	}
	
	private Collection<String> getSelectedTaxonomyLevelKeys() {
		return tableModel.getSelectedTreeNodes().stream()
				.map(CompetenceBrowserTableRow::getTaxonomyLevel)
				.filter(Objects::nonNull)
				.map(level -> level.getKey().toString())
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
	
	private static class DetailsController extends BasicController {
		
		public DetailsController(UserRequest ureq, WindowControl wControl, CompetenceBrowserTableRow row) {
			super(ureq, wControl);
			
			String title = row.getDisplayName();
			String description = row.getDescription();
			
			VelocityContainer taxonomyDetails = createVelocityContainer("taxonomy_details");
			
			if (StringHelper.containsNonWhitespace(title)) {
				taxonomyDetails.contextPut("title", title);
			}
			if (StringHelper.containsNonWhitespace(description)) {
				taxonomyDetails.contextPut("description", description);
			}
			putInitialPanel(taxonomyDetails);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			//
		}
	}
	
	public static class TaxonomyTreeNodeComparator extends FlexiTreeNodeComparator {
		
		@Override
		protected int compareNodes(FlexiTreeTableNode o1, FlexiTreeTableNode o2) {
			CompetenceBrowserTableRow r1 = (CompetenceBrowserTableRow)o1;
			CompetenceBrowserTableRow r2 = (CompetenceBrowserTableRow)o2;

			int c = 0;
			if(r1 == null || r2 == null) {
				c = compareNullObjects(r1, r2);
			} else {
				Integer s1 = r1.getSortOrder();
				Integer s2 = r2.getSortOrder();
	
				if(s1 == null || s2 == null) {
					c = -compareNullObjects(s1, s2);
				} else {
					c = s1.compareTo(s2);
				}
				
				if(c == 0) {
					String c1 = r1.getDisplayName();
					String c2 = r2.getDisplayName();
					if(c1 == null || c2 == null) {
						c = -compareNullObjects(c1, s2);
					} else {
						c = c1.compareTo(c2);
					}
				}
			}
			return c;
		}
	}
}
