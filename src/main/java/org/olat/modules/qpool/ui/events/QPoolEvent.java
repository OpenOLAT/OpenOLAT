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
package org.olat.modules.qpool.ui.events;

import org.olat.core.util.event.MultiUserEvent;

/**
 * 
 * Initial date: 22.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QPoolEvent extends MultiUserEvent {

	private static final long serialVersionUID = -2848823902400095710L;
	
	public static final String ITEM_SHARED = "qpool-item-shared";
	public static final String ITEM_MARKED = "qpool-item-marked";
	public static final String ITEM_CREATED = "qpool-item-created";
	public static final String ITEM_DELETED = "qpool-item-deleted";
	public static final String ITEM_STATUS_CHANGED = "qpool-item-status-changed";
	public static final String ITEMS_RELOADED = "qpool-items-reloaded";
	public static final String COLL_CREATED = "qpool-coll-created";
	public static final String COLL_DELETED = "qpoll-coll-deleted";
	public static final String COLL_CHANGED = "qpoll-coll-changed";
	public static final String POOL_CREATED = "qpool-pool-created";
	public static final String POOL_DELETED = "qpool-pool-deleted";
	public static final String BULK_CHANGE = "qpool-bulk-change";
	public static final String EDIT = "edit";

	private Long objectKey;

	public QPoolEvent(String cmd) {
		super(cmd);
	}
	
	public QPoolEvent(String cmd, Long objectKey) {
		super(cmd);
		this.objectKey = objectKey;
	}

	public Long getObjectKey() {
		return objectKey;
	}

	public void setObjectKey(Long objectKey) {
		this.objectKey = objectKey;
	}
}
