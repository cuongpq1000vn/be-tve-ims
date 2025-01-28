package vn.codezx.triviet.services.impl;

import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import vn.codezx.triviet.constants.ClassStatus;
import vn.codezx.triviet.constants.InvoiceStatus;
import vn.codezx.triviet.constants.MessageCode;
import vn.codezx.triviet.dtos.classTvms.ClassDTO;
import vn.codezx.triviet.dtos.classTvms.CreateClassDTO;
import vn.codezx.triviet.dtos.classTvms.UpdateClassDTO;
import vn.codezx.triviet.entities.course.ClassTvms;
import vn.codezx.triviet.entities.reporting.Enrollment;
import vn.codezx.triviet.entities.reporting.Invoice;
import vn.codezx.triviet.exceptions.TveException;
import vn.codezx.triviet.mappers.classTvms.ClassTvmsToDTOMapper;
import vn.codezx.triviet.repositories.ClassDayRepository;
import vn.codezx.triviet.repositories.ClassTvmsRepository;
import vn.codezx.triviet.repositories.CourseRepository;
import vn.codezx.triviet.repositories.EnrollmentRepository;
import vn.codezx.triviet.repositories.InvoiceRepository;
import vn.codezx.triviet.repositories.ScheduleRepository;
import vn.codezx.triviet.repositories.StaffRepository;
import vn.codezx.triviet.repositories.StudentRepository;
import vn.codezx.triviet.services.ClassDayService;
import vn.codezx.triviet.services.ClassTvmsService;
import vn.codezx.triviet.utils.DateTimeUtil;
import vn.codezx.triviet.utils.LogUtil;
import vn.codezx.triviet.utils.MessageUtil;
import vn.codezx.triviet.utils.StringUtil;

@Service
@Slf4j
@RequiredArgsConstructor
public class ClassTvmsServiceImpl implements ClassTvmsService {

  private final ClassTvmsToDTOMapper classTvmsToDTOMapper;
  private final CourseRepository courseRepository;
  private final ScheduleRepository scheduleRepository;
  private final ClassTvmsRepository classTvmsRepository;
  private final MessageUtil messageUtil;
  private final EnrollmentRepository enrollmentRepository;
  private final StudentRepository studentRepository;
  private final ClassDayService classDayService;
  private final ClassDayRepository classDayRepository;
  private final StaffRepository staffRepository;
  private final InvoiceRepository invoiceRepository;

