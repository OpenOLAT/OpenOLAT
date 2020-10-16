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
package org.olat.modules.lecture.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.modules.lecture.LectureBlockAppealStatus;
import org.olat.modules.lecture.LectureBlockRollCall;
import org.olat.modules.lecture.LectureBlockRollCallSearchParameters;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LectureBlockRollCallAndCoach;
import org.olat.modules.lecture.ui.AppealListRepositoryDataModel.AppealCols;
import org.olat.modules.lecture.ui.component.LectureBlockAppealStatusCellRenderer;
import org.olat.modules.lecture.ui.component.LectureBlockRollCallStatusCellRenderer;
import org.olat.modules.lecture.ui.component.LecturesCompulsoryRenderer;
import org.olat.modules.lecture.ui.filter.AppealRollCallRowFilter;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 juin 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AppealListRepositoryController extends FormBasicController {

	protected static final String USER_PROPS_ID = TeacherRollCallController.USER_PROPS_ID;
	protected static final int USER_PROPS_OFFSET = 500;
	
	private FormLink batchUpdateButton;
	private FlexiTableElement tableEl;
	private AppealListRepositoryDataModel tableModel;
	
	private CloseableModalController cmc;
	private EditAppealController appealCtrl;
	
	private final boolean authorizedAbsenceEnabled;
	private final boolean absenceDefaultAuthorized;
	
	private final RepositoryEntry entry;
	private final Identity profiledIdentity;
	private final LecturesSecurityCallback secCallback;
	private List<UserPropertyHandler> userPropertyHandlers;
	private LectureBlockRollCallSearchParameters searchParams = new LectureBlockRollCallSearchParameters();
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public AppealListRepositoryController(UserRequest ureq, WindowControl wControl, LecturesSecurityCallback secCallback) {
		this(ureq, wControl, null, null, secCallback);
	}
	
	public AppealListRepositoryController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry,
			LecturesSecurityCallback secCallback) {
		this(ureq, wControl, entry, null, secCallback);
		loadModel(true);
	}
	
	public AppealListRepositoryController(UserRequest ureq, WindowControl wControl, Identity profiledIdentity,
			LecturesSecurityCallback secCallback) {
		this(ureq, wControl, null, profiledIdentity, secCallback);
		loadModel(true);
	}
	
	public AppealListRepositoryController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, Identity profiledIdentity, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, "appeal_table");
		this.entry = entry;
		this.secCallback = secCallback;
		this.profiledIdentity = profiledIdentity;
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);

		authorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();
		absenceDefaultAuthorized = lectureModule.isAbsenceDefaultAuthorized();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(secCallback.viewAs() != LectureRoles.participant) {
			int colPos = USER_PROPS_OFFSET;
			for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
				if (userPropertyHandler == null) continue;
	
				String propName = userPropertyHandler.getName();
				boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);
	
				FlexiColumnModel col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, true, propName);
				columnsModel.addFlexiColumnModel(col);
				colPos++;
			}
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppealCols.lectureBlockDate, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppealCols.lectureBlockName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppealCols.coach));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppealCols.plannedLectures, new LecturesCompulsoryRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppealCols.attendedLectures));
		if(authorizedAbsenceEnabled) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppealCols.unauthorizedAbsentLectures));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppealCols.authorizedAbsentLectures));
		} else {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppealCols.absentLectures));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppealCols.lectureBlockStatus,
				new LectureBlockRollCallStatusCellRenderer(authorizedAbsenceEnabled, absenceDefaultAuthorized, getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppealCols.appealStatus,
				new LectureBlockAppealStatusCellRenderer(getTranslator())));
		
		if(secCallback.canApproveAppeal()) {
			DefaultFlexiColumnModel editColumn = new DefaultFlexiColumnModel("table.header.edit", translate("table.header.edit"), "edit");
			editColumn.setExportable(false);
			columnsModel.addFlexiColumnModel(editColumn);
		}
		
		tableModel = new AppealListRepositoryDataModel(columnsModel,authorizedAbsenceEnabled, absenceDefaultAuthorized, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		

		List<FlexiTableFilter> filters = new ArrayList<>(16);
		filters.add(new FlexiTableFilter(translate("appeal.".concat(LectureBlockAppealStatus.pending.name())),
				LectureBlockAppealStatus.pending.name()));
		filters.add(new FlexiTableFilter(translate("appeal.".concat(LectureBlockAppealStatus.approved.name())),
				LectureBlockAppealStatus.approved.name()));
		filters.add(new FlexiTableFilter(translate("appeal.".concat(LectureBlockAppealStatus.rejected.name())),
				LectureBlockAppealStatus.rejected.name()));
		tableEl.setFilters("filer", filters, true);
		tableEl.setExportEnabled(true);
		tableEl.setEmtpyTableMessageKey("empty.appeals.list");
		tableEl.setAndLoadPersistedPreferences(ureq, "appeal-roll-call-v2");

		if(secCallback.canApproveAppeal()) {
			tableEl.setMultiSelect(true);
			batchUpdateButton = uifactory.addFormLink("appeal.batch.update", formLayout, Link.BUTTON);
			batchUpdateButton.setVisible(false);
		}
	}
	
	public void loadModel(LectureBlockRollCallSearchParameters params) {
		this.searchParams = params;
		loadModel(true);
	}
	
	public void reloadModel() {
		loadModel(false);
	}
	
	protected void loadModel(boolean reset) {
		searchParams.setEntry(entry);
		searchParams.setCalledIdentity(profiledIdentity);
		searchParams.setViewAs(getIdentity(), secCallback.viewAs());
		
		if(searchParams.getAppealStatus() == null) {
			List<LectureBlockAppealStatus> status = new ArrayList<>();
			status.add(LectureBlockAppealStatus.pending);
			status.add(LectureBlockAppealStatus.approved);
			status.add(LectureBlockAppealStatus.rejected);
			searchParams.setAppealStatus(status);
		}

		List<LectureBlockRollCallAndCoach> rollCallsWithCoach = lectureService.getLectureBlockAndRollCalls(searchParams);
		List<AppealRollCallRow> rows = new ArrayList<>(rollCallsWithCoach.size());
		for(LectureBlockRollCallAndCoach rollCallWithCoach:rollCallsWithCoach) {
			rows.add(new AppealRollCallRow(rollCallWithCoach, userPropertyHandlers, getLocale()));
		}
		
		if(StringHelper.containsNonWhitespace(searchParams.getSearchString())) {
			final AppealRollCallRowFilter filter = new AppealRollCallRowFilter(searchParams.getSearchString());
			rows = rows.stream()
					.filter(filter)
					.collect(Collectors.toList());
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(reset, reset, true);
		if(batchUpdateButton != null) {
			batchUpdateButton.setVisible(tableModel.getRowCount() > 0);
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(appealCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel(false);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(appealCtrl);
		removeAsListenerAndDispose(cmc);
		appealCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				AppealRollCallRow row = tableModel.getObject(se.getIndex());
				if("edit".equals(cmd)) {
					doEditAppeal(ureq, row.getRollCall());
				}
			}
		} else if(batchUpdateButton == source) {
			List<LectureBlockRollCall> rollCalls = tableEl.getMultiSelectedIndex().stream()
				.map(index -> tableModel.getObject(index.intValue())).filter(Objects::nonNull)
				.map(AppealRollCallRow::getRollCall).collect(Collectors.toList());
			doEditAppeals(ureq, rollCalls);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doEditAppeal(UserRequest ureq, LectureBlockRollCall rollCall) {
		if(guardModalController(appealCtrl) || !secCallback.canApproveAppeal()) return;
		
		appealCtrl = new EditAppealController(ureq, getWindowControl(), rollCall);
		listenTo(appealCtrl);
		
		String title = translate("appeal.title", new String[]{ rollCall.getLectureBlock().getTitle() });
		cmc = new CloseableModalController(getWindowControl(), "close", appealCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditAppeals(UserRequest ureq, List<LectureBlockRollCall> rollCalls) {
		if(guardModalController(appealCtrl) || !secCallback.canApproveAppeal()) return;
		
		if(rollCalls.isEmpty()) {
			showWarning("warning.choose.at.least.one.appeal");
		} else {
			appealCtrl = new EditAppealController(ureq, getWindowControl(), rollCalls);
			listenTo(appealCtrl);
			
			String title = translate("appeals.update.title", new String[]{ Integer.toString(rollCalls.size()) });
			cmc = new CloseableModalController(getWindowControl(), "close", appealCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}
}
