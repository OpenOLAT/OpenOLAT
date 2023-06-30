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
package org.olat.modules.cemedia.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.admin.user.SelectOrganisationController;
import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.dropdown.DropdownItem;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.group.BusinessGroup;
import org.olat.group.model.BusinessGroupSelectionEvent;
import org.olat.group.ui.main.SelectBusinessGroupController;
import org.olat.modules.cemedia.Media;
import org.olat.modules.cemedia.MediaService;
import org.olat.modules.cemedia.MediaToGroupRelation;
import org.olat.modules.cemedia.MediaToGroupRelation.MediaToGroupRelationType;
import org.olat.modules.cemedia.model.MediaShare;
import org.olat.modules.cemedia.ui.MediaRelationsTableModel.MediaRelationsCols;
import org.olat.modules.cemedia.ui.component.MediaRelationsCellRenderer;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaRelationsController extends FormBasicController {
	
	private FormLink addUserLink;
	private FormLink addOrganisationLink;
	private FormLink addBusinessGroupLink;
	private FormLink openCloseLink;
	private DropdownItem addSharesDropdown;
	private FlexiTableElement tableEl;
	private MediaRelationsTableModel model;
	
	private CloseableModalController cmc;
	private UserSearchController userSearchController;
	private SelectBusinessGroupController selectGroupCtrl;
	private ConfirmDeleteRelationController deleteRelationCtrl;
	private SelectOrganisationController selectOrganisationCtrl;
	
	private Media media;
	private int counter = 0;
	private final boolean delaySave;
	private final boolean wrapped;
	private final boolean editable;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	private MediaService mediaService;
	@Autowired
	private OrganisationService organisationService;
	
	public MediaRelationsController(UserRequest ureq, WindowControl wControl, Media media, boolean editable) {
		super(ureq, wControl, "media_relations");
		this.media = media;
		this.editable = editable;
		delaySave = false;
		wrapped = false;
		initForm(ureq);
		loadModel();
	}
	
	public MediaRelationsController(UserRequest ureq, WindowControl wControl, Form form, Media media, boolean delay, boolean wrapped) {
		super(ureq, wControl, LAYOUT_CUSTOM, "media_relations", form);
		this.media = media;
		this.delaySave = delay;
		this.wrapped = wrapped;
		this.editable = true;
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MediaRelationsCols.name,
				new MediaRelationsCellRenderer(userManager)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MediaRelationsCols.type));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(MediaRelationsCols.editable));
		if(editable) {
			DefaultFlexiColumnModel deleteCol = new DefaultFlexiColumnModel("delete", "", "delete", "o_icon o_icon_delete_item");
			deleteCol.setIconHeader("o_icon o_icon_delete_item");
			deleteCol.setAlwaysVisible(true);
			deleteCol.setExportable(false);
			columnsModel.addFlexiColumnModel(deleteCol);
		}
		
		model = new MediaRelationsTableModel(columnsModel, getTranslator(), getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 25, false, getTranslator(), formLayout);
		tableEl.setSearchEnabled(!delaySave);
		tableEl.setCustomizeColumns(false);
		tableEl.setEmptyTableSettings("table.empty.shares", null, "o_icon_levels");
		
		addSharesDropdown = uifactory.addDropdownMenu("add.shares", "add.shares", formLayout, getTranslator());
		addSharesDropdown.setOrientation(DropdownOrientation.right);
		addSharesDropdown.setElementCssClass("o_sel_add_shares");
		addSharesDropdown.setIconCSS("o_icon o_icon_add");
		addSharesDropdown.setEmbbeded(true);
		addSharesDropdown.setButton(true);
		addSharesDropdown.setVisible(editable);
		
		addUserLink = uifactory.addFormLink("add.share.user", formLayout, Link.LINK);
		addUserLink.setIconLeftCSS("o_icon o_icon-fw o_icon_user");
		addUserLink.setVisible(editable);
		addSharesDropdown.addElement(addUserLink);
		
		addBusinessGroupLink = uifactory.addFormLink("add.share.business.group", formLayout, Link.LINK);
		addBusinessGroupLink.setIconLeftCSS("o_icon o_icon-fw o_icon_group");
		addBusinessGroupLink.setVisible(editable);
		addSharesDropdown.addElement(addBusinessGroupLink);
		
		addOrganisationLink = uifactory.addFormLink("add.share.organisation", formLayout, Link.LINK);
		addOrganisationLink.setIconLeftCSS("o_icon o_icon-fw o_icon_group");
		addOrganisationLink.setVisible(editable);
		addSharesDropdown.addElement(addOrganisationLink);
		
		if(wrapped) {
			openCloseLink = uifactory.addFormLink("open.close", formLayout, Link.LINK);
			openCloseLink.setIconLeftCSS("o_icon o_icon-fw o_icon_caret");
			openCloseLink.setUserObject(Boolean.TRUE);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == selectGroupCtrl) {
			cmc.deactivate();
			if(event instanceof BusinessGroupSelectionEvent bge) {
				List<BusinessGroup> groups = bge.getGroups();
				doShareToBusinessGroups(ureq, groups);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cleanUp();
		} else if(source == selectOrganisationCtrl) {
			if (event == Event.DONE_EVENT) {
				doShareWithOrganisation(ureq, selectOrganisationCtrl.getSelectedOrganisation());
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == userSearchController) {
			if (event instanceof SingleIdentityChosenEvent singleIdentityChosenEvent) {
				doShareWithUser(ureq, singleIdentityChosenEvent.getChosenIdentity());
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == deleteRelationCtrl) {
			if (event == Event.DONE_EVENT) {
				loadModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(source == cmc) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(userSearchController);
		removeAsListenerAndDispose(deleteRelationCtrl);
		removeAsListenerAndDispose(selectGroupCtrl);
		removeAsListenerAndDispose(cmc);
		userSearchController = null;
		deleteRelationCtrl = null;
		selectGroupCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addUserLink == source) {
			doAddUser(ureq);
		} else if(addBusinessGroupLink == source) {
			doSelectGroup(ureq);
		} else if(addOrganisationLink == source) {
			doSelectOrganisation(ureq);
		} else if(openCloseLink == source) {
			doToggleOpenClose();
		} else if(source instanceof FormToggle toggle && toggle.getUserObject() instanceof MediaShareRow row) {
			doToggle(row);
		} else if(source == tableEl) {
			if(event instanceof SelectionEvent se && "delete".equals(se.getCommand())) {
				MediaShareRow row = model.getObject(se.getIndex());
				doConfirmRemove(ureq, row.getShare());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void loadModel() {
		if(media == null || media.getKey() == null) return;
		
		List<MediaShare> shares = mediaService.getMediaShares(media);
		List<MediaShareRow> rows = new ArrayList<>(shares.size());
		for(MediaShare share:shares) {
			MediaShareRow row = new MediaShareRow(share);
			forgeRow(row);
			rows.add(row);
		}
		
		model.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private void forgeRow(MediaShareRow row) {
		FormToggle editableButton = uifactory.addToggleButton("editable_" + (++counter), null, translate("on"), translate("off"), flc);
		row.setEditableToggleButton(editableButton);
		editableButton.setEnabled(editable);
		udpateToggle(row);
		editableButton.setUserObject(row);
	}
	
	private void doToggle(MediaShareRow row) {
		if(row.getShare().getRelation() == null) return; // Delay saved
		
		MediaToGroupRelation relation = row.getShare().getRelation();
		if(relation.getType() == MediaToGroupRelationType.USER) {
			// Method will remove the first permission and add the new one
			relation = mediaService.addRelation(media, !relation.isEditable(), row.getShare().getUser());
		} else {
			relation.setEditable(!relation.isEditable());
			relation = mediaService.updateMediaToGroupRelation(relation);
		}
		row.getShare().setRelation(relation);
		udpateToggle(row);
	}
	
	private void udpateToggle(MediaShareRow row) {
		if(row.getShare().getRelation() == null) return;
		
		if(row.getShare().getRelation().isEditable()) {
			row.getEditableToggleButton().toggleOn();
		} else {
			row.getEditableToggleButton().toggleOff();
		}
	}
	
	private void doSelectGroup(UserRequest ureq) {
		removeAsListenerAndDispose(selectGroupCtrl);
		selectGroupCtrl = new SelectBusinessGroupController(ureq, getWindowControl(), null, null, null);
		listenTo(selectGroupCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				selectGroupCtrl.getInitialComponent(), true, translate("select.group"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doShareToBusinessGroups(UserRequest ureq, List<BusinessGroup> groups) {
		if(groups == null || groups.isEmpty()) return; // Nothing to do
		
		if(delaySave) {
			for(BusinessGroup businessGroup:groups) {
				addToModel(new MediaShare(null, businessGroup));
			}
		} else {
			for(BusinessGroup businessGroup:groups) {
				mediaService.addRelation(media, false, businessGroup);
			}
			dbInstance.commit();
			loadModel();
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}
	
	private void doSelectOrganisation(UserRequest ureq) {
		List<Organisation> organisations = organisationService.getOrganisations();
		selectOrganisationCtrl = new SelectOrganisationController(ureq, getWindowControl(), organisations);
		listenTo(selectOrganisationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				selectOrganisationCtrl.getInitialComponent(), true, translate("select.organisation"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doShareWithOrganisation(UserRequest ureq, Organisation organisation) {
		if(delaySave) {
			addToModel(new MediaShare(null, organisation));
		} else {
			mediaService.addRelation(media, false, organisation);
			dbInstance.commit();
			loadModel();
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}
	
	private void doAddUser(UserRequest ureq) {
		userSearchController = new UserSearchController(ureq, getWindowControl(), true);
		listenTo(userSearchController);

		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				userSearchController.getInitialComponent(), true, translate("select.user"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doShareWithUser(UserRequest ureq, Identity identity) {
		if(delaySave) {
			addToModel(new MediaShare(null, identity));
		} else {
			mediaService.addRelation(media, false, identity);
			dbInstance.commit();
			loadModel();
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}
	
	private void addToModel(MediaShare share) {
		MediaShareRow row = new MediaShareRow(share);
		forgeRow(row);
		model.addObject(row);
		tableEl.reset(true, true, true);
	}
	
	private void doConfirmRemove(UserRequest ureq, MediaShare share) {
		if(share.getRelation() == null) {
			model.removeObject(share);
			tableEl.reset(true, true, true);
		} else {
			deleteRelationCtrl = new ConfirmDeleteRelationController(ureq, getWindowControl(), media, share);
			listenTo(deleteRelationCtrl);
	
			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					deleteRelationCtrl.getInitialComponent(), true, translate("delete.share"));
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private void doToggleOpenClose() {
		Boolean state = (Boolean)openCloseLink.getUserObject();
		boolean newState = !(state == null || state.booleanValue());
		setOpenClose(newState);
	}
	
	public void setOpenClose(boolean newState) {
		openCloseLink.setUserObject(Boolean.valueOf(newState));
		if(newState) {
			openCloseLink.setIconLeftCSS("o_icon o_icon-fw o_icon_caret");
		} else {
			openCloseLink.setIconLeftCSS("o_icon o_icon-fw o_icon_caret_right");
		}
		tableEl.setVisible(newState);
		addSharesDropdown.setVisible(newState);
	}
	
	public void saveRelations(Media mediaReference) {
		this.media = mediaReference;
		
		List<MediaShareRow> rows = model.getObjects();
		for(MediaShareRow row:rows) {
			MediaShare share = row.getShare();
			if(share.getType() == MediaToGroupRelationType.USER) {
				mediaService.addRelation(mediaReference, row.getEditableToggleButton().isOn(), share.getUser());
			} else if(share.getType() == MediaToGroupRelationType.BUSINESS_GROUP) {
				mediaService.addRelation(mediaReference, row.getEditableToggleButton().isOn(), share.getBusinessGroup());
			} else if(share.getType() == MediaToGroupRelationType.ORGANISATION) {
				mediaService.addRelation(mediaReference, row.getEditableToggleButton().isOn(), share.getOrganisation());
			}
		}
	}
}