  @Override
  @Transactional
  public ClassDTO createClass(String requestId, CreateClassDTO classRequest) {
    var schedules =
        scheduleRepository.findAllByIdInAndIsDeleteIsFalse(classRequest.getScheduleIds());

    if (schedules.size() != classRequest.getScheduleIds().size()) {
      var message = LogUtil.buildFormatLog(requestId, messageUtil
          .getMessage(MessageCode.MESSAGE_SCHE_NOT_FOUND, classRequest.getScheduleIds()));
      log.error(message);
      throw new TveException(MessageCode.MESSAGE_SCHE_NOT_FOUND.getCode(), message);
    }

    var courseOptional = courseRepository.findById(classRequest.getCourseId());

    if (!courseOptional.isPresent()) {
      var message = LogUtil.buildFormatLog(requestId,
          messageUtil.getMessage(MessageCode.MESSAGE_NOT_FOUND, classRequest.getCourseId()));
      log.error(message);
      throw new TveException(MessageCode.MESSAGE_NOT_FOUND.getCode(), message);
    }

    var staffOptional = staffRepository.findByIdAndIsDeleteFalse(classRequest.getStaffId());

    if (!staffOptional.isPresent()) {
      var message = LogUtil.buildFormatLog(requestId,
          messageUtil.getMessage(MessageCode.MESSAGE_STAFF_NOT_FOUND, classRequest.getStaffId()));
      log.error(message);
      throw new TveException(MessageCode.MESSAGE_STAFF_NOT_FOUND.getCode(), message);
    }

    var classOptional = classTvmsRepository.findByNameAndIsDeleteFalse(classRequest.getClassName());

    if (classOptional.isPresent()) {
      var message = LogUtil.buildFormatLog(requestId, messageUtil
          .getMessage(MessageCode.MESSAGE_CLA_NAME_DULICATED, classRequest.getCourseId()));
      log.error(message);
      throw new TveException(MessageCode.MESSAGE_CLA_NAME_DULICATED.getCode(), message);
    }
    var course = courseOptional.get();

    var students = studentRepository.findAllByIdInAndIsDeleteIsFalse(classRequest.getStudentIds());
    if (students.size() != classRequest.getStudentIds().size()) {
      var message = LogUtil.buildFormatLog(requestId,
          messageUtil.getMessage(MessageCode.MESSAGE_STU_NOT_FOUND, classRequest.getStudentIds()));
      log.error(message);
      throw new TveException(MessageCode.MESSAGE_STU_NOT_FOUND.getCode(), message);
    }
    var classTvms = classTvmsRepository.saveAndFlush(ClassTvms.builder().staff(staffOptional.get())
        .name(classRequest.getClassName()).code(generateClassCode()).course(course)
        .schedules(schedules).students(students).startDate(classRequest.getStartDate()).build());

    List<Enrollment> enrollments = new ArrayList<>();
    List<Invoice> invoices = new ArrayList<>();
    students.forEach((student) -> {
      Enrollment enrollment = Enrollment.builder()
          .classCode(classTvms.getCode())
          .course(course)
          .student(student)
          .enrollmentDate(new Date())
          .build();
      enrollments.add(enrollment);
      Invoice invoice = Invoice.builder()
          .enrollment(enrollment)
          .tuitionOwed(enrollment.getCourse().getTuitionRate())
          .invoiceStatus(InvoiceStatus.NOT_PAID)
          .build();
      invoices.add(invoice);
    });

    enrollmentRepository.saveAll(enrollments);
    invoiceRepository.saveAll(invoices);
    classDayService.generateClassDay(requestId, classTvms);
    return classTvmsToDTOMapper.toDto(classTvms);
  }

  @Override
  public ClassDTO getClass(String requestId, int classId) {
    var classOptional = classTvmsRepository.findByIdAndIsDeleteFalse(classId);

    if (!classOptional.isPresent()) {
      var message = LogUtil.buildFormatLog(requestId,
          messageUtil.getMessage(MessageCode.MESSAGE_CLA_NOTFOUND, classId));
      log.error(message);
      throw new TveException(MessageCode.MESSAGE_CLA_NOTFOUND.getCode(), message);
    }

    return classTvmsToDTOMapper.toDto(classOptional.get());
  }

  @Override
  public Page<ClassDTO> getClass(String requestId, String searchString, ClassStatus status,
      Pageable pageable) {
    if (Objects.isNull(status)) {
      return classTvmsRepository.searchClass(searchString, pageable)
          .map(classTvmsToDTOMapper::toDto);
    }

    if (status == ClassStatus.NEW) {
      return classTvmsRepository.searchNewClass(searchString, pageable)
          .map(classTvmsToDTOMapper::toDto);
    }
    if (status == ClassStatus.ON_GOING) {
      return classTvmsRepository.searchOnGoingClass(searchString, pageable)
          .map(classTvmsToDTOMapper::toDto);
    }

    return classTvmsRepository.searchEndedClass(searchString, pageable)
        .map(classTvmsToDTOMapper::toDto);
  }

