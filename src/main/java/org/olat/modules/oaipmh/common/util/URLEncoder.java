/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 *
 * Content got modified for OpenOlat Context
 */

package org.olat.modules.oaipmh.common.util;

import java.io.UnsupportedEncodingException;


/**
 * @author Development @ Lyncode
 * @version 3.1.0
 */
public class URLEncoder {
    public static final String SEPARATOR = "&";

    public static String encode(String value) {
        try {
            return java.net.URLEncoder.encode(value, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }
}
