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
package org.olat.modules.portfolio.ui.editor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Util;
import org.olat.modules.portfolio.PagePart;
import org.olat.modules.portfolio.ui.PageController;
import org.olat.modules.portfolio.ui.editor.event.ChangePartEvent;

/**
 * 
 * Initial date: 08.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageEditorController extends BasicController {

	private final VelocityContainer mainVC;
	
	private CloseableModalController cmc;
	private PageElementAddController addCtrl;
	
	private int counter;
	private PageEditorProvider provider;
	private List<EditorFragment> fragments = new ArrayList<>();
	private Map<String,PageElementHandler> handlerMap = new HashMap<>();

	public PageEditorController(UserRequest ureq, WindowControl wControl, PageEditorProvider provider) {
		super(ureq, wControl);
		this.provider = provider;
		setTranslator(Util.createPackageTranslator(PageController.class, getLocale(), getTranslator()));
		
		mainVC = createVelocityContainer("page_editor");
		for(PageElementHandler handler:provider.getAvailableHandlers()) {
			handlerMap.put(handler.getType(), handler);
		}

		List<String> addElements = new ArrayList<>();
		for(PageElementHandler handler:provider.getCreateHandlers()) {
			if(handler instanceof InteractiveAddPageElementHandler || handler instanceof SimpleAddPageElementHandler) {
				String id = "add." + handler.getType();
				Link addLink = LinkFactory.createLink(id, id, "add", mainVC, this);
				addLink.setIconLeftCSS("o_icon o_icon-lg " + handler.getIconCssClass());
				addLink.setUserObject(handler);
				mainVC.put(id, addLink);
				addElements.add(id);
			}
		}
		
		mainVC.contextPut("addElementLinks", addElements);
		loadModel(ureq);
		putInitialPanel(mainVC);
	}

	private void loadModel(UserRequest ureq) {
		List<? extends PageElement> elements = provider.getElements();
		List<EditorFragment> newFragments = new ArrayList<>(elements.size());
		for(PageElement element:elements) {
			EditorFragment fragment = createFragment(ureq, element);
			if(fragment != null) {
				newFragments.add(fragment);
			}
		}
		fragments = newFragments;
		mainVC.contextPut("fragments", newFragments);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(addCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				PageElement element = addCtrl.getPageElement();
				doAddPageElement(ureq, element);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		} else if(isEditorPartController(source)) {
			EditorFragment fragment = getEditorFragment(source);
			if(event instanceof ChangePartEvent) {
				ChangePartEvent changeEvent = (ChangePartEvent)event;
				PagePart part = changeEvent.getPagePart();
				fragment.setPageElement(part);
				mainVC.setDirty(true);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}	
		}
		super.event(ureq, source, event);
	}
	
	private boolean isEditorPartController(Controller source) {
		for(EditorFragment fragment:fragments) {
			if(fragment.getEditorPart() == source) {
				return true;
			}
		}
		return false;
	}
	
	private EditorFragment getEditorFragment(Controller source) {
		for(EditorFragment fragment:fragments) {
			if(fragment.getEditorPart() == source) {
				return fragment;
			}
		}
		return null;
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(addCtrl);
		removeAsListenerAndDispose(cmc);
		addCtrl = null;
		cmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source instanceof Link) {
			Link link = (Link)source;
			String cmd = link.getCommand();
			if("add".equals(cmd)) {
				PageElementHandler handler = (PageElementHandler)link.getUserObject();
				doAddElement(ureq, handler);
			}
		}
	}
	
	private void doAddElement(UserRequest ureq, PageElementHandler handler) {
		if(addCtrl != null) return;
		
		if(handler instanceof InteractiveAddPageElementHandler) {
			InteractiveAddPageElementHandler interactiveHandler = (InteractiveAddPageElementHandler)handler;
			addCtrl = interactiveHandler.getAddPageElementController(ureq, getWindowControl());
			if(addCtrl == null) {
				showWarning("not.implement");
			} else {
				listenTo(addCtrl);
				String title = translate("add." + handler.getType());
				cmc = new CloseableModalController(getWindowControl(), null, addCtrl.getInitialComponent(), true, title, true);
				listenTo(cmc);
				cmc.activate();
			}
		} else if(handler instanceof SimpleAddPageElementHandler) {
			SimpleAddPageElementHandler simpleHandler = (SimpleAddPageElementHandler)handler;
			doAddPageElement(ureq, simpleHandler.createPageElement());
		}
	}
	
	private void doAddPageElement(UserRequest ureq, PageElement element) {
		PageElement pageElement = provider.appendPageElement(element);
		EditorFragment fragment = createFragment(ureq, pageElement);
		fragments.add(fragment);
		mainVC.setDirty(true);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private EditorFragment createFragment(UserRequest ureq, PageElement element) {
		PageElementHandler handler = handlerMap.get(element.getType());
		if(handler == null) {
			logError("Cannot find an handler of type: " + element.getType(), null);
		}
		Controller editorPart = handler.getEditor(ureq, getWindowControl(), element);
		listenTo(editorPart);
		String cmpId = "frag-" + (++counter);
		EditorFragment fragment = new EditorFragment(element, handler, cmpId, editorPart);
		mainVC.put(cmpId, editorPart.getInitialComponent());
		return fragment;
	}
	
	public static class EditorFragment  {
		
		private PageElement element;
		private final PageElementHandler handler;

		private final String cmpId;
		private Controller editorPart;
		
		public EditorFragment(PageElement element, PageElementHandler handler, String cmpId, Controller editorPart) {
			this.element = element;
			this.handler = handler;
			this.cmpId = cmpId;
			this.editorPart = editorPart;
		}

		public PageElement getPageElement() {
			return element;
		}
		
		public void setPageElement(PageElement element) {
			this.element = element;
		}

		public String getComponentName() {
			return cmpId;
		}
		
		public Controller getEditorPart() {
			return editorPart;
		}
		
		public PageElementHandler getHandler() {
			return handler;
		}
	}

}
