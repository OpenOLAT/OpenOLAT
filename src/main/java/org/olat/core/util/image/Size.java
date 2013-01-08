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
package org.olat.core.util.image;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix
 *
 */
public class Size {
	private final int width;
	private final int height;
	private final boolean changed;
	
	public Size(int width, int height, boolean changed) {
		this.width = width;
		this.height = height;
		this.changed = changed;
	}

	public int getWidth() {
		if(width <= 0) {
			return 1;
		}
		return width;
	}

	public int getHeight() {
		if(height <= 0) {
			return 1;
		}
		return height;
	}
	
	public boolean isChanged() {
		return changed;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Size[width=").append(width)
			.append(":height=").append(height)
			.append(":changed=").append(changed)
			.append("]").append(super.toString());
		return sb.toString();
	}
}
