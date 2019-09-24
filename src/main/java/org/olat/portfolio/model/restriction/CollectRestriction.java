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
package org.olat.portfolio.model.restriction;

import org.olat.core.commons.persistence.PersistentObject;
import org.olat.core.util.StringHelper;

/** 
 * Initial Date:  08.06.2010 <br>
 * @author rhaag
 */
public class CollectRestriction extends PersistentObject {

	/**
	 * 
	 */
	public CollectRestriction() {
		//
	}
	
	public CollectRestriction(CollectRestriction cr) {
		artefactType = cr.artefactType;
		amount = cr.amount;
		restriction = cr.restriction;
	}
	
	public CollectRestriction(String artefactType, int amount, String restriction) {
		this.artefactType = artefactType;
		this.amount = amount;
		this.restriction = restriction;
	}

	/**
	 * @uml.property  name="artefactType"
	 */
	private String artefactType;

	/**
	 * Getter of the property <tt>artefactType</tt>
	 * @return  Returns the artefactType.
	 * @uml.property  name="artefactType"
	 */
	public String getArtefactType() {
		return artefactType;
	}

	/**
	 * Setter of the property <tt>artefactType</tt>
	 * @param artefactType  The artefactType to set.
	 * @uml.property  name="artefactType"
	 */
	public void setArtefactType(String artefactType) {
		this.artefactType = artefactType;
	}

	/**
	 * @uml.property  name="amount"
	 */
	private int amount;

	/**
	 * Getter of the property <tt>amount</tt>
	 * @return  Returns the amount.
	 * @uml.property  name="amount"
	 */
	public int getAmount() {
		return amount;
	}

	/**
	 * Setter of the property <tt>amount</tt>
	 * @param amount  The amount to set.
	 * @uml.property  name="amount"
	 */
	public void setAmount(int amount) {
		this.amount = amount;
	}

	/**
	 * @uml.property  name="restriction"
	 */
	private String restriction;

	/**
	 * Getter of the property <tt>restriction</tt>
	 * @return  Returns the restriction.
	 * @uml.property  name="restriction"
	 */
	public String getRestriction() {
		return restriction;
	}

	/**
	 * Setter of the property <tt>restriction</tt>
	 * @param restriction  The restriction to set.
	 * @uml.property  name="restriction"
	 */
	public void setRestriction(String restriction) {
		this.restriction = restriction;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? 17637 : getKey().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		} else if (obj instanceof CollectRestriction) {
			CollectRestriction cr = (CollectRestriction)obj;
			return getKey() != null && getKey().equals(cr.getKey());
		}
		return false;
	}

	/**
	 * @see org.olat.core.commons.persistence.PersistentObject#toString()
	 */
	@Override
	public String toString() {
		return "key: " + getKey() + " Restriction: " + getRestriction() + " amount: " + getAmount() + " of type: " + getArtefactType();
	}
	
	// basic check for validity. do not save invalid restrictions!
	public boolean isValid(){
		if (StringHelper.containsNonWhitespace(getRestriction()) && StringHelper.containsNonWhitespace(getArtefactType()) && getAmount() > 0) return true; 
		return false;
	}
	
}
