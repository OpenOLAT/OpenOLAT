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

/** 
 * Initial Date:  08.06.2010 <br>
 * @author rhaag
 */
public class EPStructuredMap extends EPAbstractMap {

	private static final long serialVersionUID = -6360377624351045630L;

	public EPStructuredMap() {
		//
	}

	/**
	 * links to Map from where its deriving
	 * @uml.property  name="structuredMapSource"
	 */
	private EPStructuredMapTemplate structuredMapSource;
	

	/**
	 * Getter of the property <tt>structuredMapSource</tt>
	 * @return  Returns the structuredMapSource.
	 * @uml.property  name="structuredMapSource"
	 */
	public PortfolioStructureMap getStructuredMapSource() {
		return structuredMapSource;
	}

	/**
	 * Setter of the property <tt>structuredMapSource</tt>
	 * @param structuredMapSource  The structuredMapSource to set.
	 * @uml.property  name="structuredMapSource"
	 */
	public void setStructuredMapSource(EPStructuredMapTemplate structuredMapSource) {
		this.structuredMapSource = structuredMapSource;
	}

	/**
	 * links to course wherein it once was assigned to a user
	 * @uml.property  name="targetResource"
	 */
	private EPTargetResource targetResource;


	/**
	 * Getter of the property <tt>targetResource</tt>
	 * @return  Returns the targetResource.
	 * @uml.property  name="targetResource"
	 */
	public EPTargetResource getTargetResource() {
		if(targetResource == null) {
			targetResource = new EPTargetResource();
		}
		return targetResource;
	}

	/**
	 * Setter of the property <tt>targetResource</tt>
	 * @param targetResource  The targetResource to set.
	 * @uml.property  name="targetResource"
	 */
	public void setTargetResource(EPTargetResource targetResource) {
		this.targetResource = targetResource;
	}

	/**
	 * as its from an portfolio-task, this might have a return date 
	 * @uml.property  name="returnDate"
	 */
	private Date returnDate;


	/**
	 * Getter of the property <tt>returnDate</tt>
	 * @return  Returns the returnDate.
	 * @uml.property  name="returnDate"
	 */
	public Date getReturnDate() {
		return returnDate;
	}

	/**
	 * Setter of the property <tt>returnDate</tt>
	 * @param returnDate  The returnDate to set.
	 * @uml.property  name="returnDate"
	 */
	public void setReturnDate(Date returnDate) {
		this.returnDate = returnDate;
	}
	
	private Date copyDate;

	public Date getCopyDate() {
		return copyDate;
	}

	public void setCopyDate(Date copyDate) {
		this.copyDate = copyDate;
	}
	
	private Date lastSynchedDate;

	public Date getLastSynchedDate() {
		return lastSynchedDate;
	}

	public void setLastSynchedDate(Date lastSynchedDate) {
		this.lastSynchedDate = lastSynchedDate;
	}
	
	private Date deadLine;

	public Date getDeadLine() {
		return deadLine;
	}

	public void setDeadLine(Date deadLine) {
		this.deadLine = deadLine;
	}

	@Override
	public boolean equals(Object obj) {
		if(obj == this) {
			return true;
		}
		if(obj instanceof EPStructuredMap) {
			return equalsByPersistableKey((EPStructuredMap)obj);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return getKey() == null ? -9254 : getKey().hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("epStructureMap[key=").append(getKey()).append(":")
		  .append("title=").append(getTitle()).append("]");
		return sb.toString();
	}
}
