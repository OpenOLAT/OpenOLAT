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

import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.olat.restapi.system.vo.IndexerStatisticsVO;
import org.olat.restapi.system.vo.IndexerStatus;
import org.olat.search.SearchServiceStatus;
import org.olat.search.service.SearchServiceFactory;
import org.olat.search.service.SearchServiceStatusImpl;
import org.olat.search.service.indexer.FullIndexerStatus;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class IndexerWebService {
	
	/**
	 * Return the statistics about the indexer
	 * 
	 * @return The statistics about the indexer
	 */
	@GET
	@Operation(summary = "Return the statistics about the indexer", description = "Return the statistics about the indexer")
	@ApiResponse(responseCode = "200", description = "Statistics about the indexer", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = IndexerStatisticsVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = IndexerStatisticsVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getStatistics() {
		IndexerStatisticsVO stats = getIndexerStatistics();
		return Response.ok(stats).build();
	}
	
	/**
	 * Return the status of the indexer: running, stopped
	 * 
	 * @return The status of the indexer
	 */
	@GET
	@Path("status")
	@Operation(summary = "Return the status of the indexer", description = "Return the status of the indexer: running, stopped")
	@ApiResponse(responseCode = "200", description = "he status of the indexer")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getStatus() {
		String status;
		SearchServiceStatus serviceStatus = SearchServiceFactory.getService().getStatus();
		if(serviceStatus instanceof SearchServiceStatusImpl) {
			status = serviceStatus.getStatus();
		} else {
			status = FullIndexerStatus.STATUS_DISABLED;
		}
		return Response.ok(new IndexerStatus(status)).build();
	}
	
	/**
	 * Return the status of the indexer: running, stopped
	 * 
	 * @return The status of the indexer
	 */
	@GET
	@Path("status")
	@Operation(summary = "Return the status of the indexer", description = "Return the status of the indexer: running, stopped")
	@ApiResponse(responseCode = "200", description = "The status of the indexer")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.TEXT_PLAIN})
	public Response getPlainTextStatus() {
		String status;
		SearchServiceStatus serviceStatus = SearchServiceFactory.getService().getStatus();
		if(serviceStatus instanceof SearchServiceStatusImpl) {
			status = serviceStatus.getStatus();
		} else {
			status = FullIndexerStatus.STATUS_DISABLED;
		}
		return Response.ok(status).build();
	}
	
	/**
	 * Update the status of the indexer: running, stopped.
	 * Running start the indexer, stopped, stop it.
	 * 
	 * @return The status of the indexer
	 */
	@POST
	@Path("status")
	@Operation(summary = "Update the status of the indexer", description = "Update the status of the indexer: running, stopped.\n" + 
			" Running start the indexer, stopped, stop it.")
	@ApiResponse(responseCode = "200", description = "The status has changed")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	public Response setStatus(@FormParam("status") String status) {
		if(FullIndexerStatus.STATUS_RUNNING.equals(status)) {
			SearchServiceFactory.getService().startIndexing();
		} else if(FullIndexerStatus.STATUS_STOPPED.equals(status)) {
			SearchServiceFactory.getService().stopIndexing();
		}
		return Response.ok().build();
	}
	
	protected IndexerStatisticsVO getIndexerStatistics() {
		IndexerStatisticsVO stats = new IndexerStatisticsVO();

		SearchServiceStatus status = SearchServiceFactory.getService().getStatus();
		if(status instanceof SearchServiceStatusImpl) {
			SearchServiceStatusImpl statusImpl = (SearchServiceStatusImpl)status;
			FullIndexerStatus fStatus = statusImpl.getFullIndexerStatus();
			FullIndexerStatus lStatus = statusImpl.getLifeIndexerStatus();

			stats.setIndexedDocumentCount(fStatus.getDocumentCount() + (long)lStatus.getDocumentCount());
			stats.setExcludedDocumentCount(fStatus.getExcludedDocumentCount() + (long)lStatus.getExcludedDocumentCount());
			stats.setIndexSize(fStatus.getIndexSize() + (long)lStatus.getIndexSize());
			stats.setIndexingTime(fStatus.getIndexingTime() + lStatus.getIndexingTime());
			stats.setFullIndexStartedAt(fStatus.getFullIndexStartedAt());
			stats.setDocumentQueueSize(fStatus.getDocumentQueueSize() + (long)lStatus.getDocumentQueueSize());
			stats.setRunningFolderIndexerCount(fStatus.getNumberRunningFolderIndexer() + (long)lStatus.getNumberRunningFolderIndexer());
			stats.setAvailableFolderIndexerCount(fStatus.getNumberAvailableFolderIndexer() + (long)lStatus.getNumberAvailableFolderIndexer());
			stats.setLastFullIndexTime(fStatus.getLastFullIndexDateString());
			stats.setStatus(status.getStatus());
		} else {
			stats.setStatus(FullIndexerStatus.STATUS_DISABLED);
		}
		return stats;
	}
}
