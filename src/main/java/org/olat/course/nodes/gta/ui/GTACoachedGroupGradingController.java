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
package org.olat.course.nodes.gta.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.highscore.ui.HighScoreRunController;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.ui.GroupAssessmentModel.Cols;
import org.olat.course.nodes.ms.MSCourseNodeRunController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.ui.component.PassedCellRenderer;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18.03.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTACoachedGroupGradingController extends FormBasicController {
	

	protected static final String USER_PROPS_ID = GTACoachedGroupGradingController.class.getCanonicalName();
	protected static final int USER_PROPS_OFFSET = 500;
	

	private FlexiTableElement table;
	private GroupAssessmentModel model;
	private FormLink reopenButton;
	private FormLink assessmentFormButton;
	
	private CloseableModalController cmc;
	private GroupAssessmentController assessmentCtrl;
	private CloseableCalloutWindowController assessmentDocsCalloutCtrl;
	private EditAssessmentDocumentController assessmentDocsCtrl;
	private CloseableCalloutWindowController commentCalloutCtrl;

	private TaskList taskList;
	private Task assignedTask;
	private final GTACourseNode gtaNode;
	private final BusinessGroup assessedGroup;
	private final CourseEnvironment courseEnv;
	private final AssessmentManager assessmentManager;
	private final UserCourseEnvironment coachCourseEnv;
	private final boolean withScore;
	private final boolean withGrade;
	private final boolean withPassed;
	private final boolean withDocs;
	private final boolean withComment;
	private ICourse course;
	
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private GradeModule gradeModule;
	
	public GTACoachedGroupGradingController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment coachCourseEnv, CourseEnvironment courseEnv, GTACourseNode gtaNode,
			BusinessGroup assessedGroup, TaskList taskList, Task assignedTask) {
		super(ureq, wControl, "coach_group_grading");
		setTranslator(Util.createPackageTranslator(MSCourseNodeRunController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(GradeUIFactory.class, getLocale(), getTranslator()));
		this.gtaNode = gtaNode;
		this.taskList = taskList;
		this.assessedGroup = assessedGroup;
		this.courseEnv = courseEnv;
		this.assignedTask = assignedTask;
		this.coachCourseEnv = coachCourseEnv;
		assessmentManager = courseEnv.getAssessmentManager();
		
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(gtaNode);
		withScore = Mode.none != assessmentConfig.getScoreMode();
		withGrade = withScore && assessmentConfig.hasGrade() && gradeModule.isEnabled();
		withPassed = Mode.none != assessmentConfig.getPassedMode();
		withDocs = assessmentConfig.hasIndividualAsssessmentDocuments();
		withComment = assessmentConfig.hasComment();
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(GTACoachedGroupGradingController.USER_PROPS_ID, isAdministrativeUser);
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		initForm(ureq);
		loadMembers();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		assessmentFormButton = uifactory.addFormLink("coach.assessment", "coach.assessment", null, formLayout, Link.BUTTON);
		assessmentFormButton.setCustomEnabledLinkCSS("btn btn-primary");
		assessmentFormButton.setIconLeftCSS("o_icon o_icon o_icon_submit");
		assessmentFormButton.setElementCssClass("o_sel_course_gta_assessment_button");
		assessmentFormButton.setVisible(!coachCourseEnv.isCourseReadOnly() && (assignedTask == null || assignedTask.getTaskStatus() != TaskProcess.graded));

		reopenButton = uifactory.addFormLink("coach.reopen", "coach.reopen", null, formLayout, Link.BUTTON);
		reopenButton.setElementCssClass("o_sel_course_gta_reopen_button");
		reopenButton.setVisible(!coachCourseEnv.isCourseReadOnly() && assignedTask != null && assignedTask.getTaskStatus() == TaskProcess.graded);
		
		if(formLayout instanceof FormLayoutContainer) {
			ModuleConfiguration config = gtaNode.getModuleConfiguration();
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD, config.get(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD));
			layoutCont.contextPut(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD, config.get(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD));
			layoutCont.contextPut(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE, AssessmentHelper.getRoundedScore(config.getFloatEntry(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE)));
			layoutCont.contextPut(MSCourseNode.CONFIG_KEY_SCORE_MIN, AssessmentHelper.getRoundedScore(config.getFloatEntry(MSCourseNode.CONFIG_KEY_SCORE_MIN)));
			layoutCont.contextPut(MSCourseNode.CONFIG_KEY_SCORE_MAX, AssessmentHelper.getRoundedScore(config.getFloatEntry(MSCourseNode.CONFIG_KEY_SCORE_MAX)));
			
			if (config.getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD,false)){
				HighScoreRunController highScoreCtr = new HighScoreRunController(ureq, getWindowControl(), coachCourseEnv, gtaNode);
				if (highScoreCtr.isViewHighscore()) {
					Component highScoreComponent = highScoreCtr.getInitialComponent();
					layoutCont.put("highScore", highScoreComponent);							
				}
			}
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		int i=0;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			int colIndex = GTACoachedGroupGradingController.USER_PROPS_OFFSET + i++;
			if (userPropertyHandler == null) continue;
			
			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(GTACoachedGroupGradingController.USER_PROPS_ID , userPropertyHandler);
			if(visible) {
				FlexiColumnModel col;
				if(UserConstants.FIRSTNAME.equals(propName)
						|| UserConstants.LASTNAME.equals(propName)) {
					col = new DefaultFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey(),
							colIndex, userPropertyHandler.getName(), true, propName,
							new StaticFlexiCellRenderer(userPropertyHandler.getName(), new TextFlexiCellRenderer()));
				} else {
					col = new DefaultFlexiColumnModel(true, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, true, propName);
				}
				columnsModel.addFlexiColumnModel(col);
			}
		}

		if(withScore) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.scoreVal.i18nKey(), Cols.scoreVal.ordinal()));
		}
		if(withGrade) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.gradeVal.i18nKey(), Cols.gradeVal.ordinal()));
		}
		if(withPassed) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.passedVal.i18nKey(), Cols.passedVal.ordinal(),
					new PassedCellRenderer(getLocale())));
		}
		if (withDocs) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.assessmentDocsVal.i18nKey(), Cols.assessmentDocsVal.ordinal()));
		}
		if(withComment) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.commentVal.i18nKey(), Cols.commentVal.ordinal()));
		}
		
		model = new GroupAssessmentModel(userPropertyHandlers, getLocale(), columnsModel);
		table = uifactory.addTableElement(getWindowControl(), "group-list", model, getTranslator(), formLayout);
		table.setCustomizeColumns(true);
		table.setAndLoadPersistedPreferences(ureq, "gtagroup-assessment-v2");
	}

	private void loadMembers() {
		//load participants, load datas
		List<Identity> identities = businessGroupService.getMembers(assessedGroup, GroupRoles.participant.name());
		
		Map<Identity, AssessmentEntry> identityToEntryMap = new HashMap<>();
		List<AssessmentEntry> assessmentEntries = assessmentManager.getAssessmentEntries(assessedGroup, gtaNode);
		for(AssessmentEntry entry : assessmentEntries) {
			identityToEntryMap.put(entry.getIdentity(), entry);
		}

		List<AssessmentRow> rows = new ArrayList<>(identities.size());
		for(Identity identity:identities) {
			AssessmentEntry entry = identityToEntryMap.get(identity);
			AssessmentRow row = new AssessmentRow(identity, false);
			rows.add(row);
			
			if(withScore && entry != null) {
				String pointVal = AssessmentHelper.getRoundedScore(entry.getScore());
				row.setScore(pointVal);
			}
			
			if(withGrade && entry != null) {
				row.setGrade(entry.getGrade());
				row.setTranslatedGrade(GradeUIFactory.translatePerformanceClass(getTranslator(), entry.getPerformanceClassIdent(), entry.getGrade()));
			}
			
			if(withPassed && entry != null) {
				row.setPassed(entry.getPassed());
			}
			
			if(withDocs) {
				if (course == null) {
					course = CourseFactory.loadCourse(courseEnv.getCourseResourceableId());
				}
				UserCourseEnvironment userCourseEnv = row.getUserCourseEnvironment(course);
				List<File> currentAssessmentDocs = courseAssessmentService.getIndividualAssessmentDocuments(gtaNode, userCourseEnv);
				if(!currentAssessmentDocs.isEmpty()) {
					FormLink assessmentDocsTooltipLink = uifactory.addFormLink("docs-" + CodeHelper.getRAMUniqueID(),
							"assessment.docs", "assessment.docs", null, flc, Link.LINK);
					assessmentDocsTooltipLink.setIconLeftCSS("o_icon o_icon_files");
					assessmentDocsTooltipLink.setUserObject(row);
					row.setAssessmentDocsTooltipLink(assessmentDocsTooltipLink);
				}
			}
			
			if(withComment) {
				FormLink commentLink = null;
				String comment = null;
				if(entry != null && StringHelper.containsNonWhitespace(entry.getComment())) {
					comment = entry.getComment();
					commentLink = uifactory.addFormLink("comment-" + CodeHelper.getRAMUniqueID(), "comment", "comment", null, flc, Link.LINK);
					commentLink.setIconLeftCSS("o_icon o_icon_comments");
					commentLink.setUserObject(row);
				}
				row.setComment(comment);
				row.setCommentTooltipLink(commentLink);
			}
		}
		
		model.setObjects(rows);
		table.reset();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(assessmentCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadMembers();
			} else if(event == Event.CLOSE_EVENT) {
				doGrading();
				loadMembers();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(assessmentCtrl);
		removeAsListenerAndDispose(cmc);
		assessmentCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(assessmentFormButton == source) {
			doOpenAssessmentForm(ureq);
		} else if(reopenButton == source) {
			doReopenAssessment(ureq);
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("assessment.docs".equals(link.getCmd())) {
				AssessmentRow row = (AssessmentRow)link.getUserObject();
				doAssessmentDocs(ureq, row);
			} else if("comment".equals(link.getCmd())) {
				AssessmentRow row = (AssessmentRow)link.getUserObject();
				doComment(ureq, row);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doGrading() {
		if(assignedTask == null) {
			assignedTask = gtaManager.createTask(null, taskList, TaskProcess.graded, assessedGroup, null, gtaNode);
		} else {
			assignedTask = gtaManager.updateTask(assignedTask, TaskProcess.graded, gtaNode, false, getIdentity(), Role.coach);
		}
	}
	
	private void doReopenAssessment(UserRequest ureq) {
		assignedTask = gtaManager.updateTask(assignedTask, TaskProcess.grading, gtaNode, false, getIdentity(), Role.coach);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doOpenAssessmentForm(UserRequest ureq) {
		removeAsListenerAndDispose(assessmentCtrl);
		
		RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		assessmentCtrl = new GroupAssessmentController(ureq, getWindowControl(), courseEntry, gtaNode, assessedGroup, coachCourseEnv);
		listenTo(assessmentCtrl);
		
		String title = translate("grading");
		cmc = new CloseableModalController(getWindowControl(), "close", assessmentCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAssessmentDocs(UserRequest ureq, AssessmentRow row) {
		removeAsListenerAndDispose(assessmentDocsCalloutCtrl);
		removeAsListenerAndDispose(assessmentDocsCtrl);
		
		OLATResource courseOres = courseEnv.getCourseGroupManager().getCourseResource();
		assessmentDocsCtrl = new EditAssessmentDocumentController(ureq, getWindowControl(), courseOres, gtaNode, row, true);
		listenTo(assessmentDocsCtrl);
		assessmentDocsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				assessmentDocsCtrl.getInitialComponent(), row.getAssessmentDocsTooltipLink().getFormDispatchId(),
				"", true, "");
		listenTo(assessmentDocsCalloutCtrl);
		assessmentDocsCalloutCtrl.activate();
	}
	
	private void doComment(UserRequest ureq, AssessmentRow row) {
		removeAsListenerAndDispose(commentCalloutCtrl);

		VelocityContainer descriptionVC = createVelocityContainer("comment_readonly_callout");
		descriptionVC.contextPut("comment", row.getComment());
		commentCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				descriptionVC, row.getCommentTooltipLink().getFormDispatchId(), "", true, "");
		listenTo(commentCalloutCtrl);
		commentCalloutCtrl.activate();
	}
}
