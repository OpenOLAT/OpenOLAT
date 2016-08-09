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
package org.olat.ims.qti.statistics.ui;

import java.io.File;
import java.util.List;

import org.dom4j.Document;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.nodes.AssessmentToolOptions;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.qti.QTIResultManager;
import org.olat.ims.qti.container.AssessmentContext;
import org.olat.ims.qti.process.AssessmentFactory;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti.process.FilePersister;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.iq.IQManager;
import org.olat.modules.iq.IQRetrievedEvent;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 07.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI12PullTestsToolController extends BasicController implements Activateable2 {
	
	private final Link pullButton;
	private DialogBoxController retrieveConfirmationCtr;
	
	private final IQTESTCourseNode courseNode;
	private final CourseEnvironment courseEnv;
	private final List<Identity> assessedIdentities;
	
	@Autowired
	private IQManager iqm;
	@Autowired
	private UserManager userManager;
	
	public QTI12PullTestsToolController(UserRequest ureq, WindowControl wControl, CourseEnvironment courseEnv,
			AssessmentToolOptions asOptions, IQTESTCourseNode courseNode) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(QTIResultManager.class, getLocale(), getTranslator()));
		
		this.courseEnv = courseEnv;
		this.courseNode = courseNode;
		this.assessedIdentities = asOptions.getIdentities();
		
		pullButton = LinkFactory.createButton("menu.pull.tests.title", null, this);
		pullButton.setTranslator(getTranslator());
		putInitialPanel(pullButton);
		getInitialComponent().setSpanAsDomReplaceable(true); // override to wrap panel as span to not break link layout 
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(pullButton == source) {
			confirmPull(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(retrieveConfirmationCtr == source) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				doRetrieveTests();
			}
			removeAsListenerAndDispose(retrieveConfirmationCtr);
			retrieveConfirmationCtr = null;
		}
	}
	
	private void confirmPull(UserRequest ureq) {
		int count = 0;
		StringBuilder fullnames = new StringBuilder(256);
		for(Identity assessedIdentity:assessedIdentities) {
			if(courseNode.isQTI12TestRunning(assessedIdentity, courseEnv)) {
				if(fullnames.length() > 0) fullnames.append(", ");
				String name = userManager.getUserDisplayName(assessedIdentity);
				if(StringHelper.containsNonWhitespace(name)) {
					fullnames.append(name);
					count++;
				}
			}
		}
		
		if(count == 0) {
			showInfo("retrievetest.nothing.todo");
		} else if(count == 1) {
			String title = translate("retrievetest.confirm.title");
			String text = translate("retrievetest.confirm.text", new String[]{ fullnames.toString() });
			retrieveConfirmationCtr = activateYesNoDialog(ureq, title, text, retrieveConfirmationCtr);
		} else  {
			String title = translate("retrievetest.confirm.title");
			String text = translate("retrievetest.confirm.text.plural", new String[]{ fullnames.toString() });
			retrieveConfirmationCtr = activateYesNoDialog(ureq, title, text, retrieveConfirmationCtr);
		}
	}
	
	private void doRetrieveTests() {
		ICourse course = CourseFactory.loadCourse(courseEnv.getCourseResourceableId());
		for(Identity assessedIdentity:assessedIdentities) {
			if(courseNode.isQTI12TestRunning(assessedIdentity, courseEnv)) {
				IQRetrievedEvent retrieveEvent = new IQRetrievedEvent(assessedIdentity, courseEnv.getCourseResourceableId(), courseNode.getIdent());
				CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(retrieveEvent, retrieveEvent);
				retrieveTest(assessedIdentity, course);
			}
		}
	}

	private void retrieveTest(Identity assessedIdentity, ICourse course) {
		ModuleConfiguration modConfig = courseNode.getModuleConfiguration();

		String resourcePathInfo = courseEnv.getCourseResourceableId() + File.separator + courseNode.getIdent();
		AssessmentInstance ai = AssessmentFactory.createAssessmentInstance(assessedIdentity, "", modConfig, false, courseEnv.getCourseResourceableId(), courseNode.getIdent(), resourcePathInfo, null);
		//close the test
		ai.stop();
		//persist the results
		iqm.persistResults(ai);

		//reporting
		Document docResReporting = iqm.getResultsReporting(ai, assessedIdentity, I18nModule.getDefaultLocale());
		FilePersister.createResultsReporting(docResReporting, assessedIdentity, ai.getFormattedType(), ai.getAssessID());
		
		//olat results
		AssessmentContext ac = ai.getAssessmentContext();
		Float score = new Float(ac.getScore());
		Boolean passed = new Boolean(ac.isPassed());
		ScoreEvaluation sceval = new ScoreEvaluation(score, passed, Boolean.FALSE, new Long(ai.getAssessID()));
		UserCourseEnvironment userCourseEnv = AssessmentHelper.createAndInitUserCourseEnvironment(assessedIdentity, course);
		courseNode.updateUserScoreEvaluation(sceval, userCourseEnv, assessedIdentity, true);
		
		//cleanup
		ai.cleanUp();
	}
}