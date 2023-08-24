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
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
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
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.nodes.INode;
import org.olat.course.ICourse;
import org.olat.course.condition.ConditionEditController;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.editor.importnodes.ImportSettings;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.form.FormManager;
import org.olat.course.nodes.form.FormSecurityCallback;
import org.olat.course.nodes.form.FormSecurityCallbackFactory;
import org.olat.course.nodes.form.rule.FormReminderProvider;
import org.olat.course.nodes.form.ui.FormEditController;
import org.olat.course.nodes.form.ui.FormParticipationTableModel;
import org.olat.course.nodes.form.ui.FormRunCoachController;
import org.olat.course.nodes.form.ui.FormRunController;
import org.olat.course.nodes.survey.ui.SurveyEditController;
import org.olat.course.reminder.CourseNodeReminderProvider;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.VisibilityFilter;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.Role;
import org.olat.modules.forms.EvaluationFormParticipation;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.EvaluationFormSessionRef;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.EvaluationFormSurveyIdentifier;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.SessionFilterFactory;
import org.olat.modules.forms.handler.EvaluationFormResource;
import org.olat.modules.forms.ui.EvaluationFormExcelExport;
import org.olat.modules.forms.ui.EvaluationFormExcelExport.UserColumns;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;
import org.olat.modules.forms.ui.UserPropertiesColumns;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryManager;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 27.04.2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FormCourseNode extends AbstractAccessableCourseNode {

	private static final Logger log = Tracing.createLoggerFor(FormCourseNode.class);

	private static final long serialVersionUID = -5001393756214529108L;

	public static final String TYPE = "form";
	public static final String ICON_CSS = "o_icon_form";
	public static final EmptyStateConfig EMPTY_STATE = EvaluationFormExecutionController.defaultEmptyState()
			.withIconCss(FormCourseNode.ICON_CSS)
			.build();

	private static final int CURRENT_VERSION = 2;
	public static final String CONFIG_KEY_REPOSITORY_SOFTKEY = "repository.softkey";
	public static final String CONFIG_KEY_RELATIVE_DATES = "rel.dates";
	public static final String CONFIG_KEY_PARTICIPATION_DEADLINE = "participation.deadline";
	public static final String CONFIG_KEY_PARTICIPATION_DEADLINE_RELATIVE = "participation.deadline.relative";
	public static final String CONFIG_KEY_PARTICIPATION_DEADLINE_RELATIVE_TO = "participation.deadline.relative.to";
	public static final String CONFIG_KEY_CONFIRMATION_OWNERS = "confirmation.owners";
	public static final String CONFIG_KEY_CONFIRMATION_COACHES = "confirmation.coaches";
	public static final String CONFIG_KEY_CONFIRMATION_PARTICIPANT = "confirmation.participant";
	public static final String CONFIG_KEY_CONFIRMATION_EXTERNAL = "confirmation.external";
	public static final String CONFIG_KEY_CONFIRMATION_EXTERNAL_MAILS = "confirmation.external.mails";

	public FormCourseNode() {
		super(TYPE);
	}

	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return getEvaluationForm(getModuleConfiguration());
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return true;
	}

	@SuppressWarnings("deprecation")
	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null) {
			return oneClickStatusCache[0];
		}

		StatusDescription sd = StatusDescription.NOERROR;
		String repoKey = getModuleConfiguration().getStringValue(CONFIG_KEY_REPOSITORY_SOFTKEY);
		boolean repoKeyMissing = !StringHelper.containsNonWhitespace(repoKey);
		if (repoKeyMissing) {
			String shortKey = "error.repo.no.key.short";
			String longKey = "error.repo.no.key.long";
			String[] params = new String[] { this.getShortTitle() };
			String translPackage = Util.getPackageName(FormEditController.class);
			sd = new StatusDescription(StatusDescription.ERROR, shortKey, longKey, params, translPackage);
			sd.setDescriptionForUnit(getIdent());
			// set which pane is affected by error
			sd.setActivateableViewIdentifier(SurveyEditController.PANE_TAB_CONFIG);
		}
		return sd;
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			ICourse course, UserCourseEnvironment euce) {
		RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		TabbableController childTabCtrl	= new FormEditController(ureq, wControl, this, courseEntry);
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
		Controller runCtrl;
		Roles roles = ureq.getUserSession().getRoles();
		if (roles.isGuestOnly()) {
			runCtrl = MessageUIFactory.createGuestNoAccessMessage(ureq, wControl, null);
		} else if (userCourseEnv.isParticipant()) {
			runCtrl = new FormRunController(ureq, wControl, this, userCourseEnv);
		} else {
			FormSecurityCallback secCallback = FormSecurityCallbackFactory.createSecurityCallback(userCourseEnv);
			runCtrl = new FormRunCoachController(ureq, wControl, this, userCourseEnv, secCallback);
		}
		Controller ctrl = TitledWrapperHelper.getWrapper(ureq, wControl, runCtrl, userCourseEnv, this, ICON_CSS);
		return new NodeRunConstructionResult(ctrl);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		String translatorStr = Util.getPackageName(ConditionEditController.class);
		List<StatusDescription> statusDescs = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		return StatusDescriptionHelper.sort(statusDescs);
	}

	@Override
	public void exportNode(File exportDirectory, ICourse course) {
		RepositoryEntry re = getEvaluationForm(getModuleConfiguration());
		if (re == null) return;
		
		File fExportDirectory = new File(exportDirectory, getIdent());
		fExportDirectory.mkdirs();
		RepositoryEntryImportExport reie = new RepositoryEntryImportExport(re, fExportDirectory);
		reie.exportDoExport();
	}
	
	@Override
	public void importNode(File importDirectory, ICourse course, Identity owner, Organisation organisation, Locale locale, boolean withReferences) {
		RepositoryEntryImportExport rie = new RepositoryEntryImportExport(importDirectory, getIdent());
		if(withReferences && rie.anyExportedPropertiesAvailable()) {
			RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(EvaluationFormResource.TYPE_NAME);
			RepositoryEntry re = handler.importResource(owner, rie.getInitialAuthor(), rie.getDisplayName(),
				rie.getDescription(), false, organisation, locale, rie.importGetExportedFile(), null);
			setEvaluationFormReference(re, getModuleConfiguration());
			postImportCopy(course, this);
		} else {
			removeEvaluationFormReference(getModuleConfiguration());
		}
	}
	
	@Override
	public void postCopy(CourseEnvironmentMapper envMapper, Processing processType, ICourse course, ICourse sourceCourse, CopyCourseContext context) {
		super.postCopy(envMapper, processType, course, sourceCourse, context);
		postImportCopy(course, this);
		
		if (context != null) {
			ModuleConfiguration config = getModuleConfiguration();
			
			Date participationDeadline = config.getDateValue(CONFIG_KEY_PARTICIPATION_DEADLINE);
			
			if (participationDeadline != null) {
				participationDeadline.setTime(participationDeadline.getTime() + context.getDateDifference(getIdent()));
				config.setDateValue(CONFIG_KEY_PARTICIPATION_DEADLINE, participationDeadline);
			}
		}
	}
	
	@Override
	public CourseNode createInstanceForCopy(boolean isNewTitle, ICourse course, Identity author) {
		CourseNode copyInstance = super.createInstanceForCopy(isNewTitle, course, author);
		postImportCopy(course, copyInstance);
		return copyInstance;
	}

	@Override
	public void postImportCourseNodes(ICourse course, CourseNode sourceCourseNode, ICourse sourceCourse, ImportSettings settings, CourseEnvironmentMapper envMapper) {
		super.postImportCourseNodes(course, sourceCourseNode, sourceCourse, settings, envMapper);
		postImportCopy(course, this);
	}

	private void postImportCopy(ICourse course, CourseNode courseNode) {
		RepositoryEntry formEntry = getEvaluationForm(getModuleConfiguration());
		if (formEntry == null) return;
		
		FormManager formManager = CoreSpringFactory.getImpl(FormManager.class);
		EvaluationFormSurveyIdentifier surveyIdentifier = formManager.getSurveyIdentifier(courseNode, course);
		EvaluationFormSurvey survey = formManager.loadSurvey(surveyIdentifier);
		if (survey == null) {
			survey = formManager.createSurvey(surveyIdentifier, formEntry);
		} else {
			boolean isFormUpdateable = formManager.isFormUpdateable(survey);
			if (isFormUpdateable) {
				survey = formManager.updateSurveyForm(survey, formEntry);
			}
		}
	}

	@Override
	public boolean archiveNodeData(Locale locale, ICourse course, ArchiveOptions options,
			ZipOutputStream exportStream, String archivePath, String charset) {
		FormManager formManager = CoreSpringFactory.getImpl(FormManager.class);
		UserManager userManager = CoreSpringFactory.getImpl(UserManager.class);
		
		try {
			EvaluationFormSurveyIdentifier surveyIdentifier = formManager.getSurveyIdentifier(this, course);
			List<UserPropertyHandler> userPropertyHandlers = userManager
					.getUserPropertyHandlersFor(FormParticipationTableModel.USAGE_IDENTIFIER, true);
			Translator userPropertyTranslator = userManager
					.getPropertyHandlerTranslator(Util.createPackageTranslator(FormCourseNode.class, locale));
			UserColumns userColumns = new UserPropertiesColumns(userPropertyHandlers, userPropertyTranslator);
			EvaluationFormExcelExport excelExport = formManager.getExcelExport(this, surveyIdentifier, userColumns);
			excelExport.export(exportStream, archivePath);
		} catch (IOException e) {
			log.error("", e);
			return false;
		}
		return true;
	}
	
	@Override
	public void archiveForResetUserData(UserCourseEnvironment assessedUserCourseEnv, ZipOutputStream archiveStream,
			String path, Identity doer, Role by) {
		I18nManager i18nManager = CoreSpringFactory.getImpl(I18nManager.class);
		FormManager formManager = CoreSpringFactory.getImpl(FormManager.class);
		UserManager userManager = CoreSpringFactory.getImpl(UserManager.class);

		try {
			Identity assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
			Locale locale = i18nManager.getLocaleOrDefault(assessedIdentity.getUser().getPreferences().getLanguage());
			List<UserPropertyHandler> userPropertyHandlers = userManager
					.getUserPropertyHandlersFor(FormParticipationTableModel.USAGE_IDENTIFIER, true);
			Translator translator = Util.createPackageTranslator(FormCourseNode.class, locale,
					Util.createPackageTranslator(FormRunController.class, locale));
			Translator userPropertyTranslator = userManager.getPropertyHandlerTranslator(translator);
			UserColumns userColumns = new UserPropertiesColumns(userPropertyHandlers, userPropertyTranslator);
			
			RepositoryEntry courseEntry = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			EvaluationFormSurveyIdentifier surveyIdentifier = formManager.getSurveyIdentifier(this, courseEntry);

			EvaluationFormSurvey survey = formManager.loadSurvey(surveyIdentifier);
			EvaluationFormSession session = formManager.getSession(survey, assessedIdentity);
			SessionFilter sessionFilter;
			if(session != null) {
				sessionFilter = SessionFilterFactory.create(session);
			} else {
				sessionFilter = SessionFilterFactory.create(new EvaluationFormSessionRef() {
					@Override
					public Long getKey() { 
						return Long.valueOf(-1l);
					}
				});
			}
			
			EvaluationFormExcelExport excelExport = formManager.getExcelExport(this, surveyIdentifier, sessionFilter, userColumns);
			excelExport.export(archiveStream, path);
		} catch (IOException e) {
			log.error("", e);
		}
		super.archiveForResetUserData(assessedUserCourseEnv, archiveStream, path, doer, by);
	}
	
	@Override
	public void resetUserData(UserCourseEnvironment assessedUserCourseEnv, Identity identity, Role by) {
		FormManager formManager = CoreSpringFactory.getImpl(FormManager.class);
		Identity assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
		RepositoryEntry courseEntry = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		EvaluationFormSurveyIdentifier surveyIdentifier = formManager.getSurveyIdentifier(this, courseEntry);
		EvaluationFormSurvey survey = formManager.loadSurvey(surveyIdentifier);
		EvaluationFormParticipation participation = formManager.loadParticipation(survey, assessedIdentity);
		if(participation != null) {
			formManager.deleteParticipation(participation, this, assessedUserCourseEnv.getCourseEnvironment());
		}
		
		super.resetUserData(assessedUserCourseEnv, identity, by);
	}

	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);
		
		FormManager formManager = CoreSpringFactory.getImpl(FormManager.class);
		EvaluationFormSurveyIdentifier surveyIdentifier = formManager.getSurveyIdentifier(this, course);
		EvaluationFormSurvey survey = formManager.loadSurvey(surveyIdentifier);
		formManager.deleteSurvey(survey);
	}

	@Override
	public CourseNodeReminderProvider getReminderProvider(RepositoryEntryRef courseEntry, boolean rootNode) {
		return new FormReminderProvider(this);
	}

	public static RepositoryEntry getEvaluationForm(ModuleConfiguration config) {
		if (config == null) return null;
		
		String repoSoftkey = config.getStringValue(CONFIG_KEY_REPOSITORY_SOFTKEY);
		if (!StringHelper.containsNonWhitespace(repoSoftkey)) return null;

		RepositoryManager repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);
		return repositoryManager.lookupRepositoryEntryBySoftkey(repoSoftkey, false);
	}
	
	public static void setEvaluationFormReference(RepositoryEntry re, ModuleConfiguration moduleConfig) {
		moduleConfig.set(CONFIG_KEY_REPOSITORY_SOFTKEY, re.getSoftkey());
	}
	
	public static void removeEvaluationFormReference(ModuleConfiguration moduleConfig) {
		moduleConfig.remove(CONFIG_KEY_REPOSITORY_SOFTKEY);
	}
	
	@Override
	public List<Entry<String, DueDateConfig>> getNodeSpecificDatesWithLabel() {
		return List.of(Map.entry("form.participation.until", getDueDateConfig(CONFIG_KEY_PARTICIPATION_DEADLINE)));
	}

	@Override
	public DueDateConfig getDueDateConfig(String key) {
		if (CONFIG_KEY_PARTICIPATION_DEADLINE.equals(key)) {
			return DueDateConfig.ofCourseNode(this, CONFIG_KEY_RELATIVE_DATES, CONFIG_KEY_PARTICIPATION_DEADLINE,
					CONFIG_KEY_PARTICIPATION_DEADLINE_RELATIVE, CONFIG_KEY_PARTICIPATION_DEADLINE_RELATIVE_TO);
		}
		return super.getDueDateConfig(key);
	}
	
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent, NodeAccessType nodeAccessType, Identity doer) {
		super.updateModuleConfigDefaults(isNewNode, parent, nodeAccessType, doer);
		
		ModuleConfiguration config = getModuleConfiguration();
		int version = config.getConfigurationVersion();
		
		if (version < 2) {
			config.setBooleanEntry(CONFIG_KEY_RELATIVE_DATES, false);
			config.setStringValue(CONFIG_KEY_CONFIRMATION_EXTERNAL_MAILS, "");
		}
		
		config.setConfigurationVersion(CURRENT_VERSION);
	}
	
}

