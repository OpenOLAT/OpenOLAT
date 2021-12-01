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
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.GroupRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.commons.modules.singlepage.SinglePageController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.condition.Condition;
import org.olat.course.condition.KeyAndNameConverter;
import org.olat.course.condition.interpreter.ConditionExpression;
import org.olat.course.editor.ConditionAccessEditConfig;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.editor.importnodes.ImportSettings;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.sp.SPEditController;
import org.olat.course.nodes.sp.SPPeekviewController;
import org.olat.course.nodes.st.STCourseNodeEditController;
import org.olat.course.nodes.st.STCourseNodeRunController;
import org.olat.course.nodes.st.STPeekViewController;
import org.olat.course.nodes.st.STReminderProvider;
import org.olat.course.reminder.CourseNodeReminderProvider;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.scoring.FailedEvaluationType;
import org.olat.course.run.scoring.ScoreCalculator;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.tools.CourseToolLinkTreeModel;
import org.olat.course.run.userview.AccessibleFilter;
import org.olat.course.run.userview.CourseNodeSecurityCallback;
import org.olat.course.run.userview.CourseTreeNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.course.tree.CourseInternalLinkTreeModel;
import org.olat.modules.ModuleConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.ui.author.copy.wizard.CopyCourseContext;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Description:<br>
 * The structure node (ST) is used to build structures in the course hierarchy.
 * In addition it is also used to calculate score and passed values, to syndicate
 * these values e.g. from children nodes. Example: a lesson with two tests is 
 * passed when both tests are passed. This would be designed as an ST node with
 * two IMSTEST nodes as children and a scoring rule on the ST node that syndicates
 * the testresults. In the assessment tool the ST node results can be seen but not 
 * changed since these are calculated values and not saved values from properties.
 * 
 * <P>
 * Initial Date: Feb 9, 2004<br>
 * @author Mike Stock
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 */
public class STCourseNode extends AbstractAccessableCourseNode {
	
	private static final Logger log = Tracing.createLoggerFor(STCourseNode.class);

	private static final long serialVersionUID = -7460670977531082040L;
	public static final String TYPE = "st";
	private static final String ICON_CSS_CLASS = "o_st_icon";
	
	private static final int CURRENT_VERSION = 7;
	
	// Score calculation without conditions.
	public static final String CONFIG_SCORE_KEY = "score.key";
	public static final String CONFIG_SCORE_VALUE_SUM = "score.sum";
	public static final String CONFIG_SCORE_VALUE_AVG = "score.avg";
	public static final String CONFIG_PASSED_PROGRESS = "passed.progress";
	public static final String CONFIG_PASSED_ALL = "passed.all";
	public static final String CONFIG_PASSED_NUMBER = "passed.number";
	public static final String CONFIG_PASSED_NUMBER_CUT = "passed.number.cut";
	public static final String CONFIG_PASSED_POINTS = "passed.points";
	public static final String CONFIG_PASSED_POINTS_CUT = "passed.points.cut";
	// Defines whether the COACH can override the passed.
	public static final String CONFIG_PASSED_MANUALLY = "passed.manually";
	public static final String CONFIG_COACH_USER_VISIBILITY = "coach.user.visibility";

	// Score calculation with conditions.
	public static final String CONFIG_SCORE_CALCULATOR_SUPPORTED = "score.calculator.supported";
	private ScoreCalculator scoreCalculator;
	transient private Condition scoreExpression;
	transient private Condition passedExpression;
	transient private Condition failedExpression;
	
