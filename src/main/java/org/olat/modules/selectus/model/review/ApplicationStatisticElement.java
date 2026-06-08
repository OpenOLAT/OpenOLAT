/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.model.review;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Initial date: 5 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationStatisticElement {
	
	private Integer sum;
	private Integer min;
	private Integer max;
	private List<Integer> reviews = new ArrayList<>();
	
	private final ReviewElementDefinition elementDefinition;

	public ApplicationStatisticElement(ReviewElementDefinition elementDefinition) {
		this.elementDefinition = elementDefinition;
	}
	
	public ReviewElementDefinition getElementDefinition() {
		return elementDefinition;
	}
	
	public String getLabel() {
		return elementDefinition.getLabel();
	}
	
	public String getType() {
		return elementDefinition.getType() == null ? "" : elementDefinition.getType().name();
	}
	
	public Integer getSum() {
		return sum;
	}
	
	public Integer getMin() {
		return min;
	}
	
	public Integer getMax() {
		return max;
	}
	
	public Double getAverage() {
		if(reviews.isEmpty() || sum == null) {
			return null;
		}

		double average = sum.doubleValue() / reviews.size();
		return Double.valueOf(average);
	}
	
    public double getVariance() {
        double mean = sum.doubleValue() / reviews.size();

        double temp = 0;
        for(Integer review :reviews) {
        	double a = review.doubleValue();
            temp += (mean-a)*(mean-a);
        }
        return temp / reviews.size();
    }

    /**
     * Population standard deviation
     * 
     * @return A double or null 
     */
    public Double getStandardDeviation() {
		if(reviews.isEmpty() || sum == null) {
			return null;
		}
    	
        double stdDev =  Math.sqrt(getVariance());
		return Double.valueOf(stdDev);
    }
	
	public int getNumOfReviews() {
		return reviews.size();
	}
	
	public void addValue(Integer val) {
		if(val == null) return;
		
		if(sum == null) {
			sum = val;
		} else {
			sum = Integer.sum(sum.intValue(), val.intValue());
		}
		
		if(min == null || min.intValue() > val.intValue()) {
			min = val;
		}
		if(max == null || max.intValue() < val.intValue()) {
			max = val;
		}
		reviews.add(val);
	}
	
	
}
