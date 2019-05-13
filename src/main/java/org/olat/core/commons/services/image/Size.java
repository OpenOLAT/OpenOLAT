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
package org.olat.core.commons.services.image;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix
 *
 */
public class Size {
	
	private static final Logger log = Tracing.createLoggerFor(Size.class);
	
	private final int width;
	private final int height;
	private final int xOffset;
	private final int yOffset;
	private final boolean changed;

	public Size(int width, int height, boolean changed) {
		this(width, height, 0, 0, changed);
	}
	
	public Size(int width, int height, int xOffset, int yOffset, boolean changed) {
		this.width = width;
		this.height = height;
		this.xOffset = xOffset;
		this.yOffset = yOffset;
		this.changed = changed;
	}
	
	public static Size parseString(String str) {
		Size size = null;
		int index = str.indexOf('x');
		if(index > 0) {
			String widthStr = str.substring(0, index);
			String heightStr = str.substring(index+1);
			if(StringHelper.isLong(widthStr) && StringHelper.isLong(heightStr)) {
				try {
					int width = Integer.parseInt(widthStr);
					int height = Integer.parseInt(heightStr);
					size = new Size(width, height, false);
				} catch (NumberFormatException e) {
					log.warn("", e);
				}
			}
		}
		return size;
	}

	/**
	 * The minimal value for width is 1px
	 * @return
	 */
	public int getWidth() {
		if(width <= 0) {
			return 1;
		}
		return width;
	}
	/**
	 * The minimal value for height is 1px
	 * @return
	 */
	public int getHeight() {
		if(height <= 0) {
			return 1;
		}
		return height;
	}
	
	public int getXOffset() {
		if(xOffset < 0) {
			return 0;
		}
		return xOffset;
	}

	public int getYOffset() {
		if(yOffset < 0) {
			return 0;
		}
		return yOffset;
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
