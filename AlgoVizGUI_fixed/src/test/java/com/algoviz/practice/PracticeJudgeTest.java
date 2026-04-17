package com.algoviz.practice;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;

import javax.tools.ToolProvider;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PracticeJudgeTest {

    private final PracticeJudge judge = new PracticeJudge();

    @Test
    void answerCodePassesBubbleAssessment() {
        Assumptions.assumeTrue(ToolProvider.getSystemJavaCompiler() != null);

        PracticeCatalog.Problem problem = PracticeCatalog.find("bubble").orElseThrow();
        PracticeRunResult result = judge.run(problem, problem.answerCode());

        assertTrue(result.compilerAvailable());
        assertTrue(result.compiled());
        assertTrue(result.allPassed());
    }

    @Test
    void answerCodePassesBinaryAssessment() {
        Assumptions.assumeTrue(ToolProvider.getSystemJavaCompiler() != null);

        PracticeCatalog.Problem problem = PracticeCatalog.find("binary").orElseThrow();
        PracticeRunResult result = judge.run(problem, problem.answerCode());

        assertTrue(result.compilerAvailable());
        assertTrue(result.compiled());
        assertTrue(result.allPassed());
    }

    @Test
    void compileErrorsExposeStructuredDiagnostics() {
        Assumptions.assumeTrue(ToolProvider.getSystemJavaCompiler() != null);

        PracticeCatalog.Problem problem = PracticeCatalog.find("linear").orElseThrow();
        PracticeRunResult result = judge.run(problem, "public class UserSolution {");

        assertTrue(result.compilerAvailable());
        assertFalse(result.compiled());
        assertFalse(result.compilerIssues().isEmpty());
        assertTrue(result.compilerIssues().get(0).line() > 0);
        assertTrue(result.message().contains("Compilation failed"));
    }
}