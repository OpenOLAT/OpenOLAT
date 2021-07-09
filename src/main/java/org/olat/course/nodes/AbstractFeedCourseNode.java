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
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.ValidationStatus;
import org.olat.core.util.nodes.INode;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.condition.Condition;
import org.olat.course.condition.interpreter.ConditionExpression;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.noderight.NodeRight;
import org.olat.course.noderight.NodeRightGrant.NodeRightRole;
import org.olat.course.noderight.NodeRightService;
import org.olat.course.noderight.NodeRightType;
import org.olat.course.noderight.NodeRightTypeBuilder;
import org.olat.course.nodes.feed.FeedNodeEditController;
import org.olat.course.nodes.feed.FeedNodeSecurityCallback;
import org.olat.course.nodes.feed.FeedPeekviewController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.webFeed.FeedReadOnlySecurityCallback;
import org.olat.modules.webFeed.FeedSecurityCallback;
import org.olat.modules.webFeed.manager.FeedManager;
import org.olat.modules.webFeed.ui.FeedMainController;
import org.olat.modules.webFeed.ui.FeedUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryImportExport;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * 
 * <P>
 * Initial Date: Mar 30, 2009 <br>
 * 
 * @author gwassmann
 */
public abstract class AbstractFeedCourseNode extends AbstractAccessableCourseNode {
	
	private static final long serialVersionUID = -5307888583081589123L;
	
	private static final int CURRENT_VERSION = 3;
	public static final String CONFIG_KEY_REPOSITORY_SOFTKEY = "reporef";
	
	private static final String LEGACY_COACH_MODERATE_ALLOWED = "coach.moderate.allowed";
	private static final String LEGACY_COACH_POST_ALLOWED = "coach.post.allowed";
	private static final String LEGACY_PARTICIPANT_POST_ALLOWED = "participant.post.allowed";
	private static final String LEGACY_GUEST_POST_ALLOWED = "guest.post.allowed";
	
	private static final NodeRightType MODERATE = NodeRightTypeBuilder.ofIdentifier("moderate")
			.setLabel(FeedNodeEditController.class, "edit.moderator")
			.addRole(NodeRightRole.coach, true)
			.build();
	private static final NodeRightType POST = NodeRightTypeBuilder.ofIdentifier("post")
			.setLabel(FeedNodeEditController.class, "edit.poster")
			.addRole(NodeRightRole.coach, true)
			.addRole(NodeRightRole.participant, true)
			.build();
	public static final List<NodeRightType> NODE_RIGHT_TYPES = List.of(MODERATE, POST);
	
	protected Condition preConditionReader, preConditionPoster, preConditionModerator;

	public AbstractFeedCourseNode(String type, INode parent) {
		super(type, parent);
	}
	
	protected abstract String getTranslatorPackage();

	protected abstract String getResourceablTypeName();
	
	protected abstract FeedUIFactory getFeedUIFactory(Locale locale);
	
	protected abstract String geIconCssClass();
	
	protected abstract String getPeekviewWrapperCssClass();
	
