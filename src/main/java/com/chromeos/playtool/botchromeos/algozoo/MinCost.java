package com.chromeos.playtool.botchromeos.algozoo;

import com.chromeos.playtool.common.model.Pair;

import java.util.*;

public class MinCost {

    private static final int MAX_VERTICES = 2000;
    private static final int MAX_EDGES = 20000;
    private static final int INF = 1000000000;

    private int vertices, edges, source, sink, send;
    private int[] dist = new int[MAX_VERTICES];
    private int[] weight = new int[MAX_EDGES];
    private int[] capacity = new int[MAX_EDGES];
    private int[] flow = new int[MAX_EDGES];
    private boolean[] inQueue = new boolean[MAX_VERTICES];
    private Pair[] trace = new Pair[MAX_VERTICES];
    private List<Pair>[] g = new List[MAX_VERTICES];

    public MinCost(int vertices, int source, int sink, int send) {
        this.vertices = vertices;
        this.source = source;
        this.sink = sink;
        this.send = send;
        for (int i = 1; i <= vertices; i++)
            g[i] = new ArrayList<>();
    }

    public void addEdge(int u, int v, int w, int c) {
        g[u].add(new Pair(v, ++edges));
        weight[edges] = w;
        capacity[edges] = c;
        g[v].add(new Pair(u, ++edges));
        weight[edges] = w;
    }

    private boolean findPath() {
        for (int i = 1; i <= vertices; i++)
            dist[i] = INF;
        dist[source] = 0;
        inQueue[source] = true;
        Queue<Integer> Q = new LinkedList<>();
        Q.add(source);
        while (Q.size() > 0) {
            int u = Q.peek();
            Q.remove();
            inQueue[u] = false;
            for (Pair tmp : g[u]) {
                int v = tmp.first, id = tmp.second;
                if (flow[id] < capacity[id] && dist[v] > dist[u] + (flow[id] >= 0 ? weight[id] : -weight[id])) {
                    dist[v] = dist[u] + (flow[id] >= 0 ? weight[id] : -weight[id]);
                    trace[v] = new Pair(u, id);
                    if (!inQueue[v]) {
                        inQueue[v] = true;
                        Q.add(v);
                    }
                }
            }
        }
        return dist[sink] < INF;
    }

    private int enlarge(int incFlow) {
        int u = sink;
        while (u != source) {
            int id = trace[u].second;
            incFlow = Math.min(incFlow, (flow[id] >= 0 ? capacity[id] - flow[id] : -flow[id]));
            u = trace[u].first;
        }
        u = sink;
        while (u != source) {
            int id = trace[u].second;
            flow[id] += incFlow;
            flow[id + (id % 2 == 0 ? -1 : 1)] -= incFlow;
            u = trace[u].first;
        }
        return incFlow;
    }

    public int minCost() {
        for (int i = 1; i <= vertices; i++)
            Collections.shuffle(g[i]);
        int rs = 0;
        while (findPath()) {
            int incFlow = enlarge(send);
            send -= incFlow;
            rs += incFlow * dist[sink];
            if (send == 0)
                break;
        }
        return (send > 0 ? -1 : rs);
    }

    public int find(int id) {
        for (Pair temp : g[id])
            if (flow[temp.second] == 1)
                return temp.first;
        return 0;
    }
}
