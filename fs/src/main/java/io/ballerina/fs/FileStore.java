package io.ballerina.fs;

public abstract class FileStore {
    
    protected FileStore() {
    }
    
    public abstract String name();
    
    public abstract String type();
    
    public abstract boolean isReadOnly();
    
    public abstract long getTotalSpace() throws java.io.IOException;
    
    public abstract long getUsableSpace() throws java.io.IOException;
    
    public abstract long getUnallocatedSpace() throws java.io.IOException;
}
