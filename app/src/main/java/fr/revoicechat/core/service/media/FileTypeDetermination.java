package fr.revoicechat.core.service.media;

import java.util.List;
import java.util.stream.Stream;
import jakarta.enterprise.context.ApplicationScoped;

import fr.revoicechat.core.model.FileType;

@ApplicationScoped
class FileTypeDetermination {

  FileType get(String fileName) {
    if (fileName == null) {
      return FileType.OTHER;
    }
    var file = fileName.split("\\.");
    var extension = file[file.length - 1];
    return Extension.of(extension);
  }

  private enum Extension {
    PICTURE(FileType.PICTURE, List.of("jpg", "jpeg", "png", "gif", "webp", "avif", "apng", "ico")),
    SVG(FileType.SVG, List.of("svg")),
    VIDEO(FileType.VIDEO, List.of("mp4", "webm", "ogv", "avi", "mkv")),
    PDF(FileType.PDF, List.of("pdf")),
    ;

    private final FileType fileType;
    private final List<String> extensions;

    Extension(final FileType fileType, final List<String> extensions) {
      this.fileType = fileType;
      this.extensions = extensions;
    }

    public static FileType of(final String extensionFile) {
      return Stream.of(values())
                   .filter(extension -> extension.extensions.contains(extensionFile.toLowerCase()))
                   .map(extension -> extension.fileType)
                   .findFirst()
                   .orElse(FileType.OTHER);
    }
  }
}
