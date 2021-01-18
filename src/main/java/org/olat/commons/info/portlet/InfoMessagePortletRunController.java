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
package org.olat.commons.info.portlet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.NewControllerFactory;
import org.olat.commons.info.InfoMessage;
import org.olat.commons.info.InfoMessageFrontendManager;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.SubscriptionInfo;
import org.olat.core.commons.services.notifications.model.SubscriptionListItem;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.BaseTableDataModelWithoutFilter;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.portal.AbstractPortletRunController;
import org.olat.core.gui.control.generic.portal.PortletDefaultTableDataModel;
import org.olat.core.gui.control.generic.portal.PortletEntry;
import org.olat.core.gui.control.generic.portal.PortletToolSortingControllerImpl;
import org.olat.core.gui.control.generic.portal.SortingCriteria;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.filter.FilterFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Show the last five infos 
 * 
 * <P>
 * Initial Date:  27 juil. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoMessagePortletRunController extends AbstractPortletRunController<InfoSubscriptionItem> implements GenericEventListener {
	
	private Link showAllLink;
	private TableController tableController;
	private VelocityContainer portletVC;
	
	private boolean newInfos = false;
	
	@Autowired
	private NotificationsManager notificationsManager;
	
	public InfoMessagePortletRunController(WindowControl wControl, UserRequest ureq, Translator trans,
			String portletName, int defaultMaxentries) {
		super(wControl, ureq, trans, portletName, defaultMaxentries);
		
		portletVC =  createVelocityContainer("infosPortlet");
		showAllLink = LinkFactory.createLink("portlet.showall", portletVC, this);
		showAllLink.setIconRightCSS("o_icon o_icon_start");

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("portlet.no_messages"));
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
		
		listenTo(tableController);
		
		sortingTermsList.add(SortingCriteria.DATE_SORTING);
		sortingCriteria = getPersistentSortingConfiguration(ureq);
		sortingCriteria.setSortingTerm(SortingCriteria.DATE_SORTING);
		reloadModel(sortingCriteria);
		
		portletVC.put("table", tableController.getInitialComponent());
		
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, getIdentity(), InfoMessageFrontendManager.oresFrontend);

		putInitialPanel(portletVC);

		getWindowControl().getWindowBackOffice().addCycleListener(this);
	}

	@Override
	protected SortingCriteria createDefaultSortingCriteria() {
		SortingCriteria sortCriteria = new SortingCriteria(sortingTermsList, getDefaultMaxEntries());
		sortCriteria.setAscending(false);
		return sortCriteria;
	}

	@Override
	public synchronized void doDispose() {
		getWindowControl().getWindowBackOffice().removeCycleListener(this);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, InfoMessageFrontendManager.oresFrontend);
		super.doDispose();
	}

	@Override
	public void event(Event event) {
		if("new_info_message".equals(event.getCommand())) {
			newInfos = true;
		} else if (event == Window.BEFORE_INLINE_RENDERING) {
			if(newInfos) {
				reloadModel(sortingCriteria);
				newInfos = false;
			}
		}
	}

	@Override
	protected Comparator<InfoSubscriptionItem> getComparator(SortingCriteria criteria) {
		return new InfoPortletEntryComparator(criteria);
	}
	
	/**
	 * 
	 * @param items
	 * @return
	 */
	private List<PortletEntry<InfoSubscriptionItem>> convertToPortletEntryList(List<InfoSubscriptionItem> infos) {
		List<PortletEntry<InfoSubscriptionItem>> convertedList = new ArrayList<>();
		long i = 0;
		for(InfoSubscriptionItem info:infos) {
			convertedList.add(new InfoPortletEntry(i++, info));
		}
		return convertedList;
	}

	@Override
	protected void reloadModel(SortingCriteria criteria) {
		List<SubscriptionInfo> infos = notificationsManager.getSubscriptionInfos(getIdentity(), "InfoMessage");
		List<InfoSubscriptionItem> items = new ArrayList<>();
		for(SubscriptionInfo info:infos) {
			for(SubscriptionListItem item:info.getSubscriptionListItems()) {
				items.add(new InfoSubscriptionItem(info, item));
			}
		}
		items = getSortedList(items, criteria);
		List<PortletEntry<InfoSubscriptionItem>> entries = convertToPortletEntryList(items);
		InfosTableModel model = new InfosTableModel(entries);
		tableController.setTableDataModel(model);
	}

	@Override
	protected void reloadModel(List<PortletEntry<InfoSubscriptionItem>> sortedItems) {
		InfosTableModel model = new InfosTableModel(sortedItems);
		tableController.setTableDataModel(model);
	}
	
	protected PortletToolSortingControllerImpl<InfoSubscriptionItem> createSortingTool(UserRequest ureq, WindowControl wControl) {
		if(portletToolsController==null) {
			final List<PortletEntry<InfoSubscriptionItem>> empty = Collections.<PortletEntry<InfoSubscriptionItem>>emptyList();
			final PortletDefaultTableDataModel<InfoSubscriptionItem> defaultModel = new PortletDefaultTableDataModel<>(empty, 2) {
				@Override
				public Object getValueAt(int row, int col) {
					return null;
				}
			};
			portletToolsController = new PortletToolSortingControllerImpl<>(ureq, wControl, getTranslator(), sortingCriteria, defaultModel, empty);
			portletToolsController.setConfigManualSorting(false);
			portletToolsController.setConfigAutoSorting(true);
			portletToolsController.addControllerListener(this);
		}		
		return portletToolsController;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == showAllLink) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(new Date());
			cal.add(Calendar.MONTH, -1);
			// fxdiff activate homes tab in top navigation and activate the correct
			// menu item
			String resourceUrl = "[HomeSite:" + ureq.getIdentity().getKey() + "][notifications:0][type=" + InfoMessage.class.getSimpleName()
					+ ":0]" + BusinessControlFactory.getInstance().getContextEntryStringForDate(cal.getTime());
			BusinessControl bc = BusinessControlFactory.getInstance().createFromString(resourceUrl);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
			NewControllerFactory.getInstance().launch(ureq, bwControl);
		}
	}
	
	public class InfosTableModel extends BaseTableDataModelWithoutFilter<PortletEntry<InfoSubscriptionItem>> {
		private final List<PortletEntry<InfoSubscriptionItem>> infos;
		
		public InfosTableModel(List<PortletEntry<InfoSubscriptionItem>> infos) {
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
			InfoPortletEntry entry = (InfoPortletEntry)infos.get(row);
			switch(col) {
				case 0: return entry.getValue();
				default: return entry;
			}
		}
	}
	
	public class InfoNodeRenderer implements CustomCellRenderer {
		
		@Override
		public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
			if(val instanceof InfoSubscriptionItem) {
				InfoSubscriptionItem isi = (InfoSubscriptionItem)val;
				SubscriptionListItem item = isi.getItem();
				SubscriptionInfo info = isi.getInfo();
				//title
				String title = info.getTitle(SubscriptionInfo.MIME_PLAIN);
				title = StringHelper.escapeHtml(title);
				int key = info.hashCode();
				StringBuilder tipSb = null;
				String tip = item.getDescriptionTooltip();
				boolean tooltip = StringHelper.containsNonWhitespace(tip);
				if(tooltip) {
					tipSb = new StringBuilder();
					if(StringHelper.isHtml(tip)) {
						tip = FilterFactory.getHtmlTagAndDescapingFilter().filter(tip);
					}
					tip = Formatter.escWithBR(Formatter.truncate(tip, 256)).toString();
					
					tipSb.append("<b>").append(title).append(":</b>").append("<br/>").append(tip);
					sb.append("<span id='o_sel_info_msg_title_").append(key).append("'>");
				} else {
					sb.append("<span>");
				}
				sb.append(Formatter.truncate(title, 30)).append("</span>&nbsp;");
				//link
				String itemDesc = StringHelper.escapeHtml(item.getDescription());
				String infoTitle = Formatter.truncate(itemDesc, 30);
				sb.append("<a id='o_sel_info_msg_link_").append(key).append("' href=\"").append(item.getLink()).append("\" class=\"o_portlet_infomessage_link\"");

				sb.append(">")
					.append(infoTitle)
					.append("</a>");
				
				if(tooltip) {
					sb.append("<div id='o_sel_info_tooltip_").append(key).append("' style='display:none'>").append(tipSb.toString()).append("</div>");
				  sb.append("<script>")
				    .append("jQuery(function() {")
					  .append("  jQuery('#o_sel_info_msg_title_").append(key).append(",#o_sel_info_msg_link_").append(key).append("').tooltip({")
					  .append("   html: true,")
					  .append("   title: function(){ return jQuery('#o_sel_info_tooltip_").append(key).append("').html(); }")
					  .append("  });")
					  .append("});")
					  .append("</script>");
				}
			} else {
				sb.append("-");
			}
		}
	}
}
