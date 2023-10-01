package us.wistate.enterprise.aht;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.InvalidPathException;
import java.util.LinkedList;
import java.util.List;

import static java.nio.file.StandardOpenOption.READ;

public class Tail {
    private static final int DEFAULT_BUFFER_SIZE = 256;
    private static final String DEFAULT_CHARSET = "UTF-8";
    private static final char NEW_LINE = 10;

    /**
     * Read a number of lines from bottom of file
     * @see Tail https://en.wikipedia.org/wiki/Tail_(Unix)
     * @param path a string represents for path to file
     * @param numberOfLines an integer number indicates number of lines to be retrieved
     * @return a list of string represents for lines
     *
     * @throws  InvalidPathException
     *          if file does not exist
     * @throws  IOException
     *          If an I/O error occurs
     */
    public List<String> readLines(String path, int numberOfLines) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            throw new InvalidPathException(path, "File does not exist");
        }

        LinkedList<String> lines = new LinkedList<>();
        FileChannel fileChannel = FileChannel.open(file.toPath(), READ);
        long fileSize = fileChannel.size();
        if (fileSize == 0) {
            return lines;
        }

        int bufferSize = DEFAULT_BUFFER_SIZE;
        long readBytes = 0, position;
        ByteBuffer copy;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        do {
            if (bufferSize > fileSize) {
                bufferSize = (int) fileSize;
                position = 0;
            } else {
                position = fileSize - (readBytes + bufferSize);
                if (position < 0) {
                    position = 0;
                    bufferSize = (int) (fileSize - readBytes);
                }
            }

            copy = ByteBuffer.allocate(bufferSize);
            readBytes += fileChannel.read(copy, position);

            readChunk(copy.array(), out, lines, numberOfLines);
        } while (lines.size() < numberOfLines && position > 0);
        if (out.size() > 0) {
            flushLine(lines, out);
        }
        out.close();
        fileChannel.close();

        return lines;
    }

    private void readChunk(byte[] bytes, ByteArrayOutputStream out, LinkedList<String> lines, int numberOfLines) throws IOException {
        int offset = -1, limit = 0;
        boolean flush = false;
        for (int i = bytes.length - 1; i >= 0 && lines.size() < numberOfLines; i--) {
            byte b = bytes[i];
            if (b == NEW_LINE) {
                flush = true;
            } else {
                offset = i;
                limit++;
            }
            if (flush) {
                prependBuffer(out, bytes, offset, limit);
                flushLine(lines, out);
                limit = 0;
                flush = false;
            }
        }

        if (limit > 0) {
            // in case we have some characters left without in one line
            // we add them to buffer in front of the others
            prependBuffer(out, bytes, offset, limit);
        }
    }

    private void flushLine(LinkedList<String> lines, ByteArrayOutputStream out) {
        try {
            lines.addFirst(out.toString(DEFAULT_CHARSET));
        } catch (UnsupportedEncodingException e) {
            lines.addFirst(out.toString());
        }

        out.reset();
    }

    private void prependBuffer(ByteArrayOutputStream out, byte[] bytes, int offset, int limit) throws IOException {
        ByteArrayOutputStream tmp = new ByteArrayOutputStream();
        tmp.write(bytes, offset, limit);
        out.writeTo(tmp);
        out.reset();
        tmp.writeTo(out);
    }
}