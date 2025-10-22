package io.ballerina.fs;

import java.io.IOException;

public class DirectoryNotEmptyException extends IOException {
    
    private static final long serialVersionUID = 1L;
    
    public DirectoryNotEmptyException(String dir) {
        super(dir);
    }
}
