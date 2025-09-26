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
package org.olat.modules.ceditor.handler;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.text.TextComponent;
import org.olat.core.gui.components.text.TextFactory;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.CloneElementHandler;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.PageElementHandler;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.RenderingHints;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.PageService;
import org.olat.modules.ceditor.SimpleAddPageElementHandler;
import org.olat.modules.ceditor.model.BlockLayoutSettings;
import org.olat.modules.ceditor.model.TitleElement;
import org.olat.modules.ceditor.model.TitleSettings;
import org.olat.modules.ceditor.model.jpa.TitlePart;
import org.olat.modules.ceditor.ui.TextRunComponent;
import org.olat.modules.ceditor.ui.TitleEditorController;
import org.olat.modules.ceditor.ui.TitleInspectorController;

/**
 * 
 * Initial date: 04.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TitlePageElementHandler implements PageElementHandler, PageElementStore<TitleElement>, 
		SimpleAddPageElementHandler, CloneElementHandler {

	private static final AtomicInteger idGenerator = new AtomicInteger();
	
	@Override
	public String getType() {
		return "htitle";
	}

	@Override
	public String getIconCssClass() {
		return "o_icon_header";
	}
	
	@Override
	public PageElementCategory getCategory() {
		return PageElementCategory.text;
	}

	@Override
	public int getSortOrder() {
		return 10;
	}

	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element, RenderingHints options) {
		String htmlContent = "";
		String cssClass = "";
		if (element instanceof TitlePart titlePart) {
			TitleSettings titleSettings = titlePart.getTitleSettings();
			cssClass = TitleElement.toCssClassWithMarkerClass(titleSettings, false);

			if (options.isEditable()) {
				if (StringHelper.containsNonWhitespace(titlePart.getContent())) {
					htmlContent = TitleElement.toHtml(titlePart.getContent(), titleSettings);
				} else {
					String placeholder = Util.createPackageTranslator(TitleEditorController.class, ureq.getLocale()).translate("title.placeholder");
					htmlContent = TitleElement.toHtmlPlaceholder(placeholder, titleSettings);
				}
			} else {
				if (StringHelper.containsNonWhitespace(titlePart.getContent())) {
					htmlContent = TitleElement.toHtml(titlePart.getContent(), titleSettings);
				}
			}
		}
		TextComponent cmp = TextFactory.createTextComponentFromString("title_" + idGenerator.incrementAndGet(),
				htmlContent, cssClass, false, null);
		return new TextRunComponent(cmp, false);
	}

	@Override
	public PageElementEditorController getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof TitlePart titlePart) {
			return new TitleEditorController(ureq, wControl, titlePart, this, false);
		}
		return null;
	}
	
	@Override
	public PageElementInspectorController getInspector(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof TitlePart titlePart) {
			return new TitleInspectorController(ureq, wControl, titlePart, this, false);
		}
		return null;
	}

	@Override
	public PageElement createPageElement(Locale locale) {
		TitlePart title = new TitlePart();
		TitleSettings settings = new TitleSettings();
		settings.setSize(3);
		settings.setLayoutSettings(BlockLayoutSettings.getPredefined());
		title.setTitleSettings(settings);
		return title;
	}

	@Override
	public PageElement clonePageElement(PageElement element) {
		if (element instanceof TitlePart titlePart) {
			return titlePart.copy();
		}
		return null;
	}

	@Override
	public TitleElement savePageElement(TitleElement element) {
		return CoreSpringFactory.getImpl(PageService.class).updatePart((TitlePart)element);
	}
}
