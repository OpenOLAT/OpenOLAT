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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings.CalloutOrientation;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.InteractiveAddPageElementHandler;
import org.olat.modules.ceditor.PageEditorProvider;
import org.olat.modules.ceditor.PageEditorSecurityCallback;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementAddController;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.PageElementHandler;
import org.olat.modules.ceditor.SimpleAddPageElementHandler;
import org.olat.modules.ceditor.ui.event.AddElementEvent;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.ceditor.ui.event.ClosePartEvent;

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
	private AddElementsController addElementsCtrl;
	private CloseableCalloutWindowController addCalloutCtrl;
	
	private int counter;
	private final PageEditorProvider provider;
	private final PageEditorSecurityCallback secCallback;
	
	private List<EditorFragment> fragments = new ArrayList<>();
	private Map<String,PageElementHandler> handlerMap = new HashMap<>();

	public PageEditorController(UserRequest ureq, WindowControl wControl, PageEditorProvider provider,
			PageEditorSecurityCallback secCallback, Translator fallbackTranslator) {
		super(ureq, wControl, fallbackTranslator);
		this.provider = provider;
		this.secCallback = secCallback;

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
		
		String[] jss = new String[] {
				"js/dragula/dragula.js"
		};
		JSAndCSSComponent js = new JSAndCSSComponent("js", jss, null);
		mainVC.put("js", js);
		
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
				AddElementInfos uobject = addCtrl.getUserObject();
				EditorFragment fragment = doAddPageElement(ureq, element, uobject.getReferenceFragment(), uobject.getTarget());
				// close editor right away (file upload etc makes more sense)
				doSaveElement(ureq, fragment);
			}
			cmc.deactivate();
			cleanUp();
		} else if(addElementsCtrl == source) {
			addCalloutCtrl.deactivate();
			cleanUp();
			if(event instanceof AddElementEvent) {
				AddElementEvent aee = (AddElementEvent)event;
				doAddElement(ureq, aee.getReferenceFragment(), aee.getHandler(), aee.getTarget());
			}
		} else if(addCalloutCtrl == source) {
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		} else if(isEditorPartController(source)) {
			EditorFragment fragment = getEditorFragment(source);
			if(event instanceof ChangePartEvent) {
				ChangePartEvent changeEvent = (ChangePartEvent)event;
				PageElement element = changeEvent.getElement();
				fragment.setPageElement(element);
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else if(event instanceof ClosePartEvent) {
				doSaveElement(ureq, fragment);
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
		removeAsListenerAndDispose(addElementsCtrl);
		removeAsListenerAndDispose(addCalloutCtrl);
		removeAsListenerAndDispose(addCtrl);
		removeAsListenerAndDispose(cmc);
		addElementsCtrl = null;
		addCalloutCtrl = null;
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
				doAddElement(ureq, null, handler, PageElementTarget.atTheEnd);
			} else if("add.element.above".equals(cmd)) {
				EditorFragment refEl = (EditorFragment)link.getUserObject();
				openAddElementCallout(ureq, link, refEl, PageElementTarget.above);
			} else if("add.element.below".equals(cmd)) {
				EditorFragment refEl = (EditorFragment)link.getUserObject();
				openAddElementCallout(ureq, link, refEl, PageElementTarget.below);
			} else if("save.element".equals(cmd)) {
				EditorFragment fragment = (EditorFragment)link.getUserObject();
				doSaveElement(ureq, fragment);
			} else if("delete.element".equals(cmd)) {
				EditorFragment fragment = (EditorFragment)link.getUserObject();
				doDeleteElement(ureq, fragment);
			} else if("move.up.element".equals(cmd)) {
				EditorFragment fragment = (EditorFragment)link.getUserObject();
				doMoveUpElement(ureq, fragment);
			} else if("move.down.element".equals(cmd)) {
				EditorFragment fragment = (EditorFragment)link.getUserObject();
				doMoveDownElement(ureq, fragment);
			}
		} else if(mainVC == source) {
			if("edit_fragment".equals(event.getCommand())) {
				String fragmentId = ureq.getParameter("fragment");
				EditorFragment selectedFragment = null;
				for(EditorFragment f:fragments) {
					if(f.getComponentName().equals(fragmentId)) {
						selectedFragment = f;
					}
				}
				doEditElement(selectedFragment);
			} else if("drop_fragment".equals(event.getCommand())) {
				String fragmentCmpId = ureq.getParameter("dragged");
				String siblingCmpId = ureq.getParameter("sibling");
				doMove(ureq, fragmentCmpId, siblingCmpId);
			}
		}
	}
	
	private void doEditElement(EditorFragment fragment) {
		for(EditorFragment eFragment:fragments) {
			eFragment.setEditMode(eFragment.equals(fragment));
			
			List<Link> additionalTools = eFragment.getAdditionalTools();
			for(Link additionalTool:additionalTools) {
				mainVC.put(additionalTool.getComponentName(), additionalTool);
			}
		}
		//The link must every time created as new

		Link addAboveLink = LinkFactory.createLink("add.element.above", "add.element.above", getTranslator(), mainVC, this, Link.LINK);
		addAboveLink.setIconLeftCSS("o_icon o_icon-sm o_icon_element_before");
		addAboveLink.setElementCssClass("o_sel_add_element_above");
		addAboveLink.setUserObject(fragment);
		addAboveLink.setVisible(!provider.getCreateHandlers().isEmpty());
		addAboveLink.setEnabled(!provider.getCreateHandlers().isEmpty());
		fragment.setAddElementAboveLink(addAboveLink);

		Link addBelowLink = LinkFactory.createLink("add.element.below", "add.element.below", getTranslator(), mainVC, this, Link.LINK);
		addBelowLink.setIconLeftCSS("o_icon o_icon-sm o_icon_element_after");
		addBelowLink.setElementCssClass("o_sel_add_element_below");
		addBelowLink.setUserObject(fragment);
		addBelowLink.setVisible(!provider.getCreateHandlers().isEmpty());
		addBelowLink.setEnabled(!provider.getCreateHandlers().isEmpty());
		fragment.setAddElementBelowLink(addBelowLink);

		Link saveLink = LinkFactory.createLink("save.and.close", "save.element", getTranslator(), mainVC, this, Link.LINK);
		saveLink.setIconLeftCSS("o_icon o_icon-sm o_icon_close");
		saveLink.setElementCssClass("o_sel_save_element");
		saveLink.setUserObject(fragment);
		fragment.setSaveLink(saveLink);

		Link moveUpLink = LinkFactory.createLink("move.up", "move.up.element", getTranslator(), mainVC, this, Link.LINK + Link.NONTRANSLATED);
		moveUpLink.setIconLeftCSS("o_icon o_icon-sm o_icon_move_up");
		moveUpLink.setElementCssClass("o_sel_move_up_element");
		moveUpLink.setCustomDisplayText("");
		moveUpLink.setTitle(translate("move.up"));
		moveUpLink.setUserObject(fragment);
		moveUpLink.setEnabled(fragments.indexOf(fragment) > 0 && secCallback.canMoveUpAndDown());
		moveUpLink.setVisible(secCallback.canMoveUpAndDown());
		fragment.setMoveUpLink(moveUpLink);
		 
		Link moveDownLink = LinkFactory.createLink("move.down", "move.down.element", getTranslator(), mainVC, this, Link.LINK + Link.NONTRANSLATED);
		moveDownLink.setIconLeftCSS("o_icon o_icon-sm o_icon_move_down");
		moveDownLink.setElementCssClass("o_sel_move_down_element");
		moveDownLink.setCustomDisplayText("");
		moveUpLink.setTitle(translate("move.down"));
		moveDownLink.setUserObject(fragment);
		moveDownLink.setEnabled((fragments.indexOf(fragment) < (fragments.size() - 1)) && secCallback.canMoveUpAndDown());
		moveDownLink.setVisible(secCallback.canMoveUpAndDown());
		fragment.setMoveDownLink(moveDownLink);
		
		Link deleteLink = LinkFactory.createLink("delete", "delete.element", getTranslator(), mainVC, this, Link.LINK);
		deleteLink.setIconLeftCSS("o_icon o_icon-sm o_icon_delete_item");
		deleteLink.setElementCssClass("o_sel_delete_element");
		deleteLink.setUserObject(fragment);
		deleteLink.setVisible(secCallback.canDeleteElement());
		deleteLink.setEnabled(secCallback.canDeleteElement());
		fragment.setDeleteLink(deleteLink);

		mainVC.setDirty(true);
	}
	
	private void openAddElementCallout(UserRequest ureq, Link link, EditorFragment referenceFragment, PageElementTarget target) {
		addElementsCtrl = new AddElementsController(ureq, getWindowControl(), provider, referenceFragment, target);
		listenTo(addElementsCtrl);
		
		CalloutSettings calloutSettings;
		if(target == PageElementTarget.above) {
			calloutSettings = new CalloutSettings(true, CalloutOrientation.top);
		} else {
			calloutSettings = new CalloutSettings(false);
		}
		addCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				addElementsCtrl.getInitialComponent(), link.getDispatchID(),
				"", true, "", calloutSettings);
		listenTo(addCalloutCtrl);
		addCalloutCtrl.activate();
	}
	
	private void doAddElement(UserRequest ureq, EditorFragment refenceFragment, PageElementHandler handler, PageElementTarget target) {
		if(addCtrl != null) return;
		
		if(handler instanceof InteractiveAddPageElementHandler) {
			InteractiveAddPageElementHandler interactiveHandler = (InteractiveAddPageElementHandler)handler;
			addCtrl = interactiveHandler.getAddPageElementController(ureq, getWindowControl());
			if(addCtrl == null) {
				showWarning("not.implement");
			} else {
				addCtrl.setUserObject(new AddElementInfos(refenceFragment, handler, target));
				listenTo(addCtrl);
				String title = translate("add." + handler.getType());
				cmc = new CloseableModalController(getWindowControl(), null, addCtrl.getInitialComponent(), true, title, true);
				listenTo(cmc);
				cmc.activate();
			}
		} else if(handler instanceof SimpleAddPageElementHandler) {
			SimpleAddPageElementHandler simpleHandler = (SimpleAddPageElementHandler)handler;
			doAddPageElement(ureq, simpleHandler.createPageElement(getLocale()), refenceFragment, target);
		}
	}
	
	private EditorFragment doAddPageElement(UserRequest ureq, PageElement element, EditorFragment referenceFragment, PageElementTarget target) {
		EditorFragment newFragment = null;
		if(target == PageElementTarget.atTheEnd) {
			newFragment = doAddPageElementAtTheEnd(ureq, element);
		} else if(target == PageElementTarget.above || target == PageElementTarget.below) {
			int index = fragments.indexOf(referenceFragment);
			if(target == PageElementTarget.below) {
				index = index + 1;
			}
			
			if(index >= fragments.size()) {
				newFragment = doAddPageElementAtTheEnd(ureq, element);
			} else {
				if(index < 0) {
					index = 0;
				}

				PageElement pageElement = provider.appendPageElementAt(element, index);
				newFragment = createFragment(ureq, pageElement);
				fragments.add(index, newFragment);
			}
		}

		mainVC.setDirty(true);
		
		doEditElement(newFragment);
		fireEvent(ureq, Event.CHANGED_EVENT);
		return newFragment;
	}

	private EditorFragment doAddPageElementAtTheEnd(UserRequest ureq, PageElement element) {
		PageElement pageElement = provider.appendPageElement(element);
		EditorFragment fragment = createFragment(ureq, pageElement);
		fragments.add(fragment);
		return fragment;
	}
	
	private void doSaveElement(UserRequest ureq, EditorFragment fragment) {
		fragment.setEditMode(false);
		mainVC.setDirty(true);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doDeleteElement(UserRequest ureq, EditorFragment fragment) {
		provider.removePageElement(fragment.getPageElement());
		fragments.remove(fragment);
		mainVC.setDirty(true);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doMoveUpElement(UserRequest ureq, EditorFragment fragment) {
		int index = fragments.indexOf(fragment) - 1;
		if(index >= 0) {
			provider.moveUpPageElement(fragment.getPageElement());
			fragments.remove(fragment);
			fragments.add(index, fragment);
			mainVC.setDirty(true);
			doEditElement(fragment);
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}
	
	private void doMoveDownElement(UserRequest ureq, EditorFragment fragment) {
		int index = fragments.indexOf(fragment) + 1;
		if(index < fragments.size()) {
			provider.moveDownPageElement(fragment.getPageElement());
			fragments.remove(fragment);
			fragments.add(index, fragment);
			mainVC.setDirty(true);
			doEditElement(fragment);
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}
	
	private void doMove(UserRequest ureq, String fragmentCmpId, String siblingCmpId) {
		if(!StringHelper.containsNonWhitespace(fragmentCmpId)) return;
		
		EditorFragment fragment = getFragmentByCmpId(fragmentCmpId);
		if(fragment == null) return;
		
		EditorFragment sibling = getFragmentByCmpId(siblingCmpId);

		if(fragments.remove(fragment)) {
			int index = fragments.size();
			PageElement siblingElement = null;
			if(sibling != null && fragments.contains(sibling)) {
				index = fragments.indexOf(sibling);
				siblingElement = sibling.getPageElement();
			}
			fragments.add(index, fragment);
			provider.movePageElement(fragment.getPageElement(), siblingElement);
		}
		mainVC.setDirty(true);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private EditorFragment getFragmentByCmpId(String cmpId) {
		if(!StringHelper.containsNonWhitespace(cmpId)) return null;
		
		for(EditorFragment fragment:fragments) {
			if(fragment.getCmpId().equals(cmpId)) {
				return fragment;
			}
		}
		return null;
	}
	
	
	private EditorFragment createFragment(UserRequest ureq, PageElement element) {
		PageElementHandler handler = handlerMap.get(element.getType());
		if(handler == null) {
			logError("Cannot find an handler of type: " + element.getType(), null);
			return null;
		}
		Controller editorPart = handler.getEditor(ureq, getWindowControl(), element);
		listenTo(editorPart);
		String cmpId = "frag-" + (++counter);
		EditorFragment fragment = new EditorFragment(element, handler, cmpId, editorPart);
		mainVC.put(cmpId, editorPart.getInitialComponent());
		return fragment;
	}
	
	public static class EditorFragment  {
		
		private boolean editMode;
		private PageElement element;
		private final PageElementHandler handler;

		private final String cmpId;
		private Controller editorPart;
		private Link addElementAboveLink, addElementBelowLink, saveLink, deleteLink, moveUpLink, moveDownLink;
		
		public EditorFragment(PageElement element, PageElementHandler handler, String cmpId, Controller editorPart) {
			this.element = element;
			this.handler = handler;
			this.cmpId = cmpId;
			this.editorPart = editorPart;
		}

		public boolean isEditMode() {
			return editMode;
		}

		public void setEditMode(boolean editMode) {
			this.editMode = editMode;
			if(editorPart instanceof PageElementEditorController) {
				((PageElementEditorController)editorPart).setEditMode(editMode);
			}
		}
		
		public String getCmpId() {
			return cmpId;
		}

		public PageElement getPageElement() {
			return element;
		}
		
		public void setPageElement(PageElement element) {
			this.element = element;
		}

		public String getComponentName() {
			return getCmpId();
		}
		
		public Controller getEditorPart() {
			return editorPart;
		}
		
		public Link getAddElementAboveLink() {
			return addElementAboveLink;
		}

		public void setAddElementAboveLink(Link addElementAboveLink) {
			this.addElementAboveLink = addElementAboveLink;
		}

		public Link getAddElementBelowLink() {
			return addElementBelowLink;
		}

		public void setAddElementBelowLink(Link addElementBelowLink) {
			this.addElementBelowLink = addElementBelowLink;
		}

		public Link getSaveLink() {
			return saveLink;
		}

		public void setSaveLink(Link saveLink) {
			this.saveLink = saveLink;
		}

		public Link getDeleteLink() {
			return deleteLink;
		}

		public void setDeleteLink(Link deleteLink) {
			this.deleteLink = deleteLink;
		}

		public Link getMoveUpLink() {
			return moveUpLink;
		}

		public void setMoveUpLink(Link moveUpLink) {
			this.moveUpLink = moveUpLink;
		}

		public Link getMoveDownLink() {
			return moveDownLink;
		}

		public void setMoveDownLink(Link moveDownLink) {
			this.moveDownLink = moveDownLink;
		}
		
		public List<Link> getAdditionalTools() {
			if(editorPart instanceof PageElementEditorController) {
				return ((PageElementEditorController)editorPart).getOptionLinks();
			}
			return Collections.emptyList();
		}
		
		public String getType() {
			return handler.getType();
		}
		
		public String getTypeCssClass() {
			return handler.getIconCssClass();
		}

		public PageElementHandler getHandler() {
			return handler;
		}

		@Override
		public int hashCode() {
			return element.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof EditorFragment) {
				EditorFragment eFragment = (EditorFragment)obj;
				return element != null && element.equals(eFragment.getPageElement());
			}
			return false;
		}
	}
}