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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityShort;
import org.olat.core.CoreSpringFactory;
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
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.event.GenericEventListener;
import org.olat.instantMessaging.CloseInstantMessagingEvent;
import org.olat.instantMessaging.InstantMessage;
import org.olat.instantMessaging.InstantMessagingEvent;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.model.Buddy;
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
public class ChatController extends BasicController implements GenericEventListener{

	private RosterForm rosterCtrl;
	private SendMessageForm sendMessageForm;
	private final VelocityContainer mainVC;
	private final VelocityContainer chatMsgFieldContent;

	private Map<Long,Long> avatarKeyCache = new HashMap<>();
	private Deque<ChatMessage> messageHistory = new LinkedBlockingDeque<>();

	private Link refresh;
	private Link lastWeek;
	private Link lastMonth;
	private Link todayLink;
	private JSAndCSSComponent jsc;
	private FloatingResizableDialogController chatPanelCtr;
	
	private Date today;
	private List<String> allChats;
	private final Formatter formatter;

	private final boolean highlightVip;
	private final OLATResourceable ores;
	private final Roster buddyList;
	private final Long privateReceiverKey;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private MapperService mapperService;
	@Autowired
	private InstantMessagingService imService;
	@Autowired
	private DisplayPortraitManager portraitManager;
	
	private final MapperKey avatarMapperKey;

