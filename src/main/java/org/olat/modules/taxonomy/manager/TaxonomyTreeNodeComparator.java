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
package org.olat.modules.taxonomy.manager;

import java.util.Comparator;

import org.olat.core.util.nodes.INode;
import org.olat.modules.taxonomy.model.TaxonomyTreeNode;
import org.olat.modules.taxonomy.model.TaxonomyTreeNodeType;

/**
 * 
 * Initial date: 30 oct. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyTreeNodeComparator implements Comparator<INode> {

	@Override
	public int compare(INode i1, INode i2) {
		if(i1 == null || i2 == null) {
			return compareNullObjects(i1, i2);
		}
		
		Integer s1 = null;
		Integer s2 = null;
		String title1 = null;
		String title2 = null;
		TaxonomyTreeNodeType type1 = null;
		TaxonomyTreeNodeType type2 = null;

		if(i1 instanceof TaxonomyTreeNode) {
			TaxonomyTreeNode t1 = (TaxonomyTreeNode)i1;
			title1 = t1.getTitle();
			type1 = t1.getType();
			if(t1.getTaxonomyLevel() != null) {
				s1 = t1.getTaxonomyLevel().getSortOrder();
			}
		}
		
		if(i2 instanceof TaxonomyTreeNode) {
			TaxonomyTreeNode t2 = (TaxonomyTreeNode)i2;
			title2 = t2.getTitle();
			type2 = t2.getType();
			if(t2.getTaxonomyLevel() != null) {
				s2 = t2.getTaxonomyLevel().getSortOrder();
			}
		}
		
		int c = 0;
		if(type1 == TaxonomyTreeNodeType.directory && type2 == TaxonomyTreeNodeType.directory) {
			c = 0;
		} else if(type1 == TaxonomyTreeNodeType.directory) {
			return -1;
		} else if(type2 == TaxonomyTreeNodeType.directory) {
			return 1;
		}
		
		if(c == 0) {
			if(s1 == null || s2 == null) {
				c = compareNullObjects(title1, title2);
			} else {
				c = s1.compareTo(s2);
			}
		}
		
		if(c == 0) {
			if(title1 == null || title2 == null) {
				c = compareNullObjects(title1, title2);
			} else {
				c = title1.compareTo(title2);
			}
		}
		return c;
	}
	
	private final int compareNullObjects(final Object a, final Object b) {
		boolean ba = (a == null);
		boolean bb = (b == null);
		return ba? (bb? 0: -1):(bb? 1: 0);
	}
}
