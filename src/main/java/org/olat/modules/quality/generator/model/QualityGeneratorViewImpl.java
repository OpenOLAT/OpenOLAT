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
package org.olat.modules.quality.generator.model;

import java.util.Date;

import org.olat.modules.quality.generator.QualityGeneratorView;

/**
 * 
 * Initial date: 13.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class QualityGeneratorViewImpl implements QualityGeneratorView {

	private final Long key;
	private final Date creationDate;
	private final String type;
	private final String title;
	private final Boolean enabled;
	private final Long numberDataCollections;
	
	public QualityGeneratorViewImpl(Long key, Date creationDate, String type, String title, Boolean enabled,
			Long numberDataCollections) {
		this.key = key;
		this.creationDate = creationDate;
		this.type = type;
		this.title = title;
		this.enabled = enabled;
		this.numberDataCollections = numberDataCollections;
	}

	@Override
	public Long getKey() {
		return key;
	}

	@Override
	public Date getCreationDate() {
		return creationDate;
	}

	@Override
	public String getType() {
		return type;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public Boolean isEnabled() {
		return enabled;
	}

	@Override
	public Long getNumberDataCollections() {
		return numberDataCollections;
	}

}
