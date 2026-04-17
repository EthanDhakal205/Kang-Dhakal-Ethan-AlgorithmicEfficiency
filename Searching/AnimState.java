import java.util.*;

class AnimState {

    static final String[] ARRAY_ALGOS  = {
        "Linear Search", "Binary Search", "Ternary Search",
        "Jump Search", "Interpolation Search", "Exponential Search", "Fibonacci Search"
    };
    static final String[] GRAPH_ALGOS  = {"Breadth-First Search", "Depth-First Search"};
    static final String[] STRING_ALGOS = {"KMP Search", "Rabin-Karp Search"};

    String   selectedAlgo  = "Linear Search";
    volatile boolean running = false;
    int      stepDelay     = 350;
    boolean  showCaptions  = true;

    Object[] currentArray  = {2, 5, 8, 11, 14, 19, 27, 33, 45};
    Object   currentTarget = 19;
    boolean  ignoreCase    = false;
    String   currentText    = "the cat sat on the caterpillar";
    String   currentPattern = "cat";
    Map<String, List<String>> currentGraph;
    String   graphStart  = "A";
    String   graphTarget = "C";
    int      graphDepth  = 3;
    int      graphBranch = 2;

    int      comparisons  = 0;
    long     elapsedNs    = 0;
    int      resultIndex  = -2;
    List<Integer> matchPositions = new ArrayList<>();
    List<String>  visitOrder     = new ArrayList<>();

    float[]  cellAnim;
    float[]  cellAnimPrev;
    int[]    cellAnimState;
    int[]    cellStates;

    Map<String, Float> nodeAnim = new HashMap<>();

    Set<String>  graphVisited   = new HashSet<>();
    String       graphCurrent   = null;
    Set<String>  graphPath      = new HashSet<>();
    List<String> graphPathList  = null;

    String   strText         = "";
    String   strPattern      = "";
    int      strWindowStart  = -1;
    int      strMatchedChars = 0;
    boolean  strMismatch     = false;
    long     strHashVal      = -1;
    long     strPatHash      = -1;
    boolean  strIsKMP        = true;
    int[]    strLPS          = null;
    int      strKmpJ         = 0;

    int      captionPhase    = 0;
    float    captionProgress = 0f;
    String   captionText     = "";
    String   captionQueued   = null;
    int      captionHoldTicks = 0;
    int      captionHoldMax   = 0;

    boolean isArrayAlgo() {
        for (String s : ARRAY_ALGOS)  if (s.equals(selectedAlgo)) return true;
        return false;
    }
    boolean isGraphAlgo() {
        for (String s : GRAPH_ALGOS)  if (s.equals(selectedAlgo)) return true;
        return false;
    }
    boolean isStringAlgo() {
        for (String s : STRING_ALGOS) if (s.equals(selectedAlgo)) return true;
        return false;
    }

    void resetArrayState() {
        int n = currentArray != null ? currentArray.length : 0;
        cellStates    = new int[n];
        cellAnim      = new float[n];
        cellAnimPrev  = new float[n];
        cellAnimState = new int[n];
    }

    void resetStringState() {
        strWindowStart  = -1;
        strMatchedChars = 0;
        strMismatch     = false;
        strHashVal      = -1;
        strPatHash      = -1;
        strLPS          = null;
        strKmpJ         = 0;
        strText         = currentText;
        strPattern      = currentPattern;
        matchPositions.clear();
    }

    void resetGraphState() {
        graphVisited.clear();
        graphCurrent  = null;
        graphPath.clear();
        graphPathList = null;
        nodeAnim.clear();
        visitOrder.clear();
    }

    void resetCaption() {
        captionPhase     = 0;
        captionProgress  = 0f;
        captionText      = "";
        captionQueued    = null;
        captionHoldTicks = 0;
    }

    void resetStats() {
        comparisons  = 0;
        elapsedNs    = 0;
        resultIndex  = -2;
        matchPositions.clear();
        visitOrder.clear();
    }
}