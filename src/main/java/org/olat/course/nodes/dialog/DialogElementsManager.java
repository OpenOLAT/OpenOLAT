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
package org.olat.course.nodes.dialog;

import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 3 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface DialogElementsManager {
	

	public DialogElement createDialogElement(RepositoryEntry entry, Identity identity, String filename, Long size, String subIdent);
	
	public List<DialogElement> getDialogElements(RepositoryEntryRef entry, String subIdent);
	
	/**
	 * 
	 * @param author The author of the elements
	 * @return A list of dialog elements
	 */
	public List<DialogElement> getDialogElements(IdentityRef author);
	
	public DialogElement getDialogElementByForum(Long forumKey);
	
	public DialogElement getDialogElementByKey(Long elementKey);
	
	public VFSContainer getDialogContainer(DialogElement element);
	
	public VFSLeaf getDialogLeaf(DialogElement element);
	
	public void deleteDialogElement(DialogElement element);

}
