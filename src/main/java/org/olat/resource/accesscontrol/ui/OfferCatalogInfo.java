/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.resource.accesscontrol.ui;

import java.util.Date;
import java.util.List;

import org.olat.core.gui.translator.Translator;
import org.olat.resource.accesscontrol.CatalogInfo.CatalogStatusEvaluator;
import org.olat.resource.accesscontrol.Offer;

/**
 * 
 * Initial date: Jan 30, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class OfferCatalogInfo {
	
	public enum OfferCatalogStatus { 
		pending,
		bookable,
		finished,
		notAvailable,
		fullyBooked
	}
	
	private final OfferCatalogStatus status;
	private final boolean active;
	private final boolean withPeriod;
	private final Date from;
	private final Date to;
	private final boolean published;
	private final boolean webPublished;
	
	public OfferCatalogInfo(OfferCatalogStatus status, boolean active, boolean withPeriod, Date from, Date to, boolean published, boolean webPublished) {
		this.status = status;
		this.active = active;
		this.withPeriod = withPeriod;
		this.from = from;
		this.to = to;
		this.published = published;
		this.webPublished = webPublished;
	}
	
	/**
	 * @return pending, bookable, finished, notAvailable
	 */
	public OfferCatalogStatus getStatus() {
		return status;
	}

	public boolean isActive() {
		return active;
	}

	public boolean isWithPeriod() {
		return withPeriod;
	}

	public Date getFrom() {
		return from;
	}

	public Date getTo() {
		return to;
	}

	public boolean isPublished() {
		return published;
	}

	public boolean isWebPublished() {
		return webPublished;
	}

	public static OfferCatalogInfo create(Offer offer, CatalogStatusEvaluator statusEvaluator) {
		OfferCatalogStatus status;
		boolean active = true;
		boolean withPeriod = false;
		
		Date from = offer.getValidFrom();
		Date to = offer.getValidTo();
		if (to != null && to.before(new Date())) {
			status = OfferCatalogStatus.finished;
			active = false;
			withPeriod = true;
		} else if (from != null && from.after(new Date())) {
			status = OfferCatalogStatus.pending;
			active = false;
			withPeriod = true;
		} else if (to != null && to.after(new Date())) {
			if (statusEvaluator != null && !statusEvaluator.isVisibleStatusPeriod()) {
				status = OfferCatalogStatus.notAvailable;
			} else {
				status = OfferCatalogStatus.bookable;
				
			}
			withPeriod = true;
		} else {
			if (statusEvaluator != null && !statusEvaluator.isVisibleStatusNoPeriod()) {
				status = OfferCatalogStatus.notAvailable;
			} else {
				status = OfferCatalogStatus.bookable;
			}
		}
		
		return new OfferCatalogInfo(status, active, withPeriod, from, to, offer.isCatalogPublish(), offer.isCatalogWebPublish());
	}
	
	public static final String getIconCss(OfferCatalogStatus status) {
		return switch (status) {
		case pending -> "o_ac_offer_pending_icon";
		case bookable -> "o_ac_offer_bookable_icon";
		case finished -> "o_ac_offer_finished_icon";
		case notAvailable -> "o_ac_offer_not_available_icon";
		case fullyBooked -> "o_ac_offer_fully_booked_icon";
		default -> null;
		};
	}
	
	public static final String getLabelCss(OfferCatalogStatus status) {
		return switch (status) {
		case pending -> "o_ac_offer_pending";
		case bookable -> "o_ac_offer_bookable";
		case finished -> "o_ac_offer_finished";
		case notAvailable -> "o_ac_offer_not_available";
		case fullyBooked -> "o_ac_offer_fully_booked";
		default -> null;
		};
	}
	
	public static final String getLabelName(Translator translator, OfferCatalogStatus status) {
		return switch (status) {
		case pending -> translator.translate("offer.status.pending");
		case bookable -> translator.translate("offer.status.bookable");
		case finished -> translator.translate("offer.status.finished");
		case notAvailable -> translator.translate("offer.status.not.available");
		case fullyBooked -> translator.translate("offer.status.fully.booked");
		default -> null;
		};
	}
	
	public static final String getStatusLightLabel(Translator translator, OfferCatalogStatus status) {
		return "<span class=\"o_labeled_light "
				+ OfferCatalogInfo.getLabelCss(status) + "\"><i class=\"o_icon "
				+ OfferCatalogInfo.getIconCss(status) + "\"> </i> "
				+ OfferCatalogInfo.getLabelName(translator, status) + "</span>";
	}
	
	/**
	 * @return bookable, notAvailable, fullyBooked
	 */
	public static final OfferCatalogStatus getCatalogStatus(List<OfferCatalogInfo> offerCatalogInfos, CatalogStatusEvaluator statusEvaluator, boolean fullyBooked) {
		OfferCatalogStatus catalogStatus = null;
		boolean atLeastOneActive = offerCatalogInfos.stream().anyMatch(OfferCatalogInfo::isActive);
		if (atLeastOneActive) {
			if (fullyBooked) {
				catalogStatus = OfferCatalogStatus.fullyBooked;
			} else if(statusEvaluator != null) {
				boolean statusVisible = offerCatalogInfos.stream().anyMatch(OfferCatalogInfo::isWithPeriod)
						? statusEvaluator.isVisibleStatusPeriod()
						: statusEvaluator.isVisibleStatusNoPeriod();
				if (!statusVisible) {
					catalogStatus = OfferCatalogStatus.notAvailable;
				}
			}
			if (catalogStatus == null) {
				catalogStatus = OfferCatalogStatus.bookable;
			}
		}
		if (catalogStatus == null) {
			catalogStatus = OfferCatalogStatus.notAvailable;
		}
		return catalogStatus;
	}

}
