package hberk.pcbanalyser;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import org.openjdk.jmh.annotations.Benchmark;

import java.io.File;
import java.net.URL;
import java.util.*;

public class Controller implements Initializable {
    public Label componentsLabel, fileNameLabel, fileSizeLabel;
    public RadioButton originalRadioButton, grayscaleRadioButton;
    public Button redButton, greenButton, blueButton;
    public ImageView processedImageView;
    public RadioButton originalProcessRadioButton, bwRadioButton, singleCompRadioButton, allCompRadioButton, colourDSRadioButton;
    public ComboBox<String> colourComboBox;
    public CheckBox identifyComponentsCheckbox, randomColourCheckBox;
    public Slider minRectSizeSlider;
    private Image originalImage;
    private WritableImage bwImage;
    private WritableImage compColourImage;
    private int noOfComponents;
    int[] disjointSet, singleCompDisjointSet, allCompDisjointSet;
    int width, height;
    int minRectSize = 40;
    HashSet<Integer> allRoots;
    LinkedList<Integer> usedRootSizes;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        originalRadioButton.setSelected(true);
        singleCompRadioButton.setSelected(true);
        colourComboBox.getItems().addAll("Red", "Green", "Blue");
    }


    public enum ColorChannel {RED, GREEN, BLUE, GRAYSCALE}

    @FXML
    public ImageView imageView;


    /**
     * File Chooser to choose files.
     */
    public void imageChooser() {
        FileChooser fileChooser = new FileChooser();
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile != null) {
            originalImage = new Image(selectedFile.toURI().toString());
            if (originalImage.getHeight() > 497) {
                imageView.setFitHeight(497);
                processedImageView.setFitHeight(497);
            } else {
                imageView.setFitHeight(originalImage.getHeight());
                processedImageView.setFitHeight(originalImage.getHeight());
            }
            if (originalImage.getWidth() > 579) {
                imageView.setFitWidth(579);
                processedImageView.setFitWidth(579);
            } else {
                imageView.setFitWidth(originalImage.getWidth());
                processedImageView.setFitWidth(originalImage.getWidth());
            }
            imageView.setPreserveRatio(true);
            processedImageView.setPreserveRatio(true);
            imageView.setImage(originalImage);
            processedImageView.setImage(originalImage);
            fileNameLabel.setText("File name: " + selectedFile.getName());
            fileSizeLabel.setText("File size: " + selectedFile.length() + " bytes");
        }
    }

    /**
     * Instructions popup
     */
    public void help() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Open a file to get started.", ButtonType.OK);
        alert.setTitle("App instructions");
        alert.setHeaderText("App instructions");
        alert.showAndWait();
    }

    /**
     * Exit button
     */
    public void exitApp() {
        Platform.exit();
        System.exit(0);
    }


    /**
     * Processes the image
     */
    public void processImage(MouseEvent mouseEvent) {
        if (originalImage != null) {
            width = (int) imageView.getFitWidth();
            height = (int) imageView.getFitHeight();
            bwDisjointSets(mouseEvent);
            groupSets();
            setRoots();
            setProcessedOriginalRadioButton();
        }
    }

    /**
     * * Sets the values for disjoint sets, and checks to identify single components or all components
     */
    @Benchmark
    public void bwDisjointSets(MouseEvent mouseEvent) {
        PixelReader pr = originalImage.getPixelReader();
        double xPos = mouseEvent.getX();
        double yPos = mouseEvent.getY();
        int xPosition = (int) xPos;
        int yPosition = (int) yPos;
        Color col = pr.getColor(xPosition, yPosition);
        double hue = col.getHue();
        double sat = col.getSaturation();
        double bri = col.getBrightness();

        double minHue = hue * 0.8; //20% range
        double maxHue = hue * 1.2; //20% range

        double minSat = sat * 0.5; //50% range
        double maxSat = sat * 1.5; //50% range

        double minBri = bri * 0.7; //30% range
        double maxBri = bri * 1.3; //30% range
        bwImage = new WritableImage(width, height);
        compColourImage = new WritableImage(width, height);
        PixelWriter writer = bwImage.getPixelWriter();
        PixelWriter writer2 = compColourImage.getPixelWriter();
        disjointSet = new int[width * height];
        if(singleCompRadioButton.isSelected()){
            singleCompDisjointSet = new int[width * height];
        }
        if(allCompRadioButton.isSelected()){
            allCompDisjointSet = new int[width * height];
        }
        int i = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                Color color = pr.getColor(x, y);
                double colHue = color.getHue();
                double colSat = color.getSaturation();
                double colBri = color.getBrightness();
                if(singleCompRadioButton.isSelected()){
                    if ((colHue < minHue || colHue > maxHue) || (colSat < minSat || colSat > maxSat) || (colBri < minBri || colBri > maxBri)) {
                        color = Color.WHITE;
                        disjointSet[i] = -1;

                    } else {
                        color = Color.BLACK;
                        disjointSet[i] = i;
                    }
                }
                if(allCompRadioButton.isSelected()){
                    if((colHue > 200 && colHue < 250) && (colSat > 0.15 && colSat < 0.30) && (colBri > 0.20 && colBri < 0.45)){ //Integrated Circuits
                        writer2.setColor(x, y, color);
                        color = Color.BLACK;
                        disjointSet[i] = i;
                    } else{
                        if((colHue > 15 && colHue < 40) && (colSat > 0.30 && colSat < 0.70) && (colBri > 0.50 && colBri < 0.85)) { //Resistors
                            writer2.setColor(x, y, color);
                            color = Color.BLACK;
                            disjointSet[i] = i;
                        }
                        else{
                            color = Color.WHITE;
                            disjointSet[i] = -1;
                        }
                    }
                }
                writer.setColor(x, y, color);
                i++;
            }
        }
        processedImageView.setImage(originalImage);
        originalProcessRadioButton.setSelected(true);
    }

    /**
     * Groups disjoints sets with adjacent sets
     */
    @Benchmark
    public void groupSets() {
        for (int i = 0; i < disjointSet.length; i++) {
            if ((disjointSet[i] != -1) && (disjointSet[i + 1] != -1)) {
                union(disjointSet, i, i + 1);
            }

            if ((i < disjointSet.length - width) && (disjointSet[i] != -1) && (disjointSet[i + width] != -1)) {
                union(disjointSet, i, i + width);
            }
        }
    }


    /**
     * Sets area for rectangles and initiates tooltips
     */
    @Benchmark
    public void drawRectanglesTooltips(int minX, int minY, int maxX, int maxY, int root, int YPos, int XPos) {
        Rectangle rectangle = new Rectangle(minX, minY, maxX - minX, maxY - minY);
        rectangle.setFill(Color.TRANSPARENT);
        rectangle.setStroke(Color.RED);
        rectangle.setStrokeWidth(1.5);
        rectangle.setLayoutX(processedImageView.getLayoutX());
        rectangle.setLayoutY(processedImageView.getLayoutY());

        ((AnchorPane) processedImageView.getParent()).getChildren().add(rectangle);

        int setSize = pixelCounter(root);
        int pixelSize = ((maxX - minX) * (maxY - minY)); //rectangle
        int position = positionOfRoot(root);


        Tooltip tooltip = new Tooltip("Root: " + root + "\nSet Size: " + setSize + "\nRectangle Size: " + pixelSize + "\nComponent Name: " + componentName(YPos, XPos) + "\nPosition by size: " + position);
        Tooltip.install(rectangle, tooltip);
    }

    /**
     * Makes a hashset from all roots
     */
    @Benchmark
    public void setRoots() {
        allRoots = new HashSet<>();
        for (int i = 0; i < disjointSet.length; i++) if (disjointSet[i] != -1) allRoots.add(find(disjointSet, i));
    }

    /**
     *Counts the amount of pixels a root value belongs to
     */
    @Benchmark
    public int pixelCounter(int j) {
        int pixelCount = 0;
        if (allRoots != null) {
            for (int root : allRoots) {
                if (j == root) {
                    for (int i = 0; i < disjointSet.length; i++) {
                        if (disjointSet[i] != -1) { //If not white
                            int elementRoot = find(disjointSet, i); //find root of element
                            if (root == elementRoot) { // if element root matches root
                                pixelCount++;
                            }
                        }
                    }
                }
            }
        }
        return pixelCount;
    }

    /**
     * Sorts components from largest to smallest using Collections
     */
    @Benchmark
    public int positionOfRoot(int root){
        int position = 0;
        if(allRoots != null) {
            if (usedRootSizes != null && usedRootSizes.size() == noOfComponents) {
                usedRootSizes.sort(Collections.reverseOrder());
                position = usedRootSizes.indexOf(pixelCounter(root)) + 1;
            }
        }
        return position;
    }

    /**
     *Component names based on colours
     */
    @Benchmark
    public String componentName(int x, int y) {
        PixelReader reader = originalImage.getPixelReader();
        Color color = reader.getColor(x, y);
        if ((color.getHue() > 190 && color.getHue() < 260) && (color.getSaturation() > 0.40)) {
            return "Diode";
        }
        if (color.getHue() > 190 && color.getHue() < 250) {
            return "Integrated Circuit";
        }
        if (color.getHue() < 30 && color.getHue() > 15) {
            return "Resistor";
        } else {
            return "Unknown";
        }
    }

    /**
     * Identifies components and sets rectangle values for disjoint sets
     */
    @Benchmark
    public void identifyComps(int[] disjointSet) {
        noOfComponents = 0;
        usedRootSizes = new LinkedList<>();

        for (int root : allRoots) {
            int minX = width;
            int minY = height;
            int maxX = 0;
            int maxY = 0;
            int elementXPos = 0;
            int elementYPos = 0;

            for (int i = 0; i < disjointSet.length; i++) {
                if (disjointSet[i] != -1) { //If not white
                    int elementRoot = find(disjointSet, i); //find root of element
                    if (root == elementRoot) { // if element root matches root
                        elementXPos = i % width;
                        elementYPos = i / width; // How many times it passes the width vertically
                        minX = Math.min(elementXPos, minX); // Can also use minX = elementXPos < minX ? elementXPos : minX but this is quicker..
                        minY = Math.min(elementYPos, minY);
                        maxX = Math.max(elementXPos, maxX); //Can also use maxX = elementXPos > maxX ? elementXPos : maxX;
                        maxY = Math.max(elementYPos, maxY);
                    }
                }
            }
            int pixelSize = ((maxX - minX) * (maxY - minY)); //rectangle area
            if (pixelSize > minRectSize) { //minimum area  to disregard small sets OUTLIER MANAGEMENT
                usedRootSizes.add(pixelCounter(root));
                noOfComponents++;
                drawRectanglesTooltips(minY, minX, maxY, maxX, root, elementYPos, elementXPos); // Has to be flipped because I iterated x y first.
            }
        }
    }

    /**
     * Colours all components colours based on users selection
     */
    @Benchmark
    public void colourComponents() {
        if (disjointSet != null) {
            originalProcessRadioButton.setSelected(false);
            bwRadioButton.setSelected(false);
            randomColourCheckBox.setSelected(false);
            if (colourComboBox.getSelectionModel().getSelectedItem().equals("Red"))
                setComponentsColour(originalImage, ColorChannel.RED);
            if (colourComboBox.getSelectionModel().getSelectedItem().equals("Green"))
                setComponentsColour(originalImage, ColorChannel.GREEN);
            if (colourComboBox.getSelectionModel().getSelectedItem().equals("Blue"))
                setComponentsColour(originalImage, ColorChannel.BLUE);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Click a component first..", ButtonType.OK);
            alert.setTitle("Error");
            alert.setHeaderText("No component selected!");
            alert.showAndWait();
        }

    }

    /**
     * Colours all components randomly
     */
    @Benchmark
    public void colourComponentsRandomly() {
        if (disjointSet != null) {
            if (allRoots != null) {
                if (originalProcessRadioButton.isSelected()) {
                    setComponentsColourRandom(originalImage);
                    colourComboBox.getSelectionModel().clearSelection();
                }
                if (bwRadioButton.isSelected()) {
                    setComponentsColourRandom(bwImage);
                    colourComboBox.getSelectionModel().clearSelection();
                }
            } else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Identify components first..", ButtonType.OK);
                alert.setTitle("Error");
                alert.setHeaderText("Identify Components First");
                alert.showAndWait();
            }

        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Click a component first..", ButtonType.OK);
            alert.setTitle("Error");
            alert.setHeaderText("No component selected!");
            alert.showAndWait();
            randomColourCheckBox.setSelected(false);
        }
    }

    /**
     * Changes the imageview to colours of their disjoint sets
     */
    @Benchmark
    public void setColourDSRadioButton() {
        if (disjointSet != null) {
            if (allRoots != null) {
                if(!allCompRadioButton.isSelected()){
                    Alert alert = new Alert(Alert.AlertType.ERROR, "Select All Components Radio Button First", ButtonType.OK);
                    alert.setTitle("Error");
                    alert.setHeaderText("Identify Components First");
                    alert.showAndWait();
                    colourDSRadioButton.setSelected(false);
                } else{
                    processedImageView.setImage(compColourImage);
                    originalRadioButton.setSelected(false);
                    grayscaleRadioButton.setSelected(false);
                    singleCompRadioButton.setSelected(false);
                    randomColourCheckBox.setSelected(false);
                }
            }
        }
    }

    /**
     * Sets imageview to grayscale
     */

    public void setGrayscaleImageRadioButton() {
        setColour(imageView.getImage(), ColorChannel.GRAYSCALE);
        grayscaleRadioButton.setSelected(true);
        originalRadioButton.setSelected(false);
    }

    /**
     * Sets imageview to original on first tab
     */

    public void setOriginalRadioButton() {
        origImage();
        originalRadioButton.setSelected(true);
        grayscaleRadioButton.setSelected(false);
        identifyComponentsCheckbox.setSelected(false);
    }

    public void setProcessedOriginalRadioButton() {
        processedImageView.setImage(originalImage);
        originalProcessRadioButton.setSelected(true);
        bwRadioButton.setSelected(false);
        randomColourCheckBox.setSelected(false);
        colourDSRadioButton.setSelected(false);
    }

    public void setBwRadioButton() {
        if (bwImage != null) {
            originalProcessRadioButton.setSelected(false);
            processedImageView.setImage(bwImage);
            bwRadioButton.setSelected(true);
            randomColourCheckBox.setSelected(false);
            colourDSRadioButton.setSelected(false);
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Click a component first..", ButtonType.OK);
            alert.setTitle("Error");
            alert.setHeaderText("No component selected!");
            alert.showAndWait();
            bwRadioButton.setSelected(false);
        }
    }

    public void setSingleCompRadioButton() {
        singleCompRadioButton.setSelected(true);
        allCompRadioButton.setSelected(false);
    }

    public void setAllCompRadioButton() {
        allCompRadioButton.setSelected(true);
        singleCompRadioButton.setSelected(false);
    }

    public void setIdentifyComponentsCheckbox() {
        if (disjointSet != null) {
            if (identifyComponentsCheckbox.isSelected()) {
                identifyComps(disjointSet);
                componentsLabel.setText("Number of Components: " + noOfComponents);
            } else {
                ((AnchorPane) processedImageView.getParent()).getChildren().removeIf(r -> r instanceof Rectangle); //Lambda expression to remove all rectangles in the processed image view
                noOfComponents = 0;
                componentsLabel.setText("Number of Components: ");
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Click a component first..", ButtonType.OK);
            alert.setTitle("Error");
            alert.setHeaderText("No component selected!");
            alert.showAndWait();
            identifyComponentsCheckbox.setSelected(false);
        }
    }

    /**
     * Listener for noise reduction slider
     */
    @FXML
    public void onMinRectSizeSliderChanged() {
        minRectSize = (int) minRectSizeSlider.getValue();
    }

    public void setComponentsColour(Image image, ColorChannel cc) {
        PixelReader reader = image.getPixelReader();
        WritableImage newImage = new WritableImage(width, height);
        PixelWriter writer = newImage.getPixelWriter();
        if (allRoots != null) {
            for (int root : allRoots) {
                for (int i = 0; i < disjointSet.length; i++) {
                    if (disjointSet[i] != -1) { //If not white
                        int elementRoot = find(disjointSet, i); //find root of element
                        if (root == elementRoot) { // if element root matches root
                            int elementXPos = i / width;
                            int elementYPos = i % width; // How many times it passes the width vertically
                            if (cc == ColorChannel.RED) redReadWrite(reader, writer, elementXPos, elementYPos);
                            if (cc == ColorChannel.GREEN) greenReadWrite(reader, writer, elementXPos, elementYPos);
                            if (cc == ColorChannel.BLUE) blueReadWrite(reader, writer, elementXPos, elementYPos);
                        }
                    } else {
                        int elementXPos = i / width;
                        int elementYPos = i % width; // How many times it passes the width vertically
                        Color color = reader.getColor(elementXPos, elementYPos);
                        writer.setColor(elementXPos, elementYPos, color);
                    }
                }
            }
        }
        processedImageView.setImage(newImage);
    }

    /**
     *Sets the colours of disjoint sets randomly
     */
    public void setComponentsColourRandom(Image image) {
        Random random = new Random();
        PixelReader reader = image.getPixelReader();
        WritableImage newImage = new WritableImage(width, height);
        PixelWriter writer = newImage.getPixelWriter();
        if (allRoots != null) {
            for (int root : allRoots) {
                Color randomColor = Color.color(random.nextDouble(), random.nextDouble(), random.nextDouble());
                for (int i = 0; i < disjointSet.length; i++) {
                    if (disjointSet[i] != -1) { //If not white
                        int elementRoot = find(disjointSet, i); //find root of element
                        if (root == elementRoot) { // if element root matches root
                            int elementXPos = i / width;
                            int elementYPos = i % width; // How many times it passes the width vertically
                            writer.setColor(elementXPos, elementYPos, randomColor);
                        }
                    } else {
                        int elementXPos = i / width;
                        int elementYPos = i % width; // How many times it passes the width vertically
                        Color color = reader.getColor(elementXPos, elementYPos);
                        writer.setColor(elementXPos, elementYPos, color);
                    }
                }
            }
        }
        processedImageView.setImage(newImage);
    }


    /**
     * Resets imageview to original image
     */
    public void origImage() {
        imageView.setImage(originalImage);
    }

    /**
     * Code below from ImageViewer assignment.
     */

    public void setImageRed() {
        origImage();
        setColour(imageView.getImage(), ColorChannel.RED);
        originalRadioButton.setSelected(false);
        grayscaleRadioButton.setSelected(false);
    }

    public void setImageGreen() {
        origImage();
        setColour(imageView.getImage(), ColorChannel.GREEN);
        originalRadioButton.setSelected(false);
        grayscaleRadioButton.setSelected(false);
    }

    public void setImageBlue() {
        origImage();
        setColour(imageView.getImage(), ColorChannel.BLUE);
        originalRadioButton.setSelected(false);
        grayscaleRadioButton.setSelected(false);
    }


    @FXML
    private void setColour(Image src, ColorChannel cc) {
        PixelReader reader = src.getPixelReader();
        int width = (int) src.getWidth();
        int height = (int) src.getHeight();
        WritableImage newImage = new WritableImage(width, height);
        PixelWriter writer = newImage.getPixelWriter();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (cc == ColorChannel.RED) redReadWrite(reader, writer, x, y);
                if (cc == ColorChannel.GREEN) greenReadWrite(reader, writer, x, y);
                if (cc == ColorChannel.BLUE) blueReadWrite(reader, writer, x, y);
                if (cc == ColorChannel.GRAYSCALE) greyscaleReadWrite(reader, writer, x, y);
            }
        }
        imageView.setImage(newImage);
    }

    private void redReadWrite(PixelReader reader, PixelWriter writer, int x, int y) {
        Color c;
        c = new Color(
                reader.getColor(x, y).getRed(),
                0,
                0,
                reader.getColor(x, y).getOpacity());
        writer.setColor(x, y, c);
    }

    private void greenReadWrite(PixelReader reader, PixelWriter writer, int x, int y) {
        Color c;
        c = new Color(
                0,
                reader.getColor(x, y).getGreen(),
                0,
                reader.getColor(x, y).getOpacity());
        writer.setColor(x, y, c);
    }

    private void blueReadWrite(PixelReader reader, PixelWriter writer, int x, int y) {
        Color c;
        c = new Color(
                0,
                0,
                reader.getColor(x, y).getBlue(),
                reader.getColor(x, y).getOpacity());
        writer.setColor(x, y, c);
    }

    private void greyscaleReadWrite(PixelReader reader, PixelWriter writer, int x, int y) {
        Color c;
        c = new Color(
                (reader.getColor(x, y).getRed()
                        + reader.getColor(x, y).getGreen()
                        + reader.getColor(x, y).getBlue())
                        / 3,
                (reader.getColor(x, y).getRed()
                        + reader.getColor(x, y).getGreen()
                        + reader.getColor(x, y).getBlue())
                        / 3,
                (reader.getColor(x, y).getRed()
                        + reader.getColor(x, y).getGreen()
                        + reader.getColor(x, y).getBlue())
                        / 3,
                reader.getColor(x, y).getOpacity());
        writer.setColor(x, y, c);
    }

    /**
     * Quick union find methods from notes
     */
    public static int find(int[] a, int id) {
        while (a[id] != id) id = a[id];
        return id;
    }

    //Quick union of disjoint sets containing elements p and q (Version 2)
    public static void union(int[] a, int p, int q) {
        a[find(a, q)] = find(a, p); //The root of q is made reference the root of p
    }
}
