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

package org.olat.course.nodes.ms;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.DefaultController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.auditing.UserNodeAuditManager;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.ObjectivesHelper;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;

/**
 * Initial Date:  Jun 16, 2004
 * @author gnaegi
 */
public class MSCourseNodeRunController extends DefaultController {

	private static final String PACKAGE = Util.getPackageName(MSCourseNodeRunController.class);
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(MSCourseNodeRunController.class);

	private VelocityContainer myContent;

	/**
	 * Constructor for a manual scoring course run controller
	 * @param ureq The user request
	 * @param userCourseEnv The user course environment
	 * @param msCourseNode The manual scoring course node
	 * @param displayNodeInfo True: the node title and learning objectives will be displayed
	 */
	public MSCourseNodeRunController(UserRequest ureq, WindowControl wControl, UserCourseEnvironment userCourseEnv, AssessableCourseNode msCourseNode, boolean displayNodeInfo) {
		super(wControl);
		PackageTranslator trans = new PackageTranslator(PACKAGE, ureq.getLocale());
		
		myContent = new VelocityContainer("olatmsrun", VELOCITY_ROOT + "/run.html", trans, this);
		
		ModuleConfiguration config = msCourseNode.getModuleConfiguration();
		myContent.contextPut("displayNodeInfo", Boolean.valueOf(displayNodeInfo));
		if (displayNodeInfo) {
			// push title and learning objectives, only visible on intro page
			myContent.contextPut("menuTitle", msCourseNode.getShortTitle());
			myContent.contextPut("displayTitle", msCourseNode.getLongTitle());
			
			// Adding learning objectives
			String learningObj = msCourseNode.getLearningObjectives();
			if (learningObj != null) {
				Component learningObjectives = ObjectivesHelper.createLearningObjectivesComponent(learningObj, ureq); 
				myContent.put("learningObjectives", learningObjectives);
				myContent.contextPut("hasObjectives", learningObj); // dummy value, just an exists operator					
			}
		} 

		// Push variables to velcity page
		exposeConfigToVC(config);		
		exposeUserDataToVC(userCourseEnv, msCourseNode);
		
		setInitialComponent(myContent);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	private void exposeConfigToVC(ModuleConfiguration config) {
	    myContent.contextPut(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD, config.get(MSCourseNode.CONFIG_KEY_HAS_SCORE_FIELD));
	    myContent.contextPut(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD, config.get(MSCourseNode.CONFIG_KEY_HAS_PASSED_FIELD));
	    myContent.contextPut(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD, config.get(MSCourseNode.CONFIG_KEY_HAS_COMMENT_FIELD));
	    String infoTextUser = (String) config.get(MSCourseNode.CONFIG_KEY_INFOTEXT_USER);
	    myContent.contextPut(MSCourseNode.CONFIG_KEY_INFOTEXT_USER, (infoTextUser == null ? "" : infoTextUser));
	    myContent.contextPut(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE, config.get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE));
	    myContent.contextPut(MSCourseNode.CONFIG_KEY_SCORE_MIN, config.get(MSCourseNode.CONFIG_KEY_SCORE_MIN));
	    myContent.contextPut(MSCourseNode.CONFIG_KEY_SCORE_MAX, config.get(MSCourseNode.CONFIG_KEY_SCORE_MAX));
	}
	
	private void exposeUserDataToVC(UserCourseEnvironment userCourseEnv, AssessableCourseNode courseNode) {
		ScoreEvaluation scoreEval = courseNode.getUserScoreEvaluation(userCourseEnv);
		myContent.contextPut("score", AssessmentHelper.getRoundedScore(scoreEval.getScore()));
		myContent.contextPut("hasPassedValue", (scoreEval.getPassed() == null ? Boolean.FALSE : Boolean.TRUE));
		myContent.contextPut("passed", scoreEval.getPassed());
		myContent.contextPut("comment", courseNode.getUserUserComment(userCourseEnv));
		UserNodeAuditManager am = userCourseEnv.getCourseEnvironment().getAuditManager();
		myContent.contextPut("log", am.getUserNodeLog(courseNode, userCourseEnv.getIdentityEnvironment().getIdentity()));
	}
	
	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// do nothing here yet
	}
}
