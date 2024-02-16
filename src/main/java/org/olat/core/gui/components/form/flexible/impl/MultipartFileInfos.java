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
package org.olat.core.gui.components.form.flexible.impl;

import java.io.File;

import org.olat.core.gui.util.CSSHelper;

/**
 * 
 * Initial date: 03.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public record MultipartFileInfos(File file, String fileName, long size, String contentType) {
	
	public boolean isEmpty() {
		return file == null || !file.exists() || file.length() <= 0;
	}
	
	public boolean exists() {
		return file != null && file.exists();
	}
	
	public String iconCssClass() {
		return CSSHelper.createFiletypeIconCssClassFor(fileName);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(128);
		sb.append("multipartFileInfos[file=").append((file == null ? "NULL" : file.toString()))
		  .append(";fileName=").append((fileName == null ? "NULL" : fileName))
		  .append(";contentType=").append((contentType == null ? "NULL" : contentType));
		return sb.toString();
	}
}
