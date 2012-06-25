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

package org.olat.notifications;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.ControllerFactory;
import org.olat.NewControllerFactory;
import org.olat.commons.calendar.ui.CalendarController;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.util.ComponentUtil;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.DTab;
import org.olat.core.gui.control.generic.dtabs.DTabs;
import org.olat.core.gui.control.generic.portal.AbstractPortletRunController;
import org.olat.core.gui.control.generic.portal.PortletDefaultTableDataModel;
import org.olat.core.gui.control.generic.portal.PortletEntry;
import org.olat.core.gui.control.generic.portal.PortletToolSortingControllerImpl;
import org.olat.core.gui.control.generic.portal.SortingCriteria;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.event.PersistsEvent;
import org.olat.core.util.notifications.NotificationHelper;
import org.olat.core.util.notifications.NotificationsHandler;
import org.olat.core.util.notifications.NotificationsManager;
import org.olat.core.util.notifications.Publisher;
import org.olat.core.util.notifications.Subscriber;
import org.olat.core.util.notifications.SubscriptionInfo;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupModule;
import org.olat.group.BusinessGroupService;

/**
 * Description:<br>
 * Run view controller for the notifications list portlet
 * <P>
 * Initial Date:  11.07.2005 <br>
 * @author gnaegi
 */
public class NotificationsPortletRunController extends AbstractPortletRunController implements GenericEventListener {
	
	private static final String CMD_LAUNCH = "cmd.launch";

	private TableController tableCtr;
	//private NotificationsMiniTableModel notificationListModel;
	private NotificationsPortletTableDataModel notificationListModel;
	private VelocityContainer notificationsVC;	
	private boolean needsModelReload = false;
	private List<Subscriber> notificationsList;
	private Link showAllLink;

	private Date compareDate;

	private NotificationsManager man;
	
	
	/**
	 * Constructor
	 * @param ureq
	 * @param component
	 */
	public NotificationsPortletRunController(WindowControl wControl, UserRequest ureq, Translator trans, String portletName) { 		
		super(wControl, ureq, trans, portletName);
		
		sortingTermsList.add(SortingCriteria.TYPE_SORTING);
		sortingTermsList.add(SortingCriteria.ALPHABETICAL_SORTING);
		sortingTermsList.add(SortingCriteria.DATE_SORTING);
		
		this.notificationsVC = this.createVelocityContainer("notificationsPortlet");		
		showAllLink = LinkFactory.createLink("notificationsPortlet.showAll", notificationsVC, this);
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(trans.translate("notificationsPortlet.nonotifications"));
		tableConfig.setDisplayTableHeader(false);
		tableConfig.setCustomCssClass("b_portlet_table");
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
	
		this.sortingCriteria = getPersistentSortingConfiguration(ureq);
		man = NotificationsManager.getInstance();
		// default use the interval
		compareDate = man.getCompareDateFromInterval(man.getUserIntervalOrDefault(ureq.getIdentity()));
		reloadModel(sortingCriteria);

		this.notificationsVC.put("table", tableCtr.getInitialComponent());
		// notify us whenever we will be shown on screen shortly, so that we can reload the model if we received a subscription changed event in the meantime
		ComponentUtil.registerForValidateEvents(notificationsVC, this);
		
		putInitialPanel(notificationsVC);
		
		man.registerAsListener(this, ureq.getIdentity());
	}
	
	private List<PortletEntry> getAllPortletEntries() {
		notificationsList = man.getValidSubscribers(identity);
		// calc subscriptioninfo for all subscriptions and, if only those with news are to be shown, remove the other ones
		for (Iterator<Subscriber> it_subs = notificationsList.iterator(); it_subs.hasNext();) {
			Subscriber subscriber = it_subs.next();
			Publisher pub = subscriber.getPublisher();
			NotificationsHandler notifHandler = man.getNotificationsHandler(pub);
			if(notifHandler == null) {
				it_subs.remove();
			} else {
				SubscriptionInfo subsInfo = notifHandler.createSubscriptionInfo(subscriber, locale, compareDate);
				if (!subsInfo.hasNews()) {
					it_subs.remove();
				}
			}
		}
		return convertNotificationToPortletEntryList(notificationsList);
	}
	
	private List<PortletEntry> convertNotificationToPortletEntryList(List<Subscriber> items) {
		List<PortletEntry> convertedList = new ArrayList<PortletEntry>();
		Iterator<Subscriber> listIterator = items.iterator();
		while(listIterator.hasNext()) {
			convertedList.add(new SubscriberPortletEntry(listIterator.next()));
		}
		return convertedList;
	}
	
