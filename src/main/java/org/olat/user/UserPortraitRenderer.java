/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.user;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.instantMessaging.model.Presence;

/**
 * 
 * Initial date: Feb 3, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class UserPortraitRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		
		UserPortraitComponent opc = (UserPortraitComponent)source;
		PortraitUser portraitUser = opc.getPortraitUser();
		
		if (portraitUser == null) {
			return;
		}
		
		sb.append("<div class=\"o_user_portrait ");
		sb.append(getSizeCssClass(opc.getSize()));
		sb.append("\">");
		sb.append("<div class=\"o_user_portrait_avatar\"");
		if (opc.isDisplayPresence() && portraitUser.getPresence() != null) {
			sb.append(" title=\"").append(getTranslatedPresence(opc.getCompTranslator(), portraitUser.getPresence())).append("\"");
		}
		sb.append(">");
		
		if (portraitUser.isPortraitAvailable()) {
			sb.append("<img class=\"o_user_portrait_image\" src=\"");
			sb.append(UserAvatarMapper.createPathFor(opc.getAvatarMapperUrl(), portraitUser.getPortraitImagePath(), opc.getSize()));
			sb.append("\" alt=\"");
			sb.append(opc.getCompTranslator().translate("user.portrait.alt", StringHelper.escapeHtml(portraitUser.getDisplayName())));
			sb.append("\"");
			sb.append(">");
		} else {
			sb.append("<div class=\"o_user_portrait_initials o_user_initials ");
			sb.append(portraitUser.getInitialsCss());
			sb.append("\">");
			sb.append(portraitUser.getInitials());
			sb.append("</div>");
		}
		sb.append("</div>"); // o_user_portrait_avatar
		
		sb.append("<div class=\"o_user_portrait_border\"></div>");
		
		if (opc.isDisplayPresence() && portraitUser.getPresence() != null) {
			sb.append("<div class=\"o_user_portrait_im\">");
			sb.append("<i class=\"");
			sb.append(getPresenceIconCss(portraitUser.getPresence()));
			sb.append("\"></i>");
			sb.append("</div>");
		}
		
		sb.append("</div>"); // o_user_portrait
	}
	
	private String getSizeCssClass(PortraitSize size) {
		return switch (size) {
		case xsmall -> "o_user_portrait_xsmall";
		case small -> "o_user_portrait_small";
		case medium -> "o_user_portrait_medium";
		case large -> "o_user_portrait_large";
		default -> "o_user_portrait_medium";
		};
	}

	private String getTranslatedPresence(Translator compTranslator, Presence presence) {
		return switch (presence) {
		case available -> compTranslator.translate("user.portrait.presence.available");
		case dnd -> compTranslator.translate("user.portrait.presence.dnd");
		case unavailable -> compTranslator.translate("user.portrait.presence.unavailable");
		default -> null;
		};
	}
	
	private String getPresenceIconCss(Presence presence) {
		return switch (presence) {
		case available -> "o_icon o_icon_status_available";
		case dnd -> "o_icon o_icon_status_dnd";
		case unavailable -> "o_icon o_icon_status_unavailable";
		default -> "";
		};
	}

}
