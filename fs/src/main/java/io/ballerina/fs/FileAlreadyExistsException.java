package io.ballerina.fs;

import java.io.IOException;

public class FileAlreadyExistsException extends IOException {
    
    private static final long serialVersionUID = 1L;
    
    public FileAlreadyExistsException(String file) {
        super(file);
    }
    
    public FileAlreadyExistsException(String file, String other, String reason) {
        super(file + (other != null ? " -> " + other : "") + (reason != null ? ": " + reason : ""));
    }
}
