package net.xvis.streaming.rtsp;

import android.content.ContentValues;

import net.xvis.streaming.Session;
//import net.xvis.streaming.SessionBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.util.Set;

//import static net.xvis.streaming.SessionBuilder.VIDEO_NONE;

class UriParser {
    public final static String TAG = "UriParser";

    /**
     * Configures a Session according to the given URI.
     * Here are some examples of URIs that can be used to configure a Session:
     * <ul><li>rtsp://xxx.xxx.xxx.xxx:8086?h264&flash=on</li>
     * <li>rtsp://xxx.xxx.xxx.xxx:8086?h263&camera=front&flash=on</li>
     * <li>rtsp://xxx.xxx.xxx.xxx:8086?h264=200-20-320-240</li>
     * <li>rtsp://xxx.xxx.xxx.xxx:8086?aac</li></ul>
     */
    static Session parse(String uri) throws IllegalStateException, IOException {
        //SessionBuilder builder = SessionBuilder.getInstance().clone();

        String query = URI.create(uri).getQuery();
        String[] queryParams = query == null ? new String[0] : query.split("&");
        ContentValues params = new ContentValues();
        for (String param : queryParams) {
            String[] keyValue = param.split("=");
            String value = "";
            try {
                value = keyValue[1];
            } catch (ArrayIndexOutOfBoundsException ignore) {
            }

            params.put(URLEncoder.encode(keyValue[0], "UTF-8"), URLEncoder.encode(value, "UTF-8"));
        }

        if (params.size() > 0) {
            //builder.setVideoEncoder(VIDEO_NONE);
            Set<String> paramKeys = params.keySet();

            // Those parameters must be parsed first or else they won't necessarily be taken into account
            for (String paramName : paramKeys) {
                String paramValue = params.getAsString(paramName);

                // MULTICAST -> the stream will be sent to a multicast group
                // The default mutlicast address is 228.5.6.7, but the client can specify another
                if (paramName.equalsIgnoreCase("multicast")) {
                    if (paramValue != null) {
                        try {
                            InetAddress addr = InetAddress.getByName(paramValue);
                            if (!addr.isMulticastAddress()) {
                                throw new IllegalStateException("Invalid multicast address !");
                            }
                            //builder.setDestination(paramValue);
                        } catch (UnknownHostException e) {
                            throw new IllegalStateException("Invalid multicast address !");
                        }
                    } else {
                        // Default multicast address
                        //builder.setDestination("228.5.6.7");
                    }
                }
                // UNICAST -> the client can use this to specify where he wants the stream to be sent
                else if (paramName.equalsIgnoreCase("unicast")) {
                    if (paramValue != null) {
                        //builder.setDestination(paramValue);
                    }
                }
                // TTL -> the client can modify the time to live of packets
                // By default ttl=64
                else if (paramName.equalsIgnoreCase("ttl")) {
                    if (paramValue != null) {
                        try {
                            int ttl = Integer.parseInt(paramValue);
                            if (ttl < 0) throw new IllegalStateException();
                            //builder.setTimeToLive(ttl);
                        } catch (Exception e) {
                            throw new IllegalStateException("The TTL must be a positive integer !");
                        }
                    }
                }
            }
        }

//        if (builder.getVideoEncoder() == VIDEO_NONE) {
//            SessionBuilder b = SessionBuilder.getInstance();
//            builder.setVideoEncoder(b.getVideoEncoder());
//        }

        return null;//builder.build();
    }

}
