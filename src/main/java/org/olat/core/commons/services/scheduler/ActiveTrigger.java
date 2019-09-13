package org.olat.core.commons.services.scheduler;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.quartz.Trigger;

/**
 * Triggers should implement this interface such that they are added to the list of active triggers by the
 * @see org.olat.core.commons.services.scheduler.TriggerListFactory .
 *
 * @author Martin Schraner
 */
public interface ActiveTrigger {

	@Nullable
	Trigger getTrigger();
}
