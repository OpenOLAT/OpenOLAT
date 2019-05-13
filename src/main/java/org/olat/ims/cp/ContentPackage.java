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

import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultDocument;
import org.dom4j.tree.DefaultElement;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Encoder;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.ims.cp.objects.CPFile;
import org.olat.ims.cp.objects.CPItem;
import org.olat.ims.cp.objects.CPManifest;
import org.olat.ims.cp.objects.CPOrganization;
import org.olat.ims.cp.objects.CPOrganizations;
import org.olat.ims.cp.objects.CPResource;
import org.olat.ims.cp.ui.CPPage;

/**
 * This class represents an IMS Content-Package. Most of the functionality is
 * delegated to cpcore (@see org.olat.ims.cp.CPCOre)
 * 
 * <P>
 * Initial Date: 27.06.2008 <br>
 * 
 * @author Sergio Trentini
 */
public class ContentPackage {

	// Delegate
	private CPCore cpcore;
	private OLATResourceable ores;
	private static final Logger log = Tracing.createLoggerFor(ContentPackage.class);

	// *** constructors ***

	ContentPackage(DefaultDocument doc, VFSContainer parent, OLATResourceable ores) {
		this.cpcore = new CPCore(doc, parent);
		this.ores = ores;
	}

	// *** CP manipulation ***

	String addBlankPage(String title) {
		CPItem newPage = new CPItem();
		newPage.setTitle(title);
		cpcore.addElement(newPage);
		return newPage.getIdentifier();
	}

	String addBlankPage(String parentID, String title) {
		CPItem newPage = new CPItem();
		newPage.setTitle(title);
		cpcore.addElement(newPage, parentID, 0);
		return newPage.getIdentifier();
	}

	protected void updatePage(CPPage page) {
		DefaultElement ele = cpcore.getElementByIdentifier(page.getIdentifier());
		if (ele instanceof CPItem) {
			CPItem item = (CPItem) ele;
			item.setTitle(page.getTitle());
			item.setMetadata(page.getMetadata());
			String itemIdentifierRef = item.getIdentifierRef();
			if (itemIdentifierRef == null || itemIdentifierRef.equals("")) {
				// This item has no linked resource yet. Add one if there is a page file
				// attached.
				VFSLeaf pageFile = page.getPageFile();
				if (pageFile != null) {
					CPResource res = new CPResource();
					CPFile file = new CPFile(pageFile);
					res.addFile(file);
					// TODO:GW Set type according to file
					res.setType("text/html");
					res.setHref(file.getHref());
					item.setIdentifierRef(res.getIdentifier());
					cpcore.getRootNode().getResources().addResource(res);
				}
			} else {// this item has already a linked resource
				// this is not supported, we don't change linked resources...
			}

		} else if (ele instanceof CPOrganization) {
			CPOrganization organization = (CPOrganization) ele;
			organization.setTitle(page.getTitle());

		} else {
			// ERROR: this shouldn't be
			throw new OLATRuntimeException("Error while updating manifest with new Page-Data. Invalid identifier " + page.getIdentifier(), null);
		}
	}

	boolean addElement(DefaultElement newElement, String parentId, int position) {
		return cpcore.addElement(newElement, parentId, position);
	}

	boolean addElement(DefaultElement newElement) {
		cpcore.addElement(newElement);
		return true;
	}

	boolean addElementAfter(DefaultElement newElement, String id) {
		return cpcore.addElementAfter(newElement, id);
	}

	void removeElement(String identifier, boolean deleteResource) {
		// at the moment, we remove always with resources (if they are not linked
		// in another item)
		cpcore.removeElement(identifier, deleteResource);
	}

	void moveElement(String nodeID, String newParentID, int position) {
		if (nodeID.equals(newParentID)) throw new OLATRuntimeException(CPOrganizations.class,
				"error while moving element: source and destination are identical...", new Exception());
		cpcore.moveElement(nodeID, newParentID, position);
	}

	String copyElement(String sourceID, String targetID) {
		return cpcore.copyElement(sourceID, targetID);
	}

	/**
	 * writes the manifest.xml
	 */
	void writeToFile() {
		String filename = "imsmanifest.xml";
		OutputFormat format = OutputFormat.createPrettyPrint();

		try {
			VFSLeaf outFile;
			// file may exist
			outFile = (VFSLeaf) cpcore.getRootDir().resolve("/" + filename);
			if (outFile == null) {
				// if not, create it
				outFile = cpcore.getRootDir().createChildLeaf("/" + filename);
			}
			DefaultDocument manifestDocument = cpcore.buildDocument();
			XMLWriter writer = new XMLWriter(outFile.getOutputStream(false), format);
			writer.write(manifestDocument);
		} catch (Exception e) {
			log.error("imsmanifest for ores " + ores.getResourceableId() + "couldn't be written to file.", e);
			throw new OLATRuntimeException(CPOrganizations.class, "Error writing imsmanifest-file", new IOException());
		}
	}

	// *** getters ***

	protected boolean isSingleUsedResource(CPResource res) {
		int linkCount = cpcore.referencesCount(res);
		return (linkCount < 2);
	}

	public VFSContainer getRootDir() {
		return cpcore.getRootDir();
	}

	protected DefaultDocument getDocument() {
		return cpcore.buildDocument();
	}

	protected CPManifest getRootNode() {
		return cpcore.getRootNode();
	}

	protected DefaultElement getElementByIdentifier(String identifier) {
		return cpcore.getElementByIdentifier(identifier);
	}

	public CPOrganization getFirstOrganizationInManifest() {
		return cpcore.getFirstOrganizationInManifest();
	}

	/**
	 * Return the treeDataModel used for GUI-tree
	 * 
	 * @return
	 */
	protected CPTreeDataModel buildTreeDataModel() {
		String orgaIdentifier = getFirstOrganizationInManifest().getIdentifier();
		// For the root node id of the ext-js tree we use an md5 hash. This is to
		// make sure that no unwanted characters are handed over to JS.
		String rootNodeId = Encoder.md5hash(orgaIdentifier);
		return new CPTreeDataModel(orgaIdentifier, rootNodeId, this);
	}

	protected CPPage getFirstPageToDisplay() {
		CPItem it = cpcore.getFirstPageToDisplay();
		if (it == null) {
			// in case the manifest has no item at all -> use organisation identifyer
			// instead
			return new CPPage(cpcore.getFirstOrganizationInManifest().getIdentifier(), this);
		} else {
			// display the found item
			return new CPPage(it.getIdentifier(), this);
		}
	}

	protected String getPageByItemId(String itemID) {
		return cpcore.getPageByItemID(itemID);
	}

	protected String getItemTitle(String itemID) {
		return cpcore.getItemTitle(itemID);
	}

	public String getLastError() {
		return cpcore.getLastError();
	}

	// *** SETTERS ***

	public void setLastError(String error) {
		cpcore.setLastError(error);
	}

	/**
	 * @return Returns a boolean value indicating whether the CP was created with
	 *         the OLAT CP editor or not.
	 */
	public boolean isOLATContentPackage() {
		return cpcore.isOLATContentPackage();
	}

	/**
	 * @return Returns the ores.
	 */
	public OLATResourceable getResourcable() {
		return ores;
	}

}
