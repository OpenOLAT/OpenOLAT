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
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.PageElementHandler;
import org.olat.modules.ceditor.PageElementRenderingHints;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.SimpleAddPageElementHandler;
import org.olat.modules.ceditor.model.ContainerElement;
import org.olat.modules.ceditor.ui.ContainerEditorController;
import org.olat.modules.ceditor.ui.PageRunComponent;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.model.ContainerPart;

/**
 * 
 * Initial date: 10 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ContainerHandler implements PageElementHandler, PageElementStore<ContainerElement>, SimpleAddPageElementHandler {

	@Override
	public String getType() {
		return "container";
	}

	@Override
	public String getIconCssClass() {
		return "o_icon_container";
	}
	
	@Override
	public PageElementCategory getCategory() {
		return PageElementCategory.layout;
	}

	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element, PageElementRenderingHints options) {
		// rendering is done by the page component
		Component dummyCmp = new Panel("");
		return new PageRunComponent(dummyCmp);
	}

	@Override
	public PageElementEditorController getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof ContainerPart) {
			return new ContainerEditorController(ureq, wControl, (ContainerPart)element, this, null);
		}
		return null;
	}

	@Override
	public PageElement createPageElement(Locale locale) {
		return new ContainerPart();
	}

	@Override
	public ContainerElement savePageElement(ContainerElement element) {
		return CoreSpringFactory.getImpl(PortfolioService.class).updatePart((ContainerPart)element);
	}
}
