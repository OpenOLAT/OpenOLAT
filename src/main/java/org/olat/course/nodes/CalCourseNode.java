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
import java.util.Collections;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
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
import org.olat.course.nodes.cal.CalEditController;
import org.olat.course.nodes.cal.CalRunController;
import org.olat.course.nodes.cal.CalSecurityCallback;
import org.olat.course.nodes.cal.CalSecurityCallbackFactory;
import org.olat.course.nodes.cal.CourseCalendarPeekViewController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * <h3>Description:</h3> Course node for calendar
 * 
 * Initial Date: 4 nov. 2009 <br>
 * 
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class CalCourseNode extends AbstractAccessableCourseNode {

	private static final long serialVersionUID = -3174525063215323155L;
	
	public static final String TYPE = "cal";
	
	private static final int CURRENT_VERSION = 4;
	public static final String CONFIG_START_DATE = "startDate";
	public static final String CONFIG_AUTO_DATE = "autoDate";
	
	private static final String LEGACY_KEY_EDIT_BY_COACH = "edit.by.coach";
	private static final String LEGACY_KEY_EDIT_BY_PARTICIPANT = "edit.by.participant";
	
	public static final NodeRightType EDIT = NodeRightTypeBuilder.ofIdentifier("edit")
			.setLabel(CalEditController.class, "config.edit")
			.addRole(NodeRightRole.coach, true)
			.addRole(NodeRightRole.participant, false)
			.build();
	public static final List<NodeRightType> NODE_RIGHT_TYPES = Collections.singletonList(EDIT);
	
	public static final String EDIT_CONDITION_ID = "editarticle";
	private Condition preConditionEdit;

	public CalCourseNode() {
		this(null);
	}
	
	public CalCourseNode(INode parent) {
		super(TYPE, parent);
	}

	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent) {
		ModuleConfiguration config = getModuleConfiguration();
		if (isNewNode) {
			// use defaults for new course building blocks
			config.setBooleanEntry(NodeEditController.CONFIG_STARTPAGE, false);
			config.setConfigurationVersion(1);
		} else {
			if(config.getConfigurationVersion() < 2) {
				Condition cond = getPreConditionEdit();
				if(!cond.isExpertMode() && cond.isEasyModeCoachesAndAdmins() && cond.getConditionExpression() == null) {
					//ensure that the default config has a condition expression
					cond.setConditionExpression(cond.getConditionFromEasyModeConfiguration());
				}
			}
		}
		if (config.getConfigurationVersion() < 3) {
			removeDefaultPreconditions();
		}
		if (config.getConfigurationVersion() < 4 && config.has(LEGACY_KEY_EDIT_BY_COACH)) {
			NodeRightService nodeRightService = CoreSpringFactory.getImpl(NodeRightService.class);
			NodeRight right = nodeRightService.getRight(config, EDIT);
			Collection<NodeRightRole> roles = new ArrayList<>(2);
			if (config.getBooleanSafe(LEGACY_KEY_EDIT_BY_COACH)) {
				roles.add(NodeRightRole.coach);
			}
			if (config.getBooleanSafe(LEGACY_KEY_EDIT_BY_PARTICIPANT)) {
				roles.add(NodeRightRole.participant);
			}
			nodeRightService.setRoleGrants(right, roles);
			nodeRightService.setRight(config, right);
			// Remove legacy
			config.remove(LEGACY_KEY_EDIT_BY_COACH);
			config.remove(LEGACY_KEY_EDIT_BY_PARTICIPANT);
		}
		
		config.setConfigurationVersion(CURRENT_VERSION);
	}
	
	private void removeDefaultPreconditions() {
		if (hasCustomPreConditions()) {
			boolean defaultPreconditions =
					!preConditionEdit.isExpertMode()
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
		preConditionEdit = null;
	}
	
	@Override
	protected void postImportCopyConditions(CourseEnvironmentMapper envMapper) {
		super.postImportCopyConditions(envMapper);
		postImportCondition(preConditionEdit, envMapper);
	}
	
	@Override
	public void postCopy(CourseEnvironmentMapper envMapper, Processing processType, ICourse course, ICourse sourceCrourse) {
		super.postCopy(envMapper, processType, course, sourceCrourse);
		
	}

	@Override
	public void postExport(CourseEnvironmentMapper envMapper, boolean backwardsCompatible) {
		super.postExport(envMapper, backwardsCompatible);
		postExportCondition(preConditionEdit, envMapper, backwardsCompatible);
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		CalEditController childTabCntrllr = new CalEditController(ureq, wControl, this, course, euce);
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
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd) {
		CalSecurityCallback secCallback = CalSecurityCallbackFactory.createCourseNodeCallback(this, userCourseEnv,
				nodeSecCallback.getNodeEvaluation());
		CalRunController calCtlr = new CalRunController(wControl, ureq, this, userCourseEnv, secCallback);
		Controller wrapperCtrl = TitledWrapperHelper.getWrapper(ureq, wControl, calCtlr, userCourseEnv, this, "o_cal_icon");
		return new NodeRunConstructionResult(wrapperCtrl);
	}

	@Override
	public Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			CourseNodeSecurityCallback nodeSecCallback, boolean small) {
		CalSecurityCallback secCallback = CalSecurityCallbackFactory.createCourseNodeCallback(this, userCourseEnv,
				nodeSecCallback.getNodeEvaluation());
		CourseCalendarPeekViewController peekViewCtrl = new CourseCalendarPeekViewController(ureq, wControl,
				userCourseEnv, this, secCallback);
		return peekViewCtrl;
	}

	@Override
	public StatusDescription isConfigValid() {
		// first check the one click cache
		if (oneClickStatusCache != null) {
			return oneClickStatusCache[0];
		}
		return StatusDescription.NOERROR;
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;
		// only here we know which translator to take for translating condition
		// error messages
		String translatorStr = Util.getPackageName(CalEditController.class);
		List<StatusDescription> sds = isConfigValidWithTranslator(cev, translatorStr, getConditionExpressions());
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
	public List<ConditionExpression> getConditionExpressions() {
		if (hasCustomPreConditions()) {
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
			return conditions;
		}
		return super.getConditionExpressions();
	}
	
	public boolean hasCustomPreConditions() {
		return preConditionEdit != null;
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
	public void calcAccessAndVisibility(ConditionInterpreter ci, NodeEvaluation nodeEval) {
		super.calcAccessAndVisibility(ci, nodeEval);
		
		if (hasCustomPreConditions()) {
			boolean editor = (getPreConditionEdit().getConditionExpression() == null ? true : ci.evaluateCondition(getPreConditionEdit()));
			nodeEval.putAccessStatus(EDIT_CONDITION_ID, editor);
		}
	}
}