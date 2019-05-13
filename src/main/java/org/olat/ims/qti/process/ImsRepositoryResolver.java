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
*/

package org.olat.ims.qti.process;

import java.io.File;
import java.text.ParseException;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.olat.core.id.OLATResourceable;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti.QTIChangeLogMessage;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;

/**
 * Initial Date: 04.08.2003
 * @author Mike Stock Comment:
 */
public class ImsRepositoryResolver implements Resolver {
	
	private static final Logger log = Tracing.createLoggerFor(ImsRepositoryResolver.class);

	public static final String QTI_FILE = "qti.xml";
	public static final String QTI_FIB_AUTOCOMPLETE_JS_FILE = "media/fibautocompl.js";
	public static final String QTI_FIB_AUTOCOMPLETE_CSS_FILE = "media/fibautocompl.css";
	private File fUnzippedDirRoot;
	private String sUnzippedDirRel;
	private Document doc = null;

	public ImsRepositoryResolver(Long repositoryEntryKey) {
		RepositoryManager rm = RepositoryManager.getInstance();
		RepositoryEntry entry = rm.lookupRepositoryEntry(repositoryEntryKey);
		if (entry != null) {
			OLATResource ores = entry.getOlatResource();
			init(ores);
		}
	}
	
	public ImsRepositoryResolver(RepositoryEntry entry) {
		if (entry != null) {
			OLATResource ores = entry.getOlatResource();
			init(ores);
		}
	}

	public ImsRepositoryResolver(OLATResourceable fileResource) {
		init(fileResource);
	}

	private void init(OLATResourceable fileResource) {
		FileResourceManager frm = FileResourceManager.getInstance();
		fUnzippedDirRoot = frm.unzipFileResource(fileResource);
		sUnzippedDirRel = frm.getUnzippedDirRel(fileResource);
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.olat.ims.qti.process.Resolver#getObjectBank(java.lang.String)
	 */
	public Element getObjectBank(String ident) {	
		//with VFS FIXME:pb:c: remove casts to LocalFileImpl and LocalFolderImpl if no longer needed.
		VFSContainer vfsUnzippedRoot = new LocalFolderImpl(fUnzippedDirRoot);
		VFSItem vfsQTI = vfsUnzippedRoot.resolve(ident + ".xml");
		//getDocument(..) ensures that InputStream is closed in every case.
		Document theDoc = QTIHelper.getDocument((LocalFileImpl) vfsQTI);
		//if doc is null an error loading the document occured (IOException, qti.xml does not exist)
		if (theDoc == null) return null;
		Element objectBank = (Element) theDoc.selectSingleNode("questestinterop/objectbank");
		return objectBank;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.olat.ims.qti.process.Resolver#getQTIDocument()
	 */
	public Document getQTIDocument() {
		//with VFS FIXME:pb:c: remove casts to LocalFileImpl and LocalFolderImpl if no longer needed.
		VFSContainer vfsUnzippedRoot = new LocalFolderImpl(fUnzippedDirRoot);
		VFSItem vfsQTI = vfsUnzippedRoot.resolve(QTI_FILE);
		//getDocument(..) ensures that InputStream is closed in every case.
		Document theDoc = QTIHelper.getDocument((LocalFileImpl) vfsQTI);
		//if doc is null an error loading the document occured (IOException, qti.xml does not exist)
		return theDoc;
	}

	/**
	 * reads the files in the ../changelog directory, and generates a
	 * <code>QTIChangeLogMessage</code> per file.
	 * 
	 * @return qti changelog messages or an empty array if no changelog exists.
	 * @see QTIChangeLogMessage
	 */
	public QTIChangeLogMessage[] getDocumentChangeLog() {
		VFSContainer dirRoot = new LocalFolderImpl(fUnzippedDirRoot);
		VFSContainer dirChangelog = (VFSContainer) dirRoot.resolve("changelog");
		if (dirChangelog == null) {
			// no change log
			return new QTIChangeLogMessage[0];
		}
		List<VFSItem> items = dirChangelog.getItems();
		// PRECONDITION: only changelog files in the changelog directory
		QTIChangeLogMessage[] logArr = new QTIChangeLogMessage[items.size()];
		String filName;
		String msg;
		int i = 0;
		java.text.SimpleDateFormat formatter = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH_mm_ss");
		for (Iterator<VFSItem> iter = items.iterator(); iter.hasNext();) {
			VFSLeaf file = (VFSLeaf) iter.next();
			filName = file.getName();
			String[] parts = filName.split("\\.");
			msg = FileUtils.load(file.getInputStream(), "utf-8");
			try {
				logArr[i] = new QTIChangeLogMessage(msg, parts[1].equals("all"), formatter.parse(parts[0]).getTime());
				i++;
			} catch (ParseException e) {
				log.error("", e);
			}
		}

		return logArr;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.olat.ims.qti.process.Resolver#getSection(java.lang.String)
	 */
	public Element getSection(String ident) {
		Element el_section = (Element) doc.selectSingleNode("questestinterop/assessment/section[@ident='" + ident + "']");
		return el_section;
	}

	/**
	 * (non-Javadoc)
	 * 
	 * @see org.olat.ims.qti.process.Resolver#getItem(java.lang.String)
	 */
	public Element getItem(String ident) {
		// ident of item must be "globally unique"(qti...), unique within a qti
		// document
		Element el_item = (Element) doc.selectSingleNode("//item[@ident='" + ident + "']");
		return el_item;
	}

	public String getStaticsBaseURI() {
		return WebappHelper.getServletContextPath() + "/secstatic/qti/" + sUnzippedDirRel;
	}

	/**
	 * @see org.olat.ims.qti.process.Resolver#hasAutocompleteFiles()
	 */
	public boolean hasAutocompleteFiles() {
		VFSContainer vfsUnzippedRoot = new LocalFolderImpl(fUnzippedDirRoot);
		VFSItem vfsAutocompleteJsItem = vfsUnzippedRoot.resolve(QTI_FIB_AUTOCOMPLETE_JS_FILE);
		if (vfsAutocompleteJsItem != null) {
			VFSItem vfsAutocompleteCssItem = vfsUnzippedRoot.resolve(QTI_FIB_AUTOCOMPLETE_CSS_FILE);
			if (vfsAutocompleteCssItem != null) {
				return true;
			}
		} 
		return false;
	}

}
