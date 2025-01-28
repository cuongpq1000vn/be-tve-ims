package vn.codezx.triviet.repositories;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.codezx.triviet.entities.student.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, Integer> {

  Optional<Student> findTop1ByCreatedAtBetweenAndIsDeleteIsFalseOrderByCodeDesc(
      Date startOfDay, Date endOfDay);

  Optional<Student> findByCodeAndIsDeleteIsFalse(String studentCode);

  @Query(value = "SELECT COUNT(*) FROM STUDENT WHERE is_delete is false", nativeQuery = true)
  Integer getTotalNumberOfStudent();

  @Query("SELECT DISTINCT s FROM Student s WHERE "
      + "LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR "
      + "LOWER(s.nickname) LIKE LOWER(CONCAT('%', :query, '%')) OR "
      + "LOWER(s.emailAddress) LIKE LOWER(CONCAT('%', :query, '%')) OR "
      + "LOWER(s.phoneNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR "
      + "LOWER(s.code) LIKE LOWER(CONCAT('%', :query, '%')) AND (s.isDelete = false)")
  Page<Student> searchByQuery(@Param("query") String query, Pageable pageable);

  @Query("SELECT DISTINCT s FROM Student s WHERE "
      + "(LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR "
      + "LOWER(s.nickname) LIKE LOWER(CONCAT('%', :query, '%')) OR "
      + "LOWER(s.emailAddress) LIKE LOWER(CONCAT('%', :query, '%')) OR "
      + "LOWER(s.phoneNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR "
      + "LOWER(s.code) LIKE LOWER(CONCAT('%', :query, '%'))) AND "
      + "(s.avatarUrl IS NULL) AND (s.isDelete = false)")
  Page<Student> searchByQueryAndAvatarIsNull(@Param("query") String query, Pageable pageable);

  @Query("SELECT DISTINCT s FROM Student s WHERE "
      + "(LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR "
      + "LOWER(s.nickname) LIKE LOWER(CONCAT('%', :query, '%')) OR "
      + "LOWER(s.emailAddress) LIKE LOWER(CONCAT('%', :query, '%')) OR "
      + "LOWER(s.phoneNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR "
      + "LOWER(s.code) LIKE LOWER(CONCAT('%', :query, '%'))) AND "
      + "(s.avatarUrl IS NOT NULL) AND (s.isDelete = false)")
  Page<Student> searchByQueryAndAvatarIsNotNull(@Param("query") String query,
      Pageable pageable);

  @Query("SELECT s FROM Student s JOIN Enrollment e ON s.id = e.student.id WHERE e.classCode = :classCode AND e.isDelete = false AND s.isDelete = false")
  List<Student> findByClassCode(@Param("classCode") String classCode);

  Page<Student> findByAvatarUrlIsNullAndIsDeleteIsFalse(Pageable pageable);

  Page<Student> findByAvatarUrlIsNotNullAndIsDeleteIsFalse(Pageable pageable);

  Page<Student> findAllByIsDeleteIsFalse(Pageable pageable);

  List<Student> findAllByIdInAndIsDeleteIsFalse(List<Integer> id);

  @Query(value = "SELECT s FROM Student s WHERE s.isDelete = false AND s.id NOT IN (SELECT e.student.id FROM Enrollment e WHERE e.classCode IS NOT NULL AND e.isDelete = false)")
  List<Student> getPreEnrollmentStudent();

  Student findByCodeAndIsDeleteFalse(String studentCode);

  Optional<Student> findByIdAndIsDeleteFalse(int studentId);

  @Query(value = "SELECT s FROM Student s WHERE s.id IN :ids AND s.id NOT IN (SELECT st.id FROM ClassTvms c JOIN c.students st WHERE c.id = :classId)")
  List<Student> findNotIncludedStudents(@Param("ids") List<Integer> ids,
      @Param("classId") int classId);
}
