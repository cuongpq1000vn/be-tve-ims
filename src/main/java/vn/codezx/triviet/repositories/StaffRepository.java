package vn.codezx.triviet.repositories;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.codezx.triviet.entities.staff.Staff;

@Repository
public interface StaffRepository extends JpaRepository<Staff, Integer> {

  Staff findByCode(String code);


  Optional<Staff> findByIdAndIsDeleteFalse(Integer staffId);


  Page<Staff> findAllByIsDeleteIsFalse(Pageable pageable);


  Staff findByEmailAddressAndIsDeleteIsFalse(String email);


}
