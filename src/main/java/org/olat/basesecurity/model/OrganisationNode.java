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
package org.olat.basesecurity.model;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;

/**
 * 
 * Initial date: 3 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationNode {
	
	private Organisation organisation;
	private OrganisationNode parentNode;
	private final List<OrganisationNode> childrenNodes = new ArrayList<>();
	
	public OrganisationNode(Organisation organisation) {
		this.organisation = organisation;
	}

	public Organisation getOrganisation() {
		return organisation;
	}

	public OrganisationNode getParentNode() {
		return parentNode;
	}

	public void setParentNode(OrganisationNode parentNode) {
		this.parentNode = parentNode;
	}

	public List<OrganisationNode> getChildrenNode() {
		return childrenNodes;
	}

	public void addChildrenNode(OrganisationNode childrenNode) {
		childrenNodes.add(childrenNode);
	}
	
	public OrganisationNode getChild(OrganisationRef reference) {
		OrganisationNode child = null;
		for(OrganisationNode childNode:childrenNodes) {
			if(childNode.getOrganisation().getKey().equals(reference.getKey())) {
				child = childNode;
			}
		}
		return child;
	}
	
	public void visit(Consumer<OrganisationNode> visitor) {
		visitor.accept(this);
		childrenNodes.stream().forEach(node -> node.visit(visitor));
	}
}
