/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
* <p>
*/
package org.olat.portfolio.ui.structel.edit;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.spacesaver.ToggleBoxController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.portfolio.EPArtefactHandler;
import org.olat.portfolio.PortfolioModule;
import org.olat.portfolio.model.restriction.CollectRestriction;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.ui.filter.PortfolioFilterController;

/**
 * 
 * Description:<br>
 * Small controller with show the error message for the collect restriction.
 * 
 * <P>
 * Initial Date:  13 oct. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class EPCollectRestrictionResultController extends BasicController {
	
	private final VelocityContainer vc;
	private final VelocityContainer mainVc;
	private ToggleBoxController errorBoxController;
	private final PortfolioStructure structureEl;
	
	public EPCollectRestrictionResultController(UserRequest ureq, WindowControl wControl, PortfolioStructure structureEl) {
		super(ureq, wControl);
		
		this.structureEl = structureEl;

		vc = createVelocityContainer("restrictions_msg");
		mainVc = createVelocityContainer("restrictions_msg_wrapper");
		errorBoxController = new ToggleBoxController(ureq, getWindowControl(), "ep-restrictions-" + structureEl.getKey(), "  ", " ", vc);
		listenTo(errorBoxController);
		mainVc.put("description", errorBoxController.getInitialComponent());
		putInitialPanel(mainVc);
	}

	public void setMessage(UserRequest ureq, List<CollectRestriction> restrictions, boolean passed) {
		List<String> errors = new ArrayList<String>();
		for(CollectRestriction restriction:restrictions) {
			String error = getMessage(restriction, getTranslator(), null);
			errors.add(error);
		}
		
		Boolean passedObj = new Boolean(passed);
		Object currentStatus = vc.getContext().get("restrictionsPassed");
		if(Boolean.TRUE.equals(currentStatus) && Boolean.FALSE.equals(passedObj)) {
			removeAsListenerAndDispose(errorBoxController);
			errorBoxController = new ToggleBoxController(ureq, getWindowControl(), "ep-restrictions-" + structureEl.getKey(), "  ", " ", vc);
			listenTo(errorBoxController);
		}
		vc.contextPut("messages", errors);
		vc.contextPut("restrictionsPassed", passedObj);
		mainVc.contextPut("restrictionsPassed", passedObj);
		mainVc.setDirty(true);
	}
	
	public static String getMessage(CollectRestriction restriction, Translator translator, Locale locale) {
		if(translator == null) {
			translator = Util.createPackageTranslator(EPCollectRestrictionResultController.class, locale);
		}
		String[] args = getMessageArgs(restriction, translator);
		return translator.translate("restriction.error", args);
	}
	
	public static String[] getMessageArgs(CollectRestriction restriction, Translator translator) {
		String[] args = new String[3];
		args[0] = translator.translate("restriction." + restriction.getRestriction());
		PortfolioModule portfolioModule = (PortfolioModule) CoreSpringFactory.getBean("portfolioModule");
		EPArtefactHandler<?> handler = portfolioModule.getArtefactHandler(restriction.getArtefactType());
		if (handler!=null) {
			String handlerClass = PortfolioFilterController.HANDLER_PREFIX + handler.getClass().getSimpleName() + PortfolioFilterController.HANDLER_TITLE_SUFFIX;
			args[1] = handler.getHandlerTranslator(translator).translate(handlerClass);
		} else {
			args[1] = translator.translate("restriction.handler.unknown");
		}
		args[2] = Integer.toString(restriction.getAmount());
		return args;
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	@SuppressWarnings("unused")
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

}
