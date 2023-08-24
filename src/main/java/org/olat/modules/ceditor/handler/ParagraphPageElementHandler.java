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

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.controllers.linkchooser.CustomLinkTreeModel;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.text.TextComponent;
import org.olat.core.gui.control.WindowControl;
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
import org.olat.modules.ceditor.model.HTMLElement;
import org.olat.modules.ceditor.model.ParagraphElement;
import org.olat.modules.ceditor.model.jpa.ParagraphPart;
import org.olat.modules.ceditor.ui.ComponentsFactory;
import org.olat.modules.ceditor.ui.HTMLRawEditorController;
import org.olat.modules.ceditor.ui.HTMLRawInspectorController;
import org.olat.modules.ceditor.ui.TextRunComponent;

/**
 * 
 * Initial date: 19 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ParagraphPageElementHandler implements PageElementHandler, PageElementStore<HTMLElement>, SimpleAddPageElementHandler {
	
	private final CustomLinkTreeModel linkTreeModel;
	private final CustomLinkTreeModel toolLinkTreeModel;
	
	public ParagraphPageElementHandler() {
		this(null, null);
	}
	
	public ParagraphPageElementHandler(CustomLinkTreeModel linkTreeModel, CustomLinkTreeModel toolLinkTreeModel) {
		this.linkTreeModel = linkTreeModel;
		this.toolLinkTreeModel = toolLinkTreeModel;
	}

	@Override
	public String getType() {
		return "htmlparagraph";
	}

	@Override
	public String getIconCssClass() {
		// For now we use the paragraph icon until we have a minimized paragraph element o_icon_code
		return "o_icon_paragraph";
	}
	
	@Override
	public PageElementCategory getCategory() {
		return PageElementCategory.content;
	}

	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element, RenderingHints options) {
		TextComponent cmp = null;
		if(element instanceof ParagraphPart paragraphPart) {
			cmp = ComponentsFactory.getContent(paragraphPart);
		}
		return new TextRunComponent(cmp);
	}

	@Override
	public PageElementEditorController getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof ParagraphElement paragraphPart) {
			return new HTMLRawEditorController(ureq, wControl, paragraphPart, this, linkTreeModel, toolLinkTreeModel, true);
		}
		return null;
	}
	
	@Override
	public PageElementInspectorController getInspector(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof ParagraphElement paragraphPart) {
			return new HTMLRawInspectorController(ureq, wControl, paragraphPart, this);
		}
		return null;
	}

	@Override
	public PageElement createPageElement(Locale locale) {
		return new ParagraphPart();
	}

	@Override
	public ParagraphElement savePageElement(HTMLElement element) {
		return CoreSpringFactory.getImpl(PageService.class).updatePart((ParagraphPart)element);
	}
}
