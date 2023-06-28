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
package org.olat.modules.openbadges.ui;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * Initial date: 2023-06-28<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BadgeImageRenderer extends DefaultComponentRenderer implements ComponentRenderer {
	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
								Translator translator, RenderResult renderResult, String[] args) {
		BadgeImageComponent badgeImageComponent = (BadgeImageComponent) source;

		BadgeImageComponent.Size size = badgeImageComponent.getSize();
		String sizeString = "width: " + size.getWidth() + "px; height: " + size.getHeight() + "px; ";
		String maxSizeString = "max-width: " + size.getWidth() + "px; max-height: " + size.getHeight() + "px; ";

		sb.append("<div ");
		sb.append("style=\"margin: auto; text-align: center; ").append(sizeString).append("\" ");
		sb.append(">");
		sb.append("<img alt=\"\" ");
		sb.append("style=\"").append(maxSizeString).append("\" ");
		sb.append("src=\"").append(badgeImageComponent.getImageUrl()).append("\" ");
		sb.append(">");
		sb.append("</div>");
	}
}
