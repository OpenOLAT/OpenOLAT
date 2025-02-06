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
package org.olat.modules.curriculum.reports;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.core.util.openxml.OpenXMLWorkbook;
import org.olat.core.util.openxml.OpenXMLWorksheet;
import org.olat.core.util.openxml.OpenXMLWorksheet.Row;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.coach.reports.AbstractReportConfiguration;
import org.olat.modules.coach.reports.ReportConfigurationAccessSecurityCallback;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumReportConfiguration;
import org.olat.modules.curriculum.ui.CurriculumManagerRootController;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * Initial date: 2025-02-06<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class AccountingReportConfiguration extends AbstractReportConfiguration implements CurriculumReportConfiguration {

	@Override
	public boolean hasAccess(ReportConfigurationAccessSecurityCallback secCallback) {
		if (!secCallback.isCurriculumContext()) {
			return false;
		}
		if (secCallback.isCurriculumOwner() || secCallback.isCurriculumManager()) {
			return true;
		}
		return false;
	}

	@Override
	protected Translator getTranslator(Locale locale) {
		return Util.createPackageTranslator(CurriculumManagerRootController.class, locale, super.getTranslator(locale));
	}

	@Override
	protected String getI18nCategoryKey() {
		return "report.booking";
	}

	@Override
	protected int generateCustomHeaderColumns(Row header, int pos, Translator translator) {
		header.addCell(pos++, translator.translate("report.header.curriculum"));
		header.addCell(pos++, translator.translate("report.header.ext.ref"));
		header.addCell(pos++, translator.translate("report.header.org.id"));
		header.addCell(pos++, translator.translate("report.header.organisation"));
		header.addCell(pos++, translator.translate("report.header.implementation"));
		header.addCell(pos++, translator.translate("report.header.ext.ref"));
		header.addCell(pos++, translator.translate("report.header.implementation.type"));
		header.addCell(pos++, translator.translate("report.header.implementation.status"));
		header.addCell(pos++, translator.translate("report.header.implementation.format"));
		header.addCell(pos++, translator.translate("report.header.execution.from"));
		header.addCell(pos++, translator.translate("report.header.execution.to"));
		header.addCell(pos++, translator.translate("report.header.booking.number"));
		header.addCell(pos++, translator.translate("report.header.booking.status"));
		header.addCell(pos++, translator.translate("report.header.offer"));
		header.addCell(pos++, translator.translate("report.header.offer.type"));
		header.addCell(pos++, translator.translate("report.header.cost.center"));
		header.addCell(pos++, translator.translate("report.header.account"));
		header.addCell(pos++, translator.translate("report.header.po.number"));
		header.addCell(pos++, translator.translate("report.header.order.comment"));
		header.addCell(pos++, translator.translate("report.header.org.id"));
		header.addCell(pos++, translator.translate("report.header.organisation"));
		header.addCell(pos++, translator.translate("report.header.order.date"));
		header.addCell(pos++, translator.translate("report.header.price"));
		header.addCell(pos++, translator.translate("report.header.cancellation.fee"));
		header.addCell(pos++, translator.translate("report.header.billing.address"));
		header.addCell(pos++, translator.translate("report.header.name.company"));
		header.addCell(pos++, translator.translate("report.header.addition"));
		header.addCell(pos++, translator.translate("report.header.address.line", "1"));
		header.addCell(pos++, translator.translate("report.header.address.line", "2"));
		header.addCell(pos++, translator.translate("report.header.address.line", "3"));
		header.addCell(pos++, translator.translate("report.header.address.line", "4"));
		header.addCell(pos++, translator.translate("report.header.po.box"));
		header.addCell(pos++, translator.translate("report.header.region"));
		header.addCell(pos++, translator.translate("report.header.zip"));
		header.addCell(pos++, translator.translate("report.header.city"));
		header.addCell(pos++, translator.translate("report.header.country"));
		
		return pos;
	}

	@Override
	protected void generateData(OpenXMLWorkbook workbook, Identity coach, OpenXMLWorksheet sheet, 
								List<UserPropertyHandler> userPropertyHandlers) {

	}

	@Override
	protected List<UserPropertyHandler> getUserPropertyHandlers() {
		return CoreSpringFactory.getImpl(UserManager.class)
				.getUserPropertyHandlersFor(AbstractReportConfiguration.PROPS_IDENTIFIER, false);
	}

	@Override
	public ReportContent generateReport(Curriculum curriculum, CurriculumElement curriculumElement, 
										Identity doer, Locale locale, VFSLeaf file) {

		Translator translator = getTranslator(locale);
		List<UserPropertyHandler> userPropertyHandlers = getUserPropertyHandlers();

		List<String> worksheetNames = List.of(translator.translate("report.booking"));

		try (OutputStream out = file.getOutputStream(true);
			 OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1, worksheetNames)) {
			OpenXMLWorksheet sheet = workbook.nextWorksheet();
			sheet.setHeaderRows(1);
			generateHeader(sheet, userPropertyHandlers, locale);
			generateData(workbook, doer, sheet, userPropertyHandlers);
		} catch (IOException e) {
			log.error("Unable to generate export", e);
			return null;
		}

		return new ReportContent(List.of(), List.of());
	}
}
