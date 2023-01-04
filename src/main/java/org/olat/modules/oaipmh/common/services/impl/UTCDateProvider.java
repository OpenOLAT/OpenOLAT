/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 * http://www.dspace.org/license/
 *
 * Content got modified for OpenOlat Context
 */

package org.olat.modules.oaipmh.common.services.impl;

import org.olat.modules.oaipmh.common.model.Granularity;
import org.olat.modules.oaipmh.common.services.api.DateProvider;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class UTCDateProvider implements DateProvider {

    private static final String SIMPLEDATETIMEFORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
    private static final String SIMPLEDATEFORMAT = "yyyy-MM-dd";
    private static final String TIMEZONE = "CET";

    @Override
    public String format(Date date) {
        SimpleDateFormat format = new SimpleDateFormat(SIMPLEDATETIMEFORMAT);
        format.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
        return format.format(date);
    }

    @Override
    public Date now() {
        return new Date();
    }

    @Override
    public String format(Date date, Granularity granularity) {
        SimpleDateFormat format = new SimpleDateFormat(SIMPLEDATETIMEFORMAT);
        if (granularity == Granularity.Day)
            format = new SimpleDateFormat(SIMPLEDATEFORMAT);
        format.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
        return format.format(date);
    }

    @Override
    public Date parse(String date, Granularity granularity) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(SIMPLEDATETIMEFORMAT);
        if (granularity == Granularity.Day)
            format = new SimpleDateFormat(SIMPLEDATEFORMAT);
        format.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
        return format.parse(date);
    }

    @Override
    public Date parse(String string) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(SIMPLEDATETIMEFORMAT);
        format.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
        try {
            return format.parse(string);
        } catch (ParseException e) {
            format = new SimpleDateFormat(SIMPLEDATEFORMAT);
            format.setTimeZone(TimeZone.getTimeZone(TIMEZONE));
            return format.parse(string);
        }
    }
}
