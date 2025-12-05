// FILE: rs\bgroi\Documents\dev\IntellijProjects\BotMaker\src\main\java\com\botmaker\init\AppDependencyConfigurator.java
package com.botmaker.init;

import com.botmaker.config.ApplicationConfig;
import com.botmaker.di.DependencyContainer;
import com.botmaker.events.EventBus;
import com.botmaker.parser.AstRewriter;
import com.botmaker.parser.BlockFactory;
import com.botmaker.parser.NodeCreator;
import com.botmaker.runtime.CodeExecutionService;
import com.botmaker.services.*;
import com.botmaker.state.ApplicationState;
import com.botmaker.ui.BlockDragAndDropManager;
import com.botmaker.ui.UIManager;
import com.botmaker.validation.DiagnosticsManager;
import javafx.stage.Stage;

public class AppDependencyConfigurator {

    public static void configure(DependencyContainer container, ApplicationConfig config, Stage primaryStage) {
        // Core Config & State
        container.registerSingleton(ApplicationConfig.class, config);
        container.registerSingleton(ApplicationState.class, new ApplicationState());
        container.registerSingleton(EventBus.class, new EventBus(config.isEnableEventLogging()));

        // Parsing & AST
        container.registerSingleton(BlockFactory.class, new BlockFactory());
        container.registerSingleton(NodeCreator.class, new NodeCreator());
        container.registerLazySingleton(AstRewriter.class, () ->
                new AstRewriter(container.resolve(NodeCreator.class)));

        // UI Helpers & Validation
        container.registerSingleton(DiagnosticsManager.class, new DiagnosticsManager());

        // MODIFIED: Inject ApplicationState into BlockDragAndDropManager
        container.registerSingleton(BlockDragAndDropManager.class,
                new BlockDragAndDropManager(container.resolve(ApplicationState.class)));

        // Services
        registerServices(container);

        // UI Manager (Requires Stage)
        container.registerLazySingleton(UIManager.class, () -> {
            return new UIManager(
                    container.resolve(BlockDragAndDropManager.class),
                    container.resolve(EventBus.class),
                    container.resolve(CodeEditorService.class),
                    container.resolve(DiagnosticsManager.class),
                    primaryStage,
                    container.resolve(ApplicationConfig.class),
                    container.resolve(ApplicationState.class)
            );
        });
    }

    private static void registerServices(DependencyContainer container) {
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
                    text -> eventBus.publish(new com.botmaker.events.CoreApplicationEvents.OutputAppendedEvent(text)),
                    () -> eventBus.publish(new com.botmaker.events.CoreApplicationEvents.OutputClearedEvent()),
                    text -> eventBus.publish(new com.botmaker.events.CoreApplicationEvents.OutputSetEvent(text)),
                    msg -> eventBus.publish(new com.botmaker.events.CoreApplicationEvents.StatusMessageEvent(msg)),
                    container.resolve(DiagnosticsManager.class),
                    container.resolve(ApplicationConfig.class),
                    container.resolve(ApplicationState.class)
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
    }
}