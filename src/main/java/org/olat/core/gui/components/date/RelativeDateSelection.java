/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.core.gui.components.date;

/**
 * Initial date: 2026-06-15<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 */
public class RelativeDateSelection {

	private final String refKey;
	private final OffsetDirection direction;
	private final String unitKey;
	private final Integer value;
	private final boolean offsetEnabled;

	public RelativeDateSelection(String refKey, OffsetDirection direction, String unitKey, Integer value, boolean offsetEnabled) {
		this.refKey = refKey;
		this.direction = direction;
		this.unitKey = unitKey;
		this.value = value;
		this.offsetEnabled = offsetEnabled;
	}

	public String getRefKey() {
		return refKey;
	}

	public OffsetDirection getDirection() {
		return direction;
	}

	public String getUnitKey() {
		return unitKey;
	}

	public Integer getValue() {
		return value;
	}

	public boolean isOffsetEnabled() {
		return offsetEnabled;
	}

}
