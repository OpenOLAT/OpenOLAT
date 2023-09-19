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
package org.olat.course.nodes.pf.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.assessment.ui.tool.AssessmentCourseNodeController;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeController;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.PFCourseNode;
import org.olat.course.nodes.pf.manager.FileSystemExport;
import org.olat.course.nodes.pf.manager.PFManager;
import org.olat.course.nodes.pf.manager.PFView;
import org.olat.course.nodes.pf.ui.DropBoxTableModel.DropBoxCols;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.ParticipantType;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.assessment.ui.AssessedIdentityListState;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.ui.CurriculumHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.user.HomePageConfig;
import org.olat.user.HomePageDisplayController;
import org.olat.user.UserManager;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;
/**
*
* @author Fabian Kiefer, fabian.kiefer@frentix.com, http://www.frentix.com
*
*/
public class PFCoachController extends FormBasicController implements AssessmentCourseNodeController {
	
	protected static final String USER_PROPS_ID = PFCoachController.class.getCanonicalName();

	protected static final int USER_PROPS_OFFSET = 500;
	private static final String CURRICULUM_EL_PREFIX = "curriculumelement-";
	private static final String BUSINESS_GROUP_PREFIX = "businessgroup-";
	
	private PFCourseNode pfNode;
	
	private FormLink uploadLink;
	private FormLink downloadLink;
	private FormLink uploadAllLink;
	private FormLink loadTemplateStructureLink;
	private TimerComponent timerCmp;
	private DropBoxTableModel tableModel;
	private FlexiTableElement dropboxTable;
	
	private CloseableModalController cmc;
	private PFFileUploadController pfFileUploadCtr;
	private PFParticipantController pfParticipantController; 
	private HomePageDisplayController homePageDisplayController;

	private final List<UserPropertyHandler> userPropertyHandlers;

	private final CourseEnvironment courseEnv;
	private final UserCourseEnvironment coachCourseEnv;
	private final AssessmentToolSecurityCallback assessmentCallback;

	@Autowired
	private PFManager pfManager; 
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public PFCoachController(UserRequest ureq, WindowControl wControl, PFCourseNode sfNode, 
			UserCourseEnvironment coachCourseEnv) {
		super(ureq, wControl, "coach");
		setTranslator(Util.createPackageTranslator(IdentityListCourseNodeController.class, getLocale(), getTranslator()));
		
		assessmentCallback = courseAssessmentService.createCourseNodeRunSecurityCallback(ureq, coachCourseEnv);
		
		this.coachCourseEnv = coachCourseEnv;
		this.courseEnv = coachCourseEnv.getCourseEnvironment();
		this.pfNode = sfNode;
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		initForm(ureq);
		reload(ureq);
	}
	
	@Override
	public AssessmentToolSecurityCallback getAssessmentCallback() {
		return assessmentCallback;
	}
	
	@Override
	public AssessedIdentityListState getListState() {
		FlexiFiltersTab selectedTab = dropboxTable.getSelectedFilterTab();
		List<FlexiTableFilter> filters = dropboxTable.getFilters();
		return AssessedIdentityListState.valueOf(selectedTab, filters, dropboxTable.isFiltersExpanded());
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(state instanceof AssessedIdentityListState listState) {
			List<FlexiTableExtendedFilter> filters = dropboxTable.getExtendedFilters();
			listState.setValuesToFilter(filters);
			dropboxTable.setFilters(true, filters, false, false);
			dropboxTable.expandFilters(listState.isFiltersExpanded());
		}

		reload(ureq);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(timerCmp == source) {
			timerCmp = PFUIHelper.initTimeframeMessage(ureq, pfNode, flc.getFormItemComponent(), this, getTranslator());
		}
		super.event(ureq, source, event);		
	}
	
