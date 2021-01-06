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
package org.olat.course.nodes.livestream;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;

import org.olat.core.id.Identity;
import org.olat.course.nodes.cal.CourseCalendars;
import org.olat.course.nodes.livestream.model.UrlTemplate;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 28 May 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface LiveStreamService {
	
	ScheduledExecutorService getScheduler();
	
	List<? extends LiveStreamEvent> getRunningEvents(CourseCalendars calendars, int bufferBeforeMin,
			int bufferAfterMin);
	
	List<? extends LiveStreamEvent> getRunningAndPastEvents(CourseCalendars calendars, int bufferBeforeMin);

	List<? extends LiveStreamEvent> getUpcomingEvents(CourseCalendars calendars, int bufferBeforeMin);
	
	/**
	 * Create a new launch of a course node by the identity.
	 *
	 * @param courseEntry
	 * @param subIdent
	 * @param identity
	 */
	void createLaunch(RepositoryEntry courseEntry, String subIdent, Identity identity);

	/**
	 * Get the number of unique launchers (viewers) of the live stream event.
	 * 
	 * @param courseEntry
	 * @param subIdent
	 * @param from
	 * @param to
	 * @return
	 */
	Long getLaunchers(RepositoryEntryRef courseEntry, String subIdent, Date from, Date to);
	
	UrlTemplate createUrlTemplate(String name);
	
	UrlTemplate updateUrlTemplate(UrlTemplate urlTemplate);

	List<UrlTemplate> getAllUrlTemplates();

	UrlTemplate getUrlTemplate(Long key);
	
	void deleteUrlTemplate(UrlTemplate urlTemplate);
	
	/**
	 * Concatenate the urls of the template
	 *
	 * @param urlTemplate
	 * @return
	 */
	String concatUrls(UrlTemplate urlTemplate);

	/**
	 * Split the url to separate urls
	 *
	 * @param url
	 * @return
	 */
	String[] splitUrl(String url);

	/**
	 * Checks whether at the urls are currently streaming and returns the streaming urls
	 *
	 * @param urls
	 * @return
	 */
	String[] getStreamingUrls(String[] urls);
	
}
