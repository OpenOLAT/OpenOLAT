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
package org.olat.basesecurity.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.OrganisationService;
import org.olat.basesecurity.model.OrganisationChangeEvent;
import org.olat.basesecurity.model.OrganisationWithParents;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeNodeComparator;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Cache and calculate an ordered list of the organisations tree. The order
 * is by structure, but default organisation first, levels are after alphabetically
 * ordered.
 * 
 * Initial date: 7 sept. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class OrganisationOrderedTreeCache implements GenericEventListener, InitializingBean, DisposableBean {
	
	private static final Logger log = Tracing.createLoggerFor(OrganisationOrderedTreeCache.class);
	
	@Autowired
	private OrganisationService organisationService;
	
	private CoordinatorManager coordinatorManager;
	private List<OrganisationWithParents> orderedOrganisationsWithParents;
	
	@Autowired
	public OrganisationOrderedTreeCache(CoordinatorManager coordinatorManager) {
		this.coordinatorManager = coordinatorManager;
	}
	
	@Override
	public void event(Event event) {
		if(event instanceof OrganisationChangeEvent) {
			orderedOrganisationsWithParents = null;
		}
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		coordinatorManager.getCoordinator().getEventBus()
			.registerFor(this, null, OrganisationService.ORGANISATIONS_CHANGED_EVENT_CHANNEL);	
		coordinatorManager.getCoordinator().getEventBus()
			.registerFor(this, null, OresHelper.createOLATResourceableInstance(Organisation.class, Long.valueOf(0l)));
	}

	@Override
	public void destroy() throws Exception {
		coordinatorManager.getCoordinator().getEventBus()
			.deregisterFor(this, OrganisationService.ORGANISATIONS_CHANGED_EVENT_CHANNEL);
	}
	
	public List<OrganisationWithParents> getOrderedOrganisationsWithParents() {
		List<OrganisationWithParents> organisations = orderedOrganisationsWithParents;
		if(organisations == null) {
			organisations = loadOrganisations();
			orderedOrganisationsWithParents = organisations;
		}
		return organisations;
	}
	
	private List<OrganisationWithParents> loadOrganisations() {
		Organisation defaultOrganisation = organisationService.getDefaultOrganisation();
		List<Organisation> organisations = organisationService.getOrganisations();
		Map<Long,OrganisationNode> organisationsMap = organisations.stream()
				.collect(Collectors.toMap(Organisation::getKey, OrganisationNode::new, (u, v) -> u));
		
		List<OrganisationNode> organisationsNodes = new ArrayList<>(organisationsMap.values());
		for(OrganisationNode organisation:organisationsNodes) {
			List<OrganisationRef> parentLines = organisation.getOrganisation().getParentLine();
			if(parentLines != null && !parentLines.isEmpty()) {
				OrganisationRef parent = parentLines.get(parentLines.size() - 1);
				organisation.setParent(organisationsMap.get(parent.getKey()));
			}
		}
		
		try {
			Collections.sort(organisationsNodes, new OrganisationTreeComparator(defaultOrganisation));
		} catch (Exception e) {
			log.error("Cannot sort organisations", e);
		}
		
		int numOfOrganisations = organisationsNodes.size();
		List<OrganisationWithParents> rows = new ArrayList<>(numOfOrganisations);
		for(int i=0; i<numOfOrganisations; i++) {
			OrganisationNode node = organisationsNodes.get(i);
			List<OrganisationRef> parentLines = node.getOrganisation().getParentLine();
			List<Organisation> parents = new ArrayList<>(parentLines.size());
			for(OrganisationRef parent:parentLines) {
				OrganisationNode n = organisationsMap.get(parent.getKey());
				if(n != null) {
					parents.add(n.getOrganisation());
				}
			}
			rows.add(new OrganisationWithParents(node.getOrganisation(), List.copyOf(parents), i));
		}
		return rows;
	}
	
	private static class OrganisationNode implements FlexiTreeTableNode {
		
		private final Organisation organisation;
		private OrganisationNode parent;
		
		public OrganisationNode(Organisation organisation) {
			this.organisation = organisation;
		}

		public Organisation getOrganisation() {
			return organisation;
		}

		@Override
		public String getCrump() {
			return organisation.getDisplayName();
		}

		@Override
		public OrganisationNode getParent() {
			return parent;
		}
		
		public void setParent(OrganisationNode parent ) {
			this.parent = parent;
		}

		@Override
		public int hashCode() {
			return organisation.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof OrganisationNode) {
				OrganisationNode n = (OrganisationNode)obj;
				return organisation.equals(n.getOrganisation());
			}
			return false;
		}
	}
	
	private class OrganisationTreeComparator extends FlexiTreeNodeComparator {
		
		private final Organisation defaultOrganisation;
		
		public OrganisationTreeComparator(Organisation defaultOrganisation) {
			this.defaultOrganisation = defaultOrganisation;
		}
		
		@Override
		protected int compareNodes(FlexiTreeTableNode o1, FlexiTreeTableNode o2) {
			Organisation org1 = ((OrganisationNode)o1).getOrganisation();
			Organisation org2 = ((OrganisationNode)o2).getOrganisation();
			
			if(org1.equals(defaultOrganisation) && org2.equals(defaultOrganisation)) {
				return 0;
			}
			if(org1.equals(defaultOrganisation)) {
				return -1;
			}
			if(org2.equals(defaultOrganisation)) {
				return 1;
			}
			
			
			String c1 = org1.getDisplayName();
			String c2 = org2.getDisplayName();
			int c = 0;
			if(c1 == null || c2 == null) {
				c = compareNullObjects(c1, c2);
			} else {
				c = c1.compareTo(c2);
			}
			
			if(c == 0) {
				c = org1.getKey().compareTo(org2.getKey());
			}
			
			return c; 
		}
	}
}
