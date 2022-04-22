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
package org.olat.course.assessment.ui.tool;

import java.io.File;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.download.DisplayOrDownloadComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.prefs.Preferences;
import org.olat.course.CourseEntryRef;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.ms.DocumentsMapper;
import org.olat.course.nodes.ms.MSCourseNodeRunController;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.ui.AssessedIdentityListController;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeService;
import org.olat.modules.grade.GradeSystem;
import org.olat.modules.grade.ui.GradeUIFactory;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 Mar 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentParticipantViewController extends BasicController implements Activateable2 {

	private final VelocityContainer mainVC;
	private DisplayOrDownloadComponent download;
	
	private final AssessmentEvaluation assessmentEval;
	private final AssessmentConfig assessmentConfig;
	private final AssessmentDocumentsSupplier assessmentDocumentsSupplier;
	private final GradeSystemSupplier gradeSystemSupplier;
	private final PanelInfo panelInfo;
	private String mapperUri;
	
	@Autowired
	private GradeModule gradeModule;

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
		
		mainVC = createVelocityContainer("participant_view");
		
		exposeToVC(ureq);
		
		putInitialPanel(mainVC);
	}
	
	private void exposeToVC(UserRequest ureq) {
		boolean resultsVisible = assessmentEval.getUserVisible() != null && assessmentEval.getUserVisible().booleanValue();
		mainVC.contextPut("resultsVisible", resultsVisible);
		
		// Attempts
		boolean hasAttempts = assessmentConfig.hasAttempts();
		mainVC.contextPut("hasAttemptsField", Boolean.valueOf(hasAttempts));
		if (hasAttempts) {
			Integer attempts = assessmentEval.getAttempts();
			if (attempts == null) {
				attempts = Integer.valueOf(0);
			}
			mainVC.contextPut("attempts", attempts);
			if (assessmentConfig.hasMaxAttempts()) {
				Integer maxAttempts = assessmentConfig.getMaxAttempts();
				mainVC.contextPut("maxAttempts", maxAttempts);
			}
		}
		
		// Score
		boolean hasScore = Mode.none != assessmentConfig.getScoreMode();
		mainVC.contextPut("hasScoreField", Boolean.valueOf(hasScore));
		if (hasScore) {
			Float minScore = assessmentConfig.getMinScore();
			String scoreMinMax = AssessmentHelper.getMinMax(getTranslator(), minScore, assessmentConfig.getMaxScore());
			if (scoreMinMax != null) {
				mainVC.contextPut("scoreMinMax", scoreMinMax);
			}
			mainVC.contextPut("score", AssessmentHelper.getRoundedScore(assessmentEval.getScore()));
		}
		
		// Grade
		boolean hasGrade = hasScore && assessmentConfig.hasGrade() && gradeModule.isEnabled();
		mainVC.contextPut("hasGradeField", Boolean.valueOf(hasGrade));
		if (hasGrade) {
			String gradeSystemident = StringHelper.containsNonWhitespace(assessmentEval.getGradeSystemIdent())
					? assessmentEval.getGradeSystemIdent()
					: gradeSystemSupplier.getGradeSystem().getIdentifier();
			mainVC.contextPut("gradeLabel", GradeUIFactory.translateGradeSystemLabel(getTranslator(), gradeSystemident));
			mainVC.contextPut("grade", GradeUIFactory.translatePerformanceClass(getTranslator(), 
					assessmentEval.getPerformanceClassIdent(), assessmentEval.getGrade(), assessmentEval.getGradeSystemIdent()));
		}
		
		// Passed
		boolean hasPassed = Mode.none != assessmentConfig.getPassedMode();
		mainVC.contextPut("hasPassedField", Boolean.valueOf(hasPassed));
		if (hasPassed) {
			mainVC.contextPut("hasPassedValue", (assessmentEval.getPassed() == null ? Boolean.FALSE : Boolean.TRUE));
			mainVC.contextPut("passed", assessmentEval.getPassed());
			if (!hasGrade) {
				mainVC.contextPut("passedCutValue", AssessmentHelper.getRoundedScore(assessmentConfig.getCutValue()));
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
			if (comment != null && comment.length() > 0) {
				mainVC.contextPut("comment", StringHelper.xssScan(comment));
				mainVC.contextPut("incomment", isPanelOpen(ureq, "comment", true));
			}
		}
		
		// Assessment documents
		if (assessmentConfig.hasIndividualAsssessmentDocuments()) {
			List<File> docs = assessmentDocumentsSupplier.getIndividualAssessmentDocuments();
			mapperUri = registerCacheableMapper(ureq, null, new DocumentsMapper(docs));
			mainVC.contextPut("docsMapperUri", mapperUri);
			mainVC.contextPut("docs", docs);
			mainVC.contextPut("inassessmentDocuments", isPanelOpen(ureq, "assessmentDocuments", true));
			if (assessmentDocumentsSupplier.isDownloadEnabled() && download == null) {
				download = new DisplayOrDownloadComponent("", null);
				mainVC.put("download", download);
			}
		}
	}
	
	public void setCustomFields(Component customFields) {
		if (customFields != null) {
			mainVC.put("customFields", customFields);
		} else {
			mainVC.remove("customFields");
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
	public void event(UserRequest ureq, Component source, Event event) {
		if ("show".equals(event.getCommand())) {
			saveOpenPanel(ureq, ureq.getParameter("panel"), true);
		} else if ("hide".equals(event.getCommand())) {
			saveOpenPanel(ureq, ureq.getParameter("panel"), false);
		}
	}
	
	private boolean isPanelOpen(UserRequest ureq, String panelId, boolean showDefault) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		Boolean showConfig  = (Boolean) guiPrefs.get(panelInfo.getAttributedClass(), getOpenPanelId(panelId));
		return showConfig == null ? showDefault : showConfig.booleanValue();
	}
	
	private void saveOpenPanel(UserRequest ureq, String panelId, boolean newValue) {
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		if (guiPrefs != null) {
			guiPrefs.putAndSave(panelInfo.getAttributedClass(), getOpenPanelId(panelId), Boolean.valueOf(newValue));
		}
	}
	
	private String getOpenPanelId(String panelId) {
		return panelId + panelInfo.getIdSuffix();
	}
	
	public static final class PanelInfo {
		
		private final Class<?> attributedClass;
		private final String idSuffix;
		
		public PanelInfo(Class<?> attributedClass, String idSuffix) {
			this.attributedClass = attributedClass;
			this.idSuffix = idSuffix;
		}
		
		public Class<?> getAttributedClass() {
			return attributedClass;
		}
		
		public String getIdSuffix() {
			return idSuffix;
		}
		
	}
	
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
		
		public List<File> getIndividualAssessmentDocuments();
		
		public boolean isDownloadEnabled();
		
	}

}
