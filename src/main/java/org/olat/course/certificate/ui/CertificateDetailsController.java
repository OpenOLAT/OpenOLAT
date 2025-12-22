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

import java.util.Date;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.NewControllerFactory;
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
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.id.Identity;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.assessment.ui.tool.IdentityCertificatesController;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.RepositoryEntryCertificateConfiguration;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.ui.CertificationHelper;
import org.olat.modules.certificationprogram.ui.CertificationProgramCertifiedMembersController;
import org.olat.modules.certificationprogram.ui.CertificationStatus;
import org.olat.modules.creditpoint.CreditPointService;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointWallet;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 31 oct. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificateDetailsController extends BasicController {


	private Link courseLink;
	private final Link downloadButton;
	private Link startRecertificationButton;
	private final VelocityContainer mainVC;

	private final Certificate certificate;
	private final RepositoryEntry course;
	private final Identity assessedIdentity;

	@Autowired
	private CreditPointService creditPointService;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	public CertificateDetailsController(UserRequest ureq, WindowControl wControl,
			Identity assessedIdentity, CertificateRow certificateRow) {
		super(ureq, wControl, Util
				.createPackageTranslator(CertificationProgramCertifiedMembersController.class, ureq.getLocale()));
		certificate = certificateRow.getCertificate();
		this.assessedIdentity = assessedIdentity;
		
		mainVC = createVelocityContainer("certificate_details");
		String mapperThumbnailUrl = registerCacheableMapper(ureq, CertificatesListOverviewController.THUMBNAIL_MAPPER_ID,
				new ThumbnailMapper(certificate, certificatesManager, vfsRepositoryService));
		mainVC.contextPut("mapperThumbnailUrl", mapperThumbnailUrl);
		mainVC.contextPut("certificateKey", certificate.getKey());
		mainVC.contextPut("filename", DownloadCertificateCellRenderer.getName(certificate));
		mainVC.contextPut("awardedBy", StringHelper.containsNonWhitespace(certificateRow.getAwardedBy()));
		mainVC.contextPut("awardedByIconCss", certificateRow.getAwardedByIconCSS());
		mainVC.contextPut("origin", certificateRow.getOrigin());
		mainVC.contextPut("creationDate", certificate.getCreationDate());
		initStatus(ureq);
		
		downloadButton = LinkFactory.createButton("download.button", mainVC, this);
		downloadButton.setIconLeftCSS("o_icon o_icon_download");
		downloadButton.setTarget("_blank");
		
		course = certificateRow.getCourse();
		CertificationProgram program = certificateRow.getCertificationProgram();
		if(program != null) {
			String displayName = StringHelper.escapeHtml(program.getDisplayName());
			if(StringHelper.containsNonWhitespace(program.getIdentifier())) {
				displayName += " \u00B7 <small class='mute'>" + StringHelper.escapeHtml(program.getIdentifier()) + "</small>";
			}
			mainVC.contextPut("awardedBy", displayName);

			if(program.isRecertificationEnabled() && certificateRow.getRecertificationInDays() != null) {
				initRecertificationProgram(ureq, certificateRow.getRecertificationInDays(), program);
				mainVC.contextPut("recertificationEnable", Boolean.TRUE);
			}
		} else if(course != null) {
			String displayName = StringHelper.escapeHtml(course.getDisplayname());
			if(StringHelper.containsNonWhitespace(course.getExternalRef())) {
				displayName += " \u00B7 <small class='mute'>" + StringHelper.escapeHtml(course.getExternalRef()) + "</small>";
			}
			mainVC.contextPut("awardedBy", displayName);
			courseLink = LinkFactory.createLink("courselink", "courselink", "courselink", displayName, getTranslator(), mainVC, this, Link.NONTRANSLATED);
			courseLink.setIconLeftCSS("o_icon o_CourseModule_icon");
			
			RepositoryEntryCertificateConfiguration certificateConfig = certificatesManager.getConfiguration(course);
			if(certificateConfig != null && certificateConfig.isRecertificationEnabled()) {
				Date nextDate = certificatesManager.nextRecertificationWindow(certificate, certificateConfig);
				if(nextDate != null && nextDate.compareTo(ureq.getRequestTimestamp()) <= 0) {
					startRecertificationButton = LinkFactory.createButton("recertification.start", mainVC, this);
				}
				mainVC.contextPut("recertificationEnable", Boolean.TRUE);
				initRecertificationCourse(ureq, certificateRow.getRecertificationInDays(), certificateConfig);
			}
		}
		
		if(program != null || course != null) {
			IdentityCertificatesController certificatesCtrl = new IdentityCertificatesController(ureq, getWindowControl(),
					certificateRow.getCourse(), null, certificateRow.getCertificationProgram(),  assessedIdentity,
					certificateRow.getCertificate(), false, true);
			listenTo(certificatesCtrl);
			if(certificatesCtrl.getNumOfCertificates() > 0) {
				mainVC.put("certificates", certificatesCtrl.getInitialComponent());
			}
		}
		
		putInitialPanel(mainVC);
	}
	
	private void initStatus(UserRequest ureq) {
		CertificationStatus status = CertificationStatus.evaluate(certificate, ureq.getRequestTimestamp());
		mainVC.contextPut("status", status.name().toLowerCase());
		String statusString = status.asLabelExplained(certificate, ureq.getRequestTimestamp(), getTranslator());
		mainVC.contextPut("statusExplained", statusString);
		
		if(status == CertificationStatus.REVOKED) {
			mainVC.contextPut("revocationDate", certificate.getRevocationDate());
			mainVC.contextPut("revoked", Boolean.TRUE);	
		} else {
			mainVC.contextPut("revoked", Boolean.FALSE);
			mainVC.contextRemove("revocationDate");
		}
		
		if(certificate.getNextRecertificationDate() != null) {
			mainVC.contextPut("nextRecertificationDate", certificate.getNextRecertificationDate());
		} else {
			mainVC.contextRemove("nextRecertificationDate");
		}
	}
	
	private void initRecertificationCourse(UserRequest ureq, RecertificationInDays recertificationInDays,
			RepositoryEntryCertificateConfiguration certificateConfig) {
		if((certificateConfig != null && certificateConfig.isValidityEnabled())
				|| (certificateConfig == null && certificate.getNextRecertificationDate() != null)) {
			Date nextRecertificationDate = certificate.getNextRecertificationDate();
			if(certificateConfig != null && certificateConfig.isRecertificationEnabled()) {
				nextRecertificationDate = certificatesManager.nextRecertificationWindow(certificate, certificateConfig);
			}

			Formatter formatter = Formatter.getInstance(getLocale());
			String nextRecertificationDateFormatted = formatter.formatDate(nextRecertificationDate);
			long days = DateUtils.countDays(ureq.getRequestTimestamp(), nextRecertificationDate);
			String recertificationInfosDate = nextRecertificationDateFormatted;
			if(days >= 2) {
				recertificationInfosDate = translate("recertification.course.more", nextRecertificationDateFormatted, Long.toString(days));	
			} else if(days == 1) {
				recertificationInfosDate = translate("recertification.course.more.tomorrow", nextRecertificationDateFormatted, Long.toString(days));	
			} else if(days == 0) {
				recertificationInfosDate = signal(recertificationInDays) + translate("recertification.course.more.today", nextRecertificationDateFormatted);
			} else {
				recertificationInfosDate = signal(recertificationInDays) + translate("certificate.from.recertification", formatter.formatDate(nextRecertificationDate));
			}
			mainVC.contextPut("recertificationDate", recertificationInfosDate);
		}
	}
	
	private void initRecertificationProgram(UserRequest ureq, RecertificationInDays recertificationInDays, CertificationProgram program) {
		// Credit point
		CreditPointSystem creditPointSystem = program.getCreditPointSystem();
		if(creditPointSystem != null && program.getCreditPoints() != null) {
			String pointsRequirement = CertificationHelper.creditPointsToString(program.getCreditPoints(), creditPointSystem);
			mainVC.contextPut("requiredPoints", StringHelper.escapeHtml(pointsRequirement));
			
			CreditPointWallet wallet = creditPointService.getOrCreateWallet(assessedIdentity, creditPointSystem);
			String balance = CertificationHelper.creditPointsToString(wallet.getBalance(), creditPointSystem);
			mainVC.contextPut("balancePoints", StringHelper.escapeHtml(balance));
			
			boolean insufficientBalance = wallet.getBalance().compareTo(program.getCreditPoints()) < 0;
			if(insufficientBalance) {
				if(recertificationInDays.isBeforeRecertification(ureq.getRequestTimestamp())) {
					mainVC.contextPut("requirementWarningCssIcon", "o_icon_recertification_warning");
				} else if(recertificationInDays.isRecertificationOpen(ureq.getRequestTimestamp())) {
					mainVC.contextPut("requirementWarningCssIcon", "o_icon_recertification_error");
					mainVC.contextPut("requirementWarningMsg", translate("warning.requirement.credit.points"));
				}
			}
		}

		// Buttons
		mainVC.contextPut("recertificationWindowClosed", recertificationInDays.isRecertificationWindowClosed(ureq.getRequestTimestamp()));

		// Date of recertification
		Date nextRecertificationDate = recertificationInDays.nextRecertificationDate();
		Date endDateOfRecertificationWindow = recertificationInDays.endDateOfRecertificationWindow();
		initRecertificationDate(ureq, recertificationInDays, nextRecertificationDate,  endDateOfRecertificationWindow);
	}
	
	private void initRecertificationDate(UserRequest ureq, RecertificationInDays recertificationInDays, Date nextRecertificationDate, Date  endDateOfRecertificationWindow) {
		Formatter formatter = Formatter.getInstance(getLocale());
		String nextRecertificationDateFormatted = formatter.formatDate(nextRecertificationDate);
		String endDateOfRecertificationWindowFormatted = formatter.formatDate(endDateOfRecertificationWindow);
		String recertificationInfosDate = nextRecertificationDateFormatted;// Set a default value
		
		long days = DateUtils.countDays(ureq.getRequestTimestamp(), nextRecertificationDate);
		if(days >= 2) {
			recertificationInfosDate = translate("recertification.running.more", nextRecertificationDateFormatted, Long.toString(days));	
		} else if(days == 1) {
			recertificationInfosDate = translate("recertification.running.more.tomorrow", nextRecertificationDateFormatted, Long.toString(days));	
		} else if(days == 0) {
			if(endDateOfRecertificationWindow == null) {
				recertificationInfosDate = signal(recertificationInDays) + translate("recertification.running.more.today");
			} else {
				long winDays = DateUtils.countDays(ureq.getRequestTimestamp(), endDateOfRecertificationWindow);
				recertificationInfosDate = signal(recertificationInDays) + translate("recertification.running.more.today.window", endDateOfRecertificationWindowFormatted, Long.toString(winDays));
			}
		} else if(endDateOfRecertificationWindow != null) {
			long winDays = DateUtils.countDays(ureq.getRequestTimestamp(), endDateOfRecertificationWindow);
			if(winDays > 2) {
				recertificationInfosDate = signal(recertificationInDays) + translate("recertification.running.more.today.window", endDateOfRecertificationWindowFormatted, Long.toString(winDays));
			} else if(winDays == 1) {
				recertificationInfosDate = signal(recertificationInDays) + translate("recertification.running.late.tomorrow", endDateOfRecertificationWindowFormatted);
			} else if(winDays == 0) {
				recertificationInfosDate = signal(recertificationInDays) + translate("recertification.running.late.today", endDateOfRecertificationWindowFormatted);
			} else {
				recertificationInfosDate = signal(recertificationInDays) + translate("recertification.running.late", endDateOfRecertificationWindowFormatted);
			}
		} else {
			recertificationInfosDate = signal(recertificationInDays) + translate("certificate.from.recertification", recertificationInfosDate);
		}

		mainVC.contextPut("recertificationDate", recertificationInfosDate);
	}
	
	private String signal(RecertificationInDays recertificationInDays) {
		if(recertificationInDays != null && recertificationInDays.isDanger()) {
			return "<i class='o_icon o_icon_recertification_error'> </i> ";
		}
		if(recertificationInDays != null && recertificationInDays.isWarning()) {
			return "<i class='o_icon o_icon_recertification_warning'> </i> ";
		}
		return "";
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(downloadButton == source) {
			doDownload(ureq);
		} else if(courseLink == source || startRecertificationButton == source) {
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
					VFSLeaf thumbnail = vfsRepositoryService.getThumbnail(certificateLeaf,
							CertificatesListOverviewController.THUMBNAIL_SIZE.getWidth(), CertificatesListOverviewController.THUMBNAIL_SIZE.getHeight(),
							true);
					if(thumbnail != null) {
						mr = new VFSMediaResource(thumbnail, ServletUtil.CACHE_ONE_MONTH);
					}
				}
			}
			
			return mr == null ? new NotFoundMediaResource() : mr;
		}
	}
}
