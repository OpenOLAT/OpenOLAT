/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/

package org.olat.ims.cp.objects;

import java.util.Iterator;
import java.util.Vector;

import org.apache.logging.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.tree.DefaultAttribute;
import org.dom4j.tree.DefaultDocument;
import org.dom4j.tree.DefaultElement;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.ims.cp.CPCore;

/**
 * 
 * Description:<br>
 * This class represents a manifest-element of a IMS-manifest-file it is the
 * root element of a ContentPackage
 * 
 * <P>
 * Initial Date: 07.07.2008 <br>
 * 
 * @author sergio
 */
public class CPManifest extends DefaultElement implements CPNode {
	
	private static final Logger log = Tracing.createLoggerFor(CPManifest.class);

	private static final String DEFAULT_SCHEMALOC = "http://www.imsglobal.org/xsd/imscp_v1p1 imscp_v1p1.xsd http://www.imsglobal.org/xsd/imsmd_v1p2 imsmd_v1p2p2.xsd";
	private static final String DEFAULT_NMS = "http://www.imsglobal.org/xsd/imsmd_v1p2";
	private CPOrganizations organizations;
	private CPResources resources;
	private String identifier;
	private String schemaLocation;
	private CPCore cp;
	private CPMetadata metadata;


	private Vector<String> errors;

	/**
	 * this constructor is used when building up the cp (parsing XML)
	 * 
	 * @param me
	 */
	public CPManifest(CPCore cp, DefaultElement me) {
		super(me.getName());
		errors = new Vector<>();
		this.identifier = me.attributeValue(CPCore.IDENTIFIER);
		this.schemaLocation = me.attributeValue(CPCore.SCHEMALOCATION);
		this.setNamespace(me.getNamespace());
		this.cp = cp;
		// FIXME: namespaces ! xmlns
		setContent(me.content());
	}

	/**
	 * This constructor is used when creating a new CP
	 * 
	 * @param cp the cpcore to which this manifest belongs
	 * @param identifier the identifier of the manifest
	 */
	public CPManifest(CPCore cp, String identifier) {
		super(CPCore.MANIFEST);
		this.identifier = identifier;
		schemaLocation = CPManifest.DEFAULT_SCHEMALOC;
		setNamespace(new Namespace("imsmd", DEFAULT_NMS));
		organizations = new CPOrganizations();
		resources = new CPResources();
		errors = new Vector<>();
		this.cp = cp;
	}

	public CPManifest(CPCore cp) {
		this(cp, CodeHelper.getGlobalForeverUniqueID());
	}

	@Override
	public void buildChildren() {
		Iterator<Element> children = elementIterator();
		boolean organizationsAdded = false;
		boolean resourcesAdded = false;

		while (children.hasNext()) {
			Element child = children.next();
			if (child.getName().equals(CPCore.ORGANIZATIONS)) {
				if (organizationsAdded) errors.add("Invalid IMS-Manifest ( only one <organizations> element is allowed )");

				CPOrganizations org = new CPOrganizations(child);
				org.buildChildren();
				org.setParentElement(this);
				organizations = org;
				organizationsAdded = true;
			} else if (child.getName().equals(CPCore.RESOURCES)) {
				if (resourcesAdded) errors.add("Invalid IMS-Manifest ( only one <resources> element is allowed )");

				CPResources res = new CPResources(child);
				res.setParentElement(this);
				res.buildChildren();
				resources = res;
				resourcesAdded = true;
			} else if (child.getName().equals(CPCore.METADATA)) {
				metadata = new CPMetadata(child);
				metadata.setParentElement(this);
			}
		}

		this.clearContent();
		validateElement();
	}

	/**
	 * checks whether required child-elements are present
	 */
	@Override
	public boolean validateElement() {
		if (this.organizations == null) {
			errors.add("Invalid IMS-Manifest ( missing <organizations> element )");
			return false;
		}
		if (this.resources == null) {
			errors.add("Invalid IMS-Manifest ( missing <resurces> element )");
			return false;
		}

		// just to check on duplicate identifiers..
		getAllIdentifiers();

		return true;
	}

