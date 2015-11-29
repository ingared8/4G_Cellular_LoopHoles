package edu.osu.cse5469.hackcellular;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fengyuhui on 15/11/25.
 */
public class DataSet {

    private List<DataUnit> list;

    public DataSet() {
        list = new ArrayList<DataUnit>();
    }

    public int size() {
        return list.size();
    }

    public void add(DataUnit dataUnit) {
        list.add(dataUnit);
    }

    public DataUnit getData(int index) {
        if(list.size() > index)
            return list.get(index);
        else return null;
    }

    public DataUnit getLastData() {
        if(!list.isEmpty())
            return list.get(list.size()-1);
        else return null;
    }

}
