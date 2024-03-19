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

package org.olat.course.nodes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.course.CourseEntryRef;
import org.olat.course.ICourse;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.condition.Condition;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.learningpath.ui.TabbableLeaningPathNodeConfigController;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.portfolio.PortfolioAssessmentConfig;
import org.olat.course.nodes.portfolio.PortfolioCoachRunController;
import org.olat.course.nodes.portfolio.PortfolioCourseNodeConfiguration;
import org.olat.course.nodes.portfolio.PortfolioCourseNodeEditController;
import org.olat.course.nodes.portfolio.PortfolioCourseNodeRunController;
import org.olat.course.nodes.portfolio.PortfolioLearningPathNodeHandler;
import org.olat.course.reminder.AssessmentReminderProvider;
import org.olat.course.reminder.CourseNodeReminderProvider;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.VisibilityFilter;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.Role;
import org.olat.modules.grade.GradeModule;
import org.olat.modules.grade.GradeScale;
import org.olat.modules.grade.GradeService;
import org.olat.modules.portfolio.Binder;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.handler.BinderTemplateResource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;


/**
 * 
 * Description:<br>
 * course node of type portfolio.
 * 
 * <P>
 * Initial Date:  6 oct. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class PortfolioCourseNode extends AbstractAccessableCourseNode {
	
	private static final Logger log = Tracing.createLoggerFor(PortfolioCourseNode.class);
	private static final int CURRENT_CONFIG_VERSION = 3;
	
	public static final String EDIT_CONDITION_ID = "editportfolio";
	
	@SuppressWarnings("deprecation")
	private static final String PACKAGE_EP = Util.getPackageName(PortfolioCourseNodeRunController.class);
	public static final String TYPE = "ep";
	
	private Condition preConditionEdit;
	
	public PortfolioCourseNode() {
		super(TYPE);
	}
	
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent, NodeAccessType nodeAccessType, Identity doer) {
		super.updateModuleConfigDefaults(isNewNode, parent, nodeAccessType, doer);
		
		ModuleConfiguration config = getModuleConfiguration();
		int version = config.getConfigurationVersion();
		if (isNewNode) {
			MSCourseNode.initDefaultConfig(config);
		}
		if (version < 3) {
			String instructions = getInstruction();
			if (!StringHelper.containsNonWhitespace(instructions)) {
				setInstruction(config.getStringValue("node_text"));
			} else {
				setInstruction(instructions.concat(config.getStringValue("node_text")));
			}
		}
		config.setConfigurationVersion(CURRENT_CONFIG_VERSION);
	}
	
	@Override
	protected void postImportCopyConditions(CourseEnvironmentMapper envMapper) {
		super.postImportCopyConditions(envMapper);
		postImportCondition(preConditionEdit, envMapper);
	}

	@Override
	protected void postImportCourseNodeConditions(CourseNode sourceCourseNode, CourseEnvironmentMapper envMapper) {
		super.postImportCourseNodeConditions(sourceCourseNode, envMapper);
		configureOnlyGeneralAccess(((PortfolioCourseNode)sourceCourseNode).preConditionEdit, preConditionEdit, envMapper);
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		PortfolioCourseNodeEditController childTabCtrl = new PortfolioCourseNodeEditController(ureq, wControl, stackPanel,
				course, this, getModuleConfiguration());
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, euce, childTabCtrl);
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}
	
	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd, VisibilityFilter visibilityFilter) {
		Controller controller;
		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isGuestOnly()) {
			controller = MessageUIFactory.createGuestNoAccessMessage(ureq, wControl, null);
		} else {
			RepositoryEntry mapEntry = getReferencedRepositoryEntry();
			if(mapEntry != null && BinderTemplateResource.TYPE_NAME.equals(mapEntry.getOlatResource().getResourceableTypeName())) {
				if (userCourseEnv.isCoach() || userCourseEnv.isAdmin()) {
					controller = new PortfolioCoachRunController(ureq, wControl, userCourseEnv, this);
				} else {
					controller = new PortfolioCourseNodeRunController(ureq, wControl, userCourseEnv, this);
				}
			} else {
				Translator trans = Util.createPackageTranslator(PortfolioCourseNodeRunController.class, ureq.getLocale());
				controller = MessageUIFactory.createInfoMessage(ureq, wControl, "", trans.translate("error.portfolioV1"));
			}
		}
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, controller, userCourseEnv, this, "o_ep_icon");
		return new NodeRunConstructionResult(ctrl);
	}
	
	public Condition getPreConditionEdit() {
		if (preConditionEdit == null) {
			preConditionEdit = new Condition();
			preConditionEdit.setEasyModeCoachesAndAdmins(true);
			preConditionEdit.setConditionExpression(preConditionEdit.getConditionFromEasyModeConfiguration());
			preConditionEdit.setExpertMode(false);
		}
		preConditionEdit.setConditionId(EDIT_CONDITION_ID);
		return preConditionEdit;
	}
	
	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		Object repoSoftkey = getModuleConfiguration().get(PortfolioCourseNodeConfiguration.REPO_SOFT_KEY);
		if(repoSoftkey instanceof String reSoftkey) {
			RepositoryManager rm = RepositoryManager.getInstance();
			// if re is null, null will be returned
			return rm.lookupRepositoryEntryBySoftkey(reSoftkey, false);
		}
		return null;
	}
	
	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return true;
	}
	
	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null) { return oneClickStatusCache[0]; }

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
		List<StatusDescription> statusDescs = isConfigValidWithTranslator(cev, PACKAGE_EP, getConditionExpressions());
		statusDescs.addAll(validateInternalConfiguration(cev));
		oneClickStatusCache = StatusDescriptionHelper.sort(statusDescs);
		return oneClickStatusCache;
	}
	
	private List<StatusDescription> validateInternalConfiguration(CourseEditorEnv cev) {
		List<StatusDescription> sdList = new ArrayList<>(1);
		RepositoryEntry portfolioEntry = getReferencedRepositoryEntry();

		if (cev != null) {
			PortfolioAssessmentConfig assessmentConfig = new PortfolioAssessmentConfig(new CourseEntryRef(cev), this);
			
			if (isFullyAssessedPassedConfigError(assessmentConfig)) {
				addStatusErrorDescription("error.fully.assessed.passed", "error.fully.assessed.passed",
						TabbableLeaningPathNodeConfigController.PANE_TAB_LEARNING_PATH, sdList, StatusDescription.ERROR);
			}
			
			if (isFullyAssessedScoreConfigError(assessmentConfig)) {
				addStatusErrorDescription("error.fully.assessed.score", "error.fully.assessed.score",
						TabbableLeaningPathNodeConfigController.PANE_TAB_LEARNING_PATH, sdList, StatusDescription.ERROR);
			}
			
			if (getModuleConfiguration().getBooleanSafe(MSCourseNode.CONFIG_KEY_GRADE_ENABLED) && CoreSpringFactory.getImpl(GradeModule.class).isEnabled()) {
				GradeService gradeService = CoreSpringFactory.getImpl(GradeService.class);
				GradeScale gradeScale = gradeService.getGradeScale(cev.getCourseGroupManager().getCourseEntry(), getIdent());
				if (gradeScale == null) {
					addStatusErrorDescription("error.missing.grade.scale", "error.missing.grade.scale",
							PortfolioCourseNodeEditController.PANE_TAB_SCORING, sdList, StatusDescription.ERROR);
				}
			}
		} else if (portfolioEntry != null
				&& (RepositoryEntryStatusEnum.deleted == portfolioEntry.getEntryStatus()
				|| RepositoryEntryStatusEnum.trash == portfolioEntry.getEntryStatus())) {
			addStatusErrorDescription("error.portfolio.deleted.edit", "error.portfolio.deleted.edit", PortfolioCourseNodeEditController.PANE_TAB_CONFIG, sdList, StatusDescription.WARNING);
		} else if (portfolioEntry == null) {
			addStatusErrorDescription("error.noreference.short", "error.noreference.long", PortfolioCourseNodeEditController.PANE_TAB_CONFIG, sdList, StatusDescription.ERROR);
		}
		
		return sdList;
	}
	
	private boolean isFullyAssessedPassedConfigError(PortfolioAssessmentConfig assessmentConfig) {
		boolean hasPassed = Mode.none != assessmentConfig.getPassedMode();
		boolean isPassedTrigger = CoreSpringFactory.getImpl(PortfolioLearningPathNodeHandler.class)
				.getConfigs(this)
				.isFullyAssessedOnPassed(null, null)
				.isEnabled();
		return isPassedTrigger && !hasPassed;
	}
	
	private boolean isFullyAssessedScoreConfigError(PortfolioAssessmentConfig assessmentConfig) {
		boolean hasScore = Mode.none != assessmentConfig.getScoreMode();
		boolean isScoreTrigger = CoreSpringFactory.getImpl(PortfolioLearningPathNodeHandler.class)
				.getConfigs(this)
				.isFullyAssessedOnScore(null, null)
				.isEnabled();
		return isScoreTrigger && !hasScore;
	}
	
	private void addStatusErrorDescription(String shortDescKey, String longDescKey, String pane,
										   List<StatusDescription> status, Level severity) {
		String[] params = new String[] { getShortTitle() };
		StatusDescription sd = new StatusDescription(severity, shortDescKey, longDescKey, params,
				PACKAGE_EP);
		sd.setDescriptionForUnit(getIdent());
		sd.setActivateableViewIdentifier(pane);
		status.add(sd);
	}

	@Override
	public void calcAccessAndVisibility(ConditionInterpreter ci, NodeEvaluation nodeEval) {
		//nodeEval.setVisible(true);
		super.calcAccessAndVisibility(ci, nodeEval);
		
		// evaluate the preconditions
		boolean editor = (getPreConditionEdit().getConditionExpression() == null || ci.evaluateCondition(getPreConditionEdit()));
		nodeEval.putAccessStatus(EDIT_CONDITION_ID, editor);
	}

	@Override
	public void exportNode(File exportDirectory, ICourse course) {
		RepositoryEntry re = getReferencedRepositoryEntry();
		if (re == null) return;
		
		File fExportDirectory = new File(exportDirectory, getIdent());
		fExportDirectory.mkdirs();
		RepositoryEntryImportExport reie = new RepositoryEntryImportExport(re, fExportDirectory);
		reie.exportDoExport();
	}

	@Override
	public void importNode(File importDirectory, ICourse course, Identity owner, Organisation organisation, Locale locale, boolean withReferences) {
		RepositoryEntryImportExport rie = new RepositoryEntryImportExport(importDirectory, getIdent());
		if (withReferences && rie.anyExportedPropertiesAvailable()) {
			PortfolioCourseNodeEditController.removeReference(getModuleConfiguration());
		}
	}

	@Override
	public void resetUserData(UserCourseEnvironment assessedUserCourseEnv, Identity identity, Role by) {
		RepositoryEntry mapEntry = getReferencedRepositoryEntry();
		if(mapEntry != null && BinderTemplateResource.TYPE_NAME.equals(mapEntry.getOlatResource().getResourceableTypeName())) {
			Identity assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
			RepositoryEntry courseEntry = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			PortfolioService portfolioService = CoreSpringFactory.getImpl(PortfolioService.class);
			Binder templateBinder = portfolioService.getBinderByResource(mapEntry.getOlatResource());
			if(templateBinder != null) {
				Binder copyBinder = portfolioService.getBinder(assessedIdentity, templateBinder, courseEntry, getIdent());
				if(copyBinder != null) {
					log.info("Detach binder {} by {} in course {} element {}", copyBinder.getKey(), assessedIdentity.getKey(), courseEntry, getIdent());
					portfolioService.detachRepositoryEntryFromBinders(copyBinder);
				}
			}
		}
		
		super.resetUserData(assessedUserCourseEnv, identity, by);
	}

	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);
		
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		CoreSpringFactory.getImpl(PortfolioService.class).detachRepositoryEntryFromBinders(entry, this);
		
		// Delete GradeScales
		CoreSpringFactory.getImpl(GradeService.class).deleteGradeScale(entry, getIdent());
	}
	
	@Override
	public CourseNodeReminderProvider getReminderProvider(RepositoryEntryRef courseEntry, boolean rootNode) {
		return new AssessmentReminderProvider(getIdent(), new PortfolioAssessmentConfig(courseEntry, this));
	}
}