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
package org.olat.modules.portfolio.handler;

import static org.olat.core.commons.services.doceditor.DocEditor.Mode.EDIT;

import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.doceditor.DocTemplates;
import org.olat.core.commons.services.doceditor.DocTemplates.Builder;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.modules.ceditor.PageElementAddController;
import org.olat.modules.portfolio.ui.media.CreateFileMediaController;

/**
 * 
 * Initial date: 3 Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CreateFileHandler extends FileHandler {
	
	public static DocTemplates getEditableTemplates(Identity identity, Roles roles, Locale locale) {
		DocEditorService docEditorService = CoreSpringFactory.getImpl(DocEditorService.class);
		Builder builder = DocTemplates.builder(locale);
		if (docEditorService.hasEditor(identity, roles, "docx", EDIT, true, false)) {
			builder.addDocx();
		}
		if (docEditorService.hasEditor(identity, roles, "xlsx", EDIT, true, false)) {
			builder.addXlsx();
		}
		if (docEditorService.hasEditor(identity, roles, "pptx", EDIT, true, false)) {
			builder.addPptx();
		}
		return builder.build();
	}
	
	public CreateFileHandler() {
		super(true);
	}
	
	@Override
	public String getType() {
		return "bc.create";
	}
	
	@Override
	public PageElementAddController getAddPageElementController(UserRequest ureq, WindowControl wControl) {
		return new CreateFileMediaController(ureq, wControl,
				getEditableTemplates(ureq.getIdentity(), ureq.getUserSession().getRoles(), ureq.getLocale()));
	}

}
