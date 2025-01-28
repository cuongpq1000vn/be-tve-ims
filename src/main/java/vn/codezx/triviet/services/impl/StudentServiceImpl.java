package vn.codezx.triviet.services.impl;

import java.io.File;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import lombok.extern.slf4j.Slf4j;
import vn.codezx.triviet.constants.MessageCode;
import vn.codezx.triviet.dtos.student.StudentDTO;
import vn.codezx.triviet.dtos.student.StudentRequest;
import vn.codezx.triviet.entities.setting.Discount;
import vn.codezx.triviet.entities.student.Student;
import vn.codezx.triviet.exceptions.TveException;
import vn.codezx.triviet.mappers.student.StudentToDTOMapper;
import vn.codezx.triviet.repositories.DiscountRepository;
import vn.codezx.triviet.repositories.StudentRepository;
import vn.codezx.triviet.services.FileStorageService;
import vn.codezx.triviet.services.StudentService;
import vn.codezx.triviet.utils.CommonUtil;
import vn.codezx.triviet.utils.DateTimeUtil;
import vn.codezx.triviet.utils.LogUtil;
import vn.codezx.triviet.utils.MessageUtil;
import vn.codezx.triviet.utils.StringUtil;

@Service
@Slf4j
public class StudentServiceImpl implements StudentService {

  private final StudentRepository studentRepository;
  private final DiscountRepository discountRepository;
  private final StudentToDTOMapper studentToDTOMapper;
  private final MessageUtil messageUtil;
  private final FileStorageService fileStorageService;
  private final String avatarStoragePath;

  @Autowired
  public StudentServiceImpl(StudentRepository studentRepository,
      DiscountRepository discountRepository, StudentToDTOMapper studentToDTOMapper,
      MessageUtil messageUtil, FileStorageService fileStorageService,
      @Value("${file.storage.student.avatar}") String avatarStoragePath) {
    this.studentRepository = studentRepository;
    this.discountRepository = discountRepository;
    this.studentToDTOMapper = studentToDTOMapper;
    this.messageUtil = messageUtil;
    this.fileStorageService = fileStorageService;
    this.avatarStoragePath = avatarStoragePath;
  }

  @Transactional
  @Override
  public StudentDTO createStudent(String requestId, StudentRequest studentRequest,
      MultipartFile avatar) {
    validateStudentRequest(studentRequest, requestId);
    Student student;

    student = Student.builder().address(studentRequest.getAddress())
        .dateOfBirth(studentRequest.getDateOfBirth()).name(studentRequest.getName())
        .nickname(studentRequest.getNickname()).phoneNumber(studentRequest.getPhoneNumber())
        .code(generateStudentCode()).emailAddress(studentRequest.getEmailAddress())
        .note(studentRequest.getNote()).discount(null).build();
    if (studentRequest.getDiscountId() != null) {
      Optional<Discount> discount = discountRepository.findById(studentRequest.getDiscountId());
      if (discount.isPresent()) {
        student.setDiscount(discount.get());
      }
    }

    log.info(LogUtil.buildFormatLog(requestId,
        messageUtil.getMessage(MessageCode.MESSAGE_STU_CREATE_SUCCESS)));
    student = studentRepository.saveAndFlush(student);
    if (avatar != null) {
      storeAvatar(requestId, student, avatar);
    }

    return studentToDTOMapper.toDto(student);
  }

  @Override
  public Page<StudentDTO> getStudents(String requestId, String query, String hasAvatar,
      Pageable pageable) {
    if (query != null && !query.isEmpty() && "have".equalsIgnoreCase(hasAvatar)) {
      return studentRepository.searchByQueryAndAvatarIsNotNull(query, pageable)
          .map(studentToDTOMapper::toDto);
    }

    if (query != null && !query.isEmpty() && "not have".equalsIgnoreCase(hasAvatar)) {
      return studentRepository.searchByQueryAndAvatarIsNull(query, pageable)
          .map(studentToDTOMapper::toDto);
    }

    if (query != null && !query.isEmpty()) {
      return studentRepository.searchByQuery(query, pageable).map(studentToDTOMapper::toDto);
    }

    if ("have".equalsIgnoreCase(hasAvatar)) {
      return studentRepository.findByAvatarUrlIsNotNullAndIsDeleteIsFalse(pageable)
          .map(studentToDTOMapper::toDto);
    }

    if ("not have".equalsIgnoreCase(hasAvatar)) {
      return studentRepository.findByAvatarUrlIsNullAndIsDeleteIsFalse(pageable)
          .map(studentToDTOMapper::toDto);
    }
    return studentRepository.findAllByIsDeleteIsFalse(pageable).map(studentToDTOMapper::toDto);
  }

