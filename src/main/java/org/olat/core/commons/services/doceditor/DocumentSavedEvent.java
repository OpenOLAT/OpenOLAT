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
package org.olat.core.commons.services.doceditor;

import org.olat.core.util.event.MultiUserEvent;

/**
 * 
 * Initial date: 27 Apr 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class DocumentSavedEvent extends MultiUserEvent {
	
	private static final long serialVersionUID = 8912260605953132188L;
	
	private final Long accessKey;
	private final Long vfsMetadatKey;
	private final Long identityKey;

	public DocumentSavedEvent(Long accessKey, Long vfsMetadatKey, Long identityKey) {
		super("doc-comtent-changed");
		this.accessKey = accessKey;
		this.vfsMetadatKey = vfsMetadatKey;
		this.identityKey = identityKey;
	}
	
	public Long getAccessKey() {
		return accessKey;
	}
	
	public Long getVfsMetadatKey() {
		return vfsMetadatKey;
	}
	
	public Long getIdentityKey() {
		return identityKey;
	}

}