	/**
	 * 
	 * @see org.olat.core.gui.control.generic.portal.AbstractPortletRunController#reloadModel(org.olat.core.gui.UserRequest, org.olat.core.gui.control.generic.portal.SortingCriteria)
	 */
  protected void reloadModel(SortingCriteria sortingCriteria) {
  	if (sortingCriteria.getSortingType() == SortingCriteria.AUTO_SORTING) {
  		Map<Subscriber,SubscriptionInfo> subscriptionMap = NotificationHelper.getSubscriptionMap(getIdentity(), getLocale(), true, compareDate);
			
			notificationsList = new ArrayList<Subscriber>();
			for (Iterator<Map.Entry<Subscriber, SubscriptionInfo>> it_subs = subscriptionMap.entrySet().iterator(); it_subs.hasNext();) {
				Map.Entry<Subscriber, SubscriptionInfo> sInfo = it_subs.next();
				Subscriber subscrer = sInfo.getKey();
				SubscriptionInfo infos = sInfo.getValue();
				if(infos.hasNews()) {
					notificationsList.add(subscrer);
				}
			}
			notificationsList = getSortedList(notificationsList, sortingCriteria );		
			List<PortletEntry> entries = convertNotificationToPortletEntryList(notificationsList);
			notificationListModel = new NotificationsPortletTableDataModel(entries, locale, subscriptionMap);
			tableCtr.setTableDataModel(notificationListModel);
  	} else {
			reloadModel(this.getPersistentManuallySortedItems());
		}  	
  }
	
