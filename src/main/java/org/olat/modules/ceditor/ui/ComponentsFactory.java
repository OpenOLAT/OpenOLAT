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
package org.olat.modules.ceditor.ui;

import org.olat.core.gui.components.text.TextComponent;
import org.olat.core.gui.components.text.TextFactory;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.modules.ceditor.model.HTMLRawElement;
import org.olat.modules.ceditor.model.ParagraphElement;

/**
 * 
 * Initial date: 10 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ComponentsFactory {
	
	public static final String getElementCssClass(HTMLRawElement element) {
		int numOfColumns = element.getTextSettings().getNumOfColumns();
		return "o_ce_html_raw o_html_col" + numOfColumns;
	}

	public static final TextComponent getContent(HTMLRawElement element) {
		String content = Formatter.formatLatexFormulas(element.getContent());
		TextComponent cmp = TextFactory.createTextComponentFromString("htmlRawCmp" + CodeHelper.getRAMUniqueID(), content, null, false, null);
		cmp.setElementCssClass(getElementCssClass(element));
		return cmp;
	}
	
	public static final String getElementCssClass(ParagraphElement element) {
		int numOfColumns = element.getTextSettings().getNumOfColumns();
		return "o_ce_html_paragraph o_html_col" + numOfColumns;
	}
	
	public static final TextComponent getContent(ParagraphElement element) {
		String content = Formatter.formatLatexFormulas(element.getContent());
		TextComponent cmp = TextFactory.createTextComponentFromString("htmlParagraphCmp" + CodeHelper.getRAMUniqueID(), content, null, false, null);
		cmp.setElementCssClass(getElementCssClass(element));
		return cmp;
	}
}
