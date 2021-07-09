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

package org.olat.course.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.util.CSSHelper;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.core.util.vfs.filters.VFSItemFilter;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.sp.SPEditController;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * Description:<br>
 * Select elements from the course directory and inject these files
 * as single page in the course.
 * VCRP-11
 *
 * <P>
 * Initial Date:  21 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MultiSPController extends FormBasicController {
	
	private FormLink selectAll;
	private FormLink deselectAll;
	private FormLink sameLevel;
	private FormLink asChild;
	private MultipleSelectionElement rootSelection;
	private final Map<String,MultipleSelectionElement> identToSelectionMap = new HashMap<>();
	private final List<MultipleSelectionElement> nodeSelections = new ArrayList<>();

	private int position;
	private CourseEditorTreeNode selectedNode;
	private final OLATResourceable ores;
	private final VFSContainer rootContainer;
	
	public MultiSPController(UserRequest ureq, WindowControl wControl, VFSContainer rootContainer,
			OLATResourceable ores, CourseEditorTreeNode selectedNode) {
		super(ureq, wControl, "choosesps");
		this.ores = ores;
		this.selectedNode = selectedNode;
		this.rootContainer = rootContainer;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("multi.sps.desc");
		
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutContainer = (FormLayoutContainer)formLayout;
			rootSelection = initTreeRec(0, rootContainer, layoutContainer);
			layoutContainer.contextPut("nodeSelections", nodeSelections);
		}

		selectAll = uifactory.addFormLink("checkall", "form.checkall", null, formLayout, Link.LINK);
		deselectAll = uifactory.addFormLink("uncheckall", "form.uncheckall", null, formLayout, Link.LINK);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("ok-cancel", getTranslator());
		formLayout.add(buttonLayout);
		buttonLayout.setRootForm(mainForm);
		
		if(selectedNode.getParent() != null) {
			sameLevel = uifactory.addFormLink("multi.sps.sameLevel", buttonLayout, Link.BUTTON);
		}
		asChild = uifactory.addFormLink("multi.sps.asChild", buttonLayout, Link.BUTTON);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	private MultipleSelectionElement initTreeRec(int level, VFSItem item, FormLayoutContainer layoutcont) {
		SelectNodeObject node = new SelectNodeObject(item, UUID.randomUUID().toString(), level);
	
		String[] singleKey = new String[]{ node.getId() };
		String[] singleValue = new String[]{ node.getName() };
		String[] iconCSS = new String[]{ "o_icon o_icon-fw " + node.getIconCssClass() };
		MultipleSelectionElement nodeSelection = uifactory.addCheckboxesVertical("print.node.list." + nodeSelections.size(), null, layoutcont,
				singleKey, singleValue, iconCSS, 1);
		nodeSelection.setLabel("multi.sps.file", null);
		
		nodeSelection.setUserObject(node);
		nodeSelection.addActionListener(FormEvent.ONCLICK);
		nodeSelections.add(nodeSelection);
		identToSelectionMap.put(node.getId(), nodeSelection);
		layoutcont.add(nodeSelection.getComponent().getComponentName(), nodeSelection);

		if(item instanceof VFSContainer) {
			VFSContainer container = (VFSContainer)item;
			List<VFSItem> subItems = container.getItems(new MultiSPVFSItemFilter());
			Collections.sort(subItems, new VFSItemNameComparator());
			
			for(VFSItem subItem:subItems) {	
				MultipleSelectionElement sel = initTreeRec(level + 1, subItem, layoutcont);
				node.getChildren().add(sel);
			}
		}
		
		return nodeSelection;
	}
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(nodeSelections.contains(source)) {
			MultipleSelectionElement nodeSelection = (MultipleSelectionElement)source;
			if(nodeSelection.isMultiselect()) {
				selectRec(nodeSelection, nodeSelection.isSelected(0));
			}
		} else if (source == selectAll) {
			for(MultipleSelectionElement nodeSelection:nodeSelections) {
				if(nodeSelection.isMultiselect() && !nodeSelection.isSelected(0)) {
					SelectNodeObject treeNode = (SelectNodeObject)nodeSelection.getUserObject();
					String id = treeNode.getId();
					nodeSelection.select(id, true);
				}
			}
		} else if (source == deselectAll) {
			for(MultipleSelectionElement nodeSelection:nodeSelections) {
				if(nodeSelection.isMultiselect() && nodeSelection.isSelected(0)) {
					SelectNodeObject treeNode = (SelectNodeObject)nodeSelection.getUserObject();
					String id = treeNode.getId();
					nodeSelection.select(id, false);
				}
			}
		} else if (source == asChild) {
			position = -1;
			ICourse course = CourseFactory.getCourseEditSession(ores.getResourceableId());
			create(rootSelection, course, selectedNode.getCourseNode());
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if (source == sameLevel) {
			ICourse course = CourseFactory.getCourseEditSession(ores.getResourceableId());
			CourseEditorTreeNode parentNode = (CourseEditorTreeNode)selectedNode.getParent();
			position = 0;
			for(position = parentNode.getChildCount(); position-->0; ) {
				if(selectedNode.getIdent().equals(parentNode.getChildAt(position).getIdent())) {
					position++;
					break;
				}
			}
			create(rootSelection, course, parentNode.getCourseNode());
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}
	
	private void create(MultipleSelectionElement selection, ICourse course, CourseNode parentNode) {
		SelectNodeObject node = (SelectNodeObject)selection.getUserObject();
		if(selection.isMultiselect() && selection.isSelected(0)) {
			VFSItem item = node.getItem();
			
			CourseNode newNode = null;
			if(item instanceof VFSLeaf) {
				//create node
				newNode = createCourseNode(parentNode, item, "sp");
				ModuleConfiguration moduleConfig = newNode.getModuleConfiguration();
				String path = VFSManager.getRelativeItemPath(item, rootContainer, null);
				moduleConfig.set(SPEditController.CONFIG_KEY_FILE, path);
				moduleConfig.setBooleanEntry(SPEditController.CONFIG_KEY_ALLOW_RELATIVE_LINKS, true);
			} else if (item instanceof VFSContainer) {
				//add structure
				newNode = createCourseNode(parentNode, item, "st");
			}
			
			int pos = -1;
			if(position >= 0 && selectedNode.getCourseNode().getIdent().equals(parentNode.getIdent())) {
				pos = position++;
			}
			
			if(pos < 0 || pos >= parentNode.getChildCount()) {
				course.getEditorTreeModel().addCourseNode(newNode, parentNode);
			} else {
				course.getEditorTreeModel().insertCourseNodeAt(newNode, parentNode, pos);
			}
			
			if (item instanceof VFSContainer) {
				parentNode = newNode;
			}
		}
		
		//recurse
		List<MultipleSelectionElement> childElements = node.getChildren();
		for(MultipleSelectionElement childElement:childElements) {
			create(childElement, course, parentNode);
		}
	}
	
	private CourseNode createCourseNode(CourseNode parent, VFSItem item, String type) {
		CourseNodeConfiguration newNodeConfig = CourseNodeFactory.getInstance().getCourseNodeConfiguration(type);
		CourseNode newNode = newNodeConfig.getInstance(parent);
		String name = item.getName();
		if (name.length() > NodeConfigController.SHORT_TITLE_MAX_LENGTH) {
			String shortName = name.substring(0, NodeConfigController.SHORT_TITLE_MAX_LENGTH - 1);
			newNode.setShortTitle(shortName);
			newNode.setLongTitle(name);
		} else {
			newNode.setShortTitle(name);
		}
		newNode.setLearningObjectives(item.getName());
		newNode.setNoAccessExplanation("You don't have access");
		return newNode;
	}
	
	/**
	 * @param nodeSelection The node that should be selected recursively
	 * @param select true: select the node and its children; false: deselect the node and its children
	 */
	private void selectRec(MultipleSelectionElement nodeSelection, boolean select) {
		SelectNodeObject userObject = (SelectNodeObject)nodeSelection.getUserObject();
		String id = userObject.getId();
		if(nodeSelection.isMultiselect()) {
			nodeSelection.select(id, select);
		}

		for(MultipleSelectionElement childSelection:userObject.getChildren()) {
			selectRec(childSelection, select);
		}
	}

	public static class SelectNodeObject {
		private final int indentation;
		private final VFSItem item;
		private final String id;
		private final List<MultipleSelectionElement> children = new ArrayList<>();
		
		public SelectNodeObject(VFSItem item, String id, int indentation) {
			this.id = id;
			this.item = item;
			this.indentation = indentation;
		}

		public String getIndentation() {
			return Integer.toString(indentation);
		}

		public VFSItem getItem() {
			return item;
		}
		
		public String getName() {
			return item.getName();
		}

		public String getId() {
			return id;
		}
		
		public List<MultipleSelectionElement> getChildren() {
			return children;
		}
		
		public String getIconCssClass() {
			if(item instanceof VFSContainer) {
				return "o_filetype_folder";
			}
			return CSSHelper.createFiletypeIconCssClassFor(item.getName());
		}
	}
	
	public static class MultiFileEvents extends Event {
		private static final long serialVersionUID = -5564537741338183501L;
		private final String currentPath;
		private final VFSContainer currentContainer;
		private final List<String> selectedFiles;
		
		public MultiFileEvents(VFSContainer currentContainer, String currentPath, List<String> selectedFiles) {
			super("multi");
			this.currentContainer = currentContainer;
			this.currentPath = currentPath;
			this.selectedFiles = selectedFiles;
		}

		public String getCurrentPath() {
			return currentPath;
		}

		public VFSContainer getCurrentContainer() {
			return currentContainer;
		}

		public List<String> getSelectedFiles() {
			return selectedFiles;
		}
	}
	
	public static class MultiSPVFSItemFilter implements VFSItemFilter {
		@Override
		public boolean accept(VFSItem vfsItem) {
			String name = vfsItem.getName();
			return !name.startsWith(".");
		}
	}
	
	public static class VFSItemNameComparator implements Comparator<VFSItem> {

		@Override
		public int compare(VFSItem o1, VFSItem o2) {
			if(o1 == null && o2 == null) return 0;
			if(o1 == null) return -1;
			if(o2 == null) return 1;
			
			String n1 = o1.getName();
			String n2 = o2.getName();
			
			if(n1 == null && n2 == null) return 0;
			if(n1 == null) return -1;
			if(n2 == null) return 1;
			return n1.compareToIgnoreCase(n2);
		}
	}
}