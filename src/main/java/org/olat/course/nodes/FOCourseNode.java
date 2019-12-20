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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipOutputStream;

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
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerCallback;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.condition.Condition;
import org.olat.course.condition.interpreter.ConditionExpression;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.nodes.fo.FOCourseNodeEditController;
import org.olat.course.nodes.fo.FOCourseNodeRunController;
import org.olat.course.nodes.fo.FOPeekviewController;
import org.olat.course.nodes.fo.FOPreviewController;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.course.properties.PersistingCoursePropertyManager;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.ForumCallback;
import org.olat.modules.fo.ForumModule;
import org.olat.modules.fo.archiver.ForumArchiveManager;
import org.olat.modules.fo.archiver.formatters.ForumStreamedRTFFormatter;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntry;

/**
 * Initial Date: Feb 9, 2004
 * 
 * @author Mike Stock Comment:
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class FOCourseNode extends AbstractAccessableCourseNode {
	
	private static final Logger log = Tracing.createLoggerFor(FOCourseNode.class);

	private static final long serialVersionUID = 2281715263255594865L;
	private static final String PACKAGE_FO = Util.getPackageName(FOCourseNodeRunController.class);
	private static final String TYPE = "fo";
	private Condition preConditionReader, preConditionPoster, preConditionModerator;
	// null means no precondition / always accessible
	public static final String FORUM_KEY = "forumKey";

	/**
	 * Default constructor to create a forum course node
	 */
	public FOCourseNode() {
		super(TYPE);
		updateModuleConfigDefaults(true);
		// restrict moderator access to course admins and course coaches
		preConditionModerator = getPreConditionModerator();
		preConditionModerator.setEasyModeCoachesAndAdmins(true);
		preConditionModerator.setConditionExpression(preConditionModerator.getConditionFromEasyModeConfiguration());
		preConditionModerator.setExpertMode(false);
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#createEditController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl, org.olat.course.ICourse)
	 */
	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		updateModuleConfigDefaults(false);
		FOCourseNodeEditController childTabCntrllr = new FOCourseNodeEditController(ureq, wControl, this, course, euce);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, chosenNode, euce, childTabCntrllr);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			final UserCourseEnvironment userCourseEnv, NodeEvaluation ne, String nodecmd) {
		updateModuleConfigDefaults(false);
		Roles roles = ureq.getUserSession().getRoles();
		Forum theForum = loadOrCreateForum(userCourseEnv.getCourseEnvironment());
		boolean isAdministrator = userCourseEnv.isAdmin();
		boolean isGuestOnly = roles.isGuestOnly();
		// Add message id to business path if nodemcd is available
		if (nodecmd != null) {
			try {
				Long messageId = Long.valueOf(nodecmd);
				BusinessControlFactory bcf =  BusinessControlFactory.getInstance();
				BusinessControl businessControl = bcf.createFromString("[Message:"+messageId+"]");
				wControl = bcf.createBusinessWindowControl(businessControl, wControl);
			} catch (NumberFormatException e) {
				// ups, nodecmd is not a message, what the heck is it then?
				log.warn("Could not create message ID from given nodemcd::" + nodecmd, e);
			}
		}

		//for guests, check if posting is allowed
		boolean pseudonymPostAllowed = false;
		boolean defaultPseudonym = false;
		boolean guestPostAllowed = false;
		if(roles.isGuestOnly()) {
			String config = getModuleConfiguration().getStringValue(FOCourseNodeEditController.GUEST_POST_ALLOWED);
			guestPostAllowed = "true".equals(config);
		} else {
			ForumModule forumModule = CoreSpringFactory.getImpl(ForumModule.class);
			String config = getModuleConfiguration().getStringValue(FOCourseNodeEditController.PSEUDONYM_POST_ALLOWED);
			pseudonymPostAllowed = forumModule.isAnonymousPostingWithPseudonymEnabled()
					&& "true".equals(config);
			if(pseudonymPostAllowed) {
				defaultPseudonym = getModuleConfiguration().getBooleanSafe(FOCourseNodeEditController.PSEUDONYM_POST_DEFAULT,
						forumModule.isPseudonymForMessageEnabledByDefault());
			}
		}
		// Create subscription context and run controller
		SubscriptionContext forumSubContext = CourseModule.createSubscriptionContext(userCourseEnv.getCourseEnvironment(), this);
		ForumCallback foCallback = userCourseEnv.isCourseReadOnly() ?
				new ReadOnlyForumCallback(ne, isAdministrator, isGuestOnly) :
				new ForumNodeForumCallback(ne, isAdministrator, isGuestOnly, guestPostAllowed, pseudonymPostAllowed, defaultPseudonym, forumSubContext);
		FOCourseNodeRunController forumC = new FOCourseNodeRunController(ureq, wControl, theForum, foCallback, this);
		return new NodeRunConstructionResult(forumC);
	}

	/**
	 * Private helper method to load the forum from the configuration or create on
	 * if it does not yet exist
	 * 
	 * @param userCourseEnv
	 * @return the loaded forum
	 */
	public Forum loadOrCreateForum(final CourseEnvironment courseEnv) {
		updateModuleConfigDefaults(false);				

		Forum forum = null;	
		List<Property> forumKeyProps = courseEnv.getCoursePropertyManager()
				.findCourseNodeProperties(this, null, null, FORUM_KEY);
		if(forumKeyProps == null || forumKeyProps.isEmpty()) {
			forum = createForum(courseEnv);
		} else if(forumKeyProps.size() == 1) {
			forum = loadForum(courseEnv, forumKeyProps.get(0));
		} else if (forumKeyProps.size() > 1) {
			forum = saveMultiForums(courseEnv);
		}
		return forum;
	}
	
	private Forum saveMultiForums(final CourseEnvironment courseEnv) {
		final ForumManager fom = CoreSpringFactory.getImpl(ForumManager.class);
		final OLATResourceable courseNodeResourceable = OresHelper.createOLATResourceableInstance(FOCourseNode.class, Long.valueOf(getIdent()));
		return CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(courseNodeResourceable, new SyncerCallback<Forum>(){
			@Override
			public Forum execute() {
				List<Property> forumKeyProps = courseEnv.getCoursePropertyManager()
						.findCourseNodeProperties(FOCourseNode.this, null, null, FORUM_KEY);
				Forum masterForum;
				if(forumKeyProps.size() == 1) {
					masterForum = loadForum(courseEnv, forumKeyProps.get(0));
				} else if(forumKeyProps.size() > 1) {
					Long masterForumKey = forumKeyProps.get(0).getLongValue();
					List<Long> forumsToMerge = new ArrayList<>();
					for(int i=1; i<forumKeyProps.size(); i++) {
						forumsToMerge.add(forumKeyProps.get(i).getLongValue());
					}
					fom.mergeForums(masterForumKey, forumsToMerge);
					masterForum = fom.loadForum(masterForumKey);
					for(int i=1; i<forumKeyProps.size(); i++) {
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
			throw new OLATRuntimeException(FOCourseNode.class, "Tried to load forum with key " + forumKey.longValue() + " in course "
					+ courseEnv.getCourseResourceableId() + " for node " + getIdent()
					+ " as defined in course node property but forum manager could not load forum.", null);
		}
		return forum;
	}
	
	private Forum createForum(final CourseEnvironment courseEnv) {
		final ForumManager fom = CoreSpringFactory.getImpl(ForumManager.class);
		//creates resourceable from FOCourseNode.class and the current node id as key
		OLATResourceable courseNodeResourceable = OresHelper.createOLATResourceableInstance(FOCourseNode.class, new Long(getIdent()));
		//o_clusterOK by:ld 
		return CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(courseNodeResourceable, new SyncerCallback<Forum>(){
			@Override
			public Forum execute() {
				Forum forum;
				CoursePropertyManager cpm = courseEnv.getCoursePropertyManager();
				Property forumKeyProperty = cpm.findCourseNodeProperty(FOCourseNode.this, null, null, FORUM_KEY);			  
				if (forumKeyProperty == null) {
					// First call of forum, create new forum and save forum key as property			  	
					forum = fom.addAForum();
					Long forumKey = forum.getKey();
					forumKeyProperty = cpm.createCourseNodePropertyInstance(FOCourseNode.this, null, null, FORUM_KEY, null, forumKey, null, null);
					cpm.saveProperty(forumKeyProperty);	
				} else {
					// Forum does already exist, load forum with key from properties
					Long forumKey = forumKeyProperty.getLongValue();
					forum = fom.loadForum(forumKey);
					if (forum == null) {
						throw new OLATRuntimeException(FOCourseNode.class, "Tried to load forum with key " + forumKey.longValue() + " in course "
								+ courseEnv.getCourseResourceableId() + " for node " + getIdent()
								+ " as defined in course node property but forum manager could not load forum.", null);
					}
				}
				return forum;
			}
		});
	}

	@Override
	protected void calcAccessAndVisibility(ConditionInterpreter ci, NodeEvaluation nodeEval) {
		// evaluate the preconditions
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
	}

	/**
	 * Implementation of the previewController for forumnode
	 */
	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
		return new FOPreviewController(ureq, wControl, ne);
	}

	@Override
	public Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			NodeEvaluation ne) {
		if (ne.isAtLeastOneAccessible()) {
			// Create a forum peekview controller that shows the latest two messages		
			Forum theForum = loadOrCreateForum(userCourseEnv.getCourseEnvironment());
			RepositoryEntry courseEntry = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
			return new FOPeekviewController(ureq, wControl, courseEntry, theForum, getIdent(), 3);		
		} else {
			// use standard peekview
			return super.createPeekViewRunController(ureq, wControl, userCourseEnv, ne);
		}
	}

	/**
	 * @return Returns the preConditionModerator.
	 */
	public Condition getPreConditionModerator() {
		if (preConditionModerator == null) {
			preConditionModerator = new Condition();
		}
		preConditionModerator.setConditionId("moderator");
		return preConditionModerator;
	}

	/**
	 * @param preConditionModerator The preConditionModerator to set.
	 */
	public void setPreConditionModerator(Condition preConditionModerator) {
		if (preConditionModerator == null) {
			preConditionModerator = getPreConditionModerator();
		}
		preConditionModerator.setConditionId("moderator");
		this.preConditionModerator = preConditionModerator;
	}

	/**
	 * @return Returns the preConditionPoster.
	 */
	public Condition getPreConditionPoster() {
		if (preConditionPoster == null) {
			preConditionPoster = new Condition();
		}
		preConditionPoster.setConditionId("poster");
		return preConditionPoster;
	}

	/**
	 * @param preConditionPoster The preConditionPoster to set.
	 */
	public void setPreConditionPoster(Condition preConditionPoster) {
		if (preConditionPoster == null) {
			preConditionPoster = getPreConditionPoster();
		}
		preConditionPoster.setConditionId("poster");
		this.preConditionPoster = preConditionPoster;
	}

	/**
	 * @return Returns the preConditionReader.
	 */
	public Condition getPreConditionReader() {
		if (preConditionReader == null) {
			preConditionReader = new Condition();
		}
		preConditionReader.setConditionId("reader");
		return preConditionReader;
	}

	/**
	 * @param preConditionReader The preConditionReader to set.
	 */
	public void setPreConditionReader(Condition preConditionReader) {
		if (preConditionReader == null) {
			preConditionReader = getPreConditionReader();
		}
		preConditionReader.setConditionId("reader");
		this.preConditionReader = preConditionReader;
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#isConfigValid()
	 */
	@Override
	public StatusDescription isConfigValid() {
		/*
		 * first check the one click cache
		 */
		if(oneClickStatusCache!=null) {
			return oneClickStatusCache[0];
		}
		
		return StatusDescription.NOERROR;
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#isConfigValid(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;
		//only here we know which translator to take for translating condition error messages
		List<StatusDescription> sds = isConfigValidWithTranslator(cev, PACKAGE_FO, getConditionExpressions());
		oneClickStatusCache = StatusDescriptionHelper.sort(sds);
		return oneClickStatusCache;
	}
	
	/**
	 * @see org.olat.course.nodes.CourseNode#getReferencedRepositoryEntry()
	 */
	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}

	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}

	@Override
	public boolean archiveNodeData(Locale locale, ICourse course, ArchiveOptions options,
			ZipOutputStream exportStream, String archivePath, String charset) {
		CoursePropertyManager cpm = course.getCourseEnvironment().getCoursePropertyManager();
		Property forumKeyProperty = cpm.findCourseNodeProperty(this, null, null, FORUM_KEY);
		if(forumKeyProperty == null) {
			return false;
		}
		Long forumKey = forumKeyProperty.getLongValue();
		if(CoreSpringFactory.getImpl(ForumManager.class).countThreadsByForumID(forumKey) <= 0) {
			return false;
		}
		
		String forumName = "forum_" + Formatter.makeStringFilesystemSave(getShortTitle())
				+ "_" + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()));
		forumName = ZipUtil.concat(archivePath, forumName);
		ForumStreamedRTFFormatter rtff = new ForumStreamedRTFFormatter(exportStream, forumName, false, locale);	
		CoreSpringFactory.getImpl(ForumArchiveManager.class).applyFormatter(rtff, forumKey, null);
		return true;
	}

	@Override
	public String informOnDelete(Locale locale, ICourse course) {
		CoursePropertyManager cpm = PersistingCoursePropertyManager.getInstance(course);
		Property forumKeyProperty = cpm.findCourseNodeProperty(this, null, null, FORUM_KEY);
		if (forumKeyProperty == null) return null; // no forum created yet
		return new PackageTranslator(PACKAGE_FO, locale).translate("warn.forumdelete");
	}

	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);
		
		// mark the subscription to this node as deleted
		SubscriptionContext forumSubContext = CourseModule.createTechnicalSubscriptionContext(course.getCourseEnvironment(), this);
		CoreSpringFactory.getImpl(NotificationsManager.class).delete(forumSubContext);

		// delete the forum, if there is one (is created on demand only)
		CoursePropertyManager cpm = PersistingCoursePropertyManager.getInstance(course);
		Property forumKeyProperty = cpm.findCourseNodeProperty(this, null, null, FORUM_KEY);
		if (forumKeyProperty != null) {
			Long forumKey = forumKeyProperty.getLongValue();
			CoreSpringFactory.getImpl(ForumManager.class).deleteForum(forumKey); // delete the forum
			cpm.deleteProperty(forumKeyProperty); // delete the property
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
		if(isNewNode) {
			ForumModule forumModule = CoreSpringFactory.getImpl(ForumModule.class);
			boolean pseudonymAllowed = forumModule.isAnonymousPostingWithPseudonymEnabled()
					&& forumModule.isPseudonymForCourseEnabledByDefault();
			config.setStringValue(FOCourseNodeEditController.PSEUDONYM_POST_ALLOWED, pseudonymAllowed ? "true" : "false");
			boolean pseudonymDefault = pseudonymAllowed
					&& forumModule.isPseudonymForMessageEnabledByDefault();
			config.setStringValue(FOCourseNodeEditController.PSEUDONYM_POST_DEFAULT, pseudonymDefault ? "true" : "false");
			config.setStringValue(FOCourseNodeEditController.GUEST_POST_ALLOWED, "false");
		}
		if (isNewNode || config.getConfigurationVersion() < 2) {
			// use defaults for new course building blocks
			config.setBooleanEntry(NodeEditController.CONFIG_STARTPAGE, Boolean.FALSE.booleanValue());
			config.setConfigurationVersion(2);
		}
		if (config.getConfigurationVersion() < 3) {
			if(config.getStringValue(FOCourseNodeEditController.PSEUDONYM_POST_ALLOWED) == null) {
				config.setStringValue(FOCourseNodeEditController.PSEUDONYM_POST_ALLOWED, "false");	
			}
			if(config.getStringValue(FOCourseNodeEditController.GUEST_POST_ALLOWED) == null) {
				config.setStringValue(FOCourseNodeEditController.GUEST_POST_ALLOWED, "false");
			}
			config.setConfigurationVersion(3);
		}
		// else node is up-to-date - nothing to do
		config.remove(NodeEditController.CONFIG_INTEGRATION);
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
		List<ConditionExpression> retVal;
		List<ConditionExpression> parentsConditions = super.getConditionExpressions();
		if (!parentsConditions.isEmpty()) {
			retVal = new ArrayList<>(parentsConditions);
		}else {
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
		//
		return retVal;
	}
}

