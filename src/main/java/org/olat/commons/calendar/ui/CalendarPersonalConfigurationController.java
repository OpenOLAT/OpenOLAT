/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
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
import org.olat.core.commons.services.color.ColorUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.ColorPickerElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
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
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CalendarPersonalConfigurationController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private CalendarPersonalConfigurationDataModel model;
	
	private FormLink importTypeFileButton;
	private DropdownItem importTypeDropdown;
	private FormLink importTypeUrlButton;
	private FormLink showAllButton;
	private FormLink hideAllButton;

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
	private ConfirmCalendarResetController confirmResetCalendarDialog;
	private ConfirmDeleteImportedToCalendarController confirmDeleteImportedToCalendarDialog;

	private int counter;
	private final boolean allowImport;
	private final List<KalendarRenderWrapper> calendars;
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

		// Leave some space for the color dropdown plus padding plus shadow:
		flc.contextPut("marginBottomInPixels", 32 * CalendarColors.getColors().length + 12 + 10);

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(allowImport) {
			importTypeFileButton = uifactory.addFormLink("cal.import.type.file", formLayout, Link.BUTTON);
			importTypeFileButton.setIconLeftCSS("o_icon o_icon_import");

			importTypeDropdown = uifactory.addDropdownMenu("import.type.dropdown", null, null, formLayout, getTranslator());
			importTypeDropdown.setOrientation(DropdownOrientation.right);
			importTypeDropdown.setElementCssClass("o_sel_add_more");
			importTypeDropdown.setEmbbeded(true);
			importTypeDropdown.setButton(true);

			importTypeUrlButton = uifactory.addFormLink("cal.synchronize.type.url", formLayout, Link.LINK);
			importTypeUrlButton.setIconLeftCSS("o_icon o_icon_calendar_sync");
			importTypeDropdown.addElement(importTypeUrlButton);
		}

		showAllButton = uifactory.addFormLink("cal.show.all", formLayout, Link.BUTTON);
		showAllButton.setGhost(true);
		hideAllButton = uifactory.addFormLink("cal.hide.all", formLayout, Link.BUTTON);
		hideAllButton.setGhost(true);
		
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ConfigCols.type, new CalendarTypeClassRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ConfigCols.color));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ConfigCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ConfigCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ConfigCols.visible));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ConfigCols.feed.i18nKey(), ConfigCols.feed.ordinal()));
		columnsModel.addFlexiColumnModel(new StickyActionColumnModel(ConfigCols.tools));
		
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
		List<String> colorNames = CalendarColors.getColorsList();
		List<ColorPickerElement.Color> colors = ColorUIFactory.createColors(colorNames, getLocale(), "o_cal_");

		ColorPickerElement colorPickerElement = uifactory.addColorPickerElement("color_picker_" + (++counter),
				null, colors);
		colorPickerElement.setColor(row.getColor());
		colorPickerElement.addActionListener(FormEvent.ONCHANGE);
		colorPickerElement.setUserObject(row);
		colorPickerElement.setDomReplacementWrapperRequired(false);
		row.setColorPickerElement(colorPickerElement);

		FormToggle visibleToggle = uifactory.addToggleButton("vis_" + (++counter), "visible", translate("on"), translate("off"), null);
		if(isAlwaysVisible(row)) {
			visibleToggle.toggleOn();
			visibleToggle.setEnabled(false);
		} else if (row.isVisible()) {
			visibleToggle.toggleOn();
		} else {
			visibleToggle.toggleOff();
		}
		visibleToggle.setUserObject(row);
		row.setVisibleLink(visibleToggle);

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
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmResetCalendarDialog);
		removeAsListenerAndDispose(calendarFileUploadCtrl);
		removeAsListenerAndDispose(calendarUrlImportCtrl);
		removeAsListenerAndDispose(calendarToolsCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(feedUrlCtrl);
		removeAsListenerAndDispose(cmc);
		confirmResetCalendarDialog = null;
		calendarFileUploadCtrl = null;
		calendarUrlImportCtrl = null;
		calendarToolsCtrl = null;
		calloutCtrl = null;
		feedUrlCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink link) {
			if(importTypeFileButton == source) {
				doOpenImportCalendarFile(ureq);
			} else if(importTypeUrlButton == source) {
				doOpenImportCalendarUrl(ureq);
			} else if (showAllButton == source) {
				doToggleVisibilityAll(ureq, true);
			} else if (hideAllButton == source) {
				doToggleVisibilityAll(ureq, false);
			} else {
				String cmd = link.getCmd();
				if ("feed".equals(cmd)) {
					doShowFeedURL(ureq, link, (CalendarPersonalConfigurationRow) link.getUserObject());
				} else if ("tools".equals(cmd)) {
					doTools(ureq, link, (CalendarPersonalConfigurationRow) link.getUserObject());
				}
			}
		} else if (source instanceof FormToggle toggle) {
			doToggleVisibility(ureq, (CalendarPersonalConfigurationRow)toggle.getUserObject());
		} else if (source instanceof ColorPickerElement colorPickerElement) {
			if (colorPickerElement.getUserObject() != null) {
				doSetColor(ureq, (CalendarPersonalConfigurationRow) colorPickerElement.getUserObject(),
						CalendarColors.colorClassFromColor(colorPickerElement.getColor().id()));
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

	private void doToggleVisibility(UserRequest ureq, CalendarPersonalConfigurationRow row) {
		KalendarRenderWrapper calendarWrapper = row.getWrapper();
		calendarWrapper.setVisible(!calendarWrapper.isVisible());
		calendarManager.saveCalendarConfigForIdentity(calendarWrapper, getIdentity());
		if (calendarWrapper.isVisible()) {
			row.getVisibleLink().toggleOn();
		} else {
			row.getVisibleLink().toggleOff();
		}
		fireEvent(ureq, new CalendarGUISettingEvent(calendarWrapper));
	}

	private void doToggleVisibilityAll(UserRequest ureq, boolean showAll) {
		List<CalendarPersonalConfigurationRow> rows = model.getObjects();
		// do not toggle rows which are disabled
		rows.removeIf(r -> !r.getVisibleLink().isEnabled());
		rows.forEach(row -> {
			KalendarRenderWrapper calendarWrapper = row.getWrapper();
			calendarWrapper.setVisible(!calendarWrapper.isVisible());
			calendarManager.saveCalendarConfigForIdentity(calendarWrapper, getIdentity());
			if (showAll) {
				row.getVisibleLink().toggleOn();
			} else {
				row.getVisibleLink().toggleOff();
			}
			fireEvent(ureq, new CalendarGUISettingEvent(calendarWrapper));
		});
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
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmResetCalendarDialog.getInitialComponent(), true, title);
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
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmDeleteImportedToCalendarDialog.getInitialComponent(), true, title);
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
	
	private void doSetColor(UserRequest ureq, CalendarPersonalConfigurationRow row, String cssColor) {
		KalendarRenderWrapper calendarWrapper = row.getWrapper();
		calendarWrapper.setCssClass(cssColor);
		calendarManager.saveCalendarConfigForIdentity(calendarWrapper, getIdentity());
		fireEvent(ureq, new CalendarGUISettingEvent(calendarWrapper));
	}
}
