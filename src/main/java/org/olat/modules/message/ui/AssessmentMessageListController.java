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
package org.olat.modules.message.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateTimeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.coordinate.Coordinator;
import org.olat.core.util.event.GenericEventListener;
import org.olat.modules.message.AssessmentMessage;
import org.olat.modules.message.AssessmentMessageService;
import org.olat.modules.message.AssessmentMessageStatusEnum;
import org.olat.modules.message.model.AssessmentMessageInfos;
import org.olat.modules.message.ui.AssessmentMessageListDataModel.MessagesCols;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentMessageListController extends FormBasicController implements GenericEventListener {
	
	private FormLink bulkDeleteButton;
	private FormLink addMessageButton;
	private FlexiTableElement tableEl;
	private AssessmentMessageListDataModel tableModel;
	
	private final boolean admin;
	private final String resSubPath;
	private final RepositoryEntry entry;
	private final OLATResourceable messageOres;
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private AssessmentMessageEditController editCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	private ConfirmDeleteMessagesController confirmDeleteCtrl;
	
	@Autowired
	private Coordinator coordinator;
	@Autowired
	private UserManager userManager;
	@Autowired
	private AssessmentMessageService assessmentMessageService;
	
	public AssessmentMessageListController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, String resSubPath, boolean admin) {
		super(ureq, wControl, "messages_list");
		this.admin = admin;
		this.entry = entry;
		this.resSubPath = resSubPath;
		
		initForm(ureq);
		loadModel(ureq);
		
		messageOres = assessmentMessageService.getEventResourceable(entry, resSubPath);
		coordinator.getEventBus()
			.registerFor(this, getIdentity(), messageOres);
	}
	
	@Override
	protected void doDispose() {
		coordinator.getEventBus()
			.deregisterFor(this, messageOres);
		super.doDispose();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addMessageButton = uifactory.addFormLink("add.message", formLayout, Link.BUTTON);
		addMessageButton.setElementCssClass("o_sel_add_message");
		addMessageButton.setIconLeftCSS("o_icon o_icon_add");
		bulkDeleteButton = uifactory.addFormLink("delete", formLayout, Link.BUTTON);

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MessagesCols.message));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MessagesCols.status,
				new AssessmentMessageStatusCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MessagesCols.creationDate,
				new DateTimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MessagesCols.author));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MessagesCols.publicationDate,
				new DateTimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, MessagesCols.expirationDate,
				new DateTimeFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MessagesCols.read));
		
		StickyActionColumnModel toolsCol = new StickyActionColumnModel(MessagesCols.tools.i18nHeaderKey(), MessagesCols.tools.ordinal());
		toolsCol.setIconHeader("o_icon o_icon_actions o_icon-fws o_icon-lg");
		columnsModel.addFlexiColumnModel(toolsCol);
		
		tableModel = new AssessmentMessageListDataModel(columnsModel, getLocale());

		tableEl = uifactory.addTableElement(getWindowControl(), "messages", tableModel, 24, false, getTranslator(), formLayout);
		tableEl.addBatchButton(bulkDeleteButton);
		tableEl.setEmptyTableSettings("table.nomessage", null, "o_icon_chat");
		tableEl.setElementCssClass("o_as_messages_list");
		tableEl.setSelectAllEnable(true);
		tableEl.setMultiSelect(true);
		initFiltersPresets(ureq);
		tableEl.setAndLoadPersistedPreferences(ureq, "assessment-messages-list");
	}
	
	private final void initFiltersPresets(UserRequest ureq) {
		// All
		FlexiFiltersTab allTab = FlexiFiltersTabFactory.tabWithImplicitFilters("All", translate("filter.all"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf("filter", "my")));
		// Planned
		FlexiFiltersTab plannedTab = FlexiFiltersTabFactory.tabWithImplicitFilters("Planned", translate("filter.planned"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf("filter", "planned")));
		// Published
		FlexiFiltersTab publishedTab = FlexiFiltersTabFactory.tabWithImplicitFilters("Published", translate("filter.published"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf("filter", "published")));
		// Expired
		FlexiFiltersTab expiredTab = FlexiFiltersTabFactory.tabWithImplicitFilters("Expired", translate("filter.expired"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf("filter", "expired")));

		tableEl.setFilterTabs(true, List.of(allTab, plannedTab, publishedTab, expiredTab));
		tableEl.setSelectedFilterTab(ureq, allTab);
	}
	
	private void loadModel(UserRequest ureq, Long messageKey) {
		AssessmentMessageRow row = tableModel.getMessageByKey(messageKey);
		if(row == null) {
			loadModel(ureq);
		} else {
			AssessmentMessageInfos messageInfos = assessmentMessageService.getMessageInfos(messageKey);
			if(messageInfos == null) {
				loadModel(ureq);
			} else {
				AssessmentMessageStatusEnum status = AssessmentMessageStatusEnum
					.valueOf(messageInfos.getMessage(), ureq.getRequestTimestamp());
				row.updateInfos(messageInfos, status);
				tableEl.reset(false, false, true);
			}
		}
	}
	
	private void loadModel(UserRequest ureq) {
		List<AssessmentMessageInfos> messages = assessmentMessageService.getMessagesInfos(entry, resSubPath);
		List<AssessmentMessageRow> rows = new ArrayList<>();
		for(AssessmentMessageInfos message:messages) {
			String authorFullName = userManager.getUserDisplayName(message.getMessage().getAuthor());
			AssessmentMessageStatusEnum status = AssessmentMessageStatusEnum
					.valueOf(message.getMessage(), ureq.getRequestTimestamp());
			AssessmentMessageRow row = new AssessmentMessageRow(message, authorFullName, status);
			forgeRow(row, message);
			rows.add(row);
		}
		
		tableModel.setObjects(rows);
		tableModel.filter(tableEl.getSelectedFilterTab());
		tableEl.reset(true, true, true);
	}
	
	private void forgeRow(AssessmentMessageRow row, AssessmentMessageInfos message) {
		if(!admin && !getIdentity().getKey().equals(message.getMessage().getAuthor().getKey())) {
			return;
		}
		
		String toolId = "tool_" + message.getMessage().getKey();
		FormLink toolLink = (FormLink)flc.getFormComponent(toolId);
		if(toolLink == null) {
			toolLink = uifactory.addFormLink(toolId, "tools", "", tableEl, Link.LINK | Link.NONTRANSLATED);
			toolLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
			toolLink.setTitle(translate("table.header.actions"));
		}
		toolLink.setUserObject(row);
		row.setToolLink(toolLink);
	}

	@Override
	public void event(Event event) {
		if(event instanceof AssessmentMessageEvent) {
			processAssessmentMessageEvent((AssessmentMessageEvent)event);
		}
	}
	
	private void processAssessmentMessageEvent(AssessmentMessageEvent event) {
		if(!Objects.equals(entry.getKey(), event.getRepositoryEntryKey()) || !Objects.equals(resSubPath, event.getResSubPath())
				|| Objects.equals(getIdentity().getKey(), event.getEmitter())) {
			return;
		}
		
		if(AssessmentMessageEvent.READ.equals(event.getCommand())
				|| AssessmentMessageEvent.PUBLISHED.equals(event.getCommand())) {
			loadModel(new SyntheticUserRequest(getIdentity(), getLocale()), event.getMessageKey());
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editCtrl == source || confirmDeleteCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if(toolsCtrl == source) {
			if(calloutCtrl != null) {
				calloutCtrl.deactivate();
			}
			cleanUp();
		} else if(cmc == source || calloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmDeleteCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(editCtrl);
		removeAsListenerAndDispose(cmc);
		confirmDeleteCtrl = null;
		calloutCtrl = null;
		toolsCtrl = null;
		editCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof FlexiTableFilterTabEvent) {
				doSelectedTab(((FlexiTableFilterTabEvent)event).getTab());
			}
		} else if(addMessageButton == source) {
			doNewMessage(ureq);
		} else if(bulkDeleteButton == source) {
			doDeleteMessages(ureq, getSelectedRows());
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("tools".equals(link.getCmd()) && link.getUserObject() instanceof AssessmentMessageRow) {
				doOpenTools(ureq, link, (AssessmentMessageRow)link.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private List<AssessmentMessageRow> getSelectedRows() {
		return tableEl.getMultiSelectedIndex().stream()
				.map(index -> tableModel.getObject(index.intValue()))
				.collect(Collectors.toList());
	}
	
	private void doSelectedTab(FlexiFiltersTab tab) {
		tableModel.filter(tab);
		tableEl.reset(true, true, true);
	}
	
	private void doNewMessage(UserRequest ureq) {
		if(guardModalController(editCtrl)) return;
		
		editCtrl = new AssessmentMessageEditController(ureq, getWindowControl(), entry, resSubPath);
		listenTo(editCtrl);
		
		String title = translate("add.message.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();	
	}
	
	private void doEditMessage(UserRequest ureq, AssessmentMessageRow row) {
		if(guardModalController(editCtrl)) return;
		
		AssessmentMessage message = assessmentMessageService.getAssessmentMessage(row.getKey());
		if(message == null) {
			loadModel(ureq);
			return;
		}
		
		editCtrl = new AssessmentMessageEditController(ureq, getWindowControl(), message);
		listenTo(editCtrl);
		
		String title = translate("add.message.title");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();	
	}

	private void doDeleteMessages(UserRequest ureq, List<AssessmentMessageRow> rows) {
		if(guardModalController(editCtrl)) return;
		
		List<AssessmentMessage> messages = rows.stream()
				.map(AssessmentMessageRow::getMessage)
				.collect(Collectors.toList());
		confirmDeleteCtrl = new ConfirmDeleteMessagesController(ureq, getWindowControl(), messages);
		listenTo(confirmDeleteCtrl);
		
		String title = rows.size() <= 1 ? "confirm.delete.title" : "confirm.delete.title.plural";
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmDeleteCtrl.getInitialComponent(),
				true, translate(title));
		listenTo(cmc);
		cmc.activate();	
	}
	
	private void doOpenTools(UserRequest ureq, FormLink link, AssessmentMessageRow row) {
		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);
	
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private class ToolsController extends BasicController {

		private Link editLink;
		private Link deleteLink;
		private final VelocityContainer mainVC;
		
		private final AssessmentMessageRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, AssessmentMessageRow row) {
			super(ureq, wControl);
			this.row = row;
			
			mainVC = createVelocityContainer("tools");
			editLink = LinkFactory.createLink("edit", "edit", getTranslator(), mainVC, this, Link.LINK);
			editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
			mainVC.put("tool.edit", editLink);
			deleteLink = LinkFactory.createLink("delete", "delete", getTranslator(), mainVC, this, Link.LINK);
			deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
			mainVC.put("tool.delete", deleteLink);

			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			if(editLink == source) {
				doEditMessage(ureq, row);
			} else if(deleteLink == source) {
				doDeleteMessages(ureq, List.of(row));
			}
		}
	}
}
