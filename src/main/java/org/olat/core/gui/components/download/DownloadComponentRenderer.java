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
package org.olat.core.gui.components.download;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.components.form.flexible.elements.DownloadLink;
import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;

/**
 * Description:<br>
 * This is the renderer for the DownloadComponent. The first optional render
 * argument is interpreted as a CSS class that will be added in addition to the
 * icon css class of the component (if set).
 * 
 * <P>
 * Initial Date: 09.12.2009 <br>
 * 
 * @author gnaegi
 */
public class DownloadComponentRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source,
			URLBuilder ubu, Translator translator, RenderResult renderResult,
			String[] args) {
		DownloadComponent comp = (DownloadComponent) source;
		if (comp.getDownloadMediaResource() == null)
			return;
		
		boolean form = args != null && args.length == 1 && "form".equals(args[0]);
		sb.append("<p class='form-control-static'>", form);

		sb.append("<a id='o_c").append(comp.getDispatchID()).append("' href=\"");
		if(comp.getFormItem() != null) {
			DownloadLink formItem = comp.getFormItem();
			String linkText = formItem.getLinkText();
			ubu.createCopyFor(comp).buildURI(sb, null, null, linkText, AJAXFlags.MODE_NORMAL);
		} else {
			ubu.buildURI(sb, null, null, AJAXFlags.MODE_NORMAL); // rendered in new window anyway
		}
		sb.append("\"");
		// Tooltip
		String tip = comp.getLinkToolTip();
		if (tip != null) {
			sb.append(" title=\"")
			  .append(StringHelper.escapeHtml(tip))
			  .append("\"");
		}
		sb.append(" target=\"_blank\">");
		// Icon css class
		String iconCssClass = comp.getLinkCssIconClass();
		String cssArg = (args != null && args.length > 1 ? args[0] : null); // optional render argument
		if (iconCssClass != null || cssArg != null) {
			sb.append("<i class=\"");
			if (iconCssClass != null) {
				sb.append("o_icon o_icon-fw ")
				  .append(iconCssClass)
				  .append(" ");
			}
			if (cssArg != null) {
				sb.append(cssArg);
			}
			sb.append("\"></i> ");
		}
		// Link Text
		String text = comp.getLinkText();
		if (text != null) {
			sb.append(text);
		}
		sb.append("</a>")
		  .append("</p>", form);
	}
}
