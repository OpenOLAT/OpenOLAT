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
package org.olat.modules.fo.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeNodeComparator;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTreeTableNode;

/**
 * 
 * Initial date: 5 Aug 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MessageTreeRowComparator  extends FlexiTreeNodeComparator {

	@Override
	protected int compareNodes(FlexiTreeTableNode o1, FlexiTreeTableNode o2) {
		if (o1 instanceof MessageLightViewRow && o2 instanceof MessageLightViewRow) {
			MessageLightViewRow row1 = (MessageLightViewRow)o1;
			MessageLightViewRow row2 = (MessageLightViewRow)o2;
			return compare(row1, row2);
			
		}
		return super.compareNodes(o1, o2);
	}
	
	private int compare(final MessageLightViewRow m1, final MessageLightViewRow m2) {
		if(m1.isSticky() && m2.isSticky()) {
			return m2.getLastModified().compareTo(m1.getLastModified()); //last first
		} else if(m1.isSticky()) {
			return -1;
		} else if(m2.isSticky()){
			return 1;
		} else {
			return m2.getLastModified().compareTo(m1.getLastModified()); //last first
		}
	}

}
