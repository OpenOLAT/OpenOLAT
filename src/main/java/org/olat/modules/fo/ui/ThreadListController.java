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

import java.util.Iterator;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.ForumCallback;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.MessageRef;
import org.olat.modules.fo.Status;
import org.olat.modules.fo.archiver.formatters.ForumDownloadResource;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.modules.fo.model.ForumThread;
import org.olat.modules.fo.ui.MessageEditController.EditMode;
import org.olat.modules.fo.ui.ThreadListDataModel.ThreadListCols;
import org.olat.modules.fo.ui.events.SelectMessageEvent;
import org.olat.modules.fo.ui.events.SelectUserListEvent;
import org.olat.search.SearchModule;
import org.olat.search.SearchServiceUIFactory;
import org.olat.search.SearchServiceUIFactory.DisplayOption;
import org.olat.search.ui.SearchInputController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ThreadListController extends FormBasicController {
	
	private FlexiTableElement threadTable;
	private ThreadListDataModel threadTableModel;
	private FormLink archiveForumButton, newThreadButton, userListButton;
	
	private CloseableModalController cmc;
	private MessageEditController newThreadCtrl;
	private SearchInputController searchController;
	
	private final Forum forum;
	private final boolean guestOnly;
	private final ForumCallback foCallback;

	@Autowired
	private SearchModule searchModule;
	@Autowired
	private ForumManager forumManager;
	
	public ThreadListController(UserRequest ureq, WindowControl wControl, Forum forum, ForumCallback foCallback) {
		super(ureq, wControl, "threads");
		setTranslator(Util.createPackageTranslator(Forum.class, getLocale(), getTranslator()));

		this.forum = forum;
		this.foCallback = foCallback;
		guestOnly = ureq.getUserSession().getRoles().isGuestOnly();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(foCallback.mayOpenNewThread()) {
			newThreadButton = uifactory.addFormLink("msg.create", formLayout, Link.BUTTON_SMALL);
			newThreadButton.setIconLeftCSS("o_icon o_icon-fw o_forum_status_thread_icon");
			newThreadButton.setElementCssClass("o_sel_forum_thread_new");
		}
		if(foCallback.mayArchiveForum()) {
			archiveForumButton = uifactory.addFormLink("archive.forum", formLayout, Link.BUTTON_SMALL);
			archiveForumButton.setIconLeftCSS("o_icon o_icon-fw o_icon_archive_tool");
			archiveForumButton.setElementCssClass("o_sel_forum_archive");
		}
		if(foCallback.mayFilterForUser() ) {
			userListButton = uifactory.addFormLink("filter", formLayout, Link.BUTTON_SMALL);
			userListButton.setIconLeftCSS("o_icon o_icon-fw o_icon_user");
			userListButton.setElementCssClass("o_sel_forum_filter");
		}
		
		if(formLayout instanceof FormLayoutContainer) {
			SearchServiceUIFactory searchServiceUIFactory = (SearchServiceUIFactory)CoreSpringFactory.getBean(SearchServiceUIFactory.class);
			searchController = searchServiceUIFactory.createInputController(ureq, getWindowControl(), DisplayOption.STANDARD, mainForm);
			if(guestOnly && !searchModule.isGuestEnabled()) {
				searchController.setResourceContextEnable(false);
			}
			
			listenTo(searchController);
			((FormLayoutContainer)formLayout).add("search_input", searchController.getFormItem());
		}

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ThreadListCols.type, new StatusTypeCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ThreadListCols.thread, "select",
				 new StaticFlexiCellRenderer("select", new StickyCellRenderer())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ThreadListCols.creator));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ThreadListCols.lastModified));
		if(!guestOnly) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ThreadListCols.markedMessages,
					"marked", new StaticFlexiCellRenderer("marked", new TextFlexiCellRenderer())));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ThreadListCols.unreadMessages,
					"unread", new StaticFlexiCellRenderer("unread", new TextFlexiCellRenderer())));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ThreadListCols.totalMessages));
		
		threadTableModel = new ThreadListDataModel(columnsModel, getTranslator());
		threadTable = uifactory.addTableElement(getWindowControl(), "threads", threadTableModel, getTranslator(), formLayout);
		threadTable.setCustomizeColumns(false);
		threadTable.setElementCssClass("o_forum");
		threadTable.setEmptyTableSettings("forum.emtpy", null, "o_forum_status_thread_icon");
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(ThreadListCols.lastModified.name(), false));
		threadTable.setSortSettings(sortOptions);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public void loadModel() {
		Identity identity = guestOnly ? null : getIdentity();
		List<ForumThread> threads = forumManager.getForumThreads(forum, identity);
		if(!foCallback.mayEditMessageAsModerator()) {
			for(Iterator<ForumThread> threadIt=threads.iterator(); threadIt.hasNext(); ) {
				if(Status.getStatus(threadIt.next().getStatusCode()).isHidden()) {
					threadIt.remove();
				}
			}	
		}
		
		threadTableModel.setObjects(threads);
		threadTableModel.sort(new SortKey(ThreadListCols.thread.name(), true));
		threadTable.reloadData();
		threadTable.reset();
		
		if(archiveForumButton != null) {
			archiveForumButton.setVisible(threads.size() > 0);
		}
		if(userListButton != null) {
			userListButton.setVisible(threads.size() > 0);
		}
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(searchController != null) {
			searchController.event(ureq, source, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(newThreadCtrl == source) {
			if(event == Event.DONE_EVENT) {
				DBFactory.getInstance().commit();
				loadModel();
				doSelect(ureq, newThreadCtrl.getMessage());
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(newThreadCtrl);
		removeAsListenerAndDispose(cmc);
		newThreadCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(archiveForumButton == source) {
			doArchiveForum(ureq);
		} else if(newThreadButton == source) {
			doNewThread(ureq);
		} else if(userListButton == source) {
			doOpenUserList(ureq);
		} else if(source == threadTable) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				ForumThread row = threadTableModel.getObject(se.getIndex());
				if("select".equals(cmd)) {
					doSelect(ureq, row);
				} else if("marked".equals(cmd)) {
					doSelectMarked(ureq, row);
				} else if("unread".equals(cmd)) {
					doSelectNew(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doSelect(UserRequest ureq, MessageRef message) {
		fireEvent(ureq, new SelectMessageEvent(SelectMessageEvent.SELECT_THREAD, message.getKey()));
	}
	
	private void doSelectMarked(UserRequest ureq, MessageRef message) {
		fireEvent(ureq, new SelectMessageEvent(SelectMessageEvent.SELECT_MARKED, message.getKey()));
	}
	
	private void doSelectNew(UserRequest ureq, MessageRef message) {
		fireEvent(ureq, new SelectMessageEvent(SelectMessageEvent.SELECT_NEW, message.getKey()));
	}
	
	private void doArchiveForum(UserRequest ureq) {
		ForumDownloadResource download = new ForumDownloadResource("Forum", forum, foCallback, null, getLocale());
		ureq.getDispatchResult().setResultingMediaResource(download);
	}
	
	private void doNewThread(UserRequest ureq) {
		removeAsListenerAndDispose(newThreadCtrl);
		removeAsListenerAndDispose(cmc);

		// user has clicked on button 'open new thread'.
		Message m = forumManager.createMessage(forum, getIdentity(), guestOnly);
		newThreadCtrl = new MessageEditController(ureq, getWindowControl(), forum, foCallback, m, null, EditMode.newThread);
		listenTo(newThreadCtrl);
		
		String title = translate("msg.create");
		cmc = new CloseableModalController(getWindowControl(), "close", newThreadCtrl.getInitialComponent(), true, title);
		listenTo(newThreadCtrl);
		cmc.activate();
	}
	
	private void doOpenUserList(UserRequest ureq) {
		fireEvent(ureq, new SelectUserListEvent());
	}
}
