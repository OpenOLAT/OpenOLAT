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
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.model.ParticipantLectureStatistics;
import org.olat.modules.lecture.ui.ParticipantListDataModel.ParticipantsCols;
import org.olat.modules.lecture.ui.component.LectureStatisticsCellRenderer;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
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
	
	protected static final String USER_PROPS_ID = ParticipantListRepositoryController.class.getCanonicalName();

	public static final int USER_PROPS_OFFSET = 500;
	
	private final boolean isAdministrativeUser;
	private List<UserPropertyHandler> userPropertyHandlers;
	
	private FlexiTableElement tableEl;
	private ParticipantListDataModel tableModel;

	private CloseableModalController cmc;
	private EditParticipantRateController editRateCtrl;
	
	private final double defaultRate;
	private final boolean rateEnabled;
	private final boolean rollCallEnabled;
	
	private final RepositoryEntry entry;
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
	private RepositoryService repositoryService;
	@Autowired
	private BaseSecurity securityManager;
	
	public ParticipantListRepositoryController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl, "participant_list_overview");
		this.entry = entry;
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		Roles roles = ureq.getUserSession().getRoles();
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		
		lectureConfig = lectureService.getRepositoryEntryLectureConfiguration(entry);
		if(lectureConfig.isOverrideModuleDefault()) {
			rateEnabled = lectureConfig.getCalculateAttendanceRate() == null ?
					lectureModule.isRollCallCalculateAttendanceRateDefaultEnabled() : lectureConfig.getCalculateAttendanceRate().booleanValue();
			defaultRate = lectureConfig.getRequiredAttendanceRate() == null ?
					lectureModule.getRequiredAttendanceRateDefault() : lectureConfig.getRequiredAttendanceRate().doubleValue();
			rollCallEnabled	= lectureConfig.getRollCallEnabled() == null ?
					lectureModule.isRollCallDefaultEnabled() : lectureConfig.getRollCallEnabled();		
		} else {
			rateEnabled = lectureModule.isRollCallCalculateAttendanceRateDefaultEnabled();
			defaultRate = lectureModule.getRequiredAttendanceRateDefault();
			rollCallEnabled = lectureModule.isRollCallDefaultEnabled();
		}

		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(isAdministrativeUser) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantsCols.username));
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

		if(rollCallEnabled) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ParticipantsCols.progress, new LectureStatisticsCellRenderer()));
		}
		if(rateEnabled) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit"));
		}
		
		tableModel = new ParticipantListDataModel(columnsModel, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
	}
	
	private void loadModel() {
		List<Identity> participants = repositoryService.getMembers(entry, GroupRoles.participant.name());
		List<ParticipantLectureStatistics> statistics = lectureService.getParticipantsLecturesStatistics(entry);
		Map<Long, ParticipantLectureStatistics> identityToStatisticsMap = statistics.stream().collect(Collectors.toMap(s -> s.getIdentityKey(), s -> s));
		
		List<ParticipantRow> rows = new ArrayList<>(participants.size());
		for(Identity participant:participants) {
			ParticipantLectureStatistics stats = identityToStatisticsMap.get(participant.getKey());
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
		if(editRateCtrl != null) return;
		
		Identity identity = securityManager.loadIdentityByKey(row.getIdentityKey());
		editRateCtrl = new EditParticipantRateController(ureq, getWindowControl(), entry, identity, defaultRate);
		listenTo(editRateCtrl);
		
		String title = translate("edit.participant.rate");
		cmc = new CloseableModalController(getWindowControl(), "close", editRateCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
}