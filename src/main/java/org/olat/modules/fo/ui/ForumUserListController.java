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
import java.util.List;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Util;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.modules.fo.model.ForumUser;
import org.olat.modules.fo.model.ForumUserStatistics;
import org.olat.modules.fo.ui.ForumUserDataModel.UserCols;
import org.olat.modules.fo.ui.events.SelectUserEvent;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11.11.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ForumUserListController extends FormBasicController {

	protected static final String USER_PROPS_ID = ForumUserListController.class.getCanonicalName();
	
	public static final int USER_PROPS_OFFSET = 500;
	
	private FormLink backLink;
	private FlexiTableElement tableEl;
	private ForumUserDataModel dataModel;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	private final Forum forum;

	@Autowired
	private UserManager userManager;
	@Autowired
	private ForumManager forumManager;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public ForumUserListController(UserRequest ureq, WindowControl wControl, Forum forum) {
		super(ureq, wControl, "user_list");
		setTranslator(Util.createPackageTranslator(Forum.class, getLocale(), getTranslator()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.forum = forum;
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		backLink = uifactory.addFormLink("back", formLayout, Link.LINK_BACK);
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		int colPos = USER_PROPS_OFFSET;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;

			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);

			FlexiColumnModel col;
			if(UserConstants.FIRSTNAME.equals(propName)
					|| UserConstants.LASTNAME.equals(propName)
					|| UserConstants.NICKNAME.equals(propName)) {
				col = new DefaultFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, "select", true, propName,
						new StaticFlexiCellRenderer("select", new TextFlexiCellRenderer()));
			} else {
				col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, true, propName);
			}
			columnsModel.addFlexiColumnModel(col);
			colPos++;
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserCols.threads));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserCols.replies));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserCols.lastModified));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserCols.numOfWords));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserCols.numOfCharacters));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("viewswitch.title", translate("viewswitch.title"), "select"));

		dataModel = new ForumUserDataModel(columnsModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "users", dataModel, 25, false, getTranslator(), formLayout);
		tableEl.setPageSize(25);
		tableEl.setAndLoadPersistedPreferences(ureq, "forum-users-v2");
	}
	
	private void loadModel() {
		List<ForumUserStatistics> statisticsList = forumManager.getForumUserStatistics(forum);
		List<ForumUser> users = new ArrayList<>(statisticsList.size());
		for(ForumUserStatistics statistics:statisticsList) {
			users.add(new ForumUser(statistics, userPropertyHandlers, getLocale()));
		}
		dataModel.setObjects(users);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(backLink == source) {
			fireEvent(ureq, Event.BACK_EVENT);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("select".equals(cmd)) {
					ForumUser user = dataModel.getObject(se.getIndex());
					doSelect(ureq, user.getIdentityKey(), user.getPseudonym(), user.isGuest());
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doSelect(UserRequest ureq, Long identityKey, String pseudonym, boolean guest) {
		fireEvent(ureq, new SelectUserEvent(identityKey, pseudonym, guest));
	}
}
