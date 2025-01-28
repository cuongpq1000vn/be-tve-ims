package vn.codezx.triviet.services.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import vn.codezx.triviet.constants.InvoiceStatus;
import vn.codezx.triviet.constants.MessageCode;
import vn.codezx.triviet.constants.PaymentTypeConstants;
import vn.codezx.triviet.dtos.invoice.InVoiceDTO;
import vn.codezx.triviet.dtos.invoice.InvoiceRequest;
import vn.codezx.triviet.entities.course.ClassTvms;
import vn.codezx.triviet.entities.reporting.Invoice;
import vn.codezx.triviet.exceptions.TveException;
import vn.codezx.triviet.mappers.invoice.InvoiceToDTOMapper;
import vn.codezx.triviet.repositories.ClassTvmsRepository;
import vn.codezx.triviet.repositories.InvoiceRepository;
import vn.codezx.triviet.services.InvoiceService;
import vn.codezx.triviet.utils.LogUtil;
import vn.codezx.triviet.utils.MessageUtil;

@Service
@Slf4j
public class InvoiceServiceImpl implements InvoiceService {

  private final InvoiceRepository invoiceRepository;
  private final InvoiceToDTOMapper invoiceToDTOMapper;
  private final ClassTvmsRepository classTvmsRepository;
  private final MessageUtil messageUtil;

  public InvoiceServiceImpl(InvoiceRepository invoiceRepository,
      InvoiceToDTOMapper invoiceToDTOMapper, MessageUtil messageUtil,
      ClassTvmsRepository classTvmsRepository) {
    this.invoiceRepository = invoiceRepository;
    this.invoiceToDTOMapper = invoiceToDTOMapper;
    this.messageUtil = messageUtil;
    this.classTvmsRepository = classTvmsRepository;
  }


  @Override
  @Transactional
  public List<InVoiceDTO> getInvoiceByStudent(String requestId, String studentCode) {
    List<Invoice> invoiceGroup = invoiceRepository.findByStudentCode(studentCode);
    if (ObjectUtils.isEmpty(invoiceGroup)) {
      throw new TveException(requestId,
          messageUtil.getMessage(MessageCode.MESSAGE_INVOICE_NOT_FOUND));
    }
    List<InVoiceDTO> inVoiceDTOS = invoiceToDTOMapper.toListDto(invoiceGroup);

    List<String> classCodes = inVoiceDTOS.stream()
        .map(InVoiceDTO::getClassCode)
        .toList();

    Map<String, String> classCodeToNameMap = classTvmsRepository
        .findAllByCodeInAndIsDeleteFalse(classCodes)
        .stream()
        .collect(Collectors.toMap(ClassTvms::getCode, ClassTvms::getName));

    inVoiceDTOS.forEach(invoice -> {
      String className = classCodeToNameMap.get(invoice.getClassCode());
      if (className == null) {
        throw new TveException(requestId,
            messageUtil.getMessage(MessageCode.MESSAGE_CLA_NOTFOUND));
      }
      invoice.setClassName(className);
    });
    return inVoiceDTOS;
  }

  @Override
  @Transactional
  public Page<InVoiceDTO> getInvoice(String requestId, String searchString,
      List<InvoiceStatus> filter,
      Pageable pageable) {
    Page<Invoice> invoicePage =
        StringUtils.hasText(searchString) ? invoiceRepository.searchInvoice(searchString, filter,
            pageable) : invoiceRepository.searchInvoiceWithoutSearchString(
            filter, pageable);
    List<InVoiceDTO> inVoiceDTOS = invoiceToDTOMapper.toListDto(invoicePage.getContent());
    List<String> classCodes = inVoiceDTOS.stream()
        .map(InVoiceDTO::getClassCode)
        .toList();

    Map<String, String> classCodeToNameMap = classTvmsRepository
        .findAllByCodeInAndIsDeleteFalse(classCodes)
        .stream()
        .collect(Collectors.toMap(ClassTvms::getCode, ClassTvms::getName));

    inVoiceDTOS.forEach(invoice -> {
      String className = classCodeToNameMap.get(invoice.getClassCode());
      if (className == null) {
        throw new TveException(requestId,
            messageUtil.getMessage(MessageCode.MESSAGE_CLA_NOTFOUND));
      }
      invoice.setClassName(className);
    });
    return new PageImpl<>(inVoiceDTOS, pageable, invoicePage.getTotalElements());

  }

