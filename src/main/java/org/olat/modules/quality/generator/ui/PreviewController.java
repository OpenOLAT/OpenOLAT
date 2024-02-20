/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.quality.generator.ui;

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
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.modules.quality.generator.QualityGeneratorProvider;
import org.olat.modules.quality.generator.QualityGeneratorService;
import org.olat.modules.quality.generator.QualityPreview;
import org.olat.modules.quality.generator.QualityPreviewStatus;
import org.olat.modules.quality.generator.manager.QualityGeneratorProviderFactory;
import org.olat.modules.quality.ui.PreviewReportAccessController;
import org.olat.modules.quality.ui.QualityUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 Dec 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class PreviewController extends BasicController implements TooledController, Activateable2 {
	
	private Link blacklistAddLink;
	private Link blacklistRemoveLink;
	private final Link configurationLink;
	private final Link reportAccessLink;
	private final TooledStackedPanel stackPanel;
	private final ButtonGroupComponent segmentButtonsCmp;

	private PreviewConfigurationController configurationCtrl;
	private PreviewReportAccessController reportAccessCtrl;
	private CloseableModalController cmc;
	private PreviewBlacklistConfirmationController blacklistAddConfirmationCtrl;
	private PreviewBlacklistConfirmationController blacklistRemoveConfirmationCtrl;
	
	private final QualityPreview preview;
	private final boolean canEdit;
	private boolean blacklisted;
	
	@Autowired
	private QualityGeneratorService generatorService;
	@Autowired
	private QualityGeneratorProviderFactory generatorProviderFactory;
	
	protected PreviewController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, QualityPreview preview, boolean canEdit) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(QualityUIFactory.class, getLocale(), getTranslator()));
		this.stackPanel = stackPanel;
		stackPanel.addListener(this);
		this.preview = preview;
		this.canEdit = canEdit;
		this.blacklisted = QualityPreviewStatus.blacklist == preview.getStatus();
		
		segmentButtonsCmp = new ButtonGroupComponent("segments");
		configurationLink = LinkFactory.createLink("data.collection.configuration", getTranslator(), this);
		segmentButtonsCmp.addButton(configurationLink, false);
		reportAccessLink = LinkFactory.createLink("data.collection.report.access", getTranslator(), this);
		segmentButtonsCmp.addButton(reportAccessLink, false);
		
		StackedPanel mainPanel = putInitialPanel(new SimpleStackedPanel("dataCollectionSegments"));
		mainPanel.setContent(new Panel("empty"));
	}

	@Override
	public void initTools() {
		stackPanel.addTool(segmentButtonsCmp, true);
		
		blacklistAddLink = LinkFactory.createToolLink("preview.blacklist.add", translate("preview.blacklist.add"), this);
		blacklistAddLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_blacklist_add");
		stackPanel.addTool(blacklistAddLink, Align.left, true, null, this);
		
		blacklistRemoveLink = LinkFactory.createToolLink("preview.blacklist.remove", translate("preview.blacklist.remove"), this);
		blacklistRemoveLink.setIconLeftCSS("o_icon o_icon-fw o_icon_qual_blacklist_remove");
		stackPanel.addTool(blacklistRemoveLink, Align.left, true, null, this);
	}

	private void updateUI() {
		blacklistAddLink.setVisible(canEdit && !blacklisted);
		blacklistRemoveLink.setVisible(canEdit && blacklisted);
		stackPanel.setDirty(true);
		
		if (configurationCtrl != null) {
			configurationCtrl.setReadOnly(!canEdit || blacklisted);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		updateUI();
		doOpenConfiguration(ureq);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (blacklistAddLink == source) {
			doConfirmBlacklistAdd(ureq);
		} else if (blacklistRemoveLink == source) {
			doConfirmBlacklistRemove(ureq);
		} else if (configurationLink == source) {
			doOpenConfiguration(ureq);
		} else if (reportAccessLink == source) {
			doOpenReportAccess(ureq);
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
		if (blacklistAddConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doAddToBlacklist();
			}
			cmc.deactivate();
			cleanUp();
		} else if (blacklistRemoveConfirmationCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doRemoveFromBlacklist();
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(blacklistAddConfirmationCtrl);
		removeAsListenerAndDispose(blacklistRemoveConfirmationCtrl);
		removeAsListenerAndDispose(cmc);
		blacklistAddConfirmationCtrl = null;
		blacklistRemoveConfirmationCtrl = null;
		cmc = null;
	}

	@Override
	protected void doDispose() {
		if(stackPanel != null) {
			stackPanel.removeListener(this);
		}
		super.doDispose();
	}
	
	private void doOpenConfiguration(UserRequest ureq) {
		stackPanel.popUpToController(this);
		configurationCtrl = new PreviewConfigurationController(ureq, getWindowControl(), stackPanel, preview);
		listenTo(configurationCtrl);
		configurationCtrl.setReadOnly(!canEdit || blacklisted);
		stackPanel.pushController(translate("data.collection.configuration"), configurationCtrl);
		segmentButtonsCmp.setSelectedButton(configurationLink);
	}
	
	private void doOpenReportAccess(UserRequest ureq) {
		stackPanel.popUpToController(this);
		reportAccessCtrl = new PreviewReportAccessController(ureq, getWindowControl(), preview.getGenerator());
		listenTo(reportAccessCtrl);
		stackPanel.pushController(translate("data.collection.report.access"), reportAccessCtrl);
		segmentButtonsCmp.setSelectedButton(reportAccessLink);
	}

	private void doConfirmBlacklistAdd(UserRequest ureq) {
		if (guardModalController(blacklistAddConfirmationCtrl)) return;
		
		QualityGeneratorProvider provider = generatorProviderFactory.getProvider(preview.getGenerator().getType());
		String message = provider.getAddToBlacklistConfirmationMessage(getLocale(), preview);
		
		blacklistAddConfirmationCtrl = new PreviewBlacklistConfirmationController(ureq, getWindowControl(), true, message);
		listenTo(blacklistAddConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				blacklistAddConfirmationCtrl.getInitialComponent(), true, translate("preview.blacklist.add"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doAddToBlacklist() {
		generatorService.addToBlacklist(preview);
		blacklisted = true;
		updateUI();
	}
	
	private void doConfirmBlacklistRemove(UserRequest ureq) {
		if (guardModalController(blacklistRemoveConfirmationCtrl)) return;
		
		QualityGeneratorProvider provider = generatorProviderFactory.getProvider(preview.getGenerator().getType());
		String message = provider.getRemoveFromBlacklistConfirmationMessage(getLocale(), preview);
		
		blacklistRemoveConfirmationCtrl = new PreviewBlacklistConfirmationController(ureq, getWindowControl(), false, message);
		listenTo(blacklistRemoveConfirmationCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				blacklistRemoveConfirmationCtrl.getInitialComponent(), true, translate("preview.blacklist.remove"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doRemoveFromBlacklist() {
		generatorService.removeFromBlacklist(preview);
		blacklisted = false;
		updateUI();
	}

}
