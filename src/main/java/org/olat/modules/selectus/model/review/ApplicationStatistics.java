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
package org.olat.modules.selectus.model.review;

import java.util.ArrayList;
import java.util.List;

import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.ApplicationRef;

/**
 * 
 * Initial date: 20 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationStatistics implements ApplicationRef {
	
	private boolean hasReviewed = false;
	private final ApplicationLight application;
	private final List<ApplicationStatisticElement> statisticsElements = new ArrayList<>();
	private final List<ApplicationTextCollectionElement> textCollectionElements = new ArrayList<>();
	
	public ApplicationStatistics(ApplicationLight application) {
		this.application = application;
	}
	
	@Override
	public Long getKey() {
		return application.getKey();
	}
	
	public boolean isHasReviewed() {
		return hasReviewed;
	}

	public void setHasReviewed(boolean hasReviewed) {
		this.hasReviewed = hasReviewed;
	}

	public ApplicationLight getApplication() {
		return application;
	}
	
	public List<ApplicationStatisticElement> getStatisticsElements() {
		return statisticsElements;
	}
	
	public ApplicationStatisticElement getStatisticsElement(ReviewElementDefinition definition) {
		for(ApplicationStatisticElement statisticsElement:statisticsElements) {
			if(statisticsElement.getElementDefinition().getKey().equals(definition.getKey())) {
				return statisticsElement;
			}
		}
		return null;
	}
	
	public void addStatisticsElement(ApplicationStatisticElement element) {
		statisticsElements.add(element);
	}
	
	public List<ApplicationTextCollectionElement> getTextCollectionElements() {
		return textCollectionElements;
	}
	
	public ApplicationTextCollectionElement getTextCollectionElement(Reviewer reviewer, ReviewElementDefinition definition) {
		for(ApplicationTextCollectionElement textCollectionElement:textCollectionElements) {
			if(textCollectionElement.getElementDefinition().getKey().equals(definition.getKey())
					&& textCollectionElement.getReviewer().getKey().equals(reviewer.getKey())) {
				return textCollectionElement;
			}
		}
		return null;
	}
	
	public void addTextCollectionElement(ApplicationTextCollectionElement element) {
		textCollectionElements.add(element);
	}

}
