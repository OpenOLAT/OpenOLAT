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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.search.service.indexer.repository.course;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.PortfolioCourseNode;
import org.olat.portfolio.manager.EPStructureManager;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.document.PortfolioMapDocument;
import org.olat.search.service.indexer.OlatFullIndexer;


/**
 * Description:<br>
 * Index template of a course node
 * 
 * <P>
 * Initial Date:  12 nov. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PortfolioCourseNodeIndexer implements CourseNodeIndexer {
	
	public final static String NODE_TYPE = "type.course.node.ep";
	private final static String SUPPORTED_TYPE_NAME = "org.olat.course.nodes.PortfolioCourseNode";
	
	private EPStructureManager structureManager;
	
/**
 * [used by Spring]
 * @param structureManager
 */
	public void setStructureManager(EPStructureManager structureManager) {
		this.structureManager = structureManager;
	}

	@Override
	public String getSupportedTypeName() {
		return SUPPORTED_TYPE_NAME;
	}

	@Override
	public void doIndex(SearchResourceContext searchResourceContext, ICourse course, CourseNode courseNode, OlatFullIndexer indexWriter)
	throws IOException, InterruptedException {
    PortfolioCourseNode portfolioNode = (PortfolioCourseNode)courseNode;
		RepositoryEntry repoEntry = portfolioNode.getReferencedRepositoryEntry();
		if(repoEntry != null) {
			OLATResource ores = repoEntry.getOlatResource();
			PortfolioStructure element = structureManager.loadPortfolioStructure(ores);
			
			SearchResourceContext courseNodeResourceContext = new SearchResourceContext(searchResourceContext);
	    courseNodeResourceContext.setBusinessControlFor(courseNode);
	    courseNodeResourceContext.setDocumentType(NODE_TYPE);
	    courseNodeResourceContext.setDocumentContext(course.getResourceableId() + " " + courseNode.getIdent());
			courseNodeResourceContext.setTitle(courseNode.getShortTitle());
	    courseNodeResourceContext.setDescription(courseNode.getLongTitle());

			Document document = PortfolioMapDocument.createDocument(courseNodeResourceContext, element);
			indexWriter.addDocument(document);
		}
	}

	@Override
	public boolean checkAccess(ContextEntry contextEntry, BusinessControl businessControl, Identity identity, Roles roles) {
		return true;
	}
}