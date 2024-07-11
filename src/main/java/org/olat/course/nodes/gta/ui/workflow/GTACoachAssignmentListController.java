/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.gta.ui.workflow;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.commons.services.doceditor.DocEditorDisplayInfo;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DownloadLink;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.InfoPanelItem;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.nodes.GTACourseNode;
import org.olat.course.nodes.gta.Task;
import org.olat.course.nodes.gta.TaskProcess;
import org.olat.course.nodes.gta.model.DueDate;
import org.olat.course.nodes.gta.model.TaskDefinition;
import org.olat.course.nodes.gta.ui.GTACoachController;
import org.olat.course.nodes.gta.ui.GTAUIFactory;
import org.olat.course.nodes.gta.ui.workflow.CoachedParticipantTableModel.CoachCols;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 juin 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GTACoachAssignmentListController extends AbstractCoachWorkflowListController {
	
	private File tasksFolder;
	private final VFSContainer tasksContainer;
	private Map<String,TaskDefinition> fileNameToDefinitions;
	
	@Autowired
	private DocEditorService docEditorService;
	
	public GTACoachAssignmentListController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment coachCourseEnv, List<Identity> assessableIdentities, GTACourseNode gtaNode) {
		super(ureq, wControl, "assignment_list", coachCourseEnv, assessableIdentities, gtaNode);

		tasksContainer = gtaManager.getTasksContainer(courseEnv, gtaNode);
		
		initForm(ureq);
		loadModel();
	}
	
	@Override
	protected void initConfigurationInfos(InfoPanelItem panel) {
		StringBuilder infos = new StringBuilder();
		
		DueDateConfig dueDateConfig = gtaNode.getDueDateConfig(GTACourseNode.GTASK_ASSIGNMENT_DEADLINE);
		if(dueDateConfig != DueDateConfig.noDueDateConfig()) {
			String dueDateVal = dueDateConfigToString(dueDateConfig);
			if(StringHelper.containsNonWhitespace(dueDateVal)) {
				String deadlineInfos = translate("workflow.deadline.assignment", dueDateVal);
				infos.append("<p><i class='o_icon o_icon-fw o_icon_timelimit'> </i> ").append(deadlineInfos).append("</p>");
			}
		}
		
		String type = gtaNode.getModuleConfiguration().getStringValue(GTACourseNode.GTASK_ASSIGNEMENT_TYPE, GTACourseNode.GTASK_ASSIGNEMENT_TYPE_MANUAL);
		String typeI18n = GTACourseNode.GTASK_ASSIGNEMENT_TYPE_AUTO.equals(type) ? "task.assignment.type.auto" : "task.assignment.type.manual";
		String assignmentType = translate("workflow.type.assignment", translate(typeI18n));
		infos.append("<p><i class='o_icon o_icon-fw o_icon_list_check'> </i> ").append(assignmentType).append("</p>");
		
		panel.setInformations(infos.toString());
	}

	@Override
	protected void initColumnsModel(FlexiTableColumnModel columnsModel) {
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachCols.taskTitle));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachCols.taskName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachCols.assignmentOverrideDueDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CoachCols.assignment));
	}

	@Override
	protected void initFilters(List<FlexiTableExtendedFilter> filters) {
		SelectionValues assignmentStatusPK = new SelectionValues();
		assignmentStatusPK.add(SelectionValues.entry(CoachedParticipantStatus.waiting.name(), translate(CoachedParticipantStatus.waiting.i18nKey())));
		assignmentStatusPK.add(SelectionValues.entry(CoachedParticipantStatus.expired.name(), translate(CoachedParticipantStatus.expired.i18nKey())));
		assignmentStatusPK.add(SelectionValues.entry(CoachedParticipantStatus.done.name(), translate(CoachedParticipantStatus.done.i18nKey())));
		FlexiTableMultiSelectionFilter assignmentStatusFilter = new FlexiTableMultiSelectionFilter(translate("filter.assignment.status"),
				FILTER_STATUS, assignmentStatusPK, true);
		filters.add(assignmentStatusFilter);
	}
	
	@Override
	protected void initFiltersPresets(UserRequest ureq, List<FlexiFiltersTab> tabs) {
		FlexiFiltersTab waitingTab = FlexiFiltersTabFactory.tabWithImplicitFilters(WAITING_TAB_ID, translate(CoachedParticipantStatus.waiting.i18nKey()),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, List.of(CoachedParticipantStatus.waiting.name()))));
		tabs.add(waitingTab);
		
		FlexiFiltersTab epxiredTab = FlexiFiltersTabFactory.tabWithImplicitFilters(EXPIRED_TAB_ID, translate(CoachedParticipantStatus.expired.i18nKey()),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, List.of(CoachedParticipantStatus.expired.name()))));
		tabs.add(epxiredTab);
		
		FlexiFiltersTab doneTab = FlexiFiltersTabFactory.tabWithImplicitFilters(DONE_TAB_ID, translate(CoachedParticipantStatus.done.i18nKey()),
				TabSelectionBehavior.clear, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, List.of(CoachedParticipantStatus.done.name()))));
		tabs.add(doneTab);
	}
	
	@Override
	protected void initBulkTools(FormItemContainer formLayout) {
		initBulkExtendTool(formLayout);
		super.initBulkTools(formLayout);
	}

	@Override
	protected void loadModel() {
		List<TaskDefinition> taskDefinitions = gtaManager.getTaskDefinitions(courseEnv, gtaNode);
		fileNameToDefinitions = taskDefinitions.stream()
				.filter(def -> Objects.nonNull(def.getFilename()))
				.collect(Collectors.toMap(TaskDefinition::getFilename, Function.identity(), (u, v) -> u));
		tasksFolder = gtaManager.getTasksDirectory(courseEnv, gtaNode);
		
		super.loadModel();
		
	}

	@Override
	protected CoachedParticipantRow forgeRow(CoachedParticipantRow identityRow, RepositoryEntry entry) {
		identityRow.setToolsLink(forgeToolsLink(identityRow));
		
		IdentityRef assessedIdentity = new IdentityRefImpl(identityRow.getIdentityKey());
		DueDate asssignmentDueDate = gtaManager.getAssignmentDueDate(identityRow.getTask(), assessedIdentity, null, gtaNode, entry, true);
		identityRow.setAssignmentDueDate(asssignmentDueDate);
		status(identityRow, asssignmentDueDate);

		String taskName = identityRow.getTaskName();
		TaskDefinition taskDefinition = null;
		if(StringHelper.containsNonWhitespace(taskName)) {
			taskDefinition = fileNameToDefinitions.get(taskName);
		}
	
		if(taskDefinition != null) {
			FormLink openLink = null;
			VFSItem item = tasksContainer.resolve(taskDefinition.getFilename());
			if(item instanceof VFSLeaf vfsLeaf) {
				VFSMetadata metaInfo = item.getMetaInfo();
				DocEditorDisplayInfo editorInfo = docEditorService.getEditorInfo(getIdentity(), roles, vfsLeaf,
						metaInfo, true, DocEditorService.MODES_VIEW);
				// If possible retrieve openLink to open document in editor
				// If not then openLink is null and downloadLink will be used
				if (editorInfo.isEditorAvailable()) {
					openLink = uifactory.addFormLink("open_" + CodeHelper.getRAMUniqueID(), "open", taskDefinition.getFilename(), null, flc, Link.NONTRANSLATED);
					openLink.setIconLeftCSS("o_icon o_icon-fw " + CSSHelper.createFiletypeIconCssClassFor(taskDefinition.getFilename()));
					if (editorInfo.isNewWindow()) {
						openLink.setNewWindow(true, true, false);
					}
					openLink.setUserObject(item);
				}
			}
	
			File file = new File(tasksFolder, taskDefinition.getFilename());
			DownloadLink downloadLink = uifactory.addDownloadLink("task_" + (count++), taskDefinition.getFilename(), null, file, tableEl);
			identityRow.setDownloadTaskFileLink(downloadLink);
			identityRow.setOpenTaskFileLink(openLink);
		}
		
		return identityRow;
	}

	private void status(CoachedParticipantRow identityRow, DueDate asssignmentDueDate) {
		Task assignedTask = identityRow.getTask();
		if(assignedTask == null || assignedTask.getTaskStatus() == TaskProcess.assignment) {
			identityRow.setStatus(CoachedParticipantStatus.waiting);
			//assignment open?
			if(asssignmentDueDate != null && asssignmentDueDate.getDueDate() != null
					&& asssignmentDueDate.getDueDate().compareTo(new Date()) < 0) {
				//assignment is closed
				boolean hasAssignment = assignedTask != null && StringHelper.containsNonWhitespace(assignedTask.getTaskName());
				if(!hasAssignment) {
					identityRow.setStatus(CoachedParticipantStatus.expired);
				}
			}
		} else {
			identityRow.setStatus(CoachedParticipantStatus.done);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink link) {
			String cmd = link.getCmd();
			if ("open".equalsIgnoreCase(cmd) && link.getUserObject() instanceof VFSLeaf vfsLeaf) {
				doOpenMedia(ureq, vfsLeaf);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doOpenMedia(UserRequest ureq, VFSLeaf vfsLeaf) {
		addToHistory(ureq, this);

		DocEditorConfigs configs = GTAUIFactory.getEditorConfig(tasksContainer, vfsLeaf, vfsLeaf.getName(), DocEditor.Mode.EDIT, null);
		Controller docEditorCtrl = docEditorService.openDocument(ureq, getWindowControl(), configs, DocEditorService.MODES_VIEW)
				.getController();
		listenTo(docEditorCtrl);
	}
	
	@Override
	protected Controller createToolsController(UserRequest ureq, CoachedParticipantRow row) {
		return new AssignmentToolsController(ureq, getWindowControl(), row);
	}

	private class AssignmentToolsController extends BasicController {

		private Link dueDatesLink;
		
		private CoachedParticipantRow row;
		
		public AssignmentToolsController(UserRequest ureq, WindowControl wControl, CoachedParticipantRow row) {
			super(ureq, wControl, Util.createPackageTranslator(GTACoachController.class, ureq.getLocale()));
			this.row = row;
			
			VelocityContainer mainVC = createVelocityContainer("tools");
			
			dueDatesLink = LinkFactory.createLink("duedates", "duedates", getTranslator(), mainVC, this, Link.LINK);
			dueDatesLink.setIconLeftCSS("o_icon o_icon-fw o_icon_extra_time");
		
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(dueDatesLink == source) {
				doEditDueDate(ureq, row);
			}
		}
	}
}
