/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.search.service.indexer.group;


import java.io.IOException;
import java.util.List;

import org.apache.lucene.document.Document;
import org.olat.collaboration.CollaborationTools;
import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.logging.AssertException;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.group.BusinessGroup;
import org.olat.group.ui.run.BusinessGroupMainRunController;
import org.olat.modules.wiki.Wiki;
import org.olat.modules.wiki.WikiManager;
import org.olat.modules.wiki.WikiPage;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.WikiPageDocument;
import org.olat.search.service.indexer.AbstractHierarchicalIndexer;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * Index all group folders.
 * @author Christian Guretzki
 */
public class GroupWikiIndexer extends AbstractHierarchicalIndexer {

	private static final Logger log = Tracing.createLoggerFor(GroupWikiIndexer.class);
	
  //Must correspond with LocalString_xx.properties
	// Do not use '_' because we want to seach for certain documenttype and lucene haev problems with '_' 
	public static final String TYPE = "type.group.wiki";

	@Override
	public void doIndex(SearchResourceContext parentResourceContext, Object businessObj, OlatFullIndexer indexWriter) throws IOException,InterruptedException {
		if (!(businessObj instanceof BusinessGroup))
			throw new AssertException("businessObj must be BusinessGroup");
		
		BusinessGroup businessGroup = (BusinessGroup)businessObj;
		
		// Index Group Wiki
		if (log.isDebugEnabled()) log.debug("Analyse Wiki for Group=" + businessGroup);
		CollaborationTools collabTools = CollaborationToolsFactory.getInstance().getOrCreateCollaborationTools(businessGroup);
		if (collabTools.isToolEnabled(CollaborationTools.TOOL_WIKI) ) {
			try {
				Wiki wiki = WikiManager.getInstance().getOrLoadWiki(businessGroup);
				// loop over all wiki pages
				List<WikiPage> wikiPageList = wiki.getAllPagesWithContent();
				for (WikiPage wikiPage : wikiPageList) {
					SearchResourceContext wikiResourceContext = new SearchResourceContext(parentResourceContext);
					wikiResourceContext.setBusinessControlFor(BusinessGroupMainRunController.ORES_TOOLWIKI);
					wikiResourceContext.setDocumentType(TYPE);
					wikiResourceContext.setFilePath(wikiPage.getPageName());
			
					Document document = WikiPageDocument.createDocument(wikiResourceContext, wikiPage);
					indexWriter.addDocument(document);
				}
			} catch (NullPointerException nex) {
				log.warn("NullPointerException in GroupWikiIndexer.doIndex.", nex);
			}
		} else {
			if (log.isDebugEnabled()) log.debug("Group=" + businessGroup + " has no Wiki.");
		}
	}
	
	@Override
	public String getSupportedTypeName() {
		return BusinessGroupMainRunController.ORES_TOOLWIKI.getResourceableTypeName();
	}
}