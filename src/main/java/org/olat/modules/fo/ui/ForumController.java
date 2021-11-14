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

package org.olat.modules.fo.ui;

import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.ForumCallback;
import org.olat.modules.fo.ForumChangedEvent;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.Status;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.modules.fo.ui.events.DeleteMessageEvent;
import org.olat.modules.fo.ui.events.DeleteThreadEvent;
import org.olat.modules.fo.ui.events.SelectMessageEvent;
import org.olat.modules.fo.ui.events.SelectUserEvent;
import org.olat.modules.fo.ui.events.SelectUserListEvent;
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

	private VelocityContainer forumPanel;

	private ThreadListController threadListCtrl;
	private ForumUserListController userListCtrl;
	private MessageListController viewCtrl, userViewCtrl;
	
	private Forum forum;
	private ForumCallback focallback;
	private boolean reloadThreadList = false;

	private SubscriptionContext subsContext;
	private ContextualSubscriptionController csc;

	@Autowired
	private ForumManager fm;
	@Autowired
	private BaseSecurity securityManager;

	/**
	 * @param forum
	 * @param focallback
	 * @param ureq
	 * @param wControl
	 */
	public ForumController(UserRequest ureq, WindowControl wControl,
			Forum forum, ForumCallback focallback, boolean showSubscriptionButton) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(Forum.class, getLocale(), getTranslator()));

		this.forum = forum;
		this.focallback = focallback;
		addLoggingResourceable(LoggingResourceable.wrap(forum));

		forumPanel = createVelocityContainer("forum");

		// --- subscription ---
		subsContext = focallback.getSubscriptionContext();
		// if sc is null, then no subscription is desired
		if (subsContext != null && showSubscriptionButton) {
			String businessPath = wControl.getBusinessControl().getAsString();
			String data = String.valueOf(forum.getKey());
			PublisherData pdata = new PublisherData(OresHelper.calculateTypeName(Forum.class), data, businessPath);
			
			csc = new ContextualSubscriptionController(ureq, getWindowControl(), subsContext, pdata);
			listenTo(csc);
			forumPanel.put("subscription", csc.getInitialComponent());
		}

		threadListCtrl = new ThreadListController(ureq, getWindowControl(), forum, focallback);
		listenTo(threadListCtrl);
		threadListCtrl.loadModel();

		// Default view
		putInitialPanel(forumPanel);
		putContent(threadListCtrl);
					
		// Register for forum events
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, getIdentity(), forum);
	}

	@Override
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, forum);
        super.doDispose();
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			doThreadList(ureq);
		} else {
			String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
			if("Message".equalsIgnoreCase(type)) {
				Long resId = entries.get(0).getOLATResourceable().getResourceableId();
				if (resId.longValue() != 0) {
					Message message = fm.getMessageById(resId);
					if (message != null) {
						doThreadList(ureq);
						Message thread = message.getThreadtop() == null ? message : message.getThreadtop();
						if(focallback.mayEditMessageAsModerator() || !Status.getStatus(thread.getStatusCode()).isHidden()) {
							String subType = null;
							if(entries.size() > 1) {
								subType = entries.get(1).getOLATResourceable().getResourceableTypeName();
							}
							
							if("Marked".equalsIgnoreCase(subType)) {
								doMarkedView(ureq, thread, message);
							} else if("New".equalsIgnoreCase(subType)) {
								doMarkedView(ureq, thread, message);
							} else {
								doThreadView(ureq, thread, message);
							}
						}
					}
				}
			} else if("Identity".equalsIgnoreCase(type)) {
				Long resId = entries.get(0).getOLATResourceable().getResourceableId();
				doUserList(ureq);
				if (resId.longValue() > 0) {
					doUserMessageList(ureq, resId);
				}
			}
		}
	}

	@Override
	public void event(Event event) {
		if(event instanceof ForumChangedEvent) {
			reloadThreadList = true;
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (threadListCtrl ==  source) {
			if(event instanceof SelectMessageEvent) {
				doProcessSelectEvent(ureq, (SelectMessageEvent)event);
			} else if(event instanceof SelectUserListEvent) {
				doUserList(ureq);
			}
		} else if(viewCtrl == source) {
			if(event == Event.BACK_EVENT) {
				reloadThreadList |= viewCtrl.hasMarkedNewMessages();
				doThreadList(ureq);
			} else if(event instanceof DeleteThreadEvent) {
				reloadThreadList = true;
				doThreadList(ureq);
			} else if(event instanceof DeleteMessageEvent) {
				reloadThreadList = true;
			} else if(event instanceof SelectMessageEvent) {
				doProcessSelectEvent(ureq, (SelectMessageEvent)event);
			}
		} else if(userViewCtrl == source) {
			if(event == Event.BACK_EVENT) {
				reloadThreadList |= userViewCtrl.hasMarkedNewMessages();
				cleanUpMessageViews();
				doUserList(ureq);
			} else if(event instanceof SelectMessageEvent) {
				doProcessSelectEvent(ureq, (SelectMessageEvent)event);
			}
		} else if(userListCtrl == source) {
			if(event == Event.BACK_EVENT) {
				removeAsListenerAndDispose(userListCtrl);
				userListCtrl = null;
				
				doThreadList(ureq);
			} else if(event instanceof SelectUserEvent) {
				SelectUserEvent sue = (SelectUserEvent)event;
				if(sue.isGuest()) {
					doGuestMessageList(ureq);
				} else if(StringHelper.containsNonWhitespace(sue.getPseudonym())) {
					doPseudonymMessageList(ureq, sue.getIdentityKey(), sue.getPseudonym());
				} else if(sue.getIdentityKey() != null) {
					doUserMessageList(ureq, sue.getIdentityKey());
				}
			}
		}
	}
	
	private void doProcessSelectEvent(UserRequest ureq, SelectMessageEvent sme) {
		Message thread = fm.getMessageById(sme.getMessageKey());
		if(thread == null) {
			logError("Thread doesn't exists: " + sme.getMessageKey(), new Exception());
			reloadThreadList = true;
			doThreadList(ureq);
		} else {
			Message scrollTo = null;
			if(sme.getScrollToMessageKey() != null) {
				scrollTo = fm.getMessageById(sme.getScrollToMessageKey());
			}
			if(SelectMessageEvent.SELECT_THREAD.equals(sme.getCommand())) {
				doThreadView(ureq, thread, scrollTo);
			} else if(SelectMessageEvent.SELECT_MARKED.equals(sme.getCommand())) {
				doMarkedView(ureq, thread, scrollTo);
			} else if(SelectMessageEvent.SELECT_NEW.equals(sme.getCommand())) {
				doNewView(ureq, thread, scrollTo);
			}
		}
	}
	
	private void doThreadView(UserRequest ureq, Message thread, Message scrollTo) {
		cleanUpMessageViews();
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Message", thread.getKey());
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		viewCtrl = new MessageListController(ureq, bwControl, forum, focallback);
		viewCtrl.loadThread(ureq, thread);
		viewCtrl.scrollTo(scrollTo);
		viewCtrl.doShowBySettings(ureq);
		listenTo(viewCtrl);
		putContent(viewCtrl);
		addToHistory(ureq, viewCtrl);
	}
	
	private void doMarkedView(UserRequest ureq, Message thread, Message scrollTo) {
		cleanUpMessageViews();
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Message", thread.getKey());
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		OLATResourceable markedOres = OresHelper.createOLATResourceableInstance("Marked", 0l);
		WindowControl bbwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(markedOres, null, bwControl);
		
		viewCtrl = new MessageListController(ureq, bbwControl, forum, focallback);
		viewCtrl.loadThread(ureq, thread);
		viewCtrl.scrollTo(scrollTo);
		viewCtrl.doShowMarked(ureq);
		listenTo(viewCtrl);
		putContent(viewCtrl);
		addToHistory(ureq, viewCtrl);
	}
	
	private void doNewView(UserRequest ureq, Message thread, Message scrollTo) {
		cleanUpMessageViews();
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Message", thread.getKey());
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		OLATResourceable markedOres = OresHelper.createOLATResourceableInstance("New", 0l);
		WindowControl bbwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(markedOres, null, bwControl);
		
		viewCtrl = new MessageListController(ureq, bbwControl, forum, focallback);
		viewCtrl.loadThread(ureq, thread);
		viewCtrl.scrollTo(scrollTo);
		viewCtrl.doShowNew(ureq);
		listenTo(viewCtrl);
		putContent(viewCtrl);
		addToHistory(ureq, viewCtrl);
	}
	
	private void doUserMessageList(UserRequest ureq, Long identityKey) {
		cleanUpMessageViews();

		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Identity", identityKey);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		userViewCtrl = new MessageListController(ureq, bwControl, forum, focallback);
		Identity user = securityManager.loadIdentityByKey(identityKey);
		userViewCtrl.loadUserMessages(ureq, user);
		listenTo(userViewCtrl);
		putContent(userViewCtrl);
		addToHistory(ureq, userViewCtrl);
	}
	
	private void doGuestMessageList(UserRequest ureq) {
		cleanUpMessageViews();
		
		userViewCtrl = new MessageListController(ureq, getWindowControl(), forum, focallback);
		userViewCtrl.loadGuestMessages(ureq);
		listenTo(userViewCtrl);
		putContent(userViewCtrl);
	}
	
	private void doPseudonymMessageList(UserRequest ureq, Long identityKey, String pseudonym) {
		cleanUpMessageViews();
		
		userViewCtrl = new MessageListController(ureq, getWindowControl(), forum, focallback);
		Identity user = securityManager.loadIdentityByKey(identityKey);
		userViewCtrl.loadUserMessagesUnderPseudo(ureq, user, pseudonym);
		listenTo(userViewCtrl);
		putContent(userViewCtrl);
	}
	
	private void doThreadList(UserRequest ureq) {
		cleanUpMessageViews();
		
		if(reloadThreadList) {
			threadListCtrl.loadModel();
			reloadThreadList = false;
		}
		
		putContent(threadListCtrl);
		addToHistory(ureq, threadListCtrl);
	}

	private void doUserList(UserRequest ureq) {
		cleanUpMessageViews();
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Identity", 0l);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
		userListCtrl = new ForumUserListController(ureq, bwControl, forum);
		listenTo(userListCtrl);
		putContent(userListCtrl);
		addToHistory(ureq, userListCtrl);
	}
	
	private void putContent(Controller controller) {
		forumPanel.put("forum", controller.getInitialComponent());
	}
	
	private void cleanUpMessageViews() {
		removeAsListenerAndDispose(userViewCtrl);
		removeAsListenerAndDispose(viewCtrl);
		userViewCtrl = null;
		viewCtrl = null;
	}
}