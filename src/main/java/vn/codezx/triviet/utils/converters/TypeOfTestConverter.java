package vn.codezx.triviet.utils.converters;

import java.util.stream.Stream;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import vn.codezx.triviet.constants.MessageCode;
import vn.codezx.triviet.constants.TypeOfTest;
import vn.codezx.triviet.exceptions.TveException;

@Converter(autoApply = true)
public class TypeOfTestConverter implements AttributeConverter<TypeOfTest, String> {
  @Override
  public String convertToDatabaseColumn(TypeOfTest testType) {
    if (testType == null)
      return null;

    return testType.getType();
  }

  @Override
  public TypeOfTest convertToEntityAttribute(String dbData) {
    if (dbData == null) {
      return null;
    }

    return Stream.of(TypeOfTest.values()).filter(d -> d.getType().equals(dbData)).findFirst()
        .orElseThrow(() -> {
          return new TveException(MessageCode.MESSAGE_ERROR_SYSTEM_ERROR, "Illegal Argument");
        });
  }

}
