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
package org.olat.user.ui.admin;

import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.instantMessaging.model.Presence;
import org.olat.user.UserPortraitComponent;

/**
 * 
 * Initial date: 13 mai 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityChatCellRenderer extends StaticFlexiCellRenderer {
	
	public static final String CMD_ONLINE_STATUS = "oOnlineStatus";
	private static final List<String> actions = List.of(CMD_ONLINE_STATUS);
	
	private boolean onlineStatusEnabled;
	private final Translator presenceTranslator;

	public IdentityChatCellRenderer(boolean onlineStatusEnabled, Locale locale) {
		super("", CMD_ONLINE_STATUS);
		this.onlineStatusEnabled = onlineStatusEnabled;
		presenceTranslator = Util.createPackageTranslator(UserPortraitComponent.class, locale);
	}
	
	@Override
	protected String getId(Object cellValue, int row, FlexiTableComponent source) {
		return getOtherOrganisationsId(row);
	}

	@Override
	public List<String> getActions() {
		return actions;
	}
	
	public static String getOtherOrganisationsId(int row) {
		return "o_c" + CMD_ONLINE_STATUS + "_" + row;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(cellValue instanceof String onlineStatus) {
			if("me".equals(onlineStatus)) {
				//no icon
			} else if (!onlineStatusEnabled) {
				// don't show the users status when not configured, only an icon to start a chat/message
				setIconLeftCSS("o_icon o_icon_status_chat");
				setLinkTitle(presenceTranslator.translate("user.info.chat"));
				super.render(renderer, target, cellValue, row, source, ubu, translator);
			}
			// standard case: available or unavailable (offline or dnd)
			else if(Presence.available.name().equals(onlineStatus)) {
				setIconLeftCSS("o_icon o_icon_status_available");
				setLinkTitle(presenceTranslator.translate("user.portrait.presence.available"));
				super.render(renderer, target, cellValue, row, source, ubu, translator);
			} else if(Presence.dnd.name().equals(onlineStatus)) {
				setIconLeftCSS("o_icon o_icon_status_dnd");
				setLinkTitle(presenceTranslator.translate("user.portrait.presence.dnd"));
				super.render(renderer, target, cellValue, row, source, ubu, translator);
			} else {
				setIconLeftCSS("o_icon o_icon_status_unavailable");
				setLinkTitle(presenceTranslator.translate("user.portrait.presence.unavailable"));
				super.render(renderer, target, cellValue, row, source, ubu, translator);
			}
		}
	}

	@Override
	protected String getLabel(Renderer renderer, Object cellValue, int row, FlexiTableComponent source, URLBuilder ubu,
			Translator translator) {
		return "";
	}
}
