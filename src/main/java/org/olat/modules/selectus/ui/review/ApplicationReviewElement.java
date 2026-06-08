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
package org.olat.modules.selectus.ui.review;

import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.model.review.ReviewElementDefinition;
import org.olat.modules.selectus.model.review.ReviewResponse;

/**
 * 
 * Initial date: 5 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationReviewElement {
	
	private FormItem item;
	private String leftLabel;
	private String rightLabel;
	private final ReviewResponse response;
	private final ReviewElementDefinition elementDefinition;
	
	public ApplicationReviewElement(ReviewElementDefinition elementDefinition, ReviewResponse response) {
		this.response = response;
		this.elementDefinition = elementDefinition;
	}
	
	public String getType() {
		return elementDefinition.getType() == null ? "" : elementDefinition.getType().name();
	}
	
	public String getLabel() {
		return elementDefinition.getLabel();
	}
	
	public ReviewResponse getResponse() {
		return response;
	}
	
	public boolean hasLabel() {
		return StringHelper.containsNonWhitespace(leftLabel) || StringHelper.containsNonWhitespace(rightLabel);
	}
	
	public String getLeftLabel() {
		return leftLabel;
	}
	
	public void setLeftLabel(String label) {
		leftLabel = label;
	}
	
	public String getRightLabel() {
		return rightLabel;
	}
	
	public void setRightLabel(String label) {
		rightLabel = label;
	}
	
	public String getStringValue() {
		if(response == null) return "";
		if(response.getStringValue() == null) return "";
		return response.getStringValue();
	}
	
	public ReviewElementDefinition getElementDefinition() {
		return elementDefinition;
	}

	public FormItem getItem() {
		return item;
	}

	public void setItem(FormItem item) {
		this.item = item;
	}
	
	

}
