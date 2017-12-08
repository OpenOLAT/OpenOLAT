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

import java.util.Date;
import java.util.Locale;

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.CertificateStatus;
import org.olat.course.certificate.model.CertificateLightPack;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 22.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DownloadCertificateCellRenderer implements FlexiCellRenderer {
	
	private final Locale locale;
	private Identity assessedIdentity;
	
	public DownloadCertificateCellRenderer(Locale locale) {
		this.locale = locale;
	}
	
	public DownloadCertificateCellRenderer(Identity assessedIdentity, Locale locale) {
		this(locale);
		this.assessedIdentity = assessedIdentity;
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(renderer == null) {
			if(cellValue instanceof CertificateLight) {
				renderExcel(target, (CertificateLight)cellValue);
			} else if(cellValue instanceof CertificateLightPack) {
				CertificateLightPack pack = (CertificateLightPack)cellValue;
				renderExcel(target, pack.getCertificate());
			}
		} else if(cellValue instanceof CertificateLight) {
			CertificateLight certificate = (CertificateLight)cellValue;
			if(assessedIdentity == null) {
				IdentityRef idRef = new IdentityRefImpl(certificate.getIdentityKey());
				render(target, certificate, idRef, translator.getLocale());
			} else {
				render(target, certificate, assessedIdentity, translator.getLocale());
			}
		} else if(cellValue instanceof CertificateLightPack) {
			CertificateLightPack pack = (CertificateLightPack)cellValue;
			render(target, pack.getCertificate(), pack.getIdentity(), translator.getLocale());	
		}
	}
	
	private void renderExcel(StringOutput sb, CertificateLight certificate) {
		String date = Formatter.getInstance(locale).formatDate(certificate.getCreationDate());
		sb.append(date);
	}
	
	private void render(StringOutput sb, CertificateLight certificate, IdentityRef identity, Locale locale) {
		String name = Formatter.getInstance(locale).formatDate(certificate.getCreationDate());
		if(CertificateStatus.pending.equals(certificate.getStatus())) {
			sb.append("<span><i class='o_icon o_icon_pending o_icon-spin'> </i> ").append(name).append(".pdf").append("</span>");
		} else if(CertificateStatus.error.equals(certificate.getStatus())) {
			sb.append("<span><i class='o_icon o_icon_error'> </i> ").append(name).append(".pdf").append("</span>");
		} else {
			sb.append("<a href='").append(getUrl(certificate, identity))
			  .append("' target='_blank'>");
			if(certificate.getNextRecertificationDate() != null && new Date().compareTo(certificate.getNextRecertificationDate()) > 0) {
				sb.append("<i class='o_icon o_icon_warn'> </i> ");
			}
			sb.append("<i class='o_icon o_filetype_pdf'> </i> ")
			  .append(name).append(".pdf").append("</a>");
		}
	}
	
	public static String getName(Certificate certificate) {
		StringBuilder sb = new StringBuilder(100);
		String fullName = CoreSpringFactory.getImpl(UserManager.class).getUserDisplayName(certificate.getIdentity());
		String date = Formatter.formatShortDateFilesystem(certificate.getCreationDate());
		sb.append(fullName).append("_").append(certificate.getCourseTitle()).append("_").append(date);
		String finalName = StringHelper.transformDisplayNameToFileSystemName(sb.toString());
		return finalName + ".pdf";
	}
	
	public static String getName(CertificateLight certificate, IdentityRef identity) {
		StringBuilder sb = new StringBuilder(100);
		String fullName = CoreSpringFactory.getImpl(UserManager.class).getUserDisplayName(identity);
		String date = Formatter.formatShortDateFilesystem(certificate.getCreationDate());
		sb.append(fullName).append("_").append(certificate.getCourseTitle()).append("_").append(date);
		String finalName = StringHelper.transformDisplayNameToFileSystemName(sb.toString());
		return finalName + ".pdf";
	}
	
	public static String getUrl(CertificateLight certificate, IdentityRef identity) {
		StringBuilder sb = new StringBuilder(100);
		sb.append(Settings.getServerContextPath()).append("/certificate/")
		  .append(certificate.getUuid()).append("/").append(getName(certificate, identity));
		return sb.toString();
	}
	
	public static String getUrl(Certificate certificate) {
		StringBuilder sb = new StringBuilder(100);
		sb.append(Settings.getServerContextPath()).append("/certificate/")
		  .append(certificate.getUuid()).append("/").append(getName(certificate));
		return sb.toString();
	}
}
