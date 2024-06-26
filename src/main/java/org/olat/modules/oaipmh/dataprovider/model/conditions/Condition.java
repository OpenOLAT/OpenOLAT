/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 */

package org.olat.modules.oaipmh.dataprovider.model.conditions;

import org.olat.modules.oaipmh.dataprovider.filter.Filter;
import org.olat.modules.oaipmh.dataprovider.filter.FilterResolver;

/**
 * @author Development @ Lyncode
 * @version 3.1.0
 */
public interface Condition {
    Filter getFilter (FilterResolver filterResolver);
}
