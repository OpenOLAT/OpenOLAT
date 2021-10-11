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
package org.olat.modules.lecture;

import java.util.Date;
import java.util.Set;

import org.olat.core.id.CreateInfo;
import org.olat.core.id.ModifiedInfo;
import org.olat.core.id.OLATResourceable;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 17 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface LectureBlock extends LectureBlockRef, ModifiedInfo, CreateInfo, OLATResourceable {
	
	public String getExternalId();

	public void setExternalId(String externalId);
	
	public LectureBlockManagedFlag[] getManagedFlags();
	
	public String getManagedFlagsString();
	
	public void setManagedFlagsString(String managedFlagsString);

	public String getTitle();

	public void setTitle(String title);
	
	public boolean isCompulsory();
	
	public void setCompulsory(boolean compulsory);

	public String getDescription();

	public void setDescription(String description);

	public String getPreparation();

	public void setPreparation(String preparation);

	public String getLocation();

	public void setLocation(String location);

	public String getComment();

	public void setComment(String comment);
	
	public int getPlannedLecturesNumber();
	
	public void setPlannedLecturesNumber(int number);
	
	public int getEffectiveLecturesNumber();

	public void setEffectiveLecturesNumber(int effectiveLecturesNumber);
	
	/**
	 * @return The calculated number of lectures dependent of planned and
	 * 		effective lectures and the status of the block.
	 */
	public int getCalculatedLecturesNumber();

	public Date getStartDate();

	public void setStartDate(Date startDate);

	public Date getEndDate();

	public void setEndDate(Date endDate);

	public Date getEffectiveEndDate();

	public void setEffectiveEndDate(Date effectiveEndDate);
	
	public Reason getReasonEffectiveEnd();
	
	public void setReasonEffectiveEnd(Reason reason);

	public LectureBlockStatus getStatus();
	
	/**
	 * 
	 * @param status Cannot be null
	 */
	public void setStatus(LectureBlockStatus status);
	
	public LectureRollCallStatus getRollCallStatus();
	
	/**
	 * 
	 * @param rollCallStatus Cannot be null
	 */
	public void setRollCallStatus(LectureRollCallStatus rollCallStatus);
	
	public Date getAutoClosedDate();

	public RepositoryEntry getEntry();
	
	/**
	 * @return The relation to taxonomy levels (lazy loading)
	 */
	public Set<LectureBlockToTaxonomyLevel> getTaxonomyLevels();

	public boolean isRunningAt(Date date);
}
