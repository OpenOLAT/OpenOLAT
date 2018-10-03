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
import org.olat.modules.ceditor.PageElementHandler;
import org.olat.modules.ceditor.SimpleAddPageElementHandler;
import org.olat.modules.ceditor.model.ContainerElement;
import org.olat.modules.ceditor.ui.component.PageEditorComponent;
import org.olat.modules.ceditor.ui.component.PageEditorModel;
import org.olat.modules.ceditor.ui.event.AddElementEvent;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.ceditor.ui.event.ClosePartEvent;
import org.olat.modules.ceditor.ui.event.ContainerColumnEvent;
import org.olat.modules.ceditor.ui.event.DropFragmentEvent;
import org.olat.modules.ceditor.ui.event.EditFragmentEvent;
import org.olat.modules.ceditor.ui.event.EditionEvent;
import org.olat.modules.ceditor.ui.model.EditorFragment;

/**
 * 
 * Initial date: 08.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageEditorController extends BasicController {

	private PageEditorModel editorModel;
	private final VelocityContainer mainVC;
	private final PageEditorComponent editorCmp;
	
	private CloseableModalController cmc;
	private PageElementAddController addCtrl;
	private AddElementsController addElementsCtrl;
	private CloseableCalloutWindowController addCalloutCtrl;
	
	private int counter;
	private final PageEditorProvider provider;
	private final PageEditorSecurityCallback secCallback;
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
		editorCmp = new PageEditorComponent("page_editor");
		editorCmp.addListener(this);
		mainVC.put("page_editor", editorCmp);
		
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
		editorModel = new PageEditorModel(newFragments);
		editorCmp.setModel(editorModel);
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
		} else if(editorCmp.getModel().isEditorPartController(source)) {
			EditorFragment fragment = editorModel.getEditorFragment(source);
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
			if("add".equals(link.getCommand())) {
				PageElementHandler handler = (PageElementHandler)link.getUserObject();
				doAddElement(ureq, null, handler, PageElementTarget.atTheEnd);
			}
		} else if(editorCmp == source) {
			if(event instanceof EditFragmentEvent) {
				EditFragmentEvent efe = (EditFragmentEvent)event;
				doEditElement(efe.getFragment());
			} else if (event instanceof EditionEvent) {
				EditionEvent editionEvent = (EditionEvent)event;
				if(editionEvent.getLink() != null) {
					doProcessEditionEvent(ureq, editionEvent.getLink(), editionEvent.getFragment());
				}
			} else if(event instanceof DropFragmentEvent) {
				DropFragmentEvent dropEvent = (DropFragmentEvent)event;
				doDrop(ureq, dropEvent);
			} else if(event instanceof ContainerColumnEvent) {
				ContainerColumnEvent cce = (ContainerColumnEvent)event;
				doChangeContainerColumns(ureq, cce.getFragment(), cce.getNumOfColumns());
			}
		}
	}
	
	private void doProcessEditionEvent(UserRequest ureq, Link link, EditorFragment fragment) {
		String cmd = link.getCommand();
		 if("add.element.above".equals(cmd)) {
			openAddElementCallout(ureq, link, fragment, PageElementTarget.above);
		} else if("add.element.below".equals(cmd)) {
			openAddElementCallout(ureq, link, fragment, PageElementTarget.below);
		} else if("save.element".equals(cmd)) {
			doSaveElement(ureq, fragment);
		} else if("delete.element".equals(cmd)) {
			doDeleteElement(ureq, fragment);
		} else if("move.up.element".equals(cmd)) {
			doMoveUpElement(ureq, fragment);
		} else if("move.down.element".equals(cmd)) {
			doMoveDownElement(ureq, fragment);
		}
	}
	
	private void doEditElement(EditorFragment fragment) {
		List<EditorFragment> fragments = editorModel.getFragments();
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
			newFragment = doAddPageElementAtTheEnd(ureq, referenceFragment, element);
		} else if(target == PageElementTarget.above || target == PageElementTarget.below) {

			String containerCmpId = editorModel.getContainerOfFragmentCmpId(referenceFragment.getComponentName());
			if(containerCmpId != null) {
				PageElement pageElement = provider.appendPageElement(element);
				newFragment = createFragment(ureq, pageElement);
				editorModel.add(newFragment);
				
				EditorFragment container = editorModel.getFragmentByCmpId(containerCmpId);
				PageElement updatedElement = ((ContainerEditorController)container.getEditorPart())
						.addElement(pageElement.getId(), referenceFragment.getPageElement().getId(), target);
				container.setPageElement(updatedElement);
			} else {
				int index = editorModel.indexOf(referenceFragment);
				if(target == PageElementTarget.below) {
					index = index + 1;
				}
				
				if(index >= editorModel.size()) {
					newFragment = doAddPageElementAtTheEnd(ureq, referenceFragment, element);
				} else {
					if(index < 0) {
						index = 0;
					}
	
					PageElement pageElement = provider.appendPageElementAt(element, index);
					newFragment = createFragment(ureq, pageElement);
					editorModel.add(index, newFragment, false);
				}
			}
		}

		doEditElement(newFragment);
		editorCmp.setDirty(true);
		fireEvent(ureq, Event.CHANGED_EVENT);
		return newFragment;
	}
	

	private EditorFragment doAddPageElementAtTheEnd(UserRequest ureq, EditorFragment referenceFragment, PageElement element) {
		PageElement pageElement = provider.appendPageElement(element);
		EditorFragment fragment = createFragment(ureq, pageElement);
		editorModel.add(fragment);
		
		if(referenceFragment != null) {
			String containerCmpId = editorModel.getContainerOfFragmentCmpId(referenceFragment.getComponentName());
			if(containerCmpId != null) {
				EditorFragment container = editorModel.getFragmentByCmpId(containerCmpId);
				PageElement updatedElement = ((ContainerEditorController)container.getEditorPart())
						.setElementIn(fragment.getPageElement().getId(), referenceFragment.getPageElement().getId());
				container.setPageElement(updatedElement);
			}
		}
		
		return fragment;
	}
	
	private void doSaveElement(UserRequest ureq, EditorFragment fragment) {
		fragment.setEditMode(false);
		mainVC.setDirty(true);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doDeleteElement(UserRequest ureq, EditorFragment fragment) {
		provider.removePageElement(fragment.getPageElement());
		editorModel.remove(fragment);
		mainVC.setDirty(true);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doMoveUpElement(UserRequest ureq, EditorFragment fragment) {
		String containerCmpId = editorModel.getContainerOfFragmentCmpId(fragment.getComponentName());
		if(containerCmpId != null) {
			EditorFragment container = editorModel.getFragmentByCmpId(containerCmpId);
			PageElement updatedElement = ((ContainerEditorController)container.getEditorPart())
					.moveUp(fragment.getPageElement().getId());
			container.setPageElement(updatedElement);
		} else {
			int index = editorModel.indexOf(fragment) - 1;
			if(index >= 0) {
				provider.moveUpPageElement(fragment.getPageElement());
				editorModel.remove(fragment);
				editorModel.add(index, fragment, false);
			}
		}
		
		doEditElement(fragment);
		editorCmp.setDirty(true);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doMoveDownElement(UserRequest ureq, EditorFragment fragment) {
		String containerCmpId = editorModel.getContainerOfFragmentCmpId(fragment.getComponentName());
		if(containerCmpId != null) {
			EditorFragment container = editorModel.getFragmentByCmpId(containerCmpId);
			PageElement updatedElement = ((ContainerEditorController)container.getEditorPart())
					.moveDown(fragment.getPageElement().getId());
			container.setPageElement(updatedElement);
		} else {
			int index = editorModel.indexOf(fragment) + 1;
			if(index < editorModel.size()) {
				provider.moveDownPageElement(fragment.getPageElement());
				editorModel.remove(fragment);
				editorModel.add(index, fragment, false);
			}
		}
		
		doEditElement(fragment);
		editorCmp.setDirty(true);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doChangeContainerColumns(UserRequest ureq, EditorFragment fragment, int numOfColumns) {
		if(!(fragment.getPageElement() instanceof ContainerElement)) {
			return;
		}
		
		PageElement updatedElement = ((ContainerEditorController)fragment.getEditorPart())
				.setNumOfColumns(numOfColumns);
		fragment.setPageElement(updatedElement);
		fragment.setEditMode(false);

		editorCmp.setDirty(true);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doDrop(UserRequest ureq, DropFragmentEvent dropEvent) {
		String fragmentCmpId = dropEvent.getDragged();
		if(StringHelper.containsNonWhitespace(fragmentCmpId)) {
			String targetCmpId = dropEvent.getTargetCmpId();
			String sourceCmpId = dropEvent.getSource();
			String siblingCmpId = dropEvent.getSiblingCmpId();
			String containerCmpId = dropEvent.getContainerCmpId();
			if(containerCmpId == null) {
				if(targetCmpId != null) {
					containerCmpId = editorModel.getContainerOfFragmentCmpId(targetCmpId);
				} else if (siblingCmpId != null) {
					containerCmpId = editorModel.getContainerOfFragmentCmpId(siblingCmpId);
				}
			}

			if(StringHelper.containsNonWhitespace(containerCmpId)) {
				String slotId = dropEvent.getSlotId();
				doMoveInContainer(fragmentCmpId, sourceCmpId, containerCmpId, slotId, siblingCmpId);
			} else if(StringHelper.containsNonWhitespace(targetCmpId) || StringHelper.containsNonWhitespace(siblingCmpId)) {
				doMoveTo(fragmentCmpId, sourceCmpId, targetCmpId, siblingCmpId);
			}

			editorCmp.setDirty(true);
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}
	
	private void doMoveInContainer(String fragmentCmpId, String sourceCmpId, String containerId, String slot, String siblingCmpId) {
		if(!StringHelper.containsNonWhitespace(fragmentCmpId)) return;
		
		EditorFragment fragment = editorModel.getFragmentByCmpId(fragmentCmpId);
		if(fragment == null) return;
		
		EditorFragment container = editorModel.getFragmentByCmpId(containerId);
		if(container == null) return;

		EditorFragment source = editorModel.getFragmentByCmpId(sourceCmpId);
		removeFromContainer(fragment, source);
		removeFromContainers(fragment);
		
		EditorFragment sibling = editorModel.getFragmentByCmpId(siblingCmpId);
		String siblingElementId = sibling == null ? null : sibling.getElementId();

		PageElement updatedElement = ((ContainerEditorController)container.getEditorPart())
			.setElementAt(fragment.getPageElement().getId(), Integer.parseInt(slot), siblingElementId);
		container.setPageElement(updatedElement);
		fragment.setEditMode(false);
	}
	
	private void doMoveTo(String fragmentCmpId, String sourceCmpId, String targetCmpId, String siblingCmpId) {
		if(!StringHelper.containsNonWhitespace(fragmentCmpId)) return;
		
		EditorFragment fragment = editorModel.getFragmentByCmpId(fragmentCmpId);
		if(fragment == null) return;
		
		EditorFragment sibling = editorModel.getFragmentByCmpId(siblingCmpId);
		EditorFragment target = editorModel.getFragmentByCmpId(targetCmpId);
		EditorFragment source = editorModel.getFragmentByCmpId(sourceCmpId);

		if(editorModel.remove(fragment)) {
			removeFromContainer(fragment, source);
			removeFromContainers(fragment);
			
			int index = editorModel.size();
			PageElement nextElement = null;
			boolean after = false;
			if(sibling != null && editorModel.contains(sibling)) {
				// dropped at the top of the target element
				index = editorModel.indexOf(sibling);
				nextElement = sibling.getPageElement();
			} else if(target != null && editorModel.contains(target)) {
				// target: dropped at the bottom of the target element
				index = editorModel.indexOf(target);
				nextElement = target.getPageElement();
				after = true;
			}
			editorModel.add(index, fragment, after);
			provider.movePageElement(fragment.getPageElement(), nextElement, after);
			fragment.setEditMode(false);
		}
	}
	
	private void removeFromContainer(EditorFragment fragment, EditorFragment source) {
		if(source == null || !(source.getEditorPart() instanceof ContainerEditorController)) return;

		PageElement updatedElement = ((ContainerEditorController)source.getEditorPart())
				.removeElement(fragment.getPageElement().getId());
		source.setPageElement(updatedElement);
	}
	
	private void removeFromContainers(EditorFragment fragment) {
		for(EditorFragment f:editorModel.getFragments()) {
			if(f.getEditorPart() instanceof ContainerEditorController) {
				PageElement updatedElement = ((ContainerEditorController)f.getEditorPart())
						.removeElement(fragment.getPageElement().getId());
				f.setPageElement(updatedElement);
			}
		}	
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
}