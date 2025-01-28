package vn.codezx.triviet.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.codezx.triviet.entities.reporting.Enrollment;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {

  @Query("SELECT l " + "FROM Enrollment l " + "JOIN l.course c "
      + "WHERE c.code = :courseCode AND c.isDelete = false AND l.isDelete = false")
  List<Enrollment> findEnrollmentsByCourseCodeAndIsDeleteFalse(
      @Param("courseCode") String courseCode);

  @Query("SELECT l " + "FROM Enrollment l " + "JOIN l.course c "
      + "WHERE c.code = :courseCode AND c.isDelete = false AND l.isDelete = false AND l.classCode = :classCode")
  List<Enrollment> findEnrollmentsByCourseCodeAndClassCode(@Param("courseCode") String courseCode,
      @Param("classCode") String classCode);

  Enrollment findByIdAndIsDeleteFalse(int enrollmentId);

  @Query("SELECT e FROM Enrollment e WHERE e.student.code = :studentCode")
  Optional<List<Enrollment>> findEnrollmentByStudentCode(@Param("studentCode") String studentCode);

  @Query(nativeQuery = true,
      value = "select * from enrollment where student_id in (:studentIds) and course_id = :courseId")
  List<Enrollment> findStudentInClass(@Param("courseId") int courseId,
      @Param("studentIds") List<Integer> studentIds);
}
