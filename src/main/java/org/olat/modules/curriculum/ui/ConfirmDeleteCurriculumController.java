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
package org.olat.modules.curriculum.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmDeleteCurriculumController extends ConfirmationController {
	
	private final Curriculum curriculum;
	
	@Autowired
	private CurriculumService curriculumService;
	
	public ConfirmDeleteCurriculumController(UserRequest ureq, WindowControl wControl,
			String message, String confirmation, String confirmButton, Curriculum curriculum) {
		super(ureq, wControl, message, confirmation, confirmButton, ButtonType.danger, false);
		setTranslator(Util.createPackageTranslator(ConfirmationController.class, getLocale(), getTranslator()));
		this.curriculum = curriculum;
		initForm(ureq);
	}

	@Override
	protected void doAction(UserRequest ureq) {
		curriculumService.deleteSoftlyCurriculum(curriculum);
		super.doAction(ureq);
	}
}
