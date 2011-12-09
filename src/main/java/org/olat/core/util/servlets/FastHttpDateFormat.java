/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.core.util.servlets;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utility class to generate HTTP dates.
 * 
 * @author Remy Maucherat
 */
public final class FastHttpDateFormat {


    // -------------------------------------------------------------- Variables


    /**
     * HTTP date format.
     */
    protected static SimpleDateFormat format = 
        new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);


    protected final static TimeZone gmtZone = TimeZone.getTimeZone("GMT");


    /**
     * GMT timezone - all HTTP dates are on GMT
     */
    static {
        format.setTimeZone(gmtZone);
    }


    /**
     * Instant on which the currentDate object was generated.
     */
    protected static long currentDateGenerated = 0L;


    /**
     * Current formatted date.
     */
    protected static String currentDate = null;


    /**
     * Date cache.
     */
    protected static HashMap dateCache = new HashMap();


    // --------------------------------------------------------- Public Methods


    /**
     * Get the current date in HTTP format.
     */
    public static String getCurrentDate() {

        long now = System.currentTimeMillis();
        if ((now - currentDateGenerated) > 1000) {
            synchronized (format) {  //o_clusterOK by:fj
                if ((now - currentDateGenerated) > 1000) {
                    currentDateGenerated = now;
                    currentDate = format.format(new Date(now));
                }
            }
        }
        return currentDate;

    }


    /**
     * Get the HTTP format of the specified date.
     */
    public static String getDate(Date date) {

        String cachedDate = (String) dateCache.get(date);
        if (cachedDate != null)
            return cachedDate;

        String newDate = null;
        synchronized (format) {  //o_clusterOK by:fj
            newDate = format.format(date);
            dateCache.put(date, newDate);
        }
        return newDate;

    }


}
