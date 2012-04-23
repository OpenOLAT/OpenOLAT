package org.olat.search.service.indexer;

import java.io.IOException;

import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.logging.LogDelegator;
import org.olat.search.service.SearchResourceContext;

/**
 * 
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public abstract class DefaultIndexer extends LogDelegator implements Indexer {

	private IndexerAccessSecurityCallback securityCallback;
	
	/**
	 * [used by Spring]
	 * @param securityCallback
	 */
	public void setSecurityCallback(IndexerAccessSecurityCallback securityCallback) {
		this.securityCallback = securityCallback;
	}
	
	@Override
	public abstract String getSupportedTypeName();
	
	
	
	@Override
	public abstract void doIndex(SearchResourceContext searchResourceContext, Object parentObject, OlatFullIndexer indexer)
			throws IOException, InterruptedException;

	@Override
	public boolean checkAccess(ContextEntry contextEntry, BusinessControl businessControl, Identity identity, Roles roles) {
		if(securityCallback != null) {
			return securityCallback.checkAccess(contextEntry, businessControl, identity, roles);
		}
		return true;
	}
}