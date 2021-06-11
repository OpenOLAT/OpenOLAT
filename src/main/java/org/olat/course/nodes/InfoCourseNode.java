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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.commons.info.InfoMessage;
import org.olat.commons.info.InfoMessageFrontendManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
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
import org.olat.course.nodes.info.InfoConfigController;
import org.olat.course.nodes.info.InfoCourseNodeConfiguration;
import org.olat.course.nodes.info.InfoCourseNodeEditController;
import org.olat.course.nodes.info.InfoPeekViewController;
import org.olat.course.nodes.info.InfoRunController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Description:<br>
 * Course node for info messages
 * 
 * <P>
 * Initial Date:  3 aug. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class InfoCourseNode extends AbstractAccessableCourseNode {
	
	@SuppressWarnings("deprecation")
	private static final String TRANSLATOR_PACKAGE = Util.getPackageName(InfoCourseNodeEditController.class);

	public static final String TYPE = "info";
	public static final String EDIT_CONDITION_ID = "editinfos";
	public static final String ADMIN_CONDITION_ID = "admininfos";
	
	// Configs
	private static final int CURRENT_VERSION = 4;
	
	private static final String LEGACY_KEY_ADMIN_BY_COACH = "admin.by.coach";
	private static final String LEGACY_KEY_EDIT_BY_COACH = "edit.by.coach";
	private static final String LEGACY_KEY_EDIT_BY_PARTICIPANT = "edit.by.participant";
	
	public static final NodeRightType ADMIN = NodeRightTypeBuilder.ofIdentifier("admin")
			.setLabel(InfoCourseNodeEditController.class, "config.admin")
			.addRole(NodeRightRole.coach, true)
			.build();
	public static final NodeRightType EDIT = NodeRightTypeBuilder.ofIdentifier("edit")
			.setLabel(InfoCourseNodeEditController.class, "config.edit")
			.addRole(NodeRightRole.coach, true)
			.addRole(NodeRightRole.participant, false)
			.build();
	public static final List<NodeRightType> NODE_RIGHT_TYPES = List.of(ADMIN, EDIT);
	
	private Condition preConditionEdit;
	private Condition preConditionAdmin;
	
	public InfoCourseNode() {
		this(null);
	}

	public InfoCourseNode(INode parent) {
		super(TYPE, parent);
	}
	
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent) {
		ModuleConfiguration config = getModuleConfiguration();
		if (isNewNode) {
			// use defaults for new course building blocks
			config.set(InfoCourseNodeConfiguration.CONFIG_AUTOSUBSCRIBE, "on");
			config.set(InfoCourseNodeConfiguration.CONFIG_DURATION, "90");
			config.set(InfoCourseNodeConfiguration.CONFIG_LENGTH, "10");
		}
		
		int version = config.getConfigurationVersion();
		if (version < 2) {
			removeDefaultPreconditions();
		}
		if(version < 3) {
			if(!config.has(InfoCourseNodeConfiguration.CONFIG_DURATION) && config.has(InfoCourseNodeConfiguration.CONFIG_DURATION_DEPRECATED)) {
				String validDuration = getValueAllowed(config.get("duration"));
				config.set(InfoCourseNodeConfiguration.CONFIG_DURATION, validDuration);
			}
		}
		if (version < 4 && config.has(LEGACY_KEY_ADMIN_BY_COACH)) {
			NodeRightService nodeRightService = CoreSpringFactory.getImpl(NodeRightService.class);
			// Admin
			NodeRight adminRight = nodeRightService.getRight(config, ADMIN);
			Collection<NodeRightRole> moderateRoles = new ArrayList<>(1);
			if (config.getBooleanSafe(LEGACY_KEY_ADMIN_BY_COACH)) {
				moderateRoles.add(NodeRightRole.coach);
			}
			nodeRightService.setRoleGrants(adminRight, moderateRoles);
			nodeRightService.setRight(config, adminRight);
			// Edit
			NodeRight postRight = nodeRightService.getRight(config, EDIT);
			Collection<NodeRightRole> postRoles = new ArrayList<>(2);
			if (config.getBooleanSafe(LEGACY_KEY_EDIT_BY_COACH)) {
				postRoles.add(NodeRightRole.coach);
			}
			if (config.getBooleanSafe(LEGACY_KEY_EDIT_BY_PARTICIPANT)) {
				postRoles.add(NodeRightRole.participant);
			}
			nodeRightService.setRoleGrants(postRight, postRoles);
			nodeRightService.setRight(config, postRight);
			// Remove legacy
			config.remove(LEGACY_KEY_ADMIN_BY_COACH);
			config.remove(LEGACY_KEY_EDIT_BY_COACH);
			config.remove(LEGACY_KEY_EDIT_BY_PARTICIPANT);
		}
		config.setConfigurationVersion(CURRENT_VERSION);
	}
	
	private String getValueAllowed(Object value) {
		if(value instanceof Number) {
			value = value.toString();
		}
		for(String allowedValue:InfoConfigController.getAllowedValues()) {
			if(allowedValue.equals(value)) {
				return allowedValue;
			}
		}
		return "90";
	}
	
	private void removeDefaultPreconditions() {
		if (hasCustomPreConditions()) {
			boolean defaultPreconditions =
					!preConditionAdmin.isExpertMode()
				&& preConditionAdmin.isEasyModeCoachesAndAdmins()
				&& !preConditionAdmin.isEasyModeAlwaysAllowCoachesAndAdmins()
				&& !preConditionAdmin.isAssessmentMode()
				&& !preConditionAdmin.isAssessmentModeViewResults()
				&& !preConditionEdit.isExpertMode()
				&& preConditionEdit.isEasyModeCoachesAndAdmins()
				&& !preConditionEdit.isEasyModeAlwaysAllowCoachesAndAdmins()
				&& !preConditionEdit.isAssessmentMode()
				&& !preConditionEdit.isAssessmentModeViewResults();
			if (defaultPreconditions) {
				removeCustomPreconditions();
			}
		}
	}
	
	public void removeCustomPreconditions() {
		preConditionAdmin = null;
		preConditionEdit = null;
		setPreConditionAccess(null);
	}
	
	@Override
	protected void postImportCopyConditions(CourseEnvironmentMapper envMapper) {
		super.postImportCopyConditions(envMapper);
		postImportCondition(preConditionEdit, envMapper);
		postImportCondition(preConditionAdmin, envMapper);
	}

	@Override
	public void postExport(CourseEnvironmentMapper envMapper, boolean backwardsCompatible) {
		super.postExport(envMapper, backwardsCompatible);
		postExportCondition(preConditionEdit, envMapper, backwardsCompatible);
		postExportCondition(preConditionAdmin, envMapper, backwardsCompatible);
	}
	
	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}
	
	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}

	@Override
	public StatusDescription isConfigValid() {
		return StatusDescription.NOERROR;
	}
	
	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;
		List<StatusDescription> statusDescs = isConfigValidWithTranslator(cev, TRANSLATOR_PACKAGE, getConditionExpressions());
		oneClickStatusCache = StatusDescriptionHelper.sort(statusDescs);
		return oneClickStatusCache;
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		InfoCourseNodeEditController childTabCntrllr = new InfoCourseNodeEditController(ureq, wControl, this, course, euce);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, euce, childTabCntrllr);
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return hasCustomPreConditions()
				? ConditionAccessEditConfig.custom()
				: ConditionAccessEditConfig.regular(false);
	}
	
	@Override
	public Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			CourseNodeSecurityCallback nodeSecCallback) {
		if (nodeSecCallback.isAccessible()) {
			InfoPeekViewController ctrl = new InfoPeekViewController(ureq, wControl, userCourseEnv, this);
			return ctrl;
		}
		return super.createPeekViewRunController(ureq, wControl, userCourseEnv, nodeSecCallback);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd) {
		InfoRunController infoCtrl = new InfoRunController(ureq, wControl, userCourseEnv, nodeSecCallback.getNodeEvaluation(), this);
		Controller titledCtrl = TitledWrapperHelper.getWrapper(ureq, wControl, infoCtrl, this, "o_infomsg_icon");
		return new NodeRunConstructionResult(titledCtrl);
	}
	
	@Override
	public List<ConditionExpression> getConditionExpressions() {
		List<ConditionExpression> parentConditions = super.getConditionExpressions();
		List<ConditionExpression> conditions = new ArrayList<>();
		if(parentConditions != null && parentConditions.size() > 0) {
			conditions.addAll(parentConditions);
		}

		if (hasCustomPreConditions()) {
			Condition editCondition = getPreConditionEdit();
			if(editCondition != null && StringHelper.containsNonWhitespace(editCondition.getConditionExpression())) {
				ConditionExpression ce = new ConditionExpression(editCondition.getConditionId());
				ce.setExpressionString(editCondition.getConditionExpression());
				conditions.add(ce);
			}
			Condition adminCondition = getPreConditionAdmin();
			if(adminCondition != null && StringHelper.containsNonWhitespace(adminCondition.getConditionExpression())) {
				ConditionExpression ce = new ConditionExpression(adminCondition.getConditionId());
				ce.setExpressionString(adminCondition.getConditionExpression());
				conditions.add(ce);
			}
		}
		return conditions;
	}
	
	public boolean hasCustomPreConditions() {
		return preConditionAdmin != null || preConditionEdit != null;
	}
	
	/**
	 * Default set the write privileges to coaches and admin only
	 * @return
	 */
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

	/**
	 * 
	 * @param preConditionEdit
	 */
	public void setPreConditionEdit(Condition preConditionEdit) {
		if (preConditionEdit == null) {
			preConditionEdit = getPreConditionEdit();
		}
		preConditionEdit.setConditionId(EDIT_CONDITION_ID);
		this.preConditionEdit = preConditionEdit;
	}
	
	/**
	 * Default set the write privileges to coaches and admin only
	 * @return
	 */
	public Condition getPreConditionAdmin() {
		if (preConditionAdmin == null) {
			preConditionAdmin = new Condition();
			preConditionAdmin.setEasyModeCoachesAndAdmins(true);
			preConditionAdmin.setConditionExpression(preConditionAdmin.getConditionFromEasyModeConfiguration());
			preConditionAdmin.setExpertMode(false);
		}
		preConditionAdmin.setConditionId(ADMIN_CONDITION_ID);
		return preConditionAdmin;
	}

	/**
	 * 
	 * @param preConditionEdit
	 */
	public void setPreConditionAdmin(Condition preConditionAdmin) {
		if (preConditionAdmin == null) {
			preConditionAdmin = getPreConditionAdmin();
		}
		preConditionAdmin.setConditionId(ADMIN_CONDITION_ID);
		this.preConditionAdmin = preConditionAdmin;
	}

	@Override
	public void calcAccessAndVisibility(ConditionInterpreter ci, NodeEvaluation nodeEval) {
		super.calcAccessAndVisibility(ci, nodeEval);
		
		if (hasCustomPreConditions()) {
			boolean editor = (getPreConditionEdit().getConditionExpression() == null ? true : ci.evaluateCondition(getPreConditionEdit()));
			nodeEval.putAccessStatus(EDIT_CONDITION_ID, editor);
			
			boolean admin = (getPreConditionAdmin().getConditionExpression() == null ? true : ci.evaluateCondition(getPreConditionAdmin()));
			nodeEval.putAccessStatus(ADMIN_CONDITION_ID, admin);
		}
	}
	
	/**
	 * is called when deleting this node, clean up info-messages and subscriptions!
	 */
	@Override
	public void cleanupOnDelete(ICourse course) {
		super.cleanupOnDelete(course);
		// delete infoMessages and subscriptions (OLAT-6171)
		String resSubpath = getIdent();
		InfoMessageFrontendManager infoService = CoreSpringFactory.getImpl(InfoMessageFrontendManager.class);
		List<InfoMessage>  messages = infoService.loadInfoMessageByResource(course, resSubpath, null, null, null, 0, 0);
		for (InfoMessage im : messages) {
			infoService.deleteInfoMessage(im);
		}
		
		final SubscriptionContext subscriptionContext = CourseModule.createTechnicalSubscriptionContext(course.getCourseEnvironment(), this);
		NotificationsManager notifManagar =  CoreSpringFactory.getImpl(NotificationsManager.class);
		notifManagar.delete(subscriptionContext);
		super.cleanupOnDelete(course);
	}
}
