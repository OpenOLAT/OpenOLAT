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
package org.olat.modules.forms.ui.model;

import java.util.List;

import org.olat.modules.forms.EvaluationFormResponse;

/**
 * 
 * Initial date: 18.04.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CompareResponse {
	
	private final List<EvaluationFormResponse> responses;
	private final String legendName;
	private final String color;
	
	public CompareResponse(List<EvaluationFormResponse> responses, String legendName, String color) {
		this.responses = responses;
		this.legendName = legendName;
		this.color = color;
	}

	public List<EvaluationFormResponse> getResponses() {
		return responses;
	}

	public String getLegendName() {
		return legendName;
	}

	public String getColor() {
		return color;
	}
	
}
