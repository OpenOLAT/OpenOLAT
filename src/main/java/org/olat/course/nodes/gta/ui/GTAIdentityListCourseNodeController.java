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
package org.olat.course.nodes.gta.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.ICourse;
import org.olat.course.archiver.ArchiveResource;
import org.olat.course.assessment.bulk.BulkAssessmentToolController;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeController;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeTableModel.IdentityCourseElementCols;
import org.olat.course.nodes.ArchiveOptions;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.GTAManager;
import org.olat.course.nodes.gta.GTAType;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskList;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.assessment.AssessmentToolOptions;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.assessment.ui.AssessedIdentityElementRow;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This specialized view of the list assessed identities show the chosen task
 * and some bulk actions if they are configured in the course element.
 * 
 * Initial date: 18 déc. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTAIdentityListCourseNodeController extends IdentityListCourseNodeController {
	
	private FormLink downloadButton;
	private FormLink groupAssessmentButton;
	
	private GroupAssessmentController assessmentCtrl;
	private BulkAssessmentToolController bulkAssessmentToolCtrl;
	
	@Autowired
	private GTAManager gtaManager;

	public GTAIdentityListCourseNodeController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, BusinessGroup group, CourseNode courseNode, UserCourseEnvironment coachCourseEnv,
			AssessmentToolContainer toolContainer, AssessmentToolSecurityCallback assessmentCallback, boolean showTitle) {
		super(ureq, wControl, stackPanel, courseEntry, group, courseNode, coachCourseEnv, toolContainer, assessmentCallback, showTitle);
	}

	@Override
	protected String getTableId() {
		return "gta-assessment-tool-identity-list-v2";
	}

	@Override
	protected void initStatusColumns(FlexiTableColumnModel columnsModel) {
		ModuleConfiguration config =  courseNode.getModuleConfiguration();
		if(GTAType.individual.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE)) && config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.details.gta", IdentityCourseElementCols.details.ordinal()));
		}
		super.initStatusColumns(columnsModel);
	}

	@Override
	protected void initMultiSelectionTools(UserRequest ureq, FormLayoutContainer formLayout) {
		ModuleConfiguration config =  courseNode.getModuleConfiguration();
		if(GTAType.group.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))
			&& (config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT)
				|| config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)
				|| config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD))) {

			if(!coachCourseEnv.isCourseReadOnly() && group != null) {
				groupAssessmentButton = uifactory.addFormLink("assessment.group.tool", formLayout, Link.BUTTON);
			}

			initBulkDownloadController(formLayout);
		} else if(GTAType.individual.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE))) {
			if(!coachCourseEnv.isCourseReadOnly()
					&& (config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION) || config.getBooleanSafe(GTACourseNode.GTASK_GRADING))){
				initBulkAsssessmentTool(ureq, formLayout);
			}
			
			if(config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)
					|| config.getBooleanSafe(GTACourseNode.GTASK_SUBMIT)
					|| config.getBooleanSafe(GTACourseNode.GTASK_REVIEW_AND_CORRECTION)
					|| config.getBooleanSafe(GTACourseNode.GTASK_REVISION_PERIOD)) {
				initBulkDownloadController(formLayout);
			}
		}
		super.initMultiSelectionTools(ureq, formLayout);
	}
	
	private void initBulkDownloadController(FormLayoutContainer formLayout) {
		downloadButton = uifactory.addFormLink("bulk.download.title", formLayout, Link.BUTTON);
	}
	
	private void initBulkAsssessmentTool(UserRequest ureq, FormLayoutContainer formLayout) {
		removeAsListenerAndDispose(bulkAssessmentToolCtrl);
		
		bulkAssessmentToolCtrl = new BulkAssessmentToolController(ureq, getWindowControl(), getCourseEnvironment(),
				courseNode);
		listenTo(bulkAssessmentToolCtrl);
		formLayout.put("bulk.assessment", bulkAssessmentToolCtrl.getInitialComponent());	
	}

	@Override
	public void reload(UserRequest ureq) {
		super.reload(ureq);

		ModuleConfiguration config =  courseNode.getModuleConfiguration();
		if(GTAType.individual.name().equals(config.getStringValue(GTACourseNode.GTASK_TYPE)) && config.getBooleanSafe(GTACourseNode.GTASK_ASSIGNMENT)) {
			TaskList taskList = gtaManager.getTaskList(getCourseRepositoryEntry(), (GTACourseNode)courseNode);
			if(taskList != null) {
				loadTasksInModel(taskList);
			}
		}
	}
	
	private void loadTasksInModel(TaskList taskList) {
		List<Task> tasks = gtaManager.getTasks(taskList, (GTACourseNode)courseNode);
		Map<Long,Task> identityToTask = new HashMap<>();
		for(Task task:tasks) {
			if(task.getIdentity() != null) {
				identityToTask.put(task.getIdentity().getKey(), task);
			}
		}
		
		List<AssessedIdentityElementRow> rows = usersTableModel.getObjects();
		for(AssessedIdentityElementRow row:rows) {
			Task task = identityToTask.get(row.getIdentityKey());
			if(task != null && StringHelper.containsNonWhitespace(task.getTaskName())) {
				row.setDetails(task.getTaskName());
			}
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(assessmentCtrl == source) {
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				reload(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if(bulkAssessmentToolCtrl == source) {
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				reload(ureq);
			}
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(downloadButton == source) {
			doDownload(ureq);
		} else if(groupAssessmentButton == source) {
			doOpenAssessmentForm(ureq);
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}

	@Override
	protected void cleanUp() {
		removeAsListenerAndDispose(assessmentCtrl);
		assessmentCtrl = null;
		super.cleanUp();
	}
	
	@Override
	protected Controller createCalloutController(UserRequest ureq, Identity assessedIdentity) {
		return new GTAIdentityListCourseNodeToolsController(ureq, getWindowControl(), courseNode, assessedIdentity,
				coachCourseEnv);
	}

	@Override
	protected void doSetStatus(Identity assessedIdentity, AssessmentEntryStatus status, CourseNode assessableCourseNode, ICourse course) {
		super.doSetStatus(assessedIdentity, status, assessableCourseNode, course);
		
		TaskList taskList = gtaManager.getTaskList(getCourseRepositoryEntry(), (GTACourseNode)assessableCourseNode);
		Task task = gtaManager.getTask(assessedIdentity, taskList);
		if(task != null && status == AssessmentEntryStatus.done) {
			gtaManager.updateTask(task, TaskProcess.graded, (GTACourseNode)assessableCourseNode, false, getIdentity(), Role.coach);
		}
	}

	private void doDownload(UserRequest ureq) {
		AssessmentToolOptions asOptions = getOptions();
		OLATResource courseOres = getCourseRepositoryEntry().getOlatResource();
		
		ArchiveOptions options = new ArchiveOptions();
		options.setGroup(asOptions.getGroup());
		options.setIdentities(asOptions.getIdentities());
		
		ArchiveResource resource = new ArchiveResource(courseNode, courseOres, options, getLocale());
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
	
	private void doOpenAssessmentForm(UserRequest ureq) {
		removeAsListenerAndDispose(assessmentCtrl);
		
		assessmentCtrl = new GroupAssessmentController(ureq, getWindowControl(), getCourseRepositoryEntry(), (GTACourseNode)courseNode, group);
		listenTo(assessmentCtrl);
		
		String title = translate("grading");
		cmc = new CloseableModalController(getWindowControl(), "close", assessmentCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
}
