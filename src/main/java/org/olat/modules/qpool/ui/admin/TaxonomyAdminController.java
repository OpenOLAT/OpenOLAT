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
package org.olat.modules.qpool.ui.admin;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.BreadcrumbPanelAware;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.ui.TaxonomyTreeTableController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyAdminController extends BasicController implements BreadcrumbPanelAware {

	private final TaxonomyTreeTableController taxonomyCtrl;
	
	@Autowired
	private QPoolService qpoolService;
	
	public TaxonomyAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(QuestionsController.class, ureq.getLocale()));
		
		Taxonomy taxonomy = qpoolService.getQPoolTaxonomy();
		VelocityContainer mainVC = createVelocityContainer("admin_study_fields");
		taxonomyCtrl = new TaxonomyTreeTableController(ureq, getWindowControl(), taxonomy);
		listenTo(taxonomyCtrl);
		mainVC.put("taxonomy", taxonomyCtrl.getInitialComponent());
		putInitialPanel(mainVC);
	}

	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		taxonomyCtrl.setBreadcrumbPanel(stackPanel);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}