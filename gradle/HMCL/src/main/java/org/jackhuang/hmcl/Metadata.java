/*
 * Laoxu Minecraft Launcher
 * Copyright (C) 2026 Laoxu and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.jackhuang.hmcl;

import org.jackhuang.hmcl.util.StringUtils;
import org.jackhuang.hmcl.util.io.JarUtils;
import org.jackhuang.hmcl.util.platform.Architecture;
import org.jackhuang.hmcl.util.platform.OperatingSystem;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;

/**
 * Stores metadata about this application.
 */
public final class Metadata {
    private Metadata() {
    }
    // ========== Laoxu Minecraft Launcher 品牌信息 ==========
    public static final String NAME = "Laoxu";
    public static final String FULL_NAME = "Laoxu Minecraft Launcher";
    public static final String VERSION = System.getProperty("laoxu.version.override", JarUtils.getAttribute("laoxu.version", "1.0.0"));

    public static final String TITLE = NAME + " Launcher " + VERSION;
    public static final String FULL_TITLE = FULL_NAME + " v" + VERSION;

    public static final int MINIMUM_REQUIRED_JAVA_VERSION = 17;
    public static final int MINIMUM_SUPPORTED_JAVA_VERSION = 17;
    public static final int RECOMMENDED_JAVA_VERSION = 21;

    // ========== Laoxu 官方网站和链接 ==========
    public static final String PUBLISH_URL = "https://github.com/xujinhong114514/LaoxuLauncher";
    public static final String ABOUT_URL = PUBLISH_URL + "/README.md";
    public static final String DOWNLOAD_URL = PUBLISH_URL + "/releases";
    public static final String LAOXU_UPDATE_URL = System.getProperty("laoxu.update_source.override", PUBLISH_URL + "/releases/latest");

    public static final String DOCS_URL = PUBLISH_URL + "/wiki";
    public static final String CONTACT_URL = PUBLISH_URL + "/issues";
    public static final String CHANGELOG_URL = PUBLISH_URL + "/releases";
    public static final String EULA_URL = PUBLISH_URL + "/blob/main/LICENSE";
    public static final String GROUPS_URL = PUBLISH_URL;

    // 保留原有构建信息
    public static final String BUILD_CHANNEL = JarUtils.getAttribute("laoxu.version.type", "release");
    public static final String GITHUB_SHA = JarUtils.getAttribute("laoxu.version.hash", null);

    public static final Path CURRENT_DIRECTORY = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
    public static final Path MINECRAFT_DIRECTORY = OperatingSystem.getWorkingDirectory("minecraft");
    public static final Path HMCL_GLOBAL_DIRECTORY;
    public static final Path HMCL_CURRENT_DIRECTORY;
    public static final Path DEPENDENCIES_DIRECTORY;

    static {
        String laoxuHome = System.getProperty("laoxu.home");
        if (laoxuHome == null) {
            if (OperatingSystem.CURRENT_OS.isLinuxOrBSD()) {
                String xdgData = System.getenv("XDG_DATA_HOME");
                if (StringUtils.isNotBlank(xdgData)) {
                    HMCL_GLOBAL_DIRECTORY = Paths.get(xdgData, "laoxu").toAbsolutePath().normalize();
                } else {
                    HMCL_GLOBAL_DIRECTORY = Paths.get(System.getProperty("user.home"), ".local", "share", "laoxu").toAbsolutePath().normalize();
                }
            } else {
                HMCL_GLOBAL_DIRECTORY = OperatingSystem.getWorkingDirectory("laoxu");
            }
        } else {
            HMCL_GLOBAL_DIRECTORY = Paths.get(laoxuHome).toAbsolutePath().normalize();
        }

        String laoxuCurrentDir = System.getProperty("laoxu.dir");
        HMCL_CURRENT_DIRECTORY = laoxuCurrentDir != null
                ? Paths.get(laoxuCurrentDir).toAbsolutePath().normalize()
                : CURRENT_DIRECTORY.resolve(".laoxu");
        DEPENDENCIES_DIRECTORY = HMCL_CURRENT_DIRECTORY.resolve("dependencies");
    }

    public static boolean isStable() {
        return "release".equals(BUILD_CHANNEL);
    }

    public static boolean isDev() {
        return "dev".equals(BUILD_CHANNEL);
    }

    public static boolean isNightly() {
        return !isStable() && !isDev();
    }

    public static @Nullable String getSuggestedJavaDownloadLink() {
        if (OperatingSystem.CURRENT_OS == OperatingSystem.LINUX && Architecture.SYSTEM_ARCH == Architecture.LOONGARCH64_OW)
            return "https://www.loongnix.cn/zh/api/java/downloads-jdk21/index.html";
        else {
            EnumSet<Architecture> supportedArchitectures;
            if (OperatingSystem.CURRENT_OS == OperatingSystem.WINDOWS)
                supportedArchitectures = EnumSet.of(Architecture.X86_64, Architecture.X86, Architecture.ARM64);
            else if (OperatingSystem.CURRENT_OS == OperatingSystem.LINUX)
                supportedArchitectures = EnumSet.of(
                        Architecture.X86_64, Architecture.X86,
                        Architecture.ARM64, Architecture.ARM32,
                        Architecture.RISCV64, Architecture.LOONGARCH64
                );
            else if (OperatingSystem.CURRENT_OS == OperatingSystem.MACOS)
                supportedArchitectures = EnumSet.of(Architecture.X86_64, Architecture.ARM64);
            else
                supportedArchitectures = EnumSet.noneOf(Architecture.class);
            if (supportedArchitectures.contains(Architecture.SYSTEM_ARCH))
                return String.format("https://docs.laoxu.net/downloads/%s/%s.html",
                        OperatingSystem.CURRENT_OS.getCheckedName(),
                        Architecture.SYSTEM_ARCH.getCheckedName()
                );
            else
                return null;
        }
    }
}