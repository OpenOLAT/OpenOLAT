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
package org.olat.modules.appointments.ui;

import static java.util.Collections.singletonList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.date.DateComponentFactory;
import org.olat.core.gui.components.date.DateElement;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSort;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.appointments.Appointment;
import org.olat.modules.appointments.Appointment.Status;
import org.olat.modules.appointments.AppointmentsSecurityCallback;
import org.olat.modules.appointments.AppointmentsService;
import org.olat.modules.appointments.Organizer;
import org.olat.modules.appointments.Participation;
import org.olat.modules.appointments.ParticipationResult;
import org.olat.modules.appointments.ParticipationSearchParams;
import org.olat.modules.appointments.Topic;
import org.olat.modules.appointments.Topic.Type;
import org.olat.modules.appointments.ui.AppointmentDataModel.AppointmentCols;
import org.olat.modules.bigbluebutton.BigBlueButtonRecordingReference;
import org.olat.modules.bigbluebutton.ui.EditBigBlueButtonMeetingController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 Apr 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class AppointmentListController extends FormBasicController implements FlexiTableComponentDelegate {
	
	private static final String CMD_MORE = "more";
	private static final String CMD_SELECT = "select";
	private static final String CMD_ADD_USER = "add";
	private static final String CMD_REMOVE = "remove";
	private static final String CMD_EXPORT = "export";
	private static final String CMD_CONFIRM = "confirm";
	private static final String CMD_DELETE = "delete";
	private static final String CMD_EDIT = "edit";
	private static final String CMD_RECORDING = "recording";
	private static final long PARTICIPANTS_RENDER_LIMIT = 3;
	
	private FormLink backLink;
	private DropdownItem addAppointmentsDropdown;
	private FormLink addIndividualAppointmentsLink;
	private FormLink addRecurringAppointmentLink;
	private FlexiTableElement tableEl;
	private AppointmentDataModel dataModel;
	
	private CloseableModalController cmc;
	private TopicHeaderController headerCtrl;
	private DialogBoxController confirmParticipationCrtl;
	private FindingConfirmationController findingConfirmationCtrl;
	private AppointmentEditController appointmentEditCtrl;
	private AppointmentCreateController addAppointmentsCtrl;
	private UserSearchController userSearchCtrl;
	private ParticipationRemoveController removeCtrl;
	private AppointmentDeleteController appointmentDeleteCtrl;

	protected Topic topic;
	protected final AppointmentsSecurityCallback secCallback;
	protected final List<Organizer> organizers;
	private final Set<Appointment> showAllParticipations = new HashSet<>();
	
	@Autowired
	protected AppointmentsService appointmentsService;
	@Autowired
	protected UserManager userManager;
	
	protected AppointmentListController(UserRequest ureq, WindowControl wControl, Topic topic,
			AppointmentsSecurityCallback secCallback) {
		super(ureq, wControl, "appointments_list");
		setTranslator(Util.createPackageTranslator(EditBigBlueButtonMeetingController.class, getLocale(), getTranslator()));
		this.topic = topic;
		this.secCallback = secCallback;
		this.organizers = appointmentsService.getOrganizers(topic);

		initForm(ureq);
		updateModel();
	}
	
	protected abstract boolean canSelect();
	
	protected abstract boolean canEdit();

	protected abstract boolean isParticipationVisible();
	
	protected abstract List<String> getFilters();
	
	protected abstract List<String> getDefaultFilters();
	
	protected abstract String getPersistedPreferencesId();
	
	protected abstract List<AppointmentRow> loadModel();
	
	protected void setAddAppointmentVisible(boolean visible) {
		if (addAppointmentsDropdown != null) {
			addAppointmentsDropdown.setVisible(visible);
		}
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// Back
		FormLayoutContainer backButtons = FormLayoutContainer.createButtonLayout("backButtons", getTranslator());
		backButtons.setRootForm(mainForm);
		formLayout.add("backButtons", backButtons);
		backButtons.setElementCssClass("o_button_group o_button_group_left");
		
		backLink = uifactory.addFormLink("backLink", "back", "back", "", backButtons, Link.LINK_BACK);
		backLink.setElementCssClass("o_back");
		
		// Header
		headerCtrl = new TopicHeaderController(ureq, getWindowControl(), topic, false);
		listenTo(headerCtrl);
		flc.put("header", headerCtrl.getInitialComponent());
		
		// Buttons
		if (canEdit()) {
			FormLayoutContainer topButtons = FormLayoutContainer.createButtonLayout("topButtons", getTranslator());
			topButtons.setRootForm(mainForm);
			formLayout.add("topButtons", topButtons);
			topButtons.setElementCssClass("o_button_group o_button_group_right");
			
			if (secCallback.canEditAppointment(organizers)) {
				addAppointmentsDropdown = uifactory.addDropdownMenu("add.appointment", "add.appointment", topButtons, getTranslator());
				addAppointmentsDropdown.setOrientation(DropdownOrientation.right);
				
				addIndividualAppointmentsLink = uifactory.addFormLink("add.appointment.individual", formLayout, Link.LINK);
				addAppointmentsDropdown.addElement(addIndividualAppointmentsLink);
				addRecurringAppointmentLink = uifactory.addFormLink("add.appointment.recurring", formLayout, Link.LINK);
				addAppointmentsDropdown.addElement(addRecurringAppointmentLink);
			}
		}
		
		// Table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if (ureq.getUserSession().getRoles().isAdministrator()) {
			DefaultFlexiColumnModel idModel = new DefaultFlexiColumnModel(AppointmentCols.id);
			idModel.setDefaultVisible(false);
			columnsModel.addFlexiColumnModel(idModel);
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppointmentCols.status, new AppointmentStatusCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppointmentCols.start));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppointmentCols.end));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppointmentCols.location));
		DefaultFlexiColumnModel detailsModel = new DefaultFlexiColumnModel(AppointmentCols.details);
		detailsModel.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(detailsModel);
		if (Type.finding != topic.getType()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppointmentCols.maxParticipations));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppointmentCols.freeParticipations));
		}
		DefaultFlexiColumnModel numberOfParticipationsModel = new DefaultFlexiColumnModel(AppointmentCols.numberOfParticipations);
		numberOfParticipationsModel.setDefaultVisible(false);
		columnsModel.addFlexiColumnModel(numberOfParticipationsModel);
		if (isParticipationVisible()) {
			DefaultFlexiColumnModel participantsModel = new DefaultFlexiColumnModel(AppointmentCols.participants);
			participantsModel.setCellRenderer(new ParticipationsRenderer());
			participantsModel.setDefaultVisible(false);
			columnsModel.addFlexiColumnModel(participantsModel);
		}
		if (appointmentsService.isBigBlueButtonEnabled()) {
			DefaultFlexiColumnModel recordingsModel = new DefaultFlexiColumnModel(AppointmentCols.recordings);
			recordingsModel.setExportable(false);
			columnsModel.addFlexiColumnModel(recordingsModel);
		}
		if (canSelect()) {
			DefaultFlexiColumnModel selectModel = new DefaultFlexiColumnModel(AppointmentCols.select);
			selectModel.setExportable(false);
			columnsModel.addFlexiColumnModel(selectModel);
		}
		if (canEdit()) {
			DefaultFlexiColumnModel confirmModel = new DefaultFlexiColumnModel(AppointmentCols.confirm);
			confirmModel.setExportable(false);
			columnsModel.addFlexiColumnModel(confirmModel);
			DefaultFlexiColumnModel commandsModel = new DefaultFlexiColumnModel(AppointmentCols.commands);
			commandsModel.setExportable(false);
			columnsModel.addFlexiColumnModel(commandsModel);
		}
		
		dataModel = new AppointmentDataModel(columnsModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, getPersistedPreferencesId());
		tableEl.setEmtpyTableMessageKey("table.empty.appointments");

		tableEl.setElementCssClass("o_appointments o_list");
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		VelocityContainer rowVC = createVelocityContainer("appointment_row");
		rowVC.setDomReplacementWrapperRequired(false);
		tableEl.setRowRenderer(rowVC, this);
		
		initFilters();
		initSorters();
	}

	protected void initFilters() {
		List<String> filters = getFilters();
		if (filters != null && !filters.isEmpty()) {
			List<FlexiTableFilter> tableFilters = new ArrayList<>(3);
			List<String> defaultFilters = getDefaultFilters();
			List<FlexiTableFilter> selectedFilters = new ArrayList<>(defaultFilters.size());
			if (filters.contains(AppointmentDataModel.FILTER_PARTICIPATED)) {
				FlexiTableFilter filter = new FlexiTableFilter(translate("filter.participated"), AppointmentDataModel.FILTER_PARTICIPATED, false);
				tableFilters.add(filter);
				if (defaultFilters.contains(AppointmentDataModel.FILTER_PARTICIPATED)) {
					selectedFilters.add(filter);
				}
			}
			if (filters.contains(AppointmentDataModel.FILTER_FUTURE)) {
				FlexiTableFilter filter = new FlexiTableFilter(translate("filter.future"), AppointmentDataModel.FILTER_FUTURE, false);
				tableFilters.add(filter);
				if (defaultFilters.contains(AppointmentDataModel.FILTER_FUTURE)) {
					selectedFilters.add(filter);
				}
			}
			tableFilters.add(FlexiTableFilter.SPACER);
			FlexiTableFilter filter = new FlexiTableFilter(translate("filter.all"), AppointmentDataModel.FILTER_ALL, true);
			tableFilters.add(filter);
			if (defaultFilters.contains(AppointmentDataModel.FILTER_ALL)) {
				selectedFilters.add(filter);
			}
			tableEl.setFilters("Filters", tableFilters, true);
			tableEl.setSelectedFilters(selectedFilters);
		}
	}
	
	private void initSorters() {
		List<FlexiTableSort> sorters = new ArrayList<>(2);
		sorters.add(new FlexiTableSort(translate(AppointmentCols.start.i18nHeaderKey()), AppointmentCols.start.name()));
		sorters.add(new FlexiTableSort(translate(AppointmentCols.numberOfParticipations.i18nHeaderKey()), AppointmentCols.numberOfParticipations.name()));
		FlexiTableSortOptions options = new FlexiTableSortOptions(sorters);
		options.setDefaultOrderBy(new SortKey(AppointmentCols.start.name(), true));
		tableEl.setSortSettings(options);
	}

	private void updateModel() {
		List<AppointmentRow> rows = loadModel();
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	protected void forgeAppointmentView(AppointmentRow row, Appointment appointment) {
		Locale locale = getLocale();
		Date begin = appointment.getStart();
		Date end = appointment.getEnd();
		String date = null;
		String dateLong = null;
		String dateShort1 = null;
		String dateShort2 = null;
		String time = null;

		boolean sameDay = DateUtils.isSameDay(begin, end);
		boolean sameTime = DateUtils.isSameTime(begin, end);
		String startDate = StringHelper.formatLocaleDateFull(begin.getTime(), locale);
		String startTime = StringHelper.formatLocaleTime(begin.getTime(), locale);
		String endDate = StringHelper.formatLocaleDateFull(end.getTime(), locale);
		String endTime = StringHelper.formatLocaleTime(end.getTime(), locale);
		if (sameDay) {
			StringBuilder dateSb = new StringBuilder();
			dateSb.append(startDate);
			date = dateSb.toString();
			StringBuilder timeSb = new StringBuilder();
			if (sameTime) {
				timeSb.append(translate("full.day"));
			} else {
				timeSb.append(startTime);
				timeSb.append(" - ");
				timeSb.append(endTime);
			}
			time = timeSb.toString();
		} else {
			StringBuilder dateSbLong = new StringBuilder();
			dateSbLong.append(startDate);
			dateSbLong.append(" ");
			dateSbLong.append(startTime);
			dateSbLong.append(" - ");
			dateSbLong.append(endDate);
			dateSbLong.append(" ");
			dateSbLong.append(endTime);
			dateLong = dateSbLong.toString();
			StringBuilder dateSbShort1 = new StringBuilder();
			dateSbShort1.append(startDate);
			dateSbShort1.append(" ");
			dateSbShort1.append(startTime);
			dateSbShort1.append(" -");
			dateShort1 = dateSbShort1.toString();
			StringBuilder dateSbShort2 = new StringBuilder();
			dateSbShort2.append(endDate);
			dateSbShort2.append(" ");
			dateSbShort2.append(endTime);
			dateShort2 = dateSbShort2.toString();
		}

		row.setDate(date);
		row.setDateLong(dateLong);
		row.setDateShort1(dateShort1);
		row.setDateShort2(dateShort2);
		row.setTime(time);
		row.setLocation(AppointmentsUIFactory.getDisplayLocation(getTranslator(), appointment));
		row.setDetails(appointment.getDetails());
		forgeDayElement(row, appointment.getStart());
	}
	
	protected void forgeDayElement(AppointmentRow row, Date date) {
		DateElement dayEl = DateComponentFactory.createDateElementWithYear("day_" + row.getKey(), date);
		row.setDayEl(dayEl);
	}

	protected void forgeParticipants(AppointmentRow row, List<Participation> participations) {
		long limit = showAllParticipations.contains(row.getAppointment())? Long.MAX_VALUE: PARTICIPANTS_RENDER_LIMIT;
		List<String> participants = participations.stream()
				.map(p -> userManager.getUserDisplayName(p.getIdentity().getKey()))
				.sorted(String.CASE_INSENSITIVE_ORDER)
				.limit(limit)
				.collect(Collectors.toList());
		row.setParticipants(participants);
		
		if (participations.size() > PARTICIPANTS_RENDER_LIMIT) {
			String name = "more_" + row.getKey();
			Link showMoreLink = LinkFactory.createCustomLink(name, CMD_MORE, "", Link.LINK + Link.NONTRANSLATED, null, this);
			
			long hiddenParticipations = participations.size() - PARTICIPANTS_RENDER_LIMIT;
			String displayText = showAllParticipations.contains(row.getAppointment())
					? translate("show.less")
					: translate("show.more", new String[] { String.valueOf(hiddenParticipations)} );
			showMoreLink.setCustomDisplayText(displayText);
			showMoreLink.setUserObject(row);
			row.setShowMoreLink(showMoreLink);
			flc.getFormItemComponent().put(name, showMoreLink);
		}
	}
	
	protected void forgeSelectionLink(AppointmentRow row, boolean selected) {
		String i18n = selected? "appointment.selected": "appointment.select";
		FormLink link = uifactory.addFormLink("select_" + row.getKey(), CMD_SELECT, i18n, null, null, Link.BUTTON);
		link.setUserObject(row);
		if (!selected) {
			link.setIconLeftCSS("o_icon o_icon_lg o_icon_selected");
			link.setElementCssClass("btn-primary");
		}
		row.setSelectLink(link);
	}
	
	protected void forgeConfirmLink(AppointmentRow row, boolean confirmable) {
		String i18nKey = confirmable? "confirm": "unconfirm";
		FormLink link = uifactory.addFormLink("confirm_" + row.getKey(), CMD_CONFIRM, i18nKey, null, null, Link.BUTTON);
		link.setUserObject(row);
		link.setIconLeftCSS("o_icon o_icon_lg o_icon_selected");
		if (confirmable) {
			link.setElementCssClass("o_button_confirm");
		}
		row.setConfirmLink(link);
	}
	
	private DropdownItem getOrCreateCommandDroppdown(AppointmentRow row) {
		DropdownItem dropdown = row.getCommandDropdown();
		if (dropdown == null) {
			dropdown = uifactory.addDropdownMenu("cmd_" + row.getKey(), "commands", null, null, getTranslator());
			dropdown.setCarretIconCSS("o_icon o_icon-lg o_icon_commands");
			dropdown.setOrientation(DropdownOrientation.right);
			dropdown.setUserObject(row);
			row.setCommandDropdown(dropdown);
		}
		return dropdown;
	}
	
	protected void forgeAddUserLink(AppointmentRow row) {
		FormLink link = uifactory.addFormLink("add_" + row.getKey(), CMD_ADD_USER, "add.user", null, flc, Link.LINK);
		link.setUserObject(row);
		link.setIconLeftCSS("o_icon o_icon-fw o_icon_add_member");
		getOrCreateCommandDroppdown(row).addElement(link);
	}

	protected void forgeRemoveUserLink(AppointmentRow row) {
		FormLink link = uifactory.addFormLink("remove_" + row.getKey(), CMD_REMOVE, "remove.user", null, flc, Link.LINK);
		link.setIconLeftCSS("o_icon o_icon-fw o_icon_remove_member");
		link.setUserObject(row);
		getOrCreateCommandDroppdown(row).addElement(link);
	}
	
	protected void forgeExportUserLink(AppointmentRow row) {
		FormLink link = uifactory.addFormLink("export_" + row.getKey(), CMD_EXPORT, "export.participations", null, flc, Link.LINK);
		link.setUserObject(row);
		link.setIconLeftCSS("o_icon o_icon-fw o_icon_download");
		getOrCreateCommandDroppdown(row).addElement(link);
	}
	
	protected void forgeDeleteLink(AppointmentRow row) {
		FormLink link = uifactory.addFormLink("delete_" + row.getKey(), CMD_DELETE, "delete", null, flc, Link.LINK);
		link.setUserObject(row);
		link.setIconLeftCSS("o_icon o_icon-fw o_icon_delete");
		getOrCreateCommandDroppdown(row).addElement(link);
	}
	
	protected void forgeEditLink(AppointmentRow row) {
		FormLink link = uifactory.addFormLink("edit_" + row.getKey(), CMD_EDIT, "appointment.edit", null, flc, Link.LINK);
		link.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
		link.setUserObject(row);
		getOrCreateCommandDroppdown(row).addElement(link);
	}

	protected void forgeRecordingReferencesLinks(AppointmentRow row, List<BigBlueButtonRecordingReference> recordingReferences) {
		if (recordingReferences.isEmpty()) return;
		
		recordingReferences.sort((r1, r2) -> r1.getStartDate().compareTo(r2.getStartDate()));
		FormItemList recordingLinks = new FormItemList(recordingReferences.size());
		for (int i = 0; i < recordingReferences.size(); i++) {
			BigBlueButtonRecordingReference recording = recordingReferences.get(i);
			FormLink link = uifactory.addFormLink("rec_" + recording.getRecordingId(), CMD_RECORDING, null, null, flc, Link.NONTRANSLATED);
			String name = translate("recording");
			if (recordingReferences.size() > 1) {
				name = name + " " + (i+1);
			}
			name = name + "  ";
			link.setI18nKey(name);
			link.setIconLeftCSS("o_icon o_icon_lg o_vc_icon");
			link.setNewWindow(true, true, false);
			link.setUserObject(recording);
			recordingLinks.add(link);
		}
		row.setRecordingLinks(recordingLinks);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link) {
			Link link = (Link)source;
			String cmd = link.getCommand();
			if (CMD_MORE.equals(cmd)) {
				AppointmentRow row = (AppointmentRow)link.getUserObject();
				doToggleShowMoreParticipations(row);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == backLink) {
			fireEvent(ureq, Event.DONE_EVENT);
		} else if (source == addIndividualAppointmentsLink) {
			doAddIndividualAppointments(ureq);
		} else if (source == addRecurringAppointmentLink) {
			doAddRecurringAppointment(ureq);
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if (CMD_SELECT.equals(cmd)) {
				AppointmentRow row = (AppointmentRow)link.getUserObject();
				doToggleParticipation(ureq, row);
			} else if (CMD_EDIT.equals(cmd)) {
				AppointmentRow row = (AppointmentRow)link.getUserObject();
				doEditAppointment(ureq, row.getAppointment());
			} else if (CMD_DELETE.equals(cmd)) {
				AppointmentRow row = (AppointmentRow)link.getUserObject();
				doConfirmDeletion(ureq, row.getAppointment());
			} else if (CMD_CONFIRM.equals(cmd)) {
				AppointmentRow row = (AppointmentRow)link.getUserObject();
				doConfirm(ureq, row.getAppointment());
			} else if (CMD_ADD_USER.equals(cmd)) {
				AppointmentRow row = (AppointmentRow)link.getUserObject();
				doSelectUser(ureq, row.getAppointment());
			} else if (CMD_REMOVE.equals(cmd)) {
				AppointmentRow row = (AppointmentRow)link.getUserObject();
				doRemove(ureq, row.getAppointment());
			} else if (CMD_EXPORT.equals(cmd)) {
				AppointmentRow row = (AppointmentRow)link.getUserObject();
				doExportParticipations(ureq, row.getAppointment());
			} else if (CMD_RECORDING.equals(cmd)) {
				BigBlueButtonRecordingReference recordingReference = (BigBlueButtonRecordingReference)link.getUserObject();
				doOpenRecording(ureq, recordingReference);
			} 
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == confirmParticipationCrtl) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				Appointment appointment = (Appointment)confirmParticipationCrtl.getUserObject();
				doCreateParticipation(appointment);
				updateModel();
			}
		} else if (findingConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				updateModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if (appointmentEditCtrl == source) {
			if (event == Event.DONE_EVENT) {
				updateModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if (addAppointmentsCtrl == source) {
			if (event == Event.DONE_EVENT) {
				updateModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if (appointmentDeleteCtrl == source) {
			if (event == Event.DONE_EVENT) {
				updateModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if (userSearchCtrl == source) {
			Appointment appointment = (Appointment)userSearchCtrl.getUserObject();
			if (event instanceof SingleIdentityChosenEvent) {
				SingleIdentityChosenEvent singleEvent = (SingleIdentityChosenEvent)event;
				Identity choosenIdentity = singleEvent.getChosenIdentity();
				if (choosenIdentity != null) {
					List<Identity> toAdd = Collections.singletonList(choosenIdentity);
					doAddUser(appointment, toAdd);
				}
			} else if (event instanceof MultiIdentityChosenEvent) {
				MultiIdentityChosenEvent multiEvent = (MultiIdentityChosenEvent)event;
				if(!multiEvent.getChosenIdentities().isEmpty()) {
					doAddUser(appointment, multiEvent.getChosenIdentities());
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if (removeCtrl == source) {
			if (event == Event.DONE_EVENT) {
				updateModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(findingConfirmationCtrl);
		removeAsListenerAndDispose(appointmentDeleteCtrl);
		removeAsListenerAndDispose(addAppointmentsCtrl);
		removeAsListenerAndDispose(appointmentEditCtrl);
		removeAsListenerAndDispose(userSearchCtrl);
		removeAsListenerAndDispose(removeCtrl);
		removeAsListenerAndDispose(cmc);
		findingConfirmationCtrl = null;
		appointmentDeleteCtrl = null;
		addAppointmentsCtrl = null;
		appointmentEditCtrl = null;
		userSearchCtrl = null;
		removeCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doToggleShowMoreParticipations(AppointmentRow row) {
		Appointment appointment = row.getAppointment();
		if (showAllParticipations.contains(appointment)) {
			showAllParticipations.remove(appointment);
		} else {
			showAllParticipations.add(appointment);
		}
		updateModel();
	}
	
	private void doToggleParticipation(UserRequest ureq, AppointmentRow row) {
		if (row.getParticipation() == null) {
			if (topic.isAutoConfirmation()) {
				doSelfConfirmParticipation(ureq, row);
			} else {
				doCreateParticipation(row.getAppointment());
			}
		} else {
			appointmentsService.deleteParticipation(row.getParticipation());
		}
		updateModel();
	}

	private void doSelfConfirmParticipation(UserRequest ureq, AppointmentRow row) {
		String formatedDate;
		if (StringHelper.containsNonWhitespace(row.getTime())) {
			formatedDate = row.getDate() + ", " + row.getTime();
		} else if (StringHelper.containsNonWhitespace(row.getDateLong())) {
			formatedDate = row.getDateLong();
		} else {
			formatedDate = row.getDate() + ", " + translate("full.day.lower");
		}
		
		String title = translate("confirm.participation.self.title");
		String text = topic.isMultiParticipation()
				? translate("confirm.participation.self.multi", new String[] { formatedDate })
				: translate("confirm.participation.self", new String[] { formatedDate });
		confirmParticipationCrtl = activateYesNoDialog(ureq, title, text, confirmParticipationCrtl);
		confirmParticipationCrtl.setUserObject(row.getAppointment());
	}

	private void doCreateParticipation(Appointment appointment) {
		ParticipationResult participationResult = appointmentsService.createParticipations(appointment,
				singletonList(getIdentity()), getIdentity(), topic.isMultiParticipation(), topic.isAutoConfirmation(), true);
		if (ParticipationResult.Status.ok != participationResult.getStatus()) {
			showWarning("participation.not.created");
		}
	}

	private void doAddIndividualAppointments(UserRequest ureq) {
		addAppointmentsCtrl = new AppointmentCreateController(ureq, getWindowControl(), topic, false);
		listenTo(addAppointmentsCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", addAppointmentsCtrl.getInitialComponent(), true,
				translate("add.appointment.individual"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doAddRecurringAppointment(UserRequest ureq) {
		addAppointmentsCtrl = new AppointmentCreateController(ureq, getWindowControl(), topic, true);
		listenTo(addAppointmentsCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", addAppointmentsCtrl.getInitialComponent(), true,
				translate("add.appointment.recurring"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doEditAppointment(UserRequest ureq, Appointment appointment) {
		appointmentEditCtrl = new AppointmentEditController(ureq, getWindowControl(), appointment);
		listenTo(appointmentEditCtrl);

		cmc = new CloseableModalController(getWindowControl(), "close", appointmentEditCtrl.getInitialComponent(), true,
				translate("edit.appointment.title"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmDeletion(UserRequest ureq, Appointment appointment) {
		appointmentDeleteCtrl = new AppointmentDeleteController(ureq, getWindowControl(), appointment);
		listenTo(appointmentDeleteCtrl);

		cmc = new CloseableModalController(getWindowControl(), "close", appointmentDeleteCtrl.getInitialComponent(),
				true, translate("confirm.appointment.delete.title"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doConfirm(UserRequest ureq, Appointment appointment) {
		if (Status.planned == appointment.getStatus()) {
			if (Type.finding == topic.getType()) {
				doConfirmFinding(ureq, appointment);
			} else {
				appointmentsService.confirmAppointment(appointment);
			}
		} else {
			appointmentsService.unconfirmAppointment(appointment);
		}
		updateModel();
	}

	private void doConfirmFinding(UserRequest ureq, Appointment appointment) {
		findingConfirmationCtrl = new FindingConfirmationController(ureq, getWindowControl(), appointment);
		listenTo(findingConfirmationCtrl);

		cmc = new CloseableModalController(getWindowControl(), "close", findingConfirmationCtrl.getInitialComponent(), true,
				translate("edit.appointment.title"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doSelectUser(UserRequest ureq, Appointment appointment) {
		userSearchCtrl = new UserSearchController(ureq, getWindowControl(), true, true, false);
		userSearchCtrl.setUserObject(appointment);
		listenTo(userSearchCtrl);
		
		String title = translate("add.user.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), userSearchCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddUser(Appointment appointment, List<Identity> identities) {
		ParticipationResult result = appointmentsService.createParticipations(appointment, identities, getIdentity(),
				topic.isMultiParticipation(), topic.isAutoConfirmation(), false);
		if (ParticipationResult.Status.appointmentFull == result.getStatus()) {
			showWarning("error.not.as.many.participations.left");
		} else if (ParticipationResult.Status.ok != result.getStatus()) {
			showWarning("participations.not.created");
		}
		updateModel();
	}
	
	private void doRemove(UserRequest ureq, Appointment appointment) {
		removeCtrl = new ParticipationRemoveController(ureq, getWindowControl(), appointment);
		listenTo(removeCtrl);

		cmc = new CloseableModalController(getWindowControl(), "close", removeCtrl.getInitialComponent(),
				true, translate("remove.user.title"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doExportParticipations(UserRequest ureq, Appointment appointment) {
		ParticipationSearchParams searchParams = new ParticipationSearchParams();
		searchParams.setAppointment(appointment);
		ExcelExport export = new ExcelExport(ureq, searchParams, getExportName(topic));
		ureq.getDispatchResult().setResultingMediaResource(export.createMediaResource());
	}

	private String getExportName(Topic topic) {
		return new StringBuilder()
				.append(translate("export.participations.file.prefix"))
				.append("_")
				.append(topic.getTitle())
				.append("_")
				.append(Formatter.formatDatetimeFilesystemSave(new Date()))
				.toString();
	}

	private void doOpenRecording(UserRequest ureq, BigBlueButtonRecordingReference recordingReference) {
		String url = appointmentsService.getRecordingUrl(ureq.getUserSession(), recordingReference);
		if(StringHelper.containsNonWhitespace(url)) {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(url));
		} else {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
			showWarning("warning.recording.not.found");
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> cmps = new ArrayList<>(1);
		if (rowObject instanceof AppointmentRow) {
			AppointmentRow appointmentRow = (AppointmentRow)rowObject;
			if (appointmentRow.getDayEl() != null) {
				cmps.add(appointmentRow.getDayEl().getComponent());
			}
			if (appointmentRow.getShowMoreLink() != null) {
				cmps.add(appointmentRow.getShowMoreLink());
			}
		}
		return cmps;
	}
	
	public final static class FormItemList implements FormItemCollection {
		
		private List<FormItem> items;
		
		FormItemList(int initialCapacity) {
			items = new ArrayList<>(initialCapacity);
		}

		public void add(FormItem item) {
			items.add(item);
		}

		@Override
		public Iterable<FormItem> getFormItems() {
			return items;
		}

		@Override
		public FormItem getFormComponent(String name) {
			for (FormItem item : items) {
				if (name.equals(item.getName())) {
					return item;
				}
			}
			return null;
		}
		
	}
	
}
