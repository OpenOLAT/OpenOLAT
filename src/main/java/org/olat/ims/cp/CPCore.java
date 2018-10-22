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

package org.olat.ims.cp;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.dom4j.Element;
import org.dom4j.tree.DefaultDocument;
import org.dom4j.tree.DefaultElement;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.ims.cp.objects.CPDependency;
import org.olat.ims.cp.objects.CPFile;
import org.olat.ims.cp.objects.CPItem;
import org.olat.ims.cp.objects.CPManifest;
import org.olat.ims.cp.objects.CPMetadata;
import org.olat.ims.cp.objects.CPOrganization;
import org.olat.ims.cp.objects.CPOrganizations;
import org.olat.ims.cp.objects.CPResource;
import org.olat.ims.cp.objects.CPResources;
import org.olat.modules.wiki.WikiToCPExport;

/**
 * 
 * Description:<br>
 * This class provides basic functionality for a IMS Content Package
 * 
 * <P>
 * Initial Date: 27.06.2008 <br>
 * 
 * @author Sergio Trentini
 */
public class CPCore {

	/**
	 * The CP Manifest name
	 */
	public static final String MANIFEST_NAME = "imsmanifest.xml";

	// Element and Attribute Names
	public static final String MANIFEST = "manifest";
	public static final String ORGANIZATIONS = "organizations";
	public static final String RESOURCES = "resources";
	public static final String DEFAULT = "default";
	public static final String ORGANIZATION = "organization";
	public static final String ITEM = "item";
	public static final String PARAMETERS = "parameters";
	public static final String RESOURCE = "resource";
	public static final String BASE = "base";
	public static final String FILE = "file";
	public static final String TYPE = "type";
	public static final String HREF = "href";
	public static final String METADATA = "metadata";
	public static final String IDENTIFIER = "identifier";
	public static final String IDENTIFIERREF = "identifierref";
	public static final String STRUCTURE = "structure";
	public static final String TITLE = "title";
	public static final String DEPENDENCY = "dependency";
	public static final String VERSION = "version";
	public static final String SCHEMA = "schema";
	public static final String SCHEMALOCATION = "schemaLocation";
	public static final String SCHEMAVERSION = "schemaversion";
	public static final String ISVISIBLE = "isvisible";
	public static final String OLAT_MANIFEST_IDENTIFIER = "olat_ims_cp_editor_v1";
	public static final String OLAT_ORGANIZATION_IDENTIFIER = "TOC";

	private DefaultDocument doc;
	private VFSContainer rootDir;
	private CPManifest rootNode;

	private List<String> errors;

	public CPCore(DefaultDocument doc, VFSContainer rootDir) {
		this.doc = doc;
		this.rootDir = rootDir;
		errors = new Vector<>();
		buildTree();
	}

	/**
	 * parses the document, builds manifest-datamodel-tree-structure
	 */
	public void buildTree() {
		if (doc != null) {
			rootNode = new CPManifest(this, (DefaultElement) doc.getRootElement());
			rootNode.buildChildren();
		}
	}

	/**
	 * 
	 * this is case-sensitive!
	 * 
	 * @param identifier
	 * @return an Element by its IDENTIFIER attribute starting at the manifests
	 *         root element This will do a deep recursive search
	 */
	public DefaultElement getElementByIdentifier(String identifier) {
		return rootNode.getElementByIdentifier(identifier);
	}

	// *** CP manipulation ***

	/**
	 * adds an element as a child to the element with id parentId if the element
	 * with parentId is not found, it returns false
	 * 
	 * if adding was successful, it returns true
	 */
	public boolean addElement(DefaultElement newElement, String parentId, int position) {

		DefaultElement parentElement = rootNode.getElementByIdentifier(parentId);
		if (parentElement == null) { throw new OLATRuntimeException(CPOrganizations.class, "Parent-element with identifier:\"" + parentId
				+ "\" not found!", new Exception()); }

		if (parentElement instanceof CPItem) {
			// parent is a <item>
			if (newElement instanceof CPItem) {
				// only CPItems can be added to CPItems
				CPItem item = (CPItem) parentElement;
				item.addItemAt((CPItem) newElement, position);
				return true;
			} else {
				throw new OLATRuntimeException(CPOrganizations.class, "you can only add <item>  elements to an <item>-element", new Exception());
			}

		} else if (parentElement instanceof CPOrganization) {
			// parent is a <organization>
			if (newElement instanceof CPItem) {
				// add a new item to organization element
				CPOrganization org = (CPOrganization) parentElement;
				org.addItemAt((CPItem) newElement, position);
				return true;
			} else {
				throw new OLATRuntimeException(CPOrganizations.class, "you can only add <item>  elements to an <organization>-element",
						new Exception());
			}

		} else if (parentElement instanceof CPResource) {
			// parent is a <resource>
			CPResource resource = (CPResource) parentElement;
			if (newElement instanceof CPFile) {
				resource.addFile((CPFile) newElement);
			} else if (newElement instanceof CPDependency) {
				resource.addDependency((CPDependency) newElement);
			} else {
				throw new OLATRuntimeException(CPOrganizations.class, "you can only add <dependency> or <file> elements to a Resource",
						new Exception());
			}
			return true;
		} else if (parentElement instanceof CPResources) {
			// parent is <resources> !!see the "s" at the end ;)
			if (newElement instanceof CPResource) {
				CPResources resources = (CPResources) parentElement;
				resources.addResource((CPResource) newElement);
				return true;
			} else {
				throw new OLATRuntimeException(CPOrganizations.class, "you can only add <resource>elements to the <resources> element",
						new Exception());
			}
		}

		return false;
	}

