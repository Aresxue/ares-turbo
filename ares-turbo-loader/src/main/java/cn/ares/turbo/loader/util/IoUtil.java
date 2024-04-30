package cn.ares.turbo.loader.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

/**
 * @author: Ares
 * @time: 2023-11-16 11:01:14
 * @description: Io工具类
 * @description: Io util
 * @version: JDK 1.8
 */
public class IoUtil {

  /**
   * Represents the end-of-file (or stream).
   */
  public static final int EOF = -1;

  /**
   * The default buffer size ({@value}) to use in copy methods.
   */
  public static final int DEFAULT_BUFFER_SIZE = 8192;

  /**
   * Copies bytes from an {@code InputStream} to an {@code OutputStream}.
   * <p>
   * This method buffers the input internally, so there is no need to use a
   * {@code BufferedInputStream}.
   * </p>
   * <p>
   * Large streams (over 2GB) will return a bytes copied value of {@code -1} after the copy has
   * completed since the correct number of bytes cannot be returned as an int. For large streams use
   * the {@code copyLarge(InputStream, OutputStream)} method.
   * </p>
   *
   * @param inputStream  the {@code InputStream} to read.
   * @param outputStream the {@code OutputStream} to write.
   * @return the number of bytes copied, or -1 if greater than {@link Integer#MAX_VALUE}.
   * @throws NullPointerException if the InputStream is {@code null}.
   * @throws NullPointerException if the OutputStream is {@code null}.
   * @throws IOException          if an I/O error occurs.
   */
  public static int copy(final InputStream inputStream, final OutputStream outputStream)
      throws IOException {
    final long count = copyLarge(inputStream, outputStream);
    if (count > Integer.MAX_VALUE) {
      return EOF;
    }
    return (int) count;
  }

  /**
   * Copies bytes from a large (over 2GB) {@code InputStream} to an {@code OutputStream}.
   * <p>
   * This method buffers the input internally, so there is no need to use a
   * {@code BufferedInputStream}.
   * </p>
   * <p>
   * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
   * </p>
   *
   * @param inputStream  the {@code InputStream} to read.
   * @param outputStream the {@code OutputStream} to write.
   * @return the number of bytes copied.
   * @throws NullPointerException if the InputStream is {@code null}.
   * @throws NullPointerException if the OutputStream is {@code null}.
   * @throws IOException          if an I/O error occurs.
   */
  public static long copyLarge(final InputStream inputStream, final OutputStream outputStream)
      throws IOException {
    return copy(inputStream, outputStream, DEFAULT_BUFFER_SIZE);
  }


  /**
   * Copies bytes from an {@code InputStream} to an {@code OutputStream} using an internal buffer of
   * the given size.
   * <p>
   * This method buffers the input internally, so there is no need to use a
   * {@code BufferedInputStream}.
   * </p>
   *
   * @param inputStream  the {@code InputStream} to read.
   * @param outputStream the {@code OutputStream} to write to
   * @param bufferSize   the bufferSize used to copy from the input to the output
   * @return the number of bytes copied.
   * @throws NullPointerException if the InputStream is {@code null}.
   * @throws NullPointerException if the OutputStream is {@code null}.
   * @throws IOException          if an I/O error occurs.
   */
  public static long copy(final InputStream inputStream, final OutputStream outputStream,
      final int bufferSize) throws IOException {
    return copyLarge(inputStream, outputStream, new byte[bufferSize]);
  }

  /**
   * Copies bytes from a large (over 2GB) {@code InputStream} to an {@code OutputStream}.
   * <p>
   * This method uses the provided buffer, so there is no need to use a
   * {@code BufferedInputStream}.
   * </p>
   *
   * @param inputStream  the {@code InputStream} to read.
   * @param outputStream the {@code OutputStream} to write.
   * @param buffer       the buffer to use for the copy
   * @return the number of bytes copied.
   * @throws NullPointerException if the InputStream is {@code null}.
   * @throws NullPointerException if the OutputStream is {@code null}.
   * @throws IOException          if an I/O error occurs.
   */
  public static long copyLarge(final InputStream inputStream, final OutputStream outputStream,
      final byte[] buffer) throws IOException {
    Objects.requireNonNull(inputStream, "inputStream");
    Objects.requireNonNull(outputStream, "outputStream");
    long count = 0;
    int n;
    while (EOF != (n = inputStream.read(buffer))) {
      outputStream.write(buffer, 0, n);
      count += n;
    }
    return count;
  }

}
