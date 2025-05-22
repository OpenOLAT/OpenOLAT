/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.taxonomy.ui;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
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
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyCompetenceAuditLog;
import org.olat.modules.taxonomy.TaxonomyCompetenceLinkLocations;
import org.olat.modules.taxonomy.TaxonomyCompetenceTypes;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelManagedFlag;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.TaxonomyLevelCompetenceTableModel.CompetenceCols;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: May 22, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TaxonomyLevelManagementController extends FormBasicController {

	private FormLink addCompetencesButton;
	private FormLink removeCompetencesButton;
	private FlexiTableElement tableEl;
	private TaxonomyLevelCompetenceTableModel tableModel;
	
	private CloseableModalController cmc;
	private UserSearchController userSearchCtrl;
	private DialogBoxController confirmDeleteDialog;
	
	private TaxonomyLevel taxonomyLevel;
	private final List<UserPropertyHandler> userPropertyHandlers;

	@Autowired
	private UserManager userManager;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private BaseSecurityModule securityModule;

	protected TaxonomyLevelManagementController(UserRequest ureq, WindowControl wControl, TaxonomyLevel taxonomyLevel) {
		super(ureq, wControl, "level_management");
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.taxonomyLevel = taxonomyLevel;
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(TaxonomyLevelCompetenceController.USER_PROPS_ID, isAdministrativeUser);
	
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		boolean multiSelect = false;
		if(!TaxonomyLevelManagedFlag.isManaged(taxonomyLevel, TaxonomyLevelManagedFlag.manageCompetence)) {
			addCompetencesButton = uifactory.addFormLink("add.manager", formLayout, Link.BUTTON);
			addCompetencesButton.setElementCssClass("o_sel_management_add");
			removeCompetencesButton = uifactory.addFormLink("delete", formLayout, Link.BUTTON);
			multiSelect = true;
		}
		
		// table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CompetenceCols.key));

		int colPos = TaxonomyLevelCompetenceTableModel.USER_PROPS_OFFSET;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;

			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(TaxonomyLevelCompetenceController.USER_PROPS_ID , userPropertyHandler);

			FlexiColumnModel col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, true, propName);
			columnsModel.addFlexiColumnModel(col);
			colPos++;
		}
		
		tableModel = new TaxonomyLevelCompetenceTableModel(columnsModel); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setEmptyTableMessageKey("table.management.empty");
		tableEl.setMultiSelect(multiSelect);
		tableEl.setSelectAllEnable(multiSelect);
		tableEl.setAndLoadPersistedPreferences(ureq, "tax-level-management");
		tableEl.addBatchButton(removeCompetencesButton);
	}
	
	private void loadModel() {
		List<TaxonomyCompetence> competences = taxonomyService.getTaxonomyLevelCompetences(taxonomyLevel, TaxonomyCompetenceTypes.manage);
		List<TaxonomyLevelCompetenceRow> rows = competences.stream()
				.map(c -> new TaxonomyLevelCompetenceRow(c, userPropertyHandlers, getLocale()))
				.collect(Collectors.toList());
		tableModel.setObjects(rows);
		tableEl.reset(false, true, true);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addCompetencesButton == source) {
			doSearchUsersToAdd(ureq);
		} else if(removeCompetencesButton == source) {
			doConfirmRemoveCompetences(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(userSearchCtrl == source) {
			if (event instanceof SingleIdentityChosenEvent) {
				SingleIdentityChosenEvent singleEvent = (SingleIdentityChosenEvent)event;
				Identity choosenIdentity = singleEvent.getChosenIdentity();
				if (choosenIdentity != null) {
					List<Identity> toAdd = Collections.singletonList(choosenIdentity);
					doAddCompetence(toAdd);
					loadModel();
				}
			} else if (event instanceof MultiIdentityChosenEvent) {
				MultiIdentityChosenEvent multiEvent = (MultiIdentityChosenEvent)event;
				List<Identity> toAdd = multiEvent.getChosenIdentities();
				if(!toAdd.isEmpty()) {
					doAddCompetence(toAdd);
					loadModel();
				}
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmDeleteDialog == source) {
			if (DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				@SuppressWarnings("unchecked")
				List<TaxonomyLevelCompetenceRow> selectedRows = (List<TaxonomyLevelCompetenceRow>)confirmDeleteDialog.getUserObject();
				doRemoveCompetences(selectedRows);
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(userSearchCtrl);
		removeAsListenerAndDispose(cmc);
		userSearchCtrl = null;
		cmc = null;
	}
	
	private void doSearchUsersToAdd(UserRequest ureq) {
		if(guardModalController(userSearchCtrl)) return;

		userSearchCtrl = new UserSearchController(ureq, getWindowControl(), true, true, false);
		listenTo(userSearchCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), userSearchCtrl.getInitialComponent(),
				true, translate("add.competence.manage"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddCompetence(List<Identity> identities) {
		Taxonomy taxonomy = taxonomyLevel.getTaxonomy();
		for(Identity identity:identities) {
			TaxonomyCompetence competence = taxonomyService.addTaxonomyLevelCompetences(taxonomyLevel, identity, TaxonomyCompetenceTypes.manage, null, TaxonomyCompetenceLinkLocations.MANUAL_INTERNAL);
			String after = taxonomyService.toAuditXml(competence);
			taxonomyService.auditLog(TaxonomyCompetenceAuditLog.Action.addCompetence, null, after, null, taxonomy, competence, identity, getIdentity());
		}
	}
	
	private void doConfirmRemoveCompetences(UserRequest ureq) {
		List<TaxonomyLevelCompetenceRow> selectedRows = tableEl.getMultiSelectedIndex().stream()
				.map(i -> tableModel.getObject(i.intValue()))
				.filter(Objects::nonNull)
				.toList();
		
		if(selectedRows.isEmpty()) {
			showWarning("atleast.one.competence");
		} else {
			String title = translate("confirmation.remove.manage.title");
			String text = translate("confirmation.remove.manage",
					new String[] { StringHelper.escapeHtml(TaxonomyUIFactory.translateDisplayName(getTranslator(), taxonomyLevel)) });
			confirmDeleteDialog = activateOkCancelDialog(ureq, title, text, confirmDeleteDialog);
			confirmDeleteDialog.setUserObject(selectedRows);
		}
	}
	
	private void doRemoveCompetences(List<TaxonomyLevelCompetenceRow> rows) {
		for(TaxonomyLevelCompetenceRow row:rows) {
			taxonomyService.removeTaxonomyLevelCompetence(row.getCompetence());
		}
		loadModel();
		tableEl.reset(true, true, true);
	}

}
