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

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Roles;
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
	
	private FlexiTableElement tableEl;
	private AppealListRepositoryDataModel tableModel;
	
	private CloseableModalController cmc;
	private EditAppealController appealCtrl;
	
	private final boolean authorizedAbsenceEnabled;
	private final boolean absenceDefaultAuthorized;
	
	private final RepositoryEntry entry;
	private final boolean isAdministrativeUser;
	private List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public AppealListRepositoryController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl, "appeal_table");
		this.entry = entry;
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		Roles roles = ureq.getUserSession().getRoles();
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);

		authorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();
		absenceDefaultAuthorized = lectureModule.isAbsenceDefaultAuthorized();
		
		initForm(ureq);
		loadModel(true);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(isAdministrativeUser) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AppealCols.username));
		}
		
		int colPos = USER_PROPS_OFFSET;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;

			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);

			FlexiColumnModel col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, true, propName);
			columnsModel.addFlexiColumnModel(col);
			colPos++;
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
		DefaultFlexiColumnModel editColumn = new DefaultFlexiColumnModel("table.header.edit", translate("table.header.edit"), "edit");
		editColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(editColumn);
		
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
		tableEl.setAndLoadPersistedPreferences(ureq, "appeal-roll-call");
	}
	
	private void loadModel(boolean reset) {
		LectureBlockRollCallSearchParameters params = new LectureBlockRollCallSearchParameters();
		params.setEntry(entry);
		List<LectureBlockAppealStatus> status = new ArrayList<>();
		status.add(LectureBlockAppealStatus.pending);
		status.add(LectureBlockAppealStatus.approved);
		status.add(LectureBlockAppealStatus.rejected);
		params.setAppealStatus(status);
		
		List<LectureBlockRollCallAndCoach> rollCallsWithCoach = lectureService.getLectureBlockAndRollCalls(params);
		List<AppealRollCallRow> rows = new ArrayList<>(rollCallsWithCoach.size());
		for(LectureBlockRollCallAndCoach rollCallWithCoach:rollCallsWithCoach) {
			rows.add(new AppealRollCallRow(rollCallWithCoach, userPropertyHandlers, getLocale()));
		}
		tableModel.setObjects(rows);
		tableEl.reset(reset, reset, true);
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
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doEditAppeal(UserRequest ureq, LectureBlockRollCall rollCall) {
		if(appealCtrl != null) return;
		
		appealCtrl = new EditAppealController(ureq, getWindowControl(), rollCall);
		listenTo(appealCtrl);
		
		String title = translate("appeal.title", new String[]{ rollCall.getLectureBlock().getTitle() });
		cmc = new CloseableModalController(getWindowControl(), "close", appealCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
}
