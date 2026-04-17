import java.util.List;

record PracticeRunResult(
    boolean compilerAvailable,
    boolean compiled,
    boolean allPassed,
    String message,
    List<CompilerIssue> compilerIssues,
    List<CaseResult> caseResults
) {

    record CompilerIssue(long line, long column, String kind, String message) {
    }

    record CaseResult(String label, boolean passed, String details) {
    }

    static PracticeRunResult compilerUnavailable(String message) {
        return new PracticeRunResult(false, false, false, message, List.of(), List.of());
    }

    static PracticeRunResult compilationFailure(String message) {
        return compilationFailure(message, List.of());
    }

    static PracticeRunResult compilationFailure(String message, List<CompilerIssue> compilerIssues) {
        return new PracticeRunResult(true, false, false, message, List.copyOf(compilerIssues), List.of());
    }

    static PracticeRunResult completed(String message, List<CaseResult> caseResults) {
        boolean passed = caseResults.stream().allMatch(PracticeRunResult.CaseResult::passed);
        return new PracticeRunResult(true, true, passed, message, List.of(), List.copyOf(caseResults));
    }
}
