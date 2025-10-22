package io.ballerina.fs;

import java.io.IOException;
import java.net.URI;

public interface Path extends Comparable<Path> {

    static Path of(String first, String... more) {
        return new TPath(first, more);
    }

    Path toAbsolutePath();

    Path normalize();

    File toFile();
    
    java.io.File toJavaFile();
    
    URI toUri();
    
    java.nio.file.Path toNioPath();

    boolean isAbsolute();

    Path resolve(Path relativePath);

    Path getFileName();

    Path getParent();

    Path resolve(String name);

    boolean isDirectory();

    boolean exists();

    boolean isRegularFile();

    boolean canWrite();

    boolean canRead();

    boolean canExecute();

    void createDirectories();

    boolean deleteDirectory();

    void deleteIfExists() throws IOException;

    boolean notExists();

    void delete();

    void createFile();

    String readString();
    
    FileSystem getFileSystem();
    
    int getNameCount();
    
    Path getName(int index);
    
    Path subpath(int beginIndex, int endIndex);
    
    boolean startsWith(Path other);
    
    boolean startsWith(String other);
    
    boolean endsWith(Path other);
    
    boolean endsWith(String other);
    
    Path relativize(Path other);
    
    Path resolveSibling(Path other);
    
    Path resolveSibling(String other);

    Path toRealPath(LinkOption... options) throws IOException;

    String toString();
}
