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
import java.util.Vector;

import org.dom4j.Element;
import org.dom4j.tree.DefaultElement;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.ims.cp.CPCore;

/**
 * 
 * Description:<br>
 * This class represents a resources-element of a IMS-manifest-file
 * 
 * <P>
 * Initial Date: 26.06.2008 <br>
 * 
 * @author sergio
 */
public class CPResources extends DefaultElement implements CPNode {

	private static final OLog log = Tracing.createLoggerFor(CPResources.class);
	
	private List<CPResource> resources;
	private CPManifest parent;

	private List<String> errors;

	/**
	 * this constructor i used when building up the cp (parsing XML manifest)
	 * 
	 * @param me
	 */
	public CPResources(Element me) {
		super(me.getName());
		
		resources = new Vector<>();
		errors = new Vector<>();

		setAttributes(me.attributes());
		setContent(me.content());
	}

	/**
	 * this constructor is used when creating a new CP
	 */
	public CPResources() {
		super(CPCore.RESOURCES);
		resources = new Vector<>();
		errors = new Vector<>();
	}

	@Override
	public void buildChildren() {
		Iterator<Element> children = this.elementIterator();
		// iterate through children
		while (children.hasNext()) {
			Element child = children.next();
			if (child.getName().equals(CPCore.RESOURCE)) {
				CPResource res = new CPResource(child);
				res.setParentElement(this);
				res.buildChildren();
				resources.add(res);
			} else {
				errors.add("Invalid IMS-Manifest (only \"resource\"-elements allowed under <resources> )");
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
	public void buildDocument(Element parentEl) {
		DefaultElement resourceElement = new DefaultElement(CPCore.RESOURCES);

		for (Iterator<CPResource> itResources = resources.iterator(); itResources.hasNext();) {
			CPResource res = itResources.next();
			res.buildDocument(resourceElement);
		}
		parentEl.add(resourceElement);
	}

	// *** CP manipulation ***

	/**
	 * Adds a new Resource - element to the end of the resources-vector
	 */
	public void addResource(CPResource newResource) {
		newResource.setParentElement(this);
		resources.add(newResource);
	}

	/**
	 * removes a child-resource from this elements resource-collection
	 * 
	 * @param id the identifier of the <resource>-element to remove
	 */
	public void removeChild(String id) {
		try {
			CPResource res = (CPResource) getElementByIdentifier(id);
			resources.remove(res);
		} catch (Exception e) {
			log.error("child " + id + " was not removed.", e);
			throw new OLATRuntimeException(CPOrganizations.class, "error while removing child: child-element (<resource>) with identifier \""
					+ id + "\" not found!", new Exception());
		}
	}

	// ***GETTERS ***

	public List<CPResource> getResources() {
		return resources;
	}

	public Iterator<CPResource> getResourceIterator() {
		return resources.iterator();
	}

	/**
	 * Returns the Resource with the specified identifier Returns null if Resource
	 * is not found
	 * 
	 * @param identifier
	 * @return
	 */
	public CPResource getResourceByID(String identifier) {
		Iterator<CPResource> it = resources.iterator();
		CPResource res;
		while (it.hasNext()) {
			res = it.next();
			if (res.getIdentifier().equals(identifier)) { return res; }
		}
		// TODO: should it throw an exception, if no element with the given
		// identifier is found ???
		return null;
	}

	@Override
	public DefaultElement getElementByIdentifier(String id) {
		DefaultElement e;
		for (Iterator<CPResource> itResources = resources.iterator(); itResources.hasNext();) {
			CPResource res = itResources.next();
			e = res.getElementByIdentifier(id);
			if (e != null) return e;
		}
		return null;
	}

	@Override
	public int getPosition() {
		// there is only one <resources> element
		return 0;
	}

	public CPManifest getParentElement() {
		return parent;
	}

	public VFSContainer getRootDir() {
		return parent.getRootDir();
	}

	/**
	 * returns all dependencies (CPDependency) in a vector
	 * 
	 * @return
	 */
	public List<CPDependency> getAllDependencies() {
		List<CPDependency> deps = new Vector<>();
		for (CPResource res : getResources()) {
			deps.addAll(res.getDependencies());
		}
		return deps;
	}

	/**
	 * returns all files (CPFile) in a vector
	 * 
	 * @return
	 */
	public List<CPFile> getAllFiles() {
		List<CPFile> files = new Vector<>();
		for (CPResource res : getResources()) {
			files.addAll(res.getFiles());
		}
		return files;
	}

	// *** SETTERS ***
	@Override
	public void setPosition(int pos) {
	// there is only one <resources> element
	}

	public void setParentElement(CPManifest parent) {
		this.parent = parent;
	}
}