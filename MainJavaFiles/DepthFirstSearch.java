import java.util.*;

public class DepthFirstSearch extends SearchAlgorithm {

    private Map<Object, List<Object>> graph;
    private Object start;
    private List<Object> visitedOrder;

    public DepthFirstSearch(Map<Object, List<Object>> graph, Object start, Object target) {
        super(null, target, false);
        this.graph        = graph;
        this.start        = start;
        this.visitedOrder = new ArrayList<>();
    }

    @Override
    protected boolean validate() {
        if (graph == null || graph.isEmpty() || target == null || start == null) return false;
        if (!graph.containsKey(start)) return false;
        return true;
    }

    private int compare(Object a, Object b) {
        if (a instanceof Number && b instanceof Number) {
            return Double.compare(((Number) a).doubleValue(), ((Number) b).doubleValue());
        } else if (a instanceof String && b instanceof String) {
            return ignoreCase ? ((String) a).compareToIgnoreCase((String) b)
                              : ((String) a).compareTo((String) b);
        } else if (a instanceof Comparable) {
            return ((Comparable) a).compareTo(b);
        }
        return -1;
    }

    public List<Object> getVisitedOrder() {
        return visitedOrder;
    }

    @Override
    public int search() {
        if (!validate()) {
            System.out.println(getName() + ": validation failed — graph is null/empty, start node does not exist, or target is null.");
            return -1;
        }

        reset();
        visitedOrder.clear();
        startTimer();

        Deque<Object> stack        = new ArrayDeque<>();
        Map<Object, Object> parent = new HashMap<>();
        Set<Object> visited        = new HashSet<>();

        stack.push(start);
        parent.put(start, null);

        while (!stack.isEmpty()) {
            Object node = stack.pop();

            if (visited.contains(node)) continue;

            visited.add(node);
            visitedOrder.add(node);
            comparisons++;

            if (compare(node, target) == 0) {
                stopTimer();
                return buildPath(parent, node);
            }

            List<Object> neighbors = graph.getOrDefault(node, new ArrayList<>());
            for (int i = neighbors.size() - 1; i >= 0; i--) {
                Object neighbor = neighbors.get(i);
                if (!visited.contains(neighbor)) {
                    stack.push(neighbor);
                    if (!parent.containsKey(neighbor)) {
                        parent.put(neighbor, node);
                    }
                }
            }
        }

        stopTimer();
        return -1;
    }

    private int buildPath(Map<Object, Object> parent, Object node) {
        List<Object> path = new ArrayList<>();
        Object current    = node;

        while (current != null) {
            path.add(0, current);
            current = parent.get(current);
        }

        System.out.println("path: " + path);
        return visitedOrder.indexOf(node);
    }

    @Override
    public String getName() { return "Depth-First Search"; }

    @Override
    public String getTimeComplexity() { return "O(V + E)"; }

    @Override
    public String getSpaceComplexity() { return "O(V)"; }
}
