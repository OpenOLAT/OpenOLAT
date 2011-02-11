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
* <p>
*/ 

package org.olat.course.nodes.scorm;

import java.io.File;
import java.util.Iterator;
import java.util.Properties;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tree.TreeEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.ObjectivesHelper;
import org.olat.course.nodes.ScormCourseNode;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.FileResourceManager;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.scorm.ScormAPICallback;
import org.olat.modules.scorm.ScormAPIandDisplayController;
import org.olat.modules.scorm.ScormCPManifestTreeModel;
import org.olat.modules.scorm.ScormConstants;
import org.olat.modules.scorm.ScormMainManager;
import org.olat.repository.RepositoryEntry;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * Description: <BR/>Run controller for content packaging course nodes <P/>
 * 
 * @author Felix Jost
 */
public class ScormRunController extends BasicController implements ScormAPICallback {

	private ModuleConfiguration config;
	private File cpRoot;
	private Panel main;
	private VelocityContainer startPage;

	//private Translator translator;
	private ScormAPIandDisplayController scormDispC;
	private ScormCourseNode scormNode;

	// for external menu representation
	private ScormCPManifestTreeModel treeModel;
	private ControllerEventListener treeNodeClickListener;
	private UserCourseEnvironment userCourseEnv;
	private ChooseScormRunModeForm chooseScormRunMode;
	private boolean isPreview;
	
	private Identity identity;
	private boolean isAssessable;

	/**
	 * Use this constructor to launch a CP via Repository reference key set in the
	 * ModuleConfiguration. On the into page a title and the learning objectives
	 * can be placed.
	 * 
	 * @param config
	 * @param ureq
	 * @param userCourseEnv
	 * @param wControl
	 * @param cpNode
	 */
	public ScormRunController(ModuleConfiguration config, UserRequest ureq, UserCourseEnvironment userCourseEnv, WindowControl wControl,
			ScormCourseNode scormNode, boolean isPreview) {
		super(ureq, wControl);
		// assertion to make sure the moduleconfig is valid
		if (!ScormEditController.isModuleConfigValid(config)) throw new AssertException("scorm run controller had an invalid module config:"
				+ config.toString());
		this.isPreview = isPreview;
		this.userCourseEnv = userCourseEnv;
		this.config = config;
		this.scormNode = scormNode;
		this.identity = ureq.getIdentity();
		
		addLoggingResourceable(LoggingResourceable.wrap(scormNode));
		init(ureq);
	}

