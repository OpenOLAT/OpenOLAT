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
package org.olat.modules.selectus.model.committee;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.Persistable;

import org.olat.modules.selectus.model.Position;

/**
 * 
 * Initial date: 3 nov. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface ReportCommittee extends Persistable, CreateInfo, ModifiedInfo {
	
	public String getRole();

	public void setRole(String role);

	public String getRatingsRights();

	public void setRatingsRights(String ratingsRights);

	public String getGender();

	public void setGender(String gender);

	public String getUserClassification();

	public void setUserClassification(String userClassification);
	
	
	public Integer getNumOfRatingsA();

	public void setNumOfRatingsA(Integer numOfRatingsA);

	public Integer getNumOfRatingsB();

	public void setNumOfRatingsB(Integer numOfRatingsB);

	public Integer getNumOfRatingsC();

	public void setNumOfRatingsC(Integer numOfRatingsC);

	public Integer getNumOfAbstentions();

	public void setNumOfAbstentions(Integer numOfAbstentions);
	
	
	public Position getPosition();

}
