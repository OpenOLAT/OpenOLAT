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
package org.olat.course.nodes.dialog.manager;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.QueryBuilder;
import org.olat.core.id.Identity;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.LocalFolderImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.nodes.dialog.DialogElement;
import org.olat.course.nodes.dialog.DialogElementsManager;
import org.olat.course.nodes.dialog.model.DialogElementImpl;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 3 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Service
public class DialogElementsManagerImpl implements DialogElementsManager {


	@Autowired
	private DB dbInstance;
	@Autowired
	private ForumManager forumManager;

	@Override
	public DialogElement createDialogElement(RepositoryEntry entry, Identity author,
			String filename, Long size, String subIdent, String authoredBy) {
		DialogElementImpl element = new DialogElementImpl();
		element.setCreationDate(new Date());
		element.setLastModified(element.getCreationDate());
		element.setFilename(filename);
		element.setSize(size);
		element.setAuthor(author);
		element.setEntry(entry);
		element.setSubIdent(subIdent);
		element.setAuthoredBy(authoredBy);
		
		Forum forum = forumManager.addAForum();
		element.setForum(forum);
		
		dbInstance.getCurrentEntityManager().persist(element);
		return element;
	}

	@Override
	public DialogElement updateDialogElement(DialogElementImpl element, String filename, String authoredBy) {
		element.setFilename(filename);
		element.setAuthoredBy(authoredBy);
		element.setLastModified(new Date());

		dbInstance.updateObject(element);
		return element;
	}

	@Override
	public List<DialogElement> getDialogElements(RepositoryEntryRef entry, String subIdent) {
		StringBuilder sb = new StringBuilder();
		sb.append("select element from dialogelement as element")
		  .append(" inner join fetch element.entry entry")
		  .append(" left join fetch element.author author")
		  .append(" left join fetch author.user authorUser")
		  .append(" where element.entry.key=:entryKey and element.subIdent=:subIdent");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), DialogElement.class)
			.setParameter("entryKey", entry.getKey())
			.setParameter("subIdent", subIdent)
			.getResultList();
	}

	@Override
	public boolean hasDialogElementByFilename(String filename) {
		QueryBuilder qb = new QueryBuilder();
		qb.append("select element from dialogelement as element")
				.and().append("element.filename=:filename");

		List<DialogElement> result = dbInstance.getCurrentEntityManager()
				.createQuery(qb.toString(), DialogElement.class)
				.setParameter("filename", filename)
				.getResultList();

		return result.isEmpty();
	}

	@Override
	public List<DialogElement> getDialogElements(IdentityRef author) {
		StringBuilder sb = new StringBuilder();
		sb.append("select element from dialogelement as element")
		  .append(" inner join fetch element.entry entry")
		  .append(" inner join element.author author")
		  .append(" where author.key=:identityKey");
		
		return dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), DialogElement.class)
			.setParameter("identityKey", author.getKey())
			.getResultList();
	}

	@Override
	public DialogElement getDialogElementByForum(Long forumKey) {
		StringBuilder sb = new StringBuilder();
		sb.append("select element from dialogelement as element")
		  .append(" inner join fetch element.entry entry")
		  .append(" inner join fetch element.forum forum")
		  .append(" left join fetch element.author author")
		  .append(" left join fetch author.user authorUser")
		  .append(" where forum.key=:forumKey");
		
		List<DialogElement> elements = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), DialogElement.class)
			.setParameter("forumKey", forumKey)
			.getResultList();
		return elements == null || elements.isEmpty() ? null : elements.get(0);
	}
	
	@Override
	public DialogElement getDialogElementByKey(Long elementKey) {
		StringBuilder sb = new StringBuilder();
		sb.append("select element from dialogelement as element")
		  .append(" inner join fetch element.entry entry")
		  .append(" inner join fetch element.forum forum")
		  .append(" left join fetch element.author author")
		  .append(" left join fetch author.user authorUser")
		  .append(" where element.key=:elementKey");
		
		List<DialogElement> elements = dbInstance.getCurrentEntityManager()
			.createQuery(sb.toString(), DialogElement.class)
			.setParameter("elementKey", elementKey)
			.getResultList();
		return elements == null || elements.isEmpty() ? null : elements.get(0);
	}

	@Override
	public VFSContainer getDialogContainer(DialogElement element) {
		Forum forum = element.getForum();
		
		StringBuilder sb = new StringBuilder();
		sb.append("/forum/").append(forum.getKey()).append("/");
		String pathToForumDir = sb.toString();
		return VFSManager.olatRootContainer(pathToForumDir, null);
	}

	@Override
	public VFSLeaf getDialogLeaf(DialogElement element) {
		VFSContainer container = getDialogContainer(element);
		VFSItem item = container.resolve(element.getFilename());
		if(item instanceof VFSLeaf) {
			return (VFSLeaf)item;
		}
		return null;
	}

	@Override
	public void deleteDialogElement(DialogElement element) {
		Forum forum = element.getForum();
		DialogElement reloadedElement = dbInstance.getCurrentEntityManager()
				.getReference(DialogElementImpl.class, element.getKey());
		dbInstance.getCurrentEntityManager().remove(reloadedElement);
		forumManager.deleteForum(forum.getKey());
	}

	@Override
	public VFSLeaf doUpload(File fileToUpload, String fileName, Identity publishedBy) {
		VFSContainer uploadVFSContainer = new LocalFolderImpl(new File(WebappHelper.getTmpDir(), "poster_" + UUID.randomUUID()));

		return uploadNewFile(fileToUpload, fileName, uploadVFSContainer, publishedBy);
	}

	@Override
	public DialogElement doCopySelectedFile(String fileToCopy, String filename, VFSContainer courseContainer,
											Identity identity, RepositoryEntry entry, String courseNodeIdent,
											String authoredBy) {
		VFSLeaf vl = (VFSLeaf) courseContainer.resolve(fileToCopy);
		if (vl != null) {
			DialogElement newElement = createDialogElement(entry, identity,
					filename, vl.getSize(), courseNodeIdent, authoredBy);

			//copy file
			VFSContainer dialogContainer = getDialogContainer(newElement);
			VFSLeaf copyVl = dialogContainer.createChildLeaf(filename);
			if (copyVl == null) {
				copyVl = (VFSLeaf) dialogContainer.resolve(filename);
			}
			VFSManager.copyContent(vl, copyVl, true, identity);
			return newElement;
		} else {
			return null;
		}
	}

	/**
	 * upload a new file for a topic discussion
	 *
	 * @param fileToUpload
	 * @param filename
	 * @param uploadVFSContainer
	 * @param publishedBy
	 * @return VFSLeaf object if successful, otherwise null
	 */
	private VFSLeaf uploadNewFile(File fileToUpload, String filename,
								  VFSContainer uploadVFSContainer, Identity publishedBy) {
		// save file and finish
		VFSLeaf newFile = uploadVFSContainer.createChildLeaf(filename);

		if(newFile != null) {
			try(InputStream in = new FileInputStream(fileToUpload)) {
				VFSManager.copyContent(in, newFile, publishedBy);
			} catch (IOException e) {
				return null;
			}
			FileUtils.deleteFile(fileToUpload);
		}
		// else: FXOLAT-409 somehow "createChildLeaf" did not succeed...
		// if so, there is already an error-msg in log (vfsContainer.createChildLeaf)

		return newFile;
	}
}
