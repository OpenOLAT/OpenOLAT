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

import static org.olat.course.nodes.cl.ui.CheckListAssessmentController.BUSINESS_GROUP_PREFIX;
import static org.olat.course.nodes.cl.ui.CheckListAssessmentController.CURRICULUM_EL_PREFIX;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ExportableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.render.EmptyURLBuilder;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.StringOutputPool;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.course.nodes.cl.model.Checkbox;
import org.olat.course.nodes.cl.model.CheckboxList;
import org.olat.modules.assessment.model.AssessmentObligation;

/**
 * 
 * Initial date: 14.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckListAssessmentDataModel extends DefaultFlexiTableDataModel<CheckListAssessmentRow>
	implements SortableFlexiTableDataModel<CheckListAssessmentRow>, ExportableFlexiTableDataModel {
	
	private static final Logger log = Tracing.createLoggerFor(CheckListAssessmentDataModel.class);
	
	public static final int USER_PROPS_OFFSET = 500;
	public static final int CHECKBOX_OFFSET = 5000;
	
	private final Locale locale;
	private final CheckboxList checkboxList;
	private List<CheckListAssessmentRow> backupRows;
	
	public CheckListAssessmentDataModel(CheckboxList checkboxList, List<CheckListAssessmentRow> datas,
			FlexiTableColumnModel columnModel, Locale locale) {
		super(datas, columnModel);
		backupRows = datas;
		this.locale = locale;
		this.checkboxList = checkboxList;
	}
	
	/**
	 * @return The list of rows, not filtered
	 */
	public List<CheckListAssessmentRow> getBackedUpRows() {
		return backupRows;
	}

	@Override
	public DefaultFlexiTableDataModel<CheckListAssessmentRow> createCopyWithEmptyList() {
		return new CheckListAssessmentDataModel(checkboxList, new ArrayList<CheckListAssessmentRow>(), getTableColumnModel(), locale);
	}
	
	@Override
	public void sort(SortKey orderBy) {
		CheckListAssessmentDataModelSorter sorter = new CheckListAssessmentDataModelSorter(orderBy, this, locale);
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
		
		CheckListXlsFlexiTableExporter exporter = new CheckListXlsFlexiTableExporter();
		MediaResource resource = exporter.export(ftC, this, columns, ftC.getTranslator());
		//replace the current perhaps filtered rows
		super.setObjects(currentRows);
		return resource;
	}

	public void filter(List<FlexiTableFilter> filters) {
		setObjects(backupRows);
		
		List<AssessmentObligation> obligations = null;
		List<Long> businessGroupKeys = null;
		List<Long> curriculumElementKeys = null;
		if (filters != null && !filters.isEmpty()) {
			FlexiTableFilter obligationFilter = FlexiTableFilter.getFilter(filters, "obligation");
			if (obligationFilter != null) {
				List<String> filterValues = ((FlexiTableExtendedFilter)obligationFilter).getValues();
				if (!filterValues.isEmpty()) {
					obligations = filterValues.stream()
							.map(AssessmentObligation::valueOf)
							.collect(Collectors.toList());
				}
			}
			
			FlexiTableFilter groupsFilter = FlexiTableFilter.getFilter(filters, "groups");
			if(groupsFilter != null) {
				businessGroupKeys = new ArrayList<>(filters.size());
				curriculumElementKeys = new ArrayList<>(filters.size());
				List<String> filterValues = ((FlexiTableExtendedFilter)groupsFilter).getValues();
				for(String filterValue:filterValues) {
					if(filterValue.startsWith(BUSINESS_GROUP_PREFIX)) {
						String key = filterValue.substring(BUSINESS_GROUP_PREFIX.length(), filterValue.length());
						businessGroupKeys.add(Long.valueOf(key));
					} else if(filterValue.startsWith(CURRICULUM_EL_PREFIX)) {
						String key = filterValue.substring(CURRICULUM_EL_PREFIX.length(), filterValue.length());
						curriculumElementKeys.add(Long.valueOf(key));
					}
				}
			}
		}
		
		
		if(obligations != null || businessGroupKeys != null || curriculumElementKeys != null) {
			List<CheckListAssessmentRow> filteredViews = new ArrayList<>();
			int numOfRows = getRowCount();
			for(int i=0; i<numOfRows; i++) {
				CheckListAssessmentRow view = getObject(i);
				if(accept(view, obligations, businessGroupKeys, curriculumElementKeys)) {
					filteredViews.add(view);
				}
			}
			super.setObjects(filteredViews);
		}
	}
	
	private boolean accept(CheckListAssessmentRow view, List<AssessmentObligation> obligations,
			List<Long> businessGroupKeys, List<Long> curriculumElementKeys) {
		if (obligations != null && !obligations.isEmpty()) {
			if (view.getAssessmentObligation() != null && !obligations.contains(view.getAssessmentObligation())) {
				return false;
			} else if (view.getAssessmentObligation() == null && !obligations.contains(AssessmentObligation.mandatory)) {
				return false;
			} 
		}
		
		if (businessGroupKeys != null && !businessGroupKeys.isEmpty()) {
			if(view.getGroupKeys() == null || !view.getGroupKeys().stream().anyMatch(businessGroupKeys::contains)) {
				return false;
			}
		}
		
		if (curriculumElementKeys != null && !curriculumElementKeys.isEmpty()) {
			if(view.getCurriculumElmentKeys() == null || !view.getCurriculumElmentKeys().stream().anyMatch(curriculumElementKeys::contains)) {
				return false;
			}
		}
		
		return true;
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
		if(col == Cols.totalPoints.ordinal()) {
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
		totalPoints("points");
		
		private final String i18nKey;
		
		private Cols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		public String i18nKey() {
			return i18nKey;
		}
	}
	
	private static class CheckListXlsFlexiTableExporter {
		private static final URLBuilder ubu = new EmptyURLBuilder();
		

		private CheckListAssessmentDataModel dataModel;

		public MediaResource export(FlexiTableComponent ftC, CheckListAssessmentDataModel model,
				List<FlexiColumnModel> columns, Translator translator) {

			this.dataModel = model;

			
			String label = "CheckList_"
					+ Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()))
					+ ".xlsx";

			return new OpenXMLWorkbookResource(label) {
				@Override
				protected void generate(OutputStream out) {
					try(OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
						OpenXMLWorksheet sheet = workbook.nextWorksheet();
						createHeader(columns, translator, sheet, workbook);
						createData(ftC, columns, translator, sheet);
					} catch (IOException e) {
						log.error("", e);
					}
				}
			};
		}

		private void createHeader(List<FlexiColumnModel> columns, Translator translator,
				OpenXMLWorksheet sheet, OpenXMLWorkbook workbook) {
			sheet.setHeaderRows(1);
			Row headerRow = sheet.newRow();
			int pos = 0;
			for (int c=0; c<columns.size(); c++) {
				FlexiColumnModel cd = columns.get(c);
				String headerVal = cd.getHeaderLabel() == null ?
						translator.translate(cd.getHeaderKey()) : cd.getHeaderLabel();
				headerRow.addCell(pos++, headerVal, workbook.getStyles().getHeaderStyle());
				
				if(cd.getColumnIndex() >= CHECKBOX_OFFSET) {
					int propIndex = cd.getColumnIndex() - CHECKBOX_OFFSET;
					Checkbox box = dataModel.checkboxList.getList().get(propIndex);
					if(box.getPoints() != null && box.getPoints().floatValue() > 0f) {
						headerRow.addCell(pos++, "", workbook.getStyles().getHeaderStyle());
					}
				}
			}
		}

		private void createData(FlexiTableComponent ftC, List<FlexiColumnModel> columns, Translator translator, OpenXMLWorksheet sheet) {
			int numOfRow = dataModel.getRowCount();
			int numOfColumns = columns.size();
			
			for (int r=0; r<numOfRow; r++) {
				int pos = 0;
				Row dataRow = sheet.newRow();
				for (int c = 0; c<numOfColumns; c++) {
					FlexiColumnModel cd = columns.get(c);
					Object value = dataModel.getValueAt(r, cd.getColumnIndex());
					
					if(cd.getColumnIndex() >= CHECKBOX_OFFSET) {
						int propIndex = cd.getColumnIndex() - CHECKBOX_OFFSET;
						Checkbox box = dataModel.checkboxList.getList().get(propIndex);
						
						boolean checked;
						if(value instanceof Boolean) {
							checked = ((Boolean)value).booleanValue();
						} else {
							checked = false;
						}
						String checkVal = checked ? "x" : "";
						dataRow.addCell(pos++, checkVal, null);
						
						if(box.getPoints() != null && box.getPoints().floatValue() > 0f) {
							CheckListAssessmentRow assessmentRow = dataModel.getObject(r);
							Float[] scores = assessmentRow.getScores();
							if(checked && scores != null && scores.length > 0 && propIndex < scores.length) {
								dataRow.addCell(pos++, scores[propIndex], null);
							} else {
								dataRow.addCell(pos++, null);
							}
						}
					} else {
						renderCell(dataRow, pos++, value, r, ftC, cd, translator);
					}
				}
			}
		}
		
		protected void renderCell(Row dataRow, int sheetCol, Object value, int row, FlexiTableComponent ftC, FlexiColumnModel cd, Translator translator) {
			if(value instanceof Boolean) {
				Boolean val = (Boolean)value;
				dataRow.addCell(sheetCol, val.booleanValue() ? "x" : "", null);
			} else if(value instanceof Float || value instanceof Double) {
				dataRow.addCell(sheetCol, (Number)value, null);
			} else {
				StringOutput so = StringOutputPool.allocStringBuilder(1000);
				cd.getCellRenderer().render(null, so, value, row, ftC, ubu, translator);
	
				String cellValue = StringOutputPool.freePop(so);
				cellValue = StringHelper.stripLineBreaks(cellValue);
				cellValue = FilterFactory.getHtmlTagsFilter().filter(cellValue);
				if(StringHelper.containsNonWhitespace(cellValue)) {
					cellValue = StringHelper.unescapeHtml(cellValue);
				}
				dataRow.addCell(sheetCol, cellValue, null);
			}
		}
	}
}
