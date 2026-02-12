/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui.importwizard;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Initial date: 11 févr. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ValidationResultListController extends FormBasicController {
	
	private final List<CurriculumImportedValue> values;
	
	public ValidationResultListController(UserRequest ureq, WindowControl wControl, List<CurriculumImportedValue> values) {
		super(ureq, wControl, "validation_results");
		this.values = values;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_validation_result");
		
		List<CurriculumImportedValue> errors = values.stream()
				.filter(v -> v.isError())
				.toList();
		flc.contextPut("errors", errors);
		
		List<CurriculumImportedValue> warnings = values.stream()
				.filter(v -> v.isWarning())
				.toList();
		flc.contextPut("warnings", warnings);
		
		List<CurriculumImportedValue> changes = values.stream()
				.filter(v -> v.isChanged())
				.toList();
		flc.contextPut("changes", changes);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
