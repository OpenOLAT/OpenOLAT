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
package org.olat.group.ui.main;

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.group.BusinessGroupShort;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class GroupCellRenderer implements CustomCellRenderer {

	@Override
	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
		if (val instanceof MemberView) {
			MemberView member = (MemberView)val;
			List<BusinessGroupShort> groups = member.getGroups();
			if(groups != null && !groups.isEmpty()) {
				boolean and = false;
				for(BusinessGroupShort group:groups) {
					and = and(sb, and);
					if(group.getName() == null) {
						sb.append(group.getKey());
					} else {
						sb.append(group.getName());
					}
				}
			}
		}
	}
	
	private final boolean and(StringOutput sb, boolean and) {
		if(and) sb.append(", ");
		return true;
	}
}
