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
package org.olat.core.util.openxml;

/**
 * 
 * Initial date: 30.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenXMLSize {
	
	private final int widthPx;
	private final int heightPx;
	private final int widthEmu;
	private final int heightEmu;
	private final double resizeRatio;
	
	public OpenXMLSize(int widthPx, int heightPx, int widthEmu, int heightEmu, double resizeRatio) {
		this.widthPx = widthPx;
		this.heightPx = heightPx;
		this.widthEmu = widthEmu;
		this.heightEmu = heightEmu;
		this.resizeRatio = resizeRatio;
	}

	public int getWidthPx() {
		return widthPx;
	}

	public int getHeightPx() {
		return heightPx;
	}

	public int getWidthEmu() {
		return widthEmu;
	}

	public int getHeightEmu() {
		return heightEmu;
	}

	public double getResizeRatio() {
		return resizeRatio;
	}
}
