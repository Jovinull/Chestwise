package dev.chestwise.core;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Plans a complete, non-mutating assignment for a recipe-viewer transfer. */
public final class RecipeSlotPlanner {
    private RecipeSlotPlanner() {
    }

    /**
     * Assigns an available item id to each non-empty ingredient group. Empty
     * groups remain empty. Returns empty unless every requested ingredient can
     * be covered by the supplied per-id counts.
     */
    public static Optional<List<String>> plan(List<? extends List<String>> ingredients, Map<String, Long> available) {
        return plan(ingredients, available, Map.of());
    }

    /** Prefers item ids available in the player's unprotected inventory. */
    public static Optional<List<String>> plan(
        List<? extends List<String>> ingredients,
        Map<String, Long> available,
        Map<String, Long> playerAvailable
    ) {
        List<Integer> ingredientSlots = new ArrayList<>();
        Set<String> itemIds = new LinkedHashSet<>();
        for (int slot = 0; slot < ingredients.size(); slot++) {
            List<String> options = ingredients.get(slot);
            if (options == null || options.isEmpty()) {
                continue;
            }
            ingredientSlots.add(slot);
            itemIds.addAll(options);
        }
        if (ingredientSlots.isEmpty()) {
            return Optional.empty();
        }

        List<String> usableIds = itemIds.stream()
            .filter(id -> available.getOrDefault(id, 0L) > 0L)
            .toList();
        int source = 0;
        int firstIngredient = 1;
        int firstItem = firstIngredient + ingredientSlots.size();
        int sink = firstItem + usableIds.size();
        FlowNetwork network = new FlowNetwork(sink + 1);
        Map<String, Integer> itemNodes = new LinkedHashMap<>();
        List<List<ItemEdge>> assignments = new ArrayList<>(ingredientSlots.size());

        for (int item = 0; item < usableIds.size(); item++) {
            itemNodes.put(usableIds.get(item), firstItem + item);
        }

        for (int ingredient = 0; ingredient < ingredientSlots.size(); ingredient++) {
            int ingredientNode = firstIngredient + ingredient;
            network.addEdge(source, ingredientNode, 1);
            List<ItemEdge> edges = new ArrayList<>();
            List<String> candidates = new ArrayList<>(new LinkedHashSet<>(
                ingredients.get(ingredientSlots.get(ingredient))
            ));
            candidates.sort((left, right) -> Boolean.compare(
                playerAvailable.getOrDefault(right, 0L) > 0L,
                playerAvailable.getOrDefault(left, 0L) > 0L
            ));
            for (String id : candidates) {
                Integer itemNode = itemNodes.get(id);
                if (itemNode != null) {
                    Edge edge = network.addEdge(ingredientNode, itemNode, 1);
                    edges.add(new ItemEdge(id, edge));
                }
            }
            assignments.add(edges);
        }

        for (int item = 0; item < usableIds.size(); item++) {
            long count = available.getOrDefault(usableIds.get(item), 0L);
            network.addEdge(firstItem + item, sink, (int) Math.min(ingredientSlots.size(), count));
        }
        if (network.maxFlow(source, sink) != ingredientSlots.size()) {
            return Optional.empty();
        }

        List<String> result = new ArrayList<>(emptyAssignment(ingredients.size()));
        for (int ingredient = 0; ingredient < ingredientSlots.size(); ingredient++) {
            String assigned = assignments.get(ingredient).stream()
                .filter(itemEdge -> itemEdge.edge().capacity == 0)
                .map(ItemEdge::itemId)
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Completed flow omitted a recipe ingredient"));
            result.set(ingredientSlots.get(ingredient), assigned);
        }
        return Optional.of(List.copyOf(result));
    }

    private static List<String> emptyAssignment(int size) {
        return new ArrayList<>(java.util.Collections.nCopies(size, ""));
    }

    private record ItemEdge(String itemId, Edge edge) {
    }

    private static final class Edge {
        private final int destination;
        private final int reverseIndex;
        private int capacity;

        private Edge(int destination, int reverseIndex, int capacity) {
            this.destination = destination;
            this.reverseIndex = reverseIndex;
            this.capacity = capacity;
        }
    }

    private static final class FlowNetwork {
        private final List<List<Edge>> graph;
        private final int[] level;
        private final int[] nextEdge;

        private FlowNetwork(int nodes) {
            graph = new ArrayList<>(nodes);
            for (int node = 0; node < nodes; node++) {
                graph.add(new ArrayList<>());
            }
            level = new int[nodes];
            nextEdge = new int[nodes];
        }

        private Edge addEdge(int from, int to, int capacity) {
            Edge forward = new Edge(to, graph.get(to).size(), capacity);
            Edge reverse = new Edge(from, graph.get(from).size(), 0);
            graph.get(from).add(forward);
            graph.get(to).add(reverse);
            return forward;
        }

        private int maxFlow(int source, int sink) {
            int flow = 0;
            while (buildLevels(source, sink)) {
                java.util.Arrays.fill(nextEdge, 0);
                int pushed;
                while ((pushed = push(source, sink, Integer.MAX_VALUE)) > 0) {
                    flow += pushed;
                }
            }
            return flow;
        }

        private boolean buildLevels(int source, int sink) {
            java.util.Arrays.fill(level, -1);
            ArrayDeque<Integer> queue = new ArrayDeque<>();
            level[source] = 0;
            queue.add(source);
            while (!queue.isEmpty()) {
                int node = queue.removeFirst();
                for (Edge edge : graph.get(node)) {
                    if (edge.capacity > 0 && level[edge.destination] < 0) {
                        level[edge.destination] = level[node] + 1;
                        queue.addLast(edge.destination);
                    }
                }
            }
            return level[sink] >= 0;
        }

        private int push(int node, int sink, int flow) {
            if (node == sink) {
                return flow;
            }
            for (; nextEdge[node] < graph.get(node).size(); nextEdge[node]++) {
                Edge edge = graph.get(node).get(nextEdge[node]);
                if (edge.capacity <= 0 || level[edge.destination] != level[node] + 1) {
                    continue;
                }
                int pushed = push(edge.destination, sink, Math.min(flow, edge.capacity));
                if (pushed > 0) {
                    edge.capacity -= pushed;
                    graph.get(edge.destination).get(edge.reverseIndex).capacity += pushed;
                    return pushed;
                }
            }
            return 0;
        }
    }
}
