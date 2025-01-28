package vn.codezx.triviet.mappers.grade;

import org.springframework.stereotype.Component;
import vn.codezx.triviet.dtos.grade.GradeDTO;
import vn.codezx.triviet.entities.student.Grade;
import vn.codezx.triviet.mappers.DtoMapper;
import vn.codezx.triviet.mappers.classTvms.ClassTvmsToDTOMapper;
import vn.codezx.triviet.mappers.student.StudentToDTOMapper;

@Component
public class GradeToDTOMapper extends DtoMapper<Grade, GradeDTO> {

  private final StudentToDTOMapper studentToDTOMapper;

  private final ClassTvmsToDTOMapper classTvmsToDTOMapper;

  private final SkillToDTOMapper skillToDTOMapper;

  public GradeToDTOMapper(StudentToDTOMapper studentToDTOMapper,
      ClassTvmsToDTOMapper classTvmsToDTOMapper, SkillToDTOMapper skillToDTOMapper) {
    this.studentToDTOMapper = studentToDTOMapper;
    this.classTvmsToDTOMapper = classTvmsToDTOMapper;
    this.skillToDTOMapper = skillToDTOMapper;
  }

  @Override
  public GradeDTO toDto(Grade entity) {
    return GradeDTO.builder().id(entity.getId())
        .student(studentToDTOMapper.toDto(entity.getStudent()))
        .classTvms(classTvmsToDTOMapper.toDto(entity.getClassTvms())).testType(entity.getTestType())
        .comment(entity.getComment()).skills(skillToDTOMapper.toListDto(entity.getSkills()))
        .score(entity.getScore()).classification(entity.getClassification())
        .createdAt(entity.getCreatedAt()).updatedAt(entity.getUpdatedAt())
        .createdBy(entity.getCreatedBy()).updatedBy(entity.getUpdatedBy()).build();
  }
}
