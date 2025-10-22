package io.ballerina.teavm;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import io.ballerina.projects.CodeGeneratorResult;
import io.ballerina.projects.CodeModifierResult;
import io.ballerina.projects.JBallerinaBackend;
import io.ballerina.projects.JvmTarget;
import io.ballerina.projects.PackageCompilation;
import io.ballerina.projects.PackageResolution;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.environment.ResolutionOptions;
import io.ballerina.projects.internal.ProjectDiagnosticErrorCode;
import io.ballerina.projects.util.ProjectUtils;
import io.ballerina.tools.diagnostics.Diagnostic;

import io.ballerina.fs.Path;

public class Main {
    private static final PrintStream out = System.out;

    public static void main(String[] args) {
        System.setProperty("ballerina.home", "/Users/sithi/.ballerina");
        SingleFileProject project = SingleFileProject.load(Path.of("/Users/sithi/sandbox/somewhere/empty.bal"));

        codegen(project);
    }

    private static void codegen(SingleFileProject project) {
        String source = project.currentPackage().getDefaultModule().document(project.currentPackage().getDefaultModule().documentIds().iterator().next()).name();
        out.println("Compiling " + source);

        try {

            long start = 0;

            Set<String> packageImports = Set.of();
            PackageResolution packageResolution  = project.currentPackage().getResolution();

            CodeGeneratorResult codeGeneratorResult = project.currentPackage().runCodeGeneratorPlugins();

            List<Diagnostic> diagnostics = new ArrayList<>(codeGeneratorResult.reportedDiagnostics().diagnostics());

            CodeModifierResult codeModifierResult = project.currentPackage().runCodeModifierPlugins();
            diagnostics.addAll(codeModifierResult.reportedDiagnostics().diagnostics());

            Set<String> newPackageImports = Set.of();
            ResolutionOptions resolutionOptions = ResolutionOptions.builder().setOffline(true).build();
            if (!packageImports.equals(newPackageImports)) {
                resolutionOptions = ResolutionOptions.builder().setOffline(false).build();
            }

            if (packageResolution != project.currentPackage().getResolution(resolutionOptions)) {
                project.currentPackage().getResolution();
            }

            Optional<Diagnostic> projectLoadingDiagnostic = ProjectUtils.getProjectLoadingDiagnostic().stream().filter(diagnostic -> diagnostic.diagnosticInfo().code().equals(ProjectDiagnosticErrorCode.DEPRECATED_RESOURCES_STRUCTURE.diagnosticId())).findFirst();
            projectLoadingDiagnostic.ifPresent(out::println);

            PackageCompilation packageCompilation = project.currentPackage().getCompilation();
            JBallerinaBackend jBallerinaBackend = JBallerinaBackend.from(packageCompilation, JvmTarget.JAVA_21);
        } catch (ProjectException e) {
            throw new RuntimeException(e);
        }
    }
}
