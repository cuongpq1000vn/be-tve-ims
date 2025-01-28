package vn.codezx.triviet.mappers.reports;

import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.util.Calendar;
import java.util.Objects;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.codezx.triviet.constants.MessageCode;
import vn.codezx.triviet.dtos.reports.TestDayReportDTO;
import vn.codezx.triviet.entities.course.ClassDay;
import vn.codezx.triviet.exceptions.TveException;
import vn.codezx.triviet.mappers.DtoMapper;
import vn.codezx.triviet.repositories.ScheduleRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClassDayToTestDayReportDTO extends DtoMapper<ClassDay, TestDayReportDTO> {

    private final ScheduleRepository scheduleRepository;

    @Override
    public TestDayReportDTO toDto(ClassDay entity) {
        String pattern = "dd/MM/yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Calendar cal = Calendar.getInstance();
        cal.setTime(entity.getClassDate());
        var dow = DayOfWeek.of(cal.get(Calendar.DAY_OF_WEEK) - 1);

        var location = new StringBuilder();
        var schedule = scheduleRepository.findScheduleFromInfo(dow, entity.getStartTime(),
                entity.getEndTime());

        if (!schedule.isPresent()) {
            throw new TveException(MessageCode.MESSAGE_SCHE_NOT_FOUND,
                    String.valueOf(entity.getId()));
        }

        if (!Objects.isNull(entity.getLocation())) {
            location.append(entity.getLocation().getBranch()).append(" ")
                    .append(entity.getLocation().getRoom());
        }

        return TestDayReportDTO.builder().name(entity.getClassTvms().getName())
                .testDate(simpleDateFormat.format(entity.getClassDate()))
                .schedule(schedule.get().getCode())
                .testType(entity.getLesson().getLessonType().getType().name())
                .location(location.toString()).build();
    }

}
