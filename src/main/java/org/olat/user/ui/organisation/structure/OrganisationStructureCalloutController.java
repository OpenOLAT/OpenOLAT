/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.user.ui.organisation.structure;

import java.util.*;
import org.olat.basesecurity.OrganisationService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.*;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.id.Organisation;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Mai 07, 2025
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OrganisationStructureCalloutController extends BasicController {

	private final VelocityContainer vc;

	@Autowired
	private OrganisationService organisationService;

	public OrganisationStructureCalloutController(UserRequest ureq, WindowControl wControl,
												  List<Organisation> activeOrganisations) {
		super(ureq, wControl);
		vc = createVelocityContainer("org_structure");
		buildAndShowTree(activeOrganisations);
		putInitialPanel(vc);
	}

	private void buildAndShowTree(List<Organisation> activeOrgs) {
		List<Organisation> all = organisationService.getOrganisations();

		Map<Long,OrgNode> map = new LinkedHashMap<>();
		for (Organisation o : all) {
			map.put(o.getKey(), new OrgNode(o));
		}

		List<OrgNode> roots = new ArrayList<>();
		for (OrgNode node : map.values()) {
			Organisation o = node.getOrganisation();
			if (o.getParent() != null && map.containsKey(o.getParent().getKey())) {
				map.get(o.getParent().getKey()).getChildren().add(node);
			} else {
				roots.add(node);
			}
		}
		// Mark the “active” org
		if (activeOrgs != null) {
			for (Organisation o : activeOrgs) {
				OrgNode node = map.get(o.getKey());
				if (node != null) {
					node.setActive(true);
				}
			}
		}

		vc.contextPut("roots", roots);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	public static class OrgNode {
		private final Organisation organisation;
		private final List<OrgNode> children = new ArrayList<>();
		private boolean active;

		public OrgNode(Organisation organisation) {
			this.organisation = organisation;
		}

		public Organisation getOrganisation() { return organisation; }
		public List<OrgNode> getChildren() { return children; }
		public boolean isActive() { return active; }
		public void setActive(boolean active) { this.active = active; }
	}
}