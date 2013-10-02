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

import org.olat.core.CoreSpringFactory;
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
import org.olat.portfolio.EPArtefactHandler;
import org.olat.portfolio.PortfolioModule;
import org.olat.portfolio.ui.filter.PortfolioFilterController;
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

	private PortfolioModule portfolioModule;
	
	private MultipleSelectionElement portfolioEnabled;
	private final List<MultipleSelectionElement> handlersEnabled = new ArrayList<MultipleSelectionElement>();
	
	private static String[] enabledKeys = new String[]{"on"};
	private String[] enabledValues;

	private MultipleSelectionElement copyrightStepCB;

	private MultipleSelectionElement reflexionStepCB;
	
	public PortfolioAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "adminconfig");
		
		portfolioModule = (PortfolioModule)CoreSpringFactory.getBean("portfolioModule");
		enabledValues = new String[] {
			translate("enabled")	
		};
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutContainer = (FormLayoutContainer)formLayout;
			
			//module configuration
			FormLayoutContainer moduleFlc = FormLayoutContainer.createDefaultFormLayout("flc_module", getTranslator());
			layoutContainer.add(moduleFlc);
		
			portfolioEnabled = uifactory.addCheckboxesHorizontal("portfolio.module.enabled", moduleFlc, enabledKeys, enabledValues, null);
			portfolioEnabled.select(enabledKeys[0], portfolioModule.isEnabled());
			portfolioEnabled.addActionListener(listener, FormEvent.ONCHANGE);
			
			//handlers configuration
			FormLayoutContainer handlersFlc = FormLayoutContainer.createDefaultFormLayout("flc_handlers", getTranslator());
			layoutContainer.add(handlersFlc);

			List<EPArtefactHandler<?>> handlers = portfolioModule.getAllAvailableArtefactHandlers();
			for(EPArtefactHandler<?> handler:handlers) {
				Translator handlerTrans = handler.getHandlerTranslator(getTranslator());
				handlersFlc.setTranslator(handlerTrans);
				String handlerClass = PortfolioFilterController.HANDLER_PREFIX + handler.getClass().getSimpleName() + PortfolioFilterController.HANDLER_TITLE_SUFFIX;
				MultipleSelectionElement handlerEnabled = uifactory.addCheckboxesHorizontal(handlerClass, handlersFlc, enabledKeys, enabledValues, null);
				handlerEnabled.select(enabledKeys[0], handler.isEnabled());
				handlerEnabled.setUserObject(handler);
				handlerEnabled.addActionListener(listener, FormEvent.ONCHANGE);
				handlersEnabled.add(handlerEnabled);
			}
			
			// configure steps in artefact collection wizard
			FormLayoutContainer wizardFlc = FormLayoutContainer.createDefaultFormLayout("flc_wizard", getTranslator());
			layoutContainer.add(wizardFlc);	
			copyrightStepCB = uifactory.addCheckboxesHorizontal("wizard.step.copyright", wizardFlc, enabledKeys, enabledValues, null);
			copyrightStepCB.select(enabledKeys[0], portfolioModule.isCopyrightStepEnabled());
			copyrightStepCB.addActionListener(listener, FormEvent.ONCHANGE);
			
			reflexionStepCB = uifactory.addCheckboxesHorizontal("wizard.step.reflexion", wizardFlc, enabledKeys, enabledValues, null);
			reflexionStepCB.select(enabledKeys[0], portfolioModule.isReflexionStepEnabled());
			reflexionStepCB.addActionListener(listener, FormEvent.ONCHANGE);
			
			
		}
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
		if(source == portfolioEnabled) {
			boolean enabled = portfolioEnabled.isSelected(0);
			portfolioModule.setEnabled(enabled);
			showWarning("portfolio.module.change.warning");
		} else if(handlersEnabled.contains(source)) {
			EPArtefactHandler<?> handler = (EPArtefactHandler<?>)source.getUserObject();
			boolean enabled = ((MultipleSelectionElement)source).isSelected(0);
			portfolioModule.setEnableArtefactHandler(handler, enabled);
		} else if(source == reflexionStepCB){
			boolean enabled = ((MultipleSelectionElement)source).isSelected(0);
			portfolioModule.setReflexionStepEnabled(enabled);
		} else if(source == copyrightStepCB){
			boolean enabled = ((MultipleSelectionElement)source).isSelected(0);
			portfolioModule.setCopyrightStepEnabled(enabled);
		}
	}
}
