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
package org.olat.core.commons.services.doceditor.collabora.ui;

import java.util.Locale;

import org.olat.core.commons.services.doceditor.DocEditorAdminSegment;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 17 Sep 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class CollaboraAdminSegment implements DocEditorAdminSegment {

	@Override
	public String getLinkName(Locale locale) {
		Translator translator = Util.createPackageTranslator(CollaboraAdminController.class, locale);
		return translator.translate("editor.display.name");
	}

	@Override
	public String getBusinessPathType() {
		return "Collabora";
	}

	@Override
	public Controller createController(UserRequest ureq, WindowControl wControl) {
		return new CollaboraAdminController(ureq, wControl);
	}

}
