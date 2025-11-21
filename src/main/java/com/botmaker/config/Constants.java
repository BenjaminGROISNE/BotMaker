package com.botmaker.config;

/**
 * Application-wide constants
 */
public class Constants {

    // Debugger Configuration
    public static final int DEBUGGER_MAX_CONNECT_RETRIES = 10;
    public static final int DEBUGGER_RETRY_DELAY_MS = 250;
    public static final int DEBUGGER_SHUTDOWN_TIMEOUT_SECONDS = 2;
    public static final int DEBUGGER_EXIT_DELAY_MS = 500;

    // Thread Sleep Times
    public static final int SHORT_SLEEP_MS = 500;

    // JVM Options
    public static final String JVM_MAX_HEAP = "-Xmx1G";
    public static final String JVM_ENTITY_SIZE_LIMIT = "-Djdk.xml.maxGeneralEntitySizeLimit=0";
    public static final String JVM_TOTAL_ENTITY_SIZE_LIMIT = "-Djdk.xml.totalEntitySizeLimit=0";

    // LSP Configuration
    public static final String LSP_DETECT_VM_DISABLED = "-DDetectVMInstallationsJob.disabled=true";
    public static final String LSP_FILE_ENCODING = "-Dfile.encoding=UTF-8";
    public static final String LSP_LOG_DISABLE = "-Xlog:disable";
    public static final String LSP_DEPENDENCY_COLLECTOR = "-Daether.dependencyCollector.impl=bf";

    // Debug Flags (disable for production)
    public static final boolean LSP_LOG_PROTOCOL = true;
    public static final String LSP_LOG_LEVEL = "ALL";

    private Constants() {} // Prevent instantiation
}