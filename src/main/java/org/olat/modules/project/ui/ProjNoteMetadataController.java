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
package org.olat.modules.project.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Formatter;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjNote;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjNoteMetadataController extends FormBasicController {
	
	private final ProjArtefact artefact;
	private final Formatter formatter;
	
	@Autowired
	private UserManager userManager;

	public ProjNoteMetadataController(UserRequest ureq, WindowControl wControl, Form mainForm, ProjNote note) {
		super(ureq, wControl, LAYOUT_VERTICAL, null, mainForm);
		this.artefact = note.getArtefact();
		formatter = Formatter.getInstance(getLocale());
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormStyle("o_proj_metadata");
		
		String createdDateBy = translate("date.by",
				formatter.formatDate(artefact.getCreationDate()),
				userManager.getUserDisplayName(artefact.getCreator().getKey()));
		uifactory.addStaticTextElement("created", createdDateBy, formLayout);
		
		String modifiedDateBy = translate("date.by",
				formatter.formatDate(artefact.getContentModifiedDate()),
				userManager.getUserDisplayName(artefact.getContentModifiedBy()));
		uifactory.addStaticTextElement("last.modified", modifiedDateBy, formLayout);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}
