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

package org.olat.restapi.system;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.doceditor.DocEditorService;
import org.olat.restapi.system.vo.DocEditorStatisticsVO;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * Description:<br>
 * This serves information about the document editors.
 * 
 * Initial date: 09 Jul. 2020<br>
 * @author morjen, moritz.jenny@frentix.com, http://www.frentix.com
 *
 */

@Component
public class DocEditorSessionWebService {

	@GET
	@Path("{editorType}")
	@Operation(summary = "Returns information about doceditor", description = "Returns information about the doceditor given as parameter")
	@ApiResponse(responseCode = "200", description = "Information about the doceditor", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = DocEditorStatisticsVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = DocEditorStatisticsVO.class)) })
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getStatistics(@PathParam("editorType") String editorType) {
		DocEditorStatisticsVO stats = getDocEditorStatistics(editorType);
		return Response.ok(stats).build();
	}
	
	private DocEditorStatisticsVO getDocEditorStatistics(String editorType) {
		DocEditorStatisticsVO stats = new DocEditorStatisticsVO();
		DocEditorService docEditorService = CoreSpringFactory.getImpl(DocEditorService.class);
		stats.setAppName(editorType);
		stats.setOpenDocumentsWrite(docEditorService.getAccessCount(editorType, Mode.EDIT));
		stats.setOpenDocumentsRead(docEditorService.getAccessCount(editorType, Mode.VIEW));
		return stats;
	}

}
