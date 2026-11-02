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
package org.olat.course.disclaimer.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getIdentity;
import static org.olat.restapi.security.RestSecurityHelper.getLocale;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.course.CourseModule;
import org.olat.course.config.CourseConfigLockedException;
import org.olat.course.disclaimer.ConsentHandling;
import org.olat.course.disclaimer.CourseDisclaimerManager;
import org.olat.course.disclaimer.model.CourseDisclaimerError;
import org.olat.course.disclaimer.model.CourseDisclaimerSettings;
import org.olat.course.disclaimer.ui.CourseDisclaimerController;
import org.olat.repository.RepositoryEntry;
import org.olat.restapi.support.vo.ErrorVO;
import org.springframework.beans.factory.annotation.Autowired;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Read and update the disclaimer configuration of a course (terms of use
 * and data protection declaration). Same rules as the course settings.
 * 
 * Initial date: 2 nov. 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
@Tag(name = "Course - Disclaimer")
public class CourseDisclaimerWebService {

	private final RepositoryEntry courseEntry;
	private final boolean administrator;

	@Autowired
	private CourseModule courseModule;
	@Autowired
	private CourseDisclaimerManager courseDisclaimerManager;

	public CourseDisclaimerWebService(RepositoryEntry courseEntry, boolean administrator) {
		this.courseEntry = courseEntry;
		this.administrator = administrator;
	}

	/**
	 * Return the disclaimer configuration of the course.
	 *
	 * @return The disclaimer configuration
	 */
	@GET
	@Operation(summary = "Return the disclaimer configuration of the course",
		description = "Return the terms of use and the data protection declaration of the course, the number of consents and if the course disclaimers are enabled in the system configuration")
	@ApiResponse(responseCode = "200", description = "The disclaimer configuration", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = CourseDisclaimerVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = CourseDisclaimerVO.class)) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getDisclaimer() {
		if(!administrator) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		CourseDisclaimerSettings settings = courseDisclaimerManager.getSettings(courseEntry);
		return Response.ok(toVO(settings)).build();
	}

	/**
	 * Update the disclaimer configuration of the course.
	 *
	 * @param disclaimerVo The new configuration
	 * @param consents What happens with the existing consents: keep, revoke or remove
	 * @param httpRequest The HTTP request
	 * @return The saved configuration
	 */
	@PUT
	@Operation(summary = "Update the disclaimer configuration of the course",
		description = "Update the terms of use and the data protection declaration of the course. A text which is null is not changed. "
				+ "The title, terms and label of a disabled text are not changed. The terms are filtered from unsafe HTML.")
	@ApiResponse(responseCode = "200", description = "The saved disclaimer configuration", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = CourseDisclaimerVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = CourseDisclaimerVO.class)) })
	@ApiResponse(responseCode = "400", description = "The configuration is not valid, the consents value is unknown or the consents cannot be removed because a text is enabled", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ErrorVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = ErrorVO.class))) })
	@ApiResponse(responseCode = "403", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The course not found")
	@ApiResponse(responseCode = "409", description = "The course is opened in the course editor")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putDisclaimer(CourseDisclaimerVO disclaimerVo,
			@Parameter(description = "What happens with the existing consents: keep (default), revoke (the participants must accept the texts again) or remove (only if both texts are disabled)")
			@QueryParam("consents") @DefaultValue("keep") String consents,
			@Context HttpServletRequest httpRequest) {
		if(!administrator) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		if(disclaimerVo == null) {
			return Response.serverError().status(Status.BAD_REQUEST).build();
		}

		ConsentHandling consentHandling = ConsentHandling.secureValueOf(consents, null);
		if(consentHandling == null) {
			ErrorVO[] errorVos = new ErrorVO[] {
				new ErrorVO("consents", "Unknown value for consents: " + FilterFactory.getHtmlTagsFilter().filter(consents) + ". Allowed are keep, revoke or remove.")
			};
			return Response.ok(errorVos).status(Status.BAD_REQUEST).build();
		}

		CourseDisclaimerSettings settings = courseDisclaimerManager.getSettings(courseEntry);
		if(disclaimerVo.getTermsOfUse() != null) {
			settings.setTermsOfUse(disclaimerVo.getTermsOfUse().toDisclaimer(settings.getTermsOfUse()));
		}
		if(disclaimerVo.getDataProtection() != null) {
			settings.setDataProtection(disclaimerVo.getDataProtection().toDisclaimer(settings.getDataProtection()));
		}

		List<CourseDisclaimerError> errors = courseDisclaimerManager.validate(settings, consentHandling);
		if(!errors.isEmpty()) {
			return Response.ok(toErrorVOs(errors, getLocale(httpRequest))).status(Status.BAD_REQUEST).build();
		}

		try {
			CourseDisclaimerSettings savedSettings = courseDisclaimerManager
					.updateSettings(courseEntry, settings, consentHandling, getIdentity(httpRequest));
			return Response.ok(toVO(savedSettings)).build();
		} catch (CourseConfigLockedException e) {
			return Response.serverError().status(Status.CONFLICT).build();
		}
	}

	private CourseDisclaimerVO toVO(CourseDisclaimerSettings settings) {
		Long consentCount = courseDisclaimerManager.countConsents(courseEntry);
		return CourseDisclaimerVO.valueOf(settings, consentCount, courseModule.isDisclaimerEnabled());
	}

	private ErrorVO[] toErrorVOs(List<CourseDisclaimerError> errors, Locale locale) {
		Translator translator = Util.createPackageTranslator(CourseDisclaimerController.class, locale);
		List<ErrorVO> errorVos = new ArrayList<>(errors.size());
		for(CourseDisclaimerError error:errors) {
			errorVos.add(toErrorVO(error, translator));
		}
		return errorVos.toArray(new ErrorVO[errorVos.size()]);
	}

	private ErrorVO toErrorVO(CourseDisclaimerError error, Translator translator) {
		if(error.type() == CourseDisclaimerError.Type.REMOVE_NOT_ALLOWED) {
			return new ErrorVO("consents", "The consents can only be removed if the terms of use and the data protection declaration are disabled.");
		}

		String text = error.disclaimer() == 1 ? "termsOfUse" : "dataProtection";
		String code = text + "." + error.field() + ":" + error.type().name().toLowerCase();
		String message;
		if(error.type() == CourseDisclaimerError.Type.TOO_LONG) {
			message = translator.translate("input.toolong", Integer.toString(error.maxLength()));
		} else {
			message = translator.translate("form.legende.mandatory");
		}
		String translation = translator.translate("course.disclaimer." + error.disclaimer())
				+ " / " + translator.translate(toFieldI18nKey(error.field()))
				+ ": " + FilterFactory.getHtmlTagAndDescapingFilter().filter(message);
		return new ErrorVO(code, translation);
	}

	private String toFieldI18nKey(String field) {
		return switch(field) {
			case "title" -> "course.disclaimer.title";
			case "terms" -> "course.disclaimer.terms";
			case "label2" -> "course.disclaimer.label.2";
			default -> "course.disclaimer.label.1";
		};
	}
}
