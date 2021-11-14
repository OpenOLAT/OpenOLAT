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
package org.olat.core.commons.services.vfs.ui.version;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Description:<br>
 * A controller which collect a comment for a new revision of a versioned file or
 * free the lock for the file .<br>
 * <ul>
 * <li>DONE_EVENT</li>
 * </ul>
 * 
 * <P>
 * Initial Date: 15 sept. 2009 <br>
 * 
 * @author srosse
 */
public class VersionCommentController extends FormBasicController {

	private static final String KEY_LOCK = "lock";
	private static final String KEY_UNLOCK = "unlock";
	
	private boolean lock;
	private boolean comment;
	private SingleSelection lockSelection;
	private TextElement commentElement;

	public VersionCommentController(UserRequest ureq, WindowControl wControl, boolean lock, boolean comment) {
		super(ureq, wControl);
		this.lock = lock;
		this.comment = comment;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(comment && lock) {
			setFormTitle("version.comment.alltitle");
		} else if (comment) {
			setFormTitle("version.comment.title");
		} else if (lock) {
			setFormTitle("meta.comment.title");
		}

		if(comment) {
			setFormDescription("version.comment.description");
			commentElement = uifactory.addTextAreaElement("comment", "version.comment", -1, 3, 1, true, false, null, formLayout);
		}
		
		if(lock) {
			String[] values = new String[] {
					getTranslator().translate("meta.unlock"),
					getTranslator().translate("meta.retainlock")
			};
			lockSelection = uifactory.addDropdownSingleselect("lock", formLayout, new String[]{KEY_UNLOCK, KEY_LOCK}, values, null);
			lockSelection.setLabel("meta.locked", null);
		}

		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("save", buttonLayout);
	}

	public String getComment() {
		if(comment) {
			return commentElement.getValue();
		}
		return null;
	}
	
	public boolean keepLocked() {
		if(lockSelection == null) return false;
		return KEY_LOCK.equals(lockSelection.getSelectedKey());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
