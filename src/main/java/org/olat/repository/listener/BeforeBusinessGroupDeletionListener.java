package org.olat.repository.listener;

import org.olat.group.BusinessGroup;
import org.springframework.stereotype.Component;

/**
 * In order the event listener array is never null, one listener must exist.
 * Therefore this listener is implemented as class.
 *
 * Initial date: 2016-11-02<br />
 * @author Martin Schraner
 */
@Component
public class BeforeBusinessGroupDeletionListener {

	/**
	 * This method should never commit the running database transaction.
	 */
	public void onAction(BusinessGroup businessGroup) {
	}
}
