// HMCL/src/main/java/org/jackhuang/hmcl/bedrock/BedrockLauncher.java
package org.jackhuang.hmcl.bedrock;

import java.io.File;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class BedrockLauncher {
    
    private static final String EXE_NAME = "Minecraft.Windows.exe";
    private static final String FOLDER_NAME = "Minecraft for Windows";
    
    private Path installPath;
    private Path exePath;
    private boolean isInstalled;
    
    public BedrockLauncher() {
        detectInstallation();
    }
    
    private void detectInstallation() {
        // 1. 先检查默认 XboxGames 路径
        String systemDrive = System.getenv("SystemDrive");
        if (systemDrive != null) {
            Path defaultPath = Paths.get(systemDrive + "\\XboxGames\\Minecraft for Windows\\Content\\" + EXE_NAME);
            if (Files.exists(defaultPath)) {
                installPath = defaultPath.getParent();
                exePath = defaultPath;
                isInstalled = true;
                System.out.println("找到基岩版: " + exePath);
                return;
            }
        }
        
        // 2. 遍历所有磁盘分区查找
        File[] roots = File.listRoots();
        for (File root : roots) {
            try {
                Path searchPath = Paths.get(root.getAbsolutePath(), "XboxGames", FOLDER_NAME, "Content");
                Path exeFile = searchPath.resolve(EXE_NAME);
                if (Files.exists(exeFile)) {
                    installPath = searchPath;
                    exePath = exeFile;
                    isInstalled = true;
                    System.out.println("找到基岩版: " + exePath);
                    return;
                }
            } catch (Exception e) {
                // 忽略无法访问的磁盘
            }
        }
        
        // 3. 深度搜索 XboxGames 文件夹
        for (File root : roots) {
            Path xboxGamesPath = Paths.get(root.getAbsolutePath(), "XboxGames");
            if (Files.exists(xboxGamesPath)) {
                try {
                    Optional<Path> found = Files.find(xboxGamesPath, 10, (path, attrs) -> 
                        path.getFileName().toString().equalsIgnoreCase(EXE_NAME)
                    ).findFirst();
                    
                    if (found.isPresent()) {
                        exePath = found.get();
                        installPath = exePath.getParent();
                        isInstalled = true;
                        System.out.println("深度搜索找到基岩版: " + exePath);
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        
        isInstalled = false;
        System.out.println("未找到基岩版");
    }
    
    public boolean isInstalled() {
        return isInstalled;
    }
    
    public Path getExePath() {
        return exePath;
    }
    
    public Path getInstallPath() {
        return installPath;
    }
    
    public CompletableFuture<Boolean> launch() {
        return CompletableFuture.supplyAsync(() -> {
            if (!isInstalled || exePath == null) {
                return false;
            }
            
            try {
                ProcessBuilder pb = new ProcessBuilder(exePath.toString());
                pb.directory(installPath.toFile());
                pb.inheritIO();
                Process process = pb.start();
                int exitCode = process.waitFor();
                return exitCode == 0;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }
    
    public List<BedrockVersion> getInstalledVersions() {
        List<BedrockVersion> versions = new ArrayList<>();
        if (!isInstalled) return versions;
        versions.add(new BedrockVersion("基岩版", installPath));
        return versions;
    }
}