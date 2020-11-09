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

import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.BreadcrumbPanelAware;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyCompetenceAuditLog;
import org.olat.modules.taxonomy.TaxonomyCompetenceTypes;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.IdentityCompetenceTableModel.IdCompetenceCols;
import org.olat.modules.taxonomy.ui.component.TaxonomyCompetenceTypeRenderer;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 Oct 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityCompetencesController extends FormBasicController implements BreadcrumbPanelAware {
	
	private FlexiTableElement tableEl;
	private IdentityCompetenceTableModel tableModel;
	private FormLink addManageButton;
	private FormLink addTeachButton;
	private FormLink addHaveButton;
	private FormLink addTargetButton;
	
	private CloseableModalController cmc;
	private SelectTaxonomyLevelController levelsSearchCtrl;
	private DialogBoxController removeCompentenceConfirmationCtrl;

	private final boolean canModify;
	private final Identity assessedIdentity;

	@Autowired
	private UserManager userManager;
	@Autowired
	private TaxonomyService taxonomyService;
	
	public IdentityCompetencesController(UserRequest ureq, WindowControl wControl, Identity assessedIdentity, boolean canModify) {
		super(ureq, wControl, "identity_competences");
		this.canModify = canModify;
		this.assessedIdentity = assessedIdentity;
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));

		initForm(ureq);
		loadModel();
	}
	
	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		//
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(canModify) {
			addManageButton = uifactory.addFormLink("add.competence.manage", formLayout, Link.BUTTON);
			addTeachButton = uifactory.addFormLink("add.competence.teach", formLayout, Link.BUTTON);
			addHaveButton = uifactory.addFormLink("add.competence.have", formLayout, Link.BUTTON);
			addTargetButton = uifactory.addFormLink("add.competence.target", formLayout, Link.BUTTON);
		}

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, IdCompetenceCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, IdCompetenceCols.taxonomyIdentifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdCompetenceCols.taxonomyDisplayName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, IdCompetenceCols.taxonomyExternalId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, IdCompetenceCols.taxonomyLevelIdentifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdCompetenceCols.taxonomyLevelDisplayName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, IdCompetenceCols.taxonomyLevelExternalId));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdCompetenceCols.taxonomyLevelType));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdCompetenceCols.type, new TaxonomyCompetenceTypeRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdCompetenceCols.expiration, new DateFlexiCellRenderer(getLocale())));
		if(canModify) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("remove", IdCompetenceCols.remove.ordinal(), "remove",
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("remove"), "remove"), null)));
		}
		tableModel = new IdentityCompetenceTableModel(columnsModel); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "tax-identity-competences");
	}
	
	private void loadModel() {
		List<TaxonomyCompetence> competences = taxonomyService.getTaxonomyCompetences(assessedIdentity);
		List<IdentityCompetenceRow> rows = competences.stream()
				.map(IdentityCompetenceRow::new)
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
		if(addManageButton == source) {
			doSelectTaxonomyLevelsToAdd(ureq, TaxonomyCompetenceTypes.manage);
		} else if(addTeachButton == source) {
			doSelectTaxonomyLevelsToAdd(ureq, TaxonomyCompetenceTypes.teach);
		} else if(addHaveButton == source) {
			doSelectTaxonomyLevelsToAdd(ureq, TaxonomyCompetenceTypes.have);
		} else if(addTargetButton == source) {
			doSelectTaxonomyLevelsToAdd(ureq, TaxonomyCompetenceTypes.target);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				IdentityCompetenceRow row = tableModel.getObject(se.getIndex());
				if("remove".equals(se.getCommand())) {
					doConfirmRemove(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(removeCompentenceConfirmationCtrl == source) {
			if (DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				IdentityCompetenceRow row = (IdentityCompetenceRow)removeCompentenceConfirmationCtrl.getUserObject();
				doRemoveCompetence(row);
			}
		} else if(levelsSearchCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doAddTaxonomyLevelsAsCompetence(levelsSearchCtrl.getSelectedTaxonomyLevel(), levelsSearchCtrl.getCompetenceType());
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(levelsSearchCtrl);
		removeAsListenerAndDispose(cmc);
		levelsSearchCtrl = null;
		cmc = null;
	}
	
	private void doSelectTaxonomyLevelsToAdd(UserRequest ureq, TaxonomyCompetenceTypes comptenceType) {
		if(guardModalController(levelsSearchCtrl)) return;
		
		levelsSearchCtrl = new SelectTaxonomyLevelController(ureq, getWindowControl(), comptenceType);
		listenTo(levelsSearchCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), levelsSearchCtrl.getInitialComponent(),
				true, translate("add.competence." + comptenceType.name()));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddTaxonomyLevelsAsCompetence(TaxonomyLevel selectedLevel, TaxonomyCompetenceTypes competenceType) {
		if(selectedLevel == null) {
			showWarning("warning.atleastone.level.competence");
		} else {
			boolean found = false;
			List<TaxonomyCompetence> currentCompetences = taxonomyService.getTaxonomyCompetences(assessedIdentity, competenceType);
			for(TaxonomyCompetence currentCompetence:currentCompetences) {
				if(selectedLevel.equals(currentCompetence.getTaxonomyLevel())) {
					found = true;
				}
			}
			
			if(!found) {
				TaxonomyLevel taxonomyLevel = taxonomyService.getTaxonomyLevel(selectedLevel);
				Taxonomy taxonomy = taxonomyLevel.getTaxonomy();
				TaxonomyCompetence competence = taxonomyService.addTaxonomyLevelCompetences(taxonomyLevel, assessedIdentity, competenceType, null);
				String after = taxonomyService.toAuditXml(competence);
				taxonomyService.auditLog(TaxonomyCompetenceAuditLog.Action.addCompetence, null, after, null, taxonomy, competence, assessedIdentity, getIdentity());
			}
			
			loadModel();
			tableEl.reset(true, true, true);
		}
	}
	
	private void doConfirmRemove(UserRequest ureq, IdentityCompetenceRow row) {
		String title = translate("remove");
		String competence = translate(row.getCompetenceType().name());
		String levelDisplayName = StringHelper.escapeHtml(row.getTaxonomyLevel().getDisplayName());
		String text = translate("confirmation.remove.competence", new String[] { competence, levelDisplayName });
		removeCompentenceConfirmationCtrl = activateOkCancelDialog(ureq, title, text, removeCompentenceConfirmationCtrl);
		removeCompentenceConfirmationCtrl.setUserObject(row);
	}
	
	private void doRemoveCompetence(IdentityCompetenceRow row) {
		Taxonomy taxonomy = row.getTaxonomy();
		TaxonomyCompetence competence = row.getCompetence();
		competence = taxonomyService.getTaxonomyCompetence(competence);
		if(competence != null) {
			String before = taxonomyService.toAuditXml(competence);
			taxonomyService.removeTaxonomyLevelCompetence(competence);
			taxonomyService.auditLog(TaxonomyCompetenceAuditLog.Action.removeCompetence, before, null, null, taxonomy, competence, assessedIdentity, getIdentity());
		}

		loadModel();
		tableEl.reset(true, true, true);
		
		String competenceTypeName = translate(row.getCompetenceType().name());
		String levelDisplayName = StringHelper.escapeHtml(row.getTaxonomyLevel().getDisplayName());
		showInfo("confirm.removed.competence", new String[] { competenceTypeName, levelDisplayName });
	}
}