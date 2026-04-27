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
package org.olat.resource.accesscontrol;

import java.util.Collection;
import java.util.Set;

import org.olat.core.gui.components.util.SelectionValues;
import org.olat.modules.taxonomy.TaxonomyLevel;

public class CatalogInfo {

	public static final CatalogInfo UNSUPPORTED = new CatalogInfo(false, false, false, false, false, null, null, null,
			null, null, null, null, false, false, false, null, null, null, null, null, true, null);
	public static final TrueStatusEvaluator TRUE_STATUS_EVALUATOR = new TrueStatusEvaluator();

	private final boolean catalogSupported;
	private final boolean webCatalogSupported;
	private final boolean publishedGroupsSupported;
	private final boolean autoBookingSupported;
	private final boolean showDetails;
	private final String detailsLabel;
	private final String details;
	private final String customPublishedIn;
	private final CatalogStatusEvaluator statusEvaluator;
	private final String statusPeriodOption;
	private final SelectionValues availableStatuses;
	private final Set<String> defaultStatuses;
	private final boolean fullyBooked;
	private final boolean startDateAvailable;
	private final boolean endDateAvailable;
	private final String editBusinessPath;
	private final String editLabel;
	private final String catalogBusinessPath;
	private final String webCatalogBusinessPath;
	private final Collection<TaxonomyLevel> microsites;
	private final boolean showQRCode;
	private final SortPriorityProvider sortPriorityProvider;

	public CatalogInfo(boolean catalogSupported, boolean webCatalogSupported, boolean publishedGroupsSupported,
			boolean autoBookingSupported, boolean showDetails, String detailsLabel, String details,
			String customPublishedIn, CatalogStatusEvaluator statusEvaluator, String statusPeriodOption,
			SelectionValues availableStatuses, Set<String> defaultStatuses, boolean fullyBooked,
			boolean startDateAvailable, boolean endDateAvailable, String editBusinessPath, String editLabel,
			String catalogBusinessPath, String webCatalogBusinessPath, Collection<TaxonomyLevel> microsites,
			boolean showQRCode, SortPriorityProvider sortPriorityProvider) {
		this.catalogSupported = catalogSupported;
		this.webCatalogSupported = webCatalogSupported;
		this.publishedGroupsSupported = publishedGroupsSupported;
		this.autoBookingSupported = autoBookingSupported;
		this.showDetails = showDetails;
		this.detailsLabel = detailsLabel;
		this.details = details;
		this.customPublishedIn = customPublishedIn;
		this.statusEvaluator = statusEvaluator;
		this.statusPeriodOption = statusPeriodOption;
		this.availableStatuses = availableStatuses;
		this.defaultStatuses = defaultStatuses;
		this.fullyBooked = fullyBooked;
		this.startDateAvailable = startDateAvailable;
		this.endDateAvailable = endDateAvailable;
		this.editBusinessPath = editBusinessPath;
		this.editLabel = editLabel;
		this.catalogBusinessPath = catalogBusinessPath;
		this.webCatalogBusinessPath = webCatalogBusinessPath;
		this.microsites = microsites;
		this.showQRCode = showQRCode;
		this.sortPriorityProvider = sortPriorityProvider;
	}

	public boolean isCatalogSupported() {
		return catalogSupported;
	}

	public boolean isWebCatalogSupported() {
		return webCatalogSupported;
	}

	public boolean isPublishedGroupsSupported() {
		return publishedGroupsSupported;
	}

	public boolean isAutoBookingSupported() {
		return autoBookingSupported;
	}

	public boolean isShowDetails() {
		return showDetails;
	}

	public String getDetailsLabel() {
		return detailsLabel;
	}

	public String getDetails() {
		return details;
	}

	public String getCustomPublishedIn() {
		return customPublishedIn;
	}

	public CatalogStatusEvaluator getStatusEvaluator() {
		return statusEvaluator;
	}

	public String getStatusPeriodOption() {
		return statusPeriodOption;
	}

	public SelectionValues getAvailableStatuses() {
		return availableStatuses;
	}

	public Set<String> getDefaultStatuses() {
		return defaultStatuses;
	}

	public boolean isFullyBooked() {
		return fullyBooked;
	}

	public boolean isStartDateAvailable() {
		return startDateAvailable;
	}

	public boolean isEndDateAvailable() {
		return endDateAvailable;
	}

	public String getEditBusinessPath() {
		return editBusinessPath;
	}

	public String getEditLabel() {
		return editLabel;
	}

	public String getCatalogBusinessPath() {
		return catalogBusinessPath;
	}

	public String getWebCatalogBusinessPath() {
		return webCatalogBusinessPath;
	}

	public Collection<TaxonomyLevel> getMicrosites() {
		return microsites;
	}

	public boolean isShowQRCode() {
		return showQRCode;
	}
	
	public SortPriorityProvider getSortPriorityProvider() {
		return sortPriorityProvider;
	}

	public interface CatalogStatusEvaluator {
		
		boolean isVisibleStatusNoPeriod();
		
		boolean isVisibleStatusPeriod();
		
	}
	
	private static final class TrueStatusEvaluator implements CatalogStatusEvaluator {

		@Override
		public boolean isVisibleStatusNoPeriod() {
			return true;
		}

		@Override
		public boolean isVisibleStatusPeriod() {
			return true;
		}
		
	}
	
	public interface SortPriorityProvider {
		
		Integer getPriority();
		
		void setPriority(Integer priority);
		
	}

}