package vn.codezx.triviet.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SkillName {
  LISTENING("LISTENING"), SPEAKING("SPEAKING"), READING("READING"), WRITING("WRITING"), BONUS(
      "BONUS");

  private final String skill;

  public static SkillName fromName(String skill) {
    for (SkillName skillName : SkillName.values()) {
      if (skillName.getSkill().equals(skill)) {
        return skillName;
      }
    }

    return null;
  }
}
