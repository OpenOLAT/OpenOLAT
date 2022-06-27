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
package org.olat.modules.catalog.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * 
 * Initial date: 13 Jun 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogTaxonomyHeaderController extends BasicController {

	public CatalogTaxonomyHeaderController(UserRequest ureq, WindowControl wControl, TaxonomyLevel taxonomyLevel) {
		super(ureq, wControl);
		VelocityContainer mainVC = createVelocityContainer("header_taxonomy");
		
		mainVC.contextPut("displayName", taxonomyLevel.getDisplayName());
		if (taxonomyLevel.getType() != null) {
			mainVC.contextPut("typeDisplayName", taxonomyLevel.getType().getDisplayName());
		}
		if (taxonomyLevel.getType() != null) {
			mainVC.contextPut("cssClass", taxonomyLevel.getType().getCssClass());
		}
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if ("select".equals(event.getCommand())) {
			String key = ureq.getParameter("key");
			fireEvent(ureq, new OpenTaxonomyEvent(Long.valueOf(key)));
		}
	}

}
