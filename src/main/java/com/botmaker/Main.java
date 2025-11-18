package com.botmaker;

import com.botmaker.config.ApplicationConfig;
import com.botmaker.di.DependencyContainer;
import com.botmaker.events.CoreApplicationEvents;
import com.botmaker.events.EventBus;
import com.botmaker.parser.AstRewriter;
import com.botmaker.parser.BlockFactory;
import com.botmaker.runtime.CodeExecutionService;
import com.botmaker.services.*;
import com.botmaker.state.ApplicationState;
import com.botmaker.ui.BlockDragAndDropManager;
import com.botmaker.ui.ProjectSelectionScreen;
import com.botmaker.ui.UIManager;
import com.botmaker.validation.DiagnosticsManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

/**
 * Main application entry point.
 * Phase 3: Final cleanup - Main is now just a thin bootstrapper.
 */
public class Main extends Application {

    private DependencyContainer container;
    private LanguageServerService languageServerService;

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Show project selection screen first
        ProjectSelectionScreen selectionScreen = new ProjectSelectionScreen(
                primaryStage,
                projectName -> openProject(primaryStage, projectName)
        );

        primaryStage.setScene(selectionScreen.createScene());
        primaryStage.setTitle("BotMaker - Select Project");
        primaryStage.show();
    }

    /**
     * Opens a project in the editor
     */
    private void openProject(Stage primaryStage, String projectName) {
        try {
            // Initialize dependency container with project-specific config
            container = new DependencyContainer();
            ApplicationConfig config = ApplicationConfig.forProject(projectName);

            // Setup all dependencies
            setupDependencies(config);

            // Initialize services in correct order
            initializeServices();

            // Get UI and show
            UIManager uiManager = container.resolve(UIManager.class);
            primaryStage.setScene(uiManager.createScene());
            primaryStage.setTitle("BotMaker Blocks - " + projectName);

            // Load initial code
            CodeEditorService codeEditorService = container.resolve(CodeEditorService.class);
            codeEditorService.loadInitialCode();

            // Update close handler
            primaryStage.setOnCloseRequest(e -> {
                e.consume();
                new Thread(() -> {
                    try {
                        if (languageServerService != null) {
                            System.out.println("Shutting down Language Server...");
                            languageServerService.shutdown();
                            System.out.println("Language Server shut down successfully.");
                        }
                    } catch (Exception ex) {
                        System.err.println("Error during shutdown: " + ex.getMessage());
                    } finally {
                        Platform.runLater(() -> {
                            Platform.exit();
                            System.exit(0);
                        });
                    }
                }).start();
            });

        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Error opening project: " + e.getMessage());
        }
    }

    /**
     * Shows an error dialog
     */
    private void showErrorDialog(String message) {
        Alert alert = new Alert(
               Alert.AlertType.ERROR
        );
        alert.setTitle("Error");
        alert.setHeaderText("Failed to open project");
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Setup all dependencies in the container
     */
    private void setupDependencies(ApplicationConfig config) {
        // Core infrastructure
        container.registerSingleton(ApplicationConfig.class, config);
        container.registerSingleton(ApplicationState.class, new ApplicationState());
        container.registerSingleton(EventBus.class, new EventBus(config.isEnableEventLogging()));

        // Utilities
        container.registerSingleton(BlockFactory.class, new BlockFactory());
        container.registerSingleton(AstRewriter.class, new AstRewriter());
        container.registerSingleton(
                com.botmaker.validation.DiagnosticsManager.class,
                new com.botmaker.validation.DiagnosticsManager()
        );

        // BlockDragAndDropManager - will be wired after CodeEditorService exists
        container.registerSingleton(BlockDragAndDropManager.class,
                new BlockDragAndDropManager(null)
        );

        // Services
        container.registerLazySingleton(LanguageServerService.class, () ->
                new LanguageServerService(
                        container.resolve(ApplicationConfig.class),
                        container.resolve(ApplicationState.class),
                        container.resolve(EventBus.class),
                        container.resolve(com.botmaker.validation.DiagnosticsManager.class)
                )
        );

        container.registerLazySingleton(CodeExecutionService.class, () -> {
            EventBus eventBus = container.resolve(EventBus.class);
            return new CodeExecutionService(
                    text -> eventBus.publish(new CoreApplicationEvents.OutputAppendedEvent(text)),
                    () -> eventBus.publish(new CoreApplicationEvents.OutputClearedEvent()),
                    text -> eventBus.publish(new CoreApplicationEvents.OutputSetEvent(text)),
                    msg -> eventBus.publish(new CoreApplicationEvents.StatusMessageEvent(msg)),
                    container.resolve(DiagnosticsManager.class),
                    container.resolve(ApplicationConfig.class)
            );
        });

        container.registerLazySingleton(CodeEditorService.class, () ->
                new CodeEditorService(
                        container.resolve(ApplicationConfig.class),
                        container.resolve(ApplicationState.class),
                        container.resolve(EventBus.class),
                        container.resolve(BlockFactory.class),
                        container.resolve(AstRewriter.class),
                        container.resolve(BlockDragAndDropManager.class),
                        container.resolve(LanguageServerService.class),
                        container.resolve(com.botmaker.validation.DiagnosticsManager.class)
                )
        );

        container.registerLazySingleton(ExecutionService.class, () ->
                new ExecutionService(
                        container.resolve(ApplicationConfig.class),
                        container.resolve(ApplicationState.class),
                        container.resolve(EventBus.class),
                        container.resolve(CodeExecutionService.class),
                        container.resolve(com.botmaker.validation.DiagnosticsManager.class)
                )
        );

        container.registerLazySingleton(DebuggingService.class, () ->
                new DebuggingService(
                        container.resolve(ApplicationState.class),
                        container.resolve(EventBus.class),
                        container.resolve(CodeExecutionService.class),
                        container.resolve(BlockFactory.class),
                        container.resolve(ApplicationConfig.class)
                )
        );

        container.registerLazySingleton(UIManager.class, () ->
                new UIManager(
                        container.resolve(BlockDragAndDropManager.class),
                        container.resolve(EventBus.class),
                        container.resolve(CodeEditorService.class),
                        container.resolve(com.botmaker.validation.DiagnosticsManager.class)
                )
        );
    }

    /**
     * Initialize services in the correct order and ensure event subscriptions are set up
     */
    private void initializeServices() throws Exception {
        // 1. Initialize LanguageServerService first (needs LSP connection)
        languageServerService = container.resolve(LanguageServerService.class);
        languageServerService.initialize();

        // 2. Get CodeEditorService (depends on initialized LSS)
        CodeEditorService codeEditorService = container.resolve(CodeEditorService.class);

        // 3. Wire up drag-and-drop callback now that CodeEditorService exists
        BlockDragAndDropManager dragAndDropManager = container.resolve(BlockDragAndDropManager.class);
        dragAndDropManager.setCallback(dropInfo ->
                codeEditorService.getCodeEditor().addStatement(
                        dropInfo.targetBody(),
                        dropInfo.type(),
                        dropInfo.insertionIndex()
                )
        );

        // 4. CRITICAL: Force initialization of services that subscribe to events
        //    This ensures their constructors run and event subscriptions are registered
        container.resolve(ExecutionService.class);
        container.resolve(DebuggingService.class);
        container.resolve(UIManager.class);
    }

    public static void main(String[] args) {
        launch(args);
    }
}