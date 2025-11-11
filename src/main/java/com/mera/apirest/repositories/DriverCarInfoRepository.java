package com.mera.apirest.repositories;

import com.mera.apirest.models.DriverCarInfo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DriverCarInfoRepository extends JpaRepository<DriverCarInfo, Long> {
}