	protected ChatController(UserRequest ureq, WindowControl wControl, OLATResourceable ores, String roomName,
			Long privateReceiverKey, boolean highlightVip, int width, int height, int offsetX, int offsetY) {
		super(ureq, wControl);
		formatter = Formatter.getInstance(getLocale());
		this.ores = ores;
		this.privateReceiverKey = privateReceiverKey;
		this.highlightVip = highlightVip;
		setToday();

		avatarMapperKey = mapperService.register(null, "avatars-members", new UserAvatarMapper(false));
		
		allChats = new ArrayList<>();
		allChats.add(Integer.toString(hashCode()));
		
		mainVC = createVelocityContainer("chat");
		chatMsgFieldContent = createVelocityContainer("chatMsgField");
		
		boolean ajaxOn = getWindowControl().getWindowBackOffice().getWindowManager().isAjaxEnabled();
		mainVC.contextPut("isAjaxMode", Boolean.valueOf(ajaxOn));
		
		//	checks with the given intervall if dirty components are available to rerender
		jsc = new JSAndCSSComponent("intervall", this.getClass(), 2500);
		mainVC.put("updatecontrol", jsc);

		// configure anonym mode depending on configuration. separate configurations for course and group chats
		InstantMessagingModule imModule = CoreSpringFactory.getImpl(InstantMessagingModule.class);
		boolean offerAnonymMode;
		boolean defaultAnonym;
		if ("CourseModule".equals(ores.getResourceableTypeName())) {
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
		
		if(privateReceiverKey == null) {
			buddyList = new Roster(getIdentity().getKey());
			List<Buddy> buddies = imService.getBuddiesListenTo(getOlatResourceable());
			buddyList.addBuddies(buddies);
			//chat started as anonymous depending on configuratino
			rosterCtrl = new RosterForm(ureq, getWindowControl(), buddyList, defaultAnonym, offerAnonymMode);
			listenTo(rosterCtrl);
			String nickName = rosterCtrl.getNickName();
			imService.listenChat(getIdentity(), getOlatResourceable(), nickName, defaultAnonym, highlightVip, this);
		} else {
			buddyList = null;
			imService.listenChat(getIdentity(), getOlatResourceable(), null, defaultAnonym, highlightVip, this);
		}

		chatPanelCtr = new FloatingResizableDialogController(ureq, getWindowControl(), mainVC,
				roomName , width, height, offsetX, offsetY,
				rosterCtrl == null ? null : rosterCtrl.getInitialComponent(),
				translate("groupchat.roster"), true, false, true, String.valueOf(hashCode()));
		listenTo(chatPanelCtr);
		chatPanelCtr.setElementCSSClass("o_im_chat_dialog");
		
		String pn = chatPanelCtr.getPanelName();
		
		sendMessageForm = new SendMessageForm(ureq, getWindowControl(), pn);
		listenTo(sendMessageForm);
		sendMessageForm.resetTextField();
		mainVC.put("sendMessageForm", sendMessageForm.getInitialComponent());
		mainVC.contextPut("panelName", pn);

		chatMsgFieldContent.contextPut("chatMessages", messageHistory);
		chatMsgFieldContent.contextPut("panelName", pn);
		chatMsgFieldContent.contextPut("avatarBaseURL", avatarMapperKey.getUrl());
		chatMsgFieldContent.contextPut("focus", Boolean.TRUE);
		loadModel(getYesterday(), 50);
		chatMsgFieldContent.contextPut("id", hashCode());
		mainVC.put("chatMsgFieldPanel", chatMsgFieldContent);
		
		refresh = LinkFactory.createCustomLink("refresh", "cmd.refresh", " ", Link.NONTRANSLATED, mainVC, this);
		refresh.setIconLeftCSS("o_icon o_icon_refresh o_icon-lg");
		refresh.setTitle("im.refresh");
		
		todayLink = LinkFactory.createButton("im.today", mainVC, this);
		lastWeek = LinkFactory.createButton("im.lastweek", mainVC, this);
		lastMonth = LinkFactory.createButton("im.lastmonth", mainVC, this);

		putInitialPanel(chatPanelCtr.getInitialComponent());
		if(rosterCtrl != null) {
			doSendPresence(rosterCtrl.getNickName(), rosterCtrl.isUseNickName());
		}
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

	@Override
	protected void doDispose() {
		allChats.remove(Integer.toString(hashCode()));
		imService.unlistenChat(getIdentity(), getOlatResourceable(), this);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == chatPanelCtr) {
			fireEvent(ureq, new CloseInstantMessagingEvent(getOlatResourceable()));
			allChats.remove(Integer.toString(hashCode()));
			jsc.setRefreshIntervall(5000);
		} else if (source == sendMessageForm) {
			if(StringHelper.containsNonWhitespace(sendMessageForm.getMessage())) {
				InstantMessage message = doSendMessage(sendMessageForm.getMessage());
				if(message == null) {
					sendMessageForm.setErrorTextField();
				} else {
					appendToMessageHistory(message, true);
					sendMessageForm.resetTextField();
				}
			} else {
				//ignore empty manObjectessage entry and refocus on entry field
				chatMsgFieldContent.contextPut("chatMessages", messageHistory);
				chatMsgFieldContent.contextPut("focus", Boolean.TRUE);
			}
		} else if (source == rosterCtrl) {
			doSendPresence(rosterCtrl.getNickName(), rosterCtrl.isUseNickName());
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
		}
	}
	
	private void loadModel(Date from, int maxResults) {
		setToday();
		messageHistory.clear();
		List<InstantMessage> lastMessages = imService.getMessages(getIdentity(), getOlatResourceable(), from, 0, maxResults, true);
		for(int i=lastMessages.size(); i-->0; ) {
			appendToMessageHistory(lastMessages.get(i), false);
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
		}
	}
	
	private void doSendPresence(String nickName, boolean anonym) {
		imService.sendPresence(getIdentity(), nickName, anonym, highlightVip, getOlatResourceable());	
	}
	
	private InstantMessage doSendMessage(String text) {
		boolean anonym;
		String fromName;
		if(rosterCtrl != null) {
			anonym = rosterCtrl.isUseNickName();
			fromName = rosterCtrl.getNickName();
		} else {
			anonym = false;
			fromName = userManager.getUserDisplayName(getIdentity());
		}
		InstantMessage message;
		if(privateReceiverKey == null) {
			message = imService.sendMessage(getIdentity(), fromName, anonym, text, getOlatResourceable());
		} else {
			message= imService.sendPrivateMessage(getIdentity(), privateReceiverKey, text, getOlatResourceable());
		}
		return message;
	}
	
	private void processInstantMessageEvent(InstantMessagingEvent event) {
		if ("message".equals(event.getCommand())) {
			Long from = event.getFromId();
			if(!getIdentity().getKey().equals(from)) {
				Long messageId = event.getMessageId();
				InstantMessage message = imService.getMessageById(getIdentity(), messageId, true);
				appendToMessageHistory(message, false);
			}
		} else if ("participant".equals(event.getCommand())) {
			if (event.getFromId() != null) {
				updateRosterList(event.getFromId(), event.getName(), event.isAnonym(), event.isVip());
			}
		}
	}
	
	/**
	 * This method close the chat from extern
	 */
	protected void closeChat() {
		allChats.remove(Integer.toString(hashCode()));
		chatPanelCtr.executeCloseCommand();
	}
	
	private void appendToMessageHistory(InstantMessage message, boolean focus) {
		if(message == null || message.getBody() == null) return;
		
		String m = message.getBody().replace("<br/>\n", "\r\n");
		m = prepareMsgBody(m.replace("<", "&lt;").replace(">", "&gt;")).replace("\r\n", "<br/>\n");
		
		Date msgDate = message.getCreationDate();
		String creationDate;
		if(today.compareTo(msgDate) < 0) {
			creationDate = formatter.formatTime(message.getCreationDate());
		} else {
			creationDate = formatter.formatDateAndTime(message.getCreationDate());
		}

		boolean first = true;
		Long fromKey = message.getFromKey();
		String from = message.getFromNickName();
		ChatMessage last = messageHistory.peekLast();
		if(last != null
				&& fromKey.equals(last.getFromKey())
				&& from.equals(last.getFrom())) {
			first = false;
		}

		boolean anonym = message.isAnonym();
		ChatMessage msg = new ChatMessage(creationDate, from, fromKey, m, first, anonym);
		if(!anonym ) {
			msg.setAvatarKey(getAvatarKey(message.getFromKey()));
		}
		messageHistory.addLast(msg);

		chatMsgFieldContent.contextPut("chatMessages", messageHistory);
		chatMsgFieldContent.contextPut("focus", Boolean.valueOf(focus));
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
	}
}
