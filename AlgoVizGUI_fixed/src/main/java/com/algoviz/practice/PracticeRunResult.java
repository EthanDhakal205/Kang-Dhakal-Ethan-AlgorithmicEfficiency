package com.algoviz.practice;

import java.util.List;

public record PracticeRunResult(boolean compilerAvailable, boolean compiled, boolean allPassed,
                                String message, List<CompilerIssue> compilerIssues,
                                List<CaseResult> caseResults) {

    public record CompilerIssue(long line, long column, String kind, String message) {
    }

    public record CaseResult(String label, boolean passed, String details) {
    }

    public static PracticeRunResult compilerUnavailable(String message) {
        return new PracticeRunResult(false, false, false, message, List.of(), List.of());
    }

    public static PracticeRunResult compilationFailure(String message) {
        return compilationFailure(message, List.of());
    }

    public static PracticeRunResult compilationFailure(String message, List<CompilerIssue> compilerIssues) {
        return new PracticeRunResult(true, false, false, message, List.copyOf(compilerIssues), List.of());
    }

    public static PracticeRunResult completed(String message, List<CaseResult> caseResults) {
        boolean passed = caseResults.stream().allMatch(CaseResult::passed);
        return new PracticeRunResult(true, true, passed, message, List.of(), List.copyOf(caseResults));
    }
}