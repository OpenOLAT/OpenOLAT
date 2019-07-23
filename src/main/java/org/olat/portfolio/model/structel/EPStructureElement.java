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
package org.olat.portfolio.model.structel;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.commons.persistence.PersistenceHelper;
import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Formatter;
import org.olat.core.util.filter.FilterFactory;
import org.olat.portfolio.model.restriction.CollectRestriction;
import org.olat.resource.OLATResource;



/** 
 * Description:<br>
 * EPStructureElement is the base element in portfolios, can have page or map as children
 * <P>
 * Initial Date:  08.06.2010 <br>
 * @author rhaag
 */
public class EPStructureElement extends PersistentObject implements PortfolioStructure, OLATResourceable  {

	private static final long serialVersionUID = -4468638028435147963L;
	
	private OLATResource olatResource;
	private List<EPStructureToArtefactLink> artefacts;
	private List<EPStructureToStructureLink> children;
	
	/**
	 * 
	 */
	public EPStructureElement() {
		//
	}
	
	/**
	 * Need for synching
	 */
	private Long structureElSource;

	public Long getStructureElSource() {
		return structureElSource;
	}

	public void setStructureElSource(Long structureElSource) {
		this.structureElSource = structureElSource;
	}

	/**
	 * @uml.property  name="title"
	 */
	private String title;

	/**
	 * Getter of the property <tt>title</tt>
	 * @return  Returns the title.
	 * @uml.property  name="title"
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * Setter of the property <tt>title</tt>
	 * @param title  The title to set.
	 * @uml.property  name="title"
	 */
	public void setTitle(String title) {
		// OLAT-6439 truncate to allowed database limit
		this.title = PersistenceHelper.truncateStringDbSave(title, 512, true);
	}

	/**
	 * @uml.property  name="description"
	 */
	private String description;

	/**
	 * Getter of the property <tt>description</tt>
	 * @return  Returns the description.
	 * @uml.property  name="description"
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Setter of the property <tt>description</tt>
	 * @param description  The description to set.
	 * @uml.property  name="description"
	 */
	public void setDescription(String description) {
		// OLAT-6439 truncate to allowed database limit
		this.description = PersistenceHelper.truncateStringDbSave(description, 2024, true);
	}

	@Override
	public String getShortenedDescription() {
		String desc = getDescription();
		if(desc == null) {
			desc = "";
		} else if(desc.length() > 50) {
			//to remain valid html: remove html tags
			desc = FilterFactory.getHtmlTagAndDescapingFilter().filter(desc);
			desc = Formatter.truncate(desc, 50);
		}
		return desc;
	}
	
	/**
	 * [used by Hibernate]
	 * @return
	 */
	public List<EPStructureToStructureLink> getInternalChildren() {
		if(children == null) {
			children = new ArrayList<>();
		}
		return children;
	}

	public void setInternalChildren(List<EPStructureToStructureLink> children) {
		this.children = children;
	}
	
	/**
	 * [used by Hibernate]
	 * @return
	 */
	public List<EPStructureToArtefactLink> getInternalArtefacts() {
		if(artefacts == null) {
			artefacts = new ArrayList<>();
		}
		return artefacts;
	}
	

	public void setInternalArtefacts(List<EPStructureToArtefactLink> artefacts) {
		this.artefacts = artefacts;
	}

	/**
	 * editable / non-editable
	 * @uml.property  name="status"
	 */
	private String status;

	/**
	 * Getter of the property <tt>status</tt>
	 * @return  Returns the status.
	 * @uml.property  name="status"
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Setter of the property <tt>status</tt>
	 * @param status  The status to set.
	 * @uml.property  name="status"
	 */
	public void setStatus(String status) {
		this.status = status;
	}

	/**
	 * @uml.property  name="collectRestriction"
	 */
	private List<CollectRestriction> collectRestrictions;

	/**
	 * Getter of the property <tt>collectRestriction</tt>
	 * @return  Returns the collectRestriction.
	 * @uml.property  name="collectRestriction"
	 */
	public List<CollectRestriction> getCollectRestrictions() {
		if(collectRestrictions == null) {
			collectRestrictions = new ArrayList<>();
		}
		return collectRestrictions;
	}

	/**
	 * Setter of the property <tt>collectRestriction</tt>
	 * @param collectRestriction  The collectRestriction to set.
	 * @uml.property  name="collectRestriction"
	 */
	public void setCollectRestrictions(List<CollectRestriction> collectRestrictions) {
		this.collectRestrictions = collectRestrictions;
	}

	@Override
	public OLATResource getOlatResource() {
		return olatResource;
	}
	
	public void setOlatResource(OLATResource olatResource) {
		this.olatResource = olatResource;
	}

	@Override
	public Long getResourceableId() {
		return olatResource == null ? null : olatResource.getResourceableId(); 
	}

	@Override
	public String getResourceableTypeName() {
		return olatResource == null ? null : olatResource.getResourceableTypeName(); 
	}

	@Override
	public String getIcon(){
		return "o_ep_icon_struct";
	}


	/**
	 * @uml.property  name="root"
	 */
	private EPStructureElement root;

	/**
	 * Getter of the property <tt>root</tt>
	 * @return  Returns the root.
	 * @uml.property  name="root"
	 */
	public EPStructureElement getRoot() {
		return root;
	}

	/**
	 * Setter of the property <tt>root</tt>
	 * @param root  The root to set.
	 * @uml.property  name="root"
	 */
	public void setRoot(EPStructureElement root) {
		this.root = root;
	}
	
	/**
	 * 
	 */
	private PortfolioStructureMap rootMap;
	
	public PortfolioStructureMap getRootMap() {
		return rootMap;
	}

	public void setRootMap(PortfolioStructureMap rootMap) {
		this.rootMap = rootMap;
	}

	/**
	 * @param style The class to use for css-styling infos for this element
	 */
	public void setStyle(String style) {
		this.style = style;
	}

	/**
	 * @return Returns the style.
	 */
	public String getStyle() {
		return style;
	}
	//The class to use for css-styling infos for this element
	private String style;

	/**
	 * @param artefactRepresentationMode The artefactRepresentationMode (table, miniview) to set.
	 */
	@Override
	public void setArtefactRepresentationMode(String artefactRepresentationMode) {
		this.artefactRepresentationMode = artefactRepresentationMode;
	}

	/**
	 * @return Returns the artefactRepresentationMode (table, miniview)
	 */
	@Override
	public String getArtefactRepresentationMode() {
		return artefactRepresentationMode;
	}

	
	private String artefactRepresentationMode;

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("epStructureElement[key=").append(getKey()).append(":")
		  .append("title=").append(getTitle()).append("]");
		return sb.toString();
	}

	@Override
	public int hashCode() {
		return getKey() == null ? 97914 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof EPStructureElement) {
			EPStructureElement el = (EPStructureElement)obj;
			return getKey() != null && getKey().equals(el.getKey());
		}
		return false;
	}
}
