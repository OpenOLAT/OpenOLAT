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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.ceditor.PageEditorProvider;
import org.olat.modules.ceditor.PageLayoutHandler;
import org.olat.modules.ceditor.model.ContainerLayout;
import org.olat.modules.ceditor.ui.event.AddElementEvent;

/**
 * 
 * Initial date: 16 août 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AddLayoutController extends BasicController {
	
	private int count = 0;
	private final PageElementTarget target;
	
	public AddLayoutController(UserRequest ureq, WindowControl wControl, PageEditorProvider provider,
			PageElementTarget target, Translator fallbackTranslator) {
		super(ureq, wControl, fallbackTranslator);
		this.target = target;
		initContainer(provider);
	}
	

	private void initContainer(PageEditorProvider provider) {
		VelocityContainer mainVC = createVelocityContainer("add_layout");
		
		List<String> layoutIds = new ArrayList<>();
		for(PageLayoutHandler handler:provider.getCreateLayoutHandlers()) {
			ContainerLayout layout = handler.getLayout();
			if(layout.deprecated()) {
				continue;
			}
			
			String id = "add." + (++count);
			String pseudoIcon = layout.pseudoIcons();
			Link addLink = LinkFactory.createLink(id, id, "select.layout", pseudoIcon, getTranslator(), mainVC, this, Link.LINK | Link.NONTRANSLATED);
			addLink.setUserObject(handler);
			mainVC.put(id, addLink);
			layoutIds.add(id);
		}
		
		mainVC.contextPut("layouts", layoutIds);
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source instanceof Link && "select.layout".equals(((Link)source).getCommand())) {
			Link layoutLink = (Link)source;
			PageLayoutHandler handler = (PageLayoutHandler)layoutLink.getUserObject();
			fireEvent(ureq, new AddElementEvent(null, null, handler, target, -1));
		}
	}
}
