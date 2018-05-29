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

import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
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
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
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
			String filename, Long size, String subIdent) {
		DialogElementImpl element = new DialogElementImpl();
		element.setCreationDate(new Date());
		element.setLastModified(element.getCreationDate());
		element.setFilename(filename);
		element.setSize(size);
		element.setAuthor(author);
		element.setEntry(entry);
		element.setSubIdent(subIdent);
		
		Forum forum = forumManager.addAForum();
		element.setForum(forum);
		
		dbInstance.getCurrentEntityManager().persist(element);
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
		return new OlatRootFolderImpl(pathToForumDir, null);
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
}
