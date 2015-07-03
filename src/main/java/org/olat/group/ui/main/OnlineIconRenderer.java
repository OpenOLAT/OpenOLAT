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

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.table.CustomCssCellRenderer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.model.Presence;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class OnlineIconRenderer extends CustomCssCellRenderer {
	
	private final InstantMessagingModule imModule;
	
	public OnlineIconRenderer() {
		imModule = CoreSpringFactory.getImpl(InstantMessagingModule.class);
	}

	@Override
	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
		if(renderer != null) {
			super.render(sb, renderer, val, locale, alignment, action);
		}
	}

	@Override
	protected String getCssClass(Object val) {
		if ("me".equals(val)) {
			// special case: don't show any icon for myself
			return "";
		} else if (!imModule.isOnlineStatusEnabled()) {
			// don't show the users status when not configured, only an icon to start a chat/message
			return "o_icon o_icon_status_chat";
		}
		// standard case: available or unavailable (offline or dnd)
		else if(Presence.available.name().equals(val)) {
			return "o_icon o_icon_status_available";
		} else if(Presence.dnd.name().equals(val)) {
			return "o_icon o_icon_status_dnd";
		} else {
			return "o_icon o_icon_status_unavailable";
		}
	}

	@Override
	protected String getCellValue(Object val) {
		return "";
	}

	@Override
	protected String getHoverText(Object val) {
		return null;
	}
}
