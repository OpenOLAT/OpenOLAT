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
package org.olat.modules.quality.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.olat.core.commons.controllers.activity.ActivityLogController;
import org.olat.core.commons.controllers.activity.ActivityLogRow;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.util.SelectionValuesSupplier;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.DateRange;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.quality.QualityAuditLog;
import org.olat.modules.quality.QualityAuditLogSearchParams;
import org.olat.modules.quality.QualityService;
import org.olat.modules.quality.manager.QualityXStream;
import org.olat.modules.todo.ToDoService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;
import org.olat.modules.todo.ToDoTaskRef;
import org.olat.modules.todo.ui.ToDoUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityToDoActivityLogController extends ActivityLogController {
	
	private final ToDoTaskRef toDoTask;

	@Autowired
	private QualityService qualityService;
	@Autowired
	private ToDoService toDoService;

	public QualityToDoActivityLogController(UserRequest ureq, WindowControl wControl, Form mainForm, ToDoTaskRef toDoTask) {
		super(ureq, wControl, mainForm);
		setTranslator(Util.createPackageTranslator(ToDoUIFactory.class, getLocale(), getTranslator()));
		this.toDoTask = toDoTask;
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected SelectionValuesSupplier getActivityFilterValues() {
		SelectionValues filterSV = new SelectionValues();
		addActivityFilterValue(filterSV, "activity.log.message.create");
		addActivityFilterValue(filterSV, "activity.log.message.delete");
		addActivityFilterValue(filterSV, "activity.log.message.member.add");
		addActivityFilterValue(filterSV, "activity.log.message.member.remove");
		addActivityFilterValue(filterSV, "activity.log.message.tag.add");
		addActivityFilterValue(filterSV, "activity.log.message.tag.remove");
		addActivityFilterValue(filterSV, "activity.log.message.todo.task.title");
		addActivityFilterValue(filterSV, "activity.log.message.todo.task.text");
		addActivityFilterValue(filterSV, "activity.log.message.todo.task.status");
		addActivityFilterValue(filterSV, "activity.log.message.todo.task.priority");
		addActivityFilterValue(filterSV, "activity.log.message.todo.task.expenditure.of.work");
		addActivityFilterValue(filterSV, "activity.log.message.todo.task.start.date");
		addActivityFilterValue(filterSV, "activity.log.message.todo.task.due.date");
		return filterSV;
	}

	@Override
	protected List<Identity> getFilterIdentities() {
		QualityAuditLogSearchParams searchParams = new QualityAuditLogSearchParams();
		searchParams.setToDoTasks(List.of(toDoTask));
		searchParams.setActions(QualityAuditLog.TODO_ACTIONS);
		searchParams.setFetchDoer(true);
		return qualityService.loadAuditLogDoers(searchParams);
	}

	@Override
	protected List<ActivityLogRow> loadRows(DateRange dateRange, Set<Long> doerKeys) {
		QualityAuditLogSearchParams searchParams = new QualityAuditLogSearchParams();
		searchParams.setToDoTasks(List.of(toDoTask));
		searchParams.setActions(QualityAuditLog.TODO_ACTIONS);
		searchParams.setDoerKeys(doerKeys);
		searchParams.setFetchDoer(true);
		List<QualityAuditLog> auditLogs = qualityService.loadAuditLogs(searchParams, 0, -1);
		
		List<ActivityLogRow> rows = new ArrayList<>(auditLogs.size());
		for (QualityAuditLog auditLog : auditLogs) {
			addActivityRows(rows, auditLog);
		}
		return rows;
	}
	
	private void addActivityRows(List<ActivityLogRow> rows, QualityAuditLog auditLog) {
		switch (auditLog.getAction()) {
		case toDoCreate: addRow(rows, auditLog, "activity.log.message.create"); break;
		case toDoMemberAdd: addRow(rows, auditLog, "activity.log.message.member.add", null, userManager.getUserDisplayName(auditLog.getIdentity().getKey())); break;
		case toDoMemberRemove: addRow(rows, auditLog, "activity.log.message.member.remove", userManager.getUserDisplayName(auditLog.getIdentity().getKey()), null); break;
		case toDoTagsUpdate: addActivityTagsUpdateRows(rows, auditLog); break;
		case toDoStatusUpdate: {
			if (StringHelper.containsNonWhitespace(auditLog.getBefore()) && StringHelper.containsNonWhitespace(auditLog.getAfter())) {
				ToDoTask before = QualityXStream.fromXml(auditLog.getBefore(), ToDoTask.class);
				ToDoTask after = QualityXStream.fromXml(auditLog.getAfter(), ToDoTask.class);
				if (after.getStatus() == ToDoStatus.deleted) {
					addRow(rows, auditLog, "activity.log.message.delete"); break;
				} else if (!Objects.equals(before.getStatus(), after.getStatus())) {
					addRow(rows, auditLog, "activity.log.message.todo.task.status",
							ToDoUIFactory.getDisplayName(getTranslator(), before.getStatus()),
							ToDoUIFactory.getDisplayName(getTranslator(), after.getStatus()));
				}
			}
			break;
		}
		case toDoContentUpdate: {
			if (StringHelper.containsNonWhitespace(auditLog.getBefore()) && StringHelper.containsNonWhitespace(auditLog.getAfter())) {
				ToDoTask before = QualityXStream.fromXml(auditLog.getBefore(), ToDoTask.class);
				ToDoTask after = QualityXStream.fromXml(auditLog.getAfter(), ToDoTask.class);
				if (!Objects.equals(before.getTitle(), after.getTitle())) {
					addRow(rows, auditLog, "activity.log.message.todo.task.title", before.getTitle(), after.getTitle());
				}
				if (!Objects.equals(before.getDescription(), after.getDescription())) {
					addRow(rows, auditLog, "activity.log.message.todo.task.description", before.getDescription(), after.getDescription());
				}
				if (!Objects.equals(before.getPriority(), after.getPriority())) {
					addRow(rows, auditLog, "activity.log.message.todo.task.priority",
							ToDoUIFactory.getDisplayName(getTranslator(), before.getPriority()),
							ToDoUIFactory.getDisplayName(getTranslator(), after.getPriority()));
				}
				if (!Objects.equals(before.getExpenditureOfWork(), after.getExpenditureOfWork())) {
					addRow(rows, auditLog, "activity.log.message.todo.task.expenditure.of.work",
							ToDoUIFactory.format(toDoService.getExpenditureOfWork(before.getExpenditureOfWork())),
							ToDoUIFactory.format(toDoService.getExpenditureOfWork(after.getExpenditureOfWork())));
				}
				Date beforeStartDate = before.getStartDate() != null? new Date(before.getStartDate().getTime()): null;
				Date afterStartDate = after.getStartDate() != null? new Date(after.getStartDate().getTime()): null;
				if (!Objects.equals(beforeStartDate, afterStartDate)) {
					addRow(rows, auditLog, "activity.log.message.todo.task.start.date",
							formatter.formatDateAndTime(beforeStartDate),
							formatter.formatDateAndTime(afterStartDate));
				}
				Date beforeDueDate = before.getDueDate() != null? new Date(before.getDueDate().getTime()): null;
				Date afterDueDate = after.getDueDate() != null? new Date(after.getDueDate().getTime()): null;
				if (!Objects.equals(beforeDueDate, afterDueDate)) {
					addRow(rows, auditLog, "activity.log.message.todo.task.due.date",
							formatter.formatDateAndTime(beforeDueDate),
							formatter.formatDateAndTime(afterDueDate));
				}
			}
			break;
		}
		default: //
		}
	}
	
	private void addActivityTagsUpdateRows(List<ActivityLogRow> rows, QualityAuditLog auditLog) {
		List<String> tagsBefore = QualityXStream.tagsFromXml(auditLog.getBefore());
		List<String> tagsAfter = QualityXStream.tagsFromXml(auditLog.getAfter());
		for (String tagAfter : tagsAfter) {
			if (!tagsBefore.contains(tagAfter)) {
				addRow(rows, auditLog, "activity.log.message.tag.add", null, tagAfter); 
			}
		}
		for (String tagBefore : tagsBefore) {
			if (!tagsAfter.contains(tagBefore)) {
				addRow(rows, auditLog, "activity.log.message.tag.remove", tagBefore, null); 
			}
		}
	}
	
	private void addRow(List<ActivityLogRow> rows, QualityAuditLog auditLog, String messageI18n) {
		addRow(rows, auditLog, messageI18n, null, null);
	}
	
	private void addRow(List<ActivityLogRow> rows, QualityAuditLog auditLog, String messageI18nKey, String originalValue, String newValue) {
		ActivityLogRow row = createRow(auditLog.getDoer());
		row.setDate(auditLog.getCreationDate());
		row.setMessageI18nKey(messageI18nKey);
		row.setMessage(translate(messageI18nKey));
		row.setOriginalValue(originalValue);
		row.setNewValue(newValue);
		rows.add(row);
	}

}
