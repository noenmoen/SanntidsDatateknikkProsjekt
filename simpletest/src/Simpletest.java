
import java.io.File;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

class Simpletest {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) throws Exception {

        long a = System.currentTimeMillis();
        String fileName = "" + System.getProperty("user.dir") + "/A4.jpg";
        Mat newImage = Imgcodecs.imread(fileName);
        if (newImage.dataAddr() == 0) {
            throw new Exception("Couldn't open file " + fileName);
        } else {
            ImageViewer imageViewer = new ImageViewer();
            imageViewer.show(newImage, "Loaded image");
        }
        long b = System.currentTimeMillis();
        System.out.println(b-a);
    }

}
