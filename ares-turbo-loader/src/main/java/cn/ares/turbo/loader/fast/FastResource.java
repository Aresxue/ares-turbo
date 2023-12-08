package cn.ares.turbo.loader.fast;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.security.CodeSigner;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.Manifest;
import sun.nio.ByteBuffered;

/**
 * This class is used to represent a Resource that has been loaded from the class path.
 */
public abstract class FastResource {

  private final Lock lock = new ReentrantLock();

  /**
   * Returns the name of the Resource.
   */
  public abstract String getName();

  /**
   * Returns the URL of the Resource.
   */
  public abstract URL getURL();

  /**
   * Returns the CodeSource URL for the Resource.
   */
  public abstract URL getCodeSourceURL();

  /**
   * Returns an InputStream for reading the Resource data.
   */
  public abstract InputStream getInputStream() throws IOException;

  /**
   * Returns the length of the Resource data, or -1 if unknown.
   */
  public abstract int getContentLength() throws IOException;

  private InputStream cachedInputStream;

  /* Cache result in case getBytes is called after getByteBuffer. */
  private InputStream cachedInputStream() throws IOException {
    lock.lock();
    try {
      if (cachedInputStream == null) {
        cachedInputStream = getInputStream();
      }
      return cachedInputStream;
    } finally {
      lock.unlock();
    }
  }

  /**
   * Returns the Resource data as an array of bytes.
   */
  public byte[] getBytes() throws IOException {
    byte[] bytes;
    // Get stream before content length so that a FileNotFoundException
    // can propagate upwards without being caught too early
    InputStream inputStream = cachedInputStream();

    // This code has been uglified to protect against interrupts.
    // Even if a thread has been interrupted when loading resources,
    // the IO should not abort, so must carefully retry, failing only
    // if the retry leads to some other IO exception.

    boolean isInterrupted = Thread.interrupted();
    int len;
    while (true) {
      try {
        len = getContentLength();
        break;
      } catch (InterruptedIOException iioe) {
        Thread.interrupted();
        isInterrupted = true;
      }
    }

    try {
      bytes = new byte[0];
      if (len == -1) {
        len = Integer.MAX_VALUE;
      }
      int pos = 0;
      while (pos < len) {
        int bytesToRead;
        // Only expand when there's no room
        if (pos >= bytes.length) {
          bytesToRead = Math.min(len - pos, bytes.length + 1024);
          if (bytes.length < pos + bytesToRead) {
            bytes = Arrays.copyOf(bytes, pos + bytesToRead);
          }
        } else {
          bytesToRead = bytes.length - pos;
        }
        int cc = 0;
        try {
          cc = inputStream.read(bytes, pos, bytesToRead);
        } catch (InterruptedIOException exception) {
          Thread.interrupted();
          isInterrupted = true;
        }
        if (cc < 0) {
          if (len != Integer.MAX_VALUE) {
            throw new EOFException("Detect premature EOF");
          } else {
            if (bytes.length != pos) {
              bytes = Arrays.copyOf(bytes, pos);
            }
            break;
          }
        }
        pos += cc;
      }
    } finally {
      try {
        inputStream.close();
      } catch (InterruptedIOException exception) {
        isInterrupted = true;
      } catch (IOException ignore) {
      }

      if (isInterrupted) {
        Thread.currentThread().interrupt();
      }
    }
    return bytes;
  }

  /**
   * Returns the Resource data as a ByteBuffer, but only if the input stream was implemented on top
   * of a ByteBuffer. Return <tt>null</tt> otherwise.
   */
  public ByteBuffer getByteBuffer() throws IOException {
    InputStream cachedInputStream = cachedInputStream();
    if (cachedInputStream instanceof ByteBuffered) {
      return ((ByteBuffered) cachedInputStream).getByteBuffer();
    }
    return null;
  }

  /**
   * Returns the Manifest for the Resource, or null if none.
   */
  public Manifest getManifest() throws IOException {
    return null;
  }

  /**
   * Returns theCertificates for the Resource, or null if none.
   */
  public Certificate[] getCertificates() {
    return null;
  }

  /**
   * Returns the code signers for the Resource, or null if none.
   */
  public CodeSigner[] getCodeSigners() {
    return null;
  }

}