class ReadOnlyForumCallback implements ForumCallback {

	private final boolean isGuestOnly;
	private final boolean isOlatAdmin;
	private final NodeEvaluation ne;
	
	public ReadOnlyForumCallback(NodeEvaluation ne, boolean isOlatAdmin, boolean isGuestOnly) {
		this.ne = ne;
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
		if (isGuestOnly) return false;
		return ne.isCapabilityAccessible("moderator") || isOlatAdmin;
	}

	@Override
	public SubscriptionContext getSubscriptionContext() {
		return null;
	}
}

/**
 * 
 * Description:<br>
 * ForumCallback implementation.
 * 
 */
class ForumNodeForumCallback implements ForumCallback {

	private final NodeEvaluation ne;
	private final boolean isOlatAdmin;
	private final boolean isGuestOnly;
	private final boolean guestPostAllowed;
	private final boolean anonymousPostAllowed;
	private final boolean anonymousPostDefault;
	private final SubscriptionContext subscriptionContext;

	/**
	 * @param ne the nodeevaluation for this coursenode
	 * @param isOlatAdmin true if the user is olat-admin
	 * @param isGuestOnly true if the user is olat-guest
	 * @param subscriptionContext
	 */
	public ForumNodeForumCallback(NodeEvaluation ne, boolean isOlatAdmin, boolean isGuestOnly,
			boolean guestPostAllowed, boolean anonymousPostAllowed, boolean anonymousPostDefault,
			SubscriptionContext subscriptionContext) {
		this.ne = ne;
		this.isOlatAdmin = isOlatAdmin;
		this.isGuestOnly = isGuestOnly;
		this.guestPostAllowed = guestPostAllowed;
		this.anonymousPostAllowed = anonymousPostAllowed;
		this.anonymousPostDefault = anonymousPostDefault;
		this.subscriptionContext = subscriptionContext;
	}

