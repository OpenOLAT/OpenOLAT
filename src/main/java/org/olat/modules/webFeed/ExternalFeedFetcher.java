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
package org.olat.modules.webFeed;

import java.util.List;

import org.olat.modules.webFeed.manager.ValidatedURL;

/**
 * An ExternalFeedFetcher allows to retrieve feeds and items from
 * external web sites. It is responsible for the http communication
 * and the convertation to the internal feed model.
 *
 * Initial date: 12.05.2017<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface ExternalFeedFetcher {

	/**
	 * Fetches an feed from an external web sites and updates the feed with
	 * the current attributes
	 *
	 * @param feed
	 * @return the updated feed
	 */
	public Feed fetchFeed(Feed feed);

	/**
	 * Fetches the items of a feed and converts it to the internal feed model.
	 * @param feed
	 * @return the fetched items
	 */
	public List<Item> fetchItems(Feed feed);

	/**
	 * Validates if it is a valid feed URL and if the feed can be fetch from
	 * this URL.
	 *
	 * @param url
	 * @param enclosuresExpected
	 *            Indicates whether enclosures are expected e.g. in a podcast.
	 */
	public ValidatedURL validateFeedUrl(String url, boolean enclosuresExpected);

}
