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
package org.olat.user;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.user.UsersPortraitsComponent.PortraitLayout;

/**
 * 
 * Initial date: 7 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class UsersPortraitsRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		
		UsersPortraitsComponent opc = (UsersPortraitsComponent)source;
		
		sb.append("<div class=\"o_users_portraits ");
		sb.append("o_users_portraits_overlapping_portraits ", PortraitLayout.overlappingPortraits == opc.getPortraitLayout());
		sb.append("o_users_portraits_vertical_displayname ", PortraitLayout.verticalPortraitsDisplayName == opc.getPortraitLayout());
		sb.append(getSizeCssClass(opc.getSize()));
		sb.append("\">");
		sb.append("<ul class=\"list-unstyled\"");
		if (StringHelper.containsNonWhitespace(opc.getAriaLabel())) {
			sb.append(" aria-label=\"").append(opc.getAriaLabel()).append("\"");
		}
		sb.append(">");
		int numUsersVisible = opc.getMaxUsersVisible() < opc.getUserComps().size()? opc.getMaxUsersVisible(): opc.getUserComps().size();
		int numUsersNotVisible = opc.getUserComps().size() - numUsersVisible;
		for (int i = 0; i < numUsersVisible; i++) {
			UserPortraitComponent userPortraitComp = opc.getUserComps().get(i);
			sb.append("<li class=\"o_portrait_user\">");
			userPortraitComp.getHTMLRendererSingleton().render(renderer, sb, userPortraitComp, ubu, translator, renderResult, args);
			if (PortraitLayout.verticalPortraitsDisplayName == opc.getPortraitLayout()) {
				sb.append("<div class=\"o_displayname\">");
				sb.append(StringHelper.escapeHtml(userPortraitComp.getPortraitUser().getDisplayName()));
				sb.append("</div>");
			}
			sb.append("</li>");
		}

		sb.append("</ul>");
		if (numUsersNotVisible > 0) {
			sb.append("<span class=\"o_portrait_user_not_visible\">");
			sb.append("+").append(numUsersNotVisible);
			sb.append("</span>");
			
		}
		sb.append("</div>");
		
		opc.setDirty(false);
	}

	private String getSizeCssClass(PortraitSize size) {
		switch (size) {
		case xsmall: return "o_users_portraits_xsmall";
		case small: return "o_users_portraits_small";
		case medium: return "o_users_portraits_medium";
		case large: return "o_users_portraits_large";
		default:
		}
		return "";
	}

}
