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
package org.olat.course.noderight.ui;

import java.util.Comparator;

import org.olat.course.noderight.NodeRightGrant;
import org.olat.course.noderight.ui.NodeRightGrantDataModel.NodeRightGrantRow;

/**
 * 
 * Initial date: 5 Nov 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class NodeRightGrantRowComparator implements Comparator<NodeRightGrantRow> {

	@Override
	public int compare(NodeRightGrantRow row1, NodeRightGrantRow row2) {
		NodeRightGrant grant1 = row1.getGrant();
		NodeRightGrant grant2 = row2.getGrant();
		
		// Roles before identities
		if (grant1.getRole() != null && grant1.getRole() == null) {
			return -1;
		}
		
		// Roles are sorted according to the default role hierarchy.
		if (grant1.getRole() != null && grant2.getRole() != null) {
			int rolesCompare = grant1.getRole().ordinal() - grant2.getRole().ordinal();
			return rolesCompare != 0? rolesCompare: compateDates(grant1, grant2);
		}
		
		// Identities are sorted by name
		int nameCompare = row1.getName().compareToIgnoreCase(row2.getName());
		return nameCompare != 0? nameCompare: compateDates(grant1, grant2);
	}

	private int compateDates(NodeRightGrant grant1, NodeRightGrant grant2) {
		if (grant1.getStart() == null && grant2.getStart() == null) {
			return 0;
		}
		if (grant1.getStart() == null && grant2.getStart() != null) {
			return -1;
		}
		if (grant1.getStart() != null && grant2.getStart() == null) {
			return 1;
		}
		
		int startCompare = grant1.getStart().compareTo(grant2.getStart());
		if (startCompare != 0) {
			return startCompare;
		}
		
		if (grant1.getEnd() == null && grant2.getEnd() != null) {
			return -1;
		}
		if (grant1.getEnd() != null && grant2.getEnd() == null) {
			return 1;
		}
		
		return grant1.getEnd().compareTo(grant2.getEnd());
	}

}
