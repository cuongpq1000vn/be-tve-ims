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
import vn.codezx.triviet.constants.CourseLevelConstants;
import vn.codezx.triviet.entities.course.Course;

@Repository
public interface CourseRepository extends JpaRepository<Course, Integer> {

  Optional<Course> findTop1ByCreatedAtBetweenOrderByCodeDesc(Date startOfDay, Date endOfDay);

  @Query("SELECT c FROM Course c WHERE c.courseLevel IN :levels AND c.isDelete = false")
  Page<Course> findByCourseLevelAndIsDeleteFalse(
      @Param("levels") List<CourseLevelConstants> levels,
      Pageable pageable
  );

  Course findByIdAndIsDeleteFalse(int courseId);

  Course findByCodeAndIsDeleteFalse(String code);

  Page<Course> findAllByIsDeleteFalse(Pageable pageable);

  @Query(
      value = "select c from Course c where (LOWER(c.code) LIKE %:searchString% OR c.name LIKE %:searchString%) AND c.isDelete = false")
  Page<Course> searchCourse(@Param("searchString") String searchString, Pageable pageable);

  @Query(
      value = "select c from Course c where (LOWER(c.code) LIKE %:searchString% OR c.name LIKE %:searchString%) AND c.courseLevel IN :level AND c.isDelete = false")
  Page<Course> findByCourseLevelAndCode(@Param("level") List<CourseLevelConstants> level,
      @Param("searchString") String searchString, Pageable pageable);
}
