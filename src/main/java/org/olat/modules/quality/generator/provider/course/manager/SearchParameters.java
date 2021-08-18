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
package org.olat.modules.quality.generator.provider.course.manager;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.id.OrganisationRef;
import org.olat.modules.quality.generator.QualityGeneratorRef;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 09.10.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SearchParameters {
	
	private QualityGeneratorRef generatorRef;
	private Date generatorDataCollectionStart;
	private List<? extends OrganisationRef> organisationRefs;
	private Date beginFrom;
	private Date beginTo;
	private Date endFrom;
	private Date endTo;
	private Date lifecycleValidAt;
	private Collection<? extends RepositoryEntryRef> whiteListRefs;
	private Collection<? extends RepositoryEntryRef> blackListRefs;
	private Collection<Long> excludedEducationalTypeKeys;
	
	public QualityGeneratorRef getGeneratorRef() {
		return generatorRef;

	}
	public void setGeneratorRef(QualityGeneratorRef generatorRef) {
		this.generatorRef = generatorRef;
	}
	
	public Date getGeneratorDataCollectionStart() {
		return generatorDataCollectionStart;
	}

	public void setGeneratorDataCollectionStart(Date generatorDataCollectionStart) {
		this.generatorDataCollectionStart = generatorDataCollectionStart;
	}

	public List<? extends OrganisationRef> getOrganisationRefs() {
		return organisationRefs;
	}
	
	public void setOrganisationRefs(List<? extends OrganisationRef> organisationRefs) {
		this.organisationRefs = organisationRefs;
	}
	
	public Date getBeginFrom() {
		return beginFrom;
	}

	public void setBeginFrom(Date beginFrom) {
		this.beginFrom = beginFrom;
	}

	public Date getBeginTo() {
		return beginTo;
	}

	public void setBeginTo(Date beginTo) {
		this.beginTo = beginTo;
	}

	public Date getEndFrom() {
		return endFrom;
	}

	public void setEndFrom(Date endFrom) {
		this.endFrom = endFrom;
	}

	public Date getEndTo() {
		return endTo;
	}
	
	public void setEndTo(Date endTo) {
		this.endTo = endTo;
	}

	public Date getLifecycleValidAt() {
		return lifecycleValidAt;
	}
	
	public void setLifecycleValidAt(Date lifecycleValidAt) {
		this.lifecycleValidAt = lifecycleValidAt;
	}

	public Collection<? extends RepositoryEntryRef> getWhiteListRefs() {
		return whiteListRefs;
	}
	
	public void setWhiteListRefs(Collection<? extends RepositoryEntryRef> whiteListRefs) {
		this.whiteListRefs = whiteListRefs;
	}
	
	public Collection<? extends RepositoryEntryRef> getBlackListRefs() {
		return blackListRefs;
	}
	
	public void setBlackListRefs(Collection<? extends RepositoryEntryRef> blackListRefs) {
		this.blackListRefs = blackListRefs;
	}
	
	public Collection<Long> getExcludedEducationalTypeKeys() {
		return excludedEducationalTypeKeys;
	}
	
	public void setExcludedEducationalTypeKeys(Collection<Long> excludedEducationalTypeKeys) {
		this.excludedEducationalTypeKeys = excludedEducationalTypeKeys;
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SearchParameters [generatorRef=");
		builder.append(generatorRef);
		builder.append(", organisationRefs (keys)=[");
		if (organisationRefs != null) {
			builder.append(organisationRefs.stream()
					.map(OrganisationRef::getKey)
					.map(k -> k.toString())
					.collect(Collectors.joining(", ")));
		}
		builder.append("]");
		builder.append(", beginFrom=");
		builder.append(beginFrom);
		builder.append(", beginTo=");
		builder.append(beginTo);
		builder.append(", endFrom=");
		builder.append(endFrom);
		builder.append(", endTo=");
		builder.append(endTo);
		builder.append(", whiteListRefs (keys)=[");
		if (whiteListRefs != null) {
			builder.append(whiteListRefs.stream()
					.map(RepositoryEntryRef::getKey)
					.map(r -> r.toString())
					.collect(Collectors.joining(", ")));
		}
		builder.append("]");
		builder.append(", blackListRefs (keys)=[");
		if (blackListRefs != null) {
			builder.append(blackListRefs.stream()
					.map(RepositoryEntryRef::getKey)
					.map(r -> r.toString())
					.collect(Collectors.joining(", ")));
		}
		builder.append("]");
		builder.append(", generatorDataCollectionStart=");
		builder.append(generatorDataCollectionStart);
		builder.append("]");
		builder.append(", excludedEducationalTypeKeys=[");
		if (excludedEducationalTypeKeys != null) {
			builder.append(excludedEducationalTypeKeys.stream()
					.map(r -> r.toString())
					.collect(Collectors.joining(", ")));
		}
		builder.append("]");
		return builder.toString();
	}

}
