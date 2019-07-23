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

import java.util.Date;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.id.Identity;
import org.olat.portfolio.model.artefacts.AbstractArtefact;

/**
 * Initial Date:  11.06.2010 <br>
 * @author rhaag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPStructureToArtefactLink extends PersistentObject {

	private static final long serialVersionUID = -7239075055788273545L;

	public EPStructureToArtefactLink() {
		//
	}

	/**
	 * @uml.property  name="creationDate"
	 */
	private Date creationDate;

	/**
	 * Getter of the property <tt>creationDate</tt>
	 * @return  Returns the creationDate.
	 * @uml.property  name="creationDate"
	 */
	public Date getCreationDate() {
		return creationDate;
	}

	/**
	 * Setter of the property <tt>creationDate</tt>
	 * @param creationDate  The creationDate to set.
	 * @uml.property  name="creationDate"
	 */
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	/**
	 * @uml.property  name="artefact"
	 */
	private AbstractArtefact artefact;

	/**
	 * Getter of the property <tt>artefact</tt>
	 * @return  Returns the artefact.
	 * @uml.property  name="artefact"
	 */
	public AbstractArtefact getArtefact() {
		return artefact;
	}

	/**
	 * Setter of the property <tt>artefact</tt>
	 * @param artefact  The artefact to set.
	 * @uml.property  name="artefact"
	 */
	public void setArtefact(AbstractArtefact artefact) {
		this.artefact = artefact;
	}

	/**
	 * @uml.property  name="structureElement"
	 */
	private PortfolioStructure structureElement;

	/**
	 * Getter of the property <tt>structureElement</tt>
	 * @return  Returns the structureElement.
	 * @uml.property  name="structureElement"
	 */
	public PortfolioStructure getStructureElement() {
		return structureElement;
	}

	/**
	 * Setter of the property <tt>structureElement</tt>
	 * @param structureElement  The structureElement to set.
	 * @uml.property  name="structureElement"
	 */
	public void setStructureElement(PortfolioStructure structureElement) {
		this.structureElement = structureElement;
	}

	/**
	 * @uml.property  name="reflexion"
	 */
	private String reflexion;

	/**
	 * Getter of the property <tt>reflexion</tt>
	 * @return  Returns the reflexion.
	 * @uml.property  name="reflexion"
	 */
	public String getReflexion() {
		return reflexion;
	}

	/**
	 * Setter of the property <tt>reflexion</tt>
	 * @param reflexion  The reflexion to set.
	 * @uml.property  name="reflexion"
	 */
	public void setReflexion(String reflexion) {
		this.reflexion = reflexion;
	}

	/**
	 * @uml.property  name="author"
	 */
	private Identity author;

	/**
	 * Getter of the property <tt>author</tt>
	 * @return  Returns the author.
	 * @uml.property  name="author"
	 */
	public Identity getAuthor() {
		return author;
	}

	/**
	 * Setter of the property <tt>author</tt>
	 * @param author  The author to set.
	 * @uml.property  name="author"
	 */
	public void setAuthor(Identity author) {
		this.author = author;
	}

	/**
	 * @uml.property  name="order"
	 */
	private int order;

	/**
	 * Getter of the property <tt>order</tt>
	 * @return  Returns the order.
	 * @uml.property  name="order"
	 */
	public int getOrder() {
		return order;
	}

	/**
	 * Setter of the property <tt>order</tt>
	 * @param order  The order to set.
	 * @uml.property  name="order"
	 */
	public void setOrder(int order) {
		this.order = order;
	}

}
