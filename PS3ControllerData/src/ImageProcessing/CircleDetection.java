/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageProcessing;

import de.yadrone.base.IARDrone;
import java.util.Vector;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author Morten
 */
public class CircleDetection extends Thread
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
    private ImageViewer iv = new ImageViewer();
    private ImageConverter ic = new ImageConverter();
    private ImageBuffer ib;

    /**
     * clean constructor
     */
    /**
     * test Constructor.........
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
            int bufferSize)
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
        ib = new ImageBuffer(drone, bufferSize);
    }

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
     *
     *
     * @param circles: Is given by Houghcircles
     * @param image: The image you draw cirles on
     * @return
     */
    private Mat DrawCircles(Mat circles, Mat image)
    {
        System.out.println("---------------------------------------------------");
        System.out.println("Number of circles found: " + circles.cols());
        if (circles.cols() > 0) {
            for (int i = 0; i < circles.cols(); i++) {
                double[] circle = circles.get(0, i);
                Point p = new Point(circle[0], circle[1]);
                Imgproc.circle(image, p, (int) circle[2], new Scalar(0, 0, 255), 2);
            }
        }
        else {
            System.out.println("could not find any circles!!");
        }
        return image;
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
        System.out.println("---------------------------------------------------");
        System.out.println("Number of circles found: " + circles.cols());
        if (circles.cols() > 0) {
            for (int i = 0; i < circles.cols(); i++) {
                double[] circle = circles.get(0, i);
                Point p = new Point(circle[0], circle[1]);
                Imgproc.circle(image, p, (int) circle[2], color, lineWidth);
                System.out.println((i + 1) + " Radius: " + circle[2]);
            }
        }
        else {
            System.out.println("could not find any circles!!");
        }
        return image;
    }

    /**
     * Non adaptive threshold method
     *
     * @param mat
     * @param thresholdvalue
     * @return
     */
    private Mat Threshold(Mat mat, int thresholdvalue)
    {
        Mat thresh = new Mat();
        Imgproc.threshold(mat, thresh, thresholdvalue, 255, Imgproc.THRESH_BINARY);
        return thresh;

    }

    /**
     * Adaptive Threshold
     *
     * @param mat
     * @return
     */
    private Mat adaptThresholdGaussian(Mat mat)
    {
        Mat thresh = new Mat();
        Imgproc.adaptiveThreshold(mat, thresh, 255,
                Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 35, -1);
        return thresh;

    }

    @Override
    public void run()
    {

        while (true) {
            long start = System.currentTimeMillis();
            Mat image = null;
            while (image == null) {
                try {
                    image = ic.BufferedImageToMat(ib.getBufferedImage());
                }
                catch (Exception e) {
                }

            }
            Mat originalImage = image;
            Mat canny = new Mat();
            Mat gaussFiltered = new Mat();
            Vector<Mat> HSV = new Vector<>();

            Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2HSV_FULL);
            Core.split(originalImage, HSV);
            Mat h = HSV.get(0);
            Mat s = HSV.get(1);
            Mat v = HSV.get(2);

            Mat hg = new Mat();
            Mat sg = new Mat();
            Mat vg = new Mat();
            Imgproc.GaussianBlur(h, hg, gaussKernel, sigmaX);
            Imgproc.GaussianBlur(s, sg, gaussKernel, sigmaX);
            Imgproc.GaussianBlur(v, vg, gaussKernel, sigmaX);

//            //Thresholdnign:
//            Mat ht = new Mat();
//            Mat st = new Mat();
//            Mat vt = new Mat();
//            Imgproc.adaptiveThreshold(h, ht, highThreshold, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 99, -1);
//            Imgproc.adaptiveThreshold(h, st, highThreshold, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 99, -1);
//            Imgproc.adaptiveThreshold(h, vt, highThreshold, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY, 99, -1);
////        iv.show(ht, "Thresholding: HT");
////        iv.show(st, "Thresholding: ST");
////        iv.show(vt, "Thresholding: VT");
//
//            Mat ht1 = new Mat();
//            Mat st1 = new Mat();
//            Mat vt1 = new Mat();
//            Imgproc.threshold(h, ht1, doublehighThreshold, 255, Imgproc.THRESH_BINARY);
//            Imgproc.threshold(h, st1, doublehighThreshold, 255, Imgproc.THRESH_BINARY);
//            Imgproc.threshold(h, vt1, doublehighThreshold, 255, Imgproc.THRESH_BINARY);
////        iv.show(ht1, "Thresholding: HT1");
////        iv.show(st1, "Thresholding: ST1");
////        iv.show(vt1, "Thresholding: VT1");
            Mat ht2 = MinMaxThreshold(hg, 0.575 * 255, 0.664 * 255);
            Mat st2 = MinMaxThreshold(sg, 0.308 * 255, 255);
            Mat vt2 = MinMaxThreshold(vg, 0.29, 0.607 * 255);
//            iv.show(ht2, "Thresholding: HT2");
//            iv.show(st2, "Thresholding: ST2");
//            iv.show(vt2, "Thresholding: VT2");

            Mat ad1 = new Mat();
            Mat ad2 = new Mat();
            Core.add(ht2, st2, ad1);
            Core.add(ad1, vt2, ad2);
//        iv.show(ad2," Added after threshold");
            Mat circles = CircleFinder(vt2, denom, cannyThresh_upper,
                    cannyThresh_inner, circle_min, circle_max);
            Mat image1 = DrawCircles(circles, image, color, lineWidth);
            iv.show(image1, "Resulting Image");
//        Imgproc.Sobel(BW, gaussFiltered, -1, 1, 1);
//        Imgproc.Sobel(gaussFiltered, gaussFiltered, -1, 1, 1);
//        Core.add(BW, gaussFiltered, added);
//        v.show(added,"added");

//        Imgproc.Canny(thresh, canny, 10, 100, aperture, false);
//        iv.show(canny, "Canny");
//        Mat circles = new Mat();
//        circles = CircleFinder(canny, denom, cannyThresh_upper,
//                cannyThresh_inner, circle_min, circle_max);
////        circles = CircleFinder(canny);
//        Mat image = DrawCircles(circles, originalImage, color, lineWidth);
//        //Vis bildet
//
//        iv.show(image, "FUDGE");
            System.out.println("Cycletime: " + (System.currentTimeMillis() - start));
        }
    }

}
