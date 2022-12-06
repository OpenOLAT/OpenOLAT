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
package org.olat.core.gui.components.updown;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 7 Feb 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class UpDownRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source,
			URLBuilder ubu, Translator translator, RenderResult renderResult,
			String[] args) {
		
		UpDown upDown = (UpDown) source;
		
		switch (upDown.getButtonLayout()) {
		case LINK_HORIZONTAL:
			renderLinkHorizontal(renderer, sb, args, upDown);
			break;
		case BUTTON_HORIZONTAL:
			renderButtonHorizontal(renderer, sb, args, upDown);
			break;
		default:
			break;
		}
	}

	private void renderLinkHorizontal(Renderer renderer, StringOutput sb, String[] args, UpDown upDown) {
		sb.append("<span class='o_updown'>");
		sb.append("<span class='o_hidden'>", upDown.isTopmost());
		renderer.render(upDown.getUp(), sb, args);
		sb.append("</span>", upDown.isTopmost());
		
		sb.append("<span class='o_hidden'>", upDown.isLowermost());
		renderer.render(upDown.getDown(), sb, args);
		sb.append("</span>", upDown.isLowermost());
		sb.append("</span>");
	}

	private void renderButtonHorizontal(Renderer renderer, StringOutput sb, String[] args, UpDown upDown) {
		sb.append("<div class='btn-group o_updown'>");
		upDown.getUp().setEnabled(!upDown.isTopmost());
		renderer.render(upDown.getUp(), sb, args);
		upDown.getDown().setEnabled(!upDown.isLowermost());
		renderer.render(upDown.getDown(), sb, args);
		sb.append("</div>");
	}

}
