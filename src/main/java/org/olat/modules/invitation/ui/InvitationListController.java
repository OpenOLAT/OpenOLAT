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
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.Invitation;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.group.BusinessGroup;
import org.olat.modules.invitation.model.SearchInvitationParameters;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ui.ProjectUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * 
 * Initial date: 8 juil. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InvitationListController extends AbstractInvitationListController implements Activateable2 {

	private final RepositoryEntry entry;
	private final BusinessGroup businessGroup;
	private final ProjProject project;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private BaseSecurityModule securityModule;
	
	public InvitationListController(UserRequest ureq, WindowControl wControl, RepositoryEntry repositoryEntry, boolean readOnly) {
		super(ureq, wControl, "invitations", readOnly);
		this.entry = repositoryEntry;
		this.businessGroup = null;
		this.project = null;
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		
		initForm(ureq);
		loadModel();
	}
	
	public InvitationListController(UserRequest ureq, WindowControl wControl, BusinessGroup businessGroup, boolean readOnly) {
		super(ureq, wControl, "invitations", readOnly);
		this.entry = null;
		this.businessGroup = businessGroup;
		this.project = null;
		
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		
		initForm(ureq);
		loadModel();
	}
	
	public InvitationListController(UserRequest ureq, WindowControl wControl, ProjProject project, boolean readOnly) {
		super(ureq, wControl, "invitations", readOnly);
		setTranslator(Util.createPackageTranslator(ProjectUIFactory.class, ureq.getLocale(), getTranslator()));
		this.entry = null;
		this.businessGroup = null;
		this.project = project;
		
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			if(entry != null) {
				layoutCont.contextPut("title", translate("invitation.entry.list.title", entry.getDisplayname(), entry.getExternalRef()));
			} else if(businessGroup != null) {
				String name = businessGroup.getName() == null ? "" : businessGroup.getName();
				layoutCont.contextPut("title", translate("invitation.business.group.list.title", name));
			} else if(project != null) {
				String name = project.getTitle() == null ? "" : project.getTitle();
				layoutCont.contextPut("title", translate("invitation.project.list.title", name));
			}
		}
		
		super.initForm(formLayout, listener, ureq);
	}

	@Override
	protected String getTableId() {
		return "invitations-list-v2";
	}

	@Override
	protected InvitationListTableModel initTableModel(FlexiTableColumnModel columnsModel) {
		return new InvitationListTableModel(columnsModel, userPropertyHandlers, getLocale());
	}

	@Override
	protected SortKey initColumns(FlexiTableColumnModel columnsModel) {
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
		return defaultSortKey;
	}
	
	@Override
	public void loadModel() {
		List<InvitationRow> rows;
		if(entry != null) {
			List<Invitation> invitations = invitationService.findInvitations(entry, getSearchParameters());
			rows = invitations.stream()
					.map(invitation -> forgeRow(invitation, entry, null, null))
					.collect(Collectors.toList());
		} else if(businessGroup != null) {
			List<Invitation> invitations = invitationService.findInvitations(businessGroup, getSearchParameters());
			rows = invitations.stream()
					.map(invitation -> forgeRow(invitation, null, businessGroup, null))
					.collect(Collectors.toList());
		} else if(project != null) {
			List<Invitation> invitations = invitationService.findInvitations(project, getSearchParameters());
			rows = invitations.stream()
					.map(invitation -> forgeRow(invitation, null, null,  project))
					.collect(Collectors.toList());
		} else {
			rows = new ArrayList<>();
		}

		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected SearchInvitationParameters getSearchParameters() {
		SearchInvitationParameters params = super.getSearchParameters();
		params.setUserPropertyHandlers(userPropertyHandlers);
		return params;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}
}
