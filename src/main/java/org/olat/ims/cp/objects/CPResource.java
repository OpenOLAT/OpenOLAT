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
import org.olat.core.util.CodeHelper;
import org.olat.ims.cp.CPCore;

/**
 * 
 * Description:<br>
 * This class represents a resource-element of a IMS-manifest-file
 * 
 * <P>
 * Initial Date: 26.06.2008 <br>
 * 
 * @author Sergio Trentini
 */
public class CPResource extends DefaultElement implements CPNode {

	private String identifier;
	private String type;
	private String href;
	private String xmlbase;
	private CPMetadata metadata;
	private int position;
	private CPResources parent;

	private Vector<CPFile> files;
	private Vector<CPDependency> dependencies;

	/**
	 * this constructor is needed when building up the datamodel (parsing
	 * XML-manifest)
	 * 
	 * @param me
	 */
	public CPResource(Element me) {
		super(me.getName());
		files = new Vector<>();
		dependencies = new Vector<>();

		// setAttributes(me.attributes());
		setContent(me.content());

		this.identifier = me.attributeValue(CPCore.IDENTIFIER);
		this.type = me.attributeValue(CPCore.TYPE);
		this.href = me.attributeValue(CPCore.HREF);
		this.xmlbase = me.attributeValue(CPCore.BASE, "");

	}

	/**
	 * this constructor is used when creating a new resource (adding a new
	 * resource to the cp)
	 */
	public CPResource(String identifier) {
		super(CPCore.RESOURCE);
		xmlbase = "";
		this.identifier = identifier;
		type = "";
		href = "";
		files = new Vector<>();
		dependencies = new Vector<>();
	}

	public CPResource() {
		this(CodeHelper.getGlobalForeverUniqueID());
	}

	/**
	 * @see org.dom4j.tree.DefaultElement#clone()
	 */
	@Override
	public Object clone() {
		CPResource copy = (CPResource) super.clone();
		copy.setIdentifier(CodeHelper.getGlobalForeverUniqueID());
		// Don't copy metadata, since we don't edit them anyway. The parent and
		// dependencies remain the same.
		// Make a copy of the referenced file. The modification of the copy
		// shouldn't affect the original file.
		Vector<CPFile> clonedFiles = (Vector<CPFile>)files.clone();
		copy.files = clonedFiles;
		for (CPFile file : files) {
			if (href.equals(file.getHref())) {
				int index = files.indexOf(file);
				CPFile clonedFile = (CPFile)file.clone();
				clonedFile.setParent(copy);
				copy.setHref(clonedFile.getHref());
				clonedFiles.set(index, clonedFile);
			}
		}
		return copy;
	}


	@Override
	public void buildChildren() {
		Iterator<Element> children = this.elementIterator();
		// iterate through children
		while (children.hasNext()) {
			Element child = children.next();
			if (child.getName().equals(CPCore.FILE)) {
				CPFile file = new CPFile(child, this.xmlbase, parent.getRootDir());
				file.buildChildren();
				file.setParentElement(this);
				files.add(file);
			} else if (child.getName().equals(CPCore.DEPENDENCY)) {
				CPDependency dep = new CPDependency(child);
				dep.setParentElement(this);
				dependencies.add(dep);
			} else if (child.getName().equals(CPCore.METADATA)) {
				// TODO: implement METADATA
				metadata = new CPMetadata(child);
				metadata.setParentElement(this);
			} else {
				throw new OLATRuntimeException(CPOrganizations.class, "Invalid IMS-Manifest (unallowed element under <resource>)", new Exception());
			}
		}

		this.clearContent();
		validateElement();
	}

	@Override
	public boolean validateElement() {
		if (this.type == null) { throw new OLATRuntimeException(CPOrganizations.class, "Invalid IMS-Manifest (missing \"type\" attribute)",
				new Exception()); }
		return true;
	}

	@Override
	public void buildDocument(Element parentEl) {
		// String base = "";
		// if(xmlbase != null && !xmlbase.equals("")) base="
		// xml:base=\""+xmlbase+"\"";

		// TODO: xml base imlement !!!

		DefaultElement resourceElement = new DefaultElement(CPCore.RESOURCE);

		resourceElement.addAttribute(CPCore.IDENTIFIER, identifier);
		resourceElement.addAttribute(CPCore.TYPE, type);
		resourceElement.addAttribute(CPCore.HREF, href);
		if (!xmlbase.equals("")) resourceElement.addAttribute(CPCore.BASE, xmlbase);

		if (metadata != null) {
			metadata.buildDocument(resourceElement);
		}

		// build files
		for (Iterator<CPFile> itFiles = files.iterator(); itFiles.hasNext();) {
			CPFile file = itFiles.next();
			file.buildDocument(resourceElement);
		}

		// build dependencies
		for (Iterator<CPDependency> itDep = dependencies.iterator(); itDep.hasNext();) {
			CPDependency dep = itDep.next();
			dep.buildDocument(resourceElement);
		}

		parentEl.add(resourceElement);
	}

	// *** CP manipulations ***

	/**
	 * adds a new CPFile to the end of the files-vector (at the end)
	 */
	public void addFile(CPFile newFile) {
		files.add(newFile);
	}

	/**
	 * adds a new CPDependency to the end of the dependencies vector
	 * 
	 * @param newDependency
	 */
	public void addDependency(CPDependency newDependency) {
		dependencies.add(newDependency);
	}

	/**
	 * removes this <resource> object from the manifest. and deletes linked files
	 * from the FileSystem
	 */
	public void removeFromManifest() {
		// TODO:GW For now, only the page itself is deleted. All other linked files
		// are not touched since they could be part of a different resource as well.
		// It'd be neccessary to check this before deleting a file.
		// removeLinkedFiles();
		removeReferencedFile();
		parent.removeChild(this.identifier);
	}

	/**
	 * Removes all linked files from the file-system
	 */
	public void removeLinkedFiles() {
		for (Iterator<CPFile> itFiles = files.iterator(); itFiles.hasNext();) {
			CPFile f = itFiles.next();
			f.deleteFromFS();
		}
	}

	/**
	 * Removes the single file that the resource references to. All other files
	 * beloning to the resource remain untouched.
	 */
	public void removeReferencedFile() {
		for (CPFile file : files) {
			if (file.getHref().equals(href)) {
				file.deleteFromFS();
				break;
			}
		}
	}

	// *** GETTERS ***

	public String getHref() {
		return this.href;
	}

	public String getFullHref() {
		if (xmlbase.equals("")) {
			return href;
		} else {
			return xmlbase + "/" + href;
		}
	}

	public String getType() {
		return this.type;
	}

	public String getIdentifier() {
		return this.identifier;
	}

	public String getXMLBase() {
		return xmlbase;
	}

	public Vector<CPFile> getFiles() {
		return this.files;
	}

	public Vector<CPDependency> getDependencies() {
		return dependencies;
	}

	public Iterator<CPDependency> getDependencyIterator() {
		return dependencies.iterator();
	}

	@Override
	public DefaultElement getElementByIdentifier(String id) {
		if (identifier.equals(id)) return this;
		return null;
	}

	@Override
	public int getPosition() {
		return position;
	}

	public CPResources getParentElement() {
		return parent;
	}

	// *** SETTERS ***

	public void setHref(String href) {
		this.href = href;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setXMLBase(String xmlBase) {
		this.xmlbase = xmlBase;
	}

	@Override
	public void setPosition(int pos) {
		position = pos;
	}

	public void setParentElement(CPResources parent) {
		this.parent = parent;
	}

	public void setMetadata(CPMetadata md) {
		metadata = md;
	}
}