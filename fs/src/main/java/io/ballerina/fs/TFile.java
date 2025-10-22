package io.ballerina.fs;

class TFile implements File {

    TFile() {
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public boolean canWrite() {
        return true;
    }

    @Override
    public boolean canRead() {
        return true;
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    @Override
    public boolean isFile() {
        return true;
    }

    @Override
    public java.io.File[] listFiles() {
        return new java.io.File[0];
    }

    @Override
    public long lastModified() {
        return 0;
    }

    @Override
    public long length() {
        throw new RuntimeException("File operations not supported in web environment");
    }

    @Override
    public String getName() {
        throw new RuntimeException("File operations not supported in web environment");
    }

    @Override
    public String getFileName() {
        throw new RuntimeException("File operations not supported in web environment");
    }

    @Override
    public java.io.File toJavaFile() {
        throw new RuntimeException("File operations not supported in web environment");
    }
}