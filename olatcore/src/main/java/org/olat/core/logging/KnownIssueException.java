package org.olat.core.logging;

/**
 * Use this exception when you want to identify a situation where an exception
 * occurs that we want to treat as a known issues case.
 * <p>
 * Known issues shall then be given a JIRA number and instances of this class
 * hence carry that number.
 * <p>
 * When this exception ends up in a log file, we can then treat this case
 * as a known issue and not let a test fail because of this - plus we have
 * a jira number which should speed up error analysis.
 * @author Stefan
 */
public class KnownIssueException extends OLATRuntimeException {

	private int jiraNumber;

	/**
	 * Includes throwable.
	 * @param logMsg
	 * @param cause
	 */
	public KnownIssueException(String logMsg, int jiraNumber, Throwable cause) {
		super(KnownIssueException.class, logMsg+" (OLAT-"+jiraNumber+")", cause);
		this.jiraNumber = jiraNumber;
	}
	
	/**
	 * Generic signature.
	 * @param logMsg
	 */
	public KnownIssueException(String logMsg, int jiraNumber) {
		this (logMsg, jiraNumber, new Exception("KnownIssueException<init>"));
	}
	
	/**
	 * JIRA Number without prefix
	 * @return
	 */
	public int getJiraNumber(){
		return jiraNumber;
	}
	
	/**
	 * convenience method to retrieve html fragment with link to jira
	 * e.g. <a href="http://bugs.olat.org/jira/browse/OLAT-XXXX">OLAT-XXXX</a>
	 * @return
	 */
	public String getJiraLink(){
		return "<a href='http://bugs.olat.org/jira/browse/OLAT-"+jiraNumber+"'>OLAT-"+jiraNumber+"</a>";
	}
	
}
