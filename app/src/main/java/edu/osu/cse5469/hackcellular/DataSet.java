package edu.osu.cse5469.hackcellular;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fengyuhui on 15/10/8.
 */
// To store Data volume from local and operator associated with their time stamp in a list.

public class DataSet {
    private List<VolumeData> list;
    private int size;

    public DataSet() {
        list = new ArrayList<VolumeData>();
        size = 0;
    }

    public void addData(VolumeData tmpData) {
        list.add(tmpData);
        size++;
    }

    public int size() {
        return size;
    }

    public VolumeData getData() {
        if (!list.isEmpty())
            return list.get(size - 1);
        else return null;
    }

    public VolumeData getData(int index) {
        if (size > index)
            return list.get(index);
        else return null;
    }

}
