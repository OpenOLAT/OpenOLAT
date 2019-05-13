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

package org.olat.search.service.document;

import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.lucene.document.Document;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.User;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.Filter;
import org.olat.core.util.filter.FilterFactory;
import org.olat.core.util.resource.OresHelper;
import org.olat.portfolio.EPArtefactHandler;
import org.olat.portfolio.PortfolioModule;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.manager.EPPolicyManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.structel.EPAbstractMap;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.search.model.OlatDocument;
import org.olat.search.service.SearchResourceContext;

/**
 * 
 * Description:<br>
 * Deliver the lucene document made from a portfolio
 * 
 * <P>
 * Initial Date:  12 nov. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PortfolioMapDocument extends OlatDocument {

	private static final long serialVersionUID = -7960651550499734346L;
	private static final Logger log = Tracing.createLoggerFor(PortfolioMapDocument.class);
	
	private static EPFrontendManager ePFMgr; 
	private static EPPolicyManager policyManager;
	private static PortfolioModule portfolioModule;

	public PortfolioMapDocument() {
		super();
		ePFMgr = CoreSpringFactory.getImpl(EPFrontendManager.class);
		policyManager = CoreSpringFactory.getImpl(EPPolicyManager.class);
		portfolioModule = CoreSpringFactory.getImpl(PortfolioModule.class);
	}

	public static Document createDocument(SearchResourceContext searchResourceContext, PortfolioStructure map) {		
		PortfolioMapDocument document = new PortfolioMapDocument();
		if(map instanceof EPAbstractMap) {
			EPAbstractMap abstractMap = (EPAbstractMap)map;
  		if(abstractMap.getGroups() != null) {
  			List<Identity> identities = policyManager.getOwners(abstractMap);
  			StringBuilder authors = new StringBuilder();
  			for(Identity identity:identities) {
  				if(authors.length() > 0) {
  					authors.append(", ");
  				}
  				User user = identity.getUser();
  				authors.append(user.getProperty(UserConstants.FIRSTNAME, null))
  					.append(' ').append(user.getProperty(UserConstants.LASTNAME, null));
  			}
  			document.setAuthor(authors.toString());
  		}
			document.setCreatedDate(abstractMap.getCreationDate());
		}
		
		Filter filter = FilterFactory.getHtmlTagAndDescapingFilter();
		
  	document.setTitle(map.getTitle());
  	document.setDescription(filter.filter(map.getDescription()));
  	StringBuilder sb = new StringBuilder();
  	getContent(map, searchResourceContext, sb, filter);
		document.setContent(sb.toString());
		document.setResourceUrl(searchResourceContext.getResourceUrl());
		document.setDocumentType(searchResourceContext.getDocumentType());
		document.setCssIcon("o_ep_icon");
		document.setParentContextType(searchResourceContext.getParentContextType());
		document.setParentContextName(searchResourceContext.getParentContextName());
		
		if (log.isDebugEnabled()) log.debug(document.toString());
		return document.getLuceneDocument();
	}
	
	private static String getContent(PortfolioStructure map, SearchResourceContext resourceContext, StringBuilder sb, Filter filter) {
		sb.append(' ').append(map.getTitle());
		if(StringHelper.containsNonWhitespace(map.getDescription())) {
			sb.append(' ').append(filter.filter(map.getDescription()));
		}
		for(PortfolioStructure child:ePFMgr.loadStructureChildren(map)) {
			getContent(child, resourceContext, sb, filter);
		}
		for(AbstractArtefact artefact:ePFMgr.getArtefacts(map)) {
			String reflexion = artefact.getReflexion();
			if(StringHelper.containsNonWhitespace(reflexion)) {
				sb.append(' ').append(filter.filter(reflexion));
			}
			
			OLATResourceable ores = OresHelper.createOLATResourceableInstance(AbstractArtefact.class.getSimpleName(), artefact.getKey());
			EPArtefactHandler<?> handler = portfolioModule.getArtefactHandler(artefact.getResourceableTypeName());

			SearchResourceContext artefactResourceContext = new SearchResourceContext(resourceContext);
			artefactResourceContext.setBusinessControlFor(ores);
			OlatDocument doc = handler.getIndexerDocument(artefactResourceContext, artefact, ePFMgr);
			sb.append(' ').append(doc.getContent());
		}
		return sb.toString();
	}
}