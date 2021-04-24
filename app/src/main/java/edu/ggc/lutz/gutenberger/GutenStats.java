package edu.ggc.lutz.gutenberger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class GutenStats {

    private Map<String, Integer> words;
    private int[] stats;

    public GutenStats() {
        this.words = new HashMap<>();
        stats =new int[1];
    }

    public Map<String, Integer> getWords() {
        return words;
    }

    public void setWords(Map<String, Integer> wds) {
        words = wds;
    }

    public int[] getStats() {
        return stats;
    }

}
