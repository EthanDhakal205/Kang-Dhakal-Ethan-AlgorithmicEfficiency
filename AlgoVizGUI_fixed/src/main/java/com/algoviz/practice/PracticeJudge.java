package com.algoviz.practice;

import com.algoviz.practice.PracticeCatalog.Kind;
import com.algoviz.practice.PracticeCatalog.Problem;
import com.algoviz.practice.PracticeCatalog.TestCase;
import com.algoviz.practice.PracticeRunResult.CaseResult;
import com.algoviz.practice.PracticeRunResult.CompilerIssue;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PracticeJudge {

    private static final long CASE_TIMEOUT_MS = 2000;

    public PracticeRunResult run(Problem problem, String sourceCode) {
        if (problem == null) {
            return PracticeRunResult.compilationFailure("No practice problem is loaded.");
        }
        if (sourceCode == null || sourceCode.isBlank()) {
            return PracticeRunResult.compilationFailure("Enter code before running the tests.");
        }

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            return PracticeRunResult.compilerUnavailable(
                    "A JDK compiler is not available in this runtime. Launch the app with a JDK to enable the practice compiler.");
        }

        Path tempDir = null;
        try {
            tempDir = Files.createTempDirectory("algoviz-practice-");
            Path sourceFile = tempDir.resolve("UserSolution.java");
            Files.writeString(sourceFile, sourceCode, StandardCharsets.UTF_8);

            DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
            boolean compiled = compile(compiler, diagnostics, sourceFile, tempDir);
            if (!compiled) {
                List<CompilerIssue> issues = collectDiagnostics(diagnostics);
                return PracticeRunResult.compilationFailure(formatDiagnostics(issues), issues);
            }

            return executeTests(problem, tempDir);
        } catch (IOException e) {
            return PracticeRunResult.compilationFailure("Failed to prepare the temporary compiler workspace: " + e.getMessage());
        } finally {
            if (tempDir != null) {
                deleteQuietly(tempDir);
            }
        }
    }

    private boolean compile(JavaCompiler compiler, DiagnosticCollector<JavaFileObject> diagnostics,
                            Path sourceFile, Path outputDir) throws IOException {
        try (StandardJavaFileManager fileManager =
                     compiler.getStandardFileManager(diagnostics, null, StandardCharsets.UTF_8)) {
            Iterable<? extends JavaFileObject> units = fileManager.getJavaFileObjects(sourceFile.toFile());
            List<String> options = List.of("-d", outputDir.toString(), "--release", "17");
            JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, diagnostics, options, null, units);
            return Boolean.TRUE.equals(task.call());
        }
    }

    private List<CompilerIssue> collectDiagnostics(DiagnosticCollector<JavaFileObject> diagnostics) {
        List<CompilerIssue> issues = new ArrayList<>();
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            String message = diagnostic.getMessage(null).replace(System.lineSeparator(), " ").trim();
            issues.add(new CompilerIssue(
                    Math.max(1, diagnostic.getLineNumber()),
                    Math.max(1, diagnostic.getColumnNumber()),
                    diagnostic.getKind().name(),
                    message
            ));
        }
        return issues;
    }

    private PracticeRunResult executeTests(Problem problem, Path outputDir) {
        try (URLClassLoader classLoader = new URLClassLoader(
                new URL[]{outputDir.toUri().toURL()}, PracticeJudge.class.getClassLoader())) {
            Class<?> solutionClass = Class.forName("UserSolution", true, classLoader);
            Method solveMethod = resolveMethod(problem, solutionClass);

            List<CaseResult> caseResults = new ArrayList<>();
            int passed = 0;
            for (TestCase testCase : problem.testCases()) {
                CaseResult result = executeCase(problem, solveMethod, testCase);
                caseResults.add(result);
                if (result.passed()) {
                    passed++;
                }
            }

            String summary = passed == problem.testCases().size()
                    ? "All " + passed + " test cases passed."
                    : passed + "/" + problem.testCases().size() + " test cases passed.";
            return PracticeRunResult.completed(summary, caseResults);
        } catch (ClassNotFoundException e) {
            return PracticeRunResult.compilationFailure("Compilation succeeded, but class UserSolution was not found.");
        } catch (NoSuchMethodException e) {
            return PracticeRunResult.compilationFailure(
                    "Expected method signature not found. Required: " + problem.signatureHint());
        } catch (IOException e) {
            return PracticeRunResult.compilationFailure("Failed to load the compiled solution: " + e.getMessage());
        }
    }

    private Method resolveMethod(Problem problem, Class<?> solutionClass) throws NoSuchMethodException {
        Method method = problem.kind() == Kind.SORT
                ? solutionClass.getMethod("solve", int[].class)
                : solutionClass.getMethod("solve", int[].class, int.class);

        if (!Modifier.isStatic(method.getModifiers())) {
            throw new NoSuchMethodException("Method solve must be static.");
        }
        return method;
    }

    private CaseResult executeCase(Problem problem, Method solveMethod, TestCase testCase) {
        try {
            Object output = invokeWithTimeout(problem, solveMethod, testCase);
            if (problem.kind() == Kind.SORT) {
                if (!(output instanceof int[] actualArray)) {
                    return new CaseResult(testCase.label(), false,
                            "Expected an int[] result but received " + typeName(output) + ".");
                }
                boolean passed = Arrays.equals(actualArray, testCase.expectedArray());
                String details = passed
                        ? "Passed. " + Arrays.toString(actualArray)
                        : "Expected " + Arrays.toString(testCase.expectedArray())
                        + " but received " + Arrays.toString(actualArray) + ".";
                return new CaseResult(testCase.label(), passed, details);
            }

            if (!(output instanceof Integer actualIndex)) {
                return new CaseResult(testCase.label(), false,
                        "Expected an int result but received " + typeName(output) + ".");
            }
            boolean passed = actualIndex.equals(testCase.expectedIndex());
            String details = passed
                    ? "Passed. Returned index " + actualIndex + "."
                    : "Expected index " + testCase.expectedIndex() + " but received " + actualIndex + ".";
            return new CaseResult(testCase.label(), passed, details);
        } catch (TimeoutException e) {
            return new CaseResult(testCase.label(), false,
                    "Execution timed out after " + CASE_TIMEOUT_MS + " ms.");
        } catch (Throwable t) {
            Throwable root = rootCause(t);
            String message = root.getMessage() == null ? "" : ": " + root.getMessage();
            return new CaseResult(testCase.label(), false,
                    "Runtime error: " + root.getClass().getSimpleName() + message);
        }
    }

    private Object invokeWithTimeout(Problem problem, Method solveMethod, TestCase testCase) throws Throwable {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Future<Object> future = executor.submit(() -> {
                int[] inputCopy = Arrays.copyOf(testCase.input(), testCase.input().length);
                if (problem.kind() == Kind.SORT) {
                    return solveMethod.invoke(null, (Object) inputCopy);
                }
                return solveMethod.invoke(null, inputCopy, testCase.target());
            });

            try {
                return future.get(CASE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (cause instanceof InvocationTargetException invocation && invocation.getCause() != null) {
                    throw invocation.getCause();
                }
                throw cause != null ? cause : e;
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private String formatDiagnostics(List<CompilerIssue> issues) {
        if (issues.isEmpty()) {
            return "Compilation failed, but the compiler did not return any structured diagnostics.";
        }

        StringBuilder builder = new StringBuilder("Compilation failed with ")
                .append(issues.size())
                .append(issues.size() == 1 ? " issue." : " issues.");

        for (CompilerIssue issue : issues) {
            builder.append(System.lineSeparator())
                    .append(System.lineSeparator())
                    .append(issue.kind())
                    .append(" at line ")
                    .append(issue.line())
                    .append(", column ")
                    .append(issue.column())
                    .append(System.lineSeparator())
                    .append(issue.message());
        }
        return builder.toString();
    }

    private void deleteQuietly(Path dir) {
        try {
            Files.walk(dir)
                    .sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                        }
                    });
        } catch (IOException ignored) {
        }
    }

    private String typeName(Object output) {
        return output == null ? "null" : output.getClass().getSimpleName();
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }
}