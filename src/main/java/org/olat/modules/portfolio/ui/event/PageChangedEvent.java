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
package org.olat.modules.portfolio.ui.event;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.util.event.MultiUserEvent;

/**
 * 
 * Initial date: 2 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageChangedEvent extends MultiUserEvent {

	private static final long serialVersionUID = -6099560236380196678L;

	public static final String PAGE_CHANGED = "portfolio-page-changed";
	
	private final Long senderIdentityKey;
	private final Long pageKey;
	
	public PageChangedEvent(Long senderIdentityKey, Long pageKey) {
		super(PAGE_CHANGED);
		this.senderIdentityKey = senderIdentityKey;
		this.pageKey = pageKey;
	}

	public Long getSenderIdentityKey() {
		return senderIdentityKey;
	}

	public Long getPageKey() {
		return pageKey;
	}
	
	public boolean isMe(IdentityRef me) {
		return senderIdentityKey != null && senderIdentityKey.equals(me.getKey());
	}
}
