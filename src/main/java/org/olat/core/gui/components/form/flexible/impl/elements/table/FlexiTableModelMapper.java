package org.olat.core.gui.components.form.flexible.impl.elements.table;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Window;
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

		
		if(StringHelper.isLong(firstRowStr) && StringHelper.isLong(maxRowStr)) {
			FlexiTableDataModel model = ftE.getTableDataModel();
			int rows = ftE.getTableDataModel().getRowCount();
			
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
				
				System.out.println(firstRow + " :: " + lastRow);
				
				for (int i = firstRow; i < lastRow; i++) {
					JSONObject row = new JSONObject();
					
					Object key = model.getValueAt(i, 0);
					Object subject = model.getValueAt(i, 1);
					FormItem select = (FormItem)model.getValueAt(i, 2);
					FormItem mark = (FormItem)model.getValueAt(i, 3);

					row.put("key", key);
					row.put("subject", subject);
					
					String selectVal = renderFormItem(select, request, ftE.getTranslator());
					row.put("select", selectVal);
					String markVal = renderFormItem(mark, request, ftE.getTranslator());
					row.put("mark", markVal);
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
		Window w = cc.getWindow();
		return w.renderComponent(item.getComponent());
	}
}