	/**
	 * returns a vector which holds all identifiers that occur in the manifest
	 * 
	 * @return
	 */
	public Vector<String> getAllIdentifiers() {
		Vector<String> ids = new Vector<>();

		for (Iterator<CPOrganization> it = organizations.getOrganizationIterator(); it.hasNext();) {
			CPOrganization org = it.next();
			ids.add(org.getIdentifier());

			Vector<CPItem> allItems = new Vector<>();
			for (Iterator<CPItem> itemIt = org.getItemIterator(); itemIt.hasNext();) {
				CPItem item = itemIt.next();
				allItems.addAll(item.getAllItems());
			}

			for (CPItem item : allItems) {
				if (!ids.contains(item.getIdentifier())) {
					ids.add(item.getIdentifier());
				} else {
					errors.add("Invalid IMS-Manifest ( duplicate identifier " + item.getIdentifier() + " )");
				}
			}
		}

		for (Iterator<CPResource> resIt = resources.getResourceIterator(); resIt.hasNext();) {
			CPResource res = resIt.next();
			if (!ids.contains(res.getIdentifier())) {
				ids.add(res.getIdentifier());
			} else {
				errors.add("Invalid IMS-Manifest ( duplicate identifier " + res.getIdentifier() + " )");
			}
		}

		return ids;
	}

	/**
	 * 
	 * @param doc
	 */
	public void buildDocument(DefaultDocument doc) {
		// Manifest is the root-node of the document, therefore we need to pass the
		// "doc"
		DefaultElement manifestElement = new DefaultElement(CPCore.MANIFEST);

		manifestElement.add(new DefaultAttribute(CPCore.IDENTIFIER, this.identifier));
		manifestElement.add(new DefaultAttribute(CPCore.SCHEMALOCATION, this.schemaLocation));
		// manifestElement.setNamespace(this.getNamespace()); //FIXME: namespace

		doc.add(manifestElement);

		if (metadata != null) {
			metadata.buildDocument(manifestElement);
		}
		organizations.buildDocument(manifestElement);
		resources.buildDocument(manifestElement);

	}

	@Override
	public void buildDocument(Element parentEl) {
	// because the Manifest is the root-element of the document, we need "public
	// void buildDocument(DefaultDocument doc)" instead...
	}

	// *** getters ***

	public CPOrganizations getOrganizations() {
		return organizations;
	}

	public CPResources getResources() {
		return resources;
	}

	public String getIdentifier() {
		return identifier;
	}

	public DefaultElement getMetadata() {
		return metadata;
	}

	@Override
	public DefaultElement getElementByIdentifier(String id) {
		if (id.equals(identifier)) return this;
		if (id.equals(CPCore.ORGANIZATIONS)) return organizations;

		DefaultElement e = organizations.getElementByIdentifier(id);
		if (e != null) return e;
		e = resources.getElementByIdentifier(id);

		if (e == null) {
			log.info("Element with id \"" + id + "\" not found in manifest!");
		}
		return e;
	}

	@Override
	public int getPosition() {
		// there is only one <manifest> element
		return 0;
	}

	public CPCore getCP() {
		return cp;
	}

	public VFSContainer getRootDir() {
		return cp.getRootDir();
	}
	
	public String getLastError() {
		if (errors.isEmpty()) {
			return organizations.getLastError();
		} else {
			return errors.lastElement();
		}

	}

	// *** SETTERS ***

	@Override
	public void setPosition(int pos) {
	// there is only one <manifest> element
	}

	public void setMetadata(CPMetadata md) {
		this.metadata = md;
	}

	public void setCP(CPCore cpcore) {
		cp = cpcore;
	}

}
