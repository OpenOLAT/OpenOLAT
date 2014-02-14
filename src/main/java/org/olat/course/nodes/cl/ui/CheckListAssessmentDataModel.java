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
package org.olat.course.nodes.cl.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ExportableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ExportableFlexiTableDataModelDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FilterableFlexiTableModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 14.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckListAssessmentDataModel extends DefaultFlexiTableDataModel<CheckListAssessmentRow>
	implements FilterableFlexiTableModel, SortableFlexiTableDataModel<CheckListAssessmentRow>,
	    ExportableFlexiTableDataModel {
	
	public static final int USER_PROPS_OFFSET = 500;
	public static final int CHECKBOX_OFFSET = 5000;
	
	private List<CheckListAssessmentRow> backupRows;
	
	public CheckListAssessmentDataModel(List<CheckListAssessmentRow> datas, FlexiTableColumnModel columnModel) {
		super(datas, columnModel);
		backupRows = datas;
	}
	
	/**
	 * @return The list of rows, not filtered
	 */
	public List<CheckListAssessmentRow> getBackedUpRows() {
		return backupRows;
	}

	@Override
	public DefaultFlexiTableDataModel<CheckListAssessmentRow> createCopyWithEmptyList() {
		return new CheckListAssessmentDataModel(new ArrayList<CheckListAssessmentRow>(), getTableColumnModel());
	}
	
	@Override
	public void sort(SortKey orderBy) {
		SortableFlexiTableModelDelegate<CheckListAssessmentRow> sorter
			= new SortableFlexiTableModelDelegate<>(orderBy, this, null);
		List<CheckListAssessmentRow> views = sorter.sort();
		super.setObjects(views);
	}

	@Override
	public MediaResource export(FlexiTableComponent ftC) {
		List<CheckListAssessmentRow> currentRows = getObjects();
		setObjects(backupRows);
		
		FlexiTableColumnModel columnModel = getTableColumnModel();
		int numOfColumns = columnModel.getColumnCount();
		List<FlexiColumnModel> columns = new ArrayList<>(numOfColumns);
		for(int i=0; i<numOfColumns; i++) {
			FlexiColumnModel column = columnModel.getColumnModel(i);
			String headerKey = column.getHeaderKey();
			if(!"edit.checkbox".equals(headerKey)) {
				columns.add(column);
			}
		}
		ExportableFlexiTableDataModelDelegate delegate = new ExportableFlexiTableDataModelDelegate();
		MediaResource resource = delegate.export(ftC, columns, ftC.getTranslator());
		//replace the current perhaps filtered rows
		super.setObjects(currentRows);
		return resource;
	}

	/**
	 * The filter apply to the groups
	 * @param key
	 */
	@Override
	public void filter(String filter) {
		setObjects(backupRows);
		
		Long groupKey = extractGroupKey(filter);
		if(groupKey != null) {
			List<CheckListAssessmentRow> filteredViews = new ArrayList<>();
			int numOfRows = getRowCount();
			for(int i=0; i<numOfRows; i++) {
				CheckListAssessmentRow view = getObject(i);
				if(accept(view, groupKey)) {
					filteredViews.add(view);
				}
			}
			super.setObjects(filteredViews);
		}
	}
	
	private Long extractGroupKey(String filter) {
		Long key = null;
		if(StringHelper.isLong(filter)) {
			try {
				key = Long.parseLong(filter);
			} catch (NumberFormatException e) {
				//
			}
		}
		return key;
	}
	
	private boolean accept(CheckListAssessmentRow view, Long groupKey) {
		boolean accept = false;
		Long[] groupKeys = view.getGroupKeys();
		if(groupKeys != null) {
			for(Long key:groupKeys) {
				if(groupKey.equals(key)) {
					accept = true;
				}
			}
		}
		return accept;
	}

	@Override
	public void setObjects(List<CheckListAssessmentRow> objects) {
		backupRows = objects;
		super.setObjects(objects);
	}

	@Override
	public Object getValueAt(int row, int col) {
		CheckListAssessmentRow box = getObject(row);
		return getValueAt(box, col);
	}
		
	@Override
	public Object getValueAt(CheckListAssessmentRow row, int col) {
		if(col == Cols.username.ordinal()) {
			return row.getIdentityName();
		} else if(col == Cols.totalPoints.ordinal()) {
			return row.getTotalPoints();
		} else if(col >= USER_PROPS_OFFSET && col < CHECKBOX_OFFSET) {
			int propIndex = col - USER_PROPS_OFFSET;
			return row.getIdentityProp(propIndex);
		} else if(col >= CHECKBOX_OFFSET) {
			int propIndex = col - CHECKBOX_OFFSET;
			
			if(row.getCheckedEl() != null) {
				//edit mode
				MultipleSelectionElement[] checked = row.getCheckedEl();
				if(checked != null && propIndex >= 0 && propIndex < checked.length) {
					return checked[propIndex];
				}
			}
			
			Boolean[] checked = row.getChecked();
			if(checked != null && propIndex >= 0 && propIndex < checked.length
					&& checked[propIndex] != null && checked[propIndex].booleanValue()) {
				return checked[propIndex];
			}
			return null;
		}
		return row;
	}
	
	public enum Cols {
		username("username"),
		totalPoints("points");
		
		private final String i18nKey;
		
		private Cols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
}
