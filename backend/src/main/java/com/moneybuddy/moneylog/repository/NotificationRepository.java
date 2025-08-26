package com.moneybuddy.moneylog.repository;

import com.moneybuddy.moneylog.domain.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
            SELECT n FROM Notification n
            WHERE n.userId = :userId
                AND n.createdAt >= :threshold
                AND (:cursor IS NULL OR n.id < :cursor)
            ORDER BY n.id DESC
    """)
    List<Notification> findList(
            @Param("userId") Long userId,
            @Param("threshold") LocalDateTime threshold,
            @Param("cursor") Long cursor,
            Pageable pageable
    );

    long countByUserIdAndIsReadFalseAndCreatedAtGreaterThanEqual(Long userId, LocalDateTime threshold);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.expiresAt < :now")
    int deleteExpired(@Param("now") LocalDateTime now);
}
