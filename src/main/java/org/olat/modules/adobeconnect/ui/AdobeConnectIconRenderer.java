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
package org.olat.modules.adobeconnect.ui;

import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.adobeconnect.model.AdobeConnectSco;

/**
 * 
 * Initial date: 18 avr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AdobeConnectIconRenderer implements FlexiCellRenderer {

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(cellValue instanceof String) {
			String icon = getIcon((String)cellValue);
			if(StringHelper.containsNonWhitespace(icon)) {
				target.append("<i class='o_icon ").append(icon).append("'> </i>");
			}
		} else if(cellValue instanceof AdobeConnectSco) {
			String icon = getIcon((AdobeConnectSco)cellValue);
			if(StringHelper.containsNonWhitespace(icon)) {
				target.append("<i class='o_icon ").append(icon).append("'> </i>");
			}
		}
	}
	
	private String getIcon(AdobeConnectSco sco) {
		String icon = "o_filetype_file";
		
		String scoIcon = sco.getIcon();
		if("attachment".equals(scoIcon)) {
			String name = sco.getName();
			if(StringHelper.containsNonWhitespace(name)) {
				int lastIndex = name.lastIndexOf('.');
				if(lastIndex != -1 && lastIndex > (name.length() - 10)) {
					icon = CSSHelper.createFiletypeIconCssClassFor(name);
				} else if(name.startsWith("/") && name.endsWith("/")) {
					icon = "o_icon_link";
				}
			}
		} else if(StringHelper.containsNonWhitespace(scoIcon)) {
			icon = getIcon(scoIcon);
		}
		return icon;
	}
	
	private String getIcon(String val) {
		switch(val) {
			case "producer": return "o_icon_user";
			case "folder": return "o_filetype_folder";
			case "meeting": return "o_vc_icon";
			case "archive": return "o_filetype_mp4";
			default: return "o_filetype_file";
		}
	}
}
