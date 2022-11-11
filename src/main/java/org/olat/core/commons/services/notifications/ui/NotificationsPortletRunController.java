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
*/

package org.olat.core.commons.services.notifications.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.NewControllerFactory;
import org.olat.core.commons.services.notifications.NotificationHelper;
import org.olat.core.commons.services.notifications.NotificationUIFactory;
import org.olat.core.commons.services.notifications.NotificationsHandler;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionInfo;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.portal.AbstractPortletRunController;
import org.olat.core.gui.control.generic.portal.PortletDefaultTableDataModel;
import org.olat.core.gui.control.generic.portal.PortletEntry;
import org.olat.core.gui.control.generic.portal.PortletToolSortingControllerImpl;
import org.olat.core.gui.control.generic.portal.SortingCriteria;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.event.PersistsEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Run view controller for the notifications list portlet
 * <P>
 * Initial Date:  11.07.2005 <br>
 * @author gnaegi
 */
public class NotificationsPortletRunController extends AbstractPortletRunController<Subscriber> implements GenericEventListener {
	
	private static final String CMD_LAUNCH = "cmd.launch";

	private TableController tableCtr;
	private NotificationsPortletTableDataModel notificationListModel;
	private VelocityContainer notificationsVC;	
	private boolean needsModelReload = false;
	private List<Subscriber> notificationsList;
	private Link showAllLink;

	private Date compareDate;

	@Autowired
	private NotificationsManager man;

