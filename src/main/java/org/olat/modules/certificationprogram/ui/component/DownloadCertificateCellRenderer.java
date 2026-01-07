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
package org.olat.modules.certificationprogram.ui.component;

import java.util.Locale;

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
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 12 sept. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class DownloadCertificateCellRenderer implements FlexiCellRenderer {
	
	private final Formatter formatter;
	
	public DownloadCertificateCellRenderer(Locale locale) {
		formatter = Formatter.getInstance(locale);
	}

	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row, FlexiTableComponent source,
			URLBuilder ubu, Translator translator) {
		if(renderer == null) {
			if(cellValue instanceof Certificate certificate) {
				renderExcel(target, certificate);
			}
		} else if(cellValue instanceof Certificate certificate) {
			render(target, certificate, certificate.getIdentity());
		}
	}
	
	private void renderExcel(StringOutput sb, Certificate certificate) {
		String date = formatter.formatDate(certificate.getCreationDate());
		sb.append(date);
	}
	
	private void render(StringOutput sb, Certificate certificate, Identity identity) {
		String name = getName(certificate, identity);
		sb.append("<a href='").append(getUrl(certificate, identity))
		  .append("' rel='noopener noreferrer' target='_blank'>")
		  .append("<i class='o_icon o_filetype_pdf'> </i> ")
		  .append(name).append("</a>");
	}
	
	private String getUrl(Certificate certificate, Identity identity) {
		StringBuilder sb = new StringBuilder(100);
		sb.append(Settings.getServerContextPath()).append("/certificate/")
		  .append(certificate.getUuid()).append("/").append(getName(certificate, identity));
		return sb.toString();
	}
	
	private String getName(Certificate certificate, Identity identity) {
		StringBuilder sb = new StringBuilder(100);
		String fullName = CoreSpringFactory.getImpl(UserManager.class).getUserDisplayName(identity);
		String date = Formatter.formatShortDateFilesystem(certificate.getCreationDate());
		sb.append(fullName).append("_");
		if(certificate.getCertificationProgram() != null) {
			sb.append(certificate.getCertificationProgram().getDisplayName());
		} else {
			sb.append(certificate.getCourseTitle());
		}
		sb.append("_").append(date);
		String finalName = StringHelper.transformDisplayNameToFileSystemName(sb.toString());
		return finalName + ".pdf";
	}
}
