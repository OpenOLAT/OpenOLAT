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
package org.olat.modules.coach.ui.em;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Formatter;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorkbookResource;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.course.certificate.model.CertificateIdentityConfig;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.apache.logging.log4j.Logger;

/**
 * Initial date: 2025-01-14<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CertificatesExport extends OpenXMLWorkbookResource {

	private static final Logger log = Tracing.createLoggerFor(CertificatesExport.class);
	private final List<CertificateIdentityConfig> certificates;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final Translator translator;

	public CertificatesExport(List<CertificateIdentityConfig> certificates, List<UserPropertyHandler> userPropertyHandlers, 
							  Translator translator) {
		super(label());
		this.certificates = certificates;
		this.userPropertyHandlers = userPropertyHandlers;
		this.translator = translator;
	}

	private static String label() {
		return "certificates_" + Formatter.formatDatetimeFilesystemSave(new Date()) + ".xlsx";
	}
	
	@Override
	protected void generate(OutputStream out) {
		try (OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
			OpenXMLWorksheet worksheet = workbook.nextWorksheet();
			worksheet.setHeaderRows(1);
			OpenXMLWorksheet.Row headerRow = worksheet.newRow();
			int colIdx = 0;
			headerRow.addCell(colIdx++, translator.translate("table.header.id"));
			for (UserPropertyHandler userPropertyHandler : userPropertyHandlers) {
				headerRow.addCell(colIdx++, translator.translate("table.name." + userPropertyHandler.getName()));
			}
			headerRow.addCell(colIdx++, translator.translate("table.header.path"));
			headerRow.addCell(colIdx, translator.translate("table.header.course"));
			for (CertificateIdentityConfig certificateIdentityConfig : certificates) {
				OpenXMLWorksheet.Row row = worksheet.newRow();
				colIdx = 0;
				row.addCell(colIdx++, certificateIdentityConfig.getCertificate().getKey(), null);
				for (int propIdx = 0; propIdx < userPropertyHandlers.size(); propIdx++) {
					row.addCell(colIdx++, certificateIdentityConfig.getIdentityProp(propIdx));
				}
				row.addCell(colIdx++, certificateIdentityConfig.getCertificate().getPath(), null);
				row.addCell(colIdx, certificateIdentityConfig.getConfig() != null && certificateIdentityConfig.getConfig().getEntry() != null ? certificateIdentityConfig.getConfig().getEntry().getDisplayname() : "");
			}
		} catch (IOException e) {
			log.error("", e);
		}
	}
}
