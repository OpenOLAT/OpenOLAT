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

package org.olat.commons.calendar.ui;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarManagerFactory;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarEventLink;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.controllers.linkchooser.CustomMediaChooserController;
import org.olat.core.commons.controllers.linkchooser.CustomMediaChooserFactory;
import org.olat.core.commons.controllers.linkchooser.URLChoosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.tabbedpane.TabbedPaneChangedEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.helpers.Settings;
import org.olat.core.util.StringHelper;


public class CalendarEntryDetailsController extends BasicController {

	private Collection<KalendarRenderWrapper> availableCalendars;
	private boolean isNew, isReadOnly;
	private KalendarEvent kalendarEvent;
	private StackedPanel mainPanel;
	private VelocityContainer mainVC, eventVC, linkVC;
	private TabbedPane pane;
	private CalendarEntryForm eventForm;
	private LinkProvider activeLinkProvider;
	private CustomMediaChooserController customMediaChooserCtr;
	private DialogBoxController deleteYesNoController;
	private CopyEventToCalendarController copyEventToCalendarController;
	private ExternalLinksController externalLinksController;
	private MediaLinksController mediaLinksController;
	private Link deleteButton;

	public CalendarEntryDetailsController(UserRequest ureq, KalendarEvent kalendarEvent, KalendarRenderWrapper calendarWrapper,
			List<KalendarRenderWrapper> availableCalendars, boolean isNew, String caller, WindowControl wControl) {
		super(ureq, wControl);
		
		setBasePackage(CalendarManager.class);
		
		this.availableCalendars = availableCalendars;
		this.kalendarEvent = kalendarEvent;
		this.isNew = isNew;
		
		mainVC = createVelocityContainer ("calEditMain");
		mainVC.contextPut("caller", caller);
		pane = new TabbedPane("pane", getLocale());
		pane.addListener(this);
		mainVC.put("pane", pane);
		
		eventVC = createVelocityContainer("calEditDetails");
		deleteButton = LinkFactory.createButton("cal.edit.delete", eventVC, this);
		eventVC.contextPut("caller", caller);
		eventForm = new CalendarEntryForm(ureq, wControl, kalendarEvent, calendarWrapper, availableCalendars, isNew);
		listenTo(eventForm);
		eventVC.put("eventForm", eventForm.getInitialComponent());
		eventVC.contextPut("isNewEvent", new Boolean(isNew));
		isReadOnly = calendarWrapper == null ? true : calendarWrapper.getAccess() == KalendarRenderWrapper.ACCESS_READ_ONLY;
		eventVC.contextPut("isReadOnly", new Boolean(isReadOnly));
		pane.addTab(translate("tab.event"), eventVC);
		
		//linkVC = new VelocityContainer("calEditLinks", VELOCITY_ROOT + "/calEditLinks.html", getTranslator(), this);
		linkVC = createVelocityContainer ("calEditLinks");
		linkVC.contextPut("caller", caller);
		if (!isReadOnly) {
			//course node links
			pane.addTab(translate("tab.links"), linkVC);
			
			//custom media chooser
			if (CoreSpringFactory.containsBean(CustomMediaChooserFactory.class.getName())) {
				CustomMediaChooserFactory customMediaChooserFactory = (CustomMediaChooserFactory) CoreSpringFactory.getBean(CustomMediaChooserFactory.class.getName());
				customMediaChooserCtr = customMediaChooserFactory.getInstance(ureq, wControl); 
				if (customMediaChooserCtr != null) {
					listenTo(customMediaChooserCtr);
					mediaLinksController = new MediaLinksController(ureq, wControl, kalendarEvent, customMediaChooserFactory);
					pane.addTab(customMediaChooserCtr.getTabbedPaneTitle(), mediaLinksController.getInitialComponent());	
					listenTo(mediaLinksController);
				}				
			}
			
			//list of links
			externalLinksController = new ExternalLinksController(ureq, wControl, kalendarEvent);
			pane.addTab(translate("tab.links.extern"), externalLinksController.getInitialComponent());
			listenTo(externalLinksController);
		}
		
		// wrap everything in a panel
		mainPanel = putInitialPanel(mainVC);
	}

