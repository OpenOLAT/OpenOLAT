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

package org.olat.ims.cp.ui;

import java.util.logging.Logger;

import org.dom4j.tree.DefaultElement;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.ims.cp.CPManager;
import org.olat.ims.cp.CPManagerImpl;
import org.olat.ims.cp.ContentPackage;
import org.olat.ims.cp.objects.CPItem;
import org.olat.ims.cp.objects.CPMetadata;
import org.olat.ims.cp.objects.CPOrganization;
import org.olat.ims.cp.objects.CPResource;

/**
 * Represents a CP-Page used for GUI
 * 
 * <P>
 * Initial Date: 26.08.2008 <br>
 * 
 * @author Sergio Trentini
 */
public class CPPage {

	private String identifier;
	private String idRef;
	private String title;
	private VFSContainer rootDir;
	private VFSLeaf pageFile;
	private ContentPackage cp;
	private CPMetadata metadata;
	private Logger log;
	private boolean cpRoot; // if this page represents the <organization> element

	// of the manifest

	public CPPage(String identifier, String title, ContentPackage cp) {
		this.identifier = identifier;
		this.title = title;
		this.cp = cp;
		this.rootDir = cp.getRootDir();

		this.log = Logger.getLogger(CPPage.class.getName());
	}

	/**
	 * 
	 * @param identifier
	 * @param cp
	 */
	public CPPage(String identifier, ContentPackage cp) {
		this.log = Logger.getLogger(CPPage.class.getName());
		this.identifier = identifier;
		CPManagerImpl cpMgm = (CPManagerImpl) CPManager.getInstance();
		DefaultElement ele = cpMgm.getElementByIdentifier(cp, identifier);
		if (ele instanceof CPItem) {
			CPItem pageItem = (CPItem) ele;
			this.cpRoot = false;
			this.idRef = pageItem.getIdentifierRef();
			this.title = pageItem.getTitle();
			this.rootDir = cp.getRootDir();
			this.metadata = pageItem.getMetadata();
			if (metadata != null) metadata.setTitle(title);
			this.cp = cp;
			String filePath = cpMgm.getPageByItemId(cp, identifier);
			if (filePath != null && filePath != "") {
				LocalFileImpl f = (LocalFileImpl) cp.getRootDir().resolve(filePath);
				this.pageFile = f;
			}
		} else if(ele instanceof CPOrganization){
			CPOrganization orga = (CPOrganization)ele;
			this.cpRoot = true;
			this.title = orga.getTitle();
		}
	}

	protected void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getIdentifier() {
		return identifier;
	}

	protected String getIdRef() {
		return idRef;
	}

	public String getTitle() {
		return title;
	}

	protected VFSContainer getRootDir() {
		return rootDir;
	}

	public boolean isOrgaPage() {
		return cpRoot;
	}

	/**
	 * returns the html-file of this page. can return null.... check with
	 * isInfoPage()
	 * 
	 * @return
	 */
	public VFSLeaf getPageFile() {
		return pageFile;
	}

	protected String getFileName() {
		if (pageFile == null) { return ""; }
		return pageFile.getName();
	}

	protected CPResource getResource() {
		CPResource resource = null;
		CPManager mgr = CPManager.getInstance();
		DefaultElement resElement = mgr.getElementByIdentifier(cp, idRef);
		if (resElement instanceof CPResource) {
			resource = (CPResource)resElement;
		}
		return resource;
	}
	
	public CPMetadata getMetadata() {
		return metadata;
	}

	/**
	 * returns true, if this page represents a chapter page (no linked
	 * html-page-resource)
	 * 
	 * @return
	 */
	protected boolean isChapterPage() {
		if (pageFile != null) {
			return false;
		} else {
			return true;
		}
	}

	protected void setFile(VFSLeaf file) {
		this.pageFile = file;
	}

	protected void setRootDir(VFSContainer rootDir) {
		this.rootDir = rootDir;
	}

	protected void setMetadata(CPMetadata meta) {
		log.info("set Metadata for CPPage: " + this.getTitle());
		this.metadata = meta;
	}

	protected void setTitle(String title) {
		this.title = title;
	}

}
