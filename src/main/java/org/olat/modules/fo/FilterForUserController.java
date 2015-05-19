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
package org.olat.modules.fo;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.commons.modules.bc.meta.tagged.MetaTagged;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkResourceStat;
import org.olat.core.commons.services.mark.MarkingService;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.link.LinkPopupSettings;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.table.BaseTableDataModelWithoutFilter;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.ajax.autocompletion.AutoCompleterController;
import org.olat.core.gui.control.generic.ajax.autocompletion.EntriesChosenEvent;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListProvider;
import org.olat.core.gui.control.generic.ajax.autocompletion.ListReceiver;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.AssertException;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.core.util.vfs.filters.VFSItemExcludePrefixFilter;
import org.olat.user.DisplayPortraitController;
import org.olat.user.UserInfoMainController;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * This controller is implementing a search by user. There is two ways to search:
 * a text field with autocomplete to select directly the user and the list of
 * users which have posted some messages in the forum.
 * <BR>
 * Events:
 * <ul>
 * 	<li>OpenMessageInThreadEvent</li>
 * <ul>
 * <P>
 * Initial Date:  18 sept. 2009 <br>
 * @author srosse
 */
public class FilterForUserController extends BasicController {

	private static final String CMD_SHOW = "show";
	private static final String CMD_HOMEPAGE = "homepage";
	
	private final Forum forum;
	private List<Message> msgs;
	private List<Message> threadMsgs;
	private List<Map<String, Object>> currentMessagesMap;
	private UsersTableModel usersTableModel;
	private UserListProvider userListProvider;
	private boolean forumChangedEventReceived = false;
	
	private final VelocityContainer mainVC;
	private VelocityContainer vcThreadView;
	private final TableController userListCtr;
	private final AutoCompleterController userAutoCompleterCtr;
	private final DateFormat dateFormat;
	private final StackedPanel searchPanel;
	
	private final OLATResourceable forumOres;
	private final String thumbMapper;
	
	@Autowired
	private ForumManager forumManager;

	public FilterForUserController(UserRequest ureq, WindowControl wControl, Forum forum) {
		super(ureq, wControl);
		this.forum = forum;
		
		msgs = forumManager.getMessagesByForum(forum);
		forumOres = OresHelper.createOLATResourceableInstance(Forum.class,forum.getKey());
		
		mainVC = createVelocityContainer("filter_for_user");
		
		List<UserInfo> userInfoList = getUserInfoList();
		userListProvider = new UserListProvider(userInfoList);

		boolean ajax = wControl.getWindowBackOffice().getWindowManager().getGlobalSettings().getAjaxFlags().isIframePostEnabled();
		mainVC.contextPut("ajax", new Boolean(ajax));
		
		// show key in result list, users that can see this filter have administrative rights
		userAutoCompleterCtr = new AutoCompleterController(ureq, wControl, userListProvider, null, true, 60, 3, null);
		listenTo(userAutoCompleterCtr);
		
		dateFormat = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, ureq.getLocale());

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setSortingEnabled(true);
		
		userListCtr = new TableController(tableConfig, ureq, wControl, getTranslator());
		DefaultColumnDescriptor  lastNameDesc = new DefaultColumnDescriptor("table.user.lastname", 0, CMD_HOMEPAGE, ureq.getLocale());
		lastNameDesc.setIsPopUpWindowAction(true, "height=600, width=900, location=no, menubar=no, resizable=yes, status=no, scrollbars=yes, toolbar=no");
		userListCtr.addColumnDescriptor(lastNameDesc);
		DefaultColumnDescriptor  firstNameDesc = new DefaultColumnDescriptor("table.user.firstname", 1, CMD_HOMEPAGE, ureq.getLocale());
		firstNameDesc.setIsPopUpWindowAction(true, "height=600, width=900, location=no, menubar=no, resizable=yes, status=no, scrollbars=yes, toolbar=no");
		userListCtr.addColumnDescriptor(firstNameDesc);
		userListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.user.replies", 2, null, ureq.getLocale()));
		userListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.user.threads", 3, null, ureq.getLocale()));
		userListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.lastModified", 4, null, ureq.getLocale()));
		userListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.numOfCharacters", 5, null, ureq.getLocale()));
		userListCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.numOfWords", 6, null, ureq.getLocale()));
		userListCtr.addColumnDescriptor(new StaticColumnDescriptor(CMD_SHOW, "viewswitch.title", getTranslator().translate("viewswitch.title")));
		
