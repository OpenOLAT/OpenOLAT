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
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
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
import org.olat.course.nodes.WikiCourseNode;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.reminder.ReminderService;
import org.olat.modules.reminder.model.ReminderInfos;
import org.olat.repository.CopyService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext.CopyType;
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
		List<OverviewRow> courseNodes = new ArrayList<>();
		forgeRows(courseNodes, rootNode, 0, null);
		
		copyContext.setCourseNodes(courseNodes);
		copyContext.setBlog(hasBlog(courseNodes));
		copyContext.setWiki(hasWiki(courseNodes));
		copyContext.setFolder(hasFolder(courseNodes));
		copyContext.setDateDependantNodes(hasDateDependantNodes(courseNodes));
		copyContext.setLectureBlocks(hasLectureBlogs(sourceEntry));
		copyContext.setReminders(hasReminders(sourceEntry));
		copyContext.setAssessmentModes(hasAssessmentModes(sourceEntry));
		
		copySteps.setEditLectureBlocks(copyContext.hasLectureBlocks());
		copySteps.setEditReminders(copyContext.hasReminders());
		copySteps.setEditAssessmentModes(copyContext.hasAssessmentModes());

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

	@Override
	protected void doDispose() {
		// Nothing to dispose		
	}
	
	public RepositoryEntry getCopiedEntry() {
		return copyEntry;
	}
	
	private void forgeRows(List<OverviewRow> rows, INode node, int recursionLevel, OverviewRow parent) {
		if (node instanceof CourseEditorTreeNode) {
			CourseEditorTreeNode editorNode = (CourseEditorTreeNode)node;
			OverviewRow row = forgeRow(editorNode, recursionLevel, parent);
			rows.add(row);
			
			int childCount = editorNode.getChildCount();
			for (int i = 0; i < childCount; i++) {
				INode child = editorNode.getChildAt(i);
				forgeRows(rows, child, ++recursionLevel, row);
			}
		}
	}

	private OverviewRow forgeRow(CourseEditorTreeNode editorNode, int recursionLevel, OverviewRow parent) {
		CourseNode courseNode = editorNode.getCourseNode();
		OverviewRow row = new OverviewRow(editorNode, recursionLevel);
		row.setParent(parent);
		row.setTranslatedDisplayOption(getTranslatedDisplayOption(courseNode));
		if (copyContext.isLearningPath()) {
			LearningPathConfigs learningPathConfigs = learningPathService.getConfigs(courseNode);
			row.setDuration(learningPathConfigs.getDuration());
			row.setTranslatedObligation(getTranslatedObligation(learningPathConfigs));
			row.setStart(learningPathConfigs.getStartDate());
			row.setEnd(learningPathConfigs.getEndDate());
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
		case CourseNode.DISPLAY_OPTS_SHORT_TITLE_DESCRIPTION_CONTENT: return translate("nodeConfigForm.short_title_desc_content");
		case CourseNode.DISPLAY_OPTS_TITLE_DESCRIPTION_CONTENT: return translate("nodeConfigForm.title_desc_content");
		case CourseNode.DISPLAY_OPTS_SHORT_TITLE_CONTENT: return translate("nodeConfigForm.short_title_content");
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
	
	private boolean hasWiki(List<OverviewRow> courseNodes) {
		if (courseNodes == null || courseNodes.size() == 0) {
			return false;
		} else {
			for (OverviewRow courseNode : courseNodes) {
				if (courseNode.getCourseNode() instanceof WikiCourseNode) {
					return true;
				}
			}
			
			return false;
		}
	}
	
	private boolean hasBlog(List<OverviewRow> courseNodes) {
		if (courseNodes == null || courseNodes.size() == 0) {
			return false;
		} else {
			for (OverviewRow courseNode : courseNodes) {
				if (courseNode.getCourseNode() instanceof BlogCourseNode) {
					return true;
				}
			}
			
			return false;
		}
	}
	
	private boolean hasFolder(List<OverviewRow> courseNodes) {
		if (courseNodes == null || courseNodes.size() == 0) {
			return false;
		} else {
			for (OverviewRow courseNode : courseNodes) {
				if (courseNode.getCourseNode() instanceof BCCourseNode) {
					return true;
				}
			}
			
			return false;
		}
	}
	
	private boolean hasDateDependantNodes(List<OverviewRow> courseNodes) {
		if (courseNodes == null || courseNodes.size() == 0) {
			return false;
		} else {
			for (OverviewRow courseNode : courseNodes) {
				LearningPathConfigs configs = learningPathService.getConfigs(courseNode.getCourseNode());
				
				if (configs.getStartDate() != null || configs.getEndDate() != null) {
					return true;
				}
			}
			
			return false;
		}
	}
	
	private boolean hasLectureBlogs(RepositoryEntry repoEntry) {
		List<LectureBlock> lectureBlocks = lectureService.getLectureBlocks(repoEntry);
		
		return lectureBlocks != null && !lectureBlocks.isEmpty();
	}
	
	private boolean hasAssessmentModes(RepositoryEntry repoEntry) {
		List<AssessmentMode> assessmentModes = assessmentModeManager.getAssessmentModeFor(repoEntry);
		
		return assessmentModes != null && !assessmentModes.isEmpty();
	}
	
	private boolean hasReminders(RepositoryEntry repoEntry) {
		List<ReminderInfos> reminders = reminderManager.getReminderInfos(repoEntry);
		
		return reminders != null && !reminders.isEmpty();
	}
	
	private class FinishCallback implements StepRunnerCallback {
		@Override
		public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
			CopyCourseContext copyContext = (CopyCourseContext) runContext.get(CopyCourseContext.CONTEXT_KEY);
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
