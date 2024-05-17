/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.ui.tool;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorDisplayInfo;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.download.DisplayOrDownloadComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelAlignment;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.components.widget.FigureWidget;
import org.olat.core.gui.components.widget.TextWidget;
import org.olat.core.gui.components.widget.Widget;
import org.olat.core.gui.components.widget.WidgetFactory;
import org.olat.core.gui.components.widget.WidgetGroup;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.vfs.DownloadeableVFSMediaResource;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CourseEntryRef;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.assessment.ui.tool.AssessmentForm.DocumentWrapper;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.ms.DocumentsMapper;
import org.olat.course.nodes.ms.MSCourseNodeRunController;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreScalingHelper;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.ui.AssessedIdentityListController;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grade.GradeSystem;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.olat.repository.RepositoryEntryRef;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 Mar 2022<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class AssessmentParticipantViewController extends BasicController implements Activateable2 {

	private int counter = 0;
	private final VelocityContainer mainVC;
	private WidgetGroup widgetGroup;
	private TextWidget passedWidget;
	private DisplayOrDownloadComponent download;
	
	private final AssessmentEvaluation assessmentEval;
	private final AssessmentConfig assessmentConfig;
	private final AssessmentDocumentsSupplier assessmentDocumentsSupplier;
	private final GradeSystemSupplier gradeSystemSupplier;
	private final PanelInfo panelInfo;
	private String mapperUri;
	private final Roles roles;
	
	private Controller docEditorCtrl;
	
	@Autowired
	private GradeModule gradeModule;
	@Autowired
	private UserManager userManager;
	@Autowired
	private DocEditorService docEditorService;

	public AssessmentParticipantViewController(UserRequest ureq, WindowControl wControl,
			AssessmentEvaluation assessmentEval, AssessmentConfig assessmentConfig,
			AssessmentDocumentsSupplier assessmentDocumentsSupplier, GradeSystemSupplier gradeSystemSupplier,
			PanelInfo panelInfo) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(MSCourseNodeRunController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(CourseNode.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(GradeUIFactory.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(AssessedIdentityListController.class, getLocale(), getTranslator()));
		this.assessmentEval = assessmentEval;
		this.assessmentConfig = assessmentConfig;
		this.assessmentDocumentsSupplier = assessmentDocumentsSupplier;
		this.gradeSystemSupplier = gradeSystemSupplier;
		this.panelInfo = panelInfo;
		roles = ureq.getUserSession().getRoles();
		
		mainVC = createVelocityContainer("participant_view");
		
		setTitle(translate("personal.title"));
		exposeToVC(ureq);
		
		putInitialPanel(mainVC);
	}

	private void exposeToVC(UserRequest ureq) {
		widgetGroup = WidgetFactory.createWidgetGroup("results", mainVC);
		
		passedWidget = null;
		FigureWidget gradeWidget = null;
		FigureWidget scoreWidget = null;
		Widget scoreWWeightedWidget = null;
		FigureWidget attemptsWidget = null;
		
		
		boolean resultsVisible = assessmentEval.getUserVisible() != null && assessmentEval.getUserVisible().booleanValue();
		mainVC.contextPut("resultsVisible", resultsVisible);
		
		// Attempts
		boolean hasAttempts = assessmentConfig.hasAttempts();
		if (hasAttempts) {
			attemptsWidget = WidgetFactory.createFigureWidget("attempts", null, translate("attempts.yourattempts"), "o_icon_attempts");
			
			Integer attempts = assessmentEval.getAttempts();
			if (attempts == null) {
				attempts = Integer.valueOf(0);
			}
			attemptsWidget.setValue(String.valueOf(attempts));
			if (assessmentConfig.hasMaxAttempts() && attempts > 0) {
				Integer maxAttempts = assessmentConfig.getMaxAttempts();
				attemptsWidget.setDesc(translate("attempts.of", String.valueOf(maxAttempts)));
			}
		}
		
		// Score
		boolean hasScore = Mode.none != assessmentConfig.getScoreMode();
		ProgressBar scoreProgress = null;
		if (hasScore) {
			scoreWidget = WidgetFactory.createFigureWidget("score", null, translate("score"), "o_icon_score");
			scoreWidget.setValueCssClass("o_sel_score");
			
			String scoreFormatted;
			int progress;
			if (resultsVisible && assessmentEval.getScore() != null) {
				scoreFormatted = AssessmentHelper.getRoundedScore(assessmentEval.getScore());
				progress = assessmentEval.getScore() != null? assessmentEval.getScore().intValue(): 0;
			} else {
				scoreFormatted = translate("assessment.value.not.visible");
				progress = 0;
			}
			scoreWidget.setValue(scoreFormatted);
			
			Float maxScore = assessmentConfig.getMaxScore();
			if (maxScore != null && maxScore > 0) {
				scoreWidget.setDesc(translate("score.of", AssessmentHelper.getRoundedScore(maxScore)));
				
				scoreProgress = new ProgressBar("scoreProgress", 100, progress, maxScore, null);
				scoreProgress.setWidthInPercent(true);
				scoreProgress.setLabelAlignment(LabelAlignment.none);
				scoreProgress.setRenderSize(RenderSize.small);
				scoreProgress.setLabelMaxEnabled(false);
				scoreWidget.setAdditionalComp(scoreProgress);
				scoreWidget.setAdditionalCssClass("o_widget_progress");
			}
			
			BigDecimal scoreScale = assessmentEval.getScoreScale();
			if(scoreScale != null && assessmentConfig.isScoreScalingEnabled()
					&& !ScoreScalingHelper.equals(BigDecimal.ONE, scoreScale)) {
				
				if (resultsVisible) {
					
					String scale = assessmentConfig.getScoreScale();
					String i18nLabel =  ScoreScalingHelper.isFractionScale(scale)
							? "score.weighted.fraction" : "score.weighted.decorated";
					scoreWWeightedWidget = WidgetFactory.createFigureWidget("scoreWeighted", null,
							translate("score"), translate("score.weighted.subtitle"), "o_icon_score_unbalanced",
							AssessmentHelper.getRoundedScore(assessmentEval.getWeightedScore()), null,
							translate(i18nLabel, scale), null, null, null);
				} else {
					scoreWWeightedWidget = WidgetFactory.createTextWidget("scoreWeighted", null, translate("score"),
							translate("score.weighted.subtitle"), "o_icon_score_unbalanced",
							translate("assessment.value.not.visible"), null, null, null, null);
				}
			}
		}
		
		// Grade
		boolean hasGrade = hasScore && assessmentConfig.hasGrade() && gradeModule.isEnabled();
		if (hasGrade) {
			String gradeSystemident = StringHelper.containsNonWhitespace(assessmentEval.getGradeSystemIdent())
					? assessmentEval.getGradeSystemIdent()
					: gradeSystemSupplier.getGradeSystem().getIdentifier();
			String translatePerformanceClass = GradeUIFactory.translatePerformanceClass(getTranslator(), 
					assessmentEval.getPerformanceClassIdent(), assessmentEval.getGrade(), assessmentEval.getGradeSystemIdent());
			
			gradeWidget = WidgetFactory.createFigureWidget("frade", null,
					GradeUIFactory.translateGradeSystemLabel(getTranslator(), gradeSystemident), "o_icon_grade");
			if (resultsVisible && StringHelper.containsNonWhitespace(translatePerformanceClass)) {
				gradeWidget.setValue(translatePerformanceClass);
			} else {
				gradeWidget.setValue(translate("assessment.value.not.visible"));
			}
		}
		
		// Passed
		boolean hasPassed = Mode.none != assessmentConfig.getPassedMode();
		if (hasPassed) {
			passedWidget = WidgetFactory.createTextWidget("passed", null, translate("passed.success.status"), "o_icon_success_status");
			if (resultsVisible) {
				if (assessmentEval.getPassed() == null) {
					passedWidget.setValue(translate("passed.nopassed"));
					passedWidget.setValueCssClass("o_noinfo");
				} else if (assessmentEval.getPassed()) {
					passedWidget.setValue(translate("passed.yes"));
					passedWidget.setValueCssClass("o_state o_passed");
				} else {
					passedWidget.setValue(translate("passed.no"));
					passedWidget.setValueCssClass("o_state o_failed");
				}
			} else {
				passedWidget.setValue(translate("assessment.value.not.visible"));
			}
			
			if (!hasGrade && assessmentConfig.getCutValue() != null) {
				passedWidget.setAdditionalText(translate("passed.cut.from", AssessmentHelper.getRoundedScore(assessmentConfig.getCutValue())));
			}
			if (scoreProgress != null && assessmentEval.getPassed() != null) {
				if (assessmentEval.getPassed().booleanValue()) {
					scoreProgress.setBarColor(BarColor.passed);
				} else {
					scoreProgress.setBarColor(BarColor.failed);
				}
			}
		}
		
		// Status
		boolean hasStatus = AssessmentEntryStatus.inReview == assessmentEval.getAssessmentStatus()
				|| AssessmentEntryStatus.done == assessmentEval.getAssessmentStatus();
		mainVC.contextPut("hasStatusField", Boolean.valueOf(hasStatus));
		if (hasStatus) {
			String statusText = null;
			String statusIconCss = null;
			String statusLabelCss = null;
			if (AssessmentEntryStatus.done == assessmentEval.getAssessmentStatus()) {
				if (resultsVisible) {
					statusText = translate("assessment.status.done");
					statusIconCss = "o_icon_status_done";
					statusLabelCss = "o_results_visible";
				} else {
					statusText = translate("in.release");
					statusIconCss = "o_icon_status_in_review";
					statusLabelCss = "o_results_hidden";
				}
			} else {
				statusText = translate("in.review");
				statusIconCss = "o_icon_status_in_review";
				statusLabelCss = "o_results_hidden";
			}
			mainVC.contextPut("statusText", statusText);
			mainVC.contextPut("statusIconCss", statusIconCss);
			mainVC.contextPut("statusLabelCss", statusLabelCss);
		}
		
		// Comments for participant
		String rawComment = assessmentEval.getComment();
		boolean hasComment = assessmentConfig.hasComment() && StringHelper.containsNonWhitespace(rawComment);
		if (hasComment) {
			StringBuilder comment = Formatter.stripTabsAndReturns(rawComment);
			if (comment != null && !comment.isEmpty()) {
				mainVC.contextPut("comment", StringHelper.xssScan(comment));
				mainVC.contextPut("incomment", isPanelOpen(ureq, "comment", true));
			}
		}
		
		// Assessment documents
		if (assessmentConfig.hasIndividualAsssessmentDocuments()) {
			List<VFSLeaf> documents = assessmentDocumentsSupplier.getIndividualAssessmentDocuments();
			VelocityContainer docsVC = createVelocityContainer("individual_assessment_docs");
			List<DocumentWrapper> wrappers = new ArrayList<>(documents.size());
			for (VFSLeaf document : documents) {
				wrappers.add(createDocumentWrapper(document, docsVC));
			}
			
			mapperUri = registerCacheableMapper(ureq, null, new DocumentsMapper(documents));
			mainVC.contextPut("docs", docsVC);
			mainVC.contextPut("inassessmentDocuments", isPanelOpen(ureq, "assessmentDocuments", true));
			docsVC.contextPut("mapperUri", mapperUri);
			docsVC.contextPut("documents", wrappers);
			docsVC.setVisible(!documents.isEmpty());
			mainVC.put("docs", docsVC);
			
			if (assessmentDocumentsSupplier.isDownloadEnabled() && download == null) {
				download = new DisplayOrDownloadComponent("", null);
				mainVC.put("download", download);
			}
		}
		
		widgetGroup.add(passedWidget);
		widgetGroup.add(gradeWidget);
		widgetGroup.add(scoreWidget);
		widgetGroup.add(scoreWWeightedWidget);
		widgetGroup.add(attemptsWidget);
	}

	private DocumentWrapper createDocumentWrapper(VFSLeaf document, VelocityContainer docsVC) {
		String initializedBy = null;
		Date creationDate = null;
		VFSMetadata metadata = document.getMetaInfo();
		if(metadata != null) {
			creationDate = metadata.getCreationDate();
			Identity identity = metadata.getFileInitializedBy();
			if(identity != null) {
				initializedBy = userManager.getUserDisplayName(identity);
			}
		}
		DocumentWrapper wrapper = new DocumentWrapper(document, initializedBy, creationDate);
		
		DocEditorDisplayInfo editorInfo = docEditorService.getEditorInfo(getIdentity(), roles, document,
				metadata, true, DocEditorService.modesEditView(false));
		Link openLink;
		if(editorInfo != null && editorInfo.isEditorAvailable()) {
			openLink = LinkFactory.createLink("openfile" + (++counter), "open", getTranslator(), docsVC, this, Link.LINK | Link.NONTRANSLATED);
			
			if (editorInfo.isNewWindow()) {
				openLink.setNewWindow(true, true);
			}
		} else {
			openLink = LinkFactory.createLink("openfile" + (++counter), "download", getTranslator(), docsVC, this, Link.LINK | Link.NONTRANSLATED);
		}
		openLink.setCustomDisplayText(wrapper.getFilename());
		wrapper.setOpenLink(openLink);
		
		Link downloadLink = LinkFactory.createCustomLink("download_" + (++counter), "download", "", Link.BUTTON | Link.NONTRANSLATED, docsVC, this);
		downloadLink.setIconLeftCSS("o_icon o_icon-fw o_icon_download");
		downloadLink.setGhost(true);
		downloadLink.setTarget("_blank");
		wrapper.setDownloadLink(downloadLink);
		
		return wrapper;
	}
	
	public void setTitle(String title) {
		mainVC.contextPut("title", title);
	}
	
	public void setPassedProgress(Component passedProgress) {
		if (passedWidget != null) {
			passedWidget.setLeftComp(passedProgress);
		}
	}
	
	public void addCustomWidget(Widget widget) {
		if (widgetGroup != null) {
			widgetGroup.add(widget);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(type.startsWith("path")) {
			if(download != null) {
				String path = BusinessControlFactory.getInstance().getPath(entries.get(0));
				String url = mapperUri + "/" + path;
				download.triggerFileDownload(url);
			}
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(source == docEditorCtrl) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(docEditorCtrl);
		docEditorCtrl = null;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if ("show".equals(event.getCommand())) {
			saveOpenPanel(ureq, ureq.getParameter("panel"), true);
		} else if ("hide".equals(event.getCommand())) {
			saveOpenPanel(ureq, ureq.getParameter("panel"), false);
		} else if(source instanceof Link link && link.getUserObject() instanceof DocumentWrapper wrapper) {
			if("download".equals(link.getCommand())) {
				ureq.getDispatchResult()
					.setResultingMediaResource(new DownloadeableVFSMediaResource(wrapper.getDocument()));
			} else if("open".equals(link.getCommand())) {
				doOpenDocument(ureq, wrapper);
			}
		}
	}
	
	private boolean isPanelOpen(UserRequest ureq, String panelId, boolean showDefault) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		Boolean showConfig  = (Boolean) guiPrefs.get(panelInfo.attributedClass(), getOpenPanelId(panelId));
		return showConfig == null ? showDefault : showConfig.booleanValue();
	}
	
	private void saveOpenPanel(UserRequest ureq, String panelId, boolean newValue) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			guiPrefs.putAndSave(panelInfo.attributedClass(), getOpenPanelId(panelId), Boolean.valueOf(newValue));
		}
	}
	
	private String getOpenPanelId(String panelId) {
		return panelId + panelInfo.idSuffix();
	}
	
	private void doOpenDocument(UserRequest ureq, DocumentWrapper wrapper) {
		DocEditorConfigs configs = DocEditorConfigs.builder()
				.withVersionControlled(false)
				.withMode(DocEditor.Mode.VIEW)
				.build(wrapper.getDocument());
		docEditorCtrl = docEditorService.openDocument(ureq, getWindowControl(), configs, DocEditorService.modesEditView(false)).getController();
		listenTo(docEditorCtrl);
	}

	public record PanelInfo(Class<?> attributedClass, String idSuffix) { }
	
	public interface GradeSystemSupplier {
		
		public GradeSystem getGradeSystem();
		
	}
	
	public static GradeSystemSupplier gradeSystem(UserCourseEnvironment userCourseEnv, CourseNode courseNode) {
		return new DefaultGradeSystemSupplier(userCourseEnv, courseNode);
	}
	
	private static final class DefaultGradeSystemSupplier implements GradeSystemSupplier {
		
		private final RepositoryEntryRef courseEntry;
		private final String subIdent;
		
		private DefaultGradeSystemSupplier(UserCourseEnvironment userCourseEnv, CourseNode courseNode) {
			this.courseEntry = new CourseEntryRef(userCourseEnv);
			this.subIdent = courseNode.getIdent();
		}

		@Override
		public GradeSystem getGradeSystem() {
			return CoreSpringFactory.getImpl(GradeService.class).getGradeSystem(courseEntry, subIdent);
		}
		
	}
	
	public interface AssessmentDocumentsSupplier {
		
		public List<VFSLeaf> getIndividualAssessmentDocuments();
		
		public boolean isDownloadEnabled();
		
	}

}
