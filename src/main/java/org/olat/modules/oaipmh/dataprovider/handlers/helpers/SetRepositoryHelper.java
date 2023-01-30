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

import java.util.ArrayList;
import java.util.List;

import org.olat.modules.oaipmh.dataprovider.handlers.results.ListSetsResult;
import org.olat.modules.oaipmh.dataprovider.model.Context;
import org.olat.modules.oaipmh.dataprovider.model.Set;
import org.olat.modules.oaipmh.dataprovider.repository.SetRepository;

public class SetRepositoryHelper {
	private SetRepository setRepository;

	public SetRepositoryHelper(SetRepository setRepository) {
		super();
		this.setRepository = setRepository;
	}

	public ListSetsResult getSets(Context context, int offset, int length) {
		List<Set> results = new ArrayList<>();
		List<Set> statics = context.getSets();
		if (offset < statics.size()) {
			if (length + offset < statics.size()) {
				for (int i = offset; i < (offset + length); i++)
					results.add(statics.get(i));
				return new ListSetsResult(true, results);
			} else {
				for (int i = offset; i < statics.size(); i++)
					results.add(statics.get(i));
				int newLength = length - (statics.size() - offset);
				ListSetsResult res = setRepository.retrieveSets(0, newLength);
				results.addAll(res.getResults());
				if (!res.hasTotalResults())
					return new ListSetsResult(res.hasMore(), results);
				else
					return new ListSetsResult(res.hasMore(), results, res.getTotalResults() + statics.size());
			}
		} else {
			int newOffset = offset - statics.size();
			ListSetsResult res = setRepository.retrieveSets(newOffset, length);
			results.addAll(res.getResults());
			if (!res.hasTotalResults())
				return new ListSetsResult(res.hasMore(), results);
			else
				return new ListSetsResult(res.hasMore(), results, res.getTotalResults() + statics.size());
		}
	}

	public boolean exists(Context context, String setSpec) {
		List<Set> statics = context.getSets();
		for (Set set : statics)
			if (set.getSpec().equals(set))
				return true;

		return setRepository.exists(setSpec);
	}

}
