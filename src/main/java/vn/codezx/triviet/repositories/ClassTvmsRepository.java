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
import vn.codezx.triviet.entities.course.ClassTvms;
import vn.codezx.triviet.entities.course.Course;


@Repository
public interface ClassTvmsRepository extends JpaRepository<ClassTvms, Integer> {

  @Query(value = "SELECT COUNT(*) FROM CLASS_TVMS WHERE is_delete is false", nativeQuery = true)
  Integer getTotalNumberOfClass();

  Optional<ClassTvms> findTop1ByCreatedAtBetweenAndIsDeleteIsFalseOrderByCodeDesc(Date startOfDay,
      Date endOfDay);

  Optional<ClassTvms> findTop1ByCreatedAtBetweenOrderByCodeDesc(Date startOfDay, Date endOfDay);

  boolean existsByCourseId(int courseId);

  Optional<ClassTvms> findByIdAndIsDeleteFalse(int id);

  Optional<ClassTvms> findByCodeAndIsDeleteFalse(String code);

  @Query("SELECT c FROM ClassTvms c WHERE c.code IN :codes AND c.isDelete = false")
  List<ClassTvms> findAllByCodeInAndIsDeleteFalse(@Param("codes") List<String> codes);

  Optional<ClassTvms> findByNameAndIsDeleteFalse(String name);

  @Query(
      value = "select c from ClassTvms c where (c.code like %:searchString% or c.name like %:searchString%) and c.isDelete = false")
  Page<ClassTvms> searchClass(@Param("searchString") String searchString, Pageable pageable);

  @Query(nativeQuery = true, value = """
        select distinct on (ct.id) ct.*
        from class_tvms ct
        inner join class_day cd
        on ct.id = cd.class_id
        where cd.class_date >= CURRENT_DATE
        and ct.start_date <= CURRENT_DATE
        and (ct.code like concat('%', :searchString, '%')
        or ct.class_name like concat('%', :searchString, '%'))
        and ct.is_delete = false
      """)
  Page<ClassTvms> searchOnGoingClass(@Param("searchString") String searchString, Pageable pageable);

  @Query(nativeQuery = true, value = """
        select distinct on (ct.id) ct.*
        from class_tvms ct
        inner join class_day cd
        on ct.id = cd.class_id
        where ct.start_date > CURRENT_DATE
        and (ct.code like concat('%', :searchString, '%')
        or ct.class_name
        like concat('%', :searchString, '%'))
        and ct.is_delete = false
      """)
  Page<ClassTvms> searchNewClass(@Param("searchString") String searchString, Pageable pageable);

  @Query(nativeQuery = true, value = """
        select distinct on (ct.id) ct.*
        from class_tvms ct
        inner join class_day cd
        on ct.id = cd.class_id
        where cd.class_date < CURRENT_DATE
        and (ct.code like concat('%', :searchString, '%')
        or ct.class_name
        like concat('%', :searchString, '%'))
        and ct.is_delete = false
      """)
  Page<ClassTvms> searchEndedClass(@Param("searchString") String searchString, Pageable pageable);

  List<ClassTvms> findAllByCourseAndIsDeleteFalse(Course course);
}
