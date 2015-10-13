package edu.osu.cse5469.hackcellular;

/**
 * Created by fengyuhui on 15/10/8.
 * Data structure for each data in data charging issue
 */

public class VolumeData {
    private long timeStamp;
    private long local_data;
    private long operator_data;

    public VolumeData() {
    }

    public VolumeData(long timeStamp, long local_data, long operator_data) {
        this.timeStamp = timeStamp;
        this.local_data = local_data;
        this.operator_data = operator_data;
    }


    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getLocal_data() {
        return local_data;
    }

    public void setLocal_data(long local_data) {
        this.local_data = local_data;
    }

    public long getOperator_data() {
        return operator_data;
    }

    public void setOperator_data(long operator_data) {
        this.operator_data = operator_data;
    }
}
