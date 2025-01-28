package vn.codezx.triviet.dtos.lesson;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.codezx.triviet.constants.TypeOfTest;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class LessonRequest {
  private String courseId;
  private String description;
  private TypeOfTest lessonType;
}
