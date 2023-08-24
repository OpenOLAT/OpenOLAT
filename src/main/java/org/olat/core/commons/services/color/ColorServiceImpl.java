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
package org.olat.core.commons.services.color;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.util.StringHelper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Initial date: 2023-03-23<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
@Service
public class ColorServiceImpl implements ColorService {

	@Value("${color.list}")
	private String colorList;

	@Override
	public List<String> getColors() {
		return listStringToList(colorList);
	}

	@Override
	public List<String> getColorsForBadges() {
		List<String> mutableList = getColors();
		mutableList.add(0, "gold");
		mutableList.add(1, "silver");
		mutableList.add(2, "bronze");
		return mutableList;
	}

	private List<String> listStringToList(String listString) {
		List<String> list = new ArrayList<>();
		if (StringHelper.containsNonWhitespace(listString)) {
			String[] itemArray = listString.split(",");
			for (String item : itemArray) {
				if (StringHelper.containsNonWhitespace(item)) {
					list.add(item);
				}
			}
		}
		return list;
	}
}
