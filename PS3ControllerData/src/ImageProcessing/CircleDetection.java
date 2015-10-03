/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ImageProcessing;

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
public class CircleDetection {

    private int lowThreshold = 1;
    private int aperture = 3;
    private Size gaussKernel = new Size(25, 25);
    private double sigmaX = 2;
    private Scalar color = new Scalar(0,0,255);
    private int lineWidth = 4;
    private int circle_max = 500;
    private int circle_min = 100;
        

    /**
     *
     * @param mat
     */
    public CircleDetection(Mat mat, int cannyThresh_upper, int cannyThresh_inner,int denom) {
        ImageViewer v = new ImageViewer();
        Mat originalImage = mat;
        Mat canny = new Mat();
        Mat gaussFiltered = new Mat();
        Mat BW = new Mat();
        Mat added = new Mat();
        Vector<Mat> RGB = new Vector<>();
//        Imgproc.cvtColor(originalImage, originalImage, Imgproc.COLOR_RGB2BGR);
        Core.split(originalImage, RGB);
//        v.show(originalImage,"Originalbilde");
        v.show(RGB.get(0),"0");
        v.show(RGB.get(1),"1");
        v.show(RGB.get(2),"2");
        Mat g = RGB.get(0).clone();
        System.out.println(g.height()+" " + g.width());
//        int depth = originalImage.get;
        //convert to gray
//        Imgproc.cvtColor(originalImage, BW, Imgproc.COLOR_RGB2GRAY);

//        v.show(BW,"BW");
        
        Imgproc.GaussianBlur(g, gaussFiltered, gaussKernel, sigmaX);
        v.show(gaussFiltered,"gaussFiltered");
        

//        Imgproc.Sobel(BW, gaussFiltered, -1, 1, 1);
//        Imgproc.Sobel(gaussFiltered, gaussFiltered, -1, 1, 1);
     
//        Core.add(BW, gaussFiltered, added);
//        v.show(added,"added");
        Imgproc.Canny(gaussFiltered, canny, 10, 100, aperture, false);
        v.show(canny,"Canny");
        Mat circles = new Mat();
        circles = CircleFinder(canny, 1, denom, cannyThresh_upper, cannyThresh_inner, circle_min,circle_max);
//        circles = CircleFinder(canny);
        Mat image = DrawCircles(circles, originalImage,color,lineWidth);
        //Vis bildet
        
        v.show(image, "FUDGE");


    }

    /**
     * src_gray: Input image (grayscale)
     * circles: A vector that stores sets of 3 values: x_{c}, y_{c}, r for each detected circle.
     * CV_HOUGH_GRADIENT: Define the detection method. Currently this is the only one available in OpenCV
     * dp = 1: The inverse ratio of resolution
     * min_dist = src_gray.rows/8: Minimum distance between detected centers
     * param_1 = 200: Upper threshold for the internal Canny edge detector
     * param_2 = 100*: Threshold for center detection.
     * min_radius = 0: Minimum radio to be detected. If unknown, put zero as default.
     * max_radius = 0: Maximum radius to be detected. If unknown, put zero as default
     *
     * @param image
     * @return Mat: the points where circles can be found
     */
    public Mat CircleFinder(Mat image) {
        Mat circles = new Mat();
        System.out.println(image.height());
        Imgproc.HoughCircles(image, circles, Imgproc.CV_HOUGH_GRADIENT, 1, image.height() / 10, 500, 50, 0, 500);

        return circles;
    }
    
    /**
     *
     * @param image
     * @param dp dp = 1: The inverse ratio of resolution
     * @param denominator((image.height()+image.width())/2)/denominator:
     * minimum distance between circles
     * @param cannyThresh param_1 = 200: Upper threshold for the internal 
     * Canny edge detector
     * @param centerThresh param_2 = 100*: Threshold for center detection.
     * @param minRatio min_radius = 0: Minimum radio to be detected.
     * @param maxRatio max_radius = 0: Maximum radius to be detected. 
     * @return Mat Circles in 3-layered vector
     */
    public Mat CircleFinder(Mat image,int dp,int denominator, int cannyThresh,int centerThresh,int minRatio,int maxRatio) {
        Mat circles = new Mat();
        Imgproc.HoughCircles(image, circles, Imgproc.CV_HOUGH_GRADIENT, dp, 
                ((image.height()+image.width())/2)/denominator,
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
    public Mat DrawCircles(Mat circles, Mat image){
        System.out.println("Number of circles found: " + circles.cols());
        if (circles.cols() > 0) {
            for (int i = 0; i < circles.cols(); i++) {
                double[] circle = circles.get(0, i);
                Point p = new Point(circle[0], circle[1]);
                Imgproc.circle(image, p, (int) circle[2], new Scalar(0, 0, 255), 2);
            }
        } else {
            System.out.println("could not find any circles!!");
        }
        return image;
    }
    
    /**
     *
     * @param circles: Is given by Houghcircles
     * @param image: The image you draw circles on
     * @param color: type Scalar
     * @param lineWidth: 1,2,3,4
     * @return  Mat: the image drawn
     */
    public Mat DrawCircles(Mat circles, Mat image, Scalar color, int lineWidth){
        System.out.println("Number of circles found: " + circles.cols());
        if (circles.cols() > 0) {
            for (int i = 0; i < circles.cols(); i++) {
                double[] circle = circles.get(0, i);
                Point p = new Point(circle[0], circle[1]);
                Imgproc.circle(image, p, (int) circle[2], color, lineWidth);
                System.out.println((i+1) + " Radius: " + circle[2]);
            }
        } else {
            System.out.println("could not find any circles!!");
        }
        return image;
    }
}
