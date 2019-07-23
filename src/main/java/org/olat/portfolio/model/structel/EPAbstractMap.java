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

import java.util.Set;

/**
 * Initial Date:  11.06.2010 <br>
 * @author rhaag, roman.haag@frentix.com, http://www.frentix.com
 */
public abstract class EPAbstractMap extends EPStructureElement implements PortfolioStructureMap  {

	private static final long serialVersionUID = 3295737167134638317L;
	
	private Set<EPStructureElementToGroupRelation> groups;

	@Override
	public Set<EPStructureElementToGroupRelation> getGroups() {
		return groups;
	}

	public void setGroups(Set<EPStructureElementToGroupRelation> groups) {
		this.groups = groups;
	}
	
	@Override
	public String getIcon(){
		return "o_ep_icon_map";
	}
}
