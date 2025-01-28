package vn.codezx.triviet.repositories;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.codezx.triviet.entities.course.ClassTvms;
import vn.codezx.triviet.entities.course.TestType;
import vn.codezx.triviet.entities.student.Grade;
import vn.codezx.triviet.entities.student.Student;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Integer> {

  Page<Grade> findByStudentAndIsDeleteFalse(Student student, Pageable pageable);

  Page<Grade> findByClassTvmsAndIsDeleteFalse(ClassTvms classTvms, Pageable pageable);

  Optional<Grade> findByIdAndIsDeleteFalse(int gradeId);

  Page<Grade> findByClassTvmsAndTestTypeAndIsDeleteFalse(ClassTvms classTvms, TestType testType,
      Pageable pageable);

  Page<Grade> findByStudentAndClassTvmsAndTestTypeAndIsDeleteFalse(Student student,
      ClassTvms classTvms, TestType testType, Pageable pageable);

  Page<Grade> findByStudentAndTestTypeAndIsDeleteFalse(Student student, TestType testType,
      Pageable pageable);

  Page<Grade> findByStudentAndClassTvmsAndIsDeleteFalse(Student student, ClassTvms classTvms,
      Pageable pageable);
}
