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
package org.olat.course.certificate;

import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 21.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface Certificate extends CreateInfo {
	
	public Long getKey();
	
	public CertificateStatus getStatus();
	
	public String getUuid();
	
	public String getExternalId();
	
	public CertificateManagedFlag[] getManagedFlags();
	
	public Date getNextRecertificationDate();

	public void setNextRecertificationDate(Date nextRecertificationDate);
	
	public String getPath();
	
	public String getCourseTitle();
	
	public Long getArchivedResourceKey();
	
	public Identity getIdentity();

}
