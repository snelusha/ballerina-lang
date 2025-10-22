package io.ballerina.fs;

public abstract class FileSystemProvider {
    
    protected FileSystemProvider() {
    }
    
    public abstract String getScheme();
    
    public abstract FileSystem newFileSystem(java.net.URI uri, java.util.Map<String, ?> env) throws java.io.IOException;
    
    public abstract FileSystem getFileSystem(java.net.URI uri);
    
    public abstract Path getPath(java.net.URI uri);
}
