package org.jackhuang.hmcl.bedrock;

import java.nio.file.Path;
public class BedrockVersion {
    private final String version;
    private final Path installPath;
    
    public BedrockVersion(String version, Path installPath) {
        this.version = version;
        this.installPath = installPath;
    }
    
    public String getVersion() {
        return version;
    }
    
    public Path getInstallPath() {
        return installPath;
    }
    
    @Override
    public String toString() {
        return "Bedrock " + version;
    }
}