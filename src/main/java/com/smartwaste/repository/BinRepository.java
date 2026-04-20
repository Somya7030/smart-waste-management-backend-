package com.smartwaste.repository;

import com.smartwaste.model.Bin;
import com.smartwaste.model.BinStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BinRepository extends JpaRepository<Bin, Long> {

    Optional<Bin> findByBinCode(String binCode);

    List<Bin> findByStatus(BinStatus status);

    List<Bin> findByFillLevelGreaterThan(Double fillLevel);

    @Query("SELECT COUNT(b) FROM Bin b WHERE b.status = :status")
    long countByStatus(BinStatus status);

    @Query("SELECT AVG(b.fillLevel) FROM Bin b")
    Double averageFillLevel();

    @Query("SELECT b FROM Bin b WHERE b.fillLevel > 80 ORDER BY b.fillLevel DESC")
    List<Bin> findFullBinsOrderedByPriority();

    @Query("SELECT b FROM Bin b WHERE b.latitude BETWEEN :minLat AND :maxLat AND b.longitude BETWEEN :minLon AND :maxLon")
    List<Bin> findBinsInBoundingBox(double minLat, double maxLat, double minLon, double maxLon);

    boolean existsByBinCode(String binCode);
}