	public void event(UserRequest ureq, Component source, Event event) {
		if (source == pane) {
			if (event instanceof TabbedPaneChangedEvent) {
				// prepare links tab
				TabbedPaneChangedEvent tpce = (TabbedPaneChangedEvent)event;
				if (tpce.getNewComponent().equals(linkVC)) {
					// display link provider if any
					String calendarID = eventForm.getChoosenKalendarID();
					KalendarRenderWrapper calendarWrapper = null;
					for (Iterator<KalendarRenderWrapper> iter = availableCalendars.iterator(); iter.hasNext();) {
						calendarWrapper = iter.next();
						if (calendarWrapper.getKalendar().getCalendarID().equals(calendarID)) {
							break;
						}
					}
					
					if(activeLinkProvider == null) {
						activeLinkProvider = calendarWrapper.getLinkProvider();
						if (activeLinkProvider != null) {
							activeLinkProvider.addControllerListener(this);
							activeLinkProvider.setKalendarEvent(kalendarEvent);
							activeLinkProvider.setDisplayOnly(isReadOnly);
							linkVC.put("linkprovider", activeLinkProvider.getControler().getInitialComponent());
							linkVC.contextPut("hasLinkProvider", Boolean.TRUE);
						} else {
							linkVC.contextPut("hasLinkProvider", Boolean.FALSE);
						}
					}
				}
			}
		}  else if (source == deleteButton) {
			// delete calendar entry
			deleteYesNoController = activateYesNoDialog(ureq, null, translate("cal.delete.dialogtext"), deleteYesNoController);
			return;
		}
	}

	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == deleteYesNoController) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				Kalendar cal = kalendarEvent.getCalendar();
				CalendarManagerFactory.getInstance().getCalendarManager().removeEventFrom(cal,kalendarEvent);
				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else if (source == copyEventToCalendarController) {
			if (event.equals(Event.DONE_EVENT))
				fireEvent(ureq, Event.DONE_EVENT);
			else if (event.equals(Event.CANCELLED_EVENT)){
				eventForm.setMulti(false);// OO-61
				mainPanel.setContent(mainVC);
			}
		} else if (source == activeLinkProvider) {
			if(kalendarEvent.getCalendar() != null) {
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}else if (source == eventForm) {
			if (event == Event.DONE_EVENT) {
				// ok, save edited entry
				kalendarEvent = eventForm.getUpdatedKalendarEvent();
				boolean doneSuccessfully = true;
				if (isNew) {
					// this is a new event, add event to calendar
					String calendarID = eventForm.getChoosenKalendarID();
					for (Iterator<KalendarRenderWrapper> iter = availableCalendars.iterator(); iter.hasNext();) {
						KalendarRenderWrapper calendarWrapper = iter.next();
						if (!calendarWrapper.getKalendar().getCalendarID().equals(calendarID)) continue;
						Kalendar cal = calendarWrapper.getKalendar();
						boolean result = CalendarManagerFactory.getInstance().getCalendarManager().addEventTo(cal, kalendarEvent);
						if (result==false) {
							// if one failed => done not successfully
							doneSuccessfully = false;
						}
					}
				} else {
					// this is an existing event, so we get the previousely assigned calendar from the event
					Kalendar cal = kalendarEvent.getCalendar();
					doneSuccessfully =CalendarManagerFactory.getInstance().getCalendarManager().updateEventFrom(cal, kalendarEvent);
				}
				// check if event is still available
				if (!doneSuccessfully) {
					showError("cal.error.save");
					fireEvent(ureq, Event.FAILED_EVENT);
					return;
				}				
				
				if (eventForm.isMulti()) {
					// offer to copy event to multiple calendars.
					removeAsListenerAndDispose(copyEventToCalendarController);
					copyEventToCalendarController = new CopyEventToCalendarController(ureq, getWindowControl(), kalendarEvent, availableCalendars);
					listenTo(copyEventToCalendarController);
					//copyEventToCalendarController.addControllerListener(this);
					mainPanel.setContent(copyEventToCalendarController.getInitialComponent());
					return;
				}
			
				// saving was ok, finish workflow
				fireEvent(ureq, Event.DONE_EVENT);

			} else if (event == Event.CANCELLED_EVENT) {
				eventForm.setEntry(kalendarEvent);
				// user canceled, finish workflow
				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else if (source == customMediaChooserCtr) {
			boolean doneSuccessfully = true;
			if(event instanceof URLChoosenEvent) {
				URLChoosenEvent urlEvent = (URLChoosenEvent)event;
				String url = urlEvent.getURL();
				List<KalendarEventLink> links = kalendarEvent.getKalendarEventLinks();
				
				String provider = customMediaChooserCtr.getClass().getSimpleName();
				String id = url;
				String displayName = StringHelper.containsNonWhitespace(urlEvent.getDisplayName()) ? urlEvent.getDisplayName() : url;
				String uri = url.contains("://") ? url : (Settings.getServerContextPathURI() + url);
				String iconCssClass = urlEvent.getIconCssClass();
				if(!StringHelper.containsNonWhitespace(iconCssClass)) {
					iconCssClass = CSSHelper.createFiletypeIconCssClassFor(url);
				}
				links.add(new KalendarEventLink(provider, id, displayName, uri, iconCssClass));
			
				Kalendar cal = kalendarEvent.getCalendar();
				doneSuccessfully = CalendarManagerFactory.getInstance().getCalendarManager().updateEventFrom(cal, kalendarEvent);
			}
			
			if (doneSuccessfully) {
				fireEvent(ureq, event);
			} else {
				showError("cal.error.save");
				fireEvent(ureq, Event.FAILED_EVENT);
			}
		} else if (source == externalLinksController || source == mediaLinksController) {
			//save externals links
			Kalendar cal = kalendarEvent.getCalendar();
			if (kalendarEvent.getCalendar() != null) {
				boolean doneSuccessfully = CalendarManagerFactory.getInstance().getCalendarManager().updateEventFrom(cal, kalendarEvent);
				if (doneSuccessfully) {
					fireEvent(ureq, Event.DONE_EVENT);
				} else {
					showError("cal.error.save");
					fireEvent(ureq, Event.FAILED_EVENT);
				}
			}
		}
	}
	
	protected void doDispose() {
		//
	}

	public KalendarEvent getKalendarEvent() {
		return kalendarEvent;
	}

}
