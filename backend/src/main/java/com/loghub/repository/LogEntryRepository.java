package com.loghub.repository;

import com.loghub.domain.LogDomain;
import com.loghub.domain.LogEntry;
import com.loghub.dto.LevelCount;
import com.loghub.dto.LogSummary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LogEntryRepository extends JpaRepository<LogEntry, Long> {

    /** Constructor projection: the heavy payload column is never fetched for lists. */
    @Query("""
            select new com.loghub.dto.LogSummary(l.id, l.level, l.source, l.message, l.createdAt, length(l.payload))
            from LogEntry l
            where l.domain = :domain
            order by l.createdAt desc, l.id desc
            """)
    List<LogSummary> findSummaries(@Param("domain") LogDomain domain, Pageable pageable);

    Optional<LogEntry> findByIdAndDomain(Long id, LogDomain domain);

    @Query("""
            select new com.loghub.dto.LevelCount(l.level, count(l))
            from LogEntry l
            where l.domain = :domain
            group by l.level
            """)
    List<LevelCount> countByLevel(@Param("domain") LogDomain domain);
}
