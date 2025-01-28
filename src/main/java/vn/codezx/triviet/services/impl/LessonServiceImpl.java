package vn.codezx.triviet.services.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import vn.codezx.triviet.constants.MessageCode;
import vn.codezx.triviet.constants.TypeOfTest;
import vn.codezx.triviet.dtos.lesson.LessonDTO;
import vn.codezx.triviet.dtos.lesson.LessonRequest;
import vn.codezx.triviet.entities.course.Course;
import vn.codezx.triviet.entities.course.Lesson;
import vn.codezx.triviet.exceptions.TveException;
import vn.codezx.triviet.mappers.lesson.LessonToDTOMapper;
import vn.codezx.triviet.repositories.ClassDayRepository;
import vn.codezx.triviet.repositories.ClassTvmsRepository;
import vn.codezx.triviet.repositories.CourseRepository;
import vn.codezx.triviet.repositories.LessonRepository;
import vn.codezx.triviet.repositories.TestTypeRepository;
import vn.codezx.triviet.services.ClassDayService;
import vn.codezx.triviet.services.LessonService;
import vn.codezx.triviet.utils.LogUtil;
import vn.codezx.triviet.utils.MessageUtil;

@Service
@Slf4j
public class LessonServiceImpl implements LessonService {

  private final CourseRepository courseRepository;
  private final TestTypeRepository testTypeRepository;
  private final LessonRepository lessonRepository;
  private final LessonToDTOMapper lessonToDTOMapper;
  private final MessageUtil messageUtil;
  private final ClassTvmsRepository classTvmsRepository;
  private final ClassDayService classDayService;
  private final ClassDayRepository classDayRepository;

  public LessonServiceImpl(MessageUtil messageUtil, CourseRepository courseRepository,
      LessonRepository lessonRepository, LessonToDTOMapper lessonToDTOMapper,
      ClassTvmsRepository classTvmsRepository, ClassDayService classDayService,
      ClassDayRepository classDayRepository, TestTypeRepository testTypeRepository) {
    this.messageUtil = messageUtil;
    this.courseRepository = courseRepository;
    this.lessonRepository = lessonRepository;
    this.lessonToDTOMapper = lessonToDTOMapper;
    this.classTvmsRepository = classTvmsRepository;
    this.classDayService = classDayService;
    this.classDayRepository = classDayRepository;
    this.testTypeRepository = testTypeRepository;
  }

  @Override
  @Transactional
  public List<LessonDTO> createLesson(String requestId, List<LessonRequest> request) {
    List<Lesson> lessonList = new ArrayList<>();
    Set<Course> courses = new HashSet<>();
    request.forEach(lessonElm -> {
      Course courseEntity = courseRepository.findByCodeAndIsDeleteFalse(lessonElm.getCourseId());
      if (ObjectUtils.isEmpty(courseEntity)) {
        throw new TveException(requestId, messageUtil.getMessage(MessageCode.MESSAGE_NOT_FOUND));
      }

      courses.add(courseEntity);

      Lesson lesson =
          Lesson.builder().course(courseEntity).description(lessonElm.getDescription()).build();

      if (lessonElm.getLessonType() == TypeOfTest.MIDTERM
          || lessonElm.getLessonType() == TypeOfTest.FINALTERM) {
        var type = testTypeRepository.findByType(lessonElm.getLessonType());

        if (!type.isPresent()) {
          var message = LogUtil.buildFormatLog(requestId, messageUtil
              .getMessage(MessageCode.MESSAGE_LESS_TYPE_NOT_FOUND, lessonElm.getLessonType()));
          log.error(message);

          throw new TveException(MessageCode.MESSAGE_LESS_TYPE_NOT_FOUND, message);
        }
        lesson.setLessonType(type.get());
      }
      lessonList.add(lesson);
    });

    var lessons = lessonToDTOMapper.toListDto(lessonRepository.saveAll(lessonList));
    courses.forEach(course -> updateClassDay(requestId, course));

    return lessons;
  }

  @Override
  @Transactional
  public List<LessonDTO> getLessonByCourse(String requestId, String courseCode) {

    List<Lesson> lessons = lessonRepository.findLessonsByCourseCodeAndIsDeleteFalse(courseCode);
    if (ObjectUtils.isEmpty(lessons)) {
      throw new TveException(requestId, messageUtil.getMessage(MessageCode.MESSAGE_NOT_FOUND));
    }

    return lessonToDTOMapper.toListDto(lessons);
  }

  @Override
  @Transactional
  public LessonDTO editLesson(String requestId, LessonRequest request, int lessonId) {
    Lesson lesson = lessonRepository.findByIdAndIsDeleteFalse(lessonId);
    if (ObjectUtils.isEmpty(lesson)) {
      throw new TveException(requestId, messageUtil.getMessage(MessageCode.MESSAGE_NOT_FOUND));
    }
    if (!ObjectUtils.isEmpty(request.getDescription())) {
      lesson.setDescription(request.getDescription());
    }

    if (request.getLessonType() == TypeOfTest.MIDTERM
        || request.getLessonType() == TypeOfTest.FINALTERM) {
      var type = testTypeRepository.findByType(request.getLessonType());

      if (!type.isPresent()) {
        var message = LogUtil.buildFormatLog(requestId, messageUtil
            .getMessage(MessageCode.MESSAGE_LESS_TYPE_NOT_FOUND, request.getLessonType()));
        log.error(message);

        throw new TveException(MessageCode.MESSAGE_LESS_TYPE_NOT_FOUND, message);
      }
      lesson.setLessonType(type.get());
    }

    var lessonDTO = lessonToDTOMapper.toDto(lessonRepository.save(lesson));
    updateClassDay(requestId, lesson.getCourse());

    return lessonDTO;
  }

  @Override
  @Transactional
  public LessonDTO deleteLesson(String requestId, int lessonId) {
    Lesson lesson = lessonRepository.findByIdAndIsDeleteFalse(lessonId);
    if (ObjectUtils.isEmpty(lesson)) {
      throw new TveException(requestId, messageUtil.getMessage(MessageCode.MESSAGE_NOT_FOUND));
    }
    lesson.setIsDelete(true);
    LessonDTO lessonDTO = lessonToDTOMapper.toDto(lesson);
    lessonRepository.save(lesson);
    updateClassDay(requestId, lesson.getCourse());
    return lessonDTO;
  }

  private void updateClassDay(String requestId, Course course) {
    var classes = classTvmsRepository.findAllByCourseAndIsDeleteFalse(course);

    var classDays = classes.stream().flatMap(c -> c.getClassDays().stream()).toList();

    classDayRepository.deleteAll(classDays);
    for (int i = 0; i < classes.size(); i++) {
      classDayService.generateClassDay(requestId, classes.get(i));
    }
  }
}