  @Override
  public InVoiceDTO getInvoiceById(String requestId, int invoiceId) {
    Optional<Invoice> invoiceOptional = invoiceRepository.findById(invoiceId);
    if (invoiceOptional.isEmpty()) {
      throw new TveException(requestId,
          messageUtil.getMessage(MessageCode.MESSAGE_INVOICE_NOT_FOUND));
    }
    InVoiceDTO inVoiceDTO = invoiceToDTOMapper.toDto(invoiceOptional.get());

    Optional<ClassTvms> classTvms = classTvmsRepository.findByCodeAndIsDeleteFalse(
        inVoiceDTO.getClassCode());
    classTvms.ifPresent(tvms -> {
      inVoiceDTO.setClassName(tvms.getName());
      inVoiceDTO.setStaffName(tvms.getStaff().getFirstName() + " " + tvms.getStaff().getLastName());
    });

    return inVoiceDTO;
  }

  @Override
  @Transactional
  public InVoiceDTO updateInvoice(String requestId, InvoiceRequest invoiceRequest) {
    Optional<Invoice> invoiceOptional = invoiceRepository.findById(invoiceRequest.getInvoiceId());
    if (invoiceOptional.isEmpty()) {
      throw new TveException(requestId,
          messageUtil.getMessage(MessageCode.MESSAGE_INVOICE_NOT_FOUND));
    }
    Invoice entity = invoiceOptional.get();
    if (!ObjectUtils.isEmpty(invoiceRequest.getAmount())) {
      entity.setAmount(invoiceRequest.getAmount());
    }
    if (!ObjectUtils.isEmpty(invoiceRequest.getDescription())) {
      entity.setDescription(invoiceRequest.getDescription());
    }
    if (!ObjectUtils.isEmpty(invoiceRequest.getPaymentType())) {
      entity.setPaymentType(invoiceRequest.getPaymentType());
    }
    float tuitionOwed = 0f;
    if (!ObjectUtils.isEmpty(invoiceRequest.getInvoiceDiscount())) {
      if (invoiceRequest.getInvoiceDiscount() > entity.getTuitionOwed()) {
        var message = LogUtil.buildFormatLog(requestId, messageUtil
            .getMessage(MessageCode.MESSAGE_INVOICE_DISCOUNT_INVALID,
                invoiceRequest.getInvoiceId()));
        throw new TveException(MessageCode.MESSAGE_INVOICE_DISCOUNT_INVALID,
            message);
      } else {
        tuitionOwed = entity.getTuitionOwed() - invoiceRequest.getInvoiceDiscount();
        entity.setInvoiceDiscount(invoiceRequest.getInvoiceDiscount());
        entity.setTuitionOwed(tuitionOwed);
      }
    }
    if (entity.getAmount() < tuitionOwed) {
      entity.setInvoiceStatus(InvoiceStatus.PARTIALLY_PAID);
      float newTuitionOwed = tuitionOwed - entity.getAmount();
      Invoice newInvoice = Invoice.builder()
          .tuitionOwed(newTuitionOwed)
          .invoiceStatus(InvoiceStatus.NOT_PAID)
          .enrollment(entity.getEnrollment())
          .paymentType(PaymentTypeConstants.CASH)
          .amount(0f)
          .build();
      invoiceRepository.save(newInvoice);
    } else {
      entity.setInvoiceStatus(InvoiceStatus.FULLY_PAID);
    }

    invoiceRepository.save(entity);

    return invoiceToDTOMapper.toDto(entity);
  }
}
