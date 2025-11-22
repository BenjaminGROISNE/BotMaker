package com.botmaker.ui;

import com.botmaker.config.ApplicationConfig;
import com.botmaker.services.CodeEditorService;
import com.botmaker.state.ApplicationState;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

public class FileExplorerManager {

    private final ApplicationConfig config;
    private final CodeEditorService codeEditorService;
    private final ApplicationState state;
    private final TreeView<Path> fileTree;

    public FileExplorerManager(ApplicationConfig config, CodeEditorService codeEditorService, ApplicationState state) {
        this.config = config;
        this.codeEditorService = codeEditorService;
        this.state = state;
        this.fileTree = new TreeView<>();
    }

    public VBox createView() {
        VBox container = new VBox();
        container.getStyleClass().add("file-explorer");

        Label header = new Label("Project Files");
        header.getStyleClass().add("sidebar-header");

        Button newFileBtn = new Button("New Function Library");
        newFileBtn.setMaxWidth(Double.MAX_VALUE);
        newFileBtn.setOnAction(e -> showCreateFileDialog());

        configureTree();
        refreshTree();

        container.getChildren().addAll(header, newFileBtn, fileTree);
        return container;
    }

    private void configureTree() {
        fileTree.setShowRoot(false);
        fileTree.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(Path item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getFileName().toString());
                    // Highlight active file
                    if (state.getActiveFile() != null && item.equals(state.getActiveFile().getPath())) {
                        setStyle("-fx-font-weight: bold; -fx-text-fill: #007bff;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        fileTree.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.getValue() != null) {
                // Switch file
                Path selectedPath = newVal.getValue();
                // Avoid reloading if already active
                if (state.getActiveFile() == null || !state.getActiveFile().getPath().equals(selectedPath)) {
                    codeEditorService.switchToFile(selectedPath);
                    fileTree.refresh(); // Update bold styling
                }
            }
        });
    }

    public void refreshTree() {
        // Find src directory
        Path sourceDir = config.getSourceFilePath().getParent();

        TreeItem<Path> root = new TreeItem<>(sourceDir);

        try (Stream<Path> files = Files.list(sourceDir)) {
            files.filter(p -> p.toString().endsWith(".java"))
                    .forEach(path -> root.getChildren().add(new TreeItem<>(path)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        fileTree.setRoot(root);
    }

    private void showCreateFileDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("New Function Library");
        dialog.setHeaderText("Create a new library of functions");
        dialog.setContentText("Name (e.g. Movement):");

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            String className = name.trim().replaceAll("[^a-zA-Z0-9]", "");
            if (!className.isEmpty()) {
                codeEditorService.createFile(className);
                refreshTree();
            }
        });
    }
}