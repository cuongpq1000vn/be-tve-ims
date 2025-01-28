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
import vn.codezx.triviet.entities.setting.HolidaySchedule;

@Repository
public interface HolidayScheduleRepository extends JpaRepository<HolidaySchedule, Integer> {

    Page<HolidaySchedule> findAllByIsDeleteIsFalse(Pageable pageable);

    Optional<HolidaySchedule> findByIdAndIsDeleteIsFalse(Integer holidayScheduleId);

    @Query(value = "select s from HolidaySchedule s where s.startDate >= :startDate or s.endDate >= :startDate")
    List<HolidaySchedule> findByOverLappingDays(@Param("startDate") Date startDay);
}
