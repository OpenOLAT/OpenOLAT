package org.olat.core.commons.modules.bc.meta;

import org.olat.core.util.vfs.OlatRelPathImpl;


public class MetaInfoFactory {
	
	private static MetaInfo metaInfo;

	/**
	 * [spring]
	 * @param metaInfo
	 */
	private MetaInfoFactory(MetaInfo metaInfo) {
		this.metaInfo = metaInfo;
	}

	public static MetaInfo createMetaInfoFor(OlatRelPathImpl path) {
		return metaInfo.createMetaInfoFor(path);
	}

}