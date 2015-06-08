package org.jboss.aerogear.test;

import com.jayway.restassured.response.Header;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.UUID;

public class Utilities {

    public static final class ContentTypes {

        public static String json() {
            return "application/json";
        }

        public static String jsonUTF8() {
            return "application/json; charset=utf-8";
        }

        public static String multipartFormData() {
            return "multipart/form-data";
        }

        public static String octetStream() {
            return "application/octet-stream";
        }

    }

    public static final class Headers {

        public static Header acceptJson() {
            return new Header("Accept", "application/json");
        }

    }

    public static final class FileUtils {

        public static byte[] toByteArray(File file) {
            FileInputStream inputStream = null;
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                inputStream = new FileInputStream(file);
                int read = 0;
                byte[] buffer = new byte[4096];
                while ((read = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
                outputStream.flush();
                return outputStream.toByteArray();
            } catch (FileNotFoundException e) {
                throw new IllegalArgumentException(MessageFormat.format("File {0} does not exist!", file.getName()), e);
            } catch (IOException e) {
                throw new IllegalStateException(MessageFormat.format("Couldn't read file {0}!", file.getName()), e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace(); // FIXME should we care?
                    }
                }
            }
        }

    }

    public static class Strings {

        public static String randomStringOfLength(int length) {
            StringBuilder builder = new StringBuilder();

            while(builder.length() < length) {
                builder.append(UUID.randomUUID().toString());
            }

            return builder.substring(0, length);
        }

    }

    public static final class Urls {

        public static final URL from(String url) throws IllegalArgumentException {
            try {
                return new URL(url);
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Unable to convert " + url + "to URL object");
            }
        }
    }

    public static class Validate {

        public static void notNull(Object object) {
            if (object == null) {
                throw new IllegalStateException("Object must not be null");
            }
        }
    }
}
