package org.olat.core.logging.activity;

/**
 * Global list of actionVerbs existing in OLAT.
 * <p>
 * The actionVerb will be stored to the logging table
 * and the idea is to keep this list short and clean
 * <P>
 * Note that the result of ActionVerb.name() will be
 * used directly as the String stored to the database -
 * hence use a meaningful, short (16 characters at max)
 * String.
 * <p>
 * Initial Date:  10.11.2009 <br>
 * @author Stefan
 */
public enum ActionVerb {
	
	add,
	close,
	copy,
	denied,
	edit,
	exit,
	hide,
	launch,
	lock,
	move,
	open,
	perform,
	remove,
	view

}
