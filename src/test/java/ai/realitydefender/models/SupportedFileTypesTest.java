package ai.realitydefender.models;

import static org.junit.jupiter.api.Assertions.*;

import ai.realitydefender.exceptions.RealityDefenderException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class SupportedFileTypesTest {

  @ParameterizedTest
  @CsvSource({
    "test.mp4, 262144000",
    "video.mov, 262144000",
    "image.jpg, 52428800",
    "photo.png, 52428800",
    "music.mp3, 20971520",
    "audio.wav, 20971520",
    "document.txt, 5242880"
  })
  void testGetFileTypeInfo_ValidFiles(String fileName, int expectedLimit)
      throws RealityDefenderException {
    FileTypeInfo result = SupportedFileTypes.getFileTypeInfo(fileName);

    assertEquals(expectedLimit, result.getSizeLimit());
    assertTrue(result.getExtensions().contains("." + getExtension(fileName)));
  }

  @ParameterizedTest
  @ValueSource(strings = {"test.pdf", "file.doc", "data.json", "code.java", "TEST.MP4"})
  void testGetFileTypeInfo_UnsupportedFiles(String fileName) {
    RealityDefenderException exception =
        assertThrows(
            RealityDefenderException.class, () -> SupportedFileTypes.getFileTypeInfo(fileName));

    assertEquals("invalid_file", exception.getCode());
    assertTrue(exception.getMessage().contains("Unsupported file " + fileName + "!"));
  }

  @Test
  void testGetFileTypeInfo_EdgeCases() {
    assertThrows(RealityDefenderException.class, () -> SupportedFileTypes.getFileTypeInfo(""));
    assertThrows(NullPointerException.class, () -> SupportedFileTypes.getFileTypeInfo(null));
    assertThrows(
        RealityDefenderException.class,
        () -> SupportedFileTypes.getFileTypeInfo("filename_without_extension"));
  }

  @Test
  void testGetFileTypeInfo_ComplexPaths() throws RealityDefenderException {
    assertEquals(262144000, SupportedFileTypes.getFileTypeInfo("my.test.file.mp4").getSizeLimit());
    assertEquals(52428800, SupportedFileTypes.getFileTypeInfo("/path/to/file.jpg").getSizeLimit());
    assertEquals(
        20971520, SupportedFileTypes.getFileTypeInfo("C:\\Users\\music.mp3").getSizeLimit());
  }

  @Test
  void testFileTypeInfo_GroupValidation() throws RealityDefenderException {
    FileTypeInfo videoInfo = SupportedFileTypes.getFileTypeInfo("test.mp4");
    assertTrue(videoInfo.getExtensions().containsAll(List.of(".mp4", ".mov")));

    FileTypeInfo imageInfo = SupportedFileTypes.getFileTypeInfo("test.jpg");
    assertTrue(
        imageInfo.getExtensions().containsAll(List.of(".jpg", ".png", ".jpeg", ".gif", ".webp")));

    FileTypeInfo audioInfo = SupportedFileTypes.getFileTypeInfo("test.mp3");
    assertTrue(
        audioInfo
            .getExtensions()
            .containsAll(List.of(".flac", ".wav", ".mp3", ".m4a", ".aac", ".alac", ".ogg")));
  }

  private String getExtension(String fileName) {
    return fileName.substring(fileName.lastIndexOf('.') + 1);
  }
}
