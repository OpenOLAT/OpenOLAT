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
package org.olat.ims.qti21.ui.assessment.model;

import java.util.List;

import org.olat.ims.qti21.ui.components.FlowFormItem;

/**
 * 
 * Initial date: 4 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SectionRubrics {
	
	private final String title;
	private final List<FlowFormItem> rubrics;
	private final String identifier;
	private final String openLabel;
	
	public SectionRubrics(String identifier, String title, List<FlowFormItem> rubrics, String openLabel) {
		this.identifier = identifier;
		this.rubrics = rubrics;
		this.title = title;
		this.openLabel = openLabel;
	}
	
	public String getTitle() {
		return title;
	}

	public List<FlowFormItem> getRubrics() {
		return rubrics;
	}
	
	public String getIdentifier() {
		return identifier;
	}
	
	public String getOpenLabel() {
		return openLabel;
	}

}
