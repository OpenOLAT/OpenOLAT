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

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.olat.ims.cp.CPCore;

/**
 * 
 * Description:<br>
 * This class represents a organizations-element of a IMS-manifest-file
 * 
 * <P>
 * Initial Date: 26.06.2008 <br>
 * 
 * @author Sergio Trentini
 */
public class CPOrganizations extends DefaultElement implements CPNode {

	private Vector<CPOrganization> orgas;
	private CPManifest parent;

	private Vector<String> errors;

	/**
	 * this constructor is used when building up the CP (parsing XML)
	 * 
	 * @param me
	 */
	public CPOrganizations(Element me) {
		super(me.getName());
		orgas = new Vector<>();
		errors = new Vector<>();
		// setAttributes(me.attributes());
		setContent(me.content());
	}

	/**
	 * this constructor is used when creating a new CP
	 */
	public CPOrganizations() {
		super(CPCore.ORGANIZATIONS);
		orgas = new Vector<>();
		errors = new Vector<>();
	}

	@Override
	public void buildChildren() {
		Iterator<Element> children = elementIterator();
		// iterate through children
		while (children.hasNext()) {
			Element child = children.next();
			if (child.getName().equals(CPCore.ORGANIZATION)) {
				CPOrganization org = new CPOrganization(child);
				org.setParentElement(this);
				org.buildChildren();
				orgas.add(org);
			} else {
				errors.add("Invalid imsmanifest (only \"Organization\"-elements allowed under <organizations> )");
			}
		}
		this.clearContent();
		validateElement();
	}

	@Override
	public boolean validateElement() {
		if (orgas.size() < 1) {
			errors.add("Invalid IMS-Manifest ( missing <organization> element, must have one at least)");
			return false;
		}
		return true;
	}

	@Override
	public void buildDocument(Element parentEl) {
		DefaultElement orgaElement = new DefaultElement(CPCore.ORGANIZATIONS);

		for (Iterator<CPOrganization> itOrgas = orgas.iterator(); itOrgas.hasNext();) {
			CPOrganization org = itOrgas.next();
			org.buildDocument(orgaElement);
		}
		parentEl.add(orgaElement);
	}

	// *** cp manipulation ****

	/**
	 * adds a new CPOrganization to the end of the orgas-vector
	 */
	public void addOrganization(CPOrganization newOrganization) {
		newOrganization.setParent(this);
		orgas.add(newOrganization);
	}

	/**
	 * 
	 * @return
	 */
	public Vector<CPOrganization> getOrganizations() {
		return orgas;
	}

	public Iterator<CPOrganization> getOrganizationIterator() {
		return orgas.iterator();
	}

	@Override
	public int getPosition() {
		// there is only one <organizations> element, so position is always 0
		return 0;
	}

	/**
	 * Returns the Organization with identifier id Returns null if O. is not found
	 * 
	 * @param id
	 * @return
	 */
	public CPOrganization getOrganizationByID(String id) {
		Iterator<CPOrganization> it = orgas.iterator();
		CPOrganization org;
		while (it.hasNext()) {
			org = it.next();
			if (org.getIdentifier().equals(id)) { return org; }
		}
		return null;
	}

	@Override
	public DefaultElement getElementByIdentifier(String id) {
		DefaultElement e;
		for (Iterator<CPOrganization> it = orgas.iterator(); it.hasNext();) {
			CPOrganization orga = it.next();
			e = orga.getElementByIdentifier(id);
			if (e != null) return e;
		}
		return null;
	}

	public CPManifest getParentElement() {
		return parent;
	}

	/**
	 * 
	 * @return
	 */
	String getLastError() {
		if (errors.isEmpty()) {
			for (Iterator<CPOrganization> it = orgas.iterator(); it.hasNext();) {
				CPOrganization orga = it.next();
				String err = orga.getLastError();
				if (err != null) return err;
			}
			return null;
		} else {
			return errors.lastElement();
		}
	}

	// ***SETTERS***

	@Override
	public void setPosition(int pos) {
	// There is only one <organizations>...
	}

	public void setParentElement(CPManifest parent) {
		this.parent = parent;
	}

}