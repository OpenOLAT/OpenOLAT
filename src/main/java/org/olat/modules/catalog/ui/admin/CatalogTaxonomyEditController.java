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
package org.olat.modules.catalog.ui.admin;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.modules.catalog.CatalogSecurityCallback;
import org.olat.modules.catalog.ui.CatalogTaxonomySelectionController;
import org.olat.modules.catalog.ui.CatalogTaxonomySelectionController.TaxonomySelectionEvent;
import org.olat.modules.catalog.ui.CatalogV2UIFactory;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.ui.TaxonomyTreeTableController;
import org.olat.repository.RepositoryModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 1 Sep 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogTaxonomyEditController extends BasicController {
	
	public static final Event OPEN_ADMIN_EVENT = new Event("open.admin");
	
	private TooledStackedPanel stackPanel;
	private VelocityContainer mainVC;
	private Link backLink;
	private Link openAdminLink;

	private CatalogTaxonomySelectionController taxonomySelectionCtrl;
	private TaxonomyTreeTableController taxonomyCtrl;
	
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private TaxonomyService taxonomyService;

	public CatalogTaxonomyEditController(UserRequest ureq, WindowControl wControl, CatalogSecurityCallback secCallback) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(CatalogV2UIFactory.class, getLocale(), getTranslator()));
		
		mainVC = createVelocityContainer("taxonomy_edit");
		
		backLink = LinkFactory.createLinkBack(mainVC, this);
		if (secCallback.canEditCatalogAdministration()) {
			openAdminLink = LinkFactory.createLink("open.admin", mainVC, this);
		}
		
		stackPanel = new TooledStackedPanel("taxonomy", getTranslator(), this);
		stackPanel.setToolbarEnabled(false);
		mainVC.put("stack", stackPanel);
		
		Set<Long> taxonomyKeys = repositoryModule.getTaxonomyRefs().stream()
				.map(TaxonomyRef::getKey)
				.collect(Collectors.toSet());
		List<Taxonomy> taxonomies = taxonomyService.getTaxonomyList().stream()
				.filter(taxonomy -> taxonomyKeys.contains(taxonomy.getKey()))
				.sorted((t1, t2) -> t1.getDisplayName().compareTo(t2.getDisplayName()))
				.collect(Collectors.toList());
		
		if (taxonomies.size() > 1) {
			taxonomySelectionCtrl = new CatalogTaxonomySelectionController(ureq, wControl, taxonomies);
			listenTo(taxonomySelectionCtrl);
			mainVC.put("taxonomy.selection", taxonomySelectionCtrl.getInitialComponent());
		}
		
		if (!taxonomies.isEmpty()) {
			doSelectTaxonomy(ureq, taxonomies.get(0));
		}
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == backLink) {
			fireEvent(ureq, Event.BACK_EVENT);
		} else if (source == openAdminLink) {
			fireEvent(ureq, OPEN_ADMIN_EVENT);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == taxonomySelectionCtrl) {
			if (event instanceof TaxonomySelectionEvent) {
				doSelectTaxonomy(ureq, ((TaxonomySelectionEvent)event).getTaxonomy());
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void doDispose() {
		super.doDispose();
		stackPanel.removeListener(this);
	}

	private void doSelectTaxonomy(UserRequest ureq, Taxonomy taxonomy) {
		removeAsListenerAndDispose(taxonomyCtrl);
		
		taxonomyCtrl = new TaxonomyTreeTableController(ureq, getWindowControl(), taxonomy);
		taxonomyCtrl.setBreadcrumbPanel(stackPanel);
		stackPanel.pushController(taxonomy.getDisplayName(), taxonomyCtrl);
	}

}
