package vn.codezx.triviet.services.impl;

import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import vn.codezx.triviet.constants.MessageCode;
import vn.codezx.triviet.dtos.absence.AbsenceDTO;
import vn.codezx.triviet.dtos.absence.AbsenceRequest;
import vn.codezx.triviet.entities.course.ClassDay;
import vn.codezx.triviet.entities.course.ClassTvms;
import vn.codezx.triviet.entities.student.Absence;
import vn.codezx.triviet.entities.student.Student;
import vn.codezx.triviet.exceptions.TveException;
import vn.codezx.triviet.mappers.absence.AbsenceToDTOMapper;
import vn.codezx.triviet.repositories.AbsenceRepository;
import vn.codezx.triviet.repositories.ClassDayRepository;
import vn.codezx.triviet.repositories.ClassTvmsRepository;
import vn.codezx.triviet.repositories.StudentRepository;
import vn.codezx.triviet.services.AbsenceService;
import vn.codezx.triviet.utils.MessageUtil;

@Service
@Slf4j
public class AbsenceServiceImpl implements AbsenceService {

  private final AbsenceRepository absenceRepository;
  private final AbsenceToDTOMapper absenceToDTOMapper;

  private final StudentRepository studentRepository;


  private final ClassTvmsRepository classTvmsRepository;


  private final ClassDayRepository classDayRepository;

  private MessageUtil messageUtil;

  @Autowired
  public AbsenceServiceImpl(AbsenceToDTOMapper absenceToDTOMapper,
      AbsenceRepository absenceRepository, StudentRepository studentRepository,
      ClassTvmsRepository classTvmsRepository, ClassDayRepository classDayRepository,
      MessageUtil messageUtil) {
    this.messageUtil = messageUtil;
    this.absenceRepository = absenceRepository;
    this.absenceToDTOMapper = absenceToDTOMapper;
    this.studentRepository = studentRepository;
    this.classDayRepository = classDayRepository;
    this.classTvmsRepository = classTvmsRepository;
  }

  @Override
  @Transactional
  public AbsenceDTO markAbsence(String requestId, AbsenceRequest absenceRequest) {
    Optional<Student> student = studentRepository.findById(absenceRequest.getStudentId());
    if (student.isEmpty()) {
      throw new TveException(requestId,
          messageUtil.getMessage(MessageCode.MESSAGE_STU_NOT_FOUND));
    }
    Optional<ClassTvms> classTvms = classTvmsRepository.findByCodeAndIsDeleteFalse(
        absenceRequest.getClassCode());
    if (classTvms.isEmpty()) {
      throw new TveException(requestId,
          messageUtil.getMessage(MessageCode.MESSAGE_CLASS_DAY_NOT_FOUND));
    }

    Optional<ClassDay> classDay = classDayRepository.findById(absenceRequest.getClassDayId());
    if (classDay.isEmpty()) {
      throw new TveException(requestId,
          messageUtil.getMessage(MessageCode.MESSAGE_CLASS_DAY_NOT_FOUND));
    }
    Optional<Absence> existingAbsence = absenceRepository.findByStudentAndClassTvmsAndClassDayAndIsDeleteFalse(
        student.get(), classTvms.get(), classDay.get());

    Absence absence;

    if (existingAbsence.isPresent()) {
      absence = existingAbsence.get();
      absence.setIsAbsent(absenceRequest.isCheckAbsent());
    } else {
      absence = Absence.builder()
          .student(student.get())
          .classTvms(classTvms.get())
          .classDay(classDay.get())
          .isAbsent(absenceRequest.isCheckAbsent())
          .build();
    }

    Absence savedAbsence = absenceRepository.save(absence);
    return absenceToDTOMapper.toDto(savedAbsence);
  }

  @Override
  @Transactional
  public List<AbsenceDTO> getAbsentGroup(String requestId) {
    return absenceToDTOMapper.toListDto(absenceRepository.findAll());
  }

  @Override
  public List<AbsenceDTO> getAbsenceByClass(String requestId, String classCode) {
    Optional<List<Absence>> absenceList = absenceRepository.getListAbsenceByClassCode(classCode);
    if (absenceList.isEmpty()) {
      throw new TveException(MessageCode.MESSAGE_ABS_NOTFOUND,
          messageUtil.getMessage(MessageCode.MESSAGE_ABS_NOTFOUND));
    }
    return absenceToDTOMapper.toListDto(absenceList.get());
  }

  @Override
  public List<AbsenceDTO> getStudentAbsence(String requestId, String studentCode) {
    Student student = studentRepository.findByCodeAndIsDeleteFalse(studentCode);
    if (ObjectUtils.isEmpty(student)) {
      throw new TveException(requestId, messageUtil.getMessage(MessageCode.MESSAGE_STU_NOT_FOUND));
    }

    List<Absence> absenceList = absenceRepository.findByStudentAndIsDeleteFalse(student);
    if (ObjectUtils.isEmpty(absenceList)) {
      throw new TveException(requestId, messageUtil.getMessage(MessageCode.MESSAGE_ABS_NOTFOUND));
    }

    return absenceToDTOMapper.toListDto(absenceList);
  }
}

