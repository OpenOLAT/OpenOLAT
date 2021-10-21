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
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.zip.ZipOutputStream;

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
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.GenericNode;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.ICourse;
import org.olat.course.condition.Condition;
import org.olat.course.condition.KeyAndNameConverter;
import org.olat.course.condition.additionalconditions.AdditionalCondition;
import org.olat.course.condition.interpreter.ConditionErrorMessage;
import org.olat.course.condition.interpreter.ConditionExpression;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeConfigController;
import org.olat.course.editor.PublishEvents;
import org.olat.course.editor.StatusDescription;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.highscore.ui.HighScoreEditController;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.noderight.NodeRight;
import org.olat.course.noderight.NodeRightGrant;
import org.olat.course.noderight.NodeRightService;
import org.olat.course.noderight.NodeRightType;
import org.olat.course.noderight.manager.NodeRightServiceImpl;
import org.olat.course.noderight.model.NodeRightImpl;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.statistic.StatisticResourceOption;
import org.olat.course.statistic.StatisticResourceResult;
import org.olat.course.statistic.StatisticType;
import org.olat.course.style.ColorCategory;
import org.olat.course.style.CourseStyleService;
import org.olat.course.style.ImageSource;
import org.olat.course.style.ImageSourceType;
import org.olat.course.style.TeaserImageStyle;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;

