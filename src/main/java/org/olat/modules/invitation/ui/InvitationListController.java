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
package org.olat.modules.invitation.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.Invitation;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.course.member.MemberListController;
import org.olat.group.BusinessGroup;
import org.olat.modules.invitation.InvitationService;
import org.olat.modules.invitation.ui.InvitationListTableModel.InvitationCols;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 8 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InvitationListController extends FormBasicController {

	protected static final String USER_PROPS_ID = MemberListController.class.getCanonicalName();
	public static final int USER_PROPS_OFFSET = 500;
	
	private FlexiTableElement tableEl;
	private InvitationListTableModel tableModel;
	
	private int counter = 0;
	private final RepositoryEntry entry;
	private final BusinessGroup businessGroup;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	private InvitationURLController invitationUrlCtrl;
	private CloseableCalloutWindowController urlCalloutCtrl; 
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private InvitationService invitationService;
	
	public InvitationListController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry) {
		super(ureq, wControl, "invitations");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.entry = repositoryEntry;
		this.businessGroup = null;
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		
		initForm(ureq);
		loadModel();
	}
	
	public InvitationListController(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup) {
		super(ureq, wControl, "invitations");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.entry = null;
		this.businessGroup = businessGroup;
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, org.olat.core.gui.UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			if(entry != null) {
				layoutCont.contextPut("title", translate("invitation.entry.list.title", entry.getDisplayname(), entry.getExternalRef()));
			} else if(businessGroup != null) {
				String name = businessGroup.getName() == null ? "" : businessGroup.getName();
				layoutCont.contextPut("title", translate("invitation.business.group.list.title", name));
			}
		}
		
		SortKey defaultSortKey = null;
		int colPos = USER_PROPS_OFFSET;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;

			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);

			FlexiColumnModel col;
			if(UserConstants.FIRSTNAME.equals(propName) || UserConstants.LASTNAME.equals(propName)) {
				col = new DefaultFlexiColumnModel(userPropertyHandler.i18nColumnDescriptorLabelKey(),
						colPos, null, true, propName,
						new StaticFlexiCellRenderer("select", new TextFlexiCellRenderer()));
			} else {
				col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, true, propName);
			}
			columnsModel.addFlexiColumnModel(col);
			colPos++;
			
			if(defaultSortKey == null) {
				defaultSortKey = new SortKey(propName, true);
			}
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(InvitationCols.invitationDate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(InvitationCols.invitationLink));
		
		tableModel = new InvitationListTableModel(columnsModel, userPropertyHandlers, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setEmptyTableSettings("noinvitations", null, "o_icon_user", null, null, false);
		tableEl.setAndLoadPersistedPreferences(ureq, this.getClass().getSimpleName());
		tableEl.setExportEnabled(true);
		tableEl.setElementCssClass("o_sel_invitations_list");
		
		if(defaultSortKey != null) {
			FlexiTableSortOptions options = new FlexiTableSortOptions();
			options.setDefaultOrderBy(defaultSortKey);
			tableEl.setSortSettings(options);
		}
	}
	
	public void reloadModel() {
		loadModel();
	}
	
	private void loadModel() {
		List<Invitation> invitations;
		if(entry != null) {
			invitations = invitationService.findInvitations(entry);
		} else if(businessGroup != null) {
			invitations = invitationService.findInvitations(businessGroup);
		} else {
			invitations = List.of();
		}
		List<InvitationRow> rows = new ArrayList<>(invitations.size());
		for(Invitation invitation:invitations) {
			rows.add(forgeRow(invitation));
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private InvitationRow forgeRow(Invitation invitation) {
		FormLink urlLink = uifactory.addFormLink("url_" + (++counter), "url", "", null, flc, Link.LINK | Link.NONTRANSLATED);
		urlLink.setIconLeftCSS("o_icon o_icon_link o_icon-fw");
		
		InvitationRow row = new InvitationRow(invitation, urlLink);
		urlLink.setUserObject(row);
		return row;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(invitationUrlCtrl == source) {
			if(urlCalloutCtrl != null) {
				urlCalloutCtrl.deactivate();
			}
			cleanUp();
		} else if(urlCalloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(invitationUrlCtrl);
		removeAsListenerAndDispose(urlCalloutCtrl);
		invitationUrlCtrl = null;
		urlCalloutCtrl = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("url".equals(link.getCmd()) && link.getUserObject() instanceof InvitationRow) {
				doOpenUrl(ureq, link.getFormDispatchId(), (InvitationRow)link.getUserObject());
			}
		}

		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(org.olat.core.gui.UserRequest ureq) {
		//
	}
	
	private void doOpenUrl(UserRequest ureq, String elementId, InvitationRow row) {
		String url = this.invitationService.toUrl(row.getInvitation());
		invitationUrlCtrl = new InvitationURLController(ureq, getWindowControl(), url);
		listenTo(invitationUrlCtrl);

		String title = translate("invitation.url.title");
		urlCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				invitationUrlCtrl.getInitialComponent(), elementId, title, true, "");
		listenTo(urlCalloutCtrl);
		urlCalloutCtrl.activate();
	}
}
