package com.botmaker.ui;

import com.botmaker.project.ProjectCreator;
import com.botmaker.project.ProjectInfo;
import com.botmaker.project.ProjectManager;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Project selection screen shown on startup with project creation capability
 */
public class ProjectSelectionScreen {

    private final ProjectManager projectManager;
    private final ProjectCreator projectCreator;

    // Changed to BiConsumer to pass (ProjectName, ShouldClearCache)
    private final BiConsumer<String, Boolean> onProjectSelected;

    private final Stage stage;
    private ListView<ProjectInfo> projectListView;

    // New Checkbox
    private CheckBox clearCacheCheckbox;

    public ProjectSelectionScreen(Stage stage, BiConsumer<String, Boolean> onProjectSelected) {
        this.stage = stage;
        this.projectManager = new ProjectManager();
        this.projectCreator = new ProjectCreator();
        this.onProjectSelected = onProjectSelected;
    }

    public Scene createScene() {
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // Header
        Label titleLabel = new Label("Select a Project");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        VBox header = new VBox(10, titleLabel);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 20, 0));

        // Project list
        projectListView = new ListView<>();
        projectListView.setPrefHeight(400);

        projectListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(ProjectInfo project, boolean empty) {
                super.updateItem(project, empty);
                if (empty || project == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox box = new VBox(5);
                    Label nameLabel = new Label(project.getName());
                    nameLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

                    Label pathLabel = new Label(project.getProjectPath().toString());
                    pathLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");

                    Label dateLabel = new Label("Last modified: " +
                            project.getLastModified().format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")));
                    dateLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");

                    box.getChildren().addAll(nameLabel, pathLabel, dateLabel);
                    setGraphic(box);
                }
            }
        });

        refreshProjectList();

        if (!projectListView.getItems().isEmpty()) {
            projectListView.getSelectionModel().select(0);
        }

        // Controls Area
        Button openButton = new Button("Open Project");
        openButton.setPrefWidth(150);
        openButton.setDefaultButton(true);
        openButton.setOnAction(e -> openSelectedProject());

        Button createButton = new Button("Create New Project");
        createButton.setPrefWidth(150);
        createButton.setOnAction(e -> showCreateProjectDialog());

        // CLEAR CACHE CHECKBOX
        clearCacheCheckbox = new CheckBox("Clear Language Server Cache (Fix startup freeze)");
        clearCacheCheckbox.setStyle("-fx-text-fill: #666; -fx-font-size: 11px;");
        clearCacheCheckbox.setTooltip(new Tooltip("Check this if the application hangs on loading."));

        projectListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                openSelectedProject();
            }
        });

        HBox buttonBox = new HBox(10, openButton, createButton);
        buttonBox.setAlignment(Pos.CENTER);

        VBox footer = new VBox(15, clearCacheCheckbox, buttonBox);
        footer.setAlignment(Pos.CENTER);
        footer.setPadding(new Insets(20, 0, 0, 0));

        VBox center = new VBox(10, projectListView, footer);
        root.setTop(header);
        root.setCenter(center);

        Scene scene = new Scene(root, 600, 550); // Increased height slightly


        return scene;
    }

    private void openSelectedProject() {
        ProjectInfo selected = projectListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            onProjectSelected.accept(selected.getName(), clearCacheCheckbox.isSelected());
        }
    }

    // ... rest of file (refreshProjectList, showCreateProjectDialog, etc) remains the same ...

    private void refreshProjectList() {
        List<ProjectInfo> projects = projectManager.listProjects();
        projectListView.getItems().clear();
        projectListView.getItems().addAll(projects);
    }

    private void showCreateProjectDialog() {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Create New Project");
        dialog.setHeaderText("Enter project name");

        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));

        TextField projectNameField = new TextField();
        projectNameField.setPromptText("ProjectName");

        Label instructionLabel = new Label("Project name must:");
        instructionLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");

        Label rule1 = new Label("• Start with an uppercase letter");
        rule1.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");

        Label rule2 = new Label("• Contain only letters and numbers");
        rule2.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");

        Label rule3 = new Label("• Be between 2-50 characters");
        rule3.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");

        Label exampleLabel = new Label("Example: MyFirstProject");
        exampleLabel.setStyle("-fx-font-size: 10px; -fx-font-style: italic; -fx-text-fill: gray;");

        content.getChildren().addAll(
                new Label("Project Name:"),
                projectNameField,
                instructionLabel,
                rule1,
                rule2,
                rule3,
                exampleLabel
        );

        dialog.getDialogPane().setContent(content);

        Button createButton = (Button) dialog.getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);

        projectNameField.textProperty().addListener((observable, oldValue, newValue) -> {
            boolean isValid = isValidProjectName(newValue);
            createButton.setDisable(!isValid);
            if (newValue.isEmpty()) projectNameField.setStyle("");
            else if (isValid) projectNameField.setStyle("-fx-border-color: green; -fx-border-width: 2px;");
            else projectNameField.setStyle("-fx-border-color: red; -fx-border-width: 2px;");
        });

        javafx.application.Platform.runLater(projectNameField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) return projectNameField.getText();
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        result.ifPresent(this::createProject);
    }

    private boolean isValidProjectName(String name) {
        if (name == null || name.trim().isEmpty()) return false;
        if (!name.matches("^[A-Z][a-zA-Z0-9]*$")) return false;
        if (name.length() < 2 || name.length() > 50) return false;
        if (projectCreator.projectExists(name)) return false;
        return true;
    }

    private void createProject(String projectName) {
        try {
            projectCreator.createProject(projectName);
            refreshProjectList();
            for (ProjectInfo project : projectListView.getItems()) {
                if (project.getName().equals(projectName)) {
                    projectListView.getSelectionModel().select(project);
                    break;
                }
            }
        } catch (Exception e) {
            Alert errorAlert = new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("Error");
            errorAlert.setHeaderText("Failed to create project");
            errorAlert.setContentText(e.getMessage());
            errorAlert.showAndWait();
        }
    }
}