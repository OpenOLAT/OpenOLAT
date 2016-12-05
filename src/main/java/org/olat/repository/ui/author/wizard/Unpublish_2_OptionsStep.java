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
package org.olat.repository.ui.author.wizard;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;

/**
 * 
 * Options to clean-up groups and the catalog.
 * 
 * Initial date: 29.08.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Unpublish_2_OptionsStep extends BasicStep {
	
	
	public Unpublish_2_OptionsStep(UserRequest ureq, RepositoryEntry entry ) {
		super(ureq);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		setNextStep(new Unpublish_3_SendMailStep(ureq, entry));
		setI18nTitleAndDescr("close.ressource.step2", "close.ressource.step2");
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		return new CloseRessourceOptionForm(ureq, wControl, form, runContext);
	}
	
	public class CloseRessourceOptionForm extends StepFormBasicController {

		private MultipleSelectionElement checkboxClean;

		public CloseRessourceOptionForm(UserRequest ureq, WindowControl wControl,
				Form rootForm, StepsRunContext runContext) {
			super(ureq, wControl, rootForm, runContext, LAYOUT_DEFAULT, null);
			setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
			initForm(ureq);
		}
		
		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			String[] keys = new String[] {"form.clean.catalog", "form.clean.groups"};
			String[] values = new String[] {translate("form.clean.catalog"), translate("form.clean.groups")};
			checkboxClean = uifactory.addCheckboxesVertical("form.clean.catalog", null, formLayout, keys, values, 1);
		}
	
		@Override
		protected void doDispose() {
		  // nothing to do
		}
	
		@Override
		protected void formOK(UserRequest ureq) {
			boolean cleanCatalog = checkboxClean.isSelected(0);
			boolean cleanGroups = checkboxClean.isSelected(1);
			addToRunContext("cleanCatalog", new Boolean(cleanCatalog));
			addToRunContext("cleanGroups", new Boolean(cleanGroups));
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}
	}
}