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

import org.olat.modules.oaipmh.common.exceptions.InvalidResumptionTokenException;
import org.olat.modules.oaipmh.common.model.ResumptionToken;
import org.olat.modules.oaipmh.common.services.api.ResumptionTokenFormat;
import org.olat.modules.oaipmh.common.util.Base64Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class SimpleResumptionTokenFormat implements ResumptionTokenFormat {
    public ResumptionToken.Value parse(String resumptionToken) throws InvalidResumptionTokenException {
        if (resumptionToken == null) return new ResumptionToken.Value();
        int offset = 0;
        String set = null;
        Date from = null;
        Date until = null;
        String metadataPrefix = null;
        if (resumptionToken.trim().equals("")) {
            return new ResumptionToken.Value();
        } else {
            String s = Base64Utils.decode(resumptionToken);
            String[] pieces = s.split(Pattern.quote("|"));
            try {
                if (pieces.length > 0) {
                    offset = Integer.parseInt(pieces[0].substring(2));
                    if (pieces.length > 1) {
                        set = pieces[1].substring(2);
                        if (set != null && set.equals(""))
                            set = null;
                    }
                    if (pieces.length > 2) {
                        from = stringToDate(pieces[2].substring(2));
                    }
                    if (pieces.length > 3) {
                        until = stringToDate(pieces[3].substring(2));
                    }
                    if (pieces.length > 4) {
                        metadataPrefix = pieces[4].substring(2);
                        if (metadataPrefix != null && metadataPrefix.equals(""))
                            metadataPrefix = null;
                    }
                } else
                    throw new InvalidResumptionTokenException();
            } catch (Exception ex) {
                throw new InvalidResumptionTokenException(ex);
            }
        }
        return new ResumptionToken.Value()
                .withUntil(until)
                .withFrom(from)
                .withMetadataPrefix(metadataPrefix)
                .withOffset(offset)
                .withSetSpec(set);
    }

    @Override
    public String format(ResumptionToken.Value resumptionToken) {
        String s = "1:" + resumptionToken.getOffset();
        s += "|2:";
        if (resumptionToken.hasSetSpec())
            s += resumptionToken.getSetSpec();
        s += "|3:";
        if (resumptionToken.hasFrom())
            s += dateToString(resumptionToken.getFrom());
        s += "|4:";
        if (resumptionToken.hasUntil())
            s += dateToString(resumptionToken.getUntil());
        s += "|5:";
        if (resumptionToken.hasMetadataPrefix())
            s += resumptionToken.getMetadataPrefix();

        return Base64Utils.encode(s);
    }


    private String dateToString(Date date) {
        SimpleDateFormat formatDate = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss'Z'");
        return formatDate.format(date);
    }

    private Date stringToDate(String string) {
        SimpleDateFormat formatDate = new SimpleDateFormat(
                "yyyy-MM-dd'T'HH:mm:ss'Z'");
        try {
            return formatDate.parse(string);
        } catch (ParseException ex) {
            formatDate = new SimpleDateFormat(
                    "yyyy-MM-dd");
            try {
                return formatDate.parse(string);
            } catch (ParseException ex1) {
                return null;
            }
        }
    }


}
