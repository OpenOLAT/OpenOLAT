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
package org.olat.group.ui.main;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.GroupLoggingAction;
import org.olat.group.model.BGRepositoryEntryRelation;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.manager.ACFrontendManager;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.olat.resource.accesscontrol.model.PriceMethodBundle;
import org.olat.util.logging.activity.LoggingResourceable;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
abstract class AbstractBusinessGroupListController extends BasicController {
	protected static final String TABLE_ACTION_LEAVE = "bgTblLeave";
	protected static final String TABLE_ACTION_LAUNCH = "bgTblLaunch";
	protected static final String TABLE_ACTION_ACCESS = "bgTblAccess";
	
	protected final VelocityContainer mainVC;

	protected final TableController groupListCtr;
	protected BusinessGroupTableModelWithType groupListModel;
	
	private DialogBoxController deleteDialogBox;
	private DialogBoxController sendEMailOnDeleteDialogBox;
	private DialogBoxController leaveDialogBox;
	
	private final boolean admin;
	protected final MarkManager markManager;
	protected final BaseSecurity securityManager;
	protected final ACFrontendManager acFrontendManager;
	protected final BusinessGroupService businessGroupService;
	
	public AbstractBusinessGroupListController(UserRequest ureq, WindowControl wControl, String page) {
		super(ureq, wControl);
		
		admin = ureq.getUserSession().getRoles().isOLATAdmin() || ureq.getUserSession().getRoles().isGroupManager();
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		acFrontendManager = CoreSpringFactory.getImpl(ACFrontendManager.class);
		securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		markManager = CoreSpringFactory.getImpl(MarkManager.class);
		
		mainVC = createVelocityContainer(page);
		

		//table
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setPreferencesOffered(true, "group.list");
		tableConfig.setTableEmptyMessage(translate("open.nogroup"));			
		groupListCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator(), false);
		listenTo(groupListCtr);

		int numOfColumns = initColumns();
		
		groupListModel = new BusinessGroupTableModelWithType(new ArrayList<BGTableItem>(), getTranslator(), numOfColumns);
		groupListCtr.setTableDataModel(groupListModel);

		mainVC.put("groupList", groupListCtr.getInitialComponent());


