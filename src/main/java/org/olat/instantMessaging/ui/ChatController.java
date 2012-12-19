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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
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
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.model.Buddy;
import org.olat.user.UserManager;

/**
 * Description:<br />
 * Controller for a single Chat in a floating window
 * 
 * <P>
 * Initial Date:  13.07.2007 <br />
 * @author guido
 */
public class ChatController extends BasicController implements GenericEventListener{

	private SendMessageForm sendMessageForm;
	private ToggleAnonymousForm toggleAnonymousForm;
	private VelocityContainer rosterVC;
	private final VelocityContainer mainVC;
	private final VelocityContainer chatMsgFieldContent;
	private final Panel chatMsgFieldPanel;

	private StringBuilder messageHistory = new StringBuilder();

	private Link refresh;
	private JSAndCSSComponent jsc;
	private FloatingResizableDialogController chatPanelCtr;
	
	private final String jsTweakCmd;
	private final String jsFocusCmd;
	
	private List<String> allChats;
	private final Formatter formatter;
	private static final Pattern mailPattern = Pattern.compile("((mailto\\:|(news|(ht|f)tp(s?))\\://){1}\\S+)");

	private final OLATResourceable ores;
	private final Roster buddyList;
	private final Long privateReceiverKey;
	
	private final UserManager userManager;
	private final InstantMessagingService imService;

