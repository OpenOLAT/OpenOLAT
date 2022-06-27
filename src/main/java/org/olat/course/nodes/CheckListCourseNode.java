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
package org.olat.course.nodes;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.zip.ZipOutputStream;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.course.CourseEntryRef;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.PublishEvents;
import org.olat.course.editor.StatusDescription;
import org.olat.course.editor.importnodes.ImportSettings;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.learningpath.ui.TabbableLeaningPathNodeConfigController;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.cl.CLLearningPathNodeHandler;
import org.olat.course.nodes.cl.CheckListAssessmentConfig;
import org.olat.course.nodes.cl.CheckboxManager;
import org.olat.course.nodes.cl.model.Checkbox;
import org.olat.course.nodes.cl.model.CheckboxList;
import org.olat.course.nodes.cl.ui.CheckListCoachRunController;
import org.olat.course.nodes.cl.ui.CheckListEditController;
import org.olat.course.nodes.cl.ui.CheckListExcelExport;
import org.olat.course.nodes.cl.ui.CheckListRunController;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.reminder.AssessmentReminderProvider;
import org.olat.course.reminder.CourseNodeReminderProvider;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.course.run.userview.VisibilityFilter;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeScoreRange;
import org.olat.modules.grade.GradeService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;

/**
 * 
 * Initial date: 06.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckListCourseNode extends AbstractAccessableCourseNode {
	
	private static final String PACKAGE_CL = Util.getPackageName(CheckListEditController.class);
	private static final long serialVersionUID = -7460670977531082040L;
	
	public static final String TYPE = "checklist";
	private static final String ICON_CSS_CLASS = "o_cl_icon";
	
	public static final String CONFIG_KEY_PASSED_SUM_CHECKBOX = "passedSumCheckbox";
	public static final String CONFIG_KEY_PASSED_SUM_CUTVALUE = "passedSumCutValue";
	public static final String CONFIG_KEY_PASSED_MANUAL_CORRECTION = "passedManualCorrection";
	
	public static final String CONFIG_KEY_DUE_DATE = "dueDate";
	public static final String CONFIG_KEY_CLOSE_AFTER_DUE_DATE = "closeAfterDueDate";
	
	public static final String CONFIG_KEY_CHECKBOX = "checkbox";
	
	public static final String FOLDER_NAME = "checklistfiles";

	public CheckListCourseNode() {
		super(TYPE);
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		CheckListEditController childTabCntrllr = new CheckListEditController(this, ureq, wControl, course);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, euce, childTabCntrllr);
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			final UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd, VisibilityFilter visibilityFilter) {
		Controller ctrl;
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("CourseModule", userCourseEnv.getCourseEnvironment().getCourseResourceableId());
		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isGuestOnly()) {
			Translator trans = Util.createPackageTranslator(CheckListCourseNode.class, ureq.getLocale());
			String title = trans.translate("guestnoaccess.title");
			String message = trans.translate("guestnoaccess.message");
			ctrl = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		} else if(userCourseEnv.isCoach() || userCourseEnv.isAdmin()) {
			ctrl = new CheckListCoachRunController(ureq, wControl, userCourseEnv, ores, this);
		} else {
			ctrl = new CheckListRunController(ureq, wControl, userCourseEnv, ores, this, false);
		}

		Controller cont = TitledWrapperHelper.getWrapper(ureq, wControl, ctrl, userCourseEnv, this, ICON_CSS_CLASS);
		return new NodeRunConstructionResult(cont);
	}
	
	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback) {
		return createNodeRunConstructionResult(ureq, wControl, userCourseEnv, nodeSecCallback, null, null).getRunController();
	}

	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null && oneClickStatusCache.length > 0) {
			return oneClickStatusCache[0];
		}
		
		List<StatusDescription> statusDescs = validateInternalConfiguration(null);
		if(statusDescs.isEmpty()) {
			statusDescs.add(StatusDescription.NOERROR);
		}
		oneClickStatusCache = StatusDescriptionHelper.sort(statusDescs);
		return oneClickStatusCache[0];
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;
		// only here we know which translator to take for translating condition
		// error messages
		List<StatusDescription> sds = isConfigValidWithTranslator(cev, PACKAGE_CL, getConditionExpressions());
		if(oneClickStatusCache != null && oneClickStatusCache.length > 0) {
			//isConfigValidWithTranslator add first
			sds.remove(oneClickStatusCache[0]);
		}
		sds.addAll(validateInternalConfiguration(cev));
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
	}
	
	private List<StatusDescription> validateInternalConfiguration(CourseEditorEnv cev) {
		List<StatusDescription> sdList = new ArrayList<>(5);

		ModuleConfiguration config = getModuleConfiguration();
		
		Boolean hasScore = (Boolean)config.get(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD);
		if ((hasScore == null || hasScore.booleanValue()) &&
				(config.get(MSCourseNode.CONFIG_KEY_SCORE_MIN) == null || config.get(MSCourseNode.CONFIG_KEY_SCORE_MAX) == null)) {
			addStatusErrorDescription("error.missing.score.config", CheckListEditController.PANE_TAB_CLCONFIG, sdList);
		}
		
		Boolean hasPassed = (Boolean)config.get(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD);
		if (hasPassed == null || hasPassed.booleanValue()) {
			
			Boolean passedSum = (Boolean)config.get(CheckListCourseNode.CONFIG_KEY_PASSED_SUM_CHECKBOX);
			Boolean manualCorr = (Boolean)config.get(CheckListCourseNode.CONFIG_KEY_PASSED_MANUAL_CORRECTION);
			boolean grade = config.getBooleanSafe(MSCourseNode.CONFIG_KEY_GRADE_ENABLED);
			if((manualCorr == null || !manualCorr.booleanValue()) && !grade && (passedSum == null || !passedSum.booleanValue())
					&& config.get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE) == null) {
				addStatusErrorDescription("error.missing.cutvalue.config", CheckListEditController.PANE_TAB_CLCONFIG, sdList);	
			}
		}
		
		if (cev != null) {
			CheckListAssessmentConfig assessmentConfig = new CheckListAssessmentConfig(new CourseEntryRef(cev), this);
			
			if (isFullyAssessedScoreConfigError(assessmentConfig)) {
				addStatusErrorDescription("error.fully.assessed.score",
						TabbableLeaningPathNodeConfigController.PANE_TAB_LEARNING_PATH, sdList);
			}
			if (isFullyAssessedPassedConfigError(assessmentConfig)) {
				addStatusErrorDescription("error.fully.assessed.passed",
						TabbableLeaningPathNodeConfigController.PANE_TAB_LEARNING_PATH, sdList);
			}
			
			if (config.getBooleanSafe(MSCourseNode.CONFIG_KEY_GRADE_ENABLED) && CoreSpringFactory.getImpl(GradeModule.class).isEnabled()) {
				GradeService gradeService = CoreSpringFactory.getImpl(GradeService.class);
				GradeScale gradeScale = gradeService.getGradeScale(cev.getCourseGroupManager().getCourseEntry(), getIdent());
				if (gradeScale == null) {
					addStatusErrorDescription("error.missing.grade.scale", CheckListEditController.PANE_TAB_CLCONFIG, sdList);
				}
			}
		}
		
		return sdList;
	}
	
	private boolean isFullyAssessedScoreConfigError(CheckListAssessmentConfig assessmentConfig) {
		boolean hasScore = Mode.none != assessmentConfig.getScoreMode();
		boolean isScoreTrigger = CoreSpringFactory.getImpl(CLLearningPathNodeHandler.class)
				.getConfigs(this)
				.isFullyAssessedOnScore(null, null)
				.isEnabled();
		return isScoreTrigger && !hasScore;
	}
	
	private boolean isFullyAssessedPassedConfigError(CheckListAssessmentConfig assessmentConfig) {
		boolean hasPassed = assessmentConfig.getPassedMode() != Mode.none;
		boolean isPassedTrigger = CoreSpringFactory.getImpl(CLLearningPathNodeHandler.class)
				.getConfigs(this)
				.isFullyAssessedOnPassed(null, null)
				.isEnabled();
		return isPassedTrigger && !hasPassed;
	}
	
	private void addStatusErrorDescription(String key, String pane, List<StatusDescription> status) {
		String[] params = new String[] { getShortTitle() };
		StatusDescription sd = new StatusDescription(StatusDescription.ERROR, key, key, params, PACKAGE_CL);
		sd.setDescriptionForUnit(getIdent());
		sd.setActivateableViewIdentifier(pane);
		status.add(sd);
	}

	@Override
	public List<StatusDescription> publishUpdatesExplanations(CourseEditorEnv cev) {
		List<StatusDescription> statusDescs = new ArrayList<>();
		StatusDescription statusDesc1 = new StatusDescription(Level.INFO, "checklist.update.assessment", null, null, PACKAGE_CL);
		statusDesc1.setDescriptionForUnit(getIdent());
		statusDescs.add(statusDesc1);
		StatusDescription statusDesc2 = new StatusDescription(Level.INFO, "checklist.update.efficiencystatements", null, null, PACKAGE_CL);
		statusDesc2.setDescriptionForUnit(getIdent());
		statusDescs.add(statusDesc2);
		return statusDescs;
	}

	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}

	/**
	 * Make an archive of all datas.
	 */
	@Override
	public boolean archiveNodeData(Locale locale, ICourse course, ArchiveOptions options,
			ZipOutputStream exportStream, String archivePath, String charset) {
		
		String dirName;
		if(StringHelper.containsNonWhitespace(archivePath)) {
			dirName = archivePath;
		} else {
			dirName = "cl_"
					+ StringHelper.transformDisplayNameToFileSystemName(getShortName())
					+ "_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()));
		}
		
		ModuleConfiguration config = getModuleConfiguration();
		CheckboxList list = (CheckboxList)config.get(CONFIG_KEY_CHECKBOX);
		CheckboxManager checkboxManager = CoreSpringFactory.getImpl(CheckboxManager.class);
		if(list != null && list.getList() != null) {
			Set<String> usedNames = new HashSet<>();
			
			for(Checkbox checkbox:list.getList()) {
				VFSContainer dir = checkboxManager.getFileContainer(course.getCourseEnvironment(), this);
				if(dir != null) {
					VFSItem item = dir.resolve(checkbox.getFilename());
					if(item instanceof VFSLeaf) {
						String path = dirName + "/" + Formatter.makeStringFilesystemSave(checkbox.getTitle());
						if(usedNames.contains(checkbox.getTitle())) {
							path += "_" + checkbox.getCheckboxId();
						} else {
							usedNames.add(checkbox.getTitle());
						}
						ZipUtil.addToZip(item, path, exportStream, new VFSSystemItemFilter(), false);
					}
				}
			}
		}

		String filename = dirName + "/" + StringHelper.transformDisplayNameToFileSystemName(getShortName());
		new CheckListExcelExport(this, course, locale).exportAll(filename, exportStream);
		
		//assessment documents
		AssessmentConfig assessmentConfig = new CheckListAssessmentConfig(new CourseEntryRef(course), this);
		if(assessmentConfig.hasIndividualAsssessmentDocuments()) {
			List<AssessmentEntry> assessmentEntries = course.getCourseEnvironment()
					.getAssessmentManager().getAssessmentEntries(this);
			if(assessmentEntries != null && !assessmentEntries.isEmpty()) {
				String assessmentDirName = dirName + "/Assessment_documents";
				for(AssessmentEntry assessmentEntry:assessmentEntries) {
					Identity assessedIdentity = assessmentEntry.getIdentity();
					List<File> assessmentDocuments = course.getCourseEnvironment()
							.getAssessmentManager().getIndividualAssessmentDocuments(this, assessedIdentity);
					
					String name = assessedIdentity.getUser().getLastName()
							+ "_" + assessedIdentity.getUser().getFirstName()
							+ "_" + assessedIdentity.getName();
					String userDirName = assessmentDirName + "/" + StringHelper.transformDisplayNameToFileSystemName(name);
					if(assessmentDocuments != null && !assessmentDocuments.isEmpty()) {
						for(File document:assessmentDocuments) {
							String path = userDirName + "/" + document.getName(); 
							ZipUtil.addFileToZip(path, document, exportStream);
						}
					}
				}
			}
		}

		return super.archiveNodeData(locale, course, options, exportStream, archivePath, charset);
	}

	@Override
	public void exportNode(File fExportDirectory, ICourse course) {
		//export the files
		CheckboxManager checkboxManager = CoreSpringFactory.getImpl(CheckboxManager.class);
		ModuleConfiguration config = getModuleConfiguration();
		CheckboxList list = (CheckboxList)config.get(CONFIG_KEY_CHECKBOX);
		if(list != null && list.getList() != null) {
			for(Checkbox checkbox:list.getList()) {
				File dir = checkboxManager.getFileDirectory(course.getCourseEnvironment(), this);
				if(dir.exists()) {
					File fFileExportDir = new File(fExportDirectory, "checklistfiles/" + getIdent() + "/" + checkbox.getCheckboxId());
					fFileExportDir.mkdirs();
					FileUtils.copyDirContentsToDir(dir, fFileExportDir, false, "export files of checkbox");
				}
			}
		}
	}

	@Override
	public void importNode(File importDirectory, ICourse course, Identity owner, Organisation organisation, Locale locale, boolean withReferences) {
		CheckboxManager checkboxManager = CoreSpringFactory.getImpl(CheckboxManager.class);
		ModuleConfiguration config = getModuleConfiguration();
		CheckboxList list = (CheckboxList)config.get(CONFIG_KEY_CHECKBOX);
		if(list != null && list.getList() != null) {
			for(Checkbox checkbox:list.getList()) {
				File fFileImportDir = new File(importDirectory, "checklistfiles/" + getIdent() + "/" + checkbox.getCheckboxId());
				
				String newCheckboxId = UUID.randomUUID().toString();
				checkbox.setCheckboxId(newCheckboxId);
				if(fFileImportDir.exists()) {
					File dir = checkboxManager.getFileDirectory(course.getCourseEnvironment(), this);
					dir.mkdirs();
					FileUtils.copyDirContentsToDir(fFileImportDir, dir, false, "import file of checkbox");
				}
			}
		}
	}

	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);
		CoreSpringFactory.getImpl(CheckboxManager.class).deleteCheckbox(course, getIdent());
		
		// Delete GradeScales
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		CoreSpringFactory.getImpl(GradeService.class).deleteGradeScale(entry, getIdent());
	}
	
	/**
	 * The user course environment must be the coach or the user which preceed to
	 * the changes.
	 * @param userCourseEnv
	 * @param assessedIdentity
	 */
	public void updateScoreEvaluation(Identity identity, UserCourseEnvironment assessedUserCourseEnv, Identity assessedIdentity, Role by, Locale locale) {
		ModuleConfiguration config = getModuleConfiguration();
		Boolean scoreField = (Boolean)config.get(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD);
		Boolean sum = (Boolean)config.get(CheckListCourseNode.CONFIG_KEY_PASSED_SUM_CHECKBOX);
		boolean grade = config.getBooleanSafe(MSCourseNode.CONFIG_KEY_GRADE_ENABLED);
		Float cutValue = (Float)config.get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
		Float maxScore = (Float)config.get(MSCourseNode.CONFIG_KEY_SCORE_MAX);
		Boolean manualCorrection = (Boolean)config.get(CheckListCourseNode.CONFIG_KEY_PASSED_MANUAL_CORRECTION);
		if (grade && CoreSpringFactory.getImpl(GradeModule.class).isEnabled()) {
			doUpdateAssessmentByGrade(maxScore, identity, assessedUserCourseEnv, assessedIdentity, by, locale);
		} else if(cutValue != null) {
			doUpdateAssessment(cutValue, maxScore, identity, assessedUserCourseEnv, assessedIdentity, by);
		} else if(sum != null && sum.booleanValue()) {
			doUpdateAssessmentBySum(identity, assessedUserCourseEnv, assessedIdentity, by);
		} else if(manualCorrection != null && manualCorrection.booleanValue()) {
			doUpdateScoreOnly(maxScore, identity, assessedUserCourseEnv, assessedIdentity, by);
		} else if(scoreField != null && scoreField.booleanValue()) {
			doUpdateScoreOnly(maxScore, identity, assessedUserCourseEnv, assessedIdentity, by);
		} else {
			AssessmentManager am = assessedUserCourseEnv.getCourseEnvironment().getAssessmentManager();
			am.updateLastModifications(this, assessedIdentity, assessedUserCourseEnv, by);
		}
	}
	
	private void doUpdateAssessmentByGrade(Float maxScore, Identity identity,
			UserCourseEnvironment assessedUserCourseEnv, Identity assessedIdentity, Role by, Locale locale) {
		OLATResourceable courseOres = OresHelper
				.createOLATResourceableInstance("CourseModule", assessedUserCourseEnv.getCourseEnvironment().getCourseResourceableId());
		
		CheckboxManager checkboxManager = CoreSpringFactory.getImpl(CheckboxManager.class);
		float score = checkboxManager.calculateScore(assessedIdentity, courseOres, getIdent());
		if(maxScore != null && maxScore.floatValue() < score) {
			score = maxScore.floatValue();
		}
		
		ScoreEvaluation scoreEval = CoreSpringFactory.getImpl(CourseAssessmentService.class).getAssessmentEvaluation(this, assessedUserCourseEnv);
		String grade = null;
		String gradeSystemIdent = null;
		String performanceClassIdent = null;
		Boolean passed = null;
		if (getModuleConfiguration().getBooleanSafe(MSCourseNode.CONFIG_KEY_GRADE_AUTO) || (scoreEval != null && StringHelper.containsNonWhitespace(scoreEval.getGrade()))) {
			GradeService gradeService = CoreSpringFactory.getImpl(GradeService.class);
			GradeScale gradeScale = gradeService.getGradeScale(
					assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), getIdent());
			NavigableSet<GradeScoreRange> gradeScoreRanges = gradeService.getGradeScoreRanges(gradeScale, locale);
			GradeScoreRange gradeScoreRange = gradeService.getGradeScoreRange(gradeScoreRanges, score);
			grade = gradeScoreRange.getGrade();
			gradeSystemIdent = gradeScoreRange.getGradeSystemIdent();
			performanceClassIdent = gradeScoreRange.getPerformanceClassIdent();
			passed = gradeScoreRange.getPassed();
		}
		AssessmentEntryStatus status = getStartedStatus(scoreEval);
		
		ScoreEvaluation sceval = new ScoreEvaluation(Float.valueOf(score), grade, gradeSystemIdent, performanceClassIdent, passed, status, null, null, null, null, null);
		CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		courseAssessmentService.saveScoreEvaluation(this, identity, sceval, assessedUserCourseEnv, false, by);
	}

	private void doUpdateAssessment(Float cutValue, Float maxScore, Identity identity, UserCourseEnvironment assessedUserCourseEnv, Identity assessedIdentity, Role by) {
		OLATResourceable courseOres = OresHelper
				.createOLATResourceableInstance("CourseModule", assessedUserCourseEnv.getCourseEnvironment().getCourseResourceableId());
		
		CheckboxManager checkboxManager = CoreSpringFactory.getImpl(CheckboxManager.class);
		float score = checkboxManager.calculateScore(assessedIdentity, courseOres, getIdent());
		if(maxScore != null && maxScore.floatValue() < score) {
			score = maxScore.floatValue();
		}
		
		Boolean passed = null;
		if(cutValue != null) {
			boolean aboveCutValue = score >= cutValue.floatValue();
			passed = Boolean.valueOf(aboveCutValue);
		}
		ScoreEvaluation scoreEval = CoreSpringFactory.getImpl(CourseAssessmentService.class).getAssessmentEvaluation(this, assessedUserCourseEnv);
		AssessmentEntryStatus status = getStartedStatus(scoreEval);
		ScoreEvaluation sceval = new ScoreEvaluation(Float.valueOf(score), null, null, null, passed, status, null, null, null, null, null);
		
		CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		courseAssessmentService.saveScoreEvaluation(this, identity, sceval, assessedUserCourseEnv, false, by);
	}
	
	private void doUpdateAssessmentBySum(Identity identity, UserCourseEnvironment assessedUserCourseEnv, Identity assessedIdentity, Role by) {
		OLATResourceable courseOres = OresHelper
				.createOLATResourceableInstance("CourseModule", assessedUserCourseEnv.getCourseEnvironment().getCourseResourceableId());
		
		ModuleConfiguration config = getModuleConfiguration();
		CheckboxManager checkboxManager = CoreSpringFactory.getImpl(CheckboxManager.class);
		int checkedBox = checkboxManager.countChecked(assessedIdentity, courseOres, getIdent());
		CheckboxList checkboxList = (CheckboxList)config.get(CONFIG_KEY_CHECKBOX);
		Integer cut = (Integer)config.get(CheckListCourseNode.CONFIG_KEY_PASSED_SUM_CUTVALUE);
		int minNumOfCheckbox = cut == null ? checkboxList.getNumOfCheckbox() : cut.intValue();
		boolean passed = checkedBox >= minNumOfCheckbox;
		
		Float score = null;
		if(passed) {
			Boolean scoreGrantedBool = (Boolean)config.get(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD);
			if(scoreGrantedBool != null && scoreGrantedBool.booleanValue()) {
				score = checkboxManager.calculateScore(assessedIdentity, courseOres, getIdent());
				Float maxScore = (Float)config.get(MSCourseNode.CONFIG_KEY_SCORE_MAX);
				if(maxScore != null && maxScore.floatValue() < score) {
					score = maxScore.floatValue();
				}
			}
		}
		ScoreEvaluation scoreEval = CoreSpringFactory.getImpl(CourseAssessmentService.class).getAssessmentEvaluation(this, assessedUserCourseEnv);
		AssessmentEntryStatus status = getStartedStatus(scoreEval);
		
		ScoreEvaluation sceval = new ScoreEvaluation(score, null, null, null, Boolean.valueOf(passed), status, null, null, null, null, null);
		
		CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		courseAssessmentService.saveScoreEvaluation(this, identity, sceval, assessedUserCourseEnv, false, by);
	}
	
	private void doUpdateScoreOnly(Float maxScore, Identity identity, UserCourseEnvironment assessedUserCourseEnv, Identity assessedIdentity, Role by) {
		OLATResourceable courseOres = OresHelper
				.createOLATResourceableInstance("CourseModule", assessedUserCourseEnv.getCourseEnvironment().getCourseResourceableId());
		
		CheckboxManager checkboxManager = CoreSpringFactory.getImpl(CheckboxManager.class);
		float score = checkboxManager.calculateScore(assessedIdentity, courseOres, getIdent());
		if(maxScore != null && maxScore.floatValue() < score) {
			score = maxScore.floatValue();
		}
		ScoreEvaluation scoreEval = CoreSpringFactory.getImpl(CourseAssessmentService.class).getAssessmentEvaluation(this, assessedUserCourseEnv);
		AssessmentEntryStatus status = getStartedStatus(scoreEval);

		CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		ScoreEvaluation currentEval = courseAssessmentService.getAssessmentEvaluation(this, assessedUserCourseEnv);
		ScoreEvaluation sceval = new ScoreEvaluation(Float.valueOf(score), currentEval.getGrade(),
				currentEval.getGradeSystemIdent(), currentEval.getPerformanceClassIdent(), currentEval.getPassed(),
				status, currentEval.getUserVisible(), currentEval.getCurrentRunStartDate(),
				currentEval.getCurrentRunCompletion(), currentEval.getCurrentRunStatus(),
				currentEval.getAssessmentID());
		courseAssessmentService.saveScoreEvaluation(this, identity, sceval, assessedUserCourseEnv, false, by);
	}
	
	private AssessmentEntryStatus getStartedStatus(ScoreEvaluation scoreEval) {
		return scoreEval.getAssessmentStatus() == null
				|| scoreEval.getAssessmentStatus() == AssessmentEntryStatus.notReady
				|| scoreEval.getAssessmentStatus() == AssessmentEntryStatus.notStarted
				? AssessmentEntryStatus.inProgress
				: scoreEval.getAssessmentStatus();
	}
	
	@Override
	public CourseNode createInstanceForCopy(boolean isNewTitle, ICourse course, Identity author) {
		CheckListCourseNode cNode = (CheckListCourseNode)super.createInstanceForCopy(isNewTitle, course, author);
		copy(this, cNode, course, course);
		return cNode;
	}

	@Override
	public void postImportCourseNodes(ICourse course, CourseNode sourceCourseNode, ICourse sourceCourse, ImportSettings settings, CourseEnvironmentMapper envMapper) {
		super.postImportCourseNodes(course, sourceCourseNode, sourceCourse, settings, envMapper);
		copy((CheckListCourseNode)sourceCourseNode, this, sourceCourse, course);
	}

	private static void copy(CheckListCourseNode sourceCourseNode, CheckListCourseNode targetCourseNode, ICourse sourceCourse, ICourse targetCourse) {
		CheckboxManager checkboxManager = CoreSpringFactory.getImpl(CheckboxManager.class);
		CheckboxList list = (CheckboxList)targetCourseNode.getModuleConfiguration().get(CONFIG_KEY_CHECKBOX);
		if (list != null) {
			for(Checkbox checkbox:list.getList()) {
				checkbox.setCheckboxId(UUID.randomUUID().toString());
			}
		}
		
		// the ident of the course node is the same
		File sourceDir = checkboxManager.getFileDirectory(sourceCourse.getCourseEnvironment(), sourceCourseNode);
		if(sourceDir.exists()) {
			File targetDir = checkboxManager.getFileDirectory(targetCourse.getCourseEnvironment(), targetCourseNode);
			if(!targetDir.exists()) {
				targetDir.mkdirs();
				FileUtils.copyDirContentsToDir(sourceDir, targetDir, false, "copy files of checkbox");
			}
		}
	}

	@Override
	public void postCopy(CourseEnvironmentMapper envMapper, Processing processType, ICourse course, ICourse sourceCourse, CopyCourseContext context) {
		ModuleConfiguration config = getModuleConfiguration();
		CheckboxManager checkboxManager = CoreSpringFactory.getImpl(CheckboxManager.class);
		CheckboxList list = (CheckboxList)config.get(CONFIG_KEY_CHECKBOX);
		if (list !=null && list.getList().size() > 0) {
			for(Checkbox checkbox:list.getList()) {
				String sourceId = checkbox.getCheckboxId();
				String targetId = envMapper.getTargetUniqueKey(getIdent(), sourceId);
				if(targetId == null) {
					targetId = UUID.randomUUID().toString();
					envMapper.addUniqueKeyPair(getIdent(), sourceId, targetId);
				}
				checkbox.setCheckboxId(targetId);
			}
		}
		
		// the ident of the course node is the same
		File sourceDir = checkboxManager.getFileDirectory(sourceCourse.getCourseEnvironment(), this);
		if(sourceDir.exists()) {
			File targetDir = checkboxManager.getFileDirectory(course.getCourseEnvironment(), this);
			if(!targetDir.exists()) {
				targetDir.mkdirs();
				FileUtils.copyDirContentsToDir(sourceDir, targetDir, false, "copy files of checkbox");
			}
		}
		
		if (context != null) {
			// Move dates
			Date dueDate = config.getDateValue(CONFIG_KEY_DUE_DATE);
			Date closeAfterDueDate = config.getDateValue(CONFIG_KEY_CLOSE_AFTER_DUE_DATE);
			
			long dateDifference = context.getDateDifference(getIdent());
			
			if (dueDate != null) {
				dueDate.setTime(dueDate.getTime() + dateDifference);
				config.set(CONFIG_KEY_DUE_DATE, dueDate);
			}
			
			if (closeAfterDueDate != null) {
				closeAfterDueDate.setTime(closeAfterDueDate.getTime() + dateDifference);
				config.set(CONFIG_KEY_CLOSE_AFTER_DUE_DATE, closeAfterDueDate);
			}
		}
		
		checkboxManager.syncCheckbox(list, course, getIdent());
		super.postCopy(envMapper, processType, course, sourceCourse, context);
	}

	@Override
	public void updateOnPublish(Locale locale, ICourse course, Identity publisher, PublishEvents publishEvents) {
		ModuleConfiguration config = getModuleConfiguration();
		//sync the checkbox with the database
		CheckboxList list = (CheckboxList)config.get(CONFIG_KEY_CHECKBOX);
		CheckboxManager checkboxManager = CoreSpringFactory.getImpl(CheckboxManager.class);
		checkboxManager.syncCheckbox(list, course, getIdent());

		CoursePropertyManager pm = course.getCourseEnvironment().getCoursePropertyManager();
		List<Identity> assessedUsers = pm.getAllIdentitiesWithCourseAssessmentData(null);

		int count = 0;
		for(Identity assessedIdentity: assessedUsers) {
			updateScorePassedOnPublish(assessedIdentity, publisher, checkboxManager, course, locale);
			if(++count % 10 == 0) {
				DBFactory.getInstance().commitAndCloseSession();
			}
		}
		DBFactory.getInstance().commitAndCloseSession();
		super.updateOnPublish(locale, course, publisher, publishEvents);
	}
	
	private void updateScorePassedOnPublish(Identity assessedIdentity, Identity coachIdentity, CheckboxManager checkboxManager, ICourse course, Locale locale) {
		AssessmentManager am = course.getCourseEnvironment().getAssessmentManager();
		
		Float currentScore = null;
		String currentGrade = null;
		String currentGradeSystemIdent = null;
		String currentPerformanceClassIdent = null;
		Boolean currentPassed = null;
		AssessmentEntry ae = am.getAssessmentEntry(this, assessedIdentity);
		if(ae != null) {
			currentScore = ae.getScore() == null ? null : ae.getScore().floatValue();
			currentGrade = ae.getGrade();
			currentGradeSystemIdent = ae.getGradeSystemIdent();
			currentPerformanceClassIdent = ae.getPerformanceClassIdent();
			currentPassed = ae.getPassed();
		}
		
		Float updatedScore = null;
		String updateGrade = null;
		String updateGradeSystemIdent = null;
		String updatePerformanceClassIdent = null;
		Boolean updatedPassed = null;

		ModuleConfiguration config = getModuleConfiguration();
		Boolean scoreGrantedBool = (Boolean)config.get(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD);
		if(scoreGrantedBool != null && scoreGrantedBool.booleanValue()) {
			updatedScore = checkboxManager.calculateScore(assessedIdentity, course, getIdent());
		}
		
		Boolean passedBool = (Boolean)config.get(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD);
		if(passedBool != null && passedBool.booleanValue()) {
			Float cutValue = (Float)config.get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
			Boolean sumCheckbox = (Boolean)config.get(CheckListCourseNode.CONFIG_KEY_PASSED_SUM_CHECKBOX);
			boolean gradeEnabled = config.getBooleanSafe(MSCourseNode.CONFIG_KEY_GRADE_ENABLED);
			if (gradeEnabled && CoreSpringFactory.getImpl(GradeModule.class).isEnabled()) {
				if (updatedScore != null && (getModuleConfiguration().getBooleanSafe(MSCourseNode.CONFIG_KEY_GRADE_AUTO) || StringHelper.containsNonWhitespace(currentGrade))) {
					GradeService gradeService = CoreSpringFactory.getImpl(GradeService.class);
					GradeScale gradeScale = gradeService.getGradeScale(
							course.getCourseEnvironment().getCourseGroupManager().getCourseEntry(), getIdent());
					NavigableSet<GradeScoreRange> gradeScoreRanges = gradeService.getGradeScoreRanges(gradeScale, locale);
					GradeScoreRange gradeScoreRange = gradeService.getGradeScoreRange(gradeScoreRanges, updatedScore);
					updateGrade = gradeScoreRange.getGrade();
					updateGradeSystemIdent = gradeScoreRange.getGradeSystemIdent();
					updatePerformanceClassIdent = gradeScoreRange.getPerformanceClassIdent();
					updatedPassed = gradeScoreRange.getPassed();
				}
			} else if(sumCheckbox != null && sumCheckbox.booleanValue()) {
				Integer minValue = (Integer)config.get(CheckListCourseNode.CONFIG_KEY_PASSED_SUM_CUTVALUE);
				int checkedBox = checkboxManager.countChecked(assessedIdentity, course, getIdent());
				if(minValue != null && minValue.intValue() <= checkedBox) {
					updatedPassed = Boolean.TRUE;
				} else {
					updatedPassed = Boolean.FALSE;
				}

			} else if (cutValue != null) {
				if(updatedScore != null && cutValue.floatValue() <= updatedScore.floatValue()) {
					updatedPassed = Boolean.TRUE;
				} else {
					updatedPassed = Boolean.FALSE;
				}
			}
		}
		
		boolean needUpdate = false;
		
		Boolean manualCorrection = (Boolean)config.get(CheckListCourseNode.CONFIG_KEY_PASSED_MANUAL_CORRECTION);
		if(manualCorrection == null || !manualCorrection.booleanValue()) {
			//update passed
			if((currentPassed == null && updatedPassed != null && updatedScore != null && updatedScore.floatValue() > 0f)
					|| (currentPassed != null && updatedPassed == null)
					|| (currentPassed != null && !currentPassed.equals(updatedPassed))) {
				needUpdate = true;	
			}
		}
		
		if((currentScore == null && updatedScore != null && updatedScore.floatValue() > 0f)
				|| (currentScore != null && updatedScore == null)
				|| (currentScore != null && !currentScore.equals(updatedScore))) {
			needUpdate = true;	
		}
		
		needUpdate = needUpdate
				|| !Objects.equals(updateGrade, currentGrade)
				|| !Objects.equals(updateGradeSystemIdent, currentGradeSystemIdent)
				|| !Objects.equals(updatePerformanceClassIdent, currentPerformanceClassIdent);
		
		if(needUpdate) {
			ScoreEvaluation scoreEval = new ScoreEvaluation(updatedScore, updateGrade, updateGradeSystemIdent,
					updatePerformanceClassIdent, updatedPassed, null, null, null, null, null, null);
			IdentityEnvironment identityEnv = new IdentityEnvironment(assessedIdentity, null);
			UserCourseEnvironment uce = new UserCourseEnvironmentImpl(identityEnv, course.getCourseEnvironment());
			CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
			courseAssessmentService.saveScoreEvaluation(this, coachIdentity, scoreEval, uce, false, Role.coach);
		}
	}

	/**
	 * Update the module configuration to have all mandatory configuration flags
	 * set to usefull default values
	 * @param isNewNode true: an initial configuration is set; false: upgrading
	 *          from previous node configuration version, set default to maintain
	 *          previous behaviour
	 */
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent, NodeAccessType nodeAccessType) {
		super.updateModuleConfigDefaults(isNewNode, parent, nodeAccessType);
		
		ModuleConfiguration config = getModuleConfiguration();
		if (isNewNode) {
			config.setConfigurationVersion(1);
		} 
	}

	@Override
	public CourseNodeReminderProvider getReminderProvider(RepositoryEntryRef courseEntry, boolean rootNode) {
		return new AssessmentReminderProvider(getIdent(), new CheckListAssessmentConfig(courseEntry, this));
	}
	
	@Override
	public List<Entry<String, DueDateConfig>> getNodeSpecificDatesWithLabel() {
		return List.of(
				Map.entry("checklist.duedate", DueDateConfig.absolute(getModuleConfiguration().getDateValue(CONFIG_KEY_DUE_DATE))),
				Map.entry("checklist.close.after.duedate", DueDateConfig.absolute(getModuleConfiguration().getDateValue(CONFIG_KEY_CLOSE_AFTER_DUE_DATE)))
			);
	}
	
}