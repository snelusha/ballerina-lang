package io.ballerina.fs;

import java.io.IOException;
import java.net.URI;

public interface FileSystem extends java.io.Closeable {
    
    FileSystemProvider provider();
    
    void close() throws IOException;
    
    boolean isOpen();
    
    boolean isReadOnly();
    
    String getSeparator();
    
    Iterable<Path> getRootDirectories();
    
    Iterable<FileStore> getFileStores();
    
    java.util.Set<String> supportedFileAttributeViews();
    
    Path getPath(String first, String... more);
    
    PathMatcher getPathMatcher(String syntaxAndPattern);
    
    java.nio.file.attribute.UserPrincipalLookupService getUserPrincipalLookupService();
    
    java.nio.file.WatchService newWatchService() throws IOException;
}
