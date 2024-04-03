/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.folder.ui;

import java.util.function.Function;

import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.commons.editor.htmleditor.HTMLEditorConfig;
import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.commons.services.doceditor.DocEditorConfigs;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.util.ContainerAndFile;

/**
 * 
 * Initial date: 1 Mar 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
class CreateDocumentConfig implements Function<VFSLeaf, DocEditorConfigs> {

	private final VFSContainer rootContainer;
	private final VFSContainer currentContainer;
	private final String currentContainerPath;
	private final CustomLinkTreeModel customLinkTreeModel;

	public CreateDocumentConfig(VFSContainer rootContainer, VFSContainer currentContainer, String currentContainerPath,
			CustomLinkTreeModel customLinkTreeModel) {
		this.rootContainer = rootContainer;
		this.currentContainer = currentContainer;
		this.currentContainerPath = currentContainerPath;
		this.customLinkTreeModel = customLinkTreeModel;
	}

	@Override
	public DocEditorConfigs apply(VFSLeaf vfsLeaf) {
		HTMLEditorConfig htmlEditorConfig = getHtmlEditorConfig(vfsLeaf);
		return DocEditorConfigs.builder()
				.withMode(DocEditor.Mode.EDIT)
				.withVersionControlled(true)
				.addConfig(htmlEditorConfig)
				.build(vfsLeaf);
	}
	
	private HTMLEditorConfig getHtmlEditorConfig(VFSLeaf vfsLeaf) {
		return getHtmlEditorConfig(rootContainer, currentContainer, currentContainerPath, customLinkTreeModel, vfsLeaf);
	}
	
	public static HTMLEditorConfig getHtmlEditorConfig(VFSContainer rootContainer, VFSContainer currentContainer,
			String currentContainerPath, CustomLinkTreeModel customLinkTreeModel, VFSLeaf vfsLeaf) {
		// start HTML editor with the folders root folder as base and the file
		// path as a relative path from the root directory. But first check if the
		// root directory is wirtable at all (e.g. not the case in users personal
		// briefcase), and seach for the next higher directory that is writable.
		String relFilePath = "/" + vfsLeaf.getName();
		// add current container path if not at root level
		if (!currentContainerPath.equals("/")) {
			relFilePath = currentContainerPath + relFilePath;
		}
		VFSContainer writableRootContainer = rootContainer;
		ContainerAndFile result = VFSManager.findWritableRootFolderFor(writableRootContainer, relFilePath);
		if (result != null) {
			if (vfsLeaf.getParentContainer() != null) {
				writableRootContainer = vfsLeaf.getParentContainer();
				relFilePath = vfsLeaf.getName();
			} else {
				writableRootContainer = result.getContainer();
			}
		} else {
			// use fallback that always work: current directory and current file
			relFilePath = vfsLeaf.getName();
			writableRootContainer = currentContainer;
		}
		return HTMLEditorConfig.builder(writableRootContainer, relFilePath)
				.withCustomLinkTreeModel(customLinkTreeModel)
				.build();
	}

}
