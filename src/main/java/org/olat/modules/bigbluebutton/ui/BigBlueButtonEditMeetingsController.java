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
package org.olat.modules.bigbluebutton.ui;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.util.StringHelper;
import org.olat.group.BusinessGroup;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.BigBlueButtonMeeting;
import org.olat.modules.bigbluebutton.BigBlueButtonModule;
import org.olat.modules.bigbluebutton.BigBlueButtonTemplatePermissions;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrors;
import org.olat.modules.bigbluebutton.ui.BigBlueButtonMeetingTableModel.BMeetingsCols;
import org.olat.modules.bigbluebutton.ui.EditBigBlueButtonMeetingController.Mode;
import org.olat.modules.bigbluebutton.ui.recurring.RecurringMeeting;
import org.olat.modules.bigbluebutton.ui.recurring.RecurringMeeting1Step;
import org.olat.modules.bigbluebutton.ui.recurring.RecurringMeetingsContext;
import org.olat.modules.bigbluebutton.ui.recurring.RecurringMeetingsContext.RecurringMode;
import org.olat.modules.gotomeeting.ui.GoToMeetingTableModel.MeetingsCols;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BigBlueButtonEditMeetingsController extends FormBasicController {

	private FormLink deleteButton;
	private FormLink addSingleMeetingLink;
	private FormLink addPermanentMeetingLink;
	private FormLink addDailyRecurringMeetingsLink;
	private FormLink addWeekyRecurringMeetingsLink;
	private FlexiTableElement tableEl;
	private BigBlueButtonMeetingTableModel tableModel;
	
	private CloseableModalController cmc;
	private DialogBoxController confirmDelete;
	private DialogBoxController confirmBatchDelete;
	private StepsMainRunController addDailyMeetingCtrl;
	private StepsMainRunController addWeeklyMeetingCtrl;
	private EditBigBlueButtonMeetingController editMeetingCtlr;
	
	private final boolean readOnly;
	private final String subIdent;
	private final RepositoryEntry entry;
	private final BusinessGroup businessGroup;

	@Autowired
	private UserManager userManager;
	@Autowired
	private BigBlueButtonModule bigBlueButtonModule;
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	
	public BigBlueButtonEditMeetingsController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, String subIdentifier, BusinessGroup group, boolean readOnly) {
		super(ureq, wControl, "meetings_admin");
		this.readOnly = readOnly;
		this.entry = entry;
		this.subIdent = subIdentifier;
		this.businessGroup = group;
		initForm(ureq);
		updateModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(!readOnly) {
			DropdownItem addMeetingDropdown = uifactory.addDropdownMenu("add.meeting", "add.meeting", formLayout, getTranslator());
			addMeetingDropdown.setOrientation(DropdownOrientation.right);
			addMeetingDropdown.setElementCssClass("o_sel_bbb_meeting_add");
			
			addSingleMeetingLink = uifactory.addFormLink("add.single.meeting", formLayout, Link.LINK);
			addSingleMeetingLink.setElementCssClass("o_sel_bbb_single_meeting_add");
			addMeetingDropdown.addElement(addSingleMeetingLink);
			if(bigBlueButtonModule.isPermanentMeetingEnabled()) {
				addPermanentMeetingLink = uifactory.addFormLink("add.permanent.meeting", formLayout, Link.LINK);
				addPermanentMeetingLink.setElementCssClass("o_sel_bbb_permanent_meeting_add");
				addMeetingDropdown.addElement(addPermanentMeetingLink);
			}
			addDailyRecurringMeetingsLink = uifactory.addFormLink("add.daily.meeting", formLayout, Link.LINK);
			addDailyRecurringMeetingsLink.setElementCssClass("o_sel_bbb_daily_meeting_add");
			addMeetingDropdown.addElement(addDailyRecurringMeetingsLink);
			addWeekyRecurringMeetingsLink = uifactory.addFormLink("add.weekly.meeting", formLayout, Link.LINK);
			addWeekyRecurringMeetingsLink.setElementCssClass("o_sel_bbb_weekly_meeting_add");
			addMeetingDropdown.addElement(addWeekyRecurringMeetingsLink);
			
			deleteButton = uifactory.addFormLink("delete", formLayout, Link.BUTTON);
		}
		
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BMeetingsCols.name));
		if(bigBlueButtonModule.isPermanentMeetingEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BMeetingsCols.permanent));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BMeetingsCols.start));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BMeetingsCols.end));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BMeetingsCols.template));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, BMeetingsCols.server, new ServerCellRenderer()));
		if(!readOnly) {
			DefaultFlexiColumnModel editViewCol = new DefaultFlexiColumnModel(BMeetingsCols.edit, "edit",
					new BooleanCellRenderer(
							new StaticFlexiCellRenderer(translate("edit"), "edit"),
							new StaticFlexiCellRenderer(translate("view"), "edit")));
			editViewCol.setExportable(false);
			editViewCol.setAlwaysVisible(true);
			columnsModel.addFlexiColumnModel(editViewCol);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete"));
		}
		
		tableModel = new BigBlueButtonMeetingTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "meetings", tableModel, getTranslator(), formLayout);
		tableEl.setEmtpyTableMessageKey("no.meeting.configured");
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(MeetingsCols.start.name(), false));
		tableEl.setSortSettings(sortOptions);
		tableEl.setAndLoadPersistedPreferences(ureq, "bigbluebutton-connect-edit-meetings-list");
		tableEl.setMultiSelect(!readOnly);
		tableEl.setSelectAllEnable(true);
		if(deleteButton != null) {
			tableEl.addBatchButton(deleteButton);
		}
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public void updateModel() {
		List<BigBlueButtonMeeting> meetings = bigBlueButtonManager.getMeetings(entry, subIdent, businessGroup, false);
		tableModel.setObjects(meetings);
		tableEl.reset(true, true, true);	
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == editMeetingCtlr) {
			if(event == Event.DONE_EVENT) {
				updateModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmDelete == source) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				BigBlueButtonMeeting meeting = (BigBlueButtonMeeting)confirmDelete.getUserObject();
				doDelete(meeting);
			}
			cleanUp();
		} else if(confirmBatchDelete == source) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				@SuppressWarnings("unchecked")
				List<BigBlueButtonMeeting> meetings = (List<BigBlueButtonMeeting>)confirmBatchDelete.getUserObject();
				doDelete(meetings);
			}
			cleanUp();
		} else if(addDailyMeetingCtrl == source || addWeeklyMeetingCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					updateModel();
				}
				cleanUp();
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(addWeeklyMeetingCtrl);
		removeAsListenerAndDispose(addDailyMeetingCtrl);
		removeAsListenerAndDispose(confirmBatchDelete);
		removeAsListenerAndDispose(editMeetingCtlr);
		removeAsListenerAndDispose(confirmDelete);
		removeAsListenerAndDispose(cmc);
		addWeeklyMeetingCtrl = null;
		addDailyMeetingCtrl = null;
		confirmBatchDelete = null;
		editMeetingCtlr = null;
		confirmDelete = null;
		cmc = null;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(deleteButton == source) {
			List<BigBlueButtonMeeting> selectedMeetings = getSelectedMeetings();
			doConfirmDelete(ureq, selectedMeetings);
		} else if(addSingleMeetingLink == source) {
			doAddSingleMeeting(ureq);
		} else if(addPermanentMeetingLink == source) {
			doAddPermanentMeeting(ureq);
		} else if(addDailyRecurringMeetingsLink == source) {
			doAddDailyRecurringMeeting(ureq);
		} else if(addWeekyRecurringMeetingsLink == source) {
			doAddWeeklyRecurringMeeting(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("edit".equals(se.getCommand())) {
					doEditMeeting(ureq, tableModel.getObject(se.getIndex()));
				} else if("delete".equals(se.getCommand())) {
					doConfirmDelete(ureq, tableModel.getObject(se.getIndex()));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private List<BigBlueButtonMeeting> getSelectedMeetings() {
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		return selectedIndex.stream()
				.map(index -> tableModel.getObject(index.intValue()))
				.collect(Collectors.toList());
	}
	

	private void doAddSingleMeeting(UserRequest ureq) {
		if(guardModalController(editMeetingCtlr)) return;

		List<BigBlueButtonTemplatePermissions> permissions = bigBlueButtonManager
				.calculatePermissions(entry, businessGroup, getIdentity(), ureq.getUserSession().getRoles());
		editMeetingCtlr = new EditBigBlueButtonMeetingController(ureq, getWindowControl(),
				entry, subIdent, businessGroup, permissions, Mode.dates);
		listenTo(editMeetingCtlr);
		
		cmc = new CloseableModalController(getWindowControl(), "close", editMeetingCtlr.getInitialComponent(),
				true, translate("add.single.meeting"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doAddPermanentMeeting(UserRequest ureq) {
		if(guardModalController(editMeetingCtlr)) return;

		List<BigBlueButtonTemplatePermissions> permissions = bigBlueButtonManager
				.calculatePermissions(entry, businessGroup, getIdentity(), ureq.getUserSession().getRoles());
		editMeetingCtlr = new EditBigBlueButtonMeetingController(ureq, getWindowControl(),
				entry, subIdent, businessGroup, permissions, Mode.permanent);
		listenTo(editMeetingCtlr);
		
		cmc = new CloseableModalController(getWindowControl(), "close", editMeetingCtlr.getInitialComponent(),
				true, translate("add.permanent.meeting"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doAddDailyRecurringMeeting(UserRequest ureq) {
		removeAsListenerAndDispose(addDailyMeetingCtrl);
		
		List<BigBlueButtonTemplatePermissions> permissions = bigBlueButtonManager
				.calculatePermissions(entry, businessGroup, getIdentity(), ureq.getUserSession().getRoles());
		final RecurringMeetingsContext context = new RecurringMeetingsContext(entry, subIdent, businessGroup,
				permissions, RecurringMode.daily);
		context.setMainPresenter(userManager.getUserDisplayName(getIdentity()));
		
		RecurringMeeting1Step step = new RecurringMeeting1Step(ureq, context);
		StepRunnerCallback finishCallback = (uureq, swControl, runContext) -> {
			addRecurringMeetings(context);
			return StepsMainRunController.DONE_MODIFIED;
		};
		String title = translate("add.daily.meeting");
		addDailyMeetingCtrl = new StepsMainRunController(ureq, getWindowControl(), step, finishCallback, null, title, "");
		listenTo(addDailyMeetingCtrl);
		getWindowControl().pushAsModalDialog(addDailyMeetingCtrl.getInitialComponent());
	}
	
	private void doAddWeeklyRecurringMeeting(UserRequest ureq) {
		removeAsListenerAndDispose(addWeeklyMeetingCtrl);
		
		List<BigBlueButtonTemplatePermissions> permissions = bigBlueButtonManager
				.calculatePermissions(entry, businessGroup, getIdentity(), ureq.getUserSession().getRoles());
		final RecurringMeetingsContext context = new RecurringMeetingsContext(entry, subIdent, businessGroup,
				permissions, RecurringMode.weekly);
		context.setMainPresenter(userManager.getUserDisplayName(getIdentity()));

		RecurringMeeting1Step step = new RecurringMeeting1Step(ureq, context);
		StepRunnerCallback finishCallback = (uureq, swControl, runContext) -> {
			addRecurringMeetings(context);
			return StepsMainRunController.DONE_MODIFIED;
		};
		String title = translate("add.weekly.meeting");
		addWeeklyMeetingCtrl = new StepsMainRunController(ureq, getWindowControl(), step, finishCallback, null, title, "");
		listenTo(addWeeklyMeetingCtrl);
		getWindowControl().pushAsModalDialog(addWeeklyMeetingCtrl.getInitialComponent());
	}
	
	private void addRecurringMeetings(RecurringMeetingsContext context) {
		for(RecurringMeeting meeting:context.getMeetings()) {
			if(meeting.isDeleted() || !meeting.isSlotAvailable()) {
				continue;
			}
			
			BigBlueButtonMeeting bMeeting = bigBlueButtonManager.createAndPersistMeeting(context.getName(),
					context.getEntry(), context.getSubIdent(), context.getBusinessGroup(), getIdentity());
			bMeeting.setDescription(context.getDescription());
			bMeeting.setWelcome(context.getWelcome());
			bMeeting.setMainPresenter(context.getMainPresenter());
			bMeeting.setPermanent(false);
			bMeeting.setTemplate(context.getTemplate());
			bMeeting.setStartDate(meeting.getStartDate());
			bMeeting.setEndDate(meeting.getEndDate());
			bMeeting.setLeadTime(context.getLeadTime());
			bMeeting.setFollowupTime(context.getFollowupTime());
			bigBlueButtonManager.updateMeeting(bMeeting);
		}
	}
	
	private void doEditMeeting(UserRequest ureq, BigBlueButtonMeeting meeting) {
		if(guardModalController(editMeetingCtlr)) return;
		
		meeting = bigBlueButtonManager.getMeeting(meeting);
		List<BigBlueButtonTemplatePermissions> permissions = bigBlueButtonManager
				.calculatePermissions(entry, businessGroup, getIdentity(), ureq.getUserSession().getRoles());
		editMeetingCtlr = new EditBigBlueButtonMeetingController(ureq, getWindowControl(),
				meeting, permissions);
		listenTo(editMeetingCtlr);
		
		cmc = new CloseableModalController(getWindowControl(), "close", editMeetingCtlr.getInitialComponent(),
				true, translate("add.meeting"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doConfirmDelete(UserRequest ureq, BigBlueButtonMeeting meeting) {
		String confirmDeleteTitle = translate("confirm.delete.meeting.title", new String[]{ meeting.getName() });
		String confirmDeleteText = translate("confirm.delete.meeting", new String[]{ meeting.getName() });
		confirmDelete = activateYesNoDialog(ureq, confirmDeleteTitle, confirmDeleteText, confirmDelete);
		confirmDelete.setUserObject(meeting);
	}
	
	private void doConfirmDelete(UserRequest ureq, List<BigBlueButtonMeeting> meetings) {
		if(meetings.isEmpty()) {
			showWarning("warning.at.least.one.meeting");
		} else if(meetings.size() == 1) {
			doConfirmDelete(ureq, meetings.get(0));
		} else {
			Set<String> names = new HashSet<>();
			StringBuilder namesBuilder = new StringBuilder(128);
			for(BigBlueButtonMeeting meeting:meetings) {
				if(names.contains(meeting.getName())) {
					continue;
				}
				
				if(namesBuilder.length() > 0) namesBuilder.append(", ");
				namesBuilder.append(StringHelper.escapeHtml(meeting.getName()));
				names.add(meeting.getName());
			}

			String confirmDeleteTitle = translate("confirm.delete.meetings.title", new String[]{ Integer.toString(meetings.size()) });
			String confirmDeleteText = translate("confirm.delete.meetings", new String[]{ Integer.toString(meetings.size()), namesBuilder.toString() });
			confirmBatchDelete = activateYesNoDialog(ureq, confirmDeleteTitle, confirmDeleteText, confirmBatchDelete);
			confirmBatchDelete.setUserObject(meetings);
		}
	}
	
	private void doDelete(List<BigBlueButtonMeeting> meetings) {
		BigBlueButtonErrors errors = new BigBlueButtonErrors();
		for(BigBlueButtonMeeting meeting:meetings) {
			bigBlueButtonManager.deleteMeeting(meeting, errors);
		}
		updateModel();
		if(errors.hasErrors()) {
			getWindowControl().setError(BigBlueButtonErrorHelper.formatErrors(getTranslator(), errors));
		} else {
			showInfo("meeting.deleted");
		}
	}
	
	private void doDelete(BigBlueButtonMeeting meeting) {
		BigBlueButtonErrors errors = new BigBlueButtonErrors();
		bigBlueButtonManager.deleteMeeting(meeting, errors);
		updateModel();
		if(errors.hasErrors()) {
			getWindowControl().setError(BigBlueButtonErrorHelper.formatErrors(getTranslator(), errors));
		} else {
			showInfo("meeting.deleted");
		}
	}
}
