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
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkingService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSort;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Util;
import org.olat.core.util.mail.ui.BooleanCSSCellRenderer;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.MessageLight;
import org.olat.modules.fo.Status;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.modules.fo.ui.ForumMessageDataModel.ForumMessageCols;
import org.olat.modules.fo.ui.events.SelectMessageEvent;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ForumMessageListController extends FormBasicController {
	
	protected static final String USER_PROPS_ID = ForumUserListController.class.getCanonicalName();
	
	public static final int USER_PROPS_OFFSET = 500;
	
	private FlexiTableElement tableEl;
	private ForumMessageDataModel dataModel;
	
	private final Forum forum;
	private final boolean withType;
	private final boolean showMarks;
	private final boolean showNew;
	private final boolean guestOnly;
	private final boolean isAdministrativeUser;
	private final OLATResourceable forumOres;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private MessageView userObject, selectView;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private ForumManager forumManager;
	@Autowired
	private MarkingService markingService;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public ForumMessageListController(UserRequest ureq, WindowControl wControl,
			Forum forum, boolean withType, boolean showMarks, boolean showNew) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(Forum.class, getLocale(), getTranslator()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.forum = forum;
		this.withType = withType;
		this.showMarks = showMarks;
		this.showNew = showNew;
		this.guestOnly = ureq.getUserSession().getRoles().isGuestOnly();
		forumOres = OresHelper.createOLATResourceableInstance("Forum", forum.getKey());
		
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		
		initForm(ureq);
	}
	
	public MessageView getUserObject() {
		return userObject;
	}

	public void setUserObject(MessageView userObject) {
		this.userObject = userObject;
	}

	public MessageView getSelectView() {
		return selectView;
	}

	public void setSelectView(MessageView selectView) {
		this.selectView = selectView;
	}

	public void loadAllMessages() {
		Set<Long> readSet = !guestOnly? forumManager.getReadSet(getIdentity(), forum): Collections.emptySet();
		List<MessageLight> allMessages = forumManager.getLightMessagesByForum(forum);
		List<MessageLightView> views = new ArrayList<>(allMessages.size());
		Map<Long,MessageLightView> keyToViews = new HashMap<>();
		for(MessageLight message:allMessages) {
			MessageLightView view = new MessageLightView(message, userPropertyHandlers, getLocale());
			if (readSet.contains(message.getKey())) {
				view.setNewMessage(false);
			} else {
				view.setNewMessage(true);
			}
			views.add(view);
			keyToViews.put(view.getKey(), view);
		}
		
		//calculate depth
		Map<Long, List<Long>> keyToParentline = new HashMap<>();
		for(MessageLightView view:views) {
			if(view.getParentKey() == null) {
				view.setDepth(0);
			} else {
				List<Long> parentLine = new ArrayList<>(5);
				view.setDepth(1);
				for(MessageLightView parent = keyToViews.get(view.getParentKey()); parent != null; parent = keyToViews.get(parent.getParentKey())) {
					view.setDepth(view.getDepth() + 1);
					parentLine.add(parent.getKey());
				}
				keyToParentline.put(view.getKey(), parentLine);
			}
		}
		
		//order
		List<MessageNode> threads = convertToThreadTrees(views);
		Collections.sort(threads, new MessageNodeComparator());
		List<MessageLightView> orderedViews = new ArrayList<>(allMessages.size());
		flatTree(threads, orderedViews);
		List<MessageLightViewRow> rows = appendLinks(orderedViews);
		dataModel.setObjects(rows);
	}
	
	public void loadMessages(List<MessageLightView> views) {
		List<MessageLightViewRow> rows = appendLinks(views);
		dataModel.setObjects(rows);
		tableEl.reloadData();
		tableEl.reset();
	}

	private List<MessageLightViewRow> appendLinks(List<MessageLightView> views) {
		List<Mark> markList = !guestOnly
				? markingService.getMarkManager().getMarks(forumOres, getIdentity(), null)
				: Collections.emptyList();
		Map<String,Mark> marks = new HashMap<>();
		for (Mark mark : markList) {
			marks.put(mark.getResSubPath(), mark);
		}
		
		List<MessageLightViewRow> rows = new ArrayList<>(views.size());
		for (MessageLightView view : views) {
			Mark mark = marks.get(view.getKey().toString());
			boolean marked = mark != null;
			FormLink markLink = uifactory.addFormLink("mark_" + view.getKey(), "mark", "", null, null, Link.NONTRANSLATED);
			markLink.setIconLeftCSS(marked? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
			
			MessageLightViewRow row = new MessageLightViewRow(view, mark, markLink);
			markLink.setUserObject(row);
			rows.add(row);
		}
		return rows;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		List<FlexiTableSort> sorts = new ArrayList<>();
		sorts.add(new FlexiTableSort(translate("natural.sort"), "natural"));
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(withType) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ForumMessageCols.type, new StatusTypeCellRenderer()));
			sorts.add(new FlexiTableSort(translate(ForumMessageCols.type.i18nHeaderKey()), ForumMessageCols.type.name()));
		}
		
		if (showMarks) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ForumMessageCols.mark));
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ForumMessageCols.thread,
				"select", new StaticFlexiCellRenderer("select", new IndentCellRenderer())));
		sorts.add(new FlexiTableSort(translate(ForumMessageCols.thread.i18nHeaderKey()), ForumMessageCols.thread.name()));
		
		int colPos = USER_PROPS_OFFSET;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;

			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);

			FlexiColumnModel col;
			if(UserConstants.FIRSTNAME.equals(propName) || UserConstants.LASTNAME.equals(propName)) {
				col = new DefaultFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, "select", true, propName,
						new StaticFlexiCellRenderer("select", new TextFlexiCellRenderer()));
			} else {
				col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, true, propName);
			}

			sorts.add(new FlexiTableSort(translate(userPropertyHandler.i18nColumnDescriptorLabelKey()), propName));
			columnsModel.addFlexiColumnModel(col);
			colPos++;
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ForumMessageCols.lastModified));
		sorts.add(new FlexiTableSort(translate(ForumMessageCols.lastModified.i18nHeaderKey()), ForumMessageCols.lastModified.name()));

		if(showNew && !guestOnly) {
			FlexiCellRenderer newMessageRenderer = new BooleanCSSCellRenderer(getTranslator(),
					"o_icon o_forum_new_icon", null, "table.new.message.hover", null);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ForumMessageCols.newMessage, newMessageRenderer));
		}

		dataModel = new ForumMessageDataModel(columnsModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "messages", dataModel, getTranslator(), formLayout);
		tableEl.setCssDelegate(new MessageCssDelegate());
		
		FlexiTableSortOptions sortOptions = new FlexiTableSortOptions();
		sortOptions.setFromColumnModel(false);
		sortOptions.setSorts(sorts);
		tableEl.setSortSettings(sortOptions);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("select".equals(cmd)) {
					MessageLightView message = dataModel.getObject(se.getIndex()).getView();
					fireEvent(ureq, new SelectMessageEvent(SelectMessageEvent.SELECT_MESSAGE, message.getKey()));
				}
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if ("mark".equals(cmd)) {
				MessageLightViewRow row = (MessageLightViewRow) link.getUserObject();
				if (row.isMarked()) {
					doUnmark(row);
					link.setIconLeftCSS(Mark.MARK_ADD_CSS_LARGE);
				} else {
					doMark(row);
					link.setIconLeftCSS(Mark.MARK_CSS_LARGE);
				}
				link.getComponent().setDirty(true);
				fireEvent(ureq, new MessageMarkedEvent(selectView.getKey(), row.getView().getKey()));
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doMark(MessageLightViewRow row) {
		MessageLightView view = row.getView();
		Mark currentMark = row.getMark();
		String businessPath = currentMark == null ?
				getWindowControl().getBusinessControl().getAsString() + "[Message:" + view.getKey() + "]"
				: currentMark.getBusinessPath();
		Mark mark = markingService.getMarkManager().setMark(forumOres, getIdentity(), view.getKey().toString(), businessPath);
		row.setMark(mark);
	}

	private void doUnmark(MessageLightViewRow row) {
		MessageLightView view = row.getView();
		markingService.getMarkManager().removeMark(forumOres, getIdentity(), view.getKey().toString());
		row.setMark(null);
	}

	private void flatTree(List<MessageNode> nodes, List<MessageLightView> orderedViews) {
		for(MessageNode node:nodes) {
			orderedViews.add(node.getView());
			if(node.hasChildren()) {
				flatTree(node.getChildren(), orderedViews);
			}
		}
	}
	
	private List<MessageNode> convertToThreadTrees(List<MessageLightView> messages){
		List<MessageNode> topNodeList = new ArrayList<>();
	
		for (Iterator<MessageLightView> iterTop = messages.iterator(); iterTop.hasNext();) {
			MessageLightView msg = iterTop.next();
			if (msg.getParentKey() == null) {
				iterTop.remove();
				MessageNode topNode = new MessageNode(msg);
				addChildren(messages, topNode);
				topNodeList.add(topNode);
			}
		}	
		return topNodeList;
	}
	
	private void addChildren(List<MessageLightView> messages, MessageNode mn){
		for(Iterator<MessageLightView> iterMsg = messages.iterator(); iterMsg.hasNext(); ) {
			MessageLightView msg = iterMsg.next();
			if ((msg.getParentKey() != null) && (msg.getParentKey().equals(mn.getKey()))){
				MessageNode childNode = new MessageNode(msg);
				mn.addChild(childNode);
				addChildren(messages, childNode);
			}
		}
	}
	
	private class MessageCssDelegate extends DefaultFlexiTableCssDelegate {
		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			MessageLightView row = dataModel.getObject(pos).getView();
			return row != null && selectView != null && row.getKey().equals(selectView.getKey()) ? "o_row_selected" : null;
		}
	}
	
	private static class MessageNode {
		
		private final MessageLightView view;
		private List<MessageNode> children;
		
		public MessageNode(MessageLightView view) {
			this.view = view;
		}
		
		public Long getKey() {
			return view.getKey();
		}
		
		public boolean isSticky() {
			return Status.getStatus(view.getStatusCode()).isSticky();
		}
		
		public Date getLastModified() {
			return view.getLastModified();
		}
		
		public MessageLightView getView() {
			return view;
		}
		
		public boolean hasChildren() {
			return children != null && children.size() > 0;
		}
		
		public void addChild(MessageNode child) {
			if(children == null) {
				children = new ArrayList<>();
			}
			children.add(child);
		}
		
		public List<MessageNode> getChildren() {
			return children;
		}
	}
	
	public static class MessageNodeComparator implements Comparator<MessageNode> {
		@Override
		public int compare(final MessageNode m1, final MessageNode m2) {			
			if(m1.isSticky() && m2.isSticky()) {
				return m2.getLastModified().compareTo(m1.getLastModified()); //last first
			} else if(m1.isSticky()) {
				return -1;
			} else if(m2.isSticky()){
				return 1;
			} else {
				return m2.getLastModified().compareTo(m1.getLastModified()); //last first
			}				
		}
	}
}