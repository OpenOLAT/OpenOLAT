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
package org.olat.modules.quality.generator.ui;

import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.stack.ButtonGroupComponent;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.quality.QualitySecurityCallback;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GeneratorController extends BasicController implements TooledController, Activateable2 {

	private Link configurationLink;
	private Link whiteListLink;
	private final ButtonGroupComponent segmentButtonsCmp;
	private final TooledStackedPanel stackPanel;
	private final StackedPanel mainPanel;
	
	private GeneratorEditController configCtrl;
	private AbstractGeneratorEditController whiteListCtrl;
	private CloseableModalController cmc;
	private GeneratorEnableConfirmationController enableConfirmationCtrl;
	private GeneratorDisableConfirmationController disableConfirmationCtrl;
	
	private final QualitySecurityCallback secCallback;
	private QualityGenerator generator;
	
	@Autowired
	private QualityGeneratorService generatorService;

	public GeneratorController(UserRequest ureq, WindowControl wControl, QualitySecurityCallback secCallback,
			TooledStackedPanel stackPanel, QualityGenerator generator) {
		super(ureq, wControl);
		this.secCallback = secCallback;
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);
		this.generator = generator;
		
		segmentButtonsCmp = new ButtonGroupComponent("segments");
		if (secCallback.canViewGenerators()) {
			configurationLink = LinkFactory.createLink("generator.configuration", getTranslator(), this);
			segmentButtonsCmp.addButton(configurationLink, false);
			if (generatorService.hasWhiteListController(generator)) {
				whiteListLink = LinkFactory.createLink("generator.white.list", getTranslator(), this);
				segmentButtonsCmp.addButton(whiteListLink, false);
			}
		}
		
		mainPanel = putInitialPanel(new SimpleStackedPanel("dataCollectionSegments"));
		mainPanel.setContent(new Panel("empty"));
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		doOpenConfiguration(ureq);
	}

	@Override
	public void initTools() {
		stackPanel.addTool(segmentButtonsCmp, true);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (configurationLink == source) {
			doOpenConfiguration(ureq);
		} else if(whiteListLink == source) {
			doOpenWhiteList(ureq);
		} else if (stackPanel == source && stackPanel.getLastController() == this && event instanceof PopEvent) {
			PopEvent popEvent = (PopEvent) event;
			if (popEvent.isClose()) {
				stackPanel.popController(this);
			} else {
				doOpenConfiguration(ureq);
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (event instanceof GeneratorEvent) {
			GeneratorEvent gEvent = (GeneratorEvent) event;
			GeneratorEvent.Action action = gEvent.getAction();
			generator = gEvent.getGenerator();
			if (GeneratorEvent.Action.CHANGED.equals(action)) {
				stackPanel.changeDisplayname(generator.getTitle(), null, this);
			} else if (GeneratorEvent.Action.ENABLE.equals(action)) {
				doConfirmEnableGenerator(ureq);
			} else if (GeneratorEvent.Action.DISABLE.equals(action)) {
				doConfirmDisableGenerator(ureq);
			} else {
				fireEvent(ureq, event);
			}
		} else if (source == enableConfirmationCtrl) {
			if (Event.DONE_EVENT.equals(event)) {
				Date fromDate = enableConfirmationCtrl.getFromDate();
				doEnableGenerator(fromDate);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == disableConfirmationCtrl) {
			if (Event.DONE_EVENT.equals(event)) {
				doDisabledGenerator();
			}
			cmc.deactivate();
			cleanUp();
		}  else if (source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(disableConfirmationCtrl);
		removeAsListenerAndDispose(enableConfirmationCtrl);
		removeAsListenerAndDispose(cmc);
		disableConfirmationCtrl = null;
		enableConfirmationCtrl = null;
		cmc = null;
	}
	
	private void doOpenConfiguration(UserRequest ureq) {
		doOpenConfiguration(ureq, false);
	}

	private void doOpenConfiguration(UserRequest ureq, boolean validate) {
		configCtrl = new GeneratorEditController(ureq, getWindowControl(), secCallback, stackPanel,
				generator, validate);
		listenTo(configCtrl);
		stackPanel.popUpToController(this);
		stackPanel.pushController(translate("generator.configuration"), configCtrl);
		segmentButtonsCmp.setSelectedButton(configurationLink);
	}
	
	private void doOpenWhiteList(UserRequest ureq) {
		stackPanel.popUpToController(this);
		whiteListCtrl = generatorService.getWhiteListController(ureq, getWindowControl(), secCallback, stackPanel,
				generator);
		listenTo(whiteListCtrl);
		stackPanel.popUpToController(this);
		stackPanel.pushController(translate("generator.white.list"), whiteListCtrl);
		segmentButtonsCmp.setSelectedButton(whiteListLink);
	}

	private void doConfirmEnableGenerator(UserRequest ureq) {
		if (configCtrl.validateBeforeActivation(ureq)) {
			enableConfirmationCtrl = new GeneratorEnableConfirmationController(ureq, getWindowControl(), generator);
			listenTo(enableConfirmationCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					enableConfirmationCtrl.getInitialComponent(), true, translate("generator.enable.confirm.title"));
			cmc.activate();
			listenTo(cmc);
		} else {
			doOpenConfiguration(ureq, true);
		}
	}

	private void doEnableGenerator(Date fromDate) {
		generator = generatorService.loadGenerator(generator);
		generator.setEnabled(true);
		generator.setLastRun(fromDate);
		generator = generatorService.updateGenerator(generator);
		generator = generatorService.loadGenerator(generator);
		updateUI();	
	}
	
	private void doConfirmDisableGenerator(UserRequest ureq) {
		disableConfirmationCtrl = new GeneratorDisableConfirmationController(ureq, getWindowControl(), generator);
		listenTo(disableConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				disableConfirmationCtrl.getInitialComponent(), true, translate("generator.disable.confirm.title"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doDisabledGenerator() {
		generator = generatorService.loadGenerator(generator);
		generator.setEnabled(false);
		generator = generatorService.updateGenerator(generator);
		generator = generatorService.loadGenerator(generator);
		updateUI();
	}
	
	private void updateUI() {
		generator = generatorService.loadGenerator(generator);
		if (configCtrl != null) {
			configCtrl.setGenerator(generator);
			configCtrl.initTools();
		}
		if (whiteListCtrl != null) {
			whiteListCtrl.setGenerator(generator);
			whiteListCtrl.initTools();
		}
	}

	@Override
	protected void doDispose() {
		if (stackPanel != null) {
			stackPanel.removeListener(this);
		}
	}

}
