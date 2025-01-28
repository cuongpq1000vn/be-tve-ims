package vn.codezx.triviet.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import vn.codezx.triviet.dtos.staff.StaffDTO;
import vn.codezx.triviet.dtos.staff.request.StaffRequest;
import vn.codezx.triviet.entities.staff.Staff;

public interface StaffService {
	StaffDTO createStaff(String requestId, StaffRequest staffRequest, MultipartFile avatar);

	Page<StaffDTO> getStaffs(String requestId, String query, Pageable pageable);

	StaffDTO updateStaff(String requestId, Integer staffId, StaffRequest staffRequest,
			MultipartFile avatar);

	StaffDTO getStaff(String requestId, Integer staffId);

	StaffDTO deleteStaff(String requestId, Integer staffId);

	String storeAvatar(String requestId, Staff staff, MultipartFile file);
}
