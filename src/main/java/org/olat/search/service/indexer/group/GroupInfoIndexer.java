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
package org.olat.search.service.indexer.group;

import java.io.IOException;
import java.util.List;

import org.apache.lucene.document.Document;
import org.olat.commons.info.InfoMessage;
import org.olat.commons.info.InfoMessageFrontendManager;
import org.olat.commons.info.InfoMessageManager;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.resource.OresHelper;
import org.olat.group.BusinessGroup;
import org.olat.group.ui.run.BusinessGroupMainRunController;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.InfoMessageDocument;
import org.olat.search.service.indexer.AbstractHierarchicalIndexer;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * Initial Date: 20.03.2017
 * @author fkiefer, fabian.kiefer@frentix.com, www.frentix.com
 */
public class GroupInfoIndexer extends AbstractHierarchicalIndexer{
	
	private static final Logger log = Tracing.createLoggerFor(GroupInfoIndexer.class);

	public static final String TYPE = "type.group.info.message";
	
	private final static String SUPPORTED_TYPE_NAME = "org.olat.group.BusinessGroup";
	
	private InfoMessageManager infoMessageManager;

	/**
	 * [used by Spring]
	 * @param infoMessageManager
	 */
	public void setInfoMessageManager(InfoMessageManager infoMessageManager) {
		this.infoMessageManager = infoMessageManager;
	}
	
	@Override
	public String getSupportedTypeName() {
		return SUPPORTED_TYPE_NAME;
	}


	@Override
	public void doIndex(SearchResourceContext searchResourceContext, Object businessObject, OlatFullIndexer indexerWriter)
			throws IOException, InterruptedException {
		if (!(businessObject instanceof BusinessGroup)) throw new AssertException("businessObject must be BusinessGroup");
		BusinessGroup businessGroup = (BusinessGroup) businessObject;
		try {
			SearchResourceContext messagesGroupResourceContext = new SearchResourceContext(searchResourceContext);
		    messagesGroupResourceContext.setBusinessControlFor(BusinessGroupMainRunController.ORES_TOOLMSG);
		    messagesGroupResourceContext.setDocumentType(TYPE);
			doIndexInfos(messagesGroupResourceContext, businessGroup, indexerWriter);
		} catch (Exception ex) {
			log.error("Exception indexing businessGroup=" + businessGroup, ex);
		} catch (Error err) {
			log.error("Error indexing businessGroup=" + businessGroup, err);
		}
	}
	
	private void doIndexInfos(SearchResourceContext parentResourceContext, BusinessGroup businessGroup, OlatFullIndexer indexWriter)
	throws IOException, InterruptedException {
		List<InfoMessage> messages = infoMessageManager.loadInfoMessageByResource(businessGroup,
				InfoMessageFrontendManager.businessGroupResSubPath, null, null, null, 0, -1);
		for(InfoMessage message : messages) {
			SearchResourceContext searchResourceContext = new SearchResourceContext(parentResourceContext);
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(InfoMessage.class, message.getKey());
			searchResourceContext.setBusinessControlFor(ores);
			Document document = InfoMessageDocument.createDocument(searchResourceContext, message);
		  indexWriter.addDocument(document);
		}
	}
	
}
