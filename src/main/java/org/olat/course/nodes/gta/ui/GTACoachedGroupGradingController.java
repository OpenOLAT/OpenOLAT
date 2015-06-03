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

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiColumnModel;
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
import org.olat.course.assessment.bulk.PassedCellRenderer;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.ui.GroupAssessmentModel.Cols;
import org.olat.course.nodes.ms.MSCourseNodeRunController;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.ModuleConfiguration;
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
	private FormLink assessmentFormButton;
	
	private CloseableModalController cmc;
	private GroupAssessmentController assessmentCtrl;
	private CloseableCalloutWindowController commentCalloutCtrl;

	private final GTACourseNode gtaNode;
	private final BusinessGroup assessedGroup;
	private final CourseEnvironment courseEnv;
	private final AssessmentManager assessmentManager;
	private final boolean withScore, withPassed, withComment;
	
	private final boolean isAdministrativeUser;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private GTAManager gtaManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	public GTACoachedGroupGradingController(UserRequest ureq, WindowControl wControl,
			CourseEnvironment courseEnv, GTACourseNode gtaNode, BusinessGroup assessedGroup) {
		super(ureq, wControl, "coach_group_grading");
		setTranslator(Util.createPackageTranslator(MSCourseNodeRunController.class, getLocale(), getTranslator()));
		this.gtaNode = gtaNode;
		this.assessedGroup = assessedGroup;
		this.courseEnv = courseEnv;
		assessmentManager = courseEnv.getAssessmentManager();
		
		withScore = gtaNode.hasScoreConfigured();
		withPassed = gtaNode.hasPassedConfigured();
		withComment = gtaNode.hasCommentConfigured();
		
		Roles roles = ureq.getUserSession().getRoles();
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
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
		
		if(formLayout instanceof FormLayoutContainer) {
			ModuleConfiguration config = gtaNode.getModuleConfiguration();
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD, config.get(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD));
			layoutCont.contextPut(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD, config.get(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD));
			layoutCont.contextPut(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE, AssessmentHelper.getRoundedScore(config.getFloatEntry(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE)));
			layoutCont.contextPut(MSCourseNode.CONFIG_KEY_SCORE_MIN, AssessmentHelper.getRoundedScore(config.getFloatEntry(MSCourseNode.CONFIG_KEY_SCORE_MIN)));
			layoutCont.contextPut(MSCourseNode.CONFIG_KEY_SCORE_MAX, AssessmentHelper.getRoundedScore(config.getFloatEntry(MSCourseNode.CONFIG_KEY_SCORE_MAX)));
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(isAdministrativeUser) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.username.i18nKey(), Cols.username.ordinal()));
		}
		
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
					col = new StaticFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey(),
							colIndex, userPropertyHandler.getName(), true, propName,
							new StaticFlexiCellRenderer(userPropertyHandler.getName(), new TextFlexiCellRenderer()));
				} else {
					col = new DefaultFlexiColumnModel(true, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, true, propName);
				}
				columnsModel.addFlexiColumnModel(col);
			}
		}

		if(withPassed) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.passedVal.i18nKey(), Cols.passedVal.ordinal(),
					new PassedCellRenderer()));
		}
		if(withScore) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.scoreVal.i18nKey(), Cols.scoreVal.ordinal()));
		}
		if(withComment) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.commentVal.i18nKey(), Cols.commentVal.ordinal()));
		}
		
		model = new GroupAssessmentModel(gtaNode, userPropertyHandlers, getLocale(), columnsModel);
		table = uifactory.addTableElement(getWindowControl(), "group-list", model, getTranslator(), formLayout);
		table.setCustomizeColumns(true);
		table.setAndLoadPersistedPreferences(ureq, "gtagroup-assessment");
	}

	private void loadMembers() {
		//load participants, load datas
		ICourse course = CourseFactory.loadCourse(courseEnv.getCourseResourceableId());
		List<Identity> identities = businessGroupService.getMembers(assessedGroup, GroupRoles.participant.name());
		assessmentManager.preloadCache(identities);

		List<AssessmentRow> rows = new ArrayList<>(identities.size());
		for(Identity identity:identities) {
			UserCourseEnvironment userCourseEnv = AssessmentHelper.createAndInitUserCourseEnvironment(identity, course);
			ScoreEvaluation scoreEval = userCourseEnv.getScoreAccounting().evalCourseNode(gtaNode);
			if (scoreEval == null) {
				scoreEval = new ScoreEvaluation(null, null);
			}

			AssessmentRow row = new AssessmentRow(userCourseEnv, false);
			rows.add(row);
			
			if(withScore) {
				Float score = scoreEval.getScore();
				String pointVal = AssessmentHelper.getRoundedScore(score);
				row.setScore(pointVal);
			}
			
			if(withPassed) {
				row.setPassed(scoreEval.getPassed());
			}
			
			if(withComment) {
				FormLink commentLink = null;
				String comment = gtaNode.getUserUserComment(userCourseEnv);
				if(StringHelper.containsNonWhitespace(comment)) {
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
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(assessmentCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				doGrading();
				loadMembers();
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
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("comment".equals(link.getCmd())) {
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
		//assignedTask = gtaManager.updateTask(assignedTask, TaskProcess.graded);
	}
	
	private void doOpenAssessmentForm(UserRequest ureq) {
		removeAsListenerAndDispose(assessmentCtrl);
		
		assessmentCtrl = new GroupAssessmentController(ureq, getWindowControl(), courseEnv, gtaNode, assessedGroup);
		listenTo(assessmentCtrl);
		
		String title = translate("grading");
		cmc = new CloseableModalController(getWindowControl(), "close", assessmentCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
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