	public STCourseNode() {
		super(TYPE);
	}

	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		STCourseNodeEditController childTabCntrllr = new STCourseNodeEditController(ureq, wControl, this, course, euce);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		NodeEditController nodeEditController = new NodeEditController(ureq, wControl, stackPanel, course, chosenNode, euce, childTabCntrllr);
		// special case: listen to st edit controller, must be informed when the short title is being modified
		nodeEditController.addControllerListener(childTabCntrllr); 
		return nodeEditController;
	}

	@Override
	public ConditionAccessEditConfig getAccessEditConfig() {
		return ConditionAccessEditConfig.regular(true);
	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			final UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback, String nodecmd) {
		Controller cont;
		
		String displayType = getModuleConfiguration().getStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE);
		String relPath = STCourseNodeEditController.getFileName(getModuleConfiguration());
		
		if (relPath != null && displayType.equals(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_FILE)) {
			// we want a user chosen overview, so display the chosen file from the
			// material folder, otherwise display the normal overview
			// reuse the Run controller from the "Single Page" building block, since
			// we need to do exactly the same task
			Boolean allowRelativeLinks = getModuleConfiguration().getBooleanEntry(STCourseNodeEditController.CONFIG_KEY_ALLOW_RELATIVE_LINKS);
			if(allowRelativeLinks == null) {
				allowRelativeLinks = Boolean.FALSE;
			}
			DeliveryOptions deliveryOptions = (DeliveryOptions)getModuleConfiguration().get(SPEditController.CONFIG_KEY_DELIVERYOPTIONS);
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(CourseModule.class, userCourseEnv.getCourseEnvironment().getCourseResourceableId());
			Long courseRepoKey = userCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry().getKey();
			SinglePageController spCtr = new SinglePageController(ureq, wControl, userCourseEnv.getCourseEnvironment().getCourseFolderContainer(),
					relPath, allowRelativeLinks.booleanValue(), null, ores, deliveryOptions,
					userCourseEnv.getCourseEnvironment().isPreview(), courseRepoKey);
			// check if user is allowed to edit the page in the run view
			CourseGroupManager cgm = userCourseEnv.getCourseEnvironment().getCourseGroupManager();
			GroupRoles role = GroupRoles.owner;
			if (userCourseEnv.isParticipant()) {
				role = GroupRoles.participant;
			} else if (userCourseEnv.isCoach()) {
				role = GroupRoles.coach;
			}
			boolean hasEditRights = userCourseEnv.isAdmin() || cgm.hasRight(ureq.getIdentity(),CourseRights.RIGHT_COURSEEDITOR, role)
					|| (getModuleConfiguration().getBooleanSafe(SPEditController.CONFIG_KEY_ALLOW_COACH_EDIT, false) && userCourseEnv.isCoach());
			
			if (hasEditRights) {
				spCtr.allowPageEditing();
				// set the link tree model to internal for the HTML editor
				CustomLinkTreeModel linkTreeModel = new CourseInternalLinkTreeModel(userCourseEnv.getCourseEnvironment().getRunStructure().getRootNode());
				spCtr.setInternalLinkTreeModel(linkTreeModel);
				spCtr.setToolLinkTreeModel(new CourseToolLinkTreeModel(userCourseEnv.getCourseEnvironment().getCourseConfig(), ureq.getLocale()));
			}
			spCtr.addLoggingResourceable(LoggingResourceable.wrap(this));
			cont = TitledWrapperHelper.getWrapper(ureq, wControl, spCtr, userCourseEnv, this, ICON_CSS_CLASS);
		} else {
			// evaluate the score accounting for this node. this uses the score accountings local
			// cache hash map to reduce unnecessary calculations
			ScoreEvaluation se = userCourseEnv.getScoreAccounting().evalCourseNode(this);
			cont = TitledWrapperHelper.getWrapper(ureq, wControl, new STCourseNodeRunController(ureq, wControl, userCourseEnv, this, se), userCourseEnv, this, ICON_CSS_CLASS);
		}

		// access the current calculated score, if there is one, so that it can be
		// displayed in the ST-Runcontroller
		return new NodeRunConstructionResult(cont);
	}
	
	/**
	 * Checks if the given CourseNode is of type "Structure Node" and if it is set
	 * to delegate to it's first visible child
	 * 
	 * @param nodeToCheck
	 * @return returns true if the given coursenNode is a STCourseNode and is configured to delegate
	 */
	public static boolean isDelegatingSTCourseNode(CourseNode nodeToCheck) {
		if (!(nodeToCheck instanceof STCourseNode)) return false;
		
		STCourseNode node = (STCourseNode) nodeToCheck;
		String displayMode = node.getModuleConfiguration().getStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE,
				STCourseNodeEditController.CONFIG_VALUE_DISPLAY_TOC);
		return (STCourseNodeEditController.CONFIG_VALUE_DISPLAY_DELEGATE.equals(displayMode));
	}
	
	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, CourseNodeSecurityCallback nodeSecCallback) {
		return createNodeRunConstructionResult(ureq, wControl, userCourseEnv, nodeSecCallback, null).getRunController();
	}
	
	@Override
	public Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			CourseNodeSecurityCallback nodeSecCallback, boolean small) {
		if (nodeSecCallback.isAccessible()) {
			ModuleConfiguration config = getModuleConfiguration();
			INode parent = getParent();
			boolean showFile = true;
			if (parent instanceof STCourseNode) {
				STCourseNode parentNode = (STCourseNode)parent;
				String peekViewFilterConfig = parentNode.getModuleConfiguration().getStringValue(STCourseNodeEditController.CONFIG_KEY_PEEKVIEW_FILTER, STCourseNodeEditController.CONFIG_VALUE_PEEKVIEW_ALL);
				showFile = !STCourseNodeEditController.CONFIG_VALUE_PEEKVIEW_STRUCTURES.equals(peekViewFilterConfig);
			}
			if (showFile && STCourseNodeEditController.CONFIG_VALUE_DISPLAY_FILE.equals(config.getStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE))) {
				// use single page preview if a file is configured
				OLATResourceable ores = OresHelper.createOLATResourceableInstance(CourseModule.class, userCourseEnv.getCourseEnvironment().getCourseResourceableId());
				return new SPPeekviewController(ureq, wControl, userCourseEnv, config, ores);				
			}
			CourseTreeNode courseTreeNode = (CourseTreeNode)CoreSpringFactory.getImpl(NodeAccessService.class)
					.getCourseTreeModelBuilder(userCourseEnv)
					.withFilter(AccessibleFilter.create())
					.build()
					.getNodeById(this.getIdent());
			if (courseTreeNode != null && courseTreeNode.getChildCount() > 0) {
				return new STPeekViewController(ureq, wControl, courseTreeNode, userCourseEnv.getCourseEnvironment().getCourseConfig(), !small);
			}
		}
		return super.createPeekViewRunController(ureq, wControl, userCourseEnv, nodeSecCallback, small);
	}

	@Override
	public StatusDescription isConfigValid() {
		if (oneClickStatusCache != null) { return oneClickStatusCache[0]; }

		ModuleConfiguration config = getModuleConfiguration();
		StatusDescription sd = StatusDescription.NOERROR;
		if (STCourseNodeEditController.CONFIG_VALUE_DISPLAY_FILE.equals(config.getStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE))){
			String fileName = (String) config.get(STCourseNodeEditController.CONFIG_KEY_FILE);
			if (fileName == null || !StringHelper.containsNonWhitespace(fileName)){
				String shortKey = "error.missingfile.short";
				String longKey = "error.missingfile.long";
				String[] params = new String[] { this.getShortTitle() };
				String translPackage = Util.getPackageName(SPEditController.class);
				sd = new StatusDescription(StatusDescription.ERROR, shortKey, longKey, params, translPackage);
				sd.setDescriptionForUnit(getIdent());
				// set which pane is affected by error
				sd.setActivateableViewIdentifier(STCourseNodeEditController.PANE_TAB_ST_CONFIG);
			}
		}
		return sd;
	}

	@Override
	public StatusDescription[] isConfigValid(CourseEditorEnv cev) {
		oneClickStatusCache = null;
		// only here we know which translator to take for translating condition
		// error messages
		String translatorStr = Util.getPackageName(STCourseNodeEditController.class);
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

	public ScoreCalculator getScoreCalculator() {
		if (scoreCalculator == null) {
			scoreCalculator = new ScoreCalculator();
			scoreCalculator.setFailedType(FailedEvaluationType.failedAsNotPassedAfterEndDate);
		}
		
		passedExpression = new Condition();
		passedExpression.setConditionId("passed");
		if (scoreCalculator.getPassedExpression() != null) {
			passedExpression.setConditionExpression(scoreCalculator.getPassedExpression());
			passedExpression.setExpertMode(true);
		}
		
		scoreExpression = new Condition();
		scoreExpression.setConditionId("score");
		if (scoreCalculator.getScoreExpression() != null) {
			scoreExpression.setConditionExpression(scoreCalculator.getScoreExpression());
			scoreExpression.setExpertMode(true);
		}
		
		failedExpression = new Condition();
		failedExpression.setConditionId("failed");
		if (scoreCalculator.getFailedExpression() != null) {
			failedExpression.setConditionExpression(scoreCalculator.getFailedExpression());
			failedExpression.setExpertMode(true);
		}
		return scoreCalculator;
	}

	public void setScoreCalculator(ScoreCalculator scoreCalculatorP) {
		scoreCalculator = scoreCalculatorP;
		if (scoreCalculatorP == null) {
			scoreCalculator = getScoreCalculator();
		}

		String score = scoreCalculator.getScoreExpression();
		scoreExpression.setExpertMode(true);
		scoreExpression.setConditionExpression(score);
		scoreExpression.setConditionId("score");
		
		String passed = scoreCalculator.getPassedExpression();
		passedExpression.setExpertMode(true);
		passedExpression.setConditionExpression(passed);
		passedExpression.setConditionId("passed");
		
		String failed = scoreCalculator.getFailedExpression();
		failedExpression.setExpertMode(true);
		failedExpression.setConditionExpression(failed);
		failedExpression.setConditionId("failed");
	}

	/**
	 * Update the module configuration to have all mandatory configuration flags
	 * set to usefull default values
	 * @param isNewNode true: an initial configuration is set; false: upgrading
	 *          from previous node configuration version, set default to maintain
	 *          previous behaviour
	 */
	@Override
	public void updateModuleConfigDefaults(boolean isNewNode, INode parent, NodeAccessType nodeAccessType) {
		super.updateModuleConfigDefaults(isNewNode, parent, nodeAccessType);
		
		ModuleConfiguration config = getModuleConfiguration();
		if (isNewNode) {
			config.setBooleanEntry(STCourseNodeEditController.CONFIG_KEY_ALLOW_RELATIVE_LINKS, Boolean.FALSE.booleanValue());
			config.setStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE, STCourseNodeEditController.CONFIG_VALUE_DISPLAY_DELEGATE);
			config.setIntValue(STCourseNodeEditController.CONFIG_KEY_COLUMNS, 2);

			DeliveryOptions defaultOptions = DeliveryOptions.defaultWithGlossary();
			config.set(SPEditController.CONFIG_KEY_DELIVERYOPTIONS, defaultOptions);
			
			config.set(STCourseNodeEditController.CONFIG_KEY_PEEKVIEW_FILTER, STCourseNodeEditController.CONFIG_VALUE_PEEKVIEW_ALL);
			config.setStringValue(STCourseNodeEditController.CONFIG_KEY_CHILDREN_FILTER, STCourseNodeEditController.CONFIG_VALUE_CHILDREN_ALL);
			
			config.setConfigurationVersion(3);
			
			STCourseNode stParent = getFirstSTParent(parent);
			if (stParent != null) {
				boolean scoreCalculatorSupported = stParent.getModuleConfiguration().getBooleanSafe(CONFIG_SCORE_CALCULATOR_SUPPORTED, true);
				config.setBooleanEntry(CONFIG_SCORE_CALCULATOR_SUPPORTED, scoreCalculatorSupported);
				if (scoreCalculatorSupported) {
					scoreCalculator = new ScoreCalculator();
					scoreCalculator.setFailedType(FailedEvaluationType.failedAsNotPassedAfterEndDate);
				}
			} // else: is course root and initialized in CreateCourseRepositoryEntryController / REST
			
		} else {
			// update to version 2
			if (config.getConfigurationVersion() < 2) {
				// use values accoring to previous functionality
				config.setBooleanEntry(STCourseNodeEditController.CONFIG_KEY_ALLOW_RELATIVE_LINKS, Boolean.FALSE.booleanValue());
				// previous version of score st node didn't have easy mode on score
				// calculator, se to expert mode
				if (getScoreCalculator() != null) {
					getScoreCalculator().setExpertMode(true);
				}
				config.setConfigurationVersion(2);
			}
			// update to version 3
			if (config.getConfigurationVersion() < 3) {
				String fileName = (String) config.get(STCourseNodeEditController.CONFIG_KEY_FILE);
				if (fileName != null) {
					// set to custom file display config
					config.setStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE, STCourseNodeEditController.CONFIG_VALUE_DISPLAY_FILE);
				} else {
					// set the default display to plain vanilla TOC view in one column
					config.setStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE, STCourseNodeEditController.CONFIG_VALUE_DISPLAY_TOC);
					config.setIntValue(STCourseNodeEditController.CONFIG_KEY_COLUMNS, 1);
				}
				config.setConfigurationVersion(3);
			}

			if (config.getConfigurationVersion() < 4) {
				if(config.get(SPEditController.CONFIG_KEY_DELIVERYOPTIONS) == null) {
					DeliveryOptions defaultOptions = DeliveryOptions.defaultWithGlossary();
					config.set(SPEditController.CONFIG_KEY_DELIVERYOPTIONS, defaultOptions);
				}
				config.setConfigurationVersion(4);
			}
		}
		if (config.getConfigurationVersion() < 6) {
			config.set(STCourseNodeEditController.CONFIG_KEY_PEEKVIEW_FILTER, STCourseNodeEditController.CONFIG_VALUE_PEEKVIEW_ALL);
			String childrenFilter = StringHelper.containsNonWhitespace(config.getStringValue(STCourseNodeEditController.CONFIG_KEY_CHILDREN_IDENTS, ""))
					? STCourseNodeEditController.CONFIG_VALUE_CHILDREN_SELECTION
					: STCourseNodeEditController.CONFIG_VALUE_CHILDREN_ALL;
			config.setStringValue(STCourseNodeEditController.CONFIG_KEY_CHILDREN_FILTER, childrenFilter);
		}
		if (config.getConfigurationVersion() < 7) {
			config.setBooleanEntry(CONFIG_COACH_USER_VISIBILITY, true);
		}
		
		config.setConfigurationVersion(CURRENT_VERSION);
	}
	
	public static STCourseNode getFirstSTParent(INode node) {
		if (node != null) {
			CourseNode courseNode = null;
			if (node instanceof CourseEditorTreeNode) {
				CourseEditorTreeNode cetn = (CourseEditorTreeNode)node;
				courseNode = cetn.getCourseNode();
			}
			if (node instanceof CourseNode) {
				courseNode = (CourseNode)node;
			}
			if (courseNode instanceof STCourseNode) {
				return (STCourseNode)courseNode;
			}
			INode parentNode = node.getParent();
			return getFirstSTParent(parentNode);
		}
		return null;
	}

	@Override
    public void postCopy(CourseEnvironmentMapper envMapper, Processing processType, ICourse course, ICourse sourceCourse, CopyCourseContext context) {
        super.postCopy(envMapper, processType, course, sourceCourse, context);
        postImportCopy(envMapper);
    }
	
	@Override
	public void postImport(File importDirectory, ICourse course, CourseEnvironmentMapper envMapper, Processing processType) {
		super.postImport(importDirectory, course, envMapper, processType);
		postImportCopy(envMapper);
	}
		
	private void postImportCopy(CourseEnvironmentMapper envMapper) {
		ScoreCalculator calculator = getScoreCalculator();
		boolean changed = false;
		if(StringHelper.containsNonWhitespace(calculator.getScoreExpression())) {
			String score = calculator.getScoreExpression();
			String processedExpression = KeyAndNameConverter.convertExpressionKeyToKey(score, envMapper);
			if(!processedExpression.equals(score)) {
				calculator.setScoreExpression(processedExpression);
				changed = true;
			}	
		}
		
		if(StringHelper.containsNonWhitespace(calculator.getPassedExpression())) {
			String passed = calculator.getPassedExpression();
			String processedExpression = KeyAndNameConverter.convertExpressionKeyToKey(passed, envMapper);
			if(!processedExpression.equals(passed)) {
				calculator.setScoreExpression(processedExpression);
				changed = true;
			}	
		}
		
		if(changed) {
			setScoreCalculator(calculator);
		}
	}
	
	@Override
	public void postImportCourseNodes(ICourse course, CourseNode sourceCourseNode, ICourse sourceCourse, ImportSettings settings, CourseEnvironmentMapper envMapper) {
		super.postImportCourseNodes(course, sourceCourseNode, sourceCourse, settings, envMapper);
		
		// update course node references
		// getScoreCalculator()
		
		STCourseNode srcNode = (STCourseNode) sourceCourseNode;

		List<String> snodes = srcNode.getScoreCalculator().getSumOfScoreNodes();
		if (snodes != null) {
			getScoreCalculator().setSumOfScoreNodes(
				snodes.stream()
					.map(envMapper::getNodeTargetIdent)
					.filter(Objects::nonNull)
					.collect(Collectors.toList()));
			if (!getScoreCalculator().isExpertMode()) {
				getScoreCalculator().setScoreExpression(getScoreCalculator().getScoreExpressionFromEasyModeConfiguration());
			}
		}

		List<String> spnodes = srcNode.getScoreCalculator().getPassedNodes();
		if (spnodes != null) {
			getScoreCalculator().setPassedNodes(
					spnodes.stream()
						.map(envMapper::getNodeTargetIdent)
						.filter(Objects::nonNull)
						.collect(Collectors.toList()));
			if (!getScoreCalculator().isExpertMode()) {
				getScoreCalculator().setPassedExpression(getScoreCalculator().getPassedExpressionFromEasyModeConfiguration());
			}
		}

		if (getScoreCalculator().isExpertMode()) {
			getScoreCalculator().setPassedExpression(KeyAndNameConverter
					.replaceIdsInCondition(getScoreCalculator().getPassedExpression(), envMapper));
			getScoreCalculator().setScoreExpression(KeyAndNameConverter
					.replaceIdsInCondition(getScoreCalculator().getScoreExpression(), envMapper));			
		}
	}

	@Override
	public List<ConditionExpression> getConditionExpressions() {
		List<ConditionExpression> retVal;
		List<ConditionExpression> parentsConditions = super.getConditionExpressions();
		if (!parentsConditions.isEmpty()) {
			retVal = new ArrayList<>(parentsConditions);
		} else {
			retVal = new ArrayList<>();
		}
		
		if (getModuleConfiguration().getBooleanSafe(STCourseNode.CONFIG_SCORE_CALCULATOR_SUPPORTED, true)) {
			// init passedExpression and scoreExpression
			getScoreCalculator();

			passedExpression.setExpertMode(true);
			String coS = passedExpression.getConditionExpression();
			if (StringHelper.containsNonWhitespace(coS)) {
				// an active condition is defined
				ConditionExpression ce = new ConditionExpression(passedExpression.getConditionId());
				ce.setExpressionString(passedExpression.getConditionExpression());
				retVal.add(ce);
			}
			
			scoreExpression.setExpertMode(true);
			coS = scoreExpression.getConditionExpression();
			if (StringHelper.containsNonWhitespace(coS)) {
				// an active condition is defined
				ConditionExpression ce = new ConditionExpression(scoreExpression.getConditionId());
				ce.setExpressionString(scoreExpression.getConditionExpression());
				retVal.add(ce);
			}
			
			failedExpression.setExpertMode(true);
			coS = failedExpression.getConditionExpression();
			if (StringHelper.containsNonWhitespace(coS)) {
				// an active condition is defined
				ConditionExpression ce = new ConditionExpression(failedExpression.getConditionId());
				ce.setExpressionString(failedExpression.getConditionExpression());
				retVal.add(ce);
			}
		}
		return retVal;
	}

	@Override
	public String getDisplayOption() {
		// if nothing other defined, view content only, when a structure node
		// contains an html-file.
		ModuleConfiguration config = getModuleConfiguration();
		String thisConf = super.getDisplayOption(false);
		if (thisConf == null
				&& config.get(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE).equals(STCourseNodeEditController.CONFIG_VALUE_DISPLAY_FILE)) {
			log.debug("no displayOption set, use default (content) {}",  thisConf);

			return CourseNode.DISPLAY_OPTS_CONTENT;
		}
		log.debug("there is a config set, use it: {}",  thisConf);
		return super.getDisplayOption();
	}
	
	@Override
	public CourseNodeReminderProvider getReminderProvider(boolean rootNode) {
		// Only supported on root nodes. The STAssessmentHandler needs the course node hierarchy.
		// We do not have this hierarchy when we are in the editor or the hierarchy is different than
		// the hierarchy in the run model.
		if (rootNode) {
			return new STReminderProvider(this);
		}
		return null;
	}
	
}