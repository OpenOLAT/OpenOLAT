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

import org.olat.commons.calendar.CalendarManagedFlag;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarModule;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarEventLink;
import org.olat.commons.calendar.model.KalendarRecurEvent;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.commons.calendar.ui.events.CalendarGUIDeleteEvent;
import org.olat.commons.calendar.ui.events.CalendarGUIUpdateEvent;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.controllers.linkchooser.CustomMediaChooserController;
import org.olat.core.commons.controllers.linkchooser.CustomMediaChooserFactory;
import org.olat.core.commons.controllers.linkchooser.URLChoosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.tabbedpane.TabbedPaneChangedEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.helpers.Settings;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;


public class CalendarEntryDetailsController extends BasicController {

	private Collection<KalendarRenderWrapper> availableCalendars;
	private boolean isNew, isReadOnly;
	private KalendarEvent kalendarEvent;
	private VelocityContainer linkVC;
	private TabbedPane pane;
	private CalendarEntryForm eventForm;
	private CloseableModalController cmc;
	private LinkProvider activeLinkProvider;
	private ConfirmDeleteController deleteCtr;
	private ConfirmUpdateController updateCtr;
	private CustomMediaChooserController customMediaChooserCtr;
	private ExternalLinksController externalLinksController;
	private MediaLinksController mediaLinksController;

	@Autowired
	private CalendarManager calendarManager;

	public CalendarEntryDetailsController(UserRequest ureq, KalendarEvent kalendarEvent, KalendarRenderWrapper calendarWrapper,
			List<KalendarRenderWrapper> availableCalendars, boolean isNew, String caller, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(CalendarModule.class, ureq.getLocale()));
		
		this.availableCalendars = availableCalendars;
		this.kalendarEvent = kalendarEvent;
		this.isNew = isNew;
		
		pane = new TabbedPane("pane", getLocale());
		pane.addListener(this);
		
		eventForm = new CalendarEntryForm(ureq, wControl, kalendarEvent, calendarWrapper, availableCalendars, isNew, caller);
		listenTo(eventForm);
		isReadOnly = calendarWrapper == null ? true : calendarWrapper.getAccess() == KalendarRenderWrapper.ACCESS_READ_ONLY;

		pane.addTab(translate("tab.event"), eventForm.getInitialComponent());
		
		linkVC = createVelocityContainer ("calEditLinks");
		linkVC.contextPut("caller", caller);
		if (!isReadOnly && !CalendarManagedFlag.isManaged(kalendarEvent, CalendarManagedFlag.links)) {
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
		putInitialPanel(pane);
	}

