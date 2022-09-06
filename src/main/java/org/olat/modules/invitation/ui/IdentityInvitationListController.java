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

import org.olat.NewControllerFactory;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.modules.invitation.InvitationTypeEnum;
import org.olat.modules.invitation.model.InvitationWithBusinessGroup;
import org.olat.modules.invitation.model.InvitationWithRepositoryEntry;
import org.olat.modules.invitation.model.SearchInvitationParameters;
import org.olat.modules.invitation.ui.InvitationListTableModel.InvitationCols;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.ui.author.TypeRenderer;

/**
 * 
 * Initial date: 5 sept. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityInvitationListController extends AbstractInvitationListController {
	
	private final Identity invitee;
	private final InvitationTypeEnum invitationType;
	
	public IdentityInvitationListController(UserRequest ureq, WindowControl wControl,
			Identity invitee, InvitationTypeEnum invitationType, boolean readOnly) {
		super(ureq, wControl, "identity_invitations", readOnly);
		setTranslator(Util.createPackageTranslator(TypeRenderer.class, getLocale(), getTranslator()));
		
		this.invitee = invitee;
		this.invitationType = invitationType;
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("title", translate("invitation.identity.list.title"));
		}
		super.initForm(formLayout, listener, ureq);
	}
	
	@Override
	protected SortKey initColumns(FlexiTableColumnModel columnsModel) {
		if(invitationType == InvitationTypeEnum.repositoryEntry) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, InvitationCols.repositoryEntryKey));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(InvitationCols.repositoryEntryType, new TypeRenderer()));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(InvitationCols.repositoryEntryDisplayname, "selectEntry"));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, InvitationCols.repositoryEntryExternalRef));
		} else if(invitationType == InvitationTypeEnum.businessGroup) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, InvitationCols.businessGroupKey));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(InvitationCols.businessGroupName, "selectBusinessGroup"));
		}
		return null;
	}

	@Override
	protected String getTableId() {
		return "invitations-list-".concat(invitationType.name());
	}

	@Override
	protected InvitationListTableModel initTableModel(FlexiTableColumnModel columnsModel) {
		return new InvitationListTableModel(columnsModel, getLocale());
	}

	@Override
	protected void loadModel() {
		List<InvitationRow> rows;
		SearchInvitationParameters params = getSearchParameters();
		if(invitationType == InvitationTypeEnum.repositoryEntry) {
			List<InvitationWithRepositoryEntry> invitationWithEntries = invitationService.findInvitationsWithEntries(params, false);
			rows = invitationWithEntries.stream()
					.filter(invitation -> invitation.getEntry() != null && invitation.getEntry().getEntryStatus() != RepositoryEntryStatusEnum.trash)
					.map(invitation -> forgeRow(invitation.getInvitation(), invitation.getEntry(), null))
					.collect(Collectors.toList());
		} else if(invitationType == InvitationTypeEnum.businessGroup) {
			List<InvitationWithBusinessGroup> invitationWithGroups = invitationService.findInvitationsWithBusinessGroups(params);
			rows = invitationWithGroups.stream()
					.map(invitation -> forgeRow(invitation.getInvitation(), null, invitation.getBusinessGroup()))
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
		params.setIdentityKey(invitee.getKey());
		params.setUserPropertyHandlers(List.of());
		return params;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("selectEntry".equals(se.getCommand())) {
					InvitationRow row = tableModel.getObject(se.getIndex());
					launchRepositoryEntry( ureq, row.getRepositoryEntryKey());
				} else if("selectBusinessGroup".equals(se.getCommand())) {
					InvitationRow row = tableModel.getObject(se.getIndex());
					launchBusinessGroup( ureq, row.getBusinessGroupKey());
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void launchRepositoryEntry(UserRequest ureq, Long repoKey) {
		NewControllerFactory.getInstance().launch("[RepositoryEntry:" + repoKey + "]", ureq, getWindowControl());
	}
	
	private void launchBusinessGroup(UserRequest ureq, Long businessGroupKey) {
		NewControllerFactory.getInstance().launch("[BusinessGroup:" + businessGroupKey + "]", ureq, getWindowControl());
	}
}
