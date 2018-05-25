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
package org.olat.user;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 23 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface UserDataExportService {
	
	/**
	 * @return The list of services which can export user data.
	 */
	public List<String> getExporterIds();
	
	/**
	 * Delete all exports older than the specified date.
	 */
	public void deleteByDate(Date date);

	/**
	 * The method will request an export of the specified user data. The
	 * export itself is asynchronous.
	 * 
	 * @param identity The user which want its data to be exported
	 * @param eporterIds The list of service which need to export the datas
	 */
	public void requestExportData(Identity identity, Collection<String> exporterIds, Identity actingIdentity);
	
	/**
	 * The method manage the effective export.
	 * 
	 * @param requestKey The primary key of the export task
	 */
	public void exportData(Long requestKey);
	
	public UserDataExport getCurrentData(IdentityRef identity);
	
	public String getDownloadURL(Identity identity);
	
	public MediaResource getDownload(IdentityRef identity);

}
