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

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.BreadcrumbPanelAware;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.docpool.DocumentPoolModule;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyInfos;
import org.olat.modules.taxonomy.ui.TaxonomyListDataModel.TaxonomyCols;
import org.olat.repository.RepositoryModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyListAdminController extends FormBasicController implements FlexiTableComponentDelegate, Activateable2, BreadcrumbPanelAware {
	
	private FlexiTableElement tableEl;
	private TaxonomyListDataModel model;
	private BreadcrumbPanel stackPanel;
	private FormLink createTaxonomyButton;
	
	private CloseableModalController cmc;
	private TaxonomyOverviewController taxonomyCtrl;
	private EditTaxonomyController editTaxonomyCtrl;
	
	private int counter;
	
	@Autowired
	private DocumentPoolModule docPoolModule;
	@Autowired
	private QuestionPoolModule questionPoolModule;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private PortfolioV2Module portfolioModule;
	
	public TaxonomyListAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "taxonomy_list");
		initForm(ureq);
		loadModel();
	}

	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		this.stackPanel = stackPanel;
		if(taxonomyCtrl != null) {
			taxonomyCtrl.setBreadcrumbPanel(stackPanel);
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		createTaxonomyButton = uifactory.addFormLink("create.taxonomy", formLayout, Link.BUTTON);

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, TaxonomyCols.key, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TaxonomyCols.displayName, "select"));

		model = new TaxonomyListDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		tableEl.setSearchEnabled(false);
		tableEl.setCustomizeColumns(false);
		tableEl.setElementCssClass("o_taxonomy_listing");
		tableEl.setEmtpyTableMessageKey("table.taxonomy.empty");
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setPageSize(24);
		VelocityContainer row = createVelocityContainer("taxonomy_row");
		row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		tableEl.setRowRenderer(row, this);
		tableEl.setCssDelegate(new TaxonomyCssDelegate());
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		TaxonomyRow elRow = model.getObject(row);
		List<Component> components = new ArrayList<>(2);
		if(elRow.getOpenLink() != null) {
			components.add(elRow.getOpenLink().getComponent());
		}
		if(elRow.getRepoLink() != null) {
			components.add(elRow.getRepoLink().getComponent());
		}
		if(elRow.getQPoolLink() != null) {
			components.add(elRow.getQPoolLink().getComponent());
		}
		if(elRow.getDocPoolLink() != null) {
			components.add(elRow.getDocPoolLink().getComponent());
		}
		if(elRow.getEPortfolioLink() != null) {
			components.add(elRow.getEPortfolioLink().getComponent());
		}
		return components;
	}
	
	private void loadModel() {
		List<TaxonomyInfos> taxonomyList = taxonomyService.getTaxonomyInfosList();
		List<TaxonomyRow> rows = new ArrayList<>(taxonomyList.size());
		for(TaxonomyInfos taxonomy:taxonomyList) {
			rows.add(forgeTaxonomyRow(taxonomy));
		}
		model.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private TaxonomyRow forgeTaxonomyRow(TaxonomyInfos taxonomy) {
		String openLinkId = "open_" + (++counter);
		FormLink openLink = uifactory.addFormLink(openLinkId, "open.taxonomy", "open.taxonomy", null, flc, Link.LINK);
		openLink.setIconRightCSS("o_icon o_icon_start");

		boolean repoEnabled = taxonomy.getKey().toString().equals(repositoryModule.getTaxonomyTreeKey());
		boolean docPoolEnabled = taxonomy.getKey().toString().equals(docPoolModule.getTaxonomyTreeKey());
		boolean qPoolEnabled = taxonomy.getKey().toString().equals(questionPoolModule.getTaxonomyQPoolKey());
		boolean ePortfolioEnabled = portfolioModule.isTaxonomyEnabled(taxonomy.getKey());
		
		String repoLinkId = "dpool_" + (++counter);
		String repoString =  repoEnabled ? translate("taxonomy.infos.enabled") : translate("taxonomy.infos.not.enabled");
		FormLink repoLink = uifactory.addFormLink(repoLinkId, "open.repo", repoString, null, flc, Link.LINK | Link.NONTRANSLATED);
		
		String docPoolLinkId = "dpool_" + (++counter);
		String docPoolString =  docPoolEnabled ? translate("taxonomy.infos.enabled") : translate("taxonomy.infos.not.enabled");
		FormLink docPoolLink = uifactory.addFormLink(docPoolLinkId, "open.docpool", docPoolString, null, flc, Link.LINK | Link.NONTRANSLATED);

		String qPoolLinkId = "qpool_" + (++counter);
		String qPoolString =  qPoolEnabled ? translate("taxonomy.infos.enabled") : translate("taxonomy.infos.not.enabled");
		FormLink qPoolLink = uifactory.addFormLink(qPoolLinkId, "open.qpool", qPoolString, null, flc, Link.LINK | Link.NONTRANSLATED);
		
		String ePortfolioLinkId = "ePortfolio_" + (++counter);
		String ePortfolioString = ePortfolioEnabled ? translate("taxonomy.infos.enabled") : translate("taxonomy.infos.not.enabled");
		FormLink ePortfolioLink = uifactory.addFormLink(ePortfolioLinkId, "open.eportfolio", ePortfolioString, null, flc, Link.LINK | Link.NONTRANSLATED);
		
		TaxonomyRow row = new TaxonomyRow(taxonomy, docPoolEnabled, qPoolEnabled, openLink, repoLink, docPoolLink, qPoolLink, ePortfolioLink);
		openLink.setUserObject(row);
		repoLink.setUserObject(row);
		docPoolLink.setUserObject(row);
		qPoolLink.setUserObject(row);
		ePortfolioLink.setUserObject(row);
		return row;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Taxonomy".equalsIgnoreCase(type)) {
			List<ContextEntry> subEntries = entries.subList(1, entries.size());
			Long taxonomyKey = entries.get(0).getOLATResourceable().getResourceableId();
			if(taxonomyCtrl != null && taxonomyKey.equals(taxonomyCtrl.getTaxonomy().getKey())) {
				taxonomyCtrl.activate(ureq, subEntries, entries.get(0).getTransientState());
			} else {
				List<TaxonomyRow> rows = model.getObjects();
				for(TaxonomyRow row:rows) {
					if(taxonomyKey.equals(row.getKey())) {
						doOpenTaxonomy(ureq, row).activate(ureq, subEntries, entries.get(0).getTransientState());
						break;
					}
				}
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(createTaxonomyButton == source) {
			doCreateTaxonomy(ureq);
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("open.taxonomy".equals(link.getCmd())) {
				doOpenTaxonomy(ureq, (TaxonomyRow)link.getUserObject());
			} else if("open.repo".equals(link.getCmd())) {
				doOpenRepositoryAdmin(ureq);
			} else if("open.docpool".equals(link.getCmd())) {
				doOpenDocumentPoolAdmin(ureq);
			} else if("open.qpool".equals(link.getCmd())) {
				doOpenQuestionPoolAdmin(ureq);
			} else if("open.eportfolio".equals(link.getCmd())) {
				doOpenEPortfolio(ureq);
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editTaxonomyCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(taxonomyCtrl == source) {
			if(event == Event.CANCELLED_EVENT) {
				stackPanel.popController(taxonomyCtrl);
				loadModel();
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editTaxonomyCtrl);
		removeAsListenerAndDispose(cmc);
		editTaxonomyCtrl = null;
		cmc = null;
	}
	
	private void doOpenRepositoryAdmin(UserRequest ureq) {
		String businessPath = "[AdminSite:0][repositoryAdmin:0]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doOpenDocumentPoolAdmin(UserRequest ureq) {
		String businessPath = "[AdminSite:0][docpool:0]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doOpenQuestionPoolAdmin(UserRequest ureq) {
		String businessPath = "[AdminSite:0][qpool:0]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private void doOpenEPortfolio(UserRequest ureq) {
		String businessPath = "[AdminSite:0][portfolio:0]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

	private TaxonomyOverviewController doOpenTaxonomy(UserRequest ureq, TaxonomyRow row) {
		removeAsListenerAndDispose(taxonomyCtrl);
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Taxonomy", row.getKey());
		WindowControl bwControl = addToHistory(ureq, ores, null);
		Taxonomy taxonomy = taxonomyService.getTaxonomy(row);
		taxonomyCtrl = new TaxonomyOverviewController(ureq, bwControl, taxonomy);
		taxonomyCtrl.setBreadcrumbPanel(stackPanel);
		listenTo(taxonomyCtrl);
		
		stackPanel.changeDisplayname(translate("admin.menu.title"));
		stackPanel.pushController(row.getDisplayName(), taxonomyCtrl);
		return taxonomyCtrl;
	}
	
	private void doCreateTaxonomy(UserRequest ureq) {
		if(guardModalController(editTaxonomyCtrl)) return;
		
		editTaxonomyCtrl = new EditTaxonomyController(ureq, getWindowControl(), null);
		listenTo(editTaxonomyCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", editTaxonomyCtrl.getInitialComponent(), true, translate("create.taxonomy"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private static class TaxonomyCssDelegate extends DefaultFlexiTableCssDelegate {
		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			return "o_taxonomy_row";
		}
	}
}
