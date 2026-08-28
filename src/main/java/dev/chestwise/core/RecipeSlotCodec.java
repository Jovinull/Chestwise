package dev.chestwise.core;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Wire format for a recipe pushed from a recipe viewer onto the terminal grid.
 *
 * <p>A recipe is described by the item ids each slot will accept, rather than by
 * a recipe id, because every recipe viewer exposes its ingredients differently
 * and the vanilla recipe API is shaped differently on each Minecraft generation.
 * Listing the acceptable ids also lets a tag ingredient be satisfied by whichever
 * member the player actually owns.
 *
 * <p>The encoding is slot-major: {@value #SLOTS} groups separated by {@code ';'},
 * each an item id list separated by {@code ','}. An empty group means the slot
 * stays empty.
 */
public final class RecipeSlotCodec {
    /** A vanilla crafting grid; the terminal offers no larger one. */
    public static final int SLOTS = 9;

    /** Caps tag-heavy slots so the payload stays well inside the packet limit. */
    public static final int MAX_ALTERNATIVES = 8;

    private static final String SLOT_SEPARATOR = ";";
    private static final String ID_SEPARATOR = ",";

    private RecipeSlotCodec() {
    }

    /**
     * Encodes one group per slot, padding to {@link #SLOTS} and dropping blank or
     * duplicate ids. Groups past the grid size are ignored rather than shifting
     * the remaining slots.
     */
    public static String encode(List<? extends List<String>> slots) {
        List<String> groups = new ArrayList<>(SLOTS);
        for (int slot = 0; slot < SLOTS; slot++) {
            groups.add(slot < slots.size() ? encodeSlot(slots.get(slot)) : "");
        }
        return String.join(SLOT_SEPARATOR, groups);
    }

    private static String encodeSlot(List<String> ids) {
        if (ids == null) {
            return "";
        }
        Set<String> unique = new LinkedHashSet<>();
        for (String id : ids) {
            if (id == null) {
                continue;
            }
            String trimmed = id.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            unique.add(trimmed);
            if (unique.size() >= MAX_ALTERNATIVES) {
                break;
            }
        }
        return String.join(ID_SEPARATOR, unique);
    }

    /**
     * Decodes a payload into exactly {@link #SLOTS} groups. Malformed input yields
     * empty groups instead of throwing: the payload arrives over the network and a
     * bad one must never take the server down.
     */
    public static List<List<String>> decode(String encoded) {
        List<List<String>> slots = new ArrayList<>(SLOTS);
        String[] groups = encoded == null ? new String[0] : encoded.split(SLOT_SEPARATOR, -1);
        for (int slot = 0; slot < SLOTS; slot++) {
            slots.add(slot < groups.length ? decodeSlot(groups[slot]) : List.of());
        }
        return slots;
    }

    private static List<String> decodeSlot(String group) {
        List<String> ids = new ArrayList<>();
        for (String id : group.split(ID_SEPARATOR, -1)) {
            String trimmed = id.trim();
            if (!trimmed.isEmpty() && !ids.contains(trimmed)) {
                ids.add(trimmed);
            }
            if (ids.size() >= MAX_ALTERNATIVES) {
                break;
            }
        }
        return List.copyOf(ids);
    }
}
