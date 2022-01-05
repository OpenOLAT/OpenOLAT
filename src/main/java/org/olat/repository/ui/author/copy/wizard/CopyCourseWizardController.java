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
package org.olat.repository.ui.author.copy.wizard;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.admin.user.UserTableDataModel;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.config.CourseConfig;
import org.olat.course.editor.EditorMainController;
import org.olat.course.editor.overview.OverviewRow;
import org.olat.course.learningpath.FullyAssessedTrigger;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.LearningPathTranslations;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.learningpath.ui.LearningPathNodeConfigController;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.BlogCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.WikiCourseNode;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.manager.MemberViewQueries;
import org.olat.group.model.BusinessGroupQueryParams;
import org.olat.group.model.MemberView;
import org.olat.group.model.StatisticsBusinessGroupRow;
import org.olat.group.ui.main.SearchMembersParams;
import org.olat.group.ui.main.SearchMembersParams.Origin;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.reminder.ReminderModule;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.ReminderService;
import org.olat.modules.reminder.RuleSPI;
import org.olat.modules.reminder.model.ReminderInfos;
import org.olat.modules.reminder.model.ReminderRules;
import org.olat.modules.reminder.rule.BeforeDateRuleSPI;
import org.olat.modules.reminder.rule.DateRuleSPI;
import org.olat.repository.CatalogEntry;
import org.olat.repository.CopyService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.manager.CatalogManager;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext.CopyType;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 18.02.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CopyCourseWizardController extends BasicController {
	
	private StepsMainRunController copyWizardController;
	
	private RepositoryEntry copyEntry;
	private CopyCourseContext copyContext;
	private final RepositoryEntry sourceEntry;
	
	@Autowired
	private AssessmentModeManager assessmentModeManager;
	@Autowired
	private CopyService copyService;
	@Autowired
	private CopyCourseWizardModule wizardModule;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private LearningPathService learningPathService;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private ReminderService reminderManager;
	@Autowired
	private ReminderModule reminderModule;
	@Autowired
	private UserManager userManager;
	@Autowired
	private MemberViewQueries memberViewQueries;
	@Autowired
	private BaseSecurityManager securityManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private CatalogManager catalogManager;
	
	public CopyCourseWizardController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry, ICourse course) {
		super(ureq, wControl);
		
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(EditorMainController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(LearningPathNodeConfigController.class, getLocale(), getTranslator()));
		
		this.sourceEntry = repositoryEntry;
		
		FinishCallback finish = new FinishCallback();
        CancelCallback cancel = new CancelCallback();
        
        CopyCourseSteps copySteps = new CopyCourseSteps();
        copySteps.setAdvancedMode(wizardModule.getWizardMode().equals(CopyType.custom));
        
        copyContext = new CopyCourseContext();
        copyContext.setExecutingIdentity(getIdentity());
        copyContext.setSourceRepositoryEntry(repositoryEntry);
        copyContext.setCourse(course);
		copyContext.setLearningPath(LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(course).getType()));
		
		// Load copy mode defaults
		copySteps.loadFromWizardConfig(wizardModule);
		copyContext.loadFromWizardConfig(wizardModule);
		
		TreeNode rootNode = course.getEditorTreeModel().getRootNode();
		List<CopyCourseOverviewRow> courseNodes = new ArrayList<>();
		forgeRows(courseNodes, rootNode, 0, null);
		
		copyContext.setCourseNodes(courseNodes);
		copyContext.setTest(hasCourseNode(courseNodes, IQTESTCourseNode.class));
		copyContext.setBlog(hasCourseNode(courseNodes, BlogCourseNode.class));
		copyContext.setWiki(hasCourseNode(courseNodes, WikiCourseNode.class));
		copyContext.setFolder(hasCourseNode(courseNodes, BCCourseNode.class));
		copyContext.setHasTask(hasCourseNode(courseNodes, GTACourseNode.class));
		copyContext.setDateDependantNodes(hasDateDependantNodes(courseNodes));
		copyContext.setLectureBlocks(hasLectureBlogs(sourceEntry));
		copyContext.setDateDependantReminders(hasDateDependantReminders(sourceEntry));
		copyContext.setHasReminders(hasReminders(sourceEntry));
		copyContext.setAssessmentModes(hasAssessmentModes(sourceEntry));
		copyContext.setNewCoaches(getCoaches(sourceEntry));
		copyContext.setHasGroups(hasGroups(sourceEntry));
		copyContext.setHasCoaches(hasCoaches(sourceEntry));
		copyContext.setHasOwners(hasOwners(sourceEntry));
		copyContext.setHasDisclaimer(hasDisclaimer(course));
		copyContext.setHasCatalogEntry(hasCatalogEntry(sourceEntry));
		copyContext.setDocuments(hasDocuments(course));
		copyContext.setCoachDocuments(hasCoachDocuments(course));
		
        CopyCourseGeneralStep copyCourseStep = new CopyCourseGeneralStep(ureq, copySteps, copyContext);
        
        copyWizardController = new StepsMainRunController(ureq, getWindowControl(), copyCourseStep, finish, cancel, translate("course.copy"), null);
		
        listenTo(copyWizardController);
        getWindowControl().pushAsModalDialog(copyWizardController.getInitialComponent());
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		fireEvent(ureq, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == copyWizardController) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				// Close the dialog
				getWindowControl().pop();

		        // Remove steps controller
		        removeAsListenerAndDispose(copyWizardController);
		        copyWizardController = null;
		        
		        // Fire event
		        fireEvent(ureq, event);
	        }
		}
	}
	
	public RepositoryEntry getCopiedEntry() {
		return copyEntry;
	}
	
	private void forgeRows(List<CopyCourseOverviewRow> rows, INode node, int recursionLevel, CopyCourseOverviewRow parent) {
		if (node instanceof CourseEditorTreeNode) {
			CourseEditorTreeNode editorNode = (CourseEditorTreeNode)node;
			CopyCourseOverviewRow row = forgeRow(editorNode, recursionLevel, parent);
			rows.add(row);
			
			int childCount = editorNode.getChildCount();
			for (int i = 0; i < childCount; i++) {
				INode child = editorNode.getChildAt(i);
				forgeRows(rows, child, ++recursionLevel, row);
			}
		}
	}

	private CopyCourseOverviewRow forgeRow(CourseEditorTreeNode editorNode, int recursionLevel, CopyCourseOverviewRow parent) {
		CourseNode courseNode = editorNode.getCourseNode();
		CopyCourseOverviewRow row = new CopyCourseOverviewRow(editorNode, recursionLevel);
		row.setParent(parent);
		row.setTranslatedDisplayOption(getTranslatedDisplayOption(courseNode));
		if (copyContext.isLearningPath()) {
			LearningPathConfigs learningPathConfigs = learningPathService.getConfigs(courseNode, editorNode.getParent());
			row.setDuration(learningPathConfigs.getDuration());
			row.setDuration(learningPathConfigs.getDuration());
			row.setTranslatedObligation(getTranslatedObligation(learningPathConfigs));
			row.setStart(learningPathConfigs.getStartDateConfig());
			row.setEnd(learningPathConfigs.getEndDateConfig());
			row.setTranslatedTrigger(getTranslatedTrigger(courseNode, learningPathConfigs));
			row.setLearningPathConfigs(learningPathConfigs);
		}
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
		row.setAssessmentConfig(assessmentConfig);
		return row;
	}
	
	private String getTranslatedDisplayOption(CourseNode courseNode) {
		String displayOption = courseNode.getDisplayOption();
		if (displayOption == null) return null;
		
		switch(displayOption) {
		case CourseNode.DISPLAY_OPTS_TITLE_DESCRIPTION_CONTENT: return translate("nodeConfigForm.title_desc_content");
		case CourseNode.DISPLAY_OPTS_TITLE_CONTENT: return translate("nodeConfigForm.title_content");
		case CourseNode.DISPLAY_OPTS_DESCRIPTION_CONTENT: return translate("nodeConfigForm.description_content");
		case CourseNode.DISPLAY_OPTS_CONTENT: return translate("nodeConfigForm.content_only");
		default:
			// nothing
		}
		return null;
	}

	private String getTranslatedObligation(LearningPathConfigs learningPathConfigs) {
		AssessmentObligation obligation = learningPathConfigs.getObligation();
		if (obligation == null) return null;
		
		switch (obligation) {
		case mandatory: return translate("config.obligation.mandatory");
		case optional: return translate("config.obligation.optional");
		default:
			// nothing
		}
		return null;
	}

	private String getTranslatedTrigger(CourseNode courseNode, LearningPathConfigs learningPathConfigs) {
		FullyAssessedTrigger trigger = learningPathConfigs.getFullyAssessedTrigger();
		if (trigger == null) return null;
		
		switch (trigger) {
		case nodeVisited: return translate("config.trigger.visited");
		case confirmed: return translate("config.trigger.confirmed");
		case score: {
			Integer scoreTriggerValue = learningPathConfigs.getScoreTriggerValue();
			return translate("config.trigger.score.value", new String[] { scoreTriggerValue.toString() } );
		}
		case passed: return translate("config.trigger.passed");
		case statusInReview: {
			LearningPathTranslations translations = learningPathService.getEditConfigs(courseNode).getTranslations();
			return translations.getTriggerStatusInReview(getLocale()) != null
					? translations.getTriggerStatusInReview(getLocale())
					: translate("config.trigger.status.in.review");
		}
		case statusDone: {
			LearningPathTranslations translations = learningPathService.getEditConfigs(courseNode).getTranslations();
			return translations.getTriggerStatusDone(getLocale()) != null
					? translations.getTriggerStatusDone(getLocale())
					: translate("config.trigger.status.done");
		}
		default:
			// nothing
		}
		return null;
	}
	
	private boolean hasCourseNode(List<CopyCourseOverviewRow> courseNodes, Class... courseNodeClasses) {
		if (courseNodes == null || courseNodes.size() == 0) {
			return false;
		} else {
			for (OverviewRow courseNode : courseNodes) {
				for (Class courseNodeClass : courseNodeClasses) {
					if (courseNodeClass.isInstance(courseNode.getCourseNode())) {
						return true;
					}
				}
			}
			
			return false;
		}
	}
	
	private boolean hasDateDependantNodes(List<CopyCourseOverviewRow> rows) {
		if (rows == null || rows.size() == 0) {
			return false;
		} 
		
		for (CopyCourseOverviewRow row : rows) {
			if (row.getLearningPathConfigs().getStartDateConfig() != null || row.getLearningPathConfigs().getEndDateConfig() != null) {
				return true;
			}
		}
		
		return false;
	}
	
	private boolean hasLectureBlogs(RepositoryEntry repoEntry) {
		List<LectureBlock> lectureBlocks = lectureService.getLectureBlocks(repoEntry);
		
		return lectureBlocks != null && !lectureBlocks.isEmpty();
	}
	
	private boolean hasAssessmentModes(RepositoryEntry repoEntry) {
		List<AssessmentMode> assessmentModes = assessmentModeManager.getAssessmentModeFor(repoEntry);
		
		return assessmentModes != null && !assessmentModes.isEmpty();
	}
	
	private boolean hasDateDependantReminders(RepositoryEntry repoEntry) {
		List<ReminderInfos> reminders = reminderManager.getReminderInfos(repoEntry);
		
		if (reminders == null || reminders.isEmpty()) {
			return false;
		}
		
		for (ReminderInfos reminder : reminders) {
			ReminderRules rules = reminderManager.toRules(reminder.getConfiguration());
			
			if (rules != null) {
				for (ReminderRule rule : rules.getRules()) {
					RuleSPI ruleSPI = reminderModule.getRuleSPIByType(rule.getType());
					if (ruleSPI instanceof DateRuleSPI || ruleSPI instanceof BeforeDateRuleSPI) {
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	private boolean hasReminders(RepositoryEntry repositoryEntry) {
		List<ReminderInfos> reminders = reminderManager.getReminderInfos(repositoryEntry);
		
		return reminders != null && !reminders.isEmpty();
	}
	
	private boolean hasGroups(RepositoryEntry repositoryEntry) {
		BusinessGroupQueryParams params = new BusinessGroupQueryParams();
		params.setTechnicalTypes(List.of(BusinessGroup.BUSINESS_TYPE));
		params.setRepositoryEntry(repositoryEntry);
		
		List<StatisticsBusinessGroupRow> groups = businessGroupService.findBusinessGroupsFromRepositoryEntry(params, getIdentity(), params.getRepositoryEntry());
		
		return !groups.isEmpty();
	}
	
	private boolean hasDisclaimer(ICourse course) {
		CourseConfig courseConfig = course.getCourseEnvironment().getCourseConfig();
		
		return courseConfig.isDisclaimerEnabled();
	}
	
	private boolean hasOwners(RepositoryEntry repositoryEntry) {
		String usageIdentifyer = UserTableDataModel.class.getCanonicalName();
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, false);
		SearchMembersParams params = new SearchMembersParams(false, GroupRoles.owner);
		List<MemberView> memberViews = memberViewQueries.getRepositoryEntryMembers(repositoryEntry, params, userPropertyHandlers, getLocale());
		List<Long> identityKeys = memberViews.stream().map(MemberView::getIdentityKey).collect(Collectors.toList());
		
		return !identityKeys.isEmpty();
	}
	
	private boolean hasCatalogEntry(RepositoryEntry repositoryEntry) {
		List<CatalogEntry> catalogEntries = catalogManager.getCatalogCategoriesFor(repositoryEntry);
		
		return !catalogEntries.isEmpty();
	}
	
	private boolean hasCoaches(RepositoryEntry repositoryEntry) {
		String usageIdentifyer = UserTableDataModel.class.getCanonicalName();
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, false);
		SearchMembersParams params = new SearchMembersParams(false, GroupRoles.coach);
		params.setOrigin(Origin.repositoryEntry);
		
		List<MemberView> memberViews = memberViewQueries.getRepositoryEntryMembers(sourceEntry, params, userPropertyHandlers, getLocale());

		return !memberViews.isEmpty();
	}
	
	private List<Identity> getCoaches(RepositoryEntry sourceEntry) {
		String usageIdentifyer = UserTableDataModel.class.getCanonicalName();
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, false);
		SearchMembersParams params = new SearchMembersParams(false, GroupRoles.coach);
		params.setOrigin(Origin.repositoryEntry);
		
		List<MemberView> memberViews = memberViewQueries.getRepositoryEntryMembers(sourceEntry, params, userPropertyHandlers, getLocale());
		List<Long> identityKeys = memberViews.stream().map(MemberView::getIdentityKey).collect(Collectors.toList());
		List<Identity> coaches = securityManager.loadIdentityByKeys(identityKeys);
		
		return coaches;
	}
	
	private boolean hasDocuments(ICourse course) {
		CourseConfig config = course.getCourseEnvironment().getCourseConfig();
		
		return config.isDocumentsEnabled();
	}
	
	private boolean hasCoachDocuments(ICourse course) {
		CourseConfig config = course.getCourseEnvironment().getCourseConfig();
		
		return config.isCoachFolderEnabled();
	}
	
	private class FinishCallback implements StepRunnerCallback {
		@Override
		public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
			CopyCourseContext copyContext = (CopyCourseContext) runContext.get(CopyCourseContext.CONTEXT_KEY);
			
			if (copyContext.getDateDifference() == 0l) {
				copyContext.setDateDifference(copyContext.getDateDifferenceByEarliest());
			}
			
			copyEntry = copyService.copyLearningPathCourse(copyContext);
			
			return StepsMainRunController.DONE_MODIFIED;
		}		
	}
	
	private class CancelCallback implements StepRunnerCallback {
		@Override
		public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
			return Step.NOSTEP;
		}		
	}
}