		putInitialPanel(mainVC);
	}
	
	protected abstract int initColumns();
	
	protected boolean isAdmin() {
		return admin;
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link && source.getComponentName().startsWith("repo_entry_")) {
			Object uobj = ((Link)source).getUserObject();
			if (uobj instanceof Long) {
				Long repoEntryKey = (Long)((Link)source).getUserObject();
				BusinessControl bc = BusinessControlFactory.getInstance().createFromString("[RepositoryEntry:" + repoEntryKey + "]");
				WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
				NewControllerFactory.getInstance().launch(ureq, bwControl);
			} else if(uobj instanceof BusinessGroup) {
				BusinessGroup bg = (BusinessGroup)uobj;
				BusinessControl bc = BusinessControlFactory.getInstance().createFromString("[BusinessGroup:" + bg.getKey() + "][toolresources:0]");
				WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
				NewControllerFactory.getInstance().launch(ureq, bwControl);
			}
		} else if (source instanceof Link && source.getComponentName().startsWith("marked_")) {
			Object uobj = ((Link)source).getUserObject();
			if(uobj instanceof BGTableItem) {
				toogleMark((BGTableItem)uobj);
			}
		}
	}
	
	private void toogleMark(BGTableItem item) {
		OLATResource bgResource = item.getBusinessGroup().getResource();
		if(markManager.isMarked(bgResource, getIdentity(), null)) {
			markManager.removeMark(bgResource, getIdentity(), null);
			item.setMarked(false);
		} else {
			String businessPath = "[BusinessGroup:" + item.getBusinessGroup().getKey() + "]";
			markManager.setMark(bgResource, getIdentity(), null, businessPath);
			item.setMarked(true);
		}
		groupListCtr.modelChanged();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == groupListCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();

				BusinessGroup businessGroup = groupListModel.getObject(te.getRowId()).getBusinessGroup();
				businessGroup = businessGroupService.loadBusinessGroup(businessGroup);
				//prevent rs after a group is deleted by someone else
				if(businessGroup == null) {
					groupListModel.removeBusinessGroup(businessGroup);
					groupListCtr.modelChanged();
				} else if(actionid.equals(TABLE_ACTION_LAUNCH)) {
					String businessPath = "[BusinessGroup:" + businessGroup.getKey() + "]";
					NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
				} else if(actionid.equals(TABLE_ACTION_LEAVE)) {
					leaveDialogBox = activateYesNoDialog(ureq, null, translate("dialog.modal.bg.leave.text", businessGroup.getName()), leaveDialogBox);
					leaveDialogBox.setUserObject(businessGroup);
				} else if (actionid.equals(TABLE_ACTION_ACCESS)) {
					doLaunch(ureq, businessGroup);
				}
			}
		} else if (source == deleteDialogBox) {
			if (DialogBoxUIFactory.isOkEvent(event)) {
				sendEMailOnDeleteDialogBox = activateYesNoDialog(ureq, null, translate("dialog.modal.bg.mail.text"), sendEMailOnDeleteDialogBox);
				sendEMailOnDeleteDialogBox.setUserObject(deleteDialogBox.getUserObject());
			}//else cancel was clicked or box closed
		} else if(source == sendEMailOnDeleteDialogBox){
			boolean withEmail = DialogBoxUIFactory.isOkEvent(event);
			doGroupDelete(ureq, withEmail, (BusinessGroup)sendEMailOnDeleteDialogBox.getUserObject());
		} else if (source == leaveDialogBox) {
			if (event != Event.CANCELLED_EVENT && DialogBoxUIFactory.isYesEvent(event)) {
				doGroupLeave(ureq, (BusinessGroup)leaveDialogBox.getUserObject());
			}
		}
		super.event(ureq, source, event);
	}
	
	protected void doLaunch(UserRequest ureq, BusinessGroup group) {
		String businessPath = "[BusinessGroup:" + group.getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	/**
	 * Removes user from the group as owner and participant. If
	 * no other owner are found the user won't be removed from the owner group
	 * 
	 * @param ureq
	 */
	private void doGroupLeave(UserRequest ureq, BusinessGroup group) {
		// 1) remove as owner
		if (securityManager.isIdentityInSecurityGroup(getIdentity(), group.getOwnerGroup())) {
			List<Identity> ownerList = securityManager.getIdentitiesOfSecurityGroup(group.getOwnerGroup());
			if (ownerList.size() > 1) {
				businessGroupService.removeOwners(ureq.getIdentity(), Collections.singletonList(getIdentity()), group);
			} else {
				// he is the last owner, but there must be at least one oner
				// give him a warning, as long as he tries to leave, he gets
				// this warning.
				getWindowControl().setError(translate("msg.atleastone"));
				return;
			}
		}
		// if identity was also owner it must have successfully removed to end here.
		// now remove the identity also as participant.
		// 2) remove as participant
		List<Identity> identities = Collections.singletonList(getIdentity());
		businessGroupService.removeParticipants(ureq.getIdentity(), identities, group);
	}
	
	/**
	 * Deletes the group. Checks if user is in owner group,
	 * otherwhise does nothing
	 * 
	 * @param ureq
	 * @param doSendMail specifies if notification mails should be sent to users of delted group
	 */
	private void doGroupDelete(UserRequest ureq, boolean doSendMail, BusinessGroup group) {
		// 1) send notification mails to users
		boolean ow = securityManager.isIdentityInSecurityGroup(getIdentity(), group.getOwnerGroup());
		// check if user is in owner group (could fake link in table)
		if (!admin && !ow) {
			logWarn("User tried to delete a group but he was not owner of the group", null);
			return;
		}

		if (doSendMail) {
			String businessPath = getWindowControl().getBusinessControl().getAsString();
			businessGroupService.deleteBusinessGroupWithMail(group, businessPath, getIdentity(), getLocale());
		} else {
			businessGroupService.deleteBusinessGroup(group);
		}
		// do Logging
		ThreadLocalUserActivityLogger.log(GroupLoggingAction.GROUP_DELETED, getClass(), LoggingResourceable.wrap(group));
		// 4) update Tables
		showInfo("info.group.deleted");
	}
	
	protected void updateTableModel(List<BusinessGroup> groups, boolean alreadyMarked) {
		
		List<Long> groupsWithMembership = businessGroupService.isIdentityInBusinessGroups(getIdentity(), groups);
		Set<Long> memberships = new HashSet<Long>(groupsWithMembership);

		List<Long> resourceKeys = new ArrayList<Long>(groups.size());
		for(BusinessGroup group:groups) {
			resourceKeys.add(group.getResource().getKey());
		}
		List<BGRepositoryEntryRelation> resources = businessGroupService.findRelationToRepositoryEntries(groups, 0, -1);
		List<OLATResourceAccess> resourcesWithAC = acFrontendManager.getAccessMethodForResources(resourceKeys, true, new Date());
		
		Set<Long> markedResources = new HashSet<Long>(groups.size() * 2 + 1);
		for(BusinessGroup group:groups) {
			markedResources.add(group.getResource().getResourceableId());
		}
		if(!alreadyMarked) {
			markManager.filterMarks(getIdentity(), "BusinessGroup", markedResources);
		}
		
		List<BGTableItem> items = new ArrayList<BGTableItem>();
		for(BusinessGroup group:groups) {
			Long oresKey = group.getResource().getKey();
			List<PriceMethodBundle> accessMethods = null;
			for(OLATResourceAccess access:resourcesWithAC) {
				if(oresKey.equals(access.getResource().getKey())){
					accessMethods = access.getMethods();
					break;
				}
			}
			
			Boolean allowLeave =  memberships.contains(group.getKey()) ? Boolean.TRUE : null;
			Boolean allowDelete = admin ? Boolean.TRUE : null;
			boolean member = memberships.contains(group.getKey());
			boolean marked = markedResources.contains(group.getResource().getResourceableId());
			BGTableItem tableItem = new BGTableItem(group, marked, member, allowLeave, allowDelete, accessMethods);
			tableItem.setUnfilteredRelations(resources);
			items.add(tableItem);
		}
		
		groupListModel.setEntries(items);
		groupListCtr.modelChanged();
	}
}
