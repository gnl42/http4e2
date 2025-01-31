/*
 *  Copyright 2017 Eclipse HttpClient (http4e) https://nextinterfaces.com
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package me.glindholm.plugin.http4e2.crypt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.apache.commons.io.HexDump;

/**
 * Utility class dumping bytes to hexadecimal format.
 *
 * @author Atanas Roussev (https://nextinterfaces.com)
 */
public class HexUtils {

    /**
     * Don't instantiate me.
     */
    private HexUtils() {
    }

    /**
     * Converts a byte array to pretty hex formatted string.
     */
    public static String formattedHex(final byte[] b) {
        if (b == null) {
            return null;
        }

        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < b.length; ++i) {
            if (i % 16 == 0) {
                sb.append(Integer.toHexString(i & 0xFFFF | 0x10000).substring(1, 5) + "  ");
            }
            sb.append((Integer.toHexString(b[i] & 0xFF | 0x100).substring(1, 3) + " ").toUpperCase());
            if (i % 16 == 15 || i == b.length - 1) {
                int j;
                for (j = 16 - i % 16; j > 1; --j) {
                    sb.append("   ");
                }
                sb.append(" [");
                final int start = i / 16 * 16;
                final int end = b.length < i + 1 ? b.length : i + 1;
                for (j = start; j < end; ++j) {
                    if (b[j] >= 32 && b[j] <= 126) {
                        sb.append((char) b[j]);
                    } else {
                        sb.append(".");
                    }
                }
                sb.append("]\n");
            }
        }
        return sb.toString();
    }

    public static String toHexChars(final byte[] data) {
        if (data == null) {
            return null;
        }
        final StringBuilder buf = new StringBuilder();
        buf.append("hex:[");
        for (final byte element : data) {
            buf.append(byteToHex(element));
            buf.append("(");
            if (element == 13) {
                buf.append("\\r");
            } else if (element == 10) {
                buf.append("\\n");
            } else {
                buf.append((char) element);
            }
            buf.append(") ");
        }
        buf.append("]");
        return buf.toString();
    }

    public static String toByteChars(final byte[] data) {
        if (data == null) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        sb.append("bytes:[");
        for (final byte element : data) {
            sb.append(element);
            sb.append("(");
            if (element == 13) {
                sb.append("\\r");
            } else if (element == 10) {
                sb.append("\\n");
            } else {
                sb.append((char) element);
            }
            sb.append(") ");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Transforms hex string to byte array
     *
     * @param hexString the string to convert
     * @return the converted byte array
     */
    public static byte[] hexToBytes(final String hexString) {
        if (hexString == null) {
            return null;
        }
        // TODO opitmize algorithm
        // new BigInteger(hexString,16).toByteArray() appends an extra byte

        final byte[] bts = new byte[hexString.length() / 2];
        final int len = bts.length;
        for (int i = 0; i < len; i++) {
            bts[i] = (byte) Integer.parseInt(hexString.substring(2 * i, 2 * i + 2), 16);
        }
        return bts;
    }

    /**
     * Creates String of pretty hex format, f.i.:
     *
     * ---------HEX start--------- [7A786376626E6D2C2E]
     *
     * 0000 7A 78 63 76 62 6E 6D 2C 2E [zxcvbnm,.] ---------HEX end---------
     */
    public static String prettyHex(final byte[] data) {
        if (data == null) {
            return null;
        }

        final StringBuilder sb = new StringBuilder();
        String hexVal = HexUtils.bytesToHex(data);
        hexVal = hexVal != null ? hexVal.toUpperCase() : hexVal;
        sb.append("\n--------------------------------------------------------HEX start--------\n");
        sb.append("[" + hexVal + "]");
        sb.append("\n\n");
        sb.append(HexUtils.formattedHex(data));
        sb.append("--------------------------------------------------------HEX end----------\n");

        return sb.toString();
    }

    /**
     * Prints the byte array into hex format to the provided stream
     */
    public static void printHex(final byte[] b, final PrintStream out) {

        for (int i = 0; i < b.length; ++i) {
            if (i % 16 == 0) {
                out.print(Integer.toHexString(i & 0xFFFF | 0x10000).substring(1, 5) + " - ");
            }
            out.print(Integer.toHexString(b[i] & 0xFF | 0x100).substring(1, 3) + " ");
            if (i % 16 == 15 || i == b.length - 1) {
                int j;
                for (j = 16 - i % 16; j > 1; --j) {
                    out.print("   ");
                }
                out.print(" - ");
                final int start = i / 16 * 16;
                final int end = b.length < i + 1 ? b.length : i + 1;
                for (j = start; j < end; ++j) {
                    if (b[j] >= 32 && b[j] <= 126) {
                        out.print((char) b[j]);
                    } else {
                        out.print(".");
                    }
                }
                out.println();
            }
        }
        out.println();
    }

    /**
     * Convenience method to convert a byte to a hex string.
     *
     * @param data the byte to convert
     * @return String the converted byte
     */
    public static String byteToHex(final byte data) {
        final StringBuilder buf = new StringBuilder();
        buf.append(toHexChar(data >>> 4 & 0x0F));
        buf.append(toHexChar(data & 0x0F));
        return buf.toString();
    }

    /**
     * Convenience method to convert a byte array to a hex string.
     *
     * @param data the byte[] to convert
     * @return String the converted byte[]
     */
    public static String bytesToHex(final byte[] data) {
        final StringBuilder buf = new StringBuilder();
        for (final byte element : data) {
            buf.append(byteToHex(element));
        }
        return buf.toString();
    }

    public static String dumpHexChars(final byte[] data) {
        final StringBuilder buf = new StringBuilder();
        buf.append("hex:[");
        for (final byte element : data) {
            buf.append(byteToHex(element));
            buf.append("(");
            if (element == 13) {
                buf.append("\\r");
            } else if (element == 10) {
                buf.append("\\n");
            } else {
                buf.append((char) element);
            }
            buf.append(") ");
        }
        buf.append("]");
        return buf.toString();
    }

    public static String dumpByteChars(final byte[] data) {
        final StringBuilder sb = new StringBuilder();
        sb.append("bytes:[");
        for (final byte element : data) {
            sb.append(element);
            sb.append("(");
            if (element == 13) {
                sb.append("\\r");
            } else if (element == 10) {
                sb.append("\\n");
            } else {
                sb.append((char) element);
            }
            sb.append(") ");
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Convenience method to convert an int to a hex char.
     *
     * @param i the int to convert
     * @return char the converted char
     */
    public static char toHexChar(final int i) {
        if (0 <= i && i <= 9) {
            return (char) ('0' + i);
        } else {
            return (char) ('a' + (i - 10));
        }
    }

    public static String printHex(final InputStream in, final OutputStream out) {

        final StringBuilder sb = new StringBuilder();

        try {
            final int len = 16;
            byte[] ch = new byte[len];
            int i = 0;
            while (in.read(ch) > 0) {
                out.write(ch);
                boolean prevRowNull = false;
                for (int k = 0; k < ch.length; k++) {
                    if (ch[k] == 0) {
                        prevRowNull = true;
                    }
                    if (!prevRowNull) {
                        if (k % 16 == 0) {
                            sb.append(Integer.toHexString(i * len & 0xFFFF | 0x10000).substring(1, 5) + " - ");
                        }
                        sb.append(Integer.toHexString(ch[k] & 0xFF | 0x100).substring(1, 3) + " ");
                        // sb.append("("+prevRowNull+")");
                        if (k % 16 == 15 || k == ch.length - 1) {
                            int j;
                            for (j = 16 - k % 16; j > 1; --j) {
                                sb.append(" ");
                            }
                            sb.append(" - ");
                            final int start = k / 16 * 16;
                            final int end = ch.length < k + 1 ? ch.length : k + 1;
                            for (j = start; j < end; ++j) {
                                if (ch[j] >= 32 && ch[j] <= 126) {
                                    sb.append((char) ch[j]);
                                } else {
                                    sb.append(".");
                                }
                            }
                            sb.append("\n");
                        }
                    }
                }
                ch = new byte[len];
                i++;
            }

            System.out.println(sb.toString());

        } catch (final IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    /**
     * Returns the byte array into hex format
     *
     * @throws IOException
     */
    public static String toHex(final byte[] b) throws IOException {
        try (OutputStream os = new ByteArrayOutputStream()) {
            HexDump.dump(b, 0, os, 0);
            return os.toString();
        }
    }

}
