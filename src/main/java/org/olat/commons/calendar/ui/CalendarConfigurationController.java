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

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.CalendarManagerFactory;
import org.olat.commons.calendar.ICalTokenGenerator;
import org.olat.commons.calendar.model.ICalToken;
import org.olat.commons.calendar.model.KalendarConfig;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.commons.calendar.ui.events.KalendarGUIAddEvent;
import org.olat.commons.calendar.ui.events.KalendarGUIImportEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

public class CalendarConfigurationController extends BasicController {

	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(CalendarManager.class);

	private static final Object CMD_ADD = "add";
	private static final Object CMD_IMPORT = "import";
	private static final Object CMD_TOGGLE_DISPLAY = "tglvis";
	private static final Object CMD_CHOOSE_COLOR = "cc";
	private static final Object CMD_ICAL_FEED = "if";
	private static final Object CMD_ICAL_REGENERATE = "rf";
	private static final Object CMD_ICAL_REMOVE_FEED = "rmif";
	private static final String PARAM_ID = "id";

	private VelocityContainer configVC;
	private List<KalendarRenderWrapper> calendars;

	private CloseableModalController cmc;
	private CalendarExportController exportController;
	private CalendarColorChooserController colorChooser;

	private DialogBoxController confirmRemoveDialog;
	private DialogBoxController confirmRegenerateDialog;

