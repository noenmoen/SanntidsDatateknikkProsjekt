package ImageProcessing;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import javax.imageio.ImageIO;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;

/**
 *
 * @author Morten
 */
public class ImageConverter {

    Mat matrix;
    MatOfByte mob;
    String fileExten;

    public ImageConverter() {

    }

    public BufferedImage MatToBufferedImage(Mat matrix, String fileExtension) {
        this.matrix = matrix;
        fileExten = fileExtension;
        mob = new MatOfByte();
        //convert the matrix into a matrix of bytes appropriate for
        //this file extension
        Imgcodecs.imencode(fileExten, matrix, mob);
        
        //convert the "matrix of bytes" into a byte array
        byte[] byteArray = mob.toArray();
        BufferedImage bufImage = null;
        try {
            InputStream in = new ByteArrayInputStream(byteArray);
            bufImage = ImageIO.read(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bufImage;

    }

    /**
     * Converts image from BufferedImage to Mat
     *
     * @param image
     * @return
     */

    public Mat BufferedImageToMat(BufferedImage image) {
        int rows = image.getWidth();
        int cols = image.getHeight();
        int type = CvType.CV_8UC(3);
        Mat newMat = new Mat(rows, cols, CvType.channels(type));
        System.out.println(CvType.channels(type));

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                newMat.put(r, c, image.getRGB(r, c));
            }
        }
        return newMat;
    }

    public Image toBufferedImage(Mat matrix) {
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (matrix.channels() > 1) {
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int bufferSize = matrix.channels() * matrix.cols() * matrix.rows();
        byte[] buffer = new byte[bufferSize];
        matrix.get(0, 0, buffer); // get all the pixels
        BufferedImage image = new BufferedImage(matrix.cols(), matrix.
                rows(), type);
        final byte[] targetPixels = ((DataBufferByte) image.getRaster().
                getDataBuffer()).getData();
        System.arraycopy(buffer, 0, targetPixels, 0, buffer.length);
        return image;
    }
}
