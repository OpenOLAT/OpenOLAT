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
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.model.LectureBlockStatistics;
import org.olat.modules.lecture.ui.ParticipantListDataModel.ParticipantsCols;
import org.olat.modules.lecture.ui.component.LectureStatisticsCellRenderer;
import org.olat.modules.lecture.ui.component.LongCellRenderer;
import org.olat.modules.lecture.ui.component.ParticipantInfosRenderer;
import org.olat.modules.lecture.ui.component.PercentCellRenderer;
import org.olat.modules.lecture.ui.component.RateWarningCellRenderer;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 avr. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ParticipantListRepositoryController extends FormBasicController {
	
	public static final String USER_PROPS_ID = ParticipantListRepositoryController.class.getCanonicalName();

	public static final int USER_PROPS_OFFSET = 500;
	
	private List<UserPropertyHandler> userPropertyHandlers;
	
	private FlexiTableElement tableEl;
	private ParticipantListDataModel tableModel;

	private CloseableModalController cmc;
	private EditParticipantSummaryController editRateCtrl;
	
	private final double defaultRate;
	private final boolean rateEnabled;
	private final boolean rollCallEnabled;
	private final boolean authorizedAbsenceEnabled;
	
	private final boolean printView;
	private final RepositoryEntry entry;
	private final LecturesSecurityCallback secCallback;
	private RepositoryEntryLectureConfiguration lectureConfig;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private BaseSecurity securityManager;
	
	public ParticipantListRepositoryController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, LecturesSecurityCallback secCallback, boolean printView) {
		super(ureq, wControl, "participant_list_overview");
		this.entry = entry;
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.printView = printView;
		this.secCallback = secCallback;
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);

		lectureConfig = lectureService.getRepositoryEntryLectureConfiguration(entry);
		rateEnabled = ConfigurationHelper.isRateEnabled(lectureConfig, lectureModule);
		if(lectureConfig.isOverrideModuleDefault()) {
			defaultRate = lectureConfig.getRequiredAttendanceRate() == null ?
					lectureModule.getRequiredAttendanceRateDefault() : lectureConfig.getRequiredAttendanceRate().doubleValue();
			rollCallEnabled	= lectureConfig.getRollCallEnabled() == null ?
					lectureModule.isRollCallDefaultEnabled() : lectureConfig.getRollCallEnabled();		
		} else {
			defaultRate = lectureModule.getRequiredAttendanceRateDefault();
			rollCallEnabled = lectureModule.isRollCallDefaultEnabled();
		}
		authorizedAbsenceEnabled = lectureModule.isAuthorizedAbsenceEnabled();

		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			if(!printView) {
				layoutCont.contextPut("winid", "w" + layoutCont.getFormItemComponent().getDispatchID());
				layoutCont.getFormItemComponent().addListener(this);
				layoutCont.getFormItemComponent().contextPut("withPrint", Boolean.TRUE);
			}
			layoutCont.contextPut("printCommand", Boolean.valueOf(printView));
		}

		FlexiTableSortOptions options = new FlexiTableSortOptions();
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		int colPos = USER_PROPS_OFFSET;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;

			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);

			FlexiColumnModel col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, true, propName);
			columnsModel.addFlexiColumnModel(col);
			colPos++;
			
			if(!options.hasDefaultOrderBy()) {
				options.setDefaultOrderBy(new SortKey(propName, true));
			} else if(UserConstants.LASTNAME.equals(propName)) {
				options.setDefaultOrderBy(new SortKey(propName, true));
			}
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantsCols.plannedLectures));

		if(rollCallEnabled) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantsCols.attendedLectures));
			if(authorizedAbsenceEnabled) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantsCols.unauthorizedAbsenceLectures));
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantsCols.authorizedAbsenceLectures));
			} else {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantsCols.absentLectures,
						new LongCellRenderer("o_sel_absences")));
			}
			FlexiColumnModel progressCol = new DefaultFlexiColumnModel(ParticipantsCols.progress, new LectureStatisticsCellRenderer());
			progressCol.setExportable(false);
			columnsModel.addFlexiColumnModel(progressCol);
		}
		if(rateEnabled) {
			FlexiColumnModel warningCol = new DefaultFlexiColumnModel(ParticipantsCols.rateWarning, new RateWarningCellRenderer(getTranslator()));
			warningCol.setExportable(false);
			columnsModel.addFlexiColumnModel(warningCol);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantsCols.rate, new PercentCellRenderer()));
		}
		
		FlexiColumnModel infoCol = new DefaultFlexiColumnModel(ParticipantsCols.infos, new ParticipantInfosRenderer(getTranslator(), defaultRate));
		infoCol.setExportable(false);
		columnsModel.addFlexiColumnModel(infoCol);
		
		if(!printView && secCallback.canChangeRates()) {
			DefaultFlexiColumnModel editColumn = new DefaultFlexiColumnModel("table.header.edit", -1, "edit",
					new StaticFlexiCellRenderer("", "edit", "o_icon o_icon-lg o_icon_edit", translate("edit"), null));
			editColumn.setExportable(false);
			editColumn.setAlwaysVisible(true);
			columnsModel.addFlexiColumnModel(editColumn);
		}
		
		tableModel = new ParticipantListDataModel(columnsModel, getTranslator(), getLocale()); 
		int pageSize = printView ? 32000 : 20;
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, pageSize, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(!printView);
		tableEl.setEmptyTableMessageKey("empty.table.participant.list");
		tableEl.setSortSettings(options);
		tableEl.setAndLoadPersistedPreferences(ureq, "participant-list-repo-entry-v2");
	}
	
	private void loadModel() {
		List<Identity> participants = lectureService.getParticipants(entry);
		List<LectureBlockStatistics> statistics = lectureService.getParticipantsLecturesStatistics(entry);
		Map<Long, LectureBlockStatistics> identityToStatisticsMap = statistics.stream()
				.collect(Collectors.toMap(LectureBlockStatistics::getIdentityKey, s -> s));
		
		List<ParticipantRow> rows = new ArrayList<>(participants.size());
		for(Identity participant:participants) {
			LectureBlockStatistics stats = identityToStatisticsMap.get(participant.getKey());
			rows.add(new ParticipantRow(participant, stats, userPropertyHandlers, getLocale()));
		}
		tableModel.setObjects(rows);
		tableEl.reset(false, false, true);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editRateCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editRateCtrl);
		removeAsListenerAndDispose(cmc);
		editRateCtrl = null;
		cmc = null;
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(flc.getFormItemComponent() == source && "print".equals(event.getCommand())) {
			doPrint(ureq);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("edit".equals(cmd)) {
					ParticipantRow row = tableModel.getObject(se.getIndex());
					doEdit(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doEdit(UserRequest ureq, ParticipantRow row) {
		if(guardModalController(editRateCtrl)) return;
		
		Identity identity = securityManager.loadIdentityByKey(row.getIdentityKey());
		editRateCtrl = new EditParticipantSummaryController(ureq, getWindowControl(), entry, identity, rateEnabled, defaultRate);
		listenTo(editRateCtrl);
		
		String title = translate("edit.participant.rate");
		cmc = new CloseableModalController(getWindowControl(), "close", editRateCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doPrint(UserRequest ureq) {
		ControllerCreator printControllerCreator = (lureq, lwControl) -> {
			lwControl.getWindowBackOffice().getChiefController().addBodyCssClass("o_lectures_print");
			Controller printCtrl = new ParticipantListRepositoryController(lureq, lwControl, entry, secCallback, true);
			listenTo(printCtrl);
			return printCtrl;
		};
		ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createPrintPopupLayout(printControllerCreator);
		openInNewBrowserWindow(ureq, layoutCtrlr);
	}
}