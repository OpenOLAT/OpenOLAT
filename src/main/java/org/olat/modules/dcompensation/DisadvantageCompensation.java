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
package org.olat.modules.dcompensation;

import java.util.Date;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.Identity;
import org.olat.core.id.ModifiedInfo;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 22 sept. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface DisadvantageCompensation extends ModifiedInfo, CreateInfo {

	public Long getKey();
	
	/**
	 * @return Extra time in seconds
	 */
	public Integer getExtraTime();
	
	/**
	 * @param extraTime The extra time in seconds
	 */
	public void setExtraTime(Integer extraTime);
	
	public DisadvantageCompensationStatusEnum getStatusEnum();
	
	public void setStatusEnum(DisadvantageCompensationStatusEnum status);
	
	public String getApprovedBy();

	public void setApprovedBy(String approvedBy);
	
	public Date getApproval();

	public void setApproval(Date approval);
	
	public Identity getIdentity();
	
	public String getSubIdent();
	
	public void setSubIdent(String subIdent);
	
	public String getSubIdentName();

	public void setSubIdentName(String subIdentName);
	
	public RepositoryEntry getEntry();
	
	public Identity getCreator();

}
