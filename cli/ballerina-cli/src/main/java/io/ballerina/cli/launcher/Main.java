/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.cli.launcher;

import io.ballerina.cli.BLauncherCmd;
import io.ballerina.cli.TaskExecutor;
import io.ballerina.cli.launcher.util.BalToolsUtil;
import io.ballerina.cli.task.CompileTask;
import io.ballerina.projects.*;
import io.ballerina.projects.directory.ProjectLoader;
import io.ballerina.projects.internal.BalToolsManifestBuilder;
import io.ballerina.projects.util.CustomURLClassLoader;
import io.ballerina.runtime.internal.utils.RuntimeUtils;
import io.ballerina.tools.diagnostics.Diagnostic;
import org.ballerinalang.central.client.exceptions.CentralClientException;
import org.ballerinalang.central.client.model.ToolResolutionCentralRequest;
import org.ballerinalang.central.client.model.ToolResolutionCentralResponse;
import org.ballerinalang.compiler.BLangCompilerException;
import picocli.CommandLine;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * This class executes a Ballerina program.
 *
 * @since 0.8.0
 */
public final class Main {

    private static final String UNMATCHED_ARGUMENT_PREFIX = "Unmatched argument";
    private static final String MISSING_REQUIRED_PARAMETER_PREFIX = "Missing required parameter";
    private static final String COMPILATION_ERROR_MESSAGE = "compilation contains errors";

    private static final PrintStream errStream = System.err;
    private static final PrintStream outStream = System.out;

    private Main() {
    }

    public static void main(String... args) {
        Project project = ProjectLoader.load("/Users/sithi/sandbox/somewhere/panic.bal").project();

       List<Diagnostic> buildToolDiagnostics = new ArrayList<>();
       TaskExecutor taskExecutor = new TaskExecutor.TaskBuilder()
               .addTask(new CompileTask(outStream, errStream, false, true, false, buildToolDiagnostics))
               .build();

       taskExecutor.executeTasks(project);
    }
}
