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
package org.olat.core.commons.services.doceditor.manager;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.olat.core.commons.services.doceditor.DocEditor;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorSecurityCallback;
import org.olat.core.commons.services.doceditor.DocumentEditorService;
import org.olat.core.commons.services.vfs.VFSMetadata;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.VFSLeaf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 8 Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class DocumentEditorServiceImpl implements DocumentEditorService {

	@Autowired
	private List<DocEditor> editors;
	
	@Override
	public boolean hasEditor(Identity identity, Roles roles, String suffix, Mode mode, boolean hasMeta) {
		if (mode == null) return false;
		
		return editors.stream()
				.filter(DocEditor::isEnable)
				.filter(editor -> editor.isEnabledFor(identity, roles))
				.filter(editor -> editor.isSupportingFormat(suffix, mode, hasMeta))
				.findFirst()
				.isPresent();
	}

	@Override
	public List<DocEditor> getEditors(Identity identity, Roles roles, String suffix, Mode mode, boolean hasMeta) {
		return editors.stream()
				.filter(DocEditor::isEnable)
				.filter(editor -> editor.isEnabledFor(identity, roles))
				.filter(editor -> editor.isSupportingFormat(suffix, mode, hasMeta))
				.collect(Collectors.toList());
	}

	@Override
	public Optional<DocEditor> getEditor(String editorType) {
		return editors.stream()
				.filter(DocEditor::isEnable)
				.filter(editor -> editor.getType().equals(editorType))
				.findFirst();
	}
	
	@Override
	public boolean hasEditor(Identity identity, Roles roles, VFSLeaf vfsLeaf, DocEditorSecurityCallback secCallback) {
		String suffix = FileUtils.getFileSuffix(vfsLeaf.getName());
		return editors.stream()
				.filter(DocEditor::isEnable)
				.filter(editor -> editor.isEnabledFor(identity, roles))
				.filter(editor -> editor.isSupportingFormat(suffix, secCallback.getMode(), secCallback.hasMeta()))
				.filter(editor -> !editor.isLockedForMe(vfsLeaf, identity, secCallback.getMode()))
				.findFirst()
				.isPresent();
	}
	
	@Override
	public boolean hasEditor(Identity identity, Roles roles, VFSLeaf vfsLeaf, VFSMetadata metadata, DocEditorSecurityCallback secCallback) {
		String suffix = FileUtils.getFileSuffix(vfsLeaf.getName());
		return editors.stream()
				.filter(DocEditor::isEnable)
				.filter(editor -> editor.isEnabledFor(identity, roles))
				.filter(editor -> editor.isSupportingFormat(suffix, secCallback.getMode(), secCallback.hasMeta()))
				.filter(editor -> !editor.isLockedForMe(vfsLeaf, metadata, identity, secCallback.getMode()))
				.findFirst()
				.isPresent();
	}

}
