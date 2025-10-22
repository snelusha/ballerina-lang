package io.ballerina.fs;

import java.io.IOException;

public class NoSuchFileException extends IOException {
    
    private static final long serialVersionUID = 1L;
    
    public NoSuchFileException(String file) {
        super(file);
    }
    
    public NoSuchFileException(String file, String other, String reason) {
        super(file + (other != null ? " -> " + other : "") + (reason != null ? ": " + reason : ""));
    }
}
