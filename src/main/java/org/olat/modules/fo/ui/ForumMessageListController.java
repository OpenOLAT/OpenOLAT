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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRowCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Util;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.MessageLight;
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
public class ForumMessageListController extends FormBasicController implements FlexiTableRowCssDelegate {
	
	protected static final String USER_PROPS_ID = ForumUserListController.class.getCanonicalName();
	
	public static final int USER_PROPS_OFFSET = 500;
	
	private FlexiTableElement tableEl;
	private ForumMessageDataModel dataModel;
	
	private final Forum forum;
	private final boolean withType;
	private final boolean isAdministrativeUser;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private MessageView userObject, selectView;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private ForumManager forumManager;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public ForumMessageListController(UserRequest ureq, WindowControl wControl,
			Forum forum, boolean withType) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(Forum.class, getLocale(), getTranslator()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.forum = forum;
		this.withType = withType;
		
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

	@Override
	public String getRowCssClass(int pos) {
		MessageLightView row = dataModel.getObject(pos);
		return row != null && selectView != null && row.getKey().equals(selectView.getKey()) ? "o_row_selected" : null;
	}

	public void loadAllMessages() {
		List<MessageLight> allMessages = forumManager.getLightMessagesByForum(forum);
		List<MessageLightView> views = new ArrayList<>(allMessages.size());
		Map<Long,MessageLightView> keyToViews = new HashMap<>();
		for(MessageLight message:allMessages) {
			MessageLightView view = new MessageLightView(message, userPropertyHandlers, getLocale());
			views.add(view);
			keyToViews.put(view.getKey(), view);
		}

		//TODO forum: implement a reorder method which works on threads and parent line
		/*calculate depth
		
		for(MessageLightView view:views) {
			if(view.getParentKey() == null) {
				view.setDepth(0);
			} else {
				view.setDepth(1);
				for(MessageLightView parent = keyToViews.get(view.getParentKey()); parent != null; parent = keyToViews.get(parent.getParentKey())) {
					view.setDepth(view.getDepth() + 1);
				}
			}
		}
		
		//order
		 */

		dataModel.setObjects(views);
	}
	/*
	private class MessageComparator implements Comparator<MessageLightView> {

		@Override
		public int compare(MessageLightView v1, MessageLightView v2) {
			Long tt1 = v1.getThreadtopKey() == null ? v1.getKey() : v1.getThreadtopKey();
			Long tt2 = v2.getThreadtopKey() == null ? v2.getKey() : v2.getThreadtopKey();
			int c = Long.compare(tt1.longValue(), tt2.longValue());
			if(c == 0) {
				
			}
			return c;
		}
	}
	*/
	
	public void loadMessages(List<MessageLightView> views) {
		dataModel.setObjects(views);
		tableEl.reloadData();
		tableEl.reset();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(withType) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ForumMessageCols.type.i18nKey(), ForumMessageCols.type.ordinal(),
				true, ForumMessageCols.type.name(), new StatusTypeCellRenderer()));
		}
		columnsModel.addFlexiColumnModel(new StaticFlexiColumnModel(ForumMessageCols.thread.i18nKey(), ForumMessageCols.thread.ordinal(),
				"select", true, ForumMessageCols.thread.name(), new StaticFlexiCellRenderer("select", new IndentCellRenderer())));
		
		int colPos = USER_PROPS_OFFSET;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;

			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);

			FlexiColumnModel col;
			if(UserConstants.FIRSTNAME.equals(propName) || UserConstants.LASTNAME.equals(propName)) {
				col = new StaticFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, "select", true, propName,
						new StaticFlexiCellRenderer("select", new TextFlexiCellRenderer()));
			} else {
				col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, true, propName);
			}
			columnsModel.addFlexiColumnModel(col);
			colPos++;
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ForumMessageCols.lastModified.i18nKey(), ForumMessageCols.lastModified.ordinal(),
				true, ForumMessageCols.lastModified.name()));

		dataModel = new ForumMessageDataModel(columnsModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "messages", dataModel, getTranslator(), formLayout);
		tableEl.setRowCssDelegate(this);
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
					MessageLightView message = dataModel.getObject(se.getIndex());
					fireEvent(ureq, new SelectMessageEvent(SelectMessageEvent.SELECT_MESSAGE, message.getKey()));
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
}