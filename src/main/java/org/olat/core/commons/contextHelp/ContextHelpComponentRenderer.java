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
package org.olat.core.commons.contextHelp;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * Draw the link to the context help.
 *
 *
 * Initial date: 17.06.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ContextHelpComponentRenderer extends DefaultComponentRenderer {

	@Override
	public void render(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {

		ContextHelpComponent cmp = (ContextHelpComponent)source;
		String hoverTextKey = cmp.getHoverTextKey();
		String packageName = cmp.getPackageName();
		String pageName = cmp.getPageName();
		String cId = cmp.getDispatchID();

		String hooverText = translator.translate(hoverTextKey);
		if (hooverText != null) {
			hooverText = StringEscapeUtils.escapeHtml(hooverText);
		}
		sb.append("<a id='").append(cId).append("' href=\"javascript:contextHelpWindow('");
		Renderer.renderNormalURI(sb, "help/");
		sb.append(translator.getLocale().toString()).append("/").append(packageName).append("/").append(pageName)
		  .append("')\" title=\"").append(hooverText).append("\" class=\"o_chelp\"><i class='o_icon o_icon_help'></i> ")
		  .append(translator.translate("help"))
		  .append("</a>");
	}
}
