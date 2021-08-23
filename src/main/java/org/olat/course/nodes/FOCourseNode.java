/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipOutputStream;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerCallback;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.resource.OresHelper;
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
import org.olat.course.nodes.fo.FOCourseNodeEditController;
import org.olat.course.nodes.fo.FOCourseNodeRunController;
import org.olat.course.nodes.fo.FOPeekviewController;
import org.olat.course.nodes.fo.FOPreviewController;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.properties.PersistingCoursePropertyManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.ForumCallback;
import org.olat.modules.fo.ForumModule;
import org.olat.modules.fo.archiver.ForumArchive;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntry;

/**
 * Initial Date: Feb 9, 2004
 * 
 * @author Mike Stock Comment:
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen
 *         GmbH</a>)
 */
public class FOCourseNode extends AbstractAccessableCourseNode {

	private static final Logger log = Tracing.createLoggerFor(FOCourseNode.class);

	private static final long serialVersionUID = 2281715263255594865L;

	@SuppressWarnings("deprecation")
	private static final String PACKAGE_FO = Util.getPackageName(FOCourseNodeRunController.class);

	public static final String TYPE = "fo";

	private static final int CURRENT_VERSION = 5;
	public static final String CONFIG_FORUM_KEY = "forumKey";
	public static final String CONFIG_PSEUDONYM_POST_ALLOWED = "pseudonym.post.allowed";
	public static final String CONFIG_PSEUDONYM_POST_DEFAULT = "pseudonym.post.default";
	public static final String CONFIG_GUEST_POST_ALLOWED = "guest.post.allowed";
	
	private static final String LEGACY_COACH_MODERATE_ALLOWED = "coach.moderate.allowed";
	private static final String LEGACY_COACH_POST_ALLOWED = "coach.post.allowed";
	private static final String LEGACY_PARTICIPANT_POST_ALLOWED = "participant.post.allowed";
	
	private static final NodeRightType MODERATE = NodeRightTypeBuilder.ofIdentifier("moderate")
			.setLabel(FOCourseNodeEditController.class, "edit.moderator")
			.addRole(NodeRightRole.coach, true)
			.build();
	private static final NodeRightType POST = NodeRightTypeBuilder.ofIdentifier("post")
			.setLabel(FOCourseNodeEditController.class, "edit.poster")
			.enableCssClass()
			.addRole(NodeRightRole.coach, true)
			.addRole(NodeRightRole.participant, true)
			.addRole(NodeRightRole.guest, false)
			.build();
	public static final List<NodeRightType> NODE_RIGHT_TYPES = List.of(MODERATE, POST);

	// null means no precondition / always accessible
	private Condition preConditionReader, preConditionPoster, preConditionModerator;

	public FOCourseNode() {
		this(null);
	}
	
