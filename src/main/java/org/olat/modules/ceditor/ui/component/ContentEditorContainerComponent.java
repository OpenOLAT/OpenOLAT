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
package org.olat.modules.ceditor.ui.component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.model.ContainerElement;
import org.olat.modules.ceditor.model.ContainerSettings;
import org.olat.modules.ceditor.ui.ContainerEditorController;
import org.olat.modules.ceditor.ui.PageElementTarget;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;
import org.olat.modules.ceditor.ui.event.CloneElementEvent;
import org.olat.modules.ceditor.ui.event.CloseElementsEvent;
import org.olat.modules.ceditor.ui.event.ContainerRuleLinkEvent;
import org.olat.modules.ceditor.ui.event.DeleteElementEvent;
import org.olat.modules.ceditor.ui.event.DropToPageElementEvent;
import org.olat.modules.ceditor.ui.event.EditElementEvent;
import org.olat.modules.ceditor.ui.event.MoveDownElementEvent;
import org.olat.modules.ceditor.ui.event.MoveUpElementEvent;
import org.olat.modules.ceditor.ui.event.OpenAddElementEvent;
import org.olat.modules.ceditor.ui.event.OpenAddLayoutEvent;
import org.olat.modules.ceditor.ui.event.OpenRulesEvent;
import org.olat.modules.ceditor.ui.event.PositionEnum;

