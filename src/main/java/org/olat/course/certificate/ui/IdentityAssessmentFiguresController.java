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
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.olat.NewControllerFactory;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorOpenInfo;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
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
import org.olat.core.gui.components.widget.FigureWidget;
import org.olat.core.gui.components.widget.TextWidget;
import org.olat.core.gui.components.widget.WidgetFactory;
import org.olat.core.gui.components.widget.WidgetGroup;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaMapper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.EfficiencyStatement;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.assessment.model.AssessmentNodeData;
import org.olat.course.assessment.ui.tool.IdentityCertificatesController;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.ms.MSCourseNodeRunController;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.Overridable;
import org.olat.modules.assessment.ui.AssessedIdentityListController;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.olat.modules.openbadges.BadgeAssertion;
import org.olat.modules.openbadges.OpenBadgesManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.co
 *
 */
public class IdentityAssessmentFiguresController extends BasicController {
	
	private static final Size THUMBNAIL_SIZE = new Size(50, 70, false);
	private static final String CMD_OPEN_CERTIFICATE = "open.cert";

	private Link groupLink;
	private Link courseLink;
	private final VelocityContainer mainVC;
	private final ProgressBar completionItem;
	private WidgetGroup widgetGroup;
	private TextWidget courseWidget;
	private TextWidget groupWidget;
	private TextWidget passedWidget;
	private TextWidget progressWidget;
	private FigureWidget gradeWidget;
	private FigureWidget scoreWidget;
	private TextWidget scoreResultsNotVisibleWidget;
	private ProgressBar scoreProgress;
	private TextWidget certificateWidget;
	private TextWidget badgeWidget;
	
	private Controller docEditorCtrl;
	
	private final boolean links;
	private final boolean scoreScalingEnabled;
	private final BusinessGroup businessGroup;
	private final UserCourseEnvironment assessedUserCourseEnv;
	private final Formatter formatter;
	private VFSLeaf certificateLeaf;

	@Autowired
	private GradeModule gradeModule;
	@Autowired
	private GradeService gradeService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private OpenBadgesManager openBadgesManager;
	@Autowired
	private DocEditorService docEditorService;

