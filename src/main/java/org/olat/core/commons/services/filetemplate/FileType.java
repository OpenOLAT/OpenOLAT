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
package org.olat.core.commons.services.filetemplate;

/**
 * 
 * Initial date: 19 Mar 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FileType {
	
	private final String suffix;
	private final String name;
	private final ContentProvider contentProvider;
	
	public static FileType of(String suffix, String name, ContentProvider contentProvider) {
		return new FileType(suffix, name, contentProvider);
	}
	
	private FileType(String suffix, String name, ContentProvider contentProvider) {
		this.suffix = suffix;
		this.name = name;
		this.contentProvider = contentProvider;
	}

	public String getSuffix() {
		return suffix;
	}

	public String getName() {
		return name;
	}

	public ContentProvider getContentProvider() {
		return contentProvider;
	}

}
