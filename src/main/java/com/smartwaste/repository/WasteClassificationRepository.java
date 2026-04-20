package com.smartwaste.repository;

import com.smartwaste.model.WasteClassification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WasteClassificationRepository extends JpaRepository<WasteClassification, Long> {

    List<WasteClassification> findAllByOrderByClassifiedAtDesc();

    @Query("SELECT w.wasteType, COUNT(w) FROM WasteClassification w GROUP BY w.wasteType")
    List<Object[]> countByWasteType();

    List<WasteClassification> findByWasteTypeIgnoreCase(String wasteType);
}
