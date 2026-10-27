/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.gui.components.figurecards;

import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.link.Link;
import org.olat.core.util.StringHelper;

/**
 *
 * Initial date: Oct 27, 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class FigureCardsFactory {
	
	public static final String DEFAULT_ICON = "o_icon_number_of";
	
	public static FigureCardsItem createItem(String name, FormItemContainer formLayout) {
		FigureCardsItemImpl item = new FigureCardsItemImpl(name);
		if (formLayout != null) {
			formLayout.add(item);
		}
		return item;
	}
	
	public static FormLink createCardLink(String name, String cmd, FormItemContainer formLayout) {
		FormLink link = FormUIFactory.getInstance().addFormLink(name, cmd, "", null, formLayout, Link.LINK + Link.NONTRANSLATED);
		link.setElementCssClass("o_figure_card");
		return link;
	}
	
	/**
	 * @param iconCss the icon, e.g. o_icon_user. Default is the hash icon.
	 * @param colorCss the color of the icon, e.g. o_figure_card_icon_blue. Default is the primary color.
	 */
	public static String createCardText(String iconCss, String colorCss, String figure, String label) {
		StringBuilder sb = new StringBuilder();
		sb.append("<span class='o_figure_card_icon");
		if (StringHelper.containsNonWhitespace(colorCss)) {
			sb.append(" ").append(colorCss);
		}
		sb.append("'><i class='o_icon ");
		sb.append(StringHelper.containsNonWhitespace(iconCss)? iconCss: DEFAULT_ICON);
		sb.append("' aria-hidden='true'> </i></span>");
		sb.append("<span class='o_figure_card_figure'>").append(StringHelper.blankIfNull(figure)).append("</span>");
		if (StringHelper.containsNonWhitespace(label)) {
			sb.append("<span class='o_figure_card_label' title='").append(StringHelper.escapeForHtmlAttribute(label)).append("'>");
			sb.append(StringHelper.escapeHtml(label));
			sb.append("</span>");
		}
		sb.append("<i class='o_icon o_icon_start o_figure_card_chevron' aria-hidden='true'> </i>");
		return sb.toString();
	}
	
}
