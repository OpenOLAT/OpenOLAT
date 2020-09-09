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
package org.olat.core.gui.components.form.flexible.impl.elements.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.olat.core.logging.AssertException;

/**
 * 
 * Initial date: 15 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FlexiTreeNodeComparator implements Comparator<FlexiTreeTableNode> {

	@Override
	public int compare(FlexiTreeTableNode o1, FlexiTreeTableNode o2) {
		if(o1 == null || o2 == null) {
			return compareNullObjects(o1, o2);
		}
		return compareParentOf(o1, o2);
	}
	
	protected int compareParentOf(FlexiTreeTableNode o1, FlexiTreeTableNode o2) {
		FlexiTreeTableNode p1 = o1.getParent();
		FlexiTreeTableNode p2 = o2.getParent();
		
		int c = 0;
		if(p1 == null && p2 == null) {
			c = compareNodes(o1, o2);
		} else if(p1 != null && p1.equals(p2)) {
			c = compareNodes(o1, o2);
		} else if(p1 == null) {
			FlexiTreeTableNode r2 = root(p2);
			if(o1.equals(r2)) {
				c = -1;
			} else {
				c = compareNodes(o1, r2);
			}
		} else if(p2 == null) {
			FlexiTreeTableNode r1 = root(p1);
			if(r1.equals(o2)) {
				c = 1;
			} else {
				c = compareNodes(r1, o2);
			}
		} else {
			List<FlexiTreeTableNode> parentLine1 = getParentLine(o1);
			List<FlexiTreeTableNode> parentLine2 = getParentLine(o2);

			FlexiTreeTableNode pp1;
			FlexiTreeTableNode pp2;
			if(parentLine1.size() <= parentLine2.size()) {
				pp1 = parentLine1.get(parentLine1.size() - 1);
				pp2 = parentLine2.get(parentLine1.size() - 1);
			} else {
				pp1 = parentLine1.get(parentLine2.size() - 1);
				pp2 = parentLine2.get(parentLine2.size() - 1);
			}

			if(pp1.equals(pp2)) {
				c = Integer.compare(parentLine1.size(), parentLine2.size());
			} else {
				int depth = Math.min(parentLine1.size(), parentLine2.size()) - 1;
				for(int i=depth; i-->0; ) {
					pp1 = parentLine1.get(i);
					pp2 = parentLine2.get(i);
					if(pp1.equals(pp2)) {
						pp1 = parentLine1.get(i + 1);
						pp2 = parentLine2.get(i + 1);
						break;
					}	
				}
				if(pp1.equals(pp2)) {
					c = Integer.compare(parentLine1.size(), parentLine2.size());
				} else {
					c = compareNodes(pp1, pp2);
				}
			}
		}
		return c;
	}
	
	private FlexiTreeTableNode root(FlexiTreeTableNode node) {
		FlexiTreeTableNode root = node;
		int i = 0;
		for(FlexiTreeTableNode parent=node.getParent(); parent != null && i < 100; parent = parent.getParent(), i++) {
			root = parent;
		}
		return root;
	}
	
	private List<FlexiTreeTableNode> getParentLine(FlexiTreeTableNode node) {
		List<FlexiTreeTableNode> nodes = new ArrayList<>();
		for(FlexiTreeTableNode parent=node; parent != null; parent = parent.getParent()) {
			nodes.add(parent);
			if(nodes.size() > 255) {
				throw new AssertException("Flexi tree parent line in an infinite loop");
			}
		}
		Collections.reverse(nodes);
		return nodes;
	}
	
	protected int compareNodes(FlexiTreeTableNode o1, FlexiTreeTableNode o2) {
		String c1 = o1.getCrump();
		String c2 = o2.getCrump();
		if(c1 == null || c2 == null) {
			return compareNullObjects(c1, c2);
		}
		return c1.compareTo(c2);
	}

	protected final int compareNullObjects(final Object a, final Object b) {
		boolean ba = (a == null);
		boolean bb = (b == null);
		return ba? (bb? 0: -1):(bb? 1: 0);
	}
}
