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
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.condition.Condition;
import org.olat.course.condition.interpreter.ConditionExpression;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.export.CourseEnvironmentMapper;
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

	public static final String TYPE = "info";
	public static final String EDIT_CONDITION_ID = "editinfos";
	public static final String ADMIN_CONDITION_ID = "admininfos";
	private Condition preConditionEdit;
	private Condition preConditionAdmin;
	
	public InfoCourseNode() {
		super(TYPE);
		updateModuleConfigDefaults(true);
	}
	
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode) {
		ModuleConfiguration config = getModuleConfiguration();
		if (isNewNode) {
			// use defaults for new course building blocks
			config.set(InfoCourseNodeConfiguration.CONFIG_AUTOSUBSCRIBE, "on");
			config.set(InfoCourseNodeConfiguration.CONFIG_DURATION, "90");
			config.set(InfoCourseNodeConfiguration.CONFIG_LENGTH, "10");
		}
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
		String translatorStr = Util.getPackageName(InfoCourseNodeEditController.class);
		List<StatusDescription> statusDescs =isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
		oneClickStatusCache = StatusDescriptionHelper.sort(statusDescs);
		return oneClickStatusCache;
	}


	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		InfoCourseNodeEditController childTabCntrllr = new InfoCourseNodeEditController(ureq, wControl, getModuleConfiguration(), this, course, euce);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		return new NodeEditController(ureq, wControl, course, chosenNode, euce, childTabCntrllr);
	}
	
	@Override
	public Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			CourseNodeSecurityCallback nodeSecCallback) {
		if (nodeSecCallback.isAccessible()) {
			InfoPeekViewController ctrl = new InfoPeekViewController(ureq, wControl, userCourseEnv, this);
			return ctrl;
		} else {
			return super.createPeekViewRunController(ureq, wControl, userCourseEnv, nodeSecCallback);
		}
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
		return conditions;
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
		//nodeEval.setVisible(true);
		super.calcAccessAndVisibility(ci, nodeEval);
		
		// evaluate the preconditions
		boolean editor = (getPreConditionEdit().getConditionExpression() == null ? true : ci.evaluateCondition(getPreConditionEdit()));
		nodeEval.putAccessStatus(EDIT_CONDITION_ID, editor);
		
		boolean admin = (getPreConditionAdmin().getConditionExpression() == null ? true : ci.evaluateCondition(getPreConditionAdmin()));
		nodeEval.putAccessStatus(ADMIN_CONDITION_ID, admin);
	}
	
		@Override
	/**
	 * is called when deleting this node, clean up info-messages and subscriptions!
	 */
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
