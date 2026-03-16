/**

 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.notifications;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.RecruitingAuditLogLight;
import org.olat.modules.selectus.model.log.RecruitingAuditLogSearchParameters;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;
import org.olat.modules.selectus.ui.components.DateTimeCellRenderer;
import org.olat.modules.selectus.ui.events.SelectApplicationEvent;
import org.olat.modules.selectus.ui.events.SelectPositionLightEvent;
import org.olat.modules.selectus.ui.notifications.NotificationListDataModel.AuditCols;

/**
 * 
 * Initial date: 22 août 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class NotificationListController extends FormBasicController {

	private FormLink markAsReadButton;
	private FormLink markAsUnreadButton;
	private FormLink markAllAsReadButton;
	private SingleSelection filterByTypeEl;
	private DateChooser beginDateEl;
	private DateChooser endDateEl;
	
	private FlexiTableElement tableEl;
	private NotificationListDataModel dataModel;
	
	private final Roles roles;
	private PositionRef position;
	private ApplicationRef application;
	private final RecruitingAuditLogSearchParameters params = new RecruitingAuditLogSearchParameters();
	
	@Autowired
	private AuditService auditService;
	@Autowired
	private RecruitingService recruitingService;
	
	public NotificationListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "notifications", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));// don't change the direction of the fallback translator
		roles = ureq.getUserSession().getRoles();
		initForm(ureq);
	}
	
	public NotificationListController(UserRequest ureq, WindowControl wControl, PositionRef position) {
		super(ureq, wControl, "notifications", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));// don't change the direction of the fallback translator
		roles = ureq.getUserSession().getRoles();
		this.position = position;
		initForm(ureq);
	}
	
	public NotificationListController(UserRequest ureq, WindowControl wControl, PositionRef position, ApplicationRef application) {
		super(ureq, wControl, "notifications", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));// don't change the direction of the fallback translator
		roles = ureq.getUserSession().getRoles();
		this.position = position;
		this.application = application;
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		markAsReadButton = uifactory.addFormLink("mark.read", formLayout, Link.BUTTON);
		markAsUnreadButton = uifactory.addFormLink("mark.unread", formLayout, Link.BUTTON);
		markAllAsReadButton = uifactory.addFormLink("mark.all.read", formLayout, Link.BUTTON);
		
		List<String> typeFilterKeys = new ArrayList<>();
		List<String> typeFilterValues = new ArrayList<>();
		typeFilterKeys.add("showall");
		typeFilterValues.add(translate("filter.type.all"));
		for(ActionTarget target:ActionTarget.values()) {
			typeFilterKeys.add(target.name());
			typeFilterValues.add(translate("action.target." + target.name()));
		}
		filterByTypeEl = uifactory.addDropdownSingleselect("search.type", null, formLayout,
				typeFilterKeys.toArray(new String[typeFilterKeys.size()]), typeFilterValues.toArray(new String[typeFilterValues.size()]), null);
		filterByTypeEl.setDomReplacementWrapperRequired(false);
		filterByTypeEl.select(typeFilterKeys.get(0), true);
		filterByTypeEl.addActionListener(FormEvent.ONCHANGE);
		
		beginDateEl = uifactory.addDateChooser("filter.begin.date", null, null, formLayout);
		beginDateEl.setDomReplacementWrapperRequired(false);
		beginDateEl.addActionListener(FormEvent.ONCHANGE);
		endDateEl = uifactory.addDateChooser("filter.end.date", null, null, formLayout);
		endDateEl.setDomReplacementWrapperRequired(false);
		endDateEl.addActionListener(FormEvent.ONCHANGE);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AuditCols.read, new ReadCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AuditCols.time, new DateTimeCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AuditCols.identity));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AuditCols.target, new ActionTargetCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AuditCols.action, new ActionCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AuditCols.message));
		
		StaticFlexiCellRenderer gotoRenderer = new StaticFlexiCellRenderer(translate("goto"), "goto");
		gotoRenderer.setIconRightCSS("o_icon o_icon_start");
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("goto", AuditCols.gotoItem.ordinal(),
				new BooleanCellRenderer(gotoRenderer, null)));
		
		dataModel = new NotificationListDataModel(columnsModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 25, false, getTranslator(), formLayout);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setEmptyTableMessageKey("mark.no.unread");
		
		List<FlexiTableFilter> filters = new ArrayList<>(5);
		filters.add(new FlexiTableFilter(translate("notifications.show.read"), "read"));
		filters.add(new FlexiTableFilter(translate("notifications.show.unread"), "unread"));
		filters.add(FlexiTableFilter.SPACER);
		filters.add(new FlexiTableFilter(translate("notifications.show.all"), "showall", true));
		tableEl.setFilters("", filters, false);
		tableEl.setSelectedFilterKey("unread");
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(AuditCols.time.name(), false));
		tableEl.setSortSettings(sortOptions);
		
		if(roles.isAdministrator() || roles.isSelectusManager()) {
			tableEl.setExportEnabled(true);
		}
		
		if(application != null) {
			tableEl.setAndLoadPersistedPreferences(ureq, "app-notifivations");
		} else if(position != null) {
			tableEl.setAndLoadPersistedPreferences(ureq, "pos-notifications");
		} else {
			tableEl.setAndLoadPersistedPreferences(ureq, "global-notifications");
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(markAsReadButton == source) {
			doMarkAsRead();
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if(markAsUnreadButton == source) {
			doMarkAsUnread();
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if(markAllAsReadButton == source) {
			doMarkAllAsRead();
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("goto".equals(se.getCommand())) {
					AuditLogRow row = dataModel.getObject(se.getIndex());
					goTo(ureq, row);
				}
			} else {
				updateEmptyMessage();
			}
		} else if(filterByTypeEl == source || beginDateEl == source || endDateEl == source) {
			loadModel();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void updateEmptyMessage() {
		//TODO selectus
		//String currentMsg = tableEl.getEmptyTableMessageKey();
		String filterKey = tableEl.getSelectedFilterKey();
		String filterMsg;
		if("read".equals(filterKey)) {
			filterMsg = "mark.no.read";
		} else if("unread".equals(filterKey)) {
			filterMsg = "mark.no.unread";
		} else {
			filterMsg = "mark.not.found";
		}
		
		//if(!filterMsg.equals(currentMsg)) {
			tableEl.setEmptyTableMessageKey(filterMsg);
		//}
	}
	
	public int getNumOfUnreadNotifications() {
		RecruitingAuditLogSearchParameters countParams = new RecruitingAuditLogSearchParameters();
		countParams.setPosition(position);
		countParams.setApplication(application);
		countParams.setUnreadOnly(true);
		return auditService.countLogs(getIdentity(), roles, countParams);
	}
	
	public void unloadModel() {
		dataModel.setObjects(new ArrayList<>(), getIdentity(), roles, params);
		tableEl.reset(true, true, true);
	}

	public void loadModel() {
		params.setPosition(position);
		params.setApplication(application);
		
		if(beginDateEl.getDate() == null) {
			params.setFrom(null);
		} else {
			params.setFrom(beginDateEl.getDate());
		}
		
		if(endDateEl.getDate() == null) {
			params.setUntil(null);
		} else {
			Calendar cal = Calendar.getInstance();
			cal.setTime(endDateEl.getDate());
			cal.set(Calendar.HOUR_OF_DAY, 23);
			cal.set(Calendar.MINUTE, 59);
			cal.set(Calendar.SECOND, 59);
			cal.set(Calendar.MILLISECOND, 999);
			params.setUntil(cal.getTime());
		}
		
		if(filterByTypeEl.isOneSelected() && !filterByTypeEl.isSelected(0)) {
			params.setTarget(ActionTarget.valueOf(filterByTypeEl.getSelectedKey()));
		} else {
			params.setTarget(null);
		}

		List<RecruitingAuditLogLight> logs = auditService.getLightLogs(getIdentity(), roles, params);
		List<AuditLogRow> rows = toRows(logs);
		Set<Long> read = auditService.getReadAuditLogs(getIdentity());
		dataModel.setRead(read);
		dataModel.setPosition(position);
		dataModel.setObjects(rows, getIdentity(), roles, params);
		tableEl.reset(true, true, true);
		
		updateEmptyMessage();
	}
	
	private List<AuditLogRow> toRows(List<RecruitingAuditLogLight> logs) {
		List<AuditLogRow> rows = new ArrayList<>(logs.size());
		Map<Identity,String> identityToName = new HashMap<>();
		for(RecruitingAuditLogLight log:logs) {
			Identity identity = log.getIdentity();
			String fullName = null;
			if(identity != null) {
				fullName = identityToName
					.computeIfAbsent(identity, i -> RecruitingHelper.formatFullNameWithTitle(i, getLocale()));
			}
			rows.add(new AuditLogRow(fullName, log));
		}
		return rows;
	}
	
	private void doMarkAsRead() {
		List<Long> selectedLogEntries = getSelectedRows();
		if(selectedLogEntries.isEmpty()) {
			showWarning("mark.atleastone");
		} else {
			auditService.markAsRead(selectedLogEntries, getIdentity());
		}
		reloadAfterMarks();
	}
	
	private void doMarkAsUnread() {
		List<Long> selectedLogEntries = getSelectedRows();
		if(selectedLogEntries.isEmpty()) {
			showWarning("mark.atleastone");
		} else {
			auditService.markAsUnread(selectedLogEntries, getIdentity());
		}
		reloadAfterMarks();
	}
	
	private void doMarkAllAsRead() {
		List<AuditLogRow> rows = dataModel.getObjects();
		List<Long> logs = rows.stream().map(AuditLogRow::getKey).collect(Collectors.toList());
		auditService.markAsRead(logs, getIdentity());
		reloadAfterMarks();
	}
	
	private void reloadAfterMarks() {
		Set<Long> read = auditService.getReadAuditLogs(getIdentity());
		dataModel.setRead(read);
		tableEl.reset(true, true, true);
	}
	
	private List<Long> getSelectedRows() {
		Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
		List<Long> selectedLog = new ArrayList<>(selectedIndexes.size());
		for(Integer selectedIndex:selectedIndexes) {
			AuditLogRow log = dataModel.getObject(selectedIndex.intValue());
			if(log != null) {
				selectedLog.add(log.getKey());
			}
		}
		return selectedLog;
	}
	
	private void goTo(UserRequest ureq, AuditLogRow row) {
		if(row.getApplicationKey() != null) {
			Application reloadedApp = recruitingService.getApplicationByKey(row.getApplicationKey());
			if(reloadedApp == null) {
				if(row.getTarget() == ActionTarget.application && row.getAction() == Action.delete && position == null) {
					List<ContextEntry> activation = BusinessControlFactory.getInstance()
							.createCEListFromString(OresHelper.createOLATResourceableType("Applications"));
					fireEventLightPosition(ureq, row, activation);
				} else {
					showWarning("warning.application.deleted");
				}
			} else if(row.getCommentKey() != null) {
				List<ContextEntry> activation = BusinessControlFactory.getInstance()
						.createCEListFromString(OresHelper.createOLATResourceableInstance("Comment", row.getCommentKey()));
				fireEvent(ureq, new SelectApplicationEvent(reloadedApp, activation));
			} else if(ActionTarget.review.equals(row.getTarget())) {
				List<ContextEntry> activation = BusinessControlFactory.getInstance()
						.createCEListFromString(OresHelper.createOLATResourceableInstance("Review", row.getIdentityKey()));
				fireEvent(ureq, new SelectApplicationEvent(reloadedApp, activation));
			} else if(ActionTarget.committee.equals(row.getTarget())) {
				List<ContextEntry> activation = BusinessControlFactory.getInstance()
						.createCEListFromString(OresHelper.createOLATResourceableInstance("Committee", row.getIdentityKey()));
				fireEvent(ureq, new SelectApplicationEvent(reloadedApp, activation));
			} else {
				fireEvent(ureq, new SelectApplicationEvent(reloadedApp));
			}
		} else if(ActionTarget.committee.equals(row.getTarget())) {
			List<ContextEntry> activation = BusinessControlFactory.getInstance()
					.createCEListFromString(OresHelper.createOLATResourceableInstance("Committee", row.getIdentityKey()));
			fireEventLightPosition(ureq, row, activation);
		} else if(ActionTarget.position.equals(row.getTarget())
				&& (Action.changeConfiguration.equals(row.getAction()) || Action.changeStatus.equals(row.getAction()))) {
			List<ContextEntry> activation = BusinessControlFactory.getInstance()
					.createCEListFromString(OresHelper.createOLATResourceableType("Details"));
			fireEventLightPosition(ureq, row, activation);
		} else {
			List<ContextEntry> activation = BusinessControlFactory.getInstance()
					.createCEListFromString(OresHelper.createOLATResourceableType("Applications"));
			fireEventLightPosition(ureq, row, activation);
		}
	}
	
	private void fireEventLightPosition(UserRequest ureq, AuditLogRow row, List<ContextEntry> activation) {
		PositionRef eventPosition = position;
		if(eventPosition == null) {
			eventPosition = recruitingService.getPosition(row.getPositionKey());
		}
		fireEvent(ureq, new SelectPositionLightEvent(eventPosition, activation));
	}
}
