package org.olat.core.logging.activity;

import org.olat.core.id.context.ContextEntry;

public interface ILoggingResourceable {

	/**
	 * Checks whether this LoggingResourceable represents the same resource as the
	 * given ContextEntry.
	 * <p>
	 * This is used during the businessPath check.
	 * @param ce
	 * @return
	 */
	public boolean correspondsTo(ContextEntry ce);

	/**
	 * Returns the type of this LoggingResourceable - this is the OlatResourceable's type
	 * (in case this LoggingResource represents a OlatResourceable) - or the StringResourceableType's enum name()
	 * otherwise
	 * @return the type of this LoggingResourceable
	 */
	public String getType();

	/**
	 * Returns the id of this LoggingResourceable - the id varies depending on the type of this
	 * LoggingResourceable - but usually it is the olatresourceable id or the olatresource id.
	 * @return the id of this LoggingResourceable
	 */
	public String getId();

	/**
	 * Returns the name of this LoggingResourceable - the name varies depending on the type
	 * of this LoggingResource - e.g. in the course case it is the name of the course, in
	 * the CP case it is the html filename incl path
	 * @return
	 */
	public String getName();
	
	/**
	 * Returns the ILoggingResourceableType of this LoggingResourceable - used for businessPath checking
	 * @return the ILoggingResourceableType of this LoggingResourceable
	 */
	public ILoggingResourceableType getResourceableType();
}