  /**
   * 
   * @see org.olat.core.gui.control.generic.portal.AbstractPortletRunController#reloadModel(org.olat.core.gui.UserRequest, java.util.List)
   */
	protected void reloadModel(List<PortletEntry> sortedItems) {
		Map subscriptionMap = NotificationHelper.getSubscriptionMap(getIdentity(), getLocale(), true, compareDate);
		notificationListModel = new NotificationsPortletTableDataModel(sortedItems, locale, subscriptionMap);
		tableCtr.setTableDataModel(notificationListModel);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == showAllLink){
			// activate homes tab in top navigation and active bookmarks menu item
			String resourceUrl = "[HomeSite:" + ureq.getIdentity().getKey() + "][notifications:0]";
			BusinessControl bc = BusinessControlFactory.getInstance().createFromString(resourceUrl);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
			NewControllerFactory.getInstance().launch(ureq, bwControl);
		} else if (event == ComponentUtil.VALIDATE_EVENT && needsModelReload) {
			//updateTableModel(ureq.getLocale(), ureq.getIdentity());
			reloadModel(sortingCriteria);
			needsModelReload = false;
		}
	}

	/**
	 * @see org.olat.core.gui.control.ControllerEventListener#dispatchEvent(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				if (actionid.equals(CMD_LAUNCH)) {
					int rowid = te.getRowId();
					Subscriber sub = notificationListModel.getSubscriberAt(rowid);
					if (actionid.equals(CMD_LAUNCH)) {
						Publisher pub = sub.getPublisher();
						if (!man.isPublisherValid(pub)) {
							getWindowControl().setError(getTranslator().translate("error.publisherdeleted"));
						} else {						
							String resName = pub.getResName();
							Long resId = pub.getResId();
							String subidentifier = pub.getSubidentifier();
							if (subidentifier.equals(CalendarController.ACTION_CALENDAR_COURSE)) {
								resName = CourseModule.ORES_TYPE_COURSE;
							}
							if (subidentifier.equals(CalendarController.ACTION_CALENDAR_GROUP)) {
								resName = BusinessGroupModule.ORES_TYPE_GROUP;
							}
							OLATResourceable ores = OresHelper.createOLATResourceableInstance(resName, resId);
							String title = NotificationsManager.getInstance().getNotificationsHandler(pub).createTitleInfo(sub, getLocale());
							DTabs dts = (DTabs)Windows.getWindows(ureq).getWindow(ureq).getAttribute("DTabs");
							//was brasato:: DTabs dts = getWindowControl().getDTabs();
							DTab dt = dts.getDTab(ores);
							if (dt == null) {
								// does not yet exist -> create and add
								dt = dts.createDTab(ores, title);
								if (dt == null) return;
								Controller launchController = ControllerFactory.createLaunchController(ores, subidentifier, ureq, dt.getWindowControl(),
										false);
								
							//try with the new factory controller too
								boolean newFactory = false;
								if(launchController == null) {
									try  {
										String resourceUrl = "[" + resName + ":0][notifications]";
										BusinessControl bc = BusinessControlFactory.getInstance().createFromString(resourceUrl);
									  WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, dt.getWindowControl());
									  NewControllerFactory.getInstance().launch(ureq, bwControl);
										newFactory = true;
									} catch (Exception ex) {
										//fail silently
									}
								}
								
								if(newFactory) {
									//hourra
								}
								else if (launchController == null) { 
									// not possible to launch anymore
									getWindowControl().setWarning(getTranslator().translate("warn.nolaunch"));
								} else {
									dt.setController(launchController);
									dts.addDTab(dt);
									// null: do not reactivate to a certain view here, this happened in ControllerFactory.createLaunchController
									dts.activate(ureq, dt, null);
								}
							} else {
								dts.activate(ureq, dt, subidentifier);
							}
						}
					}
				}
			}
		} 
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		super.doDispose();
		man.deregisterAsListener(this);
	}

	public void event(Event event) {
		// check if our tablemodel -is- affected (see NotificationsManagerImpl where the event is fired),
		// (if we are subscriber of the publisher which data has changed)
		if (event instanceof PersistsEvent) {
			PersistsEvent pe = (PersistsEvent) event;
			if (pe.isAtLeastOneKeyInList(notificationsList)) {
				needsModelReload = true;
			}
		}
	}
	
	/**
	 * Retrieves the persistent sortingCriteria and the persistent manually sorted, if any, 
	 * creates the table model for the manual sorting, and instantiates the PortletToolSortingControllerImpl.
	 * @param ureq
	 * @param wControl
	 * @return a PortletToolSortingControllerImpl instance.
	 */
	protected PortletToolSortingControllerImpl createSortingTool(UserRequest ureq, WindowControl wControl) {
		if(portletToolsController==null) {			
						
			Map<Subscriber,SubscriptionInfo> subscriptionMap = NotificationHelper.getSubscriptionMap(ureq.getIdentity(), ureq.getLocale(), true, compareDate);
			List<PortletEntry> entries = getAllPortletEntries();
			PortletDefaultTableDataModel tableDataModel = new NotificationsManualSortingTableDataModel(entries, ureq.getLocale(), subscriptionMap);
			List sortedItems = getPersistentManuallySortedItems(); 
			
			portletToolsController = new PortletToolSortingControllerImpl(ureq, wControl, getTranslator(), sortingCriteria, tableDataModel, sortedItems);
			portletToolsController.setConfigManualSorting(true);
			portletToolsController.setConfigAutoSorting(true);
			portletToolsController.addControllerListener(this);
		}		
		return portletToolsController;
	}
	
	private List getPersistentManuallySortedItems() {
		List<PortletEntry> entries = getAllPortletEntries();
		return this.getPersistentManuallySortedItems(entries);
	}

	 protected Comparator getComparator(final SortingCriteria sortingCriteria) {
			return new Comparator(){			
				public int compare(final Object o1, final Object o2) {
					Subscriber subscriber1= (Subscriber)o1;
					Subscriber subscriber2 = (Subscriber)o2;		
					int comparisonResult = 0;
				  if(sortingCriteria.getSortingTerm()==SortingCriteria.ALPHABETICAL_SORTING) {			  	
				  	comparisonResult = collator.compare(subscriber1.getPublisher().getResName(), subscriber1.getPublisher().getResName());			  		  	
				  } else if(sortingCriteria.getSortingTerm()==SortingCriteria.DATE_SORTING) {
				  	comparisonResult = subscriber1.getLastModified().compareTo(subscriber1.getLastModified());
				  } else if(sortingCriteria.getSortingTerm()==SortingCriteria.TYPE_SORTING) {
				  	String type1 = ControllerFactory.translateResourceableTypeName(subscriber1.getPublisher().getType(), getTranslator().getLocale());
				  	String type2 = ControllerFactory.translateResourceableTypeName(subscriber2.getPublisher().getType(), getTranslator().getLocale());
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
	 private class NotificationsPortletTableDataModel extends PortletDefaultTableDataModel  {
		 private Locale locale;
		 private final Map subToSubInfo;
		 
		 public NotificationsPortletTableDataModel(List<PortletEntry> objects, Locale locale, Map subToSubInfo) {
			 super(objects, 2);
			 this.locale = locale;
			 this.subToSubInfo = subToSubInfo;
		 }
		 
		 public Object getValueAt(int row, int col) {
			 PortletEntry entry = getObject(row);
				Subscriber subscriber = (Subscriber)entry.getValue();
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
						String typeName = ControllerFactory.translateResourceableTypeName(innerType, locale);
						return typeName;
					default:
						return "ERROR";
				}
			}
		 
		 public Subscriber getSubscriberAt(int row) {
				Subscriber subscriber = (Subscriber) getObject(row).getValue();
				return subscriber;
			}
	 }
	 
	 /**
	  * 
	  * Description:<br>
	  * TODO: Lavinia Dumitrescu Class Description for NotificationsManualSortingTableDataModel
	  * 
	  * <P>
	  * Initial Date:  04.12.2007 <br>
	  * @author Lavinia Dumitrescu
	  */
	 private class NotificationsManualSortingTableDataModel extends PortletDefaultTableDataModel  {		
			private Locale locale;
			private final Map<Subscriber,SubscriptionInfo> subToSubInfo;
			/**
			 * @param objects
			 * @param locale
			 */
			public NotificationsManualSortingTableDataModel(List<PortletEntry> objects, Locale locale, Map<Subscriber,SubscriptionInfo> subToSubInfo) {
				super(objects, 3);
				this.locale = locale;
				this.subToSubInfo = subToSubInfo;
			}

			/**
			 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
			 */
			public final Object getValueAt(int row, int col) {
				PortletEntry entry = getObject(row);
				Subscriber subscriber = (Subscriber) entry.getValue();
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
						String typeName = ControllerFactory.translateResourceableTypeName(innerType, locale);
						return typeName;
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
