package org.olat.modules.assessment.service;

import java.math.BigDecimal;

/**
 * Simple DTO holding aggregate grade statistics.
 */
public class GradeStatistics {

    private final long count;
    private final BigDecimal mean;
    private final BigDecimal median;
    private final BigDecimal stddev;
    private final BigDecimal min;
    private final BigDecimal max;

    public GradeStatistics(long count, BigDecimal mean, BigDecimal median, BigDecimal stddev, BigDecimal min, BigDecimal max) {
        this.count = count;
        this.mean = mean;
        this.median = median;
        this.stddev = stddev;
        this.min = min;
        this.max = max;
    }

    public long getCount() {
        return count;
    }

    public BigDecimal getMean() {
        return mean;
    }

    public BigDecimal getMedian() {
        return median;
    }

    public BigDecimal getStddev() {
        return stddev;
    }

    public BigDecimal getMin() {
        return min;
    }

    public BigDecimal getMax() {
        return max;
    }
}
