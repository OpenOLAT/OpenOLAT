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
public class ProgressValue implements Comparable<ProgressValue> {

	private int total;
	private int green;
	private int red;
	
	public int getTotal() {
		return total;
	}
	
	public void setTotal(int total) {
		this.total = total;
	}
	
	public int getGreen() {
		return green;
	}
	
	public int getGreenPerCent() {
		return Math.round(100.0f * ((float)getGreen() / (float)getTotal()));
	}
	
	public void setGreen(int green) {
		this.green = green;
	}
	
	public int getRed() {
		return red;
	}
	
	public void setRed(int red) {
		this.red = red;
	}

	@Override
	public int compareTo(ProgressValue o) {
		double c1 = calcPercent();
		double c2 = o.calcPercent();
		return c1<c2 ? -1 : (c1==c2 ? 0 : 1);
	}
	
	private final double calcPercent() {
		return total <= 0 ? 0 : (double)green / (double)total;
	}
	
	@Override
	public String toString() {
		return "ProgressValue[total=" + total + ":green=" + green + "]";
	}
}
