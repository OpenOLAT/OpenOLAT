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
package org.olat.modules.portfolio.ui.editor.handler;

import java.util.concurrent.atomic.AtomicInteger;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.impl.elements.SpacerElementComponent;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.portfolio.model.SpacerPart;
import org.olat.modules.portfolio.ui.editor.PageElement;
import org.olat.modules.portfolio.ui.editor.PageElementEditorController;
import org.olat.modules.portfolio.ui.editor.PageElementHandler;
import org.olat.modules.portfolio.ui.editor.SimpleAddPageElementHandler;
import org.olat.modules.portfolio.ui.editor.SpacerEditorController;

/**
 * 
 * Initial date: 04.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SpacerElementHandler implements PageElementHandler, SimpleAddPageElementHandler {
	
	private static final AtomicInteger idGenerator = new AtomicInteger();

	@Override
	public String getType() {
		return "hr";
	}

	@Override
	public String getIconCssClass() {
		return "o_icon_spacer";
	}

	@Override
	public Component getContent(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof SpacerPart) {
			return new SpacerElementComponent("spacer_" + idGenerator.incrementAndGet());
		}
		return null;
	}

	@Override
	public PageElementEditorController getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		if(element instanceof SpacerPart) {
			return new SpacerEditorController(ureq, wControl, (SpacerPart)element);
		}
		return null;
	}

	@Override
	public PageElement createPageElement() {
		String content = "<hr/>";
		SpacerPart part = new SpacerPart();
		part.setContent(content);
		return part;
	}
}