	/**
	 * adds an element to the CP. Only accepts <resource> and <organization>
	 * elements
	 * 
	 * @param newElement
	 * @return
	 */
	public void addElement(DefaultElement newElement) {

		if (newElement instanceof CPResource) {
			rootNode.getResources().addResource((CPResource) newElement);
		} else if (newElement instanceof CPOrganization) {
			rootNode.getOrganizations().addOrganization((CPOrganization) newElement);
		} else if (newElement instanceof CPItem) {
			if (rootNode.getOrganizations().getOrganizations().size() > 0) {
				rootNode.getOrganizations().getOrganizations().elementAt(0).addItem((CPItem) newElement);
			}
		} else {
			throw new OLATRuntimeException(CPOrganizations.class, "invalid newElement for adding to manifest", new Exception());
		}

	}

	/**
	 * adds an element to the cp. Adds it after the item with identifier "id"
	 * 
	 * @param newElement
	 * @param id
	 * @return
	 */
	public boolean addElementAfter(DefaultElement newElement, String id) {
		DefaultElement beforeElement = rootNode.getElementByIdentifier(id);

		if (beforeElement == null) return false;

		if (beforeElement instanceof CPItem) {
			// beforeElement is a <item>
			// ==> newElement has to be an <item>
			CPItem beforeItem = (CPItem) beforeElement;
			Element parent = beforeItem.getParentElement();
			if (!(newElement instanceof CPItem)) { throw new OLATRuntimeException(CPOrganizations.class, "only <item> element allowed",
					new Exception()); }
			if (parent instanceof CPItem) {
				CPItem p = (CPItem) parent;
				p.addItemAt((CPItem) newElement, beforeItem.getPosition() + 1);
			} else if (parent instanceof CPOrganization) {
				CPOrganization o = (CPOrganization) parent;
				o.addItemAt((CPItem) newElement, beforeItem.getPosition() + 1);
			} else {
				throw new OLATRuntimeException(CPOrganizations.class, "you cannot add an <item> element to a " + parent.getName() + " element",
						new Exception());
			}

		}

		return true;
	}

	/**
	 * removes an element with identifier "identifier" from the manifest
	 * 
	 * @param identifier the identifier if the element to remove
	 * @param booleanFlag indicates whether to remove linked resources as well...!
	 *          (needed for moving elements)
	 */
	public void removeElement(String identifier, boolean resourceFlag) {

		DefaultElement el = rootNode.getElementByIdentifier(identifier);
		if (el != null) {
			if (el instanceof CPItem) {
				// element is CPItem
				CPItem item = (CPItem) el;

				// first remove resources
				if (resourceFlag) {
					// Delete children (depth first search)
					removeChildElements(item, resourceFlag);

					// remove referenced resource
					CPResource res = (CPResource) rootNode.getElementByIdentifier(item.getIdentifierRef());
					if (res != null && referencesCount(res) == 1) {
						res.removeFromManifest();
					}
				}
				// then remove item
				item.removeFromManifest();

			} else if (el instanceof CPOrganization) {
				// element is organization
				CPOrganization org = (CPOrganization) el;
				org.removeFromManifest(resourceFlag);
			} else if (el instanceof CPMetadata) {
				// element is <metadata>
				CPMetadata md = (CPMetadata) el;
				md.removeFromManifest();
			}
		} else {
			throw new OLATRuntimeException(CPOrganizations.class, "couldn't remove element with id \"" + identifier
					+ "\"! Element not found in manifest ", new Exception());

		}
	}

