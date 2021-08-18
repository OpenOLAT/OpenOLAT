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
package org.olat.course.run;

import java.util.Date;

import org.olat.core.util.StringHelper;
import org.olat.repository.RepositoryEntry;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

/**
 * 
 * Initial date: 18 Aug 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonInclude(Include.NON_NULL)
public class InfoCourse {
	
	public static final InfoCourse of(RepositoryEntry entry) {
		if (entry != null) {
			InfoCourse info = new InfoCourse();
			info.id = entry.getKey();
			if (StringHelper.containsNonWhitespace(entry.getExternalRef())) {
				info.externalRef = entry.getExternalRef();
			}
			if (StringHelper.containsNonWhitespace(entry.getTechnicalType())) {
				info.technicalType = entry.getTechnicalType();
			}
			if (entry.getEducationalType() != null) {
				info.educationalTypeIdentifier = entry.getEducationalType().getIdentifier();
			}
			info.displayname = entry.getDisplayname();
			if (StringHelper.containsNonWhitespace(entry.getDescription())) {
				info.description = entry.getDescription();
			}
			if (entry.getLifecycle() != null) {
				if (entry.getLifecycle().getValidFrom() != null) {
					info.lifecycleValidFrom = entry.getLifecycle().getValidFrom();
				}
				if (entry.getLifecycle().getValidTo() != null) {
					info.lifecycleValidTo = entry.getLifecycle().getValidTo();
				}
			}
			return info;
		}
		return null;
	}
	
	private InfoCourse() {
		//
	}
	
	private Long id;
	private String externalRef;
	private String technicalType;
	private String educationalTypeIdentifier;
	private String displayname;
	private String description;
	private Date lifecycleValidFrom;
	private Date lifecycleValidTo;
	
	public Long getId() {
		return id;
	}

	public String getExternalRef() {
		return externalRef;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getTechnicalType() {
		return technicalType;
	}

	public String getDisplayname() {
		return displayname;
	}

	public String getDescription() {
		return description;
	}

	public String getEducationalTypeIdentifier() {
		return educationalTypeIdentifier;
	}
	
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
	public Date getLifecycleValidFrom() {
		return lifecycleValidFrom;
	}

	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
	public Date getLifecycleValidTo() {
		return lifecycleValidTo;
	}
	
}
