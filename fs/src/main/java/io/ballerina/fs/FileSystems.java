package io.ballerina.fs;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

public final class FileSystems {

    private FileSystems() {
        // Utility class, no instances
    }

    public static FileSystem getDefault() {
        throw new RuntimeException("File system operations not supported in web environment");
    }

    public static FileSystem getFileSystem(URI uri) {
        throw new RuntimeException("File system operations not supported in web environment");
    }

    public static FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
        throw new RuntimeException("File system operations not supported in web environment");
    }

    public static FileSystem newFileSystem(URI uri, Map<String, ?> env, ClassLoader loader) throws IOException {
        throw new RuntimeException("File system operations not supported in web environment");
    }

    public static FileSystem newFileSystem(Path path, ClassLoader loader) throws IOException {
        throw new RuntimeException("File system operations not supported in web environment");
    }
}
