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
package org.olat.modules.ceditor;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 5 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class ContentEditorModule extends AbstractSpringModule {
	
	@Value("${ceditor.image.styles}")
	private String imageStyles;
	@Value("${ceditor.image.title.styles}")
	private String imageTitleStyles;
	
	@Autowired
	private ContentEditorModule(CoordinatorManager coordinateManager) {
		super(coordinateManager);
	}

	@Override
	public void init() {
		//
	}

	@Override
	protected void initFromChangedProperties() {
		//
	}

	public String getImageStyles() {
		return imageStyles;
	}
	
	public List<String> getImageStyleList() {
		return stylesToList(imageStyles);
	}

	public String getImageTitleStyles() {
		return imageTitleStyles;
	}
	
	public List<String> getImageTitleStyleList() {
		return stylesToList(imageTitleStyles);
	}
	
	private List<String> stylesToList(String styles) {
		List<String> styleList = new ArrayList<>();
		if(StringHelper.containsNonWhitespace(styles)) {
			String[] styleArr = styles.split("[,]");
			for(String style:styleArr) {
				if(StringHelper.containsNonWhitespace(style)) {
					styleList.add(style);
				}
			}
		}
		return styleList;
	}
}
