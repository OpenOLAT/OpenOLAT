/**
* OLAT - Online Learning and Training<br>
* http://www.olat.orgrmform
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
* <p>
*/ 

package org.olat.modules.fo;

import java.io.File;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.commons.modules.bc.meta.tagged.MetaTagged;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkResourceStat;
import org.olat.core.commons.services.mark.MarkingService;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.link.LinkPopupSettings;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.table.BooleanColumnDescriptor;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.components.table.CustomCssCellRenderer;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.GenericObjectArrayTableDataModel;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATSecurityException;
import org.olat.core.logging.activity.ILoggingAction;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.ConsumableBoolean;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.core.util.vfs.filters.VFSItemExcludePrefixFilter;
import org.olat.modules.fo.archiver.ForumArchiveManager;
import org.olat.modules.fo.archiver.formatters.ForumRTFFormatter;
import org.olat.portfolio.EPUIFactory;
import org.olat.search.SearchServiceUIFactory;
import org.olat.search.SearchServiceUIFactory.DisplayOption;
import org.olat.user.DisplayPortraitController;
import org.olat.user.UserManager;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description: <br>
 * CREATE: - new thread (topmessage) -> ask ForumCallback 'mayOpenNewThread' -
 * new message -> ask ForumCallback 'mayReplyMessage' <br>
 * READ: - everybody may read every message <br>
 * UPDATE: - who wrote a message may edit and save his message as long as it has
 * no children. - if somebody want to edit a message of somebodyelse -> ask
 * ForumCallback 'mayEditMessageAsModerator' <br>
 * DELETE: - who wrote a message may delete his message as long as it has no
 * children. - if somebody want to delete a message of somebodyelse -> ask
 * ForumCallback 'mayDeleteMessageAsModerator' <br>
 * <br>
 * Notifications: notified when: <br>
 * a new thread is opened <br>
 * a new reply is given <br>
 * a message has been edited <br>
 * but not when a message has been deleted <br>
 * 
 * @author Felix Jost
 * @author Refactorings: Roman Haag, roman.haag@frentix.com, frentix GmbH
 */
public class ForumController extends BasicController implements GenericEventListener, Activateable2 {

	protected static final String TINYMCE_EMPTYLINE_CODE = "<p>&nbsp;</p>"; //is pre/appended to quote message to allow writing inside.

	private static final String CMD_SHOWDETAIL = "showdetail";
	private static final String CMD_SHOWMARKED = "showmarked";
	private static final String CMD_SHOWNEW = "shownew";

	protected static final String GUI_PREFS_THREADVIEW_KEY = "forum.threadview.enabled";

	private ForumCallback focallback;

	Message currentMsg; // current Msg (in detailview), so we know after
	private Formatter f;

	private Collator collator; 

	private StackedPanel forumPanel;

	private VelocityContainer vcListTitles;
	private VelocityContainer vcEditMessage;
	private VelocityContainer vcThreadView;
	private VelocityContainer vcFilterView;
	private Link msgCreateButton;
	private Link archiveForumButton;
	private Link archiveThreadButton;
	private Link backLinkListTitles;
	private Link backLinkSearchListTitles;
	private List<Map<String, Object>> currentMessagesMap;
	private Link closeThreadButton;
	private Link openThreadButton;
	private Link hideThreadButton;
	private Link showThreadButton;
	private Link filterForUserButton;
	
	private TableController allThreadTableCtr;
	private TableController singleThreadTableCtr;
	
	private GenericObjectArrayTableDataModel attdmodel;
	private ForumMessagesTableDataModel sttdmodel;
	private ForumManager fm;
	private Forum forum;
	private List<Message> msgs;
	private List<Message> threadMsgs;
	private Set<Long> rms; // all keys from messages that the user already read
	private boolean forumChangedEventReceived = false;

	private DialogBoxController yesno, yesNoSplit, archiveFoDiaCtr, archiveThDiaCtr;
	private TableController moveMessageTableCtr;
	List<Message> threadList;
	private CloseableModalController cmcMoveMsg;

	private SubscriptionContext subsContext;
	private ContextualSubscriptionController csc;

	private MessageEditController msgEditCtr;
	private CloseableModalController msgEditCmc;
	private ForumThreadViewModeController viewSwitchCtr;
	private Map<Long, Integer> msgDeepMap;
	
	private boolean searchMode = false;
	private FilterForUserController filterForUserCtr;
	
	private Controller searchController;
	
	private final OLATResourceable forumOres;
	
	private final String thumbMapper;

	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private UserManager userManager;

