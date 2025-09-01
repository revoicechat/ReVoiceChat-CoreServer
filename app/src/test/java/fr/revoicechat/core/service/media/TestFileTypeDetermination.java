package fr.revoicechat.core.service.media;

import static fr.revoicechat.core.model.FileType.OTHER;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import fr.revoicechat.core.model.FileType;

class TestFileTypeDetermination {

  @ParameterizedTest
  @CsvSource(delimiterString = " -> ", textBlock = """
      file.jpg    -> PICTURE
      file.jpeg   -> PICTURE
      file.png    -> PICTURE
      file.gif    -> PICTURE
      file.webp   -> PICTURE
      file.avif   -> PICTURE
      file.apng   -> PICTURE
      file.ico    -> PICTURE
      file.svg    -> SVG
      file.mp4    -> VIDEO
      file.webm   -> VIDEO
      file.ogv    -> VIDEO
      file.avi    -> VIDEO
      file.mkv    -> VIDEO
      file.pdf    -> PDF
      file.txt    -> OTHER
      .gitignore  -> OTHER
      placeholder -> OTHER
      """)
  void test(String fileName, FileType type) {
    assertThat(new FileTypeDetermination().get(fileName)).isEqualTo(type);
  }

  @Test
  void testNull() {
    assertThat(new FileTypeDetermination().get(null)).isEqualTo(OTHER);
  }

}