	protected ChatController(UserRequest ureq, WindowControl wControl, OLATResourceable ores, String roomName,
			Long privateReceiverKey, int width, int height, int offsetX, int offsetY) {
		super(ureq, wControl);
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		imService = CoreSpringFactory.getImpl(InstantMessagingService.class);
		formatter = Formatter.getInstance(getLocale());
		this.ores = ores;
		this.privateReceiverKey = privateReceiverKey;

		
		//allChats = ureq.getUserSession().getChats();
		allChats = new ArrayList<String>();
		allChats.add(Integer.toString(hashCode()));
		
		mainVC = createVelocityContainer("chat");
		chatMsgFieldContent = createVelocityContainer("chatMsgField");
		
		boolean ajaxOn = getWindowControl().getWindowBackOffice().getWindowManager().isAjaxEnabled();
		mainVC.contextPut("isAjaxMode", Boolean.valueOf(ajaxOn));
		
		//	checks with the given intervall if dirty components are available to rerender
		jsc = new JSAndCSSComponent("intervall", this.getClass(), null, null, false, null, 5000);
		mainVC.put("updatecontrol", jsc);
		
		imService.listenChat(getIdentity(), getOlatResourceable(), this);

		sendMessageForm = new SendMessageForm(ureq, getWindowControl());
		listenTo(sendMessageForm);
		sendMessageForm.resetTextField();
		mainVC.put("sendMessageForm", sendMessageForm.getInitialComponent());

		chatMsgFieldPanel = new Panel("chatMsgField");
		chatMsgFieldPanel.setContent(chatMsgFieldContent);
		
		if(privateReceiverKey == null) {
			buddyList = new Roster(getIdentity().getKey());
			rosterVC = createVelocityContainer("roster");
			rosterVC.contextPut("rosterList", buddyList);
			updateRosterList();
			toggleAnonymousForm = new ToggleAnonymousForm(ureq, getWindowControl());
			listenTo(toggleAnonymousForm);
			mainVC.put("toggleSwitch", toggleAnonymousForm.getInitialComponent());
		} else {
			rosterVC = null;
			buddyList = null;
		}

		chatPanelCtr = new FloatingResizableDialogController(ureq, getWindowControl(), mainVC,
				roomName , width, height, offsetX, offsetY, rosterVC, translate("groupchat.roster"),
				true, false, true, String.valueOf(hashCode()));
		listenTo(chatPanelCtr);
		
		String pn = chatPanelCtr.getPanelName();
		mainVC.contextPut("panelName", pn);
		
		//due to limitations in flexi form, we have to tweak focus handling manually
		jsTweakCmd = "<script>Ext.onReady(function(){try{tweak_"+pn+"();}catch(e){}});</script>";
		jsFocusCmd = "<script>Ext.onReady(function(){try{focus_"+pn+"();}catch(e){}});</script>";

		chatMsgFieldContent.contextPut("chatMessages", "");
		List<InstantMessage> lastMessages = imService.getMessages(getIdentity(), getOlatResourceable(), 0, 10, true);
		for(int i=lastMessages.size(); i-->0; ) {
			appendToMessageHistory(lastMessages.get(i), false);
		}
		
		chatMsgFieldContent.contextPut("id", hashCode());
		mainVC.put("chatMsgFieldPanel", chatMsgFieldPanel);
		
		refresh = LinkFactory.createCustomLink("refresh", "cmd.refresh", "", Link.NONTRANSLATED, mainVC, this);
		refresh.setCustomEnabledLinkCSS("b_small_icoureq.getUserSession().getSingleUserEventCenter().n sendMessageFormo_instantmessaging_refresh_icon");
		refresh.setTitle("im.refresh");

		putInitialPanel(chatPanelCtr.getInitialComponent());
		doSendPresence(toggleAnonymousForm.getNickName(), toggleAnonymousForm.isUseNickName());
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
				appendToMessageHistory(message, true);
				sendMessageForm.resetTextField();
			} else {
				//ignore empty manObjectessage entry and refocus on entry field
				chatMsgFieldContent.contextPut("chatMessages", messageHistory.toString() + jsFocusCmd);
			}
		} else if (source == toggleAnonymousForm) {
			doSendPresence(toggleAnonymousForm.getNickName(), toggleAnonymousForm.isUseNickName());
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	/**
	 * Gets called if either a new message from one of the buddies happens
	 * @see org.olat.core.util.event.GenericEventListener#event(org.olat.core.gui.control.Event)
	 */
	public void event(Event event) {
		if(event instanceof InstantMessagingEvent) {
			processInstantMessageEvent((InstantMessagingEvent)event);
		}
	}
	
	private void doSendPresence(String nickName, boolean anonym) {
		imService.sendPresence(getIdentity(), nickName, anonym, getOlatResourceable());	
	}
	
	private InstantMessage doSendMessage(String text) {
		boolean anonym;
		String fromName;
		if(toggleAnonymousForm != null) {
			anonym = toggleAnonymousForm.isUseNickName();
			fromName = toggleAnonymousForm.getNickName();
		} else {
			anonym = false;
			fromName = userManager.getUserDisplayName(getIdentity().getUser());
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
				updateRosterList(event.getFromId(), event.getName(), event.isAnonym());
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
		String m = message.getBody().replaceAll("<br/>\n", "\r\n");
		StringBuilder sb = new StringBuilder();
		sb.append("<div><span style=\"color:");
		sb.append(message.isFromMe(getIdentity()) ? "blue" : "red");
		sb.append("\">[");
		sb.append(formatter.formatTime(message.getCreationDate()));
		sb.append("] ");
		sb.append(message.getFromNickName());
		sb.append(": </span>");
		sb.append(prepareMsgBody(m.replaceAll("<", "&lt;").replaceAll(">", "&gt;")).replaceAll("\r\n", "<br/>\n"));
		sb.append("</div>");
		synchronized (messageHistory) {
			messageHistory.append(sb);
		}

		StringBuilder fh = new StringBuilder(messageHistory);
		fh.append(jsTweakCmd);
		if (focus) {
			fh.append(jsFocusCmd);
		}
		chatMsgFieldContent.contextPut("chatMessages", fh.toString());
		chatMsgFieldContent.contextPut("id", hashCode());
	}
	
	private String prepareMsgBody(String body) {
		List<String> done = new ArrayList<String>(3);
		Matcher m = mailPattern.matcher(body);
		while (m.find()) {
			String l = m.group();
			if (!done.contains(l)) {
				body = body.replaceFirst(l, "<a href=\""+l+"\" target=\"_blank\">"+l+"</a>");
			}
			done.add(l);
		}
		return body;
	}

	protected String getMessageHistory() {
		synchronized (messageHistory) {
			return messageHistory.toString();
		}
	}
	
	protected void setMessageHistory(String m) {
		synchronized (messageHistory) {
			messageHistory.insert(0, m);
		}
		chatMsgFieldContent.contextPut("chatMessages", messageHistory.toString());
	}
	
	private void updateRosterList(Long identityKey, String name, boolean anonym) {
		if(buddyList != null) {
			Buddy entry;
			if(buddyList.contains(identityKey)) {
				entry = buddyList.get(identityKey);
			} else {
				entry = imService.getBuddyById(identityKey);
				buddyList.add(entry);
			}
			entry.setAnonym(anonym);
			if(StringHelper.containsNonWhitespace(name)) {
				entry.setName(name);
			}
			rosterVC.setDirty(true);
		}
	}
	
	private void updateRosterList() {
		if(buddyList != null) {
			List<Buddy> buddies = imService.getBuddiesListenTo(getOlatResourceable());
			buddyList.addBuddies(buddies);
		}
	}
}
