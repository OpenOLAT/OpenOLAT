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
package org.olat.instantMessaging.ui.component;

import java.util.List;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.instantMessaging.RosterEntry;
import org.olat.instantMessaging.model.RosterChannelInfos;

/**
 * 
 * Initial date: 24 févr. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RosterEntryWithUnreadCellRenderer implements FlexiCellRenderer {
	
	private final boolean displayVip;
	
	public RosterEntryWithUnreadCellRenderer(boolean displayVip) {
		this.displayVip = displayVip;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(cellValue instanceof RosterChannelInfos) {
			RosterChannelInfos infos = (RosterChannelInfos)cellValue;
			String name = getName(infos, displayVip);
			if(infos.getUnreadMessages() != null && infos.getUnreadMessages().longValue() > 0) {
				target.append("<strong>").append(name)
				      .append(" (").append(infos.getUnreadMessages().toString()).append(")").append("</strong>");
			} else {
				target.append(name);
			}
		}
	}
	
	public static String getName(RosterChannelInfos row, boolean vip) {
		StringBuilder sb = new StringBuilder();
		List<RosterEntry> entries = row.getEntries();
		for(RosterEntry entry:entries) {
			if(entry.isVip() != vip) continue;
			
			if(sb.length() > 0) sb.append("; ");
			String val = getVisibleName(entry);
			if(val != null) {
				sb.append(val);
			}
		}
		return sb.toString();
	}
	
	private static String getVisibleName(RosterEntry entry) {
		String val;
		if(entry.isAnonym()) {
			val = entry.getNickName();
		} else {
			val = entry.getFullName();
		}
		return val;
	}
	
	

}
