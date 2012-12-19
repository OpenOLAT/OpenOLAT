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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.olat.core.commons.services.search.SearchServiceStatus;
import org.olat.restapi.system.vo.IndexerStatisticsVO;
import org.olat.restapi.system.vo.IndexerStatus;
import org.olat.search.service.SearchServiceFactory;
import org.olat.search.service.SearchServiceStatusImpl;
import org.olat.search.service.indexer.FullIndexerStatus;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class IndexerWebService {
	
	/**
	 * Return the statistics about the indexer
	 * @response.representation.200.qname {http://www.example.com}releaseVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The verison of the instance
   * @response.representation.200.example {@link org.olat.restapi.system.vo.Examples#SAMPLE_OO_INDEXERSTATSVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient

	 * @return The statistics about the indexer
	 */
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getStatistics() {
		IndexerStatisticsVO stats = getIndexerStatistics();
		return Response.ok(stats).build();
	}
	
	@GET
	@Path("status")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getStatus(@Context HttpServletRequest request) {
		SearchServiceStatus serviceStatus = SearchServiceFactory.getService().getStatus();
		String status = serviceStatus.getStatus();
		return Response.ok(new IndexerStatus(status)).build();
	}
	
	@GET
	@Path("status")
	@Produces({MediaType.TEXT_PLAIN})
	public Response getPlainTextStatus(@Context HttpServletRequest request) {
		SearchServiceStatus serviceStatus = SearchServiceFactory.getService().getStatus();
		return Response.ok(serviceStatus.getStatus()).build();
	}
	
	@POST
	@Path("status")
	public Response setStatus(@FormParam("status") String status, @Context HttpServletRequest request) {
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
			stats.setIndexedDocumentCount(fStatus.getDocumentCount());
			stats.setExcludedDocumentCount(fStatus.getExcludedDocumentCount());
			stats.setIndexSize(fStatus.getIndexSize());
			stats.setIndexingTime(fStatus.getIndexingTime());
			stats.setFullIndexStartedAt(fStatus.getFullIndexStartedAt());
			stats.setDocumentQueueSize(fStatus.getDocumentQueueSize());
			stats.setRunningFolderIndexerCount(fStatus.getNumberRunningFolderIndexer());
			stats.setAvailableFolderIndexerCount(fStatus.getNumberAvailableFolderIndexer());
			stats.setLastFullIndexTime(fStatus.getLastFullIndexTime());
		}
		stats.setStatus(status.getStatus());
		return stats;
	}
}
