/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 
package org.olat.core.gui.components.form.flexible.impl.elements.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.choice.Choice;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.AutoCompleter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSort;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableStateEntry;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.AbstractTextElement;
import org.olat.core.gui.components.form.flexible.impl.elements.AutoCompleteEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.AutoCompleterImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.TextElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.ChangeFilterEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.DeleteCurrentPresetEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.ExpandFiltersEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FiltersAndSettingsEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiFiltersAndSettingsDialogController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiFiltersElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.SaveCurrentPresetEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.UpdateCurrentPresetEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFilterTabsElementImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.RemoveFiltersEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.SelectFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListProvider;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.winmgr.JSCommand;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.resource.OresHelper;


/**
 * 
 * @author Christian Guretzki
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class FlexiTableElementImpl extends FormItemImpl implements FlexiTableElement,
	ControllerEventListener, ComponentEventListener, Disposable {
	
	//settings
	private SelectionMode multiSelect = SelectionMode.disabled;
	private boolean rowSelection = false;
	private FlexiTableRendererType rendererType = FlexiTableRendererType.classic;
	private FlexiTableRendererType[] availableRendererType = new FlexiTableRendererType[] {
		FlexiTableRendererType.classic
	};
	
	private String persistentId;
	private boolean customizeColumns = true;
	
	private int rowCount = -1;
	
	private int currentPage;
	private int pageSize;
	private final int defaultPageSize;
	private boolean footer;
	private boolean bordered; 
	private boolean editMode;
	private boolean exportEnabled;
	private boolean searchEnabled;
	private boolean searchLarge;
	private boolean selectAllEnabled;
	private boolean numOfRowsEnabled = true;
	private boolean showAllRowsEnabled = false;
	private boolean extendedSearchExpanded = false;
	private boolean hasAlwaysVisibleColumns = false;
	private int columnLabelForDragAndDrop;
	
	private String emptyTableMessageKey;
	private String emptyTableHintKey;		
	private String emptyTableIconCss;	
	private FormLink emptyTablePrimaryActionButton;
	private boolean emptyShowSearch = true;
	
	private VelocityContainer rowRenderer;
	private VelocityContainer detailsRenderer;

	private FormLink customButton;
	private FormLink exportButton;
	private FormLink searchButton;
	private FormLink extendedSearchButton;
	private FormLink classicTypeButton;
	private FormLink customTypeButton;
	private FormLink externalTypeButton;
	private FormLink settingsButton;
	private AbstractTextElement searchFieldEl;
	private ExtendedFlexiTableSearchController extendedSearchCtrl;
	
	private FlexiFiltersElementImpl filtersEl;
	private FlexiFilterTabsElementImpl filterTabsEl;
	
	private final FlexiTableDataModel<?> dataModel;
	private final FlexiTableDataSource<?> dataSource;
	private final FlexiTableComponent component;
	private FlexiTableComponentDelegate componentDelegate;
	private CloseableCalloutWindowController callout;
	private CloseableModalController cmc;
	private final WindowControl wControl;
	private FlexiFiltersAndSettingsDialogController settingsCtrl;

	private FormItem zeroRowItem;
	private String wrapperSelector;
	private FlexiTableCssDelegate cssDelegate;

	private SortKey[] orderBy;
	private FlexiTableSortOptions sortOptions;
	private boolean sortEnabled = true;
	private List<FlexiTableFilter> filters;
	private boolean multiFilterSelection = false;
	private boolean allSelectedNeedLoadOfWholeModel = false;
	private Map<Integer,Object> multiSelectedIndex;
	private boolean multiDetails = false;
	private Set<Integer> detailsIndex;
	private Set<Integer> enabledColumnIndex = new HashSet<>();
	
	private FlexiTreeTableNode rootCrumb;
	private List<FlexiTreeTableNode> crumbs;
	private Map<String,FormItem> components = new HashMap<>();
	private List<FormItem> batchButtons = new ArrayList<>();
	
	public FlexiTableElementImpl(WindowControl wControl, String name, Translator translator, FlexiTableDataModel<?> tableModel) {
		this(wControl, name, translator, tableModel, -1, true);
	}
	
	public FlexiTableElementImpl(WindowControl wControl, String name, Translator translator,
			FlexiTableDataModel<?> tableModel, int pageSize, boolean loadOnStart) {
		super(name);
		this.wControl = wControl;
		this.dataModel = tableModel;
		this.dataSource = (tableModel instanceof FlexiTableDataSource) ? (FlexiTableDataSource<?>)dataModel : null;
		translator = Util.createPackageTranslator(FlexiTableElementImpl.class, translator.getLocale(), translator);
		setTranslator(translator);
		component = new FlexiTableComponent(this, translator);
		
		for(int i=dataModel.getTableColumnModel().getColumnCount(); i-->0; ) {
			FlexiColumnModel col = dataModel.getTableColumnModel().getColumnModel(i);
			if(col.isDefaultVisible()) {
				enabledColumnIndex.add(Integer.valueOf(col.getColumnIndex()));
			}
			
			if(hasAlwaysVisibleColumns || col.isAlwaysVisible()) {
				hasAlwaysVisibleColumns = true;
			}
		}
		
		String dispatchId = component.getDispatchID();
		customButton = new FormLinkImpl(dispatchId.concat("_customButton"), "rCustomButton", "", Link.BUTTON + Link.NONTRANSLATED);
		customButton.setTranslator(translator);
		customButton.setIconLeftCSS("o_icon o_icon_customize");
		customButton.setAriaLabel(translator.translate("aria.customize"));
		components.put("rCustomize", customButton);
		
		settingsButton = new FormLinkImpl(dispatchId.concat("_settingsButton"), "rSetttingsButton", "", Link.BUTTON + Link.NONTRANSLATED);
		settingsButton.setTranslator(translator);
		settingsButton.setIconLeftCSS("o_icon o_icon_actions");
		settingsButton.setAriaLabel(translator.translate("aria.settings"));
		components.put("rSettings", settingsButton);
		
		this.pageSize = pageSize;
		this.defaultPageSize = pageSize;
		if(pageSize > 0) {
			setPage(0);
		}
		
		if(dataSource != null && loadOnStart) {
			//preload it
			dataSource.load(null, null, 0, pageSize);
		}
		// Initialize empty state with standard message
		setEmptyTableSettings("default.tableEmptyMessage", null, FlexiTableElement.TABLE_EMPTY_ICON);
	}

	@Override
	public void setExternalRenderer(AbstractFlexiTableRenderer externalRenderer, String iconCssSelector) {
		component.setExternalRenderer(externalRenderer);
		externalTypeButton.setIconLeftCSS("o_icon " + iconCssSelector);
	}

	@Override
	public int getColumnIndexForDragAndDropLabel() {
		return columnLabelForDragAndDrop;
	}

	@Override
	public void setColumnIndexForDragAndDropLabel(int columnLabelForDragAndDrop) {
		this.columnLabelForDragAndDrop = columnLabelForDragAndDrop;
	}

	@Override
	public FlexiTableRendererType getRendererType() {
		return rendererType;
	}

	@Override
	public void setRendererType(FlexiTableRendererType rendererType) {
		// activate active render button
		if(customTypeButton != null) {
			customTypeButton.setActive(FlexiTableRendererType.custom == rendererType);
		}
		if(classicTypeButton != null) {
			classicTypeButton.setActive(FlexiTableRendererType.classic == rendererType);
		}
		if (externalTypeButton != null) {
			externalTypeButton.setActive(FlexiTableRendererType.external == rendererType);
		}
		// update render type
		this.rendererType = rendererType;
		if(component != null) {
			component.setDirty(true);
		}
	}
	
	public FlexiTableRendererType[] getAvailableRendererTypes() {
		return availableRendererType;
	}

	@Override
	public void setAvailableRendererTypes(FlexiTableRendererType... rendererTypes) {
		this.availableRendererType = rendererTypes;
		if(rendererTypes != null && rendererTypes.length > 1) {
			String dispatchId = component.getDispatchID();
			//custom
			customTypeButton = new FormLinkImpl(dispatchId + "_customRTypeButton", "rCustomRTypeButton", "", Link.BUTTON + Link.NONTRANSLATED);
			customTypeButton.setTranslator(translator);
			customTypeButton.setIconLeftCSS("o_icon o_icon_table_custom o_icon-lg");
			customTypeButton.setElementCssClass("o_sel_custom");
			customTypeButton.setActive(FlexiTableRendererType.custom == rendererType);
			customTypeButton.setAriaLabel(translator.translate("aria.view.custom"));
			components.put("rTypeCustom", customTypeButton);
			//classic tables
			classicTypeButton = new FormLinkImpl(dispatchId + "_classicRTypeButton", "rClassicRTypeButton", "", Link.BUTTON + Link.NONTRANSLATED);
			classicTypeButton.setTranslator(translator);
			classicTypeButton.setIconLeftCSS("o_icon o_icon_table o_icon-lg");
			classicTypeButton.setElementCssClass("o_sel_table");
			classicTypeButton.setActive(FlexiTableRendererType.classic == rendererType);
			classicTypeButton.setAriaLabel(translator.translate("aria.view.table"));
			components.put("rTypeClassic", classicTypeButton);
			// externally defined table visualization
			externalTypeButton = new FormLinkImpl(dispatchId + "_externalRTypeButton", "rExternalRTypeButton", "", Link.BUTTON + Link.NONTRANSLATED);
			externalTypeButton.setTranslator(translator);
			externalTypeButton.setIconLeftCSS("o_icon");
			externalTypeButton.setElementCssClass("o_sel_external");
			externalTypeButton.setActive(FlexiTableRendererType.external == rendererType);
			externalTypeButton.setAriaLabel(translator.translate("aria.view.other"));

			if(getRootForm() != null) {
				rootFormAvailable(customTypeButton);
				rootFormAvailable(classicTypeButton);
				rootFormAvailable(externalTypeButton);
			}
		}
	}

	@Override
	public FormLink getClassicTypeButton() {
		return classicTypeButton;
	}

	@Override
	public FormLink getCustomTypeButton() {
		return customTypeButton;
	}

	@Override
	public FormLink getExternalTypeButton() {
		return externalTypeButton;
	}

	public FormLink getEmptyTablePrimaryActionButton() {
		return emptyTablePrimaryActionButton;
	}
	
	@Override
	public boolean isBordered() {
		return bordered;
	}

	@Override
	public void setBordered(boolean bordered) {
		this.bordered = bordered;
	}

	@Override
	public boolean isFooter() {
		return footer;
	}

	@Override
	public void setFooter(boolean footer) {
		this.footer = footer;
	}

	public SelectionMode getSelectionMode() {
		return multiSelect;
	}
	
	public boolean isRowSelectionEnabled() {
		return rowSelection;
	}
	
	@Override
	public void setMultiSelect(boolean multiSelect) {
		this.multiSelect = multiSelect ? SelectionMode.multi : SelectionMode.disabled;
	}

	@Override
	public void setSelection(boolean enabled, boolean multiSelection, boolean rowSelection) {
		if(enabled) {
			multiSelect = multiSelection ? SelectionMode.multi : SelectionMode.single;
		} else {
			multiSelect = SelectionMode.disabled;
		}
		this.rowSelection = rowSelection;
	}

	@Override
	public boolean isCustomizeColumns() {
		return customizeColumns;
	}

	@Override
	public void setCustomizeColumns(boolean customizeColumns) {
		this.customizeColumns = customizeColumns;
	}

	public boolean isNumOfRowsEnabled() {
		return numOfRowsEnabled;
	}

	@Override
	public void setNumOfRowsEnabled(boolean enable) {
		numOfRowsEnabled = enable;
	}

	@Override
	public boolean isShowAllRowsEnabled() {
		return showAllRowsEnabled;
	}

	@Override
	public void setShowAllRowsEnabled(boolean showAllRowsEnabled) {
		this.showAllRowsEnabled = showAllRowsEnabled;
	}

	@Override
	public void setAndLoadPersistedPreferences(UserRequest ureq, String id) {
		persistentId = id;
		loadCustomSettings(ureq.getUserSession().getGuiPreferences());
	}
	
	@Override
	public void setAndLoadPersistedPreferences(Preferences preferences, String id) {
		persistentId = id;
		loadCustomSettings(preferences);
	}

	@Override
	public String getWrapperSelector() {
		return wrapperSelector;
	}

	@Override
	public void setWrapperSelector(String wrapperSelector) {
		this.wrapperSelector = wrapperSelector;
	}

	@Override
	public FlexiTableCssDelegate getCssDelegate() {
		return cssDelegate;
	}

	@Override
	public void setCssDelegate(FlexiTableCssDelegate cssDelegate) {
		this.cssDelegate = cssDelegate;
	}

	@Override
	public FlexiTableComponent getComponent() {
		return component;
	}

	public VelocityContainer getRowRenderer() {
		return rowRenderer;
	}

	public FlexiTableComponentDelegate getComponentDelegate() {
		return componentDelegate;
	}

	@Override
	public void setRowRenderer(VelocityContainer rowRenderer, FlexiTableComponentDelegate componentDelegate) {
		this.rowRenderer = rowRenderer;
		this.componentDelegate = componentDelegate;
	}
	
	public FormItem getZeroRowItem() {
		return zeroRowItem;
	}
	
	@Override
	public void setZeroRowItem(FormItem rowItem) {
		zeroRowItem = rowItem;
	}

	public boolean isMultiDetails() {
		return multiDetails;
	}

	@Override
	public void setMultiDetails(boolean multiDetails) {
		this.multiDetails = multiDetails;
	}
	
	public boolean hasDetailsRenderer() {
		return detailsRenderer != null;
	}
	
	public VelocityContainer getDetailsRenderer() {
		return detailsRenderer;
	}

	@Override
	public void setDetailsRenderer(VelocityContainer detailsRenderer, FlexiTableComponentDelegate componentDelegate) {
		this.detailsRenderer = detailsRenderer;
		this.componentDelegate = componentDelegate;
	}

	@Override
	public boolean isFilterEnabled() {
		return filters != null && !filters.isEmpty();
	}

	@Override
	public List<FlexiTableFilter> getFilters() {
		List<FlexiTableFilter> selectedFilters = new ArrayList<>(2);
		if(filters != null) {
			for(FlexiTableFilter filter:filters) {
				if(filter.isSelected()) {
					selectedFilters.add(filter);
				}
			}
		}
		
		if(filtersEl != null) {
			selectedFilters.addAll(filtersEl.getFilters());
		}
		return selectedFilters;
	}

	@Override
	public String getSelectedFilterKey() {
		String key = null;
		if(filters != null) {
			for(FlexiTableFilter filter:filters) {
				if(filter.isSelected()) {
					key = filter.getFilter();
				}
			}
		}
		return key;
	}

	@Override
	public void setSelectedFilterKey(String key) {
		if(filters != null) {
			for(FlexiTableFilter filter:filters) {
				boolean selected = (key == null && filter.getFilter() == null)
						|| (key != null && key.equals(filter.getFilter()));
				filter.setSelected(selected);
			}
		}
	}
	
	@Override
	public void setSelectedFilterKeys(Collection<String> keys) {
		if(filters != null) {
			for(FlexiTableFilter filter:filters) {
				boolean selected = keys.contains(filter.getFilter());
				filter.setSelected(selected);
			}
		}
	}

	@Override
	public void setSelectedFilters(List<FlexiTableFilter> selectedFilters) {
		if(filters != null) {
			for(FlexiTableFilter filter:filters) {
				boolean selected = selectedFilters.contains(filter);
				filter.setSelected(selected);
			}
		}
	}

	@Override
	public String getSelectedFilterValue() {
		String value = null;
		if(filters != null) {
			for(FlexiTableFilter filter:filters) {
				if(filter.isSelected()) {
					value = filter.getLabel();
				}
			}
		}
		return value;
	}
	
	public List<FlexiTableFilter> getDropdownFilters() {
		return filters;
	}

	@Override
	public void setFilters(String name, List<FlexiTableFilter> filters, boolean multiSelection) {
		this.filters = new ArrayList<>(filters);
		multiFilterSelection = multiSelection;
	}
	
	public boolean isSortEnabled() {
		return sortEnabled && (sortOptions != null && (!sortOptions.getSorts().isEmpty() || sortOptions.isFromColumnModel()));
	}
	
	@Override
	public void setSortEnabled(boolean sortEnabled) {
		this.sortEnabled = sortEnabled;
	}
	
	public FlexiTableSortOptions getSortOptions() {
		return sortOptions;
	}
	
	public List<FlexiTableSort> getSorts() {
		List<FlexiTableSort> sorts;
		if(sortOptions == null) {
			sorts = Collections.<FlexiTableSort>emptyList();
		} else if(sortOptions.getSorts() != null && !sortOptions.getSorts().isEmpty()) {
			sorts = sortOptions.getSorts();
		} else if(sortOptions.isFromColumnModel()) {
			FlexiTableColumnModel columnModel = getTableDataModel().getTableColumnModel();
			
			int cols = columnModel.getColumnCount();
			sorts = new ArrayList<>(cols);
			for(int i=0; i<cols; i++) {
				FlexiColumnModel fcm = columnModel.getColumnModel(i);
				if (fcm.isSortable() && fcm.getSortKey() != null) {
					String header;
					if(StringHelper.containsNonWhitespace(fcm.getHeaderLabel())) {
						header = fcm.getHeaderLabel();
					} else {
						header = translator.translate(fcm.getHeaderKey());
					}
					sorts.add(new FlexiTableSort(header, fcm.getSortKey()));
				}
			}
			sortOptions.setSorts(sorts);
		} else {
			sorts = Collections.<FlexiTableSort>emptyList();
		}
		return sorts; 
	}

	@Override
	public void setSortSettings(FlexiTableSortOptions options) {
		this.sortOptions = options;
		if(options.getDefaultOrderBy() != null) {
			SortKey defaultOrder = options.getDefaultOrderBy();
			orderBy = new SortKey[]{ defaultOrder };
			selectSortOption(defaultOrder.getKey(), defaultOrder.isAsc());
		}
	}

	@Override
	public boolean isExportEnabled() {
		return exportEnabled;
	}
	
	@Override
	public void setExportEnabled(boolean enabled) {
		this.exportEnabled = enabled;
		if(exportEnabled) {
			exportButton = null;
			
			String dispatchId = component.getDispatchID();
			exportButton = new FormLinkImpl(dispatchId + "_exportButton", "rExportButton", "", Link.BUTTON | Link.NONTRANSLATED);
			exportButton.setTranslator(translator);
			exportButton.setIconLeftCSS("o_icon o_icon_download");
			components.put("rExport", exportButton);
			rootFormAvailable(exportButton);
		} else {
			exportButton = null;
		}
	}
	
	public FormLink getExportButton() {
		return exportButton;
	}

	@Override
	public boolean isEditMode() {
		return editMode;
	}

	@Override
	public void setEditMode(boolean editMode) {
		if(this.editMode != editMode) {
			this.editMode = editMode;
			component.setDirty(true);
		}
	}

	@Override
	public boolean isSearchEnabled() {
		return searchEnabled;
	}
	
	public boolean isSearchLarge() {
		return searchLarge;
	}

	@Override
	public void setSearchEnabled(boolean enable) {
		this.searchEnabled = enable;
		if(searchEnabled) {
			String dispatchId = component.getDispatchID();
			if(searchFieldEl == null) {
				searchFieldEl = new TextElementImpl(dispatchId + "_searchField", "search", "");
				searchFieldEl.setDomReplacementWrapperRequired(false);
				searchFieldEl.showLabel(false);
				searchFieldEl.setAriaLabel(translator.translate("aria.search.input"));
				components.put("rSearch", searchFieldEl);
			}
			if(searchButton == null) {
				searchButton = new FormLinkImpl(dispatchId + "_searchButton", "rSearchButton", "search", Link.BUTTON);
				searchButton.setDomReplacementWrapperRequired(false);
				searchButton.setElementCssClass("o_table_search_button");
				searchButton.setTranslator(translator);
				searchButton.setIconLeftCSS("o_icon o_icon_search");
				components.put("rSearchB", searchButton);
			}
			rootFormAvailable(searchFieldEl);
			rootFormAvailable(searchButton);
		} else {
			components.remove("rSearch");
			components.remove("rSearchB");
			searchFieldEl = null;
			searchButton = null;
		}
	}
	
	@Override
	public void setSearchEnabled(ListProvider autoCompleteProvider, UserSession usess) {
		searchEnabled = true;

		String dispatchId = component.getDispatchID();
		searchFieldEl = new AutoCompleterImpl(dispatchId + "_searchField", "search", translator.getLocale());
		searchFieldEl.setDomReplacementWrapperRequired(false);
		searchFieldEl.showLabel(false);
		searchFieldEl.getComponent().addListener(this);
		((AutoCompleterImpl)searchFieldEl).setListProvider(autoCompleteProvider, usess);
		components.put("rSearch", searchFieldEl);
		searchButton = new FormLinkImpl(dispatchId + "_searchButton", "rSearchButton", "search", Link.BUTTON);
		searchButton.setDomReplacementWrapperRequired(false);
		searchButton.setElementCssClass("o_table_search_button");
		searchButton.setTranslator(translator);
		searchButton.setIconLeftCSS("o_icon o_icon_search");
		components.put("rSearchB", searchButton);
		rootFormAvailable(searchFieldEl);
		rootFormAvailable(searchButton);
	}

	public FormLink getExtendedSearchButton() {
		return extendedSearchButton;
	}
	
	@Override
	public boolean isExtendedSearchExpanded() {
		return extendedSearchExpanded;
	}

	public Component getExtendedSearchComponent() {
		return (extendedSearchCtrl == null) ? null : extendedSearchCtrl.getInitialComponent();
	}
	
	@Override
	public void setExtendedSearch(ExtendedFlexiTableSearchController controller) {
		extendedSearchCtrl = controller;
		if(extendedSearchCtrl != null) {
			extendedSearchCtrl.addControllerListener(this);
			
			String dispatchId = component.getDispatchID();
			extendedSearchButton = new FormLinkImpl(dispatchId + "_extSearchButton", "rExtSearchButton", "extsearch", Link.BUTTON);
			extendedSearchButton.setTranslator(translator);
			extendedSearchButton.setIconLeftCSS("o_icon o_icon_search");
			components.put("rExtSearchB", extendedSearchButton);
			rootFormAvailable(extendedSearchButton);
			extendedSearchButton.setElementCssClass("o_sel_flexi_extendedsearch");

			components.put("rExtSearchCmp", controller.getInitialFormItem());
		} else {
			extendedSearchButton = null;
			components.remove("rExtSearchB");
			components.remove("rExtSearchCmp");
		}
	}
	
	public FlexiFiltersElementImpl getFiltersElement() {
		return filtersEl;
	}
	
	@Override
	public boolean isFiltersEnabled() {
		return filtersEl != null;
	}
	
	@Override
	public List<FlexiTableExtendedFilter> getExtendedFilters() {
		if(filtersEl == null) {
			return new ArrayList<>();
		}
		
		List<FlexiTableFilter> filterList = filtersEl.getAllFilters();
		List<FlexiTableExtendedFilter> extendedFilters = new ArrayList<>();
		if(filterList != null) {
			for(FlexiTableFilter filter:filterList) {
				extendedFilters.add((FlexiTableExtendedFilter)filter);
			}
		}
		
		return extendedFilters;
	}

	@Override
	public void setFilters(boolean enable, List<FlexiTableExtendedFilter> filters, boolean customPresets, boolean alwaysExpanded) {
		String dispatchId = component.getDispatchID().concat("_extFiltersSet");
		if(enable) {
			if(filtersEl == null) {
				filtersEl = new FlexiFiltersElementImpl(wControl, dispatchId, this, getComponent().getTranslator());
				filtersEl.getComponent().addListener(this);
				components.put(dispatchId, filtersEl);
			}
			
			
			filtersEl.setAlwaysExpanded(alwaysExpanded);
			filtersEl.setCustomPresets(customPresets);
			if(getRootForm() != null) {
				rootFormAvailable(filtersEl);
			}
			filtersEl.setFilters(filters);
			if(this.filterTabsEl != null && filterTabsEl.getSelectedTab() != null) {
				FlexiFiltersTab selectedPreset = filterTabsEl.getSelectedTab();
				filtersEl.setImplicitFilters(selectedPreset.getImplicitFilters());
			}
		} else if(filtersEl != null) {
			filtersEl.getComponent().removeListener(this);
			filtersEl = null;
			components.remove(dispatchId);
		}
	}
	
	@Override
	public void setFiltersValues(String quickSearch, List<String> implicitFilters, List<FlexiTableFilterValue> values) {
		setFiltersValues(quickSearch, null, implicitFilters, values);
	}
	
	private void setFiltersValues(String quickSearch, List<String> enabledFilters, List<String> implicitFilters, List<FlexiTableFilterValue> values) {
		if(searchFieldEl != null) {
			if(StringHelper.containsNonWhitespace(quickSearch)) {
				searchFieldEl.setValue(quickSearch);
			} else {
				searchFieldEl.setValue("");
			}
		}
		if(filtersEl != null) {
			filtersEl.setFiltersValues(enabledFilters, implicitFilters, values, true);
		}
	}
	
	@Override
	public boolean isFiltersExpanded() {
		return filtersEl != null && filtersEl.isExpanded();
	}

	@Override
	public void expandFilters(boolean expand) {
		filtersEl.expand(expand);
	}
	
	private void doSaveCurrentPreset(UserRequest ureq, String name) {
		if(filterTabsEl == null || filtersEl == null) return;

		String id = "custom_" + CodeHelper.getForeverUniqueID();
		FlexiFiltersTabImpl newPreset = new FlexiFiltersTabImpl(id, name, TabSelectionBehavior.reloadData);
		filtersEl.saveCurrentSettingsTo(newPreset, false);
		filterTabsEl.addCustomFilterTab(newPreset);
		filterTabsEl.setSelectedTab(newPreset);
		saveCustomSettings(ureq);
	}
	
	private void doUpdateCurrentPreset(UserRequest ureq) {
		if(filterTabsEl == null || filterTabsEl.getSelectedTab() == null || filtersEl == null) return;
		
		FlexiFiltersTab preset = filterTabsEl.getSelectedTab();
		if(preset instanceof FlexiFiltersTabImpl) {
			filtersEl.saveCurrentSettingsTo((FlexiFiltersTabImpl)preset, false);
		}
		filterTabsEl.getComponent().setDirty(true);
		saveCustomSettings(ureq);
	}
	
	private void doDeleteCurrentPreset(UserRequest ureq) {
		FlexiFiltersTab selectedTab = filterTabsEl.getSelectedTab();
		filterTabsEl.removeSelectedTab(selectedTab);
		saveCustomSettings(ureq);
	}

	public FlexiFilterTabsElementImpl getFilterTabsElement() {
		return filterTabsEl;
	}
	
	@Override
	public boolean isFilterTabsEnabled() {
		return filterTabsEl != null;
	}

	@Override
	public void setFilterTabs(boolean enable, List<? extends FlexiFiltersTab> tabs) {
		String dispatchId = component.getDispatchID().concat("_extTabs");
		if(enable) {
			filterTabsEl = new FlexiFilterTabsElementImpl(dispatchId, this, getComponent().getTranslator());
			filterTabsEl.getComponent().addListener(this);
			if(getRootForm() != null) {
				rootFormAvailable(filterTabsEl);
			}
			filterTabsEl.setFilterTabs(tabs);
			components.put(dispatchId, filterTabsEl);
		} else if(filterTabsEl != null) {
			filterTabsEl.getComponent().removeListener(this);
			filterTabsEl = null;
			components.remove(dispatchId);
		}
	}

	@Override
	public FlexiFiltersTab getFilterTabById(String id) {
		if(filterTabsEl == null) return null;
		return filterTabsEl.getFilterTabById(id);
	}

	@Override
	public FlexiFiltersTab getSelectedFilterTab() {
		if(filterTabsEl == null) return null;
		return filterTabsEl.getSelectedTab();
	}

	@Override
	public void setSelectedFilterTab(UserRequest ureq, FlexiFiltersTab tab) {
		doUnSelectAll();
		filterTabsEl.setSelectedTab(tab);
		if(tab.isFiltersExpanded() && filtersEl != null) {
			filtersEl.expand(true);
		}
		searchLarge = tab.isLargeSearch();

		setFiltersValues(null, tab.getEnabledFilters(), tab.getImplicitFilters(), tab.getDefaultFiltersValues());
		
		if(tab.getSelectionBehavior() == TabSelectionBehavior.reloadData) {
			rowCount = -1;
			currentPage = 0;
			reloadData();
		} else if(tab.getSelectionBehavior() == TabSelectionBehavior.clear) {
			rowCount = -1;
			currentPage = 0;
			if(dataModel instanceof FlexiTableDataSource) {
				((FlexiTableDataSource<?>)dataModel).clear();
				reset(true, true, false);
			}
		}
		
		addToHistory(ureq);
	}

	@Override
	public void addToHistory(UserRequest ureq) {
		FlexiFiltersTab tab = getSelectedFilterTab();
		if(ureq != null) {
			if(tab == null) {
				BusinessControlFactory.getInstance().addToHistory(ureq, wControl);
			} else {
				OLATResourceable ores = OresHelper.createOLATResourceableInstanceWithoutCheck(tab.getId(), Long.valueOf(0l));
				BusinessControlFactory.getInstance().createBusinessWindowControl(ureq, ores, null, wControl, true);
			}
		}
	}

	@Override
	public boolean isSelectAllEnable() {
		return selectAllEnabled;
	}
	
	@Override
	public void setSelectAllEnable(boolean enable) {
		this.selectAllEnabled = enable;
	}
	
	@Override
	public boolean isDetailsExpended(int row) {
		if(detailsIndex == null) {
			return false;
		}
		return detailsIndex.contains(row);
	}

	@Override
	public void expandDetails(int row) {
		if(detailsIndex == null || !multiDetails) {
			detailsIndex = new HashSet<>();
		}
		detailsIndex.add(row);
		component.setDirty(true);
	}

	@Override
	public void collapseDetails(int row) {
		if(detailsIndex != null && detailsIndex.remove(row)) {
			component.setDirty(true);
			if(detailsIndex.isEmpty()) {
				detailsIndex = null;
			}
		}
	}

	@Override
	public void collapseAllDetails() {
		if(detailsIndex != null && detailsIndex.size() > 0) {
			detailsIndex = null;
			component.setDirty(true);
		}
	}

	@Override
	public FlexiTableStateEntry getStateEntry() {
		FlexiTableStateEntry entry = new FlexiTableStateEntry();
		if(searchFieldEl != null && searchFieldEl.isVisible()) {
			entry.setSearchString(searchFieldEl.getValue());
		}
		entry.setExpendedSearch(extendedSearchExpanded);
		return entry;
	}

	@Override
	public void setStateEntry(UserRequest ureq, FlexiTableStateEntry state) {
		if(state.isExpendedSearch()) {
			expandExtendedSearch(ureq);
		}
		if(StringHelper.containsNonWhitespace(state.getSearchString())) {
			quickSearch(ureq, state.getSearchString());
		}
	}

	public String getSearchText() {
		return searchFieldEl == null || !searchFieldEl.isVisible() || !searchFieldEl.isEnabled() ? null : searchFieldEl.getValue();
	}
	
	public List<String> getConditionalQueries() {
		return List.of();
	}
	
	@Override
	public SortKey[] getOrderBy() {
		return orderBy;
	}

	public TextElement getSearchElement() {
		return searchFieldEl;
	}
	
	public FormLink getSearchButton() {
		return searchButton;
	}
	
	public FormLink getCustomButton() {
		return customButton;
	}
	
	public FormLink getSettingsButton() {
		return settingsButton;
	}

	@Override
	public FlexiTreeTableNode getRootCrumb() {
		return rootCrumb;
	}

	@Override
	public void setRootCrumb(FlexiTreeTableNode rootCrumb) {
		this.rootCrumb = rootCrumb;
	}

	@Override
	public int getDefaultPageSize() {
		return defaultPageSize;
	}

	@Override
	public int getPageSize() {
		return pageSize;
	}

	@Override
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
	@Override
	public int getPage() {
		return currentPage;
	}
	
	@Override
	public void setPage(int page) {
		if(currentPage == page) return;
		if(page < 0) {
			page = 0;
		}
		currentPage = page;
		if(dataSource != null) {
			int firstResult = currentPage * getPageSize();
			int maxResults = getPageSize();
			if(dataModel instanceof FlexiTableDataSource) {
				((FlexiTableDataSource<?>)dataModel).load(getSearchText(), getFilters(), firstResult, maxResults, orderBy);
			} else {
				dataSource.load(getSearchText(), getFilters(), firstResult, maxResults, orderBy);
			}
		}
		component.setDirty(true);
	}
	
	@Override
	public void preloadPageOfObjectIndex(int index) {
		int page = 0;
		if(index > 0) {
			page = index / getPageSize();
		}
		int firstResult = page * getPageSize();
		int maxResults = getPageSize();
		
		if(dataModel instanceof FlexiTableDataSource) {
			((FlexiTableDataSource<?>)dataModel).load(getSearchText(), getFilters(), firstResult, maxResults, orderBy);
		} else if(this.dataSource != null) {
			dataSource.load(getSearchText(), getFilters(), firstResult, maxResults, orderBy);
		}
	}

	@Override
	public Iterable<FormItem> getFormItems() {
		List<FormItem> items = new ArrayList<>(components.values());
		if(extendedSearchCtrl != null && !extendedSearchExpanded) {
			items.remove(extendedSearchCtrl.getInitialFormItem());
		}
		return items;
	}

	@Override
	public FormItem getFormComponent(String name) {
		return components.get(name);
	}

	public void addFormItem(FormItem item) {
		components.put(item.getName(), item);
		
		// set the form translator, and parent
		Translator itemTranslator = item.getTranslator();
		if (itemTranslator != null && !itemTranslator.equals(translator)
				&& itemTranslator instanceof PackageTranslator itemPt) {
			// let the FormItem provide a more specialized translator
			itemTranslator = PackageTranslator.cascadeTranslators(itemPt, translator);
		} else {
			itemTranslator = translator;
		}
		
		if(getRootForm() != null) {
			rootFormAvailable(item);
		}
	}
	
	public List<FormItem> getBatchButtons() {
		return batchButtons;
	}
	
	@Override
	public void addBatchButton(FormItem item) {
		if(item != null) {
			batchButtons.add(item);
			addFormItem(item);
		}
	}
	
	@Override
	public boolean isBatchButtonAvailable() {
		return batchButtons != null && !batchButtons.isEmpty();
	}

	@Override
	public void doDispatchFormRequest(UserRequest ureq) {
		super.doDispatchFormRequest(ureq);
	}
	
	@Override
	protected void dispatchFormRequest(UserRequest ureq) {
		super.dispatchFormRequest(ureq);
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		Form form = getRootForm();
		String selectedIndex = form.getRequestParameter("rSelect");
		String dispatchuri = form.getRequestParameter("dispatchuri");
		String select = form.getRequestParameter("select");
		String page = form.getRequestParameter("page");
		String sort = form.getRequestParameter("sort");
		String filter = form.getRequestParameter("filter");
		String pagesize = form.getRequestParameter("pagesize");
		String checkbox = form.getRequestParameter("chkbox");
		String details = form.getRequestParameter("tt-details");
		String removeFilter = form.getRequestParameter("rm-filter");
		String resetQuickSearch = form.getRequestParameter("reset-search");
		String treeTableFocus = form.getRequestParameter("tt-focus");
		String treeTableOpen = form.getRequestParameter("tt-open");
		String treeTableClose = form.getRequestParameter("tt-close");
		String crumb = form.getRequestParameter("tt-crumb");
		String openCloseAll = form.getRequestParameter("tt-openclose");
		String selectAllColumn = form.getRequestParameter("cc-selectall");
		String deselectAllColumn = form.getRequestParameter("cc-deselectall");
		if("undefined".equals(dispatchuri)) {
			evalSearchRequest(ureq);
		} else if(StringHelper.containsNonWhitespace(checkbox)) {
			toogleSelectIndex(ureq, checkbox);
		} else if(StringHelper.containsNonWhitespace(details)) {
			toogleDetails(details, ureq);
		} else if(StringHelper.containsNonWhitespace(page)) {
			int p = Integer.parseInt(page);
			setPage(p);
		 } else if(StringHelper.containsNonWhitespace(pagesize)) {
			int p;
			if("all".equals(pagesize)) {
				p = -1;
			} else {
				p = Integer.parseInt(pagesize);
			}
			selectPageSize(ureq, p);
		} else if(StringHelper.containsNonWhitespace(sort) && dispatchuri != null && dispatchuri.equals(component.getFormDispatchId())) {
			String asc = form.getRequestParameter("asc");
			sort(sort, "asc".equals(asc));
			saveCustomSettings(ureq);
		} else if(StringHelper.containsNonWhitespace(selectedIndex)) {
			int index = selectedIndex.lastIndexOf('-');
			if(index > 0 && index+1 < selectedIndex.length()) {
				String pos = selectedIndex.substring(index+1);
				doSelect(ureq, Integer.parseInt(pos));
			}
		} else if(StringHelper.containsNonWhitespace(resetQuickSearch)) {
			resetQuickSearch(ureq);
		} else if(searchButton != null
				&& searchButton.getFormDispatchId().equals(dispatchuri)) {
			evalSearchRequest(ureq);
		} else if(extendedSearchButton != null
				&& extendedSearchButton.getFormDispatchId().equals(dispatchuri)) {
			expandExtendedSearch(ureq);
		} else if(dispatchuri != null && StringHelper.containsNonWhitespace(filter)) {
			doFilter(ureq, filter);
		} else if(StringHelper.containsNonWhitespace(removeFilter)) {
			doFilter(ureq, null);
		} else if(StringHelper.isLong(treeTableFocus)) {
			doTreeFocus(Integer.parseInt(treeTableFocus));
		} else if(StringHelper.isLong(treeTableOpen)) {
			doTreeOpen(Integer.parseInt(treeTableOpen));
		} else if(StringHelper.isLong(treeTableClose)) {
			doTreeClose(Integer.parseInt(treeTableClose));
		} else if(StringHelper.containsNonWhitespace(crumb)) {
			doCrumb(crumb);
		} else if(StringHelper.containsNonWhitespace(openCloseAll)) {
			if("openall".equals(openCloseAll)) {
				doOpenAll();
			} else if("closeall".equals(openCloseAll)) {
				doCloseAll();
			}
		} else if(exportButton != null
				&& exportButton.getFormDispatchId().equals(dispatchuri)) {
			doExport(ureq);
		} else if (emptyTablePrimaryActionButton != null && emptyTablePrimaryActionButton.getFormDispatchId().equals(dispatchuri)) {
			getRootForm().fireFormEvent(ureq, new FlexiTableEmptyNextPrimaryActionEvent(this));
		} else if(dispatchuri != null && select != null && select.equals("checkall")) {
			selectAll();
		} else if(dispatchuri != null && select != null && select.equals("checkpage")) {
			selectPage();
		} else if(dispatchuri != null && select != null && select.equals("uncheckall")) {
			doUnSelectAll();
		} else if(dispatchuri != null && StringHelper.isLong(selectAllColumn)) {
			doSelectAllColumn(ureq, Integer.parseInt(selectAllColumn));
		} else if(dispatchuri != null && StringHelper.isLong(deselectAllColumn)) {
			doUnSelectAllColumn(ureq, Integer.parseInt(deselectAllColumn));
		} else if(customButton != null
				&& customButton.getFormDispatchId().equals(dispatchuri)) {
			//snap the request
			customizeCallout(ureq);
		} else if(settingsButton != null
				&& settingsButton.getFormDispatchId().equals(dispatchuri)) {
			doOpenSettings(ureq);
		} else if(customTypeButton != null
				&& customTypeButton.getFormDispatchId().equals(dispatchuri)) {
			setRendererType(FlexiTableRendererType.custom);
			saveCustomSettings(ureq);
			getRootForm().fireFormEvent(ureq, new FlexiTableRenderEvent(FlexiTableRenderEvent.CHANGE_RENDER_TYPE, this,
					FlexiTableRendererType.custom, FormEvent.ONCLICK));
		} else if(classicTypeButton != null
				&& classicTypeButton.getFormDispatchId().equals(dispatchuri)) {
			setRendererType(FlexiTableRendererType.classic);
			saveCustomSettings(ureq);
			getRootForm().fireFormEvent(ureq, new FlexiTableRenderEvent(FlexiTableRenderEvent.CHANGE_RENDER_TYPE, this,
					FlexiTableRendererType.classic, FormEvent.ONCLICK));
		} else if (externalTypeButton != null
				&& externalTypeButton.getFormDispatchId().equals(dispatchuri)) {
			setRendererType(FlexiTableRendererType.external);
			saveCustomSettings(ureq);
			getRootForm().fireFormEvent(ureq, new FlexiTableRenderEvent(FlexiTableRenderEvent.CHANGE_RENDER_TYPE, this,
					FlexiTableRendererType.external, FormEvent.ONCLICK));
		} else if(getFormDispatchId().equals(dispatchuri) && doSelect(ureq)) {
			//do select
		}
	}
	
	private boolean doSelect(UserRequest ureq) {
		boolean select = false;
		FlexiTableColumnModel colModel = dataModel.getTableColumnModel();
		for(int i=colModel.getColumnCount(); i-->0; ) {
			FlexiColumnModel col = colModel.getColumnModel(i);
			if(col.getAction() != null) {
				String selectedRowIndex = getRootForm().getRequestParameter(col.getAction());
				if(StringHelper.containsNonWhitespace(selectedRowIndex)) {
					doSelect(ureq, col.getAction(), Integer.parseInt(selectedRowIndex));
					select = true;
					break;
				}
			}
			
			if(col.getCellRenderer() instanceof ActionDelegateCellRenderer) {
				ActionDelegateCellRenderer delegateRenderer = (ActionDelegateCellRenderer)col.getCellRenderer();
				List<String> rendererActions = delegateRenderer.getActions();
				if(rendererActions != null && !rendererActions.isEmpty()) {
					for(String rendererAction:rendererActions) {
						String selectedRowIndex = getRootForm().getRequestParameter(rendererAction);
						if(StringHelper.containsNonWhitespace(selectedRowIndex)) {
							doSelect(ureq, rendererAction, Integer.parseInt(selectedRowIndex));
							select = true;
							break;
						}
					}
				}
			}
		}
		return select;
	}
	
	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		if(source == callout) {
			if(CloseableCalloutWindowController.CLOSE_WINDOW_EVENT == event) {
				//already deactivated
				callout = null;
			}
		} else if(source == extendedSearchCtrl) {
			if(event == Event.CANCELLED_EVENT) {
				collapseExtendedSearch();
			} else if(event == Event.DONE_EVENT) {
				evalExtendedSearch(ureq);
			}
		} else if(settingsCtrl == source) {
			if(event instanceof FiltersAndSettingsEvent) {
				if(FiltersAndSettingsEvent.FILTERS_RESET.equals(event.getCommand())) {
					resetFiltersSearch(ureq);
				} else {
					doSetSettings(ureq, (FiltersAndSettingsEvent)event);
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		settingsCtrl = cleanUp(settingsCtrl);
		cmc = cleanUp(cmc);
	}
	
	private <T extends Controller> T cleanUp(T ctrl) {
		if(ctrl != null) {
			ctrl.removeControllerListener(this);
			ctrl = null;
		}
		return ctrl;
	}
	
	@Override
	public void dispatchEvent(UserRequest ureq, Component source, Event event) {
		if(filterTabsEl != null && filterTabsEl.getComponent() == source) {
			if(event instanceof SelectFilterTabEvent) {
				FlexiFiltersTab tab = ((SelectFilterTabEvent)event).getTab();
				setSelectedFilterTab(ureq, tab);
				getRootForm().fireFormEvent(ureq, new FlexiTableFilterTabEvent(this, tab, FormEvent.ONCLICK));
			} else if(event instanceof RemoveFiltersEvent) {
				resetFiltersSearch(ureq);
			}
		} else if(filtersEl != null && filtersEl.getComponent() == source) {
			if(event instanceof ChangeFilterEvent) {
				ChangeFilterEvent ce = (ChangeFilterEvent)event;
				doSearch(ureq, FlexiTableReduceEvent.FILTER, getSearchText(), List.of((FlexiTableFilter)ce.getFilter()));
			} else if(event instanceof ExpandFiltersEvent && filterTabsEl != null && filterTabsEl.isVisible()) {
				filterTabsEl.getComponent().setDirty(true);
			} else if(event instanceof RemoveFiltersEvent) {
				resetFiltersSearch(ureq);
			} else if(event instanceof SaveCurrentPresetEvent) {
				doSaveCurrentPreset(ureq, ((SaveCurrentPresetEvent)event).getName());
			} else if(event instanceof UpdateCurrentPresetEvent) {
				doUpdateCurrentPreset(ureq);
			} else if(event instanceof DeleteCurrentPresetEvent) {
				doDeleteCurrentPreset(ureq);
			}
		} else if(source instanceof Choice) {
			if(Choice.EVNT_VALIDATION_OK.equals(event)) {
				Choice visibleColsChoice = (Choice)source;
				setCustomizedColumns(ureq, visibleColsChoice);
			} else if(Choice.EVNT_FORM_RESETED.equals(event)) {
				resetCustomizedColumns(ureq);
			}
			if(callout != null) {
				callout.deactivate();
				callout = null;
			}
		} else if(searchFieldEl.getComponent() == source) {
			if(event instanceof AutoCompleteEvent) {
				AutoCompleteEvent ace = (AutoCompleteEvent)event;
				doSearch(ureq, FlexiTableReduceEvent.QUICK_SEARCH_KEY_SELECTION, ace.getKey(), null);
			}
		}
	}
	
	private void selectPageSize(UserRequest ureq, int size) {
		if(callout != null) {
			callout.deactivate();
			callout = null;
		}
		
		setPageSize(size);
		//reset
		rowCount = -1;
		currentPage = 0;
		component.setDirty(true);
		reloadData();
		saveCustomSettings(ureq);
	}
	
	@Override
	public void sort(String sortKey, boolean asc) {
		collapseAllDetails();
		
		SortKey key;
		if(StringHelper.containsNonWhitespace(sortKey)) {
			key = new SortKey(sortKey, asc);
			orderBy = new SortKey[]{ key };
		} else {
			key = null;
			orderBy = null;
		}
		
		if(dataModel instanceof SortableFlexiTableDataModel) {
			if(dataModel instanceof FlexiTreeTableDataModel && (sortOptions != null && sortOptions.isOpenAllBySort())) {
				doOpenAll();
			}
			((SortableFlexiTableDataModel<?>)dataModel).sort(key);
		} else if(dataSource != null) {
			currentPage = 0;
			if(dataModel instanceof FlexiTableDataSource) {
				((FlexiTableDataSource<?>)dataModel).clear();
				((FlexiTableDataSource<?>)dataModel).load(getSearchText(), getFilters(), 0, getPageSize(), orderBy);
			} else {
				dataSource.clear();
				dataSource.load(getSearchText(), getFilters(), 0, getPageSize(), orderBy);
			}
		}
		reorderMultiSelectIndex();
		selectSortOption(sortKey, asc);
		component.setDirty(true);
	}
	
	@Override
	public void sort(SortKey sortKey) {
		collapseAllDetails();
		orderBy = new SortKey[]{ sortKey };
		
		if(dataModel instanceof SortableFlexiTableDataModel) {
			if(dataModel instanceof FlexiTreeTableDataModel && (sortOptions != null && sortOptions.isOpenAllBySort())) {
				doOpenAll();
			}
			((SortableFlexiTableDataModel<?>)dataModel).sort(sortKey);
		} else if(dataSource != null) {
			currentPage = 0;

			if(dataModel instanceof FlexiTableDataSource) {
				((FlexiTableDataSource<?>)dataModel).clear();
				((FlexiTableDataSource<?>)dataModel).load(getSearchText(), getFilters(), 0, getPageSize(), orderBy);
			} else {
				dataSource.clear();
				dataSource.load(getSearchText(), getFilters(), 0, getPageSize(), orderBy);
			}
		}
		reorderMultiSelectIndex();
		selectSortOption(sortKey.getKey(), sortKey.isAsc());
		component.setDirty(true);
	}
	

	private void reorderMultiSelectIndex() {
		if(multiSelectedIndex == null) return;
		
		Set<Object> selectedObjects = new HashSet<>(multiSelectedIndex.values());
		multiSelectedIndex.clear();
		
		for(int i=dataModel.getRowCount(); i-->0; ) {
			Object obj = dataModel.getObject(i);
			if(obj != null && selectedObjects.contains(obj)) {
				multiSelectedIndex.put(Integer.valueOf(i), obj);
			}
		}
		
		// In the case of a data source, we need to check if all index has been found.
		// If not, we need to load all the data and find them
		if(dataSource != null && multiSelectedIndex.size() != selectedObjects.size()) {
			if(dataModel instanceof FlexiTableDataSource) {
				((FlexiTableDataSource<?>)dataModel).load(getSearchText(), getFilters(), 0, -1, orderBy);
			} else {
				dataSource.load(getSearchText(), getFilters(), 0, -1, orderBy);
			}
			for(int i=dataModel.getRowCount(); i-->0; ) {
				Object obj = dataModel.getObject(i);
				if(obj != null && selectedObjects.contains(obj)) {
					multiSelectedIndex.put(Integer.valueOf(i), obj);
				}
			}
		}
	}
	
	private void selectSortOption(String sortKey, boolean asc) {
		if(sortOptions != null) {
			for(FlexiTableSort sort:sortOptions.getSorts()) {
				boolean selected = sort.getSortKey().getKey().equals(sortKey);
				sort.setSelected(selected);
				if(selected) {
					sort.getSortKey().setAsc(asc);
				} else {
					sort.getSortKey().setAsc(false);
				}
			}
		}
	}
	
	private void doFilter(UserRequest ureq, String filterKey) {
		List<FlexiTableFilter> selectedFilters = new ArrayList<>();
		if(filterKey == null) {
			for(FlexiTableFilter filter:filters) {
				filter.setSelected(false);
			}
		} else if(multiFilterSelection) {
			boolean showAll = false;
			for(FlexiTableFilter filter:filters) {
				if(filter.getFilter().equals(filterKey) && filter.isShowAll()) {
					showAll = !filter.isSelected();//Show all is currently not selected, but the event will toggle it
				}
			}
			
			if(showAll) {
				for(FlexiTableFilter filter:filters) {
					filter.setSelected(filter.isShowAll());
				}
			} else {
				for(FlexiTableFilter filter:filters) {
					if(filter.isShowAll()) {
						filter.setSelected(false);
					} else if(filter.getFilter().equals(filterKey)) {
						filter.setSelected(!filter.isSelected());
					}
				}
			}

			for(FlexiTableFilter filter:filters) {
				if(filter.isSelected()) {
					selectedFilters.add(filter);
				}
			}
		} else {
			for(FlexiTableFilter filter:filters) {
				boolean selected = filter.getFilter().equals(filterKey);
				if(selected) {
					if(filter.isSelected()) {
						filter.setSelected(false);
					} else {
						filter.setSelected(true);
						selectedFilters.add(filter);
					}
				} else {
					filter.setSelected(false);
				}
			}
		}

		if(dataModel instanceof FilterableFlexiTableModel) {
			rowCount = -1;
			currentPage = 0;
			doUnSelectAll();
			((FilterableFlexiTableModel)dataModel).filter(getQuickSearchString(), selectedFilters);
			if(dataModel instanceof SortableFlexiTableDataModel) {
				if(orderBy != null && orderBy.length > 0) {
					((SortableFlexiTableDataModel<?>)dataModel).sort(orderBy[0]);
				}
			}
		} else if(dataSource != null) {
			rowCount = -1;
			currentPage = 0;
			doUnSelectAll();
			if(dataModel instanceof FlexiTableDataSource) {
				((FlexiTableDataSource<?>)dataModel).clear();
				((FlexiTableDataSource<?>)dataModel).load(null, selectedFilters, 0, getPageSize(), orderBy);
			} else {
				dataSource.clear();
				dataSource.load(null, selectedFilters, 0, getPageSize(), orderBy);
			}
		}
		component.setDirty(true);
		
		getRootForm().fireFormEvent(ureq, new FlexiTableFilterEvent(FlexiTableReduceEvent.FILTER, this,
				getQuickSearchString(), getFilters(), FormEvent.ONCLICK));
	}
	
	@Override
	public void focus(FlexiTreeTableNode node) {
		if(node == null) return;

		int row = -1;
		int numOfRows = dataModel.getRowCount();
		for(int i=0; i<numOfRows; i++) {
			Object obj = dataModel.getObject(i);
			if(node.equals(obj)) {
				row = i;
				break;
			}
		}
		
		if(row >= 0) {
			doTreeFocus(row);
		}
	}
	
	private void doTreeFocus(int row) {
		if(dataModel instanceof FlexiTreeTableDataModel) {
			FlexiTreeTableNode node = ((FlexiTreeTableDataModel<?>)dataModel).getObject(row);
			crumbs = new ArrayList<>();
			for(FlexiTreeTableNode parent=node; parent != null; parent=parent.getParent()) {
				crumbs.add(parent);
			}
			Collections.reverse(crumbs);
			((FlexiTreeTableDataModel<?>)dataModel).focus(row);
			reset(true, true, true);
		}
	}
	
	private void doTreeOpen(int row) {
		if(dataModel instanceof FlexiTreeTableDataModel) {
			((FlexiTreeTableDataModel<?>)dataModel).open(row);
			updateTreeInternComponents();
		}
	}
	
	private void doTreeClose(int row) {
		if(dataModel instanceof FlexiTreeTableDataModel) {
			((FlexiTreeTableDataModel<?>)dataModel).close(row);
			updateTreeInternComponents();
		}
	}
	
	private void updateTreeInternComponents() {
		rowCount = dataModel.getRowCount();
		component.setDirty(true);
		detailsIndex = null;

		if(multiSelectedIndex != null && dataModel instanceof FlexiTableSelectionDelegate) {
			FlexiTableSelectionDelegate<?> treeModel = (FlexiTableSelectionDelegate<?>)dataModel;
			List<?> nodes = treeModel.getSelectedTreeNodes();
			Set<?> nodeSet = new HashSet<>(nodes);

			multiSelectedIndex.clear();
			for(int i=0; i<rowCount; i++) {
				if(nodeSet.contains(dataModel.getObject(i))) {
					multiSelectedIndex.put(Integer.valueOf(i), dataModel.getObject(i));
				}
			}
			allSelectedNeedLoadOfWholeModel = false;
		}
	}
	
	private void doCrumb(String index) {
		FlexiTreeTableNode crumb = null;
		if("tt-root-crumb".equals(index)) {
			crumb = rootCrumb;
			crumbs = null;
		} else if(StringHelper.isLong(index)) {
			int pos = Integer.parseInt(index);
			if(crumbs != null && pos >= 0 && pos < crumbs.size()) {
				crumb = crumbs.get(pos);
				crumbs = crumbs.subList(0, pos + 1);
			}
		}
		if(dataModel instanceof FlexiTreeTableDataModel) {
			((FlexiTreeTableDataModel<?>)dataModel).popBreadcrumb(crumb);
			reset(false, true, true);
		}
	}
	
	private void doOpenAll() {
		if(dataModel instanceof FlexiTreeTableDataModel) {
			((FlexiTreeTableDataModel<?>)dataModel).openAll();
			reset(false, true, true);
		}
	}
	
	private void doCloseAll() {
		if(dataModel instanceof FlexiTreeTableDataModel) {
			((FlexiTreeTableDataModel<?>)dataModel).closeAll();
			resetInternComponents();
		}
	}
	
	private void doExport(UserRequest ureq) {
		// ensure the all rows are loaded to export
		if(dataSource != null) {
			if(dataModel instanceof FlexiTableDataSource) {
				((FlexiTableDataSource<?>)dataModel).load(getSearchText(), getFilters(), 0, -1, orderBy);
			} else {
				dataSource.load(getSearchText(), getFilters(), 0, -1, orderBy);
			}
		}
		
		MediaResource resource;
		if(dataModel instanceof ExportableFlexiTableDataModel) {
			resource = ((ExportableFlexiTableDataModel)dataModel).export(component);
		} else {
			ExportableFlexiTableDataModelDelegate exporter = new ExportableFlexiTableDataModelDelegate();
			resource = exporter.export(component, getTranslator());
		}
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
	
	@Override
	public void expandExtendedSearch(UserRequest ureq) {
		component.setDirty(true);
		extendedSearchExpanded = true;
		extendedSearchCtrl.setEnabled(true);
		if(searchFieldEl != null) {
			searchFieldEl.setValue("");
			searchFieldEl.setVisible(false);
		}
	}
	
	@Override
	public void collapseExtendedSearch() {
		extendedSearchExpanded = false;
		extendedSearchCtrl.setEnabled(false);
		if(searchFieldEl != null) {
			searchFieldEl.setValue("");
			searchFieldEl.setVisible(true);
		}
	}

	protected void customizeCallout(UserRequest ureq) {
		Choice choice = getColumnListAndTheirVisibility();
		callout = new CloseableCalloutWindowController(ureq, wControl, choice,
				customButton, translator.translate("customize.columns"), true, "o_sel_flexi_custom_callout");
		callout.activate();
		callout.addControllerListener(this);
	}
	
	public Set<Integer> getEnabledColumnIndex() {
		return new HashSet<>(enabledColumnIndex);
	}

	@Override
	public boolean isColumnModelVisible(FlexiColumnModel col) {
		return col.isAlwaysVisible() || enabledColumnIndex.contains(col.getColumnIndex());
	}

	@Override
	public void setColumnModelVisible(FlexiColumnModel col, boolean visible) {
		boolean currentlyVisible = enabledColumnIndex.contains(col.getColumnIndex());
		if(currentlyVisible != visible) {
			if(visible) {
				enabledColumnIndex.add(col.getColumnIndex());
			} else {
				enabledColumnIndex.remove(col.getColumnIndex());
			}
		}
	}
	
	protected void setCustomizedColumns(UserRequest ureq, Choice visibleColsChoice) {
		List<Integer> chosenCols = visibleColsChoice.getSelectedRows();
		if(!chosenCols.isEmpty() || hasAlwaysVisibleColumns) {
			VisibleFlexiColumnsModel model = (VisibleFlexiColumnsModel)visibleColsChoice.getModel();
			for(int i=model.getRowCount(); i-->0; ) {
				FlexiColumnModel col = model.getObject(i);
				if(chosenCols.contains(Integer.valueOf(i))) {
					enabledColumnIndex.add(col.getColumnIndex());
				} else {
					enabledColumnIndex.remove(col.getColumnIndex());
				}
			}
		}
		saveCustomSettings(ureq);
		component.setDirty(true);
	}
	
	protected void resetCustomizedColumns(UserRequest ureq) {
		enabledColumnIndex.clear();
		for(int i=dataModel.getTableColumnModel().getColumnCount(); i-->0; ) {
			FlexiColumnModel col = dataModel.getTableColumnModel().getColumnModel(i);
			if(col.isDefaultVisible()) {
				enabledColumnIndex.add(Integer.valueOf(col.getColumnIndex()));
			}
		}
		
		if(pageSize > 0) {
			selectPageSize(ureq, defaultPageSize);
		}
		saveCustomSettings(ureq);
		component.setDirty(true);
	}
	
	private void doOpenSettings(UserRequest ureq) {
		settingsCtrl = new FlexiFiltersAndSettingsDialogController(ureq, wControl, this);
		settingsCtrl.addControllerListener(this);

		String title = component.getTranslator().translate("table.settings");
		cmc = new CloseableModalController(wControl, component.getTranslator().translate("close"), settingsCtrl.getInitialComponent(), true, title, true);
		cmc.setCustomWindowCSS("o_offcanvas_right_modal o_table_filters_and_settings");
		cmc.activate();
		cmc.addControllerListener(this);
	}
	
	private void doSetSettings(UserRequest ureq, FiltersAndSettingsEvent e) {
		if(e.getRenderType() != null) {
			setRendererType(e.getRenderType());
		}
		
		if(e.isResetCustomizedColumns()) {
			resetCustomizedColumns(ureq);
		} else if(e.getCustomizedColumns() != null) {
			setCustomizedColumns(ureq, e.getCustomizedColumns());
		}
		
		if(e.getSortKey() != null) {
			setSortSettings(sortOptions);
			orderBy = new SortKey[]{ e.getSortKey() };
		}

		// need to be the last
		if(filtersEl != null && e.getFilterValues() != null) {
			List<String> implicitFilters = filterTabsEl == null ? null : filterTabsEl.getImplicitFiltersOfSelectedTab();
			filtersEl.setFiltersValues(null, implicitFilters, e.getFilterValues(), false);
			doSearch(ureq, FlexiTableReduceEvent.FILTER, getSearchText(), filtersEl.getSelectedFilters());
		} else if(e.getSortKey() != null) {
			sort(e.getSortKey());
		}
		
		saveCustomSettings(ureq);
		
		if(e.getRenderType() != null) {
			getRootForm().fireFormEvent(ureq, new FlexiTableRenderEvent(FlexiTableRenderEvent.CHANGE_RENDER_TYPE, this,
					e.getRenderType(), FormEvent.ONCLICK));
		}
	}
	
	private void saveCustomSettings(UserRequest ureq) {
		if(StringHelper.containsNonWhitespace(persistentId)) {
			Preferences prefs = ureq.getUserSession().getGuiPreferences();
			
			boolean sortDirection = false;
			String sortedColKey = null;
			if(orderBy != null && orderBy.length > 0 && orderBy[0] != null) {
				sortDirection = orderBy[0].isAsc();
				String sortKey = orderBy[0].getKey();
				if(sortKey != null) {
					FlexiTableColumnModel colModel = dataModel.getTableColumnModel();
					for(int i=colModel.getColumnCount(); i-->0; ) {
						FlexiColumnModel col = colModel.getColumnModel(i);
						if(col.getSortKey() != null && sortKey.equals(col.getSortKey())) {
							sortedColKey = col.getColumnKey();
						}
					}
				}
				
				if(sortedColKey == null && sortOptions != null && sortOptions.getSorts() != null) {
					for(FlexiTableSort sortOption :sortOptions.getSorts()) {
						if(sortOption.getSortKey().getKey().equals(sortKey)) {
							sortedColKey = sortKey;
						}
					}
				}
			}
			
			List<FlexiFiltersTab> presets = null;
			if(filterTabsEl != null) {
				List<FlexiFiltersTab> customTabs = filterTabsEl.getCustomFilterTabs();
				presets = new ArrayList<>(customTabs.size());
				for(FlexiFiltersTab customTab:customTabs) {
					presets.add(customTab);
				}
			}

			FlexiTablePreferences tablePrefs =
					new FlexiTablePreferences(getPageSize(), sortedColKey, sortDirection,
							convertColumnIndexToKeys(enabledColumnIndex), presets, rendererType);
			prefs.putAndSave(FlexiTableElement.class, persistentId, tablePrefs);
		}
	}
	
	private void loadCustomSettings(Preferences prefs) {
		if(StringHelper.containsNonWhitespace(persistentId)) {
			FlexiTablePreferences tablePrefs = (FlexiTablePreferences)prefs.get(FlexiTableElement.class, persistentId);
			if(tablePrefs != null) {
				if(tablePrefs.getPageSize() != getDefaultPageSize() && tablePrefs.getPageSize() != 0) {
					setPageSize(tablePrefs.getPageSize());
				}
				
				if(tablePrefs.getEnabledColumnKeys() != null) {
					enabledColumnIndex.clear();
					enabledColumnIndex.addAll(convertColumnKeysToIndex(tablePrefs.getEnabledColumnKeys()));
				}
				
				if(StringHelper.containsNonWhitespace(tablePrefs.getSortedColumnKey())) {
					loadCustomColumnsSettings(tablePrefs);
				}
				
				if(tablePrefs.getCustomTabs() != null && filterTabsEl != null) {
					for(FlexiFiltersTab customTab:tablePrefs.getCustomTabs()) {
						filterTabsEl.addCustomFilterTab(customTab);
					}
				}

				if(tablePrefs.getRendererType() != null) {
					setRendererType(tablePrefs.getRendererType());
				}
			}
		}
	}
	
	private void loadCustomColumnsSettings(FlexiTablePreferences tablePrefs) {
		String sortKey = null;
		String columnKey = tablePrefs.getSortedColumnKey();
		FlexiTableColumnModel colModel = dataModel.getTableColumnModel();
		for(int i=colModel.getColumnCount(); i-->0; ) {
			FlexiColumnModel col = colModel.getColumnModel(i);
			if(columnKey.equals(col.getColumnKey()) && col.isSortable()) {
				sortKey = col.getSortKey();
			}
		}
		if(sortKey == null && sortOptions != null && sortOptions.getSorts() != null) {
			for(FlexiTableSort sortOption :sortOptions.getSorts()) {
				if(sortOption.getSortKey().getKey().equals(columnKey)) {
					sortKey = columnKey;
				}
			}
		}
		
		if(sortKey != null) {
			orderBy = new SortKey[]{ new SortKey(sortKey, tablePrefs.isSortDirection()) };
			selectSortOption(sortKey, tablePrefs.isSortDirection());
		}
	}
	
	private List<Integer> convertColumnKeysToIndex(Collection<String> columnKeys) {
		if(columnKeys == null) return new ArrayList<>(0);
		
		List<Integer> index = new ArrayList<>(columnKeys.size());
		FlexiTableColumnModel colModel = dataModel.getTableColumnModel();
		for(String columnKey:columnKeys) {
			for(int i=colModel.getColumnCount(); i-->0; ) {
				FlexiColumnModel col = colModel.getColumnModel(i);
				if(columnKey.equals(col.getColumnKey())) {
					index.add(Integer.valueOf(col.getColumnIndex()));
				}
			}
		}
		return index;
	}
	
	private List<String> convertColumnIndexToKeys(Collection<Integer> columnIndex) {
		List<String> keys = new ArrayList<>();
		FlexiTableColumnModel colModel = dataModel.getTableColumnModel();
		for(Integer columnId:columnIndex) {
			for(int i=colModel.getColumnCount(); i-->0; ) {
				FlexiColumnModel col = colModel.getColumnModel(i);
				if(columnId.intValue() == col.getColumnIndex()) {
					keys.add(col.getColumnKey());
				}
			}
		}
		return keys;
	}
	
	private Choice getColumnListAndTheirVisibility() {
		Choice choice = new Choice("colchoice", component.getTranslator());
		choice.setModel(new VisibleFlexiColumnsModel(dataModel.getTableColumnModel(), enabledColumnIndex, component.getTranslator()));
		choice.addListener(this);
		choice.setEscapeHtml(false);
		choice.setCancelKey("cancel");
		choice.setSubmitKey("save");
		choice.setResetKey("reset");
		choice.setElementCssClass("o_table_config");
		return choice;
	}

	protected void evalExtendedSearch(UserRequest ureq) {
		String search = null;
		if(searchFieldEl != null && searchFieldEl.isEnabled() && searchFieldEl.isVisible()) {
			searchFieldEl.evalFormRequest(ureq);
			search = searchFieldEl.getValue();
		}
		doSearch(ureq, FlexiTableReduceEvent.SEARCH, search, null);
	}
	
	protected void evalSearchRequest(UserRequest ureq) {
		if(searchFieldEl == null || !searchFieldEl.isEnabled() || !searchFieldEl.isVisible()){
			return;//this a default behavior which can occur without the search configured
		}
		searchFieldEl.evalFormRequest(ureq);
		
		String key = null;
		if(searchFieldEl instanceof AutoCompleter) {
			key = ((AutoCompleter)searchFieldEl).getKey();
		}
		String search = searchFieldEl.getValue();

		if(key != null) {
			doSearch(ureq, FlexiTableReduceEvent.QUICK_SEARCH_KEY_SELECTION, key, null);
		} else if(isSearchEnabled() && isSearchLarge()) {
			doSearch(ureq, FlexiTableReduceEvent.SEARCH, search, getFilters());
		} else if(StringHelper.containsNonWhitespace(search)) {
			doSearch(ureq, FlexiTableReduceEvent.QUICK_SEARCH, search, getFilters());
		} else {
			resetSearch(ureq);
		}
	}
	
	protected void resetQuickSearch(UserRequest ureq) {
		if(searchFieldEl instanceof AutoCompleter) {
			((AutoCompleter)searchFieldEl).setKey(null);
		}
		searchFieldEl.setValue("");
		resetSearch(ureq);
	}
	
	@Override
	public String getQuickSearchString() {
		if(searchFieldEl != null && searchFieldEl.isEnabled() && searchFieldEl.isVisible()){
			return searchFieldEl.getValue();
		}
		return null;
	}

	@Override
	public void quickSearch(UserRequest ureq, String search) {
		if(searchFieldEl == null || !searchFieldEl.isEnabled() || !searchFieldEl.isVisible()){
			return;//this a default behavior which can occur without the search configured
		}
		if(StringHelper.containsNonWhitespace(search)) {
			searchFieldEl.setValue(search);
			doSearch(ureq, FlexiTableReduceEvent.QUICK_SEARCH, search, null);
		}
	}
	
	private void updateSelectAllToggle() {
		if (multiSelect == SelectionMode.multi) {
			int count = dataModel.getRowCount();
			int selectCount = 0;
			if(dataModel instanceof FlexiTableSelectionDelegate) {
				selectCount = ((FlexiTableSelectionDelegate<?>)dataModel).getSelectedTreeNodes().size();
			} else if(multiSelectedIndex != null) {
				selectCount = multiSelectedIndex.size();
			}
			
			boolean showSelectAll = (selectCount == 0);
			boolean showDeselectAll = (count != 0 && count == selectCount);
			
			String selectedEntriesInfo;
			if(selectCount <= 1) {
				selectedEntriesInfo = translator.translate("number.selected.entry", Integer.toString(selectCount));
			} else {
				selectedEntriesInfo = translator.translate("number.selected.entries", Integer.toString(selectCount));
			}
			
			StringBuilder sb = new StringBuilder();
			sb.append("o_table_updateCheckAllMenu('")
			.append(getFormDispatchId())
			.append("',")
			.append(Boolean.toString(showSelectAll))
			.append(",")
			.append(Boolean.toString(showDeselectAll))
			.append(",'")
			.append(selectedEntriesInfo)
			.append("');");
			JSCommand updateSelectAllToggleCmd = new JSCommand(sb.toString());
			getRootForm().getWindowControl().getWindowBackOffice().sendCommandTo(updateSelectAllToggleCmd);
		}
	}
	
	@Override
	public void selectAll() {
		if(multiSelectedIndex != null) {
			multiSelectedIndex.clear();
		} else {
			multiSelectedIndex = new HashMap<>();
		}
		
		int numOfRows = getRowCount();
		for(int i=0; i<numOfRows;i++) {
			Object objectRow = dataModel.getObject(i);
			multiSelectedIndex.put(Integer.valueOf(i), objectRow);
		}
		allSelectedNeedLoadOfWholeModel = true;
		updateSelectAllToggle();
	}
	
	@Override
	public void selectPage() {
		if(multiSelectedIndex != null) {
			multiSelectedIndex.clear();
		} else {
			multiSelectedIndex = new HashMap<>();
		}
		
		int firstRow = getFirstRow();
		int maxRows = getMaxRows();
		int rows = dataModel.getRowCount();
		int lastRow = Math.min(rows, firstRow + maxRows);
		for (int i = firstRow; i < lastRow; i++) {
			Object objectRow = dataModel.getObject(i);
			multiSelectedIndex.put(Integer.valueOf(i), objectRow);
		}
		allSelectedNeedLoadOfWholeModel = false;
		updateSelectAllToggle();
	}
	
	protected void doUnSelectAll() {
		if(multiSelectedIndex != null) {
			multiSelectedIndex.clear();
		}
		allSelectedNeedLoadOfWholeModel = false;
		updateSelectAllToggle();
	}
	
	protected void doSelect(UserRequest ureq, int index) {
		getRootForm().fireFormEvent(ureq, new SelectionEvent(ROW_SELECT_EVENT, index, this, FormEvent.ONCLICK));
	}
	
	protected void doSelect(UserRequest ureq, String selectAction, int index) {
		getRootForm().fireFormEvent(ureq, new SelectionEvent(selectAction, index, this, FormEvent.ONCLICK));
	}
	
	protected void doSelectAllColumn(UserRequest ureq, int columnIndex) {
		getRootForm().fireFormEvent(ureq, new SelectAllColumnEvent(columnIndex, this));
	}
	
	protected void doUnSelectAllColumn(UserRequest ureq, int columnIndex) {
		getRootForm().fireFormEvent(ureq, new UnselectAllColumnEvent(columnIndex, this));
	}

	private void doSearch(UserRequest ureq, String eventCmd, String search, List<FlexiTableFilter> changedFilters) {

		List<FlexiTableFilter> selectedFilters = getSelectedFilters(changedFilters);
		
		if(dataSource != null) {
			currentPage = 0;
			resetInternComponents();
			if(dataModel instanceof FlexiTableDataSource) {
				((FlexiTableDataSource<?>)dataModel).clear();
				((FlexiTableDataSource<?>)dataModel).load(search, selectedFilters, 0, getPageSize(), orderBy);
			} else {
				dataSource.clear();
				dataSource.load(search, selectedFilters, 0, getPageSize(), orderBy);
			}
		}
		getRootForm().fireFormEvent(ureq, new FlexiTableSearchEvent(eventCmd, this,
				search, selectedFilters, FormEvent.ONCLICK));
		addToHistory(ureq);
	}
	
	private List<FlexiTableFilter> getSelectedFilters(List<FlexiTableFilter> changedFilters) {
		List<FlexiTableFilter> selectedFilters = getFilters();
		if(changedFilters != null) {
			Set<String> selectedfilterNames = selectedFilters.stream()
					.map(FlexiTableFilter::getFilter)
					.collect(Collectors.toSet());
			for(FlexiTableFilter changedFilter:changedFilters) {
				if(!selectedfilterNames.contains(changedFilter.getFilter())) {
					selectedFilters.add(changedFilter);
				}
			}	
		}
		return selectedFilters;
	}
	
	private void resetFiltersSearch(UserRequest ureq) {
		if(filtersEl != null) {
			filtersEl.resetCustomizedFilters();
		}
		if(filterTabsEl != null) {
			FlexiFiltersTab tab = filterTabsEl.getSelectedTab();
			if(tab != null && filtersEl != null) {
				filtersEl.setFiltersValues(tab.getEnabledFilters(), tab.getImplicitFilters(), tab.getDefaultFiltersValues(), true);
			}
			filterTabsEl.getComponent().setDirty(true);
		}
		
		if(filterTabsEl != null && filterTabsEl.getSelectedTab() != null
				&& filterTabsEl.getSelectedTab().getSelectionBehavior() == TabSelectionBehavior.clear
				&& !StringHelper.containsNonWhitespace(getSearchText())) {
			resetInternComponents();
			if(dataSource != null) {
				if(dataModel instanceof FlexiTableDataSource) {
					((FlexiTableDataSource<?>)dataModel).clear();
				} else {
					dataSource.clear();
				}
			} else {
				getRootForm().fireFormEvent(ureq, new FlexiTableSearchEvent(this, FormEvent.ONCLICK));
			}
		} else {
			doSearch(ureq, FlexiTableReduceEvent.FILTER, getSearchText(), getFilters());
		}
	}
	
	@Override
	public void resetSearch(UserRequest ureq) {
		currentPage = 0;
		if(dataSource != null) {
			resetInternComponents();
			boolean clear = filterTabsEl != null && filterTabsEl.getSelectedTab() != null
					&& filterTabsEl.getSelectedTab().getSelectionBehavior() == TabSelectionBehavior.clear;
			
			if(dataModel instanceof FlexiTableDataSource) {
				((FlexiTableDataSource<?>)dataModel).clear();
				if(!clear) {
					((FlexiTableDataSource<?>)dataModel).load(null, getFilters(), 0, getPageSize(), orderBy);
				}
			} else {
				dataSource.clear();
				if(!clear) {
					dataSource.load(null, null, 0, getPageSize(), orderBy);
				}
			}
		} else {
			getRootForm().fireFormEvent(ureq, new FlexiTableSearchEvent(this, FormEvent.ONCLICK));
		}
	}
	


	@Override
	public Set<Integer> getMultiSelectedIndex() {
		if(allSelectedNeedLoadOfWholeModel && dataSource != null) {
			//ensure the whole data model is loaded
			if(dataModel instanceof FlexiTableDataSource) {
				((FlexiTableDataSource<?>)dataModel).load(getSearchText(), getFilters(), 0, -1, orderBy);
			} else {
				dataSource.load(getSearchText(), getFilters(), 0, -1, orderBy);
			}
			
			Set<Integer> allIndex = new HashSet<>();
			for(int i=dataModel.getRowCount(); i-->0; ) {
				allIndex.add(Integer.valueOf(i));
			}
			allSelectedNeedLoadOfWholeModel = false;
			return allIndex;
		}
		return multiSelectedIndex == null ? Collections.<Integer>emptySet() : multiSelectedIndex.keySet();
	}

	@Override
	public void setMultiSelectedIndex(Set<Integer> set) {
		if(multiSelectedIndex == null) {
			multiSelectedIndex = new HashMap<>();
		} else {
			multiSelectedIndex.clear();
		}
		for(Integer index:set) {
			Object objectRow = dataModel.getObject(index.intValue());
			multiSelectedIndex.put(index, objectRow);
		}
	}

	@Override
	public boolean isMultiSelectedIndex(int index) {
		return multiSelectedIndex != null && multiSelectedIndex.containsKey(Integer.valueOf(index));
	}
	
	public boolean hasMultiSelectedIndex() {
		return allSelectedNeedLoadOfWholeModel || (multiSelectedIndex != null && !multiSelectedIndex.isEmpty());
	}
	
	public int getNumOfMultiSelectedIndex() {
		if(multiSelectedIndex != null) {
			return multiSelectedIndex.size();
		}
		if(allSelectedNeedLoadOfWholeModel) {
			return getRowCount();
		}
		return 0;
	}
	
	protected void toogleSelectIndex(UserRequest ureq, String selection) {
		if(multiSelectedIndex == null) {
			multiSelectedIndex = new HashMap<>();
		}

		String rowStr;
		int index = selection.lastIndexOf('-');
		if(index > 0 && index+1 < selection.length()) {
			rowStr = selection.substring(index+1);
		} else {
			rowStr = selection;
		}
		
		try {
			Integer row = Integer.valueOf(rowStr);
			if(multiSelectedIndex.containsKey(row)) {
				if(multiSelectedIndex.remove(row) != null && allSelectedNeedLoadOfWholeModel) {
					allSelectedNeedLoadOfWholeModel = false;
				}
				doSelect(ureq, ROW_UNCHECKED_EVENT, row.intValue());
			} else {
				Object objectRow = dataModel.getObject(row.intValue());
				if(multiSelect == SelectionMode.single) {
					multiSelectedIndex.clear();
				}
				multiSelectedIndex.put(row, objectRow);
				doSelect(ureq, ROW_CHECKED_EVENT, row.intValue());
			}
		} catch (NumberFormatException e) {
			//can happen
		}
		updateSelectAllToggle();
	}
	
	protected void toogleDetails(String rowIndex, UserRequest ureq) {
		if(detailsIndex == null) {
			detailsIndex = new HashSet<>();
		}
		
		try {
			Integer row = Integer.valueOf(rowIndex);
			if(detailsIndex.contains(row)) {
				detailsIndex.remove(row);
				if(detailsIndex.isEmpty()) {
					detailsIndex = null;
				}
				component.setDirty(true);
				getRootForm().fireFormEvent(ureq, new DetailsToggleEvent(this, row, false));
			} else {
				if (!multiDetails && !detailsIndex.isEmpty()) {
					detailsIndex.clear();
				}
				detailsIndex.add(row);
				component.setDirty(true);
				getRootForm().fireFormEvent(ureq, new DetailsToggleEvent(this, row, true));
			}
		} catch (NumberFormatException e) {
			//can happen
		}
	}

	@Override
	public boolean validate() {
		boolean allOk = true;
		if(searchFieldEl != null) {
			allOk &= searchFieldEl.validate();
		}
		if(searchButton != null) {
			allOk &= searchButton.validate();
		}
		if(customButton != null) {
			allOk &= customButton.validate();
		}
		if(settingsButton != null) {
			allOk &= settingsButton.validate();
		}
		if(extendedSearchButton != null) {
			allOk &= extendedSearchButton.validate();
		}
		return allOk;
	}

	@Override
	public void reset() {
		resetInternComponents();
		reloadData();
	}

	@Override
	public void reset(boolean page, boolean internal, boolean reloadData) {
		if(page) {
			currentPage = 0;
		}
		if(internal) {
			resetInternComponents();
		}
		if(reloadData) {
			reloadData();
		}
	}

	private void resetInternComponents() {
		rowCount = -1;
		component.setDirty(true);
		multiSelectedIndex = null;
		allSelectedNeedLoadOfWholeModel = false;
		detailsIndex = null;
	}
	
	@Override
	public void deselectAll() {
		component.setDirty(true);
		multiSelectedIndex = null;
		allSelectedNeedLoadOfWholeModel = false;
	}

	@Override
	public void reloadData() {
		if(dataSource != null) {
			int firstResult = currentPage * getPageSize();
			if(dataModel instanceof FlexiTableDataSource) {
				((FlexiTableDataSource<?>)dataModel).clear();
				((FlexiTableDataSource<?>)dataModel).load(getSearchText(), getFilters(), firstResult, getPageSize(), orderBy);//reload needed rows
			} else {
				dataSource.clear();
				dataSource.load(getSearchText(), getFilters(), firstResult, getPageSize(), orderBy);//reload needed rows
			}
		} else {
			if(dataModel instanceof FilterableFlexiTableModel) {
				if(isFilterEnabled()) {
					List<FlexiTableFilter> filter = getFilters();
					((FilterableFlexiTableModel)dataModel).filter(getQuickSearchString(), filter);
				}
			}
			
			if(dataModel instanceof SortableFlexiTableDataModel) {
				if(orderBy != null && orderBy.length > 0) {
					((SortableFlexiTableDataModel<?>)dataModel).sort(orderBy[0]);
				}
			}
			
			if(dataModel instanceof FlexiTreeTableDataModel) {
				crumbs = ((FlexiTreeTableDataModel<?>)dataModel).reloadBreadcrumbs(crumbs);
			}
		}

		component.setDirty(true);
	}
	
	/**
	 * Prevent parent to be set as dirty for every request
	 */
	@Override
	public boolean isInlineEditingElement() {
		return true;
	}

	@Override
	protected void rootFormAvailable() {
		rootFormAvailable(searchButton);
		rootFormAvailable(customButton);
		rootFormAvailable(settingsButton);
		rootFormAvailable(exportButton);
		rootFormAvailable(searchFieldEl);
		rootFormAvailable(extendedSearchButton);
		rootFormAvailable(customTypeButton);
		rootFormAvailable(classicTypeButton);
		rootFormAvailable(externalTypeButton);
		rootFormAvailable(emptyTablePrimaryActionButton);
		if(components != null) {
			for(FormItem item:components.values()) {
				rootFormAvailable(item);
			}
		}
	}
	
	private final void rootFormAvailable(FormItem item) {
		if(item != null && item.getRootForm() != getRootForm()) {
			item.setRootForm(getRootForm());
		}
	}

	@Override
	protected FlexiTableComponent getFormItemComponent() {
		return component;
	}
	
	public int getRowCount() {
		if(rowCount < 0) {
			rowCount = dataModel.getRowCount();
		}
		return rowCount;
	}
	
	public int getFirstRow() {
		if(getPageSize() > 0) {
			return getPage() * getPageSize();
		}
		return 0;
	}
	
	public int getMaxRows() {
		if(pageSize > 0) {
			return pageSize;
		}
		return getRowCount();
	}
	
	public FlexiTableDataModel<?> getTableDataModel() {
		return dataModel;
	}
	
	public FlexiTreeTableDataModel<?> getTreeTableDataModel() {
		return dataModel instanceof FlexiTreeTableDataModel ? (FlexiTreeTableDataModel<?>)dataModel : null;
	}
	
	public List<FlexiTreeTableNode> getCrumbs() {
		return crumbs == null ? Collections.emptyList() : crumbs;
	}
	
	public FlexiTableDataSource<?> getTableDataSource() {
		return dataSource;
	}

	@Override
	public void setEmptyTableMessageKey(String i18key) {
		setEmptyTableSettings(i18key, null, TABLE_EMPTY_ICON);
	}

	@Override
	public void setEmptyTableSettings(String emptyMessagei18key, String emptyTableHintKey, String emptyTableIconCss) {
		setEmptyTableSettings(emptyMessagei18key, emptyTableHintKey, emptyTableIconCss, null, null, true);
	}
	
	@Override
	public void setEmptyTableSettings(String emptyMessagei18key, String emptyTableHintKey, String emptyTableIconCss, String emptyPrimaryActionKey, String emptyPrimaryActionIconCSS, boolean showAlwaysSearchFields) {
		this.emptyTableMessageKey = emptyMessagei18key;
		this.emptyTableHintKey = emptyTableHintKey;
		this.emptyTableIconCss = emptyTableIconCss;
		// create action button
		if (emptyPrimaryActionKey != null) {
			String dispatchId = component.getDispatchID();
			emptyTablePrimaryActionButton = new FormLinkImpl(dispatchId + "_emptyTablePrimaryActionButton", "rEmtpyTablePrimaryActionButton", emptyPrimaryActionKey, Link.BUTTON);
			emptyTablePrimaryActionButton.setTranslator(translator);
			emptyTablePrimaryActionButton.setPrimary(true);
			if (emptyPrimaryActionIconCSS != null) {
				emptyTablePrimaryActionButton.setIconLeftCSS("o_icon o_icon-fw " + emptyPrimaryActionIconCSS);
			}
			emptyTablePrimaryActionButton.setAriaLabel(emptyPrimaryActionKey);
			components.put("rEmtpyTablePrimaryActionButton", emptyTablePrimaryActionButton);			
			if(getRootForm() != null) {
				rootFormAvailable(emptyTablePrimaryActionButton);
			}		
		} else if (emptyTablePrimaryActionButton != null) {
			emptyTablePrimaryActionButton = null;
		}
		// search filed config
		this.emptyShowSearch = showAlwaysSearchFields;
	}

	
	public boolean isShowAlwaysSearchFields() {
		return emptyShowSearch;
	}
	
	public String getEmtpyTableMessageKey() {
		return emptyTableMessageKey;
	}
	public String getEmptyTableHintKey() {
		return emptyTableHintKey;
	}
	public String getEmtpyTableIconCss() {
		return emptyTableIconCss;
	}

	@Override
	public void dispose() {
		for (FormItem formItem : getFormItems()) {
			if (formItem instanceof Disposable) {
				Disposable disposableFormItem = (Disposable) formItem;
				disposableFormItem.dispose();				
			}
		}
	}
	
	public enum SelectionMode {
		
		multi,
		single,
		disabled
		
	}
}