	@Override
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
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == deleteCtr) {
			if(event instanceof CalendarGUIDeleteEvent) {
				doDelete((CalendarGUIDeleteEvent)event);
				cmc.deactivate();
				cleanUp();
				
				fireEvent(ureq, Event.DONE_EVENT);
			} else {
				cmc.deactivate();
				cleanUp();
			}
		} else if (source == activeLinkProvider) {
			if(kalendarEvent.getCalendar() != null) {
				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else if (source == eventForm) {
			if (event == Event.DONE_EVENT) {
				doSave(ureq);
			} else if("delete".equals(event.getCommand())) {
				doConfirmDelete(ureq);
			} else if (event == Event.CANCELLED_EVENT) {
				eventForm.setEntry(kalendarEvent);
				// user canceled, finish workflow
				fireEvent(ureq, Event.DONE_EVENT);
			}
		} else if(source == updateCtr) {
			if(event instanceof CalendarGUIUpdateEvent) {
				doUpdate((CalendarGUIUpdateEvent)event);
				cmc.deactivate();
				cleanUp();
				
				fireEvent(ureq, Event.DONE_EVENT);
			} else {
				cmc.deactivate();
				cleanUp();
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
				doneSuccessfully = calendarManager.updateEventFrom(cal, kalendarEvent);
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
				boolean doneSuccessfully = calendarManager.updateEventFrom(cal, kalendarEvent);
				if (doneSuccessfully) {
					fireEvent(ureq, Event.DONE_EVENT);
				} else {
					showError("cal.error.save");
					fireEvent(ureq, Event.FAILED_EVENT);
				}
			}
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(deleteCtr);
		removeAsListenerAndDispose(updateCtr);
		removeAsListenerAndDispose(cmc);
		updateCtr = null;
		deleteCtr = null;
		cmc = null;
	}
	
	private void doConfirmDelete(UserRequest ureq) {
		deleteCtr = new ConfirmDeleteController(ureq, getWindowControl(), kalendarEvent);
		listenTo(deleteCtr);
		
		String title = translate("cal.edit.delete");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteCtr.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDelete(CalendarGUIDeleteEvent event) {
		switch(event.getCascade()) {
			case all: calendarManager.removeEventFrom(kalendarEvent.getCalendar(), kalendarEvent); break;
			case once: {
				if(kalendarEvent instanceof KalendarRecurEvent) {
					calendarManager.removeOccurenceOfEvent(kalendarEvent.getCalendar(), (KalendarRecurEvent)kalendarEvent);
				}
				break;
			}
			case future: {
				if(kalendarEvent instanceof KalendarRecurEvent) {
					calendarManager.removeFutureOfEvent(kalendarEvent.getCalendar(), (KalendarRecurEvent)kalendarEvent);
				}
				break;
			}
		}
	}
	
	private void doSave(UserRequest ureq) {
		// ok, save edited entry
		kalendarEvent = eventForm.getUpdatedKalendarEvent();

		if (isNew) {
			boolean doneSuccessfully = true;
			// this is a new event, add event to calendar
			String calendarID = eventForm.getChoosenKalendarID();
			for (Iterator<KalendarRenderWrapper> iter = availableCalendars.iterator(); iter.hasNext();) {
				KalendarRenderWrapper calendarWrapper = iter.next();
				if (!calendarWrapper.getKalendar().getCalendarID().equals(calendarID)) {
					continue;
				}
				Kalendar cal = calendarWrapper.getKalendar();
				boolean result = calendarManager.addEventTo(cal, kalendarEvent);
				if (result == false) {
					// if one failed => done not successfully
					doneSuccessfully = false;
				}
			}
			reportSaveStatus(ureq, doneSuccessfully);	
		} else if(kalendarEvent instanceof KalendarRecurEvent && !StringHelper.containsNonWhitespace(kalendarEvent.getRecurrenceID())) {
			updateCtr = new ConfirmUpdateController(ureq, getWindowControl(), (KalendarRecurEvent)kalendarEvent);
			listenTo(updateCtr);

			String title = translate("cal.edit.update");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), updateCtr.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		} else {
			// this is an existing event, so we get the previousely assigned calendar from the event
			Kalendar cal = kalendarEvent.getCalendar();
			boolean doneSuccessfully = calendarManager.updateEventFrom(cal, kalendarEvent);
			reportSaveStatus(ureq, doneSuccessfully);
		}
	}
	
	private void doUpdate(CalendarGUIUpdateEvent event) {
		switch(event.getCascade()) {
			case all: {
				calendarManager.updateEventFrom(kalendarEvent.getCalendar(), kalendarEvent);
				break;
			}
			case once: {
				if(kalendarEvent instanceof KalendarRecurEvent) {
					KalendarRecurEvent refEvent = (KalendarRecurEvent)kalendarEvent;
					kalendarEvent = calendarManager.createKalendarEventRecurringOccurence(refEvent);
					calendarManager.addEventTo(refEvent.getCalendar(), kalendarEvent);
				}
				break;
			}
		}
	}
	
	private void reportSaveStatus(UserRequest ureq, boolean doneSuccessfully) {
		// check if event is still available
		if (doneSuccessfully) {
			// saving was ok, finish workflow
			fireEvent(ureq, Event.DONE_EVENT);
		} else {
			showError("cal.error.save");
			fireEvent(ureq, Event.FAILED_EVENT);
		}	
	}

	public KalendarEvent getKalendarEvent() {
		return kalendarEvent;
	}
}
