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

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipOutputStream;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.messages.MessageUIFactory;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.GenericNode;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.ICourse;
import org.olat.course.condition.Condition;
import org.olat.course.condition.KeyAndNameConverter;
import org.olat.course.condition.additionalconditions.AdditionalCondition;
import org.olat.course.condition.interpreter.ConditionErrorMessage;
import org.olat.course.condition.interpreter.ConditionExpression;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeConfigFormController;
import org.olat.course.editor.PublishEvents;
import org.olat.course.editor.StatusDescription;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.TreeEvaluation;
import org.olat.course.run.userview.TreeFilter;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.statistic.StatisticResourceOption;
import org.olat.course.statistic.StatisticResourceResult;
import org.olat.course.statistic.StatisticType;
import org.olat.modules.ModuleConfiguration;

/**
 * Description:<br>
 * @author Felix Jost
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public abstract class GenericCourseNode extends GenericNode implements CourseNode {
	
	private static final long serialVersionUID = -1093400247219150363L;
	private String type, shortTitle, longTitle, learningObjectives, displayOption;
	private ModuleConfiguration moduleConfiguration;
	private String noAccessExplanation;
	private Condition preConditionVisibility;
	private Condition preConditionAccess;
	protected transient StatusDescription[] oneClickStatusCache = null;
	protected List<AdditionalCondition> additionalConditions = new ArrayList<>();

	/**
	 * Generic course node constructor
	 * 
	 * @param type The course node type
	 *      
	 *      ATTENTION:
	 *      all course nodes must call updateModuleConfigDefaults(true) here
	 */
	public GenericCourseNode(String type) {
		super();
		this.type = type;
		moduleConfiguration = new ModuleConfiguration();
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#createEditController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl, org.olat.course.ICourse)
	 *      
	 *      ATTENTION:
	 *      all course nodes must call updateModuleConfigDefaults(false) here
	 */
	@Override
	public abstract TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course,
			UserCourseEnvironment euce);

	/**
	 * @see org.olat.course.nodes.CourseNode#createNodeRunConstructionResult(UserRequest,
	 *      WindowControl, UserCourseEnvironment, NodeEvaluation, String)
	 * 
	 *      ATTENTION: all course nodes must call
	 *      updateModuleConfigDefaults(false) here
	 */
	@Override
	public abstract NodeRunConstructionResult createNodeRunConstructionResult(
			UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, NodeEvaluation ne,
			String nodecmd);

	protected String getDefaultTitleOption() {
		return CourseNode.DISPLAY_OPTS_TITLE_DESCRIPTION_CONTENT;
	}

	/**
	 * Default implementation of the peekview controller that returns NULL: no
	 * node specific peekview information should be shown<br>
	 * Override this method with a specific implementation if you have
	 * something interesting to show in the peekview
	 * 
	 * @see org.olat.course.nodes.CourseNode#createPeekViewRunController(UserRequest, WindowControl, UserCourseEnvironment, NodeEvaluation)
	 */
	@Override
	public Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			NodeEvaluation ne) {
		return null;
	}
	
	/**
	 * default implementation of the previewController
	 * 
	 * @see org.olat.course.nodes.CourseNode#createPreviewController(org.olat.core.gui.UserRequest,
	 * @see org.olat.core.gui.control.WindowControl,
	 * @see org.olat.course.run.userview.UserCourseEnvironment,
	 * @see org.olat.course.run.userview.NodeEvaluation)
	 */
	//no userCourseEnv or NodeEvaluation needed here
	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
		Translator translator = Util.createPackageTranslator(GenericCourseNode.class, ureq.getLocale());
		String text = translator.translate("preview.notavailable");
		return MessageUIFactory.createInfoMessage(ureq, wControl, null, text);
	}
	
	@Override
	public StatisticResourceResult createStatisticNodeResult(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, StatisticResourceOption options, StatisticType type) {
		return null;
	}

	@Override
	public boolean isStatisticNodeResultAvailable(UserCourseEnvironment userCourseEnv, StatisticType type) {
		return false;
	}

	/**
	 * @return String
	 */
	@Override
	public String getLearningObjectives() {
		return learningObjectives;
	}

	/**
	 * @return String
	 */
	@Override
	public String getLongTitle() {
		return longTitle;
	}

	/**
	 * @return String
	 */
	@Override
	public String getShortTitle() {
		return shortTitle;
	}
	
	/**
	 * allows to specify if default value should be returned in case where there is no value.
	 * @param returnDefault if false: null may be returned if no value found!
	 * @return String 
	 */
	public String getDisplayOption(boolean returnDefault) {
		if(!StringHelper.containsNonWhitespace(displayOption) && returnDefault) {
			return getDefaultTitleOption();
		}
		return displayOption;
	}
	
	/**
	 * @return String with the old behavior (default value if none existing)
	 */
	@Override
	public String getDisplayOption() {
		return getDisplayOption(true);		
	}

	/**
	 * @return String
	 */
	@Override
	public String getType() {
		return type;
	}

	/**
	 * Sets the learningObjectives.
	 * 
	 * @param learningObjectives The learningObjectives to set
	 */
	@Override
	public void setLearningObjectives(String learningObjectives) {
		this.learningObjectives = learningObjectives;
	}

	/**
	 * Sets the longTitle.
	 * 
	 * @param longTitle The longTitle to set
	 */
	@Override
	public void setLongTitle(String longTitle) {
		this.longTitle = longTitle;
	}

	/**
	 * Sets the shortTitle.
	 * 
	 * @param shortTitle The shortTitle to set
	 */
	@Override
	public void setShortTitle(String shortTitle) {
		this.shortTitle = shortTitle;
	}
	
	/**
	 * Sets the display option
	 * @param displayOption
	 */
	@Override
	public void setDisplayOption(String displayOption) {
		this.displayOption = displayOption;
	}

	/**
	 * Sets the type.
	 * 
	 * @param type The type to set
	 */
	public void setType(String type) {
		this.type = type;
	}

	/**
	 * @return ModuleConfiguration
	 */
	@Override
	public ModuleConfiguration getModuleConfiguration() {
		return moduleConfiguration;
	}

	/**
	 * Sets the moduleConfiguration.
	 * 
	 * @param moduleConfiguration The moduleConfiguration to set
	 */
	public void setModuleConfiguration(ModuleConfiguration moduleConfiguration) {
		this.moduleConfiguration = moduleConfiguration;
	}

	@Override
	public NodeEvaluation eval(ConditionInterpreter ci, TreeEvaluation treeEval, TreeFilter filter) {
		// each CourseNodeImplementation has the full control over all children eval.
		// default behaviour is to eval all visible children
		NodeEvaluation nodeEval = new NodeEvaluation(this);
		calcAccessAndVisibility(ci, nodeEval);
		if(filter != null && !filter.isVisible(this)) {
			nodeEval.setVisible(false);
		}
		
		nodeEval.build();
		treeEval.cacheCourseToTreeNode(this, nodeEval.getTreeNode());
		// only add children (coursenodes/nodeeval) when I am visible and
		// atleastOneAccessible myself
		if (nodeEval.isVisible() && nodeEval.isAtLeastOneAccessible()) {
			int childcnt = getChildCount();
			for (int i = 0; i < childcnt; i++) {
				CourseNode cn = (CourseNode)getChildAt(i);
				NodeEvaluation chdEval = cn.eval(ci, treeEval, filter);
				if (chdEval.isVisible()) { // child is visible
					nodeEval.addNodeEvaluationChild(chdEval);
				}
			}
		}
		return nodeEval;
	}

	/**
	 * @param ci the ConditionInterpreter as the calculating machine
	 * @param nodeEval the object to write the results into
	 */
	protected abstract void calcAccessAndVisibility(ConditionInterpreter ci, NodeEvaluation nodeEval);

	/**
	 * @return String
	 */
	@Override
	public String getNoAccessExplanation() {
		return noAccessExplanation;
	}

	/**
	 * Sets the noAccessExplanation.
	 * 
	 * @param noAccessExplanation The noAccessExplanation to set
	 */
	@Override
	public void setNoAccessExplanation(String noAccessExplanation) {
		this.noAccessExplanation = noAccessExplanation;
	}

	/**
	 * @return Condition
	 */
	@Override
	public Condition getPreConditionVisibility() {
		if (preConditionVisibility == null) {
			preConditionVisibility = new Condition();
		}
		preConditionVisibility.setConditionId("visibility");
		return preConditionVisibility;
	}

	/**
	 * Sets the preConditionVisibility.
	 * 
	 * @param preConditionVisibility The preConditionVisibility to set
	 */
	@Override
	public void setPreConditionVisibility(Condition preConditionVisibility) {
		if (preConditionVisibility == null) {
			preConditionVisibility = getPreConditionVisibility();
		}
		this.preConditionVisibility = preConditionVisibility;
		this.preConditionVisibility.setConditionId("visibility");
	}

	/**
	 * @return Condition
	 */
	@Override
	public Condition getPreConditionAccess() {
		if (preConditionAccess == null) {
			preConditionAccess = new Condition();
		}
		preConditionAccess.setConditionId("accessability");
		return preConditionAccess;
	}
	
	@Override
	public void updateOnPublish(Locale locale, ICourse course, Identity publisher, PublishEvents publishEvents) {
		//default do nothing
	}

	/**
	 * Generic interface implementation. May be overriden by specific node's
	 * implementation.
	 * 
	 * @see org.olat.course.nodes.CourseNode#informOnDelete(org.olat.core.gui.UserRequest,
	 *      org.olat.course.ICourse)
	 */
	@Override
	public String informOnDelete(Locale locale, ICourse course) {
		return null;
	}

	/**
	 * Generic interface implementation. May be overriden by specific node's
	 * implementation.
	 * 
	 * @see org.olat.course.nodes.CourseNode#cleanupOnDelete(org.olat.course.ICourse)
	 */
	@Override
	public void cleanupOnDelete(ICourse course) {
		// do nothing in default implementation
	}

	/**
	 * Generic interface implementation. May be overriden by specific node's
	 * implementation.
	 * 
	 * @see org.olat.course.nodes.CourseNode#archiveNodeData(java.util.Locale,
	 *      org.olat.course.ICourse, java.util.zip.ZipOutputStream, String charset)
	 */
	@Override
	public boolean archiveNodeData(Locale locale, ICourse course, ArchiveOptions options,
			ZipOutputStream exportStream, String path, String charset) {
		// nothing to do in default implementation
		return true;
	}

	@Override
	public void exportNode(File exportDirectory, ICourse course) {
	// nothing to do in default implementation
	}

	/**
	 * Implemented by specialized node
	 */
	@Override
	public void importNode(File importDirectory, ICourse course, Identity owner, Organisation organisation, Locale locale, boolean withReferences) {
		// nothing to do in default implementation
	}

	@Override
	public void postCopy(CourseEnvironmentMapper envMapper, Processing processType, ICourse course, ICourse sourceCrourse) {
		postImportCopyConditions(envMapper);
	}

	@Override
	public void postImport(File importDirectory, ICourse course, CourseEnvironmentMapper envMapper, Processing processType) {
		postImportCopyConditions(envMapper);
	}
	
	/**
	 * Post process the conditions
	 * @param envMapper
	 */
	protected void postImportCopyConditions(CourseEnvironmentMapper envMapper) {
		postImportCondition(preConditionAccess, envMapper);
		postImportCondition(preConditionVisibility, envMapper);
	}
	
	protected void postImportCondition(Condition condition, CourseEnvironmentMapper envMapper) {
		if(condition == null) return;
		
		if(condition.isExpertMode()) {
			String expression = condition.getConditionExpression();
			if(StringHelper.containsNonWhitespace(expression)) {
				String processExpression = KeyAndNameConverter.convertExpressionNameToKey(expression, envMapper);
				processExpression = KeyAndNameConverter.convertExpressionKeyToKey(processExpression, envMapper);
				if(!expression.equals(processExpression)) {
					condition.setConditionExpression(processExpression);
				}
			}
		} else if(StringHelper.containsNonWhitespace(condition.getConditionFromEasyModeConfiguration())) {
			List<Long> groupKeys = condition.getEasyModeGroupAccessIdList();
			if(groupKeys == null || groupKeys.isEmpty()) {
				//this is an old course -> get group keys from original names
				groupKeys = envMapper.toGroupKeyFromOriginalNames(condition.getEasyModeGroupAccess());
			} else {
				//map the original exported group key to the newly created one
				groupKeys = envMapper.toGroupKeyFromOriginalKeys(groupKeys);
			}
			condition.setEasyModeGroupAccessIdList(groupKeys);//update keys
			condition.setEasyModeGroupAccess(envMapper.toGroupNames(groupKeys));//update names with the current values
			
			List<Long> areaKeys = condition.getEasyModeGroupAreaAccessIdList();
			if(areaKeys == null || areaKeys.isEmpty()) {
				areaKeys = envMapper.toAreaKeyFromOriginalNames(condition.getEasyModeGroupAreaAccess());
			} else {
				areaKeys = envMapper.toAreaKeyFromOriginalKeys(areaKeys);
			}
			condition.setEasyModeGroupAreaAccessIdList(areaKeys);
			condition.setEasyModeGroupAreaAccess(envMapper.toAreaNames(areaKeys));
			
			String condString = condition.getConditionFromEasyModeConfiguration();
			condition.setConditionExpression(condString);
		}
	}

	@Override
	public void postExport(CourseEnvironmentMapper envMapper, boolean backwardsCompatible) {
		postExportCondition(preConditionAccess, envMapper, backwardsCompatible);
		postExportCondition(preConditionVisibility, envMapper, backwardsCompatible);
	}
	
	protected void postExportCondition(Condition condition, CourseEnvironmentMapper envMapper, boolean backwardsCompatible) {
		if(condition == null) return;
		
		boolean easy = StringHelper.containsNonWhitespace(condition.getConditionFromEasyModeConfiguration());
		if(easy) {
			//already processed?
			if(condition.getEasyModeGroupAccessIdList() != null 
					|| condition.getEasyModeGroupAreaAccessIdList() != null) {
			
				String groupNames = envMapper.toGroupNames(condition.getEasyModeGroupAccessIdList());
				condition.setEasyModeGroupAccess(groupNames);
				String areaNames = envMapper.toAreaNames(condition.getEasyModeGroupAreaAccessIdList());
				condition.setEasyModeGroupAreaAccess(areaNames);
				String condString = condition.getConditionFromEasyModeConfiguration();
				if(backwardsCompatible) {
					condString = KeyAndNameConverter.convertExpressionKeyToName(condString, envMapper);
				}
				condition.setConditionExpression(condString);
			}
		} else if(condition.isExpertMode() && backwardsCompatible) {
			String expression = condition.getConditionExpression();
			if(StringHelper.containsNonWhitespace(expression)) {
				String processExpression = KeyAndNameConverter.convertExpressionKeyToName(expression, envMapper);
				if(!expression.equals(processExpression)) {
					condition.setConditionExpression(processExpression);
				}
			}
		}
		
		if(backwardsCompatible) {
			condition.setEasyModeGroupAreaAccessIds(null);
			condition.setEasyModeGroupAccessIds(null);
			//condition.setConditionUpgraded(null);
		}
	}
	


	/**
	 * @see org.olat.core.gui.ShortName#getShortName()
	 */
	@Override
	public String getShortName() {
		return getShortTitle();
	}
	
	@Override
	public CourseNode createInstanceForCopy(boolean isNewTitle, ICourse course, Identity author) {
		CourseNode copyInstance = (CourseNode) XStreamHelper.xstreamClone(this);
		copyInstance.setIdent(String.valueOf(CodeHelper.getForeverUniqueID()));
		copyInstance.setPreConditionVisibility(null);
		if (isNewTitle) {
			String newTitle = "Copy of " + getShortTitle();
			if (newTitle.length() > NodeConfigFormController.SHORT_TITLE_MAX_LENGTH) {
				newTitle = newTitle.substring(0, NodeConfigFormController.SHORT_TITLE_MAX_LENGTH - 1);
			}
			copyInstance.setShortTitle(newTitle);
		}
		return copyInstance;
	}

	@Override
	public void copyConfigurationTo(CourseNode courseNode, ICourse course) {
		if(courseNode instanceof GenericCourseNode) {
			GenericCourseNode newNode = (GenericCourseNode)courseNode;
			newNode.setDisplayOption(getDisplayOption());
			newNode.setLearningObjectives(getLearningObjectives());
			newNode.setLongTitle(getLongTitle());
			newNode.setNoAccessExplanation(getNoAccessExplanation());
			newNode.setShortTitle(getShortTitle());
			
			if(preConditionVisibility != null) {
				newNode.setPreConditionVisibility(preConditionVisibility.clone());
			}
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);
		sb.append("courseNode[id=").append(getIdent()).append(":title=").append(getShortTitle()).append("]");
		return sb.toString();
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#getConditionExpressions()
	 */
	public List<ConditionExpression> getConditionExpressions() {
		ArrayList<ConditionExpression> retVal = new ArrayList<>();
		String coS = getPreConditionVisibility().getConditionExpression();
		if (coS != null && !coS.equals("")) {
			// an active condition is defined
			ConditionExpression ce = new ConditionExpression(getPreConditionVisibility().getConditionId());
			ce.setExpressionString(getPreConditionVisibility().getConditionExpression());
			retVal.add(ce);
		}
		//
		return retVal;
	}

	/**
	 * must be implemented in the concrete subclasses as a translator is needed
	 * for the errormessages which comes with evaluating condition expressions
	 * 
	 * @see org.olat.course.nodes.CourseNode#isConfigValid(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	public abstract StatusDescription[] isConfigValid(CourseEditorEnv cev);

	/**
	 * @param userCourseEnv
	 * @param translatorStr
	 * @return
	 */
	//for StatusDescription.WARNING
	protected List<StatusDescription> isConfigValidWithTranslator(CourseEditorEnv cev, String translatorStr, List<ConditionExpression> condExprs) {
		List<StatusDescription> condExprsStatusDescs = new ArrayList<>();
		// check valid configuration without course environment
		StatusDescription first = isConfigValid();
		// check valid configuration within the course environment
		if (cev == null) {
			// course environment not configured!??
			condExprsStatusDescs.add(first);
			return condExprsStatusDescs;
		}
		/*
		 * there is course editor environment, we can check further. Iterate over
		 * all conditions of this course node, validate the condition expression and
		 * transform the condition error message into a status description
		 */
		for (int i = 0; i < condExprs.size(); i++) {
			ConditionExpression ce = condExprs.get(i);
			ConditionErrorMessage[] cems = cev.validateConditionExpression(ce);
			if (cems != null && cems.length > 0) {
				for (int j = 0; j < cems.length; j++) {
					StatusDescription sd = new StatusDescription(StatusDescription.WARNING, cems[j].getErrorKey(), cems[j].getSolutionMsgKey(),
							cems[j].getErrorKeyParams(), translatorStr);
					sd.setDescriptionForUnit(getIdent());
					condExprsStatusDescs.add(sd);
				}
			}
		}
		condExprsStatusDescs.add(first);
		return condExprsStatusDescs;
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#explainThisDuringPublish(org.olat.core.gui.control.StatusDescription)
	 */
	public StatusDescription explainThisDuringPublish(StatusDescription description) {
		if (description == null) return null;
		StatusDescription retVal = null;
		if (description.getShortDescriptionKey().equals("error.notfound.coursenodeid")) {
			retVal = description.transformTo("error.notfound.coursenodeid.publish", "error.notfound.coursenodeid.publish", null);
		} else if (description.getShortDescriptionKey().equals("error.notfound.name")) {
			retVal = description.transformTo("error.notfound.name.publish", "error.notfound.name.publish", null);
		} else if (description.getShortDescriptionKey().equals("error.notassessable.coursenodid")) {
			retVal = description.transformTo("error.notassessable.coursenodid.publish", "error.notassessable.coursenodid.publish", null);
		} else {
			// throw new OLATRuntimeException("node does not know how to translate <b
			// style='color:red'>" + description.getShortDescriptionKey()
			// + "</b> in publish env", new IllegalArgumentException());
			return description;
		}
		return retVal;
	}
	
	@Override
	public List<StatusDescription> publishUpdatesExplanations(CourseEditorEnv cev) {
		return Collections.<StatusDescription>emptyList();
	}

	/**
	 * Update the module configuration to have all mandatory configuration flags
	 * set to usefull default values
	 * 
	 * @param isNewNode true: an initial configuration is set; false: upgrading
	 *          from previous node configuration version, set default to maintain
	 *          previous behaviour
	 *          
	 * This is the workflow:
	 * On every click on a entry of the navigation tree, this method will be called
	 * to ensure a valid configration of the depending module. This is only done in
	 * RAM. If the user clicks on that node in course editor and publishes the course
	 * after that, then the updated config will be persisted to disk. Otherwise
	 * everything what is done here has to be done once at every course start.
	 */
	//implemented by specialized node
	public void updateModuleConfigDefaults(boolean isNewNode) {
		/**
		 *  Do NO updating here, since this method can be overwritten by all classes
		 *  implementing this. This is only implemented here to avoid changing all
		 *  couseNode classes which do not implement this method.
		 */
	}

}
