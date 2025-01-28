package vn.codezx.triviet.services;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.codezx.triviet.constants.InvoiceStatus;
import vn.codezx.triviet.dtos.invoice.InVoiceDTO;
import vn.codezx.triviet.dtos.invoice.InvoiceRequest;

public interface InvoiceService {

  List<InVoiceDTO> getInvoiceByStudent(String requestId, String studentCode);

  Page<InVoiceDTO> getInvoice(String requestId, String searchString, List<InvoiceStatus> filter,
      Pageable pageable);

  InVoiceDTO getInvoiceById(String requestId, int invoiceId);

  InVoiceDTO updateInvoice(String requestId, InvoiceRequest invoiceRequest);
}