	@Override
	public boolean mayUsePseudonym() {
		if (isGuestOnly) return false;
		return anonymousPostAllowed;
	}

	@Override
	public boolean pseudonymAsDefault() {
		return anonymousPostDefault;
	}

	/**
	 * @see org.olat.modules.fo.ForumCallback#mayOpenNewThread()
	 */
	@Override
	public boolean mayOpenNewThread() {
		if (isGuestOnly && !guestPostAllowed) return false;
		return ne.isCapabilityAccessible("poster") || ne.isCapabilityAccessible("moderator") || isOlatAdmin;
	}

	/**
	 * @see org.olat.modules.fo.ForumCallback#mayReplyMessage()
	 */
	@Override
	public boolean mayReplyMessage() {
		if (isGuestOnly && !guestPostAllowed) return false;
		return ne.isCapabilityAccessible("poster") || ne.isCapabilityAccessible("moderator") || isOlatAdmin;
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
		return ne.isCapabilityAccessible("moderator") || isOlatAdmin;
	}

	@Override
	public boolean mayDeleteMessageAsModerator() {
		if (isGuestOnly) return false;
		return ne.isCapabilityAccessible("moderator") || isOlatAdmin;
	}

	@Override
	public boolean mayArchiveForum() {
		return !isGuestOnly;
	}

	@Override
	public boolean mayFilterForUser() {
		if (isGuestOnly) return false;
		return ne.isCapabilityAccessible("moderator") || isOlatAdmin;
	}

	/**
	 * @see org.olat.modules.fo.ForumCallback#getSubscriptionContext()
	 */
	@Override
	public SubscriptionContext getSubscriptionContext() {
	// SubscriptionContext sc = new SubscriptionContext("coourseli", new
	// Long(123), "subident", "Einfuehrung in die Blabla", "Knoten gugus");
	// do not offer subscription to forums for guests
		return (isGuestOnly ? null : subscriptionContext);
	}
}