	protected abstract String getEditHelpUrl();

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			ICourse course, UserCourseEnvironment euce) {
		String translatorPackage = getTranslatorPackage();
		FeedUIFactory uiFactory = getFeedUIFactory(ureq.getLocale());
		String resourceablTypeName = getResourceablTypeName();
		String editHelpUrl = getEditHelpUrl();
		TabbableController editCtrl = new FeedNodeEditController(ureq, wControl, stackPanel, translatorPackage, course,
				this, euce, uiFactory, resourceablTypeName, editHelpUrl);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, euce, editCtrl);
	}
	
	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return hasCustomPreConditions() ? ConditionAccessEditConfig.custom() : ConditionAccessEditConfig.regular(false);
	}
	
	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd) {
		RepositoryEntry entry = getReferencedRepositoryEntry();
		FeedSecurityCallback callback = getFeedSecurityCallback(ureq, entry, userCourseEnv, nodeSecCallback);
		SubscriptionContext subsContext = CourseModule.createSubscriptionContext(userCourseEnv.getCourseEnvironment(),
				this);
		callback.setSubscriptionContext(subsContext);
		
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(this));
		
		Long courseId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
		FeedMainController mainCtrl = getFeedUIFactory(ureq.getLocale()).createMainController(entry.getOlatResource(),
				ureq, wControl, callback, courseId, getIdent());
		List<ContextEntry> entries = BusinessControlFactory.getInstance().createCEListFromResourceType(nodecmd);
		mainCtrl.activate(ureq, entries, null);
		Controller wrapperCtrl = TitledWrapperHelper.getWrapper(ureq, wControl, mainCtrl, userCourseEnv, this, geIconCssClass());
		return new NodeRunConstructionResult(wrapperCtrl);
	}
	
	@Override
	public Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback) {
		if (nodeSecCallback.isAccessible()) {
			RepositoryEntry entry = getReferencedRepositoryEntry();
			FeedSecurityCallback callback = getFeedSecurityCallback(ureq, entry, userCourseEnv, nodeSecCallback);
			
			Long courseId = userCourseEnv.getCourseEnvironment().getCourseResourceableId();
			FeedUIFactory uiFactory = getFeedUIFactory(ureq.getLocale());
			String peekviewWrapperCssClass = getPeekviewWrapperCssClass();
			return new FeedPeekviewController(entry.getOlatResource(), ureq, wControl, callback, courseId, getIdent(),
					uiFactory, 2, peekviewWrapperCssClass);
		}
		return super.createPeekViewRunController(ureq, wControl, userCourseEnv, nodeSecCallback);
	}
	
	private FeedSecurityCallback getFeedSecurityCallback(UserRequest ureq, RepositoryEntry entry,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback) {
		FeedSecurityCallback callback;
		if(userCourseEnv.isCourseReadOnly()) {
			callback = new FeedReadOnlySecurityCallback();
		} else {
			RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
			Roles roles = ureq.getUserSession().getRoles();
			boolean isPoster = isPoster(userCourseEnv, nodeSecCallback.getNodeEvaluation());
			boolean isModerator = isModerator(userCourseEnv, nodeSecCallback.getNodeEvaluation());
			boolean isAdmin = userCourseEnv.isAdmin();
			boolean isGuest = roles.isGuestOnly();
			boolean isOwner = !isGuest && repositoryService.hasRole(ureq.getIdentity(), entry, GroupRoles.owner.name());
			callback = new FeedNodeSecurityCallback(isPoster, isModerator, isAdmin, isOwner, isGuest);
		}
		return callback;
	}
	
	private boolean isModerator(UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
		if (hasCustomPreConditions()) {
			return ne != null? ne.isCapabilityAccessible("moderator"): false;
		}
		return CoreSpringFactory.getImpl(NodeRightService.class).isGranted(getModuleConfiguration(), userCourseEnv, MODERATE);
	}
	
	private boolean isPoster(UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
		if (hasCustomPreConditions()) {
			return ne != null ? ne.isCapabilityAccessible("poster") : false;
		}
		return CoreSpringFactory.getImpl(NodeRightService.class).isGranted(getModuleConfiguration(), userCourseEnv, POST);
	}

	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent) {
		ModuleConfiguration config = getModuleConfiguration();
		int version = config.getConfigurationVersion();
		
		if (isNewNode) {
			config.setBooleanEntry(NodeEditController.CONFIG_STARTPAGE, false);
		}
		if (version < 2) {
			removeDefaultPreconditions();
		}
		if (version < 3 && config.has(LEGACY_COACH_MODERATE_ALLOWED)) {
			NodeRightService nodeRightService = CoreSpringFactory.getImpl(NodeRightService.class);
			// Moderate
			NodeRight moderateRight = nodeRightService.getRight(config, MODERATE);
			Collection<NodeRightRole> moderateRoles = new ArrayList<>(1);
			if (config.getBooleanSafe(LEGACY_COACH_MODERATE_ALLOWED)) {
				moderateRoles.add(NodeRightRole.coach);
			}
			nodeRightService.setRoleGrants(moderateRight, moderateRoles);
			nodeRightService.setRight(config, moderateRight);
			// Post
			NodeRight postRight = nodeRightService.getRight(config, POST);
			Collection<NodeRightRole> postRoles = new ArrayList<>(3);
			if (config.getBooleanSafe(LEGACY_COACH_POST_ALLOWED)) {
				postRoles.add(NodeRightRole.coach);
			}
			if (config.getBooleanSafe(LEGACY_PARTICIPANT_POST_ALLOWED)) {
				postRoles.add(NodeRightRole.participant);
			}
			nodeRightService.setRoleGrants(postRight, postRoles);
			nodeRightService.setRight(config, postRight);
			// Remove legacy
			config.remove(LEGACY_COACH_MODERATE_ALLOWED);
			config.remove(LEGACY_COACH_POST_ALLOWED);
			config.remove(LEGACY_PARTICIPANT_POST_ALLOWED);
			config.remove(LEGACY_GUEST_POST_ALLOWED);
		}
		config.setConfigurationVersion(CURRENT_VERSION);
	}

	/**
	 * We don't want to have custom preConditions. So we keep these preConditions
	 * only, if they have some special configs. Otherwise we delete them and use the
	 * regular configs.
	 */
	private void removeDefaultPreconditions() {
		if (hasCustomPreConditions()) {
			boolean defaultPreconditions =
					!preConditionModerator.isExpertMode()
				&& preConditionModerator.isEasyModeCoachesAndAdmins()
				&& !preConditionModerator.isEasyModeAlwaysAllowCoachesAndAdmins()
				&& !preConditionModerator.isAssessmentMode()
				&& !preConditionModerator.isAssessmentModeViewResults()
				&& !preConditionPoster.isExpertMode()
				&& preConditionPoster.isEasyModeCoachesAndAdmins()
				&& !preConditionPoster.isEasyModeAlwaysAllowCoachesAndAdmins()
				&& !preConditionPoster.isAssessmentMode()
				&& !preConditionPoster.isAssessmentModeViewResults();
				if (defaultPreconditions && preConditionReader != null) {
					defaultPreconditions = !preConditionReader.isExpertMode()
							&& !preConditionReader.isEasyModeCoachesAndAdmins()
							&& !preConditionReader.isEasyModeAlwaysAllowCoachesAndAdmins()
							&& !preConditionReader.isAssessmentMode()
							&& !preConditionReader.isAssessmentModeViewResults();
				}
			if (defaultPreconditions) {
				removeCustomPreconditions();
			}
		}
	}

	public void removeCustomPreconditions() {
		preConditionModerator = null;
		preConditionPoster = null;
		preConditionReader = null;
	}
	
	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;
		List<StatusDescription> sds = isConfigValidWithTranslator(cev, getTranslatorPackage(), getConditionExpressions());
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
	}
	
	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null) { return oneClickStatusCache[0]; }

		StatusDescription status = StatusDescription.NOERROR;
		boolean invalid = getModuleConfiguration().get(CONFIG_KEY_REPOSITORY_SOFTKEY) == null;
		if (invalid) {
			String[] params = new String[] { this.getShortTitle() };
			String shortKey = "error.no.reference.short";
			String longKey = "error.no.reference.long";
			status = new StatusDescription(ValidationStatus.ERROR, shortKey, longKey, params, getTranslatorPackage());
			status.setDescriptionForUnit(getIdent());
			// Set which pane is affected by error
			status.setActivateableViewIdentifier(FeedNodeEditController.PANE_TAB_CONFIG);
		}
		return status;
	}

	@Override
	protected String getDefaultTitleOption() {
		return CourseNode.DISPLAY_OPTS_CONTENT;
	}

	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);

		SubscriptionContext subsContext = CourseModule.createSubscriptionContext(course.getCourseEnvironment(), this);
		CoreSpringFactory.getImpl(NotificationsManager.class).delete(subsContext);
	}

	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		ModuleConfiguration config = getModuleConfiguration();
		String repoSoftkey = (String) config.get(CONFIG_KEY_REPOSITORY_SOFTKEY);
		RepositoryManager rm = RepositoryManager.getInstance();
		return rm.lookupRepositoryEntryBySoftkey(repoSoftkey, false);
	}
	
	public static void setReference(ModuleConfiguration moduleConfig, RepositoryEntry feedEntry) {
		moduleConfig.set(CONFIG_KEY_REPOSITORY_SOFTKEY, feedEntry.getSoftkey());
	}
	
	public static void removeReference(ModuleConfiguration moduleConfig) {
		moduleConfig.remove(CONFIG_KEY_REPOSITORY_SOFTKEY);
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return true;
	}
	
	/**
	 * The conditions to control the user rights are deprecated. In new course nodes
	 * this options are controlled by module configurations. Existing course nodes
	 * may have preconditions. In that case they are still used for compatibility
	 * reasons.
	 *
	 * @return
	 */
	public boolean hasCustomPreConditions() {
		return preConditionModerator != null || preConditionPoster != null || preConditionReader != null;
	}

	public Condition getPreConditionModerator() {
		if (preConditionModerator == null) {
			preConditionModerator = new Condition();
		}
		preConditionModerator.setConditionId("moderator");
		return preConditionModerator;
	}

	public void setPreConditionModerator(Condition preConditionModerator) {
		if (preConditionModerator == null) {
			preConditionModerator = getPreConditionModerator();
		}
		preConditionModerator.setConditionId("moderator");
		this.preConditionModerator = preConditionModerator;
	}

	public Condition getPreConditionPoster() {
		if (preConditionPoster == null) {
			preConditionPoster = new Condition();
		}
		preConditionPoster.setConditionId("poster");
		return preConditionPoster;
	}

	public void setPreConditionPoster(Condition preConditionPoster) {
		if (preConditionPoster == null) {
			preConditionPoster = getPreConditionPoster();
		}
		preConditionPoster.setConditionId("poster");
		this.preConditionPoster = preConditionPoster;
	}

	public Condition getPreConditionReader() {
		if (preConditionReader == null) {
			preConditionReader = new Condition();
		}
		preConditionReader.setConditionId("reader");
		return preConditionReader;
	}

	public void setPreConditionReader(Condition preConditionReader) {
		if (preConditionReader == null) {
			preConditionReader = getPreConditionReader();
		}
		preConditionReader.setConditionId("reader");
		this.preConditionReader = preConditionReader;
	}

	@Override
	protected void postImportCopyConditions(CourseEnvironmentMapper envMapper) {
		super.postImportCopyConditions(envMapper);
		postImportCondition(preConditionReader, envMapper);
		postImportCondition(preConditionPoster, envMapper);
		postImportCondition(preConditionModerator, envMapper);
	}

	@Override
	public void postExport(CourseEnvironmentMapper envMapper, boolean backwardsCompatible) {
		super.postExport(envMapper, backwardsCompatible);
		postExportCondition(preConditionReader, envMapper, backwardsCompatible);
		postExportCondition(preConditionPoster, envMapper, backwardsCompatible);
		postExportCondition(preConditionModerator, envMapper, backwardsCompatible);
	}

	@Override
	public void calcAccessAndVisibility(ConditionInterpreter ci, NodeEvaluation nodeEval) {
		if (hasCustomPreConditions()) {
			boolean reader = (getPreConditionReader().getConditionExpression() == null ? true : ci.evaluateCondition(getPreConditionReader()));
			nodeEval.putAccessStatus("reader", reader);
			boolean poster = (getPreConditionPoster().getConditionExpression() == null ? true : ci.evaluateCondition(getPreConditionPoster()));
			nodeEval.putAccessStatus("poster", poster);
			boolean moderator = (getPreConditionModerator().getConditionExpression() == null ? true : ci
					.evaluateCondition(getPreConditionModerator()));
			nodeEval.putAccessStatus("moderator", moderator);

			boolean visible = (getPreConditionVisibility().getConditionExpression() == null ? true : ci
					.evaluateCondition(getPreConditionVisibility()));
			nodeEval.setVisible(visible);
		} else {
			super.calcAccessAndVisibility(ci, nodeEval);
		}
	}
	
	@Override
	public List<ConditionExpression> getConditionExpressions() {
		if (hasCustomPreConditions()) {
			List<ConditionExpression> retVal;
			List<ConditionExpression> parentsConditions = super.getConditionExpressions();
			if (!parentsConditions.isEmpty()) {
				retVal = new ArrayList<>(parentsConditions);
			} else {
				retVal = new ArrayList<>();
			}
			//
			String coS = getPreConditionModerator().getConditionExpression();
			if (coS != null && !coS.equals("")) {
				// an active condition is defined
				ConditionExpression ce = new ConditionExpression(getPreConditionModerator().getConditionId());
				ce.setExpressionString(getPreConditionModerator().getConditionExpression());
				retVal.add(ce);
			}
			coS = getPreConditionPoster().getConditionExpression();
			if (coS != null && !coS.equals("")) {
				// an active condition is defined
				ConditionExpression ce = new ConditionExpression(getPreConditionPoster().getConditionId());
				ce.setExpressionString(getPreConditionPoster().getConditionExpression());
				retVal.add(ce);
			}
			coS = getPreConditionReader().getConditionExpression();
			if (coS != null && !coS.equals("")) {
				// an active condition is defined
				ConditionExpression ce = new ConditionExpression(getPreConditionReader().getConditionId());
				ce.setExpressionString(getPreConditionReader().getConditionExpression());
				retVal.add(ce);
			}
			return retVal;
		}

		return super.getConditionExpressions();
	}

	@Override
	public void exportNode(File exportDirectory, ICourse course) {
		RepositoryEntry re = getReferencedRepositoryEntry();
		if (re == null) return;
		// build current export ZIP for feed learning resource
		FeedManager.getInstance().getFeedArchive(re.getOlatResource());
		// trigger resource file export
		File fExportDirectory = new File(exportDirectory, getIdent());
		fExportDirectory.mkdirs();
		RepositoryEntryImportExport reie = new RepositoryEntryImportExport(re, fExportDirectory);
		reie.exportDoExport();
	}
	
	@Override
	public void importNode(File importDirectory, ICourse course, Identity owner, Organisation organisation, Locale locale, boolean withReferences) {
		if(withReferences) {
			RepositoryHandler handler = RepositoryHandlerFactory.getInstance().getRepositoryHandler(getResourceablTypeName());
			importFeed(handler, importDirectory, owner, organisation, locale);
		} else {
			removeReference(getModuleConfiguration());
		}
	}

	private void importFeed(RepositoryHandler handler, File importDirectory, Identity owner, Organisation organisation, Locale locale) {
		RepositoryEntryImportExport rie = new RepositoryEntryImportExport(importDirectory, getIdent());
		if (rie.anyExportedPropertiesAvailable()) {
			RepositoryEntry re = handler.importResource(owner, rie.getInitialAuthor(), rie.getDisplayName(),
				rie.getDescription(), false, organisation, locale, rie.importGetExportedFile(), null);
			setReference(getModuleConfiguration(), re);
		} else {
			removeReference(getModuleConfiguration());
		}
	}
}
