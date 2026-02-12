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

import java.io.File;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.text.TextComponent;
import org.olat.core.gui.components.text.TextFactory;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.modules.ceditor.model.HTMLElement;
import org.olat.modules.ceditor.model.HTMLRawElement;
import org.olat.modules.ceditor.model.ParagraphElement;
import org.olat.modules.ceditor.model.StoredData;

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
		return getContent(element, false);
	}
	
	public static final TextComponent getContent(HTMLRawElement element, boolean editMode) {
		String content = Formatter.formatLatexFormulas(element.getContent());
		if (editMode) {
			content = deactivateLinks(content);
		}
		TextComponent cmp = TextFactory.createTextComponentFromString("htmlRawCmp" + CodeHelper.getRAMUniqueID(), content, null, false, null);
		cmp.setElementCssClass(getElementCssClass(element));
		return cmp;
	}
	
	public static final String getElementCssClass(ParagraphElement element) {
		int numOfColumns = element.getTextSettings().getNumOfColumns();
		return "o_ce_html_paragraph o_html_col" + numOfColumns;
	}
	
	public static final TextComponent getContent(ParagraphElement element) {
		return getContent(element, false);
	}
	
	public static final TextComponent getContent(ParagraphElement element, boolean editMode) {
		String content = Formatter.formatLatexFormulas(element.getContent());
		if (editMode) {
			content = deactivateLinks(content);
		}
		if(content != null) {
			// Workaround for copy/paste abuses within the editor (next step, replace with HTML parser)
			content = content
					.replace("class=\"o_page_part o_ed_htmlraw", "class=\"")
					.replace("class=\"o_page_part o_ed_htmlparagraph", "class=\"")
					.replace("class=\"o_ce_layout_predefined o_ce_html_raw o_html_col", "class=\"o")
					.replace("class=\"o_ce_layout_predefined o_ce_html_paragraph o_html_col", "class=\"o");
		}
		
		TextComponent cmp = TextFactory.createTextComponentFromString("htmlParagraphCmp" + CodeHelper.getRAMUniqueID(), content, null, false, null);
		cmp.setElementCssClass(getElementCssClass(element));
		return cmp;
	}

	public static final TextComponent getContent(ParagraphElement element, String placeholder) {
		String content = "<span class=\"o_text_placeholder\"><i class=\"o_icon o_icon-fw o_icon_align_left\"> </i> " + placeholder + "</span>";
		TextComponent cmp = TextFactory.createTextComponentFromString("htmlParagraphCmp" + CodeHelper.getRAMUniqueID(), content, null, false, null);
		cmp.setElementCssClass(getElementCssClass(element));
		return cmp;
	}
	
	public static final TextComponent getContent(HTMLRawElement element, String placeholder) {
		String content = "<span class=\"o_text_placeholder\"><i class=\"o_icon o_icon-fw o_icon_align_left\"> </i> " + placeholder + "</span>";
		TextComponent cmp = TextFactory.createTextComponentFromString("htmlRawCmp" + CodeHelper.getRAMUniqueID(), content, null, false, null);
		cmp.setElementCssClass(getElementCssClass(element));
		return cmp;
	}

	private static String deactivateLinks(String content) {
		return content.replaceAll("href=\"javascript:[^\"]*\"", "href=\"javascript:;\"");
	}
	
	public static String getCssClass(HTMLElement htmlElement, boolean inForm) {
		return BlockLayoutClassFactory.buildClass(htmlElement.getTextSettings(), inForm);
	}

	public static ImageComponent getImageComponent(UserRequest ureq, StoredData storedData) {
		if (storedData == null) {
			return null;
		}
		File mediaDir = new File(FolderConfig.getCanonicalRoot(), storedData.getStoragePath());
		File mediaVersionFile = new File(mediaDir, storedData.getRootFilename());;
		ImageComponent imageComponent = new ImageComponent(ureq.getUserSession(), "image");
		imageComponent.setDivImageWrapper(false);
		imageComponent.setMedia(mediaVersionFile);
		return imageComponent;
	}
}
