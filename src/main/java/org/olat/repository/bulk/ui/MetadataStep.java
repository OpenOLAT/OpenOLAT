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
package org.olat.repository.bulk.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryService;
import org.olat.repository.bulk.model.SettingsSteps;
import org.olat.repository.bulk.model.SettingsSteps.Step;

/**
 * 
 * Initial date: 18 Oct 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class MetadataStep extends BasicStep {

	private final SettingsSteps steps;

	public MetadataStep(UserRequest ureq, SettingsSteps step) {
		super(ureq);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.steps = step;
		
		setI18nTitleAndDescr("settings.bulk.metadata.title", null);
		setNextStep(RepositoryBulkUIFactory.getNextSettingsStep(ureq, steps, Step.metadata));
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl windowControl,
			StepsRunContext stepsRunContext, Form form) {
		return new MetadataController(ureq, windowControl, form, stepsRunContext);
	}

}
