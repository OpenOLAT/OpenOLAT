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
import org.olat.modules.curriculum.ui.member.AbstractMembersController;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 3 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class DummyBookingReportConfiguration extends AbstractReportConfiguration implements CurriculumReportConfiguration {

	@Override
	protected Translator getTranslator(Locale locale) {
		return Util.createPackageTranslator(CurriculumManagerRootController.class, locale, super.getTranslator(locale));
	}
	
	@Override
	protected String getI18nCategoryKey() {
		return "report.booking";
	}
	
	@Override
	protected List<UserPropertyHandler> getUserPropertyHandlers() {
		return CoreSpringFactory.getImpl(UserManager.class).getUserPropertyHandlersFor(AbstractMembersController.usageIdentifyer, false);
	}

	@Override
	protected int generateCustomHeaderColumns(Row header, int pos, Translator translator) {
		return pos;
	}

	@Override
	protected void generateData(OpenXMLWorkbook workbook, Identity coach, OpenXMLWorksheet sheet,
			List<UserPropertyHandler> userPropertyHandlers) {
		//
	}

	@Override
	public boolean hasAccess(ReportConfigurationAccessSecurityCallback secCallback) {
		return true;
	}

	@Override
	public ReportContent generateReport(Curriculum curriculum, CurriculumElement curriculumElement, Identity doer, Locale locale,
			VFSLeaf file) {

		List<UserPropertyHandler> userPropertyHandlers = getUserPropertyHandlers();

		try (OutputStream out = file.getOutputStream(true);
			 OpenXMLWorkbook workbook = new OpenXMLWorkbook(out, 1)) {
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
