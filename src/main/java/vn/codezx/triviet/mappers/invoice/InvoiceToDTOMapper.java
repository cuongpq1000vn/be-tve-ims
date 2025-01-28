package vn.codezx.triviet.mappers.invoice;

import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import vn.codezx.triviet.dtos.invoice.InVoiceDTO;
import vn.codezx.triviet.entities.reporting.Invoice;
import vn.codezx.triviet.mappers.DtoMapper;

@Component
public class InvoiceToDTOMapper extends DtoMapper<Invoice, InVoiceDTO> {

  @Override
  public InVoiceDTO toDto(Invoice entity) {
    return InVoiceDTO.builder()
        .id(entity.getId())
        .classCode(entity.getEnrollment().getClassCode())
        .studentCode(entity.getEnrollment().getStudent().getCode())
        .studentName(entity.getEnrollment().getStudent().getName())
        .tuitionOwed(ObjectUtils.isEmpty(entity.getTuitionOwed()) ? 0f : entity.getTuitionOwed())
        .amount(ObjectUtils.isEmpty(entity.getAmount()) ? 0f : entity.getAmount())
        .invoiceDiscount(ObjectUtils.isEmpty(entity.getInvoiceDiscount()) ? 0f : entity.getInvoiceDiscount())
        .paymentType(entity.getPaymentType())
        .description(entity.getDescription())
        .invoiceStatus(entity.getInvoiceStatus())
        .createdAt(entity.getCreatedAt())
        .createdBy(entity.getCreatedBy())
        .updatedAt(entity.getUpdatedAt())
        .updatedBy(entity.getUpdatedBy())
        .isDelete(entity.getIsDelete())
        .studentNickName(entity.getEnrollment().getStudent().getNickname())
        .studentPhoneNumber(entity.getEnrollment().getStudent().getPhoneNumber())
        .paymentType(entity.getPaymentType())
        .build();
  }
}
