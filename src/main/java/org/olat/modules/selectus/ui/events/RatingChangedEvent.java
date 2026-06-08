/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.events;

import org.olat.core.util.event.MultiUserEvent;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.application.ApplicationRefImpl;

/**
 * 
 * Initial date: 26 sept. 2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RatingChangedEvent extends MultiUserEvent  {

	private static final long serialVersionUID = 1155584122345848263L;
	public static final String RATING_CHANGED = "rating-changed"; 
	
	private final ApplicationRef application;
	private final Long doerIdentityKey;
	private final String emitter;
	
	public RatingChangedEvent(ApplicationRef application, Long doerIdentityKey, String emitter) {
		super(RATING_CHANGED);
		this.application = new ApplicationRefImpl(application.getKey());
		this.doerIdentityKey = doerIdentityKey;
		this.emitter = emitter;
	}
	
	public ApplicationRef getApplication() {
		return application;
	}
	
	public Long getDoerIdentityKey() {
		return doerIdentityKey;
	}
	
	public String getEmitter() {
		return emitter;
	}
}
