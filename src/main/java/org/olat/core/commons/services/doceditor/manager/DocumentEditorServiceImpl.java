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
import org.olat.core.commons.services.doceditor.DocumentEditorService;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.id.Identity;
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
	public boolean hasEditor(String suffix, Mode mode) {
		if (mode == null) return false;
		
		return editors.stream()
				.filter(DocEditor::isEnable)
				.filter(editor -> editor.isSupportingFormat(suffix, mode))
				.findFirst()
				.isPresent();
	}

	@Override
	public List<DocEditor> getEditors(String suffix, Mode mode) {
		return editors.stream()
				.filter(DocEditor::isEnable)
				.filter(editor -> editor.isSupportingFormat(suffix, mode))
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
	public boolean hasEditor(VFSLeaf vfsLeaf, Mode mode, Identity identity) {
		if (mode == null) return false;
		
		String suffix = FileUtils.getFileSuffix(vfsLeaf.getName());
		return editors.stream()
				.filter(DocEditor::isEnable)
				.filter(editor -> editor.isSupportingFormat(suffix, mode))
				.filter(editor -> !editor.isLockedForMe(vfsLeaf, mode, identity))
				.findFirst()
				.isPresent();
		
	}

}
