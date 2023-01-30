/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 *
 * Content got modified for OpenOlat Context
 */

package org.olat.modules.oaipmh.dataprovider.handlers.helpers;

import org.olat.modules.oaipmh.dataprovider.model.ItemIdentifier;

public class ItemIdentifyHelper {
	private ItemIdentifier item;

	public ItemIdentifyHelper(ItemIdentifier item) {
		this.item = item;
	}
//
//    public List<ReferenceSet> getSets(XOAIContext context) {
//        List<ReferenceSet> list = this.item.getSets();
//        for (Set set : context.getStaticSets()) {
//            if (set.hasCondition() && set.getCondition().getFilter().isItemShown(item))
//                list.add(set);
//            else
//                list.add(set);
//        }
//        return list;
//    }


}
