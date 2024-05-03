package application;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
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
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;

public class ImageProcessorApp extends Application {
	
    // Variabile membre pentru diverse componente UI
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
    
    private Timeline timeline; //folosit pentru timer ul din fereastra extra
    private double timeSeconds = 0; //timpul scurs pentru timer
    
    @Override
    public void start(Stage primaryStage) {
        // Configurarea ferestrei principale
        primaryStage.setTitle("Gray-level histogram");
        Label welcomeLabel = new Label("Welcome!");
        welcomeLabel.setFont(new Font(24)); 
        
        // Incarcare imagine si setarea proprietatilor
        Image image = new Image(getClass().getResourceAsStream("pic.png"));
        ImageView imageView = new ImageView(image);
        imageView.setFitHeight(100);  
        imageView.setFitWidth(100);
        imageView.setPreserveRatio(true);
        
        // Adaugare togglebutton
        ToggleButton toggleInfoButton = new ToggleButton("More Info");
        infoLabel.setVisible(false); //setare visibilitate  
        infoLabel.setWrapText(true); 
        // Adaugare listener care se activeaza la schimbarile state-ului butonului 
        toggleInfoButton.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            infoLabel.setVisible(isNowSelected);
            toggleInfoButton.setText(isNowSelected ? "Hide Info" : "Show Info");
        });

        // Adaugare Hyperlink si definire listener ce deschide fereastra principala a aplicatiei la click 
        Hyperlink nextWindowLink = new Hyperlink("Open Main Window");
        nextWindowLink.setOnAction(e -> {
            primaryStage.close();
            openMainWindow();
        });

        // Gruparea elementelor definite anterior intr un VBox 
        VBox layout = new VBox(20, welcomeLabel, imageView, toggleInfoButton, infoLabel, nextWindowLink);
        layout.setPadding(new Insets(20));
        
        // Crearea scenei pentru primul ecran si adaugarea elementelor definite anterior
        Scene primaryScene = new Scene(layout, 600, 400);
        primaryStage.setScene(primaryScene); 
        // Adaugarea stilurilor din fisierul css si atribuirea claselor din fisier 
        primaryScene.getStylesheets().add(getClass().getResource("primary.css").toExternalForm());
        welcomeLabel.getStyleClass().add("title-label");
        infoLabel.getStyleClass().add("label");
        nextWindowLink.getStyleClass().add("hyperlink");
        layout.getStyleClass().add("vbox");
        
        primaryStage.show();
        
        mainStage = new Stage();
        // Inchiderea ferestrelor secundare la inchiderea celei principale 
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

    // Metoda pentru ecranul principal
    private void openMainWindow() {
        mainStage.setTitle("Main Window");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS); 

        BorderPane mainLayout = new BorderPane();
        scrollPane.setContent(mainLayout);

        Scene mainScene = new Scene(scrollPane, 1200, 1000);
        // Adaugare stiluri 
        mainScene.getStylesheets().add(getClass().getResource("main.css").toExternalForm()); 
        mainStage.setScene(mainScene);

        // Apelare metode pentru initializarea elementelor principale din ecran
        
        try {
        	setupMenu(mainStage);
        } catch(IllegalStateException e) {
        	System.out.println("Exception encountered when setting up the menu: " + e.getMessage());
        }
        setupHistogramChart();
        setupUIComponents();

        // Adaugare stiluri
        displayArea.getStyleClass().add("vbox"); 
        imageView.getStyleClass().add("image-view"); 

        displayArea.getChildren().clear();
        displayArea.getChildren().add(imageView);
        mainLayout.setCenter(displayArea);

        mainStage.show();
    }


    private void setupMenu(Stage stage) {
    	// Verificari
        if (!(stage.getScene().getRoot() instanceof ScrollPane)) {
            throw new IllegalStateException("Root is not a ScrollPane as expected");
        }

        ScrollPane scrollPane = (ScrollPane) stage.getScene().getRoot();
        if (!(scrollPane.getContent() instanceof BorderPane)) {
            throw new IllegalStateException("ScrollPane content is not a BorderPane as expected");
        }

        BorderPane root = (BorderPane) scrollPane.getContent();

        // Initializeaza meniul, adaugare listenere si actiuni 
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

    // Initializeaza grafic pentru histograma
    private void setupHistogramChart() {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        histogramChart = new BarChart<>(xAxis, yAxis);
        xAxis.setLabel("Gray Levels");
        yAxis.setLabel("Frequency");
    }

    private void setupUIComponents() {
        // Seteaza optiunile disponibile si valoarea implicita pentru ChoiceBox
        tableOptionsChoiceBox.setItems(FXCollections.observableArrayList("Generate Table", "No Table"));
        tableOptionsChoiceBox.setValue("Generate Table"); 

        // Creeaza un container orizontal pentru checkbox si choicebox
        HBox controlBox = new HBox(10, showImageCheckbox, tableOptionsChoiceBox);

        // Configureaza butonul pentru a calcula histograma si generarea tabelului
        Button calculateHistogramButton = new Button("Proceed");
        Tooltip histogramTooltip = new Tooltip("Click to calculate the histogram & generate the table*");
        calculateHistogramButton.setTooltip(histogramTooltip);
        calculateHistogramButton.setOnAction(e -> handleButtonInMain());
        
        // Separator pentru a adauga spatiu vizual intre elemente
        Separator separator = new Separator();
        separator.setPadding(new Insets(10, 0, 10, 0));

        // Eticheta pentru descrierea optiunilor utilizatorului
        Label optionsLabel = new Label("Your options are:");

        // Configurare ListView pentru a afisa optiunile selectate
        optionsListView.setItems(FXCollections.observableArrayList());
        optionsListView.setMaxHeight(50);
        updateOptionsList(); 
        
        // Adaugarea listenerilor pentru a actualiza lista de optiuni cand starea checkbox-ului se schimba
        showImageCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            updateOptionsList();
        });

        // Adaugarea listenerilor pentru a actualiza lista de optiuni cand se schimba selectia in ChoiceBox
        tableOptionsChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updateOptionsList();
        });

        // Adauga toate controlerele intr-un VBox pentru a organiza UI-ul
        optionsBox.getChildren().addAll(separator, controlBox, optionsLabel, optionsListView, calculateHistogramButton);
        optionsBox.setPadding(new Insets(10));
    }


    private void updateOptionsList() {
        // Creaza o lista pentru a stoca optiunile selectate de utilizator
        ArrayList<String> options = new ArrayList<>();
        
        // Verifica starea checkbox-ului si adauga optiunea corespunzatoare in lista
        if (showImageCheckbox.isSelected()) {
            options.add("Keep Image");
        } else {
            options.add("Only display the histogram");
        }
        
        // Verifica valoarea selectata in ChoiceBox si adauga optiunea corespunzatoare in lista
        if ("Generate Table".equals(tableOptionsChoiceBox.getValue())) {
            options.add("Generate Table");
        } else {
            options.add("Don't generate table");
        }
        
        // Actualizeaza ListView-ul cu optiunile selectate
        optionsListView.setItems(FXCollections.observableArrayList(options));
    }
    
    private void openImage(Stage stage) {
        // Creaza un FileChooser pentru a selecta fisiere
        FileChooser fileChooser = new FileChooser();
        // Seteaza filtrul pentru tipurile de fisiere, permitand doar imagini BMP
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("BMP Images", "*.bmp"));
        // Deschide dialogul pentru selectarea fisierului si stocheaza referinta la fisierul ales
        File file = fileChooser.showOpenDialog(stage);
        // Verifica daca un fisier a fost selectat
        if (file != null) {
            // Incarca imaginea din fisier
            Image image = new Image(file.toURI().toString());
            // Seteaza imaginea in componenta ImageView
            imageView.setImage(image);

            // Inchide fereastra de histograma daca este deschisa
            if (histogramStage != null && histogramStage.isShowing()) {
                histogramStage.close();
            }
            
            // Inchide fereastra tabelului daca este deschisa
            if (tableStage != null && tableStage.isShowing()) {
                tableStage.close();
            }

            // Adauga optiunile la zona de afisare daca nu sunt deja prezente
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
        // Creeaza si configureaza o noua fereastra pentru tabel
        tableStage = new Stage();
        tableStage.setTitle("Table Window");

        // Creeaza TableView pentru a afisa datele histogramelor
        TableView<HistogramData> tableView = new TableView<>();
        TableColumn<HistogramData, String> rangeColumn = new TableColumn<>("Gray Level Range");
        rangeColumn.setCellValueFactory(new PropertyValueFactory<>("bucketRange"));
        TableColumn<HistogramData, Integer> countColumn = new TableColumn<>("Pixel Count");
        countColumn.setCellValueFactory(new PropertyValueFactory<>("pixelCount"));
        tableView.getColumns().addAll(rangeColumn, countColumn);

        // Configureaza root-ul pentru TreeTableView
        TreeItem<HistogramData> root = new TreeItem<>(new HistogramData("All Ranges", 0));
        TreeTableView<HistogramData> treeTableView = new TreeTableView<>(root);
        treeTableView.setShowRoot(false);

        // Adauga coloanele create personalizat la TreeTableView
        treeTableView.getColumns().add(createTreeTableColumnForRange());
        treeTableView.getColumns().add(createTreeTableColumnForCount());

        // Calculeaza si proceseaza datele histogramelor pentru a fi afisate
        XYChart.Series<String, Number> series = HistogramUtils.calculateHistogram(imageView.getImage());
        for (XYChart.Data<String, Number> data : series.getData()) {
            String range = data.getXValue();
            int count = data.getYValue().intValue();
            HistogramData histogramData = new HistogramData(range, count);
            tableView.getItems().add(histogramData);
            root.getChildren().add(new TreeItem<>(histogramData));  // Adauga aceleasi date si in TreeTableView
        }

        // Creeaza si adauga layout-ul componentelor vizuale
        Label titleLabel = new Label("Histogram Data");
        
        HBox tables = new HBox(10, tableView, treeTableView);
        VBox layout = new VBox(10, titleLabel, tables);
        layout.setPadding(new Insets(10));

        // Seteaza scena si arata fereastra
        Scene scene = new Scene(layout, 300, 500);  
        scene.getStylesheets().add(getClass().getResource("table.css").toExternalForm());
        titleLabel.getStyleClass().add("title-label");
        layout.getStyleClass().add("vbox");
        tableStage.setScene(scene);
        tableStage.show();
    }

 // Metoda pentru crearea unei coloane pentru intervalul de niveluri de gri in TreeTableView
    private TreeTableColumn<HistogramData, String> createTreeTableColumnForRange() {
        TreeTableColumn<HistogramData, String> treeRangeColumn = new TreeTableColumn<>("Gray Level Range");
        treeRangeColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("bucketRange"));
        return treeRangeColumn;
    }

    // Metoda pentru crearea unei coloane pentru numarul de pixeli in TreeTableView
    private TreeTableColumn<HistogramData, Number> createTreeTableColumnForCount() {
        TreeTableColumn<HistogramData, Number> treeCountColumn = new TreeTableColumn<>("Pixel Count");
        treeCountColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("pixelCount"));
        return treeCountColumn;
    }


    private void openHistogramWindow() {
        // Creeaza o noua fereastra pentru histograma
        histogramStage = new Stage(); 
        histogramStage.setTitle("Histogram Window");

        // Verifica daca imaginea din imageView nu este nula
        if (imageView.getImage() != null) {
            // Calculeaza histograma imaginii si o adauga intr-o serie de date
            XYChart.Series<String, Number> series = HistogramUtils.calculateHistogram(imageView.getImage());
            histogramChart.getData().clear(); 
            histogramChart.getData().add(series); 

            // Creeaza layout-ul pentru fereastra histogramei
            VBox layout = new VBox(10);
            Label titleLabel = new Label("Histogram");
            
            Button saveButton = new Button("Save Histogram");
            saveButton.setOnAction(e -> saveHistogramLocally(series));
            
            layout.getChildren().addAll(titleLabel, histogramChart, saveButton);

            // Verifica daca optiunea pentru afisarea imaginii este selectata
            if (showImageCheckbox.isSelected()) {
                // Daca da, creeaza o copie a imaginii si o adauga în layout
                ImageView duplicateImageView = new ImageView(imageView.getImage());
                layout.getChildren().add(1, duplicateImageView); 
            }

            // Creeaza scena pentru fereastra histogramei
            Scene scene = new Scene(layout, 1200, 1000);
            scene.getStylesheets().add(getClass().getResource("hist.css").toExternalForm()); // Adauga stilurile CSS
            titleLabel.getStyleClass().add("title-label");
            layout.getStyleClass().add("vbox");
            histogramStage.setScene(scene); // Seteaza scena in fereastra histogramei
            histogramStage.show(); // Afiseaza fereastra histogramei
        }
    }
    
    private void saveHistogramLocally(XYChart.Series<String, Number> series) {
        // Create a FileChooser to prompt the user to select the save location
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Histogram Image");
        // Set the extension filter to restrict to image file formats
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("PNG Files", "*.png")
            );
        // Show the save dialog and get the selected file
        File file = fileChooser.showSaveDialog(histogramStage);
        if (file != null) {
            try {
                // Render the chart to a WritableImage
                WritableImage writableImage = renderChartAsImage(histogramChart);
                // Convert the WritableImage to a BufferedImage
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);
                // Save the BufferedImage to the selected file
                ImageIO.write(bufferedImage, getFileExtension(file.getName()), file);
                System.out.println("Histogram image saved successfully.");
            } catch (IOException e) {
                System.out.println("Error saving histogram image: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Method to render the chart as an image
    private WritableImage renderChartAsImage(BarChart<String, Number> chart) {
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setDepthBuffer(true);
        return chart.snapshot(parameters, null);
    }

    // Method to get the file extension from a file name
    private String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == fileName.length() - 1) {
            return ""; // No file extension found
        }
        return fileName.substring(dotIndex + 1).toLowerCase();
    }



    @SuppressWarnings({ "unchecked", "rawtypes" })
    private void openExtraWindow() {
        // Creeaza o noua fereastra pentru controale suplimentare
        extraStage = new Stage();
        extraStage.setTitle("Extra Controls Window");

        // Creeaza elementele de control si le ataseaza etichetele corespunzatoare
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

        // Defineste acciunile pentru butoanele de start, stop si resetare a timerului
        startButton.setOnAction(e -> startOrResumeTimer(progressBar, progressIndicator));
        stopButton.setOnAction(e -> stopTimer());
        Button resetButton = new Button("Reset Timer");
        resetButton.setOnAction(e -> resetTimer(progressBar, progressIndicator));

        // Creeaza un layout pentru butoanele de control ale timerului
        HBox buttonBox = new HBox(20);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(startButton, resetButton, stopButton);

        // Creeaza un layout pentru bara de progres si indicatorul de progres
        VBox progressLayout = new VBox(10);
        progressLayout.setAlignment(Pos.CENTER);
        progressLayout.getChildren().addAll(progressBar, progressIndicator, buttonBox);

        // Creeaza un arbore de elemente de control si le adauga intr-un VBox
        TreeItem rootItem = new TreeItem("Controls");
        // Adauga elemente pentru diferite pagini in arbore
        TreeItem fpItem = new TreeItem("First Page");
        fpItem.getChildren().addAll(new TreeItem("Labels"), new TreeItem("ImageView"), new TreeItem("Toggle Button"), new TreeItem("Hyperlink"));
        rootItem.getChildren().add(fpItem);
        TreeItem mainItem = new TreeItem("Main Page");
        mainItem.getChildren().addAll(new TreeItem("Menu"), new TreeItem("ImageView"), new TreeItem("Labels"), new TreeItem("Checkbox"),
                new TreeItem("ChoiceBox"), new TreeItem("ScrollPane"), new TreeItem("ScrollBar"), new TreeItem("FileChooser"),
                new TreeItem("ListView"), new TreeItem("Button"), new TreeItem("Tooltip"), new TreeItem("Separator"));
        rootItem.getChildren().add(mainItem);
        TreeItem histItem = new TreeItem("Histogram Page");
        histItem.getChildren().addAll(new TreeItem("BarChart"), new TreeItem("ImageView"), new TreeItem("Labels"));
        rootItem.getChildren().add(histItem);
        TreeItem tableItem = new TreeItem("Table Page");
        tableItem.getChildren().addAll(new TreeItem("Label"), new TreeItem("TableView"));
        rootItem.getChildren().add(tableItem);
        TreeView treeView = new TreeView();
        treeView.setRoot(rootItem);
        treeView.setShowRoot(true);
        VBox treeVBox = new VBox(treeView);

        // Creeaza layout-ul principal si il adauga intr-o scena
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
        extraStage.setScene(scene); // Seteaza scena in fereastra pentru controale suplimentare
        extraStage.show(); // Afiseaza fereastra pentru controale suplimentare
    }

    
    //metode pentru resetarea, pornirea si oprirea timerului
    private void resetTimer(ProgressBar progressBar, ProgressIndicator progressIndicator) {
        if (timeline != null) {
            timeline.stop(); //intrerupere timeline
        }
        progressBar.setProgress(0);
        progressIndicator.setProgress(0);
        timeSeconds = 0; //resetare counter
        timeline = null; // resetare timeline
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