	/**
	 * @param forum
	 * @param focallback
	 * @param ureq
	 * @param wControl
	 */
	public ForumController(UserRequest ureq, WindowControl wControl,
			Forum forum, ForumCallback focallback, boolean showSubscriptionButton) {
		super(ureq, wControl);
		this.forum = forum;
		this.focallback = focallback;
		addLoggingResourceable(LoggingResourceable.wrap(forum));
		
		forumOres = OresHelper.createOLATResourceableInstance(Forum.class,forum.getKey());
		f = Formatter.getInstance(ureq.getLocale());
		fm = ForumManager.getInstance();
				
		msgs = fm.getMessagesByForum(forum);

		collator = Collator.getInstance(ureq.getLocale());
		collator.setStrength(Collator.PRIMARY);

		forumPanel = new SimpleStackedPanel("forumPanel");
		forumPanel.addListener(this);

		//create page
		vcListTitles = createVelocityContainer("list_titles");
		
		msgCreateButton = LinkFactory.createButtonSmall("msg.create", vcListTitles, this);
		msgCreateButton.setIconLeftCSS("o_icon o_icon-fw o_forum_status_thread_icon");
		msgCreateButton.setElementCssClass("o_sel_forum_thread_new");
		archiveForumButton = LinkFactory.createButtonSmall("archive.forum", vcListTitles, this);
		archiveForumButton.setIconLeftCSS("o_icon o_icon-fw o_icon_archive_tool");
		archiveForumButton.setElementCssClass("o_sel_forum_archive");
		
		if(securityModule.isUserAllowedAutoComplete(ureq.getUserSession().getRoles())) {
			filterForUserButton = LinkFactory.createButtonSmall("filter", vcListTitles, this);
			filterForUserButton.setIconLeftCSS("o_icon o_icon-fw o_icon_user");
			filterForUserButton.setElementCssClass("o_sel_forum_filter");
		}
		
		if(!this.isGuestOnly(ureq)) {
		  SearchServiceUIFactory searchServiceUIFactory = (SearchServiceUIFactory)CoreSpringFactory.getBean(SearchServiceUIFactory.class);
		  searchController = searchServiceUIFactory.createInputController(ureq, wControl, DisplayOption.STANDARD, null);
		  listenTo(searchController);
		  vcListTitles.put("search_input", searchController.getInitialComponent());
		}
		
		// a list of titles of all messages in all threads
		vcListTitles.contextPut("security", focallback);

		// --- subscription ---
		subsContext = focallback.getSubscriptionContext();
		// if sc is null, then no subscription is desired
		if (subsContext != null && showSubscriptionButton) {
			String businessPath = wControl.getBusinessControl().getAsString();
			String data = String.valueOf(forum.getKey());
			PublisherData pdata = new PublisherData(OresHelper.calculateTypeName(Forum.class), data, businessPath);
			
			csc = new ContextualSubscriptionController(ureq, getWindowControl(), subsContext, pdata);
			listenTo(csc);
			
			vcListTitles.put("subscription", csc.getInitialComponent());
		}

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setCustomCssClass("o_forum");
		tableConfig.setSelectedRowUnselectable(true);
		tableConfig.setDownloadOffered(false);
		tableConfig.setTableEmptyMessage(translate("forum.emtpy"));

		allThreadTableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(allThreadTableCtr);
		allThreadTableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.typeimg", 0, null, 
				ureq.getLocale(), ColumnDescriptor.ALIGNMENT_LEFT, new MessageIconRenderer()));
		allThreadTableCtr.addColumnDescriptor(new StickyRenderColumnDescriptor("table.thread", 1, CMD_SHOWDETAIL, ureq.getLocale(),
				ColumnDescriptor.ALIGNMENT_LEFT, new StickyThreadCellRenderer()));		
		allThreadTableCtr.addColumnDescriptor(new StickyColumnDescriptor("table.userfriendlyname", 2, null, ureq.getLocale()));
		allThreadTableCtr.addColumnDescriptor(new StickyColumnDescriptor("table.lastModified", 3, null, ureq.getLocale(),
				ColumnDescriptor.ALIGNMENT_CENTER));					
		  allThreadTableCtr.addColumnDescriptor(new StickyColumnDescriptor("table.marked", 4, CMD_SHOWMARKED, ureq.getLocale(),
				ColumnDescriptor.ALIGNMENT_RIGHT));		
		  allThreadTableCtr.addColumnDescriptor(new StickyColumnDescriptor("table.unread", 5, CMD_SHOWNEW, ureq.getLocale(),
				ColumnDescriptor.ALIGNMENT_RIGHT));
		  allThreadTableCtr.addColumnDescriptor(new StickyColumnDescriptor("table.total", 6, null, ureq.getLocale(),
				ColumnDescriptor.ALIGNMENT_RIGHT));
				
		singleThreadTableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		listenTo(singleThreadTableCtr);
		singleThreadTableCtr.addColumnDescriptor(new ThreadColumnDescriptor("table.title", 0, CMD_SHOWDETAIL));
		singleThreadTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.userfriendlyname", 1, null, ureq.getLocale()));
		singleThreadTableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.modified", 2, null, ureq.getLocale(),
				ColumnDescriptor.ALIGNMENT_CENTER));
		singleThreadTableCtr
				.addColumnDescriptor(new BooleanColumnDescriptor("table.header.state", 3, "", translate("table.row.new")));
		
		rms = getReadSet(ureq.getIdentity()); // here we fetch which messages had		
				
		// been read by the user
		threadList = prepareListTitles(msgs);
		
		//precalulate message deepness
		precalcMessageDeepness(msgs);
		
		// Default view
		forumPanel = putInitialPanel(vcListTitles);
		// jump to either the forum or the folder if the business-launch-path says so.
		BusinessControl bc = getWindowControl().getBusinessControl();
		ContextEntry ce = bc.popLauncherContextEntry();
		if ( ce != null) { // a context path is left for me
			if (isLogDebugEnabled()) logDebug("businesscontrol (for further jumps) would be: ", bc.toString());
			OLATResourceable ores = ce.getOLATResourceable();
			if (isLogDebugEnabled()) logDebug("OLATResourceable= " , ores.toString());
			Long resId = ores.getResourceableId();
			if (resId.longValue() != 0) {
				if (isLogDebugEnabled()) logDebug("messageId=" , ores.getResourceableId().toString());
				currentMsg = fm.loadMessage(ores.getResourceableId());
				if (currentMsg != null) {
					showThreadView(ureq, currentMsg, null);
					scrollToCurrentMessage();					
				} else {
					// message not found, do nothing. Load normal start screen
					showError("deleteok");
					logDebug("Invalid messageId=", ores.getResourceableId().toString());
				}
			} else {
				//FIXME:chg: Should not happen, occurs when course-node are called
				if (isLogDebugEnabled()) logDebug("Invalid messageId=" , ores.getResourceableId().toString());
			}
		}
		
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

		// Register for forum events
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), forum);
		
		//filter for user
		vcFilterView = createVelocityContainer("filter_view");
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		Long resId = entries.get(0).getOLATResourceable().getResourceableId();
		if (resId.longValue() != 0) {
			currentMsg = fm.loadMessage(resId);
			if (currentMsg != null) {
				showThreadView(ureq, currentMsg, null);
				scrollToCurrentMessage();					
			}
		}
	}

	/**
	 * If event received, must update thread overview.
	 */
	private boolean checkForumChangedEventReceived() {
		if(forumChangedEventReceived) {
			this.showThreadOverviewView();
			forumChangedEventReceived = false;
			return true;
		}
		return false;
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if(checkForumChangedEventReceived()) {
			return;
		}
		String cmd = event.getCommand();
		if (source == msgCreateButton){
			showNewThreadView(ureq);
		} else if (source == archiveForumButton){
			archiveFoDiaCtr = activateYesNoDialog(ureq, null, translate("archive.forum.dialog"), archiveFoDiaCtr);
		} else if (source == filterForUserButton) {
			showFilterForUserView(ureq);
		} else if (source == backLinkListTitles){ // back link list titles
			if(searchMode && filterForUserCtr != null && filterForUserCtr.isShowResults()) {
				forumPanel.setContent(vcFilterView);
			} else {
				searchMode = false;
				showThreadOverviewView();
			}
		} else if (source == backLinkSearchListTitles) {
			if(filterForUserCtr != null && filterForUserCtr.isShowResults()) {
				filterForUserCtr.setShowSearch();
			} else {
				showThreadOverviewView();
			}
		} else if (source == archiveThreadButton){
			archiveThDiaCtr = activateYesNoDialog(ureq, null, translate("archive.thread.dialog"), archiveThDiaCtr);
		} else if (source == closeThreadButton) {
			closeThread(currentMsg, true);
		} else if (source == openThreadButton) {
			closeThread(currentMsg, false);
		} else if (source == hideThreadButton) {
			hideThread(currentMsg, true);
		} else if (source == showThreadButton) {
			hideThread(currentMsg, false);		
		}	else if (source == vcThreadView) {
			if (cmd.startsWith("attachment_")) {
				Map<String, Object> messageMap = getMessageMapFromCommand(ureq.getIdentity(), cmd);
				Long messageId = (Long) messageMap.get("id");
				currentMsg = fm.loadMessage(messageId);
				doAttachmentDelivery(ureq, cmd, messageMap);
			}
		} else if (source instanceof Link) {
			// all other commands have the message value map id coded into the
			// the command name. get message from this id
			Link link = (Link) source;
			String command = link.getCommand();
			Map<String, Object> messageMap = getMessageMapFromCommand(ureq.getIdentity(), command);
			Long messageId = (Long) messageMap.get("id");
			
			Message updatedMessage = fm.loadMessage(messageId);
			if (updatedMessage!=null) {
				currentMsg = updatedMessage;
				// now dispatch the commands
				if (command.startsWith("qt_")) {
					showReplyView(ureq, true, currentMsg);
				} else if (command.startsWith("rp_")) {
					showReplyView(ureq, false, currentMsg);
				} else if (command.startsWith("dl_")) {
					showDeleteMessageView(ureq);
				} else if (command.startsWith("ed_")) {
					showEditMessageView(ureq);
				}	else if (command.startsWith("split_")) {
					showSplitThreadView(ureq);
				} else if (command.startsWith("move_")) {
					showMoveMessageView(ureq);
				} else if (command.startsWith("vc_")) {
					Map<String, Object> map = currentMessagesMap.get((Integer)link.getUserObject());
					DisplayPortraitController dpC = (DisplayPortraitController) map.get("portrait");
					if (dpC != null) {
						dpC.showUserInfo(ureq);
					}
				}
			} else if (currentMsg != null) {
				showInfo("header.cannoteditmessage");
				showThreadOverviewView();
			}			
		} 	
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if(checkForumChangedEventReceived()) {
			return;
		}
		if (source == yesno) {
			if (DialogBoxUIFactory.isYesEvent(event)) { // yes
				doDeleteMessage(ureq);
				if (currentMsg.getThreadtop() == null) {
					showThreadOverviewView(); // was last message in thread
				} else {
					showThreadView(ureq, currentMsg.getThreadtop(), null);
				}
			}
		} else if (source == archiveFoDiaCtr) {
			if (DialogBoxUIFactory.isYesEvent(event)) { // ok
				doArchiveForum(ureq);
				showInfo("archive.forum.successfully");
			}
		} else if (source == archiveThDiaCtr) {
			if (DialogBoxUIFactory.isYesEvent(event)) { // ok
				doArchiveThread(ureq, currentMsg);
				showInfo("archive.thread.successfully");
			}
		} else if (source == singleThreadTableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				// user has selected a message title from singleThreadTable
				// -> display message details and below all messages with the same
				// topthread_id as the selected one
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				if (actionid.equals(CMD_SHOWDETAIL)) {
					int rowid = te.getRowId();
					Message m = sttdmodel.getObjects().get(rowid);
					showThreadView(ureq, m, null);
					ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_MESSAGE_READ, getClass(), LoggingResourceable.wrap(currentMsg));
				}
			}
		} else if (source == allThreadTableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				int rowid = te.getRowId();
				Object[] msgWrapper = attdmodel.getObjects().get(rowid);
				int size = msgWrapper.length;
				Message m = (Message) msgWrapper[size-1];		
				if (actionid.equals(CMD_SHOWDETAIL)) {			
					showThreadView(ureq, m, null);
				} else if (CMD_SHOWMARKED.equals(actionid)) {
					showThreadView(ureq, m, ForumThreadViewModeController.VIEWMODE_MARKED);
				} else if (CMD_SHOWNEW.equals(actionid)) {
					showThreadView(ureq, m, ForumThreadViewModeController.VIEWMODE_NEW);
				}
			}
		} else if (source == yesNoSplit) {
			// the dialogbox is already removed from the gui stack - do not use getWindowControl().pop(); to remove dialogbox
			if (DialogBoxUIFactory.isYesEvent(event)){
				splitThread(ureq);				
			}
		} else if (source == moveMessageTableCtr) {
			TableEvent te = (TableEvent)event;
			Message topMsg = threadList.get(te.getRowId());
			moveMessage(ureq, topMsg);
		} else if(source == msgEditCmc) {
			removeAsListenerAndDispose(msgEditCmc);
			removeAsListenerAndDispose(msgEditCtr);
			msgEditCtr = null;
			msgEditCmc = null;
		}
		// events from messageEditor
		else if (source == msgEditCtr){
			//persist changed or new message
			if (event == Event.DONE_EVENT){
				if (msgEditCtr.getLastEditModus().equals(MessageEditController.EDITMODE_NEWTHREAD)){
					// creation done -> save
					doNewThread(ureq);
					msgEditCtr.persistTempUploadedFiles(currentMsg);
				} else if (msgEditCtr.getLastEditModus().equals(MessageEditController.EDITMODE_EDITMSG)){
					// edit done -> save 
					Message updatedMessage = fm.loadMessage(currentMsg.getKey());
					if(updatedMessage!=null) {
					  doEditMessage(ureq);
					  //file persisting is done already, as a msg-key was known during edit.
					}	else {
					  showInfo("header.cannoteditmessage");
					}
				} else if (msgEditCtr.getLastEditModus().equals(MessageEditController.EDITMODE_REPLYMSG)){
					// reply done -> save
					Message updatedMessage = fm.loadMessage(currentMsg.getKey());
				  if(updatedMessage!=null) {	
					  doReplyMessage(ureq);
					  msgEditCtr.persistTempUploadedFiles(currentMsg);
				  } else {
				  	showInfo("header.cannotsavemessage");
				  }
				}
				//show thread view after all kind of operations
				showThreadView(ureq, currentMsg, null);
			
			//editor was canceled
			} else if (event == Event.CANCELLED_EVENT) {
				// back to 'list all titles' if canceled on new thread
				if (msgEditCtr.getLastEditModus().equals(MessageEditController.EDITMODE_NEWTHREAD)){
					forumPanel.setContent(vcListTitles);
				} else {
					showThreadView(ureq, currentMsg, null);
				}
			}
			if(msgEditCmc != null) {
				msgEditCmc.deactivate();
			}
			cleanUp();
		} else if (source == viewSwitchCtr){
			if (event == Event.CHANGED_EVENT){
				//viewmode has been switched, so change view:
				String mode = viewSwitchCtr.getSelectedViewMode();
				showThreadView(ureq, currentMsg, mode);
			}
		} else if (source == filterForUserCtr) {
			if(event instanceof OpenMessageInThreadEvent) {
				OpenMessageInThreadEvent openEvent = (OpenMessageInThreadEvent)event;
				Message selectedMsg = openEvent.getMessage();
				showThreadView(ureq,selectedMsg, null);
				scrollToCurrentMessage();
			}
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(msgEditCtr);
		removeAsListenerAndDispose(msgEditCmc);
		msgEditCmc = null;
		msgEditCtr = null;
	}
	
	/**
	 * 
	 * @see org.olat.core.util.event.GenericEventListener#event(org.olat.core.gui.control.Event)
	 */
	public void event(Event event) {
		if(event instanceof ForumChangedEvent) {
			forumChangedEventReceived = true;			
		}		
	}
	
	
	////////////////////////////////////////
	 // Application logic, do sth...
	////////////////////////////////////////
	
	

	private void doEditMessage(UserRequest ureq) {
		//after editing message
		boolean userIsMsgCreator = ureq.getIdentity().getKey().equals(currentMsg.getCreator().getKey());
		boolean children = fm.hasChildren(currentMsg);

		if (focallback.mayEditMessageAsModerator() || ((userIsMsgCreator) && (children == false))) {

			currentMsg = msgEditCtr.getMessageBackAfterEdit();
			currentMsg.setModifier(ureq.getIdentity());			
			
			final boolean changeLastModifiedDate = true; // OLAT-6295
			fm.updateMessage(currentMsg, changeLastModifiedDate, null);
			// if notification is enabled -> notify the publisher about news
			if (subsContext != null) {
				NotificationsManager.getInstance().markPublisherNews(subsContext, ureq.getIdentity(), true);
			}

			// do logging
			ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_MESSAGE_EDIT, getClass(),
					LoggingResourceable.wrap(currentMsg));

		} else {
			showWarning("may.not.save.msg.as.author");
			forumPanel.setContent(vcEditMessage);
		}
	}

	private void doReplyMessage(UserRequest ureq) {
		//after replying to a message
		Message m = fm.createMessage();
		m = msgEditCtr.getMessageBackAfterEdit();

		fm.replyToMessage(m, ureq.getIdentity(), currentMsg);
		DBFactory.getInstance().intermediateCommit();
		// if notification is enabled -> notify the publisher about news
		if (subsContext != null) {
			NotificationsManager.getInstance().markPublisherNews(subsContext, ureq.getIdentity(), true);
		}
		currentMsg = m;
		markRead(m, ureq.getIdentity());
		
		// do logging
		ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_REPLY_MESSAGE_CREATE, getClass(),
				LoggingResourceable.wrap(currentMsg));
	}

	private void doNewThread(UserRequest ureq) {
		//after creating a thread
		Message m = fm.createMessage();
		m = msgEditCtr.getMessageBackAfterEdit();
		
		if (!focallback.mayOpenNewThread()) throw new OLATSecurityException("not allowed to open new thread in forum " + forum.getKey());
		// open a new thread
		fm.addTopMessage(ureq.getIdentity(), forum, m);
		// if notification is enabled -> notify the publisher about news
		if (subsContext != null) {
			NotificationsManager.getInstance().markPublisherNews(subsContext, ureq.getIdentity(), true);
		}
		currentMsg = m;
		markRead(m, ureq.getIdentity());

		// do logging
		addLoggingResourceable(LoggingResourceable.wrap(m));
		ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_MESSAGE_CREATE, getClass());
	}

	private void doAttachmentDelivery(UserRequest ureq, String cmd, Map<String, Object> messageMap) {
		// user selected one attachment from the attachment list
		int pos = Integer.parseInt(cmd.substring(cmd.indexOf("_") + 1, cmd.lastIndexOf("_")));
		// velocity counter starts at 1
		List<VFSItem> attachments = new ArrayList<VFSItem>();
		attachments.addAll((Collection<VFSItem>) messageMap.get("attachments"));
		VFSItem vI = attachments.get(pos - 1);
		VFSLeaf vl = (VFSLeaf) vI;
		VFSMediaResource res = new VFSMediaResource(vl);
		res.setDownloadable(true); // prevent XSS attack
		ureq.getDispatchResult().setResultingMediaResource(res);
	}

	private void doDeleteMessage(UserRequest ureq) {
		boolean children = fm.hasChildren(currentMsg);
		boolean hasParent = currentMsg.getParent() != null;
		boolean userIsMsgCreator = ureq.getIdentity().getKey().equals(currentMsg.getCreator().getKey());
		if (focallback.mayDeleteMessageAsModerator() || (userIsMsgCreator && children == false)) {
			fm.deleteMessageTree(forum.getKey(), currentMsg);
			showInfo("deleteok");
			// do logging
			if(hasParent) {
				ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_MESSAGE_DELETE, getClass(),
						LoggingResourceable.wrap(currentMsg));
			} else {
				ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_THREAD_DELETE, getClass(),
						LoggingResourceable.wrap(currentMsg));
			}
		} else {
			showWarning("may.not.delete.msg.as.author");
		}
	}

	private void doArchiveForum(UserRequest ureq) {
		ForumRTFFormatter rtff = new ForumRTFFormatter(getArchiveContainer(ureq), false);
		ForumArchiveManager fam = ForumArchiveManager.getInstance();
		fam.applyFormatter(rtff, forum.getKey(), focallback);
	}

	private void doArchiveThread(UserRequest ureq, Message currMsg) {
		Message m = currMsg.getThreadtop();
		Long topMessageId = (m == null) ? currMsg.getKey() : m.getKey();

		ForumRTFFormatter rtff = new ForumRTFFormatter(getArchiveContainer(ureq), true);
		ForumArchiveManager fam = ForumArchiveManager.getInstance();
		fam.applyFormatterForOneThread(rtff, forum.getKey(), topMessageId);
	}
	
	
	
	////////////////////////////////////////
	 // Presentation
	////////////////////////////////////////
	
	private void showFilterForUserView(UserRequest ureq) {
		if(securityModule.isUserAllowedAutoComplete(ureq.getUserSession().getRoles())) {
			searchMode = true;
			backLinkSearchListTitles = LinkFactory.createCustomLink("backLinkLT", "back", "listalltitles", Link.LINK_BACK, vcFilterView, this);
			
			removeAsListenerAndDispose(filterForUserCtr);
			filterForUserCtr = new FilterForUserController(ureq, getWindowControl(), forum);
			listenTo(filterForUserCtr);
			
			vcFilterView.put("filterForUser", filterForUserCtr.getInitialComponent());
			forumPanel.setContent(vcFilterView);
		}
	}

	private void showThreadOverviewView() {
		// user has clicked on button 'list all message titles'
		// -> display allThreadTable
		msgs = fm.getMessagesByForum(forum);
		prepareListTitles(msgs);
		forumPanel.setContent(vcListTitles);
		// do logging
		ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_MESSAGE_LIST, getClass());
	}	
	
	private void showNewThreadView(UserRequest ureq) {
		cleanUp();
		// user has clicked on button 'open new thread'.
		Message m = fm.createMessage();
		msgEditCtr = new MessageEditController(ureq, getWindowControl(), focallback, m, null);
		listenTo(msgEditCtr);
		
		String title = translate("msg.create");
		msgEditCmc = new CloseableModalController(getWindowControl(), "close",
				msgEditCtr.getInitialComponent(), true, title);
		listenTo(msgEditCmc);
		msgEditCmc.activate();
	}
	
	private void showEditMessageView(UserRequest ureq) {
		// user has clicked on button 'edit'
		boolean userIsMsgCreator = ureq.getIdentity().getKey().equals(currentMsg.getCreator().getKey());
		boolean children = fm.hasChildren(currentMsg);
		if (focallback.mayEditMessageAsModerator() || ((userIsMsgCreator) && (children == false))) {
			// user is forum-moderator -> may edit every message on every level
			// or user is author of the current message and it has still no
			// children
			cleanUp();
			
			msgEditCtr = new MessageEditController(ureq, getWindowControl(), focallback, currentMsg, null);
			listenTo(msgEditCtr);
			
			String title = translate("msg.update");
			msgEditCmc = new CloseableModalController(getWindowControl(), "close",
					msgEditCtr.getInitialComponent(), true, title);
			listenTo(msgEditCmc);
			msgEditCmc.activate();
		} else if ((userIsMsgCreator) && (children == true)) {
			// user is author of the current message but it has already at least
			// one child
			showWarning("may.not.save.msg.as.author");
		} else {
			// user isn't author of the current message
			showInfo("may.not.edit.msg");
		}
	}

	private void showDeleteMessageView(UserRequest ureq) {
		// user has clicked on button 'delete'
		// -> display modal dialog 'Do you really want to delete this message?'
		// 'yes': back to allThreadTable, 'no' back to messageDetails
		int numOfChildren = countNumOfChildren(currentMsg, threadMsgs);
		boolean children = fm.hasChildren(currentMsg);
		boolean userIsMsgCreator = ureq.getIdentity().getKey().equals(currentMsg.getCreator().getKey());
		String currentMsgTitle = StringHelper.escapeHtml(currentMsg.getTitle());
		
		if (focallback.mayDeleteMessageAsModerator()) {
			// user is forum-moderator -> may delete every message on every level
			if (numOfChildren == 0) {
				yesno = activateYesNoDialog(ureq, null, translate("reallydeleteleaf", currentMsgTitle), yesno);
			} else if (numOfChildren == 1) {
				yesno = activateYesNoDialog(ureq, null, translate("reallydeletenode1", currentMsgTitle), yesno);
			} else {
				yesno = activateYesNoDialog(ureq, null, getTranslator().translate("reallydeletenodeN", new String[] { currentMsgTitle, Integer.toString(numOfChildren) }), yesno);
			}
		} else if ((userIsMsgCreator) && (children == false)) {
			// user may delete his own message if it has no children
			yesno = activateYesNoDialog(ureq, null, translate("reallydeleteleaf", currentMsgTitle), yesno);
		} else if ((userIsMsgCreator) && (children == true)) {
			// user may not delete his own message because it has at least one child
			showWarning("may.not.delete.msg.as.author");
		} else {
			// user isn't author of the current message
			showInfo("may.not.delete.msg");
		}
	}

	private void showReplyView(UserRequest ureq, boolean quote, Message parent) {
		// user has clicked on button 'reply'
		if (focallback.mayReplyMessage()) {

			Message quotedMessage = fm.createMessage();
			String reString = "";
			if(parent!=null && parent.getThreadtop()==null) {
				//add reString only for the first answer
				reString = translate("msg.title.re");
			}			
			quotedMessage.setTitle(reString + currentMsg.getTitle());
			if (quote) {
				// load message to form as quotation				
				StringBuilder quoteSB = new StringBuilder();
				quoteSB.append(TINYMCE_EMPTYLINE_CODE);
				quoteSB.append("<div class=\"o_quote_wrapper\"><div class=\"o_quote_author mceNonEditable\">");
				String date = f.formatDateAndTime(currentMsg.getCreationDate());
				User creator = currentMsg.getCreator().getUser();
				String creatorName = creator.getProperty(UserConstants.FIRSTNAME, ureq.getLocale()) + " " + creator.getProperty(UserConstants.LASTNAME, ureq.getLocale());
				quoteSB.append(getTranslator().translate("msg.quote.intro", new String[]{date, creatorName}));
				quoteSB.append("</div><blockquote class=\"o_quote\">");
				quoteSB.append(currentMsg.getBody());
				quoteSB.append("</blockquote></div>");
				quoteSB.append(TINYMCE_EMPTYLINE_CODE);
				quotedMessage.setBody(quoteSB.toString());
			}
			
			cleanUp();
			
			msgEditCtr = new MessageEditController(ureq, getWindowControl(), focallback, currentMsg, quotedMessage);
			listenTo(msgEditCtr);
			
			String title = quote ? translate("msg.quote") : translate("msg.reply");
			msgEditCmc = new CloseableModalController(getWindowControl(), "close",
					msgEditCtr.getInitialComponent(), true, title);
			listenTo(msgEditCmc);
			msgEditCmc.activate();
		} else {
			showInfo("may.not.reply.msg");
		}
	}

	private void showSplitThreadView(UserRequest ureq) {		
		if (focallback.mayEditMessageAsModerator()) {
			// user is forum-moderator -> may delete every message on every level
			int numOfChildren = countNumOfChildren(currentMsg, threadMsgs);
			
			// provide yesNoSplit as argument, this ensures that dc is disposed before newly created
			yesNoSplit = activateYesNoDialog(ureq, null, getTranslator().translate("reallysplitthread", new String[] { currentMsg.getTitle(), Integer.toString(numOfChildren) }), yesNoSplit);

			//activateYesNoDialog means that this controller listens to it, and dialog is shown on screen.
			//nothing further to do here!
			return;
		}
	}
	
	private void showMoveMessageView(UserRequest ureq) {
		if (focallback.mayEditMessageAsModerator()) {
			//prepare the table data
			msgs = fm.getMessagesByForum(forum);
			threadList = prepareListTitles(msgs);
			DefaultTableDataModel<Message> tdm = new DefaultTableDataModel<Message>(threadList) {
				
					@Override
					public Object getValueAt(int row, int col) {
						Message m = threadList.get(row);
						boolean isSource = m.equalsByPersistableKey(currentMsg.getThreadtop());
						switch (col) {
							case 0:
								String title = StringEscapeUtils.escapeHtml(m.getTitle()).toString();
								return title;
							case 1:
								if (m.getCreator().getStatus().equals(Identity.STATUS_DELETED)) {
									return m.getCreator().getName();
								} else {
									return userManager.getUserDisplayName(m.getCreator()); 
								}
							case 2 :
								Date mod = m.getLastModified();
								return mod;
							case 3:
								return !isSource;

							default: return "error";
						}
					}
				
					@Override
					public int getColumnCount() {
						return 4;
					}
			};
			
			//prepare the table config
			TableGuiConfiguration tableConfig = new TableGuiConfiguration();
			tableConfig.setCustomCssClass("o_forum");
			tableConfig.setSelectedRowUnselectable(true);
			tableConfig.setDownloadOffered(false);
			tableConfig.setTableEmptyMessage(translate("forum.emtpy"));
			
			//prepare the table controller
			removeAsListenerAndDispose(moveMessageTableCtr);
			moveMessageTableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
			listenTo(moveMessageTableCtr);
			
			moveMessageTableCtr.addColumnDescriptor(true, new DefaultColumnDescriptor("table.thread", 0, null, ureq.getLocale()));
			moveMessageTableCtr.addColumnDescriptor(true, new DefaultColumnDescriptor("table.userfriendlyname", 1, null, ureq.getLocale()));
			moveMessageTableCtr.addColumnDescriptor(true, new DefaultColumnDescriptor("table.lastModified", 2, null, ureq.getLocale()));				
			moveMessageTableCtr.addColumnDescriptor(true, new BooleanColumnDescriptor("table.choose", 3, "move", translate("table.choose"), translate("table.source")));
			moveMessageTableCtr.setTableDataModel(tdm);
			
			//push the modal dialog with the table as content
			removeAsListenerAndDispose(cmcMoveMsg);
			cmcMoveMsg = new CloseableModalController(getWindowControl(), "close", moveMessageTableCtr.getInitialComponent());
			listenTo(cmcMoveMsg);
			
			cmcMoveMsg.activate();
		}
	}
	
	private void showThreadView(UserRequest ureq, Message m, String viewMode) {
		
		adjustBusinessControlPath(ureq, m);
		
		// remove old messages from velocity and dispose controllers
		disposeCurrentMessages();
		// now fetch current thread
		Message threadTopM = m.getThreadtop();
		currentMsg = m; // in some cases already set, but set current message anyway
		threadMsgs = fm.getThread(threadTopM == null ? m.getKey() : threadTopM.getKey());
		precalcMessageDeepness(threadMsgs);
		// for simplicity no reuse of container, always create new one
		vcThreadView = createVelocityContainer("threadview");
		
		backLinkListTitles = LinkFactory.createCustomLink("backLinkLT", "back", "listalltitles", Link.LINK_BACK, vcThreadView, this);
		archiveThreadButton = LinkFactory.createButtonSmall("archive.thread", vcThreadView, this);
		archiveThreadButton.setIconLeftCSS("o_icon o_icon-fw o_icon_archive_tool");

				
		boolean isClosed = Status.getStatus(m.getStatusCode()).isClosed(); 
		vcThreadView.contextPut("isClosed",isClosed);
		if(!isClosed) {
			closeThreadButton = LinkFactory.createButtonSmall("close.thread", vcThreadView, this);
			closeThreadButton.setIconLeftCSS("o_icon o_icon-fw o_forum_status_closed_icon");
		} else {
			openThreadButton = LinkFactory.createButtonSmall("open.thread", vcThreadView, this);
			openThreadButton.setIconLeftCSS("o_icon o_icon-fw o_forum_status_opened_icon");
		}	
		boolean isHidden = Status.getStatus(m.getStatusCode()).isHidden(); 
		vcThreadView.contextPut("isHidden",isHidden);		
		if(!isHidden) {
			hideThreadButton = LinkFactory.createButtonSmall("hide.thread", vcThreadView, this);
			hideThreadButton.setIconLeftCSS("o_icon o_icon-fw o_forum_status_hidden_icon");
		} else {
			showThreadButton = LinkFactory.createButtonSmall("show.thread", vcThreadView, this);
			showThreadButton.setIconLeftCSS("o_icon o_icon-fw o_forum_status_visible_icon");
		}	

		//allow to set thread-viewmode prefs and get actual ones
		viewSwitchCtr = new ForumThreadViewModeController(ureq,getWindowControl(), viewMode);
		listenTo(viewSwitchCtr);
		vcThreadView.put("threadViewSwitch", viewSwitchCtr.getInitialComponent());
		
		vcThreadView.contextPut("showThreadTable", Boolean.FALSE);
		vcThreadView.contextPut("threadMode", Boolean.FALSE);
		vcThreadView.contextPut("msgDeepMap", msgDeepMap);
		
		// add all messages that are needed
		currentMessagesMap = new ArrayList<Map<String, Object>>(threadMsgs.size());
		
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
		
		if(viewMode == null) {
			viewMode = viewSwitchCtr.getThreadViewMode(ureq);
		}
		
		if (ForumThreadViewModeController.VIEWMODE_FLAT.equals(viewMode)) {							
			// all messages in flat view
			List<Message> orderedMessages = new ArrayList<Message>();
						
			orderedMessages.addAll(threadMsgs);
			orderedMessages = threadMsgs;
			Collections.sort(orderedMessages);
			
			int msgNum = 0;
			Iterator<Message> iter = orderedMessages.iterator();			
			while (iter.hasNext()) {
				Message msg = iter.next();
				// add message and mark as read
				addMessageToCurrentMessagesAndVC(ureq, msg, vcThreadView, currentMessagesMap, msgNum, marks, stats);
				msgNum++;
			}
			// do logging
			ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_THREAD_READ, getClass(), LoggingResourceable.wrap(currentMsg));
		} else if (ForumThreadViewModeController.VIEWMODE_MESSAGE.equals(viewMode)){
			// single message in thread view, add message and mark as read
			addMessageToCurrentMessagesAndVC(ureq, m, vcThreadView, currentMessagesMap, 0, marks, stats);
			// init single thread list and append
			sttdmodel = new ForumMessagesTableDataModel(userManager, threadMsgs, rms);
			sttdmodel.setLocale(ureq.getLocale());
			singleThreadTableCtr.setTableDataModel(sttdmodel);
			int position = PersistenceHelper.indexOf(threadMsgs, currentMsg);
			singleThreadTableCtr.setSelectedRowId(position);			
			vcThreadView.contextPut("showThreadTable", Boolean.TRUE);
			vcThreadView.put("singleThreadTable", singleThreadTableCtr.getInitialComponent());
			// do logging
			ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_MESSAGE_READ, getClass(), LoggingResourceable.wrap(currentMsg));
			
		} else if (ForumThreadViewModeController.VIEWMODE_MARKED.equals(viewMode)) {
			// marked messages in flat view
			List<Message> orderedMessages = new ArrayList<Message>();
						
			orderedMessages.addAll(threadMsgs);
			orderedMessages = threadMsgs;
			Collections.sort(orderedMessages);
			
			int msgNum = 0;		
			for (Message msg:orderedMessages) {
				// add marked message
				if(marks.containsKey(msg.getKey().toString())) {
					addMessageToCurrentMessagesAndVC(ureq, msg, vcThreadView, currentMessagesMap, msgNum, marks, stats);
					msgNum++;
				}
			}
			// do logging
			ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_THREAD_READ, getClass(), LoggingResourceable.wrap(currentMsg));
		} else if (ForumThreadViewModeController.VIEWMODE_NEW.equals(viewMode)) {
			// new messages in flat view
			List<Message> orderedMessages = new ArrayList<Message>();
						
			orderedMessages.addAll(threadMsgs);
			orderedMessages = threadMsgs;
			Collections.sort(orderedMessages);
			
			int msgNum = 0;		
			for (Message msg:orderedMessages) {
				// add new message
				if(!rms.contains(msg.getKey())) {
					addMessageToCurrentMessagesAndVC(ureq, msg, vcThreadView, currentMessagesMap, msgNum, marks, stats);
					msgNum++;
				}
			}
			// do logging
			ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_THREAD_READ, getClass(), LoggingResourceable.wrap(currentMsg));
		} else {
			//real threaded view with indent
			vcThreadView.contextPut("threadMode", Boolean.TRUE);
			List<Message> orderedMessages = new ArrayList<Message>();
			orderMessagesThreaded(threadMsgs, orderedMessages, (threadTopM == null ? m : threadTopM));
			// all messages in thread view
			//Iterator iter = threadMsgs.iterator();
			Iterator<Message> iter = orderedMessages.iterator();			
			
			int msgNum = 0;
			while (iter.hasNext()) {
				Message msg = iter.next();
				// add message and mark as read
				addMessageToCurrentMessagesAndVC(ureq, msg, vcThreadView, currentMessagesMap, msgNum, marks, stats);
				msgNum++;
			}
			// do logging
			ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_THREAD_READ, getClass(), LoggingResourceable.wrap(m));
		}
		vcThreadView.contextPut("messages", currentMessagesMap);
		
		// Mapper to display thumbnail images of file attachments
		vcThreadView.contextPut("thumbMapper", thumbMapper);
		// add security callback
		vcThreadView.contextPut("security", focallback);
		vcThreadView.contextPut("mode", viewMode);
		forumPanel.setContent(vcThreadView);
	}

	private void scrollToCurrentMessage() {
		// Scroll to message, but only the first time the view is rendered
		if (currentMsg.getThreadtop() == null || currentMessagesMap.size() == 1) {
			vcThreadView.contextPut("goToMessage", Boolean.FALSE);
		} else {
			vcThreadView.contextPut("goToMessage", new ConsumableBoolean(true));
			vcThreadView.contextPut("goToMessageId", currentMsg.getKey());						
		}
	}
	
	
	////////////////////////////////////////
	 // Helper Methods / Classes
	////////////////////////////////////////

	
	private void precalcMessageDeepness(List<Message> msgList){
		msgDeepMap = new HashMap<Long, Integer>(); 
		for (Message message : msgList) {
			int deepness = messageDeepness(message, 0);
			msgDeepMap.put(message.getKey(), deepness);
		}		
	}
	
	private int messageDeepness(Message msg, int deep){
		if (deep > 20) return 20;
		if (msg.getParent()==null) return deep;
		else {
			int newDeep = deep + 1;
			return messageDeepness(msg.getParent(), newDeep);
		}
	}
	
	
	private void addMessageToCurrentMessagesAndVC(UserRequest ureq, Message m, VelocityContainer vcContainer, List<Map<String, Object>> allList, int msgCount, Map<String,Mark> marks, Map<String,MarkResourceStat> stats) {
		// all values belonging to a message are stored in this map
		// these values can be accessed in velocity. make sure you clean up
		// everything
		// you create here in disposeCurrentMessages()!
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("id", m.getKey());
		
		if (rms.contains(m.getKey())) {
			// already read
			map.put("newMessage", Boolean.FALSE);
		} else {
			// mark now as read
			markRead(m, ureq.getIdentity());
			map.put("newMessage", Boolean.TRUE);
		}
		// add some data now
		Date creationDate = m.getCreationDate();
		Identity modifier = m.getModifier();
		if (modifier != null) {
			map.put("isModified", Boolean.TRUE);
			map.put("modfname", modifier.getUser().getProperty(UserConstants.FIRSTNAME, ureq.getLocale()));
			map.put("modlname", modifier.getUser().getProperty(UserConstants.LASTNAME, ureq.getLocale()));
		} else {
			map.put("isModified", Boolean.FALSE);
		}
		map.put("title", m.getTitle());
		map.put("body", m.getBody());
		map.put("date", f.formatDateAndTime(creationDate));
		Identity creator = m.getCreator();
		map.put("creator", creator.getKey());
		map.put("firstname", Formatter.truncate(creator.getUser().getProperty(UserConstants.FIRSTNAME, ureq.getLocale()),18)); //keeps the first 15 chars
		map.put("lastname", Formatter.truncate(creator.getUser().getProperty(UserConstants.LASTNAME, ureq.getLocale()),18));
		map.put("modified", f.formatDateAndTime(m.getLastModified()));
		// message attachments
		OlatRootFolderImpl msgContainer = fm.getMessageContainer(forum.getKey(), m.getKey());
		map.put("messageContainer", msgContainer);
		final List<VFSItem> attachments = new ArrayList<VFSItem>(msgContainer.getItems(new VFSItemExcludePrefixFilter(MessageEditController.ATTACHMENT_EXCLUDE_PREFIXES)));				
		map.put("attachments", attachments);
		if (attachments == null || attachments.size() == 0) map.put("hasAttachments", Boolean.FALSE);
		else map.put("hasAttachments", Boolean.TRUE);
		// number of children and modify/delete permissions
		int numOfChildren;
		numOfChildren = countNumOfChildren(m, threadMsgs);
		Integer nOfCh = new Integer(numOfChildren);
		map.put("nOfCh", nOfCh);
		boolean userIsMsgCreator = getIdentity().getKey().equals(creator.getKey());
		Boolean uIsMsgC = new Boolean(userIsMsgCreator);
		map.put("uIsMsgC", uIsMsgC);
		boolean isThreadtop = m.getThreadtop()==null;
		map.put("isThreadtop", Boolean.valueOf(isThreadtop));
		boolean isThreadClosed = Status.getStatus(m.getStatusCode()).isClosed(); 
		if(!isThreadtop) {
			isThreadClosed = Status.getStatus(m.getThreadtop().getStatusCode()).isClosed();
		}
		map.put("isThreadClosed", isThreadClosed);
		if(!isGuestOnly(ureq)) {
		  // add portrait to map for later disposal and key for rendering in velocity
		  DisplayPortraitController portrait = new DisplayPortraitController(ureq, getWindowControl(), m.getCreator(), true, true, false, true);
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
		}
		allList.add(map);
		/*
		 * those Link objects are used! see event method and the instanceof Link part!
		 * but reference won't be used!
		 */
		Link dlLink = LinkFactory.createCustomLink("dl_"+msgCount, "dl_"+msgCount, "msg.delete", Link.BUTTON_SMALL, vcThreadView, this);
		dlLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
		Link edLink = LinkFactory.createCustomLink("ed_"+msgCount, "ed_"+msgCount, "msg.update", Link.BUTTON_SMALL, vcThreadView, this);
		edLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");

		Link qtLink = LinkFactory.createCustomLink("qt_"+msgCount, "qt_"+msgCount, "msg.quote", Link.BUTTON_SMALL, vcThreadView, this);
		qtLink.setIconLeftCSS("o_icon o_icon-fw o_icon_reply_with_quote");
		Link rpLink = LinkFactory.createCustomLink("rp_"+msgCount, "rp_"+msgCount, "msg.reply", Link.BUTTON_SMALL, vcThreadView, this);
		rpLink.setIconLeftCSS("o_icon o_icon-fw o_icon_reply");

		Link splitLink = LinkFactory.createCustomLink("split_"+msgCount, "split_"+msgCount, "msg.split", Link.LINK, vcThreadView, this);
		splitLink.setIconLeftCSS("o_icon o_icon-fw o_icon_split");
		Link moveLink = LinkFactory.createCustomLink("move_"+msgCount, "move_"+msgCount, "msg.move", Link.LINK, vcThreadView, this);
		moveLink.setIconLeftCSS("o_icon o_icon-fw o_icon_move");
		
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
		
		if (uIsMsgC) {
			OLATResourceable messageOres = OresHelper.createOLATResourceableInstance("Forum", m.getKey());
			String businessPath = BusinessControlFactory.getInstance().getAsString(getWindowControl().getBusinessControl()) + "[Message:" + m.getKey() + "]";
			Controller ePFCollCtrl = EPUIFactory.createArtefactCollectWizzardController(ureq, getWindowControl(), messageOres,
					businessPath);
			if (ePFCollCtrl != null) {
				String ePFAddComponentName = "eportfolio_" + msgCount;
				map.put("ePFCollCtrl", ePFCollCtrl);
				map.put("ePFAddComponentName", ePFAddComponentName);
				vcThreadView.put(ePFAddComponentName, ePFCollCtrl.getInitialComponent());
			}
		}
	}
	
	private boolean isGuestOnly(UserRequest ureq) {
    return ureq.getUserSession().getRoles().isGuestOnly();
  }

	private void disposeCurrentMessages() {
		if (currentMessagesMap != null) {
			Iterator<Map<String, Object>> iter = currentMessagesMap.iterator();
			while (iter.hasNext()) {
				Map<String, Object> messageMap = iter.next();
				// cleanup portrait controllers
				Controller ctr = (Controller) messageMap.get("portrait");
				 if(ctr!=null) { //ctr could be null for a guest user
				   ctr.dispose();
				   vcThreadView.remove(ctr.getInitialComponent());
				 }
				// cleanup mark controllers
				
				// cleanup ePortfolio controllers
				Controller ePCtr = (Controller) messageMap.get("ePFCollCtrl");
				if (ePCtr != null) ePCtr.dispose();				
			}
		}
	}

	private List<Message> prepareListTitles(List<Message> messages) {
		List<Message> tmpThreadList = new ArrayList<Message>();
		// extract threads from all messages
		List<Object[]> threads = new ArrayList<Object[]>();
		int numTableCols = 8;
		MarkingService markingService = (MarkingService)CoreSpringFactory.getBean(MarkingService.class);
		List<MarkResourceStat> stats = markingService.getMarkManager().getStats(forumOres, null, getIdentity());
		
		boolean isModerator = focallback.mayEditMessageAsModerator();	
		for (Iterator<Message> iter = messages.iterator(); iter.hasNext();) {
			Message thread = iter.next();
			if (thread.getParent() == null) {
				// put all data in a generic object array				
				Object[] mesgWrapper = new Object[numTableCols];
				String title = StringEscapeUtils.escapeHtml(thread.getTitle()).toString();
				title = Formatter.truncate(title, 50);	
				Status messageStatus = Status.getStatus(thread.getStatusCode());
				boolean isSticky = messageStatus.isSticky(); 
				boolean isClosed = messageStatus.isClosed(); 
				boolean isHidden = messageStatus.isHidden(); 
				if(isHidden && !isModerator) {
					continue;
				}
				
				mesgWrapper[0] = "status_thread";
				if(isSticky && isClosed) {
					mesgWrapper[0] = "status_sticky_closed";
				} else if (isSticky) {
					mesgWrapper[0] = "status_sticky";
				} else if (isClosed) {
					mesgWrapper[0] = "status_closed";
				}								
				if(isHidden) {
					title = translate("msg.hidden")  + " " + title;
				}
				mesgWrapper[1] = new ForumHelper.MessageWrapper(title,isSticky,collator, f);
				User creator = thread.getCreator().getUser();
				mesgWrapper[2] = new ForumHelper.MessageWrapper(userManager.getUserDisplayName(creator),isSticky, collator, f);
				// find latest date, and number of read messages for all children
				// init with thread values
				Date lastModified = thread.getLastModified();
				int readCounter = (rms.contains(thread.getKey()) ? 1 : 0);
				int childCounter = 1;
				int statCounter = 0;
				String threadSubPath = thread.getKey().toString();
				for(MarkResourceStat stat:stats) {
					if(threadSubPath.equals(stat.getSubPath())) {
						statCounter += stat.getCount();
					}
				}
				
				for (Iterator<Message> iter2 = messages.iterator(); iter2.hasNext();) {
					Message msg = iter2.next();
					if (msg.getThreadtop() != null && msg.getThreadtop().getKey().equals(thread.getKey())) {
						// a child is found, update values
						childCounter++;
						if (rms.contains(msg.getKey())) readCounter++;
						if (msg.getLastModified().after(lastModified)) lastModified = msg.getLastModified();
						
						String subPath = msg.getKey().toString();
						for(MarkResourceStat stat:stats) {
							if(subPath.equals(stat.getSubPath())) {
								statCounter += stat.getCount();
							}
						}
					}
				}				
				mesgWrapper[3] = new ForumHelper.MessageWrapper(lastModified,isSticky,collator, f);
				//lastModified
				mesgWrapper[4] = new ForumHelper.MessageWrapper(new Integer(statCounter),isSticky,collator, f);
				//marked
				mesgWrapper[5] = new ForumHelper.MessageWrapper(new Integer((childCounter - readCounter)),isSticky,collator, f);
				// unread					
				mesgWrapper[6] = new ForumHelper.MessageWrapper(new Integer(childCounter),isSticky,collator, f);
				// add message itself for later usage
				mesgWrapper[7] = thread;
				tmpThreadList.add(thread);
				threads.add(mesgWrapper);
			}
		}
		// build table model
		attdmodel = new GenericObjectArrayTableDataModel(threads, numTableCols);
		allThreadTableCtr.setTableDataModel(attdmodel);
		allThreadTableCtr.setSortColumn(3, false);
		
		vcListTitles.put("allThreadTable", allThreadTableCtr.getInitialComponent());
		vcListTitles.contextPut("hasThreads", (attdmodel.getRowCount() == 0) ? Boolean.FALSE : Boolean.TRUE);
		
		return tmpThreadList;
	}

	/**
	 * @param m
	 * @param messages
	 * @return number of all children, grandchildren, grand-grandchildren etc. of
	 *         a certain message
	 */
	private int countNumOfChildren(Message m, List<Message> messages) {
		int counter = 0;
		counter = countChildrenRecursion(m, messages, counter);
		return counter;
	}

	private int countChildrenRecursion(Message m, List<Message> messages, int counter) {
		for (Iterator<Message> iter = messages.iterator(); iter.hasNext();) {
			Message element = iter.next();
			if (element.getParent() != null) {
				if (m.getKey().equals(element.getParent().getKey())) {
					counter = countChildrenRecursion(element, messages, counter);
					counter++;
				}
			}
		}
		return counter;
	}

	private VFSContainer getArchiveContainer(UserRequest ureq) {
		VFSContainer container = new OlatRootFolderImpl(FolderConfig.getUserHomes() + File.separator + ureq.getIdentity().getName() + "/private/archive", null);
    // append export timestamp to avoid overwriting previous export 
		Date tmp = new Date(System.currentTimeMillis());
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss");
		String folder = "forum_" + forum.getKey().toString()+"_"+formatter.format(tmp);
		VFSItem vfsItem = container.resolve(folder);
		if (vfsItem == null || !(vfsItem instanceof VFSContainer)) {
			vfsItem = container.createChildContainer(folder);
		}
		container = (VFSContainer) vfsItem;
		return container;
	}

	private void adjustBusinessControlPath(UserRequest ureq, Message m) {
		ThreadLocalUserActivityLogger.addLoggingResourceInfo(LoggingResourceable.wrap(m));
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Message.class,m.getKey());
		//fxdiff BAKS-7 Resume function
		WindowControl bwControl = addToHistory(ureq, ores, null);		
		
		//Simple way to "register" the new ContextEntry although only a VelocityPage was flipped.
		Controller dummy = new BasicController(ureq, bwControl) {
		
			@Override
			protected void event(UserRequest ureq, Component source, Event event) {
			// TODO Auto-generated method stub
		
			}
		
			@Override
			protected void doDispose() {
			// TODO Auto-generated method stub
		
			}
		
		};
		dummy.dispose();
		dummy = null;
	}

	private Set<Long> getReadSet(Identity s) {
		// FIXME:fj:c put the whole readset of 1 user / 1 forum in one property
		// only: 234,45646,2343,23432 etc.
		// Problem now is that a lot of rows are generated: number of users x
		// visited messages of all forums = e.g. 5000 x 300 = 1.5 million etc.
		
		return ForumManager.getInstance().getReadSet(s, forum);
	}

	private void markRead(Message m, Identity s) {		
		if (!rms.contains(m.getKey())) {
			rms.add(m.getKey());
		  ForumManager.getInstance().markAsRead(s, m);
		}
	}
	
	protected void doDispose() {
		disposeCurrentMessages();
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, forum);
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
	
	/**
	 * Orders the messages in the logical instead of chronological order.
	 * @param messages
	 * @param orderedList
	 * @param startMessage
	 */	
	private void orderMessagesThreaded(List<Message> messages, List<Message> orderedList, Message startMessage) {
	  if(messages==null || orderedList==null || startMessage==null) return;
	   Iterator<Message> iterMsg = messages.iterator();
	  while(iterMsg.hasNext())
	  {
	   Message msg = iterMsg.next();
	   if (msg.getParent() == null) 
	   {  
	    orderedList.add(msg);     
	    ArrayList<Message> copiedMessages = new ArrayList<Message>();
	    copiedMessages.addAll(messages);
	    copiedMessages.remove(msg);
	    messages = copiedMessages;
	    
	    continue;
	   }   
	   if ((msg.getParent() != null) && (msg.getParent().getKey().equals(startMessage.getKey())))
	   {    
	    orderedList.add(msg);    
	    orderMessagesThreaded(messages, orderedList, msg);
	   }
	  }
	 }
	
	
	/**
	 * Calls splitThread on ForumManager and shows the new thread view.
	 * @param ureq
	 */
	private void splitThread(UserRequest ureq) {
		if (focallback.mayEditMessageAsModerator()) {
			Message newTopMessage = fm.splitThread(currentMsg);						
			showThreadView(ureq,newTopMessage, null);
      //do logging
			ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_THREAD_SPLIT, getClass(), LoggingResourceable.wrap(currentMsg));
			
		} else {
			showWarning("may.not.split.thread");
		}
	}
	
	/**
	 * Calls moveMessage on ForumManager
	 * @param ureq
	 * @param topMessage
	 */
	private void moveMessage(UserRequest ureq, Message topMsg) {
		if (focallback.mayEditMessageAsModerator()) {
			currentMsg = fm.moveMessage(currentMsg, topMsg);
			cmcMoveMsg.deactivate();
			showThreadView(ureq, topMsg, null);
			ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_MESSAGE_MOVE, getClass(), LoggingResourceable.wrap(currentMsg));
		} else {
			showWarning("may.not.move.message");
		}
	}
	
	/**
	 * Sets the closed status to the threadtop message.
	 * @param ureq
	 * @param msg
	 * @param closed
	 */
	private void closeThread(Message msg, boolean closed) {	
		//if the input message is not the Threadtop get the Threadtop message
		if(msg != null && msg.getThreadtop()!=null) {
			msg = msg.getThreadtop();
		}
		if (msg != null && msg.getThreadtop()==null) {
			currentMsg = fm.loadMessage(msg.getKey());
			Status status = Status.getStatus(currentMsg.getStatusCode());
			status.setClosed(closed);
			if (currentMsg.getParent() == null) {
				currentMsg.setStatusCode(Status.getStatusCode(status));
				final boolean changeLastModifiedDate = !closed; //OLAT-6295
				fm.updateMessage(currentMsg, changeLastModifiedDate, new ForumChangedEvent("close"));
			}
			// do logging
			ILoggingAction loggingAction;
			if (closed)  {
				loggingAction = ForumLoggingAction.FORUM_THREAD_CLOSE;
			} else {
				loggingAction = ForumLoggingAction.FORUM_THREAD_REOPEN;
			}
			
			ThreadLocalUserActivityLogger.log(loggingAction, getClass(), LoggingResourceable.wrap(currentMsg));
			showThreadOverviewView();
		}
	}
	
	/**
	 * Sets the hidden status to the threadtop message.
	 * @param ureq
	 * @param msg
	 * @param hidden
	 */
	private void hideThread(Message msg, boolean hidden) {		
    //if the input message is not the Threadtop get the Threadtop message
		if(msg != null && msg.getThreadtop()!=null) {
			msg = msg.getThreadtop();
		}
		if (msg != null && msg.getThreadtop()==null) {
			currentMsg = fm.loadMessage(msg.getKey());
			Status status = Status.getStatus(currentMsg.getStatusCode());
			status.setHidden(hidden);
			if(currentMsg.getParent()==null) {
			  currentMsg.setStatusCode(Status.getStatusCode(status));
			  final boolean changeLastModifiedDate = !hidden; //OLAT-6295
			  fm.updateMessage(currentMsg, changeLastModifiedDate, new ForumChangedEvent("hide"));
			}
			// do logging
			ILoggingAction loggingAction;
			if (hidden)  {
				loggingAction = ForumLoggingAction.FORUM_THREAD_HIDE;
			} else {
				loggingAction = ForumLoggingAction.FORUM_THREAD_SHOW;
			}
			
			ThreadLocalUserActivityLogger.log(loggingAction, getClass(), LoggingResourceable.wrap(currentMsg));
			showThreadOverviewView();
		}
	}
				
	
	////////////////////////////////////////
	 // Sticky things
	////////////////////////////////////////	
	
	/**
	 * 
	 * Description:<br>
	 * Tree cell renderer for the sticky thread titles.
	 * 
	 * <P>
	 * Initial Date:  09.07.2007 <br>
	 * @author Lavinia Dumitrescu
	 */
	private static class StickyThreadCellRenderer implements CustomCellRenderer {
		@Override
		public void render(final StringOutput sb, final Renderer renderer, final Object val, final Locale locale, final int alignment, final String action) {
			if(val instanceof ForumHelper.MessageWrapper) {
				ForumHelper.MessageWrapper messageWrapper = (ForumHelper.MessageWrapper)val;
				String content = messageWrapper.toString();
				if (renderer == null) {
					sb.append(content);
				} else {
					sb.append("<span class='");
					if (messageWrapper.isSticky()) {			
						sb.append("o_forum_thread_sticky");
					}
					sb.append("'>").append(content).append("</span>");			
				}
			}
		}
	}	
	
	/**
	 * 
	 * Description:<br>
	 * <code>ColumnDescriptor</code> with special <code>compareTo</code> method implementation. 
	 * Allows a special column sorting for MessageWrappers considering the sticky attribute.
	 * 
	 * <P>
	 * Initial Date:  11.07.2007 <br>
	 * @author Lavinia Dumitrescu
	 */
  private class StickyColumnDescriptor extends DefaultColumnDescriptor{		
		public StickyColumnDescriptor(String headerKey, int dataColumn, String action, Locale locale) {
			 super(headerKey, dataColumn, action, locale, ColumnDescriptor.ALIGNMENT_LEFT);
		}
		/**
		 * Sole constructor.
		 * @param headerKey
		 * @param dataColumn
		 * @param action
		 * @param locale used ONLY for method getRenderValue in case the Object is of type Date to provide locale-sensitive Date formatting
		 * @param alignment left, middle or right; constants in ColumnDescriptor
		 */
		public StickyColumnDescriptor(String headerKey, int dataColumn, String action, Locale locale, int alignment) {
			super(headerKey, dataColumn, action, locale, alignment);
		}
		
		/**
		 * Delegates comparison to the <code>ForumHelper.compare</code>. In case the <code>ForumHelper.compare</code>
		 * returns <code>ForumHelper.NOT_MY_JOB</code>, the comparison is executed by the superclass.
		 * @see org.olat.core.gui.components.table.ColumnDescriptor#compareTo(int, int)
		 */
		@Override
		public int compareTo(int rowa, int rowb) {
			ForumHelper.MessageWrapper a = (ForumHelper.MessageWrapper)getTable().getTableDataModel().getValueAt(rowa,getDataColumn());
			ForumHelper.MessageWrapper b = (ForumHelper.MessageWrapper)getTable().getTableDataModel().getValueAt(rowb,getDataColumn());
			boolean sortAscending = getTable().isSortAscending();

			int comparisonOutcome = ForumHelper.compare(a,b,sortAscending);
			if(comparisonOutcome == ForumHelper.NOT_MY_JOB) {
				comparisonOutcome = super.compareTo(rowa, rowb);
			}
			return comparisonOutcome;
		}	    
	}

  /**
   * 
   * Description:<br>
   * <code>ColumnDescriptor</code> with special <code>compareTo</code> method implementation for a <code>CustomCellRenderer</code>. 
	 * Allows a special column sorting for MessageWrappers considering the sticky attribute.
   * 
   * <P>
   * Initial Date:  11.07.2007 <br>
   * @author Lavinia Dumitrescu
   */
  private static class StickyRenderColumnDescriptor extends CustomRenderColumnDescriptor {
  	
  	public StickyRenderColumnDescriptor(String headerKey, int dataColumn, String action, Locale locale, int alignment,
  			CustomCellRenderer customCellRenderer) {
  		super(headerKey, dataColumn, action, locale, alignment,customCellRenderer);  		
  	}
  	
  		/**
		 * Delegates comparison to the <code>ForumHelper.compare</code>. In case the <code>ForumHelper.compare</code>
		 * returns <code>ForumHelper.NOT_MY_JOB</code>, the comparison is executed by the superclass.
		 * @see org.olat.core.gui.components.table.ColumnDescriptor#compareTo(int, int)
		 */	
  		@Override	
		public int compareTo(int rowa, int rowb) {
			ForumHelper.MessageWrapper a = (ForumHelper.MessageWrapper)getTable().getTableDataModel().getValueAt(rowa,getDataColumn());
			ForumHelper.MessageWrapper b = (ForumHelper.MessageWrapper)getTable().getTableDataModel().getValueAt(rowb,getDataColumn());
			boolean sortAscending = getTable().isSortAscending();

			int comparisonOutcome = ForumHelper.compare(a,b,sortAscending);
			if(comparisonOutcome == ForumHelper.NOT_MY_JOB) {
				comparisonOutcome = super.compareTo(rowa, rowb);
			}
			return comparisonOutcome;
		}				
  }
	
  public class MessageIconRenderer extends CustomCssCellRenderer {
		@Override
		protected String getHoverText(Object val) {
			return NewControllerFactory.translateResourceableTypeName((String)val, getLocale());
		}

		@Override
		protected String getCellValue(Object val) {
			return "";
		}

		@Override
		protected String getCssClass(Object val) {
			// use small icon and create icon class for resource: o_FileResource-SHAREDFOLDER_icon
			return "o_icon o_forum_" + ((String)val) + "_icon";
		}
	}
}