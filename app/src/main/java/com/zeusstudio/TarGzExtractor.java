package com.zeusstudio;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public final class TarGzExtractor {

    private static final int BLOCK_SIZE = 512;

    private TarGzExtractor() {
    }

    public static void extract(
            File archive,
            File destination
    ) throws IOException {

        if (!archive.exists()) {
            throw new IOException(
                    "Archive does not exist: "
                            + archive.getAbsolutePath()
            );
        }

        if (!destination.exists()
                && !destination.mkdirs()) {

            throw new IOException(
                    "Unable to create destination: "
                            + destination.getAbsolutePath()
            );
        }

        File canonicalDestination =
                destination.getCanonicalFile();

        try (
                InputStream fileInput =
                        new BufferedInputStream(
                                new FileInputStream(archive)
                        );

                GZIPInputStream gzipInput =
                        new GZIPInputStream(fileInput)
        ) {

            extractTar(
                    gzipInput,
                    canonicalDestination
            );
        }
    }

    private static void extractTar(
            InputStream input,
            File destination
    ) throws IOException {

        byte[] header =
                new byte[BLOCK_SIZE];

        while (true) {

            int bytesRead =
                    readFullyOrEnd(
                            input,
                            header
                    );

            if (bytesRead == -1) {
                break;
            }

            if (bytesRead != BLOCK_SIZE) {
                throw new IOException(
                        "Invalid TAR header"
                );
            }

            if (isEmptyBlock(header)) {

                /*
                 * TAR ends with two zero blocks.
                 */
                break;
            }

            String name =
                    readString(
                            header,
                            0,
                            100
                    );

            if (name.isEmpty()) {
                break;
            }

            long size =
                    parseOctal(
                            header,
                            124,
                            12
                    );

            int type =
                    header[156] & 0xff;

            File output =
                    safeResolve(
                            destination,
                            name
                    );

            if (type == '5'
                    || name.endsWith("/")) {

                if (!output.exists()
                        && !output.mkdirs()) {

                    throw new IOException(
                            "Unable to create directory: "
                                    + output
                    );
                }

                skipFully(
                        input,
                        size
                );

            } else if (type == 0
                    || type == '0') {

                File parent =
                        output.getParentFile();

                if (parent != null
                        && !parent.exists()
                        && !parent.mkdirs()) {

                    throw new IOException(
                            "Unable to create directory: "
                                    + parent
                    );
                }

                try (
                        BufferedOutputStream outputStream =
                                new BufferedOutputStream(
                                        new FileOutputStream(
                                                output
                                        )
                                )
                ) {

                    copyExactly(
                            input,
                            outputStream,
                            size
                    );
                }

                /*
                 * The TAR entry is padded to a
                 * 512-byte boundary.
                 */
                long padding =
                        paddingFor(size);

                skipFully(
                        input,
                        padding
                );

            } else {

                /*
                 * For now ignore symbolic links,
                 * hard links, devices, etc.
                 *
                 * The Node runtime we generated
                 * only needs regular files and
                 * directories.
                 */
                skipFully(
                        input,
                        size
                );

                skipFully(
                        input,
                        paddingFor(size)
                );
            }
        }
    }

    private static File safeResolve(
            File destination,
            String entryName
    ) throws IOException {

        /*
         * Normalize separators.
         */
        String normalized =
                entryName.replace(
                        '\\',
                        '/'
                );

        /*
         * Prevent TAR path traversal.
         */
        while (normalized.startsWith("/")) {
            normalized =
                    normalized.substring(1);
        }

        File result =
                new File(
                        destination,
                        normalized
                );

        File canonical =
                result.getCanonicalFile();

        String destinationPath =
                destination
                        .getCanonicalPath();

        String canonicalPath =
                canonical
                        .getCanonicalPath();

        if (!canonicalPath.equals(
                destinationPath)
                && !canonicalPath.startsWith(
                destinationPath + File.separator
        )) {

            throw new IOException(
                    "Unsafe TAR entry: "
                            + entryName
            );
        }

        return canonical;
    }

    private static boolean isEmptyBlock(
            byte[] block
    ) {

        for (byte b : block) {

            if (b != 0) {
                return false;
            }
        }

        return true;
    }

    private static String readString(
            byte[] buffer,
            int offset,
            int length
    ) {

        int end =
                offset + length;

        int actualEnd =
                offset;

        while (actualEnd < end
                && buffer[actualEnd] != 0) {

            actualEnd++;
        }

        return new String(
                buffer,
                offset,
                actualEnd - offset
        ).trim();
    }

    private static long parseOctal(
            byte[] buffer,
            int offset,
            int length
    ) {

        long value = 0;

        int end =
                offset + length;

        int index =
                offset;

        while (index < end
                && (buffer[index] == ' '
                || buffer[index] == 0)) {

            index++;
        }

        while (index < end) {

            byte c =
                    buffer[index];

            if (c < '0'
                    || c > '7') {

                break;
            }

            value =
                    (value * 8)
                            + (c - '0');

            index++;
        }

        return value;
    }

    private static long paddingFor(
            long size
    ) {

        long remainder =
                size % BLOCK_SIZE;

        if (remainder == 0) {
            return 0;
        }

        return BLOCK_SIZE - remainder;
    }

    private static int readFullyOrEnd(
            InputStream input,
            byte[] buffer
    ) throws IOException {

        int total = 0;

        while (total < buffer.length) {

            int count =
                    input.read(
                            buffer,
                            total,
                            buffer.length - total
                    );

            if (count == -1) {

                if (total == 0) {
                    return -1;
                }

                return total;
            }

            total += count;
        }

        return total;
    }

    private static void copyExactly(
            InputStream input,
            BufferedOutputStream output,
            long size
    ) throws IOException {

        byte[] buffer =
                new byte[8192];

        long remaining =
                size;

        while (remaining > 0) {

            int requested =
                    (int) Math.min(
                            buffer.length,
                            remaining
                    );

            int count =
                    input.read(
                            buffer,
                            0,
                            requested
                    );

            if (count == -1) {

                throw new IOException(
                        "Unexpected end of TAR entry"
                );
            }

            output.write(
                    buffer,
                    0,
                    count
            );

            remaining -= count;
        }
    }

    private static void skipFully(
            InputStream input,
            long amount
    ) throws IOException {

        long remaining =
                amount;

        while (remaining > 0) {

            long skipped =
                    input.skip(remaining);

            if (skipped > 0) {

                remaining -= skipped;
                continue;
            }

            if (input.read() == -1) {

                throw new IOException(
                        "Unexpected end of TAR archive"
                );
            }

            remaining--;
        }
    }
}