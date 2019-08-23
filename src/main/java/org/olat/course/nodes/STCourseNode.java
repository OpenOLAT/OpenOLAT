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

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.commons.modules.singlepage.SinglePageController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.clone.CloneController;
import org.olat.core.gui.control.generic.clone.CloneLayoutControllerCreatorCallback;
import org.olat.core.gui.control.generic.clone.CloneableController;
import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.assessment.ui.tool.AssessmentCourseNodeController;
import org.olat.course.condition.Condition;
import org.olat.course.condition.KeyAndNameConverter;
import org.olat.course.condition.interpreter.ConditionExpression;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.NodeEditController;
import org.olat.course.editor.StatusDescription;
import org.olat.course.export.CourseEnvironmentMapper;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.groupsandrights.CourseRights;
import org.olat.course.nodes.sp.SPEditController;
import org.olat.course.nodes.sp.SPPeekviewController;
import org.olat.course.nodes.st.STCourseNodeEditController;
import org.olat.course.nodes.st.STCourseNodeRunController;
import org.olat.course.nodes.st.STIdentityListCourseNodeController;
import org.olat.course.nodes.st.STPeekViewController;
import org.olat.course.run.navigation.NodeRunConstructionResult;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.FailedEvaluationType;
import org.olat.course.run.scoring.ScoreCalculator;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.NodeEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.tree.CourseInternalLinkTreeModel;
import org.olat.group.BusinessGroup;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentRunStatus;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
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
public class STCourseNode extends AbstractAccessableCourseNode implements CalculatedAssessableCourseNode {
	
	private static final Logger log = Tracing.createLoggerFor(STCourseNode.class);

	private static final long serialVersionUID = -7460670977531082040L;
	private static final String TYPE = "st";
	private static final String ICON_CSS_CLASS = "o_st_icon";

	private ScoreCalculator scoreCalculator;

	transient private Condition scoreExpression;
	transient private Condition passedExpression;
	transient private Condition failedExpression;

	/**
	 * Constructor for a course building block of the type structure
	 */
	public STCourseNode() {
		super(TYPE);
		updateModuleConfigDefaults(true);
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#createEditController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl, org.olat.course.ICourse)
	 */
	@Override
	public TabbableController createEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, ICourse course, UserCourseEnvironment euce) {
		updateModuleConfigDefaults(false);
		// only the precondition "access" can be configured till now
		STCourseNodeEditController childTabCntrllr = new STCourseNodeEditController(ureq, wControl, this, course, euce);
		CourseNode chosenNode = course.getEditorTreeModel().getCourseNode(euce.getCourseEditorEnv().getCurrentCourseNodeId());
		NodeEditController nodeEditController = new NodeEditController(ureq, wControl, course.getEditorTreeModel(), course, chosenNode, euce, childTabCntrllr);
		// special case: listen to st edit controller, must be informed when the short title is being modified
		nodeEditController.addControllerListener(childTabCntrllr); 
		return nodeEditController;

	}

