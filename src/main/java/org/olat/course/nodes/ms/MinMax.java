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
package org.olat.course.nodes.ms;

/**
 * 
 * Initial date: 12 Jun 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface MinMax {
	
	Float getMin();
	
	Float getMax();
	
	public static MinMax of(Float min, Float max) {
		return new MinMaxImpl(min, max);
	}
	
	public static MinMax add(MinMax... minMaxList) {
		Float min = null;
		Float max = null;
		
		if(minMaxList != null && minMaxList.length > 0) {
			for(MinMax minMax:minMaxList) {
				if(minMax != null) {
					min = add(minMax.getMin(), min);
					max = add(minMax.getMax(), max);
				}
			}
		}
		
		return (min == null && max == null) ? null : MinMax.of(min, max);
	}
	
	public static Float add(Float val1, Float val2) {
		if(val1 == null) {
			return val2;
		}
		if(val2 == null ) {
			return val1;
		}
		return Float.valueOf(val1.floatValue() + val2.floatValue());
	}
	
	static final class MinMaxImpl implements MinMax {
		
		private final Float min;
		private final Float max;
		
		private MinMaxImpl(Float min, Float max) {
			this.min = min;
			this.max = max;
		}

		@Override
		public Float getMin() {
			return min;
		}

		@Override
		public Float getMax() {
			return max;
		}
		
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder(24);
			sb.append("MinMax[min=").append(min).append(";max=").append(max).append("]");
			return sb.toString();
		}
	}
}
