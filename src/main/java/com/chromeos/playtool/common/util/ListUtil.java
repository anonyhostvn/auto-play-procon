package com.chromeos.playtool.common.util;

import java.util.ArrayList;
import java.util.List;

public class ListUtil {

    public static List<List<Integer>> init(List<List<Integer>> list, int width, int height) {
        list = new ArrayList<>();
        for (int i = 0; i < height; i++) {
            list.add(new ArrayList<>());
            for (int j = 0; j < width; j++)
                list.get(i).add(0);
        }
        return list;
    }

    public static int get(List<List<Integer>> list, int x, int y) {
        if (1 <= y && y <= list.size() && 1 <= x && x <= list.get(y - 1).size())
            return list.get(y - 1).get(x - 1);
        return 0;
    }

    public static List<List<Integer>> set(List<List<Integer>> list, int x, int y, int value) {
        if (1 <= y && y <= list.size() && 1 <= x && x <= list.get(y - 1).size())
            list.get(y - 1).set(x - 1, value);
        return list;
    }
}
