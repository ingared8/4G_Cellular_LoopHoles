package edu.osu.cse5469.hackcellular;

import java.util.Vector;

/**
 * Created by fengyuhui on 15/11/25.
 */
public class DataUnit {

    private long timeStamp;

    private Vector<Float> data;

    private Vector<String> dataType;              // The description of the responded data, i.e. Local Data Usage, Net Status and ...

    private int size = 1;

    public DataUnit() {
        data = new Vector<Float>();
        dataType = new Vector<String>();
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public Vector<Float> getData() {
        return data;
    }

    public Vector<String> getDataType() {
        return dataType;
    }

    public void addData(String dataType, Float data) {
        this.dataType.add(dataType);
        this.data.add(data);
        size = this.data.size();
    }

    public int getDataDimension() {
        return size;
    }

}