		usersTableModel = new UsersTableModel(userInfoList);
		userListCtr.setTableDataModel(usersTableModel);
		listenTo(userListCtr);
		
		mainVC.put("userAutoCompleter", userAutoCompleterCtr.getInitialComponent());
		mainVC.put("userList", userListCtr.getInitialComponent());
		
		//results
		vcThreadView = createVelocityContainer("threadview");

		// Mapper to display thumbnail images of file attachments
		thumbMapper = registerCacheableMapper(ureq, "fo_att_" + forum.getKey(), new Mapper() {
			@Override
			public MediaResource handle(String relPath, HttpServletRequest request) {
				String[] query = relPath.split("/"); // exptected path looks like this /messageId/attachmentUUID/filename
				if (query.length == 4) {
					try {
						Long mId = Long.valueOf(Long.parseLong(query[1]));
						Map<String, Object> map = null;
						for (Map<String, Object> m : currentMessagesMap) {
							// search for message in current message map
							if (m.get("id").equals(mId)) {
								map = m;
								break;
							}
						}
						if (map != null) {
							ArrayList<VFSItem> attachments = (ArrayList<VFSItem>) map.get("attachments");
							for (VFSItem vfsItem : attachments) {
								MetaInfo meta = ((MetaTagged)vfsItem).getMetaInfo();
								if (meta.getUUID().equals(query[2])) {
									if (meta.isThumbnailAvailable()) {
										VFSLeaf thumb = meta.getThumbnail(200, 200, false);
										if(thumb != null) {
											// Positive lookup, send as response
											return new VFSMediaResource(thumb);
										}
									}
									break;
								}
							}
						}
					} catch (NumberFormatException e) {
						logDebug("Could not parse attachment path::" + relPath, null);
					}
				}
				// In any error case, send not found
				return new NotFoundMediaResource(request.getRequestURI());
			}
		});					

