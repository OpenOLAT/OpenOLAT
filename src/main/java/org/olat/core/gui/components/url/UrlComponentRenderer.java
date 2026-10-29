/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.url;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.DefaultComponentRenderer;
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 *
 * Initial date: 29 Oct 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class UrlComponentRenderer extends DefaultComponentRenderer {
	
	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu,
			Translator translator, RenderResult renderResult, String[] args) {
		UrlComponent cmp = (UrlComponent)source;
		Translator urlTranslator = Util.createPackageTranslator(UrlComponent.class, translator.getLocale());
		String id = "o_url_" + cmp.getDispatchID();
		String qrPanelId = id + "_qr";
		
		sb.append("<div id=\"").append(id).append("\" class=\"o_url_component\"")
		  .append(" data-info-title=\"").appendHtmlAttributeEscaped(urlTranslator.translate("info.header")).append("\"")
		  .append(" data-copied=\"").appendHtmlAttributeEscaped(urlTranslator.translate("url.copied")).append("\"")
		  .append(" data-qr-show=\"").appendHtmlAttributeEscaped(urlTranslator.translate("url.qr.show")).append("\"")
		  .append(" data-qr-hide=\"").appendHtmlAttributeEscaped(urlTranslator.translate("url.qr.hide")).append("\">");
		sb.append("<div class=\"input-group\">");
		sb.append("<input type=\"text\" id=\"").append(cmp.getFormDispatchId()).append("\" class=\"form-control o_url_input\" readonly")
		  .append(" value=\"").appendHtmlAttributeEscaped(cmp.getUrl() == null ? "" : cmp.getUrl()).append("\"");
		if (StringHelper.containsNonWhitespace(cmp.getAriaLabel())) {
			sb.append(" aria-label=\"").appendHtmlAttributeEscaped(cmp.getAriaLabel()).append("\"");
		}
		sb.append(">");
		sb.append("<span class=\"input-group-btn\">");
		appendButton(sb, "o_url_copy", "o_icon_copy", urlTranslator.translate("url.copy"), null);
		if (cmp.isQrCodeEnabled()) {
			appendButton(sb, "o_url_qr", "o_icon_qrcode", urlTranslator.translate("url.qr.show"), qrPanelId);
		}
		sb.append("</span>");
		sb.append("</div>");
		if (cmp.isQrCodeEnabled()) {
			sb.append("<div id=\"").append(qrPanelId).append("\" class=\"o_url_qr_panel\" hidden>");
			sb.append("<div id=\"").append(qrPanelId).append("_code\" class=\"o_qrcode\"></div>");
			sb.append("<button type=\"button\" class=\"btn btn-default o_url_qr_download\">")
			  .append("<i class=\"o_icon o_icon_download\" aria-hidden=\"true\"></i> ")
			  .appendHtmlEscaped(urlTranslator.translate("url.qr.download"))
			  .append("</button>");
			sb.append("</div>");
		}
		sb.append("<script>\"use strict\";o_urlComponent('").append(id).append("');</script>");
		sb.append("</div>");
	}
	
	private void appendButton(StringOutput sb, String cssClass, String iconCss, String title, String controlsId) {
		sb.append("<button type=\"button\" class=\"btn btn-default ").append(cssClass).append("\"")
		  .append(" title=\"").appendHtmlAttributeEscaped(title).append("\"")
		  .append(" aria-label=\"").appendHtmlAttributeEscaped(title).append("\"");
		if (controlsId != null) {
			sb.append(" aria-expanded=\"false\" aria-controls=\"").append(controlsId).append("\"");
		}
		sb.append("><i class=\"o_icon ").append(iconCss).append("\" aria-hidden=\"true\"></i></button>");
	}
	
}