	/**
	 * Constructor
	 * @param ureq
	 * @param component
	 */
	public NotificationsPortletRunController(WindowControl wControl, UserRequest ureq, Translator trans,
			String portletName, int defaultMaxEntries) { 		
		super(wControl, ureq, trans, portletName, defaultMaxEntries);
		
		sortingTermsList.add(SortingCriteria.TYPE_SORTING);
		sortingTermsList.add(SortingCriteria.ALPHABETICAL_SORTING);
		sortingTermsList.add(SortingCriteria.DATE_SORTING);
		
		notificationsVC = createVelocityContainer("notificationsPortlet");		
		showAllLink = LinkFactory.createLink("notificationsPortlet.showAll", notificationsVC, this);
		showAllLink.setIconRightCSS("o_icon o_icon_start");
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(trans.translate("notificationsPortlet.nonotifications"), null, "o_icon_notification");
		tableConfig.setDisplayTableHeader(false);
		tableConfig.setCustomCssClass("o_portlet_table");
		tableConfig.setDisplayRowCount(false);
		tableConfig.setPageingEnabled(false);
		tableConfig.setDownloadOffered(false);
    //disable the default sorting for this table
		tableConfig.setSortingEnabled(false); 
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), trans);
		listenTo(tableCtr);
		
		// dummy header key, won't be used since setDisplayTableHeader is set to false
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("notificationsPortlet.bgname", 0, CMD_LAUNCH, trans.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("notificationsPortlet.type", 1, null, trans.getLocale(), ColumnDescriptor.ALIGNMENT_RIGHT));
	
		sortingCriteria = getPersistentSortingConfiguration(ureq);

		// default use the interval
		compareDate = man.getCompareDateFromInterval(man.getUserIntervalOrDefault(ureq.getIdentity()));
		reloadModel(sortingCriteria);

		notificationsVC.put("table", tableCtr.getInitialComponent());

		putInitialPanel(notificationsVC);
		
		man.registerAsListener(this, ureq.getIdentity());
		getWindowControl().getWindowBackOffice().addCycleListener(this);
	}
	
	private List<PortletEntry<Subscriber>> getAllPortletEntries() {
		notificationsList = man.getValidSubscribers(getIdentity());
		// calc subscriptioninfo for all subscriptions and, if only those with news are to be shown, remove the other ones
		for (Iterator<Subscriber> it_subs = notificationsList.iterator(); it_subs.hasNext();) {
			Subscriber subscriber = it_subs.next();
			Publisher pub = subscriber.getPublisher();
			NotificationsHandler notifHandler = man.getNotificationsHandler(pub);
			if(notifHandler == null) {
				it_subs.remove();
			} else {
				SubscriptionInfo subsInfo = notifHandler.createSubscriptionInfo(subscriber, getLocale(), compareDate);
				if (!subsInfo.hasNews()) {
					it_subs.remove();
				}
			}
		}
		return convertNotificationToPortletEntryList(notificationsList);
	}
	
	private List<PortletEntry<Subscriber>> convertNotificationToPortletEntryList(List<Subscriber> items) {
		List<PortletEntry<Subscriber>> convertedList = new ArrayList<>();
		Iterator<Subscriber> listIterator = items.iterator();
		while(listIterator.hasNext()) {
			convertedList.add(new SubscriberPortletEntry(listIterator.next()));
		}
		return convertedList;
	}
	
	@Override
	protected void reloadModel(SortingCriteria sortingCriteria) {
  	if (sortingCriteria.getSortingType() == SortingCriteria.AUTO_SORTING) {
  		Map<Subscriber,SubscriptionInfo> subscriptionMap = NotificationHelper.getSubscriptionMap(getIdentity(), getLocale(), true, compareDate);
			
			notificationsList = new ArrayList<>();
			for (Iterator<Map.Entry<Subscriber, SubscriptionInfo>> it_subs = subscriptionMap.entrySet().iterator(); it_subs.hasNext();) {
				Map.Entry<Subscriber, SubscriptionInfo> sInfo = it_subs.next();
				Subscriber subscrer = sInfo.getKey();
				SubscriptionInfo infos = sInfo.getValue();
				if(infos.hasNews()) {
					notificationsList.add(subscrer);
				}
			}
			notificationsList = getSortedList(notificationsList, sortingCriteria );		
			List<PortletEntry<Subscriber>> entries = convertNotificationToPortletEntryList(notificationsList);
			notificationListModel = new NotificationsPortletTableDataModel(entries, getLocale(), subscriptionMap);
			tableCtr.setTableDataModel(notificationListModel);
  	} else {
			reloadModel(getPersistentManuallySortedItems());
		}  	
  }
	
	@Override
	protected void reloadModel(List<PortletEntry<Subscriber>> sortedItems) {
		Map<Subscriber, SubscriptionInfo> subscriptionMap = NotificationHelper.getSubscriptionMap(getIdentity(), getLocale(), true, compareDate);
		notificationListModel = new NotificationsPortletTableDataModel(sortedItems, getLocale(), subscriptionMap);
		tableCtr.setTableDataModel(notificationListModel);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == showAllLink){
			// activate homes tab in top navigation and active bookmarks menu item
			String resourceUrl = "[HomeSite:" + ureq.getIdentity().getKey() + "][notifications:0]";
			BusinessControl bc = BusinessControlFactory.getInstance().createFromString(resourceUrl);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
			NewControllerFactory.getInstance().launch(ureq, bwControl);
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				if (actionid.equals(CMD_LAUNCH)) {
					int rowid = te.getRowId();
					Subscriber sub = notificationListModel.getSubscriberAt(rowid);
					NotificationUIFactory.launchSubscriptionResource(ureq, getWindowControl(), sub);
				}
			}
		} 
	}

	@Override
	protected void doDispose() {
		super.doDispose();
		man.deregisterAsListener(this);
		getWindowControl().getWindowBackOffice().removeCycleListener(this);
	}

	@Override
	public void event(Event event) {
		// check if our tablemodel -is- affected (see NotificationsManager where the event is fired),
		// (if we are subscriber of the publisher which data has changed)
		if (event instanceof PersistsEvent) {
			PersistsEvent pe = (PersistsEvent) event;
			if (pe.isAtLeastOneKeyInList(notificationsList)) {
				needsModelReload = true;
			}
		} else if (event == Window.BEFORE_INLINE_RENDERING && needsModelReload) {
			reloadModel(sortingCriteria);
			needsModelReload = false;
		}
	}
	
	/**
	 * Retrieves the persistent sortingCriteria and the persistent manually sorted, if any, 
	 * creates the table model for the manual sorting, and instantiates the PortletToolSortingControllerImpl.
	 * @param ureq
	 * @param wControl
	 * @return a PortletToolSortingControllerImpl instance.
	 */
	protected PortletToolSortingControllerImpl<Subscriber> createSortingTool(UserRequest ureq, WindowControl wControl) {
		if(portletToolsController==null) {			
						
			Map<Subscriber,SubscriptionInfo> subscriptionMap = NotificationHelper.getSubscriptionMap(ureq.getIdentity(), ureq.getLocale(), true, compareDate);
			List<PortletEntry<Subscriber>> entries = getAllPortletEntries();
			PortletDefaultTableDataModel<Subscriber> tableDataModel = new NotificationsManualSortingTableDataModel(entries, ureq.getLocale(), subscriptionMap);
			List<PortletEntry<Subscriber>> sortedItems = getPersistentManuallySortedItems(); 
			
			portletToolsController = new PortletToolSortingControllerImpl<>(ureq, wControl, getTranslator(), sortingCriteria, tableDataModel, sortedItems);
			portletToolsController.setConfigManualSorting(true);
			portletToolsController.setConfigAutoSorting(true);
			portletToolsController.addControllerListener(this);
		}		
		return portletToolsController;
	}
	
	private List<PortletEntry<Subscriber>> getPersistentManuallySortedItems() {
		List<PortletEntry<Subscriber>> entries = getAllPortletEntries();
		return getPersistentManuallySortedItems(entries);
	}

	 protected Comparator<Subscriber> getComparator(final SortingCriteria sortingCriteria) {
			return new Comparator<>(){			
				public int compare(final Subscriber subscriber1, final Subscriber subscriber2) {	
					int comparisonResult = 0;
				  if(sortingCriteria.getSortingTerm()==SortingCriteria.ALPHABETICAL_SORTING) {			  	
				  	comparisonResult = collator.compare(subscriber1.getPublisher().getResName(), subscriber1.getPublisher().getResName());			  		  	
				  } else if(sortingCriteria.getSortingTerm()==SortingCriteria.DATE_SORTING) {
				  	comparisonResult = subscriber1.getLastModified().compareTo(subscriber1.getLastModified());
				  } else if(sortingCriteria.getSortingTerm()==SortingCriteria.TYPE_SORTING) {
				  	String type1 = NewControllerFactory.translateResourceableTypeName(subscriber1.getPublisher().getType(), getTranslator().getLocale());
				  	String type2 = NewControllerFactory.translateResourceableTypeName(subscriber2.getPublisher().getType(), getTranslator().getLocale());
				  	comparisonResult = type1.compareTo(type2);
				  }
				  if(!sortingCriteria.isAscending()) {
				  	//if not isAscending return (-comparisonResult)			  	
				  	return -comparisonResult;
				  }
				  return comparisonResult;
				}};
		}
	 
	 /**
	  * 
	  * PortletDefaultTableDataModel implementation for the current portlet.
	  * 
	  * <P>
	  * Initial Date:  10.12.2007 <br>
	  * @author Lavinia Dumitrescu
	  */
	 private class NotificationsPortletTableDataModel extends PortletDefaultTableDataModel<Subscriber>  {
		 private Locale locale;
		 private final Map<Subscriber,SubscriptionInfo> subToSubInfo;
		 
		 public NotificationsPortletTableDataModel(List<PortletEntry<Subscriber>> objects, Locale locale, Map<Subscriber,SubscriptionInfo> subToSubInfo) {
			 super(objects, 2);
			 this.locale = locale;
			 this.subToSubInfo = subToSubInfo;
		 }
		 
		 public Object getValueAt(int row, int col) {
			 PortletEntry<Subscriber> entry = getObject(row);
				Subscriber subscriber = entry.getValue();
				Publisher pub = subscriber.getPublisher();
				switch (col) {
					case 0:
						Object subsInfoObj = subToSubInfo.get(subscriber);
						if(subsInfoObj instanceof SubscriptionInfo) {
							SubscriptionInfo subsInfo = (SubscriptionInfo)subsInfoObj;
							int newsCount = subsInfo.countSubscriptionListItems();
							if (newsCount == 1) {
								return translate("notificationsPortlet.single.news.in", subsInfo.getTitle(SubscriptionInfo.MIME_PLAIN));																
							} else {
								return translate("notificationsPortlet.multiple.news.in", new String[]{newsCount + "" , subsInfo.getTitle(SubscriptionInfo.MIME_PLAIN)});								
							}
						}
						return "";
					case 1:					
						String innerType = pub.getType();
						return NewControllerFactory.translateResourceableTypeName(innerType, locale);
					default:
						return "ERROR";
				}
			}
		 
		public Subscriber getSubscriberAt(int row) {
			return getObject(row).getValue();
		}
	}
	 
	 /**
	  * Initial Date:  04.12.2007 <br>
	  * @author Lavinia Dumitrescu
	  */
	 private class NotificationsManualSortingTableDataModel extends PortletDefaultTableDataModel<Subscriber>  {		
			private Locale locale;
			private final Map<Subscriber,SubscriptionInfo> subToSubInfo;
			/**
			 * @param objects
			 * @param locale
			 */
			public NotificationsManualSortingTableDataModel(List<PortletEntry<Subscriber>> objects, Locale locale, Map<Subscriber,SubscriptionInfo> subToSubInfo) {
				super(objects, 3);
				this.locale = locale;
				this.subToSubInfo = subToSubInfo;
			}

			@Override
			public final Object getValueAt(int row, int col) {
				PortletEntry<Subscriber> entry = getObject(row);
				Subscriber subscriber = entry.getValue();
				Publisher pub = subscriber.getPublisher();
				switch (col) {
					case 0: {				
						SubscriptionInfo subsInfo = subToSubInfo.get(subscriber);
						return subsInfo.getTitle(SubscriptionInfo.MIME_PLAIN);
					}
					case 1: {
						SubscriptionInfo subsInfo = subToSubInfo.get(subscriber);
						if (!subsInfo.hasNews()) return "-";
						return subsInfo.getSpecificInfo(SubscriptionInfo.MIME_HTML, locale);
					}
					case 2:
						String innerType = pub.getType();
						return NewControllerFactory.translateResourceableTypeName(innerType, locale);
					default:
						return "error";
				}
			}
			
		}
	 
	 /**
	  * 
	  * PortletEntry 
	  * 
	  * <P>
	  * Initial Date:  10.12.2007 <br>
	  * @author Lavinia Dumitrescu
	  */
	 private class SubscriberPortletEntry implements PortletEntry<Subscriber> {
	  	private Subscriber value;
	  	private Long key;
	  	
	  	public SubscriberPortletEntry(Subscriber group) {
	  		value = group;
	  		key = group.getKey();
	  	}
	  	
	  	public Long getKey() {
	  		return key;
	  	}
	  	
	  	public Subscriber getValue() {
	  		return value;
	  	}
	  }
	
}
