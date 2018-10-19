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
package org.olat.modules.qpool.manager;

import java.util.List;
import java.util.StringTokenizer;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QPoolSPI;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItemFull;
import org.olat.modules.qpool.QuestionPoolModule;
import org.olat.modules.qpool.model.QItemDocument;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.resource.OLATResource;
import org.olat.search.model.AbstractOlatDocument;
import org.olat.search.model.OlatDocument;
import org.olat.search.service.SearchResourceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 28.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("questionItemDocumentFactory")
public class QuestionItemDocumentFactory {

	@Autowired
	private PoolDAO poolDao;
	@Autowired
	private QuestionPoolModule qpoolModule;
	@Autowired
	private QuestionItemDAO questionItemDao;
	@Autowired
	private QPoolService qpoolService;
	@Autowired
	private LicenseService licenseService;

	public String getResourceUrl(Long itemKey) {
		return "[QuestionItem:" + itemKey + "]";
	}

	public Document createDocument(SearchResourceContext searchResourceContext, Long itemKey) {
		QuestionItemFull item = questionItemDao.loadById(itemKey);
		if(item != null) {
			return createDocument(searchResourceContext, item);
		}
		return null;
	}

	public Document createDocument(SearchResourceContext searchResourceContext, QuestionItemFull item) {
		OlatDocument oDocument = new OlatDocument();
		oDocument.setId(item.getKey());
		oDocument.setCreatedDate(item.getCreationDate());
		oDocument.setLastChange(item.getLastModified());
		oDocument.setTitle(item.getTitle());
		oDocument.setDescription(item.getDescription());
		oDocument.setResourceUrl(getResourceUrl(item.getKey()));
		oDocument.setDocumentType(QItemDocument.TYPE);
		oDocument.setCssIcon("o_qitem_icon");
		oDocument.setParentContextType(searchResourceContext.getParentContextType());
		oDocument.setParentContextName(searchResourceContext.getParentContextName());

		//author
		StringBuilder authorSb = new StringBuilder();
		List<Identity> owners = qpoolService.getAuthors(item);
		for(Identity owner:owners) {
			User user = owner.getUser();
			authorSb.append(user.getProperty(UserConstants.FIRSTNAME, null))
			  .append(" ")
			  .append(user.getProperty(UserConstants.LASTNAME, null))
			  .append(" ");
		}
		oDocument.setAuthor(authorSb.toString());
		
		//add specific fields
		Document document = oDocument.getLuceneDocument();
		
		//content
		QPoolSPI provider = qpoolModule.getQuestionPoolProvider(item.getFormat());
		if(provider != null) {
			String content = provider.extractTextContent(item);
			if(content != null) {
				addStringField(document, AbstractOlatDocument.CONTENT_FIELD_NAME, content);
			}
		}
		if(item.getDescription() != null) {
			addStringField(document, AbstractOlatDocument.CONTENT_FIELD_NAME, item.getDescription());
		}
		
		//general fields
		addStringField(document, QItemDocument.IDENTIFIER_FIELD, item.getIdentifier());
		addStringField(document, QItemDocument.MASTER_IDENTIFIER_FIELD,  item.getMasterIdentifier());
		addTextField(document, QItemDocument.KEYWORDS_FIELD, item.getKeywords());
		addTextField(document, QItemDocument.COVERAGE_FIELD, item.getCoverage());
		addTextField(document, QItemDocument.ADD_INFOS_FIELD, item.getAdditionalInformations());
		addStringField(document, QItemDocument.LANGUAGE_FIELD,  item.getLanguage());
		addTextField(document, QItemDocument.TOPIC_FIELD, item.getTopic());
		
		//educational
		if (qpoolModule.isEducationalContextEnabled()) {
			if(item.getEducationalContext() != null) {
				String context = item.getEducationalContext().getLevel();
				addStringField(document, QItemDocument.EDU_CONTEXT_FIELD,  context);
			}
		}
		
		//question
		if(item.getType() != null) {
			String itemType = item.getType().getType();
			addStringField(document, QItemDocument.ITEM_TYPE_FIELD,  itemType);
		}
		addStringField(document, QItemDocument.ASSESSMENT_TYPE_FIELD, item.getAssessmentType());
		
		//lifecycle
		addStringField(document, QItemDocument.ITEM_VERSION_FIELD, item.getItemVersion());
		if(item.getQuestionStatus() != null) {
			addStringField(document, QItemDocument.ITEM_STATUS_FIELD, item.getQuestionStatus().name());
		}
		
		//rights
		ResourceLicense license = licenseService.loadLicense(item);
		if(license != null && license.getLicenseType() != null) {
			String licenseKey = String.valueOf(license.getLicenseType().getKey());
			addTextField(document, QItemDocument.LICENSE_TYPE_FIELD_NAME, licenseKey);
		}

		//technical
		addTextField(document, QItemDocument.EDITOR_FIELD, item.getEditor());
		addStringField(document, QItemDocument.EDITOR_VERSION_FIELD, item.getEditorVersion());
		addStringField(document, QItemDocument.FORMAT_FIELD, item.getFormat());

		//save owners key
		for(Identity owner:owners) {
			document.add(new StringField(QItemDocument.OWNER_FIELD, owner.getKey().toString(), Field.Store.NO));
		}
		
		//link resources
		List<OLATResource> resources = questionItemDao.getSharedResources(item);
		for(OLATResource resource:resources) {
			document.add(new StringField(QItemDocument.SHARE_FIELD, resource.getKey().toString(), Field.Store.NO));
		}
		
		//need pools
		List<Pool> pools = poolDao.getPools(item);
		for(Pool pool:pools) {
			document.add(new StringField(QItemDocument.POOL_FIELD, pool.getKey().toString(), Field.Store.NO));
		}

		//need path
		if (qpoolModule.isTaxonomyEnabled()) {
			String path = item.getTaxonomicPath();
			if(StringHelper.containsNonWhitespace(path)) {
				for(StringTokenizer tokenizer = new StringTokenizer(path, "/"); tokenizer.hasMoreTokens(); ) {
					String nextToken = tokenizer.nextToken();
					document.add(new TextField(QItemDocument.TAXONOMIC_PATH_FIELD, nextToken, Field.Store.NO));
				}
				if(item instanceof QuestionItemImpl) {
					Long key = ((QuestionItemImpl)item).getTaxonomyLevel().getKey();

					TextField field = new TextField(QItemDocument.TAXONOMIC_FIELD, key.toString(), Field.Store.YES);
					document.add(field);
				}
			}
		}
		return document;
	}
	
	private void addStringField(Document doc, String fieldName, String content) {
		if(StringHelper.containsNonWhitespace(content)) {
			TextField field = new TextField(fieldName, content, Field.Store.YES);
			doc.add(field);
		}
	}
	
	/**
	 * indexed and tokenized
	 * @param fieldName
	 * @param content
	 * @param boost
	 * @return
	 */
	private void addTextField(Document doc, String fieldName, String content) {
		if(StringHelper.containsNonWhitespace(content)) {
			TextField field = new TextField(fieldName, content, Field.Store.YES);
			doc.add(field);
		}
	}
}
