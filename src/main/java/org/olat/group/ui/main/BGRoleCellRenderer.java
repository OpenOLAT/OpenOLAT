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

import java.util.Locale;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.group.BusinessGroupMembership;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BGRoleCellRenderer implements CustomCellRenderer, FlexiCellRenderer {
	
	private final Translator trans;
	
	public BGRoleCellRenderer(Locale locale) {
		trans = Util.createPackageTranslator(BGRoleCellRenderer.class, locale);
	}

	@Override
	public void render(Renderer renderer, StringOutput sb, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		render(sb, cellValue);
	}

	@Override
	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
		render(sb, val);
	}
	
	private void render(StringOutput sb, Object val) {
		if (val instanceof BusinessGroupMembership) {
			BusinessGroupMembership membership = (BusinessGroupMembership)val;
			
			boolean and = false;
			if(membership.isOwner()) {
				and = and(sb, and);
				sb.append(trans.translate("owned.groups"));
			}
			if(membership.isParticipant()) {
				and = and(sb, and);
				sb.append(trans.translate("search.attendee"));
			}
			if(membership.isWaiting()) {
				and = and(sb, and);
				sb.append(trans.translate("search.waiting"));
			}
			
			//if(membership.isExternalUser()) {
				sb.append(" ").append(trans.translate("role.external.user"));
			//}
		} else if (val instanceof GroupRoles) {
			GroupRoles membership = (GroupRoles)val;
			switch(membership) {
				case coach: sb.append(trans.translate("owned.groups")); break;
				case participant: sb.append(trans.translate("search.attendee")); break;
				case waiting: sb.append(trans.translate("search.waiting")); break;
				default: break;
			}
		}
	}
	
	private final boolean and(StringOutput sb, boolean and) {
		if(and) sb.append(", ");
		return true;
	}
}
