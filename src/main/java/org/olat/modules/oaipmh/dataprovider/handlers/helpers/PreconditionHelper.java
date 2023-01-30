/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 *
 * Content got modified for OpenOlat Context
 */
package org.olat.modules.oaipmh.dataprovider.handlers.helpers;

import org.olat.modules.oaipmh.dataprovider.exceptions.CannotDisseminateFormatException;
import org.olat.modules.oaipmh.dataprovider.model.Context;
import org.olat.modules.oaipmh.dataprovider.model.MetadataFormat;

/**
 * Helper class used to centralize preconditions for the different handlers.
 */
public class PreconditionHelper {

	/**
	 * Checks that the provided metadataPrefix can be retrieved from the given {@link Context}.
	 *
	 * @param context        expected to be not null
	 * @param metadataPrefix
	 * @throws CannotDisseminateFormatException
	 */
	public static void checkMetadataFormat(Context context, String metadataPrefix)
			throws CannotDisseminateFormatException {
		MetadataFormat format = context.formatForPrefix(metadataPrefix);
		if (format == null) {
			throw new CannotDisseminateFormatException("Format " + metadataPrefix + " unknown");
		}
	}
}
