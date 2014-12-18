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
package org.olat.course.assessment.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.AssessmentMode;

/**
 * 
 * Initial date: 18.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TransientAssessmentMode implements Serializable {

	private static final long serialVersionUID = -5738682288044689497L;
	private String displayName;
	private Long repositoryEntryKey;
	private OLATResourceable resource;
	
	private String name;
	private String description;
	private Date begin;
	private Date end;
	private int leadTime;
	
	public TransientAssessmentMode(AssessmentMode mode) {
		displayName = mode.getRepositoryEntry().getDisplayname();
		resource = OresHelper.clone(mode.getRepositoryEntry().getOlatResource());
		repositoryEntryKey = mode.getRepositoryEntry().getKey();
		
		name = mode.getName();
		description = mode.getDescription();
		begin = mode.getBegin();
		end = mode.getEnd();
		leadTime = mode.getLeadTime();
	}
	
	public static List<TransientAssessmentMode> create(List<AssessmentMode> modes) {
		List<TransientAssessmentMode> transientModes = new ArrayList<>(modes.size());
		for(AssessmentMode mode:modes) {
			transientModes.add(new TransientAssessmentMode(mode));
		}
		return transientModes;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Long getRepositoryEntryKey() {
		return repositoryEntryKey;
	}

	public OLATResourceable getResource() {
		return resource;
	}

	public void setResource(OLATResourceable resource) {
		this.resource = resource;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Date getBegin() {
		return begin;
	}

	public void setBegin(Date begin) {
		this.begin = begin;
	}

	public Date getEnd() {
		return end;
	}

	public void setEnd(Date end) {
		this.end = end;
	}

	public int getLeadTime() {
		return leadTime;
	}

	public void setLeadTime(int leadTime) {
		this.leadTime = leadTime;
	}
}