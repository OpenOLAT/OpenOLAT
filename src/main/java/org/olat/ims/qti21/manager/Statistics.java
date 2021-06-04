/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.ims.qti21.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * Thanks to: http://stackoverflow.com/questions/7988486/how-do-you-calculate-the-variance-median-and-standard-deviation-in-c-or-java
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Statistics {
    private final double[] data;
    private final double size;    

    public Statistics(double[] data) {
        this.data = data;
        size = data.length;
    }   

    public double getMean() {
        double sum = 0.0;
        for(double a : data) {
            sum += a;
        }
        return sum/size;
    }

    public double getVariance() {
        double mean = getMean();
        double temp = 0;
        for(double a :data) {
            temp += (mean-a)*(mean-a);
        }
        return temp/size;
    }

    public double getStdDev() {
        return Math.sqrt(getVariance());
    }

    public double median() {
    	if(data.length == 0) return -1.0;
    	
       double[] b = new double[data.length];
       System.arraycopy(data, 0, b, 0, b.length);
       Arrays.sort(b);

       if (data.length % 2 == 0) {
          return (b[(b.length / 2) - 1] + b[b.length / 2]) / 2.0;
       } else {
          return b[b.length / 2];
       }
    }
    

    public List<Double> mode() {
    	final List<Double> modes = new ArrayList<>();
        final Map<Double, Integer> countMap = new HashMap<>();

        int max = -1;

        double[] numbers = new double[data.length];
        System.arraycopy(data, 0, numbers, 0, numbers.length);
        for (final double n : numbers) {
            int count = 0;

            if (countMap.containsKey(n)) {
                count = countMap.get(n) + 1;
            } else {
                count = 1;
            }

            countMap.put(n, count);

            if (count > max) {
                max = count;
            }
        }

        for (final Map.Entry<Double, Integer> tuple : countMap.entrySet()) {
            if (tuple.getValue() == max) {
                modes.add(tuple.getKey());
            }
        }

	    return modes;
    }
}