		searchPanel = putInitialPanel(mainVC);
	}
	
	/**
	 * Is the controller displaying the results
	 * @return
	 */
	public boolean isShowResults() {
		return searchPanel.getContent() == vcThreadView;
	}
	
	/**
	 * set the controller to its initial status, the search form
	 */
	public void setShowSearch() {
		searchPanel.setContent(mainVC);
	}
	
	public void forumChanged() {
		forumChangedEventReceived = true;
	}
	
	private boolean checkForumChangedEventReceived() {
		if(forumChangedEventReceived) {
			loadMessages();
			forumChangedEventReceived = false;
			return true;
		}
		return false;
	}
	
	private void loadMessages() {
		
		msgs = ForumManager.getInstance().getMessagesByForum(forum);
		
		List<UserInfo> userInfoList = getUserInfoList();
		userListProvider.setUserInfos(userInfoList);
		usersTableModel = new UsersTableModel(userInfoList);
		userListCtr.setTableDataModel(usersTableModel);
	}
	
	protected List<UserInfo> getUserInfoList() {
		Map<Identity,UserInfo> infoMap = new HashMap<Identity,UserInfo>();
		
		for(Message msg:msgs) {
			Identity creator = msg.getCreator();
			if(creator == null) continue;
			UserInfo stats = infoMap.get(creator);
			if(stats == null) {
				stats = new UserInfo(creator);
				stats.setLastModified(msg.getLastModified());
				infoMap.put(creator, stats);
			} else {

				Date lastModified = msg.getLastModified();
				if(stats.getLastModified().compareTo(lastModified) > 0) {
					stats.setLastModified(lastModified);
				}
			}
			
			if(msg.getParent() == null) {
				stats.incThreads();
			} else {
				stats.incReplies();
			}
			stats.addNumOfCharacters(msg.getNumOfCharacters());
			stats.addNumOfWords(msg.getNumOfWords());
		}
		
		List<UserInfo> infoList = new ArrayList<UserInfo>(infoMap.values());
		Collections.sort(infoList);
		return infoList;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		checkForumChangedEventReceived();
		
		if (source instanceof Link) {
			// all other commands have the message value map id coded into the
			// the command name. get message from this id
			Link link = (Link) source;
			String command = link.getCommand();
			Map<String, Object> messageMap = getMessageMapFromCommand(ureq.getIdentity(), command);
			Long messageId = (Long) messageMap.get("id");
			
			Message selectedMessage = forumManager.loadMessage(messageId);
			if (selectedMessage != null) {
				if (command.startsWith("open_in_thread_")) {
					fireEvent(ureq, new OpenMessageInThreadEvent(selectedMessage));
				}
			}
		}
	}
	
	/**
	 * Get the message value map from a velocity command. The command must have
	 * the signature commandname_messagemapid
	 * 
	 * @param identity
	 * @param command
	 * @return Map the value map for the current message
	 */
	private Map<String, Object> getMessageMapFromCommand(Identity identity, String command) {
		String cmdId = command.substring(command.lastIndexOf("_") + 1);
		try {
			Integer id = Integer.valueOf(cmdId);
			return currentMessagesMap.get(id.intValue());
		} catch (NumberFormatException e) {
			throw new AssertException("Tried to parse forum message id from command::" + command
					+ " but message id was not a long. Could be a user who tries to hack the system. User::" + identity.getName(), e);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		checkForumChangedEventReceived();
		
		if(source == userListCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				
				int rowid = te.getRowId();
				final UserInfo selectedInfo = usersTableModel.getUserInfo(rowid);
				if (te.getActionId().equals(CMD_SHOW)) {
					showMessages(ureq, selectedInfo);
				} else if (te.getActionId().equals(CMD_HOMEPAGE)) {
					ControllerCreator ctrlCreator = new ControllerCreator() {
						@Override
						public Controller createController(UserRequest lureq, WindowControl lwControl) {
							return new UserInfoMainController(lureq, lwControl, selectedInfo.getIdentity(), true, false);
						}
					};
					// wrap the content controller into a full header layout
					ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
					// open in new browser window
					PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq,
							layoutCtrlr);
					pbw.open(ureq);
				}
			}
		} else if (source == userAutoCompleterCtr) {
			if(event instanceof EntriesChosenEvent) {
				List<String> selectedUsernames = ((EntriesChosenEvent)event).getEntries();
				if(selectedUsernames.size() == 1) {
					String username = selectedUsernames.get(0);
					UserInfo selectedInfo = usersTableModel.getUserInfo(username);
					if(selectedInfo != null) {
						showMessages(ureq, selectedInfo);
					}
				}
			}
		}
	}
	
	protected void showMessages(UserRequest ureq, UserInfo selectedInfo) {
		// for simplicity no reuse of container, always create new one
		vcThreadView = createVelocityContainer("threadview");
		vcThreadView.contextPut("filteredForFirstName", selectedInfo.getFirstName());
		vcThreadView.contextPut("filteredForLastName", selectedInfo.getLastName());

		vcThreadView.contextPut("isClosed", Boolean.FALSE); 
		vcThreadView.contextPut("isHidden",Boolean.FALSE);		

		vcThreadView.contextPut("showThreadTable", Boolean.FALSE);
		vcThreadView.contextPut("threadMode", Boolean.FALSE);
		vcThreadView.contextPut("msgDeepMap", null);
		
		threadMsgs = getMessages(selectedInfo.getIdentity());

		// add all messages that are needed
		currentMessagesMap = new ArrayList<Map<String, Object>>(threadMsgs.size());
		
		// load marks
		MarkingService markingService = (MarkingService)CoreSpringFactory.getBean(MarkingService.class);
		List<String> markResSubPath = new ArrayList<String>();
		for(Message threadMsg:threadMsgs) {
			markResSubPath.add(threadMsg.getKey().toString());
		}
		List<Mark> markList =  markingService.getMarkManager().getMarks(forumOres, ureq.getIdentity(), markResSubPath);
		Map<String,Mark> marks = new HashMap<String,Mark>(markList.size() * 2 + 1);
		for(Mark mark:markList) {
			marks.put(mark.getResSubPath(), mark);
		}
		List<MarkResourceStat> statList =  markingService.getMarkManager().getStats(forumOres, markResSubPath, null);
		Map<String,MarkResourceStat> stats = new HashMap<String,MarkResourceStat>(statList.size() * 2 + 1);
		for(MarkResourceStat stat:statList) {
			stats.put(stat.getSubPath(), stat);
		}
			
		// all messages in flat view
		List<Message> orderedMessages = new ArrayList<Message>();
					
		orderedMessages.addAll(threadMsgs);
		orderedMessages = threadMsgs;
		Collections.sort(orderedMessages, new MessageComparatorByDate());
		
		int msgNum = 0;
		for(Message msg:orderedMessages) {
			addMessageToCurrentMessagesAndVC(ureq, msg, vcThreadView, currentMessagesMap, msgNum++, marks, stats);
		}

		vcThreadView.contextPut("messages", currentMessagesMap);

		// Mapper to display thumbnail images of file attachments
		vcThreadView.contextPut("thumbMapper", thumbMapper);
		// add security callback
		vcThreadView.contextPut("security", new SearchForumCallback());
		searchPanel.setContent(vcThreadView);
	}
	
	//TODO this method is very similar to the same in ForumController
	private void addMessageToCurrentMessagesAndVC(UserRequest ureq, Message m, VelocityContainer vcContainer, List<Map<String, Object>> allList, int msgCount, Map<String,Mark> marks, Map<String,MarkResourceStat> stats) {
		// all values belonging to a message are stored in this map
		// these values can be accessed in velocity. make sure you clean up
		// everything
		// you create here in disposeCurrentMessages()!
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", m.getKey());
		
		map.put("newMessage", Boolean.FALSE);

		// add some data now
		Date creationDate = m.getCreationDate();
		Identity modifier = m.getModifier();
		if (modifier != null) {
			map.put("isModified", Boolean.TRUE);
			map.put("modfname",StringHelper.escapeHtml( modifier.getUser().getProperty(UserConstants.FIRSTNAME, ureq.getLocale())));
			map.put("modlname", StringHelper.escapeHtml(modifier.getUser().getProperty(UserConstants.LASTNAME, ureq.getLocale())));
		} else {
			map.put("isModified", Boolean.FALSE);
		}
		map.put("title", m.getTitle());
		map.put("body", m.getBody());
		map.put("date", dateFormat.format(creationDate));
		Identity creator = m.getCreator();
		
		
		map.put("firstname", StringHelper.escapeHtml(Formatter.truncate(creator.getUser().getProperty(UserConstants.FIRSTNAME, ureq.getLocale()), 18))); //keeps the first 15 chars
		map.put("lastname", StringHelper.escapeHtml(Formatter.truncate(creator.getUser().getProperty(UserConstants.LASTNAME, ureq.getLocale()), 18)));
		
		String subPath = m.getKey().toString();
		Mark currentMark = marks.get(subPath);
		MarkResourceStat stat = stats.get(subPath);
		MarkingService markingService = (MarkingService)CoreSpringFactory.getBean(MarkingService.class);
		
		if(!ureq.getUserSession().getRoles().isGuestOnly()) {
			String businessPath = currentMark == null ?
					getWindowControl().getBusinessControl().getAsString() + "[Message:" + m.getKey() + "]"
					: currentMark.getBusinessPath();
			Controller markCtrl = markingService.getMarkController(ureq, getWindowControl(), currentMark, stat, forumOres, subPath, businessPath);
			vcThreadView.put("mark_"+msgCount, markCtrl.getInitialComponent());
		}
		
		map.put("modified", dateFormat.format(m.getLastModified()));
		// message attachments
		ForumManager fm = ForumManager.getInstance();
		OlatRootFolderImpl msgContainer = fm.getMessageContainer(forum.getKey(), m.getKey());
		map.put("messageContainer", msgContainer);
		List<VFSItem> attachments = new ArrayList<VFSItem>(msgContainer.getItems(new VFSItemExcludePrefixFilter(MessageEditController.ATTACHMENT_EXCLUDE_PREFIXES)));
//		List attachments = msgContainer.getItems();
		map.put("attachments", attachments);
		if (attachments == null || attachments.size() == 0) map.put("hasAttachments", Boolean.FALSE);
		else map.put("hasAttachments", Boolean.TRUE);
		// number of children and modify/delete permissions
		map.put("nOfCh", new Integer(1));
		boolean userIsMsgCreator = ureq.getIdentity().getKey().equals(creator.getKey());
		Boolean uIsMsgC = new Boolean(userIsMsgCreator);
		map.put("uIsMsgC", uIsMsgC);
		boolean isThreadtop = m.getThreadtop()==null;
		map.put("isThreadtop", Boolean.valueOf(isThreadtop));
		boolean isThreadClosed = Status.getStatus(m.getStatusCode()).isClosed(); 
		if(!isThreadtop) {
			isThreadClosed = Status.getStatus(m.getThreadtop().getStatusCode()).isClosed();
		}
		map.put("isThreadClosed", isThreadClosed);
		// add portrait to map for later disposal and key for rendering in velocity
		DisplayPortraitController portrait = new DisplayPortraitController(ureq, getWindowControl(), m.getCreator(), true, true);
		// add also to velocity
		map.put("portrait", portrait);
		String portraitComponentVCName = m.getKey().toString();
		map.put("portraitComponentVCName", portraitComponentVCName);
		vcContainer.put(portraitComponentVCName, portrait.getInitialComponent());
		// Add link with username that is clickable
		String creatorFullName = StringHelper.escapeHtml(UserManager.getInstance().getUserDisplayName(creator));
		Link vcLink = LinkFactory.createCustomLink("vc_"+msgCount, "vc_"+msgCount, creatorFullName, Link.LINK_CUSTOM_CSS + Link.NONTRANSLATED, vcThreadView, this);
		vcLink.setUserObject(msgCount);
		LinkPopupSettings settings = new LinkPopupSettings(800, 600, "_blank");
		vcLink.setPopup(settings);
		allList.add(map);
		
		Link link = LinkFactory.createCustomLink("open_in_thread_"+msgCount, "open_in_thread_"+msgCount, "msg.open_in_thread", Link.BUTTON_SMALL, vcThreadView, this);
		link.setIconRightCSS("o_icon o_icon_start");
	}
	
	private List<Message> getMessages(Identity identity) {
		List<Message> usersMessages = new ArrayList<Message>();
		for(Message msg:msgs) {
			if(identity.equals(msg.getCreator())) {
				usersMessages.add(msg);
			}
		}
		return usersMessages;
	}
	
	/**
	 * Provider for the autocomplete
	 */
	public class UserListProvider implements ListProvider {
		private List<UserInfo> stats;
		
		public UserListProvider(List<UserInfo> stats) {
			this.stats = stats;
		}

		public List<UserInfo> getUserInfos() {
			return stats;
		}

		public void setUserInfos(List<UserInfo> stats) {
			this.stats = stats;
		}

		public void getResult(String searchValue, ListReceiver receiver) {
			searchValue = searchValue == null ? null : searchValue.toLowerCase();
			for(UserInfo info:stats) {
				Identity identity = info.getIdentity();
				String name = identity.getName();
				
				if(identity.getName().startsWith(searchValue)) {
					receiver.addEntry(name, name, info.getLastName() + " " + info.getFirstName(), CSSHelper.CSS_CLASS_USER);
				} else if(info.getFirstNameLowerCase().startsWith(searchValue)) {
					receiver.addEntry(name, name, info.getLastName() + " " + info.getFirstName(), CSSHelper.CSS_CLASS_USER);
				} else if(info.getLastNameLowerCase().startsWith(searchValue)) {
					receiver.addEntry(name, name, info.getLastName() + " " + info.getFirstName(), CSSHelper.CSS_CLASS_USER);
				}
			}
		}
	}
	
	/**
	 * Read-only security callback
	 */
	public class SearchForumCallback implements ForumCallback {

		public SubscriptionContext getSubscriptionContext() {
			return null;
		}

		public boolean mayArchiveForum() {
			return false;
		}

		public boolean mayDeleteMessageAsModerator() {
			return false;
		}

		public boolean mayEditMessageAsModerator() {
			return false;
		}

		public boolean mayFilterForUser() {
			return true;
		}

		public boolean mayOpenNewThread() {
			return false;
		}

		public boolean mayReplyMessage() {
			return false;
		}
	}
	
	/**
	 * TableDataModel for the overview of all users in the forum
	 */
	public class UsersTableModel extends BaseTableDataModelWithoutFilter<UserInfo> implements TableDataModel<UserInfo> {
		private final List<UserInfo> infos;
		
		public UsersTableModel(List<UserInfo> infos) {
			this.infos = infos;
		}
		
		public UserInfo getUserInfo(String username) {
			for(UserInfo info:infos) {
				Identity id = info.getIdentity();
				if(username.equals(id.getName())) {
					return info;
				}
			}
			return null;
		}
		
		public UserInfo getUserInfoByKey(Long key) {
			for (UserInfo info : infos) {
				Identity id = info.getIdentity();
				if (key.equals(id.getKey())) { return info; }
			}
			return null;
		}
		
		public UserInfo getUserInfo(int rowid) {
			if(rowid >= 0 && rowid < infos.size()) {
				return infos.get(rowid);
			}
			return null;
		}

		public int getColumnCount() {
			return 5;
		}

		public int getRowCount() {
			return infos.size();
		}

		public Object getValueAt(int row, int col) {
			UserInfo userStats = infos.get(row);
			switch(col) {
				case 0: return userStats.getLastName();
				case 1: return userStats.getFirstName();
				case 2: return Integer.toString(userStats.getReplies());
				case 3: return Integer.toString(userStats.getThreads());
				case 4: {
					if(userStats.getLastModified() == null)
						return "";
					return dateFormat.format(userStats.getLastModified());
				}
				case 5: return userStats.getNumOfCharacters();
				case 6: return userStats.getNumOfWords();
				case 7: return userStats;
				default: return "";
			}
		}

	}
	
	/**
	 * Wrapper for all the statistics needed by the overview of all users
	 * and the autocomplete
	 */
	public class UserInfo  implements Comparable<UserInfo> {
		private int replies = 0;
		private int threads = 0;
		private int numOfCharacters = 0;
		private int numOfWords = 0;
		private Date lastModified;
		private final Identity identity;
		
		private String firstName;
		private String lastName;
		private String firstNameLowerCase;
		private String lastNameLowerCase;
		
		public UserInfo(Identity identity) {
			this.identity = identity;
			
			firstName = identity.getUser().getProperty(UserConstants.FIRSTNAME, getLocale());
			lastName = identity.getUser().getProperty(UserConstants.LASTNAME, getLocale());
			firstNameLowerCase = firstName.toLowerCase();
			lastNameLowerCase = lastName.toLowerCase();
		}

		public Identity getIdentity() {
			return identity;
		}
		
		public int getReplies() {
			return replies;
		}

		public int getThreads() {
			return threads;
		}

		public int getNumOfCharacters() {
			return numOfCharacters;
		}

		public int getNumOfWords() {
			return numOfWords;
		}

		public Date getLastModified() {
			return lastModified;
		}

		public void setLastModified(Date lastModified) {
			this.lastModified = lastModified;
		}

		public String getFirstName() {
			return firstName;
		}

		public String getLastName() {
			return lastName;
		}

		public String getFirstNameLowerCase() {
			return firstNameLowerCase;
		}

		public String getLastNameLowerCase() {
			return lastNameLowerCase;
		}

		public void incReplies() {
			replies++;
		}
		
		public void incThreads() {
			threads++;
		}
		
		public void addNumOfCharacters(Integer characters) {
			if(characters != null) {
				numOfCharacters += characters.intValue();
			}
		}
		
		public void addNumOfWords(Integer words) {
			if(words != null) {
				numOfWords += words.intValue();
			}
		}

		public int compareTo(UserInfo o) {
			if(o == null) return -1;
			String l1 = getLastNameLowerCase();
			String l2 = o.getLastNameLowerCase();
			if(l1 == null)
				return -1;
			if(l2 == null)
				return 1;
			return l1.compareTo(l2);
		}
	}
	
	/**
	 * Sort the message by date, ascending
	 */
	private class MessageComparatorByDate implements Comparator<Message> {
		public int compare(Message o1, Message o2) {
			Date d1 = o1.getLastModified();
			Date d2 = o2.getLastModified();
			
			if(d1 == null)
				return -1;
			else if(d2 == null)
				return 1;
			return d1.compareTo(d2);
		}
	}
}
