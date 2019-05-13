/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.dialog.manager;

import java.io.File;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.dialog.DialogElement;
import org.olat.course.nodes.dialog.DialogElementsManager;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserDataExportable;
import org.olat.user.manager.ManifestBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 29 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class DialogElementsUserDataManager implements UserDataExportable {
	
	private static final Logger log = Tracing.createLoggerFor(DialogElementsUserDataManager.class);

	@Autowired
	private DialogElementsManager dialogElementsManager;
	
	@Override
	public String getExporterID() {
		return "dialog";
	}

	@Override
	public void export(Identity identity, ManifestBuilder manifest, File archiveDirectory, Locale locale) {
		List<DialogElement> elements = dialogElementsManager.getDialogElements(identity);
		File elementsArchiveDirectory = new File(archiveDirectory, "FileDialogs");
		for(DialogElement element:elements) {
			exportElement(element, elementsArchiveDirectory);
		}
	}
	
	private void exportElement(DialogElement element, File elementsArchiveDirectory) {
		VFSLeaf file = dialogElementsManager.getDialogLeaf(element);
		if(file != null && file.exists()) {
			RepositoryEntry entry = element.getEntry();
			String name = StringHelper.transformDisplayNameToFileSystemName(entry.getDisplayname()) +
					"_" + StringHelper.transformDisplayNameToFileSystemName(getNodeName(element));
			File elementDir = new File(elementsArchiveDirectory, name);
			elementDir.mkdirs();
			FileUtils.copyItemToDir(file, elementDir, "Copy file dialog");
		}
	}
	
	private String getNodeName(DialogElement element) {
		try {
			RepositoryEntry entry = element.getEntry();
			ICourse course = CourseFactory.loadCourse(entry.getOlatResource().getResourceableId());
			CourseNode node = course.getRunStructure().getNode(element.getSubIdent());
			if(node == null) {
				return element.getSubIdent();
			}
			if(StringHelper.containsNonWhitespace(node.getShortTitle())) {
				return node.getShortTitle();
			}
			if(StringHelper.containsNonWhitespace(node.getLongTitle())) {
				return node.getLongTitle();
			}
			return element.getSubIdent();
		} catch (Exception e) {
			log.error("", e);
			return element.getSubIdent();
		}
	}
}