  @Override
  public StudentDTO getStudent(String requestId, String studentCode) {
    Student student =
        studentRepository.findByCodeAndIsDeleteIsFalse(studentCode).orElseThrow(() -> {
          log.error(LogUtil.buildFormatLog(requestId,
              messageUtil.getMessage(MessageCode.MESSAGE_STU_NOT_FOUND, studentCode)));
          return new TveException(MessageCode.MESSAGE_NOT_FOUND,
              messageUtil.getMessage(MessageCode.MESSAGE_STU_NOT_FOUND, studentCode), requestId);
        });

    log.info(LogUtil.buildFormatLog(requestId,
        messageUtil.getMessage(MessageCode.MESSAGE_STU_GET_SUCCESS, studentCode)));

    return studentToDTOMapper.toDto(student);
  }

  @Transactional
  @Override
  public StudentDTO updateStudent(String requestId, Integer studentId,
      StudentRequest studentRequest, MultipartFile avatar) {
    Student student = findStudentById(studentId, requestId);
    validateStudentRequest(studentRequest, requestId);

    if (studentRequest.getAddress() != null) {
      student.setAddress(studentRequest.getAddress());
    }
    if (studentRequest.getDateOfBirth() != null) {
      student.setDateOfBirth(studentRequest.getDateOfBirth());
    }
    if (studentRequest.getName() != null) {
      student.setName(studentRequest.getName());
    }
    if (studentRequest.getNickname() != null) {
      student.setNickname(studentRequest.getNickname());
    }
    if (studentRequest.getEmailAddress() != null) {
      student.setEmailAddress(studentRequest.getEmailAddress());
    }
    if (studentRequest.getPhoneNumber() != null) {
      student.setPhoneNumber(studentRequest.getPhoneNumber());
    }
    if (studentRequest.getNote() != null) {
      student.setNote(studentRequest.getNote());
    }
    if (studentRequest.getDiscountId() != null) {
      Optional<Discount> discount = discountRepository.findById(studentRequest.getDiscountId());
      if (discount.isPresent()) {
        student.setDiscount(discount.get());
      }
    }
    if (avatar != null) {
      storeAvatar(requestId, student, avatar);
    }

    log.info(LogUtil.buildFormatLog(requestId,
        messageUtil.getMessage(MessageCode.MESSAGE_STU_UPDATE_SUCCESS, studentId)));

    return studentToDTOMapper.toDto(student);
  }

  @Transactional
  @Override
  public StudentDTO deleteStudent(String requestId, Integer studentId) {
    Student student = findStudentById(studentId, requestId);

    student.setIsDelete(true);
    log.info(LogUtil.buildFormatLog(requestId,
        messageUtil.getMessage(MessageCode.MESSAGE_STU_DELETE_SUCCESS, studentId)));

    return studentToDTOMapper.toDto(student);
  }

  @Override
  public List<StudentDTO> getPreEnrollmentStudent(String requestId) {
    return studentToDTOMapper.toListDto(studentRepository.getPreEnrollmentStudent());
  }

  @Override
  public String storeAvatar(String requestId, Student student, MultipartFile file) {
    String fileName = fileStorageService.storeFile(requestId, file, avatarStoragePath,
        String.valueOf(student.getId()));

    String oldAvatarUrl = student.getAvatarUrl();
    if (oldAvatarUrl != null) {
      fileStorageService.deleteFile(oldAvatarUrl, requestId);
    }

    String imagePath = avatarStoragePath + File.separator + fileName;
    student.setAvatarUrl(imagePath);
    return imagePath;
  }

  @Override
  public Integer getTotalStudent(String requestId) {
    return studentRepository.getTotalNumberOfStudent();
  }

  @Override
  public List<StudentDTO> getStudentsByClassCode(String requestId, String classCode) {
    List<Student> students = studentRepository.findByClassCode(classCode);
    return studentToDTOMapper.toListDto(students);
  }

  private Student findStudentById(Integer studentId, String requestId) {
    Student student = studentRepository.findByIdAndIsDeleteFalse(studentId)
        .orElseThrow(() -> new TveException(MessageCode.MESSAGE_NOT_FOUND,
            messageUtil.getMessage(MessageCode.MESSAGE_STU_NOT_FOUND, studentId), requestId));

    return student;
  }


  private void validateStudentRequest(StudentRequest studentRequest, String requestId) {
    String email = studentRequest.getEmailAddress();
    if (email != null && !email.isEmpty() && !CommonUtil.validateEmail(email)) {
      throw new TveException(MessageCode.MESSAGE_ERROR_INPUT_ERROR,
          messageUtil.getMessage(MessageCode.MESSAGE_STU_EMAIL_INVALID), requestId);
    }
  }

  private String generateStudentCode() {
    Optional<Student> latestStudentInDay =
        studentRepository.findTop1ByCreatedAtBetweenAndIsDeleteIsFalseOrderByCodeDesc(
            DateTimeUtil.startOfDay(), DateTimeUtil.endOfDay());

    if (latestStudentInDay.isPresent()) {
      String latestStudentCode = latestStudentInDay.get().getCode();
      Integer number =
          Integer.parseInt(latestStudentCode.substring(latestStudentCode.length() - 3));
      return StringUtil.generateCode("STU", number + 1);
    } else {
      return StringUtil.generateCode("STU", 1);
    }
  }
}
