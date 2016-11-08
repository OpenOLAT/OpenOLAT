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
package org.olat.ims.qti21.ui.assessment;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.modules.assessment.AssessmentToolOptions;
import org.olat.modules.assessment.ui.event.CompleteAssessmentTestSessionEvent;

/**
 * 
 * Initial date: 08.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21CorrectionToolController extends BasicController {
	
	private final Link correctionButton;

	private CloseableModalController cmc;
	private IdentitiesAssessmentTestCorrectionController correctionCtrl;
	
	private final IQTESTCourseNode courseNode;
	private final CourseEnvironment courseEnv;
	private final AssessmentToolOptions asOptions;

	public QTI21CorrectionToolController(UserRequest ureq, WindowControl wControl, 
			CourseEnvironment courseEnv, AssessmentToolOptions asOptions, IQTESTCourseNode courseNode) {
		super(ureq, wControl);
		this.courseNode = courseNode;
		this.courseEnv = courseEnv;
		this.asOptions = asOptions;
		
		correctionButton = LinkFactory.createButton("correction.test.title", null, this);
		correctionButton.setIconLeftCSS("o_icon o_icon-fw o_icon_correction");
		correctionButton.setTranslator(getTranslator());
		putInitialPanel(correctionButton);
		getInitialComponent().setSpanAsDomReplaceable(true); // override to wrap panel as span to not break link layout 
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(correctionButton == source) {
			doStartCorrection(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(correctionCtrl == source) {
			if(event instanceof CompleteAssessmentTestSessionEvent) {
				CompleteAssessmentTestSessionEvent catse = (CompleteAssessmentTestSessionEvent)event;
				List<AssessmentTestSession> testSessionsToComplete = catse.getTestSessions();
				doUpdateCourseNode(correctionCtrl.getTestCorrections(), testSessionsToComplete);
			}
			cmc.deactivate();
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(correctionCtrl);
		removeAsListenerAndDispose(cmc);
		correctionCtrl = null;
		cmc = null;
	}
	
	public void doStartCorrection(UserRequest ureq) {
		correctionCtrl = new IdentitiesAssessmentTestCorrectionController(ureq, getWindowControl(), courseEnv, asOptions, courseNode);
		listenTo(correctionCtrl);
		cmc = new CloseableModalController(getWindowControl(), "close", correctionCtrl.getInitialComponent(),
				true, translate("correction"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doUpdateCourseNode(AssessmentTestCorrection corrections, List<AssessmentTestSession> testSessionsToComplete) {
		Set<AssessmentTestSession> selectedSessions = new HashSet<>(testSessionsToComplete);
		for(AssessmentTestSession testSession:corrections.getTestSessions()) {
			if(selectedSessions.contains(testSession)) {
				UserCourseEnvironment assessedUserCourseEnv = AssessmentHelper
						.createAndInitUserCourseEnvironment(testSession.getIdentity(), courseEnv);
				ScoreEvaluation scoreEval = courseNode.getUserScoreEvaluation(assessedUserCourseEnv);
				
				BigDecimal finalScore = testSession.getScore();
				if(finalScore == null) {
					finalScore = testSession.getManualScore();
				} else if(testSession.getManualScore() != null) {
					finalScore = finalScore.add(testSession.getManualScore());
				}
				
				Float score = finalScore == null ? null : finalScore.floatValue();
				ScoreEvaluation manualScoreEval = new ScoreEvaluation(score, scoreEval.getPassed(),
						scoreEval.getAssessmentStatus(), scoreEval.getFullyAssessed(), testSession.getKey());
				courseNode.updateUserScoreEvaluation(manualScoreEval, assessedUserCourseEnv, getIdentity(), false);
			}
		}
	}
}
