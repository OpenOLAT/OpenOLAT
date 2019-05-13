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

package org.olat.search.service.indexer.repository;

import java.io.IOException;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.portfolio.PortfolioModule;
import org.olat.portfolio.manager.EPStructureManager;
import org.olat.portfolio.model.structel.EPStructuredMapTemplate;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.PortfolioMapDocument;
import org.olat.search.service.indexer.AbstractHierarchicalIndexer;
import org.olat.search.service.indexer.OlatFullIndexer;

/**
 * 
 * Description:<br>
 * Index templates and only templates in the repository 
 * 
 * <P>
 * Initial Date:  12 nov. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class PortfolioRepositoryIndexer extends AbstractHierarchicalIndexer {
	
	private static final Logger log = Tracing.createLoggerFor(PortfolioRepositoryIndexer.class);
	
	public static final String TYPE = "type.repository.entry.ep";
	public static final String ORES_TYPE_EP = "EPStructuredMapTemplate";
	
	private EPStructureManager structureManager;

	private PortfolioModule portfolioModule;
	
	/**
	 * [used by Spring]
	 * @param structureManager
	 */
	public void setStructureManager(EPStructureManager structureManager) {
		this.structureManager = structureManager;
	}

	/**
	 * [used by Spring]
	 * @param portfolioModule
	 */
	public void setPortfolioModule(PortfolioModule portfolioModule) {
		this.portfolioModule = portfolioModule;
	}
	
	@Override
	public String getSupportedTypeName() {
		return ORES_TYPE_EP;
	}
	
	@Override
	public void doIndex(SearchResourceContext resourceContext, Object object, OlatFullIndexer indexWriter)
	throws IOException, InterruptedException {
		if(!portfolioModule.isEnabled()) return;
		if (log.isDebugEnabled()) log.debug("Index portfolio templates...");
		
		RepositoryEntry repositoryEntry = (RepositoryEntry)object;
		OLATResource ores = repositoryEntry.getOlatResource();
		PortfolioStructure element = structureManager.loadPortfolioStructure(ores);
		// only index templates
		if(element instanceof EPStructuredMapTemplate) {
			resourceContext.setDocumentType(TYPE);
			resourceContext.setParentContextType(TYPE);
			resourceContext.setParentContextName(repositoryEntry.getDisplayname());
			resourceContext.setFilePath(element.getKey().toString());

			Document document = PortfolioMapDocument.createDocument(resourceContext, element);
			indexWriter.addDocument(document);
		}
	}

	@Override
	public boolean checkAccess(ContextEntry contextEntry, BusinessControl businessControl, Identity identity, Roles roles) {
		return super.checkAccess(contextEntry, businessControl, identity, roles);
	}
}