	private void init(UserRequest ureq) {

		startPage = createVelocityContainer ("run");
		// show browse mode option only if not assessable, hide it if in "real test mode"
		isAssessable = config.getBooleanSafe(ScormEditController.CONFIG_ISASSESSABLE);

		chooseScormRunMode = new ChooseScormRunModeForm(ureq, getWindowControl(), !isAssessable);
		listenTo(chooseScormRunMode);
		startPage.put("chooseScormRunMode", chooseScormRunMode.getInitialComponent());			
		
		main = new Panel("scormrunmain");
		// scorm always has a start page
		doStartPage(ureq);
		
		putInitialPanel(main);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == scormDispC) { // just pass on the event.
			doStartPage(ureq);
			fireEvent(ureq, event);
		} else if (source == null) { // external source
			if (event instanceof TreeEvent) {
				scormDispC.switchToPage((TreeEvent) event);
			}
		} else if (source == chooseScormRunMode) {
			doLaunch(ureq);
		}
	}

	private void doStartPage(UserRequest ureq) {

		// push title and learning objectives, only visible on intro page
		startPage.contextPut("menuTitle", scormNode.getShortTitle());
		startPage.contextPut("displayTitle", scormNode.getLongTitle());

		// Adding learning objectives
		String learningObj = scormNode.getLearningObjectives();
		if (learningObj != null) {
			Component learningObjectives = ObjectivesHelper.createLearningObjectivesComponent(learningObj, ureq);
			startPage.put("learningObjectives", learningObjectives);
			startPage.contextPut("hasObjectives", Boolean.TRUE);
		} else {
			startPage.contextPut("hasObjectives", Boolean.FALSE);
		}
		
		if (isAssessable) {
			ScoreEvaluation scoreEval = scormNode.getUserScoreEvaluation(userCourseEnv);
			Float score = scoreEval.getScore();
			startPage.contextPut("score", score != null ? AssessmentHelper.getRoundedScore(score) : "0");
			startPage.contextPut("hasPassedValue", (scoreEval.getPassed() == null ? Boolean.FALSE : Boolean.TRUE));
			startPage.contextPut("passed", scoreEval.getPassed());
			startPage.contextPut("comment", scormNode.getUserUserComment(userCourseEnv));
			startPage.contextPut("attempts", scormNode.getUserAttempts(userCourseEnv));
		}
		startPage.contextPut("isassessable", Boolean.valueOf(isAssessable));
		main.setContent(startPage);
	}

	private void doLaunch(UserRequest ureq) {
		if (cpRoot == null) {
			// it is the first time we start the contentpackaging from this instance
			// of this controller.
			// need to be strict when launching -> "true"
			RepositoryEntry re = ScormEditController.getScormCPReference(config, true);
			if (re == null) throw new AssertException("configurationkey 'CONFIG_KEY_REPOSITORY_SOFTKEY' of BB CP was missing");
			cpRoot = FileResourceManager.getInstance().unzipFileResource(re.getOlatResource());
			addLoggingResourceable(LoggingResourceable.wrapScormRepositoryEntry(re));
			// should always exist because references cannot be deleted as long as
			// nodes reference them
			if (cpRoot == null) throw new AssertException("file of repository entry " + re.getKey() + " was missing");
		}
		// else cpRoot is already set (save some db access if the user opens /
		// closes / reopens the cp from the same CPRuncontroller instance)

		String courseId;
		boolean showMenu = config.getBooleanSafe(ScormEditController.CONFIG_SHOWMENU, true);

		if (isPreview) {
			courseId = new Long(CodeHelper.getRAMUniqueID()).toString();
			scormDispC = ScormMainManager.getInstance().createScormAPIandDisplayController(ureq, getWindowControl(), showMenu, null, cpRoot, null, courseId, ScormConstants.SCORM_MODE_BROWSE, ScormConstants.SCORM_MODE_NOCREDIT, true, true);
		} else {
			courseId = userCourseEnv.getCourseEnvironment().getCourseResourceableId().toString();
			if(isAssessable) {
				scormDispC = ScormMainManager.getInstance().createScormAPIandDisplayController(ureq, getWindowControl(), showMenu, this, cpRoot, null, courseId + "-" + scormNode.getIdent(), ScormConstants.SCORM_MODE_NORMAL, ScormConstants.SCORM_MODE_CREDIT, false, true);
				scormNode.incrementUserAttempts(userCourseEnv);
			} else if(chooseScormRunMode.getSelectedElement().equals(ScormConstants.SCORM_MODE_NORMAL)){
				scormDispC = ScormMainManager.getInstance().createScormAPIandDisplayController(ureq, getWindowControl(), showMenu, null, cpRoot, null, courseId + "-" + scormNode.getIdent(), ScormConstants.SCORM_MODE_NORMAL, ScormConstants.SCORM_MODE_CREDIT, false, true);
			} else {
				scormDispC = ScormMainManager.getInstance().createScormAPIandDisplayController(ureq, getWindowControl(), showMenu, null, cpRoot, null, courseId, ScormConstants.SCORM_MODE_BROWSE, ScormConstants.SCORM_MODE_NOCREDIT, false, true);
			}
		}
		// configure some display options
		boolean showNavButtons = config.getBooleanSafe(ScormEditController.CONFIG_SHOWNAVBUTTONS, true);
		scormDispC.showNavButtons(showNavButtons);
		String height = (String) config.get(ScormEditController.CONFIG_HEIGHT);
		if ( ! height.equals(ScormEditController.CONFIG_HEIGHT_AUTO)) {
			scormDispC.setHeightPX(Integer.parseInt(height));
		}
		String contentEncoding = (String) config.get(NodeEditController.CONFIG_CONTENT_ENCODING);
		if ( ! contentEncoding.equals(NodeEditController.CONFIG_CONTENT_ENCODING_AUTO)) {
			scormDispC.setContentEncoding(contentEncoding);
		}
		String jsEncoding = (String) config.get(NodeEditController.CONFIG_JS_ENCODING);
		if ( ! jsEncoding.equals(NodeEditController.CONFIG_JS_ENCODING_AUTO)) {
			scormDispC.setJSEncoding(jsEncoding);
		}
		
		// the scormDispC activates itself
	}
	
	/* (non-Javadoc)
	 * @see org.olat.modules.scorm.ScormAPICallback#lmsCommit(java.lang.String, java.util.Properties)
	 */
	public void lmsCommit(String olatSahsId, Properties scoScores) {
		// only write score info when node is configured to do so
		if(isAssessable) {		
			// do a sum-of-scores over all sco scores
			float score = 0f;
			for (Iterator it_score = scoScores.values().iterator(); it_score.hasNext();) {
				String aScore = (String) it_score.next();
				float ascore = Float.parseFloat(aScore);
				score+= ascore;
			}
			float cutval = scormNode.getCutValueConfiguration().floatValue();
			boolean passed = (score >= cutval);
			ScoreEvaluation sceval = new ScoreEvaluation(new Float(score), Boolean.valueOf(passed));
			boolean incrementAttempts = false;
			scormNode.updateUserScoreEvaluation(sceval, userCourseEnv, identity, incrementAttempts);
			userCourseEnv.getScoreAccounting().scoreInfoChanged(scormNode, sceval);
						
			if (Tracing.isDebugEnabled(this.getClass())) {
				String msg = "for scorm node:"+scormNode.getIdent()+" ("+scormNode.getShortTitle()+") a lmsCommit for scoId "+olatSahsId+" occured, total sum = "+score+", cutvalue ="+cutval+", passed: "+passed+", all scores now = "+scoScores.toString();
				Tracing.logDebug(msg, this.getClass());
			}
		}
	}
	

	/**
	 * @return true if there is a treemodel and an event listener ready to be used
	 *         in outside this controller
	 */
	public boolean isExternalMenuConfigured() {
		return (config.getBooleanEntry(NodeEditController.CONFIG_COMPONENT_MENU).booleanValue());
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		//
	}

	/**
	 * @return the treemodel of the enclosed ScormDisplayController, or null, if
	 *         no tree should be displayed (configured by author, see
	 *         DisplayConfigurationForm.CONFIG_COMPONENT_MENU)
	 */
	public ScormCPManifestTreeModel getTreeModel() {
		return treeModel;
	}

	/**
	 * @return the listener to listen to clicks to the nodes of the treemodel
	 *         obtained calling getTreeModel()
	 */
	public ControllerEventListener getTreeNodeClickListener() {
		return treeNodeClickListener;
	}
	
}