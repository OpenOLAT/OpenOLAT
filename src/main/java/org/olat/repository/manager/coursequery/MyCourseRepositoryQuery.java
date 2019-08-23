package org.olat.repository.manager.coursequery;

import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams;

import java.util.List;

/**
 * This interface is required in order that the course search can be extended
 * with other sources. An example is the UZH Campuskurs source: campus
 * courses, that were synchronized with a third party database but have not
 * yet been created, should be found and listed.
 *
 * Initial date: 2016-06-15<br />
 * @author sev26 (UZH)
 */
public interface MyCourseRepositoryQuery {
	int countViews(SearchMyRepositoryEntryViewParams param);
	List<RepositoryEntryMyView> searchViews(SearchMyRepositoryEntryViewParams param, int firstResult, int maxResults);
}
