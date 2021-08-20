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
package org.olat.modules.taxonomy.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.admin.user.UserSearchController;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyCompetenceAuditLog;
import org.olat.modules.taxonomy.TaxonomyCompetenceLinkLocations;
import org.olat.modules.taxonomy.TaxonomyCompetenceTypes;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelManagedFlag;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.TaxonomyLevelCompetenceTableModel.CompetenceCols;
import org.olat.modules.taxonomy.ui.component.PercentCellRenderer;
import org.olat.modules.taxonomy.ui.component.TaxonomyCompetenceTypeRenderer;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 Oct 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyLevelCompetenceController extends FormBasicController {
	
	protected static final String USER_PROPS_ID = TaxonomyLevelCompetenceController.class.getCanonicalName();
	
	public static final int USER_PROPS_OFFSET = 500;
	
	private FlexiTableElement tableEl;
	private TaxonomyLevelCompetenceTableModel tableModel;
	private FormLink addCompetencesButton, removeCompetencesButton;
	
	private CloseableModalController cmc;
	private UserSearchController userSearchCtrl;
	private DialogBoxController confirmDeleteDialog;
	private AddCompetencesController addCompetencesCtrl;
	private CloseableCalloutWindowController addCompetencesCallout;
	private EditTaxonomyCompetenceController editCompetenceCtrl;

	private TaxonomyLevel taxonomyLevel;
	private final List<UserPropertyHandler> userPropertyHandlers;

	@Autowired
	private UserManager userManager;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public TaxonomyLevelCompetenceController(UserRequest ureq, WindowControl wControl, TaxonomyLevel taxonomyLevel) {
		super(ureq, wControl, "level_competences");
		this.taxonomyLevel = taxonomyLevel;
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(USER_PROPS_ID, isAdministrativeUser);

		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		boolean multiSelect = false;
		if(!TaxonomyLevelManagedFlag.isManaged(taxonomyLevel, TaxonomyLevelManagedFlag.manageCompetence)
				|| !TaxonomyLevelManagedFlag.isManaged(taxonomyLevel, TaxonomyLevelManagedFlag.teachCompetence)
				|| !TaxonomyLevelManagedFlag.isManaged(taxonomyLevel, TaxonomyLevelManagedFlag.haveCompetence)
				|| !TaxonomyLevelManagedFlag.isManaged(taxonomyLevel, TaxonomyLevelManagedFlag.targetCompetence)) {
			addCompetencesButton = uifactory.addFormLink("add.competences", formLayout, Link.BUTTON);
			addCompetencesButton.setElementCssClass("o_sel_competence_add");
			removeCompetencesButton = uifactory.addFormLink("delete", formLayout, Link.BUTTON);
			multiSelect = true;
		}
		
		// table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CompetenceCols.key));

		int colPos = USER_PROPS_OFFSET;
		for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
			if (userPropertyHandler == null) continue;

			String propName = userPropertyHandler.getName();
			boolean visible = userManager.isMandatoryUserProperty(USER_PROPS_ID , userPropertyHandler);

			FlexiColumnModel col = new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos, true, propName);
			columnsModel.addFlexiColumnModel(col);
			colPos++;
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CompetenceCols.type, new TaxonomyCompetenceTypeRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CompetenceCols.achievement, new PercentCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CompetenceCols.reliability, new PercentCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CompetenceCols.expiration, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), "select"));
		
		tableModel = new TaxonomyLevelCompetenceTableModel(columnsModel); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setEmptyTableMessageKey("table.competence.empty");
		tableEl.setMultiSelect(multiSelect);
		tableEl.setSelectAllEnable(multiSelect);
		tableEl.setAndLoadPersistedPreferences(ureq, "tax-level-competences-v2");
		tableEl.addBatchButton(removeCompetencesButton);
	}
	
	private void loadModel() {
		List<TaxonomyCompetence> competences = taxonomyService.getTaxonomyLevelCompetences(taxonomyLevel);
		List<TaxonomyLevelCompetenceRow> rows = competences.stream()
				.map(c -> new TaxonomyLevelCompetenceRow(c, userPropertyHandlers, getLocale()))
				.collect(Collectors.toList());
		tableModel.setObjects(rows);
		tableEl.reset(false, true, true);
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
		if(addCompetencesButton == source) {
			doAddCompetence(ureq);
		} else if(removeCompetencesButton == source) {
			doConfirmRemoveCompetences(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("select".equals(cmd)) {
					TaxonomyLevelCompetenceRow row = tableModel.getObject(se.getIndex());
					doSelectTaxonomyLevelCompetence(ureq, row);
				}
			}
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
					doAddCompetence(toAdd, (TaxonomyCompetenceTypes)userSearchCtrl.getUserObject());
					loadModel();
				}
			} else if (event instanceof MultiIdentityChosenEvent) {
				MultiIdentityChosenEvent multiEvent = (MultiIdentityChosenEvent)event;
				List<Identity> toAdd = multiEvent.getChosenIdentities();
				if(!toAdd.isEmpty()) {
					doAddCompetence(toAdd, (TaxonomyCompetenceTypes)userSearchCtrl.getUserObject());
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
		} else if(editCompetenceCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
				tableEl.reset(false, false, true);
			}
			cmc.deactivate();
			cleanUp();
		} else if(addCompetencesCallout == source) {
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(addCompetencesCallout);
		removeAsListenerAndDispose(addCompetencesCtrl);
		removeAsListenerAndDispose(editCompetenceCtrl);
		removeAsListenerAndDispose(userSearchCtrl);
		removeAsListenerAndDispose(cmc);
		addCompetencesCallout = null;
		addCompetencesCtrl = null;
		editCompetenceCtrl = null;
		userSearchCtrl = null;
		cmc = null;
	}
	
	private void doSelectTaxonomyLevelCompetence(UserRequest ureq, TaxonomyLevelCompetenceRow row) {
		if(guardModalController(editCompetenceCtrl)) return;

		TaxonomyCompetence competence = row.getCompetence();
		editCompetenceCtrl = new EditTaxonomyCompetenceController(ureq, getWindowControl(), competence);
		listenTo(editCompetenceCtrl);
		
		String fullname = userManager.getUserDisplayName(row.getCompetence().getIdentity());
		String type = translate(row.getCompetenceType().name());
		String title = translate("edit.competence.title", new String[] { fullname, type });
		cmc = new CloseableModalController(getWindowControl(), translate("close"), editCompetenceCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddCompetence(UserRequest ureq) {
		addCompetencesCtrl = new AddCompetencesController(ureq, getWindowControl());
		listenTo(addCompetencesCtrl);
		
		addCompetencesCallout = new CloseableCalloutWindowController(ureq, getWindowControl(),
				addCompetencesCtrl.getInitialComponent(), addCompetencesButton.getFormDispatchId(), "", true, "");
		listenTo(addCompetencesCallout);
		addCompetencesCallout.activate();
	}

	private void doSearchUsersToAdd(UserRequest ureq, TaxonomyCompetenceTypes comptenceType) {
		if(guardModalController(userSearchCtrl)) return;

		userSearchCtrl = new UserSearchController(ureq, getWindowControl(), true, true, false);
		userSearchCtrl.setUserObject(comptenceType);
		listenTo(userSearchCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), userSearchCtrl.getInitialComponent(),
				true, translate("add.competence." + comptenceType.name()));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddCompetence(List<Identity> identities, TaxonomyCompetenceTypes comptenceType) {
		Taxonomy taxonomy = taxonomyLevel.getTaxonomy();
		for(Identity identity:identities) {
			TaxonomyCompetence competence = taxonomyService.addTaxonomyLevelCompetences(taxonomyLevel, identity, comptenceType, null, TaxonomyCompetenceLinkLocations.MANUAL_INTERNAL);
			String after = taxonomyService.toAuditXml(competence);
			taxonomyService.auditLog(TaxonomyCompetenceAuditLog.Action.addCompetence, null, after, null, taxonomy, competence, identity, getIdentity());
		}
	}
	
	private void doConfirmRemoveCompetences(UserRequest ureq) {
		Set<Integer> index = tableEl.getMultiSelectedIndex();
		
		StringBuilder names = new StringBuilder(512);
		List<TaxonomyLevelCompetenceRow> selectedRows = new ArrayList<>(index.size());
		for(Integer i : index) {
			TaxonomyLevelCompetenceRow selectedRow = tableModel.getObject(i.intValue());
			
			boolean managed = false;
			if(selectedRow.getCompetenceType() == TaxonomyCompetenceTypes.manage) {
				managed = TaxonomyLevelManagedFlag.isManaged(taxonomyLevel, TaxonomyLevelManagedFlag.manageCompetence);
			} else if(selectedRow.getCompetenceType() == TaxonomyCompetenceTypes.teach) {
				managed = TaxonomyLevelManagedFlag.isManaged(taxonomyLevel, TaxonomyLevelManagedFlag.teachCompetence);
			} else if(selectedRow.getCompetenceType() == TaxonomyCompetenceTypes.have) {
				managed = TaxonomyLevelManagedFlag.isManaged(taxonomyLevel, TaxonomyLevelManagedFlag.haveCompetence);
			} else if(selectedRow.getCompetenceType() == TaxonomyCompetenceTypes.target) {
				managed = TaxonomyLevelManagedFlag.isManaged(taxonomyLevel, TaxonomyLevelManagedFlag.targetCompetence);
			}
			
			if(!managed) {
				selectedRows.add(selectedRow);
				
				String fullName = userManager.getUserDisplayName(selectedRow.getIdentityKey());
				if(names.length() > 0) {
					names.append(", ");
				}
				names.append(StringHelper.escapeHtml(fullName));
			}
		}
		
		if(selectedRows.isEmpty()) {
			showWarning("atleast.one.competence");
		} else {
			String title = translate("confirmation.remove.competence.title");
			String text = translate("confirmation.remove.competence", new String[] { names.toString(), StringHelper.escapeHtml(taxonomyLevel.getDisplayName()) });
			confirmDeleteDialog = activateOkCancelDialog(ureq, title, text, confirmDeleteDialog);
			confirmDeleteDialog.setUserObject(selectedRows);
		}
	}
	
	private void doRemoveCompetences(List<TaxonomyLevelCompetenceRow> rows) {
		for(TaxonomyLevelCompetenceRow row:rows) {
			taxonomyService.removeTaxonomyLevelCompetence(row.getCompetence());
		}
		showInfo("confirm.removed.competence");
		loadModel();
		tableEl.reset(true, true, true);
	}
	
	private class AddCompetencesController extends BasicController {

		private final VelocityContainer toolVC;

		public AddCompetencesController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl);
			
			toolVC = createVelocityContainer("tools");
			List<String> links = new ArrayList<>();
			if(!TaxonomyLevelManagedFlag.isManaged(taxonomyLevel, TaxonomyLevelManagedFlag.manageCompetence)) {
				addLink("add.competence.manage", TaxonomyCompetenceTypes.manage, links);
			}
			if(!TaxonomyLevelManagedFlag.isManaged(taxonomyLevel, TaxonomyLevelManagedFlag.teachCompetence)) {
				addLink("add.competence.teach", TaxonomyCompetenceTypes.teach, links);
			}
			if(!TaxonomyLevelManagedFlag.isManaged(taxonomyLevel, TaxonomyLevelManagedFlag.haveCompetence)) {
				addLink("add.competence.have", TaxonomyCompetenceTypes.have, links);
			}
			if(!TaxonomyLevelManagedFlag.isManaged(taxonomyLevel, TaxonomyLevelManagedFlag.targetCompetence)) {
				addLink("add.competence.target", TaxonomyCompetenceTypes.target, links);
			}
			toolVC.contextPut("links", links);
			putInitialPanel(toolVC);
		}
		
		private void addLink(String name, TaxonomyCompetenceTypes competence, List<String> links) {
			Link link = LinkFactory.createLink(name, name, getTranslator(), toolVC, this, Link.LINK);
			link.setUserObject(competence);
			toolVC.put(name, link);
			links.add(name);
		}

		@Override
		protected void doDispose() {
			//
		}
		
		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(source instanceof Link) {
				Link link = (Link)source;
				if(link.getUserObject() instanceof TaxonomyCompetenceTypes) {
					addCompetencesCallout.deactivate();
					cleanUp();
					doSearchUsersToAdd(ureq, (TaxonomyCompetenceTypes)link.getUserObject());
				}
			}
		}
	}
}