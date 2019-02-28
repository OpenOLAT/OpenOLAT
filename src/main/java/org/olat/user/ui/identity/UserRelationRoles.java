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
package org.olat.user.ui.identity;

import java.util.List;

import org.olat.basesecurity.RelationRole;

/**
 * 
 * Initial date: 28 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserRelationRoles {
	
	private final List<RelationRole> selectedRoles;
	private final List<RelationRole> selectedContraRoles;
	
	public UserRelationRoles(List<RelationRole> selectedRoles, List<RelationRole> selectedContraRoles) {
		this.selectedRoles = selectedRoles;
		this.selectedContraRoles = selectedContraRoles;
	}

	public List<RelationRole> getSelectedRoles() {
		return selectedRoles;
	}

	public List<RelationRole> getSelectedContraRoles() {
		return selectedContraRoles;
	}
}
