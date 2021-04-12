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
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentHelper;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.component.ComponentTraverser;
import org.olat.modules.ceditor.InteractiveAddPageElementHandler;
import org.olat.modules.ceditor.PageEditorProvider;
import org.olat.modules.ceditor.PageEditorSecurityCallback;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementAddController;
import org.olat.modules.ceditor.PageElementHandler;
import org.olat.modules.ceditor.SimpleAddPageElementHandler;
import org.olat.modules.ceditor.model.ContainerElement;
import org.olat.modules.ceditor.ui.component.ContentEditorComponent;
import org.olat.modules.ceditor.ui.component.ContentEditorContainerComponent;
import org.olat.modules.ceditor.ui.component.ContentEditorFragment;
import org.olat.modules.ceditor.ui.component.ContentEditorFragmentComponent;
import org.olat.modules.ceditor.ui.event.AddElementEvent;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.ceditor.ui.event.CloseElementsEvent;
import org.olat.modules.ceditor.ui.event.ClosePartEvent;
import org.olat.modules.ceditor.ui.event.DeleteElementEvent;
import org.olat.modules.ceditor.ui.event.DropToEditorEvent;
import org.olat.modules.ceditor.ui.event.DropToPageElementEvent;
import org.olat.modules.ceditor.ui.event.EditElementEvent;
import org.olat.modules.ceditor.ui.event.MoveDownElementEvent;
import org.olat.modules.ceditor.ui.event.MoveUpElementEvent;
import org.olat.modules.ceditor.ui.event.OpenAddElementEvent;
import org.olat.modules.ceditor.ui.event.OpenRulesEvent;
import org.olat.modules.ceditor.ui.event.PositionEnum;
import org.olat.modules.ceditor.ui.event.SaveElementEvent;
import org.olat.modules.portfolio.Page;

