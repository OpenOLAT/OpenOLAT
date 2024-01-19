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
package org.olat.core.gui.components.form.flexible.impl.elements.table;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;

/**
 * Works with the UserAvatarMapper, and interprets IndentityRef or Identity as cell's value.
 * 
 * Initial date: 5 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PortraitCellRenderer implements FlexiCellRenderer {

	private final String transparentGif;
	private final MapperKey avatarMapperKey;
	
	public PortraitCellRenderer(MapperKey avatarMapperKey) {
		this.avatarMapperKey = avatarMapperKey;

		StringOutput sb = new StringOutput(100);
		Renderer.renderStaticURI(sb, "images/transparent.gif");
		transparentGif = sb.toString();
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		target.append("<span class=\"o_portrait\" aria-hidden=\"true\"><span class=\"o_portrait\">")
		      .append("<img src=\"").append(transparentGif).append("\" class=\"o_portrait_avatar_small\"");
		if(cellValue instanceof Identity ident) {
			String firstName = ident.getUser().getLastName();
			String lastName = ident.getUser().getLastName();
			target.append(" alt=\"").append(lastName).append(", ").append(firstName).append("\"")
			      .append(" title=\"").append(lastName).append(", ").append(firstName).append("\"");
		}
		if(cellValue instanceof IdentityRef ident) {
			target.append(" style=\"background-image: url('").append(avatarMapperKey.getUrl()).append("/").append(ident.getKey()).append("/portrait_small.jpg')\"");
		}
		target.append("></span></span>");
	}
}