/**
 * Description:<br>
 * @author Felix Jost
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public abstract class GenericCourseNode extends GenericNode implements CourseNode {
	
	private static final long serialVersionUID = -1093400247219150363L;
	
	private static final transient String DISPLAY_OPTS_SHORT_TITLE_DESCRIPTION_CONTENT = "shorttitle+desc+content"; // legacy
	private static final transient String DISPLAY_OPTS_SHORT_TITLE_CONTENT = "shorttitle+content"; // legacy
	
	private String type;
	private String shortTitle;
	private String longTitle;
	private String learningObjectives; // legacy, replaced by description
	private String description;
	private String objectives;
	private String instruction;
	private String instructionalDesign;
	private String displayOption;
	private ImageSource teaserImageSource;
	private TeaserImageStyle teaserImageStyle;
	private String colorCategoryIdentifier;
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
	 */
	public GenericCourseNode(String type) {
		super();
		this.type = type;
		this.moduleConfiguration = new ModuleConfiguration();
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
	 *      WindowControl, UserCourseEnvironment, CourseNodeSecurityCallback, String)
	 * 
	 *      ATTENTION: all course nodes must call
	 *      updateModuleConfigDefaults(false) here
	 */
	@Override
	public abstract NodeRunConstructionResult createNodeRunConstructionResult(
			UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback,
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
	 * @see org.olat.course.nodes.CourseNode#createPeekViewRunController(UserRequest, WindowControl, UserCourseEnvironment, CourseNodeSecurityCallback, boolean)
	 */
	@Override
	public Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			CourseNodeSecurityCallback nodeSecCallback, boolean small) {
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
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback) {
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

	public String getLearningObjectives() {
		return learningObjectives;
	}

	public void setLearningObjectives(String learningObjectives) {
		this.learningObjectives = learningObjectives;
	}

	@Override
	public String getLongTitle() {
		if (!StringHelper.containsNonWhitespace(longTitle)) {
			return shortTitle;
		}
		return longTitle;
	}

	@Override
	public String getShortTitle() {
		if (!StringHelper.containsNonWhitespace(shortTitle)) {
			return Formatter.truncateOnly(longTitle, NodeConfigController.SHORT_TITLE_MAX_LENGTH);
		}
		return shortTitle;
	}
	
	public String getRawShortTitle() {
		return shortTitle;
	}
	
	/**
	 * allows to specify if default value should be returned in case where there is no value.
	 * @param returnDefault if false: null may be returned if no value found!
	 * @return String 
	 */
	public String getDisplayOption(boolean returnDefault) {
		// Fallback for old CourseNodes. They may have no displayOption, so we
		// return the old default value.
		// In new CourseNodes the displayOption is initialized during the creation of
		// the CourseNode.
		if(!StringHelper.containsNonWhitespace(displayOption) && returnDefault) {
			return getDefaultTitleOption();
		}
		// Map the legacy values to still valid values.
		if (DISPLAY_OPTS_SHORT_TITLE_DESCRIPTION_CONTENT.equals(displayOption)) {
			return DISPLAY_OPTS_TITLE_DESCRIPTION_CONTENT;
		}
		if (DISPLAY_OPTS_SHORT_TITLE_CONTENT.equals(displayOption)) {
			return DISPLAY_OPTS_TITLE_CONTENT;
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

	@Override
	public String getType() {
		return type;
	}

	@Override
	public void setLongTitle(String longTitle) {
		this.longTitle = longTitle;
	}

	@Override
	public void setShortTitle(String shortTitle) {
		this.shortTitle = shortTitle;
	}
	
	@Override
	public void setDisplayOption(String displayOption) {
		this.displayOption = displayOption;
	}
	
	@Override
	public String getDescription() {
		if (StringHelper.containsNonWhitespace(learningObjectives)) {
			return learningObjectives; // legacy fallback
		}
		return description;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getObjectives() {
		return objectives;
	}

	@Override
	public void setObjectives(String objectives) {
		this.objectives = objectives;
	}

	@Override
	public String getInstruction() {
		return instruction;
	}

	@Override
	public void setInstruction(String instruction) {
		this.instruction = instruction;
	}

	@Override
	public String getInstructionalDesign() {
		return instructionalDesign;
	}

	@Override
	public void setInstructionalDesign(String instructionalDesign) {
		this.instructionalDesign = instructionalDesign;
	}

	@Override
	public ImageSource getTeaserImageSource() {
		return teaserImageSource;
	}

	@Override
	public void setTeaserImageSource(ImageSource teaserImageSource) {
		this.teaserImageSource = teaserImageSource;
	}

	@Override
	public TeaserImageStyle getTeaserImageStyle() {
		return teaserImageStyle != null
				? teaserImageStyle
				: TeaserImageStyle.DEFAULT_COURSE_NODE;
	}

	@Override
	public void setTeaserImageStyle(TeaserImageStyle teaserImageStyle) {
		this.teaserImageStyle = teaserImageStyle;
	}

	@Override
	public String getColorCategoryIdentifier() {
		return colorCategoryIdentifier != null
				? colorCategoryIdentifier
				: ColorCategory.IDENTIFIER_DEFAULT_COURSE_NODE;
	}

	@Override
	public void setColorCategoryIdentifier(String colorCategoryIdentifier) {
		this.colorCategoryIdentifier = colorCategoryIdentifier;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public ModuleConfiguration getModuleConfiguration() {
		return moduleConfiguration;
	}

	public void setModuleConfiguration(ModuleConfiguration moduleConfiguration) {
		this.moduleConfiguration = moduleConfiguration;
	}

	@Override
	public String getNoAccessExplanation() {
		return noAccessExplanation;
	}

	@Override
	public void setNoAccessExplanation(String noAccessExplanation) {
		this.noAccessExplanation = noAccessExplanation;
	}

	@Override
	public Condition getPreConditionVisibility() {
		if (preConditionVisibility == null) {
			preConditionVisibility = new Condition();
		}
		preConditionVisibility.setConditionId("visibility");
		return preConditionVisibility;
	}

	@Override
	public void setPreConditionVisibility(Condition preConditionVisibility) {
		if (preConditionVisibility == null) {
			preConditionVisibility = getPreConditionVisibility();
		}
		this.preConditionVisibility = preConditionVisibility;
		this.preConditionVisibility.setConditionId("visibility");
	}

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
	public void postCopy(CourseEnvironmentMapper envMapper, Processing processType, ICourse course, ICourse sourceCrourse, CopyCourseContext context) {
		postImportCopyConditions(envMapper);
		
		if (context != null) {
			// Load config
			ModuleConfiguration config = getModuleConfiguration();
			
			// Move potential high score dates
			Date highScorePublicationDate = config.getDateValue(HighScoreEditController.CONFIG_KEY_DATESTART);
			
			if (highScorePublicationDate != null) {
				highScorePublicationDate.setTime(highScorePublicationDate.getTime() + context.getDateDifference(getIdent()));
				config.setDateValue(HighScoreEditController.CONFIG_KEY_DATESTART, highScorePublicationDate);
			}
			
			// Move potential user right dates
			Map<String, Object> potentialNodeRights = config.getConfigEntries(NodeRightServiceImpl.KEY_PREFIX);
			
			if (!potentialNodeRights.isEmpty()) {
				
				NodeRightService nodeRightService = CoreSpringFactory.getImpl(NodeRightService.class);
				
				for (Map.Entry<String, Object> entry : potentialNodeRights.entrySet()) {
					if (!(entry.getValue() instanceof NodeRight)) {
						continue;
					}
					
					NodeRightImpl nodeRight = (NodeRightImpl) entry.getValue();
					List<NodeRightGrant> nodeRightGrants = new ArrayList<>();
					
					if (nodeRight.getGrants() != null) {
						for (NodeRightGrant grant : nodeRight.getGrants()) {
							// Remove any rights associated with an identity or group
							if (grant.getBusinessGroupRef() != null || grant.getIdentityRef() != null) {
								continue;
							}
							
							// Move potential dates
							if (grant.getStart() != null) {
								grant.setStart(new Date(grant.getStart().getTime() + context.getDateDifference(getIdent())));
							}
							
							if (grant.getEnd() != null) {
								grant.setEnd(new Date(grant.getEnd().getTime() + context.getDateDifference(getIdent())));
							}
							
							// Only grants for roles are kept
							nodeRightGrants.add(grant);
						}
					}
					
					nodeRight.setGrants(nodeRightGrants);
					nodeRightService.setRight(getModuleConfiguration(), nodeRight);
				}
			}
		}
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
			if (CourseNodeHelper.isCustomShortTitle(getLongTitle(), getShortTitle())) {
				copyInstance.setShortTitle(Formatter.truncateOnly("Copy of " + getShortTitle(), NodeConfigController.SHORT_TITLE_MAX_LENGTH));
			} else {
				copyInstance.setShortTitle(null);
			}
			copyInstance.setLongTitle("Copy of " + getLongTitle());
		}
		return copyInstance;
	}

	@Override
	public void copyConfigurationTo(CourseNode courseNode, ICourse course, Identity savedBy) {
		if(courseNode instanceof GenericCourseNode) {
			GenericCourseNode newNode = (GenericCourseNode)courseNode;
			newNode.setDisplayOption(getDisplayOption());
			if (StringHelper.containsNonWhitespace(getLearningObjectives())) {
				newNode.setDescription(getLearningObjectives());
			} else {
				newNode.setDescription(getDescription());
			}
			newNode.setLongTitle(getLongTitle());
			newNode.setNoAccessExplanation(getNoAccessExplanation());
			newNode.setShortTitle(getShortTitle());
			newNode.setTeaserImageSource(getTeaserImageSource());
			newNode.setTeaserImageStyle(getTeaserImageStyle());
			newNode.setColorCategoryIdentifier(getColorCategoryIdentifier());
			
			if(preConditionVisibility != null) {
				newNode.setPreConditionVisibility(preConditionVisibility.clone());
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(64);
		sb.append("courseNode[id=").append(getIdent()).append(":title=").append(getShortTitle()).append("]");
		return sb.toString();
	}

	@Override
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
	@Override
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

	@Override
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

	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent, NodeAccessType nodeAccessType) {
		if (isNewNode) {
			setDisplayOption(CourseNode.DISPLAY_OPTS_TITLE_DESCRIPTION_CONTENT);
			setTeaserImageStyle(TeaserImageStyle.DEFAULT_COURSE_NODE);
			setColorCategoryIdentifier(ColorCategory.IDENTIFIER_DEFAULT_COURSE_NODE);
			ImageSource newTeaserImageSource = CoreSpringFactory.getImpl(CourseStyleService.class)
					.createEmptyImageSource(ImageSourceType.DEFAULT_COURSE_NODE);
			setTeaserImageSource(newTeaserImageSource);
		}
		
		NodeAccessService nodeAccessService = CoreSpringFactory.getImpl(NodeAccessService.class);
		nodeAccessService.updateConfigDefaults(nodeAccessType, this, isNewNode);
	}
	
	@Override
	public boolean hasDates() {
		// Check for node specific dates
		if (!getNodeSpecificDatesWithLabel().isEmpty()) {
			return true;
		}
		
		ModuleConfiguration config = getModuleConfiguration();
		
		// Check for high score dates
		if (config.getDateValue(HighScoreEditController.CONFIG_KEY_DATESTART) != null) {
			return true;
		}
		
		// Check for user rights with dates
		// Identity dependant or group denendant rights are ignored! 
		Map<String, Object> potentialNodeRights = config.getConfigEntries(NodeRightServiceImpl.KEY_PREFIX);
		
		if (!potentialNodeRights.isEmpty()) {
			for (Map.Entry<String, Object> entry : potentialNodeRights.entrySet()) {
				if (!(entry.getValue() instanceof NodeRight)) {
					continue;
				}
				
				NodeRight nodeRight = (NodeRight) entry.getValue();
				
				if (nodeRight.getGrants() != null) {
					for (NodeRightGrant grant : nodeRight.getGrants()) {
						// Remove any rights associated with an identity or group
						if (grant.getBusinessGroupRef() != null || grant.getIdentityRef() != null) {
							continue;
						}
						
						// Move potential dates
						if (grant.getStart() != null) {
							return true;
						}
						
						if (grant.getEnd() != null) {
							return true;
						}
					}
				}
			}
		}
		
		// No dates found
		return false;		
	}
	
	@Override
	public List<Map.Entry<String, Date>>  getNodeSpecificDatesWithLabel() {
		return new ArrayList<>();
	}
	
	@Override
	public List<NodeRightType> getNodeRightTypes() {
		return new ArrayList<>();
	}

}
