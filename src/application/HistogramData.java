package application;

public class HistogramData {
    private String bucketRange;
    private int pixelCount;

    public HistogramData(String bucketRange, int pixelCount) {
        this.bucketRange = bucketRange;
        this.pixelCount = pixelCount;
    }

    
	public String getBucketRange() {
        return bucketRange;
    }

    public int getPixelCount() {
        return pixelCount;
    }


	public void setPixelCount(int rangeSum) {
		this.pixelCount = rangeSum;
	}
}
