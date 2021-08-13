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
package org.olat.course.reminder.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.reminder.CourseNodeReminderProvider;
import org.olat.course.reminder.CourseNodeRuleSPI;
import org.olat.course.reminder.model.ReminderRow;
import org.olat.course.reminder.ui.CourseReminderTableModel.ReminderCols;
import org.olat.modules.reminder.Reminder;
import org.olat.modules.reminder.ReminderModule;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.ReminderService;
import org.olat.modules.reminder.RuleSPI;
import org.olat.modules.reminder.model.ReminderInfos;
import org.olat.repository.RepositoryEntry;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 01.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CourseReminderListController extends FormBasicController
		implements Activateable2, FlexiTableComponentDelegate {
	
	private FormLink addButton;
	private FormLink showLogLink;
	private FlexiTableElement tableEl;
	private CourseReminderTableModel tableModel;
	private final BreadcrumbPanel toolbarPanel;
	
	private VelocityContainer detailsVC;
	private CloseableModalController cmc;
	private ToolsController toolsCtrl;
	private StepsMainRunController wizardCtrl;
	private EmailViewController emailViewCtrl;
	private CourseReminderSendController sendCtrl;
	private DialogBoxController deleteDialogBox;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private CourseSendReminderListController sendReminderListCtrl;
	private CourseReminderLogsController reminderLogsCtrl;

	private final AtomicInteger counter = new AtomicInteger();
	private final RepositoryEntry repositoryEntry;
	private final CourseNodeReminderProvider reminderProvider;
	private final String warningI18nKey;
	
	@Autowired
	private ReminderModule reminderModule;
	@Autowired
	private ReminderService reminderService;
	
	public CourseReminderListController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel toolbarPanel,
			RepositoryEntry repositoryEntry, CourseNodeReminderProvider reminderProvider, String warningI18nKey) {
		super(ureq, wControl, "reminder_list");
		this.toolbarPanel = toolbarPanel;
		this.repositoryEntry = repositoryEntry;
		this.reminderProvider = reminderProvider;
		this.warningI18nKey = warningI18nKey;
		
		initForm(ureq);
		reload(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_course_reminder_list");
		
		addButton = uifactory.addFormLink("add.reminder", formLayout, Link.BUTTON);
		addButton.setIconLeftCSS("o_icon o_icon_add");
		addButton.setElementCssClass("o_sel_add_course_reminder");
		
		DropdownItem dropdown = uifactory.addDropdownMenu("tools", null, null, flc, getTranslator());
		dropdown.setCarretIconCSS("o_icon o_icon_commands");
		dropdown.setOrientation(DropdownOrientation.right);
		dropdown.setExpandContentHeight(true);
		
		showLogLink = uifactory.addFormLink("show.sent", "show.sent", "show.sent", null, flc, Link.LINK);
		showLogLink.setIconLeftCSS("o_icon o_icon-fw o_icon_show_send");
		dropdown.addElement(showLogLink);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ReminderCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReminderCols.description, "edit"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReminderCols.creator));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReminderCols.creationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ReminderCols.lastModified));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ReminderCols.send));
		DefaultFlexiColumnModel toolsCol = new DefaultFlexiColumnModel(ReminderCols.tools);
		toolsCol.setAlwaysVisible(true);
		toolsCol.setExportable(false);
		columnsModel.addFlexiColumnModel(toolsCol);
		
		tableModel = new CourseReminderTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setAndLoadPersistedPreferences(ureq, "course-reminder-list");
		
		detailsVC = createVelocityContainer("reminder_list_details");
		tableEl.setDetailsRenderer(detailsVC, this);
		tableEl.setMultiDetails(true);
	}
	
	private void updateUI() {
		boolean addVisible = reminderProvider.getMainRuleSPITypes() == null || !reminderProvider.getMainRuleSPITypes().isEmpty();
		addButton.setVisible(addVisible);
	}
	
	private void updateModel(UserRequest ureq) {
		List<ReminderInfos> reminders = reminderService.getReminderInfos(repositoryEntry);
		reminders.sort((r1, r2) -> r1.getDescription().compareToIgnoreCase(r2.getDescription()));
		List<ReminderRow> rows = new ArrayList<>(reminders.size());
		for(ReminderInfos reminder:reminders) {
			if (isVisible(reminder)) {
				FormLink toolsLink = uifactory.addFormLink("tools_" + counter.incrementAndGet(), "tools", "", null, null, Link.NONTRANSLATED);
				toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
				toolsLink.setElementCssClass("o_sel_course_reminder_tools");
				toolsLink.setTitle(translate("tools"));
				
				FormLink emailLink = uifactory.addFormLink("email_" + counter.incrementAndGet(), "email", "show.email", null, flc, Link.BUTTON);
				FormLink sendLink = uifactory.addFormLink("send_" + counter.incrementAndGet(), "send", "send", null, flc, Link.BUTTON);
				
				Controller rulesCtrl = new RulesViewController(ureq, getWindowControl(), repositoryEntry, reminder.getConfiguration());
				listenTo(rulesCtrl);
				String rulesCmpName = "rules_" + counter.incrementAndGet();
				detailsVC.put(rulesCmpName, rulesCtrl.getInitialComponent());
				
				ReminderRow row = new ReminderRow(reminder, toolsLink, emailLink, sendLink, rulesCmpName);
				toolsLink.setUserObject(row);
				emailLink.setUserObject(row);
				sendLink.setUserObject(row);
				rows.add(row);
			}
		}
		tableModel.setObjects(rows);
		tableEl.reset(false, false, true);
		tableEl.setVisible(!rows.isEmpty());
	}
	
	private boolean isVisible(ReminderInfos reminder) {
		String configuration = reminder.getConfiguration();
		if (StringHelper.containsNonWhitespace(configuration)) {
			List<ReminderRule> rules = reminderService.toRules(configuration).getRules();
			if(rules != null && !rules.isEmpty()) {
				List<String> nodeIdents = new ArrayList<>(1);
				for (ReminderRule rule : rules) {
					RuleSPI ruleSPI = reminderModule.getRuleSPIByType(rule.getType());
					if (ruleSPI instanceof CourseNodeRuleSPI) {
						nodeIdents.add(((CourseNodeRuleSPI)ruleSPI).getCourseNodeIdent(rule));
					}
				}
				return reminderProvider.filter(nodeIdents);
			}
		}
		
		return false;
	}

	public void reload(UserRequest ureq) {
		updateModel(ureq);
		updateUI();
	}
	
	public boolean hasReminders() {
		return tableModel.getRowCount() > 0;
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.get(0);
		String entryPoint = entry.getOLATResourceable().getResourceableTypeName();
		if("SentReminders".equalsIgnoreCase(entryPoint)) {
			Long key = entry.getOLATResourceable().getResourceableId();
			doSendReminderList(ureq, key);
		} else if("RemindersLogs".equalsIgnoreCase(entryPoint)) {
			doShowLog(ureq);
		}
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> components = new ArrayList<>(1);
		ReminderRow reminderRow = (ReminderRow)rowObject;
		components.add(reminderRow.getSendLink().getComponent());
		components.add(reminderRow.getEmailLink().getComponent());
		return components;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addButton == source) {
			doAddReminder(ureq);
		} else if (showLogLink == source) {
			doShowLog(ureq);
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("tools".equals(cmd)) {
				ReminderRow row = (ReminderRow)link.getUserObject();
				doOpenTools(ureq, row, link);
			} else if("send".equals(cmd)) {
				ReminderRow row = (ReminderRow)link.getUserObject();
				doSend(ureq, row);
			} else if("email".equals(cmd)) {
				ReminderRow row = (ReminderRow)link.getUserObject();
				doShowEmail(ureq, row);
			}
		} else if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				ReminderRow row = tableModel.getObject(se.getIndex());
				if("edit".equals(cmd)) {
					doEdit(ureq, row);
				}
			}
		}

		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (wizardCtrl == source) {
			if (event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				updateModel(ureq);
			}
			getWindowControl().pop();
			cleanUp();
		} else if (reminderLogsCtrl == source) {
			if (event == Event.CHANGED_EVENT) {
				updateModel(ureq);
			}
		} else if(deleteDialogBox == source) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				doDelete(ureq, (ReminderRow)deleteDialogBox.getUserObject());
			}
		} else if (source == emailViewCtrl) {
			cmc.deactivate();
			cleanUp();
		} else if (source == sendCtrl) {
			if (event instanceof CourseReminderSendController.SendEvent) {
				CourseReminderSendController.SendEvent se = (CourseReminderSendController.SendEvent)event;
				doSend(ureq, se.getReminder(), se.isResend());
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(emailViewCtrl);
		removeAsListenerAndDispose(wizardCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(sendCtrl);
		removeAsListenerAndDispose(cmc);
		toolsCalloutCtrl = null;
		emailViewCtrl = null;
		wizardCtrl = null;
		toolsCtrl = null;
		sendCtrl = null;
		cmc = null;
	}

	
	private void doOpenTools(UserRequest ureq, ReminderRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}

	private void doAddReminder(UserRequest ureq) {
		removeAsListenerAndDispose(wizardCtrl);
		Reminder reminder = reminderService.createReminder(repositoryEntry, getIdentity());
		wizardCtrl = new StepsMainRunController(ureq, getWindowControl(), new RulesEditStep(ureq, reminder, reminderProvider, warningI18nKey),
				doSaveReminder(), null, translate("new.reminder"), "");
		listenTo(wizardCtrl);
		getWindowControl().pushAsModalDialog(wizardCtrl.getInitialComponent());
	}
	
	private StepRunnerCallback doSaveReminder() {
		return (uureq, control, runContext) -> {
			Reminder reminder = (Reminder)runContext.get(RulesEditStep.CONTEXT_KEY);
			reminderService.save(reminder);
			return StepsMainRunController.DONE_MODIFIED;
		};
	}

	private void doSendReminderList(UserRequest ureq, Long reminderKey) {
		removeAsListenerAndDispose(sendReminderListCtrl);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("SentReminders", reminderKey);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());

		Reminder reminder = reminderService.loadByKey(reminderKey);
		sendReminderListCtrl = new CourseSendReminderListController(ureq, bwControl, reminder);
		listenTo(sendReminderListCtrl);
		addToHistory(ureq, sendReminderListCtrl);
		
		toolbarPanel.pushController(translate("send.reminder"), sendReminderListCtrl);	
	}

	private void doShowLog(UserRequest ureq) {
		removeAsListenerAndDispose(reminderLogsCtrl);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("RemindersLogs", 0l);
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrapBusinessPath(ores));
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());

		reminderLogsCtrl = new CourseReminderLogsController(ureq, bwControl, repositoryEntry, reminderProvider);
		listenTo(reminderLogsCtrl);
		addToHistory(ureq, reminderLogsCtrl);
		
		toolbarPanel.pushController(translate("send.reminder"), reminderLogsCtrl);	
	}
	
	private void doConfirmDelete(UserRequest ureq, ReminderRow row) {
		String desc = StringHelper.escapeHtml(row.getDescription());
		deleteDialogBox = activateYesNoDialog(ureq, translate("dialog.modal.delete.title"), translate("dialog.modal.delete.text", desc), deleteDialogBox);
		deleteDialogBox.setUserObject(row);
	}

	private void doDelete(UserRequest ureq, ReminderRow row) {
		Reminder reminder = reminderService.loadByKey(row.getKey());
		reminderService.delete(reminder);
		updateModel(ureq);
		fireEvent(ureq, ReminderDeletedEvent.EVENT);
	}

	private void doEdit(UserRequest ureq, ReminderRow row) {
		removeAsListenerAndDispose(wizardCtrl);
		
		Reminder reminder = reminderService.loadByKey(row.getKey());
		wizardCtrl = new StepsMainRunController(ureq, getWindowControl(), new RulesEditStep(ureq, reminder, reminderProvider, warningI18nKey),
				doSaveReminder(), null, translate("edit.reminder"), "");
		listenTo(wizardCtrl);
		getWindowControl().pushAsModalDialog(wizardCtrl.getInitialComponent());
	}
	
	private void doDuplicate(UserRequest ureq, ReminderRow row) {
		Reminder reminder = reminderService.loadByKey(row.getKey());
		reminderService.duplicate(reminder, getIdentity());
		updateModel(ureq);
	}
	
	private void doSend(UserRequest ureq, ReminderRow row) {
		Reminder reminder = reminderService.loadByKey(row.getKey());
		sendCtrl = new CourseReminderSendController(ureq, getWindowControl(), reminder, false);
		listenTo(sendCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), sendCtrl.getInitialComponent(),
				true, translate("send"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doSend(UserRequest ureq, Reminder reminder, boolean resend) {
		Reminder relodedReminder = reminderService.loadByKey(reminder.getKey());
		reminderService.sendReminder(relodedReminder, resend);
		updateModel(ureq);
	}
	
	private void doShowEmail(UserRequest ureq, ReminderRow row) {
		Reminder reminder = reminderService.loadByKey(row.getKey());
		emailViewCtrl = new EmailViewController(ureq, getWindowControl(), reminder);
		listenTo(emailViewCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), emailViewCtrl.getInitialComponent(),
				true, translate("edit.email"), true);
		listenTo(cmc);
		cmc.activate();
	}

	private class ToolsController extends BasicController {
		
		private ReminderRow row;
		private final VelocityContainer mainVC;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, ReminderRow row) {
			super(ureq, wControl);
			this.row = row;
			
			mainVC = createVelocityContainer("tools");
			List<String> links = new ArrayList<>();

			addLink("edit", "edit", "o_icon o_icon-fw o_icon_edit", links, "o_sel_course_reminder_edit");
			addLink("duplicate", "duplicate", "o_icon o_icon-fw o_icon_copy", links, "o_sel_course_reminder_duplicate");
			addLink("send", "send", "o_icon o_icon-fw o_icon_send", links, "o_sel_course_reminder_send");
			addLink("show.sent", "show.sent", "o_icon o_icon-fw o_icon_show_send", links, "o_sel_course_reminder_showsent");
			addLink("delete", "delete", "o_icon o_icon-fw o_icon_delete_item", links, "o_sel_course_reminder_delete");

			mainVC.contextPut("links", links);
			putInitialPanel(mainVC);
		}
		
		private void addLink(String name, String cmd, String iconCSS, List<String> links, String elementCssClass) {
			Link link = LinkFactory.createLink(name, cmd, getTranslator(), mainVC, this, Link.LINK);
			if(iconCSS != null) {
				link.setIconLeftCSS(iconCSS);
			}
			if(elementCssClass != null) {
				link.setElementCssClass(elementCssClass);
			}
			mainVC.put(name, link);
			links.add(name);
		}
		
		@Override
		protected void doDispose() {
			//
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(source instanceof Link) {
				Link link = (Link)source;
				String cmd = link.getCommand();
				
				toolsCalloutCtrl.deactivate();
				cleanUp();
				if("edit".equals(cmd)) {
					doEdit(ureq, row);
				} else if("show.sent".equals(cmd)) {
					doSendReminderList(ureq, row.getKey());
				} else if("delete".equals(cmd)) {
					doConfirmDelete(ureq, row);
				} else if("duplicate".equals(cmd)) {
					doDuplicate(ureq, row);
				} else if("send".equals(cmd)) {
					doSend(ureq, row);
				}
					
			}
		}
	}
}
