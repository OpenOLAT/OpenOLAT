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
package org.olat.admin.user.groups;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.group.BusinessGroupMembership;

/**
 * 
 * Initial date: 14 sept. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GroupRoleCellRenderer implements FlexiCellRenderer {
	
	private final Translator translator;
	
	public GroupRoleCellRenderer(Translator translator) {
		this.translator = translator;
	}

	@Override
	public void render(Renderer renderer, StringOutput sb, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator translat) {

		if (cellValue instanceof GroupOverviewRow) {
			GroupOverviewRow value = (GroupOverviewRow)cellValue;
			BusinessGroupMembership membership = value.getMembership();
			if(membership != null) {
				boolean and = false;
				if(membership.isOwner()) {
					and = and(sb, and);
					sb.append(translator.translate("owned.groups"));
				}
				if(membership.isParticipant()) {
					and = and(sb, and);
					sb.append(translator.translate("search.attendee"));
				}
				if(membership.isWaiting()) {
					and = and(sb, and);
					sb.append(translator.translate("search.waiting"));
				}
				
				if(value.getInvitation() != null) {
					sb.append(" ").append(translator.translate("role.external.user"));
				}
			}
		}
	}
	
	private final boolean and(StringOutput sb, boolean and) {
		if(and) sb.append(", ");
		return true;
	}
}
