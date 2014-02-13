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
import org.olat.course.nodes.cl.model.AssessmentDataView;

/**
 * 
 * Initial date: 07.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckboxAssessmentDataModel extends DefaultFlexiTableDataModel<AssessmentDataView>
	implements FilterableFlexiTableModel, SortableFlexiTableDataModel<AssessmentDataView>,
	    ExportableFlexiTableDataModel {
	
	public static final int USER_PROPS_OFFSET = 500;
	public static final int CHECKBOX_OFFSET = 5000;
	
	private List<AssessmentDataView> backupRows;
	
	public CheckboxAssessmentDataModel(List<AssessmentDataView> datas, FlexiTableColumnModel columnModel) {
		super(datas, columnModel);
		backupRows = datas;
	}
	
	/**
	 * @return The list of rows, not filtered
	 */
	public List<AssessmentDataView> getBackedUpRows() {
		return backupRows;
	}

	@Override
	public DefaultFlexiTableDataModel<AssessmentDataView> createCopyWithEmptyList() {
		return new CheckboxAssessmentDataModel(new ArrayList<AssessmentDataView>(), getTableColumnModel());
	}
	
	@Override
	public void sort(SortKey orderBy) {
		SortableFlexiTableModelDelegate<AssessmentDataView> sorter
			= new SortableFlexiTableModelDelegate<>(orderBy, this, null);
		List<AssessmentDataView> views = sorter.sort();
		super.setObjects(views);
	}

	@Override
	public MediaResource export(FlexiTableComponent ftC) {
		List<AssessmentDataView> currentRows = getObjects();
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
			List<AssessmentDataView> filteredViews = new ArrayList<>();
			int numOfRows = getRowCount();
			for(int i=0; i<numOfRows; i++) {
				AssessmentDataView view = getObject(i);
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
	
	private boolean accept(AssessmentDataView view, Long groupKey) {
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
	public void setObjects(List<AssessmentDataView> objects) {
		backupRows = objects;
		super.setObjects(objects);
	}

	@Override
	public Object getValueAt(int row, int col) {
		AssessmentDataView box = getObject(row);
		return getValueAt(box, col);
	}
		
	@Override
	public Object getValueAt(AssessmentDataView row, int col) {
		if(col == Cols.username.ordinal()) {
			return row.getIdentityName();
		} else if(col == Cols.totalPoints.ordinal()) {
			return row.getTotalPoints();
		} else if(col >= USER_PROPS_OFFSET && col < CHECKBOX_OFFSET) {
			int propIndex = col - USER_PROPS_OFFSET;
			return row.getIdentityProp(propIndex);
		} else if(col >= CHECKBOX_OFFSET) {
			int propIndex = col - CHECKBOX_OFFSET;
			Boolean[] checked = row.getChecked();
			if(checked != null && propIndex >= 0 && propIndex < checked.length) {
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