/**
 * 
 * Initial date: 08.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageEditorV2Controller extends BasicController {
	
	private final VelocityContainer mainVC;
	private final ContentEditorComponent editorCmp;
	private Link addElementButton;
	
	private CloseableModalController cmc;
	private PageElementAddController addCtrl;
	private AddElementsController addElementsCtrl;
	private CloseableCalloutWindowController addCalloutCtrl;
	
	private int counter;
	private final PageEditorProvider provider;
	private final PageEditorSecurityCallback secCallback;
	private Map<String,PageElementHandler> handlerMap = new HashMap<>();

	public PageEditorV2Controller(UserRequest ureq, WindowControl wControl, PageEditorProvider provider,
			PageEditorSecurityCallback secCallback, Translator fallbackTranslator, Page page) {
		super(ureq, wControl, fallbackTranslator);
		this.provider = provider;
		this.secCallback = secCallback;

		for(PageElementHandler handler:provider.getAvailableHandlers()) {
			handlerMap.put(handler.getType(), handler);
		}

		mainVC = createVelocityContainer("page_editor");
		
		editorCmp = new ContentEditorComponent("page_editor_v2");
		editorCmp.addListener(this);
		mainVC.put("page_editor", editorCmp);
		
		if (provider.getCreateHandlers() != null && !provider.getCreateHandlers().isEmpty()) {
			addElementButton = LinkFactory.createButton("add.element", mainVC, this);
			addElementButton.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
			addElementButton.setElementCssClass("o_sel_add_element_main");
		}
		
		mainVC.contextPut("pageIsReferenced", page != null && page.getBody() != null && page.getBody().getUsage() > 1);

		loadModel(ureq);
		putInitialPanel(mainVC);
	}
	
	public PageEditorV2Controller(UserRequest ureq, WindowControl wControl, PageEditorProvider provider,
			PageEditorSecurityCallback secCallback, Translator fallbackTranslator) {
		this(ureq, wControl, provider, secCallback, fallbackTranslator, null);
	}

	private void loadModel(UserRequest ureq) {
		List<? extends PageElement> elements = provider.getElements();
		List<ContentEditorFragment> flatFragmentsList = new ArrayList<>(elements.size());
		for(PageElement element:elements) {
			ContentEditorFragment fragment = createFragmentComponent(ureq, element);
			if(fragment != null) {
				flatFragmentsList.add(fragment);
			}
		}
		
		Map<String,ContentEditorFragment> elementIdToFragementMap = flatFragmentsList.stream()
				.collect(Collectors.toMap(ContentEditorFragment::getElementId, fragment -> fragment, (u, v) -> u));

		List<ContentEditorFragment> rootFragmentsList = new ArrayList<>(flatFragmentsList);
		for(ContentEditorFragment fragment:flatFragmentsList) {
			if(fragment instanceof ContentEditorContainerComponent) {
				ContentEditorContainerComponent container = (ContentEditorContainerComponent)fragment;
				List<String> containedElementIds = container.getContainerSettings().getAllElementIds();
				for(String containedElementId:containedElementIds) {
					ContentEditorFragment containedCmp = elementIdToFragementMap.get(containedElementId);
					if(containedCmp != null) {
						container.addComponent(containedCmp);
						rootFragmentsList.remove(containedCmp);
					}
				}
			}	
		}

		editorCmp.setRootComponents(rootFragmentsList);
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
				ContentEditorFragment fragment = doAddPageElement(ureq, element, uobject.getReferenceComponent(),
						uobject.getTarget(), uobject.getColumn());
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
				doAddElement(ureq, aee.getReferenceComponent(), aee.getHandler(),
						aee.getTarget(), aee.getContainerColumn());
			}
		} else if(addCalloutCtrl == source) {
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		} else if(event instanceof ChangePartEvent) {
			doSaveElement(ureq);
		} else if(event instanceof ClosePartEvent) {
			ClosePartEvent cpe = (ClosePartEvent)event;
			doCloseEditor(ureq, cpe.getElement());
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
		if (source == addElementButton) {
			openAddElementCallout(ureq);
		} else if(event instanceof EditElementEvent) {
			EditElementEvent e = (EditElementEvent)event;
			doCloseEditionEvent(ureq, e.getElementId());
		} else if(event instanceof CloseElementsEvent) {
			doCloseAllEditionEvent(ureq);
		} else if(event instanceof OpenAddElementEvent) {
			OpenAddElementEvent aee = (OpenAddElementEvent)event;
			openAddElementCallout(ureq, aee.getDispatchId(), aee.getComponent(), aee.getTarget(), aee.getColumn());
		} else if(event instanceof DeleteElementEvent) {
			doDeleteElement(ureq, ((DeleteElementEvent)event).getComponent());
		} else if(event instanceof MoveUpElementEvent) {
			doMoveUpElement(ureq, ((MoveUpElementEvent)event).getComponent());
		} else if(event instanceof MoveDownElementEvent) {
			doMoveDownElement(ureq, ((MoveDownElementEvent)event).getComponent());
		} else if(event instanceof DropToEditorEvent) {
			doDrop(ureq, (DropToEditorEvent)event);
		} else if(event instanceof DropToPageElementEvent) {
			doDrop(ureq, (DropToPageElementEvent)event);
		} else if(event instanceof SaveElementEvent) {
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if(event instanceof OpenRulesEvent) {
			fireEvent(ureq, event);
		}
	}
	
	private void doCloseEditor(UserRequest ureq, PageElement element) {
		new ComponentTraverser((comp, uureq) -> {
			if(comp instanceof ContentEditorFragment) {
				ContentEditorFragment elementCmp = (ContentEditorFragment)comp;
				if(elementCmp.getElementId().equals(element.getId()) && elementCmp.isEditMode()) {
					elementCmp.setEditMode(false);
				}
			}
			return true;
		}, editorCmp, false).visitAll(ureq);

		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doCloseEditionEvent(UserRequest ureq, String elementId) {
		new ComponentTraverser((comp, uureq) -> {
			if(comp instanceof ContentEditorFragment) {
				ContentEditorFragment elementCmp = (ContentEditorFragment)comp;
				if(!elementCmp.getElementId().equals(elementId) && elementCmp.isEditMode()) {
					elementCmp.setEditMode(false);
				}
			}
			return true;
		}, editorCmp, false).visitAll(ureq);
	}
	
	private void doCloseAllEditionEvent(UserRequest ureq) {
		new ComponentTraverser((comp, uureq) -> {
			if(comp instanceof ContentEditorFragment) {
				ContentEditorFragment elementCmp = (ContentEditorFragment)comp;
				if(elementCmp.isEditMode()) {
					elementCmp.setEditMode(false);
				}
			}
			return true;
		}, editorCmp, false).visitAll(ureq);
	}
	
	private ContentEditorFragment getContentEditorFragmentById(UserRequest ureq, String id) {
		List<ContentEditorFragment> fragment = new ArrayList<>();
		
		new ComponentTraverser((comp, uureq) -> {
			if(comp instanceof ContentEditorFragment) {
				ContentEditorFragment elementCmp = (ContentEditorFragment)comp;
				if(elementCmp.getComponentName().equals(id) || elementCmp.getElementId().equals(id)) {
					fragment.add(elementCmp);
				}
			}
			return true;
		}, editorCmp, false).visitAll(ureq);
		
		return fragment.isEmpty() ? null : fragment.get(0);
	}
	
	private void openAddElementCallout(UserRequest ureq) {
		addElementsCtrl = new AddElementsController(ureq, getWindowControl(), provider,
				PageElementTarget.atTheEnd, getTranslator());
		addElementsCtrl.addControllerListener(this);
		
		addCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), addElementsCtrl.getInitialComponent(),
				addElementButton, "", true, "o_sel_add_element_callout");
		addCalloutCtrl.addControllerListener(this);
		addCalloutCtrl.activate();
	}
	
	private void openAddElementCallout(UserRequest ureq, String dispatchId, ContentEditorFragment referenceFragment,
			PageElementTarget target, int column) {
		addElementsCtrl = new AddElementsController(ureq, getWindowControl(), provider,
				referenceFragment, target, column, getTranslator());
		listenTo(addElementsCtrl);
		
		addCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				addElementsCtrl.getInitialComponent(), dispatchId, "", true, "");
		listenTo(addCalloutCtrl);
		addCalloutCtrl.activate();
	}

	private void doAddElement(UserRequest ureq, ContentEditorFragment refenceFragment, PageElementHandler handler, PageElementTarget target, int column) {
		if(guardModalController(addCtrl)) return;
		
		if(handler instanceof InteractiveAddPageElementHandler) {
			InteractiveAddPageElementHandler interactiveHandler = (InteractiveAddPageElementHandler)handler;
			addCtrl = interactiveHandler.getAddPageElementController(ureq, getWindowControl());
			if(addCtrl == null) {
				showWarning("not.implement");
			} else {
				addCtrl.setUserObject(new AddElementInfos(refenceFragment, handler, target, column));
				listenTo(addCtrl);
				String title = translate("add." + handler.getType());
				cmc = new CloseableModalController(getWindowControl(), null, addCtrl.getInitialComponent(), true, title, true);
				listenTo(cmc);
				cmc.activate();
			}
		} else if(handler instanceof SimpleAddPageElementHandler) {
			SimpleAddPageElementHandler simpleHandler = (SimpleAddPageElementHandler)handler;
			doAddPageElement(ureq, simpleHandler.createPageElement(getLocale()), refenceFragment, target, column);
		}
	}
	
	private ContentEditorFragment doAddPageElement(UserRequest ureq, PageElement element, ContentEditorFragment referenceFragment,
			PageElementTarget target, int column) {
		
		ContentEditorFragment fragment = null;
		if(target == PageElementTarget.atTheEnd && referenceFragment == null) {
			fragment = doAddPageElementAtTheEnd(ureq, element);
			
			// with reference
			// with column
			
		} else if(target == PageElementTarget.atTheEnd && referenceFragment != null) {
			//
		} else if(target == PageElementTarget.within && column >= 0) {
			fragment = doAddPageElementInContainer(ureq, referenceFragment, element, column);
		} else if(target == PageElementTarget.above || target == PageElementTarget.below) {
			List<Component> ancestors = ComponentHelper.findAncestorsOrSelfByID(editorCmp, referenceFragment);
			int parentLineIndex = ancestors.indexOf(referenceFragment);
			if(parentLineIndex == 0 && ancestors.size() >= 2) {
				Component parent = ancestors.get(parentLineIndex + 1);
				if(parent == editorCmp) {
					int index = editorCmp.indexOfRootComponent(referenceFragment);
					if(target == PageElementTarget.below) {
						index = index + 1;
					}
					element = provider.appendPageElementAt(element, index);
					fragment = createFragmentComponent(ureq, element);
					editorCmp.addRootComponent(index, fragment);	
				} else if(parent instanceof ContentEditorContainerComponent) {
					ContentEditorContainerComponent container = (ContentEditorContainerComponent)parent;
					element = provider.appendPageElement(element);
					fragment = createFragmentComponent(ureq, element);
					container.addElement(fragment, referenceFragment, target);
				}
			}
		}
		
		if(fragment != null) {
			if(referenceFragment != null) {
				referenceFragment.setEditMode(false);
			}
			fragment.setEditMode(true);
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
		return fragment;
	}
	
	private ContentEditorFragment doAddPageElementInContainer(UserRequest ureq, ContentEditorFragment referenceFragment,
			PageElement element, int column) {
		ContentEditorFragment fragment = null;
		if(referenceFragment instanceof ContentEditorContainerComponent) {
			PageElement pageElement = provider.appendPageElement(element);
			fragment = createFragmentComponent(ureq, pageElement);
			ContentEditorContainerComponent containerCmp = (ContentEditorContainerComponent)referenceFragment;
			containerCmp.setElementAt(fragment, column, null);
		}
		return fragment;
	}

	private ContentEditorFragment doAddPageElementAtTheEnd(UserRequest ureq, PageElement element) {
		PageElement pageElement = provider.appendPageElement(element);
		ContentEditorFragment fragment = createFragmentComponent(ureq, pageElement);
		editorCmp.addRootComponent(fragment);
		return fragment;
	}
	
	private void doSaveElement(UserRequest ureq) {
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doSaveElement(UserRequest ureq, ContentEditorFragment fragment) {
		fragment.setEditMode(false);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doDeleteElement(UserRequest ureq, ContentEditorFragment fragment) {
		List<Component> ancestors = ComponentHelper.findAncestorsOrSelfByID(editorCmp, fragment);
		int index = ancestors.indexOf(fragment);
		if(index == 0 && ancestors.size() >= 2) {// the root component is always the editor itself
			provider.removePageElement(fragment.getElement());
			Component parent = ancestors.get(index + 1);
			if(parent == editorCmp) {
				editorCmp.removeRootComponent(fragment);
			} else if(parent instanceof ContentEditorContainerComponent) {
				ContentEditorContainerComponent container = (ContentEditorContainerComponent)parent;
				container.removeElementAt(fragment);
			}
		}
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doMoveUpElement(UserRequest ureq, ContentEditorFragment fragment) {
		List<Component> ancestors = ComponentHelper.findAncestorsOrSelfByID(editorCmp, fragment);
		int index = ancestors.indexOf(fragment);
		if(index == 0 && ancestors.size() >= 2) {// the root component is always the editor itself
			Component parent = ancestors.get(index + 1);
			if(parent == editorCmp) {
				if(editorCmp.moveUpRootComponent(fragment)) {
					provider.moveUpPageElement(fragment.getElement());
				}
			} else if(parent instanceof ContentEditorContainerComponent) {
				ContentEditorContainerComponent container = (ContentEditorContainerComponent)parent;
				container.moveUp(fragment.getElementId());
			}
		}
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doMoveDownElement(UserRequest ureq, ContentEditorFragment fragment) {
		List<Component> ancestors = ComponentHelper.findAncestorsOrSelfByID(editorCmp, fragment);
		int index = ancestors.indexOf(fragment);
		if(index == 0 && ancestors.size() >= 2) {// the root component is always the editor itself
			Component parent = ancestors.get(index + 1);
			if(parent == editorCmp) {
				if(editorCmp.moveDownRootComponent(fragment)) {
					provider.moveDownPageElement(fragment.getElement());
				}
			} else if(parent instanceof ContentEditorContainerComponent) {
				ContentEditorContainerComponent container = (ContentEditorContainerComponent)parent;
				container.moveDown(fragment.getElementId());
			}
		}
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doDrop(UserRequest ureq, DropToEditorEvent dropEvent) {
		if(editorCmp == null || editorCmp != dropEvent.getTargetComponent()) return;

		String fragmentCmpId = dropEvent.getSourceComponentId();
		ContentEditorFragment source = getContentEditorFragmentById(ureq, fragmentCmpId);
		if(source == null) {
			return;
		}
		
		Component sourceParent = getParent(source);
		if(sourceParent != null && sourceParent == editorCmp) {
			editorCmp.removeRootComponent(source);
		} else if(sourceParent instanceof ContentEditorContainerComponent) {
			ContentEditorContainerComponent container = (ContentEditorContainerComponent)sourceParent;
			container.removeElementAt(source);
		} else {
			editorCmp.setDirty(true);
			return;
		}
		
		boolean after = dropEvent.getPosition() == PositionEnum.bottom;
		provider.movePageElement(source.getElement(), null, after);
		if(after) {
			editorCmp.addRootComponent(source);
		} else {
			editorCmp.addRootComponent(0, source);
		}
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doDrop(UserRequest ureq, DropToPageElementEvent dropEvent) {
		if(editorCmp == null) return;
		
		String fragmentCmpId = dropEvent.getSourceComponentId();
		ContentEditorFragment target = dropEvent.getTargetComponent();
		ContentEditorFragment source = getContentEditorFragmentById(ureq, fragmentCmpId);
		if(target == null || source == null) {
			return;
		}
		if(target == editorCmp || source == target) {
			editorCmp.setDirty(true);
			return; // not supported yet
		}

		Component sourceParent = getParent(source);
		Component targetParent = getParent(target);
		
		// check we can add the element
		if(!(target instanceof ContentEditorContainerComponent)
				&& !(targetParent instanceof ContentEditorContainerComponent)
				&& !(targetParent == editorCmp && editorCmp.indexOfRootComponent(target) >= 0)) {
			return;
		}

		// do the thing
		boolean ok = false;
		boolean after = dropEvent.getPosition() == PositionEnum.bottom;
		if(sourceParent != null && sourceParent == editorCmp) {
			editorCmp.removeRootComponent(source);
		} else if(sourceParent instanceof ContentEditorContainerComponent) {
			ContentEditorContainerComponent container = (ContentEditorContainerComponent)sourceParent;
			container.removeElementAt(source);
		} else {
			editorCmp.setDirty(true);
			return;
		}
		
		if(target instanceof ContentEditorContainerComponent) {
			ContentEditorContainerComponent targetContainer = (ContentEditorContainerComponent)target;
			targetContainer.setElementAt(source, dropEvent.getSlot(), null);
			ok = true;
		} else if(targetParent instanceof ContentEditorContainerComponent) {
			ContentEditorContainerComponent targetContainer = (ContentEditorContainerComponent)targetParent;
			PageElementTarget pos = after ? PageElementTarget.below : PageElementTarget.above;
			targetContainer.addElement(source, target, pos);
			ok = true;
		} else if(targetParent == editorCmp) {
			int index = editorCmp.indexOfRootComponent(target);
			if(index >= 0) {
				provider.movePageElement(source.getElement(), target.getElement(), after);
				if(after) {
					index++;
				}
				editorCmp.addRootComponent(index, source);
				ok = true;
			}
		}

		if(!ok) {
			editorCmp.setDirty(true);
		}
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private ContentEditorFragment createFragmentComponent(UserRequest ureq, PageElement element) {
		PageElementHandler handler = handlerMap.get(element.getType());
		if(handler == null) {
			logError("Cannot find an handler of type: " + element.getType(), null);
			return null;
		}
		Controller editorPart = handler.getEditor(ureq, getWindowControl(), element);
		listenTo(editorPart);
		String cmpId = "frag-" + (++counter);
		
		ContentEditorFragment cmp;
		if(element instanceof ContainerElement) {
			cmp = new ContentEditorContainerComponent(cmpId, (ContainerEditorController)editorPart);
		} else {
			cmp = new ContentEditorFragmentComponent(cmpId, element, editorPart);
		}
		cmp.setDeleteable(secCallback.canDeleteElement());
		cmp.setMoveable(secCallback.canMoveUpAndDown());
		cmp.addListener(this);
		return cmp;
	}
	
	private Component getParent(ContentEditorFragment fragment) {
		List<Component> ancestors = ComponentHelper.findAncestorsOrSelfByID(editorCmp, fragment);
		if(ancestors.size() > 1) {
			return ancestors.get(1);
		}
		return null;
	}
}