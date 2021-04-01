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
import java.util.List;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.BreadcrumbPanelAware;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyCompetenceAuditLog;
import org.olat.modules.taxonomy.TaxonomyCompetenceLinkLocations;
import org.olat.modules.taxonomy.TaxonomyCompetenceTypes;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.CompetencesOverviewTableModel.CompetencesOverviewCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 19.03.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class CompetencesOverviewController extends FormBasicController implements BreadcrumbPanelAware {
	
	private static final String OPEN_RESOURCE = "open_resource";
	private static final String REMOVE = "remove";
	
	private BreadcrumbPanel stackPanel;
	private Identity assessedIdentity;
	private boolean canModify;
	
	private CompetencesOverviewTableModel tableModel;
	private FlexiTableElement tableEl;
	private CompetencesOverviewTableRow rootCrumb;
	
	private FormLink addManageButton;
	private FormLink addTeachButton;
	private FormLink addHaveButton;
	private FormLink addTargetButton;
	
	private CloseableModalController cmc;
	private SelectTaxonomyLevelController levelsSearchCtrl;
	private DialogBoxController removeCompentenceConfirmationCtrl;

	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private PortfolioService portfolioService;

	public CompetencesOverviewController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, Identity assessedIdentity, boolean canModify) {
		super(ureq, wControl, "identity_competences");
		
		this.stackPanel = stackPanel;
		this.assessedIdentity = assessedIdentity;
		this.canModify = canModify;
		
		initForm(ureq);
		loadModel();	
	}
	
	private void loadModel() {
		List<CompetencesOverviewTableRow> rows = new ArrayList<>();
		
		List<TaxonomyCompetence> competences = taxonomyService.getTaxonomyCompetences(assessedIdentity);
		
		List<TaxonomyLevel> levels = competences.stream()
				.map(competence -> competence.getTaxonomyLevel())
				.distinct()
				.collect(Collectors.toList());
		
		List<Taxonomy> taxonomies = levels.stream()
				.map(level -> level.getTaxonomy())
				.distinct()
				.collect(Collectors.toList());
		
		for (Taxonomy taxonomy : taxonomies) {
			List<TaxonomyLevel> taxonomyLevels = levels.stream()
					.filter(level -> level.getTaxonomy()
					.equals(taxonomy))
					.collect(Collectors.toList());
			
			CompetencesOverviewTableRow taxonomyRow = new CompetencesOverviewTableRow(getTranslator());
			taxonomyRow.setTaxonomy(taxonomy);
			rows.add(taxonomyRow);
			
			for (TaxonomyLevel taxonomyLevel : taxonomyLevels) {
				List<TaxonomyCompetence> taxonomyLevelCompetences = competences.stream()
						.filter(competence -> competence.getTaxonomyLevel().equals(taxonomyLevel))
						.collect(Collectors.toList());
				
				CompetencesOverviewTableRow levelRow = new CompetencesOverviewTableRow(getTranslator());
				levelRow.setTaxonomy(taxonomy);
				levelRow.setLevel(taxonomyLevel);
				levelRow.setParent(taxonomyRow);
				rows.add(levelRow);
				
				for (TaxonomyCompetence competence : taxonomyLevelCompetences) {
					CompetencesOverviewTableRow competenceRow = new CompetencesOverviewTableRow(getTranslator());
					competenceRow.setTaxonomy(taxonomy);
					competenceRow.setLevel(taxonomyLevel);
					competenceRow.setParent(levelRow);
					competenceRow.setCompetence(competence);
					if (competence.getLinkLocation().equals(TaxonomyCompetenceLinkLocations.PORTFOLIO)) {
						competenceRow.setPortfolioLocation(portfolioService.getPageToCompetence(competence));
					}
					rows.add(competenceRow);
				}
			}
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(false, true, true);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(canModify) {
			addManageButton = uifactory.addFormLink("add.competence.manage", formLayout, Link.BUTTON);
			addTeachButton = uifactory.addFormLink("add.competence.teach", formLayout, Link.BUTTON);
			addHaveButton = uifactory.addFormLink("add.competence.have", formLayout, Link.BUTTON);
			addTargetButton = uifactory.addFormLink("add.competence.target", formLayout, Link.BUTTON);
		}
		
		FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CompetencesOverviewCols.key));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CompetencesOverviewCols.competence, new TreeNodeFlexiCellRenderer(true)));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CompetencesOverviewCols.resource, OPEN_RESOURCE));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CompetencesOverviewCols.type));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CompetencesOverviewCols.taxonomyDisplayName));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CompetencesOverviewCols.taxonomyIdentifier));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CompetencesOverviewCols.taxonomyExternalId));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CompetencesOverviewCols.taxonomyLevelDisplayName));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CompetencesOverviewCols.taxonomyLevelIdentifier));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CompetencesOverviewCols.taxonomyLevelExternalId));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CompetencesOverviewCols.taxonomyLevelType));
		columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CompetencesOverviewCols.expiration));
		if(canModify) {
			columnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CompetencesOverviewCols.remove, REMOVE,
				new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("remove"), "remove"), null)));
		}
		
		tableModel = new CompetencesOverviewTableModel(columnModel);
		
		tableEl = uifactory.addTableElement(getWindowControl(), "competences_overview", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setAndLoadPersistedPreferences(ureq, "competences_overview");
		tableEl.setSearchEnabled(true);
		tableEl.setEmptyTableSettings("competences.empty.table", true);
		
		// Set rootcrumb
		rootCrumb = new CompetencesOverviewTableRow(getTranslator());
		tableEl.setRootCrumb(rootCrumb);
		
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
		} else if (source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				CompetencesOverviewTableRow row = tableModel.getObject(se.getIndex());
					
				if(REMOVE.equals(se.getCommand())) {
					doConfirmRemove(ureq, row);
				} else if (OPEN_RESOURCE.equals(se.getCommand())) {
					if (row.getPortfolioLocation() != null) {
						// Open the portfolio
						launch(ureq, row.getPortfolioLocation());
					} else {
						// External competences or competences in general without linked resource can not be openend
						showInfo("competence.without.link");
					}
				}
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(removeCompentenceConfirmationCtrl == source) {
			if (DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				CompetencesOverviewTableRow row = (CompetencesOverviewTableRow)removeCompentenceConfirmationCtrl.getUserObject();
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
	
	private void launch(UserRequest ureq, Page page) {
		String identityKey = assessedIdentity.getKey().toString();
		String binderKey = page.getSection().getBinder().getKey().toString();
		String pageKey = page.getKey().toString();
		String businessPath = "[HomeSite:" + identityKey + "][PortfolioV2:0][MyBinders:0][Binder:" + binderKey + "][Toc:0][Entry:" + pageKey + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
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
	
	private void doConfirmRemove(UserRequest ureq, CompetencesOverviewTableRow row) {
		String title = translate("remove");
		String competence = translate(row.getCompetence().getCompetenceType().name());
		String levelDisplayName = StringHelper.escapeHtml(row.getLevel().getDisplayName());
		String text = translate("confirmation.remove.competence", new String[] { competence, levelDisplayName });
		removeCompentenceConfirmationCtrl = activateOkCancelDialog(ureq, title, text, removeCompentenceConfirmationCtrl);
		removeCompentenceConfirmationCtrl.setUserObject(row);
	}
	
	private void doRemoveCompetence(CompetencesOverviewTableRow row) {
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
		
		String competenceTypeName = translate(row.getCompetence().getCompetenceType().name());
		String levelDisplayName = StringHelper.escapeHtml(row.getTaxonomyLevelDisplayName());
		showInfo("confirm.removed.competence", new String[] { competenceTypeName, levelDisplayName });
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// Nothing to submit
	}

	@Override
	protected void doDispose() {
		if(stackPanel != null) {
			stackPanel.removeListener(this);
		}		
	}

	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		this.stackPanel = stackPanel;
	}
}
