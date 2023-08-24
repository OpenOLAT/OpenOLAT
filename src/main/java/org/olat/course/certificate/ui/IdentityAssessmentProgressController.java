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
package org.olat.course.certificate.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelAlignment;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderStyle;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.EfficiencyStatement;
import org.olat.course.assessment.UserEfficiencyStatement;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.assessment.model.AssessmentNodeData;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.Overridable;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 2 août 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityAssessmentProgressController extends BasicController {

	private Link groupLink;
	private Link courseLink;
	private final VelocityContainer mainVC;
	private final ProgressBar completionItem;
	
	private final boolean links;
	private final BusinessGroup businessGroup;
	private final UserCourseEnvironment assessedUserCourseEnv;

	@Autowired
	private GradeModule gradeModule;
	@Autowired
	private GradeService gradeService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;

	protected IdentityAssessmentProgressController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment assessedUserCourseEnv, BusinessGroup businessGroup,
			UserEfficiencyStatement userEfficiencyStatement, EfficiencyStatement efficiencyStatement,
			boolean links) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(GradeUIFactory.class, getLocale(), getTranslator()));
		
		this.links = links;
		this.businessGroup = businessGroup;
		this.assessedUserCourseEnv = assessedUserCourseEnv;

		mainVC = createVelocityContainer("assessment_infos");

		completionItem = new ProgressBar("completion", 100, 0, Float.valueOf(100), "%");
		completionItem.setWidthInPercent(true);
		completionItem.setLabelAlignment(LabelAlignment.none);
		completionItem.setLabelMaxEnabled(false);
		completionItem.setRenderStyle(RenderStyle.radial);
		completionItem.setRenderSize(RenderSize.small);
		mainVC.put("completion", completionItem);

		putInitialPanel(mainVC);
		initLinks();
		
		if(efficiencyStatement != null) {
			updateFromStatement(userEfficiencyStatement, efficiencyStatement);
		} else if(assessedUserCourseEnv != null) {
			if(LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(assessedUserCourseEnv.getCourseEnvironment()).getType())) {
				updateLearningpath();
			} else {
				updateConditional();
			}
		} 
	}
	
	private void initLinks() {
		if(assessedUserCourseEnv != null) {
			RepositoryEntry entry = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			if(entry != null && RepositoryEntryStatusEnum.isInArray(entry.getEntryStatus(), RepositoryEntryStatusEnum.preparationToClosed())) {	
				courseLink = LinkFactory.createLink("course.link", mainVC, this);
				courseLink.setIconRightCSS("o_icon o_icon_content_popup");
				mainVC.put("course.link", courseLink);
			}
		}
		
		if(businessGroup != null) {
			mainVC.contextPut("groupName", StringHelper.escapeHtml(businessGroup.getName()));
			if(links) {
				groupLink = LinkFactory.createLink("group.link", mainVC, this);
				groupLink.setIconRightCSS("o_icon o_icon_content_popup");
				mainVC.put("group.link", groupLink);
			}
		}
	}
	
	public boolean hasCompletion() {
		return completionItem.isVisible();
	}

	public float getCompletion() {
		return completionItem.getActual();
	}
	
	public BarColor getBarColor() {
		return completionItem.getBarColor();
	}
	
	protected void updateFromStatement(UserEfficiencyStatement userEfficiencyStatement, EfficiencyStatement efficiencyStatement) {
		List<Map<String,Object>> assessmentNodes = efficiencyStatement.getAssessmentNodes();
		List<AssessmentNodeData> assessmentNodeList = AssessmentHelper.assessmentNodeDataMapToList(assessmentNodes);
		AssessmentNodeData rootNodeData = assessmentNodeList != null && !assessmentNodeList.isEmpty()
				? assessmentNodeList.get(0) : null;
		
		Boolean current = null;
		Double completion = null;
		Float score = null;
		String grade = null;
		String gradeSystemIdent = null;
		Float maxScore = rootNodeData == null ? null : rootNodeData.getMaxScore();
		if(userEfficiencyStatement != null) {
			current = userEfficiencyStatement.getPassed();
			completion = userEfficiencyStatement.getCompletion();
			score = userEfficiencyStatement.getScore();
			grade = userEfficiencyStatement.getGrade();
			gradeSystemIdent = userEfficiencyStatement.getGradeSystemIdent();
		} else if(rootNodeData != null) {
			current = rootNodeData.getPassed();
			completion = rootNodeData.getCompletion();
			score = rootNodeData.getScore();
			grade = rootNodeData.getGrade();
			gradeSystemIdent = rootNodeData.getGradeSystemIdent();
		}
		updateUI(completion, current, score, maxScore, grade, gradeSystemIdent);
	}

	private void updateLearningpath() {
		Overridable<Boolean> passedOverridable = courseAssessmentService.getRootPassed(assessedUserCourseEnv);
		CourseNode rootNode = assessedUserCourseEnv.getCourseEnvironment().getRunStructure().getRootNode();
		AssessmentEvaluation assessmentEvaluation = assessedUserCourseEnv.getScoreAccounting().getScoreEvaluation(rootNode);
		boolean resultsVisible = assessmentEvaluation.getUserVisible() != null && assessmentEvaluation.getUserVisible().booleanValue();
		if(resultsVisible) {
			Boolean current = passedOverridable.getCurrent();
			Double completion = assessmentEvaluation.getCompletion();
			Float score = assessmentEvaluation.getScore();
			Float maxScore = assessmentEvaluation.getMaxScore();
			
			updateUI(completion, current, score, maxScore, null, null);
		} else {
			updateUI(null, null, null, null, null, null);
		}
	}
	
	private void updateConditional() {
		RepositoryEntry courseEntry = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		CourseNode courseNode = assessedUserCourseEnv.getCourseEnvironment().getRunStructure().getRootNode();
		boolean gradeEnabled = gradeModule.isEnabled()
				&& courseNode.getModuleConfiguration().getBooleanSafe(STCourseNode.CONFIG_KEY_GRADE_ENABLED);
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, courseNode);
		
		AssessmentEvaluation assessmentEvaluation = courseAssessmentService.getAssessmentEvaluation(courseNode, assessedUserCourseEnv);
		boolean resultsVisible = assessmentEvaluation.getUserVisible() != null && assessmentEvaluation.getUserVisible().booleanValue();
		if(resultsVisible) {
			boolean hasScore = Mode.none != assessmentConfig.getScoreMode() && assessmentEvaluation.getScore() != null;
			Float score = null;
			if (hasScore) {
				score = assessmentEvaluation.getScore();
			}
			
			boolean hasPassed = Mode.none != assessmentConfig.getPassedMode() && assessmentEvaluation.getPassed() != null;
			Boolean current = null;
			if (hasPassed) {
				current = assessmentEvaluation.getPassed();
			}
			
			boolean gradeApplied = StringHelper.containsNonWhitespace(assessmentEvaluation.getGrade());
			boolean hasGrade = gradeEnabled && assessmentConfig.hasGrade() && gradeApplied && gradeModule.isEnabled();
			String grade = null;
			String gradeSystemident = null;
			if (hasGrade) {
				gradeSystemident = StringHelper.containsNonWhitespace(assessmentEvaluation.getGradeSystemIdent())
						? assessmentEvaluation.getGradeSystemIdent()
						: gradeService.getGradeSystem(courseEntry, courseNode.getIdent()).toString();
			}
	
			mainVC.setVisible(hasScore || hasPassed || hasGrade);
			updateUI(null, current, score, null, grade, gradeSystemident);
		} else {
			updateUI(null, null, null, null, null, null);
		}
	}
	
	private void updateUI(Double completion, Boolean current, Float score, Float maxScore,
			String grade, String gradeSystemIdent) {
		boolean passed = current != null && current.booleanValue();
		boolean failed = current != null && !current.booleanValue();
		
		if(completion != null) {
			BarColor barColor = failed ? BarColor.danger : BarColor.success;
			completionItem.setBarColor(barColor);
			completionItem.setActual(completion.floatValue() * 100f);
			completionItem.setVisible(true);
		} else {
			completionItem.setVisible(false);
		}
		
		if(passed) {
			mainVC.contextPut("completionPassed", Boolean.TRUE);
		} else if(failed) {
			mainVC.contextPut("completionPassed", Boolean.FALSE);
		} else {
			mainVC.contextRemove("completionPassed");
		}

		if (score != null && score.floatValue() > 0.0f) {
			String scoreStr = Integer.toString(Math.round(score.floatValue())); 
			if(maxScore != null && maxScore.floatValue() > 0.0f) {
				String maxScoreStr = Integer.toString(Math.round(maxScore.floatValue()));
				completionItem.setInfo(translate("progress.score.w.max", scoreStr, maxScoreStr));
			} else {
				completionItem.setInfo(translate("progress.score", scoreStr));
			}
		}
		completionItem.setVisible(completion != null);
		
		if(score != null) {
			mainVC.contextPut("score", AssessmentHelper.getRoundedScore(score));
		} else {
			mainVC.contextRemove("score");
		}
		
		if(maxScore != null) {
			mainVC.contextPut("maxScore", AssessmentHelper.getRoundedScore(maxScore));
		} else {
			mainVC.contextRemove("maxScore");
		}
		
		if(StringHelper.containsNonWhitespace(grade)) {
			String gradeSystemLabel = GradeUIFactory.translateGradeSystemLabel(getTranslator(), gradeSystemIdent);
			mainVC.contextPut("grade", translate("grade.value", gradeSystemLabel, grade));
		} else {
			mainVC.contextRemove("grade");
		}
		
		mainVC.setVisible(score != null || grade != null || completionItem.isVisible());
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(courseLink == source) {
			doOpenCourse(ureq);
		} else if(groupLink == source) {
			doOpenGroup(ureq);
		}
	}
	
	private void doOpenGroup(UserRequest ureq) {
		if(businessGroup != null) {
			List<ContextEntry> ces = new ArrayList<>(1);
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("BusinessGroup", businessGroup.getKey());
			ces.add(BusinessControlFactory.getInstance().createContextEntry(ores));
	
			BusinessControl bc = BusinessControlFactory.getInstance().createFromContextEntries(ces);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
			NewControllerFactory.getInstance().launch(ureq, bwControl);
		}
	}
	
	private void doOpenCourse(UserRequest ureq) {
		if(assessedUserCourseEnv != null) {
			List<ContextEntry> ces = new ArrayList<>(1);
			RepositoryEntry courseRepoEntry = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("RepositoryEntry", courseRepoEntry.getKey());
			ces.add(BusinessControlFactory.getInstance().createContextEntry(ores));
	
			BusinessControl bc = BusinessControlFactory.getInstance().createFromContextEntries(ces);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
			NewControllerFactory.getInstance().launch(ureq, bwControl);
		}
	}
}
