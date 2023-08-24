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

public enum ProjProjectImageType { 
	avatar,
	background;
	
	private static final List<String> NAMES = Arrays.stream(ProjProjectImageType.values())
			.map(ProjProjectImageType::name).toList();
	
	public static final boolean isValid(String name) {
		return NAMES.contains(name);
	}
	
	public static final List<String> AVATAR_CSS_CLASSES = List.of(
			"o_proj_avatar_dark_blue",
			"o_proj_avatar_light_blue",
			"o_proj_avatar_purple",
			"o_proj_avatar_red",
			"o_proj_avatar_orange",
			"o_proj_avatar_yellow",
			"o_proj_avatar_dark_green",
			"o_proj_avatar_light_green"
	);
	public static final String getRandmonAvatarCssClass() {
		return AVATAR_CSS_CLASSES.get(new Random().nextInt(AVATAR_CSS_CLASSES.size() - 1));
	}
	

}