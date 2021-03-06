/**
 * Detects circles
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
import org.opencv.core.CvType;
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

    private Size gaussKernel = new Size();
    private double sigmaX;
    private Scalar color = new Scalar(0, 0, 255);
    private int lineWidth = 4;
    private int circle_max = 500;
    private int circle_min = 20;
    private int cannyThresh_upper = 1000;
    private int cannyThresh_inner = 30;
    private int denom;
    private double hl;
    private double hu;
    private double sl;
    private double su;
    private double vl;
    private double vu;
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
     * @param pip
     * @param dh
     * @param kernel
     * @param highthreshold
     * @param lowthreshold
     * @param bufferSize
     * @param sigmaX
     */
    public CircleDetection(
            int denom,
            IARDrone drone,
            ProcessedImagePanel pip,
            DataHandler dh)
    {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        this.denom = denom;
        drone.getVideoManager().addImageListener(this);
        this.pip = pip;
        this.dh = dh;
        loadParameters();
    }

    @Override
    public synchronized void run()
    {
        Mat image = null;
        while (true) {
            try {
                wait();

                long start = System.currentTimeMillis();
                image = ic.BufferedImageToMat(bufferedImage);
                // Blurr image
                Imgproc.GaussianBlur(image, image, getGaussKernel(),
                        getSigmaX());
                //Change colorspectrum
                Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2HSV_FULL);
                //Thersholding
                image = thresholdHSV(image);
                // Call to houghCircles
                Mat circles = CircleFinder(
                        image,
                        getDenom(),
                        getCannyThresh_upper(),
                        getCannyThresh_inner(),
                        getCircle_min(),
                        getCircle_max());
                // Change colorspectrum
               Imgproc.cvtColor(image, image, Imgproc.COLOR_GRAY2RGB);
                Mat out = new Mat();
                // Draw circles in threshold image
                out = DrawCircles(
                        circles,
                        image,
                        getColor(),
                        lineWidth);
                pip.setBufferedImage((BufferedImage) ic.toBufferedImage(out));
                dh.addCentroidAndRadius(circles);
//                System.out.println("Circle detection cycletime: "
//                        + (System.currentTimeMillis() - start));

            }
            catch (InterruptedException ex) {
                System.out.println("wait(); in CircleDetection Failed: " + ex);
            }
            catch (Exception e) {
                System.out.println("Failed to aquire image: " + e);
            }
        }
    }

    /**
     * Loads image processing parameters
     */
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
            setGaussKernelDim(params[7]);
        }
        catch (IOException ex) {
            System.out.println("Parameter Loading Failed: " + ex);
        }

    }

    /**
     * detects circles, returned as MAT type, every colon in the Mat has a
     * double[] with {x,y,R} (R = Radius)
     * @param image 8-bit, single-channel, grayscale input image.
     * 
     * @param minCenterDistance Minimum distance between the centers of the 
     * detected circles. If the parameter is too small, multiple neighbor 
     * circles may be falsely detected in addition to a true one. If it is too 
     * large, some circles may be missed.
     * 
     * @param cannyThresh First method-specific parameter. In case of 
     * CV_HOUGH_GRADIENT , it is the higher threshold of the two passed to the 
     * Canny() edge detector (the lower one is twice smaller).
     * 
     * @param centerThresh Second method-specific parameter. In case of 
     * CV_HOUGH_GRADIENT , it is the accumulator threshold for the circle 
     * centers at the detection stage. The smaller it is, the more false 
     * circles may be detected. Circles, corresponding to the larger 
     * accumulator values, will be returned first.
     * 
     * @param minRadius Minimum circle radius.
     * @param maxRadius Maximum circle radius.
     * @return Output vector of found circles. Each vector is encoded as a 
     * 3-element floating-point vector(x,y,radius).
     */
    private Mat CircleFinder(
            Mat image,
            int minCenterDistance,
            int cannyThresh,
            int centerThresh,
            int minRadius,
            int maxRadius
)    {

        Mat circles = new Mat();
        Imgproc.HoughCircles(
                image,
                circles,
                Imgproc.CV_HOUGH_GRADIENT,
                1,
                minCenterDistance,
                cannyThresh,
                centerThresh,
                minRadius,
                maxRadius);

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
    private Mat DrawCircles(
            Mat circles,
            Mat image,
            Scalar color,
            int lineWidth)
    {

        Scalar colorFilt = new Scalar(0, 255, 0);
        if (circles.cols() > 0) {
            for (int i = 0; i < circles.cols(); i++) {
                double[] circle = circles.get(0, i);
                Point p = new Point(circle[0], circle[1]);
                Imgproc.circle(image, p, (int) circle[2], color, lineWidth);
            }
        }
        try {
            Point pFilt = new Point(dh.getAvg()[0], dh.getAvg()[1]);
            Imgproc.circle(
                    image,
                    pFilt,
                    (int) dh.getAvg()[2],
                    colorFilt,
                    lineWidth);

        }
        catch (Exception e) {
            System.out.println("Error printing filtered circle");
        }

        return image;
    }

    @Override
    public synchronized void imageUpdated(BufferedImage bi)
    {
        bufferedImage = bi;
        //System.out.println("Buffered image updated!");
        notifyAll();
    }

    /**
     * @return the gaussKernel
     */
    public synchronized double getGaussKernelDim()
    {
        return gaussKernel.width;
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
    public synchronized void setGaussKernelDim(double dim)
    {
        gaussKernel = new Size(dim, dim);
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
     * Changes the minimum distance between circles, smaller denom gives larger
     * distance
     *
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

    private Mat thresholdHSV(Mat image) {
        if (getHl() > getHu()) {
                    Mat image2 = image.clone();
                    Core.inRange(image,
                            new Scalar(
                                    0,
                                    getSl() * 255,
                                    getVl() * 255),
                            new Scalar(
                                    getHu() * 255,
                                    getSu() * 255,
                                    getVu() * 255),
                            image);
                    Core.inRange(image2,
                            new Scalar(
                                    getHl() * 255,
                                    getSl() * 255,
                                    getVl() * 255),
                            new Scalar(255,
                                    getSu() * 255,
                                    getVu() * 255),
                            image2);

                    Core.bitwise_or(image, image2, image);

                }
                else {
                    Core.inRange(image,
                            new Scalar(getHl() * 255,
                                    getSl() * 255,
                                    getVl() * 255),
                            new Scalar(getHu() * 255,
                                    getSu() * 255,
                                    getVu() * 255),
                            image);
                }
        return image;
    }
}
