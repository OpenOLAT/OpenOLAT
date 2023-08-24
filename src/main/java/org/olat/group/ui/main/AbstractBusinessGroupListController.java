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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.GroupRoles;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableSortOptions;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableEmptyNextPrimaryActionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.mail.ContactList;
import org.olat.core.util.mail.ContactMessage;
import org.olat.core.util.mail.MailHelper;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.Quota;
import org.olat.core.util.vfs.QuotaManager;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupLifecycleManager;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.BusinessGroupMembership;
import org.olat.group.BusinessGroupModule;
import org.olat.group.BusinessGroupRef;
import org.olat.group.BusinessGroupService;
import org.olat.group.BusinessGroupShort;
import org.olat.group.BusinessGroupStatusEnum;
import org.olat.group.area.BGAreaManager;
import org.olat.group.manager.BusinessGroupMailing;
import org.olat.group.manager.BusinessGroupMailing.MailType;
import org.olat.group.model.BusinessGroupQueryParams;
import org.olat.group.model.BusinessGroupQueryParams.LifecycleSyntheticStatus;
import org.olat.group.model.BusinessGroupRow;
import org.olat.group.model.BusinessGroupSelectionEvent;
import org.olat.group.model.LeaveOption;
import org.olat.group.model.MembershipModification;
import org.olat.group.right.BGRightManager;
import org.olat.group.ui.NewBGController;
import org.olat.group.ui.lifecycle.ConfirmBusinessGroupChangeStatusController;
import org.olat.group.ui.lifecycle.ConfirmBusinessGroupDefinitivelyDeleteController;
import org.olat.group.ui.lifecycle.ConfirmBusinessGroupStartChangeStatusController;
import org.olat.group.ui.lifecycle.ConfirmRestoreController;
import org.olat.group.ui.main.BusinessGroupListFlexiTableModel.Cols;
import org.olat.group.ui.wizard.BGConfigBusinessGroup;
import org.olat.group.ui.wizard.BGConfigToolsStep;
import org.olat.group.ui.wizard.BGCopyBusinessGroup;
import org.olat.group.ui.wizard.BGCopyPreparationStep;
import org.olat.group.ui.wizard.BGEmailSelectReceiversStep;
import org.olat.group.ui.wizard.BGMailNotificationEditController;
import org.olat.group.ui.wizard.BGMergeStep;
import org.olat.group.ui.wizard.BGUserMailTemplate;
import org.olat.group.ui.wizard.BGUserManagementController;
import org.olat.modules.co.ContactFormController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryShort;
import org.olat.resource.accesscontrol.ACService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public abstract class AbstractBusinessGroupListController extends FormBasicController implements Activateable2 {
	protected static final String TABLE_ACTION_LEAVE = "bgTblLeave";
	protected static final String TABLE_ACTION_EDIT = "bgTblEdit";
	protected static final String TABLE_ACTION_LAUNCH = "bgTblLaunch";
	protected static final String TABLE_ACTION_OPEN = "bgTblOpen";
	protected static final String TABLE_ACTION_ACCESS = "bgTblAccess";
	protected static final String TABLE_ACTION_DUPLICATE = "bgTblDuplicate";
	protected static final String TABLE_ACTION_MERGE = "bgTblMerge";
	protected static final String TABLE_ACTION_USERS = "bgTblUser";
	protected static final String TABLE_ACTION_CONFIG = "bgTblConfig";
	protected static final String TABLE_ACTION_EMAIL = "bgTblEmail";
	protected static final String TABLE_ACTION_SOFT_DELETE = "bgTblSoftDelete";
	protected static final String TABLE_ACTION_START_SOFT_DELETE = "bgTblStartSoftDelete";
	protected static final String TABLE_ACTION_DEFINITIVELY_DELETE = "bgTblDefinitivelyDelete";
	protected static final String TABLE_ACTION_REACTIVATE = "bgTblReactivate";
	protected static final String TABLE_ACTION_INACTIVATE = "bgTblInactivate";
	protected static final String TABLE_ACTION_START_INACTIVATE = "bgTblStartInactivate";
	protected static final String TABLE_ACTION_CANCEL_INACTIVATE = "bgTblCancelInactivate";
	protected static final String TABLE_ACTION_SELECT = "bgTblSelect";
	protected static final String TABLE_ACTION_TOOLS = "bgTblTools";
	
	protected static final BusinessGroupMembershipComparator MEMBERSHIP_COMPARATOR = new BusinessGroupMembershipComparator();

	protected FlexiTableElement tableEl;
	protected BusinessGroupListFlexiTableModel groupTableModel;
	protected BusinessGroupQueryParams lastSearchParams;
	
	private DialogBoxController leaveDialogBox;
	
	protected FormLink createButton;
	protected FormLink inactivateButton;
	protected FormLink startInactivateButton;
	protected FormLink cancelInactivateButton;
	protected FormLink softDeleteButton;
	protected FormLink startSoftDeleteButton;
	protected FormLink reactivateButton;
	protected FormLink definitivelyDeleteButton;
	protected FormLink restoreButton;
	protected FormLink duplicateButton;
	protected FormLink configButton;
	protected FormLink emailButton;
	protected FormLink usersButton;
	protected FormLink mergeButton;
	protected FormLink selectButton;


	private ContactFormController contactCtrl;
	private NewBGController groupCreateController;
	private BGUserManagementController userManagementController;
	private ConfirmRestoreController confirmRestoreController;
	private BGMailNotificationEditController userManagementSendMailController;
	private ConfirmBusinessGroupChangeStatusController confirmChangeStatusController;
	private ConfirmBusinessGroupStartChangeStatusController confirmStartChangeStatusController;
	private ConfirmBusinessGroupDefinitivelyDeleteController confirmDefinitivelyDeleteController;
	private StepsMainRunController emailWizard;
	private StepsMainRunController businessGroupWizard;
	protected CloseableModalController cmc;

	protected final Roles roles;
	protected final boolean admin;
	protected final boolean readOnly;
	protected final boolean managedEnable;
	
	private BusinessGroupViewFilter filter;
	private Object userObject;
	private final String prefsKey;
	private final boolean showAlwaysSearch;
	
	@Autowired
	protected MarkManager markManager;
	@Autowired
	protected BaseSecurity securityManager;
	@Autowired
	protected BusinessGroupModule groupModule;
	@Autowired
	protected ACService acService;
	@Autowired
	protected QuotaManager quotaManager;
	@Autowired
	protected BGAreaManager areaManager;
	@Autowired
	protected BGRightManager rightManager;
	@Autowired
	protected BusinessGroupService businessGroupService;
	@Autowired
	protected BusinessGroupLifecycleManager businessGroupLifecycleManager;
	@Autowired
	protected CollaborationToolsFactory collaborationTools;
	
	public AbstractBusinessGroupListController(UserRequest ureq, WindowControl wControl, String page, String prefsKey, boolean showAlwaysSearch) {
		this(ureq, wControl, page, false, prefsKey, showAlwaysSearch, null);
	}
	
	public AbstractBusinessGroupListController(UserRequest ureq, WindowControl wControl, String page,
			boolean readOnly, String prefsKey, boolean showAlwaysSearch, Object userObject) {
		super(ureq, wControl, page);
		setTranslator(Util.createPackageTranslator(AbstractBusinessGroupListController.class, ureq.getLocale(), getTranslator()));

		roles = ureq.getUserSession().getRoles();
		admin = roles.isAdministrator() || roles.isGroupManager();
		this.readOnly = readOnly;
		this.managedEnable = groupModule.isManagedBusinessGroups();
		this.userObject = userObject;
		this.showAlwaysSearch = showAlwaysSearch;
		this.prefsKey = prefsKey;

		initForm(ureq);
	} 
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnModel = initColumnModel();
		
		groupTableModel = initTableModel(columnModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", groupTableModel, 20, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_group_list");
		
		FlexiTableSortOptions options = new FlexiTableSortOptions();
		options.setFromColumnModel(true);
		tableEl.setSortSettings(options);
		tableEl.setSearchEnabled(true);

		initButtons(formLayout, ureq);
		if (createButton == null) {
			tableEl.setEmptyTableSettings("table.empty", null, "o_icon_group", null, null, showAlwaysSearch);		
		} else {
			tableEl.setEmptyTableSettings("table.empty", "create.group.description", "o_icon_group", "create.group", "o_icon_add", showAlwaysSearch);					
		}
		
		initFilterTabs();
		initFilters();
		
		tableEl.setAndLoadPersistedPreferences(ureq, "gbg-list-" + prefsKey);
	}
	
	protected abstract FlexiTableColumnModel initColumnModel();

	protected final BusinessGroupListFlexiTableModel initTableModel(FlexiTableColumnModel columnModel) {
		return new BusinessGroupListFlexiTableModel(columnModel, getLocale());
	}
	
	protected void initFilterTabs() {
		//
	}

	protected void initFilters() {
		//
	}

	public Object getUserObject() {
		return userObject;
	}
	
	public BusinessGroupViewFilter getFilter() {
		return filter;
	}

	public void setFilter(BusinessGroupViewFilter filter) {
		this.filter = filter;
	}

	protected abstract void initButtons(FormItemContainer formLayout, UserRequest ureq);

	protected void initButtons(FormItemContainer formLayout, UserRequest ureq, boolean create, boolean select, boolean lifecycle, boolean adminTools) {
		if(create && groupModule.isAllowedCreate(ureq.getUserSession().getRoles())) {
			createButton = uifactory.addFormLink("create.group", formLayout, Link.BUTTON);
			createButton.setElementCssClass("o_sel_group_create");
			createButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
		}
		
		if(select) {
			tableEl.setMultiSelect(true);
			tableEl.setSelectAllEnable(true);
			selectButton = uifactory.addFormLink("select", TABLE_ACTION_SELECT, "select", null, formLayout, Link.BUTTON);
			tableEl.addBatchButton(selectButton);
		}

		if(adminTools) {
			tableEl.setMultiSelect(true);
			tableEl.setSelectAllEnable(true);
			
			boolean canCreateGroup = canCreateBusinessGroup();
			if(canCreateGroup) {
				duplicateButton = uifactory.addFormLink("table.duplicate", TABLE_ACTION_DUPLICATE, "table.duplicate", null, formLayout, Link.BUTTON);
				tableEl.addBatchButton(duplicateButton);
				mergeButton = uifactory.addFormLink("table.merge", TABLE_ACTION_MERGE, "table.merge", null, formLayout, Link.BUTTON);
				tableEl.addBatchButton(mergeButton);
			}
			usersButton = uifactory.addFormLink("table.users.management", TABLE_ACTION_USERS, "table.users.management", null, formLayout, Link.BUTTON);
			tableEl.addBatchButton(usersButton);
			configButton = uifactory.addFormLink("table.config", TABLE_ACTION_CONFIG, "table.config", null, formLayout, Link.BUTTON);
			tableEl.addBatchButton(configButton);
			emailButton = uifactory.addFormLink("table.email", TABLE_ACTION_EMAIL, "table.email", null, formLayout, Link.BUTTON);
			tableEl.addBatchButton(emailButton);
			
			if(canCreateGroup && lifecycle) {
				inactivateButton = uifactory.addFormLink("table.inactivate", TABLE_ACTION_INACTIVATE, "table.inactivate", null, formLayout, Link.BUTTON);
				tableEl.addBatchButton(inactivateButton);
				
				reactivateButton = uifactory.addFormLink("table.reactivate", TABLE_ACTION_REACTIVATE, "table.reactivate", null, formLayout, Link.BUTTON);
				reactivateButton.setVisible(false);
				tableEl.addBatchButton(reactivateButton);
				
				softDeleteButton = uifactory.addFormLink("table.delete", TABLE_ACTION_SOFT_DELETE, "table.delete", null, formLayout, Link.BUTTON);
				tableEl.addBatchButton(softDeleteButton);
			}
		}
	}
	
	protected boolean canCreateBusinessGroup() {
		return roles.isAdministrator() || roles.isGroupManager()
				|| (roles.isAuthor() && groupModule.isAuthorAllowedCreate())
				|| (!roles.isGuestOnly() && !roles.isInvitee() && groupModule.isUserAllowedCreate());
	}
	
	protected boolean isAdmin() {
		return admin;
	}
	
	protected boolean isEmpty() {
		return tableEl == null || groupTableModel == null || groupTableModel.getRowCount() == 0;
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	protected List<BusinessGroupRow> getSelectedItems() {
		Set<Integer> selections = tableEl.getMultiSelectedIndex();
		List<BusinessGroupRow> rows = new ArrayList<>(selections.size());
		for(Integer i:selections) {
			BusinessGroupRow row = groupTableModel.getObject(i.intValue());
			if(row != null) {
				rows.add(row);
			}
		}
		return rows;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(createButton == source) {
			doCreate(ureq, getWindowControl(), null);
		} else if(softDeleteButton == source) {
			confirmChangeStatus(ureq, getSelectedItems(), BusinessGroupStatusEnum.trash);
		} else if(startSoftDeleteButton == source) {
			doConfirmStartChangeStatus(ureq, getSelectedItems(), BusinessGroupStatusEnum.trash);
		} else if(inactivateButton == source) {
			confirmChangeStatus(ureq, getSelectedItems(), BusinessGroupStatusEnum.inactive);
		} else if(startInactivateButton == source) {
			doConfirmStartChangeStatus(ureq, getSelectedItems(), BusinessGroupStatusEnum.inactive);
		} else if(cancelInactivateButton == source || reactivateButton == source) {
			doCancelInactivation(getSelectedItems());
		} else if(restoreButton == source) {
			doConfirmRestore(ureq, getSelectedItems());
		} else if(definitivelyDeleteButton == source) {
			doConfirmDefinitivelyDelete(ureq, getSelectedItems());
		} else if(duplicateButton == source) {
			doCopy(ureq, getSelectedItems());
		} else if(configButton == source) {
			doConfiguration(ureq, getSelectedItems());
		} else if(emailButton == source) {
			doEmails(ureq, getSelectedItems());
		} else if(usersButton == source) {
			doUserManagement(ureq, getSelectedItems());
		} else if(mergeButton == source) {
			doMerge(ureq, getSelectedItems());
		} else if(selectButton == source) {
			doSelect(ureq, getSelectedItems());
		} else if(source instanceof FormLink link) {
			String cmd = link.getCmd();
			if("mark".equals(cmd)) {
				BusinessGroupRow row = (BusinessGroupRow)link.getUserObject();
				boolean marked = toogleMark(row);
				link.setIconLeftCSS(marked ? "o_icon o_icon_bookmark o_icon-lg" : "o_icon o_icon_bookmark_add o_icon-lg");
				link.getComponent().setDirty(true);
			} else if("allresources".equals(cmd)) {
				BusinessGroupShort bg = (BusinessGroupShort)link.getUserObject();
				NewControllerFactory.getInstance().launch("[BusinessGroup:" + bg.getKey() + "][toolresources:0]", ureq, getWindowControl());
			} else if("resource".equals(cmd)) {
				RepositoryEntryShort re = (RepositoryEntryShort)link.getUserObject();
				NewControllerFactory.getInstance().launch("[RepositoryEntry:" + re.getKey() + "]", ureq, getWindowControl());
			} else if(link.getUserObject() instanceof BusinessGroupRef) {
				BusinessGroupRef item = (BusinessGroupRef)link.getUserObject();
				Long businessGroupKey = item.getKey();
				BusinessGroup businessGroup = businessGroupService.loadBusinessGroup(businessGroupKey);
				if(businessGroup == null) {
					groupTableModel.removeBusinessGroup(businessGroupKey);
					tableEl.reset();
				} else if(TABLE_ACTION_ACCESS.equals(cmd)) {
					doAccess(ureq, businessGroup);
				} else if(TABLE_ACTION_LEAVE.equals(cmd)) {
					doConfirmLeaving(ureq, businessGroup);
				}
			}
		} else if(source == tableEl) {
			
			String cmd = event.getCommand();
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if(se.getIndex() >= 0 && se.getIndex() < groupTableModel.getRowCount()) {
					BGTableItem item = groupTableModel.getObject(se.getIndex());
					Long businessGroupKey = item.getKey();
					BusinessGroup businessGroup = businessGroupService.loadBusinessGroup(businessGroupKey);
					//prevent rs after a group is deleted by someone else
					if(businessGroup == null) {
						groupTableModel.removeBusinessGroup(businessGroupKey);
						tableEl.reset();
					} else if(TABLE_ACTION_LAUNCH.equals(cmd)) {
						doLaunch(ureq, businessGroup);
					} else if(TABLE_ACTION_START_SOFT_DELETE.equals(cmd)) {
						doConfirmStartChangeStatus(ureq, List.of(item), BusinessGroupStatusEnum.trash);
					} else if(TABLE_ACTION_SOFT_DELETE.equals(cmd)) {
						confirmChangeStatus(ureq, List.of(item), BusinessGroupStatusEnum.trash);
					} else if(TABLE_ACTION_START_INACTIVATE.equals(cmd)) {
						doConfirmStartChangeStatus(ureq, List.of(item), BusinessGroupStatusEnum.inactive);
					} else if(TABLE_ACTION_INACTIVATE.equals(cmd)) {
						confirmChangeStatus(ureq, List.of(item), BusinessGroupStatusEnum.inactive);
					} else if(TABLE_ACTION_CANCEL_INACTIVATE.equals(cmd)) {
						confirmChangeStatus(ureq, List.of(item), BusinessGroupStatusEnum.active);
					} else if(TABLE_ACTION_DEFINITIVELY_DELETE.equals(cmd)) {
						doConfirmDefinitivelyDelete(ureq, List.of(item));
					} else if(TABLE_ACTION_EDIT.equals(cmd)) {
						doEdit(ureq, businessGroup);
					} else if(TABLE_ACTION_LEAVE.equals(cmd)) {
						doConfirmLeaving(ureq, businessGroup);
					} else if(TABLE_ACTION_ACCESS.equals(cmd)) {
						doAccess(ureq, businessGroup);
					} else if(TABLE_ACTION_SELECT.equals(cmd)) {
						doSelect(ureq, businessGroup);
					} else if(TABLE_ACTION_TOOLS.equals(cmd)) {
						doOpenTools(ureq, item, businessGroup);
					}
				}
			} else if(event instanceof FlexiTableFilterTabEvent) {
				doSearch(ureq, ((FlexiTableFilterTabEvent)event).getTab());
			} else if(event instanceof FlexiTableSearchEvent) {
				doSearch(ureq, (FlexiTableSearchEvent)event);
			} else if (event instanceof FlexiTableEmptyNextPrimaryActionEvent) {
				doCreate(ureq, getWindowControl(), null);
			}
		} 
		super.formInnerEvent(ureq, source, event);
	}
	
	/**
	 * Add/remove as favorite
	 * @param item
	 */
	private boolean toogleMark(BusinessGroupRow item) {
		OLATResourceable bgResource = OresHelper.createOLATResourceableInstance("BusinessGroup", item.getKey());
		if(markManager.isMarked(bgResource, getIdentity(), null)) {
			markManager.removeMark(bgResource, getIdentity(), null);
			item.setMarked(false);
		} else {
			String businessPath = "[BusinessGroup:" + item.getKey() + "]";
			markManager.setMark(bgResource, getIdentity(), null, businessPath);
			item.setMarked(true);
		}
		return item.isMarked();
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == confirmDefinitivelyDeleteController || source == confirmChangeStatusController
				|| source == confirmStartChangeStatusController || source == confirmRestoreController) {
			if(event == Event.DONE_EVENT) {
				tableEl.deselectAll();
				reloadModel();
			}
			cmc.deactivate();
			cleanUpPopups();
		} else if (source == leaveDialogBox) {
			if (event != Event.CANCELLED_EVENT && DialogBoxUIFactory.isYesEvent(event)) {
				doLeave((BusinessGroup)leaveDialogBox.getUserObject());
				tableEl.deselectAll();
				reloadModel();
			}
		} else if (source == groupCreateController) {
			BusinessGroup group = null;
			if(event == Event.DONE_EVENT) {
				List<BusinessGroup> groups = groupCreateController.getCreatedGroups();
				if(groups.size() == 1) {
					group = groups.iterator().next();
				}
	
				if(!groups.isEmpty()) {
					tableEl.deselectAll();
					reloadModel();
				}
			}
			cmc.deactivate();
			cleanUpPopups();
			//if new group -> go to the tab
			if(group != null) {
				String businessPath = "[BusinessGroup:" + group.getKey() + "]";
				NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
			}
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if (source == businessGroupWizard) { 
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(businessGroupWizard);
				businessGroupWizard = null;
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					reloadModel();
				}
			}
		} else if(source == emailWizard) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(emailWizard);
				emailWizard = null;
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					tableEl.deselectAll();
				}
			}
		} else if (source == userManagementController) {
			cmc.deactivate();
			if(event == Event.DONE_EVENT) {
				//confirm sending emails
				MembershipModification mod = userManagementController.getModifications();
				List<BusinessGroup> groups = userManagementController.getGroups();
				confirmUserManagementEmail(ureq, mod, groups);
			} else {
				cleanUpPopups();
			}
		} else if (source == userManagementSendMailController) {
			if(event == Event.DONE_EVENT) {
				BGUserMailTemplate sendMail = (BGUserMailTemplate)userManagementSendMailController.getTemplate();
				MembershipModification mod = sendMail.getModifications();
				List<BusinessGroup> groups = sendMail.getGroups();
				finishUserManagement(mod, groups, sendMail, userManagementSendMailController.isSendMail());
				reloadModel();
			}
			cmc.deactivate();
			cleanUpPopups();
		} else if(source == contactCtrl) {
			cmc.deactivate();
			cleanUpPopups();
		} else if (source == cmc) {
			cleanUpPopups();
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		//
	}
	
	/**
	 * Aggressive clean up all popup controllers
	 */
	protected void cleanUpPopups() {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(contactCtrl);
		removeAsListenerAndDispose(groupCreateController);
		removeAsListenerAndDispose(businessGroupWizard);
		removeAsListenerAndDispose(leaveDialogBox);
		removeAsListenerAndDispose(confirmChangeStatusController);
		removeAsListenerAndDispose(confirmDefinitivelyDeleteController);
		cmc = null;
		contactCtrl = null;
		leaveDialogBox = null;
		groupCreateController = null;
		businessGroupWizard = null;
		confirmChangeStatusController = null;
		confirmDefinitivelyDeleteController = null;
	}
	
	/**
	 * Launch a business group with its business path
	 * @param ureq
	 * @param group
	 */
	protected void doAccess(UserRequest ureq, BusinessGroup group) {
		String businessPath = "[BusinessGroup:" + group.getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	/**
	 * Launch a business group with its business path
	 * @param ureq
	 * @param group
	 */
	protected void doLaunch(UserRequest ureq, BusinessGroup group) {
		String businessPath = "[BusinessGroup:" + group.getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	/**
	 * Launch a business group with its business path in administration part
	 * @param ureq
	 * @param group
	 */
	protected void doEdit(UserRequest ureq, BusinessGroup group) {
		String businessPath = "[BusinessGroup:" + group.getKey() + "][tooladmin:0]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doConfirmLeaving(UserRequest ureq, BusinessGroup businessGroup) {
		if (businessGroupService.hasRoles(getIdentity(), businessGroup, GroupRoles.coach.name())
				|| businessGroupService.hasRoles(getIdentity(), businessGroup, GroupRoles.waiting.name()) ) {
			doOpenConfirmLeavingDialog(ureq, businessGroup);
		} else {
			LeaveOption option = businessGroupService.isAllowToLeaveBusinessGroup(getIdentity(), businessGroup);
			if(option.isAllowToLeave()) {
				doOpenConfirmLeavingDialog(ureq, businessGroup);
			} else {
				doAskToLeaveGroup(ureq, businessGroup, option.getContacts());
			}
		}	
	}
	
	private void doOpenConfirmLeavingDialog(UserRequest ureq, BusinessGroup businessGroup) {
		String groupName = StringHelper.escapeHtml(businessGroup.getName());
		leaveDialogBox = activateYesNoDialog(ureq, null, translate("dialog.modal.bg.leave.text", groupName), leaveDialogBox);
		leaveDialogBox.setUserObject(businessGroup);
	}
	
	private void doAskToLeaveGroup(UserRequest ureq, BusinessGroup businessGroup, ContactList contacts) {
		String[] args = new String[]{
				businessGroup.getName(),
				businessGroup.getKey().toString(),
				"",//courses
				getIdentity().getUser().getProperty(UserConstants.FIRSTNAME, getLocale()),
				getIdentity().getUser().getProperty(UserConstants.LASTNAME, getLocale())
		};
		ContactMessage msg = new ContactMessage(getIdentity());
		msg.setSubject(translate("request.leaving.subject", args));
		msg.setBodyText(translate("request.leaving.body", args));
		msg.addEmailTo(contacts);
		
		contactCtrl = new ContactFormController(ureq, getWindowControl(), true, false, true, msg);
		listenTo(contactCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), contactCtrl.getInitialComponent(),
				true, translate("dialog.modal.bg.asktoleave.title"));
		cmc.activate();
		listenTo(cmc);
	}
	
	/**
	 * Removes user from the group as owner and participant. If
	 * no other owner are found the user won't be removed from the owner group
	 * 
	 * @param ureq
	 */
	private void doLeave(BusinessGroup group) {
		List<Identity> identityToRemove = Collections.singletonList(getIdentity());
		// 1) remove as owner
		if (businessGroupService.hasRoles(getIdentity(), group, GroupRoles.coach.name())) {
			List<Identity> ownerList = businessGroupService.getMembers(group, GroupRoles.coach.name());
			if (ownerList.size() > 1) {
				businessGroupService.removeOwners(getIdentity(), identityToRemove, group);
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
		businessGroupService.removeParticipants(getIdentity(), identityToRemove, group, null);
		// 3) remove from waiting list
		businessGroupService.removeFromWaitingList(getIdentity(), identityToRemove, group, null);
	}
	
	/**
	 * Create a new business group
	 * @param ureq
	 * @param wControl
	 */
	protected void doCreate(UserRequest ureq, WindowControl wControl, RepositoryEntry re) {				
		removeAsListenerAndDispose(groupCreateController);
		groupCreateController = new NewBGController(ureq, wControl, re, true, null);
		listenTo(groupCreateController);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), groupCreateController.getInitialComponent(), true, translate("create.form.title"));
		cmc.activate();
		listenTo(cmc);
	}
	
	/**
	 * Make copies of a list of business groups
	 * @param ureq
	 * @param items
	 */
	private void doCopy(UserRequest ureq, List<? extends BusinessGroupRef> items) {
		removeAsListenerAndDispose(businessGroupWizard);
		if(items == null || items.isEmpty()) {
			showWarning("error.select.one");
			return;
		}
		
		List<BusinessGroup> groups = toBusinessGroups(items, true);
		if(groups.isEmpty()) {
			showWarning("msg.alleastone.editable.group");
			return;
		}
		if(items.size() != groups.size()) {
			showWarning("msg.only.editable.group");
			return;
		}
		
		boolean enableCoursesCopy = businessGroupService.hasResources(groups);
		boolean enableAreasCopy = areaManager.countBGAreasOfBusinessGroups(groups) > 0;
		boolean enableRightsCopy = rightManager.hasBGRight(groups);

		Step start = new BGCopyPreparationStep(ureq, groups, enableCoursesCopy, enableAreasCopy, enableRightsCopy);
		StepRunnerCallback finish = (uureq, wControl, runContext) -> {
			@SuppressWarnings("unchecked")
			List<BGCopyBusinessGroup> copies = (List<BGCopyBusinessGroup>)runContext.get("groupsCopy");
			if(copies != null && !copies.isEmpty()) {
				boolean copyAreas = convertToBoolean(runContext, "areas");
				boolean copyCollabToolConfig = convertToBoolean(runContext, "tools");
				boolean copyRights = convertToBoolean(runContext, "rights");
				boolean copyOwners = convertToBoolean(runContext, "owners");
				boolean copyParticipants = convertToBoolean(runContext, "participants");
				boolean copyMemberVisibility = convertToBoolean(runContext, "membersvisibility");
				boolean copyWaitingList = convertToBoolean(runContext, "waitingList");
				boolean copyRelations = convertToBoolean(runContext, "resources");

				for(BGCopyBusinessGroup copy:copies) {
					businessGroupService.copyBusinessGroup(getIdentity(), copy.getOriginal(), copy.getNames(), copy.getDescription(),
							copy.getMinParticipants(), copy.getMaxParticipants(),
							copyAreas, copyCollabToolConfig, copyRights, copyOwners, copyParticipants,
							copyMemberVisibility, copyWaitingList, copyRelations, copy.getAllowToLeave());
				
				}
				return StepsMainRunController.DONE_MODIFIED;
			} else {
				return StepsMainRunController.DONE_UNCHANGED;
			}
		};
		
		businessGroupWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, translate("copy.group"), "o_sel_group_copy_wizard");
		listenTo(businessGroupWizard);
		getWindowControl().pushAsModalDialog(businessGroupWizard.getInitialComponent());
	}
	
	private boolean convertToBoolean(StepsRunContext runContext, String key) {
		Object obj = runContext.get(key);
		if(obj instanceof Boolean) {
			return ((Boolean)obj).booleanValue();
		} else {
			return false;
		}
	}
	
	/**
	 * 
	 * @param ureq
	 * @param items
	 */
	private void doConfiguration(UserRequest ureq, List<? extends BusinessGroupRef> selectedItems) {
		removeAsListenerAndDispose(businessGroupWizard);
		if(selectedItems == null || selectedItems.isEmpty()) {
			showWarning("error.select.one");
			return;
		}
		
		final List<BusinessGroup> groups = toBusinessGroups(selectedItems, true);
		if(groups.isEmpty()) {
			showWarning("msg.alleastone.editable.group");
			return;
		}
		if(selectedItems.size() != groups.size()) {
			showWarning("msg.only.editable.group");
			return;
		}
		
		if(CollaborationToolsFactory.getInstance().getAvailableTools() == null) {
			//init the available tools
			CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(groups.get(0));
		}
		
		StringBuilder managedNames = new StringBuilder();
		for(BusinessGroup group:groups) {
			String gname = group.getName() == null ? "???" : group.getName();
			if(BusinessGroupManagedFlag.isManaged(group, BusinessGroupManagedFlag.resources)
					|| BusinessGroupManagedFlag.isManaged(group, BusinessGroupManagedFlag.tools)) {
				if(managedNames.length() > 0) managedNames.append(", ");
				managedNames.append(gname);
			}
		}

		if(managedNames.length() > 0) {
			showWarning("error.managed.group", managedNames.toString());
			return;
		} 
		
		boolean isAuthor = roles.isAdministrator() || roles.isAuthor() || roles.isLearnResourceManager();

		Step start = new BGConfigToolsStep(ureq, isAuthor);
		StepRunnerCallback finish = (uureq, wControl, runContext) -> {
			//configuration
			BGConfigBusinessGroup configuration = (BGConfigBusinessGroup)runContext.get("configuration");
			if(!configuration.getToolsToEnable().isEmpty() || !configuration.getToolsToDisable().isEmpty()) {
				
				for(BusinessGroup group:groups) {
					CollaborationTools tools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(group);
					for(String enabledTool:configuration.getToolsToEnable()) {
						tools.setToolEnabled(enabledTool, true);
						if(CollaborationTools.TOOL_FOLDER.equals(enabledTool)) {
							tools.saveFolderAccess(Long.valueOf(configuration.getFolderAccess()));
							
							Quota quota = configuration.getQuota();
							if(quota != null) {
								String path = tools.getFolderRelPath();
								Quota fQuota = quotaManager.createQuota(path, quota.getQuotaKB(), quota.getUlLimitKB());
								quotaManager.setCustomQuotaKB(fQuota);
							}
							
						} else if (CollaborationTools.TOOL_CALENDAR.equals(enabledTool)) {
							tools.saveCalendarAccess(Long.valueOf(configuration.getCalendarAccess()));
						}
					}
					for(String disabledTool:configuration.getToolsToDisable()) {
						tools.setToolEnabled(disabledTool, false);
					}
				}
			}
			if(configuration.getResources() != null && !configuration.getResources().isEmpty()) {
				businessGroupService.addResourcesTo(groups, configuration.getResources());
			}
			return StepsMainRunController.DONE_MODIFIED;
		};
		
		businessGroupWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, translate("config.group"), "o_sel_groups_config_wizard");
		listenTo(businessGroupWizard);
		getWindowControl().pushAsModalDialog(businessGroupWizard.getInitialComponent());
	}
	
	/**
	 * 
	 * @param ureq
	 * @param items
	 */
	private void doEmails(UserRequest ureq, List<? extends BusinessGroupRef> selectedItems) {
		removeAsListenerAndDispose(emailWizard);
		if(selectedItems == null || selectedItems.isEmpty()) {
			showWarning("error.select.one");
			return;
		}
		
		List<BusinessGroup> groups = toBusinessGroups(selectedItems, true);
		if(groups.isEmpty()) {
			showWarning("msg.alleastone.editable.group");
			return;
		}
		if(selectedItems.size() != groups.size()) {
			showWarning("msg.only.editable.group");
			return;
		}

		Step start = new BGEmailSelectReceiversStep(ureq, groups);
		StepRunnerCallback finish = (uureq, wControl, runContext) -> {
			//mails are send by the last controller of the wizard
			wControl.setInfo(translate("msg.send.ok"));
			return StepsMainRunController.DONE_MODIFIED;
		};
		
		emailWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, translate("email.group"), "o_sel_groups_email_wizard");
		listenTo(emailWizard);
		getWindowControl().pushAsModalDialog(emailWizard.getInitialComponent());
	}
	
	/**
	 * 
	 * @param ureq
	 * @param items
	 */
	private void doUserManagement(UserRequest ureq, List<? extends BusinessGroupRef> selectedItems) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(userManagementController);
		if(selectedItems == null || selectedItems.isEmpty()) {
			showWarning("error.select.one");
			return;
		}
		
		List<BusinessGroup> groups = toBusinessGroups(selectedItems, true);
		if(groups.isEmpty()) {
			showWarning("msg.alleastone.editable.group");
			return;
		}
		if(selectedItems.size() != groups.size()) {
			showWarning("msg.only.editable.group");
			return;
		}
		
		StringBuilder managedNames = new StringBuilder();
		for(BusinessGroup group:groups) {
			String gname = group.getName() == null ? "???" : group.getName();
			if(BusinessGroupManagedFlag.isManaged(group, BusinessGroupManagedFlag.membersmanagement)) {
				if(managedNames.length() > 0) managedNames.append(", ");
				managedNames.append(gname);
			}
		}

		if(managedNames.length() > 0) {
			showWarning("error.managed.group", managedNames.toString());
			return;
		} 
		
		userManagementController = new BGUserManagementController(ureq, getWindowControl(), groups);
		listenTo(userManagementController);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), userManagementController.getInitialComponent(),
				true, translate("users.group"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void confirmUserManagementEmail(UserRequest ureq, MembershipModification mod, List<BusinessGroup> groups) {
		removeAsListenerAndDispose(cmc);
		removeAsListenerAndDispose(userManagementSendMailController);
		
		MailTemplate defaultTemplate = null;
		int totalModification = (mod.size() * groups.size());
		if(totalModification == 1) {
			MailType type = BusinessGroupMailing.getDefaultTemplateType(mod);
			if(type != null) {
				defaultTemplate = BusinessGroupMailing.getDefaultTemplate(type, groups.get(0), ureq.getIdentity());
			}
		}
		
		MailTemplate template = new BGUserMailTemplate(groups, mod, defaultTemplate);
		boolean mandatoryEmail = !mod.getAddParticipants().isEmpty() &&
				groupModule.isMandatoryEnrolmentEmail(ureq.getUserSession().getRoles());
		userManagementSendMailController = new BGMailNotificationEditController(getWindowControl(), ureq, template,
				totalModification == 1, totalModification == 1, false, mandatoryEmail);
		Component cmp = userManagementSendMailController.getInitialComponent();
		listenTo(userManagementSendMailController);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), cmp, true, translate("users.group"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void finishUserManagement(MembershipModification mod, List<BusinessGroup> groups, MailTemplate template, boolean sendMail) {
		MailPackage mailing = new MailPackage(template, getWindowControl().getBusinessControl().getAsString(), sendMail);
		businessGroupService.updateMembership(getIdentity(), mod, groups, mailing);
		MailHelper.printErrorsAndWarnings(mailing.getResult(), getWindowControl(), false, getLocale());
	}
	
	public boolean hasTab() {
		return tableEl.getSelectedFilterTab() != null;
	}
	
	public void selectFilterTab(UserRequest ureq, FlexiFiltersTab tab) {
		tableEl.setSelectedFilterTab(ureq, tab);
		changeFilterTab(ureq, tab);
		doSearch(ureq, tab);
	}
	
	protected abstract void changeFilterTab(UserRequest ureq, FlexiFiltersTab tab);
	
	protected abstract BusinessGroupQueryParams getDefaultSearchParams();
	
	protected final boolean doDefaultSearch() {
		BusinessGroupQueryParams params = getDefaultSearchParams();
		return loadModel(params) > 0;
	}
	
	protected void doSearch(UserRequest ureq, FlexiTableSearchEvent event) {
		BusinessGroupQueryParams params = getDefaultSearchParams();
		if("reset".equals(event.getCommand())) {
			if(tableEl.getSelectedFilterTab() != null) {
				doSearch(ureq, tableEl.getSelectedFilterTab());
			} else {
				doDefaultSearch();
			}
		} else {
			applyFiltersToQueryParams(event.getSearch(), event.getFilters(), params);
			loadModel(params);
		}
		tableEl.addToHistory(ureq);
	}
	
	protected void doSearch(UserRequest ureq, FlexiFiltersTab tab) {
		changeFilterTab(ureq, tab);
		
		if(tab == null || tab.getSelectionBehavior() == TabSelectionBehavior.clear) {
			groupTableModel.setObjects(new ArrayList<>());
			tableEl.reset(true, true, true);
		} else if(tab.getSelectionBehavior() == TabSelectionBehavior.reloadData) {
			BusinessGroupQueryParams params = getDefaultSearchParams();
			applyFiltersToQueryParams(tableEl.getQuickSearchString(), tableEl.getFilters(), params);
			loadModel(params);
		}
		tableEl.addToHistory(ureq);
	}
	
	private void doSelect(UserRequest ureq, List<? extends BusinessGroupRef> items) {
		List<BusinessGroup> selection = toBusinessGroups(items, false);
		fireEvent(ureq, new BusinessGroupSelectionEvent(selection));
	}
	
	private void doSelect(UserRequest ureq, BusinessGroup group) {
		List<BusinessGroup> selection = Collections.singletonList(group);
		fireEvent(ureq, new BusinessGroupSelectionEvent(selection));
	}
	
	@SuppressWarnings("unused")
	protected void doOpenTools(UserRequest ureq, BGTableItem item, BusinessGroup businessGroup) {
		//
	}
	
	/**
	 * 
	 * @param ureq
	 * @param items
	 */
	private void doMerge(UserRequest ureq, List<? extends BusinessGroupRef> selectedItems) {
		removeAsListenerAndDispose(businessGroupWizard);
		if(selectedItems == null || selectedItems.size() < 2) {
			showWarning("error.select.one");
			return;
		}

		final List<BusinessGroup> groups = toBusinessGroups(selectedItems, true);
		if(groups.size() < 2) {
			showWarning("msg.alleasttwo.editable.group");
			return;
		}
		if(selectedItems.size() != groups.size()) {
			showWarning("msg.only.editable.group");
			return;
		}
		
		StringBuilder managedNames = new StringBuilder();
		for(BusinessGroup group:groups) {
			String gname = group.getName() == null ? "???" : StringHelper.escapeHtml(group.getName());
			if(StringHelper.containsNonWhitespace(group.getManagedFlagsString())) {
				if(managedNames.length() > 0) managedNames.append(", ");
				managedNames.append(gname);
			}
		}

		if(managedNames.length() > 0) {
			showWarning("error.managed.group", managedNames.toString());
			return;
		} 

		Step start = new BGMergeStep(ureq, groups);
		StepRunnerCallback finish = new StepRunnerCallback() {
			@Override
			public Step execute(UserRequest uureq, WindowControl wControl, StepsRunContext runContext) {
				BusinessGroup targetGroup = (BusinessGroup)runContext.get("targetGroup");
				groups.remove(targetGroup);
				businessGroupService.mergeBusinessGroups(getIdentity(), targetGroup, groups, null);
				return StepsMainRunController.DONE_MODIFIED;
			}
		};
		
		businessGroupWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null, translate("merge.group"), "o_sel_groups_merge_wizard");
		listenTo(businessGroupWizard);
		getWindowControl().pushAsModalDialog(businessGroupWizard.getInitialComponent());
		
	}
	
	/**
	 * Confirmation panel before deleting the groups
	 * @param ureq
	 * @param selectedItems
	 */
	private void doConfirmDefinitivelyDelete(UserRequest ureq, List<? extends BusinessGroupRef> selectedItems) {
		List<BusinessGroup> groups = toBusinessGroups(selectedItems, true);
		if(groups.isEmpty()) {
			showWarning("msg.alleastone.editable.group");
			return;
		}
		if(selectedItems.size() != groups.size()) {
			showWarning("msg.only.editable.group");
			return;
		}
		
		StringBuilder names = new StringBuilder();
		StringBuilder managedNames = new StringBuilder();
		for(BusinessGroup group:groups) {
			String gname = group.getName() == null ? "???" : group.getName();
			if(BusinessGroupManagedFlag.isManaged(group, BusinessGroupManagedFlag.delete)) {
				if(managedNames.length() > 0) managedNames.append(", ");
				managedNames.append(gname);
			} else {
				if(names.length() > 0) names.append(", ");
				names.append(gname);
			}
		}
		
		if(managedNames.length() > 0) {
			showWarning("error.managed.group", managedNames.toString());
		} else {
			confirmDefinitivelyDeleteController = new ConfirmBusinessGroupDefinitivelyDeleteController(ureq, getWindowControl(), groups, false);
			listenTo(confirmDefinitivelyDeleteController);
			cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmDefinitivelyDeleteController.getInitialComponent(),
					true, translate("dialog.modal.bg.delete.title"));
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private void doConfirmRestore(UserRequest ureq, List<? extends BusinessGroupRef> selectedItems) {
		List<BusinessGroup> groups = toBusinessGroups(selectedItems, true);
		if(groups.isEmpty()) {
			showWarning("msg.alleastone.editable.group");
			return;
		}
		if(selectedItems.size() != groups.size()) {
			showWarning("msg.only.editable.group");
			return;
		}
		
		confirmRestoreController = new ConfirmRestoreController(ureq, getWindowControl(), groups);
		listenTo(confirmRestoreController);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmRestoreController.getInitialComponent(),
				true, translate("dialog.modal.bg.restore.title"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doCancelInactivation(List<? extends BusinessGroupRef> selectedItems) {
		List<BusinessGroup> groups = toBusinessGroups(selectedItems, true);
		if(groups.isEmpty()) {
			showWarning("msg.alleastone.editable.group");
			return;
		}
		if(selectedItems.size() != groups.size()) {
			showWarning("msg.only.editable.group");
			return;
		}
		
		for(BusinessGroup group:groups) {
			boolean asOwner = businessGroupService.hasRoles(getIdentity(), group, GroupRoles.coach.name());
			businessGroupLifecycleManager.reactivateBusinessGroup(group, getIdentity(), asOwner);
		}
		
		tableEl.deselectAll();
		reloadModel();

		if(groups.size() == 1) {
			showInfo("group.reactivated");
		} else {
			showInfo("groups.reactivated", new String[] { Integer.toString(groups.size()) });
		}
	}

	private void doConfirmStartChangeStatus(UserRequest ureq, List<? extends BusinessGroupRef> selectedItems, BusinessGroupStatusEnum newStatus) {
		List<BusinessGroup> groups = toBusinessGroups(selectedItems, true);
		if(groups.isEmpty()) {
			showWarning("msg.alleastone.editable.group");
			return;
		}
		if(selectedItems.size() != groups.size()) {
			showWarning("msg.only.editable.group");
			return;
		}
		
		StringBuilder names = new StringBuilder();
		StringBuilder managedNames = new StringBuilder();
		BusinessGroupManagedFlag managedFlag = newStatus == BusinessGroupStatusEnum.inactive
				? BusinessGroupManagedFlag.inactivate : BusinessGroupManagedFlag.delete;
		
		for(BusinessGroup group:groups) {
			String gname = group.getName() == null ? "???" : group.getName();
			if(BusinessGroupManagedFlag.isManaged(group, managedFlag)) {
				if(managedNames.length() > 0) managedNames.append(", ");
				managedNames.append(gname);
			} else {
				if(names.length() > 0) names.append(", ");
				names.append(gname);
			}
		}
		
		if(managedNames.length() > 0) {
			showWarning("error.managed.group", managedNames.toString());
		} else {
			confirmStartChangeStatusController = new ConfirmBusinessGroupStartChangeStatusController(ureq, getWindowControl(), groups, newStatus);
			listenTo(confirmStartChangeStatusController);
			
			String key = "dialog.modal.bg.delete.title";
			if(newStatus == BusinessGroupStatusEnum.inactive) {
				if(groups.size() == 1) {
					key = "dialog.modal.bg.inactivate.title.singular";
				} else {
					key = "dialog.modal.bg.inactivate.title.plural";
				}
			}
					
			String title = translate(key, new String[] { names.toString(), Integer.toString(groups.size()) });
			cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmStartChangeStatusController.getInitialComponent(), true, title);
			cmc.activate();
			listenTo(cmc);
		}
	}
	

	protected final void confirmChangeStatus(UserRequest ureq, List<? extends BusinessGroupRef> selectedItems, BusinessGroupStatusEnum newStatus) {
		List<BusinessGroup> groups = toBusinessGroups(selectedItems, true);
		if(groups.isEmpty()) {
			showWarning("msg.alleastone.editable.group");
			return;
		}
		if(selectedItems.size() != groups.size()) {
			showWarning("msg.only.editable.group");
			return;
		}
		
		StringBuilder names = new StringBuilder();
		StringBuilder managedNames = new StringBuilder();
		BusinessGroupManagedFlag managedFlag = newStatus == BusinessGroupStatusEnum.inactive
				? BusinessGroupManagedFlag.inactivate : BusinessGroupManagedFlag.delete;
		
		for(BusinessGroup group:groups) {
			String gname = group.getName() == null ? "???" : group.getName();
			if(BusinessGroupManagedFlag.isManaged(group, managedFlag)) {
				if(managedNames.length() > 0) managedNames.append(", ");
				managedNames.append(gname);
			} else {
				if(names.length() > 0) names.append(", ");
				names.append(gname);
			}
		}
		
		if(managedNames.length() > 0) {
			showWarning("error.managed.group", managedNames.toString());
		} else {
			confirmChangeStatusController = new ConfirmBusinessGroupChangeStatusController(ureq, getWindowControl(), groups, newStatus);
			listenTo(confirmChangeStatusController);
			
			String key = "dialog.modal.bg.delete.title";
			if(newStatus == BusinessGroupStatusEnum.inactive) {
				if(groups.size() == 1) {
					key = "dialog.modal.bg.inactivate.title.singular";
				} else {
					key = "dialog.modal.bg.inactivate.title.plural";
				}
			}
					
			String title = translate(key, names.toString(), Integer.toString(groups.size()));
			cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmChangeStatusController.getInitialComponent(), true, title);
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	protected final List<BusinessGroup> toBusinessGroups(List<? extends BusinessGroupRef> items, boolean editableOnly) {
		List<Long> groupKeys = new ArrayList<>();
		for(BusinessGroupRef item:items) {
			groupKeys.add(item.getKey());
		}
		if(editableOnly) {
			filterEditableGroupKeys(groupKeys);
		}
		return businessGroupService.loadBusinessGroups(groupKeys);
	}
	
	protected boolean filterEditableGroupKeys(List<Long> groupKeys) {
		if(roles.isAdministrator() || roles.isGroupManager()) {
			return false;
		}
		
		int countBefore = groupKeys.size();

		for(BGTableItem item:groupTableModel.getObjects()) {
			Long groupKey = item.getBusinessGroupKey();
			if(groupKeys.contains(groupKey)) {
				BusinessGroupMembership membership = item.getMembership();
				if(membership == null || !membership.isOwner()) {
					groupKeys.remove(groupKey);
				}
			}
		}
		
		return groupKeys.size() != countBefore;
	}
	
	protected void reloadModel() {
		loadModel(lastSearchParams);
	}
	
	protected RepositoryEntryRef getResource() {
		return null;
	}

	protected abstract List<BGTableItem> searchTableItems(BusinessGroupQueryParams params);
	
	protected final int loadModel(BusinessGroupQueryParams params) {
		this.lastSearchParams = params;
		if(params == null) {
			groupTableModel.setObjects(Collections.<BGTableItem>emptyList());
			tableEl.reset(true, true, true);
			return 0;
		}
		
		List<BGTableItem> items = searchTableItems(params);
		if(filter != null) {
			for(Iterator<BGTableItem> groupIt=items.iterator(); groupIt.hasNext(); ) {
				if(!filter.accept(groupIt.next())) {
					groupIt.remove();
				}
			}
		}
		
		groupTableModel.setObjects(items);
		tableEl.reset(true, true, true);
		return items.size();
	}
	
	protected void applyFiltersToQueryParams(String quickSearch, List<FlexiTableFilter> filters, BusinessGroupQueryParams params) {
		params.setNameOrDesc(quickSearch);
		if(filters != null) {
			filters.forEach(f -> applyFilterToQueryParams(f, params));
		}
		
		// check security, only administrator are allowed to search everywhere
		if(!params.isAttendee() && !params.isOwner() && !params.isWaiting()
				&& (params.getPublicGroups() == null || !params.getPublicGroups().booleanValue())
				&& !params.isAuthorConnection() && !isAdminSearchAllowed()) {
			params.setOwner(true);
			params.setAttendee(true);
			params.setWaiting(true);
		}
	}
	
	protected boolean isAdminSearchAllowed() {
		return roles.isAdministrator() || roles.isGroupManager() 
				|| (roles.isLearnResourceManager() && groupModule.isResourceManagersAllowedToLinkGroups());
	}
	
	protected void applyFilterToQueryParams(FlexiTableFilter tableFilter, BusinessGroupQueryParams params) {
		switch(BGSearchFilter.valueOf(tableFilter.getFilter())) {
			case ID:
				params.setIdRef(tableFilter.getValue());
				break;
			case MARKED:
				params.setMarked(StringHelper.containsNonWhitespace(tableFilter.getValue()));
				break;
			case ROLE:
				String role = tableFilter.getValue();
				if(role == null || "none".equals(role)) {
					// security, only admin are allowed to see all
					boolean open = params.getPublicGroups() != null && params.getPublicGroups().booleanValue();	
					params.setAttendee(!admin && !open);
					params.setOwner(!admin && !open);
					params.setWaiting(!admin && !open);
				} else {
					params.setAttendee("all".equals(role) || "attendee".equals(role));
					params.setOwner("all".equals(role) || "owner".equals(role));
					params.setWaiting("all".equals(role) || "waiting".equals(role));
				}
				break;
			case COACH:
				params.setOwnerName(tableFilter.getValue());
				break;
			case AUTHOR:
				params.setAuthorConnection(StringHelper.containsNonWhitespace(tableFilter.getValue()));
				break;	
			case DESCRIPTION:
				params.setDescription(tableFilter.getValue());
				break;
			case COURSETITLE:
				params.setCourseTitle(tableFilter.getValue());
				break;
			case OPEN:
				Boolean publicOnly = BGSearchFilter.OPEN.yesNoTo(tableFilter);
				if(publicOnly != null &&  publicOnly.booleanValue()) {
					params.setAttendee(false);
					params.setOwner(false);
					params.setWaiting(false);
				}
				params.setPublicGroups(publicOnly);
				break;
			case MANAGED:
				params.setManaged(BGSearchFilter.MANAGED.yesNoTo(tableFilter));
				break;
			case RESOURCES:
				params.setResources(BGSearchFilter.RESOURCES.yesNoTo(tableFilter));
				break;
			case HEADLESS:
				params.setHeadless(StringHelper.containsNonWhitespace(tableFilter.getValue()));
				break;
			case LASTVISIT:
				String lastVisit = tableFilter.getValue();
				if(StringHelper.isLong(lastVisit)) {
					Date lastUsage = DateUtils.addDays(new Date(), -Integer.parseInt(lastVisit));
					params.setLastUsageBefore(CalendarUtils.startOfDay(lastUsage));
				} else {
					params.setLastUsageBefore(null);
				}
				break;
			case STATUS:
				List<String> groupStatus = ((FlexiTableMultiSelectionFilter)tableFilter).getValues();
				params.setGroupStatus(BusinessGroupStatusEnum.toList(groupStatus));
				break;
			case LIFECYCLE:
				String lifecycleStatus = tableFilter.getValue();
				if(StringHelper.containsNonWhitespace(lifecycleStatus)) {
					params.setLifecycleStatus(LifecycleSyntheticStatus.valueOf(lifecycleStatus));
					params.setLifecycleStatusReference(new Date());
				} else {
					params.setLifecycleStatus(null);
					params.setLifecycleStatusReference(null);
				}
				break;
			default:
				break;
		}
	}
	
	protected static class RoleColumnDescriptor extends CustomRenderColumnDescriptor {
		public RoleColumnDescriptor(Locale locale) {
			super(Cols.role.i18nHeaderKey(), Cols.role.ordinal(), null, locale,  ColumnDescriptor.ALIGNMENT_LEFT, new BGRoleCellRenderer(locale));
		}

		@Override
		public int compareTo(int rowa, int rowb) {
			Object a = table.getTableDataModel().getValueAt(rowa,dataColumn);
			Object b = table.getTableDataModel().getValueAt(rowb,dataColumn);
			if(a instanceof BusinessGroupMembership && b instanceof BusinessGroupMembership) {
				return MEMBERSHIP_COMPARATOR.compare((BusinessGroupMembership)a, (BusinessGroupMembership)b);
			}
			return super.compareTo(rowa, rowb);
		}
	}
	
	public enum BGSearchFilter {
		ID,
		MARKED,
		ROLE,
		COACH,
		AUTHOR,
		DESCRIPTION,
		COURSETITLE,
		OPEN,
		MANAGED,
		RESOURCES,
		HEADLESS,
		LASTVISIT,
		STATUS,
		LIFECYCLE,
		LIFECYCLE_METHOD;
		
		public Boolean yesNoTo(FlexiTableFilter tableFilter) {
			if("yes".equals(tableFilter.getValue())) {
				return Boolean.TRUE;
			} else if("no".equals(tableFilter.getValue())) {
				return Boolean.FALSE;
			} 
			return null;
		}
	}
}
