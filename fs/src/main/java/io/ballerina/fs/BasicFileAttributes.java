package io.ballerina.fs;

public interface BasicFileAttributes {
    java.nio.file.attribute.FileTime lastModifiedTime();
    java.nio.file.attribute.FileTime lastAccessTime();
    java.nio.file.attribute.FileTime creationTime();
    boolean isRegularFile();
    boolean isDirectory();
    boolean isSymbolicLink();
    boolean isOther();
    long size();
    Object fileKey();
}
