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
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.modules.webFeed;

/**
 * The interface defines permission levels for the access to a feed.
 * 
 * <P>
 * Initial Date: Jun 10, 2009 <br>
 * 
 * @author gwassmann
 */
public interface FeedSecurityCallback {
	/**
	 * @return Whether the feed metadata may be edited or not
	 */
	boolean mayEditMetadata();

	/**
	 * @return Whether items may be created or not
	 */
	boolean mayCreateItems();

	/**
	 * @return Whether items may be edited or not
	 */
	boolean mayEditItems();

	/**
	 * @return Whether items may be deleted or not
	 */
	boolean mayDeleteItems();
	
	/**
	 * @return If the user can view all drafts
	 */
	//fxdiff BAKS-18
	boolean mayViewAllDrafts();
}
