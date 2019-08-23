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
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.zip.ZipOutputStream;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
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
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.assessment.ui.tool.AssessmentCourseNodeController;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeController;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.PublishEvents;
import org.olat.course.editor.StatusDescription;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodes.cl.CheckboxManager;
import org.olat.course.nodes.cl.model.Checkbox;
import org.olat.course.nodes.cl.model.CheckboxList;
import org.olat.course.nodes.cl.ui.AssessedIdentityCheckListController;
import org.olat.course.nodes.cl.ui.CheckListEditController;
import org.olat.course.nodes.cl.ui.CheckListExcelExport;
import org.olat.course.nodes.cl.ui.CheckListRunController;
import org.olat.course.nodes.cl.ui.CheckListRunForCoachController;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroup;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 06.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CheckListCourseNode extends AbstractAccessableCourseNode implements PersistentAssessableCourseNode {
	
	private static final String PACKAGE_CL = Util.getPackageName(CheckListEditController.class);
	private static final long serialVersionUID = -7460670977531082040L;
	
	private static final String TYPE = "checklist";
	private static final String ICON_CSS_CLASS = "o_cl_icon";
	
	public static final String CONFIG_KEY_PASSED_SUM_CHECKBOX = "passedSumCheckbox";
	public static final String CONFIG_KEY_PASSED_SUM_CUTVALUE = "passedSumCutValue";
	public static final String CONFIG_KEY_PASSED_MANUAL_CORRECTION = "passedManualCorrection";
	
	public static final String CONFIG_KEY_DUE_DATE = "dueDate";
	public static final String CONFIG_KEY_CLOSE_AFTER_DUE_DATE = "closeAfterDueDate";
	
	public static final String CONFIG_KEY_CHECKBOX = "checkbox";
	
	public static final String FOLDER_NAME = "checklistfiles";

	/**
	 * Constructor for a course building block of the type structure
	 */
	public CheckListCourseNode() {
		super(TYPE);
		updateModuleConfigDefaults(true);
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		updateModuleConfigDefaults(false);
		// only the precondition "access" can be configured till now
		CheckListEditController childTabCntrllr = new CheckListEditController(this, ureq, wControl, course, euce);
		return new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, euce, childTabCntrllr);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			final UserCourseEnvironment userCourseEnv, NodeEvaluation ne, String nodecmd) {
		updateModuleConfigDefaults(false);

		Controller ctrl;
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("CourseModule", userCourseEnv.getCourseEnvironment().getCourseResourceableId());
		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isGuestOnly()) {
			Translator trans = Util.createPackageTranslator(CheckListCourseNode.class, ureq.getLocale());
			String title = trans.translate("guestnoaccess.title");
			String message = trans.translate("guestnoaccess.message");
			ctrl = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
		} else if(userCourseEnv.isCoach() || userCourseEnv.isAdmin()) {
			ctrl = new CheckListRunForCoachController(ureq, wControl, userCourseEnv, ores, this);
		} else {
			ctrl = new CheckListRunController(ureq, wControl, userCourseEnv, ores, this);
		}

		Controller cont = TitledWrapperHelper.getWrapper(ureq, wControl, ctrl, this, ICON_CSS_CLASS);
		return new NodeRunConstructionResult(cont);
	}
	
	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
		return createNodeRunConstructionResult(ureq, wControl, userCourseEnv, ne, null).getRunController();
	}

	/**
	 * the structure node does not have a score itself, but calculates the
	 * score/passed info by evaluating the configured expression in the the
	 * (condition)interpreter.
	 * 
	 * @see org.olat.course.nodes.AssessableCourseNode#getUserScoreEvaluation(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	@Override
	public AssessmentEvaluation getUserScoreEvaluation(UserCourseEnvironment userCourseEnv) {
		if(hasPassedConfigured() || hasScoreConfigured()) {
			return getUserScoreEvaluation(getUserAssessmentEntry(userCourseEnv));
		}
		return AssessmentEvaluation.EMPTY_EVAL;
	}
	
	@Override
	public AssessmentEvaluation getUserScoreEvaluation(AssessmentEntry entry) {
		return AssessmentEvaluation.toAssessmentEvalutation(entry, this);
	}

	@Override
	public AssessmentEntry getUserAssessmentEntry(UserCourseEnvironment userCourseEnv) {
		AssessmentManager am = userCourseEnv.getCourseEnvironment().getAssessmentManager();
		Identity mySelf = userCourseEnv.getIdentityEnvironment().getIdentity();
		return am.getAssessmentEntry(this, mySelf);
	}

	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null && oneClickStatusCache.length > 0) {
			return oneClickStatusCache[0];
		}
		
		List<StatusDescription> statusDescs = validateInternalConfiguration();
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
		sds.addAll(validateInternalConfiguration());
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
	}
	
	private List<StatusDescription> validateInternalConfiguration() {
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
			if((manualCorr == null || !manualCorr.booleanValue()) && (passedSum == null || !passedSum.booleanValue())
					&& config.get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE) == null) {
				addStatusErrorDescription("error.missing.cutvalue.config", CheckListEditController.PANE_TAB_CLCONFIG, sdList);	
			}
		}
		
		return sdList;
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
	
	@Override
	public boolean isAssessedBusinessGroups() {
		return false;
	}

	@Override
	public Float getCutValueConfiguration() {
		if (!hasPassedConfigured()) {
			throw new OLATRuntimeException(MSCourseNode.class, "getCutValue not defined when hasPassed set to false", null);
		}
		ModuleConfiguration config = getModuleConfiguration();
		return (Float)config.get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
	}

	@Override
	public Float getMaxScoreConfiguration() {
		if (!hasScoreConfigured()) {
			throw new OLATRuntimeException(MSCourseNode.class, "getMaxScore not defined when hasScore set to false", null);
		}
		ModuleConfiguration config = getModuleConfiguration();
		return (Float)config.get(MSCourseNode.CONFIG_KEY_SCORE_MAX);
	}

	@Override
	public Float getMinScoreConfiguration() {
		if (!hasScoreConfigured()) {
			throw new OLATRuntimeException(MSCourseNode.class, "getMinScore not defined when hasScore set to false", null);
		}
		ModuleConfiguration config = getModuleConfiguration();
		return (Float)config.get(MSCourseNode.CONFIG_KEY_SCORE_MIN);
	}

	@Override
	public String getUserCoachComment(UserCourseEnvironment userCourseEnvironment) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		return am.getNodeCoachComment(this, userCourseEnvironment.getIdentityEnvironment().getIdentity());
	}

	@Override
	public String getUserLog(UserCourseEnvironment userCourseEnvironment) {
		UserNodeAuditManager am = userCourseEnvironment.getCourseEnvironment().getAuditManager();
		return am.getUserNodeLog(this, userCourseEnvironment.getIdentityEnvironment().getIdentity());
	}

	@Override
	public String getUserUserComment(UserCourseEnvironment userCourseEnvironment) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		return am.getNodeComment(this, userCourseEnvironment.getIdentityEnvironment().getIdentity());
	}
	
	@Override
	public List<File> getIndividualAssessmentDocuments(UserCourseEnvironment userCourseEnvironment) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		return am.getIndividualAssessmentDocuments(this, mySelf);
	}

	@Override
	public boolean hasCommentConfigured() {
		return getModuleConfiguration().getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD, false);
	}
	
	@Override
	public boolean hasIndividualAsssessmentDocuments() {
		return getModuleConfiguration().getBooleanSafe(MSCourseNode.CONFIG_KEY_HAS_INDIVIDUAL_ASSESSMENT_DOCS, false);
	}

	@Override
	public boolean hasPassedConfigured() {
		ModuleConfiguration config = getModuleConfiguration();
		Boolean passed = (Boolean) config.get(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD);
		if (passed == null) return false;
		return passed.booleanValue();
	}

	@Override
	public boolean hasScoreConfigured() {
		ModuleConfiguration config = getModuleConfiguration();
		Boolean score = (Boolean)config.get(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD);
		if (score == null) return false;
		return score.booleanValue();
	}

	@Override
	public boolean hasStatusConfigured() {
		return false;
	}

	@Override
	public boolean isEditableConfigured() {
		return true;
	}

	@Override
	public void updateUserCoachComment(String coachComment, UserCourseEnvironment userCourseEnvironment) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		if (coachComment != null) {
			am.saveNodeCoachComment(this, mySelf, coachComment);
		}
	}

	@Override
	public void updateUserScoreEvaluation(ScoreEvaluation scoreEvaluation, UserCourseEnvironment userCourseEnvironment,
			Identity coachingIdentity, boolean incrementAttempts, Role by) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		am.saveScoreEvaluation(this, coachingIdentity, mySelf, new ScoreEvaluation(scoreEvaluation), userCourseEnvironment, incrementAttempts, by);
	}

	@Override
	public void updateUserUserComment(String userComment, UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity) {
		if (userComment != null) {
			AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
			Identity mySelf = userCourseEnvironment.getIdentityEnvironment().getIdentity();
			am.saveNodeComment(this, coachingIdentity, mySelf, userComment);
		}
	}
	
	@Override
	public void addIndividualAssessmentDocument(File document, String filename, UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity) {
		if(document != null) {
			AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
			Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
			am.addIndividualAssessmentDocument(this, coachingIdentity, assessedIdentity, document, filename);
		}
	}

	@Override
	public void removeIndividualAssessmentDocument(File document, UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity) {
		if(document != null) {
			AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
			Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
			am.removeIndividualAssessmentDocument(this, coachingIdentity, assessedIdentity, document);
		}
	}

	@Override
	public Integer getUserAttempts(UserCourseEnvironment userCourseEnvironment) {
		throw new OLATRuntimeException(CheckListCourseNode.class, "No attempts available in check list nodes", null);
	}

	@Override
	public boolean hasAttemptsConfigured() {
		return false;
	}

	@Override
	public void updateUserAttempts(Integer userAttempts, UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity, Role by) {
		throw new OLATRuntimeException(CheckListCourseNode.class, "Attempts variable can't be updated in ST nodes", null);
	}

	@Override
	public void incrementUserAttempts(UserCourseEnvironment userCourseEnvironment, Role by) {
		throw new OLATRuntimeException(CheckListCourseNode.class, "Attempts variable can't be updated in ST nodes", null);
	}
	
	@Override
	public boolean hasCompletion() {
		return false;
	}

	@Override
	public Double getUserCurrentRunCompletion(UserCourseEnvironment userCourseEnvironment) {
		throw new OLATRuntimeException(CheckListCourseNode.class, "No completion available in check list nodes", null);
	}
	
	@Override
	public void updateCurrentCompletion(UserCourseEnvironment userCourseEnvironment, Identity identity,
			Double currentCompletion, AssessmentRunStatus status, Role doneBy) {
		throw new OLATRuntimeException(CheckListCourseNode.class, "Completion variable can't be updated in check list nodes", null);
	}
	
	@Override
	public void updateLastModifications(UserCourseEnvironment userCourseEnvironment, Identity identity, Role by) {
		AssessmentManager am = userCourseEnvironment.getCourseEnvironment().getAssessmentManager();
		Identity assessedIdentity = userCourseEnvironment.getIdentityEnvironment().getIdentity();
		am.updateLastModifications(this, assessedIdentity, userCourseEnvironment, by);
	}

	@Override
	public Controller getDetailsEditController(UserRequest ureq, WindowControl wControl,
			BreadcrumbPanel stackPanel, UserCourseEnvironment coachCourseEnv, UserCourseEnvironment assessedUserCourseEnv) {
		Identity assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
		Long resId = assessedUserCourseEnv.getCourseEnvironment().getCourseResourceableId();
		OLATResourceable courseOres = OresHelper.createOLATResourceableInstance("CourseModule", resId);
		return new AssessedIdentityCheckListController(ureq, wControl, assessedIdentity, courseOres, coachCourseEnv, assessedUserCourseEnv, this, false, false);
	}

	@Override
	public boolean hasResultsDetails() {
		return false;
	}

	@Override
	public Controller getResultDetailsController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment assessedUserCourseEnv) {
		return null;
	}
	
	@Override
	public AssessmentCourseNodeController getIdentityListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, BusinessGroup group, UserCourseEnvironment coachCourseEnv,
			AssessmentToolContainer toolContainer, AssessmentToolSecurityCallback assessmentCallback) {
		return new IdentityListCourseNodeController(ureq, wControl, stackPanel,
				courseEntry, group, this, coachCourseEnv, toolContainer, assessmentCallback);
	}

	@Override
	public String getDetailsListView(UserCourseEnvironment userCourseEnvironment) {
		return null;
	}

	@Override
	public String getDetailsListViewHeaderKey() {
		return null;
	}

	@Override
	public boolean hasDetails() {
		return true;
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
						ZipUtil.addToZip(item, path, exportStream);
					}
				}
			}
		}

		String filename = dirName + "/" + StringHelper.transformDisplayNameToFileSystemName(getShortName());
		new CheckListExcelExport(this, course, locale).exportAll(filename, exportStream);
		
		//assessment documents
		if(hasIndividualAsssessmentDocuments()) {
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
	}
	
	/**
	 * The user course environment must be the coach or the user which preceed to
	 * the changes.
	 * @param userCourseEnv
	 * @param assessedIdentity
	 */
	public void updateScoreEvaluation(Identity identity, UserCourseEnvironment assessedUserCourseEnv, Identity assessedIdentity, Role by) {
		ModuleConfiguration config = getModuleConfiguration();
		Boolean scoreField = (Boolean)config.get(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD);
		Boolean sum = (Boolean)config.get(CheckListCourseNode.CONFIG_KEY_PASSED_SUM_CHECKBOX);
		Float cutValue = (Float)config.get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
		Float maxScore = (Float)config.get(MSCourseNode.CONFIG_KEY_SCORE_MAX);
		Boolean manualCorrection = (Boolean)config.get(CheckListCourseNode.CONFIG_KEY_PASSED_MANUAL_CORRECTION);
		if(cutValue != null) {
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
		ScoreEvaluation sceval = new ScoreEvaluation(Float.valueOf(score), passed);
		
		AssessmentManager am = assessedUserCourseEnv.getCourseEnvironment().getAssessmentManager();
		am.saveScoreEvaluation(this, identity, assessedIdentity, sceval, assessedUserCourseEnv, false, by);
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

		ScoreEvaluation sceval = new ScoreEvaluation(score, new Boolean(passed));
		
		AssessmentManager am = assessedUserCourseEnv.getCourseEnvironment().getAssessmentManager();
		am.saveScoreEvaluation(this, identity, assessedIdentity, sceval, assessedUserCourseEnv, false, by);
	}
	
	private void doUpdateScoreOnly(Float maxScore, Identity identity, UserCourseEnvironment assessedUserCourseEnv, Identity assessedIdentity, Role by) {
		OLATResourceable courseOres = OresHelper
				.createOLATResourceableInstance("CourseModule", assessedUserCourseEnv.getCourseEnvironment().getCourseResourceableId());
		
		CheckboxManager checkboxManager = CoreSpringFactory.getImpl(CheckboxManager.class);
		float score = checkboxManager.calculateScore(assessedIdentity, courseOres, getIdent());
		if(maxScore != null && maxScore.floatValue() < score) {
			score = maxScore.floatValue();
		}

		AssessmentManager am = assessedUserCourseEnv.getCourseEnvironment().getAssessmentManager();
		ScoreEvaluation currentEval = getUserScoreEvaluation(am.getAssessmentEntry(this, assessedIdentity));
		ScoreEvaluation sceval = new ScoreEvaluation(new Float(score), currentEval.getPassed());
		am.saveScoreEvaluation(this, identity, assessedIdentity, sceval, assessedUserCourseEnv, false, by);
	}
	
	@Override
	public CourseNode createInstanceForCopy(boolean isNewTitle, ICourse course, Identity author) {
		CheckListCourseNode cNode = (CheckListCourseNode)super.createInstanceForCopy(isNewTitle, course, author);
		CheckboxManager checkboxManager = CoreSpringFactory.getImpl(CheckboxManager.class);
		CheckboxList list = (CheckboxList)cNode.getModuleConfiguration().get(CONFIG_KEY_CHECKBOX);
		if (list!=null) {
			for(Checkbox checkbox:list.getList()) {
				checkbox.setCheckboxId(UUID.randomUUID().toString());
			}
		}
		// the ident of the course node is the same
		File sourceDir = checkboxManager.getFileDirectory(course.getCourseEnvironment(), this);
		if(sourceDir.exists()) {
			File targetDir = checkboxManager.getFileDirectory(course.getCourseEnvironment(), cNode);
			if(!targetDir.exists()) {
				targetDir.mkdirs();
				FileUtils.copyDirContentsToDir(sourceDir, targetDir, false, "copy files of checkbox");
			}
		}
		
		return cNode;
	}

	@Override
	public void postCopy(CourseEnvironmentMapper envMapper, Processing processType, ICourse course, ICourse sourceCourse) {
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

		checkboxManager.syncCheckbox(list, course, getIdent());
		super.postCopy(envMapper, processType, course, sourceCourse);
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
			updateScorePassedOnPublish(assessedIdentity, publisher, checkboxManager, course);
			if(++count % 10 == 0) {
				DBFactory.getInstance().commitAndCloseSession();
			}
		}
		DBFactory.getInstance().commitAndCloseSession();
		super.updateOnPublish(locale, course, publisher, publishEvents);
	}
	
	private void updateScorePassedOnPublish(Identity assessedIdentity, Identity coachIdentity, CheckboxManager checkboxManager, ICourse course) {
		AssessmentManager am = course.getCourseEnvironment().getAssessmentManager();
		
		Float currentScore = am.getNodeScore(this, assessedIdentity);
		Boolean currentPassed = am.getNodePassed(this, assessedIdentity);
		
		Float updatedScore = null;
		Boolean updatedPassed = null;

		ModuleConfiguration config = getModuleConfiguration();
		Boolean scoreGrantedBool = (Boolean)config.get(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD);
		if(scoreGrantedBool != null && scoreGrantedBool.booleanValue()) {
			updatedScore = checkboxManager.calculateScore(assessedIdentity, course, getIdent());
		} else {
			updatedScore = null;
		}
		
		Boolean passedBool = (Boolean)config.get(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD);
		if(passedBool != null && passedBool.booleanValue()) {
			Float cutValue = (Float)config.get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
			Boolean sumCheckbox = (Boolean)config.get(CheckListCourseNode.CONFIG_KEY_PASSED_SUM_CHECKBOX);
			if(sumCheckbox != null && sumCheckbox.booleanValue()) {
				Integer minValue = (Integer)config.get(CheckListCourseNode.CONFIG_KEY_PASSED_SUM_CUTVALUE);
				int checkedBox = checkboxManager.countChecked(assessedIdentity, course, getIdent());
				if(minValue != null && minValue.intValue() <= checkedBox) {
					updatedPassed = Boolean.TRUE;
				} else {
					updatedPassed = Boolean.FALSE;
				}

			} else if (cutValue != null) {
				if(updatedScore == null) {
					updatedScore = checkboxManager.calculateScore(assessedIdentity, course, getIdent());
				}
				
				if(updatedScore != null && cutValue.floatValue() <= updatedScore.floatValue()) {
					updatedPassed = Boolean.TRUE;
				} else {
					updatedPassed = Boolean.FALSE;
				}
			}
		} else {
			updatedPassed = null;
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
		
		if(needUpdate) {
			ScoreEvaluation scoreEval = new ScoreEvaluation(updatedScore, updatedPassed);
			IdentityEnvironment identityEnv = new IdentityEnvironment(assessedIdentity, null);
			UserCourseEnvironment uce = new UserCourseEnvironmentImpl(identityEnv, course.getCourseEnvironment());
			am.saveScoreEvaluation(this, coachIdentity, assessedIdentity, scoreEval, uce, false, Role.coach);
		}
	}

	/**
	 * Update the module configuration to have all mandatory configuration flags
	 * set to usefull default values
	 * 
	 * @param isNewNode true: an initial configuration is set; false: upgrading
	 *          from previous node configuration version, set default to maintain
	 *          previous behaviour
	 */
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode) {
		ModuleConfiguration config = getModuleConfiguration();
		if (isNewNode) {
			config.setConfigurationVersion(1);
		} 
	}
}