  @Override
  @Transactional
  public ClassDTO updateClass(String requestId, int classId, UpdateClassDTO updateClassDTO) {
    var classOptional = classTvmsRepository.findByIdAndIsDeleteFalse(classId);

    if (!classOptional.isPresent()) {
      var message = LogUtil.buildFormatLog(requestId,
          messageUtil.getMessage(MessageCode.MESSAGE_CLA_NOTFOUND, classId));
      log.error(message);
      throw new TveException(MessageCode.MESSAGE_CLA_NOTFOUND.getCode(), message);
    }

    var classEntity = classOptional.get();

    if (classEntity.getStartDate().getTime() <= new Date().getTime()) {
      classEntity.setStartDate(updateClassDTO.getStartDate());
      classEntity.setName(updateClassDTO.getClassName());

      var schedules =
          scheduleRepository.findAllByIdInAndIsDeleteIsFalse(updateClassDTO.getScheduleIds());

      if (schedules.size() != updateClassDTO.getScheduleIds().size()) {
        var message = LogUtil.buildFormatLog(requestId, messageUtil
            .getMessage(MessageCode.MESSAGE_SCHE_NOT_FOUND, updateClassDTO.getScheduleIds()));
        log.error(message);
        throw new TveException(MessageCode.MESSAGE_SCHE_NOT_FOUND.getCode(), message);
      }

      classEntity.setSchedules(schedules);

      classDayRepository.deleteAll(classEntity.getClassDays());

      classDayService.generateClassDay(requestId, classEntity);

      var staffOptional = staffRepository.findByIdAndIsDeleteFalse(updateClassDTO.getStaffId());

      if (!staffOptional.isPresent()) {
        var message = LogUtil.buildFormatLog(requestId, messageUtil
            .getMessage(MessageCode.MESSAGE_STAFF_NOT_FOUND, updateClassDTO.getStaffId()));
        log.error(message);
        throw new TveException(MessageCode.MESSAGE_STAFF_NOT_FOUND.getCode(), message);
      }
    }
    var students =
        studentRepository.findNotIncludedStudents(updateClassDTO.getStudentIds(), classId);

    classEntity.getStudents().addAll(students);
    classTvmsRepository.save(classEntity);
    List<Enrollment> enrollments = new ArrayList<>();
    List<Invoice> invoices = new ArrayList<>();
    students.forEach((student) -> {
      Enrollment enrollment = Enrollment.builder()
          .classCode(classEntity.getCode())
          .course(classEntity.getCourse())
          .student(student)
          .enrollmentDate(new Date())
          .build();

      Invoice invoice = Invoice.builder()
          .enrollment(enrollment)
          .tuitionOwed(enrollment.getCourse().getTuitionRate())
          .invoiceStatus(InvoiceStatus.NOT_PAID)
          .build();
      invoices.add(invoice);
      enrollments.add(enrollment);
    });

    enrollmentRepository.saveAll(enrollments);
    invoiceRepository.saveAll(invoices);

    return classTvmsToDTOMapper.toDto(classEntity);
  }

  @Override
  public void deleteClass(String requestId, int classId) {
    var classOptional = classTvmsRepository.findByIdAndIsDeleteFalse(classId);

    if (!classOptional.isPresent()) {
      var message = LogUtil.buildFormatLog(requestId,
          messageUtil.getMessage(MessageCode.MESSAGE_CLA_NOTFOUND, classId));
      log.error(message);
      throw new TveException(MessageCode.MESSAGE_CLA_NOTFOUND.getCode(), message);
    }

    var classEntity = classOptional.get();
    classEntity.setIsDelete(true);
    classTvmsRepository.save(classEntity);
  }

  private String generateClassCode() {
    Optional<ClassTvms> latestStudentInDay =
        classTvmsRepository.findTop1ByCreatedAtBetweenOrderByCodeDesc(DateTimeUtil.startOfDay(),
            DateTimeUtil.endOfDay());

    if (latestStudentInDay.isPresent()) {
      String latestStudentCode = latestStudentInDay.get().getCode();
      Integer number =
          Integer.parseInt(latestStudentCode.substring(latestStudentCode.length() - 3));
      return StringUtil.generateCode("CLA", number + 1);
    } else {
      return StringUtil.generateCode("CLA", 1);
    }
  }

  @Override
  public ClassDTO getClass(String requestId, String classCode) {
    var classOptional = classTvmsRepository.findByCodeAndIsDeleteFalse(classCode);

    if (!classOptional.isPresent()) {
      var message = LogUtil.buildFormatLog(requestId,
          messageUtil.getMessage(MessageCode.MESSAGE_CLA_NOTFOUND, classCode));
      log.error(message);
      throw new TveException(MessageCode.MESSAGE_CLA_NOTFOUND.getCode(), message);
    }

    return classTvmsToDTOMapper.toDto(classOptional.get());
  }

  @Override
  public Integer getTotalClass(String requestId) {
    return classTvmsRepository.getTotalNumberOfClass();
  }
}
