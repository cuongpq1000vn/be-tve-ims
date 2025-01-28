package vn.codezx.triviet.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.codezx.triviet.entities.course.ClassDay;
import vn.codezx.triviet.entities.course.ClassTvms;
import vn.codezx.triviet.entities.reporting.Enrollment;
import vn.codezx.triviet.entities.student.Absence;
import vn.codezx.triviet.entities.student.Student;

@Repository
public interface AbsenceRepository extends JpaRepository<Absence, Integer> {

  Optional<Absence> findByStudentAndClassTvmsAndClassDayAndIsDeleteFalse(Student student,
      ClassTvms classTvms,
      ClassDay classDay);

  List<Absence> findByStudentAndIsDeleteFalse(Student student);

  @Query("SELECT a FROM Absence a WHERE a.classTvms.code = :classCode")
  Optional<List<Absence>> getListAbsenceByClassCode(@Param("classCode") String classCode);

}