	public FOCourseNode(INode parent) {
		super(TYPE, parent);
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			ICourse course, UserCourseEnvironment euce) {
		FOCourseNodeEditController childTabCntrllr = new FOCourseNodeEditController(ureq, wControl, this, course, euce);
		CourseNode chosenNode = course.getEditorTreeModel()
				.getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, euce, childTabCntrllr);
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return hasCustomPreConditions() ? ConditionAccessEditConfig.custom() : ConditionAccessEditConfig.regular(false);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			final UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd) {
		Roles roles = ureq.getUserSession().getRoles();
		Forum theForum = loadOrCreateForum(userCourseEnv.getCourseEnvironment());
		boolean isAdministrator = userCourseEnv.isAdmin();
		boolean isGuestOnly = roles.isGuestOnly();
		// Add message id to business path if nodemcd is available
		if (nodecmd != null) {
			try {
				Long messageId = Long.valueOf(nodecmd);
				BusinessControlFactory bcf = BusinessControlFactory.getInstance();
				BusinessControl businessControl = bcf.createFromString("[Message:" + messageId + "]");
				wControl = bcf.createBusinessWindowControl(businessControl, wControl);
			} catch (NumberFormatException e) {
				// ups, nodecmd is not a message, what the heck is it then?
				log.warn("Could not create message ID from given nodemcd::" + nodecmd, e);
			}
		}

		// for guests, check if posting is allowed
		boolean pseudonymPostAllowed = false;
		boolean defaultPseudonym = false;
		boolean guestPostAllowed = false;
		if (roles.isGuestOnly()) {
			if (hasCustomPreConditions()) {
				String config = getModuleConfiguration().getStringValue(CONFIG_GUEST_POST_ALLOWED);
				guestPostAllowed = "true".equals(config);
			} else {
				guestPostAllowed = CoreSpringFactory.getImpl(NodeRightService.class).isGranted(getModuleConfiguration(), userCourseEnv, POST);
			}
		} else {
			ForumModule forumModule = CoreSpringFactory.getImpl(ForumModule.class);
			String config = getModuleConfiguration().getStringValue(CONFIG_PSEUDONYM_POST_ALLOWED);
			pseudonymPostAllowed = forumModule.isAnonymousPostingWithPseudonymEnabled() && "true".equals(config);
			if (pseudonymPostAllowed) {
				defaultPseudonym = getModuleConfiguration().getBooleanSafe(CONFIG_PSEUDONYM_POST_DEFAULT,
						forumModule.isPseudonymForMessageEnabledByDefault());
			}
		}
		// Create subscription context and run controller
		SubscriptionContext forumSubContext = CourseModule
				.createSubscriptionContext(userCourseEnv.getCourseEnvironment(), this);
		boolean moderator = isModerator(userCourseEnv, nodeSecCallback.getNodeEvaluation());
		boolean poster = isPoster(userCourseEnv, nodeSecCallback.getNodeEvaluation());
		ForumCallback foCallback = userCourseEnv.isCourseReadOnly()
				? new ReadOnlyForumCallback(moderator, isAdministrator, isGuestOnly)
				: new ForumNodeForumCallback(poster, moderator, isAdministrator, isGuestOnly, guestPostAllowed,
						pseudonymPostAllowed, defaultPseudonym, forumSubContext);
		FOCourseNodeRunController forumC = new FOCourseNodeRunController(ureq, wControl, theForum, foCallback, this, userCourseEnv);
		return new NodeRunConstructionResult(forumC);
	}

	public boolean isModerator(UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
		if (hasCustomPreConditions()) {
			return ne != null && ne.isCapabilityAccessible("moderator");
		}
		return CoreSpringFactory.getImpl(NodeRightService.class).isGranted(getModuleConfiguration(), userCourseEnv, MODERATE);
	}
	
	public boolean isPoster(UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
		if (hasCustomPreConditions()) {
			return ne != null && ne.isCapabilityAccessible("poster");
		}
		return CoreSpringFactory.getImpl(NodeRightService.class).isGranted(getModuleConfiguration(), userCourseEnv, POST);
	}

	/**
	 * Private helper method to load the forum from the configuration or create on
	 * if it does not yet exist
	 * 
	 * @param userCourseEnv
	 * @return the loaded forum
	 */
	public Forum loadOrCreateForum(final CourseEnvironment courseEnv) {
		CourseNode parent = this.getParent() instanceof CourseNode? (CourseNode)this.getParent(): null;
		updateModuleConfigDefaults(false, parent);

		Forum forum = null;
		List<Property> forumKeyProps = courseEnv.getCoursePropertyManager().findCourseNodeProperties(this, null, null,
				CONFIG_FORUM_KEY);
		if (forumKeyProps == null || forumKeyProps.isEmpty()) {
			forum = createForum(courseEnv);
		} else if (forumKeyProps.size() == 1) {
			forum = loadForum(courseEnv, forumKeyProps.get(0));
		} else if (forumKeyProps.size() > 1) {
			forum = saveMultiForums(courseEnv);
		}
		return forum;
	}

	private Forum saveMultiForums(final CourseEnvironment courseEnv) {
		final ForumManager fom = CoreSpringFactory.getImpl(ForumManager.class);
		final OLATResourceable courseNodeResourceable = OresHelper.createOLATResourceableInstance(FOCourseNode.class,
				Long.valueOf(getIdent()));
		return CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(courseNodeResourceable,
				new SyncerCallback<Forum>() {
					@Override
					public Forum execute() {
						List<Property> forumKeyProps = courseEnv.getCoursePropertyManager()
								.findCourseNodeProperties(FOCourseNode.this, null, null, CONFIG_FORUM_KEY);
						Forum masterForum;
						if (forumKeyProps.size() == 1) {
							masterForum = loadForum(courseEnv, forumKeyProps.get(0));
						} else if (forumKeyProps.size() > 1) {
							Long masterForumKey = forumKeyProps.get(0).getLongValue();
							List<Long> forumsToMerge = new ArrayList<>();
							for (int i = 1; i < forumKeyProps.size(); i++) {
								forumsToMerge.add(forumKeyProps.get(i).getLongValue());
							}
							fom.mergeForums(masterForumKey, forumsToMerge);
							masterForum = fom.loadForum(masterForumKey);
							for (int i = 1; i < forumKeyProps.size(); i++) {
								courseEnv.getCoursePropertyManager().deleteProperty(forumKeyProps.get(i));
							}
						} else {
							masterForum = null;
						}
						return masterForum;
					}
				});
	}

	private Forum loadForum(CourseEnvironment courseEnv, Property prop) {
		final ForumManager fom = CoreSpringFactory.getImpl(ForumManager.class);
		Long forumKey = prop.getLongValue();
		Forum forum = fom.loadForum(forumKey);
		if (forum == null) {
			throw new OLATRuntimeException(FOCourseNode.class,
					"Tried to load forum with key " + forumKey.longValue() + " in course "
							+ courseEnv.getCourseResourceableId() + " for node " + getIdent()
							+ " as defined in course node property but forum manager could not load forum.",
					null);
		}
		return forum;
	}

	private Forum createForum(final CourseEnvironment courseEnv) {
		final ForumManager fom = CoreSpringFactory.getImpl(ForumManager.class);
		// creates resourceable from FOCourseNode.class and the current node id as key
		OLATResourceable courseNodeResourceable = OresHelper.createOLATResourceableInstance(FOCourseNode.class, Long.valueOf(getIdent()));
		// o_clusterOK by:ld
		return CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(courseNodeResourceable,
				new SyncerCallback<Forum>() {
					@Override
					public Forum execute() {
						Forum forum;
						CoursePropertyManager cpm = courseEnv.getCoursePropertyManager();
						Property forumKeyProperty = cpm.findCourseNodeProperty(FOCourseNode.this, null, null,
								CONFIG_FORUM_KEY);
						if (forumKeyProperty == null) {
							// First call of forum, create new forum and save forum key as property
							forum = fom.addAForum();
							Long forumKey = forum.getKey();
							forumKeyProperty = cpm.createCourseNodePropertyInstance(FOCourseNode.this, null, null,
									CONFIG_FORUM_KEY, null, forumKey, null, null);
							cpm.saveProperty(forumKeyProperty);
						} else {
							// Forum does already exist, load forum with key from properties
							Long forumKey = forumKeyProperty.getLongValue();
							forum = fom.loadForum(forumKey);
							if (forum == null) {
								throw new OLATRuntimeException(FOCourseNode.class, "Tried to load forum with key "
										+ forumKey.longValue() + " in course " + courseEnv.getCourseResourceableId()
										+ " for node " + getIdent()
										+ " as defined in course node property but forum manager could not load forum.",
										null);
							}
						}
						return forum;
					}
				});
	}

	@Override
	public void calcAccessAndVisibility(ConditionInterpreter ci, NodeEvaluation nodeEval) {
		if (hasCustomPreConditions()) {
			boolean reader = (getPreConditionReader().getConditionExpression() == null ? true
					: ci.evaluateCondition(getPreConditionReader()));
			nodeEval.putAccessStatus("reader", reader);
			boolean poster = (getPreConditionPoster().getConditionExpression() == null ? true
					: ci.evaluateCondition(getPreConditionPoster()));
			nodeEval.putAccessStatus("poster", poster);
			boolean moderator = (getPreConditionModerator().getConditionExpression() == null ? true
					: ci.evaluateCondition(getPreConditionModerator()));
			nodeEval.putAccessStatus("moderator", moderator);

			boolean visible = (getPreConditionVisibility().getConditionExpression() == null ? true
					: ci.evaluateCondition(getPreConditionVisibility()));
			nodeEval.setVisible(visible);
		} else {
			super.calcAccessAndVisibility(ci, nodeEval);
		}
	}

	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback) {
		return new FOPreviewController(ureq, wControl, this, userCourseEnv, nodeSecCallback);
	}

	@Override
	public Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, boolean small) {
		if (nodeSecCallback.isAccessible()) {
			// Create a forum peekview controller that shows the latest two messages
			Forum theForum = loadOrCreateForum(userCourseEnv.getCourseEnvironment());
			RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			return new FOPeekviewController(ureq, wControl, courseEntry, theForum, getIdent(), 3);
		}
		// use standard peekview
		return super.createPeekViewRunController(ureq, wControl, userCourseEnv, nodeSecCallback, small);
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
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null) {
			return oneClickStatusCache[0];
		}

		return StatusDescription.NOERROR;
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;
		// only here we know which translator to take for translating condition error
		// messages
		List<StatusDescription> sds = isConfigValidWithTranslator(cev, PACKAGE_FO, getConditionExpressions());
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
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
	public boolean archiveNodeData(Locale locale, ICourse course, ArchiveOptions options, ZipOutputStream exportStream,
			String archivePath, String charset) {
		CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
		Property forumKeyProperty = cpm.findCourseNodeProperty(this, null, null, CONFIG_FORUM_KEY);
		if (forumKeyProperty == null) {
			return false;
		}
		Long forumKey = forumKeyProperty.getLongValue();
		if (CoreSpringFactory.getImpl(ForumManager.class).countThreadsByForumID(forumKey) <= 0) {
			return false;
		}

		String forumName = "forum_" + Formatter.makeStringFilesystemSave(getShortTitle()) + "_"
				+ Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()));
		forumName = ZipUtil.concat(archivePath, forumName);

		try {
			Forum forum = loadOrCreateForum(course.getCourseEnvironment());
			ForumArchive archiver = new ForumArchive(forum, null, locale, null);
			archiver.export(forumName + ".docx", exportStream);
		} catch (IOException e) {
			log.error("", e);
		}
		return true;
	}

	@Override
	public String informOnDelete(Locale locale, ICourse course) {
		CoursePropertyManager cpm = PersistingCoursePropertyManager.getInstance(course);
		Property forumKeyProperty = cpm.findCourseNodeProperty(this, null, null, CONFIG_FORUM_KEY);
		if (forumKeyProperty == null)
			return null; // no forum created yet
		return new PackageTranslator(PACKAGE_FO, locale).translate("warn.forumdelete");
	}

	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);

		// mark the subscription to this node as deleted
		SubscriptionContext forumSubContext = CourseModule
				.createTechnicalSubscriptionContext(course.getCourseEnvironment(), this);
		CoreSpringFactory.getImpl(NotificationsManager.class).delete(forumSubContext);

		// delete the forum, if there is one (is created on demand only)
		CoursePropertyManager cpm = PersistingCoursePropertyManager.getInstance(course);
		Property forumKeyProperty = cpm.findCourseNodeProperty(this, null, null, CONFIG_FORUM_KEY);
		if (forumKeyProperty != null) {
			Long forumKey = forumKeyProperty.getLongValue();
			CoreSpringFactory.getImpl(ForumManager.class).deleteForum(forumKey); // delete the forum
			cpm.deleteProperty(forumKeyProperty); // delete the property
		}
	}

	/**
	 * Update the module configuration to have all mandatory configuration flags set
	 * to usefull default values
	 * @param isNewNode true: an initial configuration is set; false: upgrading from
	 *                  previous node configuration version, set default to maintain
	 *                  previous behaviour
	 */
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent) {
		ModuleConfiguration config = getModuleConfiguration();
		int version = config.getConfigurationVersion();

		if (isNewNode) {
			ForumModule forumModule = CoreSpringFactory.getImpl(ForumModule.class);
			boolean pseudonymAllowed = forumModule.isAnonymousPostingWithPseudonymEnabled()
					&& forumModule.isPseudonymForCourseEnabledByDefault();
			config.setStringValue(CONFIG_PSEUDONYM_POST_ALLOWED, pseudonymAllowed ? "true" : "false");
			boolean pseudonymDefault = pseudonymAllowed && forumModule.isPseudonymForMessageEnabledByDefault();
			config.setStringValue(CONFIG_PSEUDONYM_POST_DEFAULT, pseudonymDefault ? "true" : "false");
		}
		if (isNewNode || version < 2) {
			config.setBooleanEntry(NodeEditController.CONFIG_STARTPAGE, Boolean.FALSE.booleanValue());
		}
		if (version < 3) {
			if (config.getStringValue(CONFIG_PSEUDONYM_POST_ALLOWED) == null) {
				config.setStringValue(CONFIG_PSEUDONYM_POST_ALLOWED, "false");
			}
			if (config.getStringValue(CONFIG_GUEST_POST_ALLOWED) == null) {
				config.setStringValue(CONFIG_GUEST_POST_ALLOWED, "false");
			}
		}
		if (version < 4) {
			config.setBooleanEntry(LEGACY_COACH_MODERATE_ALLOWED, true);
			config.setBooleanEntry(LEGACY_COACH_POST_ALLOWED, true);
			config.setBooleanEntry(LEGACY_PARTICIPANT_POST_ALLOWED, true);
			removeDefaultPreconditions();
		}
		if (version < 5 && config.has(CONFIG_GUEST_POST_ALLOWED)) {
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
			if (config.getBooleanSafe(CONFIG_GUEST_POST_ALLOWED)) {
				postRoles.add(NodeRightRole.guest);
			}
			nodeRightService.setRoleGrants(postRight, postRoles);
			nodeRightService.setRight(config, postRight);
			// Remove legacy
			config.remove(LEGACY_COACH_MODERATE_ALLOWED);
			config.remove(LEGACY_COACH_POST_ALLOWED);
			config.remove(LEGACY_PARTICIPANT_POST_ALLOWED);
		}

		// Clean up
		config.remove(NodeEditController.CONFIG_INTEGRATION);

		config.setConfigurationVersion(CURRENT_VERSION);
	}

	/**
	 * We don't want to have custom preConditions. So we keep these preConditions
	 * only, if they have some special configs. Otherwise we delete them and use the
	 * regular configs.
	 */
	private void removeDefaultPreconditions() {
		if (hasCustomPreConditions()) {
			preConditionPoster = null;

			boolean defaultPreconditions = (preConditionModerator == null || ( 
					!preConditionModerator.isExpertMode()
				&& preConditionModerator.isEasyModeCoachesAndAdmins()
				&& !preConditionModerator.isEasyModeAlwaysAllowCoachesAndAdmins()
				&& !preConditionModerator.isAssessmentMode()
				&& !preConditionModerator.isAssessmentModeViewResults())
			)
			&&
			(preConditionPoster == null || (
				!preConditionPoster.isExpertMode()
				&& !preConditionPoster.isEasyModeCoachesAndAdmins()
				&& !preConditionPoster.isEasyModeAlwaysAllowCoachesAndAdmins()
				&& !preConditionPoster.isAssessmentMode()
				&& !preConditionPoster.isAssessmentModeViewResults())
			)
			&&
			(preConditionReader == null || (
				!preConditionReader.isExpertMode()
				&& !preConditionReader.isEasyModeCoachesAndAdmins()
				&& !preConditionReader.isEasyModeAlwaysAllowCoachesAndAdmins()
				&& !preConditionReader.isAssessmentMode()
				&& !preConditionReader.isAssessmentModeViewResults())
			);
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
	public List<NodeRightType> getNodeRightTypes() {
		return NODE_RIGHT_TYPES;
	}
}

class ReadOnlyForumCallback implements ForumCallback {

	private final boolean isModerator;
	private final boolean isOlatAdmin;
	private final boolean isGuestOnly;

	public ReadOnlyForumCallback(boolean isModerator, boolean isOlatAdmin, boolean isGuestOnly) {
		this.isModerator = isModerator;
		this.isOlatAdmin = isOlatAdmin;
		this.isGuestOnly = isGuestOnly;
	}

	@Override
	public boolean mayUsePseudonym() {
		return false;
	}

	@Override
	public boolean mayOpenNewThread() {
		return false;
	}

	@Override
	public boolean mayReplyMessage() {
		return false;
	}

	@Override
	public boolean mayEditOwnMessage() {
		return false;
	}

	@Override
	public boolean mayDeleteOwnMessage() {
		return false;
	}

	@Override
	public boolean mayEditMessageAsModerator() {
		return false;
	}

	@Override
	public boolean mayDeleteMessageAsModerator() {
		return false;
	}

	@Override
	public boolean mayArchiveForum() {
		return !isGuestOnly;
	}

	@Override
	public boolean mayFilterForUser() {
		if (isGuestOnly)
			return false;
		return isModerator || isOlatAdmin;
	}

	@Override
	public SubscriptionContext getSubscriptionContext() {
		return null;
	}
}

class ForumNodeForumCallback implements ForumCallback {

	private final boolean isPoster;
	private final boolean isModerator;
	private final boolean isOlatAdmin;
	private final boolean isGuestOnly;
	private final boolean guestPostAllowed;
	private final boolean anonymousPostAllowed;
	private final boolean anonymousPostDefault;
	private final SubscriptionContext subscriptionContext;

	public ForumNodeForumCallback(boolean isPoster, boolean isModerator, boolean isOlatAdmin, boolean isGuestOnly,
			boolean guestPostAllowed, boolean anonymousPostAllowed, boolean anonymousPostDefault,
			SubscriptionContext subscriptionContext) {
		this.isPoster = isPoster;
		this.isModerator = isModerator;
		this.isOlatAdmin = isOlatAdmin;
		this.isGuestOnly = isGuestOnly;
		this.guestPostAllowed = guestPostAllowed;
		this.anonymousPostAllowed = anonymousPostAllowed;
		this.anonymousPostDefault = anonymousPostDefault;
		this.subscriptionContext = subscriptionContext;
	}

	@Override
	public boolean mayUsePseudonym() {
		if (isGuestOnly)
			return false;
		return anonymousPostAllowed;
	}

	@Override
	public boolean pseudonymAsDefault() {
		return anonymousPostDefault;
	}

	@Override
	public boolean mayOpenNewThread() {
		if (isGuestOnly && guestPostAllowed) return true;
		
		return isPoster || isModerator || isOlatAdmin;
	}

	@Override
	public boolean mayReplyMessage() {
		if (isGuestOnly && guestPostAllowed) return true;
		
		return isPoster || isModerator || isOlatAdmin;
	}

	@Override
	public boolean mayEditOwnMessage() {
		if (isGuestOnly && !guestPostAllowed) return false;
		
		return true;
	}

	@Override
	public boolean mayDeleteOwnMessage() {
		if (isGuestOnly && !guestPostAllowed) return false;
	
		return true;
	}

	@Override
	public boolean mayEditMessageAsModerator() {
		if (isGuestOnly) return false;
		
		return isModerator || isOlatAdmin;
	}

	@Override
	public boolean mayDeleteMessageAsModerator() {
		if (isGuestOnly) return false;
		
		return isModerator || isOlatAdmin;
	}

	@Override
	public boolean mayArchiveForum() {
		return !isGuestOnly;
	}

	@Override
	public boolean mayFilterForUser() {
		if (isGuestOnly) return false;
		
		return isModerator || isOlatAdmin;
	}

	@Override
	public SubscriptionContext getSubscriptionContext() {
		// do not offer subscription to forums for guests
		return isGuestOnly ? null : subscriptionContext;
	}
	
}