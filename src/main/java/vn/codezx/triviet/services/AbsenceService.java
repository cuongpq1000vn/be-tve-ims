package vn.codezx.triviet.services;

import java.util.List;
import vn.codezx.triviet.dtos.absence.AbsenceDTO;
import vn.codezx.triviet.dtos.absence.AbsenceRequest;

public interface AbsenceService {

  AbsenceDTO markAbsence(String requestId, AbsenceRequest attendanceRequest);

  List<AbsenceDTO> getAbsentGroup(String requestId);

  List<AbsenceDTO> getAbsenceByClass(String requestId, String classCode);

  List<AbsenceDTO> getStudentAbsence(String requestId, String studentCode);
}
