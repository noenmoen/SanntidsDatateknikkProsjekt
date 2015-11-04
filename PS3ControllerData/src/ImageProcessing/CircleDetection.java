/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageProcessing;

import de.yadrone.base.IARDrone;
import de.yadrone.base.video.ImageListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.Vector;
import org.apache.commons.io.FileUtils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import yadrone.DataHandler;

/**
 *
 * @author Morten
 */
public class CircleDetection extends Thread implements ImageListener
{

    private int highThreshold;
    private double doublehighThreshold;
    private int lowThreshold = 70;
    private int aperture = 3;
    private Size gaussKernel = new Size();
    private double sigmaX;
    private Scalar color = new Scalar(0, 0, 255);
    private int lineWidth = 4;
    private int circle_max = 500;
    private int circle_min = 20;
    private int cannyThresh_upper;
    private int cannyThresh_inner;
    private int denom;
    private double hl;
    private double hu;
    private double sl;
    private double su;
    private double vl;
    private double vu;
    private ImageViewer iv = new ImageViewer();
    private ImageConverter ic = new ImageConverter();
    private BufferedImage bufferedImage;
    private final ProcessedImagePanel pip;
    private DataHandler dh;

    /**
     *
     * @param drone
     * @param mat
     * @param cannyThresh_upper
     * @param cannyThresh_inner
     * @param denom
     * @param kernel
     * @param highthreshold
     * @param lowthreshold
     * @param bufferSize
     * @param sigmaX
     */
    public CircleDetection(
            int cannyThresh_upper,
            int cannyThresh_inner,
            int denom,
            int kernel,
            int highthreshold,
            int lowthreshold,
            double sigmaX,
            IARDrone drone,
            int bufferSize,
            ProcessedImagePanel pip,
            DataHandler dh)
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        this.cannyThresh_upper = cannyThresh_upper;
        this.cannyThresh_inner = cannyThresh_inner;
        this.denom = denom;
        this.gaussKernel = new Size(kernel, kernel);
        this.highThreshold = highthreshold;
        this.doublehighThreshold = (double) highthreshold;
        this.lowThreshold = lowthreshold;
        this.sigmaX = sigmaX;
        drone.getVideoManager().addImageListener(this);
        this.pip = pip;
        this.dh = dh;
        loadParameters();

    }

    /**
     * Anything between the min and max, is registered as 1, or 255, everyting
     * else is registered as 0.
     *
     * @param mat
     * @param minThresh
     * @param maxThresh
     * @return
     */
    private Mat MinMaxThreshold(Mat mat, double minThresh, double maxThresh)
    {

        Mat newMat = new Mat(mat.size(), mat.type());

        double[] data0 = new double[3];
        data0[0] = 0;
        data0[1] = 0;
        data0[2] = 0;
        double[] data1 = new double[3];
        data1[0] = 255;
        data1[1] = 255;
        data1[2] = 255;
        int c;
        int r;

        for (c = 0; c < mat.width(); c++) {
            for (r = 0; r < mat.height(); r++) {
                double x = mat.get(r, c)[0];
                if (x > minThresh && x < maxThresh) {
                    newMat.put(r, c, data1);

                }
                else {
                    newMat.put(r, c, data0);
                }
            }
        }

        return newMat;
    }

    /**
     * src_gray: Input image (grayscale) circles: A vector that stores sets of 3
     * values: x_{c}, y_{c}, r for each detected circle. CV_HOUGH_GRADIENT:
     * Define the detection method. Currently this is the only one available in
     * OpenCV dp = 1: The inverse ratio of resolution min_dist =
     * src_gray.rows/8: Minimum distance between detected centers param_1 = 200:
     * Upper threshold for the internal Canny edge detector param_2 = 100*:
     * Threshold for center detection. min_radius = 0: Minimum radio to be
     * detected. If unknown, put zero as default. max_radius = 0: Maximum radius
     * to be detected. If unknown, put zero as default
     *
     * @param image
     * @return Mat: the points where circles can be found
     */
    private Mat CircleFinder(Mat image)
    {
        Mat circles = new Mat();
        Imgproc.HoughCircles(image, circles, Imgproc.CV_HOUGH_GRADIENT, 1, image.height() / 10, 500, 50, 0, 500);
        return circles;
    }

    /**
     *
     * detects circles, returned as MAT type, every colon in the Mat has a
     * double[] with {x,y,R} (R = Radius)
     *
     * @param image
     * @param dp dp = 1: The inverse ratio of resolution
     * @param denominator((image.height()+image.width())/2)/denominator: minimum
     * distance between circles
     * @param cannyThresh param_1 = 200: Upper threshold for the internal Canny
     * edge detector
     * @param centerThresh param_2 = 100*: Threshold for center detection.
     * @param minRatio min_radius = 0: Minimum radio to be detected.
     * @param maxRatio max_radius = 0: Maximum radius to be detected.
     * @return Mat Circles in 3-layered vector
     */
    private Mat CircleFinder(Mat image, int denominator, int cannyThresh, int centerThresh, int minRatio, int maxRatio)
    {

        Mat circles = new Mat();
        Imgproc.HoughCircles(image, circles, Imgproc.CV_HOUGH_GRADIENT, 1,
                ((image.height() + image.width()) / 2) / denominator,
                cannyThresh, centerThresh, minRatio, maxRatio);

        return circles;
    }

    /**
     *
     * @param circles- Is given by Houghcircles
     * @param image- The image you draw circles on
     * @param color- type Scalar
     * @param lineWidth- 1,2,3,4....
     * @return Mat- the image drawn
     */
    private Mat DrawCircles(Mat circles, Mat image, Scalar color, int lineWidth)
    {
        Scalar colorFilt = new Scalar(0, 255, 0);

//        System.out.println("---------------------------------------------------");
//        System.out.println("Number of circles found: " + circles.cols());
        if (circles.cols() > 0) {
            for (int i = 0; i < circles.cols(); i++) {
                double[] circle = circles.get(0, i);
                Point p = new Point(circle[0], circle[1]);
                Imgproc.circle(image, p, (int) circle[2], color, lineWidth);

//                System.out.println((i + 1) + " Coordinates: "
//                        + circle[0] + "x" + circle[1] + " Radius: " + circle[2]);
            }
        }
        else {
            System.out.println("could not find any circles!");
        }
        try {
            Point pFilt = new Point(dh.getAvg()[0], dh.getAvg()[1]);
            Imgproc.circle(image, pFilt, (int) dh.getAvg()[2], colorFilt, lineWidth);
        }
        catch (Exception e) {
            System.out.println("Error printing filtered circle");
        }

        return image;
    }

    @Override
    public void run()
    {
        Mat oldImage = null;
        Mat image = null;
        while (true) {
            long start = System.currentTimeMillis();
            while (oldImage == image) {
                try {
                    image = ic.BufferedImageToMat(bufferedImage);
                }
                catch (Exception e) {
                }
            }
            oldImage = image;
            Imgproc.GaussianBlur(image, image, getGaussKernel(), getSigmaX());
            Mat originalImage = image.clone();
            Vector<Mat> HSV = new Vector<>();

            Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2HSV_FULL);

            Core.inRange(image, new Scalar(getHl() * 255, getSl() * 255, getVl() * 255),
                    new Scalar(getHu() * 255, getSu() * 255, getVu() * 255), image);

            Core.split(originalImage, HSV);
            Mat h = HSV.get(0);
            Mat s = HSV.get(1);
            Mat v = HSV.get(2);

//            Mat hg = new Mat();
//            Mat sg = new Mat();
//            Mat vg = new Mat();
//            Imgproc.GaussianBlur(h, hg, gaussKernel, sigmaX);
//            Imgproc.GaussianBlur(s, sg, gaussKernel, sigmaX);
//            Imgproc.GaussianBlur(v, vg, gaussKernel, sigmaX);
//            Mat ht2 = MinMaxThreshold(hg, 0.149 * 255, 0.567 * 255);
//            Mat st2 = MinMaxThreshold(sg, 0.071 * 255, 255);
//            Mat vt2 = MinMaxThreshold(vg, 0.000 * 255, 0.691 * 255);
//            iv.show(ht2, "Thresholding: HT2");
//            iv.show(st2, "Thresholding: ST2");
//            iv.show(vt2, "Thresholding: VT2");
//            Mat ad1 = new Mat();
//            System.out.println(image.channels());
//            System.out.println(originalImage.channels());
//            Mat ad1 = new Mat();
//            Mat ad2 = new Mat();
//            Mat ad3 = new Mat();
            Mat out = new Mat();
//            Core.multiply(h,s,ad1);
//            Core.multiply(ad1, v, ad2);
//            Core.multiply(ad2, image, ad3);

//            for(int i=0;i<1;i++){
//            Imgproc.erode(ad2, ad2, );
//            Imgproc.dilate(ad2, ad2, image);
//            }
//            iv.show(ad2, "Thresholding: ad2");
//            iv.show(ad3, "Thresholding: ad3");
            Mat circles = CircleFinder(image, getDenom(), getCannyThresh_upper(), getCannyThresh_inner(), getCircle_min(), getCircle_max());

            Vector<Mat> channels = new Vector<>();
            Core.split(originalImage, channels);
            Core.multiply(channels.get(0), image, channels.get(0));
            Core.multiply(channels.get(1), image, channels.get(1));
            Core.multiply(channels.get(2), image, channels.get(2));
            Core.merge(channels, originalImage);
//            try {
//                System.out.println(circles.cols() + " " + circles.rows());
//                reg.AddNewCoordinate(circles);
//
//                out = DrawCircles(reg.getLatestCoordinate(), originalImage, color, lineWidth);
//            }
//            catch (Exception e) {
//                System.out.println(e.getMessage());
//                out = DrawCircles(circles, originalImage, color, lineWidth);
//            }
            out = DrawCircles(circles, originalImage, getColor(), lineWidth);
//            iv.show(out, "Resulting Image");
            pip.setBufferedImage((BufferedImage) ic.toBufferedImage(out));
            dh.setImageWidthAndHight(image);
            dh.addCentroidAndRadius(circles);
            System.out.println("Cycletime: " + (System.currentTimeMillis() - start));
        }
    }

    @Override
    public synchronized void imageUpdated(BufferedImage bi)
    {
        bufferedImage = bi;
    }

    /**
     * @return the gaussKernel
     */
    public synchronized Size getGaussKernel()
    {
        return gaussKernel;
    }

    /**
     * @param gaussKernel the gaussKernel to set
     */
    public synchronized void setGaussKernel(Size gaussKernel)
    {
        this.gaussKernel = gaussKernel;
    }

    /**
     * @return the sigmaX
     */
    public synchronized double getSigmaX()
    {
        return sigmaX;
    }

    /**
     * @param sigmaX the sigmaX to set
     */
    public synchronized void setSigmaX(double sigmaX)
    {
        this.sigmaX = sigmaX;
    }

    /**
     * @return the color
     */
    public synchronized Scalar getColor()
    {
        return color;
    }

    /**
     * @param color the color to set
     */
    public synchronized void setColor(Scalar color)
    {
        this.color = color;
    }

    /**
     * @return the circle_max
     */
    public synchronized int getCircle_max()
    {
        return circle_max;
    }

    /**
     * @param circle_max the circle_max to set
     */
    public synchronized void setCircle_max(int circle_max)
    {
        this.circle_max = circle_max;
    }

    /**
     * @return the circle_min
     */
    public synchronized int getCircle_min()
    {
        return circle_min;
    }

    /**
     * @param circle_min the circle_min to set
     */
    public synchronized void setCircle_min(int circle_min)
    {
        this.circle_min = circle_min;
    }

    /**
     * @return the cannyThresh_upper
     */
    public synchronized int getCannyThresh_upper()
    {
        return cannyThresh_upper;
    }

    /**
     * @param cannyThresh_upper the cannyThresh_upper to set
     */
    public synchronized void setCannyThresh_upper(int cannyThresh_upper)
    {
        this.cannyThresh_upper = cannyThresh_upper;
    }

    /**
     * @return the cannyThresh_inner
     */
    public synchronized int getCannyThresh_inner()
    {
        return cannyThresh_inner;
    }

    /**
     * @param cannyThresh_inner the cannyThresh_inner to set
     */
    public synchronized void setCannyThresh_inner(int cannyThresh_inner)
    {
        this.cannyThresh_inner = cannyThresh_inner;
    }

    /**
     * @return the denom
     */
    public synchronized int getDenom()
    {
        return denom;
    }

    /**
     * @param denom the denom to set
     */
    public synchronized void setDenom(int denom)
    {
        this.denom = denom;
    }

    /**
     * @return the hl
     */
    public synchronized double getHl()
    {
        return hl;
    }

    /**
     * @param hl the hl to set
     */
    public synchronized void setHl(double hl)
    {
        if (isThresholdValueValid(hl)) {
            this.hl = hl;
        }
    }

    /**
     * @return the hu
     */
    public synchronized double getHu()
    {
        return hu;
    }

    /**
     * @param hu the hu to set
     */
    public synchronized void setHu(double hu)
    {
        if (isThresholdValueValid(hu)) {
            this.hu = hu;
        }
    }

    /**
     * @return the sl
     */
    public synchronized double getSl()
    {
        return sl;
    }

    /**
     * @param sl the sl to set
     */
    public synchronized void setSl(double sl)
    {
        if (isThresholdValueValid(sl)) {
            this.sl = sl;
        }
    }

    /**
     * @return the su
     */
    public synchronized double getSu()
    {
        return su;
    }

    /**
     * @param su the su to set
     */
    public synchronized void setSu(double su)
    {
        if (isThresholdValueValid(su)) {
            this.su = su;
        }
    }

    /**
     * @return the vl
     */
    public synchronized double getVl()
    {
        return vl;
    }

    /**
     * @param vl the vl to set
     */
    public synchronized void setVl(double vl)
    {
        if (isThresholdValueValid(vl)) {
            this.vl = vl;
        }
    }

    /**
     * @return the vu
     */
    public synchronized double getVu()
    {
        return vu;
    }

    /**
     * @param vu the vu to set
     */
    public synchronized void setVu(double vu)
    {
        if (isThresholdValueValid(vu)) {
            this.vu = vu;
        }
    }

    private boolean isThresholdValueValid(double value)
    {
        return (value >= 0.0) && (value <= 1.0);
    }

    private void loadParameters()
    {
        String s;
        try {
            s = FileUtils.readFileToString(
                    new File(System.getProperty("user.dir")
                            + "\\imProParameters.txt"));
            String[] paramStrs = s.split(" ");
            
        double[] params = new double[Array.getLength(paramStrs)];
        for (int i = 0; i < Array.getLength(paramStrs); i++) {
            params[i] = Double.valueOf(paramStrs[i]);
        }
        hl = params[0];
        hu = params[1];
        sl = params[2];
        su = params[3];
        vl = params[4];
        vu = params[5];
        sigmaX = params[6];
        }
        catch (IOException ex) {
            System.out.println("Parameter Loading Failed: " + ex);
        }
        
    }
}
