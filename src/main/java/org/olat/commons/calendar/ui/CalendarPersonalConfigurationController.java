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
package org.olat.commons.calendar.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang.RandomStringUtils;
import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.manager.ImportCalendarManager;
import org.olat.commons.calendar.manager.ImportToCalendarManager;
import org.olat.commons.calendar.model.ImportedToCalendar;
import org.olat.commons.calendar.model.Kalendar;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.ui.CalendarPersonalConfigurationDataModel.ConfigCols;
import org.olat.commons.calendar.ui.components.KalendarRenderWrapper;
import org.olat.commons.calendar.ui.events.CalendarGUIEvent;
import org.olat.commons.calendar.ui.events.CalendarGUIImportEvent;
import org.olat.commons.calendar.ui.events.CalendarGUIRemoveEvent;
import org.olat.commons.calendar.ui.events.CalendarGUISettingEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Manage your calendars.
 * 
 * Initial date: 26.08.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CalendarPersonalConfigurationController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private CalendarPersonalConfigurationDataModel model;
	
	private FormLink importTypeFileButton;
	private FormLink importTypeUrlButton;

	private CloseableModalController cmc;
	private CalendarURLController feedUrlCtrl;
	private DialogBoxController confirmRemoveTokenDialog;
	private CalendarToolsController calendarToolsCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	private DialogBoxController confirmDeleteCalendarDialog;
	private ImportCalendarFileController calendarFileUploadCtrl;
	private ImportCalendarByUrlController calendarUrlImportCtrl;
	private InjectCalendarFileController injectCalendarFileCtrl;
	private SynchronizedCalendarUrlController synchronizedCalendarUrlCtrl;
	private CalendarColorChooserController colorChooserCtrl;
	private ConfirmCalendarResetController confirmResetCalendarDialog;
	private ConfirmDeleteImportedToCalendarController confirmDeleteImportedToCalendarDialog;

	private int counter;
	private final boolean allowImport;
	private List<KalendarRenderWrapper> calendars;
	private final List<KalendarRenderWrapper> alwaysVisibleKalendars;
	
	@Autowired
	private CalendarManager calendarManager;
	@Autowired
	private ImportCalendarManager importCalendarManager;
	@Autowired
	private ImportToCalendarManager importToCalendarManager;

	public CalendarPersonalConfigurationController(UserRequest ureq, WindowControl wControl,
			List<KalendarRenderWrapper> calendars, List<KalendarRenderWrapper> alwaysVisibleKalendars, boolean allowImport) {
		super(ureq, wControl, "configuration");
		this.calendars = calendars;
		this.allowImport = allowImport;
		this.alwaysVisibleKalendars = alwaysVisibleKalendars;
		setTranslator(Util.createPackageTranslator(CalendarManager.class, getLocale(), getTranslator()));
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(allowImport) {
			importTypeFileButton = uifactory.addFormLink("cal.import.type.file", formLayout, Link.BUTTON);
			importTypeFileButton.setIconLeftCSS("o_icon o_icon_import");
			importTypeUrlButton = uifactory.addFormLink("cal.synchronize.type.url", formLayout, Link.BUTTON);
			importTypeUrlButton.setIconLeftCSS("o_icon o_icon_calendar_sync");
		}
		
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ConfigCols.type, new CalendarTypeClassRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ConfigCols.cssClass));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ConfigCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ConfigCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ConfigCols.visible));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ConfigCols.aggregated));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ConfigCols.feed.i18nKey(), ConfigCols.feed.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ConfigCols.tools.i18nKey(), ConfigCols.tools.ordinal()));
		
		model = new CalendarPersonalConfigurationDataModel(columnsModel);
		List<CalendarPersonalConfigurationRow> rows = new ArrayList<>(calendars.size());
		for(KalendarRenderWrapper calendar:calendars) {
			CalendarPersonalConfigurationRow row = new CalendarPersonalConfigurationRow(calendar);
			initLinks(row);
			rows.add(row);
		}
		model.setObjects(rows);
		
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		tableEl.setMultiSelect(false);
		tableEl.setPageSize(50);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
	}
	
	private void initLinks(CalendarPersonalConfigurationRow row) {
		FormLink colorLink = uifactory.addFormLink("col_" + (++counter), "color", "", null, null, Link.NONTRANSLATED);
		colorLink.setIconLeftCSS("o_cal_config_color ".concat(row.getCssClass()));
		colorLink.setUserObject(row);
		row.setColorLink(colorLink);
		
		FormLink visibleLink = uifactory.addFormLink("vis_" + (++counter), "visible", "", null, null, Link.NONTRANSLATED);
		if(isAlwaysVisible(row)) {
			enableDisableIcons(visibleLink, true);
			visibleLink.setEnabled(false);
		} else {
			enableDisableIcons(visibleLink, row.isVisible());
		}
		visibleLink.setUserObject(row);
		row.setVisibleLink(visibleLink);

		FormLink aggregatedLink = uifactory.addFormLink("agg_" + (++counter), "aggregated", "", null, null, Link.NONTRANSLATED);
		enableDisableIcons(aggregatedLink, row.isAggregated());
		aggregatedLink.setUserObject(row);
		row.setAggregatedLink(aggregatedLink);

		FormLink feedLink = uifactory.addFormLink("fee_" + (++counter), "feed", "", null, null, Link.NONTRANSLATED);
		feedLink.setIconLeftCSS("o_icon o_icon-lg o_icon_rss");
		feedLink.setUserObject(row);
		row.setFeedLink(feedLink);
		
		FormLink toolsLink = uifactory.addFormLink("tools_" + (++counter), "tools", "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon-lg o_icon_actions o_icon-fws");
		toolsLink.setUserObject(row);
		row.setToolsLink(toolsLink);
	}
	
	private boolean isAlwaysVisible(CalendarPersonalConfigurationRow row) {
		if(alwaysVisibleKalendars == null || alwaysVisibleKalendars.isEmpty()) return false;
		
		for(KalendarRenderWrapper alwaysVisibleKalendar:alwaysVisibleKalendars) {
			if(alwaysVisibleKalendar.getKalendar().getCalendarID().equals(row.getCalendarId())
					&& alwaysVisibleKalendar.getKalendar().getType().equals(row.getCalendarType())) {
				return true;
			}
			
		}
		
		return false;
	}
	
	private void enableDisableIcons(FormLink link, boolean enabled) {
		link.setIconLeftCSS(enabled ? "o_icon o_icon_calendar_enabled" : "o_icon o_icon_calendar_disabled");
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(calloutCtrl == source || cmc == source) {
			cleanUp();
		} else if (source == confirmRemoveTokenDialog ) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				doDeleteToken((CalendarPersonalConfigurationRow)confirmRemoveTokenDialog.getUserObject());
				showInfo("cal.icalfeed.remove.info");
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if (source == confirmDeleteCalendarDialog ) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				doDeleteCalendar(ureq, (CalendarPersonalConfigurationRow)confirmDeleteCalendarDialog.getUserObject());
				showInfo("cal.import.remove.info");
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if(source == confirmResetCalendarDialog) {
			if(event == Event.DONE_EVENT) {
				doResetCalendar(confirmResetCalendarDialog.getCalendarRow());
				showInfo("cal.reset.info");
				fireEvent(ureq, Event.CHANGED_EVENT); 
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == confirmDeleteImportedToCalendarDialog) {
			if(event == Event.DONE_EVENT) {
				doDeleteImportedToCalendar(confirmDeleteImportedToCalendarDialog.getSelectedImportedToCalendars());
				showInfo("cal.delete.imported.to.info");
				fireEvent(ureq, Event.CHANGED_EVENT); 
			}
			cmc.deactivate();
			cleanUp();
		} else if(calendarFileUploadCtrl == source) {
			KalendarRenderWrapper calendar = calendarFileUploadCtrl.getImportedCalendar();
			cmc.deactivate();
			cleanUp();
			if(event == Event.DONE_EVENT) {
				doImportCalendar(calendar);
				fireEvent(ureq, new CalendarGUIImportEvent(calendar));
			}
		} else if(calendarUrlImportCtrl == source) {
			KalendarRenderWrapper calendar = calendarUrlImportCtrl.getImportedCalendar();
			cmc.deactivate();
			cleanUp();
			
			if(event == Event.DONE_EVENT) {
				doImportCalendar(calendar);
				fireEvent(ureq, new CalendarGUIImportEvent(calendar));
			}
		} else if(calendarToolsCtrl == source) {
			CalendarPersonalConfigurationRow row = calendarToolsCtrl.getRow();
			calloutCtrl.deactivate();
			cleanUp();
			
			if(CalendarGUIEvent.IMPORT_BY_FILE.equals(event.getCommand())) {
				doOpenInjectCalendarFile(ureq, row);
			} else if(CalendarGUIEvent.IMPORT_SYNCHRONIZED_URL.equals(event.getCommand())) {
				doOpenSynchronizedCalendarUrl(ureq, row);
			} else if(CalendarGUIEvent.DELETE_TOKEN.equals(event.getCommand())) {
				doConfirmDeleteToken(ureq, row);
			} else if(CalendarGUIEvent.DELETE_CALENDAR.equals(event.getCommand())) {
				doConfirmDeleteCalendar(ureq, row);
			} else if(CalendarGUIEvent.RESET_CALENDAR.equals(event.getCommand())) {
				doConfirmResetCalendar(ureq, row);
			} else if(CalendarGUIEvent.DELETE_IMPORTED_TO.equals(event.getCommand())) {
				doConfirmDeleteImportedToCalendar(ureq, row);
			}

		} else if(injectCalendarFileCtrl == source || synchronizedCalendarUrlCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(colorChooserCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doSetColor(ureq, colorChooserCtrl.getRow(), colorChooserCtrl.getChoosenColor());
			}
			calloutCtrl.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmResetCalendarDialog);
		removeAsListenerAndDispose(calendarFileUploadCtrl);
		removeAsListenerAndDispose(calendarUrlImportCtrl);
		removeAsListenerAndDispose(calendarToolsCtrl);
		removeAsListenerAndDispose(colorChooserCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(feedUrlCtrl);
		removeAsListenerAndDispose(cmc);
		confirmResetCalendarDialog = null;
		calendarFileUploadCtrl = null;
		calendarUrlImportCtrl = null;
		calendarToolsCtrl = null;
		colorChooserCtrl = null;
		calloutCtrl = null;
		feedUrlCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			if(importTypeFileButton == source) {
				doOpenImportCalendarFile(ureq);
			} else if(importTypeUrlButton == source) {
				doOpenImportCalendarUrl(ureq);
			} else {
				FormLink link = (FormLink)source;
				String cmd = link.getCmd();
				if("visible".equals(cmd)) {
					doToogleVisibility(ureq, (CalendarPersonalConfigurationRow)link.getUserObject());
				} else if("aggregated".equals(cmd)) {
					doToogleAggregated(ureq, (CalendarPersonalConfigurationRow)link.getUserObject());
				} else if("feed".equals(cmd)) {
					doShowFeedURL(ureq, link, (CalendarPersonalConfigurationRow)link.getUserObject());
				} else if("tools".equals(cmd)) {
					doTools(ureq, link, (CalendarPersonalConfigurationRow)link.getUserObject());
				} else if("color".equals(cmd)) {
					doChooseColor(ureq, link, (CalendarPersonalConfigurationRow)link.getUserObject());
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		//
	}

	private void doToogleVisibility(UserRequest ureq, CalendarPersonalConfigurationRow row) {
		KalendarRenderWrapper calendarWrapper = row.getWrapper();
		calendarWrapper.setVisible(!calendarWrapper.isVisible());
		calendarManager.saveCalendarConfigForIdentity(calendarWrapper, getIdentity());
		enableDisableIcons(row.getVisibleLink(), calendarWrapper.isVisible());
		fireEvent(ureq, new CalendarGUISettingEvent(calendarWrapper));
	}
	
	private void doToogleAggregated(UserRequest ureq, CalendarPersonalConfigurationRow row) {
		KalendarRenderWrapper calendarWrapper = row.getWrapper();
		calendarWrapper.setInAggregatedFeed(!calendarWrapper.isInAggregatedFeed());
		calendarManager.saveCalendarConfigForIdentity(calendarWrapper, getIdentity());
		enableDisableIcons(row.getAggregatedLink(), calendarWrapper.isInAggregatedFeed());
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doShowFeedURL(UserRequest ureq, FormLink link, CalendarPersonalConfigurationRow row) {
		removeAsListenerAndDispose(feedUrlCtrl);
		removeAsListenerAndDispose(calloutCtrl);

		KalendarRenderWrapper calendarWrapper = row.getWrapper();
		if(!StringHelper.containsNonWhitespace(row.getToken())) {
			calendarWrapper.setToken(RandomStringUtils.randomAlphanumeric(6));
			calendarManager.saveCalendarConfigForIdentity(calendarWrapper, getIdentity());
		}

		String calFeedLink = row.getFeedUrl(getIdentity());
		feedUrlCtrl = new CalendarURLController(ureq, getWindowControl(), calFeedLink);
		listenTo(feedUrlCtrl);

		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				feedUrlCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doConfirmDeleteToken(UserRequest ureq, CalendarPersonalConfigurationRow row) {
		String title = translate("cal.icalfeed.remove.title");
		String msg = translate("cal.icalfeed.remove.confirmation_message");
		confirmRemoveTokenDialog = activateOkCancelDialog(ureq, title, msg, confirmRemoveTokenDialog);
		confirmRemoveTokenDialog.setUserObject(row);
	}
	
	private void doDeleteToken(CalendarPersonalConfigurationRow row) {
		KalendarRenderWrapper calendarWrapper = row.getWrapper();
		calendarWrapper.setToken(null);
		calendarManager.saveCalendarConfigForIdentity(calendarWrapper, getIdentity());
		tableEl.reloadData();
	}
	
	private void doTools(UserRequest ureq, FormLink link, CalendarPersonalConfigurationRow row) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(calendarToolsCtrl);
		
		calendarToolsCtrl = new CalendarToolsController(ureq, getWindowControl(), row);
		listenTo(calendarToolsCtrl);

		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				calendarToolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doConfirmDeleteCalendar(UserRequest ureq, CalendarPersonalConfigurationRow row) {
		String title = translate("cal.import.remove.title");
		String msg = translate("cal.import.remove.confirmation_message");
		confirmDeleteCalendarDialog = activateOkCancelDialog(ureq, title, msg, confirmDeleteCalendarDialog);
		confirmDeleteCalendarDialog.setUserObject(row);
	}
	
	private void doDeleteCalendar(UserRequest ureq, CalendarPersonalConfigurationRow row) {
		if(!row.isImported()) return;
		
		Kalendar calendar = row.getWrapper().getKalendar();
		importCalendarManager.deleteCalendar(getIdentity(), calendar);

		List<CalendarPersonalConfigurationRow> currentRows = model.getObjects();
		currentRows.remove(row);
		model.setObjects(currentRows);
		tableEl.reloadData();
		fireEvent(ureq, new CalendarGUIRemoveEvent(row.getWrapper()));
	}
	
	private void doConfirmResetCalendar(UserRequest ureq, CalendarPersonalConfigurationRow row) {
		confirmResetCalendarDialog = new ConfirmCalendarResetController(ureq, getWindowControl(), row);
		listenTo(confirmResetCalendarDialog);
		
		String title = translate("cal.confirm.reset.title", new String[] { StringHelper.escapeHtml(row.getDisplayName() )});
		cmc = new CloseableModalController(getWindowControl(), "close", confirmResetCalendarDialog.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doResetCalendar(CalendarPersonalConfigurationRow row) {
		Kalendar kalendar = row.getWrapper().getKalendar();
		importToCalendarManager.deleteImportedCalendarsAndEvents(kalendar);
		// reload the calendar without the deleted imported calendars
		kalendar = calendarManager.getCalendar(kalendar.getType(), kalendar.getCalendarID());
		List<KalendarEvent> events = kalendar.getEvents();
		// filter managed events
		List<KalendarEvent> eventsToDelete = events.stream()
				.filter(e -> !e.isManaged()).collect(Collectors.toList());
		calendarManager.removeEventsFrom(kalendar, eventsToDelete);
	}
	
	private void doConfirmDeleteImportedToCalendar(UserRequest ureq, CalendarPersonalConfigurationRow row) {
		confirmDeleteImportedToCalendarDialog = new ConfirmDeleteImportedToCalendarController(ureq, getWindowControl(), row);
		listenTo(confirmDeleteImportedToCalendarDialog);
		
		String title = translate("cal.confirm.delete.imported.to.title", new String[] { StringHelper.escapeHtml(row.getDisplayName() )});
		cmc = new CloseableModalController(getWindowControl(), "close", confirmDeleteImportedToCalendarDialog.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doDeleteImportedToCalendar(List<ImportedToCalendar> calendarsToDelete) {
		for(ImportedToCalendar calendar:calendarsToDelete) {
			importToCalendarManager.deleteImportedCalendarsAndEvents(calendar);
		}
	}

	private void doOpenImportCalendarFile(UserRequest ureq) {
		calendarFileUploadCtrl = new ImportCalendarFileController(ureq, getWindowControl());
		listenTo(calendarFileUploadCtrl);
		
		String title = translate("cal.import.type.file");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), calendarFileUploadCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doOpenImportCalendarUrl(UserRequest ureq) {
		calendarUrlImportCtrl = new ImportCalendarByUrlController(ureq, getWindowControl());
		listenTo(calendarUrlImportCtrl);
		
		String title = translate("cal.synchronize.type.url");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), calendarUrlImportCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doImportCalendar(KalendarRenderWrapper wrapper) {
		CalendarPersonalConfigurationRow row = new CalendarPersonalConfigurationRow(wrapper);
		initLinks(row);
		List<CalendarPersonalConfigurationRow> rows = model.getObjects();
		rows.add(row);
		model.setObjects(rows);
		tableEl.reloadData();
	}
	
	private void doOpenInjectCalendarFile(UserRequest ureq, CalendarPersonalConfigurationRow row) {
		injectCalendarFileCtrl = new InjectCalendarFileController(ureq, getWindowControl(), row);
		listenTo(injectCalendarFileCtrl);
		
		String title = translate("cal.import.type.file");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), injectCalendarFileCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doOpenSynchronizedCalendarUrl(UserRequest ureq, CalendarPersonalConfigurationRow row) {
		synchronizedCalendarUrlCtrl = new SynchronizedCalendarUrlController(ureq, getWindowControl(), row);
		listenTo(synchronizedCalendarUrlCtrl);
		
		String title = translate("cal.synchronize.type.url");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), synchronizedCalendarUrlCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doChooseColor(UserRequest ureq, FormLink link, CalendarPersonalConfigurationRow row) {
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(colorChooserCtrl);
		
		colorChooserCtrl = new CalendarColorChooserController(ureq, getWindowControl(), row);
		listenTo(colorChooserCtrl);
		
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				colorChooserCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doSetColor(UserRequest ureq, CalendarPersonalConfigurationRow row, String cssColor) {
		KalendarRenderWrapper calendarWrapper = row.getWrapper();
		calendarWrapper.setCssClass(cssColor);
		calendarManager.saveCalendarConfigForIdentity(calendarWrapper, getIdentity());
		row.getColorLink().setIconLeftCSS("o_cal_config_color ".concat(row.getCssClass()));
		fireEvent(ureq, new CalendarGUISettingEvent(calendarWrapper));
	}
}
