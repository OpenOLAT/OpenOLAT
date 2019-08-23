package org.olat.repository.listener;

import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryDeletionException;
import org.olat.resource.OLATResource;
import org.springframework.stereotype.Component;

/**
 * In order the event listener array is never null, one listener must exist.
 * Therefore this listener is implemented as class.
 *
 * Initial date: 2016-07-28<br />
 * @author Martin Schraner
 */
@Component
public class BeforeRepositoryEntryPermanentDeletionListener {

	/**
	 * This method should never commit the running database transaction.
	 */
	public void onAction(RepositoryEntry repositoryEntry, OLATResource resource) throws RepositoryEntryDeletionException {
	}
}
