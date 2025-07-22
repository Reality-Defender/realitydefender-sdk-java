package ai.realitydefender.models;

import ai.realitydefender.exceptions.RealityDefenderException;
import java.util.ArrayList;
import java.util.List;

public class SupportedFileTypes {
  private static final List<FileTypeInfo> supportedFileTypes;

  static {
    supportedFileTypes = new ArrayList<>();
    supportedFileTypes.add(new FileTypeInfo(List.of(".mp4", ".mov"), 262_144_000));
    supportedFileTypes.add(
        new FileTypeInfo(List.of(".jpg", ".png", ".jpeg", ".gif", ".webp"), 52_428_800));
    supportedFileTypes.add(
        new FileTypeInfo(
            List.of(".flac", ".wav", ".mp3", ".m4a", ".aac", ".alac", ".ogg"), 20_971_520));
    supportedFileTypes.add(new FileTypeInfo(List.of(".txt"), 5_242_880));
  }

  public static FileTypeInfo getFileTypeInfo(String fileName) throws RealityDefenderException {
    int extensionIndex = fileName.lastIndexOf('.');
    if (extensionIndex == -1) {
      throw new RealityDefenderException(
          "Unsupported file with no extension " + fileName + "!", "invalid_file");
    }
    String extension = fileName.substring(extensionIndex);
    return supportedFileTypes.stream()
        .filter(x -> x.getExtensions().contains(extension))
        .findFirst()
        .orElseThrow(
            () ->
                new RealityDefenderException("Unsupported file " + fileName + "!", "invalid_file"));
  }
}
