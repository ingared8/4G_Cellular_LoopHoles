package edu.osu.cse5469.hackcellular;

/**
 * Created by fengyuhui on 15/11/12.
 */
public class NetStatus {

    private long timeStamp;
    private int status;

    public NetStatus() {

    }

    public NetStatus(long timeStamp, int status) {
        this.timeStamp = timeStamp;
        this.status = status;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public int getStatus() {
        return status;
    }

    public void setTimeStamp(int timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