	/**
	 * Deletes all children of the element specified by the identifier
	 * 
	 * @param identifier
	 * @param deleteResource
	 */
	void removeChildElements(CPItem item, boolean deleteResource) {
		if (item != null) {
			for (String childIdentifier : item.getItemIdentifiers()) {
				removeElement(childIdentifier, deleteResource);
			}
		}
	}

	/**
	 * Checks how many item-elements link to the given resource element.
	 * 
	 * @param resource
	 * @return
	 */
	protected int referencesCount(CPResource resource) {

		int linkCount = 0;
		List<CPItem> items = new Vector<>();
		for (Iterator<CPOrganization> it = rootNode.getOrganizations().getOrganizationIterator(); it.hasNext();) {
			CPOrganization org = it.next();
			items.addAll(org.getAllItems());
		}

		for (CPItem item : items) {
			if (item.getIdentifierRef().equals(resource.getIdentifier())) linkCount++;
		}

		List<CPDependency> dependencies = rootNode.getResources().getAllDependencies();
		for (CPDependency dependency : dependencies) {
			if (dependency.getIdentifierRef().equals(resource.getIdentifier())) linkCount++;
		}

		return linkCount;
	}

	public void moveElement(String nodeID, String newParentID, int position) {
		DefaultElement elementToMove = rootNode.getElementByIdentifier(nodeID);
		if (elementToMove != null) {
			if (elementToMove instanceof CPItem) {
				removeElement(nodeID, false);
				addElement(elementToMove, newParentID, position);
			} else if (elementToMove instanceof CPOrganization) {
				// not yet supported
			} else {
				throw new OLATRuntimeException(CPOrganizations.class, "Only <item>-elements are moveable...!", new Exception());
			}
		}
	}

	/**
	 * duplicates an element and inserts it after targetID
	 * 
	 * @param sourceID
	 * @param targetID
	 */
	public String copyElement(String sourceID, String targetID) {
		DefaultElement elementToCopy = rootNode.getElementByIdentifier(sourceID);
		if (elementToCopy == null) { throw new OLATRuntimeException(CPOrganizations.class, "element with identifier \"" + sourceID
				+ "\" not found..!", new Exception()); }

		if (elementToCopy instanceof CPItem) {
			CPItem newItem = (CPItem) elementToCopy.clone();
			cloneResourceOfItemAndSubitems(newItem);
			addElementAfter(newItem, targetID);
			return newItem.getIdentifier();
		} else {
			// if (elementToCopy.getClass().equals(CPOrganization.class)) {
			// not yet supported
			throw new OLATRuntimeException(CPOrganizations.class, "You can only copy <item>-elements...!", new Exception());
		}

	}

	/**
	 * Clones all editable resources of the item and its subitems.
	 * 
	 * @param item
	 */
	private void cloneResourceOfItemAndSubitems(CPItem item) {
		cloneResourceOfItem(item);
		for (CPItem child : item.getItems()) {
			cloneResourceOfItemAndSubitems(child);
		}
	}

	/**
	 * Clones the resource of an item. If the resource is not editable, i.e. it is
	 * not an html, Word or Excel file, there's no need to clone it and nothing
	 * will be done. Editable resources are cloned and the single referenced file
	 * is copied.
	 * 
	 * @param item
	 */
	private void cloneResourceOfItem(CPItem item) {
		DefaultElement ref = getElementByIdentifier(item.getIdentifierRef());
		if (ref != null && ref instanceof CPResource) {
			CPResource resource = (CPResource) ref;
			// Clone the resource if the linked file is editable (i.e. it is an html,
			// Word or Excel file)
			String href = resource.getFullHref();
			if (href != null) {
				String extension = href.substring(href.lastIndexOf(".") + 1);
				if ("htm".equals(extension) || "html".equals(extension) || "doc".equals(extension) || "xls".equals(extension)) {
					CPResource clonedResource = (CPResource) resource.clone();
					addElement(clonedResource);
					item.setIdentifierRef(clonedResource.getIdentifier());
				}
			}
		}
	}

	/**
	 * Searches for <item>-elements or <dependency>-elements which references to
	 * the resource with id "resourceIdentifier"
	 * 
	 * if an element is found, search is aborted and the found element is returned
	 * 
	 * @param resourceIdentifier
	 * @return the found element or null
	 */
	public DefaultElement findReferencesToResource(String resourceIdentifier) {

		// search for <item identifierref="resourceIdentifier" >
		for (Iterator<CPOrganization> it = rootNode.getOrganizations().getOrganizationIterator(); it.hasNext();) {
			CPOrganization org = it.next();
			for (Iterator<CPItem> itO = org.getItems().iterator(); itO.hasNext();) {
				CPItem item = itO.next();
				CPItem found = _findReferencesToRes(item, resourceIdentifier);
				if (found != null) return found;
			}
		}

		// search for <dependency identifierref="resourceIdentifier" >
		for (Iterator<CPResource> itRes = rootNode.getResources().getResourceIterator(); itRes.hasNext();) {
			CPResource res = itRes.next();
			for (Iterator<CPDependency> itDep = res.getDependencyIterator(); itDep.hasNext();) {
				CPDependency dep = itDep.next();
				if (dep.getIdentifierRef().equals(resourceIdentifier)) return dep;
			}
		}

		return null;
	}