	@Override
	public NodeRunConstructionResult createNodeRunConstructionResult(UserRequest ureq, WindowControl wControl,
			final UserCourseEnvironment userCourseEnv, NodeEvaluation ne, String nodecmd) {
		updateModuleConfigDefaults(false);
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
			boolean hasEditRights = userCourseEnv.isAdmin() || cgm.hasRight(ureq.getIdentity(),CourseRights.RIGHT_COURSEEDITOR)
					|| (getModuleConfiguration().getBooleanSafe(SPEditController.CONFIG_KEY_ALLOW_COACH_EDIT, false) && userCourseEnv.isCoach());
			
			if (hasEditRights) {
				spCtr.allowPageEditing();
				// set the link tree model to internal for the HTML editor
				CustomLinkTreeModel linkTreeModel = new CourseInternalLinkTreeModel(userCourseEnv.getCourseEnvironment().getRunStructure().getRootNode());
				spCtr.setInternalLinkTreeModel(linkTreeModel);
			}
			spCtr.addLoggingResourceable(LoggingResourceable.wrap(this));
			// create clone wrapper layout, allow popping into second window
			CloneLayoutControllerCreatorCallback clccc = new CloneLayoutControllerCreatorCallback() {
				@Override
				public ControllerCreator createLayoutControllerCreator(final UserRequest uureq, final ControllerCreator contentControllerCreator) {
					return BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(uureq, new ControllerCreator() {
						@Override
						public Controller createController(UserRequest lureq, WindowControl lwControl) {
							// wrap in column layout, popup window needs a layout controller
							Controller ctr = contentControllerCreator.createController(lureq, lwControl);
							LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, lwControl, ctr);
							layoutCtr.setCustomCSS(CourseFactory.getCustomCourseCss(lureq.getUserSession(), userCourseEnv.getCourseEnvironment()));
							
							Controller wrappedCtrl = TitledWrapperHelper.getWrapper(lureq, lwControl, ctr, STCourseNode.this, ICON_CSS_CLASS);
							layoutCtr.addDisposableChildController(wrappedCtrl);
							return layoutCtr;
						}
					});
				}
			};
			Controller wrappedCtrl = TitledWrapperHelper.getWrapper(ureq, wControl, spCtr, this, ICON_CSS_CLASS);
			if(wrappedCtrl instanceof CloneableController) {
				cont = new CloneController(ureq, wControl, (CloneableController)wrappedCtrl, clccc);
			} else {
				throw new AssertException("Need to be a cloneable");
			}
		} else {
			// evaluate the score accounting for this node. this uses the score accountings local
			// cache hash map to reduce unnecessary calculations
			ScoreEvaluation se = userCourseEnv.getScoreAccounting().evalCourseNode(this);
			cont = TitledWrapperHelper.getWrapper(ureq, wControl, new STCourseNodeRunController(ureq, wControl, userCourseEnv, this, se, ne), this, ICON_CSS_CLASS);
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
	
	/**
	 * @see org.olat.course.nodes.GenericCourseNode#createPreviewController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl,
	 *      org.olat.course.run.userview.UserCourseEnvironment,
	 *      org.olat.course.run.userview.NodeEvaluation)
	 */
	@Override
	public Controller createPreviewController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, NodeEvaluation ne) {
		return createNodeRunConstructionResult(ureq, wControl, userCourseEnv, ne, null).getRunController();
	}
	
	
	/**
	 * @see org.olat.course.nodes.GenericCourseNode#createPeekViewRunController(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.WindowControl,
	 *      org.olat.course.run.userview.UserCourseEnvironment,
	 *      org.olat.course.run.userview.NodeEvaluation)
	 */
	@Override
	public Controller createPeekViewRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv,
			NodeEvaluation ne) {
		if (ne.isAtLeastOneAccessible()) {
			ModuleConfiguration config = getModuleConfiguration();
			if (STCourseNodeEditController.CONFIG_VALUE_DISPLAY_FILE.equals(config.getStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE))) {
				// use single page preview if a file is configured
				OLATResourceable ores = OresHelper.createOLATResourceableInstance(CourseModule.class, userCourseEnv.getCourseEnvironment().getCourseResourceableId());
				return new SPPeekviewController(ureq, wControl, userCourseEnv, config, ores);				
			} else {
				// a peekview controller that displays the listing of the next ST level
				return new STPeekViewController(ureq, wControl, ne);				
			}
		} else {
			// use standard peekview without content
			return super.createPeekViewRunController(ureq, wControl, userCourseEnv, ne);
		}
	}

	/**
	 * the structure node does not have a score itself, but calculates the
	 * score/passed info by evaluating the configured expression in the the
	 * (condition)interpreter.
	 * 
	 * @see org.olat.course.nodes.AssessableCourseNode#getUserScoreEvaluation(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	@Override
	public AssessmentEvaluation getUserScoreEvaluation(UserCourseEnvironment userCourseEnv) {
		Float score = null;
		Boolean passed = null;

		if (scoreCalculator == null) { 
			// this is a not-computable course node at the moment (no scoring/passing rules defined)
			return null; 
		}
		String scoreExpressionStr = scoreCalculator.getScoreExpression();
		String passedExpressionStr = scoreCalculator.getPassedExpression();

		ConditionInterpreter ci = userCourseEnv.getConditionInterpreter();
		if (scoreExpressionStr != null) {
			score = new Float(ci.evaluateCalculation(scoreExpressionStr));
		}
		if (passedExpressionStr != null) {
			passed = new Boolean(ci.evaluateCondition(passedExpressionStr));
		}
		return new AssessmentEvaluation(score, passed);
	}

	@Override
	public AssessmentEvaluation getUserScoreEvaluation(AssessmentEntry entry) {
		return AssessmentEvaluation.toAssessmentEvalutation(entry, this);
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#isConfigValid()
	 */
	@Override
	public StatusDescription isConfigValid() {
		/*
		 * first check the one click cache
		 */
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

	/**
	 * @see org.olat.course.nodes.CourseNode#isConfigValid(org.olat.course.run.userview.UserCourseEnvironment)
	 */
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

	/**
	 * @see org.olat.course.nodes.CourseNode#getReferencedRepositoryEntry()
	 */
	@Override
	public RepositoryEntry getReferencedRepositoryEntry() {
		return null;
	}

	/**
	 * @see org.olat.course.nodes.CourseNode#needsReferenceToARepositoryEntry()
	 */
	@Override
	public boolean needsReferenceToARepositoryEntry() {
		return false;
	}

	/**
	 * @return Returns the scoreCalculator.
	 */
	@Override
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

	/**
	 * @param scoreCalculator The scoreCalculator to set.
	 */
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

	@Override
	public boolean isAssessedBusinessGroups() {
		return false;
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getCutValueConfiguration()
	 */
	@Override
	public Float getCutValueConfiguration() {
		throw new OLATRuntimeException(STCourseNode.class, "Cut value never defined for ST nodes", null);
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getMaxScoreConfiguration()
	 */
	@Override
	public Float getMaxScoreConfiguration() {
		throw new OLATRuntimeException(STCourseNode.class, "Max score never defined for ST nodes", null);
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getMinScoreConfiguration()
	 */
	@Override
	public Float getMinScoreConfiguration() {
		throw new OLATRuntimeException(STCourseNode.class, "Min score never defined for ST nodes", null);
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getUserCoachComment(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	@Override
	public String getUserCoachComment(UserCourseEnvironment userCourseEnvironment) {
		throw new OLATRuntimeException(STCourseNode.class, "No coach comments available in ST nodes", null);
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getUserLog(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	@Override
	public String getUserLog(UserCourseEnvironment userCourseEnvironment) {
		throw new OLATRuntimeException(STCourseNode.class, "No user logs available in ST nodes", null);
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getUserUserComment(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	@Override
	public String getUserUserComment(UserCourseEnvironment userCourseEnvironment) {
		throw new OLATRuntimeException(STCourseNode.class, "No comments available in ST nodes", null);
	}

	@Override
	public List<File> getIndividualAssessmentDocuments(UserCourseEnvironment userCourseEnvironment) {
		return Collections.emptyList();
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#hasCommentConfigured()
	 */
	@Override
	public boolean hasCommentConfigured() {
		// never has comments
		return false;
	}

	@Override
	public boolean hasIndividualAsssessmentDocuments() {
		return false;
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#hasPassedConfigured()
	 */
	@Override
	public boolean hasPassedConfigured() {
		if (scoreCalculator != null && StringHelper.containsNonWhitespace(scoreCalculator.getPassedExpression())) {
			return true;
		}
		return false;
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#hasScoreConfigured()
	 */
	@Override
	public boolean hasScoreConfigured() {
		if (scoreCalculator != null && StringHelper.containsNonWhitespace(scoreCalculator.getScoreExpression())) {
			return true;
		}
		return false;
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#hasStatusConfigured()
	 */
	@Override
	public boolean hasStatusConfigured() {
		return false;
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#isEditableConfigured()
	 */
	@Override
	public boolean isEditableConfigured() {
		// ST nodes never editable, data generated on the fly
		return false;
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#updateUserCoachComment(java.lang.String,
	 *      org.olat.course.run.userview.UserCourseEnvironment)
	 */
	@Override
	public void updateUserCoachComment(String coachComment, UserCourseEnvironment userCourseEnvironment) {
		throw new OLATRuntimeException(STCourseNode.class, "Coach comment variable can't be updated in ST nodes", null);
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#updateUserScoreEvaluation(org.olat.course.run.scoring.ScoreEvaluation,
	 *      org.olat.course.run.userview.UserCourseEnvironment,
	 *      org.olat.core.id.Identity)
	 */
	@Override
	public void updateUserScoreEvaluation(ScoreEvaluation scoreEvaluation, UserCourseEnvironment userCourseEnvironment,
			Identity coachingIdentity, boolean incrementAttempts, Role by) {
		throw new OLATRuntimeException(STCourseNode.class, "Score variable can't be updated in ST nodes", null);
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#updateUserUserComment(java.lang.String,
	 *      org.olat.course.run.userview.UserCourseEnvironment,
	 *      org.olat.core.id.Identity)
	 */
	@Override
	public void updateUserUserComment(String userComment, UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity) {
		throw new OLATRuntimeException(STCourseNode.class, "Comment variable can't be updated in ST nodes", null);
	}

	@Override
	public void addIndividualAssessmentDocument(File document, String filename, UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity) {
		throw new OLATRuntimeException(STCourseNode.class, "Document can't be uploaded in ST nodes", null);
	}

	@Override
	public void removeIndividualAssessmentDocument(File document, UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity) {
		throw new OLATRuntimeException(STCourseNode.class, "Document can't be removed in ST nodes", null);
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#getUserAttempts(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	@Override
	public Integer getUserAttempts(UserCourseEnvironment userCourseEnvironment) {
		throw new OLATRuntimeException(STCourseNode.class, "No attempts available in ST nodes", null);

	}

	@Override
	public boolean hasCompletion() {
		return false;
	}

	@Override
	public Double getUserCurrentRunCompletion(UserCourseEnvironment userCourseEnvironment) {
		throw new OLATRuntimeException(STCourseNode.class, "No completion available in ST nodes", null);
	}
	
	@Override
	public void updateCurrentCompletion(UserCourseEnvironment userCourseEnvironment, Identity identity,
			Double currentCompletion, AssessmentRunStatus status, Role doneBy) {
		throw new OLATRuntimeException(STCourseNode.class, "Completion variable can't be updated in ST nodes", null);
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#hasAttemptsConfigured()
	 */
	@Override
	public boolean hasAttemptsConfigured() {
		return false;
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#updateUserAttempts(java.lang.Integer,
	 *      org.olat.course.run.userview.UserCourseEnvironment,
	 *      org.olat.core.id.Identity)
	 */
	@Override
	public void updateUserAttempts(Integer userAttempts, UserCourseEnvironment userCourseEnvironment, Identity coachingIdentity, Role by) {
		throw new OLATRuntimeException(STCourseNode.class, "Attempts variable can't be updated in ST nodes", null);
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#incrementUserAttempts(org.olat.course.run.userview.UserCourseEnvironment)
	 */
	@Override
	public void incrementUserAttempts(UserCourseEnvironment userCourseEnvironment, Role by) {
		throw new OLATRuntimeException(STCourseNode.class, "Attempts variable can't be updated in ST nodes", null);
	}

	@Override
	public void updateLastModifications(UserCourseEnvironment userCourseEnvironment, Identity identity, Role doneBy) {
		//do nothing
	}

	@Override
	public Controller getDetailsEditController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
			UserCourseEnvironment coachCourseEnv, UserCourseEnvironment assessedUserCourseEnv) {
		throw new OLATRuntimeException(STCourseNode.class, "Details controler not available in ST nodes", null);
	}

	@Override
	public boolean hasResultsDetails() {
		return false;
	}

	@Override
	public Controller getResultDetailsController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment assessedUserCourseEnv) {
		return null;
	}

	@Override
	public String getDetailsListView(UserCourseEnvironment userCourseEnvironment) {
		return null;
	}

	@Override
	public String getDetailsListViewHeaderKey() {
		throw new OLATRuntimeException(STCourseNode.class, "Details not available in ST nodes", null);
	}
	
	@Override
	public AssessmentCourseNodeController getIdentityListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, BusinessGroup group, UserCourseEnvironment coachCourseEnv,
			AssessmentToolContainer toolContainer, AssessmentToolSecurityCallback assessmentCallback) {
		return new STIdentityListCourseNodeController(ureq, wControl, stackPanel,
				courseEntry, group, this, coachCourseEnv, toolContainer, assessmentCallback);
	}

	/**
	 * @see org.olat.course.nodes.AssessableCourseNode#hasDetails()
	 */
	@Override
	public boolean hasDetails() {
		return false;
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
		if (isNewNode) {
			// use defaults for new course building blocks
			config.setBooleanEntry(STCourseNodeEditController.CONFIG_KEY_ALLOW_RELATIVE_LINKS, Boolean.FALSE.booleanValue());
			// set the default display to peekview in two columns
			config.setStringValue(STCourseNodeEditController.CONFIG_KEY_DISPLAY_TYPE, STCourseNodeEditController.CONFIG_VALUE_DISPLAY_PEEKVIEW);
			config.setIntValue(STCourseNodeEditController.CONFIG_KEY_COLUMNS, 2);

			DeliveryOptions defaultOptions = DeliveryOptions.defaultWithGlossary();
			config.set(SPEditController.CONFIG_KEY_DELIVERYOPTIONS, defaultOptions);
			
			config.setConfigurationVersion(3);
			
			scoreCalculator = new ScoreCalculator();
			scoreCalculator.setFailedType(FailedEvaluationType.failedAsNotPassedAfterEndDate);
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
	}
	
    @Override
    public void postCopy(CourseEnvironmentMapper envMapper, Processing processType, ICourse course, ICourse sourceCrourse) {
        super.postCopy(envMapper, processType, course, sourceCrourse);
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
			String processedExpression = KeyAndNameConverter.convertExpressionNameToKey(score, envMapper);
			processedExpression = KeyAndNameConverter.convertExpressionKeyToKey(score, envMapper);
			if(!processedExpression.equals(score)) {
				calculator.setScoreExpression(processedExpression);
				changed = true;
			}	
		}
		
		if(StringHelper.containsNonWhitespace(calculator.getPassedExpression())) {
			String passed = calculator.getPassedExpression();
			String processedExpression = KeyAndNameConverter.convertExpressionNameToKey(passed, envMapper);
			processedExpression = KeyAndNameConverter.convertExpressionKeyToKey(passed, envMapper);
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
	public void postExport(CourseEnvironmentMapper envMapper, boolean backwardsCompatible) {
		super.postExport(envMapper, backwardsCompatible);
		
		//if backwards compatible, convert expression to use names
		if(backwardsCompatible) {
			ScoreCalculator calculator = getScoreCalculator();
			boolean changed = false;
			if(StringHelper.containsNonWhitespace(calculator.getScoreExpression())) {
				String score = calculator.getScoreExpression();
				String processedExpression = KeyAndNameConverter.convertExpressionKeyToName(score, envMapper);
				if(!processedExpression.equals(score)) {
					calculator.setScoreExpression(processedExpression);
					changed = true;
				}	
			}
			
			if(StringHelper.containsNonWhitespace(calculator.getPassedExpression())) {
				String passed = calculator.getPassedExpression();
				String processedExpression = KeyAndNameConverter.convertExpressionKeyToName(passed, envMapper);
				if(!processedExpression.equals(passed)) {
					calculator.setScoreExpression(processedExpression);
					changed = true;
				}	
			}
			
			if(changed) {
				setScoreCalculator(calculator);
			}
		}
	}

	/**
	 * @see org.olat.course.nodes.AbstractAccessableCourseNode#getConditionExpressions()
	 */
	@Override
	public List<ConditionExpression> getConditionExpressions() {
		List<ConditionExpression> retVal;
		List<ConditionExpression> parentsConditions = super.getConditionExpressions();
		if (parentsConditions.size() > 0) {
			retVal = new ArrayList<>(parentsConditions);
		} else {
			retVal = new ArrayList<>();
		}
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
		return retVal;
	}

	/**
	 * @see org.olat.course.nodes.GenericCourseNode#getDefaultTitleOption()
	 */
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
}
