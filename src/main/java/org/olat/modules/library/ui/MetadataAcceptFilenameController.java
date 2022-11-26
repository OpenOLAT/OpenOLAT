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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;

/**
 * Description:<br>
 * Controller for the MetadataAcceptStepController to change the filename
 * <P>
 * Initial Date:  11 nov. 2009 <br>
 *
 * @author srosse
 */
public class MetadataAcceptFilenameController extends FormBasicController {
	private static final int FILENAME_MAX_LENGTH = 64;
	
	private String initialFilename;
	private final VFSLeaf item;
	
	private TextElement filename;
	private TextElement newFilename;
	
	public MetadataAcceptFilenameController(UserRequest ureq, WindowControl control, Form parentForm, VFSLeaf item) {
		super(ureq, control, FormBasicController.LAYOUT_DEFAULT, null, parentForm);
		this.item = item;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("mf.filename.title");
		
		initialFilename = item.getName();
		filename = uifactory.addTextElement("filename", "mf.filename", -1, initialFilename, formLayout);
		filename.setElementCssClass("o_sel_filename");
		filename.setEnabled(false);
		
		newFilename = uifactory.addTextElement("newFilename", "mf.newFilename", -1, initialFilename, formLayout);
		newFilename.setElementCssClass("o_sel_new_filename");
		if(!validateFilename(initialFilename)) {
			newFilename.setErrorKey("mf.newFilename.error", null);
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		String name = getNewFilename();
		if(!validateFilename(name)) {
			newFilename.setErrorKey("mf.newFilename.error", null);
			return false;
		}
		return true;
	}
	
	/**
	 * more hard as FileUtils.validateFilename(name)
	 * @param filename
	 * @return
	 */
	private static boolean validateFilename(String filename) {
		if (!StringHelper.containsNonWhitespace(filename)) return false;
		if(FILENAME_MAX_LENGTH < filename.length()) return false;
		return filename.matches("[0-9a-zA-Z._-]+");
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//nothing to do
	}
	
	/**
	 * Return the new filename if there is one
	 * @return
	 */
	public String getNewFilename() {
		return newFilename.getValue();
	}
	
	/**
	 * Get the form item representing this form
	 * 
	 * @return
	 */
	public FormItem getFormItem() {
		return flc;
	}
}