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
package org.olat.portfolio.model.structel;

import java.util.List;

import org.olat.core.id.OLATResourceable;
import org.olat.portfolio.model.restriction.CollectRestriction;
import org.olat.resource.OLATResource;

/**
 * 
 * Description:<br>
 * PortfolioStructure can be a map/page or struct-element
 * 
 * <P>
 * Initial Date:  11.06.2010 <br>
 * @author rhaag, roman.haag@frentix.com, http://www.frentix.com
 */
public interface PortfolioStructure extends OLATResourceable {
	
	public PortfolioStructure getRoot();
	
	public PortfolioStructureMap getRootMap();
	
	abstract Long getKey();
	
	abstract OLATResource getOlatResource();

	abstract Long getResourceableId();

	abstract String getResourceableTypeName();
	
	
	public String getTitle();
	
	public void setTitle(String title);
	
	public String getDescription();
	
	public void setDescription(String description);
	
	public String getShortenedDescription();
	
	public String getIcon();
	
	
	public List<CollectRestriction> getCollectRestrictions();
	
	public void setCollectRestrictions(List<CollectRestriction> restrictions);

	public String getArtefactRepresentationMode();
	
	public void setArtefactRepresentationMode(String artefRepMode);

}