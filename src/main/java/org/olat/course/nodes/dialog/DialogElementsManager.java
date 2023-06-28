/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.dialog;

import java.io.File;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.nodes.dialog.model.DialogElementImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 3 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public interface DialogElementsManager {
	

	public DialogElement createDialogElement(RepositoryEntry entry, Identity identity, String filename, Long size, String subIdent, String authoredBy);

	/**
	 * update dialogElement regarding its values filename and authoredBy
	 *
	 * @param element
	 * @param filename
	 * @param authoredBy
	 * @return updated dialogElement
	 */
	public DialogElement updateDialogElement(DialogElementImpl element, String filename, String authoredBy);
	
	public List<DialogElement> getDialogElements(RepositoryEntryRef entry, String subIdent);

	/**
	 * check if there is already any dialogElement with given filename
	 * in current repoEntry and course element
	 *
	 * @param filename desired filename
	 * @param subIdent current courseElement
	 * @param entry current RepositoryEntry
	 * @return true if there is a dialogElement with given filename, otherwise false
	 */
	public boolean hasDialogElementByFilename(String filename, String subIdent, RepositoryEntry entry);
	
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

	/**
	 * upload new topic discussion file
	 *
	 * @param fileToUpload
	 * @param fileName
	 * @param publishedBy
	 *
	 * @return VFSLeaf/new File if successful otherwise null
	 */
	public VFSLeaf doUpload(File fileToUpload, String fileName, Identity publishedBy);

	/**
	 * copy chosenFile and create a new file out of it
	 *
	 * @param fileToCopy
	 * @param filename
	 * @param courseContainer
	 * @param identity
	 * @param entry
	 * @param courseNodeIdent
	 * @param authoredBy
	 *
	 * @return DialogElement if copy process was successful, otherwise null
	 */
	public DialogElement doCopySelectedFile(String fileToCopy, String filename, VFSContainer courseContainer, Identity identity,
							RepositoryEntry entry, String courseNodeIdent, String authoredBy);
}
