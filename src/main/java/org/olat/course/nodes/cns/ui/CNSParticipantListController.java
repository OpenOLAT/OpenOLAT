/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.cns.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.ComponentWrapperElement;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DetailsToggleEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.panel.InfoPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.nodes.CNSCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.cns.ui.CNSParticipantDataModel.CNSParticipantCols;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.ParticipantType;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.assessment.ui.AssessedIdentityListController;
import org.olat.modules.assessment.ui.AssessedIdentityListState;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.ui.CurriculumHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserAvatarMapper;
import org.olat.user.UserInfoProfileConfig;
import org.olat.user.UserInfoService;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 Sep 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CNSParticipantListController extends FormBasicController implements FlexiTableComponentDelegate {
	
	private static final String CMD_DETAILS = "details";
	
	private final List<UserPropertyHandler> userPropertyHandlers;
	private InfoPanel configPanel;
	private CNSParticipantDataModel dataModel;
	private FlexiTableElement tableEl;
	private VelocityContainer detailsVC;
	
	private final UserCourseEnvironment coachCourseEnv;
	private final AssessmentToolSecurityCallback assessmentCallback;
	private final RepositoryEntry courseEntry;
	private final CNSCourseNode courseNode;
	private final List<CourseNode> childNodes;
	private final List<String> childNodeIdents;
	private final int requiredSelections;
	private final UserInfoProfileConfig profileConfig;
	private Set<Long> detailsOpenIdentityKeys;
	private List<CNSParticipantDetailsController> detailCtrls = new ArrayList<>(1);
	private int counter = 0;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private UserInfoService userInfoService;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private AssessmentToolManager assessmentToolManager;
	@Autowired
	private CourseAssessmentService courseAssessmentService;

	public CNSParticipantListController(UserRequest ureq, WindowControl wControl, CNSCourseNode courseNode, UserCourseEnvironment coachCourseEnv) {
		super(ureq, wControl, "participant_list");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		setTranslator(Util.createPackageTranslator(AssessedIdentityListController.class, getLocale(), getTranslator()));
		this.coachCourseEnv = coachCourseEnv;
		assessmentCallback = courseAssessmentService.createCourseNodeRunSecurityCallback(ureq, coachCourseEnv);
		courseEntry = coachCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		this.courseNode = courseNode;
		childNodes = CNSUIFactory.getChildNodes(courseNode);
		childNodeIdents = childNodes.stream().map(CourseNode::getIdent).toList();
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(CNSParticipantDataModel.USAGE_IDENTIFIER,
				isAdministrativeUser);
		
		requiredSelections = Integer.valueOf(courseNode.getModuleConfiguration().getStringValue(CNSCourseNode.CONFIG_KEY_REQUIRED_SELECTIONS));
		
		profileConfig = userInfoService.createProfileConfig();
		UserAvatarMapper avatarMapper = new UserAvatarMapper(true);
		profileConfig.setAvatarMapper(avatarMapper);
		String avatarMapperBaseURL = registerCacheableMapper(ureq, "users-avatars", avatarMapper);
		profileConfig.setAvatarMapperBaseURL(avatarMapperBaseURL);
		
		initForm(ureq);
		initFilters();
		loadModel(ureq);
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		// life-cycle
		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry("notReady", translate("filter.notReady")));
		statusValues.add(SelectionValues.entry("notStarted", translate("filter.notStarted")));
		statusValues.add(SelectionValues.entry("inProgress", translate("filter.inProgress")));
		statusValues.add(SelectionValues.entry("inReview", translate("filter.inReview")));
		statusValues.add(SelectionValues.entry("done", translate("filter.done")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.status"),
				AssessedIdentityListState.FILTER_STATUS, statusValues, true));
		
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
				FlexiTableMultiSelectionFilter filter = new FlexiTableMultiSelectionFilter(translate("filter.members.label"),
						AssessedIdentityListState.FILTER_MEMBERS, membersValues, true);
				filter.setValues(List.of(ParticipantType.member.name()));
				filters.add(filter);
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
		
		tableEl.setFilters(true, filters, false, false);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		configPanel = new InfoPanel("configs");
		configPanel.setTitle(translate("configuration"));
		configPanel.setInformations(CNSUIFactory.getConfigMessage(getTranslator(), requiredSelections));
		configPanel.setPersistedStatusId(ureq, "cns-selection-config-" + courseEntry.getKey() + "::" + courseNode.getIdent());
		formLayout.add("config", new ComponentWrapperElement(configPanel));
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		FlexiTableSortOptions sortOptions = null;
		int colIndex = CNSParticipantDataModel.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(i);
			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(CNSParticipantDataModel.USAGE_IDENTIFIER, userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible,
					userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex++, CMD_DETAILS,true, propName));
			if (UserConstants.LASTNAME.equals(userPropertyHandler.getName())) {
				SortKey sortKey = new SortKey(propName, true);
				sortOptions = new FlexiTableSortOptions();
				sortOptions.setDefaultOrderBy(sortKey);
			}
		}
		
		DefaultFlexiColumnModel selectedColumn = new DefaultFlexiColumnModel(CNSParticipantCols.selected, new TextFlexiCellRenderer(EscapeMode.none));
		selectedColumn.setAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		selectedColumn.setHeaderAlignment(FlexiColumnModel.ALIGNMENT_RIGHT);
		columnsModel.addFlexiColumnModel(selectedColumn);
		
		dataModel = new CNSParticipantDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", dataModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "cns-participants-" + courseEntry.getKey() + "::" + courseNode.getIdent());
		tableEl.setSortSettings(sortOptions);
		tableEl.setSearchEnabled(true);
		
		String page = velocity_root + "/details.html";
		detailsVC = new VelocityContainer("details_" + counter++, "vc_details", page, getTranslator(), this);
		tableEl.setDetailsRenderer(detailsVC, this);
		tableEl.setMultiDetails(true);
	}

	public void reload(UserRequest ureq) {
		loadModel(ureq);
	}
	
	private void loadModel(UserRequest ureq) {
		SearchAssessedIdentityParams params = getSearchParameters();
		List<Identity> identities = assessmentToolManager.getAssessedIdentities(getIdentity(), params);
		
		Map<Long, List<AssessmentEntry>> identityKeyToEntries = assessmentToolManager.getAssessmentEntries(getIdentity(), params, null).stream()
			.filter(entry -> entry.getIdentity() != null)
			.collect(Collectors.groupingBy(entry -> entry.getIdentity().getKey()));
		
		List<CNSParticipantRow> rows = new ArrayList<>(identities.size());
		for (Identity identity : identities) {
			CNSParticipantRow row = new CNSParticipantRow(identity, userPropertyHandlers, getLocale());
			
			List<AssessmentEntry> entries = identityKeyToEntries.getOrDefault(identity.getKey(), List.of());
			row.setSelectedEntries(entries);
			int numSelections = entries.size();
			row.setNumSelections(numSelections);
			row .setSelected(CNSUIFactory.getSelected(getTranslator(), requiredSelections, numSelections));
			
			rows.add(row);
		}
		
		applySearch(rows);
		dataModel.setObjects(rows);
		tableEl.reset(true, true, true);
		
		tableEl.collapseAllDetails();
		detailCtrls.forEach(ctrl -> {
			flc.remove(ctrl.getInitialFormItem());
			removeAsListenerAndDispose(ctrl);
		});
		detailCtrls.clear();
		if (detailsOpenIdentityKeys != null && !detailsOpenIdentityKeys.isEmpty()) {
			dataModel.getObjects().stream()
				.filter(row -> detailsOpenIdentityKeys.contains(row.getIdentityKey()))
				.forEach(row -> {
					int index = dataModel.getObjects().indexOf(row);
					doShowDetails(ureq, row);
					tableEl.expandDetails(index);
				});
		}
	}
	
	private SearchAssessedIdentityParams getSearchParameters() {
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(courseEntry, null, null, assessmentCallback);
		params.setSubIdents(childNodeIdents);
		params.setAssessmentObligations(AssessmentObligation.NOT_EXCLUDED);
		params.setUserPropertyHandlers(userPropertyHandlers);

		List<FlexiTableFilter> filters = tableEl.getFilters();
		
		FlexiTableFilter statusFilter = FlexiTableFilter.getFilter(filters, AssessedIdentityListState.FILTER_STATUS);
		if (statusFilter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter)statusFilter).getValues();
			if (filterValues != null && !filterValues.isEmpty()) {
				List<AssessmentEntryStatus> passed = filterValues.stream()
						.filter(AssessmentEntryStatus::isValueOf)
						.map(AssessmentEntryStatus::valueOf)
						.collect(Collectors.toList());
				params.setAssessmentStatus(passed);
			}
		}
		
		FlexiTableFilter membersFilter = FlexiTableFilter.getFilter(filters, AssessedIdentityListState.FILTER_MEMBERS);
		if(membersFilter != null) {
			List<String> filterValues = ((FlexiTableExtendedFilter)membersFilter).getValues();
			if (filterValues != null && !filterValues.isEmpty()) {
				Set<ParticipantType> participants = filterValues.stream()
						.map(ParticipantType::valueOf)
						.collect(Collectors.toSet());
				params.setParticipantTypes(participants);
			}
		}
		
		List<Long> businessGroupKeys = null;
		List<Long> curriculumElementKeys = null;
		FlexiTableFilter groupsFilter = FlexiTableFilter.getFilter(filters, AssessedIdentityListState.FILTER_GROUPS);
		if(groupsFilter != null && groupsFilter.isSelected()) {
			businessGroupKeys = new ArrayList<>();
			curriculumElementKeys = new ArrayList<>();
			List<String> filterValues = ((FlexiTableExtendedFilter)groupsFilter).getValues();
			if(filterValues != null) {
				for(String filterValue:filterValues) {
					int index = filterValue.indexOf('-');
					if(index > 0) {
						Long key = Long.valueOf(filterValue.substring(index + 1));
						if(filterValue.startsWith("businessgroup-")) {
							businessGroupKeys.add(key);
						} else if(filterValue.startsWith("curriculumelement-")) {
							curriculumElementKeys.add(key);
						}
					}
				}
			}
		}
		params.setBusinessGroupKeys(businessGroupKeys);
		params.setCurriculumElementKeys(curriculumElementKeys);
		
		return params;
	}
	
	private void applySearch(List<CNSParticipantRow> rows) {
		String searchValue = tableEl.getQuickSearchString();
		if (StringHelper.containsNonWhitespace(searchValue)) {
			List<String> searchValues = Arrays.stream(searchValue.toLowerCase().split(" ")).filter(StringHelper::containsNonWhitespace).toList();
			rows.removeIf(row -> 
					containsNot(searchValues, row.getIdentityProp(UserConstants.LASTNAME, userPropertyHandlers))
					&& containsNot(searchValues, row.getIdentityProp(UserConstants.FIRSTNAME, userPropertyHandlers))
				);
		}
	}
	
	private boolean containsNot(List<String> searchValues, String candidate) {
		if (StringHelper.containsNonWhitespace(candidate)) {
			String candidateLowerCase = candidate.toLowerCase();
			return searchValues.stream().noneMatch(searchValue -> candidateLowerCase.indexOf(searchValue) >= 0);
		}
		return true;
	}
	
	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		return true;
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		return null;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source instanceof CNSParticipantDetailsController) {
			if (event == Event.CHANGED_EVENT) {
				loadModel(ureq);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == tableEl) {
			if (event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				CNSParticipantRow row = dataModel.getObject(se.getIndex());
				if (CMD_DETAILS.equals(cmd)) {
					if (detailsOpenIdentityKeys == null) {
						detailsOpenIdentityKeys = new HashSet<>(1);
					}
					if (detailsOpenIdentityKeys.contains(row.getIdentityKey())) {
						detailsOpenIdentityKeys.remove(row.getIdentityKey());
					} else {
						detailsOpenIdentityKeys.add(row.getIdentityKey());
					}
					loadModel(ureq);
				}
			} else if (event instanceof DetailsToggleEvent) {
				DetailsToggleEvent dte = (DetailsToggleEvent)event;
				CNSParticipantRow row = dataModel.getObject(dte.getRowIndex());
				if (dte.isVisible()) {
					doShowDetails(ureq, row);
					setDetailsOpenIdentities();
				} else if (detailsOpenIdentityKeys.contains(row.getIdentityKey())) {
					detailsOpenIdentityKeys.remove(row.getIdentityKey());
					loadModel(ureq);
				}
			} else if (event instanceof FlexiTableFilterTabEvent) {
				detailsOpenIdentityKeys = null;
				loadModel(ureq);
			} else if (event instanceof FlexiTableSearchEvent ftse) {
				loadModel(ureq);
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doShowDetails(UserRequest ureq, CNSParticipantRow row) {
		CNSParticipantDetailsController detailsCtrl = new CNSParticipantDetailsController(ureq, getWindowControl(),
				mainForm, row.getIdentity(), profileConfig, userInfoService.createProfile(row.getIdentity()),
				coachCourseEnv.getCourseEnvironment(), courseEntry, childNodes, row.getSelectedEntries());
		listenTo(detailsCtrl);
		detailCtrls.add(detailsCtrl);
		// Add as form item to catch the events...
		flc.add("detailsform_" + counter++, detailsCtrl.getInitialFormItem());
		
		// ... and add the component to the details container.
		String detailsComponentName = "details_" + counter++;
		row.setDetailsComponentName(detailsComponentName);
		detailsVC.put(detailsComponentName, detailsCtrl.getInitialComponent());
	}
	
	private void setDetailsOpenIdentities() {
		detailsOpenIdentityKeys = tableEl.getDetailsIndex().stream()
				.map(i -> dataModel.getObject(i))
				.filter(Objects::nonNull)
				.map(CNSParticipantRow::getIdentityKey)
				.collect(Collectors.toSet());
	}

}
