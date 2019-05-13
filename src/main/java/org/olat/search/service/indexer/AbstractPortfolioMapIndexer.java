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

package org.olat.search.service.indexer;

import java.io.IOException;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.Tracing;
import org.olat.portfolio.PortfolioModule;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.structel.ElementType;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.PortfolioMapDocument;

/**
 * 
 * Description:<br>
 * Index portoflio maps
 * 
 * <P>
 * Initial Date:  15 nov. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public abstract class AbstractPortfolioMapIndexer extends AbstractHierarchicalIndexer {

	private static final Logger log = Tracing.createLoggerFor(AbstractHierarchicalIndexer.class);

	private PortfolioModule portfolioModule;
	private EPFrontendManager frontendManager;
	
	private static final int BATCH_SIZE = 500;
	
	/**
	 * [used by Spring]
	 * @param portfolioModule
	 */
	public void setPortfolioModule(PortfolioModule portfolioModule) {
		this.portfolioModule = portfolioModule;
	}

	/**
	 * [used by Spring]
	 * @param frontendManager
	 */
	public void setFrontendManager(EPFrontendManager frontendManager) {
		this.frontendManager = frontendManager;
	}
	
	protected abstract String getDocumentType();
	
	protected abstract ElementType getElementType();
	
	/**
	 * Allow to accept or refuse some map for indexing
	 * @param map
	 * @return
	 */
	protected boolean accept(PortfolioStructureMap map) {
		return map != null;
	}
	
	@Override
	public void doIndex(SearchResourceContext searchResourceContext, Object object, OlatFullIndexer indexerWriter)
	throws IOException, InterruptedException {
		if(!portfolioModule.isEnabled()) return;
		
		SearchResourceContext resourceContext = new SearchResourceContext();

		int firstResult = 0;
		List<PortfolioStructure> structures = null;
		do {
			structures = frontendManager.getStructureElements(firstResult, 500, getElementType());
			for(PortfolioStructure structure:structures) {
				if(structure instanceof PortfolioStructureMap) {
					PortfolioStructureMap map = (PortfolioStructureMap)structure;
					if(accept(map)) {
						resourceContext.setDocumentType(getDocumentType());
						resourceContext.setBusinessControlFor(map.getOlatResource());
						Document document = PortfolioMapDocument.createDocument(resourceContext, map);
						indexerWriter.addDocument(document);
					}
				}
			}
			firstResult += structures.size();
			
		} while(structures != null && structures.size() == BATCH_SIZE);
	}

	@Override
	public boolean checkAccess(ContextEntry contextEntry, BusinessControl businessControl, Identity identity, Roles roles) {
		try {
			OLATResourceable ores = contextEntry.getOLATResourceable();
			return !roles.isGuestOnly() && frontendManager.isMapVisible(identity, ores) && super.checkAccess(contextEntry, businessControl, identity, roles);
		} catch (Exception e) {
			log.warn("Couldn't ask if map is visible: " + contextEntry, e);
			return false;
		}
	}
}