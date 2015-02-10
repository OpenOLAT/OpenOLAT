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
package org.olat.modules.coach.ui;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class LightedValue implements Comparable<LightedValue> {
	
	private final Integer value;
	private final Light light;
	
	public LightedValue(Integer value, Light light) {
		this.value = value;
		this.light = light;
	}

	public String getValue() {
		return value == null ? "" : Integer.toString(value.intValue());
	}

	public Light getLight() {
		return light;
	}
	
	@Override
	public int compareTo(LightedValue o) {
		int color1 = light == null ? 0 : light.ordinal();
		int color2 = o.light == null ? 0 : o.light.ordinal();
		if(color1 < color2) {
			return -1;
		} else if(color2 < color1) {
			return 1;
		}
		
		int val1 = value == null ? 0 : value.intValue();
		int val2 = o.value == null ? 0 : o.value.intValue();
		if(val2 < val1) {
			return -1;
		} else if(val1 < val2) {
			return 1;
		}
		return 0;
	}

	public enum Light {
		grey,
		green,
		yellow,
		red,
		black,
	}
}
