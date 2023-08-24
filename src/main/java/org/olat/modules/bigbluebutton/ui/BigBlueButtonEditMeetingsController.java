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
import org.olat.core.gui.components.Component;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
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
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private DialogBoxController confirmDelete;
	private DialogBoxController confirmBatchDelete;
	private StepsMainRunController addDailyMeetingCtrl;
	private StepsMainRunController addWeeklyMeetingCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private EditBigBlueButtonMeetingController editMeetingCtlr;

	
	private int count = 0;
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
		if (bigBlueButtonModule.getMeetingDeletionDays() != null) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(BMeetingsCols.autoDelete, new DateFlexiCellRenderer(getLocale())));
		}
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

			StickyActionColumnModel toolsCol = new StickyActionColumnModel(BMeetingsCols.tools);
			toolsCol.setIconHeader("o_icon o_icon_actions o_icon-fws o_icon-lg");
			toolsCol.setExportable(false);
			toolsCol.setAlwaysVisible(true);
			columnsModel.addFlexiColumnModel(toolsCol);
		}
		
		tableModel = new BigBlueButtonMeetingTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "meetings", tableModel, getTranslator(), formLayout);
		tableEl.setEmptyTableSettings("no.meeting.configured", null, "o_icon_calendar");
		
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
	
	public void updateModel() {
		List<BigBlueButtonMeeting> meetings = bigBlueButtonManager.getMeetings(entry, subIdent, businessGroup, false);
		List<BigBlueButtonMeetingRow> rows = meetings.stream()
				.map(this::forgeRow)
				.collect(Collectors.toList());
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);	
	}
	
	private BigBlueButtonMeetingRow forgeRow(BigBlueButtonMeeting meeting) {
		BigBlueButtonMeetingRow row = new BigBlueButtonMeetingRow(meeting);
		row.setAutoDeleteDate(bigBlueButtonManager.getAutoDeletionDate(meeting));
		
		FormLink toolsLink = uifactory.addFormLink("tools_" + count++, "tools", "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
		toolsLink.setUserObject(row);
		row.setToolsLink(toolsLink);
		return row;
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
		} else if(toolsCtrl == source) {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		} else if(cmc == source || toolsCalloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(addWeeklyMeetingCtrl);
		removeAsListenerAndDispose(addDailyMeetingCtrl);
		removeAsListenerAndDispose(confirmBatchDelete);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(editMeetingCtlr);
		removeAsListenerAndDispose(confirmDelete);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		addWeeklyMeetingCtrl = null;
		addDailyMeetingCtrl = null;
		confirmBatchDelete = null;
		toolsCalloutCtrl = null;
		editMeetingCtlr = null;
		confirmDelete = null;
		toolsCtrl = null;
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
					doEditMeeting(ureq, tableModel.getMeeting(se.getIndex()));
				} else if("delete".equals(se.getCommand())) {
					doConfirmDelete(ureq, tableModel.getMeeting(se.getIndex()));
				}
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("tools".equals(link.getCmd()) && link.getUserObject() instanceof BigBlueButtonMeetingRow) {
				doOpenTools(ureq, (BigBlueButtonMeetingRow)link.getUserObject(), link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private List<BigBlueButtonMeeting> getSelectedMeetings() {
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		return selectedIndex.stream()
				.map(index -> tableModel.getMeeting(index.intValue()))
				.collect(Collectors.toList());
	}
	

	private void doAddSingleMeeting(UserRequest ureq) {
		if(guardModalController(editMeetingCtlr)) return;

		List<BigBlueButtonTemplatePermissions> permissions = bigBlueButtonManager
				.calculatePermissions(entry, businessGroup, getIdentity(), ureq.getUserSession().getRoles());
		editMeetingCtlr = new EditBigBlueButtonMeetingController(ureq, getWindowControl(),
				entry, subIdent, businessGroup, permissions, Mode.dates);
		listenTo(editMeetingCtlr);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editMeetingCtlr.getInitialComponent(),
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
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editMeetingCtlr.getInitialComponent(),
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
		boolean generateUrl = context.getTemplate() != null && context.getTemplate().isExternalUsersAllowed() && context.isGenerateUrl();
		boolean setPassword = generateUrl && StringHelper.containsNonWhitespace(context.getPassword());
				
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
			bMeeting.setRecord(context.getRecord());
			bMeeting.setMeetingLayout(context.getMeetingLayout());
			bMeeting.setJoinPolicyEnum(context.getJoinPolicy());
			bMeeting.setRecordingsPublishingEnum(context.getRecordingsPublishing());
			if(generateUrl) {
				String externalLink = String.valueOf(CodeHelper.getForeverUniqueID());
				bMeeting.setReadableIdentifier(externalLink);
				if(setPassword) {
					bMeeting.setPassword(context.getPassword());
				}
			}
			
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
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editMeetingCtlr.getInitialComponent(),
				true, translate("edit.meeting"));
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
	
	private void doCopy(UserRequest ureq, BigBlueButtonMeeting meeting) {
		String newName = translate("copy.name", new String[] { meeting.getName() });
		BigBlueButtonMeeting copiedMeeting = bigBlueButtonManager.copyMeeting(newName, meeting, getIdentity());

		List<BigBlueButtonTemplatePermissions> permissions = bigBlueButtonManager
				.calculatePermissions(entry, businessGroup, getIdentity(), ureq.getUserSession().getRoles());
		editMeetingCtlr = new EditBigBlueButtonMeetingController(ureq, getWindowControl(),
				copiedMeeting, permissions);
		editMeetingCtlr.validateFormLogic(ureq);
		
		// slides
		if(StringHelper.containsNonWhitespace(meeting.getDirectory())) {
			VFSContainer slides = bigBlueButtonManager.getSlidesContainer(meeting);
			if(slides != null && slides.exists()) {
				editMeetingCtlr.copySlides(slides);
			}	
		}

		listenTo(editMeetingCtlr);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editMeetingCtlr.getInitialComponent(),
				true, translate("edit.meeting"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doOpenTools(UserRequest ureq, BigBlueButtonMeetingRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		BigBlueButtonMeeting meeting = bigBlueButtonManager.getMeeting(row.getMeeting());
		if(meeting == null) {
			updateModel();
			showWarning("warning.no.meeting");
		} else {
			toolsCtrl = new ToolsController(ureq, getWindowControl(), meeting);
			listenTo(toolsCtrl);
	
			toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
			listenTo(toolsCalloutCtrl);
			toolsCalloutCtrl.activate();
		}
	}

	private class ToolsController extends BasicController {

		private final Link copyLink;
		private final Link deleteLink;
		private final VelocityContainer mainVC;

		private final BigBlueButtonMeeting meeting;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, BigBlueButtonMeeting meeting) {
			super(ureq, wControl);
			this.meeting = meeting;
			mainVC = createVelocityContainer("tools");
			
			copyLink = LinkFactory.createLink("copy", "copy", getTranslator(), mainVC, this, Link.LINK);
			copyLink.setIconLeftCSS("o_icon o_icon-fw o_icon_copy");
			
			deleteLink = LinkFactory.createLink("delete", "delete", getTranslator(), mainVC, this, Link.LINK);
			deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
			
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(copyLink == source) {
				doCopy(ureq, meeting);
			} else if(deleteLink == source) {
				doConfirmDelete(ureq, meeting);
			}
		}
	}
}
