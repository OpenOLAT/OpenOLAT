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
package org.olat.user.ui.identity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.IdentityRelationshipService;
import org.olat.basesecurity.IdentityToIdentityRelation;
import org.olat.basesecurity.IdentityToIdentityRelationManagedFlag;
import org.olat.basesecurity.RelationSearchParams;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.assessment.ui.tool.AssessmentToolConstants;
import org.olat.course.member.wizard.ImportMemberByUsernamesController;
import org.olat.course.member.wizard.MembersByNameContext;
import org.olat.user.UserManager;
import org.olat.user.UserModule;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.olat.user.ui.identity.UserRelationsTableModel.RelationCols;
import org.olat.user.ui.role.ManagedCellRenderer;
import org.olat.user.ui.role.RelationRolesAndRightsUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserRelationsController extends FormBasicController {
	
	public static final int USER_SOURCE_PROPS_OFFSET = 500;
	public static final int USER_TARGET_PROPS_OFFSET = 1500;
	public static final String usageIdentifyer = UserRelationsController.class.getCanonicalName();

	private FormLink addRelationsButton;
	private FormLink importRelationsButton;
	private FormLink removeButton;
	
	private FlexiTableElement tableEl;
	private UserRelationsTableModel tableModel;
	
	private CloseableModalController cmc;
	private StepsMainRunController importRelationsWizard;
	private ConfirmRemoveRelationController confirmRemoveRelationsCtrl;
	
	private final boolean canModify;
	private final Identity editedIdentity;
	private final List<UserPropertyHandler> userPropertyHandlers;

	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private IdentityRelationshipService identityRelationsService;
	
	public UserRelationsController(UserRequest ureq, WindowControl wControl, Identity editedIdentity, boolean canModify) {
		super(ureq, wControl, "relations", Util.createPackageTranslator(UserModule.class, ureq.getLocale()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.canModify = canModify;
		this.editedIdentity = editedIdentity;
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(canModify) {
			importRelationsButton = uifactory.addFormLink("import.relations", formLayout, Link.BUTTON);
			importRelationsButton.setIconLeftCSS("o_icon o_icon-fw o_icon_import");
				
			addRelationsButton = uifactory.addFormLink("add.relations", formLayout, Link.BUTTON);
			addRelationsButton.setIconLeftCSS("o_icon o_icon-fw o_icon_add_member");

			removeButton = uifactory.addFormLink("remove", formLayout, Link.BUTTON);
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, RelationCols.key));
		if(securityModule.isRelationRoleManaged()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RelationCols.managed, new ManagedCellRenderer()));
		}
		//add the table
		int colIndex = USER_SOURCE_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = UserManager.getInstance().isMandatoryUserProperty(AssessmentToolConstants.usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, null, true, "userProp-" + colIndex));
			colIndex++;
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(RelationCols.role));
		
		colIndex = USER_TARGET_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = UserManager.getInstance().isMandatoryUserProperty(AssessmentToolConstants.usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, null, true, "userProp-" + colIndex));
			colIndex++;
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("remove", RelationCols.remove.ordinal(), "remove",
				new BooleanCellRenderer(
						new StaticFlexiCellRenderer(translate("remove"), "remove"), null)));
		
		tableModel = new UserRelationsTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 24, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "relations-u-to-u-v2");
		if(removeButton != null) {
			tableEl.setMultiSelect(true);
			tableEl.setSelectAllEnable(true);
			tableEl.addBatchButton(removeButton);
		}
	}
	
	protected void loadModel() {
		List<IdentityToIdentityRelation> asSourceRelations = identityRelationsService.getRelationsAsSource(editedIdentity);
		RelationSearchParams searchParams = new RelationSearchParams();
		List<IdentityToIdentityRelation> asTargetRelations = identityRelationsService.getRelationsAsTarget(editedIdentity, searchParams);
		List<IdentityRelationRow> rows = new ArrayList<>(asSourceRelations.size() + asTargetRelations.size());
		for(IdentityToIdentityRelation rel:asSourceRelations) {
			String relationName = RelationRolesAndRightsUIFactory
					.getTranslatedRoleDescription(rel.getRole(), getLocale());
			rows.add(new IdentityRelationRow(rel.getKey(), editedIdentity, rel.getTarget(), rel.getRole(),
					relationName, rel.getManagedFlags(), userPropertyHandlers, getLocale()));
		}
		for(IdentityToIdentityRelation rel:asTargetRelations) {
			String relationName = RelationRolesAndRightsUIFactory
					.getTranslatedRoleDescription(rel.getRole(), getLocale());
			rows.add(new IdentityRelationRow(rel.getKey(), rel.getSource(), editedIdentity, rel.getRole(),
					relationName, rel.getManagedFlags(), userPropertyHandlers, getLocale()));
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == importRelationsWizard) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				cleanUp();
				if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
					loadModel();
				}
			}
		} else if(confirmRemoveRelationsCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmRemoveRelationsCtrl);
		removeControllerListener(importRelationsWizard);
		removeAsListenerAndDispose(cmc);
		confirmRemoveRelationsCtrl = null;
		importRelationsWizard = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(importRelationsButton == source) {
			doImportRelations(ureq);
		} else if(addRelationsButton == source) {
			doAddRelations(ureq);
		} else if(removeButton == source) {
			doConfirmRemove(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("remove".equals(se.getCommand())) {
					doConfirmRemove(ureq, tableModel.getObject(se.getIndex()));
				}
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doConfirmRemove(UserRequest ureq, IdentityRelationRow row) {
		if(confirmRemoveRelationsCtrl != null) return;
		
		List<IdentityRelationRow> relationsToRemove = new ArrayList<>(1);
		if(canModify && !IdentityToIdentityRelationManagedFlag.isManaged(row.getManagedFlags(), IdentityToIdentityRelationManagedFlag.delete)) {
			relationsToRemove.add(row);
		}
		doConfirmRemove(ureq, relationsToRemove);
	}
	
	private void doConfirmRemove(UserRequest ureq) {
		if(confirmRemoveRelationsCtrl != null) return;
		
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		List<IdentityRelationRow> relationsToRemove = new ArrayList<>(selectedIndex.size());
		for(Integer index:selectedIndex) {
			IdentityRelationRow row = tableModel.getObject(index.intValue());
			if(canModify && !IdentityToIdentityRelationManagedFlag.isManaged(row.getManagedFlags(), IdentityToIdentityRelationManagedFlag.delete)) {
				relationsToRemove.add(row);
			}
		}
		doConfirmRemove(ureq, relationsToRemove);
	}
	
	private void doConfirmRemove(UserRequest ureq, List<IdentityRelationRow> relationsToRemove) {	
		if(relationsToRemove.isEmpty()) {
			showWarning("warning.at.least.one.relation");
		} else {
			confirmRemoveRelationsCtrl = new ConfirmRemoveRelationController(ureq, getWindowControl(), relationsToRemove);
			listenTo(confirmRemoveRelationsCtrl);
			String title = translate("confirm.remove.relation.title");
			cmc = new CloseableModalController(getWindowControl(), "close", confirmRemoveRelationsCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private void doAddRelations(UserRequest ureq) {
		removeAsListenerAndDispose(importRelationsWizard);

		Step start = new ImportRelation_1b_Step(ureq, editedIdentity);
		StepRunnerCallback finish = (uureq, wControl, runContext) -> {
			doAddRelations(runContext);
			if(runContext.containsKey("notFounds")) {
				showWarning("user.notfound", runContext.get("notFounds").toString());
			}
			return StepsMainRunController.DONE_MODIFIED;
		};
		
		importRelationsWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("import.relations"), "o_sel_import_realtions_wizard");
		listenTo(importRelationsWizard);
		getWindowControl().pushAsModalDialog(importRelationsWizard.getInitialComponent());
	}
	
	private void doImportRelations(UserRequest ureq) {
		removeAsListenerAndDispose(importRelationsWizard);

		Step start = new ImportRelation_1a_Step(ureq, editedIdentity);
		StepRunnerCallback finish = (uureq, wControl, runContext) -> {
			doAddRelations(runContext);
			MembersByNameContext membersByNameContext = (MembersByNameContext)runContext.get(ImportMemberByUsernamesController.RUN_CONTEXT_KEY);
			if(!membersByNameContext.getNotFoundNames().isEmpty()) {
				String notFoundNames = membersByNameContext.getNotFoundNames().stream()
						.collect(Collectors.joining(", "));
				showWarning("user.notfound", notFoundNames);
			}
			return StepsMainRunController.DONE_MODIFIED;
		};
		
		importRelationsWizard = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("import.relations"), "o_sel_import_realtions_wizard");
		listenTo(importRelationsWizard);
		getWindowControl().pushAsModalDialog(importRelationsWizard.getInitialComponent());
	}
	
	private void doAddRelations(StepsRunContext runContext) {
		Set<Identity> relations = ((MembersByNameContext)runContext.get(ImportMemberByUsernamesController.RUN_CONTEXT_KEY)).getIdentities();
		UserRelationRoles relationRoles = (UserRelationRoles)runContext.get("relationRoles");
		for(Identity relation:relations) {
			identityRelationsService.addRelations(relation, editedIdentity, relationRoles.getSelectedRoles());
			identityRelationsService.addRelations(editedIdentity, relation, relationRoles.getSelectedContraRoles());
		}
	}
}
