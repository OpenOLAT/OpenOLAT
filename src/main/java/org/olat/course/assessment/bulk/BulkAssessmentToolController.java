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
package org.olat.course.assessment.bulk;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.course.assessment.manager.BulkAssessmentTask;
import org.olat.course.assessment.model.BulkAssessmentDatas;
import org.olat.course.assessment.model.BulkAssessmentFeedback;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.resource.OLATResource;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BulkAssessmentToolController extends BasicController {
	
	private final Link startButton;
	private final CourseNode courseNode;

	private DialogBoxController errorCtrl;
	private StepsMainRunController bulkAssessmentCtrl;
	private final TaskExecutorManager taskManager;
	private final OLATResource courseOres;
	
	public BulkAssessmentToolController(UserRequest ureq, WindowControl wControl, CourseEnvironment courseEnv, CourseNode courseNode) {
		super(ureq, wControl);
		this.courseNode = courseNode;
		taskManager = CoreSpringFactory.getImpl(TaskExecutorManager.class);
		courseOres = courseEnv.getCourseGroupManager().getCourseResource();
		
		startButton = LinkFactory.createButton("new.bulk", null, this);
		startButton.setTranslator(getTranslator());
		putInitialPanel(startButton);
		getInitialComponent().setSpanAsDomReplaceable(true); // override to wrap panel as span to not break link layout 
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(startButton == source) {
			doOpen(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(bulkAssessmentCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				Feedback feedback = (Feedback)bulkAssessmentCtrl.getRunContext().get("feedback");
				removeAsListenerAndDispose(bulkAssessmentCtrl);
				bulkAssessmentCtrl = null;
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					doBulkAssessmentSynchronous(ureq, feedback);
					fireEvent(ureq, Event.CHANGED_EVENT);
				} else {
					fireEvent(ureq, Event.DONE_EVENT);
				}
			}
		}
		super.event(ureq, source, event);
	}

	private void doOpen(UserRequest ureq) {
		StepRunnerCallback finish = (uureq, bwControl, runContext) -> {
			Date scheduledDate = (Date)runContext.get("scheduledDate");
			BulkAssessmentDatas datas = (BulkAssessmentDatas)runContext.get("datas");
			Feedback feedback = doBulkAssessment(scheduledDate, datas);
			runContext.put("feedback", feedback);
			return StepsMainRunController.DONE_MODIFIED;
		};
		
		Step start = new BulkAssessment_2_DatasStep(ureq, courseNode);
		bulkAssessmentCtrl = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("bulk.wizard.title"), "o_sel_bulk_assessment_wizard");
		listenTo(bulkAssessmentCtrl);
		getWindowControl().pushAsModalDialog(bulkAssessmentCtrl.getInitialComponent());
	}
	
	private Feedback doBulkAssessment(Date scheduledDate, BulkAssessmentDatas datas) {
		BulkAssessmentTask task = new BulkAssessmentTask(courseOres, courseNode, datas, getIdentity().getKey());
		Feedback feedback;
		if(scheduledDate == null) {
			List<BulkAssessmentFeedback> feedbacks = task.process();
			feedback = new Feedback(true, feedbacks);
		} else {
			taskManager.execute(task, getIdentity(), courseOres, courseNode.getIdent(), scheduledDate);
			feedback = new Feedback(false, null);
		}
		return feedback;
	}
	
	private void doBulkAssessmentSynchronous(UserRequest ureq, Feedback feedback) {
		if(feedback.isSynchronous()) {
			List<BulkAssessmentFeedback> feedbacks = feedback.getFeedbacks();
			if(feedbacks.isEmpty()) {
				showInfo("bulk.assessment.done");
			} else {
				String text = BulkAssessmentTask.renderFeedback(feedbacks, getTranslator());
				List<String> buttonLabels = Collections.singletonList(translate("ok"));
				String title = translate("bulk.assessment.error.title");
				String translatedText = translate("bulk.assessment.error.feedback", new String[]{ text });
				errorCtrl = activateGenericDialog(ureq, title, translatedText, buttonLabels, errorCtrl);
			}
		} else {
			showInfo("bulk.assessment.enlisted");
		}
	}
	
	private static class Feedback {
		private final boolean synchronous;
		private final List<BulkAssessmentFeedback> feedbacks;
		
		public Feedback(boolean synchronous, List<BulkAssessmentFeedback> feedbacks) {
			this.synchronous = synchronous;
			this.feedbacks = feedbacks;
		}

		public boolean isSynchronous() {
			return synchronous;
		}

		public List<BulkAssessmentFeedback> getFeedbacks() {
			return feedbacks;
		}
	}
}
