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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementHandler;
import org.olat.modules.ceditor.PageElementRenderingHints;
import org.olat.modules.ceditor.PageProvider;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.ui.component.PageFragmentsComponent;
import org.olat.modules.ceditor.ui.model.PageFragment;

/**
 * 
 * Initial date: 05.07.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageController extends BasicController {
	
	private int counter;
	private final PageProvider provider;
	private final PageFragmentsComponent fragmentsCmp;
	private final PageElementRenderingHints renderingHints;

	private final Map<String,PageElementHandler> handlerMap = new HashMap<>();
	
	public PageController(UserRequest ureq, WindowControl wControl, PageProvider provider, PageElementRenderingHints renderingHints) {
		super(ureq, wControl);
		this.provider = provider;
		this.renderingHints = renderingHints;
		
		for(PageElementHandler handler:provider.getAvailableHandlers()) {
			handlerMap.put(handler.getType(), handler);
		}
		fragmentsCmp = new PageFragmentsComponent("page_fragments");
		putInitialPanel(fragmentsCmp);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		fragmentsCmp.dispose();
        super.doDispose();
	}
	
	public boolean validateElements(UserRequest ureq, List<ValidationMessage> messages) {
		return fragmentsCmp.validateElements(ureq, messages);
	}
	
	/**
	 * @param ureq the user request
	 * @param reuse reuse the components already available
	 */
	public void loadElements(UserRequest ureq) {
		List<? extends PageElement> elements = provider.getElements();
		List<PageFragment> fragments = new ArrayList<>(elements.size());
		for(PageElement element:elements) {
			PageElementHandler handler = handlerMap.get(element.getType());
			if(handler != null) {
				PageRunElement runElement = handler.getContent(ureq, getWindowControl(), element, renderingHints);
				String cmpId = "cpt-" + (++counter);
				fragments.add(new PageFragment(handler.getType(), cmpId, runElement, element));
			}
		}
		fragmentsCmp.setFragments(fragments);
	}
}
