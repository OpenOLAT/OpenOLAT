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

package org.olat.ims.qti.fileresource;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.olat.core.CoreSpringFactory;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.fileresource.types.FileResource;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.ims.qti.process.QTIHelper;

import de.bps.onyx.plugin.OnyxModule;

/**
 * Initial Date: Apr 6, 2004
 * 
 * @author Mike Stock
 */
public class TestFileResource extends FileResource {

	/**
	 * IMS QTI Test file resource identifier.
	 */
	public static final String TYPE_NAME = "FileResource.TEST";

	/**
	 * Standard constructor.
	 */
	public TestFileResource() {
		super.setTypeName(TYPE_NAME);
	}
	
	/**
	 * @param unzippedDir
	 * @return True if is of type.
	 */
	public static boolean validate(File unzippedDir) {
		if(CoreSpringFactory.getImpl(OnyxModule.class).isEnabled() && OnyxModule.isOnyxTest(unzippedDir)) {
			return true;
		}

		// with VFS FIXME:pb:c: remove casts to LocalFileImpl and LocalFolderImpl if
		// no longer needed.
		VFSContainer vfsUnzippedRoot = new LocalFolderImpl(unzippedDir);
		VFSItem vfsQTI = vfsUnzippedRoot.resolve("qti.xml");
		// getDocument(..) ensures that InputStream is closed in every case.
		Document doc = QTIHelper.getDocument((LocalFileImpl) vfsQTI);
		// if doc is null an error loading the document occured
		if (doc == null) return false;
		// check if this is marked as test
		List metas = doc.selectNodes("questestinterop/assessment/qtimetadata/qtimetadatafield");
		for (Iterator iter = metas.iterator(); iter.hasNext();) {
			Element el_metafield = (Element) iter.next();
			Element el_label = (Element) el_metafield.selectSingleNode("fieldlabel");
			String label = el_label.getText();
			if (label.equals(AssessmentInstance.QMD_LABEL_TYPE)) { // type meta
				Element el_entry = (Element) el_metafield.selectSingleNode("fieldentry");
				String entry = el_entry.getText();
				if (!(entry.equals(AssessmentInstance.QMD_ENTRY_TYPE_SELF) || entry.equals(AssessmentInstance.QMD_ENTRY_TYPE_ASSESS))) return false;
			}
		}

		// check if at least one section with one item
		List sectionItems = doc.selectNodes("questestinterop/assessment/section/item");
		if (sectionItems.size() == 0) return false;
		
		
		for (Iterator iter = sectionItems.iterator(); iter.hasNext();) {
			Element it = (Element) iter.next();
			List sv = it.selectNodes("resprocessing/outcomes/decvar[@varname='SCORE']");
			// the QTIv1.2 system relies on the SCORE variable of items
			if (sv.size()!=1) return false;
		}
		
		return true;
	}
}