	@Override 
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == pfFileUploadCtr) {
			if (event == Event.DONE_EVENT) {
				if (pfFileUploadCtr.isUploadToAll()) {
					uploadToSelection(
							pfFileUploadCtr.getUpLoadFile(),
							pfFileUploadCtr.getUploadFileName(),
							pfFileUploadCtr.getFileDestinationEl().getSelectedKey());
					showInfo("upload.success");
					fireEvent(ureq, Event.CHANGED_EVENT);
				} else {
					pfManager.uploadFileToDropBox(pfFileUploadCtr.getUpLoadFile(),
							pfFileUploadCtr.getUploadFileName(), 4, courseEnv, pfNode, getIdentity());
				}
			}
			cmc.deactivate();
			cleanUpCMC();
		} else if (source == cmc) {
			cleanUpCMC();
		} else if(source == pfParticipantController) {
			if(event == Event.BACK_EVENT) {
				initialPanel.popContent();
				doBack();
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == uploadLink) {
			doOpenUploadController(ureq, false);
		} else if (source == uploadAllLink) {
			if (!dropboxTable.getMultiSelectedIndex().isEmpty()) {				
				doOpenUploadController(ureq, true);
			} else {
				showWarning("table.no.selection");
			}
		} else if (source == downloadLink) {
			if (!dropboxTable.getMultiSelectedIndex().isEmpty()) {
				downloadFromSelection(ureq);				
			} else {
				showWarning("table.no.selection");
			}
		} else if(source == dropboxTable) {
			if(event instanceof SelectionEvent se) {
				DropBoxRow currentObject = tableModel.getObject(se.getIndex());
				if (PFCourseNode.FOLDER_DROP_BOX.equals(se.getCommand())){
					doSelectParticipantFolder(ureq, currentObject.getIdentity(), PFView.displayDrop);
				} else if (PFCourseNode.FOLDER_RETURN_BOX.equals(se.getCommand())){
					doSelectParticipantFolder(ureq, currentObject.getIdentity(), PFView.displayReturn);
				} else if ("open.box".equals(se.getCommand())){
					doSelectParticipantFolder(ureq, currentObject.getIdentity(), null);
				} else if ("firstName".equals(se.getCommand()) || "lastName".equals(se.getCommand())) {
					doOpenHomePage(ureq, currentObject.getIdentity());
				} 
			} else if(event instanceof FlexiTableSearchEvent) {
				loadModel(true);
			}
		} else if (source == loadTemplateStructureLink) {
			if (!dropboxTable.getMultiSelectedIndex().isEmpty()) {
				createFoldersForSelection();
				showInfo("creating.folders.successful");
			}
		}
	}
	
	private void doSelectParticipantFolder (UserRequest ureq, UserPropertiesRow row, PFView view) {
		Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		removeAsListenerAndDispose(pfParticipantController);
		pfParticipantController = new PFParticipantController(ureq, getWindowControl(), pfNode,
				coachCourseEnv, assessedIdentity, view, true, false, true);
		listenTo(pfParticipantController);
		initialPanel.pushContent(pfParticipantController.getInitialComponent());
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			OLATResource course = courseEnv.getCourseGroupManager().getCourseResource();
			String businessPath = getWindowControl().getBusinessControl().getAsString();
			SubscriptionContext subsContext = new SubscriptionContext(course, pfNode.getIdent());
			PublisherData publisherData = new PublisherData(OresHelper.calculateTypeName(PFCourseNode.class),
					String.valueOf(course.getResourceableId()), businessPath);
			ContextualSubscriptionController contextualSubscriptionCtr = new ContextualSubscriptionController(ureq, getWindowControl(), subsContext,
					publisherData);
			listenTo(contextualSubscriptionCtr);
			layoutCont.put("contextualSubscription", contextualSubscriptionCtr.getInitialComponent());
			
			timerCmp = PFUIHelper.initTimeframeMessage(ureq, pfNode, layoutCont.getFormItemComponent(), this, getTranslator());
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		int i = 0;
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			int colIndex = USER_PROPS_OFFSET + i++;
			if (userPropertyHandler == null) continue;
			
			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);

			FlexiColumnModel col;
			if(UserConstants.FIRSTNAME.equals(propName)
					|| UserConstants.LASTNAME.equals(propName)) {
				col = new DefaultFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey(),
						colIndex, userPropertyHandler.getName(), true, propName,
						new StaticFlexiCellRenderer(userPropertyHandler.getName(), new TextFlexiCellRenderer()));
			} else {
				col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, true, propName);
			}
			columnsModel.addFlexiColumnModel(col);
			
			if(!options.hasDefaultOrderBy() || UserConstants.LASTNAME.equals(propName)) {
				options.setDefaultOrderBy(new SortKey(propName, true));
			}
		}

		if (pfNode.hasParticipantBoxConfigured()){
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DropBoxCols.numberFiles,PFCourseNode.FOLDER_DROP_BOX));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DropBoxCols.lastUpdate));
		}
		if (pfNode.hasCoachBoxConfigured()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DropBoxCols.numberFilesReturn,PFCourseNode.FOLDER_RETURN_BOX));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DropBoxCols.lastUpdateReturn));
		}
		StaticFlexiCellRenderer openCellRenderer = new StaticFlexiCellRenderer(translate("open.box"), "open.box");
		openCellRenderer.setIconRightCSS("o_icon_start o_icon-fw");
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DropBoxCols.openbox,
				"open.box", openCellRenderer));	
		tableModel = new DropBoxTableModel(columnsModel);
		
		dropboxTable = uifactory.addTableElement(getWindowControl(), "table", tableModel, getTranslator(), formLayout);
		dropboxTable.setMultiSelect(true);
		dropboxTable.setSelectAllEnable(true);
		dropboxTable.setExportEnabled(true);
		dropboxTable.setSortSettings(options);
		initFilters();
		dropboxTable.setAndLoadPersistedPreferences(ureq, "participant-folder_coach-v3");
		dropboxTable.setEmptyTableMessageKey("table.empty");
		
		downloadLink = uifactory.addFormLink("download.link", formLayout, Link.BUTTON);
		dropboxTable.addBatchButton(downloadLink);
		uploadAllLink = uifactory.addFormLink("upload.link", formLayout, Link.BUTTON);
		dropboxTable.addBatchButton(uploadAllLink);

		if (pfNode.getModuleConfiguration().get(PFCourseNode.CONFIG_KEY_TEMPLATE) != null
				&& !pfNode.getModuleConfiguration().get(PFCourseNode.CONFIG_KEY_TEMPLATE).equals("")) {
			loadTemplateStructureLink = uifactory.addFormLink("load.template.structure", formLayout, Link.BUTTON);
			dropboxTable.addBatchButton(loadTemplateStructureLink);
		}
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>(2);
		if (LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(courseEnv).getType())) {
			SelectionValues obligationValues = new SelectionValues();
			obligationValues.add(SelectionValues.entry(AssessmentObligation.mandatory.name(), translate("filter.mandatory")));
			obligationValues.add(SelectionValues.entry(AssessmentObligation.optional.name(), translate("filter.optional")));
			obligationValues.add(SelectionValues.entry(AssessmentObligation.excluded.name(), translate("filter.excluded")));
			FlexiTableMultiSelectionFilter obligationFilter = new FlexiTableMultiSelectionFilter(translate("filter.obligation"),
					AssessedIdentityListState.FILTER_OBLIGATION, obligationValues, true);
			obligationFilter.setValues(List.of(AssessmentObligation.mandatory.name(), AssessmentObligation.optional.name()));
			filters.add(obligationFilter);
		}

		SelectionValues groupValues = new SelectionValues();
		if(assessmentCallback.canAssessBusinessGoupMembers()) {
			List<BusinessGroup> coachedGroups = assessmentCallback.isAdmin()
					? courseEnv.getCourseGroupManager().getAllBusinessGroups() : coachCourseEnv.getCoachedGroups();
			if(coachedGroups != null) {
				for(BusinessGroup coachedGroup:coachedGroups) {
					groupValues.add(new SelectionValue(BUSINESS_GROUP_PREFIX + coachedGroup.getKey(), StringHelper.escapeHtml(coachedGroup.getName()),
							null, "o_icon o_icon_group", null, true));
				}
			}
		}

		if(assessmentCallback.canAssessCurriculumMembers()) {
			List<CurriculumElement> coachedElements = assessmentCallback.isAdmin()
					? courseEnv.getCourseGroupManager().getAllCurriculumElements() : coachCourseEnv.getCoachedCurriculumElements();
			if(!coachedElements.isEmpty()) {
				for(CurriculumElement coachedElement: coachedElements) {
					String displayName = CurriculumHelper.getLabel(coachedElement, getTranslator());
					groupValues.add(new SelectionValue(CURRICULUM_EL_PREFIX + coachedElement.getKey(), StringHelper.escapeHtml(displayName),
							null, "o_icon o_icon_curriculum_element", null, true));
				}
			}
		}
		
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
		
		if(!groupValues.isEmpty()) {
			FlexiTableExtendedFilter filter = new FlexiTableMultiSelectionFilter(translate("filter.groups"),
					AssessedIdentityListState.FILTER_GROUPS, groupValues, true);
			filters.add(filter);
		}
		
		if (!filters.isEmpty()) {
			dropboxTable.setFilters(true, filters, false, true);
		}
	}
	
	@Override
	public void reload(UserRequest ureq) {
		loadModel(true);
	}

	private void loadModel(boolean full) {
		List<FlexiTableFilter> filters = dropboxTable.getFilters();
		RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(courseEntry, pfNode.getIdent(), null, assessmentCallback);

		if (filters != null && !filters.isEmpty()) {
			FlexiTableFilter obligationFilter = FlexiTableFilter.getFilter(filters, "obligation");
			if (obligationFilter != null) {
				List<String> filterValues = ((FlexiTableExtendedFilter)obligationFilter).getValues();
				if (filterValues != null && !filterValues.isEmpty()) {
					List<AssessmentObligation> assessmentObligations = filterValues.stream()
							.map(AssessmentObligation::valueOf)
							.collect(Collectors.toList());
					params.setAssessmentObligations(assessmentObligations);
				} else {
					params.setAssessmentObligations(null);
				}
			}
			
			FlexiTableFilter groupsFilter = FlexiTableFilter.getFilter(filters, "groups");
			if(groupsFilter != null) {
				List<Long> businessGroupKeys = new ArrayList<>(filters.size());
				List<Long> curriculumElementKeys = new ArrayList<>(filters.size());
				List<String> filterValues = ((FlexiTableExtendedFilter)groupsFilter).getValues();
				if(filterValues != null) {
					for(String filterValue:filterValues) {
						if(filterValue.startsWith(BUSINESS_GROUP_PREFIX)) {
							String key = filterValue.substring(BUSINESS_GROUP_PREFIX.length(), filterValue.length());
							businessGroupKeys.add(Long.valueOf(key));
						} else if(filterValue.startsWith(CURRICULUM_EL_PREFIX)) {
							String key = filterValue.substring(CURRICULUM_EL_PREFIX.length(), filterValue.length());
							curriculumElementKeys.add(Long.valueOf(key));
						}
					}
				}
				params.setBusinessGroupKeys(businessGroupKeys);
				params.setCurriculumElementKeys(curriculumElementKeys);
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
		
		List<DropBoxRow> rows = pfManager.getParticipants(getIdentity(), params, pfNode, userPropertyHandlers, getLocale(), courseEnv);
		tableModel.setObjects(rows);
		dropboxTable.reset(full, full, true);
		flc.contextPut("hasParticipants", tableModel.getRowCount() > 0);
	}
	
	private void uploadToSelection (File uploadFile, String fileName, String destination) {
		List<Long> identitykeys = new ArrayList<>();
		for (int i : dropboxTable.getMultiSelectedIndex()) {			
			identitykeys.add(tableModel.getObject(i).getIdentity().getIdentityKey());
		}
		List<Identity> identities = securityManager.loadIdentityByKeys(identitykeys);

		pfManager.uploadFileToAllReturnBoxes(uploadFile, fileName, destination, courseEnv, pfNode, identities);
	}

	private void createFoldersForSelection() {
		List<Long> identityKeys = new ArrayList<>();
		for (int i : dropboxTable.getMultiSelectedIndex()) {
			identityKeys.add(tableModel.getObject(i).getIdentity().getIdentityKey());
		}
		List<Identity> identities = securityManager.loadIdentityByKeys(identityKeys);

		pfManager.createFoldersForAllSelectedParticipants(courseEnv, pfNode, identities);
	}
	
	private void downloadFromSelection (UserRequest ureq) {
		List<Long> identitykeys = new ArrayList<>();
		for (Integer i : dropboxTable.getMultiSelectedIndex()) {			
			identitykeys.add(tableModel.getObject(i).getIdentity().getIdentityKey());
		}
		List<Identity> identities = securityManager.loadIdentityByKeys(identitykeys);
		MediaResource resource = new FileSystemExport (identities, pfNode, courseEnv, getLocale());
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenUploadController (UserRequest ureq, boolean uploadToAll) {
		removeControllerListener(pfFileUploadCtr);
		removeControllerListener(cmc);
		
		pfFileUploadCtr = new PFFileUploadController(ureq, getWindowControl(), uploadToAll, pfNode);
		listenTo(pfFileUploadCtr);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), 
				pfFileUploadCtr.getInitialComponent(), true, translate("upload.link"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doOpenHomePage (UserRequest ureq, UserPropertiesRow row) {
		removeControllerListener(homePageDisplayController);
		removeControllerListener(cmc);
		
		Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		homePageDisplayController = new HomePageDisplayController(ureq, getWindowControl(), assessedIdentity, new HomePageConfig());
		listenTo(homePageDisplayController);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), 
				homePageDisplayController.getInitialComponent(), true, translate("upload.link"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void cleanUpCMC(){
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(pfFileUploadCtr);
		removeAsListenerAndDispose(homePageDisplayController);
		cmc = null;
		pfFileUploadCtr = null;
		homePageDisplayController = null;
	}
	
	private void doBack() {
		if(pfParticipantController != null) {
			removeAsListenerAndDispose(pfParticipantController);
			pfParticipantController = null;
		}
		loadModel(false);
	}
}
