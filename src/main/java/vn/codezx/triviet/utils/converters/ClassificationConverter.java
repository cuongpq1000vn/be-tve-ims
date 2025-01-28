package vn.codezx.triviet.utils.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import vn.codezx.triviet.constants.Classification;

@Converter(autoApply = true)
public class ClassificationConverter implements AttributeConverter<Classification, String> {
  @Override
  public String convertToDatabaseColumn(Classification attribute) {
    if (attribute == null) {
      return null;
    }
    return attribute.name();
  }

  @Override
  public Classification convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return null;
    }
    return Classification.valueOf(dbData);
  }

}
