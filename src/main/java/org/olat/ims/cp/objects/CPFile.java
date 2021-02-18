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
import java.util.List;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.core.util.vfs.filters.VFSSystemItemFilter;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.cp.CPCore;

/**
 * 
 * Description:<br>
 * TThis class represents a file-element of a IMS-manifest-file
 * 
 * <P>
 * Initial Date: 01.07.2008 <br>
 * 
 * @author Sergio Trentini
 */
public class CPFile extends DefaultElement implements CPNode {

	private String href;
	private VFSLeaf file;
	private CPMetadata metadata;
	private int position;
	private CPResource parent;

	/**
	 * Constructor, used when building up the cp (parsing XML-manifest)
	 * 
	 * @param me
	 * @param xmlBase xmlBase-attribute of the parental resource-element
	 */
	public CPFile(Element me, String xmlBase, VFSContainer rootDir) {
		super(me.getName());
		setContent(me.content());
		this.href = me.attributeValue(CPCore.HREF);
		if (xmlBase.equals("")) {
			file = (VFSLeaf) rootDir.resolve(href);
		} else {
			file = (VFSLeaf) rootDir.resolve(xmlBase + "/" + href);
		}
	}

	/**
	 * this constructor is used when creating a new CPfile
	 * 
	 * @param href the href-attribute of this element
	 */
	public CPFile(VFSLeaf file) {
		super(CPCore.FILE);
		this.href = calculateHref(file);
		this.file = file;
	}

	@Override
	public Object clone() {
		CPFile copy = (CPFile) super.clone();
		// Don't copy metadata, since we don't edit them anyway.
		// Make a copy of the leaf
		VFSContainer parentContainer = file.getParentContainer();
		String newName = VFSManager.similarButNonExistingName(parentContainer, file.getName());
		VFSLeaf leafCopy = parentContainer.createChildLeaf(newName);
		// Don't know who clones.
		VFSManager.copyContent(file, leafCopy, true, null);
		copy.setFile(leafCopy);
		copy.setHref(calculateHref(leafCopy));
		return copy;
	}

	/**
	 * @param leaf
	 * @return The relataive path or href attribute of the leaf
	 */
	private String calculateHref(VFSLeaf leaf) {
		final String slash = "/";
		StringBuilder path = new StringBuilder(leaf.getName());
		VFSContainer parentContainer = leaf.getParentContainer();
		while (parentContainer != null && !FileResourceManager.ZIPDIR.equals(parentContainer.getName())) {
			path.insert(0, slash).insert(0, parentContainer.getName());
			parentContainer = parentContainer.getParentContainer();
		}
		return path.toString();
	}

	@Override
	public void buildChildren() {
		Iterator<Element> children = elementIterator();
		// iterate through children
		while (children.hasNext()) {
			Element child = children.next();
			if (child.getName().equals(CPCore.METADATA)) {
				metadata = new CPMetadata(child);
			} else {
				throw new OLATRuntimeException(CPOrganizations.class, "Invalid IMS-Manifest ( only <metadata> element under <file> is allowed )",
						new Exception());
			}
		}
		clearContent();
		validateElement();
	}

	@Override
	public boolean validateElement() {
		if (this.href == null) { throw new OLATRuntimeException(CPOrganizations.class,
				"Invalid IMS-Manifest (missing \"href\" attribute in <file> element)", new Exception()); }
		return true;
	}

	@Override
	public void buildDocument(Element parentEl) {
		DefaultElement fileElement = new DefaultElement(CPCore.FILE);
		fileElement.addAttribute(CPCore.HREF, href);
		if (metadata != null) {
			metadata.buildDocument(fileElement);
		}
		parentEl.add(fileElement);
	}

	/**
	 * deletes the file from the filesystem
	 */
	public void deleteFromFS() {
		if (file != null) {
			VFSContainer parentContainer = file.getParentContainer();
			file.delete();
			// Delete the parent container if it is empty
			deleteIfEmpty(parentContainer);
		}
	}

	/**
	 * Deletes the container if it is empty (excluding files with '.' prefix).
	 * 
	 * @param parentContainer
	 */
	private void deleteIfEmpty(VFSContainer container) {
		VFSItemFilter filter = new VFSSystemItemFilter();
		if (container != null) {
			List<VFSItem> items = container.getItems(filter);
			if (items == null || items.isEmpty()) {
				container.delete();
			}
		}
	}

	// *** Getters ***

	public String getHref() {
		return href;
	}

	public VFSLeaf getFile() {
		return file;
	}

	public String getFilename() {
		return file.getName();
	}

	@Override
	public int getPosition() {
		return position;
	}

	@Override
	public DefaultElement getElementByIdentifier(String id) {
		// <file>-elements do not have an identifier and do not have children...
		return null;
	}

	public CPResource getParentElement() {
		return parent;
	}

	// *** Setters ***

	public void setHref(String href) {
		this.href = href;
	}

	public void setFile(VFSLeaf file) {
		this.file = file;
	}
	
	@Override
	public void setPosition(int pos) {
		position = pos;
	}

	public void setParentElement(CPResource parent) {
		this.parent = parent;
	}

	public void setMetadata(CPMetadata md) {
		metadata = md;
	}
}