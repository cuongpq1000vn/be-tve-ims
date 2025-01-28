package vn.codezx.triviet.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Classification {
  GOOD("GOOD"), AVERAGE("AVERAGE"), FAILED("FAILED");

  private final String classification;

  public static Classification fromClassification(String classification) {
    for (Classification classificationName : Classification.values()) {
      if (classificationName.getClassification().equals(classification)) {
        return classificationName;
      }
    }

    return null;
  }
}
