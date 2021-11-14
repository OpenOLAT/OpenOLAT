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
package org.olat.modules.library.ui;

import java.util.Collection;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.FormUIFactory;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.tree.MenuTreeItem;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.filters.VFSContainerFilter;
import org.olat.modules.library.LibraryManager;
import org.olat.modules.library.ui.comparator.FilenameComparator;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Select the destination or the destinations of the reviewed document.<br>
 * Events fired:
 * <ul>
 * 	<li>ACTIVATE_NEXT</li>
 * </ul>
 * <P>
 * Initial Date:  2 oct. 2009 <br>
 *
 * @author twuersch, srosse
 */
public class DestinationAcceptStepController extends StepFormBasicController {
	
	public static final String STEPS_RUN_CONTEXT_DESTINATIONFOLDERS_KEY = "destination";
	public static final String STEPS_RUN_CONTEXT_DESTINATIONFOLDERS_MODEL_KEY = "destination_model";
	private MenuTreeItem treeMultipleSelectionElement;
	private final LibraryTreeModel treeModel;
	
	@Autowired
	private LibraryManager libraryManager;
	
	public DestinationAcceptStepController(UserRequest ureq, WindowControl wControl, StepsRunContext stepsRunContext, Form rootForm) {
		super(ureq, wControl, rootForm, stepsRunContext, LAYOUT_VERTICAL, null);
		
		if(containsRunContextKey(STEPS_RUN_CONTEXT_DESTINATIONFOLDERS_MODEL_KEY)) {
			treeModel = (LibraryTreeModel)getFromRunContext(STEPS_RUN_CONTEXT_DESTINATIONFOLDERS_MODEL_KEY);
		} else {
			VFSContainer folder = libraryManager.getSharedFolder();
			if (folder == null) throw new OLATRuntimeException("no library-folder setup. you cannot publish items as long as no ressource-folder is configured!" , null);
			treeModel = new LibraryTreeModel(folder, new VFSContainerFilter(), new FilenameComparator(getLocale()), getLocale(), false);
			treeModel.getRootNode().setTitle(translate("main.menu.title"));
			treeModel.setIconCssClass("b_filetype_folder");
		}		
		
		initForm(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		Collection<String> selection = treeMultipleSelectionElement.getSelectedKeys();
		if (selection.isEmpty()) {
			showError("acceptstep.destination.noselectionerror");
		} else {
			addToRunContext(STEPS_RUN_CONTEXT_DESTINATIONFOLDERS_KEY, selection);
			addToRunContext(STEPS_RUN_CONTEXT_DESTINATIONFOLDERS_MODEL_KEY, treeModel);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormUIFactory formUIFactory = FormUIFactory.getInstance();
		treeMultipleSelectionElement = formUIFactory.addTreeMultiselect("acceptstep.destination.treename", null, formLayout, treeModel, this);
		treeMultipleSelectionElement.setMultiSelect(true);
		
		TreeNode rootNode = treeModel.getRootNode();
		for(int i=rootNode.getChildCount(); i-->0; ) {
			treeMultipleSelectionElement.open((TreeNode)rootNode.getChildAt(i));
		}
		
		if (containsRunContextKey(STEPS_RUN_CONTEXT_DESTINATIONFOLDERS_KEY)) {
			@SuppressWarnings("unchecked")
			Set<String> selection = (Set<String>) getFromRunContext(STEPS_RUN_CONTEXT_DESTINATIONFOLDERS_KEY);
			for (String key : selection) {
				treeMultipleSelectionElement.select(key, true);
			}
		}
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		super.event(ureq, source, event);
		addToRunContext(STEPS_RUN_CONTEXT_DESTINATIONFOLDERS_KEY, treeMultipleSelectionElement.getSelectedKeys());
		addToRunContext(STEPS_RUN_CONTEXT_DESTINATIONFOLDERS_MODEL_KEY, treeModel);
	}
}