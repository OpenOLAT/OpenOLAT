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
package org.olat.course.nodes.iq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeProvider;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.iq.QTI21IdentityListCourseNodeToolsController.AssessmentTestSessionDetailsComparator;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.jpa.AssessmentTestSessionStatistics;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Button for to extend time in a test.
 * 
 * Initial date: 4 déc. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21ExtraTimeController extends BasicController {
	
	private final Link extraTimeLink;
	
	private final IQTESTCourseNode courseNode;
	private final CourseEnvironment courseEnv;
	private final IdentityListCourseNodeProvider provider;
	
	private CloseableModalController cmc;
	private ConfirmExtraTimeController extraTimeCtrl; 
	
	@Autowired
	private QTI21Service qtiService;
	
	public QTI21ExtraTimeController(UserRequest ureq, WindowControl wControl, CourseEnvironment courseEnv,
			IQTESTCourseNode courseNode, IdentityListCourseNodeProvider provider) {
		super(ureq, wControl);
		this.provider = provider;
		this.courseEnv = courseEnv;
		this.courseNode = courseNode;
		
		extraTimeLink = LinkFactory.createButton("extra.time", null, this);
		extraTimeLink.setIconLeftCSS("o_icon o_icon_extra_time");
		extraTimeLink.setTranslator(getTranslator());
		putInitialPanel(extraTimeLink);
		getInitialComponent().setSpanAsDomReplaceable(true); // override to wrap panel as span to not break link layout 
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(extraTimeLink == source) {
			doConfirmExtraTime(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(extraTimeCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(extraTimeCtrl);
		removeAsListenerAndDispose(cmc);
		extraTimeCtrl = null;
		cmc = null;
	}

	private void doConfirmExtraTime(UserRequest ureq) {
		List<IdentityRef> identities = provider.getSelectedIdentities();
		if(identities == null || identities.isEmpty()) {
			showWarning("warning.users.extra.time");
			return;
		}
		
		RepositoryEntry courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		List<AssessmentTestSession> testSessions = new ArrayList<>(identities.size());
		for(IdentityRef identity:identities) {
			List<AssessmentTestSessionStatistics> sessionsStatistics = qtiService.getAssessmentTestSessionsStatistics(courseEntry, courseNode.getIdent(), identity);
			if(!sessionsStatistics.isEmpty()) {
				if(sessionsStatistics.size() > 1) {
					Collections.sort(sessionsStatistics, new AssessmentTestSessionDetailsComparator());
				}
				AssessmentTestSession lastSession = sessionsStatistics.get(0).getTestSession();
				if(lastSession != null && lastSession.getFinishTime() == null) {
					testSessions.add(lastSession);
				}
			}
		}
		
		if(testSessions == null || testSessions.isEmpty()) {
			showWarning("warning.users.extra.time");
			return;
		}
		
		extraTimeCtrl = new ConfirmExtraTimeController(ureq, getWindowControl(), testSessions);
		listenTo(extraTimeCtrl);

		String title = translate("extra.time");
		cmc = new CloseableModalController(getWindowControl(), null, extraTimeCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
}
