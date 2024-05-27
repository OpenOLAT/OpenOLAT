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
package org.olat.modules.ceditor.model;

/**
 * Initial date: 2024-05-15<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ImageComparisonSettings {

	private ImageComparisonOrientation orientation;
	private ImageComparisonType type;
	private String description;
	private Double initialSliderPosition;
	private String text1;
	private String text2;
	private BlockLayoutSettings layoutSettings;
	private AlertBoxSettings alertBoxSettings;

	public ImageComparisonSettings() {
	}

	public ImageComparisonOrientation getOrientation() {
		return orientation;
	}

	public void setOrientation(ImageComparisonOrientation orientation) {
		this.orientation = orientation;
	}

	public ImageComparisonType getType() {
		return type;
	}

	public void setType(ImageComparisonType type) {
		this.type = type;
	}

	public Double getInitialSliderPosition() {
		return initialSliderPosition;
	}

	public void setInitialSliderPosition(Double initialSliderPosition) {
		this.initialSliderPosition = initialSliderPosition;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getText1() {
		return text1;
	}

	public void setText1(String text1) {
		this.text1 = text1;
	}

	public String getText2() {
		return text2;
	}

	public void setText2(String text2) {
		this.text2 = text2;
	}

	public BlockLayoutSettings getLayoutSettings() {
		return layoutSettings;
	}

	public void setLayoutSettings(BlockLayoutSettings layoutSettings) {
		this.layoutSettings = layoutSettings;
	}

	public AlertBoxSettings getAlertBoxSettings() {
		return alertBoxSettings;
	}

	public void setAlertBoxSettings(AlertBoxSettings alertBoxSettings) {
		this.alertBoxSettings = alertBoxSettings;
	}
}
