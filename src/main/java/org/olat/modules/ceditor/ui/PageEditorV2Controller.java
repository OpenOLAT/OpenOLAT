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
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentHelper;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.component.ComponentTraverser;
import org.olat.modules.ceditor.CloneElementHandler;
import org.olat.modules.ceditor.InteractiveAddPageElementHandler;
import org.olat.modules.ceditor.PageEditorProvider;
import org.olat.modules.ceditor.PageEditorSecurityCallback;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementAddController;
import org.olat.modules.ceditor.PageElementHandler;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.SimpleAddPageElementHandler;
import org.olat.modules.ceditor.model.ContainerColumn;
import org.olat.modules.ceditor.model.ContainerElement;
import org.olat.modules.ceditor.ui.component.ContentEditorComponent;
import org.olat.modules.ceditor.ui.component.ContentEditorContainerComponent;
import org.olat.modules.ceditor.ui.component.ContentEditorFragment;
import org.olat.modules.ceditor.ui.component.ContentEditorFragmentComponent;
import org.olat.modules.ceditor.ui.event.AddElementEvent;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.ceditor.ui.event.CloneElementEvent;
import org.olat.modules.ceditor.ui.event.CloseElementsEvent;
import org.olat.modules.ceditor.ui.event.CloseInspectorEvent;
import org.olat.modules.ceditor.ui.event.ClosePartEvent;
import org.olat.modules.ceditor.ui.event.DeleteElementEvent;
import org.olat.modules.ceditor.ui.event.DropToEditorEvent;
import org.olat.modules.ceditor.ui.event.DropToPageElementEvent;
import org.olat.modules.ceditor.ui.event.EditElementEvent;
import org.olat.modules.ceditor.ui.event.ImportEvent;
import org.olat.modules.ceditor.ui.event.MoveDownElementEvent;
import org.olat.modules.ceditor.ui.event.MoveUpElementEvent;
import org.olat.modules.ceditor.ui.event.OpenAddElementEvent;
import org.olat.modules.ceditor.ui.event.OpenAddLayoutEvent;
import org.olat.modules.ceditor.ui.event.OpenRulesEvent;
import org.olat.modules.ceditor.ui.event.PositionEnum;
import org.olat.modules.ceditor.ui.event.SaveElementEvent;
import org.olat.modules.forms.model.xml.Container;
import org.olat.modules.portfolio.model.StandardMediaRenderingHints;

