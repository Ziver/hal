package se.hal.util;

import se.hal.daemon.SensorDataAggregatorDaemon.AggregationPeriodLength;

public class UTCTimePeriod{
    private final long start;
    private final long end;
    private final AggregationPeriodLength periodLength;

    public UTCTimePeriod(long timestamp, AggregationPeriodLength periodLength){
        start = UTCTimeUtility.getTimestampPeriodStart(periodLength, timestamp);
        end = UTCTimeUtility.getTimestampPeriodEnd(periodLength, timestamp);
        this.periodLength = periodLength;
    }

    public long getStartTimestamp(){
        return start;
    }

    public long getEndTimestamp(){
        return end;
    }

    public UTCTimePeriod getNextPeriod(){
        return new UTCTimePeriod(end+1, periodLength);
    }

    public UTCTimePeriod getPreviosPeriod(){
        return new UTCTimePeriod(start-1, periodLength);
    }

    public boolean containsTimestamp(long timestamp){
        return start <= timestamp && timestamp <= end;
    }

    public boolean equals(Object other){
        if(other == null)
            return false;
        if(other instanceof UTCTimePeriod){
            UTCTimePeriod o = (UTCTimePeriod)other;
            return start == o.start
                && end == o.end
                && periodLength == o.periodLength;
        }
        return false;
    }

    public String toString(){
        return start + "=>" + end + " (" + UTCTimeUtility.getDateString(start) + "=>" + UTCTimeUtility.getDateString(end) + ")";
    }

}