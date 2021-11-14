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
package org.olat.course.certificate.ui;

import org.olat.core.commons.modules.singlepage.SinglePageController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.util.Formatter;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificatesManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CertificateController extends BasicController {
	
	private Link downloadButton;
	private SinglePageController pageCtrl;
	
	private final Certificate certificate;
	@Autowired
	private CertificatesManager certificatesManager;
	
	public CertificateController(UserRequest ureq, WindowControl wControl, Certificate certificate) {
		super(ureq, wControl);
		this.certificate = certificate;

		VelocityContainer mainVC = createVelocityContainer("certificate");
		VFSLeaf certificateLeaf = certificatesManager.getCertificateLeaf(certificate);
		if(certificate != null) {
			Formatter formatter = Formatter.getInstance(getLocale());
			String creationDate = formatter.formatDateAndTime(certificate.getCreationDate());
			String creationDateMsg = translate("certificate.creationdate", creationDate);
			mainVC.contextPut("certificateCreationDateMsg", creationDateMsg);
			
			downloadButton = LinkFactory.createButton("download.button", mainVC, this);
			downloadButton.setIconLeftCSS("o_icon o_icon_download");
			downloadButton.setTarget("_blank");
			
			String url = DownloadCertificateCellRenderer.getUrl(certificate);
			mainVC.contextPut("certificateUrl", url);
			String name = DownloadCertificateCellRenderer.getName(certificate);
			mainVC.contextPut("certificateName", name);
		}
		
		if(certificateLeaf != null) {
			VFSContainer container = certificateLeaf.getParentContainer();
			String filename = certificateLeaf.getName();
			pageCtrl = new SinglePageController(ureq, getWindowControl(), container, filename, false);
			listenTo(pageCtrl);
			mainVC.put("certificate", pageCtrl.getInitialComponent());
		}
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(downloadButton == source) {
			VFSLeaf certificateLeaf = certificatesManager.getCertificateLeaf(certificate);
			String name = DownloadCertificateCellRenderer.getName(certificate);
			MediaResource certificateResource = new CertificateMediaResource(name, certificateLeaf);
			ureq.getDispatchResult().setResultingMediaResource(certificateResource);
		}
	}
}