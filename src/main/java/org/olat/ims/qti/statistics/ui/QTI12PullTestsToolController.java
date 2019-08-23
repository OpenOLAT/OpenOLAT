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
import java.util.Collections;
import java.util.List;

import org.dom4j.Document;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.i18n.I18nModule;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.archiver.ScoreAccountingHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroupService;
import org.olat.ims.qti.QTIResultManager;
import org.olat.ims.qti.container.AssessmentContext;
import org.olat.ims.qti.process.AssessmentFactory;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti.process.FilePersister;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentToolOptions;
import org.olat.modules.assessment.Role;
import org.olat.modules.iq.IQManager;
import org.olat.modules.iq.IQRetrievedEvent;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI12PullTestsToolController extends FormBasicController {
	

	private final IQTESTCourseNode courseNode;
	private final CourseEnvironment courseEnv;
	private final AssessmentToolOptions asOptions;
	
	@Autowired
	private IQManager iqm;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public QTI12PullTestsToolController(UserRequest ureq, WindowControl wControl, CourseEnvironment courseEnv,
			AssessmentToolOptions asOptions, IQTESTCourseNode courseNode) {
		super(ureq, wControl, "retrieve_tests");
		setTranslator(Util.createPackageTranslator(QTIResultManager.class, getLocale(), getTranslator()));
		this.courseEnv = courseEnv;
		this.courseNode = courseNode;
		this.asOptions = asOptions;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		int count = 0;
		StringBuilder fullnames = new StringBuilder(256);
		List<Identity> assessedIdentities = getIdentities();
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
		
		String msg;
		if(count == 0) {
			msg = translate("retrievetest.nothing.todo");
		} else if(count == 1) {
			msg = translate("retrievetest.confirm.text", new String[]{ fullnames.toString() });
		} else  {
			msg = translate("retrievetest.confirm.text.plural", new String[]{ fullnames.toString() });
		}
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("msg", msg);
		}
		
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("menu.pull.tests.title", formLayout);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doRetrieveTests(getIdentities());
		fireEvent(ureq, Event.DONE_EVENT);
	}

	private List<Identity> getIdentities() {
		List<Identity> identities;
		if(asOptions.getGroup() == null && asOptions.getIdentities() == null) {
			if(courseEnv != null) {
				identities = ScoreAccountingHelper.loadUsers(courseEnv);
			} else {
				identities = Collections.emptyList();
			}
		} else if (asOptions.getIdentities() != null) {
			identities = asOptions.getIdentities();
		} else {
			identities = businessGroupService.getMembers(asOptions.getGroup());
		}
		return identities;
	}
	
	private void doRetrieveTests(List<Identity> assessedIdentities) {
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
		courseAssessmentService.updateScoreEvaluation(courseNode, sceval, userCourseEnv, assessedIdentity, true,
				Role.coach);
		
		//cleanup
		ai.cleanUp();
	}
}