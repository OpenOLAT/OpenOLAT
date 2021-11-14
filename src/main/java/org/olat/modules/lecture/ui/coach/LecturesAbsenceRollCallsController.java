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
package org.olat.modules.lecture.ui.coach;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.coach.ui.UserListController;
import org.olat.modules.lecture.AbsenceNotice;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.LectureBlockRollCallSearchParameters;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockRollCallAndCoach;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.coach.LecturesAbsenceRollCallsTableModel.AbsenceCallCols;
import org.olat.modules.lecture.ui.component.LectureAbsenceRollCallCellRenderer;
import org.olat.modules.lecture.ui.component.LectureBlockTimesCellRenderer;
import org.olat.modules.lecture.ui.filter.LectureBlockRollCallAndCoachFilter;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 mai 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesAbsenceRollCallsController extends FormBasicController {

	private static final String[] unauthorizedKeys = new String[] { "all", "unauthorized" };
	
	public static final int USER_PROPS_OFFSET = 500;
	public static final String USER_USAGE_IDENTIFIER = UserListController.usageIdentifyer;
	
	private FormLink authorizeButton;
	private FlexiTableElement tableEl;
	private SingleSelection unauthorizedFilterEl;
	private LecturesAbsenceRollCallsTableModel tableModel;
	private LectureBlockRollCallSearchParameters searchParams;
	
	private final boolean showTimeOnly;
	private final boolean absenceNoticeEnabled;
	private final boolean absenceDefaultAuthorized;
	private final boolean authorizedAbsenceEnabled;
	private final LecturesSecurityCallback secCallback;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	private CloseableModalController cmc;
	private ConfirmAuthorizeRollCallController authorizeCtrl;
	private CloseableCalloutWindowController noticeCalloutCtrl;
	private AbsenceNoticeDetailsCalloutController noticeDetailsCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public LecturesAbsenceRollCallsController(UserRequest ureq, WindowControl wControl,
			LectureBlockRollCallSearchParameters searchParams, boolean showTimeOnly, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, "absences_list", Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.secCallback = secCallback;
		this.searchParams = searchParams;
		this.showTimeOnly = showTimeOnly;
		
		absenceDefaultAuthorized = lectureModule.isAbsenceDefaultAuthorized();
		authorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();
		absenceNoticeEnabled = lectureModule.isAbsenceNoticeEnabled();

		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_USAGE_IDENTIFIER, isAdministrativeUser);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String[] unauthorizedValues = new String[] { translate("all"), translate("unauthorized.filter") };
		unauthorizedFilterEl = uifactory.addRadiosHorizontal("unauthorized.filter", "unauthorized.filter.label", formLayout,
				unauthorizedKeys, unauthorizedValues);
		unauthorizedFilterEl.addActionListener(FormEvent.ONCHANGE);
		unauthorizedFilterEl.select(unauthorizedKeys[0], true);
		unauthorizedFilterEl.setVisible(authorizedAbsenceEnabled);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AbsenceCallCols.id));

		int colIndex = USER_PROPS_OFFSET;
		for (int i = 0; i<userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(USER_USAGE_IDENTIFIER, userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex,
					null, true, "userProp-" + colIndex));
			colIndex++;
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AbsenceCallCols.lectureBlockDate, new LectureBlockTimesCellRenderer(showTimeOnly, getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AbsenceCallCols.externalRef, "open.course"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AbsenceCallCols.entry, "open.course"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AbsenceCallCols.lectureBlockName, "open.lecture"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AbsenceCallCols.lectureBlockLocation));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, AbsenceCallCols.teachers, new TextFlexiCellRenderer(EscapeMode.antisamy)));
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AbsenceCallCols.absentLectures, new LectureAbsenceRollCallCellRenderer()));
		if(authorizedAbsenceEnabled) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AbsenceCallCols.authorizedAbsence));
		}
		if(absenceNoticeEnabled) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AbsenceCallCols.absenceNotice));
		}
		
		tableModel = new LecturesAbsenceRollCallsTableModel(columnsModel, userPropertyHandlers, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 24, false, getTranslator(), formLayout);
		tableEl.setEmptyTableMessageKey("empty.absences.list");
		tableEl.setAndLoadPersistedPreferences(ureq, "absences-rollcalls-list-" + secCallback.viewAs() + "-" + showTimeOnly);
		
		if(authorizedAbsenceEnabled && secCallback.canAuthorizeAbsence()) {
			tableEl.setMultiSelect(true);
			tableEl.setSelectAllEnable(true);
			authorizeButton = uifactory.addFormLink("absences.batch.authorize", formLayout, Link.BUTTON);
			tableEl.addBatchButton(authorizeButton);
		}
	}
	
	protected void loadModel() {
		boolean unauthorizedOnly = unauthorizedFilterEl.isSelected(1);
		String separator = translate("user.fullname.separator");
		List<LectureBlockRollCallAndCoach> rollCalls = lectureService.getLectureBlockAndRollCalls(searchParams, separator);
		List<LectureAbsenceRollCallRow> rows = new ArrayList<>(rollCalls.size());
		
		if(StringHelper.containsNonWhitespace(searchParams.getSearchString())) {
			final LectureBlockRollCallAndCoachFilter filter = new LectureBlockRollCallAndCoachFilter(searchParams.getSearchString(),
					userPropertyHandlers, getLocale());
			rollCalls = rollCalls.stream()
					.filter(filter)
					.collect(Collectors.toList());
		}
		
		for(LectureBlockRollCallAndCoach rollCall:rollCalls) {
			boolean authorized = isAuthorized(rollCall);
			if(unauthorizedOnly && authorized) {
				continue;
			}
			rows.add(forgeRow(rollCall, authorized));
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private LectureAbsenceRollCallRow forgeRow(LectureBlockRollCallAndCoach call, boolean authorized) {
		AbsenceNotice notice = call.getAbsenceNotice();
		LectureBlockRollCall rollCall = call.getRollCall();
		LectureAbsenceRollCallRow row = new LectureAbsenceRollCallRow(call.getLectureBlock(), call.getEntry(), rollCall, notice, call.getCoach());
		if(notice != null) {
			FormLink noticeLink = uifactory.addFormLink("notice_" + rollCall.getKey(), "notice", "", null, flc, Link.LINK | Link.NONTRANSLATED);
			noticeLink.setIconRightCSS("o_icon o_icon_info o_icon-lg");
			noticeLink.setUserObject(row);
			row.setNoticeLink(noticeLink);
		}
		row.setAuthorized(authorized);
		return row;
	}
	
	private boolean isAuthorized(LectureBlockRollCallAndCoach call) {
		AbsenceNotice notice = call.getAbsenceNotice();
		LectureBlockRollCall rollCall = call.getRollCall();
		return isAuthorized(rollCall, notice);
	}
	
	private boolean isAuthorized(LectureBlockRollCall rollCall, AbsenceNotice notice) {	
		Boolean absenceAuthorized = rollCall.getAbsenceAuthorized(); 
		Boolean absenceNoticeAuthorized = notice == null ? null : notice.getAbsenceAuthorized();
		
		if(absenceNoticeAuthorized != null) {// notice override roll call
			absenceAuthorized = absenceNoticeAuthorized;
		}
		
		if(notice != null) {
			if(absenceNoticeAuthorized != null) {
				return absenceAuthorized.booleanValue();
			} else {
				return absenceDefaultAuthorized;
			}
		} else if(rollCall.getLecturesAbsentNumber() > 0) {
			if(absenceAuthorized != null) {
				return absenceAuthorized.booleanValue();
			} else {
				return absenceDefaultAuthorized;
			}
		}
		return true;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(authorizeCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(noticeDetailsCtrl == source) {
			noticeCalloutCtrl.deactivate();
			cleanUp();
		} else if(noticeCalloutCtrl == source || cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(noticeDetailsCtrl);
		removeAsListenerAndDispose(noticeCalloutCtrl);
		removeAsListenerAndDispose(authorizeCtrl);
		removeAsListenerAndDispose(cmc);
		noticeDetailsCtrl = null;
		noticeCalloutCtrl = null;
		authorizeCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(authorizeButton == source) {
			doAuthorize(ureq);
		} else if(unauthorizedFilterEl == source) {
			loadModel();
		} else if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("open.course".equals(cmd)) {
					LectureAbsenceRollCallRow row = tableModel.getObject(se.getIndex());
					doOpenCourseLectures(ureq, row);
				} else if("open.lecture".equals(cmd)) {
					LectureAbsenceRollCallRow row = tableModel.getObject(se.getIndex());
					doOpenCourseLecture(ureq, row);
				}
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("notice".equals(link.getCmd()) && link.getUserObject() instanceof LectureAbsenceRollCallRow) {
				doOpenAbsenceNoticeCallout(ureq, link.getFormDispatchId(), (LectureAbsenceRollCallRow)link.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenCourseLectures(UserRequest ureq, LectureAbsenceRollCallRow row) {
		Long repoKey = row.getLectureBlock().getEntry().getKey();
		String businessPath = "[RepositoryEntry:" + repoKey + "][Lectures:0]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doOpenCourseLecture(UserRequest ureq, LectureAbsenceRollCallRow row) {
		Long repoKey = row.getLectureBlock().getEntry().getKey();
		String businessPath = "[RepositoryEntry:" + repoKey + "][Lectures:0][LectureBlock:" + row.getLectureBlock().getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doOpenAbsenceNoticeCallout(UserRequest ureq, String elementId, LectureAbsenceRollCallRow row) {
		noticeDetailsCtrl = new AbsenceNoticeDetailsCalloutController(ureq, getWindowControl(), row.getAbsenceNotice());
		listenTo(noticeDetailsCtrl);

		noticeCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				noticeDetailsCtrl.getInitialComponent(), elementId, "", true, "");
		listenTo(noticeCalloutCtrl);
		noticeCalloutCtrl.activate();
	}
	
	private void doAuthorize(UserRequest ureq) {
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		List<LectureBlockRollCall> rollCalls = selectedIndex.stream()
			.map(index -> tableModel.getObject(index.intValue()))
			.filter(row -> !isAuthorized(row.getRollCall(), row.getAbsenceNotice()))
			.map(LectureAbsenceRollCallRow::getRollCall)
			.collect(Collectors.toList());
		
		if(rollCalls.isEmpty()) {
			showWarning("warning.choose.at.least.one.unauthorized.absence");
		} else {
			authorizeCtrl = new ConfirmAuthorizeRollCallController(ureq, getWindowControl(), rollCalls);
			listenTo(authorizeCtrl);
	
			String title = translate("absences.batch.authorize");
			cmc = new CloseableModalController(getWindowControl(), "close", authorizeCtrl.getInitialComponent(), true, title, true);
			listenTo(cmc);
			cmc.activate();
		}
	}
}
