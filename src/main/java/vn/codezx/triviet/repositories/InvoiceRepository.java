package vn.codezx.triviet.repositories;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.codezx.triviet.constants.InvoiceStatus;
import vn.codezx.triviet.entities.reporting.Invoice;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Integer> {

  @Query("SELECT i FROM Invoice i JOIN i.enrollment e WHERE e.student.code = :studentCode AND i.isDelete = false")
  List<Invoice> findByStudentCode(@Param("studentCode") String studentCode);

  @Query(value = "SELECT i FROM Invoice i JOIN i.enrollment e where e.student.name like %:searchString% and i.invoiceStatus IN :status and i.isDelete = false")
  Page<Invoice> searchInvoice(@Param("searchString") String searchString, @Param("status")
  List<InvoiceStatus> status, Pageable pageable);

  @Query(value = "SELECT i FROM Invoice i JOIN i.enrollment e where i.invoiceStatus IN :status and i.isDelete = false")
  Page<Invoice> searchInvoiceWithoutSearchString(@Param("status")
  List<InvoiceStatus> status, Pageable pageable);

}
