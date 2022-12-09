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

import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
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
public class UserAvatarCellRenderer implements FlexiCellRenderer {
	
	private final String avatarBaseURL;
	private final String transparentGif;
	
	public UserAvatarCellRenderer(String avatarBaseURL) {
		this.avatarBaseURL = avatarBaseURL;
		transparentGif = StaticMediaDispatcher.getStaticURI("images/transparent.gif");
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(cellValue instanceof RosterChannelInfos) {
			RosterChannelInfos infos = (RosterChannelInfos)cellValue;
			List<RosterEntry> entries = infos.getNonVipEntries();
			if(!entries.isEmpty()) {
				RosterEntry entry = entries.get(0);
				String name = entry.isAnonym() ? entry.getNickName() : entry.getFullName();
				target.append("<span class=\"o_portrait\"><img src=\"").append(transparentGif).append("\"")
				      .append(" alt=\"").append(name).append("\" title=\"").append(name).append("\"")
				      .append(" class=\"o_portrait_avatar_small\"")
				      .append(" style=\"background-image: url('").append(avatarBaseURL).append("/").append(entry.getIdentityKey().toString()).append("/portrait_small.jpg')\"></span>");
			}
		}
	}
}
