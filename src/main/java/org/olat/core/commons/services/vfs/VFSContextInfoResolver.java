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
package org.olat.core.commons.services.vfs;

import java.util.Locale;

/**
 * 
 * Initial date: 16 Jan 2020<br>
 * 
 * @author gnaegi, gnaegi@frentix.com, http://www.frentix.com
 *
 */
public interface VFSContextInfoResolver {

	/**
	 * Resolve the localized type name for a give file path. This method is "light
	 * weight" without any db access, resolving is done by applying some regexp on
	 * the path.
	 * 
	 * @param VFSMetadataRelativePath the relative file path
	 * @param locale                  The user locale
	 * @return The localized type name or NULL if not resolvable by this resolver
	 */
	public String resolveContextTypeName(String VFSMetadataRelativePath, Locale locale);

	
	/**
	 * Resolve the localized context info for a give file path. This method will do
	 * db queries to build the context info.
	 * 
	 * @param VFSMetadataRelativePath the relative file path
	 * @param locale The user locale
	 * @return The localized context info or NULL if not resolvable by this resolver
	 */
	public VFSContextInfo resolveContextInfo(String VFSMetadataRelativePath, Locale locale);

}
