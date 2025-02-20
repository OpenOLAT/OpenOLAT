/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui.copy;

import java.util.ArrayList;
import java.util.List;

import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.model.CurriculumCopySettings.CopyElementSetting;
import org.olat.modules.curriculum.model.CurriculumCopySettings.CopyResources;
import org.olat.resource.accesscontrol.model.OfferAndAccessInfos;

/**
 * 
 * Initial date: 12 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CopyElementContext {
	
	private final CurriculumElement curriculumElement;
	private final List<CurriculumElement> descendants;
	private final List<OfferAndAccessCopy> offersAndAccessInfos;
	
	private String displayName;
	private String identifier;
	private CopyResources coursesEventsCopySetting;
	private CopyResources standaloneEventsCopySetting;
	private List<CopyElementSetting> curriculumElementsSettings;
	
	public CopyElementContext(CurriculumElement curriculumElement, List<CurriculumElement> descendants,
			List<OfferAndAccessInfos> offersAndAccessInfos) {
		this.curriculumElement = curriculumElement;
		this.descendants = descendants;
		this.offersAndAccessInfos = offersAndAccessInfos == null
				? List.of()
				: offersAndAccessInfos.stream()
					.map(infos -> new OfferAndAccessCopy(infos.offer(), infos.offerAccess()))
					.toList();
	}

	public CurriculumElement getCurriculumElement() {
		return curriculumElement;
	}
	
	public List<CurriculumElement> getDescendants() {
		return descendants;
	}
	
	public List<CurriculumElement> getAllCurriculumElements() {
		List<CurriculumElement> allElements = new ArrayList<>();
		allElements.add(curriculumElement);
		if(descendants != null) {
			allElements.addAll(descendants);
		}
		return allElements;
	}
	
	public List<OfferAndAccessCopy> getOffersAndAccessInfos() {
		return offersAndAccessInfos;
	}
	
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	public CopyResources getCoursesCopySetting() {
		return CopyResources.relation;
	}

	public CopyResources getCoursesEventsCopySetting() {
		return coursesEventsCopySetting;
	}

	public void setCoursesEventsCopySetting(CopyResources coursesEvents) {
		this.coursesEventsCopySetting = coursesEvents;
	}

	public CopyResources getStandaloneEventsCopySetting() {
		return standaloneEventsCopySetting;
	}

	public void setStandaloneEventsCopySetting(CopyResources standaloneEvents) {
		this.standaloneEventsCopySetting = standaloneEvents;
	}
	
	public CopyElementSetting getCurriculumElementToCopy(CurriculumElementRef ref) {
		if(curriculumElementsSettings != null) {
			return curriculumElementsSettings.stream()
					.filter(el -> ref.getKey().equals(el.originalElement().getKey()))
					.findFirst()
					.orElse(null);
		}
		return null;
	}

	public List<CopyElementSetting> getCurriculumElementsSettings() {
		return curriculumElementsSettings;
	}

	public void setCurriculumElementsToCopy(List<CopyElementSetting> settings) {
		this.curriculumElementsSettings = settings;
	}
}
