package io.ballerina.fs;

import java.io.IOException;
import java.util.Iterator;

public interface DirectoryStream<T> extends java.io.Closeable, Iterable<T> {
    
    @Override
    Iterator<T> iterator();
    
    interface Filter<T> {
        boolean accept(T entry) throws IOException;
    }
}
