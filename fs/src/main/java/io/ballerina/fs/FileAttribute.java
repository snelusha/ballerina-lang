package io.ballerina.fs;

public interface FileAttribute<T> {
    String name();
    T value();
}
