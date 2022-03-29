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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.SignOnOffEvent;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.Coordinator;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.session.UserSessionManager;
import org.olat.instantMessaging.InstantMessageTypeEnum;
import org.olat.instantMessaging.InstantMessagingEvent;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.RosterEntry;
import org.olat.instantMessaging.model.RosterChannelInfos;
import org.olat.instantMessaging.model.RosterChannelInfos.RosterStatus;
import org.olat.instantMessaging.ui.component.RosterEntryWithUnreadCellRenderer;
import org.olat.instantMessaging.ui.event.SelectChannelEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 1 mars 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SupervisorRosterForm extends FormBasicController implements GenericEventListener {
	
	private static final String ICON_CLOSE = "o_icon o_icon-fw o_icon_close_tree";
	private static final String ICON_OPEN = "o_icon o_icon-fw o_icon_open_tree";
	
	private int count = 0;
	private final String resSubPath;
	private final OLATResourceable chatResource;
	private final OLATResourceable personalEventOres;
	private final MapperKey avatarMapperKey;
	private String selectedChannel;
	private List<SupervisedRoster> activeRosters = List.of();
	private List<SupervisedRoster> completedRosters = List.of();
	private List<SupervisedRoster> requestedRosters = List.of();
	private Set<Long> identityKeys;
	
	private FormLink toggleActiveLink;
	private FormLink toggleCompletedLink;
	private FormLink toggleRequestedLink;
	private FormLayoutContainer activeContainer;
	private FormLayoutContainer completedContainer;
	private FormLayoutContainer requestedContainer;
	
	private final SupervisedRosterComparator rosterComparator;

	@Autowired
	private Coordinator coordinator;
	@Autowired
	private InstantMessagingService imService;
	@Autowired
	private UserSessionManager sessionManager;

	public SupervisorRosterForm(UserRequest ureq, WindowControl wControl,
			OLATResourceable chatResource, String resSubPath, String initialChannel,
			MapperKey avatarMapperKey) {
		super(ureq, wControl, "roster_supervisor");
		
		this.chatResource = chatResource;
		this.resSubPath = resSubPath;
		this.selectedChannel = initialChannel;
		this.avatarMapperKey = avatarMapperKey;
		rosterComparator = new SupervisedRosterComparator(getLocale());
		
		coordinator.getEventBus().registerFor(this, getIdentity(), UserSessionManager.ORES_USERSESSION);
		coordinator.getEventBus().registerFor(this, getIdentity(), chatResource);
		personalEventOres = OresHelper.createOLATResourceableInstance(InstantMessagingService.PERSONAL_EVENT_ORES_NAME, getIdentity().getKey());
		coordinator.getEventBus().registerFor(this, getIdentity(), personalEventOres);

		initForm(ureq);
		loadModel();
	}
	
	@Override
	protected void doDispose() {
		coordinator.getEventBus().deregisterFor(this, chatResource);
		coordinator.getEventBus().deregisterFor(this, personalEventOres);
		coordinator.getEventBus().deregisterFor(this, UserSessionManager.ORES_USERSESSION);
		super.doDispose();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		toggleActiveLink = uifactory.addFormLink("roster.active", formLayout, Link.LINK);
		toggleCompletedLink = uifactory.addFormLink("roster.completed", formLayout, Link.LINK);
		toggleRequestedLink = uifactory.addFormLink("roster.requested", formLayout, Link.LINK);

		activeContainer = initRosterContainer(formLayout, toggleActiveLink, "activeRosters");
		completedContainer = initRosterContainer(formLayout, toggleCompletedLink, "completedRosters");
		requestedContainer = initRosterContainer(formLayout, toggleRequestedLink, "requestedRosters");
	}
	
	private FormLayoutContainer initRosterContainer(FormItemContainer formLayout, FormLink link, String name) {
		String entriesPage = velocity_root + "/roster_supervisor_entries.html";
		FormLayoutContainer container = FormLayoutContainer.createCustomFormLayout(name, getTranslator(), entriesPage);
		container.contextPut("avatarBaseURL", avatarMapperKey.getUrl());
		formLayout.add(container);
		
		link.setIconLeftCSS(ICON_CLOSE);
		link.setUserObject(Boolean.TRUE);
		return container;
	}
	
	protected SupervisedRoster getRoster(String channel) {
		SupervisedRoster roster = findRoster(channel, activeRosters);
		if(roster == null) {
			roster = findRoster(channel, completedRosters);
			if(roster == null) {
				roster = findRoster(channel, requestedRosters);
			}
		}
		return roster;
	}
	
	protected List<RosterEntry> getRosterEntries(String channel) {
		SupervisedRoster roster = getRoster(channel);
		if(roster != null) {
			return roster.getRosterEntries().stream()
					.map(SupervisedRosterEntry::getEntry)
					.collect(Collectors.toList());
		}
		return List.of();
	}
	
	protected int getTotalOfEntries(String channel) {
		SupervisedRoster roster = getRoster(channel);
		if(roster != null) {
			return roster.getTotalEntries();
		}
		return 0;
	}
	
	private SupervisedRoster findRoster(String channel, List<SupervisedRoster> rosters) {
		for(SupervisedRoster roster:rosters) {
			if(roster != null && channel.equals(roster.getChannel())) {
				return roster;
			}	
		}
		return null;
	}
	
	protected int loadTotalEntries(String channel) {
		SupervisedRoster roster = getRoster(channel);
		if(roster != null) {
			RosterChannelInfos rosterInfos = imService.getRoster(chatResource, resSubPath, channel, getIdentity());
			roster.setTotalEntries(rosterInfos.getEntries().size());
			return roster.getTotalEntries();
		}
		return 0;
	}
	
	protected void loadModel() {
		final List<RosterChannelInfos> rosterInfos = imService.getRosters(chatResource, resSubPath, getIdentity(), true);
		final List<SupervisedRoster> actives = new ArrayList<>(rosterInfos.size());
		final List<SupervisedRoster> completed = new ArrayList<>(rosterInfos.size());
		final List<SupervisedRoster> requested = new ArrayList<>(rosterInfos.size());
		
		for(RosterChannelInfos rosterInfo:rosterInfos) {
			List<SupervisedRosterEntry> rosterEntries = rosterInfo.getNonVipEntries().stream()
					.map(entry -> forgeEntryRow(entry, rosterInfo))
					.collect(Collectors.toList());

			SupervisedRoster sRoster = new SupervisedRoster(rosterInfo, rosterEntries, rosterInfo.getEntries().size());
			RosterStatus status = rosterInfo.getRosterStatus();
			if(status == RosterStatus.request) {
				requested.add(sRoster);
			} else if(status == RosterStatus.active) {
				actives.add(sRoster);
			} else if(status == RosterStatus.completed || status == RosterStatus.ended) {
				completed.add(sRoster);
			} 
		}

		Collections.sort(actives, rosterComparator);
		Collections.sort(completed, rosterComparator);
		Collections.sort(requested, rosterComparator);
		
		Set<Long> identities = new HashSet<>();
		loadContainer(activeContainer, toggleActiveLink, actives, identities);
		loadContainer(completedContainer, toggleCompletedLink, completed, identities);
		loadContainer(requestedContainer, toggleRequestedLink, requested, identities);
		identityKeys = identities;
		
		activeRosters = actives;
		completedRosters = completed;
		requestedRosters = requested;
		
		updateSelectedChannel();
	}
	
	private SupervisedRosterEntry forgeEntryRow(RosterEntry entry, RosterChannelInfos infos) {
		String name = entry.isAnonym() ? entry.getNickName() : entry.getFullName();
		FormLink link = uifactory.addFormLink("entry_" + (++count), "entry", name, null, null, Link.LINK | Link.NONTRANSLATED);
		setOnlineStatus(entry.getIdentityKey(), link);
		long unreadMessages = infos.getUnreadMessages() == null ? 0l : infos.getUnreadMessages().longValue();
		return new SupervisedRosterEntry(entry, link, unreadMessages);
	}
	
	private void setOnlineStatus(Long identityKey, FormLink entryLink) {
		boolean presence = sessionManager.isOnline(identityKey);
		if(presence) {
			entryLink.setIconLeftCSS("o_icon o_icon-fw o_icon_status_available");
		} else {
			entryLink.setIconLeftCSS("o_icon o_icon-fw o_icon_status_unavailable");
		}
	}
	
	private void loadContainer(FormLayoutContainer container, FormLink link, List<SupervisedRoster> rosters, Set<Long> identities) {
		boolean visible = !rosters.isEmpty();
		container.contextPut("rosters", rosters);
		container.setVisible(visible);
		link.setVisible(visible);
		
		for(SupervisedRoster roster:rosters) {
			for(SupervisedRosterEntry entry:roster.getRosterEntries()) {
				container.add(entry.getEntryLink());
				identities.add(entry.getEntry().getIdentityKey());
			}
		}
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
			if(identityKeys != null && identityKeys.contains(sooe.getIdentityKey())) {
				processUserSessionEvent(sooe);
			}
		}
	}
	
	private void processInstantMessagingEvent(InstantMessagingEvent event) {
		String command = event.getCommand();
		if(InstantMessagingEvent.END_CHANNEL.equals(command)) {
			completeChannel(event.getChannel());
		} else if(InstantMessagingEvent.REQUEST.equals(command)) {
			requestChannel(event.getChannel());
		} else if(InstantMessagingEvent.MESSAGE.equals(command)) {
			InstantMessageTypeEnum messageType = event.getMessageType();
			if(messageType == InstantMessageTypeEnum.close || messageType == InstantMessageTypeEnum.end) {
				completeChannel(event.getChannel());
			} else if(messageType == InstantMessageTypeEnum.accept
					|| messageType == InstantMessageTypeEnum.reactivate
					|| messageType == InstantMessageTypeEnum.join) {
				activateChannel(event.getChannel());
			} else if((messageType == InstantMessageTypeEnum.text
					|| messageType == InstantMessageTypeEnum.meeting)
					&& !Objects.equals(selectedChannel, event.getChannel())) {
				processUnreadMessages(event.getChannel(), event.getFromId());
			}
		}
	}
	
	private void processUnreadMessages(String channel, Long identityKey) {
		SupervisedRoster roster = getRoster(channel);
		if(roster == null) return;
		
		SupervisedRosterEntry entry = roster.getRosterEntry(identityKey);
		if(entry != null) {
			entry.incrementUnreadMessages();
		}
	}
	
	private boolean processUserSessionEvent(SignOnOffEvent event) {
		return processUserSessionEvent(event.getIdentityKey(), activeRosters)
			|| processUserSessionEvent(event.getIdentityKey(), requestedRosters)
			|| processUserSessionEvent(event.getIdentityKey(), completedRosters);
	}
	
	private boolean processUserSessionEvent(Long identityKey, List<SupervisedRoster> rosters) {
		for(SupervisedRoster roster:rosters) {
			for(SupervisedRosterEntry entry:roster.getRosterEntries()) {
				if(identityKey.equals(entry.getEntry().getIdentityKey())) {
					setOnlineStatus(identityKey, entry.getEntryLink());
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(toggleActiveLink == source) {
			toogleContainer(toggleActiveLink, activeContainer);
		} else if(toggleCompletedLink == source) {
			toogleContainer(toggleCompletedLink, completedContainer);
		} else if(toggleRequestedLink == source) {
			toogleContainer(toggleRequestedLink, requestedContainer);
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("entry".equals(link.getCmd()) && link.getUserObject() instanceof SupervisedRosterEntry) {
				doSelectEntry(ureq, (SupervisedRosterEntry)link.getUserObject());
			}
		}
		
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	protected void activateChannel(String channel) {
		SupervisedRoster activeRoster = findRoster(channel, activeRosters);
		SupervisedRoster requestedRoster = findRoster(channel, requestedRosters);
		if(activeRoster == null && requestedRoster == null) {
			SupervisedRoster completedRoster = findRoster(channel, completedRosters);
			if(completedRoster != null) {
				completedRosters.remove(completedRoster);
				activeRosters.add(completedRoster);
				Collections.sort(activeRosters, rosterComparator);
				reloadContainers();
			} else {
				loadModel();
			}
		} else if(requestedRoster != null) {
			requestedRosters.remove(requestedRoster);
			activeRosters.add(requestedRoster);
			Collections.sort(activeRosters, rosterComparator);
			reloadContainers();
		}
	}
	
	protected void requestChannel(String channel) {
		SupervisedRoster requestedRoster = findRoster(channel, requestedRosters);
		if(requestedRoster == null) {
			SupervisedRoster currentRoster = findRoster(channel, activeRosters);
			if(currentRoster != null) {
				activeRosters.remove(currentRoster);
			} else {
				currentRoster = findRoster(channel, completedRosters);
				if(currentRoster != null) {
					completedRosters.remove(currentRoster);
				}
			}
			if(currentRoster != null) {
				requestedRosters.add(currentRoster);
				Collections.sort(requestedRosters, rosterComparator);
				reloadContainers();
			}
		}
	}
	
	protected void completeChannel(String channel) {
		SupervisedRoster roster = findRoster(channel, activeRosters);
		if(roster == null) {
			roster = findRoster(channel, requestedRosters);
		}
		if(roster != null ) {
			activeRosters.remove(roster);
			requestedRosters.remove(roster);
			completedRosters.add(roster);
			Collections.sort(completedRosters, rosterComparator);
			reloadContainers();
		}
	}
	
	protected void reloadContainers() {
		Set<Long> identities = new HashSet<>();
		loadContainer(activeContainer, toggleActiveLink, activeRosters, identities);
		loadContainer(requestedContainer, toggleRequestedLink, requestedRosters, identities);
		loadContainer(completedContainer, toggleCompletedLink, completedRosters, identities);
		identityKeys = identities;
	}
	
	protected void switchChannel(String newChannel) {
		selectedChannel = newChannel;
		if(getRosterEntries(newChannel).isEmpty()) {
			loadModel();
		}
		updateSelectedChannel();
	}
	
	private void doSelectEntry(UserRequest ureq, SupervisedRosterEntry entry) {
		selectedChannel = entry.getEntry().getChannel();
		updateSelectedChannel();
		fireEvent(ureq, new SelectChannelEvent(chatResource, resSubPath, selectedChannel));
	}

	private void updateSelectedChannel() {
		updateSelectedChannel(activeRosters);
		updateSelectedChannel(completedRosters);
		updateSelectedChannel(requestedRosters);
	}
	
	private void updateSelectedChannel(List<SupervisedRoster> rosters) {
		for(SupervisedRoster roster:rosters) {
			for(SupervisedRosterEntry entry:roster.getRosterEntries()) {
				String css ;
				if(selectedChannel != null && selectedChannel.equals(entry.getEntry().getChannel())) {
					css = "active";
					entry.resetUnreadMessages();
				} else {
					css = "";
				}	
				entry.getEntryLink().setElementCssClass(css);
			}
		}
	}
	
	private void toogleContainer(FormLink link, FormLayoutContainer container) {
		Boolean position = (Boolean)link.getUserObject();
		boolean next = !position.booleanValue();
		
		container.setVisible(next);
		link.setUserObject(next);
		String css = next ? ICON_CLOSE : ICON_OPEN;
		link.setIconLeftCSS(css);
	}
	
	protected void updateModel() {
		flc.setDirty(true);
	}
	
	public static class SupervisedRoster {
		
		private int totalEntries;
		private final RosterChannelInfos roster;
		private List<SupervisedRosterEntry> rosterEntries;
		private final String cachedNames;
		
		public SupervisedRoster(RosterChannelInfos roster, List<SupervisedRosterEntry> rosterEntries, int totalEntries) {
			this.roster = roster;
			this.rosterEntries = rosterEntries;
			this.totalEntries = totalEntries;
			cachedNames = RosterEntryWithUnreadCellRenderer.getName(roster, false);
		}
		
		public String getNames() {
			return cachedNames;
		}
		
		public String getChannel() {
			return roster.getChannel();
		}
		
		public int getTotalEntries() {
			return totalEntries;
		}
		
		public void setTotalEntries(int totalEntries) {
			this.totalEntries = totalEntries;
		}
		
		public List<SupervisedRosterEntry> getRosterEntries() {
			return rosterEntries;
		}
		
		public SupervisedRosterEntry getRosterEntry(Long identityKey) {
			if(rosterEntries == null || identityKey == null) return null;
			
			for(SupervisedRosterEntry entry:rosterEntries) {
				if(identityKey.equals(entry.getEntry().getIdentityKey())) {
					return entry;
				}
			}
			return null;
		}
	}
	
	private static class SupervisedRosterComparator implements Comparator<SupervisedRoster> {
		
		private final Collator collator;
		
		public SupervisedRosterComparator(Locale locale) {
			collator = Collator.getInstance(locale);
		}

		@Override
		public int compare(SupervisedRoster o1, SupervisedRoster o2) {
			if(o1 == null || o2 == null) {
				return compareNullObjects(o1, o2);
			}
			String n1 = o1.getNames();
			String n2 = o2.getNames();
			if(n1 == null || n2 == null) {
				return compareNullObjects(n1, n2);
			}
			return collator.compare(n1, n2);
		}
		
		protected final int compareNullObjects(final Object a, final Object b) {
			boolean ba = (a == null);
			boolean bb = (b == null);
			return ba? (bb? 0: -1):(bb? 1: 0);
		}
	}
	
	public static class SupervisedRosterEntry {
		
		private final RosterEntry entry;
		private final FormLink entryLink;
		private long unreadMessages = 0l;
		
		public SupervisedRosterEntry(RosterEntry entry, FormLink entryLink, long unreadMessages) {
			this.entry = entry;
			this.entryLink = entryLink;
			this.unreadMessages = unreadMessages;
			entryLink.setUserObject(this);
		}
		
		public String getName() {
			return entry.isAnonym() ? entry.getNickName() : entry.getFullName();
		}
		
		public Long getAvatarKey() {
			return entry.getIdentityKey();
		}
		
		public RosterEntry getEntry() {
			return entry;
		}
		
		public FormLink getEntryLink() {
			return entryLink;
		}

		public long getUnreadMessages() {
			return unreadMessages;
		}

		public long incrementUnreadMessages() {
			++unreadMessages;
			entryLink.getComponent().setCustomDisplayText(getName() + " <strong>( " + unreadMessages + " <i class='o_icon o_icon_mail'> </i> )</strong>");
			return unreadMessages;
		}
		
		public void resetUnreadMessages() {
			unreadMessages = 0l;
			entryLink.getComponent().setCustomDisplayText(getName()); // reset unread message
		}
	}
}
