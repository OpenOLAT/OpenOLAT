/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/
package org.olat.instantMessaging.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.IdentityShort;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.floatingresizabledialog.FloatingResizableDialogController;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.helpers.Settings;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.SignOnOffEvent;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.Coordinator;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.session.UserSessionManager;
import org.olat.instantMessaging.CloseInstantMessagingEvent;
import org.olat.instantMessaging.InstantMessage;
import org.olat.instantMessaging.InstantMessageTypeEnum;
import org.olat.instantMessaging.InstantMessagingEvent;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.LeaveChatEvent;
import org.olat.instantMessaging.RosterEntry;
import org.olat.instantMessaging.model.Buddy;
import org.olat.instantMessaging.model.InstantMessageImpl;
import org.olat.instantMessaging.model.Presence;
import org.olat.instantMessaging.ui.component.InstantMessageComparator;
import org.olat.instantMessaging.ui.event.SelectChannelEvent;
import org.olat.instantMessaging.ui.event.StartMeetingEvent;
import org.olat.modules.bigbluebutton.BigBlueButtonAttendeeRoles;
import org.olat.modules.bigbluebutton.BigBlueButtonManager;
import org.olat.modules.bigbluebutton.manager.AvatarMapper;
import org.olat.modules.bigbluebutton.model.BigBlueButtonErrors;
import org.olat.modules.teams.TeamsMeeting;
import org.olat.modules.teams.TeamsService;
import org.olat.modules.teams.model.TeamsErrors;
import org.olat.modules.teams.ui.TeamsMeetingEvent;
import org.olat.modules.teams.ui.TeamsUIHelper;
import org.olat.user.DisplayPortraitManager;
import org.olat.user.UserAvatarMapper;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br />
 * Controller for a single Chat in a floating window
 * 
 * <P>
 * Initial Date:  13.07.2007 <br />
 * @author guido
 */
public class ChatController extends BasicController implements GenericEventListener {

	private RosterForm rosterCtrl;
	private SupervisorRosterForm supervisorRosterCtrl;
	
	private SendMessageForm sendMessageForm;
	private final VelocityContainer mainVC;
	private final VelocityContainer chatMsgFieldContent;

	private Map<Long,Long> avatarKeyCache = new HashMap<>();
	private Deque<ChatMessage> messageHistory = new LinkedBlockingDeque<>();

	private Link lastWeek;
	private Link lastMonth;
	private Link todayLink;
	private JSAndCSSComponent jsc;
	private FloatingResizableDialogController chatPanelCtr;

	private int currentMaxResults;
	private Date currentDateFrom;
	
	private Date today;
	private List<String> allChats;
	private final Formatter formatter;
	private final MapperKey avatarMapperKey;

	private final boolean highlightVip;
	private final OLATResourceable ores;
	private final String resSubPath;
	private String channel;
	private Roster buddyList;
	private ChatViewConfig chatViewConfig;
	private String meetingAvatarUrl;

	private boolean rosterEntry = false;
	private final boolean persistent;
	private final Long privateReceiverKey;
	private InstantMessage errorMessage;

	@Autowired
	private Coordinator coordinator;
	@Autowired
	private UserManager userManager;
	@Autowired
	private TeamsService teamsService;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private MapperService mapperService;
	@Autowired
	private InstantMessagingModule imModule;
	@Autowired
	private InstantMessagingService imService;
	@Autowired
	private DisplayPortraitManager portraitManager;
	@Autowired
	private UserSessionManager sessionManager;
	@Autowired
	private BigBlueButtonManager bigBlueButtonManager;
	@Autowired
	private DisplayPortraitManager displayPortraitManager;

