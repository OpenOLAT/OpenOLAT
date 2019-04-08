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
package org.olat.core.commons.services.doceditor;

import java.util.List;
import java.util.Optional;

import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * 
 * Initial date: 8 Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface DocumentEditorService {
	
	/**
	 * Check if file with a specific suffix is supported by any enabled editor.
	 *
	 * @param suffix
	 * @param mode
	 * @return
	 */
	public boolean hasEditor(String suffix, Mode mode);
	
	/**
	 * Get all enabled editors which support a file with a specific suffix. Support means usually edit or read.
	 *
	 * @param suffix
	 * @param mode
	 * @return
	 */
	public List<DocEditor> getEditors(String suffix, Mode mode);
	
	/**
	 * Get the editor of a specific type.
	 * 
	 * @param editorType
	 * @return
	 */
	public Optional<DocEditor> getEditor(String editorType);
	
	/**
	 * Checks whether a vfsLeaf can be opened in any editor by a user and in a
	 * specific mode. This method checks not only if a file format is supported but
	 * also if the vfsLeaf is not locked by an other editor or user.
	 *
	 * @param vfsLeaf
	 * @param mode
	 * @param identity
	 * @return
	 */
	public boolean hasEditor(VFSLeaf vfsLeaf, Mode mode, Identity identity);
	
}
