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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.commons.services.taskexecutor.Task;
import org.olat.core.commons.services.taskexecutor.TaskExecutorManager;
import org.olat.core.commons.services.taskexecutor.ui.TaskStatusRenderer;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.Structure;
import org.olat.course.assessment.bulk.TaskDataModel.Cols;
import org.olat.course.assessment.manager.BulkAssessmentTask;
import org.olat.course.assessment.model.BulkAssessmentDatas;
import org.olat.course.assessment.model.BulkAssessmentFeedback;
import org.olat.course.nodes.CourseNode;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18.11.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BulkAssessmentOverviewController extends FormBasicController {
	
	private FormLink newButton;
	private TaskDataModel taskModel;
	private FlexiTableElement taskListEl;
	private DialogBoxController confirmDeleteCtrl;
	private StepsMainRunController bulkAssessmentCtrl;
	private DialogBoxController errorCtrl;
	
	private final RepositoryEntry courseEntry;
	private final boolean canEditUserVisibility;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private TaskExecutorManager taskManager;
	
	private Task editedTask;
	
	public BulkAssessmentOverviewController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry, boolean canEditUserVisibility) {
		super(ureq, wControl, "overview");
		this.courseEntry = courseEntry;
		this.canEditUserVisibility = canEditUserVisibility;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		newButton = uifactory.addFormLink("new.bulk", formLayout, Link.BUTTON);
		newButton.setElementCssClass("o_sel_assessment_tool_new_bulk_assessment");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.taskStatus", Cols.taskStatus.ordinal(),
				new TaskStatusRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.scheduledDate", Cols.scheduledDate.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.node", Cols.courseNode.ordinal(),
				new CourseNodeRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.score", Cols.score.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.passed", Cols.status.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.comment", Cols.comment.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.returnFiles", Cols.returnFile.ordinal()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.numOfAssessedUsers", Cols.numOfAssessedUsers.ordinal()));
		
		FlexiCellRenderer renderer = new StaticFlexiCellRenderer("select-owner", new TextFlexiCellRenderer());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.owner", Cols.owner.ordinal(), "select-owner", renderer));
		
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("edit", translate("edit"), "edit"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete"));
		
		taskModel = new TaskDataModel(Collections.<TaskData>emptyList(), columnsModel);
		taskListEl = uifactory.addTableElement(getWindowControl(), "taskList", taskModel, getTranslator(), formLayout);
		reloadTaskModel();
	}
	
	private void reloadTaskModel() {
		List<Task> tasks = taskManager.getTasks(courseEntry.getOlatResource());
		List<TaskData> taskDatas = new ArrayList<>(tasks.size());
		ICourse course = CourseFactory.loadCourse(courseEntry);
		Structure structure = course.getRunStructure();
		
		for(Task task:tasks) {
			String fullName = null;
			if(task.getCreator() != null) {
				fullName = userManager.getUserDisplayName(task.getCreator());
			}
			BulkAssessmentTask runnable = taskManager.getPersistedRunnableTask(task, BulkAssessmentTask.class);
			CourseNode courseNode = structure.getNode(runnable.getCourseNodeIdent());
			taskDatas.add(new TaskData(task, runnable, courseNode, fullName));
		}
		taskModel.setObjects(taskDatas);
		taskListEl.reset();
		flc.contextPut("hasScheduledTasks", Boolean.valueOf(taskDatas.size()>0));
	}
	
	@Override
	protected void doDispose() {
		if(editedTask != null) {//only for security purpose
			taskManager.returnTaskAfterEdition(editedTask, null);
		}
        super.doDispose();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(newButton == source) {
			doNewBulkAssessment(ureq);
		} else if(taskListEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				switch(se.getCommand()) {
					case "edit": {
						TaskData data = taskModel.getObject(se.getIndex());
						doEditBulkAssessment(ureq, data);
						break;
					}
					case "delete": {
						TaskData data = taskModel.getObject(se.getIndex());
						doConfirmDelete(ureq, data);
						break;
					}
					case "select-owner": {
						TaskData data = taskModel.getObject(se.getIndex());
						Identity creator = data.getTask().getCreator();
						doOpenCard(ureq, creator);
						break;
					}
				}
			}
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
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
					reloadTaskModel();
					doBulkAssessmentSynchronous(ureq, feedback);
				}
			}
		} else if (confirmDeleteCtrl == source) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				TaskData data = (TaskData)confirmDeleteCtrl.getUserObject();
				doDelete(data);
				reloadTaskModel();
			}
		}
		super.event(ureq, source, event);
	}
	
	private void doOpenCard(UserRequest ureq, Identity creator) {
		String businessPath = "[Identity:" + creator.getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doConfirmDelete(UserRequest ureq, TaskData data) {
		String title = translate("delete.task");
		String text = translate("delete.task.confirm", new String[]{ data.toString() });
		confirmDeleteCtrl = activateOkCancelDialog(ureq, title, text, confirmDeleteCtrl);
		confirmDeleteCtrl.setUserObject(data);
	}
	
	private void doDelete(TaskData data) {
		taskManager.delete(data.getTask());
		showInfo("task.deleted");
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

	private void doNewBulkAssessment(UserRequest ureq) {
		removeAsListenerAndDispose(bulkAssessmentCtrl);
		
		List<CourseNode> nodes = new ArrayList<>();
		ICourse course = CourseFactory.loadCourse(courseEntry);
		collectBulkCourseNode(course.getRunStructure().getRootNode(), nodes);

		Step start;
		if(nodes.size() > 1) {
			start = new BulkAssessment_1_SelectCourseNodeStep(ureq, courseEntry, canEditUserVisibility);
		} else if(nodes.size() == 1){
			start = new BulkAssessment_2_DatasStep(ureq, courseEntry, nodes.get(0), canEditUserVisibility);
		} else {
			showWarning("bulk.action.no.coursenodes");
			return;
		}

		StepRunnerCallback finish = new StepRunnerCallback() {
			@Override
			public Step execute(UserRequest uureq, WindowControl wControl, StepsRunContext runContext) {
				Date scheduledDate = (Date)runContext.get("scheduledDate");
				CourseNode courseNode = (CourseNode)runContext.get("courseNode");
				BulkAssessmentDatas datas = (BulkAssessmentDatas)runContext.get("datas");
				Feedback feedback = doBulkAssessment(courseNode, scheduledDate, datas);
				runContext.put("feedback", feedback);
				return StepsMainRunController.DONE_MODIFIED;
			}
		};
		
		bulkAssessmentCtrl = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("bulk.wizard.title"), "o_sel_bulk_assessment_wizard");
		listenTo(bulkAssessmentCtrl);
		getWindowControl().pushAsModalDialog(bulkAssessmentCtrl.getInitialComponent());
	}
	
	private void doEditBulkAssessment(UserRequest ureq, TaskData data) {
		removeAsListenerAndDispose(bulkAssessmentCtrl);
		if(editedTask != null) {//only for security purpose
			taskManager.returnTaskAfterEdition(editedTask, null);
		}

		CourseNode courseNode = data.getCourseNode();
		final Task editableTask = taskManager.pickTaskForEdition(data.getTask());
		editedTask = editableTask;
		if(editableTask == null) {
			showWarning("task.edited");
		} else {
			BulkAssessmentTask runnable = taskManager.getPersistedRunnableTask(editableTask, BulkAssessmentTask.class);
			BulkAssessmentDatas datas = runnable.getDatas();
			
			Step start = new BulkAssessment_2_DatasStep(ureq, courseEntry, courseNode, datas, editableTask, canEditUserVisibility);
			StepRunnerCallback finish = new StepRunnerCallback() {
				@Override
				public Step execute(UserRequest uureq, WindowControl wControl, StepsRunContext runContext) {
					Task task = (Task)runContext.get("task");
					Date scheduledDate = (Date)runContext.get("scheduledDate");
					CourseNode assessableCourseNode = (CourseNode)runContext.get("courseNode");
					BulkAssessmentDatas bulkDatas = (BulkAssessmentDatas)runContext.get("datas");
					Feedback feedback = doUpdateBulkAssessment(task, assessableCourseNode, scheduledDate, bulkDatas);
					runContext.put("feedback", feedback);
					editedTask = null;
					return StepsMainRunController.DONE_MODIFIED;
				}
			};
			
			StepRunnerCallback cancel = new StepRunnerCallback() {
				@Override
				public Step execute(UserRequest uureq, WindowControl wControl, StepsRunContext runContext) {
					taskManager.returnTaskAfterEdition(editableTask, null);
					editedTask = null;
					return Step.NOSTEP;
				}
			};
			
			bulkAssessmentCtrl = new StepsMainRunController(ureq, getWindowControl(), start, finish, cancel,
					translate("bulk.wizard.title"), "o_sel_bulk_assessment_wizard");
			listenTo(bulkAssessmentCtrl);
			getWindowControl().pushAsModalDialog(bulkAssessmentCtrl.getInitialComponent());
		}
	}
	
	private void collectBulkCourseNode(CourseNode courseNode, List<CourseNode> nodes) {
		for (int i=courseNode.getChildCount(); i-->0; ) {
			collectBulkCourseNode((CourseNode)courseNode.getChildAt(i), nodes);
		}
		
		if(BulkAssessmentTask.isBulkAssessable(courseEntry, courseNode)) {
			nodes.add(courseNode);
		}
	}
	
	private Feedback doUpdateBulkAssessment(Task task, CourseNode node, Date scheduledDate, BulkAssessmentDatas datas) {
		BulkAssessmentTask runnable = new BulkAssessmentTask(courseEntry.getOlatResource(), node, datas, getIdentity().getKey(), getLocale());
		Feedback feedback;
		if(scheduledDate == null) {
			List<BulkAssessmentFeedback> feedbacks = runnable.process();
			feedback = new Feedback(true, feedbacks);
			if(task != null) {
				taskManager.delete(task);
			}
		} else {
			taskManager.updateAndReturn(task, runnable, getIdentity(), scheduledDate);
			feedback = new Feedback(false, null);
		}
		return feedback;
	}
	
	private Feedback doBulkAssessment(CourseNode node, Date scheduledDate, BulkAssessmentDatas datas) {
		BulkAssessmentTask task = new BulkAssessmentTask(courseEntry.getOlatResource(), node, datas, getIdentity().getKey(), getLocale());
		Feedback feedback;
		if(scheduledDate == null) {
			List<BulkAssessmentFeedback> feedbacks = task.process();
			feedback = new Feedback(true, feedbacks);
		} else {
			taskManager.execute(task, getIdentity(), courseEntry.getOlatResource(), node.getIdent(), scheduledDate);
			feedback = new Feedback(false, null);
		}
		return feedback;
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
