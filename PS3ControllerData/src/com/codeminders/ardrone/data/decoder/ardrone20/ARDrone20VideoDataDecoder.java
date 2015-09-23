package com.codeminders.ardrone.data.decoder.ardrone20;

import java.io.InputStream;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.codeminders.ardrone.ARDrone;
import com.codeminders.ardrone.VideoDataDecoder;
import com.twilight.h264.decoder.AVFrame;
import com.twilight.h264.decoder.AVPacket;
import static com.twilight.h264.decoder.H264Context.NAL_AUD;
import static com.twilight.h264.decoder.H264Context.NAL_IDR_SLICE;
import static com.twilight.h264.decoder.H264Context.NAL_SLICE;
import com.twilight.h264.decoder.H264Decoder;
import com.twilight.h264.decoder.MpegEncContext;
//import com.twilight.h264.util.FrameUtils; (Vegard) Added new FrameUtils class that handles AVFrame

public class ARDrone20VideoDataDecoder extends VideoDataDecoder {

    Logger log = Logger.getLogger(this.getClass().getName());

    public static final int INBUF_SIZE = 65535;

    H264Decoder codec;
    MpegEncContext c = null;
    int frame, len;
    int[] got_picture = new int[1];

    AVFrame picture;
    boolean foundFrameStart;

    byte[] inbuf = new byte[INBUF_SIZE + MpegEncContext.FF_INPUT_BUFFER_PADDING_SIZE];
    int[] inbuf_int = new int[INBUF_SIZE + MpegEncContext.FF_INPUT_BUFFER_PADDING_SIZE];
    byte[] buf = new byte[1024];
    private int[] buffer = null;

    AVPacket avpkt;

    int dataPointer;

    public ARDrone20VideoDataDecoder(ARDrone drone) {
        super(drone);

        avpkt = new AVPacket();
        avpkt.av_init_packet();

        Arrays.fill(inbuf, INBUF_SIZE, MpegEncContext.FF_INPUT_BUFFER_PADDING_SIZE + INBUF_SIZE, (byte) 0);

        codec = new H264Decoder();
        if (codec == null) {
            System.out.println("codec not found\n");
            System.exit(1);
        }

        c = MpegEncContext.avcodec_alloc_context();
        picture = AVFrame.avcodec_alloc_frame();

        if ((codec.capabilities & H264Decoder.CODEC_CAP_TRUNCATED) != 0) {
            c.flags |= MpegEncContext.CODEC_FLAG_TRUNCATED; /* we do not send complete frames */

        }

        if (c.avcodec_open(codec) < 0) {
            System.out.println("could not open codec\n");
            System.exit(1);
        }
    }

    @Override
    public void run() {

        InputStream fin = getDataReader().getDataStream();
        try {
            // avpkt must contain exactly 1 NAL Unit in order for decoder to decode correctly.
            // thus we must read until we get next NAL header before sending it to decoder.
            // Find 1st NAL
            frame = 0;
            int dataPointer;
            int fileOffset = 0;
            foundFrameStart = false;

		    // avpkt must contain exactly 1 NAL Unit in order for decoder to decode correctly.
            // thus we must read until we get next NAL header before sending it to decoder.
            // Find 1st NAL
            int[] cacheRead = new int[5];
            cacheRead[0] = fin.read();
            cacheRead[1] = fin.read();
            cacheRead[2] = fin.read();
            cacheRead[3] = fin.read();

            while (!(cacheRead[0] == 0x00
                    && cacheRead[1] == 0x00
                    && cacheRead[2] == 0x00
                    && cacheRead[3] == 0x01)) {
                cacheRead[0] = cacheRead[1];
                cacheRead[1] = cacheRead[2];
                cacheRead[2] = cacheRead[3];
                cacheRead[3] = fin.read();
            } // while

            boolean hasMoreNAL = true;
            cacheRead[4] = fin.read();

            // 4 first bytes always indicate NAL header
            while (hasMoreNAL) {
                inbuf_int[0] = cacheRead[0];
                inbuf_int[1] = cacheRead[1];
                inbuf_int[2] = cacheRead[2];
                inbuf_int[3] = cacheRead[3];
                inbuf_int[4] = cacheRead[4];

                dataPointer = 5;
                // Find next NAL
                cacheRead[0] = fin.read();
                if (cacheRead[0] == -1) {
                    hasMoreNAL = false;
                }
                cacheRead[1] = fin.read();
                if (cacheRead[1] == -1) {
                    hasMoreNAL = false;
                }
                cacheRead[2] = fin.read();
                if (cacheRead[2] == -1) {
                    hasMoreNAL = false;
                }
                cacheRead[3] = fin.read();
                if (cacheRead[3] == -1) {
                    hasMoreNAL = false;
                }
                cacheRead[4] = fin.read();
                if (cacheRead[4] == -1) {
                    hasMoreNAL = false;
                }
                while (!(cacheRead[0] == 0x00
                        && cacheRead[1] == 0x00
                        && cacheRead[2] == 0x00
                        && cacheRead[3] == 0x01
                        && isEndOfFrame(cacheRead[4])) && hasMoreNAL) {
                    inbuf_int[dataPointer++] = cacheRead[0];
                    cacheRead[0] = cacheRead[1];
                    cacheRead[1] = cacheRead[2];
                    cacheRead[2] = cacheRead[3];
                    cacheRead[3] = cacheRead[4];
                    cacheRead[4] = fin.read();
                    if (cacheRead[4] == -1) {
                        hasMoreNAL = false;
                    }
                } // while

                avpkt.size = dataPointer;
                //System.out.println(String.format("Offset 0x%X, packet size 0x%X, nal=0x%X", fileOffset, dataPointer, inbuf_int[4] & 0x1F));
                fileOffset += dataPointer - 1;

                avpkt.data_base = inbuf_int;
                avpkt.data_offset = 0;

                try {
                    while (avpkt.size > 0) {
                        len = c.avcodec_decode_video2(picture, got_picture, avpkt);
                        if (len < 0) {
                            System.out.println("Error while decoding frame " + frame +" " + len);
                            // Discard current packet and proceed to next packet
                            break;
                        } // if
                        if (got_picture[0] != 0) {
                            picture = c.priv_data.displayPicture;

                            int bufferSize = picture.imageWidth * picture.imageHeight;
                            if (buffer == null || bufferSize != buffer.length) {
                                buffer = new int[bufferSize];
                            }

                            FrameUtils.YUV2RGB(picture, buffer);

                            notifyDroneWithDecodedFrame(0, 0, picture.imageWidth, picture.imageHeight, buffer, 0, picture.imageWidth);
                        }
                        avpkt.size -= len;
                        avpkt.data_offset += len;
                    }
                } catch (Exception ie) {
                    // Any exception, we should try to proceed reading next packet!
                    log.log(Level.FINEST, "Error decodeing frame", ie);
                } // try

            } // while

        } catch (Exception ex) {
            log.log(Level.FINEST, "Error in decoder initialization", ex);
        }

    }

    @Override
    public void finish() {
        c.avcodec_close();
    }

    private boolean isEndOfFrame(int code) {
        int nal = code & 0x1F;

        if (nal == NAL_AUD) {
            foundFrameStart = false;
            return true;
        }

        boolean foundFrame = foundFrameStart;
        if (nal == NAL_SLICE || nal == NAL_IDR_SLICE) {
            if (foundFrameStart) {
                return true;
            }
            foundFrameStart = true;
        } else {
            foundFrameStart = false;
        }

        return foundFrame;
    }

}
