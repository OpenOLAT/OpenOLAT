/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.commons.services.folder.ui;

import java.util.List;

import org.olat.core.commons.services.folder.ui.FolderController.CopyMoveParams;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSStatus;

/**
 * 
 * Initial date: Nov 28, 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OverwriteConfirmationController extends FormBasicController {

	private FormLink overwriteButton;
	private FormLink keepButton;

	private final CopyMoveParams params;
	private final List<VFSItem> itemsWithSameNameExists;
	private Boolean overwrite;

	public OverwriteConfirmationController(UserRequest ureq, WindowControl wControl, CopyMoveParams params,
			List<VFSItem> itemsWithSameNameExists) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.params = params;
		this.itemsWithSameNameExists = itemsWithSameNameExists;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (VFSStatus.YES == params.getTargetContainer().canVersion()) {
			if (itemsWithSameNameExists.size() == 1) {
				setFormInfo("overwrite.overwrite.single", new String[] {
						itemsWithSameNameExists.get(0).getName()});
			} else {
				setFormInfo("overwrite.overwrite.multi", new String[] { 
						String.valueOf(itemsWithSameNameExists.size()),
						String.valueOf(params.getItemsToCopy().size())});
			}
		} else {
			if (itemsWithSameNameExists.size() == 1) {
				setFormInfo("overwrite.replace.single", new String[] {
						itemsWithSameNameExists.get(0).getName()});
			} else {
				setFormInfo("overwrite.replace.multi", new String[] { 
						String.valueOf(itemsWithSameNameExists.size()),
						String.valueOf(params.getItemsToCopy().size())});
			}
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setElementCssClass("o_button_group_right o_block_top");
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		
		String i18nOverwrite = VFSStatus.YES == params.getTargetContainer().canVersion()
				? "overwrite.overwrite"
				: "overwrite.replace";
		overwriteButton = uifactory.addFormLink(i18nOverwrite, buttonsCont, Link.BUTTON);
		
		String i18nKeep = itemsWithSameNameExists.size() == 1
				? "overwrite.keep.single"
				: "overwrite.keep.multi";
		keepButton = uifactory.addFormLink(i18nKeep, buttonsCont, Link.BUTTON);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == overwriteButton) {
			overwrite = Boolean.TRUE;
			fireEvent(ureq, Event.DONE_EVENT);
		} else if (source == keepButton) {
			overwrite = Boolean.FALSE;
			fireEvent(ureq, Event.DONE_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	public Boolean getOverwrite() {
		return overwrite;
	}

	public CopyMoveParams getCopyMoveParams() {
		return params;
	}

}
