/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerina.cli.cmd;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.wso2.ballerinalang.util.RepoUtils;
import picocli.CommandLine;

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.cli.TaskExecutor;
import io.ballerina.cli.task.CleanTargetDirTask;
import io.ballerina.cli.task.CompileTask;
import io.ballerina.cli.task.CreateExecutableTask;
import io.ballerina.cli.task.DumpBuildTimeTask;
import io.ballerina.cli.task.ResolveMavenDependenciesTask;
import io.ballerina.cli.task.RunBuildToolsTask;
import io.ballerina.cli.utils.BuildTime;
import io.ballerina.cli.utils.FileUtils;
import io.ballerina.projects.BuildOptions;
import io.ballerina.projects.Document;
import io.ballerina.projects.DocumentId;
import io.ballerina.projects.Module;
import io.ballerina.projects.Project;
import io.ballerina.projects.ProjectException;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.util.ProjectConstants;
import io.ballerina.compiler.internal.parser.BallerinaParser;
import io.ballerina.compiler.internal.parser.ParserFactory;
import io.ballerina.compiler.internal.parser.ParserRuleContext;
import io.ballerina.compiler.internal.parser.tree.STIdentifierToken;
import io.ballerina.compiler.internal.parser.tree.STInvalidNodeMinutiae;
import io.ballerina.compiler.internal.parser.tree.STMinutiae;
import io.ballerina.compiler.internal.parser.tree.STNode;
import io.ballerina.compiler.internal.parser.tree.STNodeDiagnostic;
import io.ballerina.compiler.internal.parser.tree.STNodeList;
import io.ballerina.compiler.internal.parser.tree.STToken;
import io.ballerina.compiler.internal.syntax.SyntaxUtils;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.SyntaxTree;

import static io.ballerina.cli.cmd.Constants.BUILD_COMMAND;
import static io.ballerina.projects.util.ProjectUtils.isProjectUpdated;

/**
 * This class represents the "bal build" command.
 *
 * @since 2.0.0
 */
@CommandLine.Command(name = BUILD_COMMAND, description = "Compile the current package")
public class BuildCommand implements BLauncherCmd {

    private final PrintStream outStream;
    private final PrintStream errStream;
    private final boolean exitWhenFinish;

    public BuildCommand() {
        this.projectPath = Path.of(System.getProperty(ProjectConstants.USER_DIR));
        this.outStream = System.out;
        this.errStream = System.err;
        this.exitWhenFinish = true;
    }

    BuildCommand(Path projectPath, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish) {
        this.projectPath = projectPath;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.offline = true;
    }

    BuildCommand(Path projectPath, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish,
            Boolean optimizeDependencyCompilation) {
        this.projectPath = projectPath;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.optimizeDependencyCompilation = optimizeDependencyCompilation;
        this.offline = true;
    }

    BuildCommand(Path projectPath, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish,
            boolean dumpBuildTime) {
        this.projectPath = projectPath;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.dumpBuildTime = dumpBuildTime;
        this.offline = true;
    }

    BuildCommand(Path projectPath, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish,
            String output) {
        this.projectPath = projectPath;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.output = output;
        this.offline = true;
    }

    BuildCommand(Path projectPath, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish,
            Path targetDir) {
        this.projectPath = projectPath;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.targetDir = targetDir;
        this.offline = true;
    }

    BuildCommand(Path projectPath, PrintStream outStream, PrintStream errStream, boolean exitWhenFinish,
            boolean dumpBuildTime, boolean nativeImage, String graalVMBuildOptions) {
        this.projectPath = projectPath;
        this.outStream = outStream;
        this.errStream = errStream;
        this.exitWhenFinish = exitWhenFinish;
        this.dumpBuildTime = dumpBuildTime;
        this.offline = true;
        this.nativeImage = nativeImage;
        this.graalVMBuildOptions = graalVMBuildOptions;
    }

    @CommandLine.Option(names = { "--output", "-o" }, description = "Write the output to the given file. The provided "
            +
            "output file name may or may not contain the " +
            "'.jar' extension.")
    private String output;

    @CommandLine.Option(names = { "--offline" }, description = "Build/Compile offline without downloading " +
            "dependencies.")
    private Boolean offline;

    @CommandLine.Parameters(arity = "0..1")
    private final Path projectPath;

    @CommandLine.Option(names = "--dump-bir", hidden = true)
    private boolean dumpBIR;

    @CommandLine.Option(names = "--dump-bir-file", hidden = true)
    private Boolean dumpBIRFile;

