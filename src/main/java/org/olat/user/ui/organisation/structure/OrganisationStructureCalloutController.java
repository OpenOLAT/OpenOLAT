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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationWithParents;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Organisation;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: Mai 07, 2025
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OrganisationStructureCalloutController extends BasicController {

	private final VelocityContainer vc;
	private final boolean collapseUnrelated;

	@Autowired
	private OrganisationService organisationService;

	public OrganisationStructureCalloutController(UserRequest ureq, WindowControl wControl,
												  List<Organisation> activeOrganisations, boolean collapseUnrelated) {
		super(ureq, wControl);
		this.collapseUnrelated = collapseUnrelated;
		vc = createVelocityContainer("org_structure");
		buildAndShowTree(activeOrganisations);
		putInitialPanel(vc);
	}

	private void buildAndShowTree(List<Organisation> activeOrgs) {
		List<OrgNode> roots = new ArrayList<>();
		
		Map<Long,OrgNode> map = new LinkedHashMap<>();
		for (OrganisationWithParents orgWithParents : organisationService.getOrderedTreeOrganisationsWithParents()) {
			OrgNode orgNode = new OrgNode(orgWithParents.getOrganisation());
			map.put(orgWithParents.getKey(), orgNode);
			if (orgWithParents.getParents() == null || orgWithParents.getParents().isEmpty()) {
				roots.add(orgNode);
			} else {
				OrgNode parentNode = map.get(orgWithParents.getOrganisation().getParent().getKey());
				orgNode.setParent(parentNode);
				parentNode.getChildren().add(orgNode);
			}
		}

		for (Organisation act : activeOrgs) {
			OrgNode n = map.get(act.getKey());
			if (n != null) {
				n.setActive(true);
			}
		}

		if (collapseUnrelated) {
			for (OrgNode n : map.values()) {
				n.setExpanded(false);
			}

			for (Organisation act : activeOrgs) {
				OrgNode leaf = map.get(act.getKey());
				while (leaf != null) {
					leaf.setExpanded(true);
					leaf = leaf.getParent();
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
		private boolean expanded = true;
		private OrgNode parent;
		private boolean active;

		public OrgNode(Organisation organisation) {
			this.organisation = organisation;
		}

		public Organisation getOrganisation() { return organisation; }
		public List<OrgNode> getChildren() { return children; }
		public boolean isActive() { return active; }
		public void setActive(boolean active) { this.active = active; }
		public void setExpanded(boolean expanded) {
			this.expanded = expanded;
		}
		public boolean isExpanded() {
			return expanded;
		}

		public OrgNode getParent() {
			return parent;
		}

		public void setParent(OrgNode parent) {
			this.parent = parent;
		}
	}
}