/**
 * 
 * Initial date: 6 déc. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ContentEditorContainerComponent extends AbstractComponent implements ContentEditorFragment, ComponentEventListener, ControllerEventListener {
	
	private static final Logger log = Tracing.createLoggerFor(ContentEditorContainerComponent.class);
	private static final ContentEditorContainerComponentRenderer RENDERER = new ContentEditorContainerComponentRenderer();

	private boolean editMode = false;
	private boolean moveable = false;
	private boolean cloneable = false;
	private boolean deleteable = false;
	private boolean ruleLinkEnabled = false;
	
	private final Controller inspectorPart;
	private final ContainerEditorController editorPart;

	private List<Component> components = new ArrayList<>();
	
	public ContentEditorContainerComponent(String name, ContainerEditorController editorPart, Controller inspectorPart) {
		super(name);
		this.editorPart = editorPart;
		this.inspectorPart = inspectorPart;

		editorPart.addControllerListener(this);
		setDomReplacementWrapperRequired(false);
		if(inspectorPart != null) {
			components.add(inspectorPart.getInitialComponent());
			inspectorPart.addControllerListener(this);
		}
	}

	@Override
	public void setTranslator(Translator translator) {
		super.setTranslator(translator);
		for(Component component:components) {
			component.setTranslator(translator);
		}
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		String cmd = ureq.getParameter(VelocityContainer.COMMAND_ID);
		String fragment = ureq.getParameter("fragment");
		String columns = ureq.getParameter("column");
		if(cmd != null && fragment != null && getComponentName().equals(fragment)) {
			switch(cmd) {
				case "edit_fragment":
					if(isEditMode()) {
						doCloseEditFragment();
					} else {
						setEditMode(true);
						fireEvent(ureq, new EditElementEvent(editorPart.getContainer().getId()));
					}
					break;
				case "save_element":
				case "close_edit_fragment":
					doCloseEditFragment();
					break;
				case "clone_element":
					fireEvent(ureq, new CloneElementEvent(this));
					break;
				case "clone_inspector":
					fireEvent(ureq, new CloneElementEvent(this));
					break;	
				case "delete_element":
					fireEvent(ureq, new DeleteElementEvent(this));
					break;
				case "move_up":
					fireEvent(ureq, new MoveUpElementEvent(this));
					break;
				case "move_down":
					fireEvent(ureq, new MoveDownElementEvent(this));
					break;
				case "add_to_container":
					// First stop editing
					fireEvent(ureq, new CloseElementsEvent());
					// Open the callout
					String linkId =	"o_ccad_" + getElementId() + "_" + columns;
					fireEvent(ureq, new OpenAddElementEvent(linkId, this, PageElementTarget.within, Integer.parseInt(columns)));
					break;
				case "drop_fragment":
					doDropFragment(ureq);
					break;
				case "add_element_above":
					String aboveLinkId = "o_cmore_".concat(getDispatchID());
					fireEvent(ureq, new OpenAddLayoutEvent(aboveLinkId, this, PageElementTarget.above));
					break;
				case "add_element_below":
					String belowLinkId = "o_cmore_".concat(getDispatchID());
					fireEvent(ureq, new OpenAddLayoutEvent(belowLinkId, this, PageElementTarget.below));
					break;
				case "change_name":
					String nameLinkId = "o_cname_".concat(getElementId());
					editorPart.openNameCallout(ureq, nameLinkId);
					break;
				case "open_rules":
					fireEvent(ureq, new OpenRulesEvent());
					break;
				default:
					log.error("Uncatched dispatch to container {} with command {}", getComponentName(), cmd);
					break;
			}
		}
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		if (source == editorPart) {
			if (event instanceof ContainerRuleLinkEvent) {
				ContainerRuleLinkEvent crle = (ContainerRuleLinkEvent)event;
				boolean containsId = crle.getElementIds().contains(editorPart.getContainer().getId());
				if (containsId != ruleLinkEnabled) {
					ruleLinkEnabled = containsId;
					setDirty(true);
				}
			}
		} else if(source == inspectorPart) {
			if (event instanceof ChangePartEvent) {
				ChangePartEvent crle = (ChangePartEvent)event;
				editorPart.reload((ContainerElement)crle.getElement());
				setDirty(true);
			}
		}
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	public boolean isEditMode() {
		return editMode;
	}

	@Override
	public void setEditMode(boolean editMode) {
		if(this.editMode != editMode) {
			this.editMode = editMode;
			if(this.editMode) {
				setInspectorVisible(true, false);
			}
			setDirty(true);
		} else if(this.editMode && !isInspectorVisible()) {
			setInspectorVisible(true, false);
		}
	}

	@Override
	public boolean isInspectorVisible() {
		return inspectorPart != null && inspectorPart.getInitialComponent().isVisible();
	}

	@Override
	public void setInspectorVisible(boolean inspectorVisible, boolean silently) {
		if(isInspectorVisible() != inspectorVisible && inspectorPart != null) {
			inspectorPart.getInitialComponent().setVisible(inspectorVisible);
			inspectorPart.getInitialComponent().setDirty(false);
			if(!silently) {
				setDirty(true);
			}
		}
	}

	@Override
	public boolean isCloneable() {
		return cloneable;
	}

	@Override
	public void setCloneable(boolean cloneable) {
		this.cloneable = cloneable;
	}

	@Override
	public boolean isDeleteable() {
		return deleteable;
	}

	@Override
	public void setDeleteable(boolean enable) {
		deleteable = enable;
	}

	@Override
	public boolean isMoveable() {
		return moveable;
	}

	@Override
	public void setMoveable(boolean enable) {
		this.moveable = enable;
	}
	
	@Override
	public boolean isEditable() {
		return true;
	}

	public void setElementAt(UserRequest ureq, ContentEditorFragment component, int column, String sibling) {
		if(!checkAdd(component)) return;

		editorPart.setElementAt(ureq, component.getElementId(), column, sibling);
		addComponent(component);
		setDirty(true);
	}
	
	public void removeElementAt(UserRequest ureq, ContentEditorFragment component) {
		editorPart.removeElement(ureq, component.getElementId());
		removeComponent(component);
		setDirty(true);
	}
	
	public void transferElements(UserRequest ureq, List<ContentEditorFragment> fragments) {
		int lastSlot = editorPart.getLastSlot();
		transferElements(ureq, fragments, lastSlot);
	}
	
	public void transferElements(UserRequest ureq, List<ContentEditorFragment> fragments, int slot) {
		List<String> elementsIds = fragments.stream()
				.map(ContentEditorFragment::getElementId)
				.collect(Collectors.toList());
		editorPart.transferElements(ureq, elementsIds, slot);
		for(ContentEditorFragment fragment:fragments) {
			addComponent(fragment);
		}
		setDirty(true);
	}
	
	public void moveUp(UserRequest ureq, String fragmentId) {
		editorPart.moveUp(ureq, fragmentId);
		setDirty(true);
	}
	
	public void moveDown(UserRequest ureq, String fragmentId) {
		editorPart.moveDown(ureq, fragmentId);
		setDirty(true);
	}
	
	public void addElement(UserRequest ureq, ContentEditorFragment newComponent, ContentEditorFragment collocator, PageElementTarget target) {
		if(!checkAdd(newComponent)) return;
		
		editorPart.addElement(ureq, newComponent.getElementId(), collocator.getElementId(), target);
		addComponent(newComponent);
		setDirty(true);
	}
	
	private boolean checkAdd(ContentEditorFragment componentToAdd) {
		if(componentToAdd == this) {
			log.warn("Add container to itself: {}", componentToAdd);
			setDirty(true);
			return false;
		}
		return true;
	}
	
	private void doDropFragment(UserRequest ureq) {
		String sourceId = ureq.getParameter("source");
		String slotId = ureq.getParameter("slot");
		int slot = -1;
		if(StringHelper.isLong(slotId)) {
			slot = Integer.parseInt(slotId);
		}
		String position = ureq.getParameter("position");
		String target = ureq.getParameter("target");
		if(StringHelper.containsNonWhitespace(target)) {
			Component editFragment = getComponent(target);
			if(editFragment instanceof ContentEditorFragment) {
				fireEvent(ureq, new DropToPageElementEvent(sourceId, (ContentEditorFragment)editFragment,
						PositionEnum.valueOf(position, PositionEnum.top)));
			}
		} else {
			fireEvent(ureq, new DropToPageElementEvent(sourceId, this, slot,
					PositionEnum.valueOf(position, PositionEnum.bottom)));
		}
	}
	
	private void doCloseEditFragment() {
		editMode = false;
		setInspectorVisible(false, false);
		setDirty(true);
	}

	@Override
	public String getElementId() {
		return editorPart.getContainer().getId();
	}
	
	@Override
	public ContainerElement getElement() {
		return editorPart.getContainer();
	}
	
	public boolean supportsName() {
		return editorPart.getContainer().supportsName();
	}

	public boolean isRuleLinkEnabled() {
		return ruleLinkEnabled;
	}

	public ContainerSettings getContainerSettings() {
		return editorPart.getContainer().getContainerSettings();
	}
	
	public ContentEditorFragment getComponentByElementId(String elementId) {
		for(Component component:getComponents()) {
			if(component instanceof ContentEditorFragment
					&& ((ContentEditorFragment)component).getElementId().equals(elementId)) {
				return (ContentEditorFragment)component;
			}
		}
		return null;
	}
	
	public List<ContentEditorFragment> getAllContentEditorFragments() {
		List<ContentEditorFragment> editorFragements = new ArrayList<>();
		for(Component component:getComponents()) {
			if(component instanceof ContentEditorFragment) {
				editorFragements.add((ContentEditorFragment)component);
			}
		}
		return editorFragements;
	}
	
	public void addComponent(ContentEditorFragment fragmentCmp) {
		if(fragmentCmp == null || components.contains(fragmentCmp)) return;
		
		components.add(fragmentCmp);
		if(getTranslator() != null) {
			fragmentCmp.setTranslator(getTranslator());
		}
	}
	
	public void removeComponent(ContentEditorFragment fragmentCmp) {
		if(fragmentCmp == null) return;
		components.remove(fragmentCmp);
	}

	public Component getInspectorComponent() {
		return inspectorPart == null ? null : inspectorPart.getInitialComponent();
	}

	@Override
	public Component getComponent(String name) {
		for(Component component:getComponents()) {
			if(component.getComponentName().equals(name)) {
				return component;
			}
		}
		return null;
	}

	@Override
	public Iterable<Component> getComponents() {
		return new ArrayList<>(components);
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
