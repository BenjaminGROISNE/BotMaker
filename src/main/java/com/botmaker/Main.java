package com.botmaker;

import com.botmaker.config.ApplicationConfig;
import com.botmaker.core.BodyBlock;
import com.botmaker.core.StatementBlock;
import com.botmaker.di.DependencyContainer;
import com.botmaker.events.CoreApplicationEvents;
import com.botmaker.events.EventBus;
import com.botmaker.parser.AstRewriter;
import com.botmaker.parser.BlockFactory;
import com.botmaker.parser.NodeCreator;
import com.botmaker.project.ProjectConfig;
import com.botmaker.runtime.CodeExecutionService;
import com.botmaker.services.*;
import com.botmaker.state.ApplicationState;
import com.botmaker.ui.BlockDragAndDropManager;
import com.botmaker.ui.ProjectSelectionScreen;
import com.botmaker.ui.UIManager;
import com.botmaker.util.BlockLookupHelper;
import com.botmaker.validation.DiagnosticsManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Paths;

public class Main extends Application {

    private DependencyContainer container;
    private LanguageServerService languageServerService;

    @Override
    public void start(Stage primaryStage) throws Exception {
        String lastProject = ProjectConfig.getLastOpened();
        if (lastProject != null && projectExists(lastProject)) {
            System.out.println("Auto-loading last project: " + lastProject);
            openProject(primaryStage, lastProject, false);
        } else {
            showProjectSelection(primaryStage);
        }
    }

    private void showProjectSelection(Stage primaryStage) {
        ProjectSelectionScreen selectionScreen = new ProjectSelectionScreen(
                primaryStage,
                (projectName, clearCache) -> openProject(primaryStage, projectName, clearCache)
        );
        primaryStage.setScene(selectionScreen.createScene());
        primaryStage.setTitle("BotMaker - Select Project");
        primaryStage.show();
    }

    private boolean projectExists(String projectName) {
        return Files.exists(Paths.get("projects", projectName)) &&
                Files.exists(Paths.get("projects", projectName, "build.gradle"));
    }

    private void openProject(Stage primaryStage, String projectName, boolean clearCache) {
        try {
            ProjectConfig.updateLastOpened(projectName);
            container = new DependencyContainer();
            ApplicationConfig config = ApplicationConfig.forProject(projectName);
            setupDependencies(config, primaryStage);

            if (clearCache) {
                LanguageServerService lss = container.resolve(LanguageServerService.class);
                lss.setShouldClearCache(true);
            }

            initializeServices();
            UIManager uiManager = container.resolve(UIManager.class);
            primaryStage.setScene(uiManager.createScene());
            primaryStage.setTitle("BotMaker Blocks - " + projectName);
            CodeEditorService codeEditorService = container.resolve(CodeEditorService.class);
            codeEditorService.loadInitialCode();
            primaryStage.show();

            primaryStage.setOnCloseRequest(e -> {
                e.consume();
                new Thread(() -> {
                    try {
                        if (languageServerService != null) languageServerService.shutdown();
                    } catch (Exception ex) {
                        System.err.println("Error during shutdown: " + ex.getMessage());
                    } finally {
                        Platform.runLater(() -> { Platform.exit(); System.exit(0); });
                    }
                }).start();
            });

        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("Error opening project: " + e.getMessage());
            if (ProjectConfig.getLastOpened() != null) showProjectSelection(primaryStage);
        }
    }

    private void showErrorDialog(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Failed to open project");
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void setupDependencies(ApplicationConfig config, Stage primaryStage) {
        container.registerSingleton(ApplicationConfig.class, config);
        container.registerSingleton(ApplicationState.class, new ApplicationState());
        container.registerSingleton(EventBus.class, new EventBus(config.isEnableEventLogging()));

        container.registerSingleton(BlockFactory.class, new BlockFactory());
        container.registerSingleton(NodeCreator.class, new NodeCreator());
        container.registerLazySingleton(AstRewriter.class, () -> new AstRewriter(container.resolve(NodeCreator.class)));

        container.registerSingleton(DiagnosticsManager.class, new DiagnosticsManager());
        container.registerSingleton(BlockDragAndDropManager.class, new BlockDragAndDropManager(null));

        container.registerLazySingleton(LanguageServerService.class, () ->
                new LanguageServerService(
                        container.resolve(ApplicationConfig.class),
                        container.resolve(ApplicationState.class),
                        container.resolve(EventBus.class),
                        container.resolve(DiagnosticsManager.class)
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
                        container.resolve(DiagnosticsManager.class)
                )
        );

        container.registerLazySingleton(ExecutionService.class, () ->
                new ExecutionService(
                        container.resolve(ApplicationConfig.class),
                        container.resolve(ApplicationState.class),
                        container.resolve(EventBus.class),
                        container.resolve(CodeExecutionService.class),
                        container.resolve(DiagnosticsManager.class)
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

        container.registerLazySingleton(UIManager.class, () -> {
            UIManager uiManager = new UIManager(
                    container.resolve(BlockDragAndDropManager.class),
                    container.resolve(EventBus.class),
                    container.resolve(CodeEditorService.class),
                    container.resolve(DiagnosticsManager.class),
                    primaryStage
            );
            uiManager.setOnSelectProject(v -> showProjectSelection(primaryStage));
            return uiManager;
        });
    }

    private void initializeServices() throws Exception {
        languageServerService = container.resolve(LanguageServerService.class);
        languageServerService.initialize();
        CodeEditorService codeEditorService = container.resolve(CodeEditorService.class);
        ApplicationState state = container.resolve(ApplicationState.class);
        BlockDragAndDropManager dragAndDropManager = container.resolve(BlockDragAndDropManager.class);

        dragAndDropManager.setCallback(dropInfo ->
                codeEditorService.getCodeEditor().addStatement(dropInfo.targetBody(), dropInfo.type(), dropInfo.insertionIndex())
        );

        dragAndDropManager.setMoveCallback(moveInfo -> {
            StatementBlock blockToMove = BlockLookupHelper.findBlockById(moveInfo.blockId(), state.getNodeToBlockMap());
            if (blockToMove != null) {
                BodyBlock sourceBody = BlockLookupHelper.findParentBody(blockToMove, state.getNodeToBlockMap());
                if (sourceBody != null) {
                    codeEditorService.getCodeEditor().moveStatement(blockToMove, sourceBody, moveInfo.targetBody(), moveInfo.insertionIndex());
                }
            }
        });

        container.resolve(ExecutionService.class);
        container.resolve(DebuggingService.class);
        container.resolve(UIManager.class);
    }

    public static void main(String[] args) { launch(args); }
}