/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.tree;

import java.util.Comparator;

import org.olat.core.util.nodes.INode;

/**
 * 
 * Initial date: Nov 12, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TreeNodeTitleComparator implements Comparator<INode> {

	@Override
	public int compare(INode o1, INode o2) {
		String title1 = null;
		String title2 = null;
		if (o1 instanceof TreeNode gtn1) {
			title1 = gtn1.getTitle();
		}
		if (o2 instanceof TreeNode gtn2) {
			title2 = gtn2.getTitle();
		}
		
		if (title1 == null && title2 == null) {
			return 0;
		} else if (title1 == null) {
			return -1; 
		} else if (title2 == null) {
			return 1;
		}
		return title1.compareToIgnoreCase(title2);
	}
	
}
