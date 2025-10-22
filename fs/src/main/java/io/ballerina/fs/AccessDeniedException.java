package io.ballerina.fs;

import java.io.IOException;

public class AccessDeniedException extends IOException {
    
    private static final long serialVersionUID = 1L;
    
    public AccessDeniedException(String file) {
        super(file);
    }
    
    public AccessDeniedException(String file, String other, String reason) {
        super(file + (other != null ? " -> " + other : "") + (reason != null ? ": " + reason : ""));
    }
}
