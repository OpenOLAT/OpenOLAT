/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.search.service.indexer;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.core.logging.Tracing;
import org.olat.search.SearchService;
import org.olat.search.service.SearchServiceFactory;
import org.olat.search.service.SearchServiceImpl;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.InterruptableJob;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.UnableToInterruptJobException;

/**
 * Description:<br>
 * job that starts search indexing process.
 * 
 * <P>
 * Initial Date:  09.09.2008 <br>
 * @author Christian Guretzki
 */
@DisallowConcurrentExecution
public class SearchIndexingJob extends JobWithDB implements InterruptableJob {

	private static final Logger log = Tracing.createLoggerFor(SearchIndexingJob.class);

	@Override
	public void executeWithDB(JobExecutionContext arg0) throws JobExecutionException {
		log.info("Search indexer started via cronjob.");
		SearchService searchService = SearchServiceFactory.getService();
		if(searchService instanceof SearchServiceImpl) {	
			((SearchServiceImpl)searchService).getInternalIndexer().startFullIndex();
		}
	}

	@Override
	public void interrupt() throws UnableToInterruptJobException {
		log.info("Interrupt indexer via quartz.");
		SearchService searchService = SearchServiceFactory.getService();
		if(searchService instanceof SearchServiceImpl) {	
			((SearchServiceImpl)searchService).getInternalIndexer().stopFullIndex();
		}
	}
}
