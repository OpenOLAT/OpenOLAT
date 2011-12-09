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
package org.olat.resource.accesscontrol.model;

import java.util.List;

import org.olat.group.BusinessGroup;

public class BusinessGroupAccess {
	
	private BusinessGroup group;
	private List<PriceMethodBundle> methods;
	
	public BusinessGroupAccess() {
		//
	}
	
	public BusinessGroupAccess(BusinessGroup group, List<PriceMethodBundle> methods) {
		this.group = group;
		this.methods = methods;
	}
	
	public BusinessGroup getGroup() {
		return group;
	}
	
	public void setGroup(BusinessGroup group) {
		this.group = group;
	}
	
	public List<PriceMethodBundle> getMethods() {
		return methods;
	}
	
	

}
