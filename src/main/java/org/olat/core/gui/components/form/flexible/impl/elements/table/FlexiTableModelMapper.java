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
package org.olat.core.gui.components.form.flexible.impl.elements.table;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.media.JSONMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.session.UserSessionManager;

/**
 * 
 * Initial date: 28.01.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiTableModelMapper implements Mapper {
	
	private static final OLog log = Tracing.createLoggerFor(FlexiTableModelMapper.class);
	
	private final FlexiTableComponent ftC;
	
	public FlexiTableModelMapper(FlexiTableComponent ftC) {
		this.ftC = ftC;
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
	
		String firstRowStr = request.getParameter("iDisplayStart");
		String maxRowStr = request.getParameter("iDisplayLength");
		String echo = request.getParameter("sEcho");
		String sortCol = request.getParameter("iSortCol_0");
		String sortDir = request.getParameter("sSortDir_0");
		
		FlexiTableElementImpl ftE = ftC.getFlexiTableElement();
		URLBuilder ubu = getURLBuilder(request);
		
		int sortedColIndex = -1;
		if(StringHelper.containsNonWhitespace(sortCol)) {
			sortedColIndex = Integer.parseInt(sortCol);
			if(ftE.isMultiSelect()) {
				sortedColIndex--;
			}
		}

		if(StringHelper.isLong(firstRowStr) && StringHelper.isLong(maxRowStr)) {
			FlexiTableDataModel<?> dataModel = ftE.getTableDataModel();
			FlexiTableColumnModel columnsModel = dataModel.getTableColumnModel();
			
			SortKey orderBy = null;
			if(sortedColIndex >= 0 && sortedColIndex < columnsModel.getColumnCount()) {
				int count = 0;
				for(int i=0; i<columnsModel.getColumnCount() && count <= sortedColIndex; i++) {
					FlexiColumnModel sortedColumn = columnsModel.getColumnModel(i);
					if(ftE.isColumnModelVisible(sortedColumn)) {
						if(count == sortedColIndex && StringHelper.containsNonWhitespace(sortedColumn.getSortKey())) {
							orderBy = new SortKey(sortedColumn.getSortKey(), "asc".equals(sortDir));
							break;
						}
						count++;
					}
				}
			}

			int rows = dataModel.getRowCount();
			String dispatchId = ftE.getComponent().getDispatchID();
			String rowIdPrefix = "row_" + dispatchId + "-";
			
			try {
				JSONObject root = new JSONObject();
				root.put("sEcho", Integer.parseInt(echo) + 1);
				root.put("iTotalRecords", rows);
				root.put("iTotalDisplayRecords", rows);
				
				JSONArray ja = new JSONArray();
				root.put("aaData", ja);
				
				int firstRow = Integer.parseInt(firstRowStr);
				int maxRows = Integer.parseInt(maxRowStr);
				int lastRow = Math.min(rows, firstRow + maxRows);
				//paged loading
				ResultInfos<?> results = ftE.doScroll(firstRow, maxRows, orderBy);
				ftE.setCurrentFirstResult(results.getNextFirstResult());
				
				for (int i = firstRow; i < lastRow; i++) {
					JSONObject row = new JSONObject();

					if(ftE.isMultiSelect()) {
						StringBuilder sb = new StringBuilder();
						sb.append("<input type='checkbox' name='tb_ms' value='").append(rowIdPrefix).append(i).append("'");
						if(ftE.isAllSelectedIndex() || ftE.isMultiSelectedIndex(i)) {
							sb.append(" checked='checked'");
						}   
						sb.append("/>");
						row.put("multiSelectCol", sb.toString());
					}
					
					for(int j=0; j<columnsModel.getColumnCount(); j++) {
						FlexiColumnModel col = columnsModel.getColumnModel(j);
						if(ftE.isColumnModelVisible(col)) {
							int columnIndex = col.getColumnIndex();
							Object value = columnIndex >= 0 ? dataModel.getValueAt(i, columnIndex) : null;
	
							String val;
							if(value instanceof FormItem) {
								FormItem item = (FormItem)value;
								if(ftE.getRootForm() != item.getRootForm()) {
									item.setRootForm(ftE.getRootForm());
								}
								ftE.addFormItem(item);
								val = renderFormItem(item, request, ftE.getTranslator());
							} else {
								val = renderColumnRenderer(col, value, i, ftC, ubu, ftE.getTranslator());
							}
							
							row.put(col.getColumnKey(), val);
						}
					}
					row.put("DT_RowId", rowIdPrefix + Integer.toString(i));
					
					ja.put(row);
				}
				return new JSONMediaResource(root, "UTF-8");
			} catch (NumberFormatException e) {
				log.error("", e);
			} catch (JSONException e) {
				log.error("", e);
			}
		}
		return null;
	}
	
	private String renderColumnRenderer(FlexiColumnModel col, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		
		StringOutput target = new StringOutput(128);
		col.getCellRenderer().render(target, cellValue, row, source, ubu, translator);
		return target.toString();
	}
	
	private String renderFormItem(FormItem item, HttpServletRequest request, Translator translator) {
		UserSession usess = CoreSpringFactory.getImpl(UserSessionManager.class).getUserSession(request);
		ChiefController cc = (ChiefController)Windows.getWindows(usess).getAttribute("AUTHCHIEFCONTROLLER");
		return cc.getWindow().renderComponent(item.getComponent());
	}
	
	private URLBuilder getURLBuilder(HttpServletRequest request) {
		UserSession usess = CoreSpringFactory.getImpl(UserSessionManager.class).getUserSession(request);
		ChiefController cc = (ChiefController)Windows.getWindows(usess).getAttribute("AUTHCHIEFCONTROLLER");
		return cc.getWindow().getURLBuilder();
	}
}
