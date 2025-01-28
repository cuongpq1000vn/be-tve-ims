package vn.codezx.triviet.services.impl;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.codezx.triviet.constants.MessageCode;
import vn.codezx.triviet.dtos.formula.FormulaDTO;
import vn.codezx.triviet.dtos.formula.FormulaRequest;
import vn.codezx.triviet.entities.course.Course;
import vn.codezx.triviet.entities.setting.Formula;
import vn.codezx.triviet.exceptions.TveException;
import vn.codezx.triviet.mappers.setting.FormulaToDTOMapper;
import vn.codezx.triviet.mappers.setting.FormulaToEntityMapper;
import vn.codezx.triviet.repositories.CourseRepository;
import vn.codezx.triviet.repositories.FormulaRepository;
import vn.codezx.triviet.services.FormulaService;
import vn.codezx.triviet.utils.MessageUtil;


@Service
public class FormulaServiceImpl implements FormulaService {
  private final FormulaRepository formulaRepository;
  private final FormulaToDTOMapper formulaToDTOMapper;
  private final FormulaToEntityMapper formulaToEntityMapper;
  private final MessageUtil messageUtil;
  private final CourseRepository courseRepository;

  public FormulaServiceImpl(FormulaRepository formulaRepository,
      FormulaToDTOMapper formulaToDTOMapper, FormulaToEntityMapper formulaToEntityMapper,
      MessageUtil messageUtil, CourseRepository courseRepository) {
    this.formulaRepository = formulaRepository;
    this.formulaToDTOMapper = formulaToDTOMapper;
    this.formulaToEntityMapper = formulaToEntityMapper;
    this.messageUtil = messageUtil;
    this.courseRepository = courseRepository;
  }

  @Override
  @Transactional
  public FormulaDTO createFormula(String requestId, FormulaRequest formulaRequest) {
    Formula formula = formulaToEntityMapper.toEntity(formulaRequest);
    formula = formulaRepository.saveAndFlush(formula);
    return formulaToDTOMapper.toDto(formula);
  }

  @Override
  public FormulaDTO getFormula(String requestId, Integer formulaId) {
    Formula formula = findFormulaById(requestId, formulaId);
    return formulaToDTOMapper.toDto(formula);
  }

  @Override
  @Transactional
  public FormulaDTO updateFormula(String requestId, Integer formulaId,
      FormulaRequest formulaRequest) {
    Formula formula = findFormulaById(requestId, formulaId);

    if (formulaRequest.getName() != null) {
      formula.setName(formulaRequest.getName());
    }

    if (formulaRequest.getMidtermListeningMaxScore() != null) {
      formula.setMidtermListeningMaxScore(formulaRequest.getMidtermListeningMaxScore());
    }

    if (formulaRequest.getMidtermReadingMaxScore() != null) {
      formula.setMidtermReadingMaxScore(formulaRequest.getMidtermReadingMaxScore());
    }

    if (formulaRequest.getMidtermWritingMaxScore() != null) {
      formula.setMidtermWritingMaxScore(formulaRequest.getMidtermWritingMaxScore());
    }

    if (formulaRequest.getMidtermSpeakingMaxScore() != null) {
      formula.setMidtermSpeakingMaxScore(formulaRequest.getMidtermSpeakingMaxScore());
    }

    if (formulaRequest.getMidtermSumFormula() != null) {
      formula.setMidtermSumFormula(formulaRequest.getMidtermSumFormula());
    }

    if (formulaRequest.getMidtermPercentageFormula() != null) {
      formula.setMidtermPercentageFormula(formulaRequest.getMidtermPercentageFormula());
    }

    if (formulaRequest.getMidtermClassificationFormula() != null) {
      formula.setMidtermClassificationFormula(formulaRequest.getMidtermClassificationFormula());
    }

    if (formulaRequest.getFinalListeningMaxScore() != null) {
      formula.setFinalListeningMaxScore(formulaRequest.getFinalListeningMaxScore());
    }

    if (formulaRequest.getFinalReadingMaxScore() != null) {
      formula.setFinalReadingMaxScore(formulaRequest.getFinalReadingMaxScore());
    }

    if (formulaRequest.getFinalWritingMaxScore() != null) {
      formula.setFinalWritingMaxScore(formulaRequest.getFinalWritingMaxScore());
    }

    if (formulaRequest.getFinalSpeakingMaxScore() != null) {
      formula.setFinalSpeakingMaxScore(formulaRequest.getFinalSpeakingMaxScore());
    }

    if (formulaRequest.getFinalSumFormula() != null) {
      formula.setFinalSumFormula(formulaRequest.getFinalSumFormula());
    }

    if (formulaRequest.getFinalPercentageFormula() != null) {
      formula.setFinalPercentageFormula(formulaRequest.getFinalPercentageFormula());
    }

    if (formulaRequest.getFinalClassificationFormula() != null) {
      formula.setFinalClassificationFormula(formulaRequest.getFinalClassificationFormula());
    }

    if (formulaRequest.getMidtermGradeWeight() != null) {
      formula.setMidtermGradeWeight(formulaRequest.getMidtermGradeWeight());
    }

    if (formulaRequest.getFinalGradeWeight() != null) {
      formula.setFinalGradeWeight(formulaRequest.getFinalGradeWeight());
    }

    if (formulaRequest.getBonusGradeWeight() != null) {
      formula.setBonusGradeWeight(formulaRequest.getBonusGradeWeight());
    }

    if (formulaRequest.getClassificationFormula() != null) {
      formula.setClassificationFormula(formulaRequest.getClassificationFormula());
    }


    if (formulaRequest.getCourseIds() != null) {
      List<Course> newCourses = courseRepository.findAllById(formulaRequest.getCourseIds());
      formula.getCourses().forEach(course -> {
        if (!newCourses.contains(course)) {
          course.setFormula(null);
        }
      });

      newCourses.forEach(course -> course.setFormula(formula));

      formula.setCourses(newCourses);
    }

    return formulaToDTOMapper.toDto(formula);
  }

  @Override
  @Transactional
  public FormulaDTO deleteFormula(String requestId, Integer formulaId) {
    Formula formula = findFormulaById(requestId, formulaId);
    formula.setIsDelete(true);
    return formulaToDTOMapper.toDto(formula);
  }

  @Override
  public Page<FormulaDTO> getFormulas(String requestId, Pageable pageable) {
    return formulaRepository.findAllByIsDeleteFalse(pageable).map(formulaToDTOMapper::toDto);
  }

  private Formula findFormulaById(String requestId, Integer formulaId) {
    Formula formula = formulaRepository.findByIdAndIsDeleteFalse(formulaId)
        .orElseThrow(() -> new TveException(MessageCode.MESSAGE_NOT_FOUND,
            messageUtil.getMessage(MessageCode.MESSAGE_FOR_NOT_FOUND, formulaId), requestId));

    return formula;
  }

}
