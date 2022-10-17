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
package org.olat.core.dispatcher.mapper;

import java.io.Serializable;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.core.gui.media.MediaResource;

/**
 * !DO not implement hashCode and equals, it mimics the common state
 * of the real mappers which haven't any equals implemented.
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class PersistentMapper implements Mapper, Serializable {
	
	private static final long serialVersionUID = -4318331557447328475L;
	private String key;
	
	public PersistentMapper(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}

	@Override
	public MediaResource handle(String relPath, HttpServletRequest request) {
		return null;
	}
}
