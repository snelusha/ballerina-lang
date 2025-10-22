package io.ballerina.fs;

import java.io.IOException;

class TPath implements io.ballerina.fs.Path {
    private final String path;

    public TPath(String first, String... more) {
        if (first == null) {
            throw new IllegalArgumentException("First path component cannot be null");
        }

        StringBuilder pathBuilder = new StringBuilder(first);
        for (String component : more) {
            if (component != null && !component.isEmpty()) {
                if (!pathBuilder.toString().endsWith("/") && !component.startsWith("/")) {
                    pathBuilder.append("/");
                }
                pathBuilder.append(component);
            }
        }
        this.path = pathBuilder.toString();
    }

    @Override
    public io.ballerina.fs.Path toAbsolutePath() {
        if (isAbsolute()) {
            return this;
        }
        throw new RuntimeException("Cannot resolve absolute path in web environment - no working directory");
    }

    @Override
    public io.ballerina.fs.Path normalize() {
        if (path.isEmpty()) {
            return new TPath("");
        }

        String[] components = path.split("/");
        java.util.List<String> normalized = new java.util.ArrayList<>();

        for (String component : components) {
            if (component.isEmpty() || ".".equals(component)) {
                // Skip empty components and current directory
                continue;
            } else if ("..".equals(component)) {
                // Go up one directory if possible
                if (!normalized.isEmpty() && !"..".equals(normalized.get(normalized.size() - 1))) {
                    normalized.remove(normalized.size() - 1);
                } else if (!isAbsolute()) {
                    // Keep .. for relative paths when we can't go up further
                    normalized.add("..");
                }
            } else {
                normalized.add(component);
            }
        }

        String result = String.join("/", normalized);
        if (isAbsolute() && !result.startsWith("/")) {
            result = "/" + result;
        }
        if (result.isEmpty() && !isAbsolute()) {
            result = ".";
        }

        return new TPath(result);
    }

    @Override
    public io.ballerina.fs.File toFile() {
        throw new RuntimeException("Path operations not supported in web environment");
    }

    @Override
    public boolean isAbsolute() {
        return path.startsWith("/");
    }

    @Override
    public io.ballerina.fs.Path resolve(io.ballerina.fs.Path relativePath) {
        if (relativePath instanceof TPath tPath) {
            return resolve(tPath.path);
        }
        throw new IllegalArgumentException("Incompatible path type");
    }

    @Override
    public io.ballerina.fs.Path getFileName() {
        if (path.isEmpty()) {
            return new TPath("");
        }

        int lastSlash = path.lastIndexOf('/');
        if (lastSlash == -1) {
            return new TPath(path);
        }

        String fileName = path.substring(lastSlash + 1);
        return new TPath(fileName);
    }

    @Override
    public io.ballerina.fs.Path getParent() {
        if (path.isEmpty()) {
            return null;
        }

        int lastSlash = path.lastIndexOf('/');
        if (lastSlash == -1) {
            return null;
        }

        if (lastSlash == 0) {
            return new TPath("/");
        }

        String parent = path.substring(0, lastSlash);
        return new TPath(parent);
    }

    @Override
    public io.ballerina.fs.Path resolve(String other) {
        if (other == null) {
            throw new IllegalArgumentException("Path component cannot be null");
        }

        if (other.isEmpty()) {
            return this;
        }

        if (other.startsWith("/")) {
            return new TPath(other);
        }

        if (path.isEmpty()) {
            return new TPath(other);
        }

        if (path.endsWith("/")) {
            return new TPath(path + other);
        } else {
            return new TPath(path + "/" + other);
        }
    }

    @Override
    public boolean isDirectory() {
        throw new RuntimeException("Path operations not supported in web environment");
    }

    @Override
    public boolean exists() {
        throw new RuntimeException("Path operations not supported in web environment");
    }

    @Override
    public boolean isRegularFile() {
        throw new RuntimeException("Path operations not supported in web environment");
    }

    @Override
    public boolean canWrite() {
        throw new RuntimeException("Path operations not supported in web environment");
    }

    @Override
    public boolean canRead() {
        throw new RuntimeException("Path operations not supported in web environment");
    }

    @Override
    public boolean canExecute() {
        throw new RuntimeException("Path operations not supported in web environment");
    }

    @Override
    public void createDirectories() {
        throw new RuntimeException("Path operations not supported in web environment");
    }

    @Override
    public boolean deleteDirectory() {
        throw new RuntimeException("Path operations not supported in web environment");
    }

    @Override
    public void deleteIfExists() throws IOException {
        throw new RuntimeException("Path operations not supported in web environment");
    }

    @Override
    public boolean notExists() {
        throw new RuntimeException("Path operations not supported in web environment");
    }

    @Override
    public void delete() {
        throw new RuntimeException("Path operations not supported in web environment");
    }

    @Override
    public void createFile() {
        throw new RuntimeException("Path operations not supported in web environment");
    }

    @Override
    public String readString() {
        throw new RuntimeException("Path operations not supported in web environment");
    }
    
    @Override
    public java.io.File toJavaFile() {
        throw new RuntimeException("Path operations not supported in web environment");
    }
    
    @Override
    public java.net.URI toUri() {
        throw new RuntimeException("Path operations not supported in web environment");
    }
    
    @Override
    public java.nio.file.Path toNioPath() {
        throw new RuntimeException("Path operations not supported in web environment");
    }
    
    @Override
    public FileSystem getFileSystem() {
        throw new RuntimeException("Path operations not supported in web environment");
    }
    
    @Override
    public int getNameCount() {
        String[] parts = path.split("/");
        return (int) java.util.Arrays.stream(parts).filter(s -> !s.isEmpty()).count();
    }
    
    @Override
    public Path getName(int index) {
        throw new RuntimeException("Path operations not supported in web environment");
    }
    
    @Override
    public Path subpath(int beginIndex, int endIndex) {
        throw new RuntimeException("Path operations not supported in web environment");
    }
    
    @Override
    public boolean startsWith(Path other) {
        if (other instanceof TPath tPath) {
            return path.startsWith(tPath.path);
        }
        return false;
    }
    
    @Override
    public boolean startsWith(String other) {
        return path.startsWith(other);
    }
    
    @Override
    public boolean endsWith(Path other) {
        if (other instanceof TPath tPath) {
            return path.endsWith(tPath.path);
        }
        return false;
    }
    
    @Override
    public boolean endsWith(String other) {
        return path.endsWith(other);
    }
    
    @Override
    public Path relativize(Path other) {
        throw new RuntimeException("Path operations not supported in web environment");
    }
    
    @Override
    public Path resolveSibling(Path other) {
        throw new RuntimeException("Path operations not supported in web environment");
    }
    
    @Override
    public Path resolveSibling(String other) {
        throw new RuntimeException("Path operations not supported in web environment");
    }
    
    @Override
    public Path toRealPath(LinkOption... options) throws java.io.IOException {
        throw new RuntimeException("Path operations not supported in web environment");
    }

    @Override
    public int compareTo(io.ballerina.fs.Path o) {
        if (o instanceof TPath other) {
            return this.path.compareTo(other.path);
        }
        throw new IllegalArgumentException("Cannot compare with incompatible path type");
    }

    @Override
    public String toString() {
        return path;
    }
}