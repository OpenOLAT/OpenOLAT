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
package org.olat.course.certificate.ui;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.NewControllerFactory;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.assessment.ui.tool.IdentityCertificatesController;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.modules.certificationprogram.CertificationCoordinator;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 31 oct. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificateDetailsController extends BasicController {

	private static final Size THUMBNAIL_SIZE = new Size(249, 172, false);

	private Link courseLink;
	private Link downloadButton;
	private Link startRecertificationButton;

	private final RepositoryEntry course;
	private final Certificate certificate;

	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	@Autowired
	private CertificationCoordinator certificationCoordinator;
	
	public CertificateDetailsController(UserRequest ureq, WindowControl wControl,
			Identity assessedIdentity, CertificateRow certificateRow) {
		super(ureq, wControl);
		certificate = certificateRow.getCertificate();
		
		VelocityContainer mainVC = createVelocityContainer("certificate_details");
		String mapperThumbnailUrl = registerCacheableMapper(ureq, "media-thumbnail-249-172",
				new ThumbnailMapper(certificate, certificatesManager, vfsRepositoryService));
		mainVC.contextPut("mapperThumbnailUrl", mapperThumbnailUrl);
		mainVC.contextPut("row", certificateRow);
		mainVC.contextPut("certificateKey", certificateRow.getKey());
		mainVC.contextPut("filename", DownloadCertificateCellRenderer.getName(certificateRow.getCertificate()));
		
		downloadButton = LinkFactory.createButton("download.button", mainVC, this);
		downloadButton.setIconLeftCSS("o_icon o_icon_download");
		downloadButton.setTarget("_blank");

		course = certificateRow.getCourse();
		if(course != null) {
			String displayName = StringHelper.escapeHtml(course.getDisplayname());
			if(StringHelper.containsNonWhitespace(course.getExternalRef())) {
				displayName += " \u00B7 <small class='mute'>" + StringHelper.escapeHtml(course.getExternalRef()) + "</small>";
			}
			courseLink = LinkFactory.createLink("courselink", "courselink", "courselink", displayName, getTranslator(), mainVC, this, Link.NONTRANSLATED);
			courseLink.setIconLeftCSS("o_icon o_CourseModule_icon");

			IdentityCertificatesController certificatesCtrl = new IdentityCertificatesController(ureq, getWindowControl(),
					certificateRow.getCourse(), null, certificateRow.getCertificationProgram(),  assessedIdentity,
					certificateRow.getCertificate(), false, true);
			listenTo(certificatesCtrl);
			if(certificatesCtrl.getNumOfCertificates() > 0) {
				mainVC.put("certificates", certificatesCtrl.getInitialComponent());
			}
		}
		
		CertificationProgram program = certificateRow.getCertificationProgram();
		if(program != null && program.isRecertificationEnabled()) {
			startRecertificationButton = LinkFactory.createButton("recertification.start", mainVC, this);
			boolean enabled = certificationCoordinator.isRecertificationAllowed(program, certificate, ureq.getRequestTimestamp());
			startRecertificationButton.setEnabled(enabled);	
		}
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(downloadButton == source) {
			doDownload(ureq);
		} else if(courseLink == source) {
			doOpenCourse(ureq);
		} else if(startRecertificationButton == source) {
			fireEvent(ureq, Event.CLOSE_EVENT);
			doOpenCourse(ureq);
		}
	}
	
	private void doDownload(UserRequest ureq) {
		VFSLeaf certificateLeaf = certificatesManager.getCertificateLeaf(certificate);
		String name = DownloadCertificateCellRenderer.getName(certificate);
		MediaResource certificateResource = new CertificateMediaResource(name, certificateLeaf, false);
		ureq.getDispatchResult().setResultingMediaResource(certificateResource);
	}
	
	private void doOpenCourse(UserRequest ureq) {
		if(course == null) return;
		
		String businessPath = "[RepositoryEntry:" + course.getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
	
	private static class ThumbnailMapper implements Mapper {
		
		private final Certificate certificate;
		private final CertificatesManager certificatesManager;
		private final VFSRepositoryService vfsRepositoryService;
		
		public ThumbnailMapper(Certificate certificate, CertificatesManager certificatesManager, VFSRepositoryService vfsRepositoryService) {
			this.certificate = certificate;
			this.certificatesManager = certificatesManager;
			this.vfsRepositoryService = vfsRepositoryService;
		}

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			MediaResource mr = null;
			
			String row = relPath;
			if(row.startsWith("/")) {
				row = row.substring(1, row.length());
			}
			int index = row.indexOf("/");
			if(index > 0) {
				row = row.substring(0, index);
				VFSLeaf certificateLeaf = certificatesManager.getCertificateLeaf(certificate);
				if(certificateLeaf != null) {
					VFSLeaf thumbnail = vfsRepositoryService.getThumbnail(certificateLeaf, THUMBNAIL_SIZE.getWidth(), THUMBNAIL_SIZE.getHeight(), true);
					if(thumbnail != null) {
						mr = new VFSMediaResource(thumbnail);
					}
				}
			}
			
			return mr == null ? new NotFoundMediaResource() : mr;
		}
	}
}
