/**
 *  RELOAD TOOLS
 *
 *  Copyright (c) 2003 Oleg Liber, Bill Olivier, Phillip Beauvoir, Paul Sharples
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in
 *  all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 *  Project Management Contact:
 *
 *  Oleg Liber
 *  Bolton Institute of Higher Education
 *  Deane Road
 *  Bolton BL3 5AB
 *  UK
 *
 *  e-mail:   o.liber@bolton.ac.uk
 *
 *
 *  Technical Contact:
 *
 *  Phillip Beauvoir
 *  e-mail:   p.beauvoir@bolton.ac.uk
 *
 *  Paul Sharples
 *  e-mail:   p.sharples@bolton.ac.uk
 *
 *  Web:      http://www.reload.ac.uk
 *
 */

package org.olat.modules.scorm.server.servermodels;

/**
 * A utility class used for operations on CMI datamodel elements.
 * 
 * @author Paul Sharples
*/
public final class ScoUtils {

    /**
     * Utility method to add two CMITimespan values together, such as
     * session_time and total_time from the CMI datamodel.
     *
     * Recognized formats:
     * HHHH:MM:SS.SS
     * HHH:MM:SS.SS
     * HH:MM:SS.SS
     * HHHH:MM:SS.S
     * HHH:MM:SS.S
     * HH:MM:SS.S
     * HHHH:MM:SS
     * HHH:MM:SS
     * HH:MM:SS
     * @param atotalTime - a cmi.core.total_time
     * @param asessionTime a cmi.core.session_time
     * @return a CMITimeSpan of the two added times. (String)
     */
    public static String addTimes(String atotalTime, String asessionTime) {
        int[] totalTime = parseTime(atotalTime);
        int[] sessionTime = parseTime(asessionTime);
        int millisecondCount = 0, millisecondsLeft = 0, secondCount = 0;
        int secondsLeft = 0, minutesCount = 0, minutesLeft = 0;

        int bothMilliseconds = totalTime[3] + sessionTime[3];
        if (bothMilliseconds > 99) {
            millisecondCount++;
            millisecondsLeft = bothMilliseconds - 100;
        }
        else
            millisecondsLeft = bothMilliseconds;

        int bothSeconds = totalTime[2] + sessionTime[2] + millisecondCount;
        if (bothSeconds > 59) {
            secondCount++;
            secondsLeft = bothSeconds - 60;
        }
        else
            secondsLeft = bothSeconds;

        int bothMinutes = totalTime[1] + sessionTime[1] + secondCount;
        if (bothMinutes > 59) {
            minutesCount++;
            minutesLeft = bothMinutes - 60;
        }
        else
            minutesLeft = bothMinutes;

        int bothHours = totalTime[0] + sessionTime[0] + minutesCount;

        String finalMilliseconds = Integer.toString(millisecondsLeft);
        if (finalMilliseconds.length() == 1)
            finalMilliseconds = "0" + millisecondsLeft;
        String finalSeconds = Integer.toString(secondsLeft);
        if (finalSeconds.length() < 2)
            finalSeconds = "0" + secondsLeft;
        String finalMinutes = Integer.toString(minutesLeft);
        if (finalMinutes.length() < 2)
            finalMinutes = "0" + minutesLeft;
        String finalHours = Integer.toString(bothHours);
        if (finalHours.length() < 2)
            finalHours = "0" + bothHours;
        else
            finalHours = Integer.toString(bothHours);

        String finalTime = "";
        if (!finalMilliseconds.equals("00"))
            finalTime = finalHours + ":" + finalMinutes + ":" + finalSeconds +
                "." + finalMilliseconds;
        else
            finalTime = finalHours + ":" + finalMinutes + ":" + finalSeconds;
        return finalTime;
    }


    /**
     * Calculates the total_time to store for a SCO: the total time as it was when
     * the SCO was launched plus the session time reported in the current session.
     * Because the result is always derived from the launch value, repeated commits
     * of the same session are idempotent and the session is counted exactly once.
     * A session time above maxSessionSeconds (e.g. a browser tab left open for days)
     * is capped, an empty or malformed session time is ignored.
     *
     * @param launchTotalTime the cmi.core.total_time stored when the SCO was launched
     * @param sessionTime the cmi.core.session_time sent by the SCO, can be null or empty
     * @param maxSessionSeconds the longest accepted session in seconds
     * @return a CMITimespan, or the launch total time if there is no usable session time
     */
    public static String addCappedSessionTime(String launchTotalTime, String sessionTime, int maxSessionSeconds) {
        long seconds = toSeconds(sessionTime);
        if (seconds < 0) {
            return launchTotalTime;
        }
        String cappedSessionTime = sessionTime.trim();
        if (seconds > maxSessionSeconds) {
            cappedSessionTime = formatSeconds(maxSessionSeconds);
        }
        try {
            return addTimes(launchTotalTime, cappedSessionTime);
        } catch (RuntimeException e) {
            // malformed launch total time
            return launchTotalTime;
        }
    }

    /**
     * @param time a CMITimespan, can be null or empty
     * @return the number of whole seconds of the timespan, or -1 if the
     *      timespan is empty or malformed
     */
    public static long toSeconds(String time) {
        if (time == null || time.trim().isEmpty()) {
            return -1;
        }
        try {
            int[] bits = parseTime(time.trim());
            return bits[0] * 3600L + bits[1] * 60L + bits[2];
        } catch (RuntimeException e) {
            // NumberFormatException or ArrayIndexOutOfBoundsException on a malformed timespan
            return -1;
        }
    }

    /**
     * @param seconds a positive number of seconds
     * @return a CMITimespan in the format HH:MM:SS
     */
    public static String formatSeconds(long seconds) {
        return String.format("%02d:%02d:%02d", seconds / 3600, (seconds % 3600) / 60, seconds % 60);
    }

    /**
     * Method to take in a CMITimespan string value and return an integer
     * array of the values it contains, so that we can can perform arithmetic
     * @param strtime strin CMITimespan
     * @return int array of values
     */
    public static int[] parseTime(String strtime) {
        int[] bits = new int[] {
            0, 0, 0, 0};
        String[] result = strtime.split(":");
        // do hours...
        bits[0] = Integer.parseInt(result[0]);
        // do minutes
        bits[1] = Integer.parseInt(result[1]);
        // do seconds
        if (result[2].indexOf(".") != -1) {
            // do milliseconds... (if exist)
            String[] millis = result[2].split("\\.");
            if (millis.length == 2) {
                bits[2] = Integer.parseInt(millis[0]);
                if (millis[1].length() == 1)
                    bits[3] = Integer.parseInt(millis[1]) * 10;
                else
                    bits[3] = Integer.parseInt(millis[1]);
            }
        }
        else {
            bits[2] = Integer.parseInt(result[2]);
        }
        return bits;
    }

}