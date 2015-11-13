package edu.osu.cse5469.hackcellular;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fengyuhui on 15/11/12.
 */
public class NetStatSet {
    private List<NetStatus> list;

    public NetStatSet() {
        list = new ArrayList<NetStatus>();
    }

    public void add(NetStatus netStatus) {
        list.add(netStatus);
    }

    public NetStatus get(int i) {
        return list.get(i);
    }

    public int size() {
        return list.size();
    }

}
