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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.choice.Choice;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSort;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.TextElementImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ValidationStatus;


/**
 * 
 * @author Christian Guretzki
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class FlexiTableElementImpl extends FormItemImpl implements FlexiTableElement, FormItemCollection,
	ControllerEventListener, ComponentEventListener {

	//settings
	private boolean multiSelect;
	private FlexiTableRendererType rendererType = FlexiTableRendererType.classic;
	private FlexiTableRendererType[] availableRendererType = new FlexiTableRendererType[] {
		FlexiTableRendererType.classic
	};
	
	private boolean customizeColumns = true;
	
	private int rowCount = -1;
	
	private int currentPage;
	private int currentFirstResult;
	private int pageSize;
	private boolean editMode;
	private boolean exportEnabled;
	private boolean searchEnabled;
	private boolean selectAllEnabled;
	private boolean extendedSearchExpanded = false;
	private boolean extendedSearchCallout;
	private int columnLabelForDragAndDrop;
	
	private VelocityContainer rowRenderer;

	private FormLink customButton, exportButton;
	private FormLink searchButton, extendedSearchButton;
	private FormLink classicTypeButton, customTypeButton, dataTablesTypeButton;
	private TextElement searchFieldEl;
	private ExtendedFlexiTableSearchController extendedSearchCtrl;
	
	private final FlexiTableDataModel<?> dataModel;
	private final FlexiTableDataSource<?> dataSource;
	private final FlexiTableComponent component;
	private FlexiTableComponentDelegate componentDelegate;
	private CloseableCalloutWindowController callout;
	private final WindowControl wControl;
	private final String mapperUrl;
	
	private String wrapperSelector;

	private SortKey[] orderBy;
	private List<FlexiTableSort> sorts;
	private List<FlexiTableFilter> filters;
	private Object selectedObj;
	private boolean allSelectedIndex;
	private Set<Integer> multiSelectedIndex;
	private List<String> conditionalQueries;
	private Set<Integer> enabledColumnIndex = new HashSet<Integer>();
	
	private Map<String,FormItem> components = new HashMap<String,FormItem>();
	
	public FlexiTableElementImpl(UserRequest ureq, WindowControl wControl, String name, FlexiTableDataModel<?> tableModel) {
		this(ureq, wControl, name, null, tableModel, -1, true);
	}
	
	public FlexiTableElementImpl(UserRequest ureq, WindowControl wControl, String name, Translator translator, FlexiTableDataModel<?> tableModel) {
		this(ureq, wControl, name, translator, tableModel, -1, true);
	}
	
	public FlexiTableElementImpl(UserRequest ureq, WindowControl wControl, String name, Translator translator,
			FlexiTableDataModel<?> tableModel, int pageSize, boolean loadOnStart) {
		super(name);
		this.wControl = wControl;
		this.dataModel = tableModel;
		this.dataSource = (tableModel instanceof FlexiTableDataSource) ? (FlexiTableDataSource<?>)dataModel : null;
		component = new FlexiTableComponent(this, translator);
		
		for(int i=dataModel.getTableColumnModel().getColumnCount(); i-->0; ) {
			FlexiColumnModel col = dataModel.getTableColumnModel().getColumnModel(i);
			if(col.isDefaultVisible()) {
				enabledColumnIndex.add(new Integer(col.getColumnIndex()));
			}
		}

		MapperService mapper = CoreSpringFactory.getImpl(MapperService.class);
		mapperUrl = mapper.register(ureq.getUserSession(), new FlexiTableModelMapper(component));

		String dispatchId = component.getDispatchID();
		customButton = new FormLinkImpl(dispatchId + "_customButton", "rCustomButton", "", Link.BUTTON + Link.NONTRANSLATED);
		customButton.setTranslator(translator);
		customButton.setIconLeftCSS("o_icon o_icon_customize");
		components.put("rCustomize", customButton);

		this.pageSize = pageSize;
		if(pageSize > 0) {
			setPage(0);
		}
		
		if(dataSource != null && loadOnStart) {
			//preload it
			dataSource.load(null, null, 0, pageSize);
		}
	}

	@Override
	public int getColumnLabelForDragAndDrop() {
		return columnLabelForDragAndDrop;
	}

	@Override
	public void setColumnLabelForDragAndDrop(int columnLabelForDragAndDrop) {
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
		if(dataTablesTypeButton != null) {
			dataTablesTypeButton.setActive(FlexiTableRendererType.dataTables == rendererType);
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
			customTypeButton.setIconLeftCSS("o_icon o_icon_list o_icon-lg");
			customTypeButton.setActive(FlexiTableRendererType.custom == rendererType);
			components.put("rTypeCustom", customTypeButton);
			//classic tables
			classicTypeButton = new FormLinkImpl(dispatchId + "_classicRTypeButton", "rClassicRTypeButton", "", Link.BUTTON + Link.NONTRANSLATED);
			classicTypeButton.setTranslator(translator);
			classicTypeButton.setIconLeftCSS("o_icon o_icon_table o_icon-lg");
			classicTypeButton.setActive(FlexiTableRendererType.classic == rendererType);
			components.put("rTypeClassic", classicTypeButton);
			//jquery tables
			dataTablesTypeButton = new FormLinkImpl(dispatchId + "_dataTablesRTypeButton", "rDataTablesRTypeButton", "", Link.BUTTON + Link.NONTRANSLATED);
			dataTablesTypeButton.setTranslator(translator);
			dataTablesTypeButton.setIconLeftCSS("o_icon o_icon_table o_icon-lg");
			dataTablesTypeButton.setActive(FlexiTableRendererType.dataTables == rendererType);
			components.put("rTypeDataTables", dataTablesTypeButton);
			
			if(getRootForm() != null) {
				rootFormAvailable(customTypeButton);
				rootFormAvailable(classicTypeButton);
				rootFormAvailable(dataTablesTypeButton);
			}
		}
	}

	public FormLink getClassicTypeButton() {
		return classicTypeButton;
	}

	public FormLink getCustomTypeButton() {
		return customTypeButton;
	}

	public FormLink getDataTablesTypeButton() {
		return dataTablesTypeButton;
	}

	@Override
	public boolean isMultiSelect() {
		return multiSelect;
	}
	
	@Override
	public void setMultiSelect(boolean multiSelect) {
		this.multiSelect = multiSelect;
	}

	public boolean isCustomizeColumns() {
		return customizeColumns;
	}

	public void setCustomizeColumns(boolean customizeColumns) {
		this.customizeColumns = customizeColumns;
	}

	public String getWrapperSelector() {
		return wrapperSelector;
	}

	public void setWrapperSelector(String wrapperSelector) {
		this.wrapperSelector = wrapperSelector;
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

	@Override
	public boolean isFilterEnabled() {
		return filters != null && filters.size() > 0;
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
	
	public List<FlexiTableFilter> getFilters() {
		return filters;
	}

	@Override
	public void setFilters(String name, List<FlexiTableFilter> filters) {
		this.filters = new ArrayList<>(filters);
	}
	
	public boolean isSortEnabled() {
		return sorts != null && sorts.size() > 0;
	}
	
	public List<FlexiTableSort> getSorts() {
		return sorts;
	}

	@Override
	public void setSorts(String label, List<FlexiTableSort> sorts) {
		this.sorts = sorts;
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

	@Override
	public void setSearchEnabled(boolean enable) {
		this.searchEnabled = enable;
		if(searchEnabled) {
			String dispatchId = component.getDispatchID();
			searchFieldEl = new TextElementImpl(dispatchId + "_searchField", "search", "");
			searchFieldEl.showLabel(false);
			components.put("rSearch", searchFieldEl);
			searchButton = new FormLinkImpl(dispatchId + "_searchButton", "rSearchButton", "search", Link.BUTTON);
			searchButton.setTranslator(translator);
			searchButton.setIconLeftCSS("o_icon o_icon_search");
			components.put("rSearchB", searchButton);
			rootFormAvailable(searchFieldEl);
			rootFormAvailable(searchButton);
		} else {
			components.remove("rSearch");
			components.remove("rSearchB");
			searchFieldEl = null;
			searchButton = null;
		}
	}
	
	public FormLink getExtendedSearchButton() {
		return extendedSearchButton;
	}
	
	@Override
	public boolean isExtendedSearchExpanded() {
		return extendedSearchExpanded;
	}
	
	public boolean isExtendedSearchCallout() {
		return extendedSearchCallout;
	}
	
	public Component getExtendedSearchComponent() {
		return (extendedSearchCtrl == null) ? null : extendedSearchCtrl.getInitialComponent();
	}
	
	@Override
	public void setExtendedSearch(ExtendedFlexiTableSearchController controller, boolean callout) {
		extendedSearchCtrl = controller;
		extendedSearchCallout = callout;
		if(extendedSearchCtrl != null) {
			extendedSearchCtrl.addControllerListener(this);
			
			String dispatchId = component.getDispatchID();
			extendedSearchButton = new FormLinkImpl(dispatchId + "_extSearchButton", "rExtSearchButton", "extsearch", Link.BUTTON);
			extendedSearchButton.setTranslator(translator);
			extendedSearchButton.setIconLeftCSS("o_icon o_icon_search");
			components.put("rExtSearchB", extendedSearchButton);
			rootFormAvailable(extendedSearchButton);
			
			if(!callout) {
				components.put("rExtSearchCmp", controller.getInitialFormItem());
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

	public String getSearchText() {
		return searchFieldEl == null ? null : searchFieldEl.getValue();
	}
	
	public List<String> getConditionalQueries() {
		return conditionalQueries;
	}
	
	public SortKey[] getOrderBy() {
		return orderBy;
	}

	public TextElement getSearchElement() {
		return searchFieldEl;
	}
	
	public FormItem getSearchButton() {
		return searchButton;
	}
	
	public FormItem getCustomButton() {
		return customButton;
	}

	public Object getSelectedObj() {
		return selectedObj;
	}

	public void setSelectedObj(Object selectedObj) {
		this.selectedObj = selectedObj;
	}

	@Override
	public int getPageSize() {
		return pageSize;
	}

	@Override
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	
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
			dataSource.load(getSearchText(), getConditionalQueries(), firstResult, maxResults, orderBy);
		}
		component.setDirty(true);
	}

	public int getCurrentFirstResult() {
		return currentFirstResult;
	}

	public void setCurrentFirstResult(int currentFirstResult) {
		this.currentFirstResult = currentFirstResult;
	}

	public String getMapperUrl() {
		return mapperUrl;
	}

	@Override
	public Iterable<FormItem> getFormItems() {
		return components.values();
	}

	@Override
	public FormItem getFormComponent(String name) {
		return components.get(name);
	}

	public void addFormItem(FormItem item) {
		components.put(item.getName(), item);
	}

	@Override
	public void doDispatchFormRequest(UserRequest ureq) {
		super.doDispatchFormRequest(ureq);
	}
	
	@Override
	protected void dispatchFormRequest(UserRequest ureq) {
		super.dispatchFormRequest(ureq);
	}
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.FormItemImpl#evalFormRequest(org.olat.core.gui.UserRequest)
	 */
	@Override
	public void evalFormRequest(UserRequest ureq) {
		String[] selectedIndexArr = getRootForm().getRequestParameterValues("tb_ms");
		if(selectedIndexArr != null) {
			setMultiSelectIndex(selectedIndexArr);
		}

		Form form = getRootForm();
		String selectedIndex = form.getRequestParameter("rSelect");
		String dispatchuri = form.getRequestParameter("dispatchuri");
		String select = form.getRequestParameter("select");
		String page = form.getRequestParameter("page");
		String sort = form.getRequestParameter("sort");
		String filter = form.getRequestParameter("filter");
		if("undefined".equals(dispatchuri)) {
			evalSearchRequest(ureq);
		} else if(StringHelper.containsNonWhitespace(page)) {
			int p = Integer.parseInt(page);
			setPage(p);
		} else if(StringHelper.containsNonWhitespace(sort)) {
			String asc = form.getRequestParameter("asc");
			sort(sort, "asc".equals(asc));
		} else if(StringHelper.containsNonWhitespace(selectedIndex)) {
			int index = selectedIndex.lastIndexOf('-');
			if(index > 0 && index+1 < selectedIndex.length()) {
				String pos = selectedIndex.substring(index+1);
				int selectedPosition = Integer.parseInt(pos);
				selectedObj = dataModel.getObject(selectedPosition);
				doSelect(ureq, selectedPosition);
			}
		} else if(searchButton != null
				&& searchButton.getFormDispatchId().equals(dispatchuri)) {
			evalSearchRequest(ureq);
		} else if(extendedSearchButton != null
				&& extendedSearchButton.getFormDispatchId().equals(dispatchuri)) {
			expandExtendedSearch(ureq);
		} else if(dispatchuri != null && StringHelper.containsNonWhitespace(filter)) {
			filter(filter);
		} else if(exportButton != null
				&& exportButton.getFormDispatchId().equals(dispatchuri)) {
			export(ureq);
		} else if(dispatchuri != null && select != null && select.equals("checkall")) {
			doSelectAll();
		} else if(dispatchuri != null && select != null && select.equals("uncheckall")) {
			doUnSelectAll();
		} else if(customButton != null
				&& customButton.getFormDispatchId().equals(dispatchuri)) {
			//snap the request
			customizeCallout(ureq);
		} else if(customTypeButton != null
				&& customTypeButton.getFormDispatchId().equals(dispatchuri)) {
			setRendererType(FlexiTableRendererType.custom);
		} else if(classicTypeButton != null
				&& classicTypeButton.getFormDispatchId().equals(dispatchuri)) {
			setRendererType(FlexiTableRendererType.classic);
		} else if(dataTablesTypeButton != null
				&& dataTablesTypeButton.getFormDispatchId().equals(dispatchuri)) {
			setRendererType(FlexiTableRendererType.dataTables);
		} else {
			FlexiTableColumnModel colModel = dataModel.getTableColumnModel();
			for(int i=colModel.getColumnCount(); i-->0; ) {
				FlexiColumnModel col = colModel.getColumnModel(i);
				if(col.getAction() != null) {
					String selectedRowIndex = getRootForm().getRequestParameter(col.getAction());
					if(StringHelper.containsNonWhitespace(selectedRowIndex)) {
						doSelect(ureq, col.getAction(), Integer.parseInt(selectedRowIndex));
					}
				}
			}
		}
	}
	
	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		if(source == callout) {
			//System.out.println("dispatchEvent (Controller): " + source);
		} else if(source == extendedSearchCtrl) {
			if(event == Event.CANCELLED_EVENT) {
				if(callout != null) {
					callout.deactivate();
				} else {
					collapseExtendedSearch();
				}
			} else if(event == Event.DONE_EVENT) {
				evalExtendedSearch(ureq);
			}
		}
	}
	
	@Override
	public void dispatchEvent(UserRequest ureq, Component source, Event event) {
		if(source instanceof Choice) {
			Choice visibleColsChoice = (Choice)source;
			setCustomizedColumns(visibleColsChoice);
			callout.deactivate();
		}
	}
	
	protected void sort(String sortKey, boolean asc) {
		SortKey key = new SortKey(sortKey, asc);
		orderBy = new SortKey[]{ key };
		if(dataModel instanceof SortableFlexiTableDataModel) {
			((SortableFlexiTableDataModel<?>)dataModel).sort(key);
		} else if(dataSource != null) {
			dataSource.clear();
			dataSource.load(null, conditionalQueries, 0, getPageSize(), orderBy);
		}

		if(sorts != null) {
			for(FlexiTableSort sort:sorts) {
				boolean selected = sort.getSortKey().getKey().equals(sortKey);
				sort.setSelected(selected);
				if(selected) {
					sort.getSortKey().setAsc(asc);
				} else {
					sort.getSortKey().setAsc(false);
				}
			}
		}
		
		if(rendererType != FlexiTableRendererType.dataTables) {
			component.setDirty(true);
		}
	}
	
	protected void filter(String filterKey) {
		if(filterKey == null) {
			for(FlexiTableFilter filter:filters) {
				filter.setSelected(false);
			}
		} else {
			for(FlexiTableFilter filter:filters) {
				filter.setSelected(filter.getFilter().equals(filterKey));
			}
		}
		
		if(dataModel instanceof FilterableFlexiTableModel) {
			((FilterableFlexiTableModel)dataModel).filter(filterKey);
		} else if(dataSource != null) {
			List<String> conditionalQueries = Collections.singletonList(filterKey);
			dataSource.clear();
			dataSource.load(null, conditionalQueries, 0, getPageSize(), orderBy);
		}
		component.setDirty(true);
	}
	
	protected void export(UserRequest ureq) {
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
		if(extendedSearchCallout) {
			callout = new CloseableCalloutWindowController(ureq, wControl, extendedSearchCtrl.getInitialComponent(),
				extendedSearchButton, getTranslator().translate("search"), true, "o_sel_flexi_search_callout");
			callout.activate();
			callout.addControllerListener(this);
		} else {
			component.setDirty(true);
		}
		extendedSearchExpanded = true;
	}
	
	@Override
	public void collapseExtendedSearch() {
		if(callout != null) {
			callout.deactivate();
		}
		extendedSearchExpanded = false;
	}

	protected void customizeCallout(UserRequest ureq) {
		Choice choice = getColumnListAndTheirVisibility();
		callout = new CloseableCalloutWindowController(ureq, wControl, choice,
				customButton, "Customize", true, "o_sel_flexi_custom_callout");
		callout.activate();
		callout.addControllerListener(this);
	}

	@Override
	public boolean isColumnModelVisible(FlexiColumnModel col) {
		return enabledColumnIndex.contains(col.getColumnIndex());
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
	
	protected void setCustomizedColumns(Choice visibleColsChoice) {
		List<Integer> visibleCols = visibleColsChoice.getSelectedRows();
		if(visibleCols.size() > 1) {
			VisibleFlexiColumnsModel model = (VisibleFlexiColumnsModel)visibleColsChoice.getTableDataModel();
			for(int i=model.getRowCount(); i-->0; ) {
				FlexiColumnModel col = model.getObject(i);
				if(visibleCols.contains(new Integer(i))) {
					enabledColumnIndex.add(col.getColumnIndex());
				} else {
					enabledColumnIndex.remove(col.getColumnIndex());
				}
			}
		}
		component.setDirty(true);
	}
	
	private Choice getColumnListAndTheirVisibility() {
		Choice choice = new Choice("colchoice", getTranslator());
		choice.setTableDataModel(new VisibleFlexiColumnsModel(dataModel.getTableColumnModel(), enabledColumnIndex, getTranslator()));
		choice.addListener(this);
		choice.setCancelKey("cancel");
		choice.setSubmitKey("save");
		return choice;
	}

	protected void evalExtendedSearch(UserRequest ureq) {
		String search = null;
		if(searchFieldEl != null) {
			searchFieldEl.evalFormRequest(ureq);
			search = searchFieldEl.getValue();
		}
		List<String> condQueries = extendedSearchCtrl.getConditionalQueries();
		doSearch(search, condQueries);
	}

	protected void evalSearchRequest(UserRequest ureq) {
		if(searchFieldEl == null) return;//this a default behavior which can occur without the search configured
		searchFieldEl.evalFormRequest(ureq);
		String search = searchFieldEl.getValue();
		if(StringHelper.containsNonWhitespace(search)) {
			doSearch(search, null);
		} else {
			doResetSearch();
		}
	}
	
	protected void doSelectAll() {
		allSelectedIndex = true;
		if(multiSelectedIndex != null) {
			multiSelectedIndex.clear();
		}
	}
	
	protected void doUnSelectAll() {
		allSelectedIndex = false;
		if(multiSelectedIndex != null) {
			multiSelectedIndex.clear();
		}
	}
	
	protected void doSelect(UserRequest ureq, int index) {
		getRootForm().fireFormEvent(ureq, new SelectionEvent(ROM_SELECT_EVENT, index, this, FormEvent.ONCLICK));
	}
	
	protected void doSelect(UserRequest ureq, String action, int index) {
		getRootForm().fireFormEvent(ureq, new SelectionEvent(action, index, this, FormEvent.ONCLICK));
	}

	
	protected void doSearch(String search, List<String> condQueries) {
		if(condQueries == null || condQueries.isEmpty()) {
			conditionalQueries = null;
		} else {
			conditionalQueries = new ArrayList<String>(condQueries);
		}
		
		if(dataSource != null) {
			resetInternComponents();
			dataSource.clear();
			dataSource.load(search, conditionalQueries, 0, getPageSize(), orderBy);
		}
	}
	
	protected ResultInfos<?> doScroll(int firstResult, int maxResults, SortKey... sortKeys) {
		boolean same = isOrderByEqual(sortKeys);
		if(!same) {
			//clear data source
			dataSource.clear();
			orderBy = sortKeys;
		}
		
		return dataSource.load(getSearchText(), getConditionalQueries(), firstResult, maxResults, sortKeys);
	}
	
	private boolean isOrderByEqual(SortKey... sortKeys) {
		if(orderBy == null &&
				(sortKeys == null || sortKeys.length == 0 ||
					(sortKeys.length == 1 && sortKeys[0] == null))) {
			return true;
		}
		return Arrays.equals(orderBy , sortKeys);
	}
	
	protected void doResetSearch() {
		if(dataSource != null) {
			resetInternComponents();
			dataSource.clear();
			dataSource.load(null, null, 0, getPageSize());
		}
	}

	@Override
	public boolean isAllSelectedIndex() {
		return allSelectedIndex;
	}

	public void setAllSelectedIndex(boolean allSelectedIndex) {
		this.allSelectedIndex = allSelectedIndex;
	}

	@Override
	public Set<Integer> getMultiSelectedIndex() {
		if(allSelectedIndex && dataSource != null) {
			//ensure the whole data model is loaded
			dataSource.load(getSearchText(), getConditionalQueries(), 0, -1);
			Set<Integer> allIndex = new HashSet<Integer>();
			for(int i=dataModel.getRowCount(); i-->0; ) {
				allIndex.add(new Integer(i));
			}
			return allIndex;
		}
		return multiSelectedIndex == null ? Collections.<Integer>emptySet() : multiSelectedIndex;
	}

	@Override
	public boolean isMultiSelectedIndex(int index) {
		return allSelectedIndex
				|| (multiSelectedIndex != null && multiSelectedIndex.contains(new Integer(index)));
	}
	
	protected void setMultiSelectIndex(String[] selections) {
		if(multiSelectedIndex == null) {
			multiSelectedIndex = new HashSet<Integer>();
		}
		multiSelectedIndex.clear();
		//selection format row_{formDispId}-{index}
		if(selections != null && selections.length > 0) {
			for(String selection:selections) {	
				int index = selection.lastIndexOf('-');
				if(index > 0 && index+1 < selection.length()) {
					String rowStr = selection.substring(index+1);
					int row = Integer.parseInt(rowStr);
					multiSelectedIndex.add(new Integer(row));
				}
			}
		}
		
		// allSelectedIndex is a flag which is not updated if someone
		// deselect a row. check if the num of selected rows is equal
		// to the number of loaded rows
		if(allSelectedIndex) {
			int manuallySelectedRows = multiSelectedIndex.size();
			int modelCount = dataModel.getRowCount();
			int loadedRows = 0;
			for(int i=0; i<modelCount; i++) {
				if(dataModel.isRowLoaded(i)) {
					loadedRows++;
				}
			}
			
			if(manuallySelectedRows != loadedRows) {
				allSelectedIndex = false;
			}
		}
	}

	@Override
	public void validate(List<ValidationStatus> validationResults) {
		if(searchFieldEl != null) searchFieldEl.validate(validationResults);
		if(searchButton != null) searchButton.validate(validationResults);
		if(customButton != null) customButton.validate(validationResults);
		if(extendedSearchButton != null) extendedSearchButton.validate(validationResults);
	}

	@Override
	public void reset() {
		resetInternComponents();
		reloadData();
	}
	
	private void resetInternComponents() {
		rowCount = -1;
		component.setDirty(true);
		multiSelectedIndex = null;
	}

	@Override
	public void reloadData() {
		if(dataSource != null) {
			dataSource.clear();
			dataSource.load(getSearchText(), getConditionalQueries(), 0, getPageSize());//reload needed rows
		} else {
			if(dataModel instanceof FilterableFlexiTableModel) {
				if(isFilterEnabled()) {
					String filter = getSelectedFilterKey();
					((FilterableFlexiTableModel)dataModel).filter(filter);
				}
			}
			
			if(dataModel instanceof SortableFlexiTableDataModel) {
				if(orderBy != null && orderBy.length > 0) {
					((SortableFlexiTableDataModel<?>)dataModel).sort(orderBy[0]);
				}
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
		rootFormAvailable(exportButton);
		rootFormAvailable(searchFieldEl);
		rootFormAvailable(extendedSearchButton);
		rootFormAvailable(customTypeButton);
		rootFormAvailable(classicTypeButton);
		rootFormAvailable(dataTablesTypeButton);
	}
	
	private final void rootFormAvailable(FormItem item) {
		if(item != null && item.getRootForm() != getRootForm())
			item.setRootForm(getRootForm());
	}

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
	
	public FlexiTableDataSource<?> getTableDataSource() {
		return dataSource;
	}
}