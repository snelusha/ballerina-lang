package io.ballerina.fs;

public class FileSystemAlreadyExistsException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    public FileSystemAlreadyExistsException() {
        super();
    }
    
    public FileSystemAlreadyExistsException(String msg) {
        super(msg);
    }
}
