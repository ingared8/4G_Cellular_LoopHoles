package edu.osu.cse5469.hackcellular;

import java.util.Vector;

/**
 * Created by fengyuhui on 15/11/25.
 */
public class DataSet {

    private Vector<DataUnit> list;

    public DataSet() {
        list = new Vector<DataUnit>();
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