	public CalendarConfigurationController(List<KalendarRenderWrapper> calendars, UserRequest ureq, WindowControl wControl, boolean insideManager) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(CalendarManager.class, ureq.getLocale()));
		
		configVC = new VelocityContainer("calEdit", VELOCITY_ROOT + "/calConfig.html", getTranslator(), this);
		setCalendars(calendars);
		configVC.contextPut("insideManager", insideManager);
		configVC.contextPut("identity", ureq.getIdentity());
		putInitialPanel(configVC);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	public void setCalendars(List<KalendarRenderWrapper> calendars) {
		this.calendars = calendars;
		configVC.contextPut("calendars", calendars);
		
		Map<ICalTokenKey, ICalToken> tokenMap = new HashMap<>();
		List<ICalToken> tokens = ICalTokenGenerator.getICalAuthTokens(getIdentity());
		for(ICalToken token:tokens) {
			tokenMap.put(new ICalTokenKey(token.getType(), token.getResourceId()), token);
		}
		configVC.contextPut("icalTokens", new ICalTokens(getIdentity().getKey(), tokenMap));
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == configVC) {
			String command = event.getCommand();
			if (command.equals(CMD_ADD)) {
				// add new event to calendar
				String calendarID = ureq.getParameter(PARAM_ID);
				fireEvent(ureq, new KalendarGUIAddEvent(calendarID, new Date()));
			} else if (command.equals(CMD_IMPORT)) {
				// add new event to calendar
				String calendarID = ureq.getParameter(PARAM_ID);
				fireEvent(ureq, new KalendarGUIImportEvent(calendarID));
			} else if (command.equals(CMD_TOGGLE_DISPLAY)) {
				String calendarID = ureq.getParameter(PARAM_ID);
				KalendarRenderWrapper calendarWrapper = findKalendarRenderWrapper(calendarID);
				KalendarConfig config = calendarWrapper.getKalendarConfig();
				config.setVis(!config.isVis());
				CalendarManagerFactory.getInstance().getCalendarManager().saveKalendarConfigForIdentity(
						config, calendarWrapper.getKalendar(), ureq);
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else if (command.equals(CMD_CHOOSE_COLOR)) {
				doOpenColorChooser(ureq, ureq.getParameter(PARAM_ID));
			} else if (command.equals(CMD_ICAL_FEED)) {
				doShowICalink(ureq.getParameter(PARAM_ID));
			} else if (command.equals(CMD_ICAL_REGENERATE)) {
				confirmRegenerateDialog = activateOkCancelDialog(ureq, translate("cal.icalfeed.regenerate.title"), translate("cal.icalfeed.regenerate.warning"), confirmRegenerateDialog);
				confirmRegenerateDialog.setUserObject(ureq.getParameter(PARAM_ID));
			} else if (command.equals(CMD_ICAL_REMOVE_FEED)) {
				confirmRemoveDialog = activateOkCancelDialog(ureq, translate("cal.icalfeed.remove.title"), translate("cal.icalfeed.remove.confirmation_message"), confirmRemoveDialog);
				confirmRemoveDialog.setUserObject(ureq.getParameter(PARAM_ID));
			}
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == colorChooser) {
			cmc.deactivate();
			if (event == Event.DONE_EVENT) {
				doChooseColor(ureq);
			}
			cleanUp();
		} else if (source == confirmRemoveDialog ) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				doRemoveToken((String)confirmRemoveDialog.getUserObject());
				showInfo("cal.icalfeed.remove.info");
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if (source == confirmRegenerateDialog) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				doRegenerateToken((String)confirmRegenerateDialog.getUserObject()); 	
			}
		} else if (source == cmc) {
			cleanUp();
		}
		configVC.setDirty(true);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(colorChooser);
		removeAsListenerAndDispose(exportController);
		
		cmc = null;
		colorChooser = null;
		exportController = null;
	}
	
	private void doShowICalink(String calendarID) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(exportController);
		
		KalendarRenderWrapper calendarWrapper = findKalendarRenderWrapper(calendarID);
		String calFeedLink = ICalTokenGenerator.getIcalFeedLink(calendarWrapper.getKalendar().getType(), calendarID, getIdentity());
		exportController = new CalendarExportController(getLocale(), getWindowControl(), calFeedLink);
		listenTo(exportController);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), exportController.getInitialComponent());
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doOpenColorChooser(UserRequest ureq, String calendarID) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(colorChooser);
		
		KalendarRenderWrapper calendarWrapper = findKalendarRenderWrapper(calendarID);
		colorChooser = new CalendarColorChooserController(ureq, getWindowControl(), calendarWrapper, calendarWrapper.getKalendarConfig().getCss());

		listenTo(colorChooser);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),  colorChooser.getInitialComponent(), false, translate("cal.color.title"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doChooseColor(UserRequest ureq) {
		String choosenColor = colorChooser.getChoosenColor();
		KalendarRenderWrapper calendarWrapper = colorChooser.getCalendarWrapper();
		KalendarConfig config = calendarWrapper.getKalendarConfig();
		config.setCss(choosenColor);
		CalendarManagerFactory.getInstance().getCalendarManager().saveKalendarConfigForIdentity(
				config, calendarWrapper.getKalendar(), ureq);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doRegenerateToken(String calendarId) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(exportController);
		
		KalendarRenderWrapper calendarWrapper = findKalendarRenderWrapper(calendarId);
		ICalTokenGenerator.regenerateIcalAuthToken(calendarWrapper.getKalendar().getType(), calendarId, getIdentity());			
		String calFeedLink = ICalTokenGenerator.getIcalFeedLink(calendarWrapper.getKalendar().getType(), calendarId, getIdentity());
		
		exportController = new CalendarExportController(getLocale(), getWindowControl(), calFeedLink);
		listenTo(exportController);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), exportController.getInitialComponent());
		cmc.activate();
		listenTo(cmc);
		
		//update token
	}
	
	private void doRemoveToken(String calendarId) {
		KalendarRenderWrapper calendarWrapper = findKalendarRenderWrapper(calendarId);
		ICalTokenGenerator.destroyIcalAuthToken(calendarWrapper.getKalendar().getType(), calendarId, getIdentity());
		//remove tokens
	}
	
	private KalendarRenderWrapper findKalendarRenderWrapper(String calendarID) {
		for (KalendarRenderWrapper calendarWrapper : calendars) {
			if (calendarWrapper.getKalendar().getCalendarID().equals(calendarID))
				return calendarWrapper;
		}
		return null;
	}

	public static class ICalTokens {
		
		private final Long identityKey;
		private final Map<ICalTokenKey, ICalToken> tokenMap;
		
		public ICalTokens(Long identityKey, Map<ICalTokenKey, ICalToken> tokenMap) {
			this.identityKey = identityKey;
			this.tokenMap = tokenMap;
		}
		
		public boolean hasIcalFeed(KalendarRenderWrapper wrapper) {
			String type = wrapper.getKalendar().getType();
			Long calendarId;
			if(CalendarManager.TYPE_USER.equals(type)) {
				calendarId = identityKey;
			} else {
				calendarId = Long.valueOf(wrapper.getKalendar().getCalendarID());
			}
			ICalTokenKey tokenKey = new ICalTokenKey(type, calendarId);
			ICalToken token = tokenMap.get(tokenKey);
			return token != null && StringHelper.containsNonWhitespace(token.getToken());
		}
	}
	
	public static class ICalTokenKey {
		
		private final String type;
		private final Long resourceId;
		
		public ICalTokenKey(String type, Long resourceId) {
			this.type = type;
			this.resourceId = resourceId;
		}

		@Override
		public int hashCode() {
			return type.hashCode()
					+ resourceId.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof ICalTokenKey) {
				ICalTokenKey key = (ICalTokenKey)obj;
				return key.resourceId.equals(resourceId)
						&& key.type.equals(type);
			}
			return false;
		}	
	}
}
