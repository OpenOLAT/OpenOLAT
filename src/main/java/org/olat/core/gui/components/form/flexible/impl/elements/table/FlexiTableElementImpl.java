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
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.SingleSelectionImpl;
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
	private boolean customizeColumns = true;
	
	private int rowCount = -1;
	
	private int currentPage;
	private int currentFirstResult;
	private int pageSize;
	private boolean editMode;
	private boolean exportEnabled;
	private boolean searchEnabled;
	private boolean selectAllEnabled;
	private int columnLabelForDragAndDrop;
	
	private VelocityContainer rowRenderer;

	private FormLink customButton, exportButton;
	private FormLink searchButton, extendedSearchButton;
	private SingleSelection filterEl;
	private TextElement searchFieldEl;
	private ExtendedFlexiTableSearchController extendedSearchCtrl;
	
	private final FlexiTableDataModel<?> dataModel;
	private final FlexiTableDataSource<?> dataSource;
	private final FlexiTableComponent component;
	private CloseableCalloutWindowController callout;
	private final WindowControl wControl;
	private final String mapperUrl;
	
	private String wrapperSelector;

	private SortKey[] orderBy;
	private Object selectedObj;
	private boolean allSelectedIndex;
	private Set<Integer> multiSelectedIndex;
	private List<String> conditionalQueries;
	private Set<Integer> enabledColumnIndex = new HashSet<Integer>();
	
	private Map<String,FormItem> components = new HashMap<String,FormItem>();
	
	public FlexiTableElementImpl(UserRequest ureq, WindowControl wControl, String name, FlexiTableDataModel<?> tableModel) {
		this(ureq, wControl, name, null, tableModel, -1);
	}
	
	public FlexiTableElementImpl(UserRequest ureq, WindowControl wControl, String name, Translator translator, FlexiTableDataModel<?> tableModel) {
		this(ureq, wControl, name, translator, tableModel, -1);
	}
	
	public FlexiTableElementImpl(UserRequest ureq, WindowControl wControl, String name, Translator translator,
			FlexiTableDataModel<?> tableModel, int pageSize) {
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
		customButton = new FormLinkImpl(dispatchId + "_customButton", "rCustomButton", "search", Link.BUTTON);
		customButton.setTranslator(translator);
		customButton.setCustomEnabledLinkCSS("b_with_small_icon_only b_table_prefs");
		components.put("rCustomize", customButton);

		this.pageSize = pageSize;
		if(pageSize > 0) {
			setPage(0);
		}
		
		if(dataSource != null) {
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
		this.rendererType = rendererType;
		if(component != null) {
			component.setDirty(true);
		}
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

	public VelocityContainer getRowRenderer() {
		return rowRenderer;
	}

	public void setRowRenderer(VelocityContainer rowRenderer) {
		this.rowRenderer = rowRenderer;
	}

	@Override
	public boolean isFilterEnabled() {
		return filterEl != null;
	}
	
	public String getSelectedFilterKey() {
		if(filterEl != null && filterEl.isOneSelected()) {
			return filterEl.getSelectedKey();
		}
		return null;
	}
	
	public String getSelectedFilterValue() {
		if(filterEl != null && filterEl.isOneSelected()) {
			return filterEl.getSelectedValue();
		}
		return null;
	}

	@Override
	public void setFilterKeysAndValues(String labelI18nKey, String[] keys, String[] values) {
		if(keys == null || keys.length == 0) {
			components.remove("rFilter");
			filterEl = null;
		} else {
			String dispatchId = component.getDispatchID();
			String name = dispatchId + "_filter";
			filterEl = new SingleSelectionImpl(name, name, SingleSelectionImpl.createSelectboxLayouter(name, name));
			filterEl.setTranslator(getTranslator());
			if(StringHelper.containsNonWhitespace(labelI18nKey)) {
				filterEl.setLabel(labelI18nKey, null);
				filterEl.showLabel(true);
			}
			filterEl.addActionListener(FormEvent.ONCHANGE);
			filterEl.setKeysAndValues(keys, values, null);
			components.put("rFilter", filterEl);
			rootFormAvailable(filterEl);
		}
	}
	
	public SingleSelection getFilterSelection() {
		return filterEl;
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
			exportButton = new FormLinkImpl(dispatchId + "_exportButton", "rExportButton", "export", Link.BUTTON);
			exportButton.setTranslator(translator);
			exportButton.setCustomEnabledLinkCSS("b_with_small_icon_only b_table_download");
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
			searchButton.setCustomEnabledLinkCSS("b_with_small_icon_right b_with_small_icon_only o_fulltext_search_button");
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
	public void setExtendedSearchCallout(ExtendedFlexiTableSearchController callout) {
		extendedSearchCtrl = callout;
		if(extendedSearchCtrl != null) {
			extendedSearchCtrl.addControllerListener(this);
			
			String dispatchId = component.getDispatchID();
			extendedSearchButton = new FormLinkImpl(dispatchId + "_extSearchButton", "rExtSearchButton", "extsearch", Link.BUTTON);
			extendedSearchButton.setTranslator(translator);
			//extendedSearchButton.setCustomEnabledLinkCSS("b_with_small_icon_right b_with_small_icon_only o_fulltext_search_button");
			components.put("rExtSearchB", extendedSearchButton);
			rootFormAvailable(extendedSearchButton);
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
			openExtendedSearch(ureq);
		} else if(filterEl != null
				&& filterEl.getFormDispatchId().equals(dispatchuri)) {
			filterEl.evalFormRequest(ureq);
			filter();
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
				callout.deactivate();
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
			component.setDirty(true);
		} else if(dataSource != null) {
			dataSource.load(null, conditionalQueries, 0, getPageSize(), orderBy);
		}
	}
	
	protected void filter() {
		if(filterEl.isOneSelected()) {
			String filter = filterEl.getSelectedKey();
			if(dataModel instanceof FilterableFlexiTableModel) {
				((FilterableFlexiTableModel)dataModel).filter(filter);
			} else if(dataSource != null) {
				List<String> conditionalQueries = Collections.singletonList(filter);
				dataSource.load(null, conditionalQueries, 0, getPageSize(), orderBy);
			}
		}
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
	
	protected void openExtendedSearch(UserRequest ureq) {
		callout = new CloseableCalloutWindowController(ureq, wControl, extendedSearchCtrl.getInitialComponent(),
				extendedSearchButton, "Search", true, "o_sel_flexi_search_callout");
		callout.activate();
		callout.addControllerListener(this);
	}
	
	@Override
	public void closeExtendedSearch() {
		if(callout != null) {
			callout.deactivate();
		}
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
				if(filterEl != null && filterEl.isOneSelected()) {
					String filter = filterEl.getSelectedKey();
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
		rootFormAvailable(filterEl);
		rootFormAvailable(searchButton);
		rootFormAvailable(customButton);
		rootFormAvailable(exportButton);
		rootFormAvailable(searchFieldEl);
		rootFormAvailable(extendedSearchButton);
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
