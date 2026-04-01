import java.util.*;

public class AlgorithmRunner {

    private SearchAlgorithm activeAlgorithm;

    public AlgorithmRunner() {
        this.activeAlgorithm = null;
    }

    public void setAlgorithm(SearchAlgorithm algorithm) {
        this.activeAlgorithm = algorithm;
    }

    public SearchAlgorithm getAlgorithm() {
        return activeAlgorithm;
    }

    public String getActiveName() {
        if (activeAlgorithm == null) return "none";
        return activeAlgorithm.getName();
    }

    public void run() {
        if (activeAlgorithm == null) {
            System.out.println("no algorithm set.");
            return;
        }

        printHeader();
        runSingle(activeAlgorithm);
    }

    private void runSingle(SearchAlgorithm algo) {
        System.out.println("\nalgorithm  : " + algo.getName());
        System.out.println("complexity : time " + algo.getTimeComplexity() + " | space " + algo.getSpaceComplexity());

        int result = algo.search();

        if (result != -1) {
            System.out.println("result     : found at index/position " + result);
        } else {
            System.out.println("result     : not found or validation failed");
        }

        System.out.println("comparisons: " + algo.getComparisons());
        System.out.println("time       : " + algo.getElapsedTime() + " ns");

        if (algo instanceof KMPSearch) {
            List<Integer> positions = ((KMPSearch) algo).getMatchPositions();
            if (!positions.isEmpty()) System.out.println("all matches: " + positions);
        }

        if (algo instanceof RabinKarpSearch) {
            List<Integer> positions = ((RabinKarpSearch) algo).getMatchPositions();
            if (!positions.isEmpty()) System.out.println("all matches: " + positions);
        }

        if (algo instanceof BreadthFirstSearch) {
            List<Object> order = ((BreadthFirstSearch) algo).getVisitedOrder();
            if (!order.isEmpty()) System.out.println("visit order: " + order);
        }

        if (algo instanceof DepthFirstSearch) {
            List<Object> order = ((DepthFirstSearch) algo).getVisitedOrder();
            if (!order.isEmpty()) System.out.println("visit order: " + order);
        }

        System.out.println("-".repeat(60));
    }

    private void printHeader() {
        System.out.println("=".repeat(60));
        System.out.println(" ALGORITHM RUNNER");
        System.out.println("=".repeat(60));
    }

    public void reset() {
        if (activeAlgorithm != null) activeAlgorithm.reset();
    }

    public void clear() {
        activeAlgorithm = null;
    }

    public static void main(String[] args) {

        AlgorithmRunner runner = new AlgorithmRunner();

        Object[] sortedNumbers = {2, 5, 8, 11, 14, 19, 27, 33, 45};

        Map<Object, List<Object>> graph = new HashMap<>();
        graph.put("A", Arrays.asList("B", "C"));
        graph.put("B", Arrays.asList("A", "D", "E"));
        graph.put("C", Arrays.asList("A", "F"));
        graph.put("D", Arrays.asList("B"));
        graph.put("E", Arrays.asList("B", "F"));
        graph.put("F", Arrays.asList("C", "E"));

        runner.setAlgorithm(new LinearSearch(sortedNumbers, 19, false));
        runner.run();

        runner.setAlgorithm(new BinarySearch(sortedNumbers, 19, false));
        runner.run();

        runner.setAlgorithm(new BreadthFirstSearch(graph, "A", "F"));
        runner.run();

        runner.setAlgorithm(new KMPSearch("the cat sat on the caterpillar", "cat", false));
        runner.run();
    }
}