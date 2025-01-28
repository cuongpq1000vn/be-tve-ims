package vn.codezx.triviet.entities.student;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import vn.codezx.triviet.constants.Classification;
import vn.codezx.triviet.entities.base.BaseInfo;
import vn.codezx.triviet.entities.course.ClassTvms;
import vn.codezx.triviet.entities.course.TestType;
import vn.codezx.triviet.entities.setting.Skill;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Data
@EqualsAndHashCode(callSuper = true)
@Table(name = "grade",
    uniqueConstraints = @UniqueConstraint(columnNames = {"student_id", "class_id", "test_type_id"}))
public class Grade extends BaseInfo {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private int id;

  @ManyToOne
  @JoinColumn(name = "student_id", nullable = false)
  private Student student;

  @ManyToOne
  @JoinColumn(name = "class_id", nullable = false)
  private ClassTvms classTvms;

  @ManyToOne
  @JoinColumn(name = "test_type_id", nullable = false)
  private TestType testType;

  @Column(name = "comment")
  private String comment;

  @Column(name = "score")
  private Float score;

  @Column(name = "classification")
  @Enumerated(EnumType.STRING)
  private Classification classification;

  @OneToMany(mappedBy = "grade", cascade = {CascadeType.ALL}, orphanRemoval = true)
  @Builder.Default
  private List<Skill> skills = new ArrayList<>();
}
