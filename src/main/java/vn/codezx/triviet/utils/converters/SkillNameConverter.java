package vn.codezx.triviet.utils.converters;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import vn.codezx.triviet.constants.SkillName;

@Converter(autoApply = true)
public class SkillNameConverter implements AttributeConverter<SkillName, String> {
  @Override
  public String convertToDatabaseColumn(SkillName attribute) {
    if (attribute == null) {
      return null;
    }
    return attribute.name();
  }

  @Override
  public SkillName convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return null;
    }
    return SkillName.valueOf(dbData);
  }
}
