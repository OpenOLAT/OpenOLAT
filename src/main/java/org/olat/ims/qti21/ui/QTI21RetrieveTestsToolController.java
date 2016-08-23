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
package org.olat.ims.qti21.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
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
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.nodes.AssessmentToolOptions;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.ims.qti.QTIResultManager;
import org.olat.ims.qti21.AssessmentSessionAuditLogger;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.ui.event.RetrieveAssessmentTestSessionEvent;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 07.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21RetrieveTestsToolController extends BasicController implements Activateable2 {
	
	private final Link pullButton;
	private DialogBoxController retrieveConfirmationCtr;
	
	private final IQTESTCourseNode courseNode;
	private final CourseEnvironment courseEnv;
	private final List<Identity> assessedIdentities;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private QTI21Service qtiService;
	
	public QTI21RetrieveTestsToolController(UserRequest ureq, WindowControl wControl, CourseEnvironment courseEnv,
			AssessmentToolOptions asOptions, IQTESTCourseNode courseNode) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(QTIResultManager.class, getLocale(), getTranslator()));
		
		this.courseEnv = courseEnv;
		this.courseNode = courseNode;
		this.assessedIdentities = asOptions.getIdentities();
		
		pullButton = LinkFactory.createButton("menu.retrieve.tests.title", null, this);
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
				@SuppressWarnings("unchecked")
				List<AssessmentTestSession> sessionsToRetrieve = (List<AssessmentTestSession>)retrieveConfirmationCtr.getUserObject();
				doRetrieveTests(sessionsToRetrieve);
			}
			removeAsListenerAndDispose(retrieveConfirmationCtr);
			retrieveConfirmationCtr = null;
		}
	}
	
	private void confirmPull(UserRequest ureq) {
		StringBuilder fullnames = new StringBuilder(256);
		
		List<AssessmentTestSession> sessions = qtiService
				.getRunningAssessmentTestSession(courseEnv.getCourseGroupManager().getCourseEntry(), courseNode.getIdent(), courseNode.getReferencedRepositoryEntry());
		
		List<AssessmentTestSession> sessionsToRetrieve = new ArrayList<>();
		for(AssessmentTestSession session:sessions) {
			if(assessedIdentities.contains(session.getIdentity())) {
				if(fullnames.length() > 0) fullnames.append(", ");
				String name = userManager.getUserDisplayName(session.getIdentity());
				if(StringHelper.containsNonWhitespace(name)) {
					fullnames.append(name);
					sessionsToRetrieve.add(session);
				}
			}
		}
		
		if(sessionsToRetrieve.size() == 0) {
			showInfo("retrievetest.nothing.todo");
		} else if(sessionsToRetrieve.size() == 1) {
			String title = translate("retrievetest.confirm.title");
			String text = translate("retrievetest.confirm.text", new String[]{ fullnames.toString() });
			retrieveConfirmationCtr = activateYesNoDialog(ureq, title, text, retrieveConfirmationCtr);
			retrieveConfirmationCtr.setUserObject(sessionsToRetrieve);
		} else  {
			String title = translate("retrievetest.confirm.title");
			String text = translate("retrievetest.confirm.text.plural", new String[]{ fullnames.toString() });
			retrieveConfirmationCtr = activateYesNoDialog(ureq, title, text, retrieveConfirmationCtr);
			retrieveConfirmationCtr.setUserObject(sessionsToRetrieve);
		}
	}
	
	private void doRetrieveTests(List<AssessmentTestSession> sessionsToRetrieve) {
		for(AssessmentTestSession sessionToRetrieve:sessionsToRetrieve) {
			doRetrieveTest(sessionToRetrieve);
		}
	}

	private void doRetrieveTest(AssessmentTestSession session) {
		if(session.getFinishTime() == null) {
			session.setFinishTime(new Date());
		}
		session.setTerminationTime(new Date());
		session = qtiService.updateAssessmentTestSession(session);
		dbInstance.commit();//make sure that the changes committed before sending the event
		
		AssessmentSessionAuditLogger candidateAuditLogger = qtiService.getAssessmentSessionAuditLogger(session, false);
		candidateAuditLogger.logTestRetrieved(session, getIdentity());
		
		OLATResourceable sessionOres = OresHelper.createOLATResourceableInstance(AssessmentTestSession.class, session.getKey());
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.fireEventToListenersOf(new RetrieveAssessmentTestSessionEvent(session.getKey()), sessionOres);
	}
}