	/**
	 * searches recursively for <item>-elements with identifierRef "id" in the
	 * children-collection of the item "item"
	 * 
	 * @param item
	 * @param id
	 * @return
	 */
	private CPItem _findReferencesToRes(CPItem item, String id) {
		if (item.getIdentifierRef().equals(id)) return item;
		for (Iterator<CPItem> itO = item.getItems().iterator(); itO.hasNext();) {
			CPItem it = itO.next();
			CPItem found = _findReferencesToRes(it, id);
			if (found != null) return found;
		}
		return null;
	}

	// *** getters ***

	/**
	 * Returns the rootNode of the manifest
	 * 
	 * @return CPManifest
	 */
	public CPManifest getRootNode() {
		return rootNode;
	}

	public VFSContainer getRootDir() {
		return rootDir;
	}

	/**
	 * Returns the DefaultDocument of this CP
	 * 
	 * @return the xml Document of this CP
	 */
	public DefaultDocument buildDocument() {
		// if (doc != null) return doc;
		DefaultDocument newDoc = new DefaultDocument();
		rootNode.buildDocument(newDoc);
		return newDoc;
	}

	/**
	 * returns the first <organization> element of this manifest Note: IMS
	 * standard allows multiple <organization>-elements
	 * 
	 * @return
	 */
	public CPOrganization getFirstOrganizationInManifest() {
		Vector<CPOrganization> orgas = rootNode.getOrganizations().getOrganizations();
		// integrity check already done, there is at least one <organization> at
		// this moment
		return orgas.firstElement();
	}

	/**
	 * Gets the linked page for the <item> element with given id if no resource
	 * (page) is referenced, null is returned
	 * 
	 * @param id
	 * @return
	 */
	public String getPageByItemID(String id) {
		DefaultElement ele = getElementByIdentifier(id);
		if (ele instanceof CPItem) {
			CPItem item = (CPItem) ele;
			if (item.getIdentifierRef() == null || item.getIdentifierRef().equals("")) { return null; }
			DefaultElement resElement = getElementByIdentifier(item.getIdentifierRef());
			if (resElement instanceof CPResource) {
				CPResource res = (CPResource) resElement;
				return res.getFullHref();
			} else {
				Logger log = Logger.getLogger(this.getClass().getName());
				log.info("method: getPageByItemID(" + id + ") :  invalid manifest.. identifierred of <item> must point to a <resource>-element");
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * Returns the first page within the given organization returns null if no
	 * page found (empty organization)
	 * 
	 * @return
	 */
	public CPItem getFirstPageToDisplay() {
		CPOrganization orga = getFirstOrganizationInManifest();
		return orga.getFirstItem();
	}

	/**
	 * returns the item of an <item> element (with given identifier) in the
	 * manifest
	 * 
	 * @param itemID the identifier of the item
	 * @return returns the title. returns null if element is not found, or element
	 *         is not an <item>
	 */
	public String getItemTitle(String itemID) {
		DefaultElement ele = getElementByIdentifier(itemID);
		if (ele == null) { return null; }
		if (ele instanceof CPItem) {
			CPItem item = (CPItem) ele;
			return item.getTitle();
		} else {
			return null;
		}

	}

	/**
	 * Returns the last error of this ContentPackage (after building it.. )
	 * returns null, if no error occurred..
	 * 
	 * @return
	 */
	String getLastError() {
		if (errors.isEmpty()) { return rootNode.getLastError(); }
		return errors.get(0);
	}

	protected void setLastError(String err) {
		errors.add(err);
	}

	/**
	 * @return Returns a true if the CP was created with the OLAT CP editor or
	 *         exported from an OLAT wiki. False otherwise.
	 */
	public boolean isOLATContentPackage() {
		boolean isOLATCP = false;
		String identifier = rootNode.getIdentifier();
		isOLATCP = OLAT_MANIFEST_IDENTIFIER.equals(identifier);
		isOLATCP |= WikiToCPExport.WIKI_MANIFEST_IDENTIFIER.equals(identifier);
		return isOLATCP;
	}

}
