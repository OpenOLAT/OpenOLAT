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
package org.olat.portfolio.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.portfolio.EPArtefactHandler;
import org.olat.portfolio.PortfolioModule;
import org.olat.portfolio.ui.filter.PortfolioFilterController;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * 
 * Description:<br>
 * allows to admin the ePortfolio-module
 * 
 * <P>
 * Initial Date:  21.07.2010 <br>
 * @author: srosse
 */
public class PortfolioAdminController extends FormBasicController  {

	private static String[] enabledKeys = new String[]{ "on" };
	private static String[] enabledPortfolioKeys = new String[]{ "on", "legacy"};

	private FormLayoutContainer wizardFlc;
	private MultipleSelectionElement portfoliosEnabled;
	private MultipleSelectionElement userCanCreatePortfolioEnabled;
	private final List<MultipleSelectionElement> handlersEnabled = new ArrayList<>();
	private MultipleSelectionElement copyrightStepCB, reflexionStepCB;

	@Autowired
	private PortfolioModule portfolioModule;
	@Autowired
	private PortfolioV2Module portfolioV2Module;
	
	public PortfolioAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "adminconfig");
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//module configuration
		FormLayoutContainer moduleFlc = FormLayoutContainer.createDefaultFormLayout("flc_module", getTranslator());
		formLayout.add(moduleFlc);
		
		String[] enabledPortfolioValues = new String[] { translate("enabled"), translate("portfolio.v1.module.enabled") };
		portfoliosEnabled = uifactory.addCheckboxesVertical("portfolio.module.enabled", moduleFlc, enabledPortfolioKeys, enabledPortfolioValues, 1);
		if(portfolioModule.isEnabled() || portfolioV2Module.isEnabled()) {
			portfoliosEnabled.select(enabledPortfolioKeys[0], true);
		}
		if(portfolioModule.isEnabled()) {
			portfoliosEnabled.select(enabledPortfolioKeys[1], true);
		}
		portfoliosEnabled.addActionListener(FormEvent.ONCHANGE);

		String[] enabledValues = new String[] { translate("enabled")};
		
		userCanCreatePortfolioEnabled = uifactory.addCheckboxesHorizontal("portfolio.user.can.create.binder", moduleFlc, enabledKeys, enabledValues);
		userCanCreatePortfolioEnabled.select(enabledKeys[0], portfolioV2Module.isLearnerCanCreateBinders());
		userCanCreatePortfolioEnabled.addActionListener(FormEvent.ONCHANGE);
		userCanCreatePortfolioEnabled.setVisible(portfolioV2Module.isEnabled());
		
		//handlers configuration
		FormLayoutContainer handlersFlc = FormLayoutContainer.createDefaultFormLayout("flc_handlers", getTranslator());
		formLayout.add(handlersFlc);

		List<EPArtefactHandler<?>> handlers = portfolioModule.getAllAvailableArtefactHandlers();
		for(EPArtefactHandler<?> handler:handlers) {
			Translator handlerTrans = handler.getHandlerTranslator(getTranslator());
			handlersFlc.setTranslator(handlerTrans);
			String handlerClass = PortfolioFilterController.HANDLER_PREFIX + handler.getClass().getSimpleName() + PortfolioFilterController.HANDLER_TITLE_SUFFIX;
			MultipleSelectionElement handlerEnabled = uifactory.addCheckboxesHorizontal(handlerClass, handlersFlc, enabledKeys, enabledValues);
			handlerEnabled.select(enabledKeys[0], handler.isEnabled());
			handlerEnabled.setUserObject(handler);
			handlerEnabled.addActionListener(FormEvent.ONCHANGE);
			handlersEnabled.add(handlerEnabled);
		}
		
		// configure steps in artefact collection wizard
		wizardFlc = FormLayoutContainer.createDefaultFormLayout("flc_wizard", getTranslator());
		formLayout.add(wizardFlc);	
		copyrightStepCB = uifactory.addCheckboxesHorizontal("wizard.step.copyright", wizardFlc, enabledKeys, enabledValues);
		copyrightStepCB.select(enabledKeys[0], portfolioModule.isCopyrightStepEnabled());
		copyrightStepCB.addActionListener(FormEvent.ONCHANGE);
		
		reflexionStepCB = uifactory.addCheckboxesHorizontal("wizard.step.reflexion", wizardFlc, enabledKeys, enabledValues);
		reflexionStepCB.select(enabledKeys[0], portfolioModule.isReflexionStepEnabled());
		reflexionStepCB.addActionListener(FormEvent.ONCHANGE);
		wizardFlc.setVisible(portfoliosEnabled.isSelected(1));
	}
	
	@Override
	protected void doDispose() {
		//auto-disposed
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
		if(source == portfoliosEnabled) {
			boolean enabled = portfoliosEnabled.isSelected(0);
			if(enabled) {
				portfolioModule.setEnabled(portfoliosEnabled.isSelected(1));
				portfolioV2Module.setEnabled(true);
			} else {
				portfolioModule.setEnabled(false);
				portfolioV2Module.setEnabled(false);
			}
			// update collaboration tools list

			wizardFlc.setVisible(portfoliosEnabled.isSelected(1));
			userCanCreatePortfolioEnabled.setVisible(portfolioV2Module.isEnabled());
			CollaborationToolsFactory.getInstance().initAvailableTools();
			showInfo("save.admin.settings");
		} else if(handlersEnabled.contains(source)) {
			EPArtefactHandler<?> handler = (EPArtefactHandler<?>)source.getUserObject();
			boolean enabled = ((MultipleSelectionElement)source).isSelected(0);
			portfolioModule.setEnableArtefactHandler(handler, enabled);
		} else if(source == reflexionStepCB){
			boolean enabled = reflexionStepCB.isSelected(0);
			portfolioModule.setReflexionStepEnabled(enabled);
		} else if(source == copyrightStepCB){
			boolean enabled = copyrightStepCB.isSelected(0);
			portfolioModule.setCopyrightStepEnabled(enabled);
		} else if(userCanCreatePortfolioEnabled == source) {
			boolean enabled = userCanCreatePortfolioEnabled.isSelected(0);
			portfolioV2Module.setLearnerCanCreateBinders(enabled);
		}
	}
}
