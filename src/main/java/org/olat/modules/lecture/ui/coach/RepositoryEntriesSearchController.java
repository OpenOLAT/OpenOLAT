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

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.modules.lecture.model.LectureBlockStatistics;
import org.olat.modules.lecture.model.LecturesMemberSearchParameters;
import org.olat.modules.lecture.ui.ConfigurationHelper;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.LectureRoles;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.coach.LecturesMembersTableModel.MemberCols;
import org.olat.modules.lecture.ui.component.PercentCellRenderer;
import org.olat.modules.lecture.ui.event.SelectLectureIdentityEvent;
import org.olat.modules.lecture.ui.event.SelectLectureRepositoryEntryEvent;
import org.olat.modules.lecture.ui.profile.IdentityProfileController;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The courses search with search fields and list.
 * 
 * Initial date: 9 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntriesSearchController extends BasicController {
	
	private BreadcrumbedStackedPanel panel;
	
	private final LecturesSecurityCallback secCallback;
	
	private final RepositoryEntriesListController entriesSearchCtrl;
	private ParticipantsSearchListController participantsSearchCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private BaseSecurityManager securityManager;
	
	public RepositoryEntriesSearchController(UserRequest ureq, WindowControl wControl, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		this.secCallback = secCallback;
		
		Identity iamTeacher = null;
		if(secCallback.viewAs() == LectureRoles.teacher) {
			iamTeacher = getIdentity();
		}
		
		entriesSearchCtrl = new RepositoryEntriesListController(ureq, getWindowControl(), iamTeacher, secCallback);
		listenTo(entriesSearchCtrl);
		panel = new BreadcrumbedStackedPanel("t-search", getTranslator(), this);
		panel.pushController(translate("search.entries"), entriesSearchCtrl);
		putInitialPanel(panel);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == entriesSearchCtrl) {
			if(event instanceof SelectLectureRepositoryEntryEvent) {
				SelectLectureRepositoryEntryEvent slree = (SelectLectureRepositoryEntryEvent)event;
				doSelectRepositoryEntry(ureq, slree.getEntry());
			}
		} else if(source == participantsSearchCtrl) {
			if(event instanceof SelectLectureIdentityEvent) {
				SelectLectureIdentityEvent sie = (SelectLectureIdentityEvent)event;
				doSelectParticipant(ureq, sie.getIdentityKey());
			}
		}
	}
	
	private void doSelectRepositoryEntry(UserRequest ureq, RepositoryEntry entry) {
		participantsSearchCtrl = new ParticipantsSearchListController(ureq, getWindowControl(), entry);
		listenTo(participantsSearchCtrl);
		panel.pushController(entry.getDisplayname(), participantsSearchCtrl);
		participantsSearchCtrl.doSearch(null);
	}
	
	private void doSelectParticipant(UserRequest ureq, Long identityKey) {
		Identity profiledIdentity = securityManager.loadIdentityByKey(identityKey);
		IdentityProfileController profileCtrl = new IdentityProfileController(ureq, getWindowControl(), profiledIdentity, secCallback, false);
		listenTo(profileCtrl);
		String fullname = userManager.getUserDisplayName(profiledIdentity);
		panel.pushController(fullname, profileCtrl);
	}
	
	private class ParticipantsSearchListController extends FormBasicController {
		
		private FlexiTableElement tableEl;
		private LecturesMembersTableModel tableModel;

		private final double defaultRate;
		private final boolean rateEnabled;
		private final RepositoryEntry restrictToEntry;
		private RepositoryEntryLectureConfiguration lectureConfig;
		private final List<UserPropertyHandler> userPropertyHandlers;

		@Autowired
		private UserManager userManager;
		@Autowired
		private LectureModule lectureModule;
		@Autowired
		private BaseSecurityModule securityModule;
		
		public ParticipantsSearchListController(UserRequest ureq, WindowControl wControl, RepositoryEntry restrictToEntry) {
			super(ureq, wControl, "participants_search", Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
			setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
			
			this.restrictToEntry = restrictToEntry;
			lectureConfig = lectureService.getRepositoryEntryLectureConfiguration(restrictToEntry);
			rateEnabled = ConfigurationHelper.isRateEnabled(lectureConfig, lectureModule);
			if(lectureConfig.isOverrideModuleDefault()) {
				defaultRate = lectureConfig.getRequiredAttendanceRate() == null ?
						lectureModule.getRequiredAttendanceRateDefault() : lectureConfig.getRequiredAttendanceRate().doubleValue();	
			} else {
				defaultRate = lectureModule.getRequiredAttendanceRateDefault();
			}
			
			boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
			userPropertyHandlers = userManager.getUserPropertyHandlersFor(LecturesMembersSearchController.USER_USAGE_IDENTIFIER, isAdministrativeUser);
			
			initForm(ureq);
		}
		
		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
			
			int colIndex = LecturesMembersSearchController.USER_PROPS_OFFSET;
			for (int i = 0; i < userPropertyHandlers.size(); i++) {
				UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
				boolean visible = userManager.isMandatoryUserProperty(LecturesMembersSearchController.USER_USAGE_IDENTIFIER, userPropertyHandler);
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, "rollcall",
						true, "userProp-" + colIndex));
				colIndex++;
			}
			
			if(rateEnabled) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MemberCols.rate, new PercentCellRenderer()));
			}
			
			tableModel = new LecturesMembersTableModel(columnsModel, getLocale());	
			tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 26, false, getTranslator(), formLayout);
			tableEl.setSearchEnabled(true);
		}
	
		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if(tableEl == source) {
				if(event instanceof SelectionEvent) {
					LecturesMemberRow row = tableModel.getObject(((SelectionEvent)event).getIndex());
					fireEvent(ureq, new SelectLectureIdentityEvent(row.getIdentityKey()));
				} else if(event instanceof FlexiTableSearchEvent) {
					FlexiTableSearchEvent ftse = (FlexiTableSearchEvent)event;
					doSearch(ftse.getSearch());
				}
			}
			super.formInnerEvent(ureq, source, event);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}

		private void doSearch(String searchString) {
			LecturesMemberSearchParameters searchParams = new LecturesMemberSearchParameters();
			searchParams.setSearchString(searchString);
			searchParams.setRepositoryEntry(restrictToEntry);
			searchParams.setViewAs(getIdentity(), secCallback.viewAs());
			List<Identity> participants = lectureService.searchParticipants(searchParams);
			
			List<LectureBlockStatistics> statistics = lectureService.getParticipantsLecturesStatistics(restrictToEntry);
			Map<Long,LectureBlockStatistics> identityToStatistics = statistics.stream()
					.collect(Collectors.toMap(LectureBlockStatistics::getIdentityKey, stats -> stats, (u, v) -> u));
			
			List<LecturesMemberRow> rows = participants.stream()
					.map(id -> {
						double requiredRate = defaultRate;
						Double attendanceRate = null;
						LectureBlockStatistics idStatistics = identityToStatistics.get(id.getKey());
						if(idStatistics != null) {
							requiredRate = idStatistics.getRequiredRate();
							attendanceRate = idStatistics.getAttendanceRate();
						}
						return new LecturesMemberRow(id, userPropertyHandlers, attendanceRate, requiredRate, getLocale());
					}).collect(Collectors.toList());
		
			tableModel.setObjects(rows);
			tableEl.reset(true, true, true);
		}
	}
}
