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
package org.olat.modules.fo.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkResourceStat;
import org.olat.core.commons.services.mark.MarkingService;
import org.olat.core.commons.services.mark.impl.ui.MarkedEvent;
import org.olat.core.commons.services.mark.impl.ui.UnmarkedEvent;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.link.LinkPopupSettings;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.core.util.ConsumableBoolean;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.core.util.vfs.filters.VFSLeafButSystemFilter;
import org.olat.course.nodes.FOCourseNode;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.ForumCallback;
import org.olat.modules.fo.ForumChangedEvent;
import org.olat.modules.fo.ForumLoggingAction;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.MessageLight;
import org.olat.modules.fo.MessageRef;
import org.olat.modules.fo.Status;
import org.olat.modules.fo.archiver.formatters.ForumDownloadResource;
import org.olat.modules.fo.export.FinishCallback;
import org.olat.modules.fo.export.SendMailStepForm;
import org.olat.modules.fo.export.Step_1_SelectCourse;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.modules.fo.manager.QuoterFilter;
import org.olat.modules.fo.portfolio.ForumMediaHandler;
import org.olat.modules.fo.ui.MessageEditController.EditMode;
import org.olat.modules.fo.ui.events.DeleteMessageEvent;
import org.olat.modules.fo.ui.events.DeleteThreadEvent;
import org.olat.modules.fo.ui.events.ErrorEditMessage;
import org.olat.modules.fo.ui.events.SelectMessageEvent;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.portfolio.ui.component.MediaCollectorComponent;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResourceManager;
import org.olat.user.DisplayPortraitController;
import org.olat.user.UserInfoMainController;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * The list of messages in a thread.
 * 
 * Initial date: 10.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MessageListController extends BasicController implements GenericEventListener, Activateable2 {

	protected static final String USER_PROPS_ID = ForumUserListController.class.getCanonicalName();
	
	protected static final String VIEWMODE_THREAD = "thread";
	protected static final String VIEWMODE_FLAT = "flat";
	protected static final String VIEWMODE_MESSAGE = "message";
	private static final String GUI_PREFS_VIEWMODE_KEY = "forum.threadview.mode";
	private static final String GUI_PREFS_VIEWMODE_CLASS = "org.olat.modules.fo.ForumThreadViewModeController";
	
	private final VelocityContainer mainVC;

	private Link backLink, archiveThreadButton,
			stickyButton, removeStickyButton,
			closeThreadButton, openThreadButton,
			hideThreadButton, showThreadButton,
			allButton, allFlatButton, oneButton, markedButton, newButton;
	
	private CloseableModalController cmc;
	private MessageEditController editMessageCtrl, replyMessageCtrl;
	private DialogBoxController confirmDeleteCtrl, confirmSplitCtrl;
	private ForumMessageListController moveCtrl, messageTableCtrl;
	private StepsMainRunController exportCtrl;
	
	private Message thread;
	private boolean reloadList;
	private boolean hasMarkedNewMessages;
	
	private final Forum forum;
	private final boolean guestOnly;
	private final Formatter formatter;
	private final String thumbnailMapper;
	private final ForumCallback foCallback;
	private final OLATResourceable forumOres;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	private LoadMode loadMode;
	private List<MessageView> backupViews;

	@Autowired
	private UserManager userManager;
	@Autowired
	private ForumManager forumManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private OLATResourceManager olatManager;
	@Autowired
	private MarkingService markingService;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private PortfolioV2Module portfolioModule;
	@Autowired
	private ForumMediaHandler forumMediaHandler;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	public MessageListController(UserRequest ureq, WindowControl wControl,
			Forum forum, ForumCallback foCallback) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(Forum.class, getLocale(), getTranslator()));

		this.forum = forum;
		this.foCallback = foCallback;
		formatter = Formatter.getInstance(getLocale());
		forumOres = OresHelper.createOLATResourceableInstance("Forum", forum.getKey());
		guestOnly = ureq.getUserSession().getRoles().isGuestOnly();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		
		thumbnailMapper = registerCacheableMapper(ureq, "fo_att_" + forum.getKey(), new AttachmentsMapper());

		mainVC = createVelocityContainer("threadview");
		mainVC.contextPut("threadMode", Boolean.TRUE);
		mainVC.contextPut("thumbMapper", thumbnailMapper);
		mainVC.contextPut("guestOnly", Boolean.valueOf(guestOnly));
		
		messageTableCtrl = new ForumMessageListController(ureq, getWindowControl(), forum, false, true, true);
		listenTo(messageTableCtrl);
		mainVC.put("singleThreadTable", messageTableCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
		initButtons();
		
		// Register for forum events
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, getIdentity(), forum);
	}
	
	/**
	 * @return true if the controller has marked some new messages as seen
	 */
	public boolean hasMarkedNewMessages() {
		return hasMarkedNewMessages;
	}
	
	@Override
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, forum);
	}
	
	private void initButtons() {
		backLink = LinkFactory.createCustomLink("backLinkLT", "back", "listalltitles", Link.LINK_BACK, mainVC, this);

		archiveThreadButton = LinkFactory.createButtonSmall("archive.thread", mainVC, this);
		archiveThreadButton.setIconLeftCSS("o_icon o_icon-fw o_icon_archive_tool");
		
		stickyButton = LinkFactory.createLink("msg.sticky", mainVC, this);
		stickyButton.setIconLeftCSS("o_icon o_icon-fw o_forum_status_sticky_icon");
		removeStickyButton = LinkFactory.createLink("remove.sticky", mainVC, this);
		removeStickyButton.setIconLeftCSS("o_icon o_icon-fw o_forum_status_thread_icon");

		closeThreadButton = LinkFactory.createLink("close.thread", mainVC, this);
		closeThreadButton.setIconLeftCSS("o_icon o_icon-fw o_forum_status_closed_icon");
		openThreadButton = LinkFactory.createLink("open.thread", mainVC, this);
		openThreadButton.setIconLeftCSS("o_icon o_icon-fw o_forum_status_opened_icon");	

		hideThreadButton = LinkFactory.createLink("hide.thread", mainVC, this);
		hideThreadButton.setIconLeftCSS("o_icon o_icon-fw o_forum_status_hidden_icon");
		showThreadButton = LinkFactory.createLink("show.thread", mainVC, this);
		showThreadButton.setIconLeftCSS("o_icon o_icon-fw o_forum_status_visible_icon");

		allButton = LinkFactory.createButtonSmall("viewswitch.threadview", mainVC, this);
		allButton.setIconLeftCSS("o_icon o_icon-fw o_icon-flip-vertical o_forum_all_icon");
		allButton.setElementCssClass("o_forum_all_messages");
		allFlatButton = LinkFactory.createButtonSmall("viewswitch.flatview", mainVC, this);
		allFlatButton.setIconLeftCSS("o_icon o_icon-fw o_forum_all_flat_icon");
		allFlatButton.setElementCssClass("o_forum_all_flat_messages");
		oneButton = LinkFactory.createButtonSmall("viewswitch.messageview", mainVC, this);
		oneButton.setIconLeftCSS("o_icon o_icon-fw o_forum_one_icon");
		oneButton.setElementCssClass("o_forum_one_message");
		markedButton = LinkFactory.createButtonSmall("viewswitch.marked", mainVC, this);
		markedButton.setIconLeftCSS("o_icon o_icon-fw o_forum_marked_icon");
		markedButton.setElementCssClass("o_forum_marked_messages");
		newButton = LinkFactory.createButtonSmall("viewswitch.new", mainVC, this);
		newButton.setIconLeftCSS("o_icon o_icon-fw o_forum_new_icon");
		newButton.setElementCssClass("o_forum_new_messages");
	}
	
	private void updateButtons(Link activeLink) {
		allButton.setCustomEnabledLinkCSS(activeButton(allButton, activeLink));
		allFlatButton.setCustomEnabledLinkCSS(activeButton(allFlatButton, activeLink));
		oneButton.setCustomEnabledLinkCSS(activeButton(oneButton, activeLink));
		markedButton.setCustomEnabledLinkCSS(activeButton(markedButton, activeLink));
		newButton.setCustomEnabledLinkCSS(activeButton(newButton, activeLink));
		
		archiveThreadButton.setVisible(thread != null && foCallback.mayArchiveForum());
		if(thread == null || guestOnly || !foCallback.mayEditMessageAsModerator()) {
			closeThreadButton.setVisible(false);
			openThreadButton.setVisible(false);
			hideThreadButton.setVisible(false);
			showThreadButton.setVisible(false);
			stickyButton.setVisible(false);
			removeStickyButton.setVisible(false);
		} else {
			Status status = Status.getStatus(thread.getStatusCode());
			boolean isClosed = status.isClosed();
			boolean isHidden = status.isHidden();
			boolean isSticky = status.isSticky();
			closeThreadButton.setVisible(!isClosed);
			openThreadButton.setVisible(isClosed);
			hideThreadButton.setVisible(!isHidden);
			showThreadButton.setVisible(isHidden);
			stickyButton.setVisible(foCallback.mayEditMessageAsModerator() && thread != null && !isSticky);
			removeStickyButton.setVisible(foCallback.mayEditMessageAsModerator() && thread != null && isSticky);
		}
	}
	
	private String activeButton(Link link, Link activeLink) {
		return "btn btn-sm btn-default o_forum_tool " + (link == activeLink ? "active" : "");
	}
	
	private void reloadModel(UserRequest ureq, Message message) {
		reloadList = false;
		if(loadMode == LoadMode.thread) {
			loadThread(ureq, thread);
			String settings = doShowBySettings(ureq);
			if(VIEWMODE_MESSAGE.equals(settings)) {
				if(message != null && message.getKey() != null) {
					doSelectTheOne(ureq, message.getKey());
				}
			} else {
				scrollTo(message);
			}
		} else if(message != null && message.getKey() != null) {
			MessageView view = loadView(ureq, message);
			int index = 0;
			for(int i=0; i<backupViews.size(); i++) {
				if(backupViews.get(i).getKey().equals(message.getKey())) {
					backupViews.remove(backupViews.get(i));
					index = i;
					break;
				}
			}
			if(index >= 0) {
				backupViews.add(index, view);
			} else {
				backupViews.add(view);
			}
			mainVC.contextPut("messages", backupViews);
			messageTableCtrl.loadMessages(new ArrayList<>(0));

			updateButtons(allFlatButton);
			mainVC.contextPut("threadMode", Boolean.FALSE);
			scrollTo(message);
		}
	}
	
	/**
	 * The method doesn't scroll has the delete poped a blue box
	 * @param ureq
	 * @param message
	 */
	private void reloadModelAfterDelete(UserRequest ureq, MessageView message) {
		if(loadMode == LoadMode.thread) {
			loadThread(ureq, thread);
			doShowBySettings(ureq);
		} else if(message != null) {
			for(MessageView msg:backupViews) {
				if(msg.getKey().equals(message.getKey())) {
					backupViews.remove(msg);
					break;
				}
			}
			
			mainVC.contextPut("messages", backupViews);
			messageTableCtrl.loadMessages(new ArrayList<>(0));

			updateButtons(allFlatButton);
			mainVC.contextPut("threadMode", Boolean.FALSE);
		}
	}
	
	public void scrollTo(MessageRef ref) {
		if(ref != null && (thread == null || !thread.getKey().equals(ref.getKey()))) {
			mainVC.contextPut("goToMessage", new ConsumableBoolean(true));
			mainVC.contextPut("goToMessageId", ref.getKey());
		}
	}
	
	public void loadUserMessages(UserRequest ureq, Identity user) {
		loadMode = LoadMode.userMessages;
		List<MessageLight> messages = forumManager.getLightMessagesByUser(forum, user);
		backupViews = loadThread(ureq, messages, false);
		messageTableCtrl.loadMessages(new ArrayList<>(0));
		
		allButton.setVisible(false);
		updateButtons(allFlatButton);
		mainVC.contextPut("threadMode", Boolean.FALSE);
		mainVC.contextPut("filteredForFirstName", user.getUser().getProperty(UserConstants.FIRSTNAME, getLocale()));
		mainVC.contextPut("filteredForLastName", user.getUser().getProperty(UserConstants.LASTNAME, getLocale()));
	}
	
	public void loadUserMessagesUnderPseudo(UserRequest ureq, Identity user, String pseudonym) {
		loadMode = LoadMode.userMessagesUnderPseudo;
		List<MessageLight> messages = forumManager.getLightMessagesByUserUnderPseudo(forum, user, pseudonym);
		backupViews = loadThread(ureq, messages, false);
		messageTableCtrl.loadMessages(new ArrayList<>(0));
		
		allButton.setVisible(false);
		updateButtons(allFlatButton);
		mainVC.contextPut("threadMode", Boolean.FALSE);
		mainVC.contextRemove("filteredForFirstName");
		mainVC.contextPut("filteredForLastName", pseudonym);
	}
	
	public void loadGuestMessages(UserRequest ureq) {
		loadMode = LoadMode.guestMessages;
		List<MessageLight> messages = forumManager.getLightMessagesOfGuests(forum);
		backupViews = loadThread(ureq, messages, false);
		messageTableCtrl.loadMessages(new ArrayList<>(0));
		
		allButton.setVisible(false);
		updateButtons(allFlatButton);
		mainVC.contextPut("threadMode", Boolean.FALSE);
		mainVC.contextRemove("filteredForFirstName");
		mainVC.contextPut("filteredForLastName", translate("guest"));
	}
	
	public void loadThread(UserRequest ureq, Message threadMessage) {
		loadMode = LoadMode.thread;
		thread = threadMessage;

		List<MessageLight> messages = forumManager.getLightMessagesByThread(forum, thread);
		messages.add(0, thread);
		backupViews = loadThread(ureq, messages, true);
		messageTableCtrl.loadMessages(new ArrayList<>(0));
		
		allButton.setVisible(true);
		updateButtons(allButton);
		mainVC.contextPut("threadMode", Boolean.TRUE);
		mainVC.contextRemove("filteredForFirstName");
		mainVC.contextRemove("filteredForLastName");
	}
	
	private MessageView loadView(UserRequest ureq, MessageLight message) {
		Set<Long> rms =  null;
		Map<String,Mark> marks = Collections.emptyMap();
		List<String> subPaths = Collections.singletonList(message.getKey().toString());
		if(!guestOnly) {
			marks = new HashMap<>();
			List<Mark> markList = markingService.getMarkManager().getMarks(forumOres, getIdentity(), subPaths);
			for(Mark mark:markList) {
				marks.put(mark.getResSubPath(), mark);
			}
		}
		
		List<MarkResourceStat> statList = markingService.getMarkManager().getStats(forumOres, subPaths, getIdentity());
		Map<String,MarkResourceStat> stats = new HashMap<>(statList.size() * 2 + 1);
		for(MarkResourceStat stat:statList) {
			stats.put(stat.getSubPath(), stat);
		}
		
		String body = message.getBody();
		String messageMapperUri = thumbnailMapper + "/" + message.getKey() + "/";
		body = FilterFactory.getBaseURLToMediaRelativeURLFilter(messageMapperUri).filter(body);
		MessageView view = new MessageView(message, body, userPropertyHandlers, getLocale());
		view.setNumOfChildren(0);
		addMessageToCurrentMessagesAndVC(ureq, message, view, marks, stats, rms);
		return view;
	}
	
	private List<MessageView> loadThread(UserRequest ureq, List<MessageLight> messages, boolean reorder) {
		Set<Long> rms =  null;
		Map<String,Mark> marks = Collections.emptyMap();
		if(!guestOnly) {
			rms = forumManager.getReadSet(getIdentity(), forum);

			marks = new HashMap<>(marks.size() * 2 + 1);
			List<Mark> markList = markingService.getMarkManager().getMarks(forumOres, getIdentity(), null);
			for(Mark mark:markList) {
				marks.put(mark.getResSubPath(), mark);
			}
		}
		
		List<MarkResourceStat> statList = markingService.getMarkManager().getStats(forumOres, null, getIdentity());
		Map<String,MarkResourceStat> stats = new HashMap<>(statList.size() * 2 + 1);
		for(MarkResourceStat stat:statList) {
			stats.put(stat.getSubPath(), stat);
		}

		if(reorder) {
			List<MessageLight> orderedMessages = new ArrayList<>();
			orderMessagesThreaded(messages, orderedMessages, thread);
			messages = orderedMessages;
		}

		List<MessageView> views = new ArrayList<>(messages.size());
		Map<Long,MessageView> keyToViews = new HashMap<>();
		for(MessageLight msg:messages) {
			String body = msg.getBody();
			String messageMapperUri = thumbnailMapper + "/" + msg.getKey() + "/";
			body = FilterFactory.getBaseURLToMediaRelativeURLFilter(messageMapperUri).filter(body);
			MessageView view = new MessageView(msg, body, userPropertyHandlers, getLocale());
			view.setNumOfChildren(0);
			views.add(view);
			keyToViews.put(msg.getKey(), view);
		}

		//calculate depth and number of children
		for(MessageView view:views) {
			if(view.getParentKey() == null) {
				view.setDepth(0);
			} else {
				view.setDepth(1);
				for(MessageView parent = keyToViews.get(view.getParentKey()); parent != null; parent = keyToViews.get(parent.getParentKey())) {
					parent.setNumOfChildren(parent.getNumOfChildren() + 1);
					view.setDepth(view.getDepth() + 1);
				}
			}
		}
		
		//append ui things
		for (MessageLight msg: messages) {
			addMessageToCurrentMessagesAndVC(ureq, msg, keyToViews.get(msg.getKey()), marks, stats, rms);
		}
		
		mainVC.contextPut("messages", views);
		return views;
	}
	
	/**
	 * Orders the messages in the logical instead of chronological order.
	 * @param messages
	 * @param orderedList
	 * @param startMessage
	 */	
	private void orderMessagesThreaded(List<MessageLight> messages, List<MessageLight> orderedList, MessageLight startMessage) {
		if (messages == null || orderedList == null || startMessage == null) return;

		Map<Long, MessageNode> messagesMap = new HashMap<>();
		messagesMap.put(startMessage.getKey(), new MessageNode(startMessage));
		for(MessageLight message:messages) {
			if(message.getParentKey() != null) {
				messagesMap.put(message.getKey(), new MessageNode(message));
			}
		}

		List<MessageNode> roots = new ArrayList<>();
		for(MessageLight message:messages) {
			Long parentKey = message.getParentKey();
			if(parentKey != null && messagesMap.containsKey(parentKey)) {
				MessageNode parentMessage = messagesMap.get(parentKey);
				MessageNode messageNode = messagesMap.get(message.getKey());
				parentMessage.getChildren().add(messageNode);
			} else {
				MessageNode rootMessage = messagesMap.get(message.getKey());
				roots.add(rootMessage);
			}
		}
		
		for(MessageNode root:roots) {
			orderMessagesThreaded(root, orderedList);
		}
	}
	
	private void orderMessagesThreaded(MessageNode message, List<MessageLight> orderedList) {
		if(message == null) {
			return;
		}
		if(message.getMessage() != null) {
			orderedList.add(message.getMessage());
		}
		List<MessageNode> children = message.getChildren();
		if(!children.isEmpty()) {
			if(children.size() > 1) {
				Collections.sort(children);
			}
		
			for(MessageNode child:children) {
				orderMessagesThreaded(child, orderedList);
			}
		}
	}
	
	private void markRead(MessageLight message) {
		if(!guestOnly) {
			forumManager.markAsRead(getIdentity(), forum, message);
			hasMarkedNewMessages = true;
		}
	}
	
	private void addMessageToCurrentMessagesAndVC(UserRequest ureq, MessageLight m, MessageView messageView,
			Map<String,Mark> marks, Map<String,MarkResourceStat> stats, Set<Long> readSet) {
		
		// all values belonging to a message are stored in this map
		// these values can be accessed in velocity. make sure you clean up
		// everything
		// you create here in disposeCurrentMessages()!
		String keyString = m.getKey().toString();
		if (readSet == null || readSet.contains(m.getKey())) {
			messageView.setNewMessage(false);
		} else {// mark now as read
			markRead(m);
			messageView.setNewMessage(true);
		}
		// add some data now
		messageView.setFormattedCreationDate(formatter.formatDateAndTime(m.getCreationDate()));
		messageView.setFormattedLastModified(formatter.formatDateAndTime(m.getLastModified()));

		Identity creator = m.getCreator();
		Identity modifier = m.getModifier();
		if (modifier != null) {
			messageView.setModified(true);
			if(modifier.equals(creator) && StringHelper.containsNonWhitespace(m.getPseudonym())) {
				messageView.setModifierPseudonym(m.getPseudonym());
			} else {
				messageView.setModifierFirstName(modifier.getUser().getProperty(UserConstants.FIRSTNAME, getLocale()));
				messageView.setModifierLastName(modifier.getUser().getProperty(UserConstants.LASTNAME, getLocale()));
			}
			
			if(m.getModificationDate() != null) {
				messageView.setFormattedModificationDate(formatter.formatDateAndTime(m.getModificationDate()));
			} else {
				messageView.setFormattedModificationDate(messageView.getFormattedLastModified());
			}	
		} else {
			messageView.setModified(false);
		}
		
		boolean userIsMsgCreator = false;
		//keeps the first 15 chars
		if(creator != null) {
			userIsMsgCreator = getIdentity().equals(creator);
			if(!StringHelper.containsNonWhitespace(m.getPseudonym())) {
				messageView.setCreatorFirstname(Formatter.truncate(creator.getUser().getProperty(UserConstants.FIRSTNAME, getLocale()), 18));
				messageView.setCreatorLastname(Formatter.truncate(creator.getUser().getProperty(UserConstants.LASTNAME, getLocale()), 18));
			}
		}
		
		// message attachments
		VFSContainer msgContainer = forumManager.getMessageContainer(forum.getKey(), m.getKey());
		if(msgContainer != null) {
			messageView.setMessageContainer(msgContainer);
			List<VFSItem> attachmentsItem = msgContainer.getItems(new VFSLeafButSystemFilter());
			List<VFSLeaf> attachments = new ArrayList<>(attachmentsItem.size());
			for(VFSItem attachmentItem:attachmentsItem) {
				if(attachmentItem instanceof VFSLeaf) {
					attachments.add((VFSLeaf)attachmentItem);
				}
			}
			messageView.setAttachments(attachments);
		} else {
			messageView.setAttachments(new ArrayList<>());
		}

		// number of children and modify/delete permissions
		int numOfChildren = messageView.getNumOfChildren();
		
		messageView.setAuthor(userIsMsgCreator);
		boolean threadTop = m.getThreadtop() == null;
		messageView.setThreadTop(threadTop);
		boolean isThreadClosed;
		if(threadTop) {
			isThreadClosed = Status.getStatus(m.getStatusCode()).isClosed();
		} else {
			if(thread == null) {
				isThreadClosed = Status.getStatus(m.getThreadtop().getStatusCode()).isClosed();
			} else {
				isThreadClosed = Status.getStatus(thread.getStatusCode()).isClosed();
			}
		}
		messageView.setClosed(isThreadClosed);
		
		if(!guestOnly && !m.isGuest() && creator != null && !StringHelper.containsNonWhitespace(m.getPseudonym())) {
			// add portrait to map for later disposal and key for rendering in velocity
			DisplayPortraitController portrait = new DisplayPortraitController(ureq, getWindowControl(), creator, true, true, false, true);
			messageView.setPortrait(portrait);
			mainVC.put("portrait_".concat(keyString), portrait.getInitialComponent());
		  
			// Add link with username that is clickable
			String creatorFullName = StringHelper.escapeHtml(UserManager.getInstance().getUserDisplayName(creator));
			Link visitingCardLink = LinkFactory.createCustomLink("vc_".concat(keyString), "vc", creatorFullName, Link.LINK_CUSTOM_CSS + Link.NONTRANSLATED, mainVC, this);
			visitingCardLink.setUserObject(creator);
			LinkPopupSettings settings = new LinkPopupSettings(800, 600, "_blank");
			visitingCardLink.setPopup(settings);
			if (creator.getStatus().equals(Identity.STATUS_DELETED)) {
				// keep link to show something, but disable
				visitingCardLink.setEnabled(false);
			} else if(creator.getStatus().equals(Identity.STATUS_INACTIVE)) {
				visitingCardLink.setIconRightCSS("o_icon o_icon_identity_inactive");
				visitingCardLink.setTitle("creator.inactive");
			}
		}

		if(!isThreadClosed) {
			if((numOfChildren == 0 && userIsMsgCreator && foCallback.mayDeleteOwnMessage()) || foCallback.mayDeleteMessageAsModerator()) {
				Link deleteLink = LinkFactory.createCustomLink("dl_".concat(keyString), "dl", "msg.delete", Link.BUTTON_SMALL, mainVC, this);
				deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
				deleteLink.setUserObject(messageView);
			}
			
			if((numOfChildren == 0 && userIsMsgCreator && foCallback.mayEditOwnMessage()) || foCallback.mayEditMessageAsModerator()) {
				Link editLink = LinkFactory.createCustomLink("ed_".concat(keyString), "ed", "msg.update", Link.BUTTON_SMALL, mainVC, this);
				editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
				editLink.setUserObject(messageView);
			}
			
			if(foCallback.mayReplyMessage()) {
				Link quoteLink = LinkFactory.createCustomLink("qt_".concat(keyString), "qt", "msg.quote", Link.BUTTON_SMALL, mainVC, this);
				quoteLink.setElementCssClass("o_sel_forum_reply_quoted");
				quoteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_reply_with_quote");
				quoteLink.setUserObject(messageView);
				
				Link replyLink = LinkFactory.createCustomLink("rp_".concat(keyString), "rp", "msg.reply", Link.BUTTON_SMALL, mainVC, this);
				replyLink.setElementCssClass("o_sel_forum_reply");
				replyLink.setIconLeftCSS("o_icon o_icon-fw o_icon_reply");
				replyLink.setUserObject(messageView);
			}
			
			if(foCallback.mayEditMessageAsModerator()) {
				if (!threadTop) { 
					Link splitLink = LinkFactory.createCustomLink("split_".concat(keyString), "split", "msg.split", Link.LINK, mainVC, this);
					splitLink.setIconLeftCSS("o_icon o_icon-fw o_icon_split");
					splitLink.setUserObject(messageView);
					
					Link moveLink = LinkFactory.createCustomLink("move_".concat(keyString), "move", "msg.move", Link.LINK, mainVC, this);
					moveLink.setIconLeftCSS("o_icon o_icon-fw o_icon_move");
					moveLink.setUserObject(messageView);
				}
				Link exileLink = LinkFactory.createCustomLink("exile_".concat(keyString), "exile", "msg.exile", Link.LINK, mainVC, this);
				exileLink.setIconLeftCSS("o_icon o_icon-fw o_forum_status_thread_icon");
				exileLink.setUserObject(messageView);				
			}
		}
		
		Mark currentMark = marks.get(keyString);
		MarkResourceStat stat = stats.get(keyString);
		if(!guestOnly) {
			String businessPath = currentMark == null ?
					getWindowControl().getBusinessControl().getAsString() + "[Message:" + m.getKey() + "]"
					: currentMark.getBusinessPath();
			Controller markCtrl = markingService.getMarkController(ureq, getWindowControl(), currentMark, stat, forumOres, keyString, businessPath);
			listenTo(markCtrl);
			mainVC.put("mark_".concat(keyString), markCtrl.getInitialComponent());
		}
		
		if(userIsMsgCreator && !StringHelper.containsNonWhitespace(m.getPseudonym())) {
			if(portfolioModule.isEnabled()) {
				String businessPath = BusinessControlFactory.getInstance().getAsString(getWindowControl().getBusinessControl())
						+ "[Message:" + m.getKey() + "]";
				String collectorId = "eportfolio_" + keyString;
				Component collectorCmp = new MediaCollectorComponent(collectorId, getWindowControl(), m, forumMediaHandler, businessPath);
				mainVC.put(collectorId, collectorCmp);
			}
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	public void event(Event event) {
		if(event instanceof ForumChangedEvent) {
			ForumChangedEvent fce = (ForumChangedEvent)event;
			if(ForumChangedEvent.CHANGED_MESSAGE.equals(fce.getCommand())
					|| ForumChangedEvent.NEW_MESSAGE.equals(fce.getCommand())
					|| ForumChangedEvent.DELETED_MESSAGE.equals(fce.getCommand()) ) {
				Long threadtopKey = fce.getThreadtopKey();
				Long senderId = fce.getSendByIdentityKey();
				if(thread != null && threadtopKey != null && thread.getKey().equals(threadtopKey)
						&& (senderId == null || !senderId.equals(getIdentity().getKey()))) {
					reloadList = true;
				}
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(backLink == source) {
			fireEvent(ureq, Event.BACK_EVENT);
		} else if(archiveThreadButton == source) {
			doArchiveThread(ureq, thread);
		} else if (closeThreadButton == source) {
			doCloseThread();
		} else if (openThreadButton == source) {
			doOpenThread();
		} else if (hideThreadButton == source) {
			doHideThread();
		} else if (showThreadButton == source) {
			doShowThread();
		} else if (allButton == source) {
			doShowAll(ureq);
			saveViewSettings(ureq, VIEWMODE_THREAD);
		} else if (allFlatButton == source) {
			doShowAllFlat(ureq);
			saveViewSettings(ureq, VIEWMODE_FLAT);
		}  else if (oneButton == source) {
			doShowOne(ureq);
			saveViewSettings(ureq, VIEWMODE_MESSAGE);
		}  else if (markedButton == source) {
			doShowMarked(ureq);		
		}  else if (newButton == source) {
			doShowNew(ureq);		
		} else if(stickyButton == source || removeStickyButton == source) {
			doToogleSticky();
		} else if (source instanceof Link) {
			Link link = (Link)source;
			String command = link.getCommand();
			Object uobject = link.getUserObject();
			if (command.startsWith("qt")) {
				doReply(ureq, (MessageView)uobject, true);
			} else if (command.startsWith("rp")) {
				doReply(ureq, (MessageView)uobject, false);
			} else if (command.startsWith("dl")) {
				doConfirmDeleteMessage(ureq, (MessageView)uobject);
			} else if (command.startsWith("ed")) {
				doEditMessage(ureq, (MessageView)uobject);
			}	else if (command.startsWith("split")) {
				doConfirmSplit(ureq, (MessageView)uobject);
			} else if (command.startsWith("move")) {
				doMoveMessage(ureq, (MessageView)uobject);
			} else if (command.startsWith("exile")) {
				doExportForumItem(ureq, (MessageView)uobject);
			} else if(command.equals("vc")) {
				doOpenVisitingCard(ureq, (Identity)uobject);
			}
		} else if(mainVC == source) {
			String cmd = event.getCommand();
			if (cmd.startsWith("attachment")) {
				doDeliverAttachment(ureq, cmd);
			}
		}
	}

	private void doDeliverAttachment(UserRequest ureq, String cmd) {
		MediaResource res = null;
		try {
			int index = cmd.lastIndexOf('_');
			String attachmentPosition = cmd.substring(cmd.indexOf('_') + 1, index);
			String messageKey = cmd.substring(index + 1);
			
			int position = Integer.parseInt(attachmentPosition);
			Long key = Long.valueOf(messageKey);
			for(MessageView view:backupViews) {
				if(view.getKey().equals(key)) {
					List<VFSLeaf> attachments = view.getAttachments();
					VFSLeaf attachment = attachments.get(position - 1);//velocity counter start with 1
					VFSMediaResource fileResource = new VFSMediaResource(attachment);
					fileResource.setDownloadable(true); // prevent XSS attack
					res = fileResource;
				}
			}
		} catch (Exception e) {
			logError("Cannot deliver message attachment", e);
		}
		if(res == null) {
			res = new NotFoundMediaResource();
		}
		ureq.getDispatchResult().setResultingMediaResource(res);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == exportCtrl) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				if (event == Event.CHANGED_EVENT) {
					StepsRunContext runContext = exportCtrl.getRunContext();
					Message originTopMessage = (Message)runContext.get(SendMailStepForm.START_THREADTOP);
					originTopMessage = forumManager.loadMessage(originTopMessage.getKey());
					if (originTopMessage != null) {
						//refresh origin thread
						fireEvent(ureq, new SelectMessageEvent(SelectMessageEvent.SELECT_THREAD, originTopMessage.getKey()));
					} else {
						fireEvent(ureq, Event.BACK_EVENT);
					}					
				}
				getWindowControl().pop();
				removeAsListenerAndDispose(exportCtrl);
				exportCtrl = null;
			} 
		} else if (source == confirmDeleteCtrl) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				MessageView deletedMessage = (MessageView)confirmDeleteCtrl.getUserObject();
				doDeleteMessage(ureq, deletedMessage);
				
			}
		} else if(editMessageCtrl == source) {
			// edit done -> save 
			if(event instanceof  ErrorEditMessage) {
				handleEditError(ureq);
			} else {
				Message message = editMessageCtrl.getMessage();
				if(message != null) {
					if(thread != null && thread.getKey().equals(message.getKey())) {
						thread = message;
					}
					reloadModel(ureq, message);
				}
			}
			cmc.deactivate();
		} else if(replyMessageCtrl == source) {
			if(event instanceof  ErrorEditMessage) {
				handleEditError(ureq);
			} else {
				Message reply = replyMessageCtrl.getMessage();
				if(reply != null) {	
					reloadModel(ureq, reply);
				} else {
				  	showInfo("header.cannotsavemessage");
				}
			}
			cmc.deactivate();
		} else if(messageTableCtrl == source) {
			if(event instanceof SelectMessageEvent) {
				SelectMessageEvent sme = (SelectMessageEvent)event;
				doSelectTheOne(ureq, sme.getMessageKey());
			} else if (event instanceof MessageMarkedEvent) {
				MessageMarkedEvent mme = (MessageMarkedEvent) event;
				Message message = forumManager.getMessageById(mme.getSelectedMessageKey());
				reloadModel(ureq, message);
			}
		} else if(moveCtrl == source) {
			if(event instanceof SelectMessageEvent) {
				SelectMessageEvent sme = (SelectMessageEvent)event;
				doFinalizeMove(ureq, moveCtrl.getSelectView(), sme.getMessageKey());
				cmc.deactivate();
			}
		} else if(confirmSplitCtrl == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				MessageView splitedMessage = (MessageView)confirmSplitCtrl.getUserObject();
				doSplitThread(ureq, splitedMessage);
			}
		} else if(event instanceof MarkedEvent) {
			MarkedEvent me = (MarkedEvent) event;
			messageTableCtrl.onMarked(me.getMark());
		} else if(event instanceof UnmarkedEvent) {
			UnmarkedEvent ue = (UnmarkedEvent)event;
			messageTableCtrl.onUnmarked(ue.getSubPath());
		} else if(source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(replyMessageCtrl);
		removeAsListenerAndDispose(editMessageCtrl);
		removeAsListenerAndDispose(cmc);
		replyMessageCtrl = null;
		editMessageCtrl = null;
		cmc = null;
	}
	
	private void handleEditError(UserRequest ureq) {
		if(thread == null) {
			fireEvent(ureq, Event.BACK_EVENT);
			showWarning("error.message.deleted");
		} else if(forumManager.existsMessageById(thread.getKey())) {
			reloadModel(ureq, null);
			showWarning("error.message.deleted");
		} else {
			fireEvent(ureq, Event.BACK_EVENT);
			showWarning("error.message.deleted");
		}
	}

	private void doReply(UserRequest ureq, MessageView parent, boolean quote) {
		// user has clicked on button 'reply'
		if (foCallback.mayReplyMessage()) {
			Message newMessage = forumManager.createMessage(forum, getIdentity(), guestOnly);
			Message parentMessage = forumManager.getMessageById(parent.getKey());
			if(parentMessage == null) {
				handleEditError(ureq);
				return;
			}
			
			String reString = "";
			if(parent.isThreadTop()) {
				//add reString only for the first answer
				reString = translate("msg.title.re");
			}			
			newMessage.setTitle(reString + parentMessage.getTitle());
			if (quote) {
				String quoted = buildReplyWithQuote(parentMessage);
				newMessage.setBody(quoted);
			}

			replyMessageCtrl = new MessageEditController(ureq, getWindowControl(), forum, foCallback, newMessage, parentMessage, EditMode.reply);
			listenTo(replyMessageCtrl);
			
			String title = quote ? translate("msg.quote") : translate("msg.reply");
			cmc = new CloseableModalController(getWindowControl(), "close", replyMessageCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		} else {
			showInfo("may.not.reply.msg");
		}
	}
	
	private String buildReplyWithQuote(Message parentMessage) {
		// load message to form as quotation				
		StringBuilder quoteSb = new StringBuilder();
		quoteSb.append("<p></p><div class=\"o_quote_wrapper\"><div class=\"o_quote_author mceNonEditable\">");
		String date = formatter.formatDateAndTime(parentMessage.getCreationDate());
		String creatorName;
		if(StringHelper.containsNonWhitespace(parentMessage.getPseudonym())) {
			creatorName = parentMessage.getPseudonym();
		} else if(parentMessage.isGuest()) {
			creatorName = translate("guest");
		} else {
			User creator = parentMessage.getCreator().getUser();
			creatorName = creator.getProperty(UserConstants.FIRSTNAME, getLocale()) + " " + creator.getProperty(UserConstants.LASTNAME, getLocale());
		}
		
		String originalBody = parentMessage.getBody();
		String filteredBody = new QuoterFilter().filter(originalBody);
		quoteSb.append(translate("msg.quote.intro", new String[]{ date, creatorName}))
		     .append("</div><blockquote class=\"o_quote\">")
		     .append(filteredBody)
		     .append("</blockquote></div>")
		     .append("<p></p>");
		return quoteSb.toString();
	}
	
	private void doConfirmDeleteMessage(UserRequest ureq, MessageView message) {
		// user has clicked on button 'delete'
		// -> display modal dialog 'Do you really want to delete this message?'
		// 'yes': back to allThreadTable, 'no' back to messageDetails
		
		int numOfChildren = forumManager.countMessageChildren(message.getKey());
		boolean children = numOfChildren > 0;
		boolean userIsMsgCreator = message.isAuthor() ;
		String currentMsgTitle = StringHelper.escapeHtml(message.getTitle());
		
		if (foCallback.mayDeleteMessageAsModerator()) {
			// user is forum-moderator -> may delete every message on every level
			if (numOfChildren == 0) {
				confirmDeleteCtrl = activateYesNoDialog(ureq, null, translate("reallydeleteleaf", currentMsgTitle), confirmDeleteCtrl);
				confirmDeleteCtrl.setUserObject(message);
			} else if (numOfChildren == 1) {
				confirmDeleteCtrl = activateYesNoDialog(ureq, null, translate("reallydeletenode1", currentMsgTitle), confirmDeleteCtrl);
				confirmDeleteCtrl.setUserObject(message);
			} else {
				confirmDeleteCtrl = activateYesNoDialog(ureq, null, getTranslator().translate("reallydeletenodeN", new String[] { currentMsgTitle, Integer.toString(numOfChildren) }), confirmDeleteCtrl);
				confirmDeleteCtrl.setUserObject(message);
			}
		} else if (userIsMsgCreator && !children ) {
			// user may delete his own message if it has no children
			confirmDeleteCtrl = activateYesNoDialog(ureq, null, translate("reallydeleteleaf", currentMsgTitle), confirmDeleteCtrl);
			confirmDeleteCtrl.setUserObject(message);
		} else if (userIsMsgCreator && children) {
			// user may not delete his own message because it has at least one child
			showWarning("may.not.delete.msg.as.author");
		} else {
			// user isn't author of the current message
			showInfo("may.not.delete.msg");
		}
	}
	
	private void doDeleteMessage(UserRequest ureq, MessageView message) { 
		boolean userIsMsgCreator = message.isAuthor();
		if (foCallback.mayDeleteMessageAsModerator()
				|| (userIsMsgCreator && forumManager.countMessageChildren(message.getKey()) == 0)) {
			Message reloadedMessage = forumManager.getMessageById(message.getKey());
			
			
			if(reloadedMessage != null) {
				//this delete the topic / thread
				if(reloadedMessage.getParent() == null) {
					forumManager.deleteMessageTree(forum.getKey(), reloadedMessage);
					//delete topics
					ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_THREAD_DELETE, getClass(),
							LoggingResourceable.wrap(reloadedMessage));
					//back to thread list
					fireEvent(ureq, new DeleteThreadEvent());
					ForumChangedEvent event = new ForumChangedEvent(ForumChangedEvent.DELETED_THREAD, reloadedMessage.getKey(), reloadedMessage.getKey(), getIdentity());
					CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(event, forum);	
				} else {
					Message threadTop = reloadedMessage.getThreadtop();
					forumManager.deleteMessageTree(forum.getKey(), reloadedMessage);
					threadTop = forumManager.updateMessage(threadTop, true);
					if(thread != null) {
						thread = threadTop;//update with the fresh version
					}
					showInfo("deleteok");
					ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_MESSAGE_DELETE, getClass(),
						LoggingResourceable.wrap(reloadedMessage));
					//reload
					reloadModelAfterDelete(ureq, message);
					fireEvent(ureq, new DeleteMessageEvent());
					ForumChangedEvent event = new ForumChangedEvent(ForumChangedEvent.DELETED_MESSAGE, threadTop.getKey(), message.getKey(), getIdentity());
					CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(event, forum);	
				}
			}
		} else {
			showWarning("may.not.delete.msg.as.author");
		}
	}
	
	private void doEditMessage(UserRequest ureq, MessageView message) {
		// user has clicked on button 'edit'
		boolean userIsMsgCreator = message.isAuthor();
		boolean children = forumManager.countMessageChildren(message.getKey()) > 0;
		if (foCallback.mayEditMessageAsModerator() || (userIsMsgCreator && !children)) {
			Message reloadedMessage = forumManager.loadMessage(message.getKey());
			if(reloadedMessage == null) {
				showWarning("error.message.deleted");
				reloadModel(ureq, null);
			} else {
				editMessageCtrl = new MessageEditController(ureq, getWindowControl(), forum, foCallback, reloadedMessage, null, EditMode.edit);
				listenTo(editMessageCtrl);
				
				String title = translate("msg.update");
				cmc = new CloseableModalController(getWindowControl(), "close", editMessageCtrl.getInitialComponent(), true, title);
				listenTo(editMessageCtrl);
				cmc.activate();
			}
		} else if (userIsMsgCreator && children) {
			// user is author of the current message but it has already at least
			// one child
			showWarning("may.not.save.msg.as.author");
		} else {
			// user isn't author of the current message
			showInfo("may.not.edit.msg");
		}
	}
	
	private void doConfirmSplit(UserRequest ureq, MessageView message) {		
		if (foCallback.mayEditMessageAsModerator()) {
			// user is forum-moderator -> may delete every message on every level
			int numOfChildren = forumManager.countMessageChildren(message.getKey());
			// provide yesNoSplit as argument, this ensures that dc is disposed before newly created
			String text =  translate("reallysplitthread", new String[] { message.getTitle(), Integer.toString(numOfChildren) });
			confirmSplitCtrl = activateYesNoDialog(ureq, null, text, confirmSplitCtrl);
			confirmSplitCtrl.setUserObject(message);
		}
	}
	
	private void doSplitThread(UserRequest ureq, MessageView message) {
		if (foCallback.mayEditMessageAsModerator()) {
			Message reloadedMessage = forumManager.getMessageById(message.getKey());
			Message newTopMessage = forumManager.splitThread(reloadedMessage);
			//do logging
			ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_THREAD_SPLIT, getClass(), LoggingResourceable.wrap(newTopMessage));
			showInfo("new.thread.location");
			//open the new thread
			fireEvent(ureq, new SelectMessageEvent(SelectMessageEvent.SELECT_THREAD, newTopMessage.getKey()));
		} else {
			showWarning("may.not.split.thread");
		}
	}
	
	private void doArchiveThread(UserRequest ureq, Message currMsg) {
		Message m = currMsg.getThreadtop();
		Long topMessageId = (m == null) ? currMsg.getKey() : m.getKey();
		ForumDownloadResource download = new ForumDownloadResource("Forum", forum, foCallback, topMessageId, getLocale());
		ureq.getDispatchResult().setResultingMediaResource(download);
	}
	
	private void doToogleSticky() {
		Status status = Status.getStatus(thread.getStatusCode());
		status.setSticky(!status.isSticky());
		thread.setStatusCode(Status.getStatusCode(status));
		thread = forumManager.updateMessage(thread, false);
		DBFactory.getInstance().commit();
		
		stickyButton.setVisible(!status.isSticky() && foCallback.mayEditMessageAsModerator());
		removeStickyButton.setVisible(status.isSticky() && foCallback.mayEditMessageAsModerator());
		mainVC.setDirty(true);
		
		ForumChangedEvent event = new ForumChangedEvent(ForumChangedEvent.STICKY, thread.getKey(), null, getIdentity());
		CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(event, forumOres);	
		ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_MESSAGE_EDIT, getClass(), LoggingResourceable.wrap(thread));
	}
	
	/**
	 * Sets the closed status to the thread message.
	 * @param ureq
	 * @param msg
	 * @param closed
	 */
	private void doCloseThread() {	
		if (thread != null) {
			thread = forumManager.getMessageById(thread.getKey());
			Status status = Status.getStatus(thread.getStatusCode());
			status.setClosed(true);
			thread.setStatusCode(Status.getStatusCode(status));
			thread = forumManager.updateMessage(thread, false);
			DBFactory.getInstance().commit();// before sending async event
			
			closeThreadButton.setVisible(false);
			openThreadButton.setVisible(!guestOnly);
			mainVC.setDirty(true);
			
			ForumChangedEvent event = new ForumChangedEvent(ForumChangedEvent.CLOSE, thread.getKey(), null, getIdentity());
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(event, forumOres);	
			ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_THREAD_CLOSE, getClass(), LoggingResourceable.wrap(thread));
		}
	}
	
	private void doOpenThread() {	
		if (thread != null) {
			thread = forumManager.getMessageById(thread.getKey());
			Status status = Status.getStatus(thread.getStatusCode());
			status.setClosed(false);
			thread.setStatusCode(Status.getStatusCode(status));
			thread = forumManager.updateMessage(thread, true);
			DBFactory.getInstance().commit();// before sending async event
			
			closeThreadButton.setVisible(!guestOnly);
			openThreadButton.setVisible(false);
			mainVC.setDirty(true);

			ForumChangedEvent event = new ForumChangedEvent(ForumChangedEvent.OPEN, thread.getKey(), null, getIdentity());
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(event, forumOres);	
			ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_THREAD_REOPEN, getClass(), LoggingResourceable.wrap(thread));
		}
	}
	
	/**
	 * Sets the hidden status to the thread message.
	 * @param ureq
	 * @param msg
	 * @param hidden
	 */
	private void doHideThread() {
		if (thread != null) {
			thread = forumManager.getMessageById(thread.getKey());
			Status status = Status.getStatus(thread.getStatusCode());
			status.setHidden(true);
			thread.setStatusCode(Status.getStatusCode(status));
			thread = forumManager.updateMessage(thread, false);
			DBFactory.getInstance().commit();// before sending async event
			
			hideThreadButton.setVisible(false);
			showThreadButton.setVisible(!guestOnly);
			mainVC.setDirty(true);

			ForumChangedEvent event = new ForumChangedEvent(ForumChangedEvent.HIDE, thread.getKey(), null, getIdentity());
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(event, forumOres);	
			ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_THREAD_HIDE, getClass(), LoggingResourceable.wrap(thread));
		}
	}
	
	/**
	 * Sets the hidden status to the threadtop message.
	 * @param ureq
	 * @param msg
	 * @param hidden
	 */
	private void doShowThread() {
		if (thread != null) {
			thread = forumManager.getMessageById(thread.getKey());
			Status status = Status.getStatus(thread.getStatusCode());
			status.setHidden(false);
			thread.setStatusCode(Status.getStatusCode(status));
			thread = forumManager.updateMessage(thread, true);
			DBFactory.getInstance().commit();// before sending async event
			
			hideThreadButton.setVisible(!guestOnly);
			showThreadButton.setVisible(false);
			mainVC.setDirty(true);

			ForumChangedEvent event = new ForumChangedEvent(ForumChangedEvent.SHOW, thread.getKey(), null, getIdentity());
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(event, forumOres);	
			ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_THREAD_SHOW, getClass(), LoggingResourceable.wrap(thread));
		}
	}
	
	protected String doShowBySettings(UserRequest ureq) {
		String viewSettings = getViewSettings(ureq);
		switch(viewSettings) {
			case VIEWMODE_THREAD: doShowAll(ureq); break;
			case VIEWMODE_FLAT: doShowAllFlat(ureq); break;
			case VIEWMODE_MESSAGE: doShowOne(ureq); break;
			default: doShowAll(ureq);
		}
		return viewSettings;
	}
	
	private void doShowAll(UserRequest ureq) {
		if(reloadList) {
			reloadModel(ureq, null);
		}
		updateButtons(allButton);
		mainVC.contextPut("threadMode", Boolean.TRUE);
		mainVC.contextPut("messages", backupViews);
		mainVC.contextRemove("mode");
	}
	
	private void doShowAllFlat(UserRequest ureq) {
		if(reloadList) {
			reloadModel(ureq, null);
		}
		updateButtons(allFlatButton);
		mainVC.contextPut("threadMode", Boolean.FALSE);
		mainVC.contextPut("messages", backupViews);
		mainVC.contextRemove("mode");
	}
	
	private void doShowOne(UserRequest ureq) {
		if(reloadList) {
			reloadModel(ureq, null);
		}
		updateButtons(oneButton);
		mainVC.contextPut("mode", "one");
		mainVC.contextPut("threadMode", Boolean.FALSE);
		
		if(backupViews != null && !backupViews.isEmpty()) {
			List<MessageView> oneView = new ArrayList<>(1);
			oneView.add(backupViews.get(0));
			mainVC.contextPut("messages", oneView);
			messageTableCtrl.setSelectView(oneView.get(0));
			messageTableCtrl.loadMessages(new ArrayList<>(backupViews));
		}
	}

	private String getViewSettings(UserRequest ureq) {
		Preferences prefs = ureq.getUserSession().getGuiPreferences();
		Object setting = prefs.get(GUI_PREFS_VIEWMODE_CLASS, GUI_PREFS_VIEWMODE_KEY);
		if(VIEWMODE_THREAD.equals(setting) || VIEWMODE_FLAT.equals(setting) || VIEWMODE_MESSAGE.equals(setting)) {
			return (String)setting;
		}
		return VIEWMODE_THREAD;
	}

	private void saveViewSettings(UserRequest ureq, String viewMode) {
		Preferences prefs = ureq.getUserSession().getGuiPreferences();
		prefs.putAndSave(GUI_PREFS_VIEWMODE_CLASS, GUI_PREFS_VIEWMODE_KEY, viewMode);
	}
	
	private void doSelectTheOne(UserRequest ureq, Long messageKey) {
		if(reloadList) {
			reloadModel(ureq, null);
		}
		updateButtons(oneButton);
		mainVC.contextPut("mode", "one");
		mainVC.contextPut("threadMode", Boolean.FALSE);
		
		if(backupViews != null && !backupViews.isEmpty()) {
			List<MessageView> oneView = new ArrayList<>(1);
			for(MessageView message:backupViews) {
				if(message.getKey().equals(messageKey)) {
					oneView.add(message);
				}
			}
			mainVC.contextPut("messages", oneView);
			if(!oneView.isEmpty()) {
				messageTableCtrl.setSelectView(oneView.get(0));
			} else {
				showWarning("error.message.deleted");
			}
			messageTableCtrl.loadMessages(new ArrayList<>(backupViews));
		}
	}
	
	protected void doShowMarked(UserRequest ureq) {
		if(reloadList) {
			reloadModel(ureq, null);
		}
		
		updateButtons(markedButton);
		mainVC.contextPut("threadMode", Boolean.FALSE);
		mainVC.contextPut("mode", "marked");
		
		List<Mark> markList = markingService.getMarkManager().getMarks(forumOres, getIdentity(), null);
		Set<String> marks = new HashSet<>(markList.size() * 2 + 1);
		for(Mark mark:markList) {
			marks.add(mark.getResSubPath());
		}

		List<MessageView> views = new ArrayList<>();
		for(MessageView view:backupViews) {
			if(marks.contains(view.getKey().toString())) {
				views.add(view);
			}
		}
		
		mainVC.contextPut("messages", views);
	}
	
	protected void doShowNew(UserRequest ureq) {
		if(reloadList) {
			reloadModel(ureq, null);
		}
		
		updateButtons(newButton);
		mainVC.contextPut("threadMode", Boolean.FALSE);
		mainVC.contextPut("mode", "new");
		
		List<MessageView> views = new ArrayList<>();
		for(MessageView view:backupViews) {
			if(view.isNewMessage()) {
				views.add(view);
			}
		}
		mainVC.contextPut("messages", views);
	}
	
	private void doMoveMessage(UserRequest ureq, MessageView message) {
		removeAsListenerAndDispose(moveCtrl);
		removeAsListenerAndDispose(cmc);
		
		if (foCallback.mayEditMessageAsModerator()) {
			moveCtrl = new ForumMessageListController(ureq, getWindowControl(), forum, true, false, false);
			moveCtrl.loadAllMessages();
			moveCtrl.setSelectView(message);
			listenTo(moveCtrl);

			//push the modal dialog with the table as content
			String title = "";
			cmc = new CloseableModalController(getWindowControl(), "close", moveCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			
			cmc.activate();
		}
	}
	
	private void doExportForumItem(UserRequest ureq, MessageView messageToMove) {
		if (foCallback.mayEditMessageAsModerator()) {
			StepRunnerCallback finish = new FinishCallback(); 
			Translator trans = Util.createPackageTranslator(Step_1_SelectCourse.class, getLocale());
			Step start = new Step_1_SelectCourse(ureq);
			Message message = forumManager.getMessageById(messageToMove.getKey());
			String wizardTitle = trans.translate("title.wizard.movethread", new String[]{message.getTitle()}); 
			exportCtrl = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, wizardTitle,
					"o_sel_bulk_assessment_wizard");
			StepsRunContext runContext = exportCtrl.getRunContext();
			// can be threadtop message
			runContext.put(SendMailStepForm.MESSAGE_TO_MOVE, message);
			// starting thread
			runContext.put(SendMailStepForm.START_THREADTOP, message.getThreadtop() == null ? message : message.getThreadtop());
			// get start course
			RepositoryEntry courseEntry = getStartCourse();
			if(courseEntry != null) {
				runContext.put(SendMailStepForm.START_COURSE, courseEntry);
			}
			
			listenTo(exportCtrl);
			getWindowControl().pushAsModalDialog(exportCtrl.getInitialComponent());
		} else {
			showWarning("may.not.move.message");
		}
	}
	
	private RepositoryEntry getStartCourse() {
		PropertyManager propertyManager = PropertyManager.getInstance();
		Long forumResourceableId = forum.getResourceableId();
		Property forumproperty = propertyManager.getPropertyByLongValue(forumResourceableId, FOCourseNode.CONFIG_FORUM_KEY);
		if (forumproperty != null) {
			Long resourcetypeId = forumproperty.getResourceTypeId();
			String resourcetypeName = forumproperty.getResourceTypeName();
			OLATResourceable olatResourceable = olatManager.findResourceable(resourcetypeId, resourcetypeName);
			RepositoryEntry startCourse = repositoryManager.lookupRepositoryEntry(olatResourceable, false);
			if (startCourse != null) {
				return startCourse;
			}
		}
		SubscriptionContext ctx = foCallback.getSubscriptionContext();
		if(ctx != null && "CourseModule".equals(ctx.getResName())) {
			OLATResourceable olatResourceable = OresHelper.createOLATResourceableInstance("CourseModule", ctx.getResId());
			RepositoryEntry startCourse = repositoryManager.lookupRepositoryEntry(olatResourceable, false);
			if (startCourse != null) {
				return startCourse;
			}
		}
		
		return null;
	}
	
	private void doFinalizeMove(UserRequest ureq, MessageView messageToMove, Long parentMessageKey) {
		if (foCallback.mayEditMessageAsModerator()) {
			Message message = forumManager.getMessageById(messageToMove.getKey());
			Message parentMessage = forumManager.getMessageById(parentMessageKey);
			message = forumManager.moveMessage(message, parentMessage);
			markRead(message);
			DBFactory.getInstance().commit();//commit before sending event
			
			ThreadLocalUserActivityLogger.log(ForumLoggingAction.FORUM_MESSAGE_MOVE, getClass(), LoggingResourceable.wrap(message));
			Long threadKey = parentMessage.getThreadtop() == null ? parentMessage.getKey() : parentMessage.getThreadtop().getKey();
			fireEvent(ureq, new SelectMessageEvent(SelectMessageEvent.SELECT_THREAD, threadKey, message.getKey()));
		} else {
			showWarning("may.not.move.message");
		}
	}
	
	private void doOpenVisitingCard(UserRequest ureq, Identity creator) {
		ControllerCreator userInfoMainControllerCreator = (lureq, lwControl)
				-> new UserInfoMainController(lureq, lwControl, creator, true, false);
		//wrap the content controller into a full header layout
		ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, userInfoMainControllerCreator);
		PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
		pbw.open(ureq);
	}
	
	public enum LoadMode {
		thread,
		userMessages,
		userMessagesUnderPseudo,
		guestMessages,
	}
	
	private static class MessageNode implements Comparable<MessageNode> {
		private final MessageLight message;
		private final List<MessageNode> children = new ArrayList<>();
		
		private MessageNode(MessageLight message) {
			this.message = message;
		}
		
		public MessageLight getMessage() {
			return message;
		}
		
		public List<MessageNode> getChildren() {
			return children;
		}
		
		@Override
		public int compareTo(MessageNode arg0) {
			return message.getCreationDate().compareTo(arg0.getMessage().getCreationDate());
		}
	}
	
	private class AttachmentsMapper implements Mapper {
		
		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			String[] query = relPath.split("/"); // expected path looks like this /messageId/attachmentUUID/filename
			
			MediaResource resource = null;
			if (query.length == 4) {
				MessageView view = getView(query[1]);
				if (view != null) {
					if("media".equals(query[2])) {
						resource = getMedia(view, query[3]);
					} else {
						resource = getThumbnail(view, query[2]);
					}
				}
			}
			// In any error case, send not found
			if(resource == null) {
				resource = new NotFoundMediaResource();
			}
			return resource;
		}
		 
		private MessageView getView(String queryParam) {
			MessageView view = null;
			try {
				Long mId = Long.valueOf(Long.parseLong(queryParam ));
				for (MessageView m : backupViews) {
					// search for message in current message map
					if (m.getKey().equals(mId)) {
						view = m;
						break;
					}
				}
			} catch (NumberFormatException e) {
				//
			}
			return view;
		}
		
		private MediaResource getMedia(MessageView view, String queryParam) {
			VFSContainer messageContainer = view.getMessageContainer();
			if(messageContainer == null) return null;
			VFSItem mediaItem = messageContainer.resolve("media");
			if(mediaItem instanceof VFSContainer) {
				VFSContainer mediaContainer = (VFSContainer)mediaItem;
				VFSItem media = mediaContainer.resolve(queryParam);
				if(media instanceof VFSLeaf) {
					return new VFSMediaResource((VFSLeaf)media);
				}
			}
			return null;
		}
		
		private MediaResource getThumbnail(MessageView view, String queryParam) {
			List<VFSLeaf> attachments = view.getAttachments();
			for (VFSLeaf attachment : attachments) {
				VFSMetadata meta = attachment.getMetaInfo();
				if (meta.getUuid().equals(queryParam)) {
					VFSLeaf thumb = vfsRepositoryService.getThumbnail(attachment, meta, 200, 200, false);
					if(thumb != null) {
						// Positive lookup, send as response
						return new VFSMediaResource(thumb);
					}
					break;
				}
			}
			return null;
		}
	}
}