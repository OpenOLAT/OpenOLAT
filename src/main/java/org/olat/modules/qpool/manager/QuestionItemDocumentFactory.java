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
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.id.Identity;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionPoolService;
import org.olat.modules.qpool.model.QuestionItemDocument;
import org.olat.resource.OLATResource;
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
	private BaseSecurity securityManager;
	@Autowired
	private QuestionItemDAO questionItemDao;
	@Autowired
	private QuestionPoolService qpoolService;


	public Document createDocument(SearchResourceContext searchResourceContext, Long itemKey) {
		QuestionItem item = questionItemDao.loadById(itemKey);
		if(item != null) {
			return createDocument(searchResourceContext, item);
		}
		return null;
	}

	public Document createDocument(SearchResourceContext searchResourceContext, QuestionItem item) {	
		
		OlatDocument oDocument = new OlatDocument();
		oDocument.setId(item.getKey());
		oDocument.setCreatedDate(item.getCreationDate());
		oDocument.setTitle(item.getSubject());
		oDocument.setDescription(item.getDescription());
		oDocument.setResourceUrl("[QuestionItem:" + item.getKey() + "]");
		oDocument.setDocumentType(QuestionItemDocument.TYPE);
		oDocument.setCssIcon("o_qitem_icon");
		oDocument.setParentContextType(searchResourceContext.getParentContextType());
		oDocument.setParentContextName(searchResourceContext.getParentContextName());
		oDocument.setContent(item.getDescription());
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

		//save owners key
		Document document = oDocument.getLuceneDocument();
		for(Identity owner:owners) {
			document.add(new StringField(QuestionItemDocument.OWNER_FIELD, owner.getKey().toString(), Field.Store.NO));
		}
		
		//link resources
		List<OLATResource> resources = questionItemDao.getSharedResources(item);
		for(OLATResource resource:resources) {
			document.add(new StringField(QuestionItemDocument.SHARE_FIELD, resource.getKey().toString(), Field.Store.NO));
		}
		
		//need pools
		List<Pool> pools = poolDao.getPools(item);
		for(Pool pool:pools) {
			document.add(new StringField(QuestionItemDocument.POOL_FIELD, pool.getKey().toString(), Field.Store.NO));
		}

		//need path
		String path = item.getStudyFieldPath();
		if(StringHelper.containsNonWhitespace(path)) {
			for(StringTokenizer tokenizer = new StringTokenizer(path, "/"); tokenizer.hasMoreTokens(); ) {
				String nextToken = tokenizer.nextToken();
				document.add(new TextField(QuestionItemDocument.STUDY_FIELD, nextToken, Field.Store.NO));
			}
		}
		return document;
	}
	
	/**
	 * indexed and tokenized
	 * @param fieldName
	 * @param content
	 * @param boost
	 * @return
	 */
	protected Field createTextField(String fieldName, String content, float boost) {
		TextField field = new TextField(fieldName,content, Field.Store.YES);
		field.setBoost(boost);
		return field;
	}
}
