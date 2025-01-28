package org.olat.resource.accesscontrol.model;

import org.olat.resource.accesscontrol.Offer;
import org.olat.resource.accesscontrol.OfferAccess;

/**
 * 
 * Initial date: 27 janv. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public record OfferAndAccessInfos(Offer offer, OfferAccess offerAccess, int numOfOrders) {

}
