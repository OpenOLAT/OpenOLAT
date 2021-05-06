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

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.model.ContainerSettings;
import org.olat.modules.ceditor.ui.ContainerEditorController;
import org.olat.modules.ceditor.ui.PageElementTarget;
import org.olat.modules.ceditor.ui.event.CloneElementEvent;
import org.olat.modules.ceditor.ui.event.ContainerRuleLinkEvent;
import org.olat.modules.ceditor.ui.event.DeleteElementEvent;
import org.olat.modules.ceditor.ui.event.DropToPageElementEvent;
import org.olat.modules.ceditor.ui.event.EditElementEvent;
import org.olat.modules.ceditor.ui.event.MoveDownElementEvent;
import org.olat.modules.ceditor.ui.event.MoveUpElementEvent;
import org.olat.modules.ceditor.ui.event.OpenAddElementEvent;
import org.olat.modules.ceditor.ui.event.OpenRulesEvent;
import org.olat.modules.ceditor.ui.event.PositionEnum;

/**
 * 
 * Initial date: 6 déc. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ContentEditorContainerComponent extends FormBaseComponentImpl implements ContentEditorFragment, ComponentEventListener, ControllerEventListener {
	
	private static final Logger log = Tracing.createLoggerFor(ContentEditorContainerComponent.class);
	private static final ContentEditorContainerComponentRenderer RENDERER = new ContentEditorContainerComponentRenderer();

	private boolean editMode = false;
	private boolean moveable = false;
	private boolean cloneable = false;
	private boolean deleteable = false;
	private boolean ruleLinkEnabled = false;
	
	private final ContainerEditorController editorPart;

	private List<Component> components = new ArrayList<>();
	
	public ContentEditorContainerComponent(String name, ContainerEditorController editorPart) {
		super(name);
		this.editorPart = editorPart;
		editorPart.addControllerListener(this);
		setDomReplacementWrapperRequired(false);
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
					setEditMode(true);
					fireEvent(ureq, new EditElementEvent(editorPart.getContainer().getId()));
					break;
				case "change_nbre_columns":
					setNumOfColumns(Integer.parseInt(columns));
					setDirty(true);
					fireEvent(ureq, Event.CHANGED_EVENT);
					break;
				case "save_element":
				case "close_edit_fragment":
					doCloseEditFragment();
					break;
				case "clone_element":
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
					String linkId =	"o_ccad_" + getElementId() + "_" + columns;
					fireEvent(ureq, new OpenAddElementEvent(linkId, this, PageElementTarget.within, Integer.parseInt(columns)));
					break;
				case "drop_fragment":
					doDropFragment(ureq);
					break;
				case "add_element_above":
					String aboveLinkId = "o_ccaab_".concat(getDispatchID());
					fireEvent(ureq, new OpenAddElementEvent(aboveLinkId, this, PageElementTarget.above));
					break;
				case "add_element_below":
					String belowLinkId = "o_ccabe_".concat(getDispatchID());
					fireEvent(ureq, new OpenAddElementEvent(belowLinkId, this, PageElementTarget.below));
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
			editorPart.setEditMode(editMode);
			setDirty(true);
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
	
	public void setElementAt(ContentEditorFragment component, int column, String sibling) {
		if(!checkAdd(component)) return;

		editorPart.setElementAt(component.getElementId(), column, sibling);
		addComponent(component);
		setDirty(true);
	}
	
	public void removeElementAt(ContentEditorFragment component) {
		editorPart.removeElement(component.getElementId());
		removeComponent(component);
		setDirty(true);
	}
	
	public void moveUp(String fragmentId) {
		editorPart.moveUp(fragmentId);
		setDirty(true);
	}
	
	public void moveDown(String fragmentId) {
		editorPart.moveDown(fragmentId);
		setDirty(true);
	}
	
	public void addElement(ContentEditorFragment newComponent, ContentEditorFragment collocator, PageElementTarget target) {
		if(!checkAdd(newComponent)) return;
		
		editorPart.addElement(newComponent.getElementId(), collocator.getElementId(), target);
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
		fireEvent(ureq, new DropToPageElementEvent(sourceId, this, slot,
				PositionEnum.valueOf(position, PositionEnum.bottom)));
	}
	
	private void doCloseEditFragment() {
		editMode = false;
		editorPart.setEditMode(editMode);
		setDirty(true);
	}
	
	protected void setNumOfColumns(int numOfColumns) {
		ContainerSettings settings = getContainerSettings();
		settings.setNumOfColumns(numOfColumns);
		editorPart.setNumOfColumns(numOfColumns);
	}

	@Override
	public String getElementId() {
		return editorPart.getContainer().getId();
	}
	
	@Override
	public PageElement getElement() {
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
