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
import org.olat.core.logging.OLATRuntimeException;
import org.olat.ims.cp.CPCore;

/**
 * 
 * Description:<br>
 * This class represents a organization-element of a IMS-manifest-file
 * 
 * <P>
 * Initial Date: 26.06.2008 <br>
 * 
 * @author Sergio Trentini
 */
public class CPOrganization extends DefaultElement implements CPNode {

	private String title;
	private String identifier;
	private String structure;
	private CPMetadata metadata;
	private int position;
	private CPOrganizations parent;
	private Vector<CPItem> items;
	private Vector<String> errors;

	/**
	 * this constructor is used when the cp is built (parsing XML manifest file)
	 * 
	 * @param me
	 */
	CPOrganization(Element me) {
		super(me.getName());
		items = new Vector<>();
		errors = new Vector<>();
		setAttributes(me.attributes());
		setContent(me.content());

		identifier = me.attributeValue(CPCore.IDENTIFIER);
		structure = me.attributeValue(CPCore.STRUCTURE, "hierarchical");
	}

	@Override
	public void buildChildren() {
		Iterator<Element> children = this.elementIterator();
		// iterate through children
		while (children.hasNext()) {
			Element child = children.next();
			if (child.getName().equals(CPCore.ITEM)) {
				CPItem item = new CPItem(child, this);
				item.setParentElement(this);
				item.buildChildren();
				item.setPosition(items.size());
				items.add(item);
			} else if (child.getName().equals(CPCore.TITLE)) {
				title = child.getText();
			} else if (child.getName().equals(CPCore.METADATA)) {
				metadata = new CPMetadata(child);
				metadata.setParentElement(this);
			} else {
				errors.add("Invalid IMS-Manifest ( unallowed element under <organization> )");
			}
		}

		this.clearContent();
		validateElement();
	}

	@Override
	public boolean validateElement() {
		// nothing to validate
		return true;
	}

	@Override
	public void buildDocument(Element parent) {

		DefaultElement orgaElement = new DefaultElement(CPCore.ORGANIZATION);

		orgaElement.addAttribute(CPCore.IDENTIFIER, identifier);
		orgaElement.addAttribute(CPCore.STRUCTURE, structure);

		DefaultElement titleElement = new DefaultElement(CPCore.TITLE);
		titleElement.setText(title);
		orgaElement.add(titleElement);

		if (metadata != null) {
			metadata.buildDocument(orgaElement);
		}
		for (Iterator<CPItem> itItem = items.iterator(); itItem.hasNext();) {
			CPItem item = itItem.next();
			item.buildDocument(orgaElement);
		}
		parent.add(orgaElement);

	}

	// *** CP manipulation ***

	/**
	 * adds a new CPItem to the end of the items-vector
	 */
	public void addItem(CPItem newItem) {
		newItem.setParentElement(this);
		items.add(newItem);
	}

	public void addItemAt(CPItem newItem, int index) {
		newItem.setParentElement(this);
		if (index > -1 && index <= items.size()) {
			items.add(index, newItem);
			newItem.setPosition(index);
		} else {
			items.add(newItem);
		}
	}

	/**
	 * removes this <organization> element from the manifest
	 * 
	 * @param resourceFlag indicates whether linked resources should be deleted as
	 *          well (true-->delete resources too)
	 */
	public void removeFromManifest(boolean resourceFlag) {
		for (Iterator<CPItem> itItem = items.iterator(); itItem.hasNext();) {
			CPItem item = itItem.next();
			parent.getParentElement().getCP().removeElement(item.getIdentifier(), resourceFlag);
		}
	}

	/**
	 * removes a child <item> from this elements children-collection
	 * 
	 * @param id the identifier of the child to remove
	 */
	public void removeChild(String id) {
		boolean removed = false;
		for (Iterator<CPItem> itItem = items.iterator(); itItem.hasNext();) {
			CPItem item = itItem.next();
			if (item.getIdentifier().equals(id)) {
				items.remove(item);
				removed = true;
				break;
			}
		}
		if (!removed) { throw new OLATRuntimeException(CPOrganizations.class, "error while removing child: child-element with identifier \""
				+ id + "\" not found!", new Exception()); }

	}

	// *** getters ***

	/**
	 * Returns the Item with the specified identifier Returns null if Item is not
	 * found
	 * 
	 * @param identifier id
	 * @return CPItem or null
	 */
	public CPItem getItemByID(String id) {
		Iterator<CPItem> it = items.iterator();
		CPItem item;
		while (it.hasNext()) {
			item = it.next();
			if (item.getIdentifier().equals(id)) { return item; }
		}
		// TODO: should it throw an exception, if no element with the given
		// identifier is found ???
		return null;
	}

	public Vector<CPItem> getItems() {
		return items;
	}

	public Iterator<CPItem> getItemIterator() {
		return items.iterator();
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}	
	
	String getStructure() {
		return structure;
	}

	@Override
	public DefaultElement getElementByIdentifier(String id) {
		if (identifier.equals(id)) return this;

		DefaultElement e;
		for (Iterator<CPItem> it = items.iterator(); it.hasNext();) {
			CPItem item = it.next();
			e = item.getElementByIdentifier(id);
			if (e != null) return e;
		}
		return null;
	}

	@Override
	public int getPosition() {
		return position;
	}

	/**
	 * returns a vector that holds all items in this organization-element
	 * (recursively, all ! )
	 * 
	 * @return vector
	 */
	public Vector<CPItem> getAllItems() {
		Vector<CPItem> allItems = new Vector<>();
		for (Iterator<CPItem> itItem = items.iterator(); itItem.hasNext();) {
			CPItem item = itItem.next();
			allItems.add(item);
			allItems.addAll(item.getAllItems());
		}
		return allItems;
	}

	/**
	 * Returns the first Item. If this organization has no child-items, null is
	 * returned.
	 * 
	 * @return
	 */
	public CPItem getFirstItem() {
		if (!items.isEmpty()) return items.firstElement();
		return null;
	}

	/**
	 * returns the parent element
	 * 
	 * @return
	 */
	CPOrganizations getParentElement() {
		return parent;
	}

	/**
	 * 
	 * @return
	 */
	String getLastError() {
		for (CPItem item : getAllItems()) {
			String err = item.getLastError();
			if (err != null) return err;
		}
		return null;
	}

	// *** SETTERS ***

	@Override
	public void setPosition(int pos) {
		position = pos;
	}

	public void setParentElement(CPOrganizations parent) {
		this.parent = parent;
	}

	public void setMetadata(CPMetadata md) {
		metadata = md;
	}

}
