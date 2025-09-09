package fr.revoicechat.core.web.openapi;

import java.util.stream.Collectors;

import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.media.Schema;
import org.eclipse.microprofile.openapi.models.media.Schema.SchemaType;

public class EnumFormatFilter implements OASFilter {

  @Override
  public Schema filterSchema(Schema schema) {

    if (isStringType(schema) && isEnum(schema)) {
      schema.setFormat("enum");
      schema.setPattern(schema.getEnumeration().stream().map(Object::toString).collect(Collectors.joining("|", "(", ")")));
    }
    return schema;
  }

  private static boolean isStringType(final Schema schema) {
    return schema.getType() != null && schema.getType().contains(SchemaType.STRING);
  }

  private static boolean isEnum(final Schema schema) {
    return schema.getEnumeration() != null && !schema.getEnumeration().isEmpty();
  }
}
