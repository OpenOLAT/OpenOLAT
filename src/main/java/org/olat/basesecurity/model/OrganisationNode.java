package org.olat.basesecurity.model;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.id.Organisation;

/**
 * 
 * Initial date: 3 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OrganisationNode {
	
	private final Organisation organisation;
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
}
