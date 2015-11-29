package edu.osu.cse5469.hackcellular;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fengyuhui on 15/11/25.
 */
public class DataUnit {

    private long timeStamp;

    private List<Long> data;

    private List<String> dataType;              // The description of the responded data, i.e. Local Data Usage, Net Status and ...

    public DataUnit() {
        data = new ArrayList<Long>();
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public List<Long> getData() {
        return data;
    }

    public List<String> getDataType() {
        return dataType;
    }

    public void addData(String dataType, Long data) {
        this.dataType.add(dataType);
        this.data.add(data);
    }

    public int getDataDimesion() {
        return data.size();
    }

}
