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
package org.olat.modules.catalog.model;

import org.olat.course.certificate.RepositoryEntryCertificateConfiguration;
import org.olat.modules.creditpoint.RepositoryEntryCreditPointConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 18 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public record RepositoryEntryInfos(RepositoryEntry entry,
		RepositoryEntryCertificateConfiguration certificateConfiguration,
		RepositoryEntryCreditPointConfiguration creditPointConfiguration) implements RepositoryEntryRef {

	@Override
	public Long getKey() {
		return entry.getKey();
	}
	
	public OLATResource getOlatResource() {
		return entry.getOlatResource();
	}
}
