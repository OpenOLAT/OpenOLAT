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
package org.olat.modules.wopi;

import java.io.File;
import java.time.Instant;
import java.util.List;

import javax.ws.rs.core.HttpHeaders;

/**
 * 
 * Initial date: 11 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class WopiRestHelper {
	
	public static String getFirstRequestHeader(HttpHeaders httpHeaders, String headerKey) {
		List<String> requestHeader = httpHeaders.getRequestHeader(headerKey);
		return requestHeader != null && !requestHeader.isEmpty()? requestHeader.get(0): null;
	}

	public static String getLastModifiedAsIso6801(File file) {
		long lastModified = file.lastModified();
		return Instant.ofEpochMilli(lastModified).toString();
	}

}
