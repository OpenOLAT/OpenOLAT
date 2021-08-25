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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
import org.olat.core.util.ValidationStatus;
import org.olat.core.util.nodes.INode;
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
import org.olat.course.nodes.portfolio.PortfolioCourseNodeConfiguration.DeadlineType;
import org.olat.course.nodes.portfolio.PortfolioCourseNodeEditController;
import org.olat.course.nodes.portfolio.PortfolioCourseNodeRunController;
import org.olat.course.nodes.portfolio.PortfolioLearningPathNodeHandler;
import org.olat.course.reminder.AssessmentReminderProvider;
import org.olat.course.reminder.CourseNodeReminderProvider;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.handler.BinderTemplateResource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryManager;


/**
 * 
 * Description:<br>
 * course node of type portfolio.
 * 
 * <P>
 * Initial Date:  6 oct. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PortfolioCourseNode extends AbstractAccessableCourseNode {
	
	private static final Logger log = Tracing.createLoggerFor(PortfolioCourseNode.class);
	private static final int CURRENT_CONFIG_VERSION = 2;
	
	public static final String EDIT_CONDITION_ID = "editportfolio";
	
	@SuppressWarnings("deprecation")
	private static final String PACKAGE_EP = Util.getPackageName(PortfolioCourseNodeRunController.class);
	public static final String TYPE = "ep";
	
	private Condition preConditionEdit;
	
	public PortfolioCourseNode() {
		super(TYPE);
	}
	
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent, NodeAccessType nodeAccessType) {
		super.updateModuleConfigDefaults(isNewNode, parent, nodeAccessType);
		
		ModuleConfiguration config = getModuleConfiguration();
		if (isNewNode) {
			MSCourseNode.initDefaultConfig(config);
		} 
		config.setConfigurationVersion(CURRENT_CONFIG_VERSION);
	}
	
	@Override
	protected void postImportCopyConditions(CourseEnvironmentMapper envMapper) {
		super.postImportCopyConditions(envMapper);
		postImportCondition(preConditionEdit, envMapper);
	}

	@Override
	public void postExport(CourseEnvironmentMapper envMapper, boolean backwardsCompatible) {
		super.postExport(envMapper, backwardsCompatible);
		postExportCondition(preConditionEdit, envMapper, backwardsCompatible);
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		PortfolioCourseNodeEditController childTabCntrllr = new PortfolioCourseNodeEditController(ureq, wControl, stackPanel,
				course, this, getModuleConfiguration());
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, euce, childTabCntrllr);
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(false);
	}
	
	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd) {
		Controller controller;
		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isGuestOnly()) {
			Translator trans =  Util.createPackageTranslator(PortfolioCourseNode.class, ureq.getLocale());
			String title = trans.translate("guestnoaccess.title");
			String message = trans.translate("guestnoaccess.message");
			controller = MessageUIFactory.createInfoMessage(ureq, wControl, title, message);
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

	public void setPreConditionEdit(Condition preConditionEdit) {
		if (preConditionEdit == null) {
			preConditionEdit = getPreConditionEdit();
		}
		preConditionEdit.setConditionId(EDIT_CONDITION_ID);
		this.preConditionEdit = preConditionEdit;
	}
	
	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		Object repoSoftkey = getModuleConfiguration().get(PortfolioCourseNodeConfiguration.REPO_SOFT_KEY);
		if(repoSoftkey instanceof String) {
			RepositoryManager rm = RepositoryManager.getInstance();
			RepositoryEntry entry = rm.lookupRepositoryEntryBySoftkey((String)repoSoftkey, false);
			if(entry != null) {
				return entry;
			}
		}
		return null;
	}
	
	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return true;
	}
	
	public Date getDeadline() {
		ModuleConfiguration config = getModuleConfiguration();
		String type = (String)config.get(PortfolioCourseNodeConfiguration.DEADLINE_TYPE);
		if(StringHelper.containsNonWhitespace(type)) {
			switch(DeadlineType.valueOf(type)) {
				case none: return null;
				case absolut: 
					Date date = (Date)config.get(PortfolioCourseNodeConfiguration.DEADLINE_DATE);
					return date;
				case relative:
					Calendar cal = Calendar.getInstance();
					cal.setTime(new Date());
					boolean applied = applyRelativeToDate(cal, PortfolioCourseNodeConfiguration.DEADLINE_MONTH, Calendar.MONTH, 1);
					applied |= applyRelativeToDate(cal, PortfolioCourseNodeConfiguration.DEADLINE_WEEK, Calendar.DATE, 7);
					applied |= applyRelativeToDate(cal, PortfolioCourseNodeConfiguration.DEADLINE_DAY, Calendar.DATE, 1);
					if(applied) {
						return cal.getTime();
					}
					return null;
				default: return null;
			}
		}
		return null;
	}
	
	private boolean applyRelativeToDate(Calendar cal, String time, int calTime, int factor) {
		String t = (String)getModuleConfiguration().get(time);
		if(StringHelper.containsNonWhitespace(t)) {
			int timeToApply;
			try {
				timeToApply = Integer.parseInt(t) * factor;
			} catch (NumberFormatException e) {
				log.warn("Not a number: " + t, e);
				return false;
			}
			cal.add(calTime, timeToApply);
			return true;
		}
		return false;
	}
	
	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null) { return oneClickStatusCache[0]; }

		StatusDescription sd = StatusDescription.NOERROR;
		boolean isValid = PortfolioCourseNodeEditController.isModuleConfigValid(getModuleConfiguration());
		if (!isValid) {
			String shortKey = "error.noreference.short";
			String longKey = "error.noreference.long";
			String[] params = new String[] { getShortTitle() };
			sd = new StatusDescription(ValidationStatus.ERROR, shortKey, longKey, params, PACKAGE_EP);
			sd.setDescriptionForUnit(getIdent());
			// set which pane is affected by error
			sd.setActivateableViewIdentifier(PortfolioCourseNodeEditController.PANE_TAB_CONFIG);
		}
		return sd;
	}
	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;
		List<StatusDescription> statusDescs = isConfigValidWithTranslator(cev, PACKAGE_EP, getConditionExpressions());
		statusDescs.addAll(validateInternalConfiguration());
		oneClickStatusCache = StatusDescriptionHelper.sort(statusDescs);
		return oneClickStatusCache;
	}
	
	private boolean isFullyAssessedScoreConfigError() {
		boolean hasScore = Mode.none != new PortfolioAssessmentConfig(getModuleConfiguration()).getScoreMode();
		boolean isScoreTrigger = CoreSpringFactory.getImpl(PortfolioLearningPathNodeHandler.class)
				.getConfigs(this)
				.isFullyAssessedOnScore(null, null)
				.isEnabled();
		return isScoreTrigger && !hasScore;
	}
	
	private List<StatusDescription> validateInternalConfiguration() {
		List<StatusDescription> sdList = new ArrayList<>(1);
		
		if (isFullyAssessedScoreConfigError()) {
			addStatusErrorDescription("error.fully.assessed.score", "error.fully.assessed.score",
					TabbableLeaningPathNodeConfigController.PANE_TAB_LEARNING_PATH, sdList);
		}
		if (isFullyAssessedPassedConfigError()) {
			addStatusErrorDescription("error.fully.assessed.passed", "error.fully.assessed.passed",
					TabbableLeaningPathNodeConfigController.PANE_TAB_LEARNING_PATH, sdList);
		}
		
		return sdList;
	}
	
	private boolean isFullyAssessedPassedConfigError() {
		boolean hasPassed = Mode.none != new PortfolioAssessmentConfig(getModuleConfiguration()).getPassedMode();
		boolean isPassedTrigger = CoreSpringFactory.getImpl(PortfolioLearningPathNodeHandler.class)
				.getConfigs(this)
				.isFullyAssessedOnPassed(null, null)
				.isEnabled();
		return isPassedTrigger && !hasPassed;
	}
	
	private void addStatusErrorDescription(String shortDescKey, String longDescKey, String pane,
			List<StatusDescription> status) {
		String[] params = new String[] { getShortTitle() };
		StatusDescription sd = new StatusDescription(StatusDescription.ERROR, shortDescKey, longDescKey, params,
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
		boolean editor = (getPreConditionEdit().getConditionExpression() == null ? true : ci.evaluateCondition(getPreConditionEdit()));
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
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);
		
		RepositoryEntry entry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		CoreSpringFactory.getImpl(PortfolioService.class).detachRepositoryEntryFromBinders(entry, this);
	}
	
	@Override
	public CourseNodeReminderProvider getReminderProvider(boolean rootNode) {
		return new AssessmentReminderProvider(getIdent(), new PortfolioAssessmentConfig(getModuleConfiguration()));
	}
}