/**
 * 
 * Initial date: 08.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageEditorV2Controller extends BasicController {
	
	private final VelocityContainer mainVC;
	private final ContentEditorComponent editorCmp;
	private Link addLayoutButton;
	private Link addElementButton;
	private Link importContentButton;
	
	private CloseableModalController cmc;
	private PageElementAddController addCtrl;
	private AddLayoutController addLayoutCtrl;
	private AddElementsController addElementsCtrl;
	private DeleteConfirmationController deleteConfirmationCtrl;
	private CloseableCalloutWindowController addCalloutCtrl;
	
	private int counter;
	private final PageEditorProvider provider;
	private final PageEditorSecurityCallback secCallback;
	private Map<String,PageElementHandler> handlerMap = new HashMap<>();
	private Map<String,CloneElementHandler> cloneHandlerMap = new HashMap<>();
	
	public PageEditorV2Controller(UserRequest ureq, WindowControl wControl, PageEditorProvider provider,
			PageEditorSecurityCallback secCallback, Translator fallbackTranslator) {
		super(ureq, wControl, fallbackTranslator);
		this.provider = provider;
		this.secCallback = secCallback;

		for(PageElementHandler handler:provider.getAvailableHandlers()) {
			handlerMap.put(handler.getType(), handler);
			if (handler instanceof CloneElementHandler) {
				cloneHandlerMap.put(handler.getType(), (CloneElementHandler)handler);
			}
		}
		
		mainVC = createVelocityContainer("page_editor");
		
		editorCmp = new ContentEditorComponent("page_editor_v2");
		editorCmp.addListener(this);
		mainVC.put("page_editor", editorCmp);
		
		if (provider.getCreateHandlers() != null && !provider.getCreateHandlers().isEmpty()) {
			addElementButton = LinkFactory.createButton("add.element", mainVC, this);
			addElementButton.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
			addElementButton.setElementCssClass("o_sel_add_element_main");
			
			addLayoutButton = LinkFactory.createButton("add.layout", mainVC, this);
			addLayoutButton.setIconLeftCSS("o_icon o_icon-lg o_icon_add");
			addLayoutButton.setElementCssClass("o_sel_add_container_main");
		}
		
		if (StringHelper.containsNonWhitespace(provider.getImportButtonKey())) {
			importContentButton = LinkFactory.createLink("import.content", "import.content", "import.content", provider.getImportButtonKey(), getTranslator(), mainVC, this, Link.BUTTON);
			importContentButton.setIconLeftCSS("o_icon o_icon-lg o_icon_import");
			importContentButton.setVisible(provider.getElements().isEmpty());
		}
		
		loadModel(ureq);
		putInitialPanel(mainVC);
		
		// wControl.getWindowBackOffice().getChiefController().addBodyCssClass("o_ceditor");
	}
	
	@Override
	protected void doDispose() {
		super.doDispose();
		//getWindowControl().getWindowBackOffice().getChiefController().removeBodyCssClass("o_ceditor");
	}

	public void loadModel(UserRequest ureq) {
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
		
		updateImportButtonVisibility();
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
		} else if(addElementsCtrl == source || addLayoutCtrl == source) {
			addCalloutCtrl.deactivate();
			cleanUp();
			if(event instanceof AddElementEvent) {
				AddElementEvent aee = (AddElementEvent)event;
				doAddElement(ureq, aee.getReferenceComponent(), aee.getHandler(),
						aee.getTarget(),  aee.getContainerColumn());
			}
		} else if(deleteConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doDeleteElement(ureq, deleteConfirmationCtrl.getFragment(), false);
			}
			cmc.deactivate();
			cleanUp();
		} else if(addCalloutCtrl == source) {
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		} else if(event instanceof ChangePartEvent) {
			doSaveElement(ureq);
		} else if(event instanceof ClosePartEvent) {
			ClosePartEvent cpe = (ClosePartEvent)event;
			doCloseEditor(ureq, cpe.getElement());
		} else if(event instanceof CloseInspectorEvent) {
			CloseInspectorEvent cpe = (CloseInspectorEvent)event;
			doCloseInspector(ureq, cpe.getElementId(), cpe.isSilently());
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(deleteConfirmationCtrl);
		removeAsListenerAndDispose(addElementsCtrl);
		removeAsListenerAndDispose(addCalloutCtrl);
		removeAsListenerAndDispose(addLayoutCtrl);
		removeAsListenerAndDispose(addCtrl);
		removeAsListenerAndDispose(cmc);
		addElementsCtrl = null;
		addCalloutCtrl = null;
		addLayoutCtrl = null;
		addCtrl = null;
		cmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == addElementButton) {
			doCloseAllEditionEvent(ureq);
			openAddElementCallout(ureq);
		} else if (source == addLayoutButton) {
			doCloseAllEditionEvent(ureq);
			openAddLayoutCallout(ureq);
		} else if(source == importContentButton) {
			fireEvent(ureq, new ImportEvent());
		} else if(event instanceof EditElementEvent) {
			EditElementEvent e = (EditElementEvent)event;
			doCloseEditionEvent(ureq, e.getElementId());
		} else if(event instanceof CloseElementsEvent) {
			doCloseAllEditionEvent(ureq);
		} else if(event instanceof OpenAddElementEvent) {
			OpenAddElementEvent aee = (OpenAddElementEvent)event;
			openAddElementCallout(ureq, aee.getDispatchId(), aee.getComponent(), aee.getTarget(), aee.getColumn());
		} else if(event instanceof OpenAddLayoutEvent) {
			OpenAddLayoutEvent ale = (OpenAddLayoutEvent)event;
			openAddLayoutCallout(ureq, ale.getDispatchId(), ale.getComponent(), ale.getTarget());
		} else if(event instanceof CloneElementEvent) {
			doCloneElement(ureq, ((CloneElementEvent)event).getComponent());
		} else if(event instanceof DeleteElementEvent) {
			doDeleteElement(ureq, ((DeleteElementEvent)event).getComponent(), true);
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
	
	private void updateImportButtonVisibility() {
		if (importContentButton != null) {
			importContentButton.setVisible(provider.getElements().isEmpty());
			mainVC.setDirty(true);
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
	
	private void doCloseInspector(UserRequest ureq, String elementId, boolean silently) {
		new ComponentTraverser((comp, uureq) -> {
			if(comp instanceof ContentEditorFragment) {
				ContentEditorFragment elementCmp = (ContentEditorFragment)comp;
				if(elementCmp.getElementId().equals(elementId) && elementCmp.isEditMode()) {
					elementCmp.setInspectorVisible(false, silently);
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
				addElementsCtrl.getInitialComponent(), dispatchId, "", true, "o_sel_add_element_callout");
		listenTo(addCalloutCtrl);
		addCalloutCtrl.activate();
	}
	
	private void openAddLayoutCallout(UserRequest ureq) {
		addLayoutCtrl = new AddLayoutController(ureq, getWindowControl(), provider,
				null, PageElementTarget.atTheEnd, getTranslator());
		listenTo(addLayoutCtrl);
		
		addCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				addLayoutCtrl.getInitialComponent(), addLayoutButton, "", true, "");
		listenTo(addCalloutCtrl);
		addCalloutCtrl.activate();
	}
	
	private void openAddLayoutCallout(UserRequest ureq, String dispatchId, ContentEditorFragment referenceFragment,
			PageElementTarget target) {
		addLayoutCtrl = new AddLayoutController(ureq, getWindowControl(), provider,
				referenceFragment, target, getTranslator());
		listenTo(addLayoutCtrl);
		
		addCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				addLayoutCtrl.getInitialComponent(), dispatchId, "", true, "");
		listenTo(addCalloutCtrl);
		addCalloutCtrl.activate();
	}

	private void doAddElement(UserRequest ureq, ContentEditorFragment refenceFragment,
			PageElementHandler handler, PageElementTarget target, int column) {
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
			PageElement element = simpleHandler.createPageElement(getLocale());
			doAddPageElement(ureq, element, refenceFragment, target, column);
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
					// components are hierarchically build, elements are flat
					int indexCmp = editorCmp.indexOfRootComponent(referenceFragment);
					int indexEl = provider.indexOf(referenceFragment.getElement());
					if(indexEl < 0) {
						indexEl = indexCmp;
					}
					if(target == PageElementTarget.below) {
						indexCmp = indexCmp + 1;
						indexEl = indexEl + 1;
					}
					element = provider.appendPageElementAt(element, indexEl);
					fragment = createFragmentComponent(ureq, element);
					editorCmp.addRootComponent(indexCmp, fragment);	
				} else if(parent instanceof ContentEditorContainerComponent) {
					ContentEditorContainerComponent container = (ContentEditorContainerComponent)parent;
					element = provider.appendPageElement(element);
					fragment = createFragmentComponent(ureq, element);
					container.addElement(ureq, fragment, referenceFragment, target);
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
		
		updateImportButtonVisibility();
		
		return fragment;
	}
	
	private ContentEditorFragment doAddPageElementInContainer(UserRequest ureq, ContentEditorFragment referenceFragment,
			PageElement element, int column) {
		ContentEditorFragment fragment = null;
		if(referenceFragment instanceof ContentEditorContainerComponent) {
			PageElement pageElement = provider.appendPageElement(element);
			fragment = createFragmentComponent(ureq, pageElement);
			ContentEditorContainerComponent containerCmp = (ContentEditorContainerComponent)referenceFragment;
			containerCmp.setElementAt(ureq, fragment, column, null);
		}
		
		updateImportButtonVisibility();
		
		return fragment;
	}

	private ContentEditorFragment doAddPageElementAtTheEnd(UserRequest ureq, PageElement element) {
		PageElement pageElement = provider.appendPageElement(element);
		ContentEditorFragment fragment = createFragmentComponent(ureq, pageElement);
		editorCmp.addRootComponent(fragment);
		
		updateImportButtonVisibility();
		
		return fragment;
	}
	
	private void doSaveElement(UserRequest ureq) {
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doSaveElement(UserRequest ureq, ContentEditorFragment fragment) {
		fragment.setEditMode(false);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doCloneElement(UserRequest ureq, ContentEditorFragment fragment) {
		ContentEditorFragment clonedFragment = doCloneAndAddElement(ureq, fragment);
		
		doCloneContainerElements(ureq, clonedFragment, fragment.getElement());
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	private ContentEditorFragment doCloneAndAddElement(UserRequest ureq, ContentEditorFragment fragment) {
		PageElement element = fragment.getElement();
		
		CloneElementHandler cloneHandler = cloneHandlerMap.get(element.getType());
		if (cloneHandler == null) {
			logError("Cannot find a cloneable handler of type: " + element.getType(), null);
			return null;
		}
		
		doCloseAllEditionEvent(ureq);
		PageElement clonedElement = cloneHandler.clonePageElement(element);
		ContentEditorFragment clonedFragment = null;
		if (clonedElement != null) {
			clonedFragment = doAddPageElement(ureq, clonedElement, fragment, PageElementTarget.below, 0);
		}
		return clonedFragment;
	}

	private void doCloneContainerElements(UserRequest ureq, ContentEditorFragment clonedFragment, PageElement originalElement) {
		if (clonedFragment != null && originalElement instanceof Container) {
			Map<String, ? extends PageElement> idToElement = provider.getElements().stream()
					.collect(Collectors.toMap(PageElement::getId, Function.identity()));
			List<ContainerColumn> columns = ((Container)originalElement).getContainerSettings().getColumns();
			for (int columnIndex = 0; columnIndex < columns.size(); columnIndex++) {
				for (String elementId : columns.get(columnIndex).getElementIds()) {
					PageElement innerElementToClone = idToElement.get(elementId);
					if (innerElementToClone != null) {
						CloneElementHandler innerCloneHandler = cloneHandlerMap.get(innerElementToClone.getType());
						if (innerCloneHandler == null) {
							logError("Cannot find a cloneable handler of type: " + innerElementToClone.getType(), null);
							continue;
						}
						
						PageElement innerClonedElement = innerCloneHandler.clonePageElement(innerElementToClone);
						if (innerClonedElement != null) {
							ContentEditorFragment innerClonedFragment = doAddPageElementInContainer(ureq, clonedFragment, innerClonedElement, columnIndex);
							doCloneContainerElements(ureq, innerClonedFragment, innerElementToClone);
						}
					}
				}
			}
		}
	}
	
	private void doDeleteElement(UserRequest ureq, ContentEditorFragment fragment, boolean confirm) {
		List<Component> ancestors = ComponentHelper.findAncestorsOrSelfByID(editorCmp, fragment);
		int index = ancestors.indexOf(fragment);
		if(index == 0 && ancestors.size() >= 2) {// the root component is always the editor itself
			if (confirm && provider.isRemoveConfirmation(fragment.getElement())) {
				doDeleteConfirmation(ureq, fragment);
			} else {
				doDeleteElement(ureq, fragment, ancestors, index);
			}
		}
	}

	private void doDeleteConfirmation(UserRequest ureq, ContentEditorFragment fragment) {
		deleteConfirmationCtrl = new DeleteConfirmationController(ureq, getWindowControl(), getTranslator(),
				provider.getRemoveConfirmationI18nKey(), fragment);
		listenTo(deleteConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), null, deleteConfirmationCtrl.getInitialComponent(),
				true, translate("delete.element"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doDeleteElement(UserRequest ureq, ContentEditorFragment fragment, List<Component> ancestors,
			int index) {
		provider.removePageElement(fragment.getElement());
		Component parent = ancestors.get(index + 1);
		if(parent == editorCmp) {
			if(fragment instanceof ContentEditorContainerComponent) {
				moveElementsToPreviousContainer(ureq, (ContentEditorContainerComponent)fragment);
			}
			editorCmp.removeRootComponent(fragment);
		} else if(parent instanceof ContentEditorContainerComponent) {
			ContentEditorContainerComponent container = (ContentEditorContainerComponent)parent;
			if(fragment instanceof ContentEditorContainerComponent) {
				moveElementsToContainerSlot(ureq, (ContentEditorContainerComponent)fragment, container);
			}
			container.removeElementAt(ureq, fragment);
		}
		updateImportButtonVisibility();
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	/**
	 * Transfer the element of the container to a previous container. Only
	 * for root containers.
	 * 
	 * @param container The container to empty
	 */
	private void moveElementsToPreviousContainer(UserRequest ureq, ContentEditorContainerComponent container) {
		List<ContentEditorFragment> fragmentsToTransfer = container.getAllContentEditorFragments();
		if(fragmentsToTransfer.isEmpty()) {
			return;
		}
		
		ContentEditorContainerComponent previousContainer = editorCmp.previousRootContainerComponent(container);
		if(previousContainer != null) {
			previousContainer.transferElements(ureq, fragmentsToTransfer);
		} else {
			ContentEditorContainerComponent nextContainer = editorCmp.nextRootContainerComponent(container);
			if(nextContainer != null) {
				nextContainer.transferElements(ureq, fragmentsToTransfer);
			}
		}
	}
	
	/**
	 * 
	 * @param container The container to empty
	 * @param parent The parent of the container to empty
	 */
	private void moveElementsToContainerSlot(UserRequest ureq, ContentEditorContainerComponent container, ContentEditorContainerComponent parent) {
		List<ContentEditorFragment> fragmentsToTransfer = container.getAllContentEditorFragments();
		if(fragmentsToTransfer.isEmpty()) {
			return;
		}

		int slot = parent.getContainerSettings().getColumnIndex(container.getElementId());
		if(slot > 0) {
			parent.transferElements(ureq, fragmentsToTransfer, slot);
		} else {
			parent.transferElements(ureq, fragmentsToTransfer);
		}
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
				container.moveUp(ureq, fragment.getElementId());
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
				container.moveDown(ureq, fragment.getElementId());
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
			container.removeElementAt(ureq, source);
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
			container.removeElementAt(ureq, source);
		} else {
			editorCmp.setDirty(true);
			return;
		}
		
		if(target instanceof ContentEditorContainerComponent) {
			ContentEditorContainerComponent targetContainer = (ContentEditorContainerComponent)target;
			// Containers are never dropped in an other container, it's forbidden
			if(source instanceof ContentEditorContainerComponent) {
				ok = moveContainerInEditor(target, source, after);
			} else {
				targetContainer.setElementAt(ureq, source, dropEvent.getSlot(), null);
				ok = true;
			}
		} else if(targetParent instanceof ContentEditorContainerComponent) {
			ContentEditorContainerComponent targetContainer = (ContentEditorContainerComponent)targetParent;
			PageElementTarget pos = after ? PageElementTarget.below : PageElementTarget.above;
			targetContainer.addElement(ureq, source, target, pos);
			ok = true;
		} else if(targetParent == editorCmp) {
			ok = moveContainerInEditor(target, source, after);
		}

		if(!ok) {
			editorCmp.setDirty(true);
		}
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private boolean moveContainerInEditor(ContentEditorFragment target, ContentEditorFragment source, boolean after) {
		boolean ok = false;
		int index = editorCmp.indexOfRootComponent(target);
		if(index >= 0) {
			provider.movePageElement(source.getElement(), target.getElement(), after);
			if(after) {
				index++;
			}
			editorCmp.addRootComponent(index, source);
			ok = true;
		}
		return ok;
	}
	
	private ContentEditorFragment createFragmentComponent(UserRequest ureq, PageElement element) {
		PageElementHandler handler = handlerMap.get(element.getType());
		if(handler == null) {
			logError("Cannot find an handler of type: " + element.getType(), null);
			return null;
		}
		
		PageRunElement viewPart = handler.getContent(ureq, getWindowControl(), element, new StandardMediaRenderingHints());
		Controller editorPart = handler.getEditor(ureq, getWindowControl(), element);
		if(editorPart != null) {
			listenTo(editorPart);
		}
		String cmpId = "frag-" + (++counter);
		
		PageElementInspectorController inspectorPart = handler.getInspector(ureq, getWindowControl(), element);
		if(inspectorPart != null) {
			if(editorPart instanceof ControllerEventListener) {
				inspectorPart.addControllerListener((ControllerEventListener)editorPart);
			}
			if(inspectorPart instanceof ControllerEventListener) {
				editorPart.addControllerListener((ControllerEventListener)inspectorPart);
			}
			
			inspectorPart = new ModalInspectorController(ureq, getWindowControl(), inspectorPart, element);
			inspectorPart.getInitialComponent().setVisible(false);
			inspectorPart.getInitialComponent().setDirty(false);
			listenTo(inspectorPart);
		}
		
		if(viewPart instanceof ControllerEventListener) {
			if(editorPart != null) {
				editorPart.addControllerListener((ControllerEventListener)viewPart);
			}
			if(inspectorPart != null) {
				inspectorPart.addControllerListener((ControllerEventListener)viewPart);
			}
		}
		
		ContentEditorFragment cmp;
		if(element instanceof ContainerElement) {
			cmp = new ContentEditorContainerComponent(cmpId, (ContainerEditorController)editorPart, inspectorPart);
		} else {
			cmp = new ContentEditorFragmentComponent(cmpId, element, viewPart, editorPart, inspectorPart);
		}
		cmp.setCloneable(secCallback.canCloneElement() && cloneHandlerMap.containsKey(element.getType()));
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