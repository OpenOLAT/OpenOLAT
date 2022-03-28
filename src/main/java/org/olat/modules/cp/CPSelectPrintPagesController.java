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
package org.olat.modules.cp;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.tree.MenuTreeItem;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;

/**
 * 
 * Description:<br>
 * Controller to select to list of nodes to print
 * VCRP-14
 * 
 * <P>
 * Initial Date:  9 mars 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CPSelectPrintPagesController extends FormBasicController {

	private final CPManifestTreeModel ctm;
	
	private FormLink selectAll;
	private FormLink deselectAll;
	private FormSubmit submit;
	private MenuTreeItem cpTree;
	
	public CPSelectPrintPagesController(UserRequest ureq, WindowControl wControl, CPManifestTreeModel ctm) {
		super(ureq, wControl, "cpprint");
		this.ctm = ctm;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("print.node.list.desc");

		cpTree = uifactory.addTreeMultiselect("cpprint", null, formLayout, ctm, this);
		cpTree.setMultiSelect(true);
		cpTree.setTreeModel(ctm);
		cpTree.selectAll();
		formLayout.add("cpprint", cpTree);

		selectAll = uifactory.addFormLink("checkall", "form.checkall", null, formLayout, Link.LINK);
		deselectAll = uifactory.addFormLink("uncheckall", "form.uncheckall", null, formLayout, Link.LINK);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("print-cancel", getTranslator());
		formLayout.add(buttonLayout);
		buttonLayout.setRootForm(mainForm);
		submit = uifactory.addFormSubmitButton("print-button", "print.node", buttonLayout);
		submit.setNewWindowAfterDispatchUrl(true);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	public List<String> getSelectedNodeIdentifiers() {
		final Set<String> selectedKeys = cpTree.getSelectedKeys();
		final List<String> orderedIdentifiers = new ArrayList<>();
		TreeVisitor visitor = new TreeVisitor(new Visitor() {
			@Override
			public void visit(INode node) {
				if(selectedKeys.contains(node.getIdent())) {
					orderedIdentifiers.add(node.getIdent());
				}
			}
		}, ctm.getRootNode(), false);

		visitor.visitAll();
		return orderedIdentifiers;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == selectAll) {
			cpTree.selectAll();
			submit.setEnabled(true);
		} else if (source == deselectAll) {
			cpTree.deselectAll();
			submit.setEnabled(false);
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}