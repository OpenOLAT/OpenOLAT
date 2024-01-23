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

import java.beans.Transient;

/**
 * Initial date: 2024-01-16<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class BlockLayoutSettings {


	private BlockLayoutSpacing customTopSpacing, customRightSpacing, customBottomSpacing, customLeftSpacing;

	private BlockLayoutSpacing spacing;

	public BlockLayoutSpacing getSpacing() {
		return spacing;
	}

	public void setSpacing(BlockLayoutSpacing spacing) {
		this.spacing = spacing;
	}

	public BlockLayoutSpacing getCustomTopSpacing() {
		return customTopSpacing;
	}

	public void setCustomTopSpacing(BlockLayoutSpacing customTopSpacing) {
		this.customTopSpacing = customTopSpacing;
	}

	public BlockLayoutSpacing getCustomRightSpacing() {
		return customRightSpacing;
	}

	public void setCustomRightSpacing(BlockLayoutSpacing customRightSpacing) {
		this.customRightSpacing = customRightSpacing;
	}

	public BlockLayoutSpacing getCustomBottomSpacing() {
		return customBottomSpacing;
	}

	public void setCustomBottomSpacing(BlockLayoutSpacing customBottomSpacing) {
		this.customBottomSpacing = customBottomSpacing;
	}

	public BlockLayoutSpacing getCustomLeftSpacing() {
		return customLeftSpacing;
	}

	public void setCustomLeftSpacing(BlockLayoutSpacing customLeftSpacing) {
		this.customLeftSpacing = customLeftSpacing;
	}

	@Transient
	public String getCssClass() {
		if (!spacing.equals(BlockLayoutSpacing.custom)) {
			return spacing.getCssClass();
		} else {
			return String.format("%s %s_top %s_right %s_bottom %s_left", spacing.getCssClass(),
					getCustomTopSpacing().getCssClass(), getCustomRightSpacing().getCssClass(),
					getCustomBottomSpacing().getCssClass(), getCustomLeftSpacing().getCssClass());
		}
	}

	public static BlockLayoutSettings getDefaults(boolean inForm) {
		BlockLayoutSettings layoutSettings = new BlockLayoutSettings();
		BlockLayoutSpacing defaultSpacing = BlockLayoutSpacing.defaultValue(inForm);
		layoutSettings.setSpacing(defaultSpacing);
		layoutSettings.setCustomTopSpacing(defaultSpacing);
		layoutSettings.setCustomRightSpacing(defaultSpacing);
		layoutSettings.setCustomBottomSpacing(defaultSpacing);
		layoutSettings.setCustomLeftSpacing(defaultSpacing);
		return layoutSettings;
	}
}
