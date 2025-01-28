package vn.codezx.triviet.services.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import vn.codezx.triviet.constants.MessageCode;
import vn.codezx.triviet.dtos.enrollment.EnrollmentDTO;
import vn.codezx.triviet.dtos.enrollment.EnrollmentRequest;
import vn.codezx.triviet.entities.course.ClassTvms;
import vn.codezx.triviet.entities.course.Course;
import vn.codezx.triviet.entities.reporting.Enrollment;
import vn.codezx.triviet.entities.student.Student;
import vn.codezx.triviet.exceptions.TveException;
import vn.codezx.triviet.mappers.enrollment.EnrollmentToDTOMapper;
import vn.codezx.triviet.repositories.ClassTvmsRepository;
import vn.codezx.triviet.repositories.CourseRepository;
import vn.codezx.triviet.repositories.EnrollmentRepository;
import vn.codezx.triviet.repositories.StudentRepository;
import vn.codezx.triviet.services.EnrollmentService;
import vn.codezx.triviet.utils.MessageUtil;

@Service
@Slf4j
public class EnrollmentServiceImpl implements EnrollmentService {

  private final StudentRepository studentRepository;
  private final CourseRepository courseRepository;
  private final EnrollmentRepository enrollmentRepository;
  private final EnrollmentToDTOMapper enrollmentToDTOMapper;

  private final ClassTvmsRepository classTvmsRepository;
  private final MessageUtil messageUtil;

  @Autowired
  public EnrollmentServiceImpl(StudentRepository studentRepository,
      CourseRepository courseRepository, EnrollmentRepository enrollmentRepository,
      EnrollmentToDTOMapper enrollmentToDTOMapper, MessageUtil messageUtil,
      ClassTvmsRepository classTvmsRepository) {
    this.classTvmsRepository = classTvmsRepository;
    this.enrollmentToDTOMapper = enrollmentToDTOMapper;
    this.messageUtil = messageUtil;
    this.courseRepository = courseRepository;
    this.studentRepository = studentRepository;
    this.enrollmentRepository = enrollmentRepository;
  }

  @Override
  public List<EnrollmentDTO> addEnrollment(String requestId, List<EnrollmentRequest> request) {
    List<Enrollment> enrollments = new ArrayList<>();

    request.forEach((enrollmentRequest) -> {
      Student student = studentRepository.findById(enrollmentRequest.getStudentId()).orElseThrow(
          () -> new TveException(requestId,
              messageUtil.getMessage(MessageCode.MESSAGE_STU_NOT_FOUND)));

      Course course = courseRepository.findById(enrollmentRequest.getCourseId()).orElseThrow(
          () -> new TveException(requestId, messageUtil.getMessage(MessageCode.MESSAGE_NOT_FOUND)));

      Enrollment enrollment = Enrollment.builder().student(student).course(course)
          .classCode(ObjectUtils.isEmpty(enrollmentRequest.getClassCode()) ? ""
              : enrollmentRequest.getClassCode())
          .enrollmentDate(ObjectUtils.isEmpty(enrollmentRequest.getEnrollmentDate()) ? new Date()
              : enrollmentRequest.getEnrollmentDate())
          .build();
      enrollments.add(enrollment);
    });

    return enrollmentToDTOMapper.toListDto(enrollmentRepository.saveAll(enrollments));
  }

  @Override
  public EnrollmentDTO deleteEnrollment(String requestId, int enrollmentId) {
    Optional<Enrollment> enrollmentObject = enrollmentRepository.findById(enrollmentId);
    if (enrollmentObject.isEmpty()) {
      throw new TveException(requestId,
          messageUtil.getMessage(MessageCode.MESSAGE_ENROLLMENT_NOT_FOUND));
    }
    Enrollment enrollment = enrollmentObject.get();
    EnrollmentDTO enrollmentDTO = enrollmentToDTOMapper.toDto(enrollment);
    enrollment.setIsDelete(true);
    enrollmentRepository.save(enrollment);
    return enrollmentDTO;
  }

  @Override
  public List<EnrollmentDTO> getListEnrollment(String requestId, String courseCode,
      String classCode) {
    List<Enrollment> enrollments = ObjectUtils.isEmpty(classCode)
        ? enrollmentRepository.findEnrollmentsByCourseCodeAndIsDeleteFalse(courseCode)
        : enrollmentRepository.findEnrollmentsByCourseCodeAndClassCode(courseCode, classCode);

    if (ObjectUtils.isEmpty(enrollments)) {
      throw new TveException(requestId,
          messageUtil.getMessage(MessageCode.MESSAGE_ENROLLMENT_NOT_FOUND));
    }

    return enrollmentToDTOMapper.toListDto(enrollments);
  }

  @Override
  public List<EnrollmentDTO> getListEnrollmentByStudent(String requestId, String studentCode) {
    Optional<List<Enrollment>> enrollmentList = enrollmentRepository.findEnrollmentByStudentCode(
        studentCode);
    if (enrollmentList.isEmpty()) {
      throw new TveException(MessageCode.MESSAGE_ENROLLMENT_NOT_FOUND.getCode(),
          messageUtil.getMessage(MessageCode.MESSAGE_ENROLLMENT_NOT_FOUND));
    }
    return enrollmentToDTOMapper.toListDto(enrollmentList.get());
  }

  @Override
  public EnrollmentDTO updateEnrollment(String requestId, int enrollmentId,
      EnrollmentRequest request) {
    Enrollment enrollment = enrollmentRepository.findByIdAndIsDeleteFalse(enrollmentId);
    if (ObjectUtils.isEmpty(enrollment)) {
      throw new TveException(requestId, messageUtil.getMessage(MessageCode.MESSAGE_NOT_FOUND));
    }
    Course course = courseRepository.findByIdAndIsDeleteFalse(request.getCourseId());
    if (!ObjectUtils.isEmpty(course)) {
      enrollment.setCourse(course);
    }
    Student student = studentRepository.findByIdAndIsDeleteFalse(request.getStudentId())
        .orElseThrow(() -> new TveException(requestId,
            messageUtil.getMessage(MessageCode.MESSAGE_NOT_FOUND)));

    enrollment.setStudent(student);
    enrollment.setEnrollmentDate(ObjectUtils.isEmpty(request.getEnrollmentDate()) ? new Date()
        : request.getEnrollmentDate());
    return enrollmentToDTOMapper.toDto(enrollmentRepository.save(enrollment));
  }
}
