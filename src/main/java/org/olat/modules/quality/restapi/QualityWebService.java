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
package org.olat.modules.quality.restapi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.core.StreamingOutput;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nModule;
import org.olat.modules.forms.EvaluationFormManager;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.Figures;
import org.olat.modules.forms.SessionFilter;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.ui.LegendNameGenerator;
import org.olat.modules.forms.ui.NameShuffleAnonymousComparator;
import org.olat.modules.forms.ui.ReportHelper;
import org.olat.modules.forms.ui.ReportHelperUserColumns;
import org.olat.modules.forms.ui.SessionInformationLegendNameGenerator;
import org.olat.modules.quality.analysis.AnalysisPresentation;
import org.olat.modules.quality.analysis.AnlaysisFigures;
import org.olat.modules.quality.analysis.EvaluationFormView;
import org.olat.modules.quality.analysis.EvaluationFormViewSearchParams;
import org.olat.modules.quality.analysis.QualityAnalysisService;
import org.olat.modules.quality.analysis.ui.AnalysisController;
import org.olat.modules.quality.analysis.ui.AnalysisExcelExport;
import org.olat.modules.quality.analysis.ui.FiguresFactory;
import org.olat.modules.quality.ui.QualityUIFactory;
import org.olat.modules.quality.ui.security.MainSecurityCallback;
import org.olat.modules.quality.ui.security.QualitySecurityCallbackFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.restapi.security.RestSecurityHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * Initial date: Sep 16, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
@Tag(name = "Quality management")
@Component
@Path("qm/analysis")
public class QualityWebService {
	
	private static final Logger log = Tracing.createLoggerFor(QualityWebService.class);
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

	@Autowired
	private QualityAnalysisService analysisService;
	@Autowired
	private EvaluationFormManager evaluationFormManager;
	
	@GET
	@Path("{formEntryKey}")
	@Operation(summary = "Get the excel report of a form", description = "Get the excel report of a form. The report only contains the answers to which the user has access according to his rights.")
	@ApiResponse(responseCode = "200", description = "The excel file", content = {
			@Content(mediaType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet") })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@Produces({ "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", MediaType.APPLICATION_OCTET_STREAM })
	public Response getReport(
			@PathParam("formEntryKey") Long formEntryKey,
			@QueryParam("startDate") @Parameter(description = "Filter data collections started after this date. Format: yyyy-MM-dd") String startDateStr,
			@QueryParam("endDate") @Parameter(description = "Filter data collections ended before this date. Format: yyyy-MM-dd") String endDateStr,
			@Context HttpServletRequest httpRequest) {
		
		MainSecurityCallback secCallback = getSecCallback(httpRequest);
		if(!secCallback.canViewAnalysis()) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		EvaluationFormViewSearchParams searchParams = new EvaluationFormViewSearchParams();
		searchParams.setOrganisationRefs(secCallback.getViewAnalysisOrganisationRefs());
		searchParams.setFormEntryKeys(List.of(formEntryKey));
		List<EvaluationFormView> forms = analysisService.loadEvaluationForms(searchParams);
		
		if (forms.size() != 1) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		EvaluationFormView formView = forms.get(0);
		
		// Have to be the same as in AnalysisController
		Locale locale = I18nModule.getDefaultLocale();
		Translator translator = Util.createPackageTranslator(AnalysisController.class, locale);
		translator = Util.createPackageTranslator(QualityUIFactory.class, locale, translator);
		
		AnalysisPresentation presentation = analysisService.createPresentation(formView.getFormEntry(), secCallback.getViewAnalysisOrganisationRefs());
		Date startDate = StringHelper.containsNonWhitespace(startDateStr) ? parseDate(startDateStr) : null;
		startDate = DateUtils.getStartOfDay(startDate);
		Date endDate = StringHelper.containsNonWhitespace(endDateStr) ? parseDate(endDateStr) : null;
		endDate = DateUtils.getEndOfDay(endDate);
		presentation.getSearchParams().setDateRangeFrom(startDate);
		presentation.getSearchParams().setDateRangeTo(endDate);
		
		RepositoryEntry formEntry = presentation.getFormEntry();
		Form form = evaluationFormManager.loadForm(formEntry);
		SessionFilter reportSessionFilter = analysisService.createSessionFilter(presentation.getSearchParams());
		
		Comparator<EvaluationFormSession> comparator = new NameShuffleAnonymousComparator();
		LegendNameGenerator legendNameGenerator = new SessionInformationLegendNameGenerator(reportSessionFilter);
		ReportHelper reportHelper = ReportHelper.builder(locale)
				.withLegendNameGenrator(legendNameGenerator)
				.withSessionComparator(comparator)
				.withColors()
				.build();
		
		AnlaysisFigures analyticFigures = analysisService.loadFigures(presentation.getSearchParams());
		Figures analysisFigures = FiguresFactory.createFigures(translator, formEntry, analyticFigures, false);
		AnalysisExcelExport export = new AnalysisExcelExport(locale, formEntry, form, reportSessionFilter, comparator,
				new ReportHelperUserColumns(reportHelper, translator), "survey", analysisFigures);
		
		StreamingOutput stream = export::createWorkbook;
		
		return Response.ok(stream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
				.header("Content-Disposition", "attachment; filename=\"" + export.getFileName() + "\"")
				.build();
	}

	private MainSecurityCallback getSecCallback(HttpServletRequest request) {
		try {
			Roles roles = RestSecurityHelper.getRoles(request);
			Identity identity = RestSecurityHelper.getIdentity(request);
			if (roles != null && identity != null) {
				return QualitySecurityCallbackFactory.createMainSecurityCallback(roles, identity);
			}
		} catch (Exception e) {
			//
		}
		return QualitySecurityCallbackFactory.createForbiddenSecurityCallback();
	}
	
	public Date parseDate(String date) {
		if(!StringHelper.containsNonWhitespace(date)) {
			return null;
		}
		
		try {
			synchronized(dateFormat) {
				return dateFormat.parse(date);
			}
		} catch (ParseException e) {
			log.debug("", e);
			return null;
		}
	}
	
}
