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

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.dropdown.Dropdown;
import org.olat.core.gui.components.dropdown.DropdownOrientation;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.gui.components.stack.ButtonGroupComponent;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Organisation;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.Tracing;
import org.olat.modules.quality.generator.QualityGenerator;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.olat.modules.quality.ui.GeneratorReportAccessController;
import org.olat.modules.quality.ui.security.GeneratorSecurityCallback;
import org.olat.modules.quality.ui.security.QualitySecurityCallbackFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class GeneratorController extends BasicController implements TooledController, Activateable2 {

	private static final Logger log = Tracing.createLoggerFor(GeneratorController.class);

	private Dropdown enableDropdown;
	private Link enableLink;
	private Link disableLink;
	private Link deleteLink;
	private Link configurationLink;
	private Link reportAccessLink;
	private Link whiteListLink;
	private Link blackListLink;
	private final ButtonGroupComponent segmentButtonsCmp;
	private final TooledStackedPanel stackPanel;
	private final StackedPanel mainPanel;
	
	private GeneratorEditController configCtrl;
	private GeneratorReportAccessController reportAccessCtrl;
	private Controller whiteListCtrl;
	private Controller blackListCtrl;
	private CloseableModalController cmc;
	private GeneratorEnableConfirmationController enableConfirmationCtrl;
	private GeneratorDisableConfirmationController disableConfirmationCtrl;
	
	private GeneratorSecurityCallback secCallback;
	private QualityGenerator generator;
	private final List<Organisation> organisations;
	
	@Autowired
	private QualityGeneratorService generatorService;

	public GeneratorController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			QualityGenerator generator) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);
		this.generator = generator;
		organisations = generatorService.loadGeneratorOrganisations(generator);
		this.secCallback = QualitySecurityCallbackFactory
				.createGeneratorSecurityCallback(ureq.getUserSession().getRoles(), generator, organisations);
		
		segmentButtonsCmp = new ButtonGroupComponent("segments");
		configurationLink = LinkFactory.createLink("generator.configuration", getTranslator(), this);
		segmentButtonsCmp.addButton(configurationLink, false);
		reportAccessLink = LinkFactory.createLink("generator.report.access", getTranslator(), this);
		segmentButtonsCmp.addButton(reportAccessLink, false);
		if (generatorService.hasWhiteListController(generator)) {
			whiteListLink = LinkFactory.createLink("generator.white.list", getTranslator(), this);
			segmentButtonsCmp.addButton(whiteListLink, false);
		}
		if (generatorService.hasBlackListController(generator)) {
			blackListLink = LinkFactory.createLink("generator.black.list", getTranslator(), this);
			segmentButtonsCmp.addButton(blackListLink, false);
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
		initButtons();
	}
	
	public void initButtons() {
		stackPanel.removeTool(enableDropdown, this);
		if (secCallback.canActivateGenerators()) {
			String enabled = generator.isEnabled()? "enabled": "disabled";
			
			enableDropdown = new Dropdown("generator.enable.dropdown", "generator." + enabled + ".hover", false, getTranslator());
			enableDropdown.setIconCSS("o_icon o_icon-fw o_icon_qual_gen_" + enabled);
			enableDropdown.setOrientation(DropdownOrientation.normal);
		
			enableLink = LinkFactory.createToolLink("generator.enable", translate("generator.enable"), this);
			enableLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_gen_enabled");
			enableLink.setVisible(!generator.isEnabled());
			enableDropdown.addComponent(enableLink);
			
			disableLink = LinkFactory.createToolLink("generator.disable", translate("generator.disable"), this);
			disableLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_gen_disabled");
			disableLink.setVisible(generator.isEnabled());
			enableDropdown.addComponent(disableLink);
			
			stackPanel.addTool(enableDropdown, Align.left, true, null, this);
		}
		
		long numberDataCollections = generatorService.getNumberOfDataCollections(generator);
		stackPanel.removeTool(deleteLink, this);
		if (secCallback.canDeleteGenerator(numberDataCollections)) {
			deleteLink = LinkFactory.createToolLink("generator.delete", translate("generator.delete"), this);
			deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_gen_delete");
			stackPanel.addTool(deleteLink, Align.left, true, null, this);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == enableLink) {
			doConfirmEnableGenerator(ureq);
		} else if (source == disableLink) {
			doConfirmDisableGenerator(ureq);
		} else if (source == deleteLink) {
			fireEvent(ureq, new GeneratorEvent(generator, GeneratorEvent.Action.DELETE));
		} else if (configurationLink == source) {
			doOpenConfiguration(ureq);
		} else if(reportAccessLink == source) {
			doOpenReportAccess(ureq);
		} else if(whiteListLink == source) {
			doOpenWhiteList(ureq);
		} else if(blackListLink == source) {
			doOpenBlackList(ureq);
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
			} else {
				fireEvent(ureq, event);
			}
		} else if (source == enableConfirmationCtrl) {
			if (Event.DONE_EVENT.equals(event)) {
				Date fromDate = enableConfirmationCtrl.getFromDate();
				doEnableGenerator(ureq, fromDate);
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == disableConfirmationCtrl) {
			if (Event.DONE_EVENT.equals(event)) {
				doDisabledGenerator(ureq);
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
	
	private void doOpenReportAccess(UserRequest ureq) {
		reportAccessCtrl = new GeneratorReportAccessController(ureq, getWindowControl(), secCallback, generator);
		listenTo(reportAccessCtrl);
		stackPanel.popUpToController(this);
		stackPanel.pushController(translate("generator.report.access"), reportAccessCtrl);
		segmentButtonsCmp.setSelectedButton(reportAccessLink);
	}
	
	private void doOpenWhiteList(UserRequest ureq) {
		whiteListCtrl = generatorService.getWhiteListController(ureq, getWindowControl(), secCallback, stackPanel,
				generator);
		listenTo(whiteListCtrl);
		stackPanel.popUpToController(this);
		stackPanel.pushController(translate("generator.white.list"), whiteListCtrl);
		segmentButtonsCmp.setSelectedButton(whiteListLink);
	}
	
	private void doOpenBlackList(UserRequest ureq) {
		blackListCtrl = generatorService.getBlackListController(ureq, getWindowControl(), secCallback, stackPanel,
				generator);
		listenTo(blackListCtrl);
		stackPanel.popUpToController(this);
		stackPanel.pushController(translate("generator.black.list"), blackListCtrl);
		segmentButtonsCmp.setSelectedButton(blackListLink);
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

	private void doEnableGenerator(UserRequest ureq, Date fromDate) {
		generator = generatorService.loadGenerator(generator);
		generator.setEnabled(true);
		generator.setLastRun(fromDate);
		generator = generatorService.updateGenerator(generator);
		log.info("Generator {} enabled by {}", generator, getIdentity());
		updateUI(ureq);	
	}
	
	private void doConfirmDisableGenerator(UserRequest ureq) {
		disableConfirmationCtrl = new GeneratorDisableConfirmationController(ureq, getWindowControl(), generator);
		listenTo(disableConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				disableConfirmationCtrl.getInitialComponent(), true, translate("generator.disable.confirm.title"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doDisabledGenerator(UserRequest ureq) {
		generator = generatorService.loadGenerator(generator);
		generator.setEnabled(false);
		generator = generatorService.updateGenerator(generator);
		updateUI(ureq);
	}
	
	private void updateUI(UserRequest ureq) {
		secCallback = QualitySecurityCallbackFactory.createGeneratorSecurityCallback(ureq.getUserSession().getRoles(),
				generator, organisations);
		
		initButtons();
		if (configCtrl != null) {
			configCtrl.onChanged(generator, secCallback);
		}
		if (reportAccessCtrl != null) {
			reportAccessCtrl.onChanged(secCallback, ureq);
		}
	}

	@Override
	protected void doDispose() {
		if (stackPanel != null) {
			stackPanel.removeListener(this);
		}
        super.doDispose();
	}

}
