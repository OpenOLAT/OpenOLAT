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
package org.olat.modules.portfolio.model;

import org.olat.core.id.Identity;
import org.olat.modules.portfolio.PortfolioElement;
import org.olat.modules.portfolio.PortfolioRoles;

/**
 * 
 * Initial date: 16.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AccessRightChange {
	
	private final PortfolioRoles role;
	private final PortfolioElement element;
	private final Identity identity;
	private final boolean add;
	
	public AccessRightChange(PortfolioRoles role, PortfolioElement element, Identity identity, boolean add) {
		this.role = role;
		this.element = element;
		this.identity = identity;
		this.add = add;
	}
	
	public PortfolioRoles getRole() {
		return role;
	}
	
	public PortfolioElement getElement() {
		return element;
	}
	
	public Identity getIdentity() {
		return identity;
	}
	
	public boolean isAdd() {
		return add;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("accessRightChange[").append(element.getType().name()).append("=").append(element.getKey()).append(":")
			.append(role.name()).append(":").append(add ? "add" : "remove").append("]");
		return sb.toString();
	}
}