    @CommandLine.Option(names = "--dump-ast-file", description = "Dump the AST to a file.", hidden = true)
    private Boolean dumpASTFile;

    @CommandLine.Option(names = "--dump-graph", description = "Print the dependency graph.", hidden = true)
    private boolean dumpGraph;

    @CommandLine.Option(names = "--dump-raw-graphs", description = "Print all intermediate graphs created in the " +
            "dependency resolution process.", hidden = true)
    private boolean dumpRawGraphs;

    @CommandLine.Option(names = { "--help", "-h" }, hidden = true)
    private boolean helpFlag;

    @CommandLine.Option(names = "--experimental", description = "Enable experimental language features.")
    private Boolean experimentalFlag;

    @CommandLine.Option(names = "--generate-config-schema", hidden = true)
    private Boolean configSchemaGen;

    private static final String buildCmd = "bal build [-o <output>] [--offline] [--taint-check]\n" +
            "                    [<ballerina-file | package-path>]";

    @CommandLine.Option(names = "--observability-included", description = "package observability in the executable " +
            "JAR file(s).")
    private Boolean observabilityIncluded;

    @CommandLine.Option(names = "--cloud", description = "Enable cloud artifact generation")
    private String cloud;

    @CommandLine.Option(names = "--show-dependency-diagnostics", description = "Show the diagnostics " +
            "generated by the dependencies")
    private Boolean showDependencyDiagnostics;

    @CommandLine.Option(names = "--remote-management", description = "enable service management tools in " +
            "the executable JAR file(s).")
    private Boolean remoteManagement;

    @CommandLine.Option(names = "--list-conflicted-classes", description = "list conflicted classes when generating executable")
    private Boolean listConflictedClasses;

    @CommandLine.Option(names = "--dump-build-time", description = "calculate and dump build time", hidden = true)
    private Boolean dumpBuildTime;

    @CommandLine.Option(names = "--sticky", description = "stick to exact versions locked (if exists)")
    private Boolean sticky;

    @CommandLine.Option(names = "--target-dir", description = "target directory path")
    private Path targetDir;

    @CommandLine.Option(names = "--export-openapi", description = "generate openAPI contract files for all" +
            " the services in the current package")
    private Boolean exportOpenAPI;

    @CommandLine.Option(names = "--export-component-model", description = "generate a model to represent " +
            "interactions between the package components (i.e. service/type definitions) and, export it in JSON format", hidden = true)
    private Boolean exportComponentModel;

    @CommandLine.Option(names = "--enable-cache", description = "enable caches for the compilation", hidden = true)
    private Boolean enableCache;

    @CommandLine.Option(names = "--graalvm", description = "enable native image generation")
    private Boolean nativeImage;

    @CommandLine.Option(names = "--disable-syntax-tree-caching", hidden = true, description = "disable syntax tree " +
            "caching for source files", defaultValue = "false")
    private Boolean disableSyntaxTreeCaching;

    @CommandLine.Option(names = "--graalvm-build-options", description = "additional build options for native image " +
            "generation")
    private String graalVMBuildOptions;

    @CommandLine.Option(names = "--optimize-dependency-compilation", hidden = true, description = "experimental memory optimization for large projects")
    private Boolean optimizeDependencyCompilation;

    @CommandLine.Option(names = "--locking-mode", hidden = true, description = "allow passing the package locking mode.")
    private String lockingMode;