	protected ChatController(UserRequest ureq, WindowControl wControl,
			OLATResourceable ores, String resSubPath, String channel, ChatViewConfig chatViewConfig,
			Long privateReceiverKey, boolean highlightVip, boolean persistent,
			int width, int height, int offsetX, int offsetY) {
		super(ureq, wControl);
		formatter = Formatter.getInstance(getLocale());
		this.ores = ores;
		this.resSubPath = resSubPath;
		this.channel = channel;
		this.privateReceiverKey = privateReceiverKey;
		this.highlightVip = highlightVip;
		this.persistent = persistent;
		this.chatViewConfig = chatViewConfig;
		setToday();

		avatarMapperKey = mapperService.register(null, "avatars-members", new UserAvatarMapper(false));
		
		allChats = new ArrayList<>();
		allChats.add(Integer.toString(hashCode()));
		
		mainVC = createVelocityContainer("chat");
		chatMsgFieldContent = createVelocityContainer("chatMsgField");
		chatMsgFieldContent.setDomReplacementWrapperRequired(false);
		
		boolean ajaxOn = getWindowControl().getWindowBackOffice().getWindowManager().isAjaxEnabled();
		mainVC.contextPut("isAjaxMode", Boolean.valueOf(ajaxOn));
		
		//	checks with the given intervall if dirty components are available to rerender
		jsc = new JSAndCSSComponent("intervall", this.getClass(), 2500);
		mainVC.put("updatecontrol", jsc);

		// configure anonym mode depending on configuration. separate configurations for course and group chats
		boolean offerAnonymMode;
		boolean defaultAnonym;
		if ("CourseModule".equals(ores.getResourceableTypeName())
				&& !StringHelper.containsNonWhitespace(resSubPath) && !StringHelper.containsNonWhitespace(channel)) {
			offerAnonymMode = imModule.isCourseAnonymEnabled();
			defaultAnonym = offerAnonymMode && imModule.isCourseAnonymDefaultEnabled();
		} else if ("BusinessGroup".equals(ores.getResourceableTypeName())){
			offerAnonymMode = imModule.isGroupAnonymEnabled();
			defaultAnonym = offerAnonymMode && imModule.isGroupAnonymDefaultEnabled();			
		} else {
			offerAnonymMode = false;
			defaultAnonym = false;
		}
		
		// register to chat events for this resource
		if(privateReceiverKey != null) {
			rosterEntry = imService.listenChat(getIdentity(), getOlatResourceable(), resSubPath, channel,
					null, defaultAnonym, highlightVip, persistent, chatViewConfig.isCreateRosterEntry(),
					this);
		} else {
			initRoster(ureq, defaultAnonym, offerAnonymMode);
		}

		chatPanelCtr = new FloatingResizableDialogController(ureq, getWindowControl(), mainVC,
				chatViewConfig.getRoomName(), "o_im_floating", width, height, offsetX, offsetY, true, false, true, String.valueOf(hashCode()));
		listenTo(chatPanelCtr);
		chatPanelCtr.setElementCSSClass("o_im_chat_dialog");
		
		String pn = chatPanelCtr.getPanelName();
		
		sendMessageForm = new SendMessageForm(ureq, getWindowControl(), chatViewConfig, pn);
		listenTo(sendMessageForm);
		sendMessageForm.resetTextField();
		mainVC.put("sendMessageForm", sendMessageForm.getInitialComponent());
		mainVC.contextPut("panelName", pn);
		
		initInstanceMessage();
		initMainPanel();
		initChatMessageField(pn);

		putInitialPanel(chatPanelCtr.getInitialComponent());
		if(rosterCtrl != null) {
			if(chatViewConfig.isCreateRosterEntry()) {
				doSendPresence(rosterCtrl.getNickName(), rosterCtrl.isUseNickName());
			}
			coordinator.getEventBus().registerFor(this, getIdentity(), UserSessionManager.ORES_USERSESSION);
		}
	}
	
	private void initInstanceMessage() {
		if(StringHelper.containsNonWhitespace(chatViewConfig.getErrorMessage())) {
			String message = chatViewConfig.getErrorMessage();
			InstantMessageImpl error = new InstantMessageImpl();
			error.setKey(Long.valueOf(-1l));
			error.setCreationDate(new Date());
			error.setBody(message);
			error.setType(InstantMessageTypeEnum.error);
			error.setFromKey(Long.valueOf(-1l));
			error.setFromNickName("");
			error.setAnonym(true);
			errorMessage = error;
		}
	}
	
	private void initMainPanel() {
		if(StringHelper.containsNonWhitespace(chatViewConfig.getResourceInfos())) {
			mainVC.contextPut("resourceInfos", chatViewConfig.getResourceInfos());
			mainVC.contextPut("resourceIconCssClass", chatViewConfig.getResourceIconCssClass());
		}
		mainVC.contextPut("avatarBaseURL", avatarMapperKey.getUrl());
		
		todayLink = LinkFactory.createButton("im.today", mainVC, this);
		lastWeek = LinkFactory.createButton("im.lastweek", mainVC, this);
		lastMonth = LinkFactory.createButton("im.lastmonth", mainVC, this);
	}

