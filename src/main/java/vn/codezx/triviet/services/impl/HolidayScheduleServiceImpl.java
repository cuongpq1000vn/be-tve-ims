package vn.codezx.triviet.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import vn.codezx.triviet.constants.MessageCode;
import vn.codezx.triviet.dtos.holiday_schedule.HolidayScheduleDTO;
import vn.codezx.triviet.dtos.holiday_schedule.HolidayScheduleRequest;
import vn.codezx.triviet.entities.setting.HolidaySchedule;
import vn.codezx.triviet.exceptions.TveException;
import vn.codezx.triviet.mappers.setting.HolidayScheduleToDTOMapper;
import vn.codezx.triviet.repositories.HolidayScheduleRepository;
import vn.codezx.triviet.services.HolidayScheduleService;
import vn.codezx.triviet.utils.LogUtil;
import vn.codezx.triviet.utils.MessageUtil;

@Service
@Slf4j
public class HolidayScheduleServiceImpl implements HolidayScheduleService {

    private final HolidayScheduleRepository holidayScheduleRepository;
    private final HolidayScheduleToDTOMapper holidayScheduleMapper;
    private final MessageUtil messageUtil;

    @Autowired
    HolidayScheduleServiceImpl(HolidayScheduleRepository holidayScheduleRepository,
            HolidayScheduleToDTOMapper holidayScheduleMapper, MessageUtil messageUtil) {
        this.holidayScheduleRepository = holidayScheduleRepository;
        this.holidayScheduleMapper = holidayScheduleMapper;
        this.messageUtil = messageUtil;
    }

    @Override
    @Transactional
    public HolidayScheduleDTO createHolidaySchedule(String requestId,
            HolidayScheduleRequest holidayScheduleRequest) {
        HolidaySchedule holidaySchedule =
                HolidaySchedule.builder().holidayType(holidayScheduleRequest.getHolidayType())
                        .startDate(holidayScheduleRequest.getStartDate())
                        .endDate(holidayScheduleRequest.getEndDate())
                        .description(holidayScheduleRequest.getDescription()).build();
        holidaySchedule = holidayScheduleRepository.save(holidaySchedule);
        return holidayScheduleMapper.toDto(holidaySchedule);
    }

    @Override
    public Page<HolidayScheduleDTO> getHolidaySchedules(String requestId, Pageable pageable) {
        return holidayScheduleRepository.findAllByIsDeleteIsFalse(pageable)
                .map(holidayScheduleMapper::toDto);
    }

    @Override
    public HolidayScheduleDTO getHolidaySchedule(String requestId, Integer holidayScheduleId) {
        return holidayScheduleMapper.toDto(findHolidayScheduleById(holidayScheduleId, requestId));
    }

    @Override
    @Transactional
    public HolidayScheduleDTO updateHolidaySchedule(String requestId, Integer holidayScheduleId,
            HolidayScheduleRequest holidayScheduleRequest) {
        HolidaySchedule holidaySchedule = findHolidayScheduleById(holidayScheduleId, requestId);

        if (holidayScheduleRequest.getStartDate() != null) {
            holidaySchedule.setStartDate(holidayScheduleRequest.getStartDate());
        }

        if (holidayScheduleRequest.getEndDate() != null) {
            holidaySchedule.setEndDate(holidayScheduleRequest.getEndDate());
        }

        if (holidayScheduleRequest.getDescription() != null) {
            holidaySchedule.setDescription(holidayScheduleRequest.getDescription());
        }

        if (holidayScheduleRequest.getHolidayType() != null) {
            holidaySchedule.setHolidayType(holidayScheduleRequest.getHolidayType());
        }

        return holidayScheduleMapper.toDto(holidaySchedule);
    }

    @Override
    @Transactional
    public HolidayScheduleDTO deleteHolidaySchedule(String requestId, Integer holidayScheduleId) {
        HolidaySchedule holidaySchedule = findHolidayScheduleById(holidayScheduleId, requestId);
        holidaySchedule.setIsDelete(true);
        return holidayScheduleMapper.toDto(holidaySchedule);
    }

    private HolidaySchedule findHolidayScheduleById(Integer holidayScheduleId, String requestId) {
        return holidayScheduleRepository.findByIdAndIsDeleteIsFalse(holidayScheduleId)
                .orElseThrow(() -> {
                    log.error(LogUtil.buildFormatLog(requestId, messageUtil
                            .getMessage(MessageCode.MESSAGE_HOLI_NOT_FOUND, holidayScheduleId)));
                    return new TveException(MessageCode.MESSAGE_HOLI_NOT_FOUND, messageUtil
                            .getMessage(MessageCode.MESSAGE_HOLI_NOT_FOUND, holidayScheduleId));
                });
    }
}
