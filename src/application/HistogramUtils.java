package application;

import javafx.scene.chart.XYChart;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

public class HistogramUtils {

	public static XYChart.Series<String, Number> calculateHistogram(Image image) {
	    // Initializeaza un vector pentru histograma cu 11 elemente
	    int[] histogram = new int[11];
	    // Verifica daca imaginea nu este nula
	    if (image != null) {
	        // Obtine un cititor de pixeli pentru imagine
	        PixelReader pixelReader = image.getPixelReader();

	        // Parcurge fiecare pixel din imagine
	        for (int y = 0; y < image.getHeight(); y++) {
	            for (int x = 0; x < image.getWidth(); x++) {
	                // Obtine culoarea pixelului la coordonatele (x, y)
	                Color color = pixelReader.getColor(x, y);
	                // Calculeaza nivelul de gri al pixelului utilizand formula NTSC
	                int grayLevel = (int) ((color.getRed() * 255 * 0.3) + (color.getGreen() * 255 * 0.59) + (color.getBlue() * 255 * 0.11));
	                // Determina in ce interval se afla nivelul de gri si actualizeaza histograma
	                int bucket = grayLevel / 25;
	                if (bucket > 10) bucket = 10;
	                histogram[bucket]++;
	            }
	        }
	    }
	    // Initializeaza o serie pentru graficul de tip XYChart
	    XYChart.Series<String, Number> series = new XYChart.Series<>();
	    // Adauga datele din histograma in seria graficului
	    for (int i = 0; i < histogram.length; i++) {
	        if (histogram[i] > 0) {
	            series.getData().add(new XYChart.Data<>(String.valueOf(i), histogram[i]));
	        }
	    }
	    return series;
	}

}
