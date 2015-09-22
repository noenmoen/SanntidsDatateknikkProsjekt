
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

class Simpletest
{
    
    static {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
    }
    
    public static void main(String[] args) throws Exception
    {
        int i = 0;
        long sum = 0;
        while (i < 2) {
            long a = System.currentTimeMillis();
            String fileName = "" + System.getProperty("user.dir") + "/A4.jpg";
            Mat inImage = Imgcodecs.imread(fileName);
            
            System.out.println(CvType.typeToString(inImage.type()));
             // Printer bildetypen
            Imgproc.cvtColor(inImage, inImage, Imgproc.COLOR_RGB2GRAY);
            // Konverterer bildet fra RGB til grå.
            System.out.println(CvType.typeToString(inImage.type()));           
            Imgproc.GaussianBlur(inImage, inImage, new Size(5, 5), 1);
            // 5x5 Gaussian filter med sigma=1.
            System.out.println("Threshold = "
                    + Imgproc.threshold(inImage, inImage, 100, 255, Imgproc.THRESH_OTSU));
            // Terskler bildet. Parameterene 0 og 255 tells ikke når OTSU eller 
            // andre automatiske tersklinger er valgt.
           Imgproc.Sobel(inImage, inImage, -1, 1, 1);
           // Kantdetektering med sobel, parm3=type ut, parm4/5=x/y derivat
           Mat kernel =new Mat(3, 3, CvType.CV_8UC1); 
           kernel.setTo(new Scalar(1));
           if(i==0){
           Imgproc.dilate(inImage, inImage, kernel);
           Imgproc.erode(inImage, inImage, kernel);}
            
            if (inImage.dataAddr() == 0) {
                throw new Exception("Couldn't open file " + fileName);
            }
            else {
                ImageViewer imageViewer = new ImageViewer();
                imageViewer.show(inImage, "Loaded image");
            }
            long b = System.currentTimeMillis();
            sum += b - a;
            System.out.println(b - a);
            i++;
        }
        System.out.println("Gjennomsnitt: " + sum / i);
    }
    
}
