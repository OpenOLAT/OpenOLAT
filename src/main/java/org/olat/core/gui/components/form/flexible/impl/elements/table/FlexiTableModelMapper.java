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
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.media.JSONMediaResource;
import org.olat.core.gui.media.MediaResource;
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
	
	private final FlexiTableElementImpl ftE;
	
	public FlexiTableModelMapper(FlexiTableElementImpl ftE) {
		this.ftE = ftE;
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
	
		String firstRowStr = request.getParameter("iDisplayStart");
		String maxRowStr = request.getParameter("iDisplayLength");
		String echo = request.getParameter("sEcho");
		String sortCol = request.getParameter("iSortCol_0");
		String sortDir = request.getParameter("sSortDir_0");
		
		int sortedColIndex = -1;
		if(StringHelper.containsNonWhitespace(sortCol)) {
			sortedColIndex = Integer.parseInt(sortCol);
			if(ftE.isMultiSelect()) {
				sortedColIndex--;
			}
		}

		if(StringHelper.isLong(firstRowStr) && StringHelper.isLong(maxRowStr)) {
			FlexiTableDataModel model = ftE.getTableDataModel();
			FlexiTableColumnModel columnModel = model.getTableColumnModel();
			
			SortKey orderBy = null;
			if(sortedColIndex >= 0 && sortedColIndex < columnModel.getColumnCount()) {
				FlexiColumnModel sortedColumn = model.getTableColumnModel().getColumnModel(sortedColIndex);
				orderBy = new SortKey(sortedColumn.getSortKey(), "asc".equals(sortDir));
			}

			int rows = ftE.getTableDataModel().getRowCount();
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
				model.load(firstRow, maxRows, orderBy);

				for (int i = firstRow; i < lastRow; i++) {
					JSONObject row = new JSONObject();
					
					Object key = model.getValueAt(i, 0);
					Object subject = model.getValueAt(i, 1);
					FormItem select = (FormItem)model.getValueAt(i, 2);
					if(ftE.getRootForm() != select.getRootForm()) {
						select.setRootForm(ftE.getRootForm());
					}
					FormItem mark = (FormItem)model.getValueAt(i, 3);
					if(ftE.getRootForm() != mark.getRootForm()) {
						mark.setRootForm(ftE.getRootForm());
					}
					ftE.addFormItem(select);
					ftE.addFormItem(mark);
					
					if(ftE.isMultiSelect()) {
						StringBuilder sb = new StringBuilder();
						sb.append("<input type='checkbox' name='ftb_ms' value='").append(rowIdPrefix).append(i).append("'");
						if(ftE.isMultiSelectedIndex(i)) {
							sb.append(" checked='checked'");
						}   
						sb.append(">");
						row.put("choice", sb.toString());
					}

					row.put("key", key);
					row.put("subject", subject);
					
					String selectVal = renderFormItem(select, request, ftE.getTranslator());
					row.put("select", selectVal);
					String markVal = renderFormItem(mark, request, ftE.getTranslator());
					row.put("mark", markVal);
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
	
	private String renderFormItem(FormItem item, HttpServletRequest request, Translator translator) {
		UserSession usess = CoreSpringFactory.getImpl(UserSessionManager.class).getUserSession(request);
		ChiefController cc = (ChiefController)Windows.getWindows(usess).getAttribute("AUTHCHIEFCONTROLLER");
		return cc.getWindow().renderComponent(item.getComponent());
	}
}
