package vn.codezx.triviet.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TypeOfTest {
  MIDTERM("MIDTERM"), FINALTERM("FINALTERM"), RESULT("RESULT");

  private final String type;

  public static TypeOfTest fromType(String type) {
    for (TypeOfTest testType : TypeOfTest.values()) {
      if (testType.getType().equals(type)) {
        return testType;
      }
    }

    return null;
  }
}
