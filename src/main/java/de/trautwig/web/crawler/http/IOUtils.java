package de.trautwig.web.crawler.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class IOUtils {

    public static byte[] consumeFully(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        for (int len = 0; len != -1; len = inputStream.read(buffer)) {
            baos.write(buffer, 0, len);
            // TODO length limit
        }
        return baos.toByteArray();
    }

    private IOUtils() {
    }

}
