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
package org.olat.course.learningpath.ui;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeController;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.learningpath.ui.LearningPathIdentityDataModel.LearningPathIdentityCols;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.ParticipantType;
import org.olat.modules.assessment.ui.AssessedIdentityListState;
import org.olat.modules.assessment.ui.ScoreCellRenderer;
import org.olat.modules.assessment.ui.component.LearningProgressCompletionCellRenderer;
import org.olat.modules.assessment.ui.component.PassedCellRenderer;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRelationType;
import org.olat.repository.RepositoryService;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 Dec 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathIdentityListController extends FormBasicController implements Activateable2 {
	
	private static final String USAGE_IDENTIFIER = LearningPathIdentityListController.class.getCanonicalName();
	private static final String ORES_TYPE_IDENTITY = "Identity";
	private static final String CMD_SELECT = "select";
	
	private FlexiTableElement tableEl;
	private LearningPathIdentityDataModel dataModel;

	private LearningPathIdentityController currentIdentityCtrl;
	
	private final TooledStackedPanel stackPanel;
	private final UserCourseEnvironment coachCourseEnv;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final boolean isAdministrativeUser;
	private final List<Identity> coachedIdentities;
	private final Set<Long> fakeParticipantKeys;

	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private AssessmentToolManager assessmentToolManager;

	public LearningPathIdentityListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			UserCourseEnvironment coachCourseEnv) {
		super(ureq, wControl, "identities");
		this.stackPanel = stackPanel;
		this.coachCourseEnv = coachCourseEnv;
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		setTranslator(Util.createPackageTranslator(IdentityListCourseNodeController.class, getLocale(), getTranslator()));
		
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USAGE_IDENTIFIER, isAdministrativeUser);
		
		Set<Identity> identities = new HashSet<>(getCoachedIdentities());
		Set<IdentityRef> fakeParticipants = assessmentToolManager.getFakeParticipants(
				coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), getIdentity(),
				coachCourseEnv.isAdmin(), coachCourseEnv.isCoach());
		identities.addAll(securityManager.loadIdentityByRefs(fakeParticipants));
		
		coachedIdentities = new ArrayList<>(identities);
		fakeParticipantKeys = fakeParticipants.stream().map(IdentityRef::getKey).collect(Collectors.toSet());
		
		initForm(ureq);
	}
	
	public List<Identity> getCoachedIdentities() {
		CourseGroupManager cgm = coachCourseEnv.getCourseEnvironment().getCourseGroupManager();
		RepositoryEntry re = cgm.getCourseEntry();
		
		return coachCourseEnv.isAdmin()
				? repositoryService.getMembers(re, RepositoryEntryRelationType.all, GroupRoles.participant.name())
						.stream().distinct().collect(Collectors.toList())
				: repositoryService.getCoachedParticipants(getIdentity(), re);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String courseTitle = coachCourseEnv.getCourseEnvironment().getCourseTitle();
		flc.contextPut("courseTitle", courseTitle);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		int colIndex = LearningPathIdentityDataModel.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(USAGE_IDENTIFIER, userPropertyHandler);
			columnsModel.addFlexiColumnModel(
					new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex,
							CMD_SELECT, true, "userProp-" + colIndex));
			colIndex++;
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LearningPathIdentityCols.progress,
				new LearningProgressCompletionCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LearningPathIdentityCols.passed, new PassedCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LearningPathIdentityCols.score, new ScoreCellRenderer()));
		
		dataModel = new LearningPathIdentityDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setEmptyTableSettings("table.empty.curriculum", null, "o_icon_user");
		tableEl.setExportEnabled(true);
		initFilters();
		
		loadModel();
	}
	
	private void initFilters() {
		if (!fakeParticipantKeys.isEmpty()) {
			SelectionValues membersValues = new SelectionValues();
			membersValues.add(SelectionValues.entry(ParticipantType.member.name(), translate("filter.members")));
			membersValues.add(SelectionValues.entry(ParticipantType.fakeParticipant.name(), translate("filter.fake.participants")));
			FlexiTableMultiSelectionFilter membersFilter = new FlexiTableMultiSelectionFilter(translate("filter.members.label"),
					AssessedIdentityListState.FILTER_MEMBERS, membersValues, true);
			membersFilter.setValues(List.of(ParticipantType.member.name()));
			tableEl.setFilters(true, List.of(membersFilter), false, true);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries == null || entries.isEmpty()) return;
		
		String resourceType = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(ORES_TYPE_IDENTITY.equalsIgnoreCase(resourceType)) {
			Long identityKey = entries.get(0).getOLATResourceable().getResourceableId();
			for(int i=dataModel.getRowCount(); i--> 0; ) {
				LearningPathIdentityRow row = dataModel.getObject(i);
				if(row.getIdentityKey().equals(identityKey)) {
					doSelect(ureq, row);
				}
			}
		}	
		
	}

	private void loadModel() {
		Set<ParticipantType> filterParticipants = getFilterParticipants();
		CourseGroupManager cgm = coachCourseEnv.getCourseEnvironment().getCourseGroupManager();
		RepositoryEntry re = cgm.getCourseEntry();
		String subIdent = coachCourseEnv.getCourseEnvironment().getRunStructure().getRootNode().getIdent();
		
		List<AssessmentEntry> assessmentEntries = assessmentService.loadAssessmentEntriesBySubIdent(re, subIdent);
		Map<Long, AssessmentEntry> identityKeyToCompletion = new HashMap<>();
		for (AssessmentEntry assessmentEntry : assessmentEntries) {
			identityKeyToCompletion.put(assessmentEntry.getIdentity().getKey(), assessmentEntry);
		}
		
		List<LearningPathIdentityRow> rows = new ArrayList<>(coachedIdentities.size());
		for (Identity coachedIdentity : coachedIdentities) {
			if (isExcludedByParticipant(filterParticipants, coachedIdentity)) continue;
			
			AssessmentEntry assessmentEntry = identityKeyToCompletion.get(coachedIdentity.getKey());
			Double completion = assessmentEntry != null ? assessmentEntry.getCompletion(): null;
			Boolean passed = assessmentEntry != null ? assessmentEntry.getPassed(): null;
			BigDecimal score = assessmentEntry != null && assessmentEntry.getScore() != null? assessmentEntry.getScore(): null;
			LearningPathIdentityRow row = new LearningPathIdentityRow(coachedIdentity, userPropertyHandlers,
					getLocale(), completion, passed, score);
			rows.add(row);
		}
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private Set<ParticipantType> getFilterParticipants() {
		List<FlexiTableFilter> filters = tableEl.getFilters();
		FlexiTableFilter membersFilter = FlexiTableFilter.getFilter(filters, AssessedIdentityListState.FILTER_MEMBERS);
		if(membersFilter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter)membersFilter).getValues();
			if (filterValues != null && !filterValues.isEmpty()) {
				return filterValues.stream()
						.map(ParticipantType::valueOf)
						.collect(Collectors.toSet());
			}
		}
		return null;
	}
	
	private boolean isExcludedByParticipant(Set<ParticipantType> filterParticipants, Identity identity) {
		if (filterParticipants != null && filterParticipants.size() == 1) {
			if (filterParticipants.contains(ParticipantType.fakeParticipant)) {
				return !fakeParticipantKeys.contains(identity.getKey());
			}
			return fakeParticipantKeys.contains(identity.getKey());
		}
		return false;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (tableEl == source) {
			if (event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				LearningPathIdentityRow row = dataModel.getObject(se.getIndex());
				if (CMD_SELECT.equals(cmd)) {
					doSelect(ureq, row);
				}
			} else if(event instanceof FlexiTableSearchEvent) {
				loadModel();
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doSelect(UserRequest ureq, LearningPathIdentityRow row) {
		removeAsListenerAndDispose(currentIdentityCtrl);
		
		Identity coachedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		String fullName = userManager.getUserDisplayName(coachedIdentity);
		
		OLATResourceable identityOres = OresHelper.createOLATResourceableInstance(ORES_TYPE_IDENTITY, coachedIdentity.getKey());
		WindowControl bwControl = addToHistory(ureq, identityOres, null);
		
		currentIdentityCtrl = new LearningPathIdentityController(ureq, bwControl, stackPanel, coachCourseEnv.getCourseEnvironment(), coachedIdentity);
		listenTo(currentIdentityCtrl);
		stackPanel.pushController(fullName, currentIdentityCtrl);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
