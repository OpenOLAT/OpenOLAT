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
package org.olat.repository.manager;

import org.apache.lucene.document.Document;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.search.model.OlatDocument;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.RepositoryEntryDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 13.03.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class RepositoryEntryDocumentFactory {
	
	@Autowired
	private RepositoryService repositoryService;
	

	public String getResourceUrl(Long itemKey) {
		return "[RepositoryEntry:" + itemKey + "]";
	}

	public Document createDocument(SearchResourceContext searchResourceContext, Long repoEntryKey) {
		RepositoryEntry re = repositoryService.loadByKey(repoEntryKey);
		if(re != null) {
			return createDocument(searchResourceContext, re);
		}
		return null;
	}

	public Document createDocument(SearchResourceContext searchResourceContext, RepositoryEntry re) {
		OlatDocument oDocument = new OlatDocument();
		oDocument.setId(re.getKey());
		oDocument.setCreatedDate(re.getCreationDate());
		oDocument.setLastChange(re.getLastModified());
		oDocument.setTitle(re.getDisplayname());
		
		StringBuilder sb = new StringBuilder();
		String desc = re.getDescription();
		if(desc != null) {
			sb.append(desc).append(" ");
		}
		String objectives = re.getObjectives();
		if(objectives != null) {
			sb.append(objectives).append(" ");
		}
		String requirements = re.getRequirements();
		if(requirements != null) {
			sb.append(requirements);
		}
		oDocument.setDescription(sb.toString());
		oDocument.setResourceUrl(getResourceUrl(re.getKey()));
		
		String docType = RepositoryEntryDocument.TYPE + re.getOlatResource().getResourceableTypeName();
		oDocument.setDocumentType(docType);
		oDocument.setCssIcon(RepositoryEntryDocument.getIconCss(docType));
		oDocument.setParentContextType(searchResourceContext.getParentContextType());
		oDocument.setParentContextName(searchResourceContext.getParentContextName());
		oDocument.setAuthor(re.getAuthors());
		
		//add specific fields
		Document document = oDocument.getLuceneDocument();
		return document;
	}
}
