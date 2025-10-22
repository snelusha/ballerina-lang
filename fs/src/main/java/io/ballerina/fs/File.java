package io.ballerina.fs;

public interface File {

    long length();

    String getName();

    String getFileName();
    
    java.io.File toJavaFile();

    boolean exists();

    boolean isDirectory();

    boolean canWrite();

    boolean canRead();

    boolean canExecute();

    boolean isFile();

    java.io.File[] listFiles();

    long lastModified();
}