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
import java.util.Date;
import java.util.List;

import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.model.CurriculumCopySettings;
import org.olat.modules.curriculum.model.CurriculumCopySettings.CopyElementSetting;
import org.olat.modules.curriculum.model.CurriculumCopySettings.CopyOfferSetting;
import org.olat.modules.curriculum.model.CurriculumCopySettings.CopyResources;
import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.model.OfferAndAccessInfos;

/**
 * 
 * Initial date: 12 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CopyElementContext {
	
	private final CurriculumElement curriculumElement;
	private final List<CurriculumElement> descendants;
	private final List<OfferAndAccessCopy> offersAndAccessInfos;
	
	private final CurriculumCopySettings copySettings = new CurriculumCopySettings();
	
	public CopyElementContext(CurriculumElement curriculumElement, List<CurriculumElement> descendants,
			List<OfferAndAccessInfos> offersAndAccessInfos) {
		copySettings.setCopyOffers(true);
		copySettings.setCopyResources(CopyResources.relation);
		copySettings.setBaseIdentifier(curriculumElement.getIdentifier());
		copySettings.setCopyTaxonomy(true);
		
		this.curriculumElement = curriculumElement;
		this.descendants = descendants;
		this.offersAndAccessInfos = offersAndAccessInfos == null
				? List.of()
				: offersAndAccessInfos.stream()
					.map(infos -> new OfferAndAccessCopy(infos.offer(), infos.offerAccess()))
					.toList();
	}
	
	public CurriculumCopySettings getCopySettings() {
		return copySettings;
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
	
	public boolean hasOffersAndAccessInfos() {
		return offersAndAccessInfos != null && !offersAndAccessInfos.isEmpty();
	}
	
	public List<OfferAndAccessCopy> getOffersAndAccessInfos() {
		return offersAndAccessInfos;
	}
	
	public String getDisplayName() {
		return copySettings.getDisplayName();
	}

	public void setDisplayName(String displayName) {
		copySettings.setDisplayName(displayName);
	}

	public String getIdentifier() {
		return copySettings.getIdentifier();
	}

	public void setIdentifier(String identifier) {
		copySettings.setIdentifier(identifier);
	}
	
	public String evaluateIdentifier(CurriculumElement element) {
		String derivatedIdentifier = element.getIdentifier();
		if(element.equals(getCurriculumElement())) {
			if(StringHelper.containsNonWhitespace(getIdentifier())) {
				derivatedIdentifier = getIdentifier();
			}
		} else {
			derivatedIdentifier = copySettings.evaluateIdentifier(derivatedIdentifier);
		}
		return derivatedIdentifier;
	}
	
	public String evaluateIdentifier(String originalIdentifier) {
		return copySettings.evaluateIdentifier(originalIdentifier);
	}

	public long getShiftDateByDays() {
		return copySettings.getShiftDateByDays();
	}

	public void setShiftDateByDays(long days) {
		copySettings.setShiftDateByDays(days);
	}
	
	public Date shiftDate(Date date) {
		return copySettings.shiftDate(date);
	}

	public CopyResources getCoursesEventsCopySetting() {
		return copySettings.getCopyResources();
	}

	public void setCoursesEventsCopySetting(CopyResources coursesEvents) {
		copySettings.setCopyResources(coursesEvents);
	}

	public boolean isStandaloneEventsCopySetting() {
		return copySettings.isCopyStandaloneEvents();
	}

	public void setStandaloneEventsCopySetting(boolean standaloneEvents) {
		copySettings.setCopyStandaloneEvents(standaloneEvents);
	}
	
	public void setCopyOwnersMemberships(boolean copy) {
		copySettings.setCopyOwnersMemberships(copy);
	}
	
	public void setCopyMasterCoachesMemberships(boolean copy) {
		copySettings.setCopyMasterCoachesMemberships(copy);
	}

	public void setCopyCoachesMemberships(boolean copy) {
		copySettings.setCopyCoachesMemberships(copy);
	}

	public void setAddCoachesAsTeacher(boolean addAsTeacher) {
		copySettings.setAddCoachesAsTeacher(addAsTeacher);
	}
	
	public CopyOfferSetting getOfferToCopy(Offer offer) {
		return copySettings.getCopyOfferSetting(offer);
	}
	
	public List<CopyOfferSetting> getOfferSettings() {
		return copySettings.getCopyOfferSettings();
	}
	
	public void setOfferSettings(List<CopyOfferSetting> settings) {
		copySettings.setCopyOfferSettings(settings);
	}
	
	public CopyElementSetting getCurriculumElementToCopy(CurriculumElementRef ref) {
		return copySettings.getCopyElementSetting(ref);
	}

	public List<CopyElementSetting> getCurriculumElementsSettings() {
		return copySettings.getCopyElementSettings();
	}

	public void setCurriculumElementsToCopy(List<CopyElementSetting> settings) {
		copySettings.setCopyElementSettings(settings);
	}
}
