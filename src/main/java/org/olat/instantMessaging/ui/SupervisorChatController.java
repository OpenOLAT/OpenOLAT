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
package org.olat.instantMessaging.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.commons.persistence.DB;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
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
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.SignOnOffEvent;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.Coordinator;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.session.UserSessionManager;
import org.olat.instantMessaging.InstantMessageTypeEnum;
import org.olat.instantMessaging.InstantMessagingEvent;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.OpenInstantMessageEvent;
import org.olat.instantMessaging.RosterEntry;
import org.olat.instantMessaging.manager.ChatLogHelper;
import org.olat.instantMessaging.model.Presence;
import org.olat.instantMessaging.model.RosterChannelInfos;
import org.olat.instantMessaging.model.RosterChannelInfos.RosterStatus;
import org.olat.instantMessaging.ui.SupervisorChatDataModel.SupervisedChatCols;
import org.olat.instantMessaging.ui.component.LastActivityCellRenderer;
import org.olat.instantMessaging.ui.component.LastMessageCellRenderer;
import org.olat.instantMessaging.ui.component.RosterEntryStatusCellRenderer;
import org.olat.instantMessaging.ui.component.RosterEntryWithUnreadCellRenderer;
import org.olat.instantMessaging.ui.component.RosterStatusCellRenderer;
import org.olat.instantMessaging.ui.component.UserAvatarCellRenderer;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserAvatarMapper;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * 
 * 
 * Initial date: 21 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SupervisorChatController extends FormBasicController implements GenericEventListener {
	
	private FormLink bulkSendMessageButton;
	private FormLink bulkCompleteButton;
	protected FlexiTableElement tableEl;
	protected SupervisorChatDataModel tableModel;
	
	private final String fromMe;
	protected RepositoryEntry entry;
	private final OLATResourceable chatResource;
	protected final String resSubPath;
	private final MapperKey avatarMapperKey;
	private final ChatViewConfig basisViewConfig;
	private final OLATResourceable personalEventOres;
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private SendMessageController sendMessageCtrl;
	private DialogBoxController confirmCompleteDialog;
	private CloseableCalloutWindowController calloutCtrl;

	@Autowired
	private DB dbInstance;
	@Autowired
	private ChatLogHelper chatLog;
	@Autowired
	private Coordinator coordinator;
	@Autowired
	private UserManager userManager;
	@Autowired
	private MapperService mapperService;
	@Autowired
	private InstantMessagingService imService;
	@Autowired
	private UserSessionManager sessionManager;
	
	public SupervisorChatController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, String resSubPath,
			ChatViewConfig basisViewConfig) {
		this(ureq, wControl, entry, null, resSubPath, basisViewConfig);
	}
	
	public SupervisorChatController(UserRequest ureq, WindowControl wControl, OLATResourceable chatResource, String resSubPath,
			ChatViewConfig basisViewConfig) {
		this(ureq, wControl, null, chatResource, resSubPath, basisViewConfig);
	}
	
	private SupervisorChatController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, OLATResourceable chatOres, String resSubPath,
			ChatViewConfig basisViewConfig) {
		super(ureq, wControl, "supervised_chat");
		setTranslator(Util.createPackageTranslator(SupervisorChatController.class, getLocale(), getTranslator()));
		this.entry = entry;
		this.chatResource = chatOres == null ? entry.getOlatResource() : chatOres;
		this.resSubPath = resSubPath;
		this.basisViewConfig = basisViewConfig;
		fromMe = userManager.getUserDisplayName(getIdentity());
		avatarMapperKey = mapperService.register(null, "avatars-members", new UserAvatarMapper(false));
		
		coordinator.getEventBus().registerFor(this, getIdentity(), this.chatResource);
		personalEventOres = OresHelper.createOLATResourceableInstance(InstantMessagingService.PERSONAL_EVENT_ORES_NAME, getIdentity().getKey());
		coordinator.getEventBus().registerFor(this, getIdentity(), personalEventOres);
		coordinator.getEventBus().registerFor(this, getIdentity(), UserSessionManager.ORES_USERSESSION);

		initForm(ureq);
		loadModel(true);
	}

	@Override
	protected void doDispose() {
		coordinator.getEventBus().deregisterFor(this, chatResource);
		coordinator.getEventBus().deregisterFor(this, personalEventOres);
		coordinator.getEventBus().deregisterFor(this, UserSessionManager.ORES_USERSESSION);
		super.doDispose();
	}

	public void add(Identity identity, String channel) {
		String fullName = userManager.getUserDisplayName(identity);
		imService.addToRoster(identity, chatResource, resSubPath, channel, fullName, false, false);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SupervisedChatCols.portrait,
				new UserAvatarCellRenderer(avatarMapperKey.getUrl())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SupervisedChatCols.online,
				new RosterEntryStatusCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SupervisedChatCols.participant,
				new RosterEntryWithUnreadCellRenderer(false)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SupervisedChatCols.lastMessage,
				new LastMessageCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SupervisedChatCols.supervisor));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SupervisedChatCols.status,
				new RosterStatusCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SupervisedChatCols.lastActivity,
				new LastActivityCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SupervisedChatCols.join));
		
		StickyActionColumnModel toolsCol = new StickyActionColumnModel(SupervisedChatCols.tools.i18nHeaderKey(), SupervisedChatCols.tools.ordinal());
		toolsCol.setIconHeader("o_icon o_icon_actions o_icon-fws o_icon-lg");
		columnsModel.addFlexiColumnModel(toolsCol);
		
		tableModel = new SupervisorChatDataModel(columnsModel, getIdentity(), getTranslator());
		
		tableEl = uifactory.addTableElement(getWindowControl(), "chats", tableModel, 24, false, getTranslator(), formLayout);
		tableEl.setEmptyTableSettings("table.nochats", null, "o_icon_chat");
		tableEl.setElementCssClass("o_im_supervised_list");
		tableEl.setSelectAllEnable(true);
		tableEl.setMultiSelect(true);
		tableEl.setCssDelegate(tableModel);
		initFiltersPresets(ureq);
		tableEl.setAndLoadPersistedPreferences(ureq, "supervisor-chat");
		
		bulkSendMessageButton = uifactory.addFormLink("bulk.send.message", formLayout, Link.BUTTON);
		bulkCompleteButton = uifactory.addFormLink("bulk.complete", formLayout, Link.BUTTON);
		tableEl.addBatchButton(bulkSendMessageButton);
		tableEl.addBatchButton(bulkCompleteButton);
	}

	private final void initFiltersPresets(UserRequest ureq) {
		// all
		FlexiFiltersTab allTab = FlexiFiltersTabFactory.tabWithImplicitFilters("All", translate("filter.all"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf("filter", "my")));
		// zu bearbeiten
		FlexiFiltersTab requestedTab = FlexiFiltersTabFactory.tabWithImplicitFilters("Requested", translate("filter.requested"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf("filter", "requested")));
		// my chats
		FlexiFiltersTab myTab = FlexiFiltersTabFactory.tabWithImplicitFilters("My", translate("filter.my"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf("filter", "my")));
		// active
		FlexiFiltersTab activeTab = FlexiFiltersTabFactory.tabWithImplicitFilters("Active", translate("filter.active"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf("filter", "active")));
		// completed
		FlexiFiltersTab completedTab = FlexiFiltersTabFactory.tabWithImplicitFilters("Completed", translate("filter.completed"),
				TabSelectionBehavior.reloadData, List.of(FlexiTableFilterValue.valueOf("filter", "completed")));
		
		tableEl.setFilterTabs(true, List.of(allTab, requestedTab, myTab, activeTab, completedTab));
		tableEl.setSelectedFilterTab(ureq, allTab);
	}
	
	public void reloadModel() {
		loadModel(false);
	}
	
	protected RosterRow reloadModel(RosterRow row) {
		RosterChannelInfos infos = imService.getRoster(chatResource, resSubPath, row.getChannel(), getIdentity());	
		RosterRow currentRow = tableModel.getObjectByChannel(row.getChannel());
		if(currentRow != null) {
			currentRow.setRoster(infos);
			forgeRow(currentRow, infos);
			tableEl.reset(false, false, true);
			updateLastActivity();
		} else {
			loadModel(false);
			currentRow = tableModel.getObjectByChannel(row.getChannel());
		}
		return currentRow;
	}
	
	protected List<RosterRow> loadModel(boolean reset) {
		List<RosterChannelInfos> rosterInfos = imService.getRosters(chatResource, resSubPath, getIdentity(), false);
		List<RosterRow> rows = new ArrayList<>(rosterInfos.size());
		for(RosterChannelInfos roster:rosterInfos) {
			RosterRow row = new RosterRow(roster);
			forgeRow(row, roster);
			rows.add(row);
		}
		tableModel.setObjects(rows);
		tableEl.reset(reset, reset, true);
		updateLastActivity();
		return rows;
	}
	
	private void updateLastActivity() {
		Date lastActivity = tableModel.getLastActivity();
		if(lastActivity == null) {
			flc.contextRemove("lastActivity");
		} else {
			long delay = SupervisorChatDataModel.FIVE_MINUTES_MS - (new Date().getTime() - lastActivity.getTime());
			if(delay > 0) {
				flc.contextPut("lastActivity", Long.valueOf(delay + 5));
			} else {
				flc.contextRemove("lastActivity");
			}
		}
	}
	
	private void forgeRow(RosterRow row, RosterChannelInfos roster) {
		row.setOnlineStatus(getOnlineStatus(row));
		
		List<Long> supervisors = getSupervisorsIdentities(row);
		String joinI18nKey;
		if(supervisors.contains(getIdentity().getKey())) {
			joinI18nKey = "im.chat.open";
		} else {
			joinI18nKey = "im.chat.join";
		}
		String joinId = "join_".concat(roster.getChannel());
		FormLink joinLink = (FormLink)tableEl.getFormComponent(joinId);
		if(joinLink == null) {
			joinLink = uifactory.addFormLink(joinId, "im", joinI18nKey, tableEl, Link.LINK);
			joinLink.setDomReplacementWrapperRequired(false);
		} else {
			joinLink.setTranslator(getTranslator());
			joinLink.setI18nKey(joinI18nKey);
		}
		joinLink.setUserObject(row);
		joinLink.setRootForm(mainForm);
		row.setJoinLink(joinLink);

		String toolId = "tool_".concat(roster.getChannel());
		FormLink toolLink = (FormLink)flc.getFormComponent(toolId);
		if(toolLink == null) {
			toolLink = uifactory.addFormLink(toolId, "tools", "", tableEl, Link.LINK | Link.NONTRANSLATED);
			toolLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
			toolLink.setTitle(translate("table.header.actions"));
		}
		toolLink.setUserObject(row);
		row.setToolLink(toolLink);
	}
	
	private String getOnlineStatus(RosterRow row) {
		List<Long> identityKeys = getSupervisedIdentities(row);
		if(identityKeys.size() == 1 &&  identityKeys.contains(getIdentity().getKey())) {
			return "me";
		} else if(identityKeys.isEmpty()) {
			return Presence.unavailable.name();
		} else {
			Presence presence = getPresence(identityKeys);
			return presence.name();
		}
	}
	
	private Presence getPresence(List<Long> identityKeys) {
		for(Long identityKey:identityKeys) {
			if(sessionManager.isOnline(identityKey)) {
				return Presence.available;
			}
		}
		return Presence.unavailable;
	}
	
	private List<Long> getSupervisorsIdentities(RosterRow row) {
		List<RosterEntry> entries = row.getRoster().getEntries();
		return entries.stream()
			.filter(RosterEntry::isVip)
			.map(RosterEntry::getIdentityKey)
			.collect(Collectors.toList());
	}
	
	private List<Long> getSupervisedIdentities(RosterRow row) {
		List<RosterEntry> entries = row.getRoster().getEntries();
		return entries.stream()
			.filter(entry -> !entry.isVip())
			.map(RosterEntry::getIdentityKey)
			.collect(Collectors.toList());
	}

	@Override
	public void event(Event event) {
		if(event instanceof InstantMessagingEvent) {
			InstantMessagingEvent ime = (InstantMessagingEvent)event;
			if(resSubPath.equals(ime.getResSubPath())
					&& StringHelper.containsNonWhitespace(ime.getChannel())) {
				processInstantMessagingEvent(ime);
			}
		} else if(event instanceof SignOnOffEvent) {
			SignOnOffEvent sooe = (SignOnOffEvent)event;
			if(tableModel.isInRoster(sooe.getIdentityKey())) {
				processUserSessionEvent(sooe);
			}
		}
	}
	
	private void processInstantMessagingEvent(InstantMessagingEvent event) {
		RosterRow currentRow = tableModel.getObjectByChannel(event.getChannel());
		if(currentRow == null) {
			reloadModel();
		} else {
			String command = event.getCommand();
			if(InstantMessagingEvent.END_CHANNEL.equals(command)
					&& (currentRow.getRosterStatus() == RosterStatus.active || currentRow.getRosterStatus() == RosterStatus.request)) {
				reloadModel(currentRow);
			} else if(InstantMessagingEvent.REQUEST.equals(command)
					&& currentRow.getRosterStatus() != RosterStatus.request) {
				reloadModel(currentRow);
			} else if(InstantMessagingEvent.MESSAGE.equals(command)
					&& currentRow.inRoster(getIdentity())
					&& (currentRow.getRosterStatus() == RosterStatus.active || currentRow.getRosterStatus() == RosterStatus.request)) {
				reloadModel(currentRow);
			}
		}
	}
	
	private void processUserSessionEvent(SignOnOffEvent event) {
		RosterRow currentRow = tableModel.getObjectByChannel(event.getIdentityKey().toString());
		if(currentRow != null) {
			String status = getOnlineStatus(currentRow);
			if(!status.equals(currentRow.getOnlineStatus())) {
				currentRow.setOnlineStatus(status);
				tableEl.reset(false, false, true);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(toolsCtrl == source) {
			if(calloutCtrl != null) {
				calloutCtrl.deactivate();
			}
			cleanUp();
		} else if(confirmCompleteDialog == source) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				@SuppressWarnings("unchecked")
				List<RosterRow> toComplete = (List<RosterRow>)confirmCompleteDialog.getUserObject();
				doClose(toComplete);
			}
		} else if(sendMessageCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel(false);
			}
			cmc.deactivate();
			cleanUp();
		} else if(calloutCtrl == source || cmc == source) {
			cleanUp();
		} 
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		calloutCtrl = null;
		toolsCtrl = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof FlexiTableFilterTabEvent) {
				doSelectedTab(((FlexiTableFilterTabEvent)event).getTab());
			}
		} else if(bulkCompleteButton == source) {
			doConfirmComplete(ureq);
		} else if(bulkSendMessageButton == source) {
			doSendMessages(ureq);
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("im".equals(link.getCmd()) && link.getUserObject() instanceof RosterRow) {
				doJoin(ureq, (RosterRow)link.getUserObject());
			} else if("tools".equals(link.getCmd()) && link.getUserObject() instanceof RosterRow) {
				doOpenTools(ureq, link, (RosterRow)link.getUserObject());
			}
		} else if(source == flc && "activity".equals(ureq.getParameter("last"))) {
			loadModel(false);
			flc.setDirty(true);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doConfirmComplete(UserRequest ureq) {
		List<RosterRow> rows = getSelectedRows();
		if(rows.isEmpty()) {
			showWarning("warning.selectatleastonerow");
			return;
		}
		
		boolean singular = rows.size() <= 1;
		String i18nTitle = singular ? "confirm.complete.title.singular" : "confirm.complete.title";
		String i18nText = singular ? "confirm.complete.text.singular" : "confirm.complete.text";
		String[] args = { Integer.toString(rows.size()) };
		String title = translate(i18nTitle, args);
		String text = translate(i18nText, args);
		confirmCompleteDialog = activateYesNoDialog(ureq, title, text, confirmCompleteDialog);
		confirmCompleteDialog.setUserObject(rows);
	}
	
	private void doClose(List<RosterRow> rows) {
		for(RosterRow row:rows) {
			imService.sendStatusMessage(getIdentity(), fromMe, false, InstantMessageTypeEnum.close,
					chatResource, resSubPath, row.getChannel());
		}
		loadModel(false);
	}
	
	private void doSendMessages(UserRequest ureq) {
		List<RosterRow> rows = getSelectedRows();
		if(rows.isEmpty()) {
			showWarning("warning.selectatleastonerow");
			return;
		}
		
		sendMessageCtrl = new SendMessageController(ureq, getWindowControl(), rows, fromMe);
		listenTo(sendMessageCtrl);
		String title = translate("send.message.title");
		cmc = new CloseableModalController(getWindowControl(), "close", sendMessageCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private List<RosterRow> getSelectedRows() {
		return tableEl.getMultiSelectedIndex().stream()
				.map(index -> tableModel.getObject(index.intValue()))
				.collect(Collectors.toList());
	}
	
	private void doSelectedTab(FlexiFiltersTab tab) {
		tableModel.filter(tab);
		tableEl.reset(true, true, true);
	}
	
	private void doJoin(UserRequest ureq, RosterRow row) {
		String channel = row.getRoster().getChannel();
		imService.addToRoster(getIdentity(), chatResource, resSubPath, channel, fromMe, false, true);
		boolean joinRoster = !row.inRoster(getIdentity());
		if(joinRoster) {
			imService.sendStatusMessage(getIdentity(), fromMe, false, InstantMessageTypeEnum.join,
					chatResource, resSubPath, row.getChannel());
		} else {
			imService.sendPresence(getIdentity(), chatResource, resSubPath, row.getChannel(), fromMe, false, true, true);
		}
		imService.deleteNotifications(chatResource, resSubPath, channel);
		dbInstance.commit();
		
		if(joinRoster) {
			reloadModel(row);
		}
		
		ChatViewConfig viewConfig = ChatViewConfig.valueOf(basisViewConfig);
		viewConfig.setRosterDisplay(RosterFormDisplay.supervisor);
		OpenInstantMessageEvent event = new OpenInstantMessageEvent(chatResource, resSubPath, channel, viewConfig, true, false);
		ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(event, InstantMessagingService.TOWER_EVENT_ORES);
	}
	
	private void doOpenTools(UserRequest ureq, FormLink link, RosterRow row) {
		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);
	
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doActivate(RosterRow row) {
		imService.sendStatusMessage(getIdentity(), fromMe, false, InstantMessageTypeEnum.accept,
				chatResource, resSubPath, row.getChannel());
		loadModel(true);
	}
	
	private void doExportLog(UserRequest ureq, RosterRow row) {
		MediaResource log = chatLog.logMediaResource(chatResource, resSubPath, row.getChannel(), getLocale());
		ureq.getDispatchResult().setResultingMediaResource(log);
	}
	
	private class ToolsController extends BasicController {

		private Link activateLink;
		private Link closeLink;
		private Link exportLogLink;
		private final RosterRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, RosterRow row) {
			super(ureq, wControl);
			this.row = row;
			
			VelocityContainer mainVC = createVelocityContainer("tools");
			
			RosterChannelInfos lastInfos = imService.getRoster(chatResource, resSubPath, row.getChannel(), getIdentity());	
			if(lastInfos.getRosterStatus() == RosterStatus.completed) {
				activateLink = LinkFactory.createLink("tool.activate", "activate", getTranslator(), mainVC, this, Link.LINK);
				mainVC.put("tool.activate", activateLink);
			} else if(lastInfos.getRosterStatus() == RosterStatus.active) {
				closeLink = LinkFactory.createLink("tool.complete", "complete", getTranslator(), mainVC, this, Link.LINK);
				mainVC.put("tool.complete", closeLink);
			}
			
			exportLogLink = LinkFactory.createLink("tool.log", "log", getTranslator(), mainVC, this, Link.LINK);
			mainVC.put("tool.log", exportLogLink);
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			if(activateLink == source) {
				doActivate(row);
			} else if(closeLink == source) {
				doClose(List.of(row));
			} else if(exportLogLink == source) {
				doExportLog(ureq, row);
			}
		}
	}
}