    @Override
    public void execute() {
        long start = 0;
        if (this.helpFlag) {
            String commandUsageInfo = BLauncherCmd.getCommandUsageInfo(BUILD_COMMAND);
            this.errStream.println(commandUsageInfo);
            return;
        }

        // load project
        Project project;
        BuildOptions buildOptions = constructBuildOptions();

        boolean isSingleFileBuild = false;
        if (FileUtils.hasExtension(this.projectPath)) {
            try {
                if (buildOptions.dumpBuildTime()) {
                    start = System.currentTimeMillis();
                    BuildTime.getInstance().timestamp = start;
                }
                project = SingleFileProject.load(this.projectPath, buildOptions);
                if (buildOptions.dumpBuildTime()) {
                    BuildTime.getInstance().projectLoadDuration = System.currentTimeMillis() - start;
                }
            } catch (ProjectException e) {
                CommandUtil.printError(this.errStream, e.getMessage(), null, false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
            isSingleFileBuild = true;
        } else {
            // Check if the output flag is set when building all the modules.
            if (null != this.output) {
                CommandUtil.printError(this.errStream,
                        "'-o' and '--output' are only supported when building a single Ballerina " +
                                "file.",
                        "bal build -o <output-file> <ballerina-file> ",
                        true);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }

            try {
                if (buildOptions.dumpBuildTime()) {
                    start = System.currentTimeMillis();
                    BuildTime.getInstance().timestamp = start;
                }
                project = BuildProject.load(this.projectPath, buildOptions);
                if (buildOptions.dumpBuildTime()) {
                    BuildTime.getInstance().projectLoadDuration = System.currentTimeMillis() - start;
                }
            } catch (ProjectException e) {
                CommandUtil.printError(this.errStream, e.getMessage(), null, false);
                CommandUtil.exitError(this.exitWhenFinish);
                return;
            }
        }

        if (this.dumpASTFile != null && this.dumpASTFile) {
            String astJSON = generateJSON(this.projectPath);
            if (astJSON != null) {
                try {
                   Path outputDir;
                    if (FileUtils.hasExtension(this.projectPath)) {
                        outputDir = this.projectPath.toAbsolutePath().getParent();
                    } else {
                        outputDir = this.projectPath;
                    }
                    
                    String fileName = getASTFileName();
                    Path astFile = outputDir.resolve(fileName);
                    Files.writeString(astFile, astJSON, StandardCharsets.UTF_8);
                    this.outStream.println("AST JSON written to: " + astFile.toAbsolutePath());
                } catch (IOException e) {
                    this.errStream.println("Failed to write AST JSON to file: " + e.getMessage());
                    return;
                }
            }
        }
        
        RepoUtils.readSettings();

        if (!project.buildOptions().nativeImage() && !project.buildOptions().graalVMBuildOptions().isEmpty()) {
            this.outStream.println("WARNING: Additional GraalVM build options are ignored since graalvm " +
                    "flag is not set");
        }

        // Check package files are modified after last build
        boolean isPackageModified = isProjectUpdated(project);

        TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
                // clean the target directory(projects only)
                .addTask(new CleanTargetDirTask(isPackageModified, buildOptions.enableCache()), isSingleFileBuild)
                // Run build tools
                .addTask(new RunBuildToolsTask(outStream), isSingleFileBuild)
                // resolve maven dependencies in Ballerina.toml
                .addTask(new ResolveMavenDependenciesTask(outStream))
                // compile the modules
                .addTask(new CompileTask(outStream, errStream, false, true,
                        isPackageModified, buildOptions.enableCache()))
                .addTask(new CreateExecutableTask(outStream, this.output, null, false))
                .addTask(new DumpBuildTimeTask(outStream), !project.buildOptions().dumpBuildTime())
                .build();

        taskExecutor.executeTasks(project);
        if (this.exitWhenFinish) {
            Runtime.getRuntime().exit(0);
        }
    }

    private BuildOptions constructBuildOptions() {
        BuildOptions.BuildOptionsBuilder buildOptionsBuilder = BuildOptions.builder();

        buildOptionsBuilder
                .setExperimental(experimentalFlag)
                .setOffline(offline)
                .setObservabilityIncluded(observabilityIncluded)
                .setCloud(cloud)
                .setRemoteManagement(remoteManagement)
                .setDumpBir(dumpBIR)
                .setDumpBirFile(dumpBIRFile)
                .setDumpASTFile(dumpASTFile)
                .setDumpGraph(dumpGraph)
                .setDumpRawGraphs(dumpRawGraphs)
                .setListConflictedClasses(listConflictedClasses)
                .setDumpBuildTime(dumpBuildTime)
                .setSticky(sticky)
                .setConfigSchemaGen(configSchemaGen)
                .setExportOpenAPI(exportOpenAPI)
                .setExportComponentModel(exportComponentModel)
                .setEnableCache(enableCache)
                .setNativeImage(nativeImage)
                .disableSyntaxTreeCaching(disableSyntaxTreeCaching)
                .setGraalVMBuildOptions(graalVMBuildOptions)
                .setShowDependencyDiagnostics(showDependencyDiagnostics)
                .setOptimizeDependencyCompilation(optimizeDependencyCompilation)
                .setLockingMode(lockingMode);

        if (targetDir != null) {
            buildOptionsBuilder.targetDir(targetDir.toString());
        }

        return buildOptionsBuilder.setConfigSchemaGen(configSchemaGen)
                .build();
    }

    @Override
    public String getName() {
        return BUILD_COMMAND;
    }

    @Override
    public void printLongDesc(StringBuilder out) {
        out.append(BLauncherCmd.getCommandUsageInfo(BUILD_COMMAND));
    }

    @Override
    public void printUsage(StringBuilder out) {
        out.append("  bal build [-o <output>] [--offline] \\n\" +\n" +
                "            \"                    [<ballerina-file | package-path>]");
    }

    @Override
    public void setParentCmdParser(CommandLine parentCmdParser) {
    }

    private String generateJSON(Path sourceFilePath) {
        try {
            if (Files.isDirectory(sourceFilePath)) {
                return generateJSONForProject(sourceFilePath);
            } else {
                return generateJSONForFile(sourceFilePath);
            }
        } catch (IOException e) {
            this.errStream.println("Failed to generate AST JSON: " + e.getMessage());
            return null;
        }
    }

    private String generateJSONForProject(Path projectPath) throws IOException {
        Path[] balFiles = Files.walk(projectPath)
                .filter(path -> path.toString().endsWith(".bal"))
                .filter(path -> !path.toString().contains("target"))
                .limit(10)
                .toArray(Path[]::new);

        if (balFiles.length == 0) {
            this.errStream.println("No .bal files found in the project.");
            return null;
        }

        JsonObject projectJson = new JsonObject();
        JsonArray filesArray = new JsonArray();
        boolean hasParseErrors = false;

        for (Path balFile : balFiles) {
            JsonObject fileJson = new JsonObject();
            fileJson.addProperty("file", projectPath.relativize(balFile).toString());

            try {
                String content = Files.readString(balFile, StandardCharsets.UTF_8);
                STNode tree = parseSource(content);
                fileJson.add("ast", getJSONFromSTNode(tree));
            } catch (Exception e) {
                this.errStream.println("Failed to parse file " + projectPath.relativize(balFile).toString() + ": " + e.getMessage());
                hasParseErrors = true;
                break;
            }

            filesArray.add(fileJson);
        }

        if (hasParseErrors) {
            return null;
        }

        projectJson.add("files", filesArray);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(projectJson);
    }

    private String generateJSONForFile(Path sourceFilePath) throws IOException {
        try {
            byte[] bytes = Files.readAllBytes(sourceFilePath);
            String content = new String(bytes, StandardCharsets.UTF_8);
            STNode tree = parseSource(content);
            return generateJSONFromSTNode(tree);
        } catch (Exception e) {
            this.errStream.println("Failed to parse file " + sourceFilePath.getFileName() + ": " + e.getMessage());
            throw new IOException("Parse error occurred", e);
        }
    }

    private STNode parseSource(String source) {
        BallerinaParser parser = ParserFactory.getParser(source);
        return parser.parse(ParserRuleContext.COMP_UNIT);
    }

    private String generateJSONFromSTNode(STNode treeNode) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(getJSONFromSTNode(treeNode));
    }

    private JsonElement getJSONFromSTNode(STNode treeNode) {
        JsonObject jsonNode = new JsonObject();
        SyntaxKind nodeKind = treeNode.kind;
        jsonNode.addProperty("kind", nodeKind.name());

        if (treeNode.isMissing()) {
            jsonNode.addProperty("isMissing", treeNode.isMissing());
            addDiagnosticsToJSON(treeNode, jsonNode);
            if (isToken(treeNode)) {
                addTriviaToJSON((STToken) treeNode, jsonNode);
            }
            return jsonNode;
        }

        addDiagnosticsToJSON(treeNode, jsonNode);
        if (isToken(treeNode)) {
            if (!isKeyword(nodeKind)) {
                jsonNode.addProperty("value", getTokenText((STToken) treeNode));
            }
            addTriviaToJSON((STToken) treeNode, jsonNode);
        } else {
            addChildrenToJSON(treeNode, jsonNode);
        }

        return jsonNode;
    }

    private void addChildrenToJSON(STNode tree, JsonObject node) {
        JsonArray children = new JsonArray();
        int size = tree.bucketCount();
        for (int i = 0; i < size; i++) {
            STNode childNode = tree.childInBucket(i);
            if (childNode == null || childNode.kind == SyntaxKind.NONE) {
                continue;
            }
            children.add(getJSONFromSTNode(childNode));
        }
        node.add("children", children);
    }

    private void addTriviaToJSON(STToken token, JsonObject jsonNode) {
        if (token.leadingMinutiae().bucketCount() != 0) {
            addMinutiaeListToJSON((STNodeList) token.leadingMinutiae(), jsonNode, "leadingMinutiae");
        }

        if (token.trailingMinutiae().bucketCount() != 0) {
            addMinutiaeListToJSON((STNodeList) token.trailingMinutiae(), jsonNode, "trailingMinutiae");
        }
    }

    private void addMinutiaeListToJSON(STNodeList minutiaeList, JsonObject node, String key) {
        JsonArray minutiaeJsonArray = new JsonArray();
        int size = minutiaeList.size();
        for (int i = 0; i < size; i++) {
            STMinutiae minutiae = (STMinutiae) minutiaeList.get(i);
            JsonObject minutiaeJson = new JsonObject();
            minutiaeJson.addProperty("kind", minutiae.kind.name());
            switch (minutiae.kind) {
                case WHITESPACE_MINUTIAE:
                case END_OF_LINE_MINUTIAE:
                case COMMENT_MINUTIAE:
                    minutiaeJson.addProperty("value", minutiae.text());
                    break;
                case INVALID_NODE_MINUTIAE:
                    STInvalidNodeMinutiae invalidNodeMinutiae = (STInvalidNodeMinutiae) minutiae;
                    STNode invalidNode = invalidNodeMinutiae.invalidNode();
                    minutiaeJson.add("invalidNode", getJSONFromSTNode(invalidNode));
                    break;
                default:
                    break;
            }
            minutiaeJsonArray.add(minutiaeJson);
        }
        node.add(key, minutiaeJsonArray);
    }

    private void addDiagnosticsToJSON(STNode treeNode, JsonObject jsonNode) {
        if (!treeNode.hasDiagnostics()) {
            return;
        }

        jsonNode.addProperty("hasDiagnostics", treeNode.hasDiagnostics());
        Collection<STNodeDiagnostic> diagnostics = treeNode.diagnostics();
        if (diagnostics.isEmpty()) {
            return;
        }

        JsonArray diagnosticsJsonArray = new JsonArray();
        diagnostics.forEach(syntaxDiagnostic -> diagnosticsJsonArray.add(syntaxDiagnostic.diagnosticCode().toString()));
        jsonNode.add("diagnostics", diagnosticsJsonArray);
    }

    private boolean isToken(STNode node) {
        return SyntaxUtils.isToken(node);
    }

    private boolean isKeyword(SyntaxKind syntaxKind) {
        return SyntaxKind.IDENTIFIER_TOKEN.compareTo(syntaxKind) > 0 || syntaxKind == SyntaxKind.EOF_TOKEN;
    }

    private String getTokenText(STToken token) {
        switch (token.kind) {
            case IDENTIFIER_TOKEN:
                return ((STIdentifierToken) token).text;
            case STRING_LITERAL_TOKEN:
                String val = token.text();
                int stringLen = val.length();
                int lastCharPosition = val.endsWith("\"") ? stringLen - 1 : stringLen;
                return val.substring(1, lastCharPosition);
            case DECIMAL_INTEGER_LITERAL_TOKEN:
            case HEX_INTEGER_LITERAL_TOKEN:
            case DECIMAL_FLOATING_POINT_LITERAL_TOKEN:
            case HEX_FLOATING_POINT_LITERAL_TOKEN:
            case PARAMETER_NAME:
            case DEPRECATION_LITERAL:
            case INVALID_TOKEN:
                return token.text();
            default:
                return token.text();
        }
    }

    private String getASTFileName() {
        if (FileUtils.hasExtension(this.projectPath)) {
            String fileName = this.projectPath.getFileName().toString();
            int extensionIndex = fileName.lastIndexOf('.');
            if (extensionIndex > 0) {
                fileName = fileName.substring(0, extensionIndex);
            }
            return fileName + ".json";
        } else {
            Path ballerinaTomlPath = this.projectPath.resolve("Ballerina.toml");
            if (Files.exists(ballerinaTomlPath)) {
                try {
                    String content = Files.readString(ballerinaTomlPath, StandardCharsets.UTF_8);
                    String[] lines = content.split("\n");
                    for (String line : lines) {
                        line = line.trim();
                        if (line.startsWith("name") && line.contains("=")) {
                            String name = line.substring(line.indexOf('=') + 1).trim();
                            name = name.replaceAll("^\"|\"$", "");
                            if (!name.isEmpty()) {
                                return name + ".json";
                            }
                        }
                    }
                } catch (IOException e) {
                    this.errStream.println("Warning: Failed to read Ballerina.toml: " + e.getMessage());
                    return this.projectPath.getFileName().toString() + ".json";
                }
            }
            return this.projectPath.getFileName().toString() + ".json";
        }
    }
}
