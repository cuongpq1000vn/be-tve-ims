package vn.codezx.triviet.controllers.invoice;

import java.util.List;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.SortDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.codezx.triviet.constants.InvoiceStatus;
import vn.codezx.triviet.dtos.invoice.InVoiceDTO;
import vn.codezx.triviet.dtos.invoice.InvoiceRequest;
import vn.codezx.triviet.services.InvoiceService;

@RestController
@RequestMapping("/api/invoice")
public class InvoiceController {

  private final InvoiceService invoiceService;

  @Autowired
  public InvoiceController(InvoiceService invoiceService) {
    this.invoiceService = invoiceService;
  }

  @GetMapping("{request-id}/{student-code}/student")
  public ResponseEntity<List<InVoiceDTO>> getInvoiceByStudent(
      @PathVariable("request-id") String requestId,
      @PathVariable("student-code") String studentCode) {
    return ResponseEntity.ok(invoiceService.getInvoiceByStudent(requestId, studentCode));
  }

  @GetMapping("{request-id}/{id}")
  public ResponseEntity<InVoiceDTO> getInvoiceById(@PathVariable("request-id") String requestId,
      @PathVariable("id") int invoiceId) {
    return ResponseEntity.ok(invoiceService.getInvoiceById(requestId, invoiceId));
  }

  @GetMapping("{request-id}")
  public ResponseEntity<Page<InVoiceDTO>> getInvoice(@PathVariable("request-id") String requestId,
      @RequestParam(required = false) String searchString,
      @RequestParam(required = false) List<InvoiceStatus> filter,
      @SortDefault(sort = "createdAt", direction = Direction.DESC)
      @ParameterObject Pageable pageable) {
    return ResponseEntity.ok(invoiceService.getInvoice(requestId, searchString, filter, pageable));
  }

  @PutMapping("{request-id}")
  public ResponseEntity<InVoiceDTO> updateInvoice(@PathVariable("request-id") String requestId,
      @RequestBody InvoiceRequest invoiceRequest) {
    return ResponseEntity.ok(invoiceService.updateInvoice(requestId, invoiceRequest));
  }

}
