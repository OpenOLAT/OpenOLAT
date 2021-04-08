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
package org.olat.modules.fo.export;

import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.nodes.FOCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.ForumCallback;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.Status;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.modules.fo.model.ForumThread;
import org.olat.modules.fo.ui.StatusTypeCellRenderer;
import org.olat.modules.fo.ui.StickyCellRenderer;
import org.olat.modules.fo.ui.ThreadListDataModel;
import org.olat.modules.fo.ui.ThreadListDataModel.ThreadListCols;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial Date: 15.02.2017
 * @author fkiefer, fabian.kiefer@frentix.com, www.frentix.com
 */
public class SelectThreadStepForm extends StepFormBasicController {

	private Forum forum;
	
	private FlexiTableElement threadTable;
	private ThreadListDataModel threadTableModel;
	
	private FormLink newThreadButton;
	private boolean guestOnly;
	private final ForumCallback foCallback;

	
	@Autowired
	private ForumManager forumManager;
	@Autowired
	private UserManager userManager; 

 
	public SelectThreadStepForm(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext, String customLayoutPageName) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_VERTICAL, customLayoutPageName);
		setTranslator(Util.createPackageTranslator(Forum.class, getLocale(), getTranslator()));

		guestOnly = ureq.getUserSession().getRoles().isGuestOnly();
		foCallback = new DefaultReadOnlyForumCallback();
		
		FOCourseNode node = (FOCourseNode)getFromRunContext(SendMailStepForm.FORUM);
		ICourse course = (ICourse)getFromRunContext(SendMailStepForm.ICOURSE);
		CourseEnvironment courseEnv = course.getCourseEnvironment();
		forum = node.loadOrCreateForum(courseEnv);
		initForm(ureq);
	}


	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == threadTable) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				ForumThread row = threadTableModel.getObject(se.getIndex());
				if("select".equals(cmd)) {
					doSelect(ureq, row);
				}
			}
		} else if (source == newThreadButton) {
			displayAsNewThread(ureq);
		} 
		super.formInnerEvent(ureq, source, event);
	}
		
	private void doSelect(UserRequest ureq, ForumThread row) {
		long parentMessageKey = row.getKey();
		Message parentMessage = forumManager.getMessageById(parentMessageKey);
		Boolean newThread = (Boolean)getFromRunContext(SendMailStepForm.NEW_THREAD);
		if (newThread != null && newThread) {
			return;
		} else {
			addToRunContext(SendMailStepForm.PARENT_MESSAGE, parentMessage);
			formOK(ureq);
		}
	}
	
	private void displayAsNewThread(UserRequest ureq) {
		Message messageToMove = (Message)getFromRunContext(SendMailStepForm.MESSAGE_TO_MOVE);
		String creatorFullname = userManager.getUserDisplayName(messageToMove.getCreator());
		Date lastModified = messageToMove.getLastModified();
		int numOfPosts = forumManager.countMessageChildren(messageToMove.getKey()) + 1;
		ForumThread row = new ForumThread(messageToMove, creatorFullname, lastModified, numOfPosts);
		List<ForumThread> threads = threadTableModel.getObjects();
		if (containsMessage(row)) {
			showWarning("thread.already.exits");
		} else {
			threads.add(row);
			addToRunContext(SendMailStepForm.NEW_THREAD, Boolean.TRUE);
			threadTableModel.setObjects(threads);
			threadTableModel.sort(new SortKey(ThreadListCols.thread.name(), true));
			threadTable.reloadData();
			threadTable.reset();
			// move on to next wizard step directly
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}
	}
	
	private boolean containsMessage(ForumThread row) {
		Identity identity = guestOnly ? null : getIdentity();
		List<ForumThread> threads = forumManager.getForumThreads(forum, identity);
		for (ForumThread forumThread : threads) {
			if (row.getKey().equals(forumThread.getKey())) {
				return true;
			}
		}
		List<ForumThread> tablethreads = threadTableModel.getObjects();
		for (ForumThread forumThread : tablethreads) {
			if (row.getKey().equals(forumThread.getKey())) {
				return true;
			}
		}
		return false;
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOK = true;
		Boolean newThread = (Boolean)getFromRunContext(SendMailStepForm.NEW_THREAD);
		if (newThread != null) {
			allOK &= newThread;
		} else {
			allOK &= containsRunContextKey(SendMailStepForm.PARENT_MESSAGE);			
		}		
		return allOK &= super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}


	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ThreadListCols.type, new StatusTypeCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ThreadListCols.thread, "choose",
				 new StaticFlexiCellRenderer("choose", new StickyCellRenderer())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ThreadListCols.creator));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ThreadListCols.lastModified));
		if(!guestOnly) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ThreadListCols.markedMessages,
					"marked", new StaticFlexiCellRenderer("marked", new TextFlexiCellRenderer())));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ThreadListCols.unreadMessages,
					"unread", new StaticFlexiCellRenderer("unread", new TextFlexiCellRenderer())));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ThreadListCols.totalMessages));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ThreadListCols.select, "select",
				new StaticFlexiCellRenderer(translate("select"), "select", "", "o_icon o_icon_select o_icon-fw")));		
		
		threadTableModel = new ThreadListDataModel(columnsModel, getTranslator());
		threadTable = uifactory.addTableElement(getWindowControl(), "threads", threadTableModel, getTranslator(), formLayout);
		threadTable.setCustomizeColumns(false);
		threadTable.setElementCssClass("o_forum");
		threadTable.setEmptyTableSettings("forum.emtpy", null, "o_forum_status_thread_icon");
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setDefaultOrderBy(new SortKey(ThreadListCols.lastModified.name(), false));
		threadTable.setSortSettings(sortOptions);
		
		loadModel();		

		newThreadButton = uifactory.addFormLink("link.new.thread", formLayout, Link.BUTTON);
		newThreadButton.setIconLeftCSS("o_icon o_icon-fw o_forum_status_thread_icon");
		newThreadButton.setElementCssClass("o_sel_forum_thread_new");

	}
	
	private void loadModel() {
		Identity identity = guestOnly ? null : getIdentity();
		Message messageToMove = (Message)getFromRunContext(SendMailStepForm.MESSAGE_TO_MOVE);
		messageToMove = messageToMove.getThreadtop() == null ? messageToMove : messageToMove.getThreadtop();
		List<ForumThread> threads = forumManager.getForumThreads(forum, identity);
		if (!foCallback.mayEditMessageAsModerator()) {
			for (Iterator<ForumThread> threadIt = threads.iterator(); threadIt.hasNext();) {
				ForumThread next = threadIt.next();
				if (Status.getStatus(next.getStatusCode()).isHidden()) {
					threadIt.remove();
				} else if (messageToMove.getKey().equals(next.getKey())) {
					threadIt.remove();
				}
			}
		}
		
		threadTableModel.setObjects(threads);
		threadTableModel.sort(new SortKey(ThreadListCols.thread.name(), true));
		threadTable.reloadData();
		threadTable.reset();
	}

}
