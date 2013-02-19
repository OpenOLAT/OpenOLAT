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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.ValidationStatus;


/**
 * 
 * @author Christian Guretzki
 */
public class FlexiTableElementImpl extends FormItemImpl implements FlexiTableElement, FormItemCollection {

	//settings
	private boolean multiSelect;
	
	private int rowCount = -1;
	private int pageSize;
	
	private FlexiTableDataModel tableModel;
	private FlexiTableComponent component;
	private String mapperUrl;
	
	private Set<Integer> multiSelectedIndex;
	private Map<String,FormItem> components = new HashMap<String,FormItem>();
	
	public FlexiTableElementImpl(UserRequest ureq, String name, FlexiTableDataModel tableModel) {
		this(ureq, name, null, tableModel, -1);
	}
	public FlexiTableElementImpl(UserRequest ureq, String name, Translator translator, FlexiTableDataModel tableModel) {
		this(ureq, name, translator, tableModel, -1);
	}
	
	public FlexiTableElementImpl(UserRequest ureq, String name, Translator translator, FlexiTableDataModel tableModel, int pageSize) {
		super(name);
		this.tableModel = tableModel;
		component = new FlexiTableComponent(this, translator);
		
		MapperService mapper = CoreSpringFactory.getImpl(MapperService.class);
		mapperUrl = mapper.register(ureq.getUserSession(), new FlexiTableModelMapper(this));
		
		this.pageSize = pageSize;
		if(pageSize > 0) {
			setPage(0);
		}
	}
	
	public boolean isMultiSelect() {
		return multiSelect;
	}
	public void setMultiSelect(boolean multiSelect) {
		this.multiSelect = multiSelect;
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
	public void setPage(int page) {
		tableModel.load(0, getPageSize());
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
	
	protected void addFormItem(FormItem item) {
		components.put(item.getName(), item);
	}

	/**
	 * @see org.olat.core.gui.components.form.flexible.FormItemImpl#evalFormRequest(org.olat.core.gui.UserRequest)
	 */
	@Override
	public void evalFormRequest(UserRequest ureq) {
		String[] selectedIndexArr = getRootForm().getRequestParameterValues("ftb_ms");
		if(selectedIndexArr != null) {
			setMultiSelectIndex(selectedIndexArr);
		}

		String selectedIndex = getRootForm().getRequestParameter("rSelect");
		if(StringHelper.containsNonWhitespace(selectedIndex)) {
			int index = selectedIndex.lastIndexOf('-');
			if(index > 0 && index+1 < selectedIndex.length()) {
				String pos = selectedIndex.substring(index+1);
				doSelect(ureq, Integer.parseInt(pos));
			}
		} else {
			
			boolean actionEvent = false;
			FlexiTableColumnModel colModel = tableModel.getTableColumnModel();
			for(int i=colModel.getColumnCount(); i-->0; ) {
				FlexiColumnModel col = colModel.getColumnModel(i);
				if(col.getAction() != null) {
					String selectedRowIndex = getRootForm().getRequestParameter(col.getAction());
					if(StringHelper.containsNonWhitespace(selectedRowIndex)) {
						doSelect(ureq, col.getAction(), Integer.parseInt(selectedRowIndex));
						actionEvent = true;
					}
				}
			}
			
			if(!actionEvent) {
				String paramId = component.getFormDispatchId();
				String value = getRootForm().getRequestParameter(paramId);
				if (value != null) {
					//TODO:cg:XXX do something with value TextElement e.g. setValue(value);
					// mark associated component dirty, that it gets rerendered
					component.setDirty(true);
				}
			}
		}
	}
	
	protected void doSelect(UserRequest ureq, int index) {
		getRootForm().fireFormEvent(ureq, new SelectionEvent(ROM_SELECT_EVENT, index, this, FormEvent.ONCLICK));
	}
	
	protected void doSelect(UserRequest ureq, String action, int index) {
		getRootForm().fireFormEvent(ureq, new SelectionEvent(action, index, this, FormEvent.ONCLICK));
	}
	
	public Set<Integer> getMultiSelectedIndex() {
		return multiSelectedIndex == null ? Collections.<Integer>emptySet() : multiSelectedIndex;
	}
	
	public boolean isMultiSelectedIndex(int index) {
		if(multiSelectedIndex == null) {
			return false;
		}
		return multiSelectedIndex.contains(new Integer(index));
	}
	
	protected void setMultiSelectIndex(String[] selections) {
		if(multiSelectedIndex == null) {
			multiSelectedIndex = new HashSet<Integer>();
		}
		multiSelectedIndex.clear();
		//selection format row_{formDispId}-{index}
		if(selections.length > 0) {
			int index = selections[0].lastIndexOf('-');
			if(index > 0) {
				for(String selection:selections) {	
					if(index > 0 && index+1 < selection.length()) {
						String rowStr = selection.substring(index+1);
						int row = Integer.parseInt(rowStr);
						multiSelectedIndex.add(new Integer(row));
					}
				}
			}
		}
	}

	@Override
	public void validate(List<ValidationStatus> validationResults) {
		//static text must not validate
	}

	@Override
	public void reset() {
		// static text can not be resetted
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
		//root form not interesting for Static text
	}

	protected FlexiTableComponent getFormItemComponent() {
		return component;
	}
	
	public int getRowCount() {
		if(rowCount < 0) {
			rowCount = tableModel.getRowCount();
		}
		return rowCount;
	}
	
	public int getFirstRow() {
		return 0;
	}
	
	public int getMaxRows() {
		if(pageSize > 0) {
			return pageSize;
		}
		return getRowCount();
	}
	
	public FlexiTableDataModel getTableDataModel() {
		return tableModel;
	}
}
