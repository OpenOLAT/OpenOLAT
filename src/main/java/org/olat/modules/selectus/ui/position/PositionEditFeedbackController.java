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
package org.olat.modules.selectus.ui.position;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.FeedbackService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.ApplicationsFeedbackConfiguration;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.feedback.appsfeedback.PositionEditApplicationsFeedbackConfigurationController;
import org.olat.modules.selectus.ui.feedback.publicfeedback.PositionEditPublicFeedbackController;

/**
 * 
 * Initial date: 21 avr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditFeedbackController extends BasicController implements Activateable2, PositionEditableController {

	private final TabbedPane tabPane;
	
	private Position position;
	
	private final PositionEditPublicFeedbackController publicFeedbackCtrl;
	private final List<PositionEditApplicationsFeedbackConfigurationController> appsFeedbackCtrls = new ArrayList<>();

	@Autowired
	private FeedbackService feedbackService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	
	public PositionEditFeedbackController(UserRequest ureq, WindowControl wControl, Position position) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		
		if(position.getKey() != null) {
			position = recruitingService.getPosition(position.getKey());
		} else {
			position = recruitingService.savePosition(position);
		}
		this.position = position;
		
		tabPane = new TabbedPane("evalTabPane", getLocale());
		tabPane.setElementCssClass("o_sel_edit_position_feedback_tab");
		tabPane.setHideDisabledTab(true);
		tabPane.addListener(this);
		
		publicFeedbackCtrl = new PositionEditPublicFeedbackController(ureq, getWindowControl(), position);
		listenTo(publicFeedbackCtrl);

		String defaultConfigurationName = translate("edit.apps.feedback.default.configuration");
		List<ApplicationsFeedbackConfiguration> configurations = feedbackService
				.getOrCreateApplicationsFeedbackConfigurations(defaultConfigurationName, position);

		for(ApplicationsFeedbackConfiguration configuration:configurations) {
			PositionEditApplicationsFeedbackConfigurationController appsFeedbackCtrl
				= new PositionEditApplicationsFeedbackConfigurationController(ureq, wControl, position, configuration);
			listenTo(appsFeedbackCtrl);
			appsFeedbackCtrls.add(appsFeedbackCtrl);
		}
	
		if(recruitingModule.isMembersFeedbackEnabled()) {
			for(PositionEditApplicationsFeedbackConfigurationController appsFeedbackCtrl:appsFeedbackCtrls) {
				tabPane.addTab(appsFeedbackCtrl.getConfigurationName(), appsFeedbackCtrl);
			}
		}
		
		if(recruitingModule.isPublicFeedbackEnabled()) {
			tabPane.addTab(translate("edit.step.public.feedback"), publicFeedbackCtrl);
		}
		
		putInitialPanel(tabPane);
	}

	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public void updatePosition(Position updatedPosition) {
		this.position = updatedPosition;
		publicFeedbackCtrl.updatePosition(updatedPosition);
		for(PositionEditApplicationsFeedbackConfigurationController appsFeedbackCtrl:appsFeedbackCtrls) {
			appsFeedbackCtrl.updatePosition(updatedPosition);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("tab".equalsIgnoreCase(type)) {
			tabPane.activate(ureq, entries, state);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source instanceof PositionEditableController) {
			position = ((PositionEditableController)source).getPosition();
			fireEvent(ureq, event);
		} else if(appsFeedbackCtrls.contains(source)) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == tabPane) {
			tabPane.addToHistory(ureq, getWindowControl());
		}
	}
}
