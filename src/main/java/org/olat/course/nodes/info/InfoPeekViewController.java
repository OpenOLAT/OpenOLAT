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

package org.olat.course.nodes.info;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

import org.olat.commons.info.InfoMessage;
import org.olat.commons.info.InfoMessageFrontendManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.table.BaseTableDataModelWithoutFilter;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.helpers.Settings;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.course.nodes.InfoCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Peekview for info messages
 * 
 * <P>
 * Initial Date:  3 aug. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoPeekViewController extends BasicController {
	
	private OLATResourceable ores;
	private final InfoCourseNode courseNode;
	
	private TableController tableController;
	
	@Autowired
	private InfoMessageFrontendManager infoService;

	public InfoPeekViewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, 
			InfoCourseNode courseNode) {
		super(ureq, wControl);
		
		this.courseNode = courseNode;
		Long resId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
		ores = OresHelper.createOLATResourceableInstance(CourseModule.class, resId);
		
		init(ureq);
		
		putInitialPanel(tableController.getInitialComponent());
	}
	
	private void init(UserRequest ureq) {
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("peekview.noInfos"), null, "o_infomsg_icon");
		tableConfig.setDisplayTableHeader(false);
		tableConfig.setCustomCssClass("o_portlet_table");
		tableConfig.setDisplayRowCount(false);
		tableConfig.setPageingEnabled(false);
		tableConfig.setDownloadOffered(false);
		tableConfig.setSortingEnabled(false);
		
		removeAsListenerAndDispose(tableController);
		tableController = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		tableController.addColumnDescriptor(new CustomRenderColumnDescriptor("peekview.title", 0,
				null, ureq.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, new InfoNodeRenderer()));
		
		String resSubPath = courseNode.getIdent();
		List<InfoMessage> infos = infoService.loadInfoMessageByResource(ores, resSubPath, null, null, null, 0, 5);

		InfosTableModel model = new InfosTableModel(infos);
		tableController.setTableDataModel(model);
		listenTo(tableController);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	private class InfosTableModel extends BaseTableDataModelWithoutFilter<InfosTableModel> {
		private final List<InfoMessage> infos;
		
		public InfosTableModel(List<InfoMessage> infos) {
			this.infos = infos;
		}

		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public int getRowCount() {
			return infos.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			InfoMessage info = infos.get(row);
			switch(col) {
				case 0: return info;
				default: return null;
			}
		}
	}
	
	public String getUrl(String businessPath) {
		BusinessControlFactory bCF = BusinessControlFactory.getInstance(); 
		List<ContextEntry> ceList = bCF.createCEListFromString(businessPath);
		StringBuilder retVal = new StringBuilder();
		retVal.append(Settings.getServerContextPathURI()).append("/url/");
		for (ContextEntry contextEntry : ceList) {
			String ceStr = contextEntry.toString();
			ceStr = ceStr.replace(':', '/');
			ceStr = ceStr.replaceFirst("\\]", "/");
			ceStr= ceStr.replaceFirst("\\[", "");
			retVal.append(ceStr);
		}
		return retVal.substring(0, retVal.length() - 1);
	}
	
	public class InfoNodeRenderer implements CustomCellRenderer {
		private DateFormat formatter;
		
		public InfoNodeRenderer() {
			//
		}
		
		@Override
		public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
			if(val instanceof InfoMessage) {
				InfoMessage item = (InfoMessage)val;
				//date
				if(formatter == null) {
					formatter = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);
				}
				sb.append(formatter.format(item.getCreationDate())).append(": ");
				//title
				boolean tooltip = StringHelper.containsNonWhitespace(item.getMessage());
				if(tooltip) {
					String message = Formatter.escWithBR(Formatter.truncate(item.getMessage(), 255)).toString();
					sb.append("<span title=\"").appendHtmlEscaped(message).append("\">");
				} else {
					sb.append("<span>");
				}
				String title = StringHelper.escapeHtml(item.getTitle());
				title = Formatter.truncate(title, 64);
				sb.append(title).append("</span>&nbsp;");
				//link
				if(StringHelper.containsNonWhitespace(item.getBusinessPath())) {
					String url = getUrl(item.getBusinessPath());
					sb.append("<a href=\"").append(url).append("\" class=\"o_peekview_infomsg_link\">")
						.append(translate("peekview.more"))
						.append("</a>");
				}
			} else {
				sb.append("-");
			}
		}
	}
}