	private void initChatMessageField(String pn) {
		chatMsgFieldContent.contextPut("chatMessages", messageHistory);
		chatMsgFieldContent.contextPut("panelName", pn);
		chatMsgFieldContent.contextPut("avatarBaseURL", avatarMapperKey.getUrl());
		chatMsgFieldContent.contextPut("focus", Boolean.TRUE);
		if(StringHelper.containsNonWhitespace(chatViewConfig.getWelcome())) {
			chatMsgFieldContent.contextPut("welcome", chatViewConfig.getWelcome());
			chatMsgFieldContent.contextPut("welcomeFrom", chatViewConfig.getWelcomeFrom());
			chatMsgFieldContent.contextPut("welcomeDate", formatter.formatTimeShort(new Date()));
		}
		loadModel(getYesterday(), 50);
		imService.updateLastSeen(getIdentity(), ores, resSubPath, channel);
		chatMsgFieldContent.contextPut("id", hashCode());
		mainVC.put("chatMsgFieldPanel", chatMsgFieldContent);
	}
		
	private void initRoster(UserRequest ureq, boolean defaultAnonym, boolean offerAnonymMode) {
		RosterFormDisplay rosterDisplay = chatViewConfig.getRosterDisplay();
		if(rosterDisplay == RosterFormDisplay.none) return;

		String nickName;
		if(rosterDisplay == RosterFormDisplay.supervisor) {
			supervisorRosterCtrl = new SupervisorRosterForm(ureq, getWindowControl(),
					ores, resSubPath, channel, avatarMapperKey);
			listenTo(supervisorRosterCtrl);
			mainVC.put("roster", supervisorRosterCtrl.getInitialComponent());
			nickName = userManager.getUserDisplayName(getIdentity());
		} else {
			buddyList = new Roster(getIdentity().getKey(), rosterDisplay != RosterFormDisplay.supervised);
			List<Buddy> buddies = imService.getBuddiesListenTo(getOlatResourceable(), resSubPath, channel);
			buddyList.addBuddies(buddies);

			rosterCtrl = new RosterForm(ureq, getWindowControl(), buddyList, defaultAnonym, offerAnonymMode, rosterDisplay, avatarMapperKey);
			listenTo(rosterCtrl);
			mainVC.put("roster", rosterCtrl.getInitialComponent());
			nickName = rosterCtrl.getNickName();
		}

		rosterEntry = imService.listenChat(getIdentity(), getOlatResourceable(), resSubPath, channel,
				nickName, defaultAnonym, highlightVip, persistent, chatViewConfig.isCreateRosterEntry(),
				this);
		mainVC.contextPut("rosterDisplay", rosterDisplay.name());
	}
	
