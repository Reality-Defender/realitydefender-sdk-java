package ai.realitydefender.models;

import java.util.List;

/**
 * Represents file type information, including a list of acceptable file extensions and a size
 * limit. This class provides the ability to retrieve the supported file extensions and the size
 * limit for the given file type.
 */
public class FileTypeInfo {
  private final List<String> extensions;
  private final long sizeLimit;

  protected FileTypeInfo(List<String> extensions, long sizeLimit) {
    this.extensions = extensions;
    this.sizeLimit = sizeLimit;
  }

  public List<String> getExtensions() {
    return this.extensions;
  }

  public long getSizeLimit() {
    return this.sizeLimit;
  }
}
