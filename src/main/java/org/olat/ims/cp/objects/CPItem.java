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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.apache.logging.log4j.Logger;
import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.ims.cp.CPCore;

/**
 * 
 * Description:<br>
 * This class represents an item-element of a IMS-manifest-file
 * 
 * <P>
 * Initial Date: 26.06.2008 <br>
 * 
 * @author Sergio Trentini
 */
public class CPItem extends DefaultElement implements CPNode {

	private static final Logger log = Tracing.createLoggerFor(CPItem.class);

	private String identifier;
	private String identifierRef;
	private String title;
	private CPMetadata metadata;

	private int position;
	private Element parent;

	private boolean visible;

	private Vector<CPItem> items;
	private Vector<String> errors;


	/**
	 * constructor is needed while building the datamodel-tree (parsing XML)
	 * 
	 * @param me
	 */
	public CPItem(Element me, Element parent) {
		super(me.getName());
		items = new Vector<>();
		errors = new Vector<>();
		setContent(me.content());
		this.parent = parent;
		this.identifier = me.attributeValue(CPCore.IDENTIFIER);
		this.identifierRef = me.attributeValue(CPCore.IDENTIFIERREF, "");

		String val = me.attributeValue(CPCore.ISVISIBLE, "true");
		this.visible = (val != null && val.equals("true"));
	}

	/**
	 * Constructor is needed when generating a new Item (e.g. adding a new Item to
	 * the CP)
	 */
	public CPItem(String identifier) {
		super(CPCore.ITEM);
		visible = true;
		this.identifier = identifier;
		this.identifierRef = "";
		items = new Vector<>();
		errors = new Vector<>();
	}

	public CPItem() {
		this(CodeHelper.getGlobalForeverUniqueID());
	}

	@Override
	public void buildChildren() {
		Iterator<Element> children = elementIterator();
		// iterate through children
		while (children.hasNext()) {
			Element child = children.next();
			if (child.getName().equals(CPCore.ITEM)) {
				CPItem item = new CPItem(child, this);
				item.buildChildren();
				item.setPosition(items.size());
				item.setParentElement(this);
				items.add(item);
			} else if (child.getName().equals(CPCore.TITLE)) {
				title = child.getText();
			} else if (child.getName().equals(CPCore.METADATA)) {
				metadata = new CPMetadata(child);
				metadata.setParentElement(this);
			}
		}
		this.clearContent();
		validateElement();
	}

	@Override
	public boolean validateElement() {
		if (this.title == null || this.title.equals("")) {
			errors.add("Invalid IMS-Manifest (missing \"title\" element in item " + this.identifier + " )");
			return false;
		}
		if (this.identifier == null || this.identifier.equals("")) {
			errors.add("Invalid IMS-Manifest (missing \"identifier\" attribute in item " + this.identifier + " )");
			return false;
		}
		return true;
	}
	
	@Override
	public void buildDocument(Element parentEl) {
		if(!validateElement()) return;
		
		DefaultElement itemElement = new DefaultElement(CPCore.ITEM);

		itemElement.addAttribute(CPCore.IDENTIFIER, identifier);
		if (!identifierRef.equals("")) itemElement.addAttribute(CPCore.IDENTIFIERREF, identifierRef);
		itemElement.addAttribute(CPCore.ISVISIBLE, isVisibleString());

		if (metadata != null) {
			metadata.buildDocument(itemElement);
		}

		DefaultElement titleElement = new DefaultElement(CPCore.TITLE);
		titleElement.setText(title);
		itemElement.add(titleElement);

		for (Iterator<CPItem> itItem = items.iterator(); itItem.hasNext();) {
			CPItem item = itItem.next();
			item.buildDocument(itemElement);
		}

		parentEl.add(itemElement);

	}

	// *** CP manipulation ***

	/**
	 * adds a new CPItem to the childrens-list of this item (inserts at the end)
	 * 
	 * @param newItem the new CPItem to add
	 */
	public void addItem(CPItem newItem) {
		newItem.setParentElement(this);
		items.add(newItem);
		newItem.setPosition(items.size() - 1);
		log.info("addItem:  added " + newItem.getIdentifier() + " to " + this.getIdentifier());
	}

	/**
	 * adds a new CPItem to the childrens-list of this item at position index
	 * 
	 * @param newItem the new CPItem to add
	 * @param index position
	 */
	public void addItemAt(CPItem newItem, int index) {
		newItem.setParentElement(this);
		if (index > -1 && index <= items.size()) {
			items.add(index, newItem);
			newItem.setPosition(index);
		} else {
			addItem(newItem);
		}
	}

