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
package org.olat.ims.qti21.ui.editor;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;

import uk.ac.ed.ph.jqtiplus.node.test.AbstractPart;
import uk.ac.ed.ph.jqtiplus.node.test.ItemSessionControl;

/**
 * 
 * Initial date: 03.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class ItemSessionControlController extends FormBasicController {

	private static final String[] yesnoKeys = new String[] { "y", "n" };

	private SingleSelection allowCommentEl, showSolutionEl;
	
	private final boolean restrictedEdit;
	private final AbstractPart part;
	
	public ItemSessionControlController(UserRequest ureq, WindowControl wControl,
			AbstractPart part, boolean restrictedEdit) {
		super(ureq, wControl, Util.createPackageTranslator(AssessmentTestDisplayController.class, ureq.getLocale()));
		this.part = part;
		this.restrictedEdit = restrictedEdit;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		ItemSessionControl itemSessionControl = part.getItemSessionControl();//can be null
		
		String[] yesnoValues = new String[] { translate("yes"), translate("no") };
		allowCommentEl = uifactory.addRadiosHorizontal("item.session.control.allow.comment", formLayout, yesnoKeys, yesnoValues);
		allowCommentEl.addActionListener(FormEvent.ONCHANGE);
		allowCommentEl.setEnabled(!restrictedEdit);
		if(itemSessionControl != null && itemSessionControl.getAllowComment() != null && itemSessionControl.getAllowComment().booleanValue()) {
			allowCommentEl.select(yesnoKeys[0], true);
		} else {
			allowCommentEl.select(yesnoKeys[1], false);
		}
	
		showSolutionEl = uifactory.addRadiosHorizontal("item.session.control.show.solution", formLayout, yesnoKeys, yesnoValues);
		showSolutionEl.addActionListener(FormEvent.ONCHANGE);
		showSolutionEl.setEnabled(!restrictedEdit);
		if(itemSessionControl != null && itemSessionControl.getShowSolution() != null && itemSessionControl.getShowSolution().booleanValue()) {
			showSolutionEl.select(yesnoKeys[0], true);
		} else {
			showSolutionEl.select(yesnoKeys[1], false);
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		allowCommentEl.clearError();
		if(!allowCommentEl.isOneSelected()) {
			allowCommentEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		showSolutionEl.clearError();
		if(!showSolutionEl.isOneSelected()) {
			showSolutionEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}

		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		ItemSessionControl itemSessionControl = part.getItemSessionControl();//can be null
		
		if(allowCommentEl.isOneSelected() && allowCommentEl.isSelected(0)) {
			checkNotNull(itemSessionControl).setAllowComment(Boolean.TRUE);
		} else if(itemSessionControl != null) {
			itemSessionControl.setAllowComment(Boolean.FALSE);
		}
		
		if(showSolutionEl.isOneSelected() && showSolutionEl.isSelected(0)) {
			checkNotNull(itemSessionControl).setShowSolution(Boolean.TRUE);
		} else if(itemSessionControl != null) {
			itemSessionControl.setShowSolution(Boolean.FALSE);
		}
	}
	
	private ItemSessionControl checkNotNull(ItemSessionControl itemSessionControl) {
		if(itemSessionControl == null) {
			itemSessionControl = new ItemSessionControl(part);
			part.setItemSessionControl(itemSessionControl);
		}
		return itemSessionControl;
	}
	
	

}