	private void setToday() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		today = cal.getTime();
	}
	
	private Date getYesterday() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -1);
		return cal.getTime();
	}
	
	private Date getLastWeek() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -7);
		return cal.getTime();
	}
	
	private Date getLastMonth() {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, -1);
		return cal.getTime();
	}
	
	public OLATResourceable getOlatResourceable() {
		return ores;
	}
	
	public String getResSubPath() {
		return resSubPath;
	}
	
	public String getChannel() {
		return channel;
	}

	@Override
	protected void doDispose() {
		allChats.remove(Integer.toString(hashCode()));
		imService.unlistenChat(getIdentity(), getOlatResourceable(), resSubPath, channel, this);
		coordinator.getEventBus().deregisterFor(this, UserSessionManager.ORES_USERSESSION);
        super.doDispose();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == chatPanelCtr) {
			fireEvent(ureq, new CloseInstantMessagingEvent(getOlatResourceable(), resSubPath, channel));
			allChats.remove(Integer.toString(hashCode()));
			jsc.setRefreshIntervall(5000);
		} else if (source == sendMessageForm) {
			if(event == Event.DONE_EVENT) {
				doSendMessage();
			} else if(event == Event.CLOSE_EVENT) {
				doCloseChat(ureq);
			} else if(event == Event.BACK_EVENT) {
				doReactivateChat();
			} else if(event instanceof StartMeetingEvent) {
				doSendMeetingMessage();
			}
		} else if (source == rosterCtrl) {
			doSendPresence(rosterCtrl.getNickName(), rosterCtrl.isUseNickName());
		} else if (source == supervisorRosterCtrl) {
			if(event instanceof SelectChannelEvent) {
				doSwitchChannel(((SelectChannelEvent)event).getChannel());
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(todayLink == source) {
			loadModel(getYesterday(), -1);
		} else if(lastWeek == source) {
			loadModel(getLastWeek(), -1);
		} else if(lastMonth == source) {
			loadModel(getLastMonth(), -1);
		} else if(source instanceof Link) {
			Link link = (Link)source;
			if("meeting".equals(link.getCommand()) && link.getUserObject() instanceof ChatMessage) {
				doStartMeeting((ChatMessage)link.getUserObject());
			}
		}
	}
	
	private void doSendMessage() {
		if(StringHelper.containsNonWhitespace(sendMessageForm.getMessage())) {
			InstantMessage message = doSendMessage(sendMessageForm.getMessage());
			if(message == null) {
				sendMessageForm.setErrorTextField();
			} else {
				appendToMessageHistory(message, true);
				sendMessageForm.resetTextField();
	
				if(supervisorRosterCtrl != null
						&& (message.getType() == InstantMessageTypeEnum.text || message.getType() == InstantMessageTypeEnum.meeting)) {
					// make sure the channel is active after texting
					supervisorRosterCtrl.activateChannel(channel);
				}
				updateSendMessageForm();
			}
		} else {
			//ignore empty manObjectessage entry and refocus on entry field
			chatMsgFieldContent.contextPut("chatMessages", messageHistory);
			chatMsgFieldContent.contextPut("focus", Boolean.TRUE);
		}
	}

	private void doSendMeetingMessage() {
		boolean anonym = isAnonym();
		String fromName = getFromName();
		imService.sendMeetingMessage(getIdentity(), fromName, anonym, chatViewConfig.getRoomName(), ores, resSubPath, channel);
		loadModel(currentDateFrom, currentMaxResults);
	}
	
	private void doStartMeeting(ChatMessage message) {
		InstantMessage im = imService.getMessageById(getIdentity(), message.getMessageKey(), true);
		if(im.getBbbMeeting() != null) {
			BigBlueButtonErrors errors = new BigBlueButtonErrors();
			BigBlueButtonAttendeeRoles role = highlightVip ? BigBlueButtonAttendeeRoles.moderator: BigBlueButtonAttendeeRoles.viewer;
			String avatarUrl = isAnonym() ? null : getAvatarUrl();
			String meetingUrl = bigBlueButtonManager.join(im.getBbbMeeting(), getIdentity(), getFromName(), avatarUrl, role, null, errors);
			redirectTo(meetingUrl, errors);
		} else if(im.getTeamsMeeting() != null) {
			TeamsErrors errors = new TeamsErrors();
			TeamsMeeting meeting = teamsService.joinMeeting(im.getTeamsMeeting(), getIdentity(), highlightVip, false, errors);
			redirectTo(meeting, errors);	
		}
	}
	
	private String getAvatarUrl() {
		if(meetingAvatarUrl == null) {
			File portraitFile = displayPortraitManager.getBigPortrait(getIdentity());
			if(portraitFile != null) {
				String rnd = "r" + getIdentity().getKey() + CodeHelper.getRAMUniqueID();
				meetingAvatarUrl = Settings.createServerURI()
						+ registerCacheableMapper(null, rnd, new AvatarMapper(portraitFile), 5 * 60 * 60)
						+ "/" + portraitFile.getName();
			}
		}
		return meetingAvatarUrl;
	}
	
	private void redirectTo(String meetingUrl, BigBlueButtonErrors errors) {
		if(errors.hasErrors()) {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
		} else if(StringHelper.containsNonWhitespace(meetingUrl)) {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(meetingUrl));
		} else {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
			showWarning("warning.no.access");
		}
	}
	

	private void redirectTo(TeamsMeeting meeting, TeamsErrors errors) {
		if(meeting == null) {
			showWarning("warning.no.meeting");
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
			return;
		} else if(errors.hasErrors()) {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
			getWindowControl().setError(TeamsUIHelper.formatErrors(getTranslator(), errors));
			return;
		}
		
		String joinUrl = meeting.getOnlineMeetingJoinUrl();
		if(StringHelper.containsNonWhitespace(joinUrl)) {
			TeamsMeetingEvent event = new TeamsMeetingEvent(meeting.getKey(), getIdentity().getKey());
			OLATResourceable meetingOres = OresHelper.createOLATResourceableInstance(TeamsMeeting.class.getSimpleName(), meeting.getKey());
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(event, meetingOres);
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowRedirectTo(joinUrl));
		} else {
			getWindowControl().getWindowBackOffice().sendCommandTo(CommandFactory.createNewWindowCancelRedirectTo());
			showWarning("warning.no.access");
		}
	}
	
	private void doCloseChat(UserRequest ureq) {
		boolean anonym = isAnonym();
		String fromName = getFromName();
		imService.sendStatusMessage(getIdentity(), fromName, anonym, InstantMessageTypeEnum.close, ores, resSubPath, channel);
		
		if(supervisorRosterCtrl != null) {
			loadModel(currentDateFrom, currentMaxResults);
			supervisorRosterCtrl.loadModel();
		} else {
			fireEvent(ureq, Event.CLOSE_EVENT);
		}
	}
	
	private void doReactivateChat() {
		boolean anonym = isAnonym();
		String fromName = getFromName();
		imService.sendStatusMessage(getIdentity(), fromName, anonym, InstantMessageTypeEnum.reactivate, ores, resSubPath, channel);
		loadModel(currentDateFrom, currentMaxResults);
		
		if(supervisorRosterCtrl != null) {
			supervisorRosterCtrl.loadModel();
		}
	}
	
	public void switchChannel(String newChannel) {
		if(supervisorRosterCtrl != null) {
			supervisorRosterCtrl.switchChannel(newChannel);
		}
		doSwitchChannel(newChannel);
	}
	
	private void doSwitchChannel(String newChannel) {
		imService.unlistenChat(getIdentity(), ores, resSubPath, channel, this);
		
		channel = newChannel;
		
		String name;
		if(rosterCtrl != null) {
			name = rosterCtrl.getNickName();
		} else {
			name = userManager.getUserDisplayName(getIdentity());
		}
		
		rosterEntry = imService.listenChat(getIdentity(), getOlatResourceable(), resSubPath, channel,
				name, false, highlightVip, persistent, true, this);
		loadModel(currentDateFrom, currentMaxResults);
		
		chatMsgFieldContent.contextPut("chatMessages", messageHistory);
		chatMsgFieldContent.contextPut("focus", Boolean.TRUE);
		
		if(!messageHistory.isEmpty()) {
			imService.updateLastSeen(getIdentity(), ores, resSubPath, channel);
		}
	}
	
	private void loadModel(Date from, int maxResults) {
		this.currentDateFrom = from;
		this.currentMaxResults = maxResults;
		
		setToday();
		messageHistory.clear();
		List<InstantMessage> lastMessages = imService
				.getMessages(getIdentity(), getOlatResourceable(), resSubPath, channel, from, 0, maxResults, true);
		if(errorMessage != null) {
			lastMessages.add(errorMessage);
		}
		Collections.sort(lastMessages, new InstantMessageComparator());
		
		for(int i=lastMessages.size(); i-->0; ) {
			appendToMessageHistory(lastMessages.get(i), false);
		}
		
		if(supervisorRosterCtrl != null && channel != null) {
			List<RosterEntry> allEntries = supervisorRosterCtrl.getRosterEntries(channel);
			List<ChatRosterEntry> entries = allEntries.stream()
					.filter(e -> !e.isVip() && !getIdentity().getKey().equals(e.getIdentityKey()))
					.map(e -> new ChatRosterEntry(e, sessionManager.isOnline(e.getIdentityKey())))
					.collect(Collectors.toList());
			mainVC.contextPut("rosterNonVipEntries", entries);
			
			int totalOfEntries = supervisorRosterCtrl.getTotalOfEntries(channel);
			String i18nNum = allEntries.size() <= 1 ? "num.of.entry" : "num.of.entries";
			mainVC.contextPut("numOfAllEntriesMsg", translate(i18nNum, Integer.toString(totalOfEntries)));
		}
		
		updateSendMessageForm();
	}
	
	private void updateSendMessageForm() {
		if(chatViewConfig.isCanClose() || chatViewConfig.isCanReactivate()) {
			if(!messageHistory.isEmpty() && messageHistory.getLast().getTypeEnum() == InstantMessageTypeEnum.close) {
				sendMessageForm.setCloseableChat(!chatViewConfig.isCanReactivate(), false, true);
			} else if(!messageHistory.isEmpty() && messageHistory.getLast().getTypeEnum() == InstantMessageTypeEnum.end) {
				sendMessageForm.setCloseableChat(!chatViewConfig.isCanReactivate(), false, true);
			} else {
				sendMessageForm.setCloseableChat(true, true, false);
			}
		} else {
			sendMessageForm.setCloseableChat(true, false, false);
		}
	}
	
	public static class ChatRosterEntry {
		
		private String onlineCssStatus;
		private Long avatarKey;
		private String name;
		
		public ChatRosterEntry(RosterEntry entry, boolean online) {
			name = entry.isAnonym() ? entry.getNickName() : entry.getFullName();
			avatarKey = entry.getIdentityKey();
			onlineCssStatus = online ? "o_icon_status_available" : "o_icon_status_unavailable";
		}

		public String getOnlineCssStatus() {
			return onlineCssStatus;
		}

		public Long getAvatarKey() {
			return avatarKey;
		}

		public String getName() {
			return name;
		}
	}
	
	/**
	 * Gets called if either a new message from one of the buddies happens
	 * @see org.olat.core.util.event.GenericEventListener#event(org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(Event event) {
		if(event instanceof InstantMessagingEvent) {
			processInstantMessageEvent((InstantMessagingEvent)event);
		} else if(event instanceof LeaveChatEvent) {
			processInstantMessageEvent((LeaveChatEvent)event);
		} else if(event instanceof SignOnOffEvent) {
			processUserSessionEvent((SignOnOffEvent)event);
		}
	}
	
	private void doSendPresence(String nickName, boolean anonym) {
		imService.sendPresence(getIdentity(), getOlatResourceable(), resSubPath, channel,
				nickName, anonym, highlightVip, persistent);	
	}
	
	private InstantMessage doSendMessage(String text) {
		boolean anonym = isAnonym();
		String fromName = getFromName();
		
		// create the roster entry if it not exists
		if(!rosterEntry) {
			imService.addToRoster(getIdentity(), getOlatResourceable(), resSubPath, channel, fromName, anonym, highlightVip);
			rosterEntry = true;
		}

		InstantMessage message;
		if(privateReceiverKey == null) {
			InstantMessageTypeEnum msgType = InstantMessageTypeEnum.text;
			List<IdentityRef> toNotifyRequests = null;
			InstantMessageTypeEnum stopperStatus = stopper();
			// Status null is equivalent to messages queue empty
			if(stopperStatus == null || stopperStatus == InstantMessageTypeEnum.end) {
				msgType = InstantMessageTypeEnum.request;
				toNotifyRequests = chatViewConfig.getToNotifyRequests();
			} else if(stopperStatus == InstantMessageTypeEnum.close) {
				msgType = InstantMessageTypeEnum.request;
				toNotifyRequests = getRequestToNotifiyTo();
			}
			
			message = imService.sendMessage(getIdentity(), fromName, anonym, text, msgType, getOlatResourceable(), resSubPath, channel, toNotifyRequests);
		} else {
			message = imService.sendPrivateMessage(getIdentity(), privateReceiverKey, text, getOlatResourceable());
		}
		return message;
	}
	
	private InstantMessageTypeEnum stopper() {
		if(highlightVip) return null;
		
		if(messageHistory.isEmpty()) {
			return null;
		}
		
		ChatMessage lastMessage = null;
		// Ignore virtual error message
		for(Iterator<ChatMessage> reverseIt=messageHistory.descendingIterator(); reverseIt.hasNext(); ) {
			ChatMessage message = reverseIt.next();
			if(message.getMessageKey() != null && message.getMessageKey().longValue() > 0) {
				lastMessage = message;
				break;
			}
		}
		return lastMessage == null ? null : lastMessage.getTypeEnum();
	}
	
	private List<IdentityRef> getRequestToNotifiyTo() {
		List<IdentityRef> toNotifyRequests = chatViewConfig.getToNotifyRequests();
		if(toNotifyRequests != null && rosterCtrl != null) {
			toNotifyRequests = buddyList.getBuddies().stream().filter(Buddy::isVip)
					.map(Buddy::getIdentityKey)
					.map(IdentityRefImpl::new)
					.collect(Collectors.toList());
		}
		return toNotifyRequests;
	}
	
	private boolean isAnonym() {
		boolean anonym;
		if(rosterCtrl != null) {
			anonym = rosterCtrl.isUseNickName();
		} else {
			anonym = false;
		}
		return anonym;
	}
	
	private String getFromName() {
		String fromName;
		if(rosterCtrl != null) {
			fromName = rosterCtrl.getNickName();
		} else {
			fromName = userManager.getUserDisplayName(getIdentity());
		}
		return fromName;
	}
	
	private void processInstantMessageEvent(InstantMessagingEvent event) {
		if ((InstantMessagingEvent.MESSAGE.equals(event.getCommand()) || InstantMessagingEvent.REQUEST.equals(event.getCommand()))
				&& !getIdentity().getKey().equals(event.getFromId())
				&& Objects.equals(resSubPath, event.getResSubPath())) {
			processInstantMessageTextEvent(event);
		} else if (InstantMessagingEvent.PARTICIPANT.equals(event.getCommand())
				&& event.getFromId() != null
				&& Objects.equals(resSubPath, event.getResSubPath()) && Objects.equals(channel, event.getChannel())) {
			updateRosterList(event.getFromId(), event.getName(), event.isAnonym(), event.isVip());
		}
	}
	
	private void processInstantMessageEvent(LeaveChatEvent event) {
		if(buddyList != null && rosterCtrl != null && event.sameOres(ores)) {
			processStatusUpdate(event.getIdentityKey());
		}
	}
	
	private void processStatusUpdate(Long identityKey) {
		if(buddyList.contains(identityKey)) {
			Buddy entry = buddyList.get(identityKey);
			if(persistent) {
				boolean online = imService.isOnline(new IdentityRefImpl(identityKey));
				if(online) {
					entry.setStatus(Presence.available.name());
				} else {
					entry.setStatus(Presence.unavailable.name());
				}
			} else {
				buddyList.remove(entry);
			}
		}
		rosterCtrl.updateModel();
	}
	
	private void processInstantMessageTextEvent(InstantMessagingEvent event) {
		Long messageId = event.getMessageId();
		
		boolean appended = false;
		if(Objects.equals(channel, event.getChannel())) {
			InstantMessage message = imService.getMessageById(getIdentity(), messageId, true);
			appended = appendToMessageHistory(message, false);
		}
		
		if(supervisorRosterCtrl != null && StringHelper.containsNonWhitespace(event.getChannel())) {
			if(channel != null && channel.equals(event.getChannel())) {
				updateSendMessageForm();
			}
		} else if(buddyList != null
				&& (event.getMessageType() == InstantMessageTypeEnum.accept
					|| event.getMessageType() == InstantMessageTypeEnum.reactivate
					|| event.getMessageType() == InstantMessageTypeEnum.join)) {
			Long identityKey = event.getFromId();
			if(!buddyList.contains(identityKey)) {
				updateRosterList(identityKey, event.getName(), event.isAnonym(), event.isVip());
			}
		}
		
		if(event.getMessageType() == InstantMessageTypeEnum.join) {
			updateRosterList(event.getFromId(), event.getName(), event.isAnonym(), event.isVip());
		}
		if(appended) {
			imService.updateLastSeen(getIdentity(), ores, resSubPath, channel);
		}
	}
	
	private void processUserSessionEvent(SignOnOffEvent event) {
		Long identityKey = event.getIdentityKey();
		if(rosterCtrl != null && buddyList != null && buddyList.contains(identityKey)) {
			processStatusUpdate(identityKey);
		}
	}
	
	/**
	 * This method close the chat from external
	 */
	protected void closeChat() {
		allChats.remove(Integer.toString(hashCode()));
		chatPanelCtr.executeCloseCommand();
	}
	
	private boolean appendToMessageHistory(InstantMessage message, boolean focus) {
		if(message == null) {
			return false;
		}
		
		ChatMessage oldEntry = messageHistory.stream()
				.filter(historyEntry -> message.getKey().equals(historyEntry.getMessageKey()))
				.findFirst().orElse(null);
		if(oldEntry != null) {
			return false;
		}
		
		String m = "";
		Link link = null;
		if(message.getType() == InstantMessageTypeEnum.meeting) {
			String id = "meet" + message.getKey();
			link = (Link)mainVC.getComponent(id);
			if(link == null) {
				link = LinkFactory.createLink(id, id, "meeting", "meeting.invitation.long", getTranslator(), chatMsgFieldContent, this, Link.LINK);
				link.setIconLeftCSS("o_icon o_icon-fw o_livestream_icon");
				link.setIconRightCSS("o_icon o_icon-fw o_icon_external_link");
				link.setNewWindow(true, true);
				link.setTarget("chat_video");
			}
		} else if(StringHelper.containsNonWhitespace(message.getBody())) {
			m = message.getBody().replace("<br>\n", "\r\n");
			m = prepareMsgBody(m.replace("<", "&lt;").replace(">", "&gt;")).replace("\r\n", "<br>\n");
		} else if (message.getType().isStatus()) {
			m = prepareStatusMessage(message);
		} else {
			return false;
		}

		Date msgDate = message.getCreationDate();
		String creationDate;
		if(today.compareTo(msgDate) < 0) {
			creationDate = formatter.formatTimeShort(message.getCreationDate());
		} else {
			creationDate = formatter.formatDateAndTime(message.getCreationDate());
		}

		boolean first = true;
		Long fromKey = message.getFromKey();
		String from = message.getFromNickName();
		ChatMessage last = messageHistory.peekLast();
		if(last != null
				&& fromKey.equals(last.getFromKey()) && from.equals(last.getFrom())
				&& !message.getType().isStatus() && !last.getTypeEnum().isStatus()) {
			first = false;
		}

		boolean anonym = message.isAnonym();
		ChatMessage msg = new ChatMessage(message.getKey(), creationDate, from, fromKey, m, link, message.getType(),
				first, anonym, getIdentity().getKey().equals(message.getFromKey()));
		if(!anonym ) {
			msg.setAvatarKey(getAvatarKey(message.getFromKey()));
		}
		if(link != null) {
			link.setUserObject(msg);
		}
		
		messageHistory.addLast(msg);

		chatMsgFieldContent.contextPut("chatMessages", messageHistory);
		chatMsgFieldContent.contextPut("focus", Boolean.valueOf(focus));
		return true;
	}
	
	private String prepareStatusMessage(InstantMessage message) {
		String i18nKey;
		if (message.getType() == InstantMessageTypeEnum.join) {
			i18nKey = "chat.join";
		} else if (message.getType() == InstantMessageTypeEnum.accept) {
			i18nKey = "chat.accept";
		} else if (message.getType() == InstantMessageTypeEnum.reactivate) {
			i18nKey = "chat.reactivate";
		} else if (message.getType() == InstantMessageTypeEnum.close) {
			i18nKey = "chat.close";
		} else if (message.getType() == InstantMessageTypeEnum.end) {
			i18nKey = "chat.end";
		} else {
			return null;
		}
		
		String time = formatter.formatTimeShort(message.getCreationDate());
		String from;
		if(message.isAnonym()) {
			from = message.getFromNickName();
		} else {
			from = userManager.getUserDisplayName(message.getFromKey());
		}
		return translate(i18nKey, time, from);
	}
	
	private Long getAvatarKey(Long identityKey) {
		Long avatarKey = avatarKeyCache.get(identityKey);
		if(avatarKey == null && buddyList != null) {
			Buddy buddy = buddyList.get(identityKey);
			if(buddy != null) {
				// check if avatar image exists at all
				if (portraitManager.getSmallPortraitResource(buddy.getIdentityKey())  != null) {
					avatarKey = buddy.getIdentityKey();
				} else {
					avatarKey = Long.valueOf(-1);
				}
				avatarKeyCache.put(identityKey, avatarKey);
			}
		}
		if(avatarKey == null) {
			IdentityShort id = securityManager.loadIdentityShortByKey(identityKey);
			if(id != null) {
				// check if avatar image exists at all
				if (portraitManager.getSmallPortraitResource(id.getKey())  != null) {
					avatarKey = id.getKey();
				} else {
					avatarKey = Long.valueOf(-1);
				}				
				avatarKeyCache.put(identityKey, avatarKey);
			}
		}
		if (avatarKey == null) {
			// use not-available when still not set to something
			avatarKey = Long.valueOf(-1);
		}
		return avatarKey;
	}
	
	private String prepareMsgBody(String body) {
		body = Formatter.formatURLsAsLinks(body, true);
		body = Formatter.formatEmoticonsAsImages(body);
		return body;
	}
	
	private void updateRosterList(Long identityKey, String name, boolean anonym, boolean vip) {
		if(buddyList != null && rosterCtrl != null) {
			Buddy entry;
			if(buddyList.contains(identityKey)) {
				entry = buddyList.get(identityKey);
			} else {
				entry = imService.getBuddyById(identityKey);
				buddyList.add(entry);
			}
			entry.setVip(vip);
			entry.setAnonym(anonym);
			if(StringHelper.containsNonWhitespace(name)) {
				entry.setName(name);
			}
			rosterCtrl.updateModel();
		}
		
		if(supervisorRosterCtrl != null) {
			int totalOfEntries = supervisorRosterCtrl.loadTotalEntries(channel);
			String i18nNum = totalOfEntries <= 1 ? "num.of.entry" : "num.of.entries";
			mainVC.contextPut("numOfAllEntriesMsg", translate(i18nNum, Integer.toString(totalOfEntries)));
		}
	}
}