	protected IdentityAssessmentFiguresController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment assessedUserCourseEnv, BusinessGroup businessGroup,
			EfficiencyStatement efficiencyStatement, boolean scoreScalingEnabled, boolean links) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(MSCourseNodeRunController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(GradeUIFactory.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(AssessedIdentityListController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(IdentityCertificatesController.class, getLocale(), getTranslator()));
		
		this.links = links;
		this.businessGroup = businessGroup;
		this.assessedUserCourseEnv = assessedUserCourseEnv;
		this.scoreScalingEnabled = scoreScalingEnabled;
		formatter = Formatter.getInstance(getLocale());

		mainVC = createVelocityContainer("assessment_figures");
		putInitialPanel(mainVC);

		completionItem = new ProgressBar("completion", 100, 0, Float.valueOf(100), "%");
		completionItem.setWidthInPercent(true);
		completionItem.setLabelAlignment(LabelAlignment.none);
		completionItem.setLabelMaxEnabled(false);
		completionItem.setRenderStyle(RenderStyle.radial);
		completionItem.setRenderSize(RenderSize.small);
		
		scoreProgress = new ProgressBar("scoreProgress", 100, 0, 0, null);
		scoreProgress.setWidthInPercent(true);
		scoreProgress.setLabelAlignment(LabelAlignment.none);
		scoreProgress.setRenderSize(RenderSize.small);
		scoreProgress.setLabelMaxEnabled(false);
		
		mainVC.put("completion", completionItem);
		mainVC.contextPut("scoreScalingEnabled", Boolean.valueOf(scoreScalingEnabled));
		
		initLinks(ureq);
		initWidgets();

		if (efficiencyStatement != null) {
			updateFromStatement(efficiencyStatement);
		} else if (assessedUserCourseEnv != null) {
			if (LearningPathNodeAccessProvider.TYPE
					.equals(NodeAccessType.of(assessedUserCourseEnv.getCourseEnvironment()).getType())) {
				updateLearningpath();
			} else {
				updateConditional();
			}
		}
	}
	
	private void initWidgets() {
		widgetGroup = WidgetFactory.createWidgetGroup("widgets", mainVC);
		passedWidget = WidgetFactory.createTextWidget("passed", null, translate("passed.success.status"), "o_icon_success_status");
		passedWidget.setVisible(false);
		widgetGroup.add(passedWidget);
		progressWidget = WidgetFactory.createTextWidget("progress", null, translate("learning.progress"), "o_icon_progress");
		progressWidget.setVisible(false);
		widgetGroup.add(progressWidget);
		gradeWidget = WidgetFactory.createFigureWidget("grade", null, null, "o_icon_grade");
		gradeWidget.setVisible(false);
		widgetGroup.add(gradeWidget);
		scoreWidget = WidgetFactory.createFigureWidget("score", null, translate("score"), "o_icon_score");
		scoreWidget.setAdditionalCssClass("o_widget_progress");
		scoreWidget.setVisible(false);
		widgetGroup.add(scoreWidget);
		scoreResultsNotVisibleWidget = WidgetFactory.createTextWidget("score.rnv", null, translate("score"), "o_icon_score");
		scoreResultsNotVisibleWidget.setVisible(false);
		widgetGroup.add(scoreResultsNotVisibleWidget);
		widgetGroup.add(courseWidget);
		widgetGroup.add(groupWidget);
		badgeWidget = WidgetFactory.createTextWidget("badge", null, translate("badge.widget.title"), "o_icon_badge");
		badgeWidget.setVisible(false);
		widgetGroup.add(badgeWidget);
		certificateWidget = WidgetFactory.createTextWidget("certificate", null, translate("certificate.widget.title"), "o_icon_certificate");
		certificateWidget.setVisible(false);
		widgetGroup.add(certificateWidget);
	}

	private void initLinks(UserRequest ureq) {
		if(assessedUserCourseEnv != null) {
			RepositoryEntry entry = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			if(entry != null && RepositoryEntryStatusEnum.isInArray(entry.getEntryStatus(), RepositoryEntryStatusEnum.preparationToClosed())) {
				courseLink = LinkFactory.createCustomLink("course.link", "course.link",
						StringHelper.escapeHtml(entry.getDisplayname()), Link.LINK + Link.NONTRANSLATED, mainVC, this);
				courseLink.setElementCssClass("o_nowrap");
				courseWidget = WidgetFactory.createTextWidget("course", mainVC, translate("course"), "o_CourseModule_icon");
				courseWidget.setValueComp(courseLink);
				courseWidget.setValueCssClass("o_widget_link");
				if (StringHelper.containsNonWhitespace(entry.getExternalRef())) {
					courseWidget.setAdditionalText(entry.getExternalRef());
				}
				
				VFSLeaf vfsLeaf = repositoryService.getIntroductionImage(entry);
				if (vfsLeaf != null) {
					VelocityContainer courseThumbCont = createVelocityContainer("course_widget_thumb");
					courseThumbCont.setDomReplacementWrapperRequired(false);
					courseWidget.setLeftComp(courseThumbCont);
					VFSLeaf thumbnail = vfsRepositoryService.getThumbnail(vfsLeaf, 75, 50, true);
					if (thumbnail != null) {
						VFSMediaMapper mapper = new VFSMediaMapper(thumbnail);
						String mapperId = Long.toString(CodeHelper.getUniqueIDFromString(thumbnail.getRelPath() + thumbnail.getLastModified()));
						String url = registerCacheableMapper(ureq, mapperId, mapper);
						courseThumbCont.contextPut("url", url);
					}
				}
			}
		}
		
		if(businessGroup != null) {
			mainVC.contextPut("groupName", StringHelper.escapeHtml(businessGroup.getName()));
			if(links) {
				groupLink = LinkFactory.createCustomLink("group.link", "group.link",
						StringHelper.escapeHtml(businessGroup.getName()), Link.LINK + Link.NONTRANSLATED, mainVC, this);
				groupLink.setElementCssClass("o_nowrap");
				groupWidget = WidgetFactory.createTextWidget("group", mainVC, translate("group"), "o_icon_group");
				groupWidget.setValueComp(groupLink);
				groupWidget.setValueCssClass("o_widget_link");
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
	
	protected void updateFromStatement(EfficiencyStatement efficiencyStatement) {
		List<Map<String,Object>> assessmentNodes = efficiencyStatement.getAssessmentNodes();
		List<AssessmentNodeData> assessmentNodeList = AssessmentHelper.assessmentNodeDataMapToList(assessmentNodes);
		AssessmentNodeData rootNodeData = assessmentNodeList != null && !assessmentNodeList.isEmpty()
				? assessmentNodeList.get(0) : null;
		
		if (rootNodeData != null) {
			Boolean current = rootNodeData.getPassed();
			Double completion = rootNodeData.getCompletion();
			Float score = null;
			Float maxScore = null;
			if(scoreScalingEnabled && rootNodeData.getWeightedScore() != null) {
				score = rootNodeData.getWeightedScore();
				maxScore = rootNodeData.getWeightedMaxScore();
			} else {
				score = rootNodeData.getScore();
				maxScore = rootNodeData.getMaxScore();
			}
			String grade = rootNodeData.getGrade();
			
			updatePassedUI(current != null, completion != null, true, current, completion);
			updateGradeUI(grade != null, true, rootNodeData.getPerformanceClassIdent(), grade, rootNodeData.getGradeSystemIdent());
			updateScoreUI(score != null, true, scoreScalingEnabled, score, maxScore);
		} else {
			updatePassedUI(false, false, false, null, null);
			updateGradeUI(false, false, null, null, null);
			updateScoreUI(false, false, false, null, null);
		}
	}

	private void updateLearningpath() {
		RepositoryEntry courseEntry = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		CourseNode rootNode = assessedUserCourseEnv.getCourseEnvironment().getRunStructure().getRootNode();
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, rootNode);
		AssessmentEvaluation assessmentEvaluation = assessedUserCourseEnv.getScoreAccounting().getScoreEvaluation(rootNode);
		boolean resultsVisible = assessmentEvaluation.getUserVisible() != null && assessmentEvaluation.getUserVisible().booleanValue();
		Overridable<Boolean> passedOverridable = courseAssessmentService.getRootPassed(assessedUserCourseEnv);
		
		boolean hasPassed = Mode.none != assessmentConfig.getPassedMode();
		updatePassedUI(hasPassed, true, resultsVisible, passedOverridable.getCurrent(), assessmentEvaluation.getCompletion());
		
		updateGradeUI(false, false, null, null, null);
		
		boolean hasScore = Mode.none != assessmentConfig.getScoreMode();
		updateScoreUI(hasScore, resultsVisible, scoreScalingEnabled,
				scoreScalingEnabled ? assessmentEvaluation.getWeightedScore(): assessmentEvaluation.getScore(),
				scoreScalingEnabled ? assessmentEvaluation.getWeightedMaxScore(): assessmentEvaluation.getMaxScore());
	}
	
	private void updateConditional() {
		RepositoryEntry courseEntry = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		CourseNode courseNode = assessedUserCourseEnv.getCourseEnvironment().getRunStructure().getRootNode();
		boolean gradeEnabled = gradeModule.isEnabled()
				&& courseNode.getModuleConfiguration().getBooleanSafe(STCourseNode.CONFIG_KEY_GRADE_ENABLED);
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, courseNode);
		
		AssessmentEvaluation assessmentEvaluation = courseAssessmentService.getAssessmentEvaluation(courseNode, assessedUserCourseEnv);
		boolean resultsVisible = assessmentEvaluation.getUserVisible() != null && assessmentEvaluation.getUserVisible().booleanValue();
		
		boolean hasPassed = Mode.none != assessmentConfig.getPassedMode();
		updatePassedUI(hasPassed, false, resultsVisible, assessmentEvaluation.getPassed(), null);
		
		boolean gradeApplied = StringHelper.containsNonWhitespace(assessmentEvaluation.getGrade());
		boolean hasGrade = gradeEnabled && assessmentConfig.hasGrade() && gradeApplied && gradeModule.isEnabled();
		String gradeSystemident = null;
		if (hasGrade) {
			gradeSystemident = StringHelper.containsNonWhitespace(assessmentEvaluation.getGradeSystemIdent())
					? assessmentEvaluation.getGradeSystemIdent()
					: gradeService.getGradeSystem(courseEntry, courseNode.getIdent()).toString();
		}
		updateGradeUI(hasGrade, resultsVisible, assessmentEvaluation.getPerformanceClassIdent(), assessmentEvaluation.getGrade(), gradeSystemident);
		
		boolean hasScore = Mode.none != assessmentConfig.getScoreMode();
		updateScoreUI(hasScore, resultsVisible, false, assessmentEvaluation.getScore(), null);
	}

	private void updatePassedUI(boolean hasPassed, boolean hasCompletion, boolean resultsVisible, Boolean passed, Double completion) {
		passedWidget.setVisible(hasPassed);
		progressWidget.setVisible(!hasPassed && hasCompletion);
		
		progressWidget.setValueCssClass("o_widget_text_success");
		if (resultsVisible) {
			if (passed == null) {
				passedWidget.setValue(translate("passed.nopassed"));
				passedWidget.setValueCssClass("o_noinfo");
				scoreProgress.setBarColor(BarColor.primary);
			} else if (passed) {
				passedWidget.setValue(translate("passed.yes"));
				passedWidget.setValueCssClass("o_state o_passed");
				scoreProgress.setBarColor(BarColor.passed);
			} else {
				passedWidget.setValue(translate("passed.no"));
				passedWidget.setValueCssClass("o_state o_failed");
				scoreProgress.setBarColor(BarColor.failed);
				progressWidget.setValueCssClass("o_widget_text_large o_widget_text_danger");
			}
		} else {
			passedWidget.setValue(translate("assessment.value.not.visible"));
		}
		
		if (completion != null) {
			completionItem.setActual(completion.floatValue() * 100f);
			completionItem.setPercentagesEnabled(hasPassed);
			
			completionItem.setBarColor(BarColor.success);
			completionItem.setCssClass(null);
			if (passed != null) {
				if (passed.booleanValue()) {
					completionItem.setCssClass("o_progress_passed");
				} else {
					completionItem.setBarColor(BarColor.danger);
					completionItem.setCssClass("o_progress_failed");
				}
			}
			
			passedWidget.setLeftComp(completionItem);
			progressWidget.setLeftComp(completionItem);
			
			progressWidget.setValue(Math.round(completion * 100) + "%");
		} else {
			passedWidget.setLeftComp(null);
			progressWidget.setLeftComp(null);
		}
	}
	
	private void updateGradeUI(boolean visible, boolean resultsVisible, String performanceClassIdent, String grade, String gradeSystemident) {
		if (!visible) {
			gradeWidget.setVisible(false);
			return;
		}
		
		gradeWidget.setVisible(true);
		
		String gradeSystemLabel = GradeUIFactory.translateGradeSystemLabel(getTranslator(), gradeSystemident);
		gradeWidget.setTitle(gradeSystemLabel);
		
		if (resultsVisible) {
			String translatePerformanceClass = GradeUIFactory.translatePerformanceClass(getTranslator(),
					performanceClassIdent, grade, gradeSystemident);
			gradeWidget.setValue(translatePerformanceClass);
		} else {
			gradeWidget.setValue(translate("assessment.value.not.visible"));
		}
	}
	
	private void updateScoreUI(boolean visible, boolean resultsVisible, boolean weighted, Float score, Float maxScore) {
		if (!visible) {
			scoreWidget.setVisible(false);
			scoreResultsNotVisibleWidget.setVisible(false);
			return;
		}
		
		scoreWidget.setVisible(true);
		scoreResultsNotVisibleWidget.setVisible(true);
		
		scoreWidget.setSubTitle(weighted? translate("score.weighted.subtitle"): null);
		scoreResultsNotVisibleWidget.setSubTitle(weighted? translate("score.weighted.subtitle"): null);
		
		if (!resultsVisible) {
			scoreWidget.setVisible(false);
			scoreResultsNotVisibleWidget.setVisible(true);
		}
		
		scoreWidget.setVisible(true);
		scoreResultsNotVisibleWidget.setVisible(false);
		
		if (score != null) {
			scoreWidget.setValue(AssessmentHelper.getRoundedScore(score));
			scoreProgress.setActual(score);
		} else {
			scoreWidget.setValue(translate("assessment.value.not.visible"));
		}
		
		if (maxScore != null && maxScore > 0) {
			scoreWidget.setDesc(translate("score.of", AssessmentHelper.getRoundedScore(maxScore)));
			
			scoreProgress.setMax(maxScore);
			scoreWidget.setAdditionalComp(scoreProgress);
		} else {
			scoreWidget.setDesc(null);
			scoreWidget.setAdditionalComp(null);
		}
	}
	
	public void updateCerificate(UserRequest ureq, boolean hasCertificate, Certificate certificate, String certCtrlTitleID) {
		if (!hasCertificate || certificate == null) {
			certificateWidget.setVisible(false);
			return;
		}
		
		certificateWidget.setVisible(true);
		
		certificateWidget.setValueCssClass("o_widget_text_regular");
		if (certificate.getNextRecertificationDate() != null) {
			if (certificate.getNextRecertificationDate().before(new Date())) {
				long days = DateUtils.countDays(certificate.getNextRecertificationDate(), new Date());
				if (days == 0) {
					certificateWidget.setValue(translate("row.expiration.today"));
				} else if (days == 1) {
					certificateWidget.setValue(translate("row.expiration.day", String.valueOf(days)));
				} else {
					certificateWidget.setValue(translate("row.expiration.days", String.valueOf(days)));
				}
			} else {
				certificateWidget.setValue(translate("row.valid.until", formatter.formatDate(certificate.getNextRecertificationDate())));
			}
		} else {
			certificateWidget.setValue(translate("certificate.widget.issued", formatter.formatDate(certificate.getCreationDate())));
		}
		
		certificateWidget.setAdditionalText("<a href=\"#" + certCtrlTitleID + "\">" + translate("goto.certificates") + "</a>");
		
		certificateLeaf = certificatesManager.getCertificateLeaf(certificate);
		if (certificateLeaf != null) {
			VFSLeaf thumbnail = vfsRepositoryService.getThumbnail(certificateLeaf, THUMBNAIL_SIZE.getWidth(), THUMBNAIL_SIZE.getHeight(), true);
			if (thumbnail != null) {
				VFSMediaMapper mapper = new VFSMediaMapper(thumbnail);
				String mapperId = Long.toString(CodeHelper.getUniqueIDFromString(thumbnail.getRelPath() + thumbnail.getLastModified()));
				String url = registerCacheableMapper(ureq, mapperId, mapper);
				String certContainerHtml = "<div class=\"o_text_widget_image\">" 
						+ "<img src=\"" + url + "\">"
						+ "</div>";
				Link certificateLink = LinkFactory.createCustomLink("thumb.link", CMD_OPEN_CERTIFICATE,
						certContainerHtml, Link.LINK + Link.NONTRANSLATED, mainVC, this);
				certificateWidget.setLeftComp(certificateLink);
			}
		}
		if (certificateWidget.getLeftComp() == null) {
			VelocityContainer certThumbCont = createVelocityContainer("certificate_widget_thumb");
			certThumbCont.setDomReplacementWrapperRequired(false);
			certificateWidget.setLeftComp(certThumbCont);
		}
	}

	public void updateBadge(UserRequest ureq, int numOfBagdeAssertions, BadgeAssertion badgeAssertion, String titleID) {
		if (numOfBagdeAssertions <= 0 || badgeAssertion == null) {
			badgeWidget.setVisible(false);
			return;
		}
		
		badgeWidget.setVisible(true);
		
		if (numOfBagdeAssertions == 1) {
			badgeWidget.setValueCssClass(null);
			badgeWidget.setValue(badgeAssertion.getBadgeClass().getNameWithScan());
		} else {
			badgeWidget.setValueCssClass("o_widget_text_regular");
			badgeWidget.setValue(translate("badge.of.badges", String.valueOf(1), String.valueOf(numOfBagdeAssertions)));
		}
		
		badgeWidget.setAdditionalText("<a href=\"#" + titleID + "\">" + translate("goto.badges") + "</a>");
		
		VelocityContainer badgeCont = createVelocityContainer("certificate_widget_thumb");
		badgeCont.setDomReplacementWrapperRequired(false);
		badgeWidget.setLeftComp(badgeCont);
		VFSLeaf vfsLeaf = openBadgesManager.getBadgeAssertionVfsLeaf(badgeAssertion.getBakedImage());
		if (vfsLeaf != null) {
			VFSMediaMapper mapper = new VFSMediaMapper(vfsLeaf);
			String mapperId = Long.toString(CodeHelper.getUniqueIDFromString(vfsLeaf.getRelPath() + vfsLeaf.getLastModified()));
			String url = registerCacheableMapper(ureq, mapperId, mapper);
			badgeCont.contextPut("url", url);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (docEditorCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(docEditorCtrl);
		docEditorCtrl = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(courseLink == source) {
			doOpenCourse(ureq);
		} else if(groupLink == source) {
			doOpenGroup(ureq);
		} else if (CMD_OPEN_CERTIFICATE.equals(event.getCommand())) {
			doShowCertificate(ureq);
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
	
	private void doShowCertificate(UserRequest ureq) {
		if (certificateLeaf == null) {
			return;
		}

		DocEditorConfigs configs = DocEditorConfigs.builder().build(certificateLeaf);
		DocEditorOpenInfo docEditorOpenInfo = docEditorService.openDocument(ureq, getWindowControl(), configs, DocEditorService.MODES_VIEW);
		docEditorCtrl = listenTo(docEditorOpenInfo.getController());
	}
}
