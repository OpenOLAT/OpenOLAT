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
package org.olat.modules.selectus.ui.app_wizard;

import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.BasicStep;
import org.olat.core.gui.control.generic.wizard.PrevNextFinishConfig;
import org.olat.core.gui.control.generic.wizard.StepFormController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.position.TabConfiguration;
import org.olat.modules.selectus.model.position.TabsConfiguration.Tab;

/**
 * 
 * Initial date: 13 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CustomStep extends BasicStep {
	
	private final Tab tab;
	private final Position preselectedPosition;
	private final RecruitingModule recruitingModule;
	
	public CustomStep(UserRequest ureq, Position preselectedPosition) {
		this(ureq, preselectedPosition, null);
	}

	public CustomStep(UserRequest ureq, Position preselectedPosition, Tab tab) {
		super(ureq);
		this.preselectedPosition = preselectedPosition;
		recruitingModule = CoreSpringFactory.getImpl(RecruitingModule.class);

		List<Tab> customTabs = preselectedPosition == null ? null : preselectedPosition.getCustomEnabledTabsList();
		if(recruitingModule.isPositionCustomStepsEnabled() && customTabs != null && !customTabs.isEmpty()) {
			if(tab == null) {
				this.tab = customTabs.get(0);
			} else {
				this.tab = tab;
			}
			TabConfiguration tabConfiguration = preselectedPosition.getTabConfiguration(this.tab);
			String title = tabConfiguration.getTitle(getLocale());
			if(title == null) {
				title = tabConfiguration.getTitle();
			}
			if(title == null) {
				title = tabConfiguration.getTitleDe();
			}
			setI18nTitleAndDescr("wizard." + this.tab +".title", "wizard.custom.description", new String[] { title });
		} else {
			this.tab = null;
			setI18nTitleAndDescr("wizard.custom.title", "wizard.custom.description", new String[] { "-" });
		}

		if(isEnabled() && customTabs != null && !customTabs.isEmpty()) {
			int nextIndex = customTabs.indexOf(this.tab) + 1;
			if(nextIndex >= 0 && nextIndex < customTabs.size()) {
				Tab nextTab = customTabs.get(nextIndex);
				setNextStep(new CustomStep(ureq, preselectedPosition, nextTab));
			} else {
				setNextNotCustomStep(ureq);
			}
		} else {
			setNextNotCustomStep(ureq);
		}
	}
	
	private void setNextNotCustomStep(UserRequest ureq) {
		DocumentsStep nextStep = new DocumentsStep(ureq, preselectedPosition);
		if(nextStep.isEnabled()) {
			setNextStep(nextStep);
		} else {
			setNextStep(nextStep.nextStep());
		}
	}
	
	protected boolean isEnabled() {
		return tab != null;
	}

	@Override
	public PrevNextFinishConfig getInitialPrevNextFinishConfig() {
		return new PrevNextFinishConfig(true, true, false);
	}

	@Override
	public StepFormController getStepController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext, Form form) {
		form.setMultipartEnabled(true);
		return new CustomStepController(ureq, wControl, tab, preselectedPosition, runContext, form);
	}
}
