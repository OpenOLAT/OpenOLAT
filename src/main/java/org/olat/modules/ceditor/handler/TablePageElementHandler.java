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
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.ceditor.CloneElementHandler;
import org.olat.modules.ceditor.ContentEditorXStream;
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
import org.olat.modules.ceditor.model.TableElement;
import org.olat.modules.ceditor.model.TableSettings;
import org.olat.modules.ceditor.model.jpa.TablePart;
import org.olat.modules.ceditor.ui.PageRunComponent;
import org.olat.modules.ceditor.ui.TableEditorController;
import org.olat.modules.ceditor.ui.TableInspectorController;
import org.olat.modules.ceditor.ui.TableRunController;

/**
 * 
 * Initial date: 19 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TablePageElementHandler implements PageElementHandler, PageElementStore<TableElement>, 
		SimpleAddPageElementHandler, CloneElementHandler {
	
	@Override
	public String getType() {
		return "table";
	}

	@Override
	public String getIconCssClass() {
		return "o_icon_table";
	}
	
	@Override
	public PageElementCategory getCategory() {
		return PageElementCategory.text;
	}

	@Override
	public int getSortOrder() {
		return 30;
	}

	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element, RenderingHints options) {
		if(element instanceof TablePart tablePart) {
			return new TableRunController(ureq, wControl, tablePart, false);
		}
		return new PageRunComponent(new Panel("empty"));
	}

	@Override
	public PageElementEditorController getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof TablePart tablePart) {
			return new TableEditorController(ureq, wControl, tablePart, this, false);
		}
		return null;
	}
	
	@Override
	public PageElementInspectorController getInspector(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof TablePart tablePart) {
			return new TableInspectorController(ureq, wControl, tablePart, this, false);
		}
		return null;
	}

	@Override
	public PageElement createPageElement(Locale locale) {
		TablePart tablePart = new TablePart();
		TableSettings tableSettings = new TableSettings();
		tableSettings.setBordered(true);
		String layoutOptions = ContentEditorXStream.toXml(tableSettings);
		tablePart.setLayoutOptions(layoutOptions);
		return tablePart;
	}

	@Override
	public PageElement clonePageElement(PageElement element) {
		if (element instanceof TablePart tablePart) {
			return tablePart.copy();
		}
		return null;
	}

	@Override
	public TablePart savePageElement(TableElement element) {
		return CoreSpringFactory.getImpl(PageService.class).updatePart((TablePart)element);
	}
}
