package org.olat.repository.ui.author;

import org.olat.repository.RepositoryEntryAuthorView;

/**
 * @author Martin Schraner
 */
public interface AuthoringEntryRowFactory {

	AuthoringEntryRow create(RepositoryEntryAuthorView view, String fullnameAuthor);
}
