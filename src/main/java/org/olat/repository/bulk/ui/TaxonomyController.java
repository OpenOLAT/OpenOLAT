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
package org.olat.repository.bulk.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.modules.taxonomy.ui.component.TaxonomyLevelSelection;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryModule;
import org.olat.repository.RepositoryService;
import org.olat.repository.bulk.SettingsBulkEditable;
import org.olat.repository.bulk.model.SettingsContext;
import org.olat.repository.bulk.ui.TaxonomyController.BulkTaxonomyRow.BulkTaxonomyStatus;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 Oct 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyController extends StepFormBasicController {
	
	private static final String CMD_USAGE = "usage";
	private static final String CMD_APPLY = "apply";
	private static final String CMD_REMOVE = "remove";
	
	private TaxonomyLevelSelection taxonomyLevelEl;
	private FormLink taxonomyLevelAddLink;
	private FlexiTableElement tableEl;
	private BulkTaxonomyDataModel dataModel;
	
	private CloseableCalloutWindowController calloutCtrl;
	
	private final SettingsContext context;
	private final Set<TaxonomyLevel> taxonomyLevels;
	private final Map<TaxonomyLevel, Set<RepositoryEntry>> levelToEntry;
	private final Set<Long> taxonomyLevelCurrentKeys;
	private final Set<Long> taxonomyLevelAddKeys;
	private final Set<Long> taxonomyLevelRemoveKeys;
	private int counter = 0;
	
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private CatalogV2Module catalogModule;

	public TaxonomyController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, null);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		this.context = (SettingsContext)runContext.get(SettingsContext.DEFAULT_KEY);
		
		// Current taxonomy levels
		Map<RepositoryEntryRef, List<TaxonomyLevel>> entryRefToTaxonomyLevel = repositoryService.getTaxonomy(context.getRepositoryEntries(), false);
		this.taxonomyLevels = entryRefToTaxonomyLevel.values().stream().flatMap(List::stream).collect(Collectors.toSet());
		
		List<RepositoryEntryToTaxonomyLevel> entryToLevel = new ArrayList<>();
		for (Entry<RepositoryEntryRef, List<TaxonomyLevel>> entryToLevels : entryRefToTaxonomyLevel.entrySet()) {
			Long entryKey = entryToLevels.getKey().getKey();
			RepositoryEntry entry = context.getRepositoryEntries().stream().filter(re -> entryKey.equals(re.getKey())).findFirst().get();
			for (TaxonomyLevel taxonomyLevel : entryToLevels.getValue()) {
				entryToLevel.add(new RepositoryEntryToTaxonomyLevel(entry, taxonomyLevel));
			}
		}
		this.levelToEntry = entryToLevel.stream()
				.collect(Collectors.groupingBy(
						RepositoryEntryToTaxonomyLevel::getTaxonomyLevel, 
						Collectors.mapping(RepositoryEntryToTaxonomyLevel::getRepositoryEntry, Collectors.toSet())));
		
		// Taxonomy level keys
		this.taxonomyLevelCurrentKeys = entryRefToTaxonomyLevel.values().stream().flatMap(List::stream).map(TaxonomyLevel::getKey).collect(Collectors.toSet());
		
		this.taxonomyLevelAddKeys = context.getTaxonomyLevelAddKeys() != null
				? new HashSet<>(context.getTaxonomyLevelAddKeys())
				: new HashSet<>(1);
		this.taxonomyLevels.addAll(taxonomyService.getTaxonomyLevelsByKeys(taxonomyLevelAddKeys));
		
		this.taxonomyLevelRemoveKeys = context.getTaxonomyLevelRemoveKeys() != null && !context.getTaxonomyLevelRemoveKeys().isEmpty()
				? taxonomyLevels.stream().filter(level -> context.getTaxonomyLevelRemoveKeys().contains(level.getKey())).map(TaxonomyLevel::getKey).collect(Collectors.toSet())
				: new HashSet<>(1);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("settings.bulk.taxonomy.title");
		setFormInfo("noTransOnlyParam",
				new String[] {RepositoryBulkUIFactory.getSettingsDescription(getTranslator(), context.getRepositoryEntries(), "settings.bulk.taxonomy.desc")});
		
		FormLayoutContainer addCont = FormLayoutContainer.createDefaultFormLayout("addCont", getTranslator());
		addCont.setRootForm(mainForm);
		formLayout.add(addCont);
		
		Set<TaxonomyLevel> allTaxonomieLevels = new HashSet<>(taxonomyService.getTaxonomyLevels(repositoryModule.getTaxonomyRefs()));
		String labelI18nKey = catalogModule.isEnabled()? "settings.bulk.taxonomy.title.catalog": "settings.bulk.taxonomy.title";
		taxonomyLevelEl = uifactory.addTaxonomyLevelSelection("taxonomyLevel", labelI18nKey, addCont,
				getWindowControl(), allTaxonomieLevels);
		taxonomyLevelEl.setDisplayNameHeader(translate(labelI18nKey));
		
		FormLayoutContainer addButtonCont = FormLayoutContainer.createButtonLayout("addButtonCont", getTranslator());
		addButtonCont.setRootForm(mainForm);
		addCont.add(addButtonCont);
		
		taxonomyLevelAddLink = uifactory.addFormLink("settings.bulk.taxonomy.add", addButtonCont, Link.BUTTON);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BulkTaxonomyCols.status, new BulkTaxonomyStatusRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BulkTaxonomyCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BulkTaxonomyCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BulkTaxonomyCols.usage));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BulkTaxonomyCols.applyAll));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BulkTaxonomyCols.remove));
		
		dataModel = new BulkTaxonomyDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
	}
	
	private void loadModel() {
		List<BulkTaxonomyRow> rows = new ArrayList<>(taxonomyLevels.size());
		for (TaxonomyLevel taxonomyLevel : taxonomyLevels) {
			rows.add(createRow(taxonomyLevel));
		}
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	private BulkTaxonomyRow createRow(TaxonomyLevel taxonomyLevel) {
		BulkTaxonomyRow row = new BulkTaxonomyRow();
		row.setKey(taxonomyLevel.getKey());
		
		BulkTaxonomyStatus status = null;
		if (taxonomyLevelAddKeys.contains(taxonomyLevel.getKey()) && !taxonomyLevelCurrentKeys.contains(taxonomyLevel.getKey())) {
			status = BulkTaxonomyStatus.add;
		} else if (taxonomyLevelRemoveKeys.contains(taxonomyLevel.getKey())) {
			status = BulkTaxonomyStatus.remove;
		}
		row.setStatus(status);
		
		row.setName(TaxonomyUIFactory.translateDisplayName(getTranslator(), taxonomyLevel));
		row.setIdentifier(taxonomyLevel.getIdentifier());
		
		Set<RepositoryEntry> usageEntries = levelToEntry.getOrDefault(taxonomyLevel, Collections.emptySet());
		row.setUsageEntries(usageEntries);
		
		String usageLinkName = "usage-" + counter++;
		FormLink usageLink = uifactory.addFormLink(usageLinkName, CMD_USAGE, "", null, null, Link.LINK | Link.NONTRANSLATED);
		String usageText = translate("settings.bulk.taxonomy.usage.num", new String[] {String.valueOf(usageEntries.size())});
		usageLink.setI18nKey(usageText);
		usageLink.setEnabled(!usageEntries.isEmpty());
		usageLink.setUserObject(row);
		row.setUsageItem(usageLink);
		
		if (row.getStatus() == null) {
			String applyLinkName = "apply-" + counter++;
			FormLink applyLink = uifactory.addFormLink(applyLinkName, CMD_APPLY, "", null, null, Link.LINK | Link.NONTRANSLATED);
			String applyText = taxonomyLevelAddKeys.contains(row.getKey())? translate("settings.bulk.taxonomy.undo"): translate("settings.bulk.taxonomy.apply.all");
			applyLink.setI18nKey(applyText);
			applyLink.setUserObject(row);
			row.setApplyAllItem(applyLink);
		}
		
		if (!taxonomyLevelAddKeys.contains(row.getKey())) {
			String removeLinkName = "remove-" + counter++;
			FormLink removeLink = uifactory.addFormLink(removeLinkName, CMD_REMOVE, "", null, null, Link.LINK | Link.NONTRANSLATED);
			String removeText = BulkTaxonomyStatus.remove == row.getStatus()? translate("settings.bulk.taxonomy.undo"): translate("settings.bulk.taxonomy.remove");
			removeLink.setI18nKey(removeText);
			removeLink.setUserObject(row);
			row.setRemoveItem(removeLink);
		}
		
		return row;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == taxonomyLevelAddLink) {
			doAddTaxonomyLevels();
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if (CMD_USAGE.equals(cmd) && link.getUserObject() instanceof BulkTaxonomyRow) {
				BulkTaxonomyRow row = (BulkTaxonomyRow)link.getUserObject();
				doOpenUsage(ureq, row.getUsageEntries(), link);
			} else if (CMD_APPLY.equals(cmd) && link.getUserObject() instanceof BulkTaxonomyRow) {
				BulkTaxonomyRow row = (BulkTaxonomyRow)link.getUserObject();
				doApplyAll(row.getKey());
			} else if (CMD_REMOVE.equals(cmd) && link.getUserObject() instanceof BulkTaxonomyRow) {
				BulkTaxonomyRow row = (BulkTaxonomyRow)link.getUserObject();
				doRemove(row.getKey());
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	private void doAddTaxonomyLevels() {
		Set<TaxonomyLevelRef> selection = taxonomyLevelEl.getSelection();
		if (selection.isEmpty()) return;
		
		List<TaxonomyLevel> taxonomyLevelsToAdd = taxonomyService.getTaxonomyLevelsByRefs(selection);
		for (TaxonomyLevel taxonomyLevel : taxonomyLevelsToAdd) {
			if (!taxonomyLevelCurrentKeys.contains(taxonomyLevel.getKey())) {
				taxonomyLevels.add(taxonomyLevel);
				taxonomyLevelAddKeys.add(taxonomyLevel.getKey());
			}
		}
		
		taxonomyLevelEl.setSelection(null);
		loadModel();
	}

	private void doOpenUsage(UserRequest ureq, Set<RepositoryEntry> usageEntries, FormLink link) {
		removeAsListenerAndDispose(calloutCtrl);
		
		VelocityContainer usageVC = createVelocityContainer("repository_entry_name");
		
		List<String> entryNames = usageEntries.stream().map(RepositoryEntry::getDisplayname).sorted().collect(Collectors.toList());
		usageVC.contextPut("entryNames", entryNames);
		
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), usageVC, link.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doApplyAll(Long taxonomyLevelKey) {
		if (taxonomyLevelAddKeys.contains(taxonomyLevelKey)) {
			taxonomyLevelAddKeys.remove(taxonomyLevelKey);
		} else {
			taxonomyLevelAddKeys.add(taxonomyLevelKey);
		}
		loadModel();
	}
	
	private void doRemove(Long taxonomyLevelKey) {
		if (taxonomyLevelRemoveKeys.contains(taxonomyLevelKey)) {
			taxonomyLevelRemoveKeys.remove(taxonomyLevelKey);
		} else {
			taxonomyLevelRemoveKeys.add(taxonomyLevelKey);
		}
		loadModel();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		context.select(SettingsBulkEditable.taxonomyLevelsAdd, !taxonomyLevelAddKeys.isEmpty());
		context.setTaxonomyLevelAddKeys(Set.copyOf(taxonomyLevelAddKeys));
		context.select(SettingsBulkEditable.taxonomyLevelsRemove, !taxonomyLevelRemoveKeys.isEmpty());
		context.setTaxonomyLevelRemoveKeys(Set.copyOf(taxonomyLevelRemoveKeys));
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	
	public static final class BulkTaxonomyDataModel extends DefaultFlexiTableDataModel<BulkTaxonomyRow>
	implements SortableFlexiTableDataModel<BulkTaxonomyRow> {
		
		private static final BulkTaxonomyCols[] COLS = BulkTaxonomyCols.values();
		private final Locale locale;
		
		public BulkTaxonomyDataModel(FlexiTableColumnModel columnsModel, Locale locale) {
			super(columnsModel);
			this.locale = locale;
		}

		@Override
		public void sort(SortKey orderBy) {
			List<BulkTaxonomyRow> rows = new SortableFlexiTableModelDelegate<>(orderBy, this, locale).sort();
			super.setObjects(rows);
		}

		@Override
		public Object getValueAt(int row, int col) {
			BulkTaxonomyRow reason = getObject(row);
			return getValueAt(reason, col);
		}

		@Override
		public Object getValueAt(BulkTaxonomyRow row, int col) {
			switch(COLS[col]) {
				case status: return row.getStatus();
				case name: return row.getName();
				case identifier: return row.getIdentifier();
				case usage: return row.getUsageItem();
				case applyAll: return row.getApplyAllItem();
				case remove: return row.getRemoveItem();
				default: return null;
			}
		}
	}
	
	public static enum BulkTaxonomyCols implements FlexiSortableColumnDef {
		status("settings.bulk.taxonomy.status"),
		name("settings.bulk.taxonomy.name"),
		identifier("settings.bulk.taxonomy.identifier"),
		usage("settings.bulk.taxonomy.usage"),
		applyAll("settings.bulk.taxonomy.apply.all"),
		remove("settings.bulk.taxonomy.remove");
		
		private final String i18nKey;
		
		private BulkTaxonomyCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return true;
		}

		@Override
		public String sortKey() {
			return name();
		}
	}
	
	public static final class BulkTaxonomyRow {
		
		public enum BulkTaxonomyStatus { add, remove }
		
		private Long key;
		private BulkTaxonomyStatus status;
		private String name;
		private String identifier;
		private Set<RepositoryEntry> usageEntries;
		private FormItem usageItem;
		private FormItem applyAllItem;
		private FormItem removeItem;
		
		public Long getKey() {
			return key;
		}

		public void setKey(Long key) {
			this.key = key;
		}

		public BulkTaxonomyStatus getStatus() {
			return status;
		}
		
		public void setStatus(BulkTaxonomyStatus status) {
			this.status = status;
		}
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public String getIdentifier() {
			return identifier;
		}
		
		public void setIdentifier(String identifier) {
			this.identifier = identifier;
		}
		
		public Set<RepositoryEntry> getUsageEntries() {
			return usageEntries;
		}

		public void setUsageEntries(Set<RepositoryEntry> usageEntries) {
			this.usageEntries = usageEntries;
		}

		public FormItem getUsageItem() {
			return usageItem;
		}
		
		public void setUsageItem(FormItem usageItem) {
			this.usageItem = usageItem;
		}
		
		public FormItem getApplyAllItem() {
			return applyAllItem;
		}

		public void setApplyAllItem(FormItem applyAllItem) {
			this.applyAllItem = applyAllItem;
		}

		public FormItem getRemoveItem() {
			return removeItem;
		}
		
		public void setRemoveItem(FormItem removeItem) {
			this.removeItem = removeItem;
		}
		
	}
	
	public static final class RepositoryEntryToTaxonomyLevel {
		
		private final RepositoryEntry repositoryEntry;
		private final TaxonomyLevel taxonomyLevel;
		
		public RepositoryEntryToTaxonomyLevel(RepositoryEntry repositoryEntry, TaxonomyLevel taxonomyLevel) {
			this.repositoryEntry = repositoryEntry;
			this.taxonomyLevel = taxonomyLevel;
		}

		public RepositoryEntry getRepositoryEntry() {
			return repositoryEntry;
		}

		public TaxonomyLevel getTaxonomyLevel() {
			return taxonomyLevel;
		}
		
	}

}
