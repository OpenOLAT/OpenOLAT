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
package org.olat.modules.curriculum.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.creditpoint.CreditPointService;
import org.olat.modules.creditpoint.CurriculumElementCreditPointConfiguration;
import org.olat.modules.curriculum.CurriculumElement;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementInfosBenefitsController extends BasicController {
	
	@Autowired
	private CreditPointService creditPointService;
	
	public CurriculumElementInfosBenefitsController(UserRequest ureq, WindowControl wControl, CurriculumElement element) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = createVelocityContainer("curriculum_element_benefits");
		mainVC.contextPut("hasCertificate", Boolean.valueOf(element.isShowCertificateBenefit()));
		mainVC.contextPut("hasCreditPoints", Boolean.valueOf(element.isShowCreditPointsBenefit()));
		
		if(element.isShowCreditPointsBenefit()) {
			CurriculumElementCreditPointConfiguration config = creditPointService.getConfiguration(element);
			if(config.isEnabled()) {
				String amount = config.getCreditPoints().toString() + " " + config.getCreditPointSystem().getLabel();
				mainVC.contextPut("amount", amount);
			}
		}
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
