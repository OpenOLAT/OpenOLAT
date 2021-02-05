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

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;

import org.olat.core.commons.services.doceditor.DocEditor.Mode;
import org.olat.core.commons.services.vfs.VFSMetadata;

/**
 * 
 * Initial date: 28 Jan 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AccessSearchParams {
	
	private Long identityKey;
	private Collection<Long> metadataKeys;
	private String editorType;
	private Mode mode;
	private boolean fetch;
	
	public Long getIdentityKey() {
		return identityKey;
	}

	public void setIdentityKey(Long identityKey) {
		this.identityKey = identityKey;
	}

	public Collection<Long> getMetadataKeys() {
		return metadataKeys;
	}
	
	public void setMatadatas(Collection<VFSMetadata> metadatas) {
		this.metadataKeys = metadatas.stream().map(VFSMetadata::getKey).collect(Collectors.toSet());
	}
	
	public void setMetadataKey(Long metadataKey) {
		this.metadataKeys = Collections.singletonList(metadataKey);
	}
	
	public String getEditorType() {
		return editorType;
	}

	public void setEditorType(String editorType) {
		this.editorType = editorType;
	}

	public Mode getMode() {
		return mode;
	}
	
	public void setMode(Mode mode) {
		this.mode = mode;
	}

	public boolean isFetch() {
		return fetch;
	}

	public void setFetch(boolean fetch) {
		this.fetch = fetch;
	}
	
}
