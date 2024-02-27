package org.olat.course.archiver.webdav;

import org.olat.core.commons.services.webdav.WebDAVProvider;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.util.vfs.VFSContainer;

/**
 * 
 * Initial date: 26 févr. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MyArchivesWebDAVProvider implements WebDAVProvider {

	private static final String MOUNTPOINT = "mycoursearchives";

	@Override
	public String getMountPoint() {
		return MOUNTPOINT;
	}
	
	@Override
	public boolean hasAccess(IdentityEnvironment identityEnv) {
		return identityEnv != null;
	}

	@Override
	public VFSContainer getContainer(IdentityEnvironment identityEnv) {
		return new MyArchivesWebDAVSource(identityEnv.getIdentity());
	}
}
