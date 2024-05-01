package application;

import java.io.File;
import java.util.ArrayList;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Slider;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ImageProcessorApp extends Application {
	
    private ImageView imageView = new ImageView();
    private BarChart<String, Number> histogramChart;
    private CheckBox showImageCheckbox = new CheckBox("Keep Image");
    private ChoiceBox<String> tableOptionsChoiceBox = new ChoiceBox<>();
    private VBox displayArea = new VBox(10);
    private VBox optionsBox = new VBox(10);
    private Label infoLabel = new Label("This JavaFX application helps you calculate the gray-level histogram for a BMP image of your choice");
    private Stage mainStage; 
    private Stage histogramStage; 
    private Stage tableStage; 
    private Stage extraStage;
    private ListView<String> optionsListView = new ListView<>();
    
    private Timeline timeline;
    private double timeSeconds = 0;
    
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Gray-level histogram");
        Label welcomeLabel = new Label("Welcome!");
        welcomeLabel.setFont(new Font(24)); 
        
        Image image = new Image(getClass().getResourceAsStream("pic.png"));
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(100);  // You can set the size as per your requirement
        imageView.setFitWidth(100);
        imageView.setPreserveRatio(true);
        
        ToggleButton toggleInfoButton = new ToggleButton("More Info");
        infoLabel.setVisible(false); 
        infoLabel.setWrapText(true);
        toggleInfoButton.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            infoLabel.setVisible(isNowSelected);
            toggleInfoButton.setText(isNowSelected ? "Hide Info" : "Show Info");
        });

        Hyperlink nextWindowLink = new Hyperlink("Open Main Window");
        nextWindowLink.setOnAction(e -> {
            primaryStage.close();
            openMainWindow();
        });

        VBox layout = new VBox(20, welcomeLabel, imageView, toggleInfoButton, infoLabel, nextWindowLink);
        layout.setPadding(new Insets(20));
        
        Scene primaryScene = new Scene(layout, 600, 400);
        primaryStage.setScene(primaryScene); 
        primaryScene.getStylesheets().add(getClass().getResource("primary.css").toExternalForm());
        welcomeLabel.getStyleClass().add("title-label");
        infoLabel.getStyleClass().add("label");
        nextWindowLink.getStyleClass().add("hyperlink");
        layout.getStyleClass().add("vbox");
        
        primaryStage.show();
        
        mainStage = new Stage();
        mainStage.setOnCloseRequest(event -> {
            if (histogramStage != null && histogramStage.isShowing()) {
                histogramStage.close();
            }
            if (tableStage != null && tableStage.isShowing()) {
                tableStage.close();
            }
            
            if (extraStage != null && extraStage.isShowing()) {
            	extraStage.close();
            }
            
        });
    }

    private void openMainWindow() {
        mainStage.setTitle("Image Processor - Main");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS); 

        BorderPane mainLayout = new BorderPane();
        scrollPane.setContent(mainLayout);

        Scene mainScene = new Scene(scrollPane, 1200, 1000);
        mainScene.getStylesheets().add(getClass().getResource("main.css").toExternalForm()); 
        mainStage.setScene(mainScene);

        setupMenu(mainStage);
        setupHistogramChart();
        setupUIComponents();

        displayArea.getStyleClass().add("vbox"); 
        imageView.getStyleClass().add("image-view"); 

        displayArea.getChildren().clear();
        displayArea.getChildren().add(imageView);
        mainLayout.setCenter(displayArea);

        mainStage.show();
    }


    private void setupMenu(Stage stage) {
        if (!(stage.getScene().getRoot() instanceof ScrollPane)) {
            throw new IllegalStateException("Root is not a ScrollPane as expected");
        }

        ScrollPane scrollPane = (ScrollPane) stage.getScene().getRoot();
        if (!(scrollPane.getContent() instanceof BorderPane)) {
            throw new IllegalStateException("ScrollPane content is not a BorderPane as expected");
        }

        BorderPane root = (BorderPane) scrollPane.getContent();

        MenuBar menuBar = new MenuBar();
        Menu fileMenu = new Menu("File");
        MenuItem openItem = new MenuItem("Open Image");
        openItem.setOnAction(e -> openImage(stage));
        fileMenu.getItems().add(openItem);
        menuBar.getMenus().add(fileMenu);
        
        Menu extraMenu = new Menu("Extra");
        MenuItem openExtraWindowItem = new MenuItem("Open Extra Window");
        openExtraWindowItem.setOnAction(e -> openExtraWindow());
        extraMenu.getItems().add(openExtraWindowItem);
        menuBar.getMenus().add(extraMenu);

        root.setTop(menuBar);
    }

    private void setupHistogramChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        histogramChart = new BarChart<>(xAxis, yAxis);
        xAxis.setLabel("Gray Levels");
        yAxis.setLabel("Frequency");
    }

    private void setupUIComponents() {
        tableOptionsChoiceBox.setItems(FXCollections.observableArrayList("Generate Table", "No Table"));
        tableOptionsChoiceBox.setValue("Generate Table"); 

        HBox controlBox = new HBox(10, showImageCheckbox, tableOptionsChoiceBox);
        Button calculateHistogramButton = new Button("Proceed");
        Tooltip histogramTooltip = new Tooltip("Click to calculate the histogram & generate the table*");
        calculateHistogramButton.setTooltip(histogramTooltip);
        calculateHistogramButton.setOnAction(e -> handleButtonInMain());
        
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));

        Label optionsLabel = new Label("Your options are:");

        optionsListView.setItems(FXCollections.observableArrayList());
        optionsListView.setMaxHeight(50);
        updateOptionsList(); 
        
        showImageCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            updateOptionsList();
        });

        tableOptionsChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updateOptionsList();
        });

        optionsBox.getChildren().addAll(separator, controlBox, optionsLabel, optionsListView, calculateHistogramButton);
        optionsBox.setPadding(new Insets(10));
    }

    private void updateOptionsList() {
        ArrayList<String> options = new ArrayList<>();
        if (showImageCheckbox.isSelected()) {
            options.add("Keep Image");
        } else {
        	options.add("Only display the histogram");
        }
        
        if ("Generate Table".equals(tableOptionsChoiceBox.getValue())) {
            options.add("Generate Table");
        } else {
            options.add("Don't generate table");
        }
        optionsListView.setItems(FXCollections.observableArrayList(options));
    }

    private void openImage(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("BMP Images", "*.bmp"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            Image image = new Image(file.toURI().toString());
            imageView.setImage(image);

            if (histogramStage != null && histogramStage.isShowing()) {
                histogramStage.close();
            }
            
            if(tableStage != null && tableStage.isShowing()) {
            	tableStage.close();
            }

            if (!displayArea.getChildren().contains(optionsBox)) {
                displayArea.getChildren().add(optionsBox);
            }
        }
    }

    private void handleButtonInMain() {
    	openHistogramWindow();
    	
    	 if (tableOptionsChoiceBox.getValue().equals("Generate Table") && imageView.getImage() != null) {
             openTableWindow();
         }   
    	 
    }

    @SuppressWarnings("unchecked")
    private void openTableWindow() {
        tableStage = new Stage();
        tableStage.setTitle("Table Window");

        TableView<HistogramData> tableView = new TableView<>();
        TableColumn<HistogramData, String> rangeColumn = new TableColumn<>("Gray Level Range");
        rangeColumn.setCellValueFactory(new PropertyValueFactory<>("bucketRange"));
        TableColumn<HistogramData, Integer> countColumn = new TableColumn<>("Pixel Count");
        countColumn.setCellValueFactory(new PropertyValueFactory<>("pixelCount"));
        tableView.getColumns().addAll(rangeColumn, countColumn);

        TreeItem<HistogramData> root = new TreeItem<>(new HistogramData("All Ranges", 0));  // Root node for the tree table
        TreeTableView<HistogramData> treeTableView = new TreeTableView<>(root);
        treeTableView.setShowRoot(false);

        treeTableView.getColumns().add(createTreeTableColumnForRange());
        treeTableView.getColumns().add(createTreeTableColumnForCount());

        XYChart.Series<String, Number> series = HistogramUtils.calculateHistogram(imageView.getImage());
        for (XYChart.Data<String, Number> data : series.getData()) {
            String range = data.getXValue();
            int count = data.getYValue().intValue();
            HistogramData histogramData = new HistogramData(range, count);
            tableView.getItems().add(histogramData);
            root.getChildren().add(new TreeItem<>(histogramData));  // Add the same data to the TreeTableView
        }

        Label titleLabel = new Label("Histogram Data");
        VBox layout = new VBox(10, titleLabel, tableView, treeTableView);
        layout.setPadding(new Insets(10));

        Scene scene = new Scene(layout, 300, 500);  // Adjusted for better visibility of both components
        scene.getStylesheets().add(getClass().getResource("table.css").toExternalForm());
        titleLabel.getStyleClass().add("title-label");
        tableStage.setScene(scene);
        tableStage.show();
    }

    private TreeTableColumn<HistogramData, String> createTreeTableColumnForRange() {
        TreeTableColumn<HistogramData, String> treeRangeColumn = new TreeTableColumn<>("Gray Level Range");
        treeRangeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("bucketRange"));
        return treeRangeColumn;
    }

    private TreeTableColumn<HistogramData, Number> createTreeTableColumnForCount() {
        TreeTableColumn<HistogramData, Number> treeCountColumn = new TreeTableColumn<>("Pixel Count");
        treeCountColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("pixelCount"));
        return treeCountColumn;
    }
    
    

    private void openHistogramWindow() {
        histogramStage = new Stage(); 
        histogramStage.setTitle("Histogram Window");

        if (imageView.getImage() != null) {
            XYChart.Series<String, Number> series = HistogramUtils.calculateHistogram(imageView.getImage());
            histogramChart.getData().clear();
            histogramChart.getData().add(series);

            VBox layout = new VBox(10);
            Label titleLabel = new Label("Histogram");
            layout.getChildren().addAll(titleLabel, histogramChart);

            if (showImageCheckbox.isSelected()) {
                ImageView duplicateImageView = new ImageView(imageView.getImage());
                layout.getChildren().add(1, duplicateImageView);
            }

            Scene scene = new Scene(layout, 1200, 1000);
            scene.getStylesheets().add(getClass().getResource("hist.css").toExternalForm());
            titleLabel.getStyleClass().add("title-label");
            histogramStage.setScene(scene);
            histogramStage.show();
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
	private void openExtraWindow() {
        extraStage = new Stage();
        extraStage.setTitle("Extra Controls Window");

        RadioButton radioButton = new RadioButton("Option");
        Label radioLabel = new Label();
        radioLabel.textProperty().bind(Bindings.when(radioButton.selectedProperty())
            .then("Your choice is: Selected")
            .otherwise("Your choice is: Not Selected"));
        
        TextField textField = new TextField();
        Label textFieldLabel = new Label();
        textFieldLabel.textProperty().bind(Bindings.concat("Your input is: ").concat(textField.textProperty()));
        
        PasswordField passwordField = new PasswordField();
        Label passwordFieldLabel = new Label();
        passwordFieldLabel.textProperty().bind(Bindings.concat("Your password is: ").concat(passwordField.textProperty()));

        ComboBox<String> comboBox = new ComboBox<>(FXCollections.observableArrayList("Option 1", "Option 2", "Option 3"));
        Label comboBoxLabel = new Label();
        comboBoxLabel.textProperty().bind(Bindings.concat("Your choice is: ").concat(comboBox.valueProperty()));

        Slider slider = new Slider(0, 100, 50);
        Label sliderLabel = new Label();
        sliderLabel.textProperty().bind(slider.valueProperty().asString("Slider Value: %.0f"));

        ProgressBar progressBar = new ProgressBar(0.0);
        progressBar.setPrefWidth(300);
        ProgressIndicator progressIndicator = new ProgressIndicator(0.0);

        Button startButton = new Button("Start/Resume Timer");
        Button stopButton = new Button("Stop Timer");
        startButton.setMinWidth(120);
        stopButton.setMinWidth(120);

        startButton.setOnAction(e -> startOrResumeTimer(progressBar, progressIndicator));
        stopButton.setOnAction(e -> stopTimer());
        Button resetButton = new Button("Reset Timer");
        resetButton.setOnAction(e -> resetTimer(progressBar, progressIndicator));

        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(startButton, resetButton, stopButton);

        VBox progressLayout = new VBox(10);
        progressLayout.setAlignment(Pos.CENTER);
        progressLayout.getChildren().addAll(progressBar, progressIndicator, buttonBox);
        
        TreeItem rootItem = new TreeItem("Controls");

        TreeItem fpItem = new TreeItem("First Page");
        fpItem.getChildren().add(new TreeItem("Labels"));
        fpItem.getChildren().add(new TreeItem("ImageView"));
        fpItem.getChildren().add(new TreeItem("Toggle Button"));
        fpItem.getChildren().add(new TreeItem("Hyperlink"));
        rootItem.getChildren().add(fpItem);

        TreeItem mainItem = new TreeItem("Main Page");
        mainItem.getChildren().add(new TreeItem("Menu"));
        mainItem.getChildren().add(new TreeItem("ImageView"));
        mainItem.getChildren().add(new TreeItem("Labels"));
        mainItem.getChildren().add(new TreeItem("Checkbox"));
        mainItem.getChildren().add(new TreeItem("ChoiceBox"));
        mainItem.getChildren().add(new TreeItem("ScrollPane"));
        mainItem.getChildren().add(new TreeItem("ScrollBar"));
        mainItem.getChildren().add(new TreeItem("FileChooser"));
        mainItem.getChildren().add(new TreeItem("ListView"));
        mainItem.getChildren().add(new TreeItem("Button"));
        mainItem.getChildren().add(new TreeItem("Tooltip"));
        mainItem.getChildren().add(new TreeItem("Separator"));
        rootItem.getChildren().add(mainItem);
        
        
        TreeItem histItem = new TreeItem("Histogram Page");
        histItem.getChildren().add(new TreeItem("BarChart"));
        histItem.getChildren().add(new TreeItem("ImageView"));
        histItem.getChildren().add(new TreeItem("Labels"));
        rootItem.getChildren().add(histItem);
        
		TreeItem tableItem = new TreeItem("Table Page");
        tableItem.getChildren().add(new TreeItem("Label"));
        tableItem.getChildren().add(new TreeItem("TableView"));
        rootItem.getChildren().add(tableItem);
        
        TreeView treeView = new TreeView();
        treeView.setRoot(rootItem);

        treeView.setShowRoot(true);
        VBox treeVBox = new VBox(treeView);

        VBox layout = new VBox(10);
        layout.getChildren().addAll(
            radioButton, radioLabel,
            textField, textFieldLabel,
            passwordField, passwordFieldLabel,
            comboBox, comboBoxLabel,
            slider, sliderLabel,
            progressLayout, 
            treeVBox
        );

        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 800, 600);
        extraStage.setScene(scene);
        extraStage.show();
    }
    
    private void resetTimer(ProgressBar progressBar, ProgressIndicator progressIndicator) {
        if (timeline != null) {
            timeline.stop(); // Stops the timeline
        }
        progressBar.setProgress(0);
        progressIndicator.setProgress(0);
        timeSeconds = 0; // Reset the time counter
        timeline = null; // Reset the timeline
    }
    
    
    private void startOrResumeTimer(ProgressBar progressBar, ProgressIndicator progressIndicator) {
        if (timeline == null) {
            timeline = new Timeline();
            timeline.setCycleCount(Timeline.INDEFINITE);
            KeyFrame keyFrame = new KeyFrame(
                Duration.seconds(1),
                event -> {
                    timeSeconds += 1.0;
                    double progress = timeSeconds / 60.0;
                    progressBar.setProgress(progress);
                    progressIndicator.setProgress(progress);

                    if (progress >= 1.0) {
                        timeline.stop();
                    }
                }
            );
            timeline.getKeyFrames().add(keyFrame);
        }
        timeline.play(); 
    }

    private void stopTimer() {
        if (timeline != null) {
            timeline.pause(); 
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
