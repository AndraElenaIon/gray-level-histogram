package application;

import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

public class HistogramUtils {

	public static XYChart.Series<String, Number> calculateHistogram(Image image) {
        int[] histogram = new int[11];
        if (image != null) {
            PixelReader pixelReader = image.getPixelReader();

            for (int y = 0; y < image.getHeight(); y++) {
                for (int x = 0; x < image.getWidth(); x++) {
                    Color color = pixelReader.getColor(x, y);
                    int grayLevel = (int) ((color.getRed() * 255 * 0.3) + (color.getGreen() * 255 * 0.59) + (color.getBlue() * 255 * 0.11));
                    int bucket = grayLevel / 25;
                    if (bucket > 10) bucket = 10;
                    histogram[bucket]++;
                }
            }
        }
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 0; i < histogram.length; i++) {
            if (histogram[i] > 0) {
                series.getData().add(new XYChart.Data<>(String.valueOf(i), histogram[i]));
            }
        }
        return series;
    }
}
