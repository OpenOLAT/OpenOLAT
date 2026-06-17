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
package org.olat.modules.selectus.ui.reference;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.PositionDetailsController;
import org.olat.modules.selectus.ui.PositionDocumentMapper;

/**
 * 
 * Initial date: 16.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReferencePositionController extends BasicController {

	private final Link nextButton;
	private final PositionDetailsController positionDetailsCtrl;

	private final String mapperBaseUrl;
	
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;

	public ReferencePositionController(UserRequest ureq, WindowControl wControl,
			Position position, Application application, List<Application> applicationsList,
			RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		mapperBaseUrl = registerMapper(ureq, new PositionDocumentMapper(position));
		
		VelocityContainer mainVC = createVelocityContainer("reference_reviewer_position");
		positionDetailsCtrl = new PositionDetailsController(ureq, getWindowControl(), position, mapperBaseUrl, secCallback);
		listenTo(positionDetailsCtrl);
		mainVC.put("content", positionDetailsCtrl.getInitialComponent());
		initDetails(position, application, applicationsList);
		
		nextButton = LinkFactory.createButton("next", mainVC, this);
		mainVC.put("next", nextButton);
		
		putInitialPanel(mainVC);	
	}
	
	/**
	 * Can override some values in the position details panel.
	 * 
	 * @param position The position
	 * @param application The application
	 */
	private void initDetails(Position position, Application application, List<Application> applicationsList) {
		String positionTitle = position.getMLTitle(getLocale());
		String[] positionTitleArgs = new String[] {
			StringHelper.escapeHtml(positionTitle),													// 0
			salutationGenerator.getTitleLastName(application, applicationsList, getLocale()),		// 1
			salutationGenerator.getTitleFirstLastName(application, applicationsList, getLocale())	// 2
		};
		String positionValue = translate("reference.position.details.text", positionTitleArgs);
		positionDetailsCtrl.setPositionTitle(positionValue);
	}
	
	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(nextButton == source) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
}
