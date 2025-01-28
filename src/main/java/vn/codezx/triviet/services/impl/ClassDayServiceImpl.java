package vn.codezx.triviet.services.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.codezx.triviet.constants.MessageCode;
import vn.codezx.triviet.constants.TypeOfTest;
import vn.codezx.triviet.dtos.classDay.ClassDayDTO;
import vn.codezx.triviet.dtos.classDay.ClassDayRequest;
import vn.codezx.triviet.dtos.classDay.UpdateClassDayDTO;
import vn.codezx.triviet.dtos.fileexport.DownloadDTO;
import vn.codezx.triviet.entities.course.ClassDay;
import vn.codezx.triviet.entities.course.ClassTvms;
import vn.codezx.triviet.entities.setting.Location;
import vn.codezx.triviet.entities.setting.Schedule;
import vn.codezx.triviet.entities.staff.Staff;
import vn.codezx.triviet.exceptions.TveException;
import vn.codezx.triviet.mappers.ClassDay.ClassDayToDTOMapper;
import vn.codezx.triviet.mappers.reports.ClassDayToTestDayReportDTO;
import vn.codezx.triviet.repositories.ClassDayRepository;
import vn.codezx.triviet.repositories.HolidayScheduleRepository;
import vn.codezx.triviet.repositories.LocationRepository;
import vn.codezx.triviet.repositories.ScheduleRepository;
import vn.codezx.triviet.repositories.StaffRepository;
import vn.codezx.triviet.services.ClassDayService;
import vn.codezx.triviet.services.FileExportService;
import vn.codezx.triviet.utils.LogUtil;
import vn.codezx.triviet.utils.MessageUtil;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClassDayServiceImpl implements ClassDayService {

  private final ClassDayRepository classDayRepository;
  private final ClassDayToDTOMapper classDayToDTOMapper;
  private final MessageUtil messageUtil;
  private final HolidayScheduleRepository holidayScheduleRepository;

  private final StaffRepository staffRepository;
  private final LocationRepository locationRepository;
  private final ScheduleRepository scheduleRepository;
  private final FileExportService fileExportService;
  private final ClassDayToTestDayReportDTO classDayToTestDayReportDTO;

  @Override
  public ClassDayDTO getClassDayById(String requestId, Integer classDayId) {
    Optional<ClassDay> classDay = classDayRepository.findById(classDayId);
    if (classDay.isEmpty()) {
      throw new TveException(requestId,
          messageUtil.getMessage(MessageCode.MESSAGE_CLASS_DAY_NOT_FOUND));
    }
    return classDayToDTOMapper.toDto(classDay.get());
  }

  @Override
  @Transactional
  public ClassDayDTO updateClassDayById(String requestId, Integer classDayId,
      ClassDayRequest classDayRequest) {
    Optional<ClassDay> classDay = classDayRepository.findById(classDayId);
    if (classDay.isEmpty()) {
      throw new TveException(requestId,
          messageUtil.getMessage(MessageCode.MESSAGE_CLASS_DAY_NOT_FOUND));
    }
    ClassDay entity = classDay.get();
    if (classDayRequest.getComment() != null) {
      entity.setComment(classDayRequest.getComment());
    }
    if (classDayRequest.getHomework() != null) {
      entity.setHomeWork(classDayRequest.getHomework());
    }
    if (classDayRequest.getRating() != null) {
      entity.setRating(classDayRequest.getRating());
    }

    return classDayToDTOMapper.toDto(entity);
  }

  @Override
  @Transactional
  public List<ClassDayDTO> generateClassDay(String requestId, ClassTvms classTvms) {
    var holidays = holidayScheduleRepository.findByOverLappingDays(classTvms.getStartDate())
        .stream().map(d -> {
          List<Long> dates = new ArrayList<>();
          dates.add(d.getStartDate().getTime());
          dates.add(d.getEndDate().getTime());
          return dates;
        }).toList();

    var classDays = getDays(requestId, classTvms, holidays);

    if (classDays.isEmpty()) {
      return new ArrayList<>();
    }
    return classDayToDTOMapper.toListDto(classDayRepository.saveAll(classDays));
  }

  private List<ClassDay> getDays(String requestId, ClassTvms classTvms, List<List<Long>> holidays) {
    var course = classTvms.getCourse();

    var lessons = course.getLessons();
    var schedules = classTvms.getSchedules();
    schedules.sort(this::comparingSchedule);


    var currentDay = classTvms.getStartDate().getTime();
    var currentSchedule = getStartSchedule(schedules, classTvms.getStartDate());

    if (currentSchedule == -1) {
      throw new TveException(MessageCode.MESSAGE_CONFLICT_ERROR,
          messageUtil.getMessage(MessageCode.MESSAGE_CONFLICT_ERROR), requestId);
    }

    var currentHoliday = 0;
    var currentLesson = 0;
    var diffs = new ArrayList<Long>();

    for (int i = 1; i < schedules.size(); i++) {
      diffs.add((schedules.get(i).getDayOfWeek().getValue()
          - schedules.get(i - 1).getDayOfWeek().getValue()) * TimeUnit.DAYS.toMillis(1));

      if (i == schedules.size() - 1) {
        diffs.add((schedules.get(0).getDayOfWeek().getValue() + 7
            - schedules.get(i).getDayOfWeek().getValue()) * TimeUnit.DAYS.toMillis(1));
      }
    }

    var classDays = new ArrayList<ClassDay>();

    while (currentLesson < lessons.size()) {
      if (!holidays.isEmpty()) {
        var holi = holidays.get(currentHoliday);

        if (currentDay > holi.get(1) && currentHoliday < holidays.size() - 1) {
          currentHoliday++;
        }

        if (currentDay >= holi.get(0) && currentDay <= holi.get(1)) {
          currentDay += diffs.get(currentSchedule);
          currentSchedule++;
          if (currentSchedule == schedules.size()) {
            currentSchedule = 0;
          }
          continue;
        }
      }
      classDays.add(ClassDay.builder().lesson(lessons.get(currentLesson))
          .classDate(new Date(currentDay)).startTime(schedules.get(currentSchedule).getStartTime())
          .endTime(schedules.get(currentSchedule).getEndTime()).classTvms(classTvms).build());
      currentLesson++;
      currentDay += diffs.get(currentSchedule);
      currentSchedule++;
      if (currentSchedule == schedules.size()) {
        currentSchedule = 0;
      }
    }

    return classDays;
  }

  private int comparingSchedule(Schedule s1, Schedule s2) {
    var o1 = s1.getDayOfWeek().compareTo(s2.getDayOfWeek());

    if (o1 != 0) {
      return o1;
    }

    return s1.getStartTime().compareTo(s2.getStartTime());
  }

  private int getDOW(Date date) {
    Calendar c = Calendar.getInstance();
    c.setTime(date);
    return c.get(Calendar.DAY_OF_WEEK);
  }

  private int getStartSchedule(List<Schedule> schedules, Date date) {
    int dow = getDOW(date) - 1;
    for (int i = 0; i < schedules.size(); i++) {
      var sDow = schedules.get(i).getDayOfWeek().getValue();
      if (sDow == dow) {
        return i;
      }
    }

    return -1;
  }

  @Override
  @Transactional
  public ClassDayDTO updateClassDay(String requestId, UpdateClassDayDTO updateClassDayDTO) {
    Staff teacher = null;
    Location location = null;
    Schedule schedule = null;

    if (!Objects.isNull(updateClassDayDTO.getLocationId())) {
      var optional =
          locationRepository.findByIdAndIsDeleteIsFalse(updateClassDayDTO.getLocationId());

      if (!optional.isPresent()) {
        var message = LogUtil.buildFormatLog(requestId, messageUtil
            .getMessage(MessageCode.MESSAGE_LOC_NOT_FOUND, updateClassDayDTO.getLocationId()));
        log.error(message);
        throw new TveException(MessageCode.MESSAGE_LOC_NOT_FOUND.getCode(), message);
      }
      location = optional.get();
    }

    if (!Objects.isNull(updateClassDayDTO.getTeacherId())) {
      var optional = staffRepository.findByIdAndIsDeleteFalse(updateClassDayDTO.getLocationId());

      if (!optional.isPresent()) {
        var message = LogUtil.buildFormatLog(requestId, messageUtil
            .getMessage(MessageCode.MESSAGE_STAFF_NOT_FOUND, updateClassDayDTO.getLocationId()));
        log.error(message);
        throw new TveException(MessageCode.MESSAGE_STAFF_NOT_FOUND.getCode(), message);
      }
      teacher = optional.get();
    }

    if (!Objects.isNull(updateClassDayDTO.getScheduleId())) {
      var optional =
          scheduleRepository.findByIdAndIsDeleteIsFalse(updateClassDayDTO.getScheduleId());

      if (!optional.isPresent()) {
        var message = LogUtil.buildFormatLog(requestId, messageUtil
            .getMessage(MessageCode.MESSAGE_SCHE_NOT_FOUND, updateClassDayDTO.getLocationId()));
        log.error(message);
        throw new TveException(MessageCode.MESSAGE_SCHE_NOT_FOUND.getCode(), message);
      }
      schedule = optional.get();
    }

    var optional = classDayRepository.findByIdAndIsDeleteIsFalse(updateClassDayDTO.getId());

    if (!optional.isPresent()) {
      var message = LogUtil.buildFormatLog(requestId, messageUtil
          .getMessage(MessageCode.MESSAGE_CLASS_DAY_NOT_FOUND, updateClassDayDTO.getLocationId()));
      log.error(message);
      throw new TveException(MessageCode.MESSAGE_CLASS_DAY_NOT_FOUND.getCode(), message);
    }
    var classDay = optional.get();

    if (!Objects.isNull(location)) {
      classDay.setLocation(location);
    }

    if (!Objects.isNull(teacher)) {
      classDay.setTeacher(teacher);
    }

    if (!Objects.isNull(schedule)) {
      if (Objects.isNull(updateClassDayDTO.getClassDate())) {
        var message = LogUtil.buildFormatLog(requestId, messageUtil
            .getMessage(MessageCode.MESSAGE_ERROR_INPUT_ERROR, updateClassDayDTO.getLocationId()));
        log.error(message);
        throw new TveException(MessageCode.MESSAGE_ERROR_INPUT_ERROR.getCode(), message);
      }

      var dow = getDOW(updateClassDayDTO.getClassDate()) - 1;

      if (dow != schedule.getDayOfWeek().getValue()) {
        var message = LogUtil.buildFormatLog(requestId, messageUtil
            .getMessage(MessageCode.MESSAGE_ERROR_INPUT_ERROR, updateClassDayDTO.getLocationId()));
        log.error(message);
        throw new TveException(MessageCode.MESSAGE_ERROR_INPUT_ERROR.getCode(), message);
      }

      classDay.setClassDate(updateClassDayDTO.getClassDate());
      classDay.setStartTime(schedule.getStartTime());
      classDay.setEndTime(schedule.getEndTime());
    }

    return classDayToDTOMapper.toDto(classDayRepository.save(classDay));
  }

  @Override
  public DownloadDTO testDayReport(String requestId, Date from, Date to) {
    var days = classDayRepository.findTestDaysInRange(from, to,
        Arrays.asList(TypeOfTest.MIDTERM, TypeOfTest.FINALTERM));

    return fileExportService.exportXLSX(requestId, classDayToTestDayReportDTO.toListDto(days),
        "test_days");
  }
}
