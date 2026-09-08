/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.resource.accesscontrol.ui;

import java.util.Date;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.pdf.PdfModule;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormSurvey;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.manager.EvaluationFormExportResource;
import org.olat.modules.forms.manager.EvaluationFormExportResource.FormExportInfos;
import org.olat.modules.forms.model.xml.FileUpload;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.ui.EvaluationFormExcelExport;

/**
 * Creates the export of offer surveys: the Excel export alone if there is
 * nothing else to export, otherwise a zip with the Excel exports, the uploaded
 * files and the PDF of every session.
 *
 * Initial date: 8 Sep 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OfferSurveyExportFactory {

	private OfferSurveyExportFactory() {
		//
	}
	
	public static MediaResource createExport(Identity doer, WindowControl wControl, Translator translator,
			String exportName, List<SurveyExportInfos> surveyExportInfos) {
		PdfModule pdfModule = CoreSpringFactory.getImpl(PdfModule.class);
		if (surveyExportInfos.size() == 1 && !pdfModule.isEnabled() && !hasFileUpload(surveyExportInfos.get(0).form())) {
			return surveyExportInfos.get(0).excelExport().createMediaResource();
		}
		
		List<FormExportInfos> exportInfos = surveyExportInfos.stream()
				.map(infos -> new FormExportInfos(infos.excelExport(), infos.form(), infos.filter(), infos.path(),
						false, new OfferSurveyPrintProvider(infos.survey(), translator)))
				.toList();
		
		String fileName = StringHelper.transformDisplayNameToFileSystemName(exportName)
				+ "_" + Formatter.formatDatetimeFilesystemSave(new Date()) + ".zip";
		return new EvaluationFormExportResource(wControl, doer, fileName, exportInfos);
	}
	
	private static boolean hasFileUpload(Form form) {
		EvaluationFormManager evaluationFormManager = CoreSpringFactory.getImpl(EvaluationFormManager.class);
		return evaluationFormManager.getUncontainerizedElements(form).stream().anyMatch(FileUpload.class::isInstance);
	}
	
	/**
	 * @param path Folder inside the zip, empty for the root of the zip.
	 */
	public record SurveyExportInfos(EvaluationFormSurvey survey, Form form, SessionFilter filter,
			EvaluationFormExcelExport excelExport, String path) {
	}

}
