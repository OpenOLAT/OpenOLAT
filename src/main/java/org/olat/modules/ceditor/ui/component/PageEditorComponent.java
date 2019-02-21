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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.ControllerEventListener;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.render.ValidationResult;
import org.olat.modules.ceditor.PageEditorProvider;
import org.olat.modules.ceditor.ui.AddElementsController;
import org.olat.modules.ceditor.ui.PageElementTarget;
import org.olat.modules.ceditor.ui.event.AddElementEvent;
import org.olat.modules.ceditor.ui.event.ContainerColumnEvent;
import org.olat.modules.ceditor.ui.event.DropFragmentEvent;
import org.olat.modules.ceditor.ui.event.EditFragmentEvent;
import org.olat.modules.ceditor.ui.event.EditionEvent;
import org.olat.modules.ceditor.ui.model.EditorFragment;

/**
 * 
 * Initial date: 11 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageEditorComponent extends FormBaseComponentImpl implements ComponentCollection, ComponentEventListener, ControllerEventListener {
	
	private static final PageEditorComponentRenderer RENDERER = new PageEditorComponentRenderer();
	
	private PageEditorModel editorModel;

	private final Link container1Column;
	private final Link container2Columns;
	private final Link container3Columns;
	private final Link container4Columns;

	private final WindowControl wControl;
	private AddElementsController addElementsCtrl;
	private CloseableCalloutWindowController callout;
	
	private final PageEditorProvider provider;
	
	public PageEditorComponent(String name, WindowControl wControl, PageEditorProvider provider) {
		super(name);
		this.wControl = wControl;
		this.provider = provider;
		setDomReplacementWrapperRequired(false);

		container1Column = LinkFactory.createLink("container.col.1", "text.column.1", null, this);
		container2Columns = LinkFactory.createLink("container.col.2", "text.column.2", null, this);
		container3Columns = LinkFactory.createLink("container.col.3", "text.column.3", null, this);
		container4Columns = LinkFactory.createLink("container.col.4", "text.column.4", null, this);
	}

	public PageEditorModel getModel() {
		return editorModel;
	}

	public void setModel(PageEditorModel editorModel) {
		this.editorModel = editorModel;
	}

	public Link getContainer1Column() {
		return container1Column;
	}
	
	public Link getContainer2Columns() {
		return container2Columns;
	}

	public Link getContainer3Columns() {
		return container3Columns;
	}

	public Link getContainer4Columns() {
		return container4Columns;
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Component source, Event event) {
		if(source == container1Column) {
			processContainerEvent(ureq, 1);
		} else if(source == container2Columns) {
			processContainerEvent(ureq, 2);
		} else if(source == container3Columns) {
			processContainerEvent(ureq, 3);
		} else if(source == container4Columns) {
			processContainerEvent(ureq, 4);
		}
	}

	private void processContainerEvent(UserRequest ureq, int numOfColumns) {
		EditorFragment fragment = editorModel.getEditedFragment();
		if(fragment != null) {
			fireEvent(ureq, new ContainerColumnEvent(fragment, numOfColumns));
		}
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		String cmd = ureq.getParameter(VelocityContainer.COMMAND_ID);
		String fragment = ureq.getParameter("fragment");
		if("edit_fragment".equals(cmd)) {
			doEditFragment(ureq, fragment);
		} else if("close_edit_fragment".equals(cmd)) {
			doCloseEditFragment();
		} else if("drop_fragment".equals(cmd)) {
			processDropFragment(ureq);
		}  else if("add_to_container".equals(cmd)) {
			doAddElementsCallout(ureq);
		} else {
			EditorFragment editedFragment = editorModel.getEditedFragment();
			if(editedFragment != null) {
				Link link = getLink(cmd, editedFragment);
				fireEvent(ureq, new EditionEvent(cmd, editedFragment, link));
			}
		}
	}
	
	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		if(addElementsCtrl == source) {
			callout.deactivate();
			if(event instanceof AddElementEvent) {
				fireEvent(ureq, event);
			}
			cleanUp();
		} else if(callout == source) {
			if(CloseableCalloutWindowController.CLOSE_WINDOW_EVENT == event) {
				cleanUp();
			}
		}
	}
	
	private void cleanUp() {
		if(addElementsCtrl != null) {
			addElementsCtrl.removeControllerListener(this);
			addElementsCtrl = null;
		}
		if(callout != null) {
			callout.removeControllerListener(this);
			callout = null;
		}
	}

	private Link getLink(String cmd, EditorFragment editedFragment) {
		if("add.element.above".equals(cmd)) {
			return editedFragment.getAddElementAboveLink();
		} else if("add.element.below".equals(cmd)) {
			return editedFragment.getAddElementBelowLink();
		} else if("save.element".equals(cmd)) {
			return editedFragment.getSaveLink();
		} else if("delete.element".equals(cmd)) {
			return editedFragment.getDeleteLink();
		} else if("move.up.element".equals(cmd)) {
			return editedFragment.getMoveUpLink();
		} else if("move.down.element".equals(cmd)) {
			return editedFragment.getMoveDownLink();
		}
		return null;
	}
	
	private void processDropFragment(UserRequest ureq) {
		String fragmentCmpId = ureq.getParameter("dragged");
		String sourceCmpId = ureq.getParameter("source");
		String targetCmpId = ureq.getParameter("target");
		String siblingCmpId = ureq.getParameter("sibling");
		String containerCmpId = ureq.getParameter("container");
		String slotId = ureq.getParameter("slot");
		fireEvent(ureq, new DropFragmentEvent(fragmentCmpId, sourceCmpId, targetCmpId, siblingCmpId, containerCmpId, slotId));
		setDirty(true);
	}
	
	private void doEditFragment(UserRequest ureq, String fragmentName) {
		EditorFragment editedFragment = null;
		for(EditorFragment fragment:editorModel.getFragments()) {
			boolean editMode = fragment.getComponentName().equals(fragmentName);
			fragment.setEditMode(editMode);
			if(editMode) {
				editedFragment = fragment;
			}
		}
		setDirty(true);
		fireEvent(ureq, new EditFragmentEvent(editedFragment));
	}
	
	private void doCloseEditFragment() {
		for(EditorFragment fragment:editorModel.getFragments()) {
			fragment.setEditMode(false);
		}
		setDirty(true);
	}
	
	private void doAddElementsCallout(UserRequest ureq) {
		String cmpId = ureq.getParameter("container");
		String column = ureq.getParameter("column");
		EditorFragment referenceFragment = editorModel.getFragmentByCmpId(cmpId);
		String containerId = referenceFragment.getPageElement().getId();
		String targetId = "o_ccad_" + containerId + "_" + column;
		
		addElementsCtrl = new AddElementsController(ureq, wControl, provider,
				referenceFragment, PageElementTarget.within, Integer.parseInt(column), getTranslator());
		addElementsCtrl.addControllerListener(this);
		
		callout = new CloseableCalloutWindowController(ureq, wControl, addElementsCtrl.getInitialComponent(),
				targetId, "Filter", true, "o_sel_flexi_filter_callout");
		callout.addControllerListener(this);
		callout.activate();
	}

	@Override
	public Component getComponent(String name) {
		if(container1Column.getComponentName().equals(name)) {
			return container1Column;
		}
		if(container2Columns.getComponentName().equals(name)) {
			return container2Columns;
		}
		if(container3Columns.getComponentName().equals(name)) {
			return container3Columns;
		}
		if(container3Columns.getComponentName().equals(name)) {
			return container3Columns;
		}
		
		for(EditorFragment fragment:editorModel.getFragments()) {
			if(fragment.isEditMode()) {
				List<Link> tools = fragment.getAdditionalTools();
				if(tools != null && !tools.isEmpty()) {
					for(Link tool:tools) {
						if(tool.getComponentName().equals(name)) {
							return tool;
						}
					}
				}
			}
		}
		return null;
	}

	@Override
	public Iterable<Component> getComponents() {
		List<EditorFragment> fragments = editorModel.getFragments();
		List<Component> components = new ArrayList<>(fragments.size() + 20);
		components.add(container1Column);
		components.add(container2Columns);
		components.add(container3Columns);
		components.add(container4Columns);
		
		for(EditorFragment fragment:fragments) {
			Component cmp = fragment.getComponent();
			if(cmp != null) {
				components.add(cmp);
			}
			List<Link> tools = fragment.getAdditionalTools();
			if(tools != null && !tools.isEmpty()) {
				for(Link tool:tools) {
					components.add(tool);
				}
			}
		}
		return components;
	}

	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		vr.getJsAndCSSAdder().addRequiredStaticJsFile("js/dragula/dragula.js");
		vr.getJsAndCSSAdder().addRequiredStaticJsFile("js/jquery/openolat/jquery.contenteditor.js");
		super.validate(ureq, vr);
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
