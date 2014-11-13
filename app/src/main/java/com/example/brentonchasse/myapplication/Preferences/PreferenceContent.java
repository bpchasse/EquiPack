package com.example.brentonchasse.myapplication.Preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class PreferenceContent {

    /**
     * An array of sample (dummy) items.
     */
    public static List<PreferenceOption> ITEMS = new ArrayList<PreferenceOption>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<String, PreferenceOption> ITEM_MAP = new HashMap<String, PreferenceOption>();

    static {
        // Add 3 sample items.
        addItem(new PreferenceOption("1", "Preference 1"));
        addItem(new PreferenceOption("2", "Preference 2"));
        addItem(new PreferenceOption("3", "Preference 3"));
    }

    private static void addItem(PreferenceOption item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class PreferenceOption {
        public String id;
        public String content;

        public PreferenceOption(String id, String content) {
            this.id = id;
            this.content = content;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
