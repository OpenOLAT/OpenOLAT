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
package org.olat.modules.project;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.olat.core.commons.services.color.ColorSet;

public enum ProjProjectImageType { 
	avatar,
	background;
	
	private static final Random RANDOM = new Random();
	
	private static final List<String> NAMES = Arrays.stream(ProjProjectImageType.values())
			.map(ProjProjectImageType::name).toList();
	
	public static final boolean isValid(String name) {
		return NAMES.contains(name);
	}
	
	/**
	 * @return A random hue of the colour set M (no theme colour), the key of
	 * 		its solid swatch, e.g. "blue". The avatar renders with the swatch
	 * 		class, e.g. "o_swatch_blue", see ColorService.toStoredValueCssClass().
	 */
	public static final String getRandomAvatarColor() {
		List<String> hues = ColorSet.M.getHueKeys();
		return hues.get(RANDOM.nextInt(hues.size()));
	}
	

}