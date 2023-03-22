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
package org.olat.course.assessment.ui.reset;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.assessment.ui.reset.ResetDataContext.ResetCourse;
import org.olat.course.assessment.ui.reset.ResetDataIdentitiesTableModel.IdentityCols;
import org.olat.course.assessment.ui.tool.AssessmentStatusCellRenderer;
import org.olat.course.assessment.ui.tool.AssessmentToolConstants;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.ParticipantType;
import org.olat.modules.assessment.ui.AssessedIdentityListState;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.assessment.ui.component.ColorizedScoreCellRenderer;
import org.olat.modules.assessment.ui.component.PassedCellRenderer;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.ui.CurriculumHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 mars 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ResetDataIdentitiesSelectionController extends StepFormBasicController {

	private FlexiTableElement tableEl;
	private ResetDataIdentitiesTableModel tableModel;
	
	private final CourseNode courseNode;
	private final ResetDataContext dataContext;
	private final AssessmentConfig assessmentConfig;
	private final UserCourseEnvironment coachCourseEnv;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final AssessmentToolSecurityCallback assessmentCallback;

	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private UserCourseInformationsManager userInfosMgr;
	@Autowired
	private AssessmentToolManager assessmentToolManager;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public ResetDataIdentitiesSelectionController(UserRequest ureq, WindowControl wControl, Form rootForm,
			StepsRunContext runContext, ResetDataContext dataContext, UserCourseEnvironment coachCourseEnv,
			AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "identities_list");

		this.dataContext = dataContext;
		this.coachCourseEnv = coachCourseEnv;
		this.assessmentCallback = assessmentCallback;
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(AssessmentToolConstants.usageIdentifyer, isAdministrativeUser);

		RepositoryEntry courseEntry =  dataContext.getRepositoryEntry();
		if(dataContext.getResetCourse() == ResetCourse.elements && dataContext.getCourseNodes().size() == 1) {
			courseNode = dataContext.getCourseNodes().get(0);
		} else {
			ICourse course = CourseFactory.loadCourse(courseEntry);
			courseNode = course.getRunStructure().getRootNode();
		}
		
		assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, courseNode);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		int colIndex = AssessmentToolConstants.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(AssessmentToolConstants.usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, null, true, "userProp-" + colIndex));
			colIndex++;
		}
		
		if(assessmentConfig.isAssessable()) {
			if(Mode.none != assessmentConfig.getScoreMode()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCols.score, new ColorizedScoreCellRenderer()));
			}
			if(Mode.none != assessmentConfig.getPassedMode()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCols.passed, new PassedCellRenderer(getLocale())));
			}
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCols.assessmentStatus, new AssessmentStatusCellRenderer(getLocale())));

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCols.initialLaunchDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCols.lastModified));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCols.lastUserModified));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, IdentityCols.lastCoachModified));
		
		tableModel = new ResetDataIdentitiesTableModel(columnsModel);
		Translator translator = userManager.getPropertyHandlerTranslator(getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, translator, formLayout);
		tableEl.setSelectAllEnable(true);
		tableEl.setMultiSelect(true);
		tableEl.setCustomizeColumns(true);
		initFilters();
		tableEl.setAndLoadPersistedPreferences(ureq, "reset-data-course-v2");
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		// members
		if (assessmentCallback.canAssessNonMembers() || assessmentCallback.canAssessFakeParticipants()) {
			SelectionValues membersValues = new SelectionValues();
			membersValues.add(SelectionValues.entry(ParticipantType.member.name(), translate("filter.members")));
			if (assessmentCallback.canAssessNonMembers()) {
				membersValues.add(SelectionValues.entry(ParticipantType.nonMember.name(), translate("filter.other.users")));
			}
			if (assessmentCallback.canAssessFakeParticipants()) {
				membersValues.add(SelectionValues.entry(ParticipantType.fakeParticipant.name(), translate("filter.fake.participants")));
			}
			if (membersValues.size() > 1) {
				FlexiTableMultiSelectionFilter membersFilter = new FlexiTableMultiSelectionFilter(translate("filter.members.label"),
						AssessedIdentityListState.FILTER_MEMBERS, membersValues, true);
				membersFilter.setValues(List.of(ParticipantType.member.name()));
				filters.add(membersFilter);
			}
		}
		
		// groups
		SelectionValues groupValues = new SelectionValues();
		if(assessmentCallback.canAssessBusinessGoupMembers()) {
			List<BusinessGroup> coachedGroups;
			if(assessmentCallback.isAdmin()) {
				coachedGroups = coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getAllBusinessGroups();
			} else {
				coachedGroups = assessmentCallback.getCoachedGroups();
			}

			if(coachedGroups != null && !coachedGroups.isEmpty()) {
				for(BusinessGroup coachedGroup:coachedGroups) {
					String groupName = StringHelper.escapeHtml(coachedGroup.getName());
					groupValues.add(new SelectionValue("businessgroup-" + coachedGroup.getKey(), groupName, null,
							"o_icon o_icon_group", null, true));
				}
			}
		}
		
		if(assessmentCallback.canAssessCurriculumMembers()) {
			List<CurriculumElement> coachedCurriculumElements;
			if(assessmentCallback.isAdmin()) {
				coachedCurriculumElements = coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getAllCurriculumElements();
			} else {
				coachedCurriculumElements = coachCourseEnv.getCoachedCurriculumElements();
			}
			
			if(!coachedCurriculumElements.isEmpty()) {
				for(CurriculumElement coachedCurriculumElement:coachedCurriculumElements) {
					String name = CurriculumHelper.getLabel(coachedCurriculumElement, getTranslator());
					groupValues.add(new SelectionValue("curriculumelement-" + coachedCurriculumElement.getKey(), name, null,
							"o_icon o_icon_curriculum_element", null, true));
				}
			}
		}
		
		if(!groupValues.isEmpty()) {
			filters.add(new FlexiTableMultiSelectionFilter(translate("filter.groups"),
					AssessedIdentityListState.FILTER_GROUPS, groupValues, true));
		}

		tableEl.setFilters(true, filters, false, true);
	}
	
	private void loadModel() {
		RepositoryEntry courseEntry =  dataContext.getRepositoryEntry();
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(courseEntry, courseNode.getIdent(), null, assessmentCallback);
		List<Identity> assessedIdentities = assessmentToolManager.getAssessedIdentities(getIdentity(), params);
		
		List<AssessmentEntry> assessmentEntries = assessmentToolManager.getAssessmentEntries(getIdentity(), params, null);
		Map<Long,AssessmentEntry> entryMap = assessmentEntries.stream()
			.filter(entry -> entry.getIdentity() != null)
			.collect(Collectors.toMap(entry -> entry.getIdentity().getKey(), entry -> entry, (u, v) -> u));
		
		Map<Long, Date> initialLaunchDates = userInfosMgr
				.getInitialLaunchDates(courseEntry.getOlatResource());
		
		List<ResetDataIdentityRow> rows = new ArrayList<>(assessedIdentities.size());
		for(Identity assessedIdentity:assessedIdentities) {
			ResetDataIdentityRow row = new ResetDataIdentityRow(assessedIdentity, userPropertyHandlers, getLocale());
			AssessmentEntry entry = entryMap.get(assessedIdentity.getKey());
			if(entry != null) {
				row.setAssessmentEntry(entry);
			}
			row.setInitialCourseLaunchDate(initialLaunchDates.get(assessedIdentity.getKey()));
			rows.add(row);
		}

		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		tableEl.clearError();
		if(tableEl.getMultiSelectedIndex().isEmpty()) {
			tableEl.setErrorKey("error.atleast.one.identity");
			allOk &= false;
		}
	
		return allOk;
	}
	
	@Override
	protected void formNext(UserRequest ureq) {
		Set<Integer> selectedIndexes = tableEl.getMultiSelectedIndex();
		List<Identity> selectedIdentities = new ArrayList<>();
		for(Integer selectedIndex:selectedIndexes) {
			ResetDataIdentityRow row = tableModel.getObject(selectedIndex.intValue());
			if(row != null && row.getIdentity() != null) {
				selectedIdentities.add(row.getIdentity());
			}
		}
		dataContext.setSelectedParticipants(selectedIdentities);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}
