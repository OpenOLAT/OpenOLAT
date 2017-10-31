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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;

/**
 * 
 * Initial date: 18 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyAdminController extends BasicController {

	private final Link configurationLink, taxonomyTreesLink;
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	
	private TaxonomyTreesAdminController treesCtrl;
	private TaxonomyConfigurationAdminController configurationCtrl;
	
	public TaxonomyAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("taxonomy_admin");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		configurationLink = LinkFactory.createLink("admin.configuration", mainVC, this);
		segmentView.addSegment(configurationLink, true);
	
		taxonomyTreesLink = LinkFactory.createLink("admin.taxonomy.trees", mainVC, this);
		segmentView.addSegment(taxonomyTreesLink, false);
		doOpenConfiguration(ureq);
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == configurationLink) {
					doOpenConfiguration(ureq);
				} else if (clickedLink == taxonomyTreesLink){
					doOpenTaxonomyTrees(ureq);
				}
			}
		}
	}
	
	private void doOpenConfiguration(UserRequest ureq) {
		if(configurationCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("configuration", 0l);
			WindowControl bwControl = addToHistory(ureq, ores, null);
			configurationCtrl = new TaxonomyConfigurationAdminController(ureq, bwControl);
			listenTo(configurationCtrl);
		}
		
		mainVC.put("segmentCmp", configurationCtrl.getInitialComponent());
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("configuration", 0l);
		addToHistory(ureq, ores, null);
	}

	private void doOpenTaxonomyTrees(UserRequest ureq) {
		if(treesCtrl == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("trees", 0l);
			WindowControl bwControl = addToHistory(ureq, ores, null);
			treesCtrl = new TaxonomyTreesAdminController(ureq, bwControl);
			listenTo(treesCtrl);
		}
		
		mainVC.put("segmentCmp", treesCtrl.getInitialComponent());
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("trees", 0l);
		addToHistory(ureq, ores, null);
	}
}
