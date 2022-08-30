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
package org.olat.modules.portfolio.handler;

import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.PageElementHandler;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageElementRenderingHints;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.SimpleAddPageElementHandler;
import org.olat.modules.ceditor.model.TableElement;
import org.olat.modules.ceditor.ui.PageRunComponent;
import org.olat.modules.ceditor.ui.TableEditorController;
import org.olat.modules.ceditor.ui.TableInspectorController;
import org.olat.modules.ceditor.ui.TableRunController;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.model.TablePart;

/**
 * 
 * Initial date: 19 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TablePageElementHandler implements PageElementHandler, PageElementStore<TableElement>, SimpleAddPageElementHandler {
	
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
		return PageElementCategory.content;
	}

	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element, PageElementRenderingHints options) {
		if(element instanceof TablePart) {
			return new TableRunController(ureq, wControl, (TablePart)element);
		}
		return new PageRunComponent(new Panel("empty"));
	}

	@Override
	public PageElementEditorController getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof TablePart) {
			return new TableEditorController(ureq, wControl, (TablePart)element, this);
		}
		return null;
	}
	
	@Override
	public PageElementInspectorController getInspector(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof TablePart) {
			return new TableInspectorController(ureq, wControl, (TablePart)element, this);
		}
		return null;
	}

	@Override
	public PageElement createPageElement(Locale locale) {
		return new TablePart();
	}

	@Override
	public TablePart savePageElement(TableElement element) {
		return CoreSpringFactory.getImpl(PortfolioService.class).updatePart((TablePart)element);
	}
}
