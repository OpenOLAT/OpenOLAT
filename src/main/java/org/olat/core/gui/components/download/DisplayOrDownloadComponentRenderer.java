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
import org.olat.core.gui.render.RenderResult;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

/**
 * Description:<br>
 * The DisplayOrDownloadFileComponentRenderer opens a window to download the
 * file with the given file url from the component
 * 
 * <P>
 * Initial Date: 05.11.2009 <br>
 * 
 * @author gnaegi
 */
class DisplayOrDownloadComponentRenderer extends DefaultComponentRenderer {

	@Override
	public void renderComponent(Renderer renderer, StringOutput sb, Component source, URLBuilder ubu, Translator translator,
			RenderResult renderResult, String[] args) {
		String fileUrl = ((DisplayOrDownloadComponent) source).consumeFileUrl();
		if (fileUrl != null) { 
			// use javascript to open a new file that loads the file.
			sb.append("<script>");
			sb.append("/* <![CDATA[ */ ");
			sb.append("jQuery(function() {");
			sb.append("  window.open('").append(fileUrl).append("', 'downloadwindow','');");
			sb.append("});");
			sb.append("/* ]]> */");
			sb.append("</script>");
		}
	}
}
