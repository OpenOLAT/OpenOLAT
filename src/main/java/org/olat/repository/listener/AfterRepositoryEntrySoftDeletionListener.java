package org.olat.repository.listener;

import org.olat.repository.RepositoryEntry;
import org.springframework.stereotype.Component;

/**
 * In order the event listener array is never null, one listener must exist.
 * Therefore this listener is implemented as class.
 *
 * Initial date: 2017-07-19<br />
 * @author Martin Schraner
 */
@Component
public class AfterRepositoryEntrySoftDeletionListener {

	/**
	 * This method should never commit the running database transaction.
	 */
	public void onAction(RepositoryEntry repositoryEntry) {
	}
}
