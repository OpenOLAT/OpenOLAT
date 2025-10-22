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
package org.olat.repository.ui.list;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.RepositoryEntryCertificateConfiguration;
import org.olat.course.certificate.ui.CertificatesOptionsController;
import org.olat.modules.creditpoint.CreditPointFormat;
import org.olat.modules.creditpoint.CreditPointService;
import org.olat.modules.creditpoint.RepositoryEntryCreditPointConfiguration;
import org.olat.modules.creditpoint.ui.CreditPointRepositoryEntryConfigController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class RepositoryEntryDetailsBenefitsController extends BasicController {

	@Autowired
	private CreditPointService creditPointService;
	@Autowired
	private CertificatesManager certificatesManager;
	
	public RepositoryEntryDetailsBenefitsController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry) {
		super(ureq, wControl, Util.createPackageTranslator(RepositoryService.class, ureq.getLocale(),
				Util.createPackageTranslator(CreditPointRepositoryEntryConfigController.class, ureq.getLocale(),
						Util.createPackageTranslator(CertificatesOptionsController.class, ureq.getLocale()))));
		
		VelocityContainer mainVC = createVelocityContainer("details_benefits");
		
		RepositoryEntryCertificateConfiguration certificateConfig = certificatesManager.getConfiguration(entry);
		if(certificateConfig != null && certificateConfig.isCertificateEnabled()) {
			mainVC.contextPut("hasCertificate", Boolean.TRUE);
			
			int expiration = certificateConfig.getValidityTimelapse();
			if(expiration >= 0 && certificateConfig.isValidityEnabled() && certificateConfig.getValidityTimelapseUnit() != null) {
				String unit = translate(certificateConfig.getValidityTimelapseUnit().name());
				mainVC.contextPut("certificateValidity", translate("details.valid.for", Integer.toString(expiration), unit));
			}
		}
		
		RepositoryEntryCreditPointConfiguration creditPointConfig = creditPointService.getOrCreateConfiguration(entry);
		if(creditPointConfig != null && creditPointConfig.isEnabled()) {
			mainVC.contextPut("hasCreditPoints", Boolean.TRUE);
			
			String amount = CreditPointFormat.format(creditPointConfig.getCreditPoints(), creditPointConfig.getCreditPointSystem());
			mainVC.contextPut("amount", amount);
			
			if(creditPointConfig.getExpiration()  != null && creditPointConfig.getExpiration().intValue() > 0) {
				Integer expiration = creditPointConfig.getExpiration();
				String unit = translate(creditPointConfig.getExpirationType().i18n(expiration));
				mainVC.contextPut("pointsValidity", translate("details.valid.for", expiration.toString(), unit));
			}
		}
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}
