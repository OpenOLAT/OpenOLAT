package org.olat.repository.ui.list;

import org.olat.repository.RepositoryEntryMyView;

import java.util.LinkedHashMap;
import java.util.List;

/**
 * Initial date: 2016-06-29<br />
 * @author sev26 (UZH)
 */
public interface RepositoryEntryRowsFactory {

	// Must be LinkedHashMap to preserve insertion order of repositoryEntryViews (sorted)
	LinkedHashMap<RepositoryEntryMyView, RepositoryEntryRow> create(List<RepositoryEntryMyView> repositoryEntryViews);

	RepositoryEntryDataSourceUIFactory getUiFactory();
}
