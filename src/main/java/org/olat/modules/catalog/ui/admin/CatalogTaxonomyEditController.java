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
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.catalog.CatalogSecurityCallback;
import org.olat.modules.catalog.ui.CatalogV2UIFactory;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomySecurityCallback;
import org.olat.modules.taxonomy.ui.TaxonomyOverviewController;
import org.olat.modules.taxonomy.ui.TaxonomySelectionController;
import org.olat.modules.taxonomy.ui.TaxonomySelectionController.TaxonomySelectionEvent;
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
	private Link openAdminLink;

	private TaxonomySelectionController taxonomySelectionCtrl;
	private TaxonomyOverviewController taxonomyCtrl;
	
	@Autowired
	private RepositoryModule repositoryModule;

	public CatalogTaxonomyEditController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			CatalogSecurityCallback secCallback) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(CatalogV2UIFactory.class, getLocale(), getTranslator()));
		this.stackPanel = stackPanel;
		
		mainVC = createVelocityContainer("taxonomy_edit");
		
		if (secCallback.canEditCatalogAdministration()) {
			openAdminLink = LinkFactory.createLink("open.admin", mainVC, this);
			openAdminLink.setIconLeftCSS("o_icon o_icon_external_link");
		}
		
		taxonomySelectionCtrl = new TaxonomySelectionController(ureq, wControl, repositoryModule.getTaxonomyRefs());
		listenTo(taxonomySelectionCtrl);
		mainVC.put("taxonomy.selection", taxonomySelectionCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == openAdminLink) {
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
		
		Roles roles = ureq.getUserSession().getRoles();
		TaxonomySecurityCallback secCallback = roles != null && roles.isSystemAdmin()
				? TaxonomySecurityCallback.FULL
				: new CatalogTaxonomySecurityCallback(taxonomy, getIdentity());
		taxonomyCtrl = new TaxonomyOverviewController(ureq, getWindowControl(),
				secCallback, taxonomy);
		taxonomyCtrl.setBreadcrumbPanel(stackPanel);
		stackPanel.pushController(StringHelper.escapeHtml(taxonomy.getDisplayName()), taxonomyCtrl);
	}

}
