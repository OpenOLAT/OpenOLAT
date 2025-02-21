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
package org.olat.modules.curriculum.model;

import java.util.Date;
import java.util.List;

import org.olat.core.util.DateUtils;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.resource.accesscontrol.Offer;

/**
 * 
 * Initial date: 19 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumCopySettings {
	
	private boolean copyDates;
	private boolean copyTaxonomy;
	private boolean copyOffers;
	private boolean copyStandaloneEvents;
	
	private String identifier;
	private String displayName;
	/**
	 * The identifier of the original root element to copy. If other identifiers starts with it,
	 * replace the part with the new identifier.
	 */
	private String baseIdentifier;
	
	private long shiftDateByDays = 0;
	
	private CopyResources copyResources;

	private List<CopyOfferSetting> copyOfferSettings;
	private List<CopyElementSetting> copyElementSettings;
	
	public CurriculumCopySettings() {
		//
	}
	
	public void setBaseIdentifier(String baseIdentifier) {
		this.baseIdentifier = baseIdentifier;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	
	public String evaluateIdentifier(String originalIdentifier) {
		String derivatedIdentifier = originalIdentifier;
		if(baseIdentifier != null && derivatedIdentifier != null && StringHelper.containsNonWhitespace(getIdentifier())
				&& derivatedIdentifier.startsWith(baseIdentifier)) {
			String suffix = derivatedIdentifier.substring(baseIdentifier.length());
			derivatedIdentifier = getIdentifier() + suffix;
		}
		return derivatedIdentifier;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public boolean isCopyDates() {
		return copyDates;
	}

	public void setCopyDates(boolean copyDates) {
		this.copyDates = copyDates;
	}

	public CopyResources getCopyResources() {
		return copyResources;
	}

	public void setCopyResources(CopyResources copyResources) {
		this.copyResources = copyResources;
	}

	public boolean isCopyStandaloneEvents() {
		return copyStandaloneEvents;
	}

	public void setCopyStandaloneEvents(boolean copy) {
		this.copyStandaloneEvents = copy;
	}

	public boolean isCopyOffers() {
		return copyOffers;
	}

	public void setCopyOffers(boolean copy) {
		this.copyOffers = copy;
	}

	public boolean isCopyTaxonomy() {
		return copyTaxonomy;
	}

	public void setCopyTaxonomy(boolean copyTaxonomy) {
		this.copyTaxonomy = copyTaxonomy;
	}
	
	public long getShiftDateByDays() {
		return shiftDateByDays;
	}

	public void setShiftDateByDays(long shiftDateByDays) {
		this.shiftDateByDays = shiftDateByDays;
	}
	
	public Date shiftDate(Date date) {
		if(date == null) return null;
		if(shiftDateByDays == 0) return date;
		return DateUtils.addDays(date, (int)shiftDateByDays);
	}

	public CopyElementSetting getCopyElementSetting(CurriculumElementRef ref) {
		if(copyElementSettings != null) {
			return copyElementSettings.stream()
					.filter(el -> ref.getKey().equals(el.originalElement().getKey()))
					.findFirst()
					.orElse(null);
		}
		return null;
	}
	
	public List<CopyElementSetting> getCopyElementSettings() {
		return copyElementSettings;
	}

	public void setCopyElementSettings(List<CopyElementSetting> copyElementSettings) {
		this.copyElementSettings = copyElementSettings;
	}
	
	public CopyOfferSetting getCopyOfferSetting(Offer ref) {
		if(copyOfferSettings != null) {
			return copyOfferSettings.stream()
					.filter(el -> ref.getKey().equals(el.originalOffer().getKey()))
					.findFirst()
					.orElse(null);
		}
		return null;
	}

	public List<CopyOfferSetting> getCopyOfferSettings() {
		return copyOfferSettings;
	}

	public void setCopyOfferSettings(List<CopyOfferSetting> copyOfferSettings) {
		this.copyOfferSettings = copyOfferSettings;
	}

	public enum CopyResources {
		dont,
		relation,
		resource;
		
		public static CopyResources valueOf(String val, CopyResources def) {
			if(val == null) return def;
			
			for(CopyResources value:values()) {
				if(val.equals(value.name())) {
					return value;
				}
			}
			return def;
		}
	}
	
	public record CopyOfferSetting(Offer originalOffer, Date validFrom, Date validTo) {
		
		public boolean hasDates() {
			return validFrom != null || validTo != null;
		}
	}
	
	public record CopyElementSetting(CurriculumElement originalElement, String displayName, String identifier, Date begin, Date end) {
		
		public boolean hasDates() {
			return begin != null || end != null;
		}
	}
}
