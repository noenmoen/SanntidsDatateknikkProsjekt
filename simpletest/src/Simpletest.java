
import java.io.File;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

class SimpleSample {

    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }

    public static void main(String[] args) throws Exception {

        String fileName = "" + System.getProperty("user.dir") + "/A4.jpg";
        File f = new File(fileName);
        System.out.println(f.isFile());
        Mat newImage = Imgcodecs.imread(fileName);
        if (newImage.dataAddr() == 0) {
            throw new Exception("Couldn't open file " + fileName);
        } else {
            ImageViewer imageViewer = new ImageViewer();
            imageViewer.show(newImage, "Loaded image");
        }
    }

}