	/**
	 * removes this item from the manifest
	 */
	public void removeFromManifest() {
		if (parent.getClass().equals(CPItem.class)) {
			CPItem p = (CPItem) parent;
			p.removeChild(identifier);
		} else if (parent.getClass().equals(CPOrganization.class)) {
			CPOrganization p = (CPOrganization) parent;
			p.removeChild(identifier);
		}
	}

	/**
	 * removes a child <item>
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

	@Override
	public Object clone() {
		CPItem copy = (CPItem) super.clone();
		copy.setIdentifier(CodeHelper.getGlobalForeverUniqueID());
		copy.setTitle(title + " copy");
		// Since metadata cannot be edited for now we don't do a shallow
		// copy. The parent remains the same as well, so does the logger.
		// The subitems should be cloned though.
		Vector<CPItem> clonedItems = (Vector<CPItem>) items.clone();
		copy.items = clonedItems;
		for (CPItem item : items) {
			int index = items.indexOf(item);
			CPItem clonedItem = (CPItem) item.clone();
			clonedItem.setParentElement(copy);
			clonedItems.set(index, clonedItem);
		}
		return copy;
	}

	/**
	 * Returns true, if item is visible
	 * 
	 * @return boolean
	 */
	public boolean isVisible() {
		return this.visible;
	}

	/**
	 * return "true" if item is visible "false" otherwise
	 * 
	 * @return
	 */
	public String isVisibleString() {
		if (this.isVisible()) return "true";
		return "false";
	}

	// *** GETTERS ***

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
		return null;
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

	public String getTitle() {
		return this.title;
	}

	public String getIdentifier() {
		return this.identifier;
	}

	public String getIdentifierRef() {
		return this.identifierRef;
	}

	@Override
	public int getPosition() {
		return position;
	}

	/**
	 * searches for the first item with a linked resource (identifierRef id not
	 * "") within the children items of this element if this element itself has a
	 * referenced resource, this item is returned if no resource is found, null is
	 * returned
	 * 
	 * @return
	 */
	public CPItem getFirstItemWithResource() {

		if (this.identifierRef != "") {
			// this item has a linked resource
			return this;
		} else {
			if (items.size() < 1) return null;
			int i = 0;
			CPItem it = items.elementAt(i);
			while (it.identifierRef == "" && i < items.size()) {
				it = items.elementAt(i);
				i++;
			}
			if (it.identifierRef == "") { return null; }
			return it;
		}

	}

	public Element getParentElement() {
		return parent;
	}

	public CPMetadata getMetadata() {
		return metadata;
	}

	public Vector<CPItem> getItems() {
		return items;
	}

	public List<String> getItemIdentifiers() {
		List<String> ids = new ArrayList<>();
		for (CPItem item : items) {
			ids.add(item.getIdentifier());
		}
		return ids;
	}

	/**
	 * returns a Vector which holds all the children and its subchildren etc.
	 * 
	 * @return
	 */
	public Vector<CPItem> getAllItems() {

		Vector<CPItem> allItems = new Vector<>();
		allItems.addAll(this.getItems());
		for (Iterator<CPItem> it = items.iterator(); it.hasNext();) {
			CPItem item = it.next();
			allItems.addAll(item.getAllItems());
		}
		return allItems;

	}

	public Iterator<CPItem> getItemIterator() {
		return items.iterator();
	}

	/**
	 * 
	 * @return
	 */
	String getLastError() {
		if (!errors.isEmpty()) return errors.lastElement();
		return null;
	}

	// *** SETTERS ***

	public void setTitle(String title) {
		this.title = title;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public void setIdentifierRef(String identifierRef) {
		this.identifierRef = identifierRef;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	@Override
	public void setPosition(int position) {
		this.position = position;
	}

	public void setParentElement(DefaultElement parent) {
		if (parent.getClass().equals(CPItem.class) || parent.getClass().equals(CPOrganization.class)) {
			this.parent = parent;
		} else {
			throw new OLATRuntimeException(CPOrganizations.class, "error while setting parentElement in element \"" + this.identifier
					+ "\". Only <item> or <organization> as parent-element allowed", new Exception());
		}
	}

	public void setMetadata(CPMetadata md) {
		metadata = md;
	}

	/**
	 * generates a new system-unique identifier and sets it
	 */
	public void setNewUniqueID() {
		identifier = CodeHelper.getGlobalForeverUniqueID();
	}

	/**
	 * generates new system-unique ids for this element and all it's children
	 * 
	 * @deprecated
	 */
	public void setNewUniqueIDrev() {
		identifier = CodeHelper.getGlobalForeverUniqueID();
		for (Iterator<CPItem> it = items.iterator(); it.hasNext();) {
			CPItem item = it.next();
			item.setNewUniqueIDrev();
		}
	}

}
