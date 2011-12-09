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

import org.olat.basesecurity.SecurityGroup;

/**
 * 
 * Description:<br>
 * TODO: srosse Class Description for EPAbstractMap
 * 
 * <P>
 * Initial Date:  11.06.2010 <br>
 * @author rhaag, roman.haag@frentix.com, http://www.frentix.com
 */
public abstract class EPAbstractMap extends EPStructureElement implements PortfolioStructureMap  {

	/**
	 * @uml.property  name="ownerGroup"
	 */
	transient private SecurityGroup ownerGroup;


	/**
	 * Getter of the property <tt>ownerGroup</tt>
	 * @return  Returns the ownerGroup.
	 * @uml.property  name="ownerGroup"
	 */
	public SecurityGroup getOwnerGroup() {
		return ownerGroup;
	}

	/**
	 * Setter of the property <tt>ownerGroup</tt>
	 * @param ownerGroup  The ownerGroup to set.
	 * @uml.property  name="ownerGroup"
	 */
	public void setOwnerGroup(SecurityGroup ownerGroup) {
		this.ownerGroup = ownerGroup;
	}
	
	public String getIcon(){
		return "b_ep_map_icon";
	}
}
