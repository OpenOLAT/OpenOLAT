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
package org.olat.modules.certificationprogram.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.ButtonGroupComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.ui.component.Duration;
import org.olat.modules.certificationprogram.ui.component.DurationCellRenderer;
import org.olat.modules.certificationprogram.ui.component.DurationType;

/**
 * 
 * 
 * Initial date: 29 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class EditCertificationProgramController extends BasicController implements Activateable2 {

	private final Link metadataLink;
	private final Link certificateLink;
	private final Link configurationLink;
	private final VelocityContainer mainVC;
	private final ButtonGroupComponent segmentButtonsCmp;

	private final boolean administrator;
	private CertificationProgram certificationProgram;
	private final CertificationProgramSecurityCallback secCallback;
	
	private EditCertificationProgramMetadataController metadataCtrl;
	private EditCertificationProgramCertificateController certificateCtrl;
	private EditCertificationProgramConfigurationController configurationCtrl;
	
	public EditCertificationProgramController(UserRequest ureq, WindowControl wControl,
			CertificationProgram certificationProgram, CertificationProgramSecurityCallback secCallback) {
		super(ureq, wControl);
		this.secCallback = secCallback;
		this.certificationProgram = certificationProgram;
		administrator = ureq.getUserSession().getRoles().isAdministrator();
		
		mainVC = createVelocityContainer("program_edit");
		putInitialPanel(mainVC);
		exposeToVC();
		
		segmentButtonsCmp = new ButtonGroupComponent("segments");
		mainVC.put("segments", segmentButtonsCmp);
		metadataLink = LinkFactory.createLink("certification.program.metadata", getTranslator(), this);
		segmentButtonsCmp.addButton(metadataLink, true);
		configurationLink = LinkFactory.createLink("certification.program.configuration", getTranslator(), this);
		segmentButtonsCmp.addButton(configurationLink, false);
		certificateLink = LinkFactory.createLink("certification.program.certificate", getTranslator(), this);
		segmentButtonsCmp.addButton(certificateLink, false);
		
		doOpenMetadata(ureq);
	}
	
	public CertificationProgram getCertificationProgram() {
		return certificationProgram;
	}
	
	private void exposeToVC() {
		if (certificationProgram == null) {
			return;
		}
		
		if (administrator) {
			mainVC.contextPut("key", certificationProgram.getKey());
		}
		
		if(certificationProgram.isRecertificationEnabled()
				&& certificationProgram.getRecertificationMode() != null) {
			mainVC.contextPut("rmode", translate("recertification.mode." + certificationProgram.getRecertificationMode().name()));
		}
		
		if(certificationProgram.getCreditPointSystem() != null) {
			String text = CertificationHelper.creditPointsToString(certificationProgram);
			mainVC.contextPut("creditPoints", text);
		}
		
		Duration duration = certificationProgram.getValidityTimelapseDuration();
		if(duration != null) {
			String text = DurationCellRenderer.toString(duration, getTranslator());
			mainVC.contextPut("validity", text);
		}
		
		DurationType windowUnitType = certificationProgram.getRecertificationWindowUnit();
		int window = certificationProgram.getRecertificationWindow();
		if(windowUnitType != null && window > 0) {
			String text = DurationCellRenderer.toString(new Duration(window, windowUnitType), getTranslator());
			mainVC.contextPut("window", text);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == metadataLink) {
			doOpenMetadata(ureq);
		} else if (source == configurationLink) {
			doOpenConfiguration(ureq);
		} else if (source == certificateLink) {
			doOpenCertificate(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(metadataCtrl == source) {
			certificationProgram = metadataCtrl.getCertificationProgram();
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if(configurationCtrl == source) {
			certificationProgram = configurationCtrl.getCertificationProgram();
			exposeToVC();
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if(certificateCtrl == source) {
			certificationProgram = certificateCtrl.getCertificationProgram();
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}
	
	private void doOpenMetadata(UserRequest ureq) {
		removeAsListenerAndDispose(metadataCtrl);
		
		metadataCtrl = new EditCertificationProgramMetadataController(ureq, getWindowControl(), certificationProgram, secCallback);
		listenTo(metadataCtrl);
		mainVC.put("content", metadataCtrl.getInitialComponent());
		segmentButtonsCmp.setSelectedButton(metadataLink);
	}
	
	private void doOpenConfiguration(UserRequest ureq) {
		removeAsListenerAndDispose(configurationCtrl);
		
		configurationCtrl = new EditCertificationProgramConfigurationController(ureq, getWindowControl(), certificationProgram, secCallback);
		listenTo(configurationCtrl);
		mainVC.put("content", configurationCtrl.getInitialComponent());
		segmentButtonsCmp.setSelectedButton(configurationLink);
	}
	
	private void doOpenCertificate(UserRequest ureq) {
		removeAsListenerAndDispose(certificateCtrl);
		
		certificateCtrl = new EditCertificationProgramCertificateController(ureq, getWindowControl(), certificationProgram, secCallback);
		listenTo(certificateCtrl);
		mainVC.put("content", certificateCtrl.getInitialComponent());
		segmentButtonsCmp.setSelectedButton(certificateLink);
	}
}
