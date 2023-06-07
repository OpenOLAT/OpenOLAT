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
package org.olat.modules.project.ui.component;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.project.ui.component.ProjAvatarComponent.Size;

/**
 * 
 * Initial date: 10 May 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjAvatarRenderer extends DefaultComponentRenderer implements ComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		ProjAvatarComponent avatar = (ProjAvatarComponent)source;
		boolean isImageAvailable = StringHelper.containsNonWhitespace(avatar.getImageUrl());
		boolean isCssClassAvailable = StringHelper.containsNonWhitespace(avatar.getAvatarCssClass());
		
		if (isImageAvailable || isCssClassAvailable) {
			sb.append("<div aria-hidden=\"true\" class=\"o_proj_avatar ");
			if (Size.large == avatar.getSize()) {
				sb.append("o_proj_avatar_large ");
			} else if (Size.medium == avatar.getSize()) {
				sb.append("o_proj_avatar_medium ");
			} else if (Size.small == avatar.getSize()) {
				sb.append("o_proj_avatar_small ");
			}
			sb.append("\">");
			if (isImageAvailable) {
				sb.append("<img alt=\"\" class=\"o_proj_avatar_image ");
				if (avatar.isBorder()) {
					sb.append("o_proj_avatar_prevborder ");
				}
				sb.append("\" src=\"").append(avatar.getImageUrl()).append("\">");
			} else if (isCssClassAvailable) {
				sb.append("<div class=\"o_proj_avatar_image ");
				sb.append("o_proj_avatar_color " + avatar.getAvatarCssClass() + " ");
				if (avatar.isBorder()) {
					sb.append("o_proj_avatar_prevborder ");
				}
				sb.append("\">");
				sb.append(avatar.getAbbrev());
				sb.append("</div>");
			}
			// Border in a separate overlay div to keep the image dimension.
			if (avatar.isBorder()) {
				sb.append("<div class=\"o_proj_avatar_border\"></div>");
			}
			sb.append("</div>");
		}

	}

}
