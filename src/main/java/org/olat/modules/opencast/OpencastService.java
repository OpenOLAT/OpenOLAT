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
package org.olat.modules.opencast;

import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.util.UserSession;

/**
 * 
 * Initial date: 4 Aug 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface OpencastService {

	/**
	 * Check if the connection to Opencast can be established with the url and credentials of the OpencastModul.
	 *
	 * @return true if the connection was successfully established.
	 */
	boolean checkApiConnection();

	/**
	 * Get the event.
	 *
	 * @param identifier
	 * @return
	 */
	OpencastEvent getEvent(String identifier);
	
	/**
	 * Get all events of the corresponding authDelegate.
	 *
	 * @param identifier
	 * @return
	 */
	List<OpencastEvent> getEvents(AuthDelegate authDelegate);
	
	/**
	 * Get the events with the metadata in the bibliographic data
	 *
	 * @param metadata
	 * @param publishedOnly only get events which are published on the presentation service
	 * @return
	 */
	List<OpencastEvent> getEvents(String metadata, boolean publishedOnly);
	
	/**
	 * Delete all events with the identifier.
	 *
	 * @param identifier
	 * @return true if some event was deleted
	 */
	boolean deleteEvents(String identifier);
	
	/**
	 * Get the series.
	 *
	 * @param identifier
	 * @return
	 */
	OpencastSeries getSeries(String identifier);
	
	/**
	 * Get all series of the corresponding authDelegate.
	 *
	 * @param authDelegate
	 * @return
	 */
	List<OpencastSeries> getSeries(AuthDelegate authDelegate);
	
	AuthDelegate getAuthDelegate(Identity identity);

	String getUserId(Identity identity);

	/**
	 * 
	 *
	 * @param usess
	 * @param identifier
	 * @param roles
	 * @return
	 */
	String getLtiEventMapperUrl(UserSession usess, String identifier, String roles);

	/**
	 * 
	 *
	 * @param userSession
	 * @param opencastSeries
	 * @param roles
	 * @return
	 */
	String getLtiSeriesMapperUrl(UserSession userSession, OpencastSeries opencastSeries